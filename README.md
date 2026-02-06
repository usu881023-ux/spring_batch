# HC-JOB-BATCH

HC Batch 프로젝트의 Spring Batch 6.0 전환 버전

## 📋 프로젝트 정보

### 기술 스택
- **Spring Boot**: 4.0.1
- **Spring Batch**: 6.0.1
- **Java**: 25 (LTS)
- **ORM**: JPA (Hibernate), QueryDSL 6.0, myBatis 3.5.16
- **Database**: Oracle
- **Logging**: Logback + SLF4J (Lombok @Slf4j)
- **Build Tool**: Gradle

### 주요 특징
- Spring Batch 6.0 신규 API 사용 (JobBuilder, StepBuilder)
- 다중 데이터소스 지원 (PRVA, MALL)
- JPA + QueryDSL 기반 데이터 처리
- 체계적인 로깅 시스템
- Profile 기반 환경 분리 (dev, prod)

---

## 🏗️ 프로젝트 구조

```
hc_job_batch/
├── src/main/java/
│   └── com/company/hcbatch/
│       ├── HcBatchApplication.java          # 메인 애플리케이션
│       ├── config/
│       │   ├── BatchConfig.java             # Spring Batch 설정
│       │   ├── DataSourceConfig.java        # 다중 데이터소스 설정
│       │   ├── JpaConfig.java               # JPA 설정
│       │   └── QueryDslConfig.java          # QueryDSL 설정
│       ├── job/
│       │   ├── prva/
│       │   │   ├── OrderPrvaMaskJobConfig.java
│       │   │   ├── OrderPrvaMaskTasklet.java
│       │   │   └── OrderPrvaMaskListener.java
│       │   └── stcs/
│       │       ├── DayGoodsSumJobConfig.java
│       │       └── DayGoodsSumTasklet.java
│       ├── common/
│       │   ├── constant/
│       │   │   └── BatchJobParameter.java
│       │   └── util/
│       │       └── DateUtil.java
│       └── domain/
│           ├── entity/                      # JPA Entity
│           └── repository/                  # JPA Repository
│
├── src/main/resources/
│   ├── application.yml                      # 기본 설정
│   ├── application-dev.yml                  # 개발 환경
│   ├── application-prod.yml                 # 운영 환경
│   └── logback-spring.xml                   # 로깅 설정
│
├── run-job.sh                               # Job 실행 스크립트
├── build.gradle                             # Gradle 빌드 설정
├── settings.gradle                          # Gradle 프로젝트 설정
└── gradle.properties                        # Gradle 속성
```

---

## 💼 배치 Job 목록

### 1. OrderPrvaMaskJob (개인정보 마스킹)

**Job 이름**: `orderPrvaMaskJob`

**설명**: 해피콘 선물하기 주문 완료/취소 개인정보 삭제 및 마스킹

**실행 주기**:
- 매일 02:10 - 미결재 취소건 처리 (PROC_CD=CNCL)
- 매일 03:10 - 주문 완료건 처리 (PROC_CD=COMP)

**파라미터**:
| 파라미터 | 필수 | 기본값 | 설명 |
|---------|-----|--------|------|
| BASE_DT | 선택 | 전일 | 처리 기준일자 (yyyyMMdd) |
| PROC_CD | 필수 | - | COMP: 완료건, CNCL: 취소건 |

**처리 대상 테이블** (11개):
1.주문
2.장바구니
3.주문배송
4.쿠폰정보
5.환불
6.환불이력
7.해피콘정보
8.해피콘복수
9.해피콘이력
10.해피콘반려
11.해피콘반려이력

**실행 예시**:
```bash
# 완료건 처리
./run-job.sh orderPrvaMaskJob BASE_DT=20250203 PROC_CD=COMP

# 취소건 처리
./run-job.sh orderPrvaMaskJob BASE_DT=20250203 PROC_CD=CNCL

# 전일 데이터 처리 (BASE_DT 생략)
./run-job.sh orderPrvaMaskJob PROC_CD=COMP
```

---

### 2. DayGoodsSumJob (상품별 판매현황 집계)

**Job 이름**: `dayGoodsSumJob`

**설명**: 해피콘 상품별 일 단위 판매현황 집계

**실행 주기**: 매일 새벽 (판매 데이터 확정 후)

