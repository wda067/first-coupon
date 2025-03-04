package com.firstcoupon.kafka;

import com.firstcoupon.domain.Coupon;
import com.firstcoupon.domain.CouponIssuedEvent;
import com.firstcoupon.domain.IssuedCoupon;
import com.firstcoupon.exception.CouponNotFound;
import com.firstcoupon.repository.CouponRepository;
import com.firstcoupon.repository.IssuedCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class CouponConsumer {

    private final IssuedCouponRepository issuedCouponRepository;
    private final CouponRepository couponRepository;

    @KafkaListener(topics = "coupon-issued", groupId = "coupon-group", containerFactory = "kafkaListenerContainerFactory")
    public void consume(CouponIssuedEvent event) {
        Coupon coupon = couponRepository.findById(event.getCouponId())
                .orElseThrow(CouponNotFound::new);
        IssuedCoupon issuedCoupon = IssuedCoupon.issue(event.getUserId(), coupon);
        issuedCouponRepository.save(issuedCoupon);
    }
}
