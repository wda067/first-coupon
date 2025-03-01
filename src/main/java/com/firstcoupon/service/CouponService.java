package com.firstcoupon.service;

import com.firstcoupon.domain.Coupon;
import com.firstcoupon.repository.CouponRepository;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private static final int MAX_COUPON_COUNT = 100;
    private static final String COUPON_ALREADY_ISSUED = "이미 쿠폰을 발급받은 회원입니다.";
    private static final String COUPON_SOLD_OUT = "쿠폰이 모두 소진되었습니다.";

    private static final String COUPON_COUNT_KEY = "coupon_count";
    private static final String COUPON_USER_KEY_PREFIX = "coupon_user:";

    private final CouponRepository couponRepository;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public void issueCoupon(Long userId) {
        boolean isAlreadyIssued = couponRepository.findByUserId(userId).isPresent();
        if (isAlreadyIssued) {
            throw new IllegalStateException(COUPON_ALREADY_ISSUED);
        }

        long count = couponRepository.count();
        if (count >= MAX_COUPON_COUNT) {
            throw new IllegalStateException(COUPON_SOLD_OUT);
        }

        couponRepository.save(new Coupon(userId));
    }

    @Transactional
    public synchronized void issueCouponWithSynchronized(Long userId) {
        boolean isAlreadyIssued = couponRepository.findByUserId(userId).isPresent();
        if (isAlreadyIssued) {
            throw new IllegalStateException(COUPON_ALREADY_ISSUED);
        }

        long count = couponRepository.count();
        if (count >= MAX_COUPON_COUNT) {
            throw new IllegalStateException(COUPON_SOLD_OUT);
        }

        couponRepository.save(new Coupon(userId));
    }

    public void issueCouponWithRedis(Long userId) {
        String userKey = COUPON_USER_KEY_PREFIX + userId;

        Boolean canIssue = redisTemplate.opsForValue().setIfAbsent(userKey, "1", 10, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(canIssue)) {  //이미 발급받은 사용자일 경우
            throw new IllegalStateException(COUPON_ALREADY_ISSUED);
        }

        Long currentCount = redisTemplate.opsForValue().increment(COUPON_COUNT_KEY);
        if (currentCount > MAX_COUPON_COUNT) {  //초과 발급시 롤백
            redisTemplate.opsForValue().decrement(COUPON_COUNT_KEY);
            throw new IllegalStateException(COUPON_SOLD_OUT);
        }

        couponRepository.save(new Coupon(userId));
    }
}
