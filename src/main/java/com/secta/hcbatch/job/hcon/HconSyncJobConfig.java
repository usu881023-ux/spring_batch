package com.secta.hcbatch.job.hcon;

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
 * 상품권 동기화 Job 설정
 *
 * Job명: hconSyncJob
 * 설명: 해피콘 상품권 상태 동기화 (사용/취소/환불/유효기간연장)
 * 실행주기: 5초 간격 (Scheduler에서 제어)
 *
 * 처리 내용:
 * - HCON DB의 CPN_HIST 테이블에서 변경 이력 조회
 * - HC DB의 T_HAPPYCON_COUPON 테이블 상태 동기화
 * - H_HAPPYCON_COUPON_HST 테이블에 이력 저장
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class HconSyncJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final HconSyncTasklet hconSyncTasklet;
    private final HconSyncListener hconSyncListener;

    /**
     * hconSyncJob 정의
     */
    @Bean
    public Job hconSyncJob() {
        log.info("Initializing hconSyncJob");

        return new JobBuilder("hconSyncJob", jobRepository)
                .listener(hconSyncListener)
                .start(hconSyncStep())
                .build();
    }

    /**
     * hconSyncStep 정의
     */
    @Bean
    public Step hconSyncStep() {
        log.info("Initializing hconSyncStep");

        return new StepBuilder("hconSyncStep", jobRepository)
                .tasklet(hconSyncTasklet, transactionManager)
                .build();
    }
}
