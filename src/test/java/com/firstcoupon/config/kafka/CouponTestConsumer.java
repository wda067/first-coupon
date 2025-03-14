package com.firstcoupon.config.kafka;

import com.firstcoupon.domain.Coupon;
import com.firstcoupon.domain.CouponIssuedEvent;
import com.firstcoupon.domain.CouponStatus;
import com.firstcoupon.domain.CouponUsedEvent;
import com.firstcoupon.domain.IssuedCoupon;
import com.firstcoupon.exception.CouponNotFound;
import com.firstcoupon.repository.CouponRepository;
import com.firstcoupon.repository.IssuedCouponRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("test")
public class CouponTestConsumer {

    private final IssuedCouponRepository issuedCouponRepository;
    private final CouponRepository couponRepository;
    private final DataSource dataSource;

    //@KafkaListener(topics = "coupon-issued", groupId = "test-group-${random.uuid}", containerFactory = "issuedKafkaListenerContainerFactory")
    //public void consumeCouponIssuedEvent(CouponIssuedEvent event, Acknowledgment ack) {
    //    Coupon coupon = couponRepository.findById(event.getCouponId())
    //            .orElseThrow(CouponNotFound::new);
    //    coupon.decrementQuantity();  //쿠폰 재고 감소
    //    IssuedCoupon issuedCoupon = IssuedCoupon.issue(event.getEmail(), coupon);  //쿠폰 발급
    //
    //    couponRepository.save(coupon);
    //    issuedCouponRepository.save(issuedCoupon);
    //
    //    ack.acknowledge();  //수동 비동기 커밋
    //}

    //@KafkaListener(topics = "coupon-issued", groupId = "test-group-${random.uuid}", containerFactory = "issuedKafkaListenerContainerFactory")
    //public void consumeCouponIssuedEvent(List<CouponIssuedEvent> events, Acknowledgment ack) {
    //    //이벤트 리스트 처리
    //    List<Coupon> couponsToUpdate = new ArrayList<>();
    //    List<IssuedCoupon> issuedCouponsToSave = new ArrayList<>();
    //
    //    for (CouponIssuedEvent event : events) {
    //        Coupon coupon = couponRepository.findById(event.getCouponId())
    //                .orElseThrow(CouponNotFound::new);
    //        coupon.decrementQuantity();
    //        IssuedCoupon issuedCoupon = IssuedCoupon.issue(event.getEmail(), coupon);
    //
    //        couponsToUpdate.add(coupon);
    //        issuedCouponsToSave.add(issuedCoupon);
    //    }
    //
    //    //벌크 저장
    //    couponRepository.saveAll(couponsToUpdate);
    //    issuedCouponRepository.saveAll(issuedCouponsToSave);
    //
    //    ack.acknowledge();  //배치 단위로 커밋
    //}

    @PersistenceContext
    private EntityManager em;

    @KafkaListener(topics = "coupon-issued", groupId = "test-group-${random.uuid}", containerFactory = "issuedKafkaListenerContainerFactory")
    public void consumeCouponIssuedEvent(List<CouponIssuedEvent> events, Acknowledgment ack) {
        List<Coupon> couponsToUpdate = new ArrayList<>();
        List<IssuedCoupon> issuedCouponsToSave = new ArrayList<>();

        for (CouponIssuedEvent event : events) {
            Coupon coupon = couponRepository.findById(event.getCouponId())
                    .orElseThrow(CouponNotFound::new);
            coupon.decrementQuantity();
            IssuedCoupon issuedCoupon = IssuedCoupon.issue(event.getEmail(), coupon);
            coupon.addIssuedCoupon(issuedCoupon);
            couponsToUpdate.add(coupon);
            issuedCouponsToSave.add(issuedCoupon);
        }
        couponRepository.saveAll(couponsToUpdate);

        Session session = em.unwrap(Session.class);
        session.doWork(connection -> {
            connection.setAutoCommit(false);
            saveUserCoupon(connection, issuedCouponsToSave);
            connection.commit();
        });

        //try (Connection connection = dataSource.getConnection()) {
        //    saveUserCoupon(connection, issuedCouponsToSave);
        //} catch (Exception e) {
        //    log.error("Failed to get connection: {}", e.getMessage(), e);
        //    throw new RuntimeException(e);
        //}
        ack.acknowledge();
    }

    public void saveUserCoupon(Connection connection, List<IssuedCoupon> issuedCoupons) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO issued_coupon (coupon_id, email, issued_at, status, used_at) VALUES (?, ?, ?, ?, ?)"
        )) {
            for (IssuedCoupon issuedCoupon : issuedCoupons) {
                statement.setLong(1, issuedCoupon.getCoupon().getId());
                statement.setString(2, issuedCoupon.getEmail());
                statement.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                statement.setString(4, CouponStatus.ISSUED.toString());
                statement.setTimestamp(5, null);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            log.error("Failed: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    @KafkaListener(topics = "coupon-used", groupId = "coupon-used-group", containerFactory = "usedKafkaListenerContainerFactory")
    public void consumeCouponIssuedEvent(CouponUsedEvent event, Acknowledgment ack) {
        //emailService.sendCouponUsedEmail(event.getEmail(), event.getCouponName());
        ack.acknowledge();
    }
}
