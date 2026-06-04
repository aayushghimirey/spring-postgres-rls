package io.github.aayushghimirey.spring_postgres_rls.core.impl;

import io.github.aayushghimirey.spring_postgres_rls.core.RlsContextHolder;
import io.github.aayushghimirey.spring_postgres_rls.core.RlsSessionInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class DefaultRlsSessionInjector implements RlsSessionInjector {

    private static final Logger log = LoggerFactory.getLogger(DefaultRlsSessionInjector.class);

    private static final String SET_CONFIG_SQL = "SELECT set_config(?, ?, true)";

    private final DataSource dataSource;

    public DefaultRlsSessionInjector(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public void inject() {

        log.debug("Injecting RLS session config: {}", RlsContextHolder.getAll());

        Map<String, Object> context = RlsContextHolder.getAll();

        if (context.isEmpty()) {
            log.debug("RlsContextHolder is empty, skipping set_config injection");
            return;
        }

        // DataSourceUtils.getConnection returns the transaction-bound connection
        // when a Spring-managed transaction is active. We MUST NOT wrap this
        // in try-with-resources because that would call connection.close() directly,
        // potentially interfering with Spring's transaction management.
        Connection connection = DataSourceUtils.getConnection(dataSource);

        try {
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                try (PreparedStatement ps = connection.prepareStatement(SET_CONFIG_SQL)) {
                    ps.setString(1, entry.getKey());
                    ps.setString(2, String.valueOf(entry.getValue()));
                    ps.execute();
                    log.debug("Injected RLS config: {} = {}", entry.getKey(), entry.getValue());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to inject RLS session config via set_config", e);
        } finally {
            // releaseConnection honours Spring's transaction lifecycle:
            // - If the connection is transaction-bound, it is NOT closed here.
            // - If it was borrowed outside a transaction, it IS returned to the pool.
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
