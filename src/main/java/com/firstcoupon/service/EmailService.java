package com.firstcoupon.service;

import com.firstcoupon.domain.Coupon;
import com.firstcoupon.exception.EmailSendFailure;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@EnableRetry
@EnableAsync
public class EmailService {

    private static final Logger couponLogger = LoggerFactory.getLogger("CouponLogger");

    private final JavaMailSender mailSender;

    @Async
    @Retryable(
            retryFor = {EmailSendFailure.class},
            backoff = @Backoff(delay = 2000)
    )
    public void sendEmail(String email, Coupon coupon) {
        try {
            couponLogger.info("쿠폰 만료 알림 이메일 전송 요청 - 이메일: {}", email);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("쿠폰 만료 알림");
            String emailContent = String.format(
                    "<h1>쿠폰 만료 안내</h1><p>고객님의 쿠폰 '%s'이 7일 후 만료됩니다. 서둘러 사용해 주세요!</p>",
                    coupon.getCouponName()
            );
            helper.setText(emailContent, true);

            mailSender.send(message);
            couponLogger.info("이메일 전송 성공 - 이메일: {}", email);
        } catch (MessagingException e) {
            throw new EmailSendFailure();
        }
    }
}
