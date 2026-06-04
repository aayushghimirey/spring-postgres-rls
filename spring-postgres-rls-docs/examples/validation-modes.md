# Validation Modes

There are three validation modes available out of the box when starting your Spring Boot application:

### `STRICT`
In STRICT mode, if any configured table is missing from PostgreSQL, lacks Row Level Security, or is missing a configured policy, a runtime exception is thrown and the application fails to start.

### `PERMISSIVE`
In PERMISSIVE mode, the application will simply log an error message to the console for any validation failures, but it will continue to start.

### `NONE`
Validation is entirely bypassed and disabled.
