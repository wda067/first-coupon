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
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class CouponConsumer {

    private static final Logger couponLogger = LoggerFactory.getLogger("CouponLogger");

    private final IssuedCouponRepository issuedCouponRepository;
    private final CouponRepository couponRepository;
    private final EmailService emailService;

    @KafkaListener(topics = "coupon-issued", groupId = "coupon-issued-group", containerFactory = "issuedKafkaListenerContainerFactory")
    @Transactional
    public void consumeCouponIssuedEvent(CouponIssuedEvent event, Acknowledgment ack) {
        Long couponId = event.getCouponId();
        couponRepository.decrementQuantity(couponId);  //JPQL 쿠폰 재고 감소
        Coupon coupon = couponRepository.findById(couponId)  //쿠폰 조회
                .orElseThrow(CouponNotFound::new);

        //쿠폰 발급
        IssuedCoupon issuedCoupon = IssuedCoupon.issue(event.getEmail(), coupon);
        issuedCouponRepository.save(issuedCoupon);

        ack.acknowledge();  //수동 동기 커밋
        couponLogger.info("쿠폰 남은 재고: {}", coupon.getRemainingQuantity());
        couponLogger.info("쿠폰 발급됨 - 코드: {}, 사용자: {}", coupon.getCode(), event.getEmail());
    }

    @KafkaListener(topics = "coupon-used", groupId = "coupon-used-group", containerFactory = "usedKafkaListenerContainerFactory")
    public void consumeCouponIssuedEvent(CouponUsedEvent event, Acknowledgment ack) {
        emailService.sendCouponUsedEmail(event.getEmail(), event.getCouponName());
        ack.acknowledge();
    }
}
