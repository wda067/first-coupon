package com.firstcoupon.config.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.images.PullPolicy;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@Configuration
@Profile("test")
public class KafkaTestContainer {

    private static final KafkaContainer KAFKA_CONTAINER;

    static {
        KAFKA_CONTAINER = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
                .withImagePullPolicy(PullPolicy.alwaysPull());
        KAFKA_CONTAINER.start();
        System.setProperty("KAFKA_BOOTSTRAP_SERVERS", KAFKA_CONTAINER.getBootstrapServers());
        System.setProperty("spring.kafka.bootstrap-servers", KAFKA_CONTAINER.getBootstrapServers());
    }

    public static String getBootstrapServers() {
        return KAFKA_CONTAINER.getBootstrapServers();
    }
}
