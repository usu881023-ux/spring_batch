package com.secta.hcbatch.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MmsSendDto {
    private String rcvPhone;
    private String sendPhone;
    private String sendType;
    private String orderNo;
    private String mmsSeq;
    private String imgCnt;
    private String mmsCtt;
    private String mmsSubj;
    private String mmsType;
    private String mmsImg;
}
