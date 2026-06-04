-- =========================
-- 1. ROLE
-- =========================
DO $$
BEGIN
   IF NOT EXISTS (
      SELECT 1 FROM pg_roles WHERE rolname = 'sample_user'
   ) THEN
CREATE ROLE sample_user LOGIN PASSWORD 'sample_password';
END IF;
END;
$$;

-- =========================
-- 2. DATABASE + SCHEMA PRIVILEGES
-- =========================
GRANT CONNECT ON DATABASE sample_db TO sample_user;
GRANT USAGE ON SCHEMA public TO sample_user;

-- =========================
-- 3. TABLE (IMPORTANT: DROP SAFE RESET FOR DEV)
-- =========================
DROP TABLE IF EXISTS employees;

CREATE TABLE employees (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(255) NOT NULL,
                           tenant_id BIGINT NOT NULL
);

-- =========================
-- 4. GRANTS (IMPORTANT FIX)
-- =========================
GRANT SELECT, INSERT, UPDATE, DELETE
      ON ALL TABLES IN SCHEMA public
          TO sample_user;

-- =========================
-- 5. ROW LEVEL SECURITY
-- =========================
ALTER TABLE employees ENABLE ROW LEVEL SECURITY;

CREATE POLICY employee_tenant_isolation
ON employees
FOR ALL
USING (
    tenant_id = COALESCE(NULLIF(current_setting('app.tenant_id', true), ''), '0')::BIGINT
)
WITH CHECK (
    tenant_id = COALESCE(NULLIF(current_setting('app.tenant_id', true), ''), '0')::BIGINT
);

-- =========================
-- 6. DATA
-- =========================
INSERT INTO employees (name, tenant_id) VALUES
                                            ('Alice', 1),
                                            ('Bob', 2),
                                            ('Charlie', 1),
                                            ('David', 2);