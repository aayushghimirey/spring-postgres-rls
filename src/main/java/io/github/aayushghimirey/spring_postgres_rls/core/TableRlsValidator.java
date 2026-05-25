package io.github.aayushghimirey.spring_postgres_rls.core;

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

public class TableRlsValidator {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TableRlsValidator.class);

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


    public TableRlsValidator(
            DataSource dataSource,
            CoreRlsConfig coreRlsConfig) {
        this.dataSource = dataSource;
        this.coreRlsConfig = coreRlsConfig;
    }

    public void validate() {

        ValidationMode mode = coreRlsConfig.getValidationMode();

        if (mode == ValidationMode.NONE) {
            LOGGER.info("RLS validation disabled");
            return;
        }

        try (Connection conn = dataSource.getConnection()) {

            for (CoreRlsConfig.CoreTableConfig table : coreRlsConfig.getTables()) {

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

            ps.setString(1, table.getName());

            try (ResultSet rs = ps.executeQuery()) {

                if (!rs.next()) {
                    handleFailure(mode,
                            new TableNotFoundException("Table " + table.getName() + " not found"));
                    return;
                }

                boolean rls = rs.getBoolean(1);

                if (!rls) {
                    handleFailure(mode,
                            new RlsNotEnabledException("RLS not enabled for " + table.getName()));
                }
            }
        }

        if(table.getPolicies() != null && !table.getPolicies().isEmpty()) {
            validatePolicies(conn, table, mode);
        }
    }

    private void validatePolicies(Connection conn,
                                  CoreRlsConfig.CoreTableConfig table,
                                  ValidationMode mode) throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(VALIDATION_QUERY_FOR_POLICIES)) {

            ps.setString(1, table.getName());

            try (ResultSet rs = ps.executeQuery()) {

                List<String> existingPolicies = new java.util.ArrayList<>();

                while (rs.next()) {
                    existingPolicies.add(rs.getString("policyname"));
                }

                for (String expected : table.getPolicies()) {

                    if (!existingPolicies.contains(expected)) {
                        handleFailure(mode,
                                new RlsPolicyNotFoundException(
                                        "Policy '" + expected + "' missing for table " + table.getName()
                                ));
                    }
                }
            }
        }
    }

    private void handleFailure(
            ValidationMode validationMode,
            RuntimeException exception) {

        switch (validationMode) {
            case STRICT -> throw exception;
            case PERMISSIVE -> LOGGER.error(exception.getMessage());
            case NONE -> {
                // no-op
            }
        }
    }
}