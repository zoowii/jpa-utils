package com.zoowii.jpa_utils.exceptions;

/**
 * Created by zoowii on 15/1/26.
 */
public class JdbcRuntimeException extends RuntimeException {
    public JdbcRuntimeException() {
    }

    public JdbcRuntimeException(String message) {
        super(message);
    }

    public JdbcRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public JdbcRuntimeException(Throwable cause) {
        super(cause);
    }

    public JdbcRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
