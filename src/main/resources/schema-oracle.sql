-- Spring Batch 6.0 메타데이터 테이블 (Oracle)
-- 
-- 이 스크립트는 Spring Batch가 Job 실행 이력을 관리하기 위해 필요한
-- 메타데이터 테이블과 시퀀스를 생성합니다.
--
-- 실행 방법:
--   sqlplus username/password@database @schema-oracle.sql

-- ========================================================================
-- BATCH_JOB_INSTANCE
-- Job의 논리적 실행 단위를 나타냅니다.
-- 동일한 Job과 파라미터 조합은 하나의 인스턴스를 갖습니다.
-- ========================================================================
CREATE TABLE BATCH_JOB_INSTANCE (
    JOB_INSTANCE_ID NUMBER(19) NOT NULL PRIMARY KEY,
    VERSION NUMBER(19),
    JOB_NAME VARCHAR2(100) NOT NULL,
    JOB_KEY VARCHAR2(32) NOT NULL,
    CONSTRAINT JOB_INST_UN UNIQUE (JOB_NAME, JOB_KEY)
);

-- ========================================================================
-- BATCH_JOB_EXECUTION
-- Job의 물리적 실행 정보를 저장합니다.
-- 하나의 Job Instance는 여러 번 실행(Execution)될 수 있습니다.
-- ========================================================================
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

-- ========================================================================
-- BATCH_JOB_EXECUTION_PARAMS
-- Job 실행 시 전달된 파라미터를 저장합니다.
-- ========================================================================
CREATE TABLE BATCH_JOB_EXECUTION_PARAMS (
    JOB_EXECUTION_ID NUMBER(19) NOT NULL,
    PARAMETER_NAME VARCHAR2(100) NOT NULL,
    PARAMETER_TYPE VARCHAR2(100) NOT NULL,
    PARAMETER_VALUE VARCHAR2(2500),
    IDENTIFYING CHAR(1) NOT NULL,
    CONSTRAINT JOB_EXEC_PARAMS_FK FOREIGN KEY (JOB_EXECUTION_ID)
        REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);

-- ========================================================================
-- BATCH_STEP_EXECUTION
-- Step의 실행 정보를 저장합니다.
-- ========================================================================
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

-- ========================================================================
-- BATCH_STEP_EXECUTION_CONTEXT
-- Step 실행 컨텍스트 정보를 저장합니다.
-- ========================================================================
CREATE TABLE BATCH_STEP_EXECUTION_CONTEXT (
    STEP_EXECUTION_ID NUMBER(19) NOT NULL PRIMARY KEY,
    SHORT_CONTEXT VARCHAR2(2500) NOT NULL,
    SERIALIZED_CONTEXT CLOB,
    CONSTRAINT STEP_EXEC_CTX_FK FOREIGN KEY (STEP_EXECUTION_ID)
        REFERENCES BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
);

-- ========================================================================
-- BATCH_JOB_EXECUTION_CONTEXT
-- Job 실행 컨텍스트 정보를 저장합니다.
-- ========================================================================
CREATE TABLE BATCH_JOB_EXECUTION_CONTEXT (
    JOB_EXECUTION_ID NUMBER(19) NOT NULL PRIMARY KEY,
    SHORT_CONTEXT VARCHAR2(2500) NOT NULL,
    SERIALIZED_CONTEXT CLOB,
    CONSTRAINT JOB_EXEC_CTX_FK FOREIGN KEY (JOB_EXECUTION_ID)
        REFERENCES BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);

-- ========================================================================
-- 시퀀스 생성
-- ========================================================================
CREATE SEQUENCE BATCH_STEP_EXECUTION_SEQ START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE BATCH_JOB_EXECUTION_SEQ START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE BATCH_JOB_SEQ START WITH 1 INCREMENT BY 1 NOCACHE;

-- ========================================================================
-- 인덱스 생성 (성능 최적화)
-- ========================================================================
CREATE INDEX JOB_INST_UN ON BATCH_JOB_INSTANCE (JOB_NAME, JOB_KEY);
CREATE INDEX JOB_EXEC_INST_ID_IDX ON BATCH_JOB_EXECUTION (JOB_INSTANCE_ID);
CREATE INDEX JOB_EXEC_PARAMS_IDX ON BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID);
CREATE INDEX STEP_EXEC_JOB_IDX ON BATCH_STEP_EXECUTION (JOB_EXECUTION_ID);

-- ========================================================================
-- 완료
-- ========================================================================
COMMIT;

PROMPT 'Spring Batch 메타테이블 생성 완료';
PROMPT '생성된 테이블:';
PROMPT '  - BATCH_JOB_INSTANCE';
PROMPT '  - BATCH_JOB_EXECUTION';
PROMPT '  - BATCH_JOB_EXECUTION_PARAMS';
PROMPT '  - BATCH_STEP_EXECUTION';
PROMPT '  - BATCH_STEP_EXECUTION_CONTEXT';
PROMPT '  - BATCH_JOB_EXECUTION_CONTEXT';
PROMPT '';
PROMPT '생성된 시퀀스:';
PROMPT '  - BATCH_JOB_SEQ';
PROMPT '  - BATCH_JOB_EXECUTION_SEQ';
PROMPT '  - BATCH_STEP_EXECUTION_SEQ';
