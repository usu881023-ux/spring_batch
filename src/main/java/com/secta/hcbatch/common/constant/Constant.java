package com.secta.hcbatch.common.constant;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Constant {

    // 상품권 동기화 영역
    // 1. 처리 대상 변경 유형 코드
    public static final Set<String> VALID_CHG_CODES = new HashSet<>(Arrays.asList(
            "AD", "AC", "MAD", "MAC", "ADNC", "ACNC",    // 사용/사용취소
            "RR", "CRR", "C", "CC", "CCI", "CCCI",        // 폐기
            "VCCI", "CVCCI", "PC", "RC", "SC", "SCRC", "CTI",  // 폐기
            "VC", "CVC"                                   // 유효기간 연장
    ));

    // 2. 사용/사용취소 코드
    public static final Set<String> USE_CODES = new HashSet<>(Arrays.asList(
            "AD", "AC", "MAD", "MAC", "ADNC", "ACNC"
    ));

    // 3. 사용 코드 (사용취소 제외)
    public static final Set<String> USE_ONLY_CODES = new HashSet<>(Arrays.asList(
            "AD", "MAD", "ACNC"
    ));

    // 4. 폐기 코드
    public static final Set<String> CANCEL_CODES = new HashSet<>(Arrays.asList(
            "RR", "CRR", "C", "CC", "CCI", "CCCI", "VCCI", "CVCCI",
            "PC", "RC", "SC", "SCRC", "CTI"
    ));

    // 유효기간 연장 코드
    public static final Set<String> EXTEND_CODES = new HashSet<>(Arrays.asList(
            "VC", "CVC"
    ));
}
