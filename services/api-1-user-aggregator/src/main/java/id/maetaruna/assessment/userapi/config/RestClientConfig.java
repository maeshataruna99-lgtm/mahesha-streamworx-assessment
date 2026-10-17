package id.maetaruna.assessment.userapi.config;

import id.maetaruna.assessment.userapi.config.ClientProperties.Endpoint;
import java.net.http.HttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ClientProperties.class)
public class RestClientConfig {

    @Bean
    RestClient postRestClient(ClientProperties properties) {
        return restClient(properties.posts());
    }

    @Bean
    RestClient todoRestClient(ClientProperties properties) {
        return restClient(properties.todos());
    }

    public static RestClient restClient(Endpoint endpoint) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(endpoint.connectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(endpoint.readTimeout());
        return RestClient.builder()
                .baseUrl(endpoint.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}
