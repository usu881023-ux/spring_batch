# HC-BATCH Spring Batch 6.0 전환 프로젝트 요약

## 📌 전환 개요

### 기존 시스템 (Legacy)
- **프레임워크**: Anyframe Batch 1.0.0
- **Spring 버전**: 2.5.6
- **Spring Batch**: 1.1.4.RELEASE
- **Java**: 1.6~1.8
- **설정 방식**: XML 기반

### 신규 시스템 (Modernized)
- **프레임워크**: Spring Boot 4.0.1
- **Spring Batch**: 6.0.1
- **Java**: 25 (LTS)
- **ORM**: JPA (Hibernate) + QueryDSL 6.0
- **설정 방식**: Java Config + Annotation

---

## 🔄 주요 변경사항

### 1. API 변경

#### Job/Step 빌더
```java
// ❌ 기존 (Spring Batch 1.x)
@Bean
public Job job(JobBuilderFactory jobBuilderFactory) {
    return jobBuilderFactory.get("myJob")
        .start(step1())
        .build();
}

// ✅ 신규 (Spring Batch 6.x)
@Bean
public Job job(JobRepository jobRepository) {
    return new JobBuilder("myJob", jobRepository)
        .start(step1())
        .build();
}
```

#### Job Repository 설정
```java
// ❌ 기존 (XML)
<bean id="jobRepository" 
      class="com.sds.anyframe.batch.core.repository.AgentJobRepository"/>

// ✅ 신규 (Annotation)
@Configuration
@EnableBatchProcessing
@EnableJdbcJobRepository(
    dataSourceRef = "prvaDataSource",
    transactionManagerRef = "batchTransactionManager"
)
public class BatchConfig {
    // ...
}
```

### 2. 패키지 변경

| 항목 | 기존 (javax) | 신규 (jakarta) |
|-----|-------------|---------------|
| Servlet | javax.servlet.* | jakarta.servlet.* |
| Persistence | javax.persistence.* | jakarta.persistence.* |
| Validation | javax.validation.* | jakarta.validation.* |

### 3. 트랜잭션 관리

```java
// 기존: Anyframe의 자동 트랜잭션
public class OrderPrvaMask extends AnyframeAbstractTasklet {
    @Override
    public void execute(AnyframeItemReaderFactory readerFactory,
                       AnyframeItemWriterFactory writerFactory) {
        // 자동 트랜잭션 처리
    }
}

// 신규: 명시적 TransactionManager
@Bean
public Step orderPrvaMaskStep(JobRepository jobRepository,
                              PlatformTransactionManager transactionManager,
                              Tasklet tasklet) {
    return new StepBuilder("orderPrvaMaskStep", jobRepository)
        .tasklet(tasklet, transactionManager)
        .build();
}
```

---

## 🎯 Job 전환 상세

### Job 1: OrderPrvaMaskJob

#### 기존 구조
```xml
<!-- OrderPrvaMask_CFG.xml -->
<job id="OrderPrvaMaskJob">
    <step id="OrderPrvaMask" type="java" class="hc.batch.prva.OrderPrvaMask">
        <resources>
            <reader id="selectOrderFnshList" type="DB" url="DS_PRVA"/>
            <writer id="insertOrderTrsf" type="DB" url="DS_PRVA"/>
            <!-- ... 20개 이상의 Reader/Writer -->
        </resources>
    </step>
</job>
```

```java
// OrderPrvaMask.java
public class OrderPrvaMask extends AnyframeAbstractTasklet {
    @Override
    public void execute(AnyframeItemReaderFactory readerFactory,
                       AnyframeItemWriterFactory writerFactory) {
        String baseDt = ParamUtil.getParameter("BASE_DT");
        // ...
    }
}
```

