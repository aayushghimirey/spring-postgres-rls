package io.github.aayushghimirey.spring_postgres_rls.exception;

public class RlsNotEnabledException extends RuntimeException{
    public RlsNotEnabledException(String message) {
        super(message);
    }
}
