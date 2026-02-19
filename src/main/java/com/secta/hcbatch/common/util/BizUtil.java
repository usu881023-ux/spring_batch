package com.secta.hcbatch.common.util;

import com.kbs.util.encryptor.BCipher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 비즈니스 유틸리티
 */
@Slf4j
public class BizUtil {

    private BizUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 마스킹 처리
     * - 3자 이상: 첫글자와 마지막 글자 제외 마스킹
     * - 2자 이하: 마지막 글자만 마스킹
     */
    public static String maskingChar(String plainVal) {
        if (StringUtil.isEmpty(plainVal)) {
            return "";
        }

        if (plainVal.length() > 2) {
            char[] plainArr = plainVal.toCharArray();
            for (int i = 1; i < plainVal.length() - 1; i++) {
                plainArr[i] = '*';
            }
            return String.valueOf(plainArr);
        } else {
            return plainVal.replaceAll(".$", "*");
        }
    }

    /**
     * 전화번호 마스킹
     * - 010-1234-5678 -> 010-****-5678
     */
    public static String maskingPhone(String phone) {
        if (StringUtil.isEmpty(phone)) {
            return "";
        }
        String cleaned = phone.replaceAll("-", "");
        if (cleaned.length() < 7) {
            return maskingChar(cleaned);
        }
        // 가운데 4자리 마스킹
        return cleaned.substring(0, 3) + "****" + cleaned.substring(cleaned.length() - 4);
    }

    /**
     * 랜덤 영숫자 생성
     */
    public static String getRandomAlphanumeric(int length) {
        return RandomStringUtils.randomAlphanumeric(length);
    }

    /**
     * SHA-256 해시
     */
    public static String sha256(String plainVal) {
        if (StringUtil.isEmpty(plainVal)) {
            return "";
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(plainVal.getBytes());
            byte[] byteData = md.digest();

            StringBuilder hexString = new StringBuilder();
            for (byte b : byteData) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString().toLowerCase();

        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 해시 실패: {}", e.getMessage());
            return "";
        }
    }

    /**
     * 상품권 번호 복호화
     * TODO: BCipher 라이브러리 연동 필요
     */
    public static String decryptVoucherNo(String encryptedNo) {
        if (StringUtil.isEmpty(encryptedNo)) {
            return "";
        }

        try {
            encryptedNo = BCipher.decryptCoupon(encryptedNo);
            // -Dbcipher.library.path에 "libbcipher.so/dll" 경로 지정 필요
            log.warn("BCipher 복호화 미구현 - 원본값 반환: {}", encryptedNo);
            return encryptedNo;
        } catch (Exception e) {
            log.error("상품권 복호화 실패: {}", e.getMessage());
            return "";
        }
    }
}
