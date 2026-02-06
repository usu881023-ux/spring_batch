# BatchScheduler 실행 가이드

## 📋 목차
1. [개요](#개요)
2. [자동 스케줄 실행](#1-자동-스케줄-실행-운영-환경)
3. [애플리케이션 시작 시 즉시 실행](#2-애플리케이션-시작-시-즉시-실행)
4. [REST API를 통한 수동 실행](#3-rest-api를-통한-수동-실행)
5. [JUnit 테스트로 실행](#4-junit-테스트로-실행)
6. [실행 확인 방법](#5-실행-확인-방법)
7. [문제 해결](#6-문제-해결)

---

## 개요

`BatchScheduler.java`의 `run()` 메서드를 실행하는 다양한 방법을 설명합니다.

**파일 위치:**
- BatchScheduler: `src/main/java/com/secta/hcbatch/scheduler/BatchScheduler.java`
- 수동 실행 샘플: `src/main/java/com/secta/hcbatch/sample/BatchSchedulerManualRunner.java`
- REST API: `src/main/java/com/secta/hcbatch/controller/BatchController.java`
- 테스트: `src/test/java/com/secta/hcbatch/scheduler/BatchSchedulerTest.java`

---

## 1. 자동 스케줄 실행 (운영 환경)

### 설정
```java
@Scheduled(cron = "0 0 2 * * *")  // 매일 새벽 2시
public void run() {
    // 배치 실행
}
```

### 크론 표현식 예시
| 표현식 | 설명 |
|--------|------|
| `0 0 2 * * *` | 매일 새벽 2시 |
| `0 */10 * * * *` | 10분마다 |
| `0 0 0 * * MON` | 매주 월요일 자정 |
| `0 0 1 1 * *` | 매월 1일 새벽 1시 |
| `0 * * * * *` | 매분마다 (테스트용) |

### 실행 방법
1. `HcBatchApplication` 실행
2. 설정된 시간에 자동 실행됨
3. 별도의 조작 불필요

### 주의사항
- `@EnableScheduling` 어노테이션 필요 (이미 설정됨)
- 운영 환경에서는 적절한 크론 표현식 설정 필요

---

## 2. 애플리케이션 시작 시 즉시 실행

### 파일
`src/main/java/com/secta/hcbatch/sample/BatchSchedulerManualRunner.java`

### 활성화 방법
```java
// ✅ 활성화 (즉시 실행)
@Component
@RequiredArgsConstructor
public class BatchSchedulerManualRunner implements CommandLineRunner {
    // ...
}

// ❌ 비활성화 (실행 안 함)
//@Component  <- 주석 처리
@RequiredArgsConstructor
public class BatchSchedulerManualRunner implements CommandLineRunner {
    // ...
}
```

### 실행 방법
1. `BatchSchedulerManualRunner.java`에서 `@Component` 주석 해제
2. IntelliJ에서 `HcBatchApplication` 실행
3. 애플리케이션 시작 시 자동으로 `BatchScheduler.run()` 실행됨
4. 콘솔 로그 확인

### 로그 예시
```
===== BatchScheduler 수동 실행 시작 =====
========================================
배치 스케줄러 시작
========================================
Job 실행 시작: dayGoodsSumJob
Job 실행 완료: dayGoodsSumJob (executionId: 1)
========================================
배치 스케줄러 완료
========================================
===== BatchScheduler 수동 실행 완료 =====
```

---

## 3. REST API를 통한 수동 실행

### 파일
`src/main/java/com/secta/hcbatch/controller/BatchController.java`

### API 엔드포인트
```
POST http://localhost:8080/api/batch/run
POST http://localhost:8080/api/batch/status
```

### 실행 방법

#### 방법 1: IntelliJ HTTP Client (가장 간편)
1. `test-batch-scheduler.http` 파일 열기
2. POST 요청 옆 초록색 실행 버튼 클릭
3. 결과 확인

#### 방법 2: cURL
```bash
# BatchScheduler 실행
curl -X POST http://localhost:8080/api/batch/run

# 상태 조회
curl -X POST http://localhost:8080/api/batch/status
```

#### 방법 3: Postman
1. Method: `POST`
2. URL: `http://localhost:8080/api/batch/run`
3. Headers: `Content-Type: application/json`
4. Send 클릭

#### 방법 4: 웹 브라우저 개발자 도구
```javascript
fetch('http://localhost:8080/api/batch/run', {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    }
})
.then(response => response.json())
.then(data => console.log(data));
```

### 응답 예시

**성공:**
```json
{
  "success": true,
  "message": "배치 스케줄러가 성공적으로 실행되었습니다.",
  "timestamp": 1707194567890
}
```

**실패:**
```json
{
  "success": false,
  "message": "배치 스케줄러 실행 중 오류가 발생했습니다: ...",
  "timestamp": 1707194567890
}
```

---

## 4. JUnit 테스트로 실행

### 파일
`src/test/java/com/secta/hcbatch/scheduler/BatchSchedulerTest.java`

### 실행 방법

#### 방법 1: IntelliJ 실행 버튼 (가장 빠름)
1. `BatchSchedulerTest.java` 파일 열기
2. `testBatchSchedulerRun()` 메서드 옆 초록색 실행 버튼 클릭
3. 테스트 결과 확인

#### 방법 2: 단축키
- Windows/Linux: `Ctrl + Shift + F10`
- Mac: `Ctrl + Shift + R`

#### 방법 3: 전체 테스트 클래스 실행
- 클래스 이름 옆 실행 버튼 클릭

### 테스트 메서드
| 테스트 메서드 | 설명 |
|--------------|------|
| `testBatchSchedulerBeanLoaded()` | BatchScheduler Bean 로딩 확인 |
| `testBatchSchedulerRun()` | **BatchScheduler.run() 직접 실행** |
| `testJobOperatorBeanLoaded()` | JobOperator Bean 로딩 확인 |

---

## 5. 실행 확인 방법

### 5-1. 콘솔 로그 확인
```
2024-02-06 02:00:00.000  INFO --- [scheduling-1] c.s.h.scheduler.BatchScheduler : ========================================
2024-02-06 02:00:00.001  INFO --- [scheduling-1] c.s.h.scheduler.BatchScheduler : 배치 스케줄러 시작
2024-02-06 02:00:00.002  INFO --- [scheduling-1] c.s.h.scheduler.BatchScheduler : ========================================
2024-02-06 02:00:00.100  INFO --- [scheduling-1] c.s.h.scheduler.BatchScheduler : Job 실행 시작: dayGoodsSumJob
2024-02-06 02:00:02.345  INFO --- [scheduling-1] c.s.h.scheduler.BatchScheduler : Job 실행 완료: dayGoodsSumJob (executionId: 1)
2024-02-06 02:00:02.456  INFO --- [scheduling-1] c.s.h.scheduler.BatchScheduler : ========================================
2024-02-06 02:00:02.457  INFO --- [scheduling-1] c.s.h.scheduler.BatchScheduler : 배치 스케줄러 완료
2024-02-06 02:00:02.458  INFO --- [scheduling-1] c.s.h.scheduler.BatchScheduler : ========================================
```

### 5-2. 데이터베이스 확인 (Spring Batch 메타데이터)

```sql
-- Job 실행 이력 조회
SELECT * FROM BATCH_JOB_EXECUTION 
ORDER BY JOB_EXECUTION_ID DESC;

-- Step 실행 이력 조회
SELECT * FROM BATCH_STEP_EXECUTION 
ORDER BY STEP_EXECUTION_ID DESC;

-- Job Instance 조회
SELECT * FROM BATCH_JOB_INSTANCE 
ORDER BY JOB_INSTANCE_ID DESC;

-- 최근 실행된 Job 상세 정보
SELECT 
    ji.JOB_INSTANCE_ID,
    ji.JOB_NAME,
    je.JOB_EXECUTION_ID,
    je.START_TIME,
    je.END_TIME,
    je.STATUS,
    je.EXIT_CODE
FROM BATCH_JOB_INSTANCE ji
INNER JOIN BATCH_JOB_EXECUTION je ON ji.JOB_INSTANCE_ID = je.JOB_INSTANCE_ID
ORDER BY je.START_TIME DESC
FETCH FIRST 10 ROWS ONLY;
```

---

## 6. 문제 해결

### 6-1. BatchScheduler Bean을 찾을 수 없는 경우

**증상:**
```
NoSuchBeanDefinitionException: No qualifying bean of type 'BatchScheduler'
```

**해결 방법:**
1. `@Component` 어노테이션 확인
2. 패키지 스캔 범위 확인
   ```java
   @SpringBootApplication
   @ComponentScan(basePackages = "com.secta.hcbatch")
   public class HcBatchApplication {
       // ...
   }
   ```

### 6-2. Job을 찾을 수 없는 경우

**증상:**
```
NoSuchJobException: No job configuration with the name [dayGoodsSumJob] was registered
```

**해결 방법:**
1. Job 설정 클래스에 `@Configuration` 확인
2. Job Bean의 이름 확인
   ```java
   @Bean
   public Job dayGoodsSumJob() {  // <- 이름이 일치해야 함
       // ...
   }
   ```

### 6-3. 스케줄이 동작하지 않는 경우

**증상:**
- 설정된 시간에 배치가 실행되지 않음

**해결 방법:**
1. `@EnableScheduling` 어노테이션 확인
   ```java
   @SpringBootApplication
   @EnableScheduling  // <- 필수!
   public class HcBatchApplication {
       // ...
   }
   ```
2. 크론 표현식 확인
3. 프로파일 설정 확인

### 6-4. JobOperator 실행 실패

**증상:**
```
JobInstanceAlreadyCompleteException: A job instance already exists
```

**해결 방법:**
- JobParameters를 매번 다르게 설정 (현재 코드에서는 timestamp 사용 중)
- 또는 DB에서 기존 실행 이력 삭제

---

## 7. 추천 실행 방법

### 개발 환경 (로컬)
1. **JUnit 테스트 실행** ⭐ (가장 빠르고 편리)
   - `BatchSchedulerTest.java` → `testBatchSchedulerRun()` 실행
   
2. **REST API 실행**
   - `test-batch-scheduler.http` 파일 사용

### 운영 환경 (Production)
1. **자동 스케줄 실행** ⭐ (가장 안정적)
   - Cron 설정으로 자동 실행
   
2. **REST API 수동 실행** (긴급 시)
   - 필요 시 수동으로 실행

### 디버깅/테스트
1. **CommandLineRunner** (초기 테스트)
   - 애플리케이션 시작 시 즉시 실행
   
2. **JUnit 테스트** (단위 테스트)
   - 빠른 반복 테스트

---

## 8. 주요 설정 파일

### 8-1. application-local.yml
```yaml
spring:
  batch:
    job:
      enabled: false  # 자동 실행 방지 (수동 실행만)
    jdbc:
      initialize-schema: always
      
logging:
  level:
    com.secta.hcbatch: DEBUG
```

### 8-2. application-prod.yml
```yaml
spring:
  batch:
    job:
      enabled: false  # 스케줄러로만 실행
    jdbc:
      initialize-schema: never
      
logging:
  level:
    com.secta.hcbatch: INFO
```

---

## 9. 빠른 시작 가이드

### 🚀 30초 안에 실행하기

#### Step 1: 프로젝트 실행
```bash
./gradlew bootRun
```

#### Step 2: 테스트 실행 (가장 간단)
```bash
./gradlew test --tests BatchSchedulerTest.testBatchSchedulerRun
```

또는

#### Step 2: REST API 실행
```bash
curl -X POST http://localhost:8080/api/batch/run
```

---

## 📞 참고 자료

- Spring Batch 공식 문서: https://spring.io/projects/spring-batch
- Spring Scheduling 가이드: https://spring.io/guides/gs/scheduling-tasks/
- 프로젝트 README: `PROJECT_SUMMARY.md`
