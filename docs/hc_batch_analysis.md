# hc.batch 프로젝트 분석 보고서

## 1. 프로젝트 개요

| 항목 | 내용 |
|------|------|
| 프로젝트명 | hc.batch |
| 프로젝트 경로 | D:\IntelliJProject\hc_batch |
| 프레임워크 | Spring 4.1 + Quartz + MyBatis |
| 스케줄링 | Quartz Scheduler (XML 기반 설정) |
| 데이터베이스 | Oracle (JNDI DataSource) |
| 배포 형태 | WAR (Tomcat) |

---

## 2. 패키지 구조

```
src/main/java/
├── hc/
│   ├── batch/
│   │   ├── message/                    # MMS 발송 배치
│   │   │   ├── service/
│   │   │   │   ├── HcSendMsgTask.java      # Task 진입점
│   │   │   │   └── impl/
│   │   │   │       ├── HcSendMsgSvc.java   # 비즈니스 로직
│   │   │   │       └── HcMsgDao.java       # DAO
│   │   │   └── worker/
│   │   │       └── VchrSenderWorker.java   # Quartz Worker
│   │   │
│   │   └── voucher/                    # 상품권 동기화 배치
│   │       ├── service/
│   │       │   ├── HconSyncTask.java       # Task 진입점
│   │       │   └── impl/
│   │       │       ├── HconSyncSvc.java    # 비즈니스 로직
│   │       │       ├── HcTaskDao.java      # HC DB DAO
│   │       │       └── HconTaskDao.java    # HCON DB DAO
│   │       └── worker/
│   │           └── VchrSyncWorker.java     # Quartz Worker
│   │
│   └── common/
│       ├── aspect/
│       │   ├── HcBatchException.java       # 커스텀 예외
│       │   └── MdcInterceptor.java         # MDC 인터셉터
│       ├── socket/
│       │   └── HttpManager.java            # HTTP 클라이언트
│       └── util/
│           ├── AwsS3Utils.java             # S3 유틸리티
│           ├── BizUtils.java               # 비즈니스 유틸리티
│           ├── ConstantsUtils.java         # 상수 정의
│           ├── DateUtils.java              # 날짜 유틸리티
│           ├── DecimalUtils.java           # 숫자 유틸리티
│           ├── FileUtils.java              # 파일 유틸리티
│           ├── SocketUtils.java            # 소켓 유틸리티
│           └── StringUtils.java            # 문자열 유틸리티

src/main/resources/
├── spring/
│   ├── context-annotation.xml
│   ├── context-mybatis.xml                 # MyBatis 설정
│   ├── context-quartz.xml                  # Quartz 스케줄러 설정
│   ├── context-transaction.xml             # 트랜잭션 설정
│   └── core-servlet.xml
└── sql/
    ├── hc-mapping.xml                      # HC DB 매핑
    ├── hcon-mapping.xml                    # HCON DB 매핑
    ├── mapper-hcon.xml                     # 해피콘 쿼리
    ├── mapper-message.xml                  # MMS 쿼리
    └── mapper-vchr.xml                     # 상품권 쿼리
```

---

## 3. 배치 Job 목록

### 3.1 VchrSenderWorker (MMS 발송)

| 항목 | 내용 |
|------|------|
| Worker | `VchrSenderWorker.java` |
| Task | `HcSendMsgTask.java` |
| Service | `HcSendMsgSvc.java` |
| DAO | `HcMsgDao.java` |
| Cron | `0/7 * * * * ?` (7초 주기) |
| 상태 | **활성화됨** |

**처리 흐름:**
1. Lock 파일(`hcon_msg_send.lock`)에서 기준 타임스탬프 읽기
2. `T_HAPPYCON_MMS` 테이블에서 발송 대상 조회
3. S3에서 MMS 이미지 다운로드
4. `EM_TRAN`, `EM_TRAN_MMS` 테이블에 발송 데이터 INSERT
5. 오류 시 주문 취소 API 호출
6. Lock 파일 업데이트

### 3.2 VchrSyncWorker (상품권 동기화)

| 항목 | 내용 |
|------|------|
| Worker | `VchrSyncWorker.java` |
| Task | `HconSyncTask.java` |
| Service | `HconSyncSvc.java` |
| DAO | `HcTaskDao.java`, `HconTaskDao.java` |
| Cron | `0/5 * * * * ?` (5초 주기) |
| 상태 | **비활성화됨** (주석 처리) |

**처리 흐름:**
1. Lock 파일(`hcon_vchr_trig.lock`)에서 기준 타임스탬프 읽기
2. HCON DB의 `CPN_HIST` 테이블에서 변경 이력 조회
3. 변경 유형(chgClCd)에 따른 상태 처리:
   - 사용/사용취소: AD, AC, MAD, MAC, ADNC, ACNC
   - 폐기: RR, CRR, C, CC, CCI, CCCI, VCCI, CVCCI, PC, RC, SC, SCRC, CTI
   - 유효기간 연장: VC, CVC
4. HC DB의 `T_HAPPYCON_COUPON` 테이블 UPDATE
5. `H_HAPPYCON_COUPON_HST` 테이블에 이력 INSERT
6. Lock 파일 업데이트

---

## 4. 데이터베이스 구성

### 4.1 DataSource

| DataSource | JNDI | 용도 |
|------------|------|------|
| hcSource | jdbc/hc | HC Mall DB |
| hconSource | jdbc/con | 해피콘 DB |

### 4.2 주요 테이블

