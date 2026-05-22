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

        List<String> tables =
                coreRlsConfig.getTables();

        try (Connection connection = dataSource.getConnection();
                PreparedStatement ps =
                     connection.prepareStatement(VALIDATION_QUERY)) {

            for (String table : tables) {

                ps.setString(1, table);

                try (ResultSet rs = ps.executeQuery()) {

                    if (!rs.next()) {

                        handleFailure(
                                validationMode,
                                new TableNotFoundException(
                                        "Table "
                                                + table
                                                + " does not exist"
                                )
                        );

                        continue;
                    }

                    boolean enabled =
                            rs.getBoolean("relrowsecurity");

                    if (!enabled) {

                        handleFailure(
                                validationMode,
                                new RlsNotEnabledException(
                                        "RLS not enabled for table: "
                                                + table
                                )
                        );

                        continue;
                    }

                    LOGGER.info(
                            "Validated RLS for table: "
                                    + table
                    );
                }
            }

        } catch (SQLException ex) {

            throw new RuntimeException(
                    "Failed to validate RLS tables",
                    ex
            );
        }
    }

    private void handleFailure(
            ValidationMode validationMode,
            RuntimeException exception) {

        switch (validationMode) {

            case STRICT -> throw exception;

            case PERMISSIVE ->
                    LOGGER.warn(exception.getMessage());

            case NONE -> {
                // no-op
            }
        }
    }
}