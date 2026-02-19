package com.secta.hcbatch.job.message.service;

import com.secta.hcbatch.common.dto.MmsSendDto;
import com.secta.hcbatch.common.dto.MmsTriggerDto;
import com.secta.hcbatch.common.dto.OrderInfoDto;
import com.secta.hcbatch.common.exception.BatchException;
import com.secta.hcbatch.common.util.HcApiClient;
import com.secta.hcbatch.common.util.S3Util;
import com.secta.hcbatch.common.util.StringUtil;
import com.secta.hcbatch.job.message.mapper.HcMsgMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * MMS 발송 Service
 */
@Slf4j
@Service
public class HcSendMsgService {

    private final S3Util s3Util;
    private final HcApiClient hcApiClient;

    @Value("${batch.hc-api.url:https://dev-hc.happypointcard.com}")
    private String hcApiUrl;

    public HcSendMsgService(S3Util s3Util, HcApiClient hcApiClient) {
        this.s3Util = s3Util;
        this.hcApiClient = hcApiClient;
    }

    /**
     * 메시지 유효성 검증 - 실패 시 에러 메시지 반환, 성공 시 null
     */
    public String validateMessage(MmsTriggerDto msgHm) {
        if (msgHm.getOrderNo() == null) {
            return "주문번호 누락";
        }
        if (StringUtil.isEmpty(msgHm.getSendPhone())) {
            return "발신전화번호 누락";
        }
        if (StringUtil.isEmpty(msgHm.getRcvPhone())) {
            return "수신전화번호 누락";
        }
        if (StringUtil.isEmpty(msgHm.getMmsImgFile())) {
            return "S3파일 누락";
        }
        return null;
    }

    /**
     * MMS 발송 처리: S3 다운로드 → 이미지/메시지 등록 → 트리거 업데이트
     *
     * @return 발송 성공 시 true, 실패 시 false
     */
    public boolean sendMms(HcMsgMapper mapper, HcMsgMapper batchMapper,
                           MmsTriggerDto msgHm, String taskDtm, String taskNo) {
        try {
            String localImg = s3Util.downloadVoucherImage(msgHm.getMmsImgFile());

            if (StringUtil.isEmpty(localImg)) {
                batchMapper.updateMsgErrorInfo(taskDtm, taskNo, "S3다운로드 실패");
                log.error("[{}] S3다운로드 실패", msgHm.getOrderNo());
                return false;
            }

            // MMS 시퀀스 조회 (SELECT → SIMPLE session)
            String mmsSeq = mapper.selectMsgSeq();

            MmsSendDto taskHm = MmsSendDto.builder()
                    .rcvPhone(msgHm.getRcvPhone())
                    .sendPhone(msgHm.getSendPhone())
                    .sendType("6")       // MMS
                    .orderNo(msgHm.getOrderNo())
                    .mmsSeq(mmsSeq)
                    .imgCnt("1")
                    .mmsCtt(msgHm.getMmsCtt())
                    .mmsSubj(msgHm.getMmsSubj())
                    .mmsType("IMG")
                    .mmsImg(localImg)
                    .build();

            // MMS 이미지 등록 (BATCH session)
            batchMapper.insertSendImage(taskHm);
            // MMS 메시지 등록 (BATCH session)
            batchMapper.insertSendMsg(taskHm);
            // 트리거 상태 업데이트 (BATCH session)
            batchMapper.updateMsgTrigInfo(taskDtm, taskNo);

            log.info("[{}] MMS 발송 등록 완료", msgHm.getOrderNo());
            return true;

        } catch (BatchException e) {
            batchMapper.updateMsgErrorInfo(taskDtm, taskNo, e.getMessage());
            log.error("[{}] MMS 처리 실패: {}", msgHm.getOrderNo(), e.getMessage());
            return false;
        }
    }

    /**
     * 오류 시 주문 취소 API 호출
     */
    public void cancelOrder(HcMsgMapper mapper, String orderNo) {
        try {
            OrderInfoDto orderHm = mapper.selectOrderInfo(orderNo);
            if (orderHm != null) {
                JSONObject reqObj = new JSONObject();
                reqObj.put("cartNo", orderHm.getCartNo());
                reqObj.put("mbrNo", orderHm.getUserId());

                String response = hcApiClient.postJson(
                        hcApiUrl + "/api/order-cancel",
                        reqObj.toJSONString(),
                        7000
                );
                log.info("[{}] 주문취소 API 응답: {}", orderNo, response);
            } else {
                log.error("[{}] 주문정보 없음", orderNo);
            }
        } catch (Exception e) {
            log.error("[{}] 주문취소 API 호출 실패: {}", orderNo, e.getMessage());
        }
    }
}
