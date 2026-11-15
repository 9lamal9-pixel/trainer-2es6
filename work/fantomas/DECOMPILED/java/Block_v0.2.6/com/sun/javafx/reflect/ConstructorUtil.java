/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.reflect;

import com.sun.javafx.reflect.ReflectUtil;
import java.lang.reflect.Constructor;

public final class ConstructorUtil {
    private ConstructorUtil() {
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>[] classArray) throws NoSuchMethodException {
        ReflectUtil.checkPackageAccess(clazz);
        return clazz.getConstructor(classArray);
    }

    public static Constructor<?>[] getConstructors(Class<?> clazz) {
        ReflectUtil.checkPackageAccess(clazz);
        return clazz.getConstructors();
    }
}

