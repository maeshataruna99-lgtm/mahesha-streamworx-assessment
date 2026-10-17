package id.maetaruna.assessment.postapi;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

@TestConfiguration(proxyBeanMethods = false)
public class PostgresContainerConfig {

    // Uses the same init scripts as docker compose, so tests fail if the schema or seed drifts.
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:18.6-alpine")
                .withCopyFileToContainer(
                        MountableFile.forHostPath("../../infra/postgres/init"),
                        "/docker-entrypoint-initdb.d");
    }
}
