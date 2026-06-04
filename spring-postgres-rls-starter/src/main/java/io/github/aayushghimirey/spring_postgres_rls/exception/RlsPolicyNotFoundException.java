package io.github.aayushghimirey.spring_postgres_rls.exception;

public class RlsPolicyNotFoundException extends RuntimeException{
    public RlsPolicyNotFoundException(String message) {
        super(message);
    }
}
