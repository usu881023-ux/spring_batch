package com.secta.hcbatch.job.hcon;

import com.secta.hcbatch.common.dto.VoucherChangeHistDto;
import com.secta.hcbatch.common.util.FileUtil;
import com.secta.hcbatch.common.util.StringUtil;
import com.secta.hcbatch.job.hcon.mapper.HcTaskMapper;
import com.secta.hcbatch.job.hcon.mapper.HconTaskMapper;
import com.secta.hcbatch.job.hcon.service.VoucherSyncService;
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
 * 상품권 동기화 Tasklet
 *
 * 처리 프로세스:
 * 1. Lock 파일에서 기준 타임스탬프 읽기
 * 2. HCON DB의 CPN_HIST 테이블에서 변경 이력 조회
 * 3. 변경 유형에 따른 상태 처리 (사용/사용취소/폐기/유효기간연장)
 * 4. HC DB의 T_HAPPYCON_COUPON 테이블 UPDATE
 * 5. H_HAPPYCON_COUPON_HST 테이블에 이력 INSERT
 * 6. Lock 파일 업데이트
 */
@Slf4j
@Component
public class HconSyncTasklet implements Tasklet {

    private static final String LOCK_FILE_NAME = "hcon_vchr_trig.lock";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final SqlSession hcSqlSession;
    private final SqlSession hcBatchSqlSession;
    private final SqlSession hconSqlSession;
    private final FileUtil fileUtil;
    private final VoucherSyncService voucherSyncService;

    public HconSyncTasklet(@Qualifier("prvaSqlSessionTemplate") SqlSession hcSqlSession,
                           @Qualifier("prvaBatchSqlSessionTemplate") SqlSession hcBatchSqlSession,
                           @Qualifier("hconSqlSessionTemplate") SqlSession hconSqlSession,
                           FileUtil fileUtil,
                           VoucherSyncService voucherSyncService) {
        this.hcSqlSession = hcSqlSession;
        this.hcBatchSqlSession = hcBatchSqlSession;
        this.hconSqlSession = hconSqlSession;
        this.fileUtil = fileUtil;
        this.voucherSyncService = voucherSyncService;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        String baseDt = "";
        String baseSno = "";
        String taskDtm = "";
        BigInteger taskNo = BigInteger.ZERO;
        int taskCnt = 0;
        int chgCnt = 0;

        try {
            // 1. Lock 파일에서 기준 타임스탬프 읽기
            String lockContent = fileUtil.readLockFile(LOCK_FILE_NAME);
            String[] lockArr = lockContent.split(",", -1);
            baseDt = lockArr.length > 0 ? lockArr[0] : "";
            baseSno = lockArr.length > 1 ? lockArr[1] : "";

            // 24시간 이내만 처리
            String minDt = LocalDateTime.now().minusHours(24).format(TIMESTAMP_FORMAT);

            if (StringUtil.isEmpty(baseDt)) {
                baseDt = LocalDateTime.now().minusSeconds(5).format(TIMESTAMP_FORMAT) + "000000000";
            } else if (baseDt.length() >= 14 && baseDt.substring(0, 14).compareTo(minDt) < 0) {
                baseDt = minDt + "000000000";
            }

            log.info("[TASK START] taskDtm={}, chgSno={}", baseDt, baseSno);

            // SELECT는 SIMPLE session, UPDATE/INSERT는 BATCH session 사용
            HcTaskMapper hcMapper = hcSqlSession.getMapper(HcTaskMapper.class);
            HcTaskMapper hcBatchMapper = hcBatchSqlSession.getMapper(HcTaskMapper.class);
            HconTaskMapper hconMapper = hconSqlSession.getMapper(HconTaskMapper.class);

            // 2. 해피콘 상품권 변경이력 조회
            List<VoucherChangeHistDto> vchrList = hconMapper.selectVchrChgHst(baseDt, baseSno);
            log.info("동기화 대상 건수: {}", vchrList.size());

            // 3. 변경 이력 처리
            for (VoucherChangeHistDto vchrHm : vchrList) {

                taskDtm = vchrHm.getTaskDtm();
                taskNo = new BigInteger(vchrHm.getChgSno());

                if (new BigInteger(baseDt).compareTo(new BigInteger(taskDtm)) < 0) {
                    baseDt = taskDtm;
                }
                taskCnt++;

                // 단일 변경이력 동기화 처리 - Service 위임
                boolean processed = voucherSyncService.syncVoucher(hcMapper, hcBatchMapper, vchrHm, taskDtm);
                if (processed) {
                    chgCnt++;
                }
            }

            // Batch flush (축적된 UPDATE/INSERT 일괄 실행)
            if (chgCnt > 0) {
                hcBatchSqlSession.flushStatements();
                log.info("Batch flush 완료 - 총 {}건 처리", chgCnt);
            }

            log.info("[TASK FINISH] baseDt={}, taskNo={}, chgCnt={}", baseDt, taskNo, chgCnt);

        } catch (Exception e) {
            log.error("상품권 동기화 Job 실패: {}", e.getMessage(), e);
            throw e;
        }

        // Lock 파일 업데이트
        if (taskCnt > 0) {
            fileUtil.writeLockFile(LOCK_FILE_NAME, baseDt + "," + taskNo.toString());
        }

        // 처리 건수 기록
        contribution.incrementWriteCount(chgCnt);

        return RepeatStatus.FINISHED;
    }
}
