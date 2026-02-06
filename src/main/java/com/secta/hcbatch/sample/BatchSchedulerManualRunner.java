package com.secta.hcbatch.sample;

import com.secta.hcbatch.scheduler.BatchScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * BatchScheduler 수동 실행 샘플
 * 
 * 용도: 애플리케이션 시작 시 BatchScheduler.run() 메서드를 즉시 실행
 * 
 * 활성화/비활성화:
 * - 실행하려면: @Component 어노테이션 활성화
 * - 실행 안하려면: @Component 주석 처리
 */
@Slf4j
@Component  // <- 주석 해제하면 애플리케이션 시작 시 즉시 실행됨
@RequiredArgsConstructor
public class BatchSchedulerManualRunner implements CommandLineRunner {

    private final BatchScheduler batchScheduler;

    @Override
    public void run(String... args) throws Exception {
        log.info("===== BatchScheduler 수동 실행 시작 =====");
        
        // BatchScheduler의 run() 메서드 실행
        batchScheduler.run();
        
        log.info("===== BatchScheduler 수동 실행 완료 =====");
    }
}
