package ua.com.wrc.client;

import ua.com.wrc.client.impl.WebResourceCheckerClientException;
import ua.com.wrc.proto.WebResourceResponseInfo;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for clients checking Web Resources in multiple threads.
 *
 * @author Igor Volnov
 */
public interface ConcurrentWebResourceCheckerClient extends WebResourceCheckerClient {
    Set<WebResourceResponseInfo> blockingConcurrentCallsToMultipleResources(Set<String> urlSet)
            throws WebResourceCheckerClientException;

    CompletableFuture<Set<WebResourceResponseInfo>> nonblockingConcurrentCallsToMultipleResources(Set<String> urlSet)
            throws WebResourceCheckerClientException;
}
