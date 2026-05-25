# Contributing to Spring Postgres RLS Validator

First off, thank you for taking the time to contribute! 🎉 

This project aims to prevent silent security misconfigurations in PostgreSQL Row Level Security (RLS) within Spring Boot applications. By contributing, you are helping build a more secure, robust developer ecosystem.

Here are the guidelines and best practices to keep in mind when working on the project:

---

## 🛠️ 1. Local Development Setup

To work on this library locally, ensure your machine has the following tools installed:

* **Java**: JDK 21
* **Maven**: 3.8+
* **Docker / Docker Desktop**: Required by Testcontainers to spin up a live PostgreSQL instance for test runs.

### Step-by-Step Setup

1. **Fork and Clone** the repository:
   ```bash
   git clone https://github.com/your-username/spring-postgres-rls.git
   cd spring-postgres-rls
   ```

2. **Run the Test Suite** to verify your setup:
   ```bash
   mvn clean test
   ```
   *Note: On the first test execution, Testcontainers will automatically download the required `postgres:17` and `testcontainers/ryuk` images. Ensure your Docker daemon is active before running this command.*

---

## 📐 2. Development Guidelines

When introducing changes, please keep the following design goals and architecture practices in mind:

### Connection & Lifecycle Safety
* **Dynamic Connection Acquisition**: Never open database connections or perform I/O operations inside Spring `@Bean` factory methods (e.g. inside `RlsAutoConfiguration`). 
* **Auto-Closing Connections**: Always retrieve connections from the `DataSource` dynamically inside target runners/validators using a `try-with-resources` block to ensure they are immediately returned to the connection pool.

### Scope Control ("Not Anything Extra")
* Only validate the tables and policies explicitly defined in the user's `spring.rls` configuration.
* Avoid querying or asserting schemas or tables outside of the user-provided config.

### Custom Exception Hierarchy
If you add new validations, define specific, descriptive runtime exceptions under the `exception` package:
* Use `TableNotFoundException` for table existence failures.
* Use `RlsNotEnabledException` for disabled RLS.
* Use `RlsPolicyNotFoundException` for missing policies.

---

## 🧪 3. Testing Requirements

* **Integration Tests**: Any new validation logic or bug fix must be covered by comprehensive integration tests inside [TableRlsValidatorTest](file:///Users/aayushghimire/projects/spring-postgres-rls/src/test/java/io/github/aayushghimirey/spring_postgres_rls/core/TableRlsValidatorTest.java).
* **Verify All Modes**: Ensure changes are tested across all three validation modes: `STRICT` (assert exceptions are thrown), `PERMISSIVE` (assert errors are logged but no exceptions are thrown), and `NONE` (assert checks are bypassed).

---

## 🚀 4. Submitting a Pull Request

1. **Branch Naming**: Create a clear branch name describing your work (e.g., `feature/support-schema-prefix` or `bugfix/connection-leak`).
2. **Write Clean Code**: Follow standard Java style guidelines. Remove unused imports and formatting artifacts.
3. **Commit Messages**: Write meaningful, imperative commit messages (e.g., `Add RlsPolicyNotFoundException validation`).
4. **Open a PR**: Open a pull request against the `main` branch. Provide a brief explanation of the problem you are solving and how you tested the changes.

---

Thank you for contributing! Your help in making Spring Boot applications secure is highly appreciated.
