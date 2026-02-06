package com.secta.hcbatch.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL 설정
 */
@Slf4j
@Configuration
public class QueryDslConfig {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * JPAQueryFactory Bean 등록
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        log.info("Initializing JPAQueryFactory");
        return new JPAQueryFactory(entityManager);
    }
}
