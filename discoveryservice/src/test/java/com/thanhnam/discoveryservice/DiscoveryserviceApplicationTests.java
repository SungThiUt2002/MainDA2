package com.thanhnam.discoveryservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@SpringBootTest(properties = {
    "eureka.client.enabled=false",
    "spring.cloud.service-registry.auto-registration.enabled=false"
})
@Import(DiscoveryserviceApplicationTests.NoEurekaServerConfig.class)
class DiscoveryserviceApplicationTests {

    @Test
    void contextLoads() {
    }

    @Configuration
    static class NoEurekaServerConfig {
        // Không cấu hình gì cả để tránh EurekaServerAutoConfiguration
    }
}
