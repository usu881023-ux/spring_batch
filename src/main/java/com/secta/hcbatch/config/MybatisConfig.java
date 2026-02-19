package com.secta.hcbatch.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * MyBatis 설정 (다중 데이터소스)
 * - PRVA: 개인정보 처리용 DB
 * - MALL: 쇼핑몰 DB
 *
 * 다중 DataSource 환경에서는 SqlSessionTemplate을 통해 Mapper를 가져와서 사용
 * 예: sqlSession.getMapper(XxxMapper.class)
 */
@Slf4j
@Configuration
public class MybatisConfig {

    private static final String MAPPER_LOCATION = "classpath:mapper/**/*.xml";
    private static final String TYPE_ALIASES_PACKAGE = "com.secta.hcbatch.entity";

    /**
     * PRVA SqlSessionFactory
     */
    @Primary
    @Bean(name = "prvaSqlSessionFactory")
    public SqlSessionFactory prvaSqlSessionFactory(
            @Qualifier("prvaDataSource") DataSource dataSource) throws Exception {
        log.info("Initializing PRVA SqlSessionFactory");
        return createSqlSessionFactory(dataSource);
    }

    /**
     * MALL SqlSessionFactory
     */
    @Bean(name = "mallSqlSessionFactory")
    public SqlSessionFactory mallSqlSessionFactory(
            @Qualifier("mallDataSource") DataSource dataSource) throws Exception {
        log.info("Initializing MALL SqlSessionFactory");
        return createSqlSessionFactory(dataSource);
    }

    /**
     * HCON SqlSessionFactory (해피콘 DB)
     */
    @Bean(name = "hconSqlSessionFactory")
    public SqlSessionFactory hconSqlSessionFactory(
            @Qualifier("hconDataSource") DataSource dataSource) throws Exception {
        log.info("Initializing HCON SqlSessionFactory");
        return createSqlSessionFactory(dataSource);
    }

    /**
     * PRVA SqlSessionTemplate
     */
    @Primary
    @Bean(name = "prvaSqlSessionTemplate")
    public SqlSessionTemplate prvaSqlSessionTemplate(
            @Qualifier("prvaSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    /**
     * MALL SqlSessionTemplate
     */
    @Bean(name = "mallSqlSessionTemplate")
    public SqlSessionTemplate mallSqlSessionTemplate(
            @Qualifier("mallSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    /**
     * HCON SqlSessionTemplate (해피콘 DB)
     */
    @Bean(name = "hconSqlSessionTemplate")
    public SqlSessionTemplate hconSqlSessionTemplate(
            @Qualifier("hconSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    /**
     * PRVA Batch SqlSessionTemplate (ExecutorType.BATCH)
     * - INSERT/UPDATE를 모아서 일괄 flush하여 DB round-trip 최소화
     */
    @Bean(name = "prvaBatchSqlSessionTemplate")
    public SqlSessionTemplate prvaBatchSqlSessionTemplate(
            @Qualifier("prvaSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        log.info("Initializing PRVA Batch SqlSessionTemplate (ExecutorType.BATCH)");
        return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
    }

    /**
     * HCON Batch SqlSessionTemplate (ExecutorType.BATCH)
     */
    @Bean(name = "hconBatchSqlSessionTemplate")
    public SqlSessionTemplate hconBatchSqlSessionTemplate(
            @Qualifier("hconSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        log.info("Initializing HCON Batch SqlSessionTemplate (ExecutorType.BATCH)");
        return new SqlSessionTemplate(sqlSessionFactory, ExecutorType.BATCH);
    }

    /**
     * SqlSessionFactory 생성 헬퍼 메서드
     */
    private SqlSessionFactory createSqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver().getResources(MAPPER_LOCATION));
        factoryBean.setTypeAliasesPackage(TYPE_ALIASES_PACKAGE);

        // MyBatis 설정
        org.apache.ibatis.session.Configuration configuration = new org.apache.ibatis.session.Configuration();
        configuration.setMapUnderscoreToCamelCase(true);
        configuration.setCallSettersOnNulls(true);
        configuration.setUseGeneratedKeys(false);  // INSERT SELECT 문에서 RETURNING 절 방지
        factoryBean.setConfiguration(configuration);

        return factoryBean.getObject();
    }
}
