package com.secta.hcbatch.controller;

import com.secta.hcbatch.scheduler.BatchScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 배치 작업 수동 실행 API
 * 
 * 용도: REST API를 통해 BatchScheduler.run() 메서드를 수동으로 실행
 */
@Slf4j
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
public class BatchController {

    private final BatchScheduler batchScheduler;

    /**
     * 배치 스케줄러 수동 실행
     * 
     * @return 실행 결과
     * 
     * 사용 예시:
     * POST http://localhost:8080/api/batch/run
     */
    @PostMapping("/run")
    public ResponseEntity<Map<String, Object>> runBatchScheduler() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            log.info("===== REST API를 통한 배치 스케줄러 수동 실행 요청 =====");
            
            // BatchScheduler의 run() 메서드 실행
            batchScheduler.run();
            
            response.put("success", true);
            response.put("message", "배치 스케줄러가 성공적으로 실행되었습니다.");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("배치 스케줄러 실행 중 오류 발생", e);
            
            response.put("success", false);
            response.put("message", "배치 스케줄러 실행 중 오류가 발생했습니다: " + e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 배치 작업 상태 조회
     * 
     * @return 상태 정보
     */
    @PostMapping("/status")
    public ResponseEntity<Map<String, Object>> getBatchStatus() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("status", "running");
        response.put("message", "배치 시스템이 정상 작동 중입니다.");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }
}
