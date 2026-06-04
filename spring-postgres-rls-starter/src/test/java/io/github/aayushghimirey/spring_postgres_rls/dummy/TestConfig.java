package io.github.aayushghimirey.spring_postgres_rls.dummy;

import io.github.aayushghimirey.spring_postgres_rls.aop.RlsSessionInterceptor;
import io.github.aayushghimirey.spring_postgres_rls.core.RlsSessionInjector;
import io.github.aayushghimirey.spring_postgres_rls.properties.RlsProperties;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootConfiguration
@EnableTransactionManagement
@EnableAspectJAutoProxy
public class TestConfig {

    @Bean
    public RlsProperties rlsProperties() {
        return new RlsProperties();
    }

    @Bean
    public DataSource dataSource() throws java.sql.SQLException {
        DataSource ds = org.mockito.Mockito.mock(DataSource.class);
        java.sql.Connection conn = org.mockito.Mockito.mock(java.sql.Connection.class);
        org.mockito.Mockito.when(ds.getConnection()).thenReturn(conn);
        return ds;
    }

    @Bean
    public TestService testService() {
        return new TestService();
    }

    @Bean
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean
    public RlsSessionInterceptor rlsSessionInterceptor(
            RlsSessionInjector rlsSessionInjector,
            RlsProperties rlsProperties) {
        return new RlsSessionInterceptor(rlsSessionInjector, rlsProperties);
    }
}