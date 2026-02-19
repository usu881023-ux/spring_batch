package com.secta.hcbatch.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherInfoDto {
    private String vchrNo;
    private String cpnType;
    private String cpnState;
    private String hcSellCode;
    private String avlEndDt;
    private String remAmt;
}
