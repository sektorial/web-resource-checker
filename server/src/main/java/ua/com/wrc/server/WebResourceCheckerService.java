package ua.com.wrc.server;

import com.google.common.base.Stopwatch;
import io.grpc.stub.StreamObserver;
import ua.com.wrc.proto.WebResourceCheckerGrpc;
import ua.com.wrc.proto.WebResourceLocator;
import ua.com.wrc.proto.WebResourceLocators;
import ua.com.wrc.proto.WebResourceResponseInfo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service that provides logic to check Web Resources.
 *
 * @author Igor Volnov
 */
public class WebResourceCheckerService extends WebResourceCheckerGrpc.WebResourceCheckerImplBase {

    private static final Logger LOG = Logger.getLogger(WebResourceCheckerService.class.getName());
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @Override
    public void checkSingleWebResource(
            final WebResourceLocator request, final StreamObserver<WebResourceResponseInfo> responseObserver
    ) {
        checkWebResources(Collections.singletonList(request), responseObserver);
    }


    @Override
    public void checkMultipleWebResources(
            final WebResourceLocators request, final StreamObserver<WebResourceResponseInfo> responseObserver
    ) {
        checkWebResources(request.getLocatorsList(), responseObserver);
    }

    @Override
    public StreamObserver<WebResourceLocator> checkMultipleWebResourcesAsync(
            final StreamObserver<WebResourceResponseInfo> responseObserver
    ) {
        return new StreamObserver<>() {

            @Override
            public void onNext(final WebResourceLocator locator) {
                if (locator == null) {
                    return;
                }

                try {
                    responseObserver.onNext(retrieveWebResourceResponseInfo(locator.getUrl()));
                } catch (IOException | InterruptedException e) {
                    LOG.log(Level.SEVERE, "Failed to access Web Resource at {0}.", locator.getUrl());
                    responseObserver.onError(e);
                }
            }

            @Override
            public void onError(final Throwable t) {
                LOG.log(Level.SEVERE, "checkMultipleWebResourcesAsync cancelled");
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }

    private void checkWebResources(
            final List<WebResourceLocator> locatorsList, final StreamObserver<WebResourceResponseInfo> responseObserver
    ) {
        for (WebResourceLocator locator : locatorsList) {
            try {
                WebResourceResponseInfo responseInfo = retrieveWebResourceResponseInfo(locator.getUrl());

                responseObserver.onNext(responseInfo);
            } catch (IOException | InterruptedException e) {
                LOG.log(Level.SEVERE, "Failed to access Web Resource at {0}.", locator.getUrl());
                responseObserver.onError(e);
                return;
            }
        }
        responseObserver.onCompleted();
    }


    private WebResourceResponseInfo retrieveWebResourceResponseInfo(final String url)
            throws IOException, InterruptedException {
        final HttpRequest httpRequest = HttpRequest.newBuilder().uri(URI.create(url)).build();

        final Stopwatch stopwatch = Stopwatch.createStarted();
        final HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        stopwatch.stop();

        final long elapsedTimeInMillis = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        final int responseStatusCode = httpResponse.statusCode();

        return WebResourceResponseInfo.newBuilder()
                .setResponseCode(responseStatusCode)
                .setResponseTime(elapsedTimeInMillis)
                .setUrl(url)
                .build();
    }
}
