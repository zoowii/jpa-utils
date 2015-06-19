package com.zoowii.jpa_utils.exceptions;

/**
 * Created by zoowii on 15/1/26.
 */
public class JdbcTransactionRuntimeException extends JdbcRuntimeException {
    public JdbcTransactionRuntimeException() {
    }

    public JdbcTransactionRuntimeException(String message) {
        super(message);
    }

    public JdbcTransactionRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public JdbcTransactionRuntimeException(Throwable cause) {
        super(cause);
    }

    public JdbcTransactionRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
