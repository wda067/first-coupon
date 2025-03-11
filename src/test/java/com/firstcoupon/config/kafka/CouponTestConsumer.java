package com.firstcoupon.config.kafka;

import com.firstcoupon.domain.Coupon;
import com.firstcoupon.domain.CouponIssuedEvent;
import com.firstcoupon.domain.CouponUsedEvent;
import com.firstcoupon.domain.IssuedCoupon;
import com.firstcoupon.exception.CouponNotFound;
import com.firstcoupon.repository.CouponRepository;
import com.firstcoupon.repository.IssuedCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("test")
public class CouponTestConsumer {

    private final IssuedCouponRepository issuedCouponRepository;
    private final CouponRepository couponRepository;

    @KafkaListener(topics = "coupon-issued", groupId = "coupon-issued-group", containerFactory = "issuedKafkaListenerContainerFactory")
    public void consumeCouponIssuedEvent(CouponIssuedEvent event, Acknowledgment ack) {
        Coupon coupon = couponRepository.findById(event.getCouponId())
                .orElseThrow(CouponNotFound::new);
        coupon.decrementQuantity();  //쿠폰 재고 감소
        IssuedCoupon issuedCoupon = IssuedCoupon.issue(event.getEmail(), coupon);  //쿠폰 발급

        couponRepository.save(coupon);
        issuedCouponRepository.save(issuedCoupon);

        ack.acknowledge();  //수동 비동기 커밋
    }

    @KafkaListener(topics = "coupon-used", groupId = "coupon-used-group", containerFactory = "usedKafkaListenerContainerFactory")
    public void consumeCouponIssuedEvent(CouponUsedEvent event, Acknowledgment ack) {
        //emailService.sendCouponUsedEmail(event.getEmail(), event.getCouponName());
        ack.acknowledge();
    }
}
