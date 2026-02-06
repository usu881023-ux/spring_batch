package com.secta.hcbatch.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 개인정보 마스킹 Mapper
 *
 * 처리 테이블:
 * - T_ORDER: 주문
 * - T_ORDER_BASKET: 장바구니
 * - T_ORDER_DELIVERY: 주문배송
 * - T_OUTSIDE_MMS: 외부쿠폰 MMS
 * - T_REFUND_ACC_INFO: 환불계좌정보
 * - T_REFUND_IN_LOG: 환불입금로그
 * - T_HAPPYCON_COUPON: 해피콘쿠폰
 * - T_HAPPYCON_COUPON_MULTI: 해피콘복수발송
 * - T_HAPPYCON_COUPON_HST: 해피콘쿠폰이력
 * - T_HAPPYCON_REJ: 해피콘반려
 * - T_HAPPYCON_REJ_IN_LOG: 해피콘반려로그
 */
public interface OrderPrvaMaskMapper {

    // ===== 대상 주문 조회 =====

    /**
     * 완료건 주문 목록 조회 (사용/취소/환불 완료)
     */
    List<Map<String, Object>> selectOrderFnshList(@Param("day") String day);

    /**
     * 미결재 취소건 주문 목록 조회
     */
    List<Map<String, Object>> selectOrderCnclList(@Param("day") String day);

    // ===== 주문 테이블 처리 =====

    /**
     * 주문 이관 테이블 MERGE
     */
    int insertOrderTrsf(@Param("orderCode") String orderCode);

    /**
     * 주문 마스킹 UPDATE
     */
    int updateOrderMask(@Param("orderCode") String orderCode);

    // ===== 장바구니 테이블 처리 =====

    /**
     * 장바구니 이관 테이블 MERGE
     */
    int insertCartTrsf(@Param("orderCode") String orderCode);

    /**
     * 장바구니 마스킹 UPDATE
     */
    int updateCartMask(@Param("orderCode") String orderCode);

    // ===== 주문배송 테이블 처리 =====

    /**
     * 주문배송 이관 테이블 MERGE
     */
    int insertOrderDelvTrsf(@Param("orderNo") String orderNo);

    /**
     * 주문배송 마스킹 UPDATE
     */
    int updateOrderDelvMask(@Param("orderNo") String orderNo);

    // ===== 외부쿠폰 MMS 테이블 처리 =====

    /**
     * 외부쿠폰 MMS 이관 테이블 MERGE
     */
    int insertCpnInfoTrsf(@Param("orderNo") String orderNo);

    /**
     * 외부쿠폰 MMS 마스킹 UPDATE
     */
    int updateCpnInfoMask(@Param("orderNo") String orderNo);

    // ===== 환불계좌정보 테이블 처리 =====

    /**
     * 환불계좌정보 이관 테이블 MERGE
     */
    int insertRefundTrsf(@Param("orderNo") String orderNo);

    /**
     * 환불계좌정보 마스킹 MERGE
     */
    int updateRefundMask(@Param("orderNo") String orderNo);

    // ===== 환불입금로그 테이블 처리 =====

    /**
     * 환불입금로그 이관 테이블 MERGE
     */
    int insertRefundHstTrsf(@Param("orderNo") String orderNo);

    /**
     * 환불입금로그 마스킹 UPDATE
     */
    int updateRefundHstMask(@Param("orderNo") String orderNo);

    // ===== 해피콘쿠폰 테이블 처리 =====

    /**
     * 해피콘쿠폰 이관 테이블 MERGE
     */
    int insertHconInfoTrsf(@Param("orderNo") String orderNo);

    /**
     * 해피콘쿠폰 마스킹 UPDATE
     */
    int updateHconInfoMask(@Param("orderNo") String orderNo);

    // ===== 해피콘복수발송 테이블 처리 =====

    /**
     * 해피콘복수발송 이관 테이블 MERGE
     */
    int insertHconMultiTrsf(@Param("orderNo") String orderNo);

    /**
     * 해피콘복수발송 마스킹 UPDATE
     */
    int updateHconMultiMask(@Param("orderNo") String orderNo);

    // ===== 해피콘쿠폰이력 테이블 처리 =====

    /**
     * 해피콘쿠폰이력 이관 테이블 MERGE
     */
    int insertHconHstTrsf(@Param("orderNo") String orderNo);

    /**
     * 해피콘쿠폰이력 마스킹 UPDATE
     */
    int updateHconHstMask(@Param("orderNo") String orderNo);

    // ===== 해피콘반려 테이블 처리 =====

    /**
     * 해피콘반려 이관 테이블 MERGE
     */
    int insertHconRejtTrsf(@Param("orderNo") String orderNo);

    /**
     * 해피콘반려 마스킹 UPDATE
     */
    int updateHconRejtMask(@Param("orderNo") String orderNo);

    // ===== 해피콘반려로그 테이블 처리 =====

    /**
     * 해피콘반려로그 이관 테이블 MERGE
     */
    int insertHconRejtHstTrsf(@Param("orderNo") String orderNo);

    /**
     * 해피콘반려로그 마스킹 UPDATE
     */
    int updateHconRejtHstMask(@Param("orderNo") String orderNo);
}
