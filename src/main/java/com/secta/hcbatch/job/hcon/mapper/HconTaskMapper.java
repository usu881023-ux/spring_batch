package com.secta.hcbatch.job.hcon.mapper;

import com.secta.hcbatch.common.dto.VoucherChangeHistDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 해피콘 DB 상품권 Mapper
 */
@Mapper
public interface HconTaskMapper {

    /**
     * 상품권 변경 이력 조회
     */
    List<VoucherChangeHistDto> selectVchrChgHst(@Param("taskDtm") String taskDtm,
                                                @Param("chgSno") String chgSno);
}
