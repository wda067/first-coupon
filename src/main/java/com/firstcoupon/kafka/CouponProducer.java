package com.firstcoupon.kafka;

import com.firstcoupon.domain.CouponIssuedEvent;
import com.firstcoupon.domain.CouponUsedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(String email, Long couponId) {
        kafkaTemplate.send("coupon-issued", new CouponIssuedEvent(email, couponId));
    }

    public void send(String email, String couponName) {
        kafkaTemplate.send("coupon-used", new CouponUsedEvent(email, couponName));
    }
}
