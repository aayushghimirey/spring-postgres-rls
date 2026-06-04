package io.github.aayushghimirey.spring_postgres_rls.core;
 
import io.github.aayushghimirey.spring_postgres_rls.core.impl.DefaultRlsValidatorImpl;
import io.github.aayushghimirey.spring_postgres_rls.exception.RlsNotEnabledException;
import io.github.aayushghimirey.spring_postgres_rls.exception.RlsPolicyNotFoundException;
import io.github.aayushghimirey.spring_postgres_rls.exception.TableNotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
 
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Logger;
 
import static org.junit.jupiter.api.Assertions.*;
 
class TableRlsValidatorTest {
 
    private static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("postgres")
                    .withUsername("postgres")
                    .withPassword("postgres");
 
    @BeforeAll
    static void setUp() throws SQLException {
        postgres.start();
 
        try (Connection conn = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword())) {
 
            try (Statement stmt = conn.createStatement()) {
                // 1. Create table with RLS enabled and policies
                stmt.execute("CREATE TABLE tenants (id INT PRIMARY KEY, name VARCHAR(100))");
                stmt.execute("ALTER TABLE tenants ENABLE ROW LEVEL SECURITY");
                stmt.execute("""
                 CREATE POLICY tenant_policy ON tenants FOR ALL 
                        USING(id = current_setting('app.tenant_id', true)::int)
                        WITH CHECK(id = current_setting('app.tenant_id', true)::int)
                        """);

                // 2. Create table with RLS disabled
                stmt.execute("CREATE TABLE employees (id INT PRIMARY KEY, name VARCHAR(100))");

                // 3. Create table with RLS enabled but no policies
                stmt.execute("CREATE TABLE departments (id INT PRIMARY KEY, name VARCHAR(100))");
                stmt.execute("ALTER TABLE departments ENABLE ROW LEVEL SECURITY");
            }
        }
    }
 
    private static DataSource createDataSource() {
        return new DataSource() {
            @Override
            public Connection getConnection() throws SQLException {
                return DriverManager.getConnection(
                        postgres.getJdbcUrl(),
                        postgres.getUsername(),
                        postgres.getPassword()
                );
            }
 
            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                return DriverManager.getConnection(postgres.getJdbcUrl(), username, password);
            }
 
            @Override
            public PrintWriter getLogWriter() throws SQLException { return null; }
            @Override
            public void setLogWriter(PrintWriter out) throws SQLException {}
            @Override
            public void setLoginTimeout(int seconds) throws SQLException {}
            @Override
            public int getLoginTimeout() throws SQLException { return 0; }
            @Override
            public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                throw new SQLFeatureNotSupportedException();
            }
            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException { return null; }
            @Override
            public boolean isWrapperFor(Class<?> iface) throws SQLException { return false; }
        };
    }
 
    @Test // without policies
    void testStrictValidationSuccess() {
        DataSource dataSource = createDataSource();
        CoreRlsConfig.CoreTableConfig tableConfig = new CoreRlsConfig.CoreTableConfig(
                "tenants",
                List.of("tenant_policy")
        );
        CoreRlsConfig config = new CoreRlsConfig(List.of(tableConfig), ValidationMode.STRICT);
        DefaultRlsValidatorImpl validator = new DefaultRlsValidatorImpl(dataSource, config);
 
        assertDoesNotThrow(validator::validate);
    }


    @Test
    void testStrictValidationSuccessWithPolicies() {
        DataSource dataSource = createDataSource();
        CoreRlsConfig.CoreTableConfig tableConfig = new CoreRlsConfig.CoreTableConfig(
                "tenants",
                List.of()
        );
        CoreRlsConfig config = new CoreRlsConfig(List.of(tableConfig), ValidationMode.STRICT);
        DefaultRlsValidatorImpl validator = new DefaultRlsValidatorImpl(dataSource, config);

        assertDoesNotThrow(validator::validate);
    }

    @Test
    void testStrictValidationRlsDisabled() {
        DataSource dataSource = createDataSource();
        CoreRlsConfig.CoreTableConfig tableConfig = new CoreRlsConfig.CoreTableConfig(
                "employees",
                List.of()
        );
        CoreRlsConfig config = new CoreRlsConfig(List.of(tableConfig), ValidationMode.STRICT);
        DefaultRlsValidatorImpl validator = new DefaultRlsValidatorImpl(dataSource, config);
 
        assertThrows(RlsNotEnabledException.class, validator::validate);
    }
 
    @Test
    void testStrictValidationTableNotFound() {
        DataSource dataSource = createDataSource();
        CoreRlsConfig.CoreTableConfig tableConfig = new CoreRlsConfig.CoreTableConfig(
                "non_existent",
                List.of()
        );
        CoreRlsConfig config = new CoreRlsConfig(List.of(tableConfig), ValidationMode.STRICT);
        DefaultRlsValidatorImpl validator = new DefaultRlsValidatorImpl(dataSource, config);
 
        assertThrows(TableNotFoundException.class, validator::validate);
    }

    @Test
    void testStrictValidationPolicyNotFound() {
        DataSource dataSource = createDataSource();
        CoreRlsConfig.CoreTableConfig tableConfig = new CoreRlsConfig.CoreTableConfig(
                "tenants",
                List.of("non_existent_policy")
        );
        CoreRlsConfig config = new CoreRlsConfig(List.of(tableConfig), ValidationMode.STRICT);
        DefaultRlsValidatorImpl validator = new DefaultRlsValidatorImpl(dataSource, config);

        assertThrows(RlsPolicyNotFoundException.class, validator::validate);
    }


 
    @Test
    void testPermissiveValidationDoesNotThrow() {
        DataSource dataSource = createDataSource();
        CoreRlsConfig.CoreTableConfig t1 = new CoreRlsConfig.CoreTableConfig("employees", List.of());
        CoreRlsConfig.CoreTableConfig t2 = new CoreRlsConfig.CoreTableConfig("non_existent", List.of());
        CoreRlsConfig.CoreTableConfig t3 = new CoreRlsConfig.CoreTableConfig("tenants", List.of("missing_policy"));
 
        CoreRlsConfig config = new CoreRlsConfig(List.of(t1, t2, t3), ValidationMode.PERMISSIVE);
        DefaultRlsValidatorImpl validator = new DefaultRlsValidatorImpl(dataSource, config);
 
        assertDoesNotThrow(validator::validate);
    }

    @Test
    void testNoneValidationDisabled() {
        DataSource dataSource = createDataSource();
        CoreRlsConfig.CoreTableConfig tableConfig = new CoreRlsConfig.CoreTableConfig(
                "non_existent",
                List.of()
        );
        CoreRlsConfig config = new CoreRlsConfig(List.of(tableConfig), ValidationMode.NONE);
        DefaultRlsValidatorImpl validator = new DefaultRlsValidatorImpl(dataSource, config);
 
        assertDoesNotThrow(validator::validate);
    }



}