package id.maetaruna.assessment.userapi.support;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class StubHttpServer implements AutoCloseable {

    private final HttpServer server;

    private StubHttpServer(HttpServer server) {
        this.server = server;
    }

    public static StubHttpServer respond(String path, int status, String contentType, String body) {
        return respond(path, status, contentType, body, Duration.ZERO);
    }

    public static StubHttpServer respond(String path, int status, String contentType, String body, Duration delay) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
            server.createContext(path, exchange -> {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(status, bytes.length);
                try (OutputStream out = exchange.getResponseBody()) {
                    out.write(bytes);
                } catch (IOException ignored) {
                    // the client may already have given up after its read timeout
                }
            });
            server.start();
            return new StubHttpServer(server);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public URI baseUrl() {
        return URI.create("http://localhost:" + server.getAddress().getPort());
    }

    @Override
    public void close() {
        server.stop(0);
    }
}
