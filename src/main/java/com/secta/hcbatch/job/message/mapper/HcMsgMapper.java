package com.secta.hcbatch.job.message.mapper;

import com.secta.hcbatch.common.dto.MmsSendDto;
import com.secta.hcbatch.common.dto.MmsTriggerDto;
import com.secta.hcbatch.common.dto.OrderInfoDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * MMS 발송 Mapper
 */
@Mapper
public interface HcMsgMapper {

    /**
     * MMS 발송 트리거 목록 조회
     */
    List<MmsTriggerDto> selectMsgTrigList(@Param("taskDtm") String taskDtm,
                                           @Param("taskNo") String taskNo);

    /**
     * MMS 시퀀스 조회
     */
    String selectMsgSeq();

    /**
     * 주문 정보 조회
     */
    OrderInfoDto selectOrderInfo(@Param("orderNo") String orderNo);

    /**
     * MMS 발송 트리거 상태 업데이트
     */
    int updateMsgTrigInfo(@Param("taskDtm") String taskDtm,
                          @Param("taskNo") String taskNo);

    /**
     * MMS 발송 오류 업데이트
     */
    int updateMsgErrorInfo(@Param("taskDtm") String taskDtm,
                           @Param("taskNo") String taskNo,
                           @Param("errMsg") String errMsg);

    /**
     * MMS 발송 메시지 등록
     */
    int insertSendMsg(MmsSendDto param);

    /**
     * MMS 이미지 등록
     */
    int insertSendImage(MmsSendDto param);
}
