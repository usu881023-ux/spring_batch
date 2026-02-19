package com.secta.hcbatch.job.prva;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 개인정보 마스킹 Job 설정
 * 
 * Job명: orderPrvaMaskJob
 * 설명: 해피콘 선물하기 주문완료/취소 개인정보 삭제/마스킹
 * 실행주기: 매일 02:10 (취소건), 03:10 (완료건)
 * 
 * 처리 대상 테이블 (11개):
 * 1. T_ORDER (주문)
 * 2. T_ORDER_BASKET (장바구니)
 * 3. T_ORDER_DELIVERY (주문배송)
 * 4. T_OUTSIDE_MMS (외부쿠폰정보)
 * 5. T_REFUND_ACC_INFO (환불)
 * 6. T_REFUND_IN_LOG (환불이력)
 * 7. T_HAPPYCON_COUPON (해피콘정보)
 * 8. T_HAPPYCON_COUPON_MULTI (해피콘복수)
 * 9. T_HAPPYCON_COUPON_HST (해피콘이력)
 * 10. T_HAPPYCON_REJ (해피콘반려)
 * 11. T_HAPPYCON_REJ_IN_LOG (해피콘반려이력)
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class OrderPrvaMaskJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final OrderPrvaMaskTasklet orderPrvaMaskTasklet;
    private final OrderPrvaMaskListener orderPrvaMaskListener;

    /**
     * orderPrvaMaskJob 정의
     */
    @Bean
    public Job orderPrvaMaskJob() {
        log.info("Initializing orderPrvaMaskJob");
        
        return new JobBuilder("orderPrvaMaskJob", jobRepository)
                .listener(orderPrvaMaskListener)
                .start(orderPrvaMaskStep())
                .build();
    }

    /**
     * orderPrvaMaskStep 정의
     * Tasklet 방식 사용 (복잡한 다중 테이블 처리)
     */
    @Bean
    public Step orderPrvaMaskStep() {
        log.info("Initializing orderPrvaMaskStep");
        
        return new StepBuilder("orderPrvaMaskStep", jobRepository)
                .tasklet(orderPrvaMaskTasklet, transactionManager)
                .build();
    }
}
