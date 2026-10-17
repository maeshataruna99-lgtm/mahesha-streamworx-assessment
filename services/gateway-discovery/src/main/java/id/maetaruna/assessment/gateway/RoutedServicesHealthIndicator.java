package id.maetaruna.assessment.gateway;

import java.net.URI;
import java.util.List;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.server.mvc.config.GatewayMvcProperties;
import org.springframework.cloud.gateway.server.mvc.config.RouteProperties;
import org.springframework.stereotype.Component;

// The gateway's only job is to route to these services, so it reports ready once every lb:// route
// target is visible to the discovery client the load balancer uses. Compose waits on this, which
// keeps the first request after `docker compose up` from racing Eureka registration.
@Component
public class RoutedServicesHealthIndicator implements HealthIndicator {

    private final DiscoveryClient discoveryClient;
    private final List<String> routedServices;

    public RoutedServicesHealthIndicator(DiscoveryClient discoveryClient, GatewayMvcProperties gatewayProperties) {
        this.discoveryClient = discoveryClient;
        this.routedServices = gatewayProperties.getRoutes().stream()
                .map(RouteProperties::getUri)
                .filter(uri -> "lb".equals(uri.getScheme()))
                .map(URI::getHost)
                .distinct()
                .toList();
    }

    @Override
    public Health health() {
        List<String> missing = routedServices.stream()
                .filter(service -> discoveryClient.getInstances(service).isEmpty())
                .toList();
        if (missing.isEmpty()) {
            return Health.up().withDetail("services", routedServices).build();
        }
        return Health.outOfService().withDetail("waitingFor", missing).build();
    }
}
