/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;

class Trampoline {
    Trampoline() {
    }

    private static void ensureInvocableMethod(Method method) throws InvocationTargetException {
        Class<?> clazz = method.getDeclaringClass();
        if (clazz.equals(AccessController.class) || clazz.equals(Method.class) || clazz.getName().startsWith("java.lang.invoke.")) {
            throw new InvocationTargetException(new UnsupportedOperationException("invocation not supported"));
        }
    }

    private static Object invoke(Method method, Object object, Object[] objectArray) throws InvocationTargetException, IllegalAccessException {
        Trampoline.ensureInvocableMethod(method);
        return method.invoke(object, objectArray);
    }

    static {
        if (Trampoline.class.getClassLoader() == null) {
            throw new Error("Trampoline must not be defined by the bootstrap classloader");
        }
        if (Trampoline.class.getClassLoader() == ClassLoader.getPlatformClassLoader()) {
            throw new Error("Trampoline must not be defined by the platform classloader");
        }
    }
}

