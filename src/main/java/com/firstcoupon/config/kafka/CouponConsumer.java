package com.firstcoupon.config.kafka;

import com.firstcoupon.domain.Coupon;
import com.firstcoupon.domain.CouponIssuedEvent;
import com.firstcoupon.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class CouponConsumer {

    private final CouponRepository couponRepository;

    @KafkaListener(topics = "coupon-issued", groupId = "coupon-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(CouponIssuedEvent event) {
        couponRepository.save(new Coupon(event.getUserId()));
    }
}
