package com.secta.hcbatch.common.util;

import com.secta.hcbatch.common.exception.BatchException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * AWS S3 유틸리티
 */
@Slf4j
@Component
public class S3Util {

    @Value("${aws.s3.access-key:}")
    private String accessKey;

    @Value("${aws.s3.secret-key:}")
    private String secretKey;

    @Value("${aws.s3.bucket-name:happy-objects}")
    private String bucketName;

    @Value("${aws.s3.region:ap-northeast-2}")
    private String region;

    @Value("${batch.mms.local-path:./mms}")
    private String mmsLocalPath;

    private S3Client s3Client;

    @PostConstruct
    public void init() {
        if (StringUtil.isEmpty(accessKey) || StringUtil.isEmpty(secretKey)) {
            log.warn("AWS S3 자격증명이 설정되지 않았습니다. S3 기능이 비활성화됩니다.");
            return;
        }

        try {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            this.s3Client = S3Client.builder()
                    .region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();
            log.info("AWS S3 클라이언트 초기화 완료: bucket={}, region={}", bucketName, region);
        } catch (Exception e) {
            log.error("AWS S3 클라이언트 초기화 실패: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void destroy() {
        if (s3Client != null) {
            s3Client.close();
            log.info("AWS S3 클라이언트 종료");
        }
    }

    /**
     * S3에서 상품권 이미지 다운로드
     *
     * @param s3KeyName S3 키 (예: /voucher/2026/02/09/image.jpg)
     * @return 로컬 파일 경로
     */
    public String downloadVoucherImage(String s3KeyName) throws BatchException {
        if (s3Client == null) {
            throw new BatchException("S3_01", "S3 클라이언트가 초기화되지 않았습니다");
        }

        if (StringUtil.isEmpty(s3KeyName)) {
            throw new BatchException("S3_02", "S3 키가 비어있습니다");
        }

        String localFilePath = "";
        FileOutputStream fos = null;

        try {
            // S3 키 파싱 (예: /voucher/2026/02/09/image.jpg)
            String[] keyArr = s3KeyName.split("/", -1);
            if (keyArr.length < 6) {
                throw new BatchException("S3_03", "잘못된 S3 키 형식: " + s3KeyName);
            }

            // 로컬 경로 생성 (예: ./mms/20260209/image.jpg)
            String datePath = keyArr[2] + keyArr[3] + keyArr[4];
            String fileName = keyArr[5];
            Path localDir = Paths.get(mmsLocalPath, datePath);

            if (!Files.exists(localDir)) {
                Files.createDirectories(localDir);
            }

            localFilePath = localDir.resolve(fileName).toString();

            // S3에서 다운로드
            GetObjectRequest request = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3KeyName)
                    .build();

            ResponseBytes<GetObjectResponse> response = s3Client.getObjectAsBytes(request);

            // 로컬 파일로 저장
            fos = new FileOutputStream(new File(localFilePath));
            fos.write(response.asByteArray());

            log.debug("S3 이미지 다운로드 완료: {} -> {}", s3KeyName, localFilePath);
            return localFilePath;

        } catch (S3Exception e) {
            log.error("S3 다운로드 실패: {}", e.getMessage());
            throw new BatchException("S3_04", "S3 다운로드 실패: " + e.getMessage(), e);
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", e.getMessage());
            throw new BatchException("S3_05", "파일 저장 실패: " + e.getMessage(), e);
        } catch (BatchException e) {
            throw e;
        } catch (Exception e) {
            log.error("S3 처리 오류: {}", e.getMessage());
            throw new BatchException("S3_99", "S3 처리 오류: " + e.getMessage(), e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * S3 클라이언트 사용 가능 여부
     */
    public boolean isAvailable() {
        return s3Client != null;
    }

    /**
     * S3 클라이언트 반환 (직접 사용이 필요한 경우)
     */
    public S3Client getS3Client() {
        return s3Client;
    }
}
