package com.secta.hcbatch.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * 다중 데이터소스 설정
 * - DS_PRVA: 개인정보 처리용 DB
 * - DS_MALL: 쇼핑몰 DB
 */
@Slf4j
@Configuration
public class DataSourceConfig {

    /**
     * PRVA 데이터소스 (Primary)
     */
    @Primary
    @Bean(name = "prvaDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.prva")
    public DataSource prvaDataSource() {
        log.info("Initializing PRVA DataSource");
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * MALL 데이터소스
     */
    @Bean(name = "mallDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.mall")
    public DataSource mallDataSource() {
        log.info("Initializing MALL DataSource");
        return DataSourceBuilder.create()
                .type(HikariDataSource.class)
                .build();
    }

    /**
     * PRVA JdbcTemplate
     */
    @Primary
    @Bean(name = "prvaJdbcTemplate")
    public JdbcTemplate prvaJdbcTemplate(@Qualifier("prvaDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * MALL JdbcTemplate
     */
    @Bean(name = "mallJdbcTemplate")
    public JdbcTemplate mallJdbcTemplate(@Qualifier("mallDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
