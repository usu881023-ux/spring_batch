package com.secta.hcbatch.job.message;

import com.secta.hcbatch.common.dto.MmsTriggerDto;
import com.secta.hcbatch.common.util.FileUtil;
import com.secta.hcbatch.common.util.StringUtil;
import com.secta.hcbatch.job.message.mapper.HcMsgMapper;
import com.secta.hcbatch.job.message.service.HcSendMsgService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * MMS 발송 Tasklet
 *
 * 처리 프로세스:
 * 1. Lock 파일에서 기준 타임스탬프 읽기
 * 2. T_HAPPYCON_MMS 테이블에서 발송 대상 조회
 * 3. S3에서 MMS 이미지 다운로드
 * 4. EM_TRAN, EM_TRAN_MMS 테이블에 발송 데이터 INSERT
 * 5. 오류 시 주문 취소 API 호출
 * 6. Lock 파일 업데이트
 */
@Slf4j
@Component
public class HcSendMsgTasklet implements Tasklet {

    private static final String LOCK_FILE_NAME = "hcon_msg_send.lock";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final SqlSession sqlSession;
    private final SqlSession batchSqlSession;
    private final FileUtil fileUtil;
    private final HcSendMsgService hcSendMsgService;

    public HcSendMsgTasklet(@Qualifier("prvaSqlSessionTemplate") SqlSession sqlSession,
                            @Qualifier("prvaBatchSqlSessionTemplate") SqlSession batchSqlSession,
                            FileUtil fileUtil,
                            HcSendMsgService hcSendMsgService) {
        this.sqlSession = sqlSession;
        this.batchSqlSession = batchSqlSession;
        this.fileUtil = fileUtil;
        this.hcSendMsgService = hcSendMsgService;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        String baseDt = "";
        String baseSno = "";
        String taskDtm = "";
        BigInteger taskNo = BigInteger.ZERO;
        int taskCnt = 0;
        int sendCnt = 0;

        try {
            // 1. Lock 파일에서 기준 타임스탬프 읽기
            String lockContent = fileUtil.readLockFile(LOCK_FILE_NAME);
            String[] lockArr = lockContent.split(",", -1);
            baseDt = lockArr.length > 0 ? lockArr[0] : "";
            baseSno = lockArr.length > 1 ? lockArr[1] : "";

            // 3시간 이내만 처리
            String minDt = LocalDateTime.now().minusHours(3).format(TIMESTAMP_FORMAT);

            if (StringUtil.isEmpty(baseDt)) {
                baseDt = LocalDateTime.now().minusSeconds(5).format(TIMESTAMP_FORMAT) + "000000000";
            } else if (baseDt.length() >= 14 && baseDt.substring(0, 14).compareTo(minDt) < 0) {
                baseDt = minDt + "000000000";
            }

            log.info("[TASK START] taskDtm={}, taskNo={}", baseDt, baseSno);

            // SELECT는 SIMPLE session, INSERT/UPDATE는 BATCH session 사용
            HcMsgMapper mapper = sqlSession.getMapper(HcMsgMapper.class);
            HcMsgMapper batchMapper = batchSqlSession.getMapper(HcMsgMapper.class);

            // 2. MMS 발송 대상 조회 (SIMPLE session)
            List<MmsTriggerDto> msgList = mapper.selectMsgTrigList(baseDt, baseSno);
            log.info("MMS 발송 대상 건수: {}", msgList.size());

            // 3. 발송 처리
            for (MmsTriggerDto msgHm : msgList) {

                taskDtm = msgHm.getTaskDtm();
                taskNo = new BigInteger(msgHm.getTaskNo());

                if (new BigInteger(baseDt).compareTo(new BigInteger(taskDtm)) < 0) {
                    baseDt = taskDtm;
                }
                taskCnt++;

                String orderNo = msgHm.getOrderNo();

                // 유효성 검증 - Service 위임
                String errMsg = hcSendMsgService.validateMessage(msgHm);

                if (errMsg != null) {
                    batchMapper.updateMsgErrorInfo(taskDtm, taskNo.toString(), errMsg);
                    log.error("[{}] {}", orderNo != null ? orderNo : taskNo, errMsg);

                    // 오류 발생 시 주문 취소 API 호출 - Service 위임
                    if (orderNo != null) {
                        hcSendMsgService.cancelOrder(mapper, orderNo);
                    }
                    continue;
                }

                // MMS 발송 처리 - Service 위임
                boolean sent = hcSendMsgService.sendMms(mapper, batchMapper, msgHm, taskDtm, taskNo.toString());

                if (sent) {
                    sendCnt++;
                } else {
                    // 발송 실패 시 주문 취소 API 호출 - Service 위임
                    hcSendMsgService.cancelOrder(mapper, orderNo);
                }
            }

            // Batch flush (축적된 INSERT/UPDATE 일괄 실행)
            if (taskCnt > 0) {
                batchSqlSession.flushStatements();
                log.info("Batch flush 완료 - 총 {}건 처리", taskCnt);
            }

            log.info("[TASK FINISH] baseDt={}, taskNo={}, sendCnt={}", baseDt, taskNo, sendCnt);

        } catch (Exception e) {
            log.error("MMS 발송 Job 실패: {}", e.getMessage(), e);
            throw e;
        }

        // 4. Lock 파일 업데이트
        if (taskCnt > 0) {
            fileUtil.writeLockFile(LOCK_FILE_NAME, baseDt + "," + taskNo.toString());
        }

        // 처리 건수 기록
        contribution.incrementWriteCount(sendCnt);

        return RepeatStatus.FINISHED;
    }
}
