# Spring Postgres RLS (Row Level Security) Validator

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/downloads/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.0-green.svg)](https://spring.io/projects/spring-boot)

A lightweight Spring Boot library that validates PostgreSQL **Row Level Security (RLS)** configurations during application startup. Prevent silent security vulnerabilities by ensuring tables and RLS policies defined in your application configuration match the active database schema in PostgreSQL.

---

## 🌟 Why Use This?

PostgreSQL's Row Level Security is an incredibly powerful feature for multi-tenant and secure architectures. However, it fails silently:
* If a table is created but RLS is never explicitly enabled (`ALTER TABLE ... ENABLE ROW LEVEL SECURITY`), queries execute without RLS enforcement.
* If a critical security policy (like `tenant_policy`) is accidentally dropped or missing, PostgreSQL defaults to a "default-deny" state or runs without the policy.

This library acts as a **startup-time gatekeeper**, checking your database catalog at boot to ensure your RLS rules are perfectly aligned before the application begins accepting requests.

---

## 🚀 How It Works

During the Spring Application Context initialization, an `ApplicationRunner` bean runs `TableRlsValidator` which:
1. **Dynamic Connection Handling**: Dynamically borrows a connection from your active `DataSource` and returns it immediately using `try-with-resources`. No long-running connections are held.
2. **Table Check**: Queries `pg_class` to verify each configured table exists and has RLS enabled (`relrowsecurity = true`).
3. **Policy Check**: Queries `pg_policies` to verify that every user-specified policy exists on the respective table.
4. **Validation Policy Enforcement**: Based on the `ValidationMode`, it will either throw a dedicated exception or log error outputs.

---

## ⚙️ Configuration

Configure the RLS validation definitions in your `application.yml` or `application.properties`:

### Example `application.yml`:

```yaml
spring:
  rls:
    # Activate/deactivate the validation runner
    enabled: true
    # Modes: STRICT, PERMISSIVE, NONE (Default: PERMISSIVE)
    validation-mode: strict
    # List of tables and required RLS policies to check
    tables:
      - name: tenants
        policies:
          - tenant_policy
      - name: documents
        policies:
          - owner_policy
          - shared_policy
      - name: employees
        # Validates table exists and RLS is enabled, without checking specific policies
```

### Configuration Properties:

| Property | Type | Default | Description |
| :--- | :--- | :--- | :--- |
| `spring.rls.enabled` | `boolean` | `true` | When `true`, enables RLS validation at startup. |
| `spring.rls.validation-mode` | `enum` | `PERMISSIVE` | Severity strategy: `STRICT`, `PERMISSIVE`, or `NONE`. |
| `spring.rls.tables` | `List` | `[]` | List of target tables to validate. |
| `spring.rls.tables[].name` | `String` | - | Table name to verify. |
| `spring.rls.tables[].policies` | `List<String>` | `[]` | List of specific policy names that must exist on this table. |

---

## 🚦 Validation Modes

### 1. `STRICT`
Halts application startup immediately if any check fails by throwing dedicated custom exceptions:
* **Table Missing**: Throws `TableNotFoundException` (e.g. `Table tenants not found`).
* **RLS Disabled**: Throws `RlsNotEnabledException` (e.g. `RLS not enabled for employees`).
* **Policy Missing**: Throws `RlsPolicyNotFoundException` (e.g. `Policy 'tenant_policy' missing for table tenants`).

### 2. `PERMISSIVE`
Ideal for development environments. Failing validation checks are logged at the `ERROR` level at startup, but the application continues to boot normally.

### 3. `NONE`
Bypasses validation completely. Ideal for running lightweight local test suites or offline environments.

---

## 🛠️ Testing

The project is fully integrated with **Testcontainers** to test RLS validations against a real, running PostgreSQL container instance:

To run the test suite locally:
```bash
mvn clean test
```

*Note: Make sure your local Docker environment is running, as Testcontainers requires it to boot up the Postgres instance.*

---

## 📄 License

Licensed under the Apache License, Version 2.0. See the [LICENSE](LICENSE) file for more information.
