package id.maetaruna.assessment.userapi.client;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

// Holds the service out of rotation until the todos warm-up has finished, whether or not it
// succeeded, so the first request through the gateway does not race it. An outage of the
// todos service itself never makes this service unhealthy.
@Component
public class TodosWarmUpHealthIndicator implements HealthIndicator {

    private final TodoClient todoClient;

    public TodosWarmUpHealthIndicator(TodoClient todoClient) {
        this.todoClient = todoClient;
    }

    @Override
    public Health health() {
        return todoClient.isWarmUpFinished() ? Health.up().build() : Health.outOfService().build();
    }
}
