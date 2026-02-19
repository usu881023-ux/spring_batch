package com.secta.hcbatch.job.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * MMS 발송 Job 리스너
 */
@Slf4j
@Component
public class HcSendMsgListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("╔═══════════════════════════════════════════════════════════╗");
        log.info("║  HcSendMsg Job 시작                                        ║");
        log.info("║  Job Name: {}                                             ║", jobExecution.getJobInstance().getJobName());
        log.info("║  Job Instance ID: {}                                      ║", jobExecution.getJobInstance().getId());
        log.info("║  Job Execution ID: {}                                     ║", jobExecution.getId());
        log.info("║  Parameters: {}                                           ║", jobExecution.getJobParameters());
        log.info("╚═══════════════════════════════════════════════════════════╝");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        LocalDateTime start = jobExecution.getStartTime();
        LocalDateTime end = jobExecution.getEndTime();
        long duration = Duration.between(start, end).toMillis();

        log.info("╔═══════════════════════════════════════════════════════════╗");
        log.info("║  HcSendMsg Job 종료                                        ║");
        log.info("║  Status: {}                                              ║", jobExecution.getStatus());
        log.info("║  Exit Code: {}                                           ║", jobExecution.getExitStatus().getExitCode());
        log.info("║  Duration: {} ms ({} sec)                                ║", duration, duration / 1000);

        if (jobExecution.getAllFailureExceptions().size() > 0) {
            log.error("║  Failures: {}                                       ║",
                    jobExecution.getAllFailureExceptions().size());
            jobExecution.getAllFailureExceptions().forEach(e ->
                    log.error("║    - {}", e.getMessage()));
        }

        log.info("╚═══════════════════════════════════════════════════════════╝");
    }
}
