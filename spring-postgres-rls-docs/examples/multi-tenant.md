# Multi-Tenant Design Architecture

This module implements Row Level Security (RLS) primarily aimed at Multi-Tenant architectures.

Instead of writing a `WHERE tenant_id = ?` clause on every query (which is error-prone and can easily leak data), PostgreSQL's RLS is used to mathematically ensure isolation at the lowest layer of the database.

When a query is executed from Spring:
1. `RlsSessionInterceptor` wraps the `@UseRls` method.
2. It obtains a transaction-bound connection.
3. It performs a `set_config('app.tenant_id', '123', true)`.
4. Your queries execute normally.
5. PostgreSQL enforces the configured policy on `employees`, stripping out any rows that do not match `tenant_id = 123`.

This pattern ensures deep-layer data isolation.
