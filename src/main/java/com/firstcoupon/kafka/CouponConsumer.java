package com.firstcoupon.kafka;

import com.firstcoupon.domain.Coupon;
import com.firstcoupon.domain.CouponIssuedEvent;
import com.firstcoupon.domain.CouponUsedEvent;
import com.firstcoupon.domain.IssuedCoupon;
import com.firstcoupon.exception.CouponNotFound;
import com.firstcoupon.repository.CouponRepository;
import com.firstcoupon.repository.IssuedCouponRepository;
import com.firstcoupon.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("!test")
public class CouponConsumer {

    private static final Logger couponLogger = LoggerFactory.getLogger("CouponLogger");

    private final IssuedCouponRepository issuedCouponRepository;
    private final CouponRepository couponRepository;
    private final EmailService emailService;

    @KafkaListener(topics = "coupon-issued", groupId = "coupon-issued-group", containerFactory = "issuedKafkaListenerContainerFactory")
    public void consumeCouponIssuedEvent(CouponIssuedEvent event, Acknowledgment ack) {
        Coupon coupon = couponRepository.findById(event.getCouponId())
                .orElseThrow(CouponNotFound::new);
        IssuedCoupon issuedCoupon = IssuedCoupon.issue(event.getEmail(), coupon);
        issuedCouponRepository.save(issuedCoupon);
        ack.acknowledge();
        couponLogger.info("쿠폰 발급됨 - 코드: {}, 사용자: {}", coupon.getCode(), event.getEmail());
    }

    @KafkaListener(topics = "coupon-used", groupId = "coupon-used-group", containerFactory = "usedKafkaListenerContainerFactory")
    public void consumeCouponIssuedEvent(CouponUsedEvent event, Acknowledgment ack) {
        emailService.sendCouponUsedEmail(event.getEmail(), event.getCouponName());
        ack.acknowledge();
    }
}
