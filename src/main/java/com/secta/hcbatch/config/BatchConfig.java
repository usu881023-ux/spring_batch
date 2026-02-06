package com.secta.hcbatch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.batch.autoconfigure.BatchProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Spring Batch 6.x 설정 (Spring Boot 4.0)
 * 
 * Spring Boot 4.0에서는 @EnableBatchProcessing을 사용하지 않습니다.
 * Spring Boot의 자동 설정(Auto Configuration)을 활용합니다.
 * 
 * 커스터마이징이 필요한 부분만 Bean으로 정의합니다.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(BatchProperties.class)
public class BatchConfig {

    @Value("${batch.chunk-size:1000}")
    private int chunkSize;

    @Value("${batch.pool-size:5}")
    private int poolSize;

    @Value("${batch.queue-capacity:10}")
    private int queueCapacity;

    /**
     * Batch 전용 TaskExecutor
     *
     * 1. JobRegistryBeanPostProcessor
     * Spring Batch 6에서 사실상 제거 수순
     * Boot 4에서는 JobRegistry 자체를 거의 쓰지 않음
     * Job 자동 등록은 ApplicationContext 기반으로 처리
     *
     * 2. TaskExecutorJobLauncher
     * Spring Batch 6에서 더 이상 public API로 권장되지 않음
     * Boot 4 Auto Configuration이 JobLauncher를 이미 생성
     * 커스터마이징은 TaskExecutor Bean만 제공하면 됨
     *
     * 3. JobLauncher 직접 Bean 정의
     * Boot 4 + Batch 6 조합에서는
     * → 직접 만들면 오히려 충돌
     */
    @Bean(name = "batchTaskExecutor")
    public TaskExecutor batchTaskExecutor() {
        log.info("Initializing Batch TaskExecutor - poolSize: {}, queueCapacity: {}",
                poolSize, queueCapacity);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(poolSize);
        executor.setMaxPoolSize(poolSize * 2);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("batch-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        return executor;
    }

    /**
     * Chunk Size Getter
     */
    public int getChunkSize() {
        return chunkSize;
    }
}