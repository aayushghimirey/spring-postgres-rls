package io.github.aayushghimirey.spring_postgres_rls.core;


 import java.util.List;

public record CoreRlsConfig(
        List<CoreTableConfig> tables,
        ValidationMode validationMode
) {
    public record CoreTableConfig(
            String name,
            List<String> policies
    ) {
    }
}