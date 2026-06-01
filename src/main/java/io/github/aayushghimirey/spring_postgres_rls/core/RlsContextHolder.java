package io.github.aayushghimirey.spring_postgres_rls.core;

import java.util.HashMap;
import java.util.Map;

public class RlsContextHolder {

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
}