package ua.com.wrc.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * The gRPC server to check Web Resources.
 *
 * @author Igor Volnov
 */
public class WebResourceCheckerServer {
    private static final Logger logger = Logger.getLogger(WebResourceCheckerServer.class.getName());

    private final int port;

    private final Server server;

    public WebResourceCheckerServer(final int port) {
        this(port, ServerBuilder.forPort(port));
    }

    public WebResourceCheckerServer(final int port, final ServerBuilder<?> serverBuilder) {
        this.port = port;
        this.server = serverBuilder.addService(new WebResourceCheckerService()).build();
    }

    public void start() throws IOException {
        server.start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                WebResourceCheckerServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    public void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

}