**파라미터**:
| 파라미터 | 필수 | 기본값 | 설명 |
|---------|-----|--------|------|
| BASE_DT | 선택 | 전일 | 집계 기준일자 (yyyyMMdd) |

**처리 내용**:
- 일 단위 상품별 판매 집계
- S_DAY_GOODS_SALE_SUM 테이블에 저장

**실행 예시**:
```bash
# 특정 날짜 집계
./run-job.sh dayGoodsSumJob BASE_DT=20250203

# 전일 집계 (BASE_DT 생략)
./run-job.sh dayGoodsSumJob
```

---

## 🚀 빌드 및 실행

### 1. 프로젝트 빌드

```bash
# Gradle 빌드
./gradlew clean build

# 테스트 제외 빌드
./gradlew clean build -x test

# Bootable JAR 생성
./gradlew bootJar
```

### 2. 로컬 실행 (개발 환경)

```bash
# Profile: dev
java -jar target/hc_job_batch-1.0.0-SNAPSHOT.jar \
     --spring.profiles.active=dev \
     --spring.batch.job.name=orderPrvaMaskJob \
     BASE_DT=20250203 \
     PROC_CD=COMP
```

### 3. 운영 환경 실행

```bash
# Profile: prod
./run-job.sh orderPrvaMaskJob BASE_DT=20250203 PROC_CD=COMP
```

### 4. 특정 Job만 실행

```bash
# 환경변수로 Job 지정
export SPRING_BATCH_JOB_NAME=orderPrvaMaskJob
java -jar target/hc_job_batch-1.0.0-SNAPSHOT.jar BASE_DT=20250203 PROC_CD=COMP
```

---

## 🔧 환경 설정

### 데이터베이스 설정

**개발 환경** (`application-dev.yml`):
```yaml
spring:
  datasource:
    prva:
      jdbc-url: jdbc:oracle:thin:@localhost:1521:ORCL
      username: prva_user
      password: prva_password
    mall:
      jdbc-url: jdbc:oracle:thin:@localhost:1521:ORCL
      username: mall_user
      password: mall_password
```

**운영 환경** (`application-prod.yml`):
```yaml
spring:
  datasource:
    prva:
      jdbc-url: ${DB_PRVA_URL}
      username: ${DB_PRVA_USERNAME}
      password: ${DB_PRVA_PASSWORD}
    mall:
      jdbc-url: ${DB_MALL_URL}
      username: ${DB_MALL_USERNAME}
      password: ${DB_MALL_PASSWORD}
```

### 환경변수 설정 (운영)

```bash
export DB_PRVA_URL="jdbc:oracle:thin:@prod-db:1521:ORCL"
export DB_PRVA_USERNAME="prva_prod"
export DB_PRVA_PASSWORD="secure_password"

export DB_MALL_URL="jdbc:oracle:thin:@prod-db:1521:ORCL"
export DB_MALL_USERNAME="mall_prod"
export DB_MALL_PASSWORD="secure_password"
```

---

## 📊 Spring Batch 메타테이블

Spring Batch는 실행 이력 관리를 위해 다음 메타테이블을 사용합니다:

