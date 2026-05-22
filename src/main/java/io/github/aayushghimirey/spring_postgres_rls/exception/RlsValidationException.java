package io.github.aayushghimirey.spring_postgres_rls.exception;

public class RlsValidationException extends RuntimeException {
    public RlsValidationException(String message) {
        super(message);
    }

    public RlsValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
