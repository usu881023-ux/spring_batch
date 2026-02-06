package com.secta.hcbatch.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job dayGoodsSumJob;
    private final Job orderPrvaMaskJob;

    public BatchScheduler(JobLauncher jobLauncher, Job dayGoodsSumJob, Job orderPrvaMaskJob) {
        this.jobLauncher = jobLauncher;
        this.dayGoodsSumJob = dayGoodsSumJob;
        this.orderPrvaMaskJob = orderPrvaMaskJob;
        log.info("BatchScheduler 초기화 완료 - Job: {}", dayGoodsSumJob.getName());
    }

    /**
     * 배치 작업 실행
     * - 매일 새벽 2시에 실행 (운영 환경)
     * - 테스트: 매분마다 실행
     */
    /*@Scheduled(cron = "0 0 2 * * *") // 운영*/
    /*@Scheduled(cron = "0 * * * * *")     // 테스트: 매분 0초*/
    public void run() {
        log.info("========================================");
        log.info("배치 스케줄러 시작");
        log.info("========================================");

        /*runJob(dayGoodsSumJob);*/
        runJobOrderPrvaMask(orderPrvaMaskJob);

        log.info("========================================");
        log.info("배치 스케줄러 완료");
        log.info("========================================");
    }

    /**
     * Job 실행 헬퍼 메서드
     */
    private void runJob(Job job) {
        try {
            String jobName = job.getName();
            log.info("Job 실행 시작: {}", jobName);

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .addString("jobName", jobName)
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(job, jobParameters);

            log.info("Job 실행 완료: {} (executionId: {}, status: {})",
                    jobName, execution.getId(), execution.getStatus());

        } catch (Exception e) {
            log.error("Job 실행 실패: {}", job.getName(), e);
        }
    }

    /**
     * Job 실행 헬퍼 메서드
     */
    private void runJobOrderPrvaMask(Job job) {
        try {
            String jobName = job.getName();
            log.info("Job 실행 시작: {}", jobName);

            JobParameters jobParameters = new JobParametersBuilder()
                    /*.addLong("timestamp", System.currentTimeMillis())*/
                    .addString("jobName", jobName)
                    .addLong("BASE_DT", System.currentTimeMillis())
                    .addString("PROC_CD", "COMP")
                    .toJobParameters();

            JobExecution execution = jobLauncher.run(job, jobParameters);

            log.info("Job 실행 완료: {} (executionId: {}, status: {})",
                    jobName, execution.getId(), execution.getStatus());

        } catch (Exception e) {
            log.error("Job 실행 실패: {}", job.getName(), e);
        }
    }
}
