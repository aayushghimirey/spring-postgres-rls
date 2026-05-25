package io.github.aayushghimirey.spring_postgres_rls.core;

import io.github.aayushghimirey.spring_postgres_rls.exception.RlsNotEnabledException;
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

    private static final String VALIDATION_QUERY = """
            SELECT relrowsecurity
            FROM pg_class
            WHERE relname = ?
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
        ValidationMode validationMode =
                coreRlsConfig.getValidationMode();

        if (validationMode == ValidationMode.NONE) {
            LOGGER.info("RLS validation disabled");
            return;
        }

        try (Connection connection = dataSource.getConnection();
             PreparedStatement psRls = connection.prepareStatement(VALIDATION_QUERY)) {

            for (CoreRlsConfig.CoreTableConfig tableConfig : coreRlsConfig.getTables()) {
                String tableName = tableConfig.getName();
                List<String> expectedPolicies = tableConfig.getPolicies();

                // 1. Verify table exists and RLS status
                psRls.setString(1, tableName);
                try (ResultSet rsRls = psRls.executeQuery()) {
                    if (!rsRls.next()) {
                        handleFailure(
                                validationMode,
                                new TableNotFoundException("Table '" + tableName + "' does not exist")
                        );
                        continue;
                    }

                    boolean enabled = rsRls.getBoolean(1);
                    if (!enabled) {
                        handleFailure(
                                validationMode,
                                new RlsNotEnabledException("RLS not enabled for table '" + tableName + "'")
                        );
                    }
                }


                LOGGER.debug("Validated RLS for table: {}", tableName);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to validate RLS tables", e);
        }
    }

    private void handleFailure(
            ValidationMode validationMode,
            RuntimeException exception) {

        switch (validationMode) {
            case STRICT -> throw exception;
            case PERMISSIVE -> LOGGER.warn(exception.getMessage());
            case NONE -> {
                // no-op
            }
        }
    }
}