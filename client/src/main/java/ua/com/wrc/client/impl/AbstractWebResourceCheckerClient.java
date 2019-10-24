package ua.com.wrc.client.impl;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import ua.com.wrc.client.WebResourceCheckerClient;
import ua.com.wrc.proto.WebResourceCheckerGrpc;
import ua.com.wrc.proto.WebResourceLocator;
import ua.com.wrc.proto.WebResourceResponseInfo;

import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * The abstract gRPC client with common logic to check Web Resources.
 *
 * @author Igor Volnov
 */
public abstract class AbstractWebResourceCheckerClient implements WebResourceCheckerClient {
    private final ManagedChannel channel;
    protected final WebResourceCheckerGrpc.WebResourceCheckerBlockingStub blockingStub;
    protected final WebResourceCheckerGrpc.WebResourceCheckerStub asyncStub;
    protected final ExecutorService executorService;


//    /**
//     * Construct client for accessing RouteGuide server at {@code host:port}.
//     */
//    public AbstractWebResourceCheckerClient(String host, int port) {
//        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
//    }

    /**
     * Construct client for accessing RouteGuide server using the existing channel.
     */
    protected AbstractWebResourceCheckerClient(
            final ManagedChannelBuilder<?> channelBuilder, final ExecutorService executorService
    ) {
        this.channel = channelBuilder.build();
        this.blockingStub = WebResourceCheckerGrpc.newBlockingStub(channel);
        this.asyncStub = WebResourceCheckerGrpc.newStub(channel);
        this.executorService = executorService;
    }

    /**
     * Gets a {@link Logger} of a particular client implementation.
     *
     * @return The {@link Logger}
     */
    protected abstract Logger getLog();

    @Override
    public void shutdown() throws InterruptedException {
        executorService.shutdownNow();
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    @Override
    public WebResourceResponseInfo blockingCallToSingleResource(final String url) throws
            WebResourceCheckerClientException {
        WebResourceLocator request = WebResourceLocator.newBuilder().setUrl(url).build();

        WebResourceResponseInfo responseInfo;

        try {
            responseInfo = blockingStub.checkSingleWebResource(request);
        } catch (StatusRuntimeException e) {
            getLog().log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            throw new WebResourceCheckerClientException(e);
        }

        logResponseInfo(responseInfo);

        return responseInfo;
    }

    protected CompletableFuture<Set<WebResourceResponseInfo>> nonblockingCallsToMultipleResources(
            final Set<String> urlSet, final ExecutorService executorService
    ) throws WebResourceCheckerClientException {
        final CompletableFuture<Set<WebResourceResponseInfo>> completableFuture = new CompletableFuture<>();

        StreamObserver<WebResourceResponseInfo> responseObserver = new StreamObserver<>() {
            final Set<WebResourceResponseInfo> responses = new CopyOnWriteArraySet<>();

            @Override
            public void onNext(WebResourceResponseInfo value) {
                responses.add(value);
            }

            @Override
            public void onError(Throwable t) {
                completableFuture.completeExceptionally(t);
            }

            @Override
            public void onCompleted() {
                completableFuture.complete(responses);
            }
        };

        Set<WebResourceLocator> webResourceLocators = urlSet.stream()
                .map(url -> WebResourceLocator.newBuilder().setUrl(url).build())
                .collect(Collectors.toSet());


        StreamObserver<WebResourceLocator> requestObserver =
                asyncStub.checkMultipleWebResourcesAsync(responseObserver);

        try {
            Set<Callable<Void>> callables = webResourceLocators.stream().map(locator ->
                    (Callable<Void>) () -> {
                        requestObserver.onNext(locator);
                        return null;
                    }
            ).collect(Collectors.toSet());

            executorService.invokeAll(callables);
        } catch (RuntimeException | InterruptedException e) {
            requestObserver.onError(e);
            throw new WebResourceCheckerClientException(e);
        }

        // Mark the end of requests
        requestObserver.onCompleted();

        return completableFuture;
    }


    protected void logResponseInfo(WebResourceResponseInfo responseInfo) {
        info("Web resource at {0} responded with {1} status code in {2} millis.", responseInfo.getUrl(),
                responseInfo.getResponseCode(), responseInfo.getResponseTime());
    }

    protected void info(String msg, Object... params) {
        getLog().log(Level.INFO, msg, params);
    }
}
