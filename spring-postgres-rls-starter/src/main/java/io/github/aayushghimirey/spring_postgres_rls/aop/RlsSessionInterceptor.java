package io.github.aayushghimirey.spring_postgres_rls.aop;

import io.github.aayushghimirey.spring_postgres_rls.core.RlsContextHolder;
import io.github.aayushghimirey.spring_postgres_rls.core.RlsSessionInjector;
import io.github.aayushghimirey.spring_postgres_rls.core.ValidationMode;
import io.github.aayushghimirey.spring_postgres_rls.exception.RlsTransactionRequiredException;
import io.github.aayushghimirey.spring_postgres_rls.exception.RlsValidationException;
import io.github.aayushghimirey.spring_postgres_rls.properties.RlsProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * AOP interceptor that fires on methods annotated with {@code @UseRls}.
 *
 * <p>Before the annotated method executes, this interceptor calls
 * {@link RlsSessionInjectorImpl#inject()} to push all values from
 * {@link io.github.aayushghimirey.spring_postgres_rls.core.RlsContextHolder}
 * into PostgreSQL's session via {@code set_config}.
 *
 * <p><strong>Transaction Requirement:</strong><br>
 * A Spring-managed transaction MUST be active when {@code @UseRls} fires.
 * This is because {@code set_config(key, value, true)} is transaction-local —
 * it lives on the exact database connection bound to the transaction.
 * Without an active transaction, the injector cannot guarantee it will use
 * the same connection as subsequent queries in the method.
 *
 * <p>In {@code STRICT} mode, missing transaction throws
 * {@link RlsTransactionRequiredException}.
 * In {@code PERMISSIVE} mode, the method is executed without injection
 * and a warning is logged.
 */

@Aspect
public class RlsSessionInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RlsSessionInterceptor.class);

    private final RlsSessionInjector rlsSessionInjector;
    private final RlsProperties rlsProperties;

    public RlsSessionInterceptor(RlsSessionInjector rlsSessionInjector, RlsProperties rlsProperties) {
        this.rlsSessionInjector = rlsSessionInjector;
        this.rlsProperties = rlsProperties;
    }

    @Around("@annotation(io.github.aayushghimirey.spring_postgres_rls.annotations.UseRls)")
    public Object injectRlsSession(ProceedingJoinPoint pjp) throws Throwable {

        log.debug("@UseRls interceptor triggered for method: {}.{}",
                pjp.getSignature().getDeclaringTypeName(),
                pjp.getSignature().getName()
        );

        if (!TransactionSynchronizationManager.isActualTransactionActive()) {

            if (rlsProperties.getValidationMode() == ValidationMode.STRICT) {
                throw new RlsTransactionRequiredException(
                        "@UseRls requires an active Spring transaction. " +
                        "Ensure the calling method (or this method) is annotated with @Transactional."
                );
            }

            log.error(
                    "@UseRls on [{}.{}] called without an active transaction. " +
                    "RLS context will NOT be injected. Add @Transactional to enforce this.",
                    pjp.getSignature().getDeclaringTypeName(),
                    pjp.getSignature().getName()
            );

            return pjp.proceed();
        }

        log.trace("Active transaction found. Proceeding to inject RLS session config.");
        try {
            rlsSessionInjector.inject();
            return pjp.proceed();
        } finally {
            RlsContextHolder.clear();
        }
    }
}
