package id.maetaruna.assessment.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class GatewayDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayDiscoveryApplication.class, args);
    }
}
