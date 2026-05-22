package io.github.aayushghimirey.spring_postgres_rls.core;

 import java.util.List;

public class CoreRlsConfig {
    private final List<String> tables;
    private final ValidationMode validationMode;

    public CoreRlsConfig(List<String> tables, ValidationMode validationMode) {
         this.tables = tables;
         this.validationMode = validationMode;
    }

    public List<String> getTables() {
        return tables;
    }
    public ValidationMode getValidationMode() {
        return validationMode;
    }

}