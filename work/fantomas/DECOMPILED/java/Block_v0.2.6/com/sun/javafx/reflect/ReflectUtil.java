/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.reflect;

import java.lang.reflect.Proxy;

public final class ReflectUtil {
    public static final String PROXY_PACKAGE = "com.sun.proxy";

    private ReflectUtil() {
    }

    public static void checkPackageAccess(Class<?> clazz) {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            ReflectUtil.privateCheckPackageAccess(securityManager, clazz);
        }
    }

    private static void privateCheckPackageAccess(SecurityManager securityManager, Class<?> clazz) {
        while (clazz.isArray()) {
            clazz = clazz.getComponentType();
        }
        String string = clazz.getPackageName();
        if (string != null && !string.isEmpty()) {
            securityManager.checkPackageAccess(string);
        }
        if (ReflectUtil.isNonPublicProxyClass(clazz)) {
            ReflectUtil.privateCheckProxyPackageAccess(securityManager, clazz);
        }
    }

    public static void checkPackageAccess(String string) {
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            int n;
            String string2 = string.replace('/', '.');
            if (string2.startsWith("[") && (n = string2.lastIndexOf(91) + 2) > 1 && n < string2.length()) {
                string2 = string2.substring(n);
            }
            if ((n = string2.lastIndexOf(46)) != -1) {
                securityManager.checkPackageAccess(string2.substring(0, n));
            }
        }
    }

    public static boolean isPackageAccessible(Class<?> clazz) {
        try {
            ReflectUtil.checkPackageAccess(clazz);
        }
        catch (SecurityException securityException) {
            return false;
        }
        return true;
    }

    private static void privateCheckProxyPackageAccess(SecurityManager securityManager, Class<?> clazz) {
        if (Proxy.isProxyClass(clazz)) {
            for (Class<?> clazz2 : clazz.getInterfaces()) {
                ReflectUtil.privateCheckPackageAccess(securityManager, clazz2);
            }
        }
    }

    public static boolean isNonPublicProxyClass(Class<?> clazz) {
        if (!Proxy.isProxyClass(clazz)) {
            return false;
        }
        String string = clazz.getPackageName();
        return string == null || !string.startsWith(PROXY_PACKAGE);
    }
}

