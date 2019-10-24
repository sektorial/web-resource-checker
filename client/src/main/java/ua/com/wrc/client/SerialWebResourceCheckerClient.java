package ua.com.wrc.client;

import ua.com.wrc.client.impl.WebResourceCheckerClientException;
import ua.com.wrc.proto.WebResourceResponseInfo;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for clients checking Web Resources in a single thread.
 *
 * @author Igor Volnov
 */
public interface SerialWebResourceCheckerClient extends WebResourceCheckerClient {
    Set<WebResourceResponseInfo> blockingSerialCallsToMultipleResources(Set<String> urlSet)
            throws WebResourceCheckerClientException;

    CompletableFuture<Set<WebResourceResponseInfo>> nonblockingSerialCallsToMultipleResources(Set<String> urlSet)
            throws WebResourceCheckerClientException;
}
