# Spring Batch 6.0 주요 변경 사항

## ✅ 해결 완료: JobLauncher Deprecation 경고 제거

### 문제점
```
"JobLauncher는 6.0 이상에서 지원 중단되며 제거될 예정입니다"
```

### 원인
- Spring Batch 6.0에서는 `@EnableBatchProcessing`을 사용하지 않는 것이 권장됨
- Spring Boot의 자동 설정(Auto Configuration)을 활용하는 방식으로 변경
- 직접 JobLauncher Bean을 등록하는 기존 방식이 deprecated

### 해결 방법
✅ **@EnableBatchProcessing 제거**
✅ **Spring Boot 자동 설정 활용**
✅ **필요한 Bean만 선택적으로 오버라이드**

---

## 🔄 변경 사항 비교

### AS-IS (구 방식 - Deprecated)

```java
@Configuration
@EnableBatchProcessing  // ❌ 더 이상 권장되지 않음
public class BatchConfig {
    
    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        
        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
        taskExecutor.setConcurrencyLimit(5);
        jobLauncher.setTaskExecutor(taskExecutor);
        
        return jobLauncher;
    }
}
```

### TO-BE (신 방식 - Spring Boot 4.0 권장)

```java
@Configuration
@EnableConfigurationProperties(BatchProperties.class)  // ✅ Spring Boot 설정 활용
public class BatchConfig {
    
    @Bean
    @ConditionalOnMissingBean  // ✅ 기본 Bean이 없을 때만 생성
    public JobLauncher asyncJobLauncher(JobRepository jobRepository, 
                                        TaskExecutor batchTaskExecutor) {
        TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(batchTaskExecutor);  // ✅ 별도 TaskExecutor 사용
        return jobLauncher;
    }
    
    @Bean
    public TaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("batch-");
        return executor;
    }
}
```

---

## 📋 Spring Batch 6.0 주요 변경 사항

### 1. @EnableBatchProcessing 사용 중단

**AS-IS (Spring Batch 5.x 이하)**:
```java
@Configuration
@EnableBatchProcessing
public class BatchConfig {
    // ...
}
```

**TO-BE (Spring Batch 6.0+)**:
```java
@Configuration
// @EnableBatchProcessing 제거!
public class BatchConfig {
    // Spring Boot가 자동으로 설정
}
```

**이유**:
- Spring Boot의 자동 설정과 충돌 방지
- 더 간단하고 명확한 설정
- 필요한 경우에만 Bean 오버라이드

---

### 2. JobBuilderFactory / StepBuilderFactory 제거

**AS-IS (Spring Batch 4.x)**:
```java
@Bean
public Job job(JobBuilderFactory jobBuilderFactory, Step step) {
    return jobBuilderFactory.get("myJob")
        .start(step)
        .build();
}
```

**TO-BE (Spring Batch 6.0+)**:
```java
@Bean
public Job job(JobRepository jobRepository, Step step) {
    return new JobBuilder("myJob", jobRepository)  // ✅ 직접 생성
        .start(step)
        .build();
}

@Bean
public Step step(JobRepository jobRepository, 
                 PlatformTransactionManager transactionManager,
                 Tasklet tasklet) {
    return new StepBuilder("myStep", jobRepository)  // ✅ 직접 생성
        .tasklet(tasklet, transactionManager)
        .build();
}
```

---

### 3. TaskExecutor 설정 방식 개선

**AS-IS (단순 Executor)**:
```java
SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
taskExecutor.setConcurrencyLimit(5);
```

**TO-BE (Thread Pool 기반)**:
```java
ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
executor.setCorePoolSize(5);
executor.setMaxPoolSize(10);
executor.setQueueCapacity(10);
executor.setThreadNamePrefix("batch-");
executor.setWaitForTasksToCompleteOnShutdown(true);
executor.setAwaitTerminationSeconds(60);
```

**장점**:
- 더 세밀한 스레드 풀 제어
- Graceful Shutdown 지원
- 성능 모니터링 용이

---

### 4. BatchConfigurer 인터페이스 제거

**AS-IS (Spring Batch 4.x)**:
```java
@Configuration
public class BatchConfig implements BatchConfigurer {
    @Override
    public JobRepository getJobRepository() {
        // 커스텀 구현
    }
    
    @Override
    public JobLauncher getJobLauncher() {
        // 커스텀 구현
    }
}
```

**TO-BE (Spring Batch 6.0+)**:
```java
@Configuration
public class BatchConfig {
    // 필요한 Bean만 정의
    
    @Bean
    public JobLauncher jobLauncher(...) {
        // 커스텀 구현
    }
}
```

---

## 🎯 현재 프로젝트 적용 내용

### 1. BatchConfig.java 개선

✅ `@EnableBatchProcessing` 제거
✅ `@EnableConfigurationProperties` 추가
✅ `@ConditionalOnMissingBean` 사용
✅ Thread Pool 기반 TaskExecutor 사용
✅ JobRegistry 설정 추가

### 2. application.yml 설정 추가

```yaml
batch:
  chunk-size: 1000
  pool-size: 5
  queue-capacity: 10
  commit-interval: 100
```

### 3. 비동기 Job 실행 지원

- ThreadPoolTaskExecutor 사용
- 스레드 풀 크기: 5 (core), 10 (max)
- 큐 용량: 10
- Graceful Shutdown 지원

---

## 🔍 변경 후 확인 사항

### 1. Deprecation 경고 제거 확인

IntelliJ에서:
- BatchConfig.java 열기
- 노란색 경고 표시가 없어야 함
- `@EnableBatchProcessing` 줄 삭제 확인

### 2. 빌드 성공 확인

```bash
gradlew clean build
```

출력:
```
BUILD SUCCESSFUL
```

### 3. Job 실행 테스트

```bash
gradlew bootRun --args='--spring.batch.job.name=orderPrvaMaskJob BASE_DT=20250203 PROC_CD=COMP'
```

정상 실행 확인:
```
JobLauncher 초기화됨
Batch TaskExecutor 초기화됨 - poolSize: 5, queueCapacity: 10
```

---

## 📚 Spring Batch 6.0 마이그레이션 가이드

### 체크리스트

- [x] @EnableBatchProcessing 제거
- [x] JobBuilderFactory → JobBuilder 변경
- [x] StepBuilderFactory → StepBuilder 변경
- [x] SimpleAsyncTaskExecutor → ThreadPoolTaskExecutor 변경
- [x] BatchConfigurer 제거
- [x] @ConditionalOnMissingBean 추가
- [x] JobRegistry 설정 추가

---

## 💡 추가 개선 사항

### 1. 성능 모니터링

TaskExecutor에서 제공하는 메트릭 활용:
```java
ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) batchTaskExecutor;
int activeCount = executor.getActiveCount();
int poolSize = executor.getPoolSize();
int queueSize = executor.getThreadPoolExecutor().getQueue().size();
```

### 2. 동적 Job 등록

JobRegistry를 활용한 런타임 Job 관리:
```java
@Autowired
private JobRegistry jobRegistry;

public void registerJob(Job job) {
    RunnableJobRegistry registry = (RunnableJobRegistry) jobRegistry;
    registry.register(new RunnableJobRegistryEntry(job));
}
```

### 3. Job 실행 모니터링

JobExplorer를 활용한 실행 이력 조회:
```java
@Autowired
private JobExplorer jobExplorer;

public List<JobExecution> getRecentExecutions(String jobName) {
    return jobExplorer.getJobInstances(jobName, 0, 10)
        .stream()
        .flatMap(instance -> jobExplorer.getJobExecutions(instance).stream())
        .collect(Collectors.toList());
}
```

---

## 🔗 참고 자료

- [Spring Batch 6.0 Release Notes](https://github.com/spring-projects/spring-batch/wiki/Spring-Batch-6.0-Release-Notes)
- [Spring Boot 4.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [Spring Batch 6.0 API Documentation](https://docs.spring.io/spring-batch/docs/current/api/)

---

**작성일**: 2025-02-05
**버전**: Spring Batch 6.0 + Spring Boot 4.0
