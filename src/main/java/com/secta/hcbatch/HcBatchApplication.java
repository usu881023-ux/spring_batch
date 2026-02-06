package com.secta.hcbatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * HC Batch Application
 * Spring Boot 4.0 + Spring Batch 6.0
 */
@SpringBootApplication
@EnableScheduling
public class HcBatchApplication {

    public static void main(String[] args) {
        SpringApplication.run(HcBatchApplication.class, args);
    }
}
