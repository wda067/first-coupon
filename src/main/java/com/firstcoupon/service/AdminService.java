package com.firstcoupon.service;

import com.firstcoupon.domain.Coupon;
import com.firstcoupon.dto.CouponCreate;
import com.firstcoupon.dto.CouponResponse;
import com.firstcoupon.exception.CouponAlreadyExists;
import com.firstcoupon.exception.InvalidExpirationDate;
import com.firstcoupon.repository.CouponRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final CouponRepository couponRepository;

    @Transactional
    public void createCoupon(CouponCreate request) {
        LocalDate expirationDate = parseLocalDateTime(request.getExpirationDate()).toLocalDate();
        LocalDateTime issueStartTime = parseLocalDateTime(request.getIssueStartTime());
        LocalDateTime issueEndTime = parseLocalDateTime(request.getIssueEndTime());

        boolean exists = couponRepository.existsByCouponNameAndExpirationDate(request.getCouponName(), expirationDate);
        if (exists) {  //같은 이름, 만료일의 쿠폰이 존재
            throw new CouponAlreadyExists();
        }

        Coupon coupon = Coupon.create(request.getCouponName(), request.getTotalQuantity(), expirationDate,
                issueStartTime, issueEndTime);
        couponRepository.save(coupon);
    }

    private static LocalDateTime parseLocalDateTime(String raw) {
        LocalDateTime localDateTime;

        try {  //String -> LocalDateTime 변환
            localDateTime = LocalDateTime.parse(raw);
        } catch (DateTimeParseException e) {
            throw new InvalidExpirationDate();
        }
        return localDateTime;
    }

    public List<CouponResponse> getCoupons() {
        return couponRepository.findAll().stream()
                .map(CouponResponse::new)
                .toList();
    }
}
