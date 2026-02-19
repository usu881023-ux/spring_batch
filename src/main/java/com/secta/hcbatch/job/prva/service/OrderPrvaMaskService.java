package com.secta.hcbatch.job.prva.service;

import com.secta.hcbatch.common.constant.BatchJobParameter;
import com.secta.hcbatch.common.dto.OrderMaskTargetDto;
import com.secta.hcbatch.job.prva.mapper.OrderPrvaMaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 개인정보 마스킹 Service
 */
@Slf4j
@Service
public class OrderPrvaMaskService {

    /**
     * 대상 주문 조회
     */
    public List<OrderMaskTargetDto> selectTargetOrders(OrderPrvaMaskMapper mapper, String baseDt, String procCd) {
        if (BatchJobParameter.PROC_CD_COMP.equals(procCd)) {
            return mapper.selectOrderFnshList(baseDt);
        } else {
            return mapper.selectOrderCnclList(baseDt);
        }
    }

    /**
     * 단일 주문의 11개 테이블 이관+마스킹 처리
     */
    public void maskOrder(OrderPrvaMaskMapper batchMapper, String orderCode, String orderNo) {
        // 1. 주문 테이블
        batchMapper.insertOrderTrsf(orderCode);
        batchMapper.updateOrderMask(orderCode);

        // 2. 장바구니 테이블
        batchMapper.insertCartTrsf(orderCode);
        batchMapper.updateCartMask(orderCode);

        // 3. 주문배송 테이블
        batchMapper.insertOrderDelvTrsf(orderNo);
        batchMapper.updateOrderDelvMask(orderNo);

        // 4. 외부쿠폰 MMS 테이블
        batchMapper.insertCpnInfoTrsf(orderNo);
        batchMapper.updateCpnInfoMask(orderNo);

        // 5. 환불계좌정보 테이블
        batchMapper.insertRefundTrsf(orderNo);
        batchMapper.updateRefundMask(orderNo);

        // 6. 환불입금로그 테이블
        batchMapper.insertRefundHstTrsf(orderNo);
        batchMapper.updateRefundHstMask(orderNo);

        // 7. 해피콘쿠폰 테이블
        batchMapper.insertHconInfoTrsf(orderNo);
        batchMapper.updateHconInfoMask(orderNo);

        // 8. 해피콘복수발송 테이블
        batchMapper.insertHconMultiTrsf(orderNo);
        batchMapper.updateHconMultiMask(orderNo);

        // 9. 해피콘쿠폰이력 테이블
        batchMapper.insertHconHstTrsf(orderNo);
        batchMapper.updateHconHstMask(orderNo);

        // 10. 해피콘반려 테이블
        batchMapper.insertHconRejtTrsf(orderNo);
        batchMapper.updateHconRejtMask(orderNo);

        // 11. 해피콘반려로그 테이블
        batchMapper.insertHconRejtHstTrsf(orderNo);
        batchMapper.updateHconRejtHstMask(orderNo);
    }
}
