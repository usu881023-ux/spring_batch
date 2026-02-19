package com.secta.hcbatch.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MmsTriggerDto {
    private String taskDtm;
    private String taskNo;
    private String orderNo;
    private String sendPhone;
    private String rcvPhone;
    private String mmsSubj;
    private String mmsCtt;
    private String mmsImgFile;
    private String mmsAtFile;
}
