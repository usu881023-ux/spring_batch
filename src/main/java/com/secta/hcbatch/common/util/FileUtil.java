package com.secta.hcbatch.common.util;

import com.secta.hcbatch.common.exception.BatchException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 파일 유틸리티
 */
@Slf4j
@Component
public class FileUtil {

    private final String lockPath;

    public FileUtil(@Value("${batch.lock.path:./locks}") String lockPath) {
        this.lockPath = lockPath;
        // Lock 디렉토리 생성
        try {
            Path path = Paths.get(lockPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("Lock 디렉토리 생성: {}", lockPath);
            }
        } catch (IOException e) {
            log.warn("Lock 디렉토리 생성 실패: {}", e.getMessage());
        }
    }

    /**
     * Lock 파일 읽기
     */
    public String readLockFile(String fileName) throws BatchException {
        Path filePath = Paths.get(lockPath, fileName);

        if (!Files.exists(filePath)) {
            log.debug("Lock 파일 없음: {}", filePath);
            return "";
        }

        try (BufferedReader reader = new BufferedReader(
                new FileReader(filePath.toFile(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (IOException e) {
            throw new BatchException("FU01", "Lock 파일 읽기 실패: " + e.getMessage(), e);
        }
    }

    /**
     * Lock 파일 쓰기
     */
    public void writeLockFile(String fileName, String content) throws BatchException {
        Path filePath = Paths.get(lockPath, fileName);

        try (BufferedOutputStream bos = new BufferedOutputStream(
                new FileOutputStream(filePath.toFile(), false))) {
            bos.write(content.getBytes(StandardCharsets.UTF_8));
            log.debug("Lock 파일 저장: {} = {}", fileName, content);
        } catch (IOException e) {
            throw new BatchException("FU02", "Lock 파일 쓰기 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 디렉토리 생성
     */
    public boolean createDirectory(String dirPath) {
        try {
            Path path = Paths.get(dirPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                return true;
            }
            return false;
        } catch (IOException e) {
            log.error("디렉토리 생성 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 파일 존재 여부 확인
     */
    public boolean exists(String filePath) {
        return Files.exists(Paths.get(filePath));
    }

    /**
     * 파일 삭제
     */
    public boolean delete(String filePath) {
        try {
            return Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", e.getMessage());
            return false;
        }
    }
}
