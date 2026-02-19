package com.secta.hcbatch.job.stcs;

import com.secta.hcbatch.common.constant.BatchJobParameter;
import com.secta.hcbatch.common.util.DateUtil;
import com.secta.hcbatch.job.stcs.mapper.DayGoodsSumMapper;
import com.secta.hcbatch.job.stcs.service.DayGoodsSumService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 상품별 판매현황 집계 Tasklet
 *
 * 처리 프로세스:
 * 1. 기준일자의 기존 데이터 삭제
 * 2. 상품별 판매 데이터 집계 INSERT
 */
@Slf4j
@Component
public class DayGoodsSumTasklet implements Tasklet {

    private final SqlSession mallSqlSession;
    private final DayGoodsSumService dayGoodsSumService;

    public DayGoodsSumTasklet(@Qualifier("mallSqlSessionTemplate") SqlSession mallSqlSession,
                              DayGoodsSumService dayGoodsSumService) {
        this.mallSqlSession = mallSqlSession;
        this.dayGoodsSumService = dayGoodsSumService;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
            throws Exception {

        // 1. Job 파라미터 추출
        Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
        String baseDt = getParameter(jobParameters, BatchJobParameter.BASE_DT, DateUtil.getYesterday());

        log.info("=================================================================");
        log.info("상품별 판매현황 집계 Job 시작");
        log.info("기준일자: {}", baseDt);
        log.info("=================================================================");

        // 2. Mapper 획득
        DayGoodsSumMapper mapper = mallSqlSession.getMapper(DayGoodsSumMapper.class);

        // 3. Service 호출 - 비즈니스 로직 위임
        int[] result = dayGoodsSumService.aggregate(mapper, baseDt);
        int deletedCount = result[0];
        int insertedCount = result[1];

        // 4. 처리 결과 기록
        contribution.incrementWriteCount(insertedCount);

        log.info("=================================================================");
        log.info("상품별 판매현황 집계 Job 완료");
        log.info("삭제 건수: {}, INSERT 건수: {}", deletedCount, insertedCount);
        log.info("=================================================================");

        return RepeatStatus.FINISHED;
    }

    /**
     * Job 파라미터 추출 헬퍼 메서드
     */
    private String getParameter(Map<String, Object> jobParameters, String key, String defaultValue) {
        Object value = jobParameters.get(key);
        return value != null ? value.toString() : defaultValue;
    }
}
