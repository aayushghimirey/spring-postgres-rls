package io.github.aayushghimirey.spring_postgres_rls.autoconfigure;

import io.github.aayushghimirey.spring_postgres_rls.aop.RlsSessionInterceptor;
import io.github.aayushghimirey.spring_postgres_rls.core.CoreRlsConfig;
import io.github.aayushghimirey.spring_postgres_rls.core.RlsSessionInjector;
import io.github.aayushghimirey.spring_postgres_rls.core.TableRlsValidator;
import io.github.aayushghimirey.spring_postgres_rls.core.impl.DefaultRlsSessionInjector;
import io.github.aayushghimirey.spring_postgres_rls.core.impl.DefaultRlsValidatorImpl;
import io.github.aayushghimirey.spring_postgres_rls.properties.RlsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@ConditionalOnProperty(
        prefix = "spring.rls",
        name = "enabled",
        havingValue = "true"
)
@EnableConfigurationProperties(RlsProperties.class)
public class RlsAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(RlsAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public TableRlsValidator tableRlsValidator(
            DataSource dataSource,
            RlsProperties properties) {

        log.trace("Creating TableRlsValidator bean with properties: {}", properties);

        List<CoreRlsConfig.CoreTableConfig> tableConfigs = properties.getTables().stream()
                .map(tableConfig -> new CoreRlsConfig.CoreTableConfig(tableConfig.getName(), tableConfig.getPolicies()))
                .collect(Collectors.toList());

        CoreRlsConfig coreRlsConfig = new CoreRlsConfig(
                tableConfigs,
                properties.getValidationMode()
        );

        return new DefaultRlsValidatorImpl(dataSource, coreRlsConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    public RlsSessionInjector rlsSessionInjector(DataSource dataSource) {
        log.trace("Creating RlsSessionInjector bean");
        return new DefaultRlsSessionInjector(dataSource);
    }

    @Bean
    @ConditionalOnMissingBean
    public RlsSessionInterceptor rlsSessionInterceptor(
            RlsSessionInjector rlsSessionInjector,
            RlsProperties rlsProperties) {
        log.trace("Creating RlsSessionInterceptor bean");
        return new RlsSessionInterceptor(rlsSessionInjector, rlsProperties);
    }

    @Bean
    public ApplicationRunner rlsStartupValidator(TableRlsValidator validator) {
        log.debug("Registering RLS startup validation runner");
        return args -> {
             validator.validate();
        };
    }
}
