package com.secta.hcbatch.job.hcon.service;

import com.secta.hcbatch.common.constant.Constant;
import com.secta.hcbatch.common.dto.VoucherChangeHistDto;
import com.secta.hcbatch.common.dto.VoucherInfoDto;
import com.secta.hcbatch.common.dto.VoucherUpdateDto;
import com.secta.hcbatch.common.util.BizUtil;
import com.secta.hcbatch.common.util.StringUtil;
import com.secta.hcbatch.job.hcon.mapper.HcTaskMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 상품권 동기화 Service
 */
@Slf4j
@Service
public class VoucherSyncService {

    /**
     * 단일 변경이력 동기화 처리
     * 복호화 → 검증 → 상태조회 → 변경유형 처리 → UPDATE/INSERT
     *
     * @return 스킵이면 false, 처리 성공이면 true
     */
    public boolean syncVoucher(HcTaskMapper hcMapper, HcTaskMapper hcBatchMapper,
                               VoucherChangeHistDto vchrHm, String taskDtm) {

        BigInteger taskNo = new BigInteger(vchrHm.getChgSno());

        // 가상 상품권 번호 확인
        if (vchrHm.getImgnVchrNo() == null) {
            log.error("[{}] imgnVchrNo 누락", taskNo);
            return false;
        }

        // 상품권 번호 복호화
        String vchrNo = BizUtil.decryptVoucherNo(vchrHm.getVchrNo());
        if (StringUtil.isEmpty(vchrNo)) {
            log.error("[{}] vchrNo 복호화 실패", taskNo);
            return false;
        }

        String chgClCd = vchrHm.getChgClCd();
        if (chgClCd == null || !Constant.VALID_CHG_CODES.contains(chgClCd)) {
            log.error("[{}] chgClCd 유효하지 않음: {}", vchrNo, chgClCd);
            return false;
        }

        // 월렛 상품권 정보 조회
        VoucherInfoDto cpnHm = hcMapper.selectVchrInfo(vchrNo);

        if (cpnHm == null) {
            log.info("[{}] 판매내역 없음", vchrNo);
            return false;
        }

        log.debug("[{}] cpnInfo={}, chgHst={}", vchrNo, cpnHm, vchrHm);

        // 변경 유형별 처리
        String cpnState = "";
        String avlEndDt = "";
        String remAmt = "";

        // 사용 및 사용취소
        if (Constant.USE_CODES.contains(chgClCd)) {
            remAmt = vchrHm.getRemAmt();

            // 사용
            if (Constant.USE_ONLY_CODES.contains(chgClCd)) {
                cpnState = "E";

                // 금액권(A)이고 잔액이 0 이하면 사용완료
                if ("A".equals(cpnHm.getCpnType())) {
                    String remAmtStr = vchrHm.getRemAmt();
                    if (remAmtStr != null && new BigDecimal(remAmtStr).compareTo(BigDecimal.ZERO) <= 0) {
                        // 필요시 추가 로직
                    }
                }
            }
            // 사용취소
            else {
                // 기사용완료된 교환권은 원복
                if ("E".equals(cpnHm.getCpnState()) && !"A".equals(cpnHm.getCpnType())) {
                    cpnState = "N";
                }
            }
        }
        // 폐기
        else if (Constant.CANCEL_CODES.contains(chgClCd)) {
            if ("RR".equals(chgClCd) || "CRR".equals(chgClCd) ||
                "C".equals(chgClCd) || "CC".equals(chgClCd) ||
                "PC".equals(chgClCd) || "SCRC".equals(chgClCd) ||
                "CCI".equals(chgClCd) || "CCCI".equals(chgClCd) ||
                "VCCI".equals(chgClCd) || "CVCCI".equals(chgClCd) ||
                "CTI".equals(chgClCd)) {
                cpnState = "C";
            }
        }
        // 유효기간 연장
        else if (Constant.EXTEND_CODES.contains(chgClCd)) {
            String avlEndDtm = vchrHm.getAvlEndDtm();
            if (avlEndDtm != null && avlEndDtm.trim().length() >= 8) {
                avlEndDt = avlEndDtm.trim().substring(0, 8);
            }
        }

        VoucherUpdateDto taskHm = VoucherUpdateDto.builder()
                .vchrNo(cpnHm.getVchrNo())
                .cpnState(cpnState)
                .avlEndDt(avlEndDt)
                .remAmt(remAmt)
                .useType(chgClCd)
                .trxAmt(vchrHm.getTrxAmt())
                .useDtm(taskDtm.substring(0, 14))
                .brndCd(vchrHm.getBrndCd())
                .stoCd(vchrHm.getStoCd())
                .stoNm(vchrHm.getStoNm())
                .build();

        log.debug("[{}] taskHm={}", vchrNo, taskHm);

        // 해피콘 정보 변경 동기화 (BATCH session)
        hcBatchMapper.updateVchrInfo(taskHm);
        hcBatchMapper.insertVchrChgHst(vchrNo);

        return true;
    }
}
