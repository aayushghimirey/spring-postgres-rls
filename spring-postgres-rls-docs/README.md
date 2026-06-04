# Spring Postgres RLS (Row Level Security)

[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java Version](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/technologies/downloads/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.0-green.svg)](https://spring.io/projects/spring-boot)

A lightweight Spring Boot library that provides **Row Level Security (RLS)** integration with PostgreSQL. This library enables deep-layer data isolation (ideal for multi-tenant applications) and validates your database security configurations during application startup to prevent silent security vulnerabilities.

---

## 🌟 Key Features

1. **AOP-based Runtime RLS Injection (`@UseRls`)**: Automatically injects tenant or user context into the PostgreSQL session natively via `set_config` before your queries run, enforcing RLS at the database layer.
2. **Startup Validation (`TableRlsValidator`)**: Fails fast if your database schema drifts from your application security configuration. Ensures tables have RLS enabled and required policies exist.
3. **Safe Context Management**: The injected `ThreadLocal` context (`RlsContextHolder`) is safely and automatically cleared after method execution to prevent memory leaks in web servers.
4. **Transaction Aware**: Seamlessly integrates with Spring's `@Transactional` to ensure RLS context stays bound to the correct database connection.

---

## 🚀 How It Works

### 1. Runtime RLS Injection
When a method is annotated with `@UseRls` and `@Transactional`, the `RlsSessionInterceptor` intercepts the call, reads values from the `RlsContextHolder`, and executes `SELECT set_config('key', 'value', true)` on the active database connection. Your standard Spring Data JPA or JDBC queries then execute under that restricted context.

### 2. Startup Validation
During Spring Application Context initialization, an `ApplicationRunner` bean borrows a connection and queries `pg_class` and `pg_policies` to verify each configured table exists, has RLS enabled (`relrowsecurity = true`), and possesses the required policies.

---

## ⚙️ Configuration & Basic Usage

### 1. Configure properties in `application.yml`:

```yaml
spring:
  rls:
    # Activate/deactivate the validation runner
    enabled: true
    # Modes: STRICT, PERMISSIVE, NONE (Default: PERMISSIVE)
    validation-mode: STRICT
    # List of tables and required RLS policies to check at startup
    tables:
      - name: employees
        policies:
          - employee_tenant_isolation
```

### 2. Wrap Service Methods with `@UseRls`

```java
@Service
public class EmployeeService {

    @UseRls
    @Transactional
    public List<Employee> getEmployeesForCurrentTenant() {
        // The RLS policy applies automatically; no WHERE tenant_id = ? needed!
        return employeeRepository.findAll();
    }
}
```

### 3. Set Context Before Execution

```java
// E.g., Inside a Controller or Filter
RlsContextHolder.setTenantId(123L);
employeeService.getEmployeesForCurrentTenant();
// The interceptor automatically clears the context afterwards!
```

---

## 🚦 Validation Modes

### 1. `STRICT`
Halts application startup immediately if any check fails by throwing dedicated custom exceptions (`TableNotFoundException`, `RlsNotEnabledException`, `RlsPolicyNotFoundException`).

### 2. `PERMISSIVE`
Failing validation checks are logged at the `ERROR` level at startup, but the application continues to boot normally (Ideal for dev environments).

### 3. `NONE`
Bypasses startup validation completely.

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
