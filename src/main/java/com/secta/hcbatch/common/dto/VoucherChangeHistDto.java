package com.secta.hcbatch.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherChangeHistDto {
    private String taskDtm;
    private String chgSno;
    private String vchrNo;
    private String imgnVchrNo;
    private String chgClCd;
    private String brndCd;
    private String stoCd;
    private String stoNm;
    private String trxAmt;
    private String remAmt;
    private String avlEndDtm;
}
