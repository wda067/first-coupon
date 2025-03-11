package com.firstcoupon.service;


import com.firstcoupon.domain.Coupon;
import com.firstcoupon.domain.IssuedCoupon;
import com.firstcoupon.dto.CouponIssue;
import com.firstcoupon.exception.CouponAlreadyIssued;
import com.firstcoupon.exception.CouponAlreadyUsed;
import com.firstcoupon.exception.CouponError;
import com.firstcoupon.exception.CouponExpired;
import com.firstcoupon.exception.CouponLockFailed;
import com.firstcoupon.exception.CouponSoldOut;
import com.firstcoupon.exception.InvalidCouponCode;
import com.firstcoupon.exception.IssuedCouponNotFound;
import com.firstcoupon.exception.NotIssuableTime;
import com.firstcoupon.kafka.CouponProducer;
import com.firstcoupon.repository.CouponRepository;
import com.firstcoupon.repository.IssuedCouponRepository;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private static final Logger couponLogger = LoggerFactory.getLogger("CouponLogger");

    private static final String COUPON_COUNT_KEY = "coupon_count";
    private static final String COUPON_USER_KEY_PREFIX = "coupon_user:";
    private static final String LOCK_KEY = "coupon_lock";

    private final CouponRepository couponRepository;
    private final IssuedCouponRepository issuedCouponRepository;
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final CouponProducer couponProducer;

    @Transactional
    public void issueCoupon(CouponIssue request) {
        boolean isAlreadyIssued = issuedCouponRepository.findByEmail(request.getEmail()).isPresent();
        if (isAlreadyIssued) {
            throw new CouponAlreadyIssued();
        }

        Coupon coupon = couponRepository.findByCode(request.getCode())
                .orElseThrow(InvalidCouponCode::new);

        boolean isNotIssuable = !coupon.isIssuable();
        if (isNotIssuable) {  //현재 시간이 발급 제한 시간일 때
            throw new NotIssuableTime();
        }

        int totalQuantity = coupon.getTotalQuantity();  //총 쿠폰 발급 수량
        long issuedCount = issuedCouponRepository.count();  //현재 발급된 쿠폰 수량

        if (issuedCount >= totalQuantity) {  //재고가 없을 경우
            throw new CouponSoldOut();
        }

        IssuedCoupon issuedCoupon = IssuedCoupon.issue(request.getEmail(), coupon);
        issuedCouponRepository.save(issuedCoupon);
    }

    @Transactional
    public synchronized void issueCouponWithSynchronized(CouponIssue request) {
        boolean isAlreadyIssued = issuedCouponRepository.findByEmail(request.getEmail()).isPresent();
        if (isAlreadyIssued) {
            throw new CouponAlreadyIssued();
        }

        Coupon coupon = couponRepository.findByCode(request.getCode())
                .orElseThrow(InvalidCouponCode::new);
        int totalQuantity = coupon.getTotalQuantity();  //총 쿠폰 발급 수량
        long issuedCount = issuedCouponRepository.count();  //현재 발급된 쿠폰 수량

        if (issuedCount >= totalQuantity) {  //재고가 없을 경우
            throw new CouponSoldOut();
        }

        IssuedCoupon issuedCoupon = IssuedCoupon.issue(request.getEmail(), coupon);
        issuedCouponRepository.save(issuedCoupon);
    }

    public void issueCouponWithRedis(CouponIssue request) {
        String userKey = COUPON_USER_KEY_PREFIX + request.getEmail();

        Coupon coupon = couponRepository.findByCode(request.getCode())
                .orElseThrow(InvalidCouponCode::new);
        long duration = coupon.getDuration();  //쿠폰 사용 기간

        Boolean canIssue = redisTemplate.opsForValue().setIfAbsent(userKey, "1", duration, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(canIssue)) {  //이미 발급받은 사용자일 경우
            throw new CouponAlreadyIssued();
        }

        int totalQuantity = coupon.getTotalQuantity();  //총 쿠폰 발급 수량
        Long currentCount = redisTemplate.opsForValue().increment(COUPON_COUNT_KEY);  //현재 발급된 쿠폰 수량
        redisTemplate.expire(COUPON_COUNT_KEY, duration, TimeUnit.SECONDS);

        try {
            if (currentCount > totalQuantity) {  //재고가 없을 경우
                throw new CouponSoldOut();
            }

            IssuedCoupon issuedCoupon = IssuedCoupon.issue(request.getEmail(), coupon);
            issuedCouponRepository.save(issuedCoupon);
        } catch (Exception e) {  //초과 발급 or 쿠폰 발급 실패시 롤백
            redisTemplate.opsForValue().decrement(COUPON_COUNT_KEY);
            redisTemplate.delete(userKey);
            throw new CouponError();
        }
    }

    @Transactional
    public void issueCouponWithRedisson(CouponIssue request) {
        RLock lock = redissonClient.getLock(LOCK_KEY);

        try {
            //5초 동안 락 획득을 시도하고, 락을 획득하면 10초 후 자동 해제
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                try {
                    boolean isAlreadyIssued = issuedCouponRepository.findByEmail(request.getEmail()).isPresent();
                    if (isAlreadyIssued) {  //이미 발급받은 사용자일 경우
                        throw new CouponAlreadyIssued();
                    }

                    Coupon coupon = couponRepository.findByCode(request.getCode())
                            .orElseThrow(InvalidCouponCode::new);
                    int totalQuantity = coupon.getTotalQuantity();  //총 쿠폰 발급 수량
                    long currentCount = issuedCouponRepository.count();  //현재 발급된 쿠폰 수량

                    if (currentCount >= totalQuantity) {  //재고가 없을 경우
                        throw new CouponSoldOut();
                    }

                    IssuedCoupon issuedCoupon = IssuedCoupon.issue(request.getEmail(), coupon);
                    issuedCouponRepository.save(issuedCoupon);
                } finally {
                    lock.unlock();
                }
            } else {
                throw new CouponLockFailed();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CouponError();
        }
    }

    public void issueCouponWithKafka(CouponIssue request) {
        String userKey = COUPON_USER_KEY_PREFIX + request.getEmail();

        Coupon coupon = couponRepository.findByCode(request.getCode())
                .orElseThrow(InvalidCouponCode::new);
        long duration = coupon.getDuration();  //쿠폰 사용 기간

        Boolean canIssue = redisTemplate.opsForValue().setIfAbsent(userKey, "1", duration, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(canIssue)) {  //이미 발급받은 사용자일 경우
            throw new CouponAlreadyIssued();
        }

        int totalQuantity = coupon.getTotalQuantity();  //총 쿠폰 발급 수량
        Long currentCount = redisTemplate.opsForValue().increment(COUPON_COUNT_KEY);  //현재 발급된 쿠폰 수량
        redisTemplate.expire(COUPON_COUNT_KEY, duration, TimeUnit.SECONDS);

        if (currentCount > totalQuantity) {  //재고가 없을 경우
            throw new CouponSoldOut();
        }

        couponProducer.send(request.getEmail(), coupon.getId());
    }

    @Transactional
    public void useCoupon(String email) {
        IssuedCoupon issuedCoupon = issuedCouponRepository.findByEmail(email)
                .orElseThrow(IssuedCouponNotFound::new);

        LocalDate expirationDate = issuedCoupon.getCoupon().getExpirationDate();
        boolean isExpired = issuedCoupon.isExpired(expirationDate);
        if (isExpired) {  //만료일이 지났을 경우
            issuedCoupon.expire();  //만료 처리
            throw new CouponExpired();
        }

        boolean isUsed = issuedCoupon.isUsed();
        if (isUsed) {  //이미 사용한 쿠폰일 경우
            throw new CouponAlreadyUsed();
        }

        issuedCoupon.use();
        couponProducer.send(email, issuedCoupon.getCoupon().getCouponName());
        couponLogger.info("쿠폰 사용됨 - 코드: {}, 사용자: {}", issuedCoupon.getCoupon().getCode(), email);
    }
}
