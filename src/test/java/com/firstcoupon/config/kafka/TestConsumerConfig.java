package com.firstcoupon.config.kafka;

import static org.apache.kafka.clients.consumer.ConsumerConfig.*;
import static org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
import static org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG;
import static org.springframework.kafka.listener.ContainerProperties.AckMode.MANUAL_IMMEDIATE;
import static org.springframework.kafka.support.serializer.JsonDeserializer.*;
import static org.springframework.kafka.support.serializer.JsonDeserializer.TRUSTED_PACKAGES;

import com.firstcoupon.domain.CouponIssuedEvent;
import com.firstcoupon.domain.CouponUsedEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
@Profile("test")
public class TestConsumerConfig {

    @Bean
    public ConsumerFactory<String, CouponIssuedEvent> issuedConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(BOOTSTRAP_SERVERS_CONFIG, KafkaTestContainer.getBootstrapServers());
        config.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(TRUSTED_PACKAGES, "*");
        config.put(ENABLE_AUTO_COMMIT_CONFIG, "false");
        config.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(MAX_POLL_RECORDS_CONFIG, 1000);  //한 번에 가져올 레코드의 최대 개수
        config.put(FETCH_MIN_BYTES_CONFIG, 1024);  //한 번에 가져올 데이터의 크기
        config.put(FETCH_MAX_WAIT_MS_CONFIG, 2000);  //fetch.min.bytes보다 데이터 크기가 적은 경우 최대 대기 시간
        config.put(VALUE_DEFAULT_TYPE, CouponIssuedEvent.class.getName());

        return new DefaultKafkaConsumerFactory<>(config,
                new StringDeserializer(),
                new JsonDeserializer<>(CouponIssuedEvent.class));
    }

    @Bean
    public ConsumerFactory<String, CouponUsedEvent> usedConsumerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(BOOTSTRAP_SERVERS_CONFIG, KafkaTestContainer.getBootstrapServers());
        config.put(KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        config.put(TRUSTED_PACKAGES, "*");
        config.put(ENABLE_AUTO_COMMIT_CONFIG, "false");
        config.put(AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(VALUE_DEFAULT_TYPE, CouponUsedEvent.class.getName());

        return new DefaultKafkaConsumerFactory<>(config,
                new StringDeserializer(),
                new JsonDeserializer<>(CouponUsedEvent.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CouponIssuedEvent> issuedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CouponIssuedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(issuedConsumerFactory());
        factory.getContainerProperties().setAckMode(MANUAL_IMMEDIATE);
        factory.getContainerProperties().setAsyncAcks(true);
        factory.setConcurrency(3);
        factory.setBatchListener(true);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, CouponUsedEvent> usedKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, CouponUsedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(usedConsumerFactory());
        factory.getContainerProperties().setAckMode(MANUAL_IMMEDIATE);
        factory.getContainerProperties().setAsyncAcks(true);
        factory.setConcurrency(3);
        return factory;
    }
}
