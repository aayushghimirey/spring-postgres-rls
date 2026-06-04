package io.github.aayushghimirey.spring_postgres_rls.core;

// This interface implementation is responsible for validating
// the RLS policies on the tables before the application starts.
public interface TableRlsValidator {
     void validate();
}
