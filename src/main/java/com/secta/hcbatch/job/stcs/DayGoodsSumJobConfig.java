package com.secta.hcbatch.job.stcs;

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
 * 상품별 판매현황 집계 Job 설정
 * 
 * Job명: dayGoodsSumJob
 * 설명: 해피콘 상품별 일 단위 판매현황 집계
 * 실행주기: 매일 새벽 (판매 데이터 확정 후)
 * 
 * 처리 내용:
 * - 일 단위 상품별 판매 집계
 * - S_DAY_GOODS_SALE_SUM 테이블에 저장
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DayGoodsSumJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DayGoodsSumTasklet dayGoodsSumTasklet;

    /**
     * dayGoodsSumJob 정의
     */
    @Bean
    public Job dayGoodsSumJob() {
        log.info("Initializing dayGoodsSumJob");
        
        return new JobBuilder("dayGoodsSumJob", jobRepository)
                .start(dayGoodsSumStep())
                .build();
    }

    /**
     * dayGoodsSumStep 정의
     * Tasklet 방식 사용 (단순 집계 INSERT)
     */
    @Bean
    public Step dayGoodsSumStep() {
        log.info("Initializing dayGoodsSumStep");
        
        return new StepBuilder("dayGoodsSumStep", jobRepository)
                .tasklet(dayGoodsSumTasklet, transactionManager)
                .build();
    }
}
