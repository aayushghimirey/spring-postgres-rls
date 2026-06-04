package io.github.aayushghimirey.spring_postgres_rls.dummy;

import io.github.aayushghimirey.spring_postgres_rls.core.RlsContextHolder;
import io.github.aayushghimirey.spring_postgres_rls.core.RlsSessionInjector;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
 @SpringBootTest(classes = TestConfig.class)
class TestApplication {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

     @MockBean
     RlsSessionInjector rlsSessionInjector;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    TestService testService;

    @Test
    void testUseRlsInterceptorInjectsSessionAndClearsContext() {
        RlsContextHolder.setTenantId(99L);
        Assertions.assertFalse(RlsContextHolder.getAll().isEmpty());

        testService.testInjection();

        Mockito.verify(rlsSessionInjector).inject();
        Assertions.assertTrue(RlsContextHolder.getAll().isEmpty(), "Context should be cleared after execution");
     }
}