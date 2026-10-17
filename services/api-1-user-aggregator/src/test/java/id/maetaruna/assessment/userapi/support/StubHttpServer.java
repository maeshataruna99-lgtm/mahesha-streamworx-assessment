package id.maetaruna.assessment.userapi.support;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public final class StubHttpServer implements AutoCloseable {

    private record Behaviour(int status, String contentType, String body, Duration delayBeforeHeaders,
            Duration stallAfterHeaders) {
    }

    private final HttpServer server;
    private final AtomicInteger requests = new AtomicInteger();
    private volatile Behaviour behaviour;

    private StubHttpServer(String path, Behaviour behaviour) {
        this.behaviour = behaviour;
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        server.createContext(path, this::handle);
        server.start();
    }

    public static StubHttpServer respond(String path, int status, String contentType, String body) {
        return respond(path, status, contentType, body, Duration.ZERO);
    }

    public static StubHttpServer respond(String path, int status, String contentType, String body, Duration delay) {
        return new StubHttpServer(path, new Behaviour(status, contentType, body, delay, Duration.ZERO));
    }

    public static StubHttpServer stallAfterHeaders(String path, String contentType, String body, Duration stall) {
        return new StubHttpServer(path, new Behaviour(200, contentType, body, Duration.ZERO, stall));
    }

    public void respondWith(int status, String contentType, String body) {
        behaviour = new Behaviour(status, contentType, body, Duration.ZERO, Duration.ZERO);
    }

    public int requestCount() {
        return requests.get();
    }

    public URI baseUrl() {
        return URI.create("http://localhost:" + server.getAddress().getPort());
    }

    @Override
    public void close() {
        server.stop(0);
    }

    private void handle(HttpExchange exchange) throws IOException {
        requests.incrementAndGet();
        Behaviour current = behaviour;
        sleep(current.delayBeforeHeaders());
        byte[] bytes = current.body().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", current.contentType());
        exchange.sendResponseHeaders(current.status(), bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            int half = bytes.length / 2;
            out.write(bytes, 0, half);
            out.flush();
            sleep(current.stallAfterHeaders());
            out.write(bytes, half, bytes.length - half);
        } catch (IOException ignored) {
            // the client may already have given up after its timeout
        }
    }

    private static void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
