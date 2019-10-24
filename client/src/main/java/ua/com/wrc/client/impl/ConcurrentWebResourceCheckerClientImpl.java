package ua.com.wrc.client.impl;

import io.grpc.ManagedChannelBuilder;
import ua.com.wrc.client.ConcurrentWebResourceCheckerClient;
import ua.com.wrc.proto.WebResourceResponseInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ConcurrentWebResourceCheckerClient}.
 *
 * @author Igor Volnov
 */
public class ConcurrentWebResourceCheckerClientImpl extends AbstractWebResourceCheckerClient implements
        ConcurrentWebResourceCheckerClient {
    private static final Logger LOG = Logger.getLogger(ConcurrentWebResourceCheckerClientImpl.class.getName());

    public ConcurrentWebResourceCheckerClientImpl(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    }

    public ConcurrentWebResourceCheckerClientImpl(ManagedChannelBuilder<?> channelBuilder) {
        super(channelBuilder, Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }


    @Override
    public Set<WebResourceResponseInfo> blockingConcurrentCallsToMultipleResources(final Set<String> urlSet)
            throws WebResourceCheckerClientException {
        Set<Callable<WebResourceResponseInfo>> callables = urlSet.stream().map(url ->
                (Callable<WebResourceResponseInfo>) () -> ConcurrentWebResourceCheckerClientImpl.this
                        .blockingCallToSingleResource(url)
        ).collect(Collectors.toSet());

        final List<Future<WebResourceResponseInfo>> callableResponses;
        try {
            callableResponses = executorService.invokeAll(callables);
        } catch (InterruptedException e) {
            throw new WebResourceCheckerClientException(e);
        }

        final Set<WebResourceResponseInfo> responseInfos = new HashSet<>();

        for (Future<WebResourceResponseInfo> future : callableResponses) {
            try {
                responseInfos.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new WebResourceCheckerClientException(e);
            }
        }

        return responseInfos;
    }


    @Override
    public CompletableFuture<Set<WebResourceResponseInfo>> nonblockingConcurrentCallsToMultipleResources(
            final Set<String> urlSet) throws WebResourceCheckerClientException {
        return nonblockingCallsToMultipleResources(urlSet, executorService);
    }

    @Override
    protected Logger getLog() {
        return LOG;
    }


//    /**
//     * /** Issues several different requests and then exits.
//     */
//    public static void main(String[] args) throws InterruptedException {
//        WebResourceCheckerClientImpl client = new WebResourceCheckerClientImpl("localhost", 8980);
//        try {
//            // Looking for a valid feature
//            client.blockingCallToSingleResource("https://www.google.com/");
//        } finally {
//            client.shutdown();
//        }
//    }


}