```sql
-- Oracle DDL
CREATE TABLE BATCH_JOB_INSTANCE (
    JOB_INSTANCE_ID NUMBER(19) NOT NULL PRIMARY KEY,
    VERSION NUMBER(19),
    JOB_NAME VARCHAR2(100) NOT NULL,
    JOB_KEY VARCHAR2(32) NOT NULL
);

CREATE TABLE BATCH_JOB_EXECUTION (
    JOB_EXECUTION_ID NUMBER(19) NOT NULL PRIMARY KEY,
    VERSION NUMBER(19),
    JOB_INSTANCE_ID NUMBER(19) NOT NULL,
    CREATE_TIME TIMESTAMP NOT NULL,
    START_TIME TIMESTAMP,
    END_TIME TIMESTAMP,
    STATUS VARCHAR2(10),
    EXIT_CODE VARCHAR2(2500),
    EXIT_MESSAGE VARCHAR2(2500),
    LAST_UPDATED TIMESTAMP,
    CONSTRAINT JOB_INST_EXEC_FK FOREIGN KEY (JOB_INSTANCE_ID)
        REFERENCES BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
);

CREATE TABLE BATCH_JOB_EXECUTION_PARAMS (
    JOB_EXECUTION_ID NUMBER(19) NOT NULL,
    PARAMETER_NAME VARCHAR2(100) NOT NULL,
    PARAMETER_TYPE VARCHAR2(100) NOT NULL,
    PARAMETER_VALUE VARCHAR2(2500),
    IDENTIFYING CHAR(1) NOT NULL,
    CONSTRAINT JOB_EXEC_PARAMS_FK FOREIGN KEY (JOB_EXECUTION_ID)
        REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);

CREATE TABLE BATCH_STEP_EXECUTION (
    STEP_EXECUTION_ID NUMBER(19) NOT NULL PRIMARY KEY,
    VERSION NUMBER(19) NOT NULL,
    STEP_NAME VARCHAR2(100) NOT NULL,
    JOB_EXECUTION_ID NUMBER(19) NOT NULL,
    CREATE_TIME TIMESTAMP NOT NULL,
    START_TIME TIMESTAMP,
    END_TIME TIMESTAMP,
    STATUS VARCHAR2(10),
    COMMIT_COUNT NUMBER(19),
    READ_COUNT NUMBER(19),
    FILTER_COUNT NUMBER(19),
    WRITE_COUNT NUMBER(19),
    READ_SKIP_COUNT NUMBER(19),
    WRITE_SKIP_COUNT NUMBER(19),
    PROCESS_SKIP_COUNT NUMBER(19),
    ROLLBACK_COUNT NUMBER(19),
    EXIT_CODE VARCHAR2(2500),
    EXIT_MESSAGE VARCHAR2(2500),
    LAST_UPDATED TIMESTAMP,
    CONSTRAINT JOB_EXEC_STEP_FK FOREIGN KEY (JOB_EXECUTION_ID)
        REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);

-- 시퀀스 생성
CREATE SEQUENCE BATCH_JOB_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ START WITH 1 INCREMENT BY 1;
```

---

## 📝 로깅

### 로그 파일 위치

- **애플리케이션 로그**: `logs/hc-batch.log`
- **배치 Job 로그**: `logs/batch-jobs.log`

### 로그 레벨 설정

```yaml
logging:
  level:
    root: INFO
    com.company.hcbatch: DEBUG
    org.springframework.batch: DEBUG
    org.hibernate.SQL: DEBUG
```

### 로그 확인

```bash
# 실시간 로그 확인
tail -f logs/batch-jobs.log

# 특정 Job 로그 검색
grep "orderPrvaMaskJob" logs/batch-jobs.log
```

---

## 🔍 모니터링

### Job 실행 이력 조회

```sql
-- 최근 실행된 Job 목록
SELECT 
    ji.JOB_NAME,
    je.JOB_EXECUTION_ID,
    je.STATUS,
    je.START_TIME,
    je.END_TIME,
    ROUND((je.END_TIME - je.START_TIME) * 24 * 60 * 60, 2) AS DURATION_SEC
FROM 
    BATCH_JOB_EXECUTION je
    JOIN BATCH_JOB_INSTANCE ji ON je.JOB_INSTANCE_ID = ji.JOB_INSTANCE_ID
ORDER BY 
    je.START_TIME DESC
FETCH FIRST 10 ROWS ONLY;

-- 실패한 Job 조회
SELECT 
    ji.JOB_NAME,
    je.JOB_EXECUTION_ID,
    je.EXIT_MESSAGE,
    je.START_TIME
FROM 
    BATCH_JOB_EXECUTION je
    JOIN BATCH_JOB_INSTANCE ji ON je.JOB_INSTANCE_ID = ji.JOB_INSTANCE_ID
WHERE 
    je.STATUS = 'FAILED'
ORDER BY 
    je.START_TIME DESC;
```

---

## 🧪 테스트

### 단위 테스트 실행

```bash
./gradlew test
```

### 통합 테스트

```bash
./gradlew integrationTest
```

---

## 📚 참고 자료

- [Spring Boot 4.0 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes)
- [Spring Batch 6.0 Documentation](https://docs.spring.io/spring-batch/docs/current/reference/html/)
- [QueryDSL Documentation](http://querydsl.com/static/querydsl/latest/reference/html/)

---

## 👥 기여

프로젝트 개선 사항이나 버그 리포트는 이슈로 등록해주세요.

---

## 📄 라이선스

Copyright © 2025 Company. All rights reserved.
