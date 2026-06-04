package io.github.aayushghimirey.spring_postgres_rls.core;

import java.util.HashMap;
import java.util.Map;

public class RlsContextHolder {

    public static final String TENANT_ID_KEY = "app.tenant_id";
    public static final String USER_ID_KEY = "app.user_id";

    private static final ThreadLocal<Map<String, Object>> CONTEXT =
            ThreadLocal.withInitial(HashMap::new);

    public static void insert(String key, Object value) {
        CONTEXT.get().put(key, value);
    }

    public static Object get(String key) {
        return CONTEXT.get().get(key);
    }

    public static Map<String, Object> getAll() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public static void setTenantId(Object tenantId) {
        insert(TENANT_ID_KEY, tenantId);
    }

    public static Object getTenantId() {
        return get(TENANT_ID_KEY);
    }

    public static void setUserId(Object userId) {
        insert(USER_ID_KEY, userId);
    }

    public static Object getUserId() {
        return get(USER_ID_KEY);
    }
}