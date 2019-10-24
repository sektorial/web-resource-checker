package ua.com.wrc.it;

import org.junit.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import ua.com.wrc.client.ConcurrentWebResourceCheckerClient;
import ua.com.wrc.client.impl.ConcurrentWebResourceCheckerClientImpl;
import ua.com.wrc.client.impl.WebResourceCheckerClientException;
import ua.com.wrc.proto.WebResourceResponseInfo;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RunWith(JUnitPlatform.class)
public class ConcurrentWebResourceCheckerTest extends
        AbstractWebResourceCheckerTest<ConcurrentWebResourceCheckerClient> {

    @Override
    protected ConcurrentWebResourceCheckerClient provideClient(String host, int port) {
        return new ConcurrentWebResourceCheckerClientImpl(host, port);
    }

    @Test
    public void testBlockingConcurrentCallsToMultipleResources() throws WebResourceCheckerClientException {
        Set<WebResourceResponseInfo> webResourceResponseInfos = client.blockingConcurrentCallsToMultipleResources(urls);

        assertResponseInfos(webResourceResponseInfos, urls);
    }

    @Test
    public void testNonblockingConcurrentCallsToMultipleResources()
            throws ExecutionException, InterruptedException, WebResourceCheckerClientException {
        CompletableFuture<Set<WebResourceResponseInfo>> completableFuture =
                client.nonblockingConcurrentCallsToMultipleResources(urls);

        Set<WebResourceResponseInfo> responseInfos = completableFuture.get();

        assertResponseInfos(responseInfos, urls);
    }

}
