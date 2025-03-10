package com.firstcoupon.kafka;

import com.firstcoupon.domain.CouponIssuedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponProducer {

    private static final String TOPIC_NAME = "coupon-issued";

    private final KafkaTemplate<String, CouponIssuedEvent> kafkaTemplate;

    public void send(String email, Long couponId) {
        kafkaTemplate.send(TOPIC_NAME, new CouponIssuedEvent(email, couponId));
    }
}
