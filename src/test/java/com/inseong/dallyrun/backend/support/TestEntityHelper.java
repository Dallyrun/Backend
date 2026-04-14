package com.inseong.dallyrun.backend.support;

import java.lang.reflect.Field;

public final class TestEntityHelper {

    private TestEntityHelper() {
    }

    public static void setId(Object entity, Long id) {
        try {
            Field field = findField(entity.getClass(), "id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set id on " + entity.getClass().getSimpleName(), e);
        }
    }

    private static Field findField(Class<?> clazz, String fieldName) {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new RuntimeException("Field '" + fieldName + "' not found");
    }
}
