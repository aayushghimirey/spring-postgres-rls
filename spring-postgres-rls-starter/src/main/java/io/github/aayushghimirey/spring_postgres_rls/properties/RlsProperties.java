package io.github.aayushghimirey.spring_postgres_rls.properties;

import io.github.aayushghimirey.spring_postgres_rls.core.ValidationMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "spring.rls")
public class RlsProperties {

    private boolean enabled = true;
    private ValidationMode validationMode = ValidationMode.PERMISSIVE;

    public List<TableConfig> getTables() {
        return tables;
    }

    public void setTables(List<TableConfig> tables) {
        this.tables = tables;
    }

    public ValidationMode getValidationMode() {
        return validationMode;
    }

    public void setValidationMode(ValidationMode validationMode) {
        this.validationMode = validationMode;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private List<TableConfig> tables = new ArrayList<>();

    @Override
    public String toString() {
        return "RlsProperties{" +
                "enabled=" + enabled +
                ", validationMode=" + validationMode +
                ", tables=" + tables +
                '}';
    }

    public static class TableConfig {

        private String name;
        private List<String> policies;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getPolicies() {
            return policies;
        }

        public void setPolicies(List<String> policies) {
            this.policies = policies;
        }
    }
}
