package com.firstcoupon.service;

import static com.firstcoupon.constants.CouponConstant.MAX_COUPON_COUNT;
import static com.firstcoupon.constants.CouponMessage.COUPON_ALREADY_ISSUED;
import static com.firstcoupon.constants.CouponMessage.COUPON_ERROR;
import static com.firstcoupon.constants.CouponMessage.COUPON_LOCK_FAILED;
import static com.firstcoupon.constants.CouponMessage.COUPON_SOLD_OUT;

import com.firstcoupon.config.kafka.CouponProducer;
import com.firstcoupon.domain.Coupon;
import com.firstcoupon.exception.CouponAlreadyIssued;
import com.firstcoupon.exception.CouponError;
import com.firstcoupon.exception.CouponLockFailed;
import com.firstcoupon.exception.CouponSoldOut;
import com.firstcoupon.repository.CouponRepository;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private static final String COUPON_COUNT_KEY = "coupon_count";
    private static final String COUPON_USER_KEY_PREFIX = "coupon_user:";
    private static final String LOCK_KEY = "coupon_lock";

    private final CouponRepository couponRepository;
    private final StringRedisTemplate redisTemplate;
    private final RedissonClient redissonClient;
    private final CouponProducer couponProducer;

    @Transactional
    public void issueCoupon(Long userId) {
        boolean isAlreadyIssued = couponRepository.findByUserId(userId).isPresent();
        if (isAlreadyIssued) {
            throw new CouponAlreadyIssued();
        }

        long count = couponRepository.count();
        if (count >= MAX_COUPON_COUNT.getValue()) {
            throw new CouponSoldOut();
        }

        couponRepository.save(new Coupon(userId));
    }

    @Transactional
    public synchronized void issueCouponWithSynchronized(Long userId) {
        boolean isAlreadyIssued = couponRepository.findByUserId(userId).isPresent();
        if (isAlreadyIssued) {
            throw new CouponAlreadyIssued();
        }

        long count = couponRepository.count();
        if (count >= MAX_COUPON_COUNT.getValue()) {
            throw new CouponSoldOut();
        }

        couponRepository.save(new Coupon(userId));
    }

    public void issueCouponWithRedis(Long userId) {
        String userKey = COUPON_USER_KEY_PREFIX + userId;

        Boolean canIssue = redisTemplate.opsForValue().setIfAbsent(userKey, "1", 10, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(canIssue)) {  //이미 발급받은 사용자일 경우
            throw new CouponAlreadyIssued();
        }

        Long currentCount = redisTemplate.opsForValue().increment(COUPON_COUNT_KEY);
        try {
            if (currentCount > MAX_COUPON_COUNT.getValue()) {
                throw new CouponSoldOut();
            }

            couponRepository.save(new Coupon(userId));
        } catch (Exception e) {  //초과 발급 or 쿠폰 발급 실패시 롤백
            redisTemplate.opsForValue().decrement(COUPON_COUNT_KEY);
            redisTemplate.delete(userKey);
            throw new CouponError();
        }
    }

    @Transactional
    public void issueCouponWithRedisson(Long userId) {
        RLock lock = redissonClient.getLock(LOCK_KEY);

        try {
            //5초 동안 락 획득을 시도하고, 락을 획득하면 10초 후 자동 해제
            if (lock.tryLock(5, 10, TimeUnit.SECONDS)) {
                try {
                    boolean isAlreadyIssued = couponRepository.findByUserId(userId).isPresent();
                    if (isAlreadyIssued) {  //이미 발급받은 사용자일 경우
                        throw new CouponAlreadyIssued();
                    }

                    long count = couponRepository.count();
                    if (count >= MAX_COUPON_COUNT.getValue()) {  //재고가 없을 경우
                        throw new CouponSoldOut();
                    }

                    couponRepository.save(new Coupon(userId));
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

    public void issueCouponWithKafka(Long userId) {
        String userKey = COUPON_USER_KEY_PREFIX + userId;

        Boolean canIssue = redisTemplate.opsForValue().setIfAbsent(userKey, "1", 10, TimeUnit.MINUTES);
        if (Boolean.FALSE.equals(canIssue)) {  //이미 발급받은 사용자일 경우
            throw new CouponAlreadyIssued();
        }

        Long currentCount = redisTemplate.opsForValue().increment(COUPON_COUNT_KEY);
        if (currentCount > MAX_COUPON_COUNT.getValue()) {  //재고가 없을 경우
            redisTemplate.opsForValue().decrement(COUPON_COUNT_KEY);
            redisTemplate.delete(userKey);
            throw new CouponSoldOut();
        }

        couponProducer.send(userId);
    }
}