**HC Mall DB (hcSource):**
| 테이블명 | 설명 |
|----------|------|
| T_ORDER | 주문 정보 |
| T_HAPPYCON_COUPON | 해피콘 쿠폰 정보 |
| T_HAPPYCON_MMS | MMS 발송 트리거 |
| H_HAPPYCON_COUPON_HST | 쿠폰 변경 이력 |
| EM_TRAN | MMS 발송 큐 |
| EM_TRAN_MMS | MMS 이미지 정보 |

**해피콘 DB (hconSource):**
| 테이블명 | 설명 |
|----------|------|
| CPN_HIST | 쿠폰 변경 이력 (원본) |

---

## 5. 외부 연동

### 5.1 AWS S3
- **용도:** MMS 이미지 저장/조회
- **Bucket:** happy-objects
- **Region:** ap-northeast-2
- **인증:** AccessKey/SecretKey (ConstantsUtils에서 관리)

### 5.2 HC API
- **운영:** https://hc.happypointcard.com
- **개발:** https://dev-hc.happypointcard.com
- **용도:** 주문 취소 API 호출 (`/api/order-cancel`)

### 5.3 해피콘 암복호화
- **라이브러리:** BCipher (libbcipher.so/dll)
- **용도:** 상품권 번호 복호화

---

## 6. 주요 특징

### 6.1 Lock 파일 기반 상태 관리
- 파일 경로: `${CATALINA_HOME}/log/lock/`
- `hcon_msg_send.lock`: MMS 발송 기준점
- `hcon_vchr_trig.lock`: 동기화 기준점
- 형식: `타임스탬프,시퀀스번호`

### 6.2 타임스탬프 기반 증분 처리
- Oracle TIMESTAMP 사용 (FF9 = 나노초 9자리)
- 형식: `YYYYMMDDHH24MISSFF9` (23자리)

### 6.3 트랜잭션 관리
- `@Transactional` 어노테이션 사용
- DataSource별 별도 TransactionManager

---

## 7. 이관 대상 목록

### 7.1 즉시 이관 가능 (우선순위 높음)

| 구분 | 원본 | 대상 (hc_job_batch) |
|------|------|---------------------|
| MMS Task | HcSendMsgTask | HcSendMsgTasklet |
| MMS Service | HcSendMsgSvc | HcSendMsgService |
| MMS DAO | HcMsgDao | HcMsgMapper |
| 동기화 Task | HconSyncTask | HconSyncTasklet |
| 동기화 Service | HconSyncSvc | HconSyncService |
| 동기화 DAO | HcTaskDao, HconTaskDao | HcTaskMapper, HconTaskMapper |

### 7.2 공통 유틸리티 이관

| 원본 | 대상 | 비고 |
|------|------|------|
| DateUtils | DateUtil | 기존 DateUtil 확장 |
| StringUtils | StringUtil | 신규 생성 |
| FileUtils | FileUtil | 신규 생성 |
| BizUtils | BizUtil | 신규 생성 |
| AwsS3Utils | AwsS3Util | 신규 생성 |
| SocketUtils | HttpUtil | 신규 생성 |
| HcBatchException | BatchException | 기존 확장 |
| ConstantsUtils | application.yml | 설정 파일로 이관 |

### 7.3 SQL Mapper 이관

| 원본 | 대상 |
|------|------|
| mapper-message.xml | HcMsgMapper.xml |
| mapper-vchr.xml | HcTaskMapper.xml |
| mapper-hcon.xml | HconTaskMapper.xml |

---

## 8. 이관 시 고려사항

### 8.1 프레임워크 변환
| 항목 | 원본 (hc.batch) | 대상 (hc_job_batch) |
|------|-----------------|---------------------|
| 스케줄링 | Quartz XML 설정 | Spring Batch Job + @Scheduled |
| DI 방식 | @Inject/@Named | @Autowired/@Qualifier |
| 로깅 | Log4j 1.x | Slf4j + Logback |
| 트랜잭션 | XML AOP | @Transactional |

### 8.2 Lock 파일 처리
- 기존: 파일 기반 상태 관리
- 권장: DB 테이블 또는 Redis 기반으로 변환 검토

### 8.3 환경 변수
| 원본 | 대상 |
|------|------|
| System.getProperty("CATALINA_HOME") | application.yml 설정 |
| System.getProperty("MMS_PATH") | application.yml 설정 |
| System.getProperty("RMODE") | spring.profiles.active |
| JNDI DataSource | HikariCP DataSource |

---

## 9. 이관 작업 체크리스트

- [ ] Job 설정 클래스 생성 (HcSendMsgJobConfig, HconSyncJobConfig)
- [ ] Tasklet 클래스 생성
- [ ] Service 클래스 변환
- [ ] Mapper Interface 생성
- [ ] SQL XML 이관 및 수정
- [ ] 공통 유틸리티 이관
- [ ] application.yml 설정 추가
- [ ] Lock 파일 처리 방식 결정
- [ ] 테스트 코드 작성
- [ ] 통합 테스트

---

## 10. 참고 정보

### 10.1 파일 수
- Java 파일: 20개
- XML 설정 파일: 11개
- SQL Mapper: 5개

### 10.2 의존성 라이브러리
- anyframe-core (org.anyframe.util.StringUtil 사용)
- commons-codec (Base64)
- commons-lang3 (StringUtils, RandomStringUtils)
- json-simple (JSONObject)
- AWS SDK v2 (S3Client)
- Apache HttpClient
- BCipher (해피콘 암복호화)

---

*분석일: 2026-02-09*
*분석 대상: hc.batch 프로젝트*
