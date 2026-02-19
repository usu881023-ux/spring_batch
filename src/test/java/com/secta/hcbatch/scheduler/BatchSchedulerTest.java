package com.secta.hcbatch.scheduler;

import com.kbs.util.encryptor.BCipher;
import com.secta.hcbatch.common.util.BizUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import lombok.extern.slf4j.Slf4j;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BatchScheduler 테스트
 */
@Slf4j
@SpringBootTest
@ActiveProfiles("local")
class BatchSchedulerTest {

    @Autowired(required = false)
    private BatchScheduler batchScheduler;

    @Autowired(required = false)
    private JobOperator jobOperator;

    @Autowired(required = false)
    private JobLauncher jobLauncher;

    @Autowired(required = false)
    private Job orderPrvaMaskJob;

    @Autowired(required = false)
    private Job dayGoodsSumJob;

    @Autowired(required = false)
    private Job hcSendMsgJob;

    @Autowired(required = false)
    private Job hconSyncJob;

    @Test
    @DisplayName("BatchScheduler Bean 로딩 테스트")
    void testBatchSchedulerBeanLoaded() {
        assertNotNull(batchScheduler, "BatchScheduler Bean이 로딩되어야 합니다.");
        log.info("✅ BatchScheduler Bean 로딩 성공");
    }
    
    @Test
    @DisplayName("BatchScheduler.run() 메서드 실행 테스트")
    void testBatchSchedulerRun() {
        // Given
        assertNotNull(batchScheduler, "BatchScheduler가 null이 아니어야 합니다.");
        
        // When & Then
        assertDoesNotThrow(() -> {
            log.info("===== BatchScheduler.run() 실행 시작 =====");
            batchScheduler.run();
            log.info("===== BatchScheduler.run() 실행 완료 =====");
        }, "BatchScheduler.run() 메서드가 예외 없이 실행되어야 합니다.");
    }
    
    @Test
    @DisplayName("JobOperator Bean 로딩 테스트")
    void testJobOperatorBeanLoaded() {
        assertNotNull(jobOperator, "JobOperator Bean이 로딩되어야 합니다.");
        log.info("✅ JobOperator Bean 로딩 성공");
    }

    @Test
    @DisplayName("개인정보 마스킹처리 실행 테스트")
    void testMaskingRun() {
        String jobName = orderPrvaMaskJob.getName();
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("jobName", jobName)
                    .addLong("BASE_DT", System.currentTimeMillis())
                    .addString("PROC_CD", "COMP")
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(orderPrvaMaskJob, jobParameters);

            log.info("Job 실행 완료: {} (executionId: {}, status: {})",
                    jobName, execution.getId(), execution.getStatus());

        } catch (Exception e) {
            log.error("Job 실행 실패: {}", jobName, e);
        }
    }

    @Test
    @DisplayName("상품별 판매현황 집계 실행 테스트")
    void testDayGoodsSumRun() {
        String jobName = dayGoodsSumJob.getName();
        try {
            log.info("Job 실행 시작: {}", jobName);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("jobName", jobName)
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(dayGoodsSumJob, jobParameters);

            log.info("Job 실행 완료: {} (executionId: {}, status: {})",
                    jobName, execution.getId(), execution.getStatus());

        } catch (Exception e) {
            log.error("Job 실행 실패: {}", jobName, e);
        }
    }

    @Test
    @DisplayName("MMS 발송 Job 실행 테스트")
    void testHcSendMsgJobRun() {
        if (hcSendMsgJob == null) {
            log.warn("hcSendMsgJob Bean이 로딩되지 않았습니다.");
            return;
        }

        String jobName = hcSendMsgJob.getName();
        try {
            log.info("Job 실행 시작: {}", jobName);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("jobName", jobName)
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(hcSendMsgJob, jobParameters);

            log.info("Job 실행 완료: {} (executionId: {}, status: {})",
                    jobName, execution.getId(), execution.getStatus());

        } catch (Exception e) {
            log.error("Job 실행 실패: {}", jobName, e);
        }
    }

    @Test
    @DisplayName("상품권 동기화 Job 실행 테스트")
    void testHconSyncJobRun() {
        if (hconSyncJob == null) {
            log.warn("hconSyncJob Bean이 로딩되지 않았습니다.");
            return;
        }

        String jobName = hconSyncJob.getName();
        try {
            log.info("Job 실행 시작: {}", jobName);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("jobName", jobName)
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(hconSyncJob, jobParameters);

            log.info("Job 실행 완료: {} (executionId: {}, status: {})",
                    jobName, execution.getId(), execution.getStatus());

        } catch (Exception e) {
            log.error("Job 실행 실패: {}", jobName, e);
        }
    }

    @Test
    @DisplayName("해피콘 쿠폰번호 암호화 테스트")
    void testEncryptVoucherNo() {
        // 암호화 : 91032g4000I4104^
        // 복호화 : 910300032542
        String voucherNo = "910300032542";

        String encryptedNo = BCipher.encryptCoupon(voucherNo);
        log.info("encryptedNo: {}", encryptedNo);

        String decryptedNo = BizUtil.decryptVoucherNo(encryptedNo);
        log.info("decryptedNo: {}", decryptedNo);
    }
}
