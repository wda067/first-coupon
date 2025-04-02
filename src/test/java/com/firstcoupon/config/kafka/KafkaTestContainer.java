package com.firstcoupon.config.kafka;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
@Profile("test")
public class KafkaTestContainer {

    @Container
    @ServiceConnection
    private static final KafkaContainer KAFKA_CONTAINER;

    static {
        KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
                .withReuse(false);
        KAFKA_CONTAINER.start();
    }

    public static String getBootstrapServers() {
        return KAFKA_CONTAINER.getBootstrapServers();
    }
}
