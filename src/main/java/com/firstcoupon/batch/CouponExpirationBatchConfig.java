package com.firstcoupon.batch;

import com.firstcoupon.domain.Coupon;
import com.firstcoupon.domain.IssuedCoupon;
import com.firstcoupon.service.EmailService;
import java.time.LocalDate;
import java.util.Collections;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.MySqlPagingQueryProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
public class CouponExpirationBatchConfig {

    private static final Logger couponLogger = LoggerFactory.getLogger("CouponLogger");

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final EmailService emailService;

    @Bean
    public Job couponExpirationBatchJob() {
        return new JobBuilder("couponExpirationJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(couponExpirationBatchStep())
                .build();
    }

    @Bean
    public Step couponExpirationBatchStep() {
        return new StepBuilder("couponExpirationStep", jobRepository)
                .<IssuedCoupon, IssuedCoupon>chunk(10, transactionManager)
                .reader(couponPagingItemReader())
                .processor(couponItemProcessor())
                .writer(couponItemWriter())
                .build();
    }

    @Bean
    public JdbcPagingItemReader<IssuedCoupon> couponPagingItemReader() {
        return new JdbcPagingItemReaderBuilder<IssuedCoupon>()
                .name("couponPagingItemReader")
                .dataSource(dataSource)
                .fetchSize(100)
                .rowMapper((rs, rowNum) -> IssuedCoupon.builder()
                        .email(rs.getString("email"))
                        .coupon(Coupon.builder()
                                .couponName(rs.getString("coupon_name"))
                                .expirationDate(rs.getObject("expiration_date", LocalDate.class))
                                .build()
                        )
                        .build()
                )
                .queryProvider(new MySqlPagingQueryProvider() {{
                    setSelectClause("SELECT ic.email, c.coupon_name, c.expiration_date");
                    setFromClause("FROM issued_coupon ic JOIN coupon c ON ic.coupon_id = c.id");
                    setWhereClause("WHERE c.expiration_date = :targetDate AND ic.status = 'ISSUED'");
                    setSortKeys(Collections.singletonMap("ic.email", Order.ASCENDING));
                }})
                .parameterValues(Collections.singletonMap("targetDate", LocalDate.of(2025, 3, 31)))
                .build();
    }

    @Bean
    public ItemProcessor<IssuedCoupon, IssuedCoupon> couponItemProcessor() {
        return issuedCoupon -> {
            emailService.sendCouponExpirationEmail(issuedCoupon.getEmail(), issuedCoupon.getCoupon().getCouponName());
            return issuedCoupon;
        };
    }

    @Bean
    public ItemWriter<IssuedCoupon> couponItemWriter() {
        return issuedCoupons -> issuedCoupons.forEach(issuedCoupon ->
                couponLogger.info("쿠폰 만료 알림 전송 완료 - 사용자: {}, 쿠폰: {}",
                        issuedCoupon.getEmail(), issuedCoupon.getCoupon().getCouponName())
        );
    }
}
