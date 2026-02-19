package com.secta.hcbatch.job.hcon.mapper;

import com.secta.hcbatch.common.dto.VoucherInfoDto;
import com.secta.hcbatch.common.dto.VoucherUpdateDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * HC Mall 상품권 Mapper
 */
@Mapper
public interface HcTaskMapper {

    /**
     * 상품권 정보 조회
     */
    VoucherInfoDto selectVchrInfo(@Param("vchrNo") String vchrNo);

    /**
     * 상품권 정보 업데이트
     */
    int updateVchrInfo(VoucherUpdateDto param);

    /**
     * 상품권 변경 이력 등록
     */
    int insertVchrChgHst(@Param("vchrNo") String vchrNo);
}
