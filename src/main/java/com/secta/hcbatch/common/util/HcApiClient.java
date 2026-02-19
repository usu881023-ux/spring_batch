package com.secta.hcbatch.common.util;

import com.secta.hcbatch.common.exception.BatchException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class HcApiClient {

    private static final int DEFAULT_TIMEOUT = 10_000;

    /**
     * timeout별 RestTemplate 캐시 (동일 timeout에 대해 RestTemplate 재사용)
     */
    private final ConcurrentHashMap<Integer, RestTemplate> templateCache = new ConcurrentHashMap<>();

    /**
     * timeout 적용된 RestTemplate 조회 (캐싱)
     */
    private RestTemplate getRestTemplate(int timeoutMillis) {
        return templateCache.computeIfAbsent(timeoutMillis, timeout -> {
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(timeout);
            factory.setReadTimeout(timeout);
            return new RestTemplate(factory);
        });
    }

    /**
     * POST 요청 (JSON) - 타임아웃 지정
     */
    public String postJson(String url, String jsonBody, int timeoutMillis) throws BatchException {
        RestTemplate restTemplate = getRestTemplate(timeoutMillis);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(java.util.Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            log.debug("HTTP POST 요청: url={}, body={}", url, jsonBody);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new BatchException(
                        "HTTP" + response.getStatusCode().value(),
                        "HTTP 요청 실패: " + response.getStatusCode()
                );
            }

            log.debug("HTTP POST 응답: status={}, body={}", response.getStatusCode(), response.getBody());
            return response.getBody();

        } catch (BatchException e) {
            throw e;
        } catch (Exception e) {
            log.error("HTTP POST 요청 실패: url={}, error={}", url, e.getMessage(), e);
            throw new BatchException("HTTP00", "HTTP 요청 실패: " + e.getMessage(), e);
        }
    }

    /**
     * POST 요청 (Form Data)
     */
    public String postForm(String url, Map<String, String> formData, int timeoutMillis) throws BatchException {
        RestTemplate restTemplate = getRestTemplate(timeoutMillis);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            StringBuilder body = new StringBuilder();
            for (Map.Entry<String, String> entry : formData.entrySet()) {
                if (body.length() > 0) {
                    body.append("&");
                }
                body.append(entry.getKey()).append("=").append(entry.getValue());
            }

            HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new BatchException(
                        "HTTP" + response.getStatusCode().value(),
                        "HTTP 요청 실패: " + response.getStatusCode()
                );
            }

            return response.getBody();

        } catch (BatchException e) {
            throw e;
        } catch (Exception e) {
            log.error("HTTP POST Form 요청 실패: url={}, error={}", url, e.getMessage(), e);
            throw new BatchException("HTTP00", "HTTP 요청 실패: " + e.getMessage(), e);
        }
    }

    /**
     * GET 요청
     */
    public String get(String url, int timeoutMillis) throws BatchException {
        RestTemplate restTemplate = getRestTemplate(timeoutMillis);

        try {
            ResponseEntity<String> response =
                    restTemplate.getForEntity(url, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new BatchException(
                        "HTTP" + response.getStatusCode().value(),
                        "HTTP 요청 실패: " + response.getStatusCode()
                );
            }

            return response.getBody();

        } catch (BatchException e) {
            throw e;
        } catch (Exception e) {
            log.error("HTTP GET 요청 실패: url={}, error={}", url, e.getMessage(), e);
            throw new BatchException("HTTP00", "HTTP 요청 실패: " + e.getMessage(), e);
        }
    }
}