#### 신규 구조
```java
// OrderPrvaMaskJobConfig.java
@Configuration
public class OrderPrvaMaskJobConfig {
    @Bean
    public Job orderPrvaMaskJob(JobRepository jobRepository) {
        return new JobBuilder("orderPrvaMaskJob", jobRepository)
            .listener(orderPrvaMaskListener)
            .start(orderPrvaMaskStep())
            .build();
    }
}

// OrderPrvaMaskTasklet.java
@Component
public class OrderPrvaMaskTasklet implements Tasklet {
    private final JdbcTemplate prvaJdbcTemplate;
    
    @Override
    public RepeatStatus execute(StepContribution contribution,
                               ChunkContext chunkContext) {
        String baseDt = getParameter(jobParameters, "BASE_DT", 
                                     DateUtil.getYesterday());
        // ...
        return RepeatStatus.FINISHED;
    }
}
```

#### 전환 이유: Tasklet 선택
1. **복잡한 비즈니스 로직**: 11개 테이블을 순차적으로 처리
2. **트랜잭션 범위**: 주문 단위 원자성 보장 필요
3. **다중 테이블 조작**: Reader-Processor-Writer 패턴 부적합

---

### Job 2: DayGoodsSumJob

#### 기존 구조
```xml
<!-- DayGoodsSum_CFG.xml -->
<job id="DayGoodsSumJob">
    <step id="DayGoodsSum" type="java" class="hc.batch.stcs.day.goods.DayGoodsSum">
        <resources>
            <writer id="insertDayGoodsSum" type="DB" url="DS_MALL"/>
        </resources>
    </step>
</job>
```

```java
// DayGoodsSum.java
public class DayGoodsSum extends AnyframeAbstractTasklet {
    @Override
    public void execute(AnyframeItemReaderFactory readerFactory,
                       AnyframeItemWriterFactory writerFactory) {
        insertDayGoodsSum.write();
    }
}
```

#### 신규 구조
```java
// DayGoodsSumJobConfig.java
@Configuration
public class DayGoodsSumJobConfig {
    @Bean
    public Job dayGoodsSumJob(JobRepository jobRepository) {
        return new JobBuilder("dayGoodsSumJob", jobRepository)
            .start(dayGoodsSumStep())
            .build();
    }
}

// DayGoodsSumTasklet.java
@Component
public class DayGoodsSumTasklet implements Tasklet {
    private final JdbcTemplate mallJdbcTemplate;
    
    @Override
    public RepeatStatus execute(StepContribution contribution,
                               ChunkContext chunkContext) {
        int insertedCount = insertDayGoodsSum(baseDt);
        contribution.incrementWriteCount(insertedCount);
        return RepeatStatus.FINISHED;
    }
}
```

#### 전환 이유: Tasklet 선택
1. **단순 집계**: 단일 INSERT 문으로 처리
2. **성능**: 대량 데이터를 한 번에 집계
3. **복잡도**: Chunk 방식의 오버헤드 불필요

---

## 🔧 기술 스택 비교

| 구분 | 기존 | 신규 |
|-----|------|------|
| **Spring Boot** | - | 4.0.1 |
| **Spring Framework** | 2.5.6 | 7.0 |
| **Spring Batch** | 1.1.4.RELEASE | 6.0.1 |
| **Java** | 1.6~1.8 | 25 (LTS) |
| **Jakarta EE** | - | 11 |
| **ORM** | - | JPA + QueryDSL |
| **Database** | Oracle JDBC | Oracle JDBC 23.6 |
| **Logging** | Log4j 1.2 | Logback + SLF4J |
| **Build** | Ant (Eclipse) | Gradle |
| **설정** | XML | Java Config + YAML |

---

## 📊 프로젝트 구조 비교

### 기존 구조
```
hc-batch/
├── config/
│   ├── batch.properties
│   ├── log4j.xml
│   └── spring/batch/
│       ├── batch-application-context.xml
│       └── data-source-context.xml
├── lib/
│   └── *.jar (수동 관리)
└── src/hc/batch/
    ├── common/
    ├── dvo/
    ├── prva/
    │   ├── OrderPrvaMask.java
    │   ├── OrderPrvaMask_CFG.xml
    │   └── OrderPrvaMask_SQL.xml
    └── stcs/
```

