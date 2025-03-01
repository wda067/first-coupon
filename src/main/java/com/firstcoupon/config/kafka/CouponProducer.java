package com.firstcoupon.config.kafka;

import com.firstcoupon.domain.CouponIssuedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponProducer {

    private static final String TOPIC_NAME = "coupon-issued";
    private static final String COUPON_COUNT_KEY = "coupon_count";

    private final KafkaTemplate<String, CouponIssuedEvent> kafkaTemplate;
    private final StringRedisTemplate redisTemplate;

    public void send(Long userId, String userKey) {
        kafkaTemplate.send(TOPIC_NAME, new CouponIssuedEvent(userId))
                .thenAccept(result -> log.info("쿠폰 발급 이벤트 전송 성공!"))
                .exceptionally(ex -> {
                    redisTemplate.opsForValue().decrement(COUPON_COUNT_KEY);
                    redisTemplate.delete(userKey);
                    throw new IllegalStateException("Kafka 전송 실패!", ex);
                });
    }
}
