package ua.com.wrc.it;

import io.grpc.ServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import org.assertj.core.api.SoftAssertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import ua.com.wrc.client.WebResourceCheckerClient;
import ua.com.wrc.proto.WebResourceResponseInfo;
import ua.com.wrc.server.WebResourceCheckerServer;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class AbstractWebResourceCheckerTest<T extends WebResourceCheckerClient> {
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();

    private WebResourceCheckerServer server;
    private ExecutorService serverExecutor;
    protected T client;
    protected Set<String> urls;

    @Before
    public void setUp() throws Exception {
        int port = 9999;

        serverExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        server = new WebResourceCheckerServer(port, ServerBuilder.forPort(port).executor(serverExecutor));
        server.start();

        client = provideClient("localhost", port);

        urls = new HashSet<>(Files.readAllLines(Paths.get("src", "test", "resources", "web_resources.txt")));
    }


    @After
    public void tearDown() throws InterruptedException {
        server.stop();
        serverExecutor.shutdownNow();
        client.shutdown();
    }

    protected abstract T provideClient(String host, int port);

    protected void assertResponseInfos(Set<WebResourceResponseInfo> actualResponseInfos, Set<String> expectedUrls) {
        final SoftAssertions softAssertions = new SoftAssertions();

        softAssertions.assertThat(actualResponseInfos.size())
                .isEqualTo(expectedUrls.size());

        actualResponseInfos.forEach(responseInfo -> {
                    softAssertions.assertThat(responseInfo.getResponseCode())
                            .isGreaterThan(0);
                    softAssertions.assertThat(responseInfo.getResponseTime())
                            .isGreaterThan(0);
                    softAssertions.assertThat(responseInfo.getUrl())
                            .isIn(expectedUrls);
                }
        );

        softAssertions.assertAll();
    }
}