### 신규 구조
```
hc_job_batch/
├── src/main/
│   ├── java/com/company/hcbatch/
│   │   ├── config/              # 설정 클래스
│   │   ├── job/                 # Job 구현
│   │   ├── common/              # 공통 유틸
│   │   └── domain/              # Entity, Repository
│   └── resources/
│       ├── application.yml      # 통합 설정
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── logback-spring.xml
├── build.gradle                 # Gradle 빌드 설정
├── settings.gradle              # Gradle 프로젝트 설정
└── gradle.properties            # Gradle 속성
└── run-job.sh                   # 실행 스크립트
```

---

## ✅ 개선사항

### 1. 코드 품질
- **타입 안정성**: Generic, Lambda 사용
- **Null 안정성**: JSpecify 어노테이션
- **가독성**: Java Config > XML
- **테스트**: Spring Batch Test 지원

### 2. 성능
- **HikariCP**: 최신 Connection Pool
- **JPA 2차 캐시**: Hibernate 최적화
- **Batch Insert**: JDBC Batch 지원
- **Virtual Threads**: Java 25 기능 활용 가능

### 3. 운영
- **Profile 분리**: dev, prod 환경 독립
- **로깅 개선**: 구조화된 로그
- **모니터링**: Spring Batch 메타데이터 활용
- **재시작**: Job 재시작 메커니즘

### 4. 유지보수
- **의존성 관리**: Gradle로 자동화
- **버전 관리**: 명확한 버전 정책
- **문서화**: README, 주석 강화
- **표준화**: Spring 생태계 표준 준수

---

## 🚀 마이그레이션 체크리스트

### Phase 1: 준비 ✅
- [x] Spring Boot 4.0 프로젝트 생성
- [x] Spring Batch 6.0 설정
- [x] 다중 데이터소스 설정
- [x] JPA + QueryDSL 설정
- [x] 로깅 설정

### Phase 2: Job 전환 ✅
- [x] OrderPrvaMaskJob → Tasklet
- [x] DayGoodsSumJob → Tasklet
- [x] Job 파라미터 처리
- [x] 리스너 구현

### Phase 3: 테스트 (진행 예정)
- [ ] 단위 테스트 작성
- [ ] 통합 테스트 작성
- [ ] 성능 테스트
- [ ] 데이터 정합성 검증

### Phase 4: 운영 준비 (진행 예정)
- [ ] 스케줄러 연동
- [ ] 모니터링 설정
- [ ] 알림 체계 구축
- [ ] 운영 문서 작성

---

## 📝 다음 단계

1. **테스트 코드 작성**
   - JUnit 5 기반 단위 테스트
   - Spring Batch Test 통합 테스트

2. **실제 DB 스키마 매핑**
   - JPA Entity 클래스 작성
   - QueryDSL Q클래스 생성

3. **스케줄러 연동**
   - Cron 표현식 설정
   - Job 파라미터 자동 생성

4. **운영 환경 배포**
   - Docker 이미지 빌드
   - CI/CD 파이프라인 구축

---

## 💡 주요 학습 포인트

### 1. Spring Batch 6.0 신규 API
- JobBuilder, StepBuilder 사용법
- JobRepository 직접 주입
- @EnableJdbcJobRepository

### 2. Chunk vs Tasklet 선택
- 복잡한 비즈니스 로직 → Tasklet
- 단순 ETL → Chunk

### 3. 다중 데이터소스
- @Primary 지정
- @Qualifier로 구분

### 4. Java 25 기능
- Virtual Threads (Project Loom)
- Record Pattern Matching
- Scoped Values

---

**작성일**: 2025-02-05  
**버전**: 1.0  
**작성자**: AI Migration Team
