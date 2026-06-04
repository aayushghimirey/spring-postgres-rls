package io.github.aayushghimirey.spring_postgres_rls.core;


// This interface implementation is responsible
// for injecting all the RlsContextHolder sessions into the database connection as Local Transaction scope.
public interface RlsSessionInjector {
    void inject();
}
