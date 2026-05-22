package io.github.aayushghimirey.spring_postgres_rls.impl;

import io.github.aayushghimirey.spring_postgres_rls.core.CoreRlsConfig;
import io.github.aayushghimirey.spring_postgres_rls.core.TableRlsValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

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
    public TableRlsValidator tableRlsValidator(
            DataSource dataSource,
            RlsProperties properties) {

        CoreRlsConfig coreRlsConfig = new CoreRlsConfig(
                properties.getTables(),
                properties.getValidationMode()
        );

        return new TableRlsValidator(dataSource, coreRlsConfig);
    }

    @Bean
    public ApplicationRunner rlsStartupValidator(TableRlsValidator validator) {
        log.debug("Registering RLS startup validation runner");
        return args -> {
            log.debug("Executing RLS validation logic...");
            validator.validate();
        };
    }
}
