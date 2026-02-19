package com.secta.hcbatch.common.util;

import org.json.simple.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 문자열 유틸리티
 */
public class StringUtil {

    private StringUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 문자열 null/empty 체크
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Object null/empty 체크
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof JSONObject) {
            return "{}".equals(((JSONObject) obj).toJSONString().trim());
        }
        return obj.toString().trim().isEmpty();
    }

    /**
     * JSONObject에서 문자열 추출
     */
    @SuppressWarnings("unchecked")
    public static String jsonToStr(JSONObject obj, String key) {
        if (obj == null || obj.get(key) == null) {
            return "";
        }
        return String.valueOf(obj.get(key));
    }

    /**
     * null을 빈 문자열로 변환
     */
    public static String nvl(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    /**
     * null을 빈 문자열로 변환
     */
    public static String nvl(String str) {
        return isEmpty(str) ? "" : str;
    }

    /**
     * null을 공백으로 변환 (Oracle용)
     */
    public static String nvlOracle(String str) {
        String val = nvl(str);
        return val.isEmpty() ? " " : val;
    }

    /**
     * 평문을 JSON 이스케이프 처리
     */
    public static String plainToJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\r\n", "\\n")
                  .replace("\n", "\\n");
    }

    /**
     * HTML 태그 제거
     */
    public static String stripHtmlTags(String str) {
        if (str == null) {
            return "";
        }

        Pattern scripts = Pattern.compile("<(no)?script[^>]*>.*?</(no)?script>", Pattern.DOTALL);
        Pattern style = Pattern.compile("<style[^>]*>.*</style>", Pattern.DOTALL);
        Pattern tags = Pattern.compile("<(\"[^\"]*\"|'[^']*'|[^'\">])*>");
        Pattern entityRefs = Pattern.compile("&[^;]+;");
        Pattern whitespace = Pattern.compile("\\s\\s+");

        Matcher m;
        String ret = str;

        m = scripts.matcher(ret);
        ret = m.replaceAll("");
        m = style.matcher(ret);
        ret = m.replaceAll("");
        m = tags.matcher(ret);
        ret = m.replaceAll("");
        m = entityRefs.matcher(ret);
        ret = m.replaceAll("");
        m = whitespace.matcher(ret);
        ret = m.replaceAll(" ");

        return ret;
    }

    /**
     * URL 인코딩
     */
    public static String urlEncode(String str) {
        if (str == null) {
            return "";
        }
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * 왼쪽 패딩
     */
    public static String leftPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        return String.valueOf(padChar).repeat(pads) + str;
    }

    /**
     * 오른쪽 패딩
     */
    public static String rightPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        return str + String.valueOf(padChar).repeat(pads);
    }

    /**
     * Base64 인코딩
     */
    public static String encodeBase64(String plain) {
        if (plain == null) {
            return "";
        }
        try {
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(plain.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Base64 디코딩
     */
    public static String decodeBase64(String encoded) {
        if (encoded == null) {
            return "";
        }
        try {
            return new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }
}
