package ua.com.wrc.it;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import ua.com.wrc.client.SerialWebResourceCheckerClient;
import ua.com.wrc.client.impl.SerialWebResourceCheckerClientImpl;
import ua.com.wrc.client.impl.WebResourceCheckerClientException;
import ua.com.wrc.proto.WebResourceResponseInfo;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RunWith(JUnitPlatform.class)
public class SerialWebResourceCheckerTest extends AbstractWebResourceCheckerTest<SerialWebResourceCheckerClient> {

    @Override
    protected SerialWebResourceCheckerClient provideClient(String host, int port) {
        return new SerialWebResourceCheckerClientImpl(host, port);
    }

    @Test
    public void testBlockingCallToSingleResource() throws WebResourceCheckerClientException {
        final String url = urls.stream().findAny().orElse("https://www.google.com");

        final WebResourceResponseInfo responseInfo = client.blockingCallToSingleResource(url);

        final SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(responseInfo.getResponseCode())
                .isGreaterThan(0);
        softAssertions.assertThat(responseInfo.getResponseTime())
                .isGreaterThan(0);
        softAssertions.assertThat(responseInfo.getUrl())
                .isEqualTo(url);
        softAssertions.assertAll();
    }

    @Test
    public void testBlockingSerialCallsToMultipleResources() throws WebResourceCheckerClientException {
        Set<WebResourceResponseInfo> responseInfos = client.blockingSerialCallsToMultipleResources(urls);

        assertResponseInfos(responseInfos, urls);
    }

    @Test
    public void testNonblockingSerialCallsToMultipleResources()
            throws ExecutionException, InterruptedException, WebResourceCheckerClientException {
        CompletableFuture<Set<WebResourceResponseInfo>> completableFuture =
                client.nonblockingSerialCallsToMultipleResources(urls);

        Set<WebResourceResponseInfo> responseInfos = completableFuture.get();

        assertResponseInfos(responseInfos, urls);
    }

}
