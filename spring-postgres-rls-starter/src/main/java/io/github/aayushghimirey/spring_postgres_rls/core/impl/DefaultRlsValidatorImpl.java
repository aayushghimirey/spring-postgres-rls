package io.github.aayushghimirey.spring_postgres_rls.core.impl;

import io.github.aayushghimirey.spring_postgres_rls.core.CoreRlsConfig;
import io.github.aayushghimirey.spring_postgres_rls.core.TableRlsValidator;
import io.github.aayushghimirey.spring_postgres_rls.core.ValidationMode;
import io.github.aayushghimirey.spring_postgres_rls.exception.RlsNotEnabledException;
import io.github.aayushghimirey.spring_postgres_rls.exception.RlsPolicyNotFoundException;
import io.github.aayushghimirey.spring_postgres_rls.exception.TableNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DefaultRlsValidatorImpl implements TableRlsValidator {

    private static final Logger log =
            LoggerFactory.getLogger(DefaultRlsValidatorImpl.class);

    private static final String VALIDATION_QUERY_FOR_RLS_ENABLED = """
            SELECT relrowsecurity
            FROM pg_class
            WHERE relname = ?
            """;

    private static final String VALIDATION_QUERY_FOR_POLICIES = """
            SELECT policyname
            FROM pg_policies
            WHERE tablename = ?
            """;

    private final DataSource dataSource;
    private final CoreRlsConfig coreRlsConfig;


    public DefaultRlsValidatorImpl(
            DataSource dataSource,
            CoreRlsConfig coreRlsConfig) {
        this.dataSource = dataSource;
        this.coreRlsConfig = coreRlsConfig;
    }

    public void validate() {
        log.trace("Starting RLS validation");

        ValidationMode mode = coreRlsConfig.validationMode();

        if (mode == ValidationMode.NONE) {
            log.info("RLS validation disabled");
            return;
        }

        try (Connection conn = dataSource.getConnection()) {

            for (CoreRlsConfig.CoreTableConfig table : coreRlsConfig.tables()) {

                validateTable(conn, table, mode);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to validate RLS", e);
        }
    }

    private void validateTable(Connection conn,
                               CoreRlsConfig.CoreTableConfig table,
                               ValidationMode mode) throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(VALIDATION_QUERY_FOR_RLS_ENABLED)) {

            ps.setString(1, table.name());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    handleFailure(mode,
                            new TableNotFoundException("Table " + table.name() + " not found"));
                    return;
                }

                boolean rls = rs.getBoolean(1);

                if (!rls) {
                    handleFailure(mode,
                            new RlsNotEnabledException("RLS not enabled for " + table.name()));
                }

                log.debug("Table '{}' found with RLS enabled: {}", table.name(), rls);
            }
        }

        if(table.policies() != null && !table.policies().isEmpty()) {
            validatePolicies(conn, table, mode);
        }
    }

    private void validatePolicies(Connection conn,
                                  CoreRlsConfig.CoreTableConfig table,
                                  ValidationMode mode) throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(VALIDATION_QUERY_FOR_POLICIES)) {

            ps.setString(1, table.name());

            try (ResultSet rs = ps.executeQuery()) {

                List<String> existingPolicies = new java.util.ArrayList<>();

                while (rs.next()) {
                    existingPolicies.add(rs.getString("policyname"));
                }

                for (String expected : table.policies()) {

                    if (!existingPolicies.contains(expected)) {
                        handleFailure(mode,
                                new RlsPolicyNotFoundException(
                                        "Policy '" + expected + "' missing for table " + table.name()
                                ));
                    }

                    log.trace("Policy '{}' found for table '{}'", expected, table.name());
                }
            }
        }
    }

    private void handleFailure(
            ValidationMode validationMode,
            RuntimeException exception) {

        switch (validationMode) {
            case STRICT -> throw exception;
            case PERMISSIVE -> log.error(exception.getMessage());
            case NONE -> {
                // no-op
            }
        }
    }
}