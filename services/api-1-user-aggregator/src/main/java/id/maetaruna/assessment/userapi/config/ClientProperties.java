package id.maetaruna.assessment.userapi.config;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("clients")
public record ClientProperties(Endpoint posts, Endpoint todos) {

    public record Endpoint(URI baseUrl, Duration connectTimeout, Duration readTimeout) {
    }
}
