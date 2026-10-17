package id.maetaruna.assessment.userapi.config;

import id.maetaruna.assessment.userapi.config.ClientProperties.Endpoint;
import java.net.http.HttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(ClientProperties.class)
public class RestClientConfig {

    // The Eureka client sends its own registry calls with the RestClient.Builder bean, so the
    // default builder must stay plain; only api-2 calls go through the load-balanced one.
    @Bean
    @Primary
    RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    RestClient postRestClient(@LoadBalanced RestClient.Builder loadBalanced, ClientProperties properties) {
        return restClient(loadBalanced, properties.posts());
    }

    @Bean
    RestClient todoRestClient(ClientProperties properties) {
        return restClient(properties.todos());
    }

    public static RestClient restClient(Endpoint endpoint) {
        return restClient(RestClient.builder(), endpoint);
    }

    private static RestClient restClient(RestClient.Builder builder, Endpoint endpoint) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(endpoint.connectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(endpoint.readTimeout());
        return builder.clone()
                .baseUrl(endpoint.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}
