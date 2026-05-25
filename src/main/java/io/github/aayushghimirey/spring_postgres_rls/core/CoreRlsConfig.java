package io.github.aayushghimirey.spring_postgres_rls.core;


 import java.util.List;

public class CoreRlsConfig {
    private  ValidationMode validationMode;
    private  List<CoreTableConfig> tables;


    public CoreRlsConfig(List<CoreTableConfig> tables, ValidationMode validationMode) {
        this.tables = tables;
        this.validationMode = validationMode;
    }
    public CoreRlsConfig() {}

    public void setTables(List<CoreTableConfig> tables) {
        this.tables = tables;
    }

    public void setValidationMode(ValidationMode validationMode) {
        this.validationMode = validationMode;
    }

    public ValidationMode getValidationMode() {
        return validationMode;
    }

    public List<CoreTableConfig> getTables() {
        return tables;
    }

    public static class CoreTableConfig  {
        private String name;
        private List<String> policies;

        public CoreTableConfig(String name, List<String> policies) {
            this.name = name;
            this.policies = policies;
        }

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