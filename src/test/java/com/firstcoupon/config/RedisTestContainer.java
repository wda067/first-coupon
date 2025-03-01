package com.firstcoupon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.FixedHostPortGenericContainer;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles("test")
@Configuration
public class RedisTestContainer {

    private static final FixedHostPortGenericContainer<?> REDIS_CONTAINER;
    private static final String REDIS_IMAGE = "redis:alpine";

    static {
        REDIS_CONTAINER = new FixedHostPortGenericContainer<>(DockerImageName.parse(REDIS_IMAGE).toString())
                .withFixedExposedPort(6380, 6379) // 호스트 6380 -> 컨테이너 6379
                .withCommand("redis-server", "--requirepass", "1234");
        REDIS_CONTAINER.start();
    }
}