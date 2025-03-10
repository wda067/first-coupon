package com.firstcoupon.service;

import com.firstcoupon.domain.Coupon;
import com.firstcoupon.dto.CouponCreate;
import com.firstcoupon.dto.CouponResponse;
import com.firstcoupon.exception.CouponAlreadyExists;
import com.firstcoupon.repository.CouponRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private static final Logger couponLogger = LoggerFactory.getLogger("CouponLogger");

    private final CouponRepository couponRepository;

    @Transactional
    public void createCoupon(CouponCreate request) {
        LocalDate expirationDate = request.getExpirationDate();
        LocalDateTime issueStartTime = request.getIssueStartTime();
        LocalDateTime issueEndTime = request.getIssueEndTime();

        boolean exists = couponRepository.existsByCouponNameAndExpirationDate(request.getCouponName(), expirationDate);
        if (exists) {  //같은 이름, 만료일의 쿠폰이 존재
            throw new CouponAlreadyExists();
        }

        Coupon coupon = Coupon.create(request.getCouponName(), request.getTotalQuantity(), expirationDate,
                issueStartTime, issueEndTime);
        couponRepository.save(coupon);

        couponLogger.info("쿠폰 생성됨 - 코드: {}, 쿠폰명: {}", coupon.getCode(), coupon.getCouponName());
    }

    public List<CouponResponse> getCoupons() {
        return couponRepository.findAll().stream()
                .map(CouponResponse::new)
                .toList();
    }
}
