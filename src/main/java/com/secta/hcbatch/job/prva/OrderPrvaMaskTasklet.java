package com.secta.hcbatch.job.prva;

import com.secta.hcbatch.common.constant.BatchJobParameter;
import com.secta.hcbatch.common.util.DateUtil;
import com.secta.hcbatch.mapper.OrderPrvaMaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 개인정보 마스킹 Tasklet (MyBatis 적용)
 *
 * 처리 프로세스:
 * 1. PROC_CD에 따라 대상 주문 조회
 *    - COMP: 완료된 주문 (사용/취소/환불완료)
 *    - CNCL: 미결재 취소된 주문
 * 2. 각 주문별로 11개 테이블 처리
 *    - 이관 테이블에 MERGE (백업)
 *    - 원본 테이블 개인정보 마스킹 UPDATE
 * 3. Commit Interval: 100건
 */
@Slf4j
@Component
public class OrderPrvaMaskTasklet implements Tasklet {

    private final SqlSession prvaSqlSession;

    public OrderPrvaMaskTasklet(@Qualifier("prvaSqlSessionTemplate") SqlSession prvaSqlSession) {
        this.prvaSqlSession = prvaSqlSession;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
            throws Exception {

        // 1. Job 파라미터 추출
        Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
        String baseDt = getParameter(jobParameters, BatchJobParameter.BASE_DT, DateUtil.getYesterday());
        String procCd = getParameter(jobParameters, BatchJobParameter.PROC_CD, null);

        // 2. 파라미터 검증
        if (procCd == null) {
            log.error("PROC_CD parameter is required");
            throw new IllegalArgumentException("PROC_CD parameter is required");
        }

        log.info("=================================================================");
        log.info("개인정보 마스킹 Job 시작");
        log.info("기준일자: {}, 처리구분: {}", baseDt, procCd);
        log.info("=================================================================");

        OrderPrvaMaskMapper mapper = prvaSqlSession.getMapper(OrderPrvaMaskMapper.class);

        // 3. 대상 주문 목록 조회
        List<Map<String, Object>> orderList = selectTargetOrders(mapper, baseDt, procCd);
        log.info("처리 대상 주문 건수: {}", orderList.size());

        int processedCount = 0;
        int commitInterval = 100;

        // 4. 각 주문별 처리
        for (Map<String, Object> order : orderList) {
            String orderNo = (String) order.get("ORDER_NO");
            String orderCode = (String) order.get("ORDER_CODE");

            try {
                // 11개 테이블 순차 처리 (이관 -> 마스킹)
                processOrderTable(mapper, orderCode);
                processCartTable(mapper, orderCode);
                processOrderDelvTable(mapper, orderNo);
                processCpnInfoTable(mapper, orderNo);
                processRefundTable(mapper, orderNo);
                processRefundHstTable(mapper, orderNo);
                processHconInfoTable(mapper, orderNo);
                processHconMultiTable(mapper, orderNo);
                processHconHstTable(mapper, orderNo);
                processHconRejtTable(mapper, orderNo);
                processHconRejtHstTable(mapper, orderNo);

                processedCount++;

                // Commit Interval 처리
                if (processedCount % commitInterval == 0) {
                    log.info("처리 진행 중... {}/{}", processedCount, orderList.size());
                }

            } catch (Exception e) {
                log.error("주문번호 {} (주문코드: {}) 처리 실패: {}", orderNo, orderCode, e.getMessage());
                throw e;
            }
        }

        // 5. 처리 결과 기록
        contribution.incrementWriteCount(processedCount);

        log.info("=================================================================");
        log.info("개인정보 마스킹 Job 완료");
        log.info("총 처리 건수: {}", processedCount);
        log.info("=================================================================");

        return RepeatStatus.FINISHED;
    }

    /**
     * 대상 주문 목록 조회
     */
    private List<Map<String, Object>> selectTargetOrders(OrderPrvaMaskMapper mapper, String baseDt, String procCd) {
        if (BatchJobParameter.PROC_CD_COMP.equals(procCd)) {
            // 완료건 조회 (사용/취소/환불 완료)
            return mapper.selectOrderFnshList(baseDt);
        } else {
            // 미결재 취소건 조회
            return mapper.selectOrderCnclList(baseDt);
        }
    }

    /**
     * 주문 테이블 처리 (이관 -> 마스킹)
     */
    private void processOrderTable(OrderPrvaMaskMapper mapper, String orderCode) {
        mapper.insertOrderTrsf(orderCode);
        mapper.updateOrderMask(orderCode);
    }

    /**
     * 장바구니 테이블 처리 (이관 -> 마스킹)
     */
    private void processCartTable(OrderPrvaMaskMapper mapper, String orderCode) {
        mapper.insertCartTrsf(orderCode);
        mapper.updateCartMask(orderCode);
    }

    /**
     * 주문배송 테이블 처리 (이관 -> 마스킹)
     */
    private void processOrderDelvTable(OrderPrvaMaskMapper mapper, String orderNo) {
        mapper.insertOrderDelvTrsf(orderNo);
        mapper.updateOrderDelvMask(orderNo);
    }

    /**
     * 외부쿠폰 MMS 테이블 처리 (이관 -> 마스킹)
     */
    private void processCpnInfoTable(OrderPrvaMaskMapper mapper, String orderNo) {
        mapper.insertCpnInfoTrsf(orderNo);
        mapper.updateCpnInfoMask(orderNo);
    }

    /**
     * 환불계좌정보 테이블 처리 (이관 -> 마스킹)
     */
    private void processRefundTable(OrderPrvaMaskMapper mapper, String orderNo) {
        mapper.insertRefundTrsf(orderNo);
        mapper.updateRefundMask(orderNo);
    }

    /**
     * 환불입금로그 테이블 처리 (이관 -> 마스킹)
     */
    private void processRefundHstTable(OrderPrvaMaskMapper mapper, String orderNo) {
        mapper.insertRefundHstTrsf(orderNo);
        mapper.updateRefundHstMask(orderNo);
    }

    /**
     * 해피콘쿠폰 테이블 처리 (이관 -> 마스킹)
     */
    private void processHconInfoTable(OrderPrvaMaskMapper mapper, String orderNo) {
        mapper.insertHconInfoTrsf(orderNo);
        mapper.updateHconInfoMask(orderNo);
    }

    /**
     * 해피콘복수발송 테이블 처리 (이관 -> 마스킹)
     */
    private void processHconMultiTable(OrderPrvaMaskMapper mapper, String orderNo) {
        mapper.insertHconMultiTrsf(orderNo);
        mapper.updateHconMultiMask(orderNo);
    }

    /**
     * 해피콘쿠폰이력 테이블 처리 (이관 -> 마스킹)
     */
    private void processHconHstTable(OrderPrvaMaskMapper mapper, String orderNo) {
        mapper.insertHconHstTrsf(orderNo);
        mapper.updateHconHstMask(orderNo);
    }

    /**
     * 해피콘반려 테이블 처리 (이관 -> 마스킹)
     */
    private void processHconRejtTable(OrderPrvaMaskMapper mapper, String orderNo) {
        mapper.insertHconRejtTrsf(orderNo);
        mapper.updateHconRejtMask(orderNo);
    }

    /**
     * 해피콘반려로그 테이블 처리 (이관 -> 마스킹)
     */
    private void processHconRejtHstTable(OrderPrvaMaskMapper mapper, String orderNo) {
        mapper.insertHconRejtHstTrsf(orderNo);
        mapper.updateHconRejtHstMask(orderNo);
    }

    /**
     * Job 파라미터 추출 헬퍼 메서드
     */
    private String getParameter(Map<String, Object> jobParameters, String key, String defaultValue) {
        Object value = jobParameters.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}
