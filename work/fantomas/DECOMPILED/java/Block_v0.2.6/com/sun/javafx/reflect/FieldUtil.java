/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.reflect;

import com.sun.javafx.reflect.ReflectUtil;
import java.lang.reflect.Field;

public final class FieldUtil {
    private FieldUtil() {
    }

    public static Field getField(Class<?> clazz, String string) throws NoSuchFieldException {
        ReflectUtil.checkPackageAccess(clazz);
        return clazz.getField(string);
    }
}

