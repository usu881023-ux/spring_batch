package com.secta.hcbatch.job.stcs.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 상품별 판매현황 집계 Mapper
 */
public interface DayGoodsSumMapper {

    /**
     * 기준일자 데이터 삭제
     */
    int deleteDayGoodsSum(@Param("baseDt") String baseDt);

    /**
     * 상품별 판매현황 집계 INSERT
     */
    int insertDayGoodsSum(@Param("baseDt") String baseDt);
}
