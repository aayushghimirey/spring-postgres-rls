package io.github.aayushghimirey.spring_postgres_rls.impl;

 import io.github.aayushghimirey.spring_postgres_rls.core.ValidationMode;
 import org.springframework.boot.context.properties.ConfigurationProperties;

 import java.util.ArrayList;
 import java.util.List;

@ConfigurationProperties(prefix = "spring.rls")
public class RlsProperties {

    private boolean enabled = true;

    private List<String> tables =
            new ArrayList<>();

    private ValidationMode validationMode = ValidationMode.PERMISSIVE;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public List<String> getTables() {
        return tables;
    }

    public void setTables(List<String> tables) {
        this.tables = tables;
    }

    public ValidationMode getValidationMode() {
        return validationMode;
    }

    public void setValidationMode(ValidationMode validationMode) {
        this.validationMode = validationMode;
    }
}