package com.secta.hcbatch.job.prva;

import com.secta.hcbatch.common.constant.BatchJobParameter;
import com.secta.hcbatch.common.dto.OrderMaskTargetDto;
import com.secta.hcbatch.job.prva.mapper.OrderPrvaMaskMapper;
import com.secta.hcbatch.job.prva.service.OrderPrvaMaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
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
    private final SqlSession prvaBatchSqlSession;
    private final OrderPrvaMaskService orderPrvaMaskService;

    public OrderPrvaMaskTasklet(@Qualifier("prvaSqlSessionTemplate") SqlSession prvaSqlSession,
                                @Qualifier("prvaBatchSqlSessionTemplate") SqlSession prvaBatchSqlSession,
                                OrderPrvaMaskService orderPrvaMaskService) {
        this.prvaSqlSession = prvaSqlSession;
        this.prvaBatchSqlSession = prvaBatchSqlSession;
        this.orderPrvaMaskService = orderPrvaMaskService;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext)
            throws Exception {

        // 1. Job 파라미터 추출
        Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
        String baseDt = getBaseDtParameter(jobParameters);
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

        // SELECT는 SIMPLE session, INSERT/UPDATE는 BATCH session 사용
        OrderPrvaMaskMapper mapper = prvaSqlSession.getMapper(OrderPrvaMaskMapper.class);
        OrderPrvaMaskMapper batchMapper = prvaBatchSqlSession.getMapper(OrderPrvaMaskMapper.class);

        // 3. 대상 주문 목록 조회 (SIMPLE session) - Service 위임
        List<OrderMaskTargetDto> orderList = orderPrvaMaskService.selectTargetOrders(mapper, baseDt, procCd);
        log.info("처리 대상 주문 건수: {}", orderList.size());

        int processedCount = 0;
        int commitInterval = 100;

        // 4. 각 주문별 처리 (BATCH session으로 INSERT/UPDATE 일괄 처리)
        for (OrderMaskTargetDto order : orderList) {
            String orderNo = order.getOrderNo();
            String orderCode = order.getOrderCode();

            try {
                // 11개 테이블 순차 처리 - Service 위임
                orderPrvaMaskService.maskOrder(batchMapper, orderCode, orderNo);

                processedCount++;

                // Commit Interval마다 Batch flush (축적된 SQL 일괄 실행)
                if (processedCount % commitInterval == 0) {
                    prvaBatchSqlSession.flushStatements();
                    log.info("Batch flush 완료 - {}/{}", processedCount, orderList.size());
                }

            } catch (Exception e) {
                log.error("주문번호 {} (주문코드: {}) 처리 실패: {}", orderNo, orderCode, e.getMessage());
                throw e;
            }
        }

        // 잔여분 flush
        if (processedCount % commitInterval != 0) {
            prvaBatchSqlSession.flushStatements();
            log.info("Batch flush 완료 (잔여) - {}/{}", processedCount, orderList.size());
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
     * 기준일자(BASE_DT) 파라미터 추출 - 항상 전일자로 반환
     * 파라미터가 있으면 해당 날짜의 전일자, 없으면 현재 날짜의 전일자
     */
    private String getBaseDtParameter(Map<String, Object> jobParameters) {
        Object value = jobParameters.get(BatchJobParameter.BASE_DT);

        LocalDate baseDate;
        if (value == null) {
            baseDate = LocalDate.now();
        } else if (value instanceof Long) {
            baseDate = Instant.ofEpochMilli((Long) value)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } else if (value instanceof Date) {
            baseDate = Instant.ofEpochMilli(((Date) value).getTime())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } else {
            baseDate = LocalDate.parse(value.toString(), DateTimeFormatter.ofPattern("yyyyMMdd"));
        }

        return baseDate.minusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    /**
     * Job 파라미터 추출 헬퍼 메서드
     */
    private String getParameter(Map<String, Object> jobParameters, String key, String defaultValue) {
        Object value = jobParameters.get(key);
        if (value == null) {
            return defaultValue;
        }

        if (value instanceof Long) {
            return convertMillisToDateString((Long) value);
        }

        if (value instanceof Date) {
            return convertMillisToDateString(((Date) value).getTime());
        }

        return value.toString();
    }

    /**
     * 밀리초 타임스탬프를 yyyyMMdd 형식 문자열로 변환
     */
    private String convertMillisToDateString(Long millis) {
        return Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
