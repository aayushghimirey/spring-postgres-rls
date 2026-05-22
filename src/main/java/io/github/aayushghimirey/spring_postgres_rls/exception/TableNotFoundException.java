package io.github.aayushghimirey.spring_postgres_rls.exception;

public class TableNotFoundException extends RuntimeException{
    public TableNotFoundException(String message) {
        super(message);
    }
}
