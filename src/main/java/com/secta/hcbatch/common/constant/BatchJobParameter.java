package com.secta.hcbatch.common.constant;

/**
 * Batch Job 파라미터 상수
 */
public class BatchJobParameter {

    // 공통 파라미터
    public static final String BASE_DT = "BASE_DT";           // 기준일자
    public static final String RUN_DATE = "RUN_DATE";         // 실행일시
    
    // OrderPrvaMask Job 파라미터
    public static final String PROC_CD = "PROC_CD";           // 처리구분 (COMP: 완료, 기타: 미결재)
    
    // 처리구분 코드
    public static final String PROC_CD_COMP = "COMP";         // 완료건
    public static final String PROC_CD_CNCL = "CNCL";         // 취소건

    private BatchJobParameter() {
        throw new IllegalStateException("Constant class");
    }
}
