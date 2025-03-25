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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private static final Logger couponLogger = LoggerFactory.getLogger("CouponLogger");

    private static final String COUPON_COUNT_KEY_PREFIX = "coupon_count:";
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
        if (isAlreadyIssued) {  //이미 쿠폰을 발급받은 회원일 경우
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

        coupon.decrementQuantity();  //쿠폰 재고 감소
        IssuedCoupon issuedCoupon = IssuedCoupon.issue(request.getEmail(), coupon);  //쿠폰 발급
        coupon.addIssuedCoupon(issuedCoupon);  //양방향 연관관계 설정

        issuedCouponRepository.save(issuedCoupon);
    }

    @Transactional
    public synchronized void issueCouponWithSynchronized(CouponIssue request) {
        boolean isAlreadyIssued = issuedCouponRepository.findByEmail(request.getEmail()).isPresent();
        if (isAlreadyIssued) {  //이미 쿠폰을 발급받은 회원일 경우
            throw new CouponAlreadyIssued();
        }

        Coupon coupon = couponRepository.findByCodeForUpdate(request.getCode())
                .orElseThrow(InvalidCouponCode::new);  //쿠폰 조회에 비관적 락 적용

        boolean isNotIssuable = !coupon.isIssuable();
        if (isNotIssuable) {  //현재 시간이 발급 제한 시간일 때
            throw new NotIssuableTime();
        }

        int totalQuantity = coupon.getTotalQuantity();  //총 쿠폰 발급 수량
        long issuedCount = issuedCouponRepository.count();  //현재 발급된 쿠폰 수량

        if (issuedCount >= totalQuantity) {  //재고가 없을 경우
            throw new CouponSoldOut();
        }

        coupon.decrementQuantity();  //쿠폰 재고 감소
        IssuedCoupon issuedCoupon = IssuedCoupon.issue(request.getEmail(), coupon);  //쿠폰 발급
        coupon.addIssuedCoupon(issuedCoupon);  //양방향 연관관계 설정

        issuedCouponRepository.save(issuedCoupon);
    }

    @Transactional
    public void issueCouponWithRedis(CouponIssue request) {
        String userKey = COUPON_USER_KEY_PREFIX + request.getCode() + ":" + request.getEmail();
        String countKey = COUPON_COUNT_KEY_PREFIX + request.getCode();

        Coupon coupon = couponRepository.findByCode(request.getCode())
                .orElseThrow(InvalidCouponCode::new);
        long duration = coupon.getDuration();  //쿠폰 사용 기간
        int totalQuantity = coupon.getTotalQuantity();  //총 쿠폰 발급 수량

        //Lua 스크립트 정의
        String luaScript = """
                if redis.call('EXISTS', KEYS[1]) == 1 then
                    return -1
                end
                local currentCount = redis.call('GET', KEYS[2]) or "0"
                if tonumber(currentCount) >= tonumber(ARGV[1]) then
                    return 0
                end
                redis.call('INCR', KEYS[2])
                redis.call('SET', KEYS[1], 'issued', 'EX', ARGV[2])
                return 1
                """;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);

        List<String> keys = Arrays.asList(userKey, countKey);
        long result = redisTemplate.execute(script, keys, String.valueOf(totalQuantity), String.valueOf(duration));

        if (result == -1) {  //이미 발급받은 사용자일 경우
            throw new CouponAlreadyIssued();
        } else if (result == 0) {  //재고가 없을 경우
            throw new CouponSoldOut();
        }

        try {
            coupon.decrementQuantity();  //쿠폰 재고 감소
            IssuedCoupon issuedCoupon = IssuedCoupon.issue(request.getEmail(), coupon);  //쿠폰 발급
            coupon.addIssuedCoupon(issuedCoupon);  //양방향 연관관계 설정

            issuedCouponRepository.save(issuedCoupon);
        } catch (Exception e) {  //쿠폰 발급 실패시 롤백
            redisTemplate.opsForValue().decrement(countKey);
            redisTemplate.delete(userKey);
            throw new CouponError();
        }
    }

    //@Transactional
    //public void issueCouponWithRedis2(CouponIssue request) {
    //    String userKey = COUPON_USER_KEY_PREFIX + request.getCode() + ":" + request.getEmail();
    //    String countKey = COUPON_COUNT_KEY_PREFIX + request.getCode();
    //    Coupon coupon = couponRepository.findByCode(request.getCode())
    //            .orElseThrow(InvalidCouponCode::new);
    //    long duration = coupon.getDuration();  //쿠폰 사용 기간
    //
    //    Boolean canIssue = redisTemplate.opsForValue().setIfAbsent(userKey, "issued", duration, TimeUnit.SECONDS);
    //    if (Boolean.FALSE.equals(canIssue)) {  //이미 발급받은 사용자일 경우
    //        throw new CouponAlreadyIssued();
    //    }
    //
    //    int totalQuantity = coupon.getTotalQuantity();  //총 쿠폰 발급 수량
    //    Long currentCount = redisTemplate.opsForValue().increment(countKey);  //현재 발급된 쿠폰 수량
    //    redisTemplate.expire(countKey, duration, TimeUnit.SECONDS);
    //
    //    if (currentCount > totalQuantity) {  //총 재고를 초과했을 경우
    //        redisTemplate.delete(userKey);
    //        redisTemplate.opsForValue().decrement(countKey);
    //        throw new CouponSoldOut();
    //    }
    //
    //    try {
    //        coupon.decrementQuantity();  //쿠폰 재고 감소
    //        IssuedCoupon issuedCoupon = IssuedCoupon.issue(request.getEmail(), coupon);  //쿠폰 발급
    //        coupon.addIssuedCoupon(issuedCoupon);  //양방향 연관관계 설정
    //
    //        issuedCouponRepository.save(issuedCoupon);
    //    } catch (Exception e) {  //쿠폰 발급 실패시 롤백
    //        redisTemplate.delete(userKey);
    //        throw new CouponError();
    //    }
    //}

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
        String userKey = COUPON_USER_KEY_PREFIX + request.getCode() + ":" + request.getEmail();
        String countKey = COUPON_COUNT_KEY_PREFIX + request.getCode();

        Coupon coupon = couponRepository.findByCode(request.getCode())
                .orElseThrow(InvalidCouponCode::new);
        long duration = coupon.getDuration();  //쿠폰 사용 기간
        int totalQuantity = coupon.getTotalQuantity();  //총 쿠폰 발급 수량

        //Lua 스크립트 정의
        String luaScript = """
                if redis.call('EXISTS', KEYS[1]) == 1 then
                    return -1
                end
                local currentCount = redis.call('GET', KEYS[2]) or "0"
                if tonumber(currentCount) >= tonumber(ARGV[1]) then
                    return 0
                end
                redis.call('INCR', KEYS[2])
                redis.call('SET', KEYS[1], 'issued', 'EX', ARGV[2])
                return 1
                """;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(luaScript);
        script.setResultType(Long.class);

        List<String> keys = Arrays.asList(userKey, countKey);
        long result = redisTemplate.execute(script, keys, String.valueOf(totalQuantity), String.valueOf(duration));

        if (result == -1) {  //이미 발급받은 사용자일 경우
            throw new CouponAlreadyIssued();
        } else if (result == 0) {  //재고가 없을 경우
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
