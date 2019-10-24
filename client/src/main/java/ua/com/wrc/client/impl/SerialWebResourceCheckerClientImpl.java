package ua.com.wrc.client.impl;

import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import ua.com.wrc.client.SerialWebResourceCheckerClient;
import ua.com.wrc.proto.WebResourceLocator;
import ua.com.wrc.proto.WebResourceLocators;
import ua.com.wrc.proto.WebResourceResponseInfo;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Implementation of {@link SerialWebResourceCheckerClient}.
 *
 * @author Igor Volnov
 */
public class SerialWebResourceCheckerClientImpl extends AbstractWebResourceCheckerClient
        implements SerialWebResourceCheckerClient {
    private static final Logger LOG = Logger.getLogger(SerialWebResourceCheckerClientImpl.class.getName());

    /**
     * Construct client for accessing RouteGuide server at {@code host:port}.
     */
    public SerialWebResourceCheckerClientImpl(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    }

    public SerialWebResourceCheckerClientImpl(ManagedChannelBuilder<?> channelBuilder) {
        super(channelBuilder, Executors.newSingleThreadExecutor());
    }


    @Override
    public Set<WebResourceResponseInfo> blockingSerialCallsToMultipleResources(final Set<String> urlSet)
            throws WebResourceCheckerClientException {
        final WebResourceLocators request = WebResourceLocators.newBuilder()
                .addAllLocators(
                        urlSet.stream()
                                .map(
                                        url -> WebResourceLocator.newBuilder().setUrl(url).build()
                                ).collect(Collectors.toSet())
                ).build();

        Iterator<WebResourceResponseInfo> responseInfoIterator;

        try {
            responseInfoIterator = blockingStub.checkMultipleWebResources(request);
        } catch (StatusRuntimeException e) {
            LOG.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            throw new WebResourceCheckerClientException(e);
        }

        final Iterable<WebResourceResponseInfo> iterable = () -> responseInfoIterator;

        return StreamSupport.stream(iterable.spliterator(), false)
                .peek(this::logResponseInfo)
                .collect(Collectors.toSet());
    }


    @Override
    public CompletableFuture<Set<WebResourceResponseInfo>> nonblockingSerialCallsToMultipleResources(
            final Set<String> urlSet) throws WebResourceCheckerClientException {
        return nonblockingCallsToMultipleResources(urlSet, executorService);
    }

    @Override
    protected Logger getLog() {
        return LOG;
    }
}
