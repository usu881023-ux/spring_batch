package com.secta.hcbatch.job.stcs.service;

import com.secta.hcbatch.job.stcs.mapper.DayGoodsSumMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 상품별 판매현황 집계 Service
 */
@Slf4j
@Service
public class DayGoodsSumService {

    /**
     * 집계 실행: 기존 데이터 삭제 + 신규 집계 INSERT
     *
     * @return int[]{삭제 건수, INSERT 건수}
     */
    public int[] aggregate(DayGoodsSumMapper mapper, String baseDt) {
        int deletedCount = mapper.deleteDayGoodsSum(baseDt);
        log.info("기존 데이터 삭제 완료 - 삭제 건수: {}", deletedCount);

        int insertedCount = mapper.insertDayGoodsSum(baseDt);
        log.info("집계 데이터 INSERT 완료 - 처리 건수: {}", insertedCount);

        return new int[]{deletedCount, insertedCount};
    }
}
