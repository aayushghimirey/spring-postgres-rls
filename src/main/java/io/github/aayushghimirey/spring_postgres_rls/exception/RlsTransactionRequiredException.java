package io.github.aayushghimirey.spring_postgres_rls.exception;

/**
 * Thrown in STRICT validation mode when a method annotated with {@code @UseRls}
 * is invoked without an active Spring-managed transaction.
 *
 * <p>{@code set_config(key, value, true)} is transaction-local, meaning it only
 * persists on the specific database connection for the duration of one transaction.
 * Without a transaction, the injector cannot guarantee the RLS session settings
 * will be visible to subsequent queries in the same call chain.
 */
public class RlsTransactionRequiredException extends RuntimeException {

    public RlsTransactionRequiredException(String message) {
        super(message);
    }
}
