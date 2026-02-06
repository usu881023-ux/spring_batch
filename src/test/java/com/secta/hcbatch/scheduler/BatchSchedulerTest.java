package com.secta.hcbatch.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
}
