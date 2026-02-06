package com.secta.hcbatch.common.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 날짜 유틸리티
 */
@Slf4j
public class DateUtil {

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter YYYYMMDD_HHMMSS = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");

    /**
     * 현재 날짜를 yyyyMMdd 형식으로 반환
     */
    public static String getCurrentDate() {
        return LocalDate.now().format(YYYYMMDD);
    }

    /**
     * 현재 날짜시간을 yyyyMMdd HH:mm:ss 형식으로 반환
     */
    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(YYYYMMDD_HHMMSS);
    }

    /**
     * 날짜 더하기/빼기
     * 
     * @param type "D"(일), "M"(월), "Y"(년)
     * @param amount 더할 값 (음수면 빼기)
     * @param format 출력 포맷
     * @return 계산된 날짜 문자열
     */
    public static String addDateTime(String type, int amount, String format) {
        LocalDate date = LocalDate.now();
        
        switch (type.toUpperCase()) {
            case "D":
                date = date.plusDays(amount);
                break;
            case "M":
                date = date.plusMonths(amount);
                break;
            case "Y":
                date = date.plusYears(amount);
                break;
            default:
                log.warn("Unknown date type: {}", type);
        }
        
        return date.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * 문자열을 LocalDate로 변환
     */
    public static LocalDate parseDate(String dateStr) {
        return LocalDate.parse(dateStr, YYYYMMDD);
    }

    /**
     * LocalDate를 문자열로 변환
     */
    public static String formatDate(LocalDate date) {
        return date.format(YYYYMMDD);
    }

    /**
     * 전일 날짜 반환
     */
    public static String getYesterday() {
        return addDateTime("D", -1, "yyyyMMdd");
    }
}
