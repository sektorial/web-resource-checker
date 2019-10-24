package ua.com.wrc.client;

import ua.com.wrc.client.impl.WebResourceCheckerClientException;
import ua.com.wrc.proto.WebResourceResponseInfo;

/**
 * Base interface for clients checking Web Resources.
 *
 * @author Igor Volnov
 */
public interface WebResourceCheckerClient {

    /**
     * Gracefully shuts down the client.
     *
     * @throws InterruptedException
     */
    void shutdown() throws InterruptedException;

    /**
     * The simplest gRPC call to check a single resource.
     *
     * @param url The URL of the web resource
     * @return The instance of {@link WebResourceResponseInfo}
     * @throws WebResourceCheckerClientException
     */
    WebResourceResponseInfo blockingCallToSingleResource(String url) throws WebResourceCheckerClientException;
}
