package com.secta.hcbatch.common.exception;

/**
 * 배치 처리 공통 예외
 */
public class BatchException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String errorCode;

    public BatchException(String errorCode, String message) {
        super(errorCode + ": " + message);
        this.errorCode = errorCode;
    }

    public BatchException(String errorCode, String message, Throwable cause) {
        super(errorCode + ": " + message, cause);
        this.errorCode = errorCode;
    }

    public BatchException(Throwable cause) {
        super(cause.getMessage(), cause);
        this.errorCode = "UNKNOWN";
    }

    public String getErrorCode() {
        return errorCode;
    }
}
