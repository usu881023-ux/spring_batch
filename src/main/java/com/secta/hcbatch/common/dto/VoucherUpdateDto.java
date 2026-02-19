package com.secta.hcbatch.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherUpdateDto {
    private String vchrNo;
    private String cpnState;
    private String avlEndDt;
    private String remAmt;
    private String useType;
    private String trxAmt;
    private String useDtm;
    private String brndCd;
    private String stoCd;
    private String stoNm;
}
