package com.secta.hcbatch.job.message;

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
 * MMS 발송 Job 설정
 *
 * Job명: hcSendMsgJob
 * 설명: 해피콘 MMS 발송 처리
 * 실행주기: 7초 간격 (Scheduler에서 제어)
 *
 * 처리 내용:
 * - T_HAPPYCON_MMS 테이블에서 발송 대상 조회
 * - S3에서 이미지 다운로드
 * - EM_TRAN/EM_TRAN_MMS 테이블에 발송 데이터 등록
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class HcSendMsgJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final HcSendMsgTasklet hcSendMsgTasklet;
    private final HcSendMsgListener hcSendMsgListener;

    /**
     * hcSendMsgJob 정의
     */
    @Bean
    public Job hcSendMsgJob() {
        log.info("Initializing hcSendMsgJob");

        return new JobBuilder("hcSendMsgJob", jobRepository)
                .listener(hcSendMsgListener)
                .start(hcSendMsgStep())
                .build();
    }

    /**
     * hcSendMsgStep 정의
     */
    @Bean
    public Step hcSendMsgStep() {
        log.info("Initializing hcSendMsgStep");

        return new StepBuilder("hcSendMsgStep", jobRepository)
                .tasklet(hcSendMsgTasklet, transactionManager)
                .build();
    }
}
