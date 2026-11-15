/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.reflect;

import com.sun.javafx.reflect.ReflectUtil;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.PrivilegedExceptionAction;
import java.security.SecureClassLoader;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public final class MethodUtil
extends SecureClassLoader {
    private static final String MISC_PKG = "com.sun.javafx.reflect.";
    private static final String TRAMPOLINE = "com.sun.javafx.reflect.Trampoline";
    private static final Method bounce = MethodUtil.getTrampoline();

    private MethodUtil() {
    }

    public static Method getMethod(Class<?> clazz, String string, Class<?>[] classArray) throws NoSuchMethodException {
        ReflectUtil.checkPackageAccess(clazz);
        return clazz.getMethod(string, classArray);
    }

    public static Method[] getMethods(Class<?> clazz) {
        ReflectUtil.checkPackageAccess(clazz);
        return clazz.getMethods();
    }

    static Method[] getPublicMethods(Class<?> clazz) {
        boolean bl;
        if (System.getSecurityManager() == null) {
            return clazz.getMethods();
        }
        HashMap<Signature, Method> hashMap = new HashMap<Signature, Method>();
        while (clazz != null && !(bl = MethodUtil.getInternalPublicMethods(clazz, hashMap))) {
            MethodUtil.getInterfaceMethods(clazz, hashMap);
            clazz = clazz.getSuperclass();
        }
        return hashMap.values().toArray(new Method[hashMap.size()]);
    }

    private static void getInterfaceMethods(Class<?> clazz, Map<Signature, Method> map) {
        Class<?>[] classArray = clazz.getInterfaces();
        for (int i = 0; i < classArray.length; ++i) {
            Class<?> clazz2 = classArray[i];
            boolean bl = MethodUtil.getInternalPublicMethods(clazz2, map);
            if (bl) continue;
            MethodUtil.getInterfaceMethods(clazz2, map);
        }
    }

    private static boolean getInternalPublicMethods(Class<?> clazz, Map<Signature, Method> map) {
        Class<?> clazz2;
        int n;
        Method[] methodArray = null;
        try {
            if (!Modifier.isPublic(clazz.getModifiers())) {
                return false;
            }
            if (!ReflectUtil.isPackageAccessible(clazz)) {
                return false;
            }
            methodArray = clazz.getMethods();
        }
        catch (SecurityException securityException) {
            return false;
        }
        boolean bl = true;
        for (n = 0; n < methodArray.length; ++n) {
            clazz2 = methodArray[n].getDeclaringClass();
            if (Modifier.isPublic(clazz2.getModifiers())) continue;
            bl = false;
            break;
        }
        if (bl) {
            for (n = 0; n < methodArray.length; ++n) {
                MethodUtil.addMethod(map, methodArray[n]);
            }
        } else {
            for (n = 0; n < methodArray.length; ++n) {
                clazz2 = methodArray[n].getDeclaringClass();
                if (!clazz.equals(clazz2)) continue;
                MethodUtil.addMethod(map, methodArray[n]);
            }
        }
        return bl;
    }

    private static void addMethod(Map<Signature, Method> map, Method method) {
        Method method2;
        Signature signature = new Signature(method);
        if (!map.containsKey(signature)) {
            map.put(signature, method);
        } else if (!method.getDeclaringClass().isInterface() && (method2 = map.get(signature)).getDeclaringClass().isInterface()) {
            map.put(signature, method);
        }
    }

    public static Module getTrampolineModule() {
        return bounce.getDeclaringClass().getModule();
    }

    public static Object invoke(Method method, Object object, Object[] objectArray) throws InvocationTargetException, IllegalAccessException {
        try {
            return bounce.invoke(null, method, object, objectArray);
        }
        catch (InvocationTargetException invocationTargetException) {
            Throwable throwable = invocationTargetException.getCause();
            if (throwable instanceof InvocationTargetException) {
                throw (InvocationTargetException)throwable;
            }
            if (throwable instanceof IllegalAccessException) {
                throw (IllegalAccessException)throwable;
            }
            if (throwable instanceof RuntimeException) {
                throw (RuntimeException)throwable;
            }
            if (throwable instanceof Error) {
                throw (Error)throwable;
            }
            throw new Error("Unexpected invocation error", throwable);
        }
        catch (IllegalAccessException illegalAccessException) {
            throw new Error("Unexpected invocation error", illegalAccessException);
        }
    }

    private static Method getTrampoline() {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Method>(){

                @Override
                public Method run() throws Exception {
                    Class clazz = MethodUtil.getTrampolineClass();
                    Class[] classArray = new Class[]{Method.class, Object.class, Object[].class};
                    Method method = clazz.getDeclaredMethod("invoke", classArray);
                    method.setAccessible(true);
                    return method;
                }
            });
        }
        catch (Exception exception) {
            throw new InternalError("bouncer cannot be found", exception);
        }
    }

    @Override
    protected synchronized Class<?> loadClass(String string, boolean bl) throws ClassNotFoundException {
        ReflectUtil.checkPackageAccess(string);
        Class<?> clazz = this.findLoadedClass(string);
        if (clazz == null) {
            try {
                clazz = this.findClass(string);
            }
            catch (ClassNotFoundException classNotFoundException) {
                // empty catch block
            }
            if (clazz == null) {
                clazz = this.getParent().loadClass(string);
            }
        }
        if (bl) {
            this.resolveClass(clazz);
        }
        return clazz;
    }

    @Override
    protected Class<?> findClass(String string) throws ClassNotFoundException {
        block9: {
            Class<?> clazz;
            block10: {
                if (!string.startsWith(MISC_PKG)) {
                    throw new ClassNotFoundException(string);
                }
                String string2 = string.replace('.', '/').concat(".class");
                InputStream inputStream = MethodUtil.class.getModule().getResourceAsStream(string2);
                if (inputStream == null) break block9;
                InputStream inputStream2 = inputStream;
                try {
                    byte[] byArray = inputStream.readAllBytes();
                    clazz = this.defineClass(string, byArray);
                    if (inputStream2 == null) break block10;
                }
                catch (Throwable throwable) {
                    try {
                        if (inputStream2 != null) {
                            try {
                                inputStream2.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    catch (IOException iOException) {
                        throw new ClassNotFoundException(string, iOException);
                    }
                }
                inputStream2.close();
            }
            return clazz;
        }
        throw new ClassNotFoundException(string);
    }

    private Class<?> defineClass(String string, byte[] byArray) throws IOException {
        CodeSource codeSource = new CodeSource(null, (Certificate[])null);
        if (!string.equals(TRAMPOLINE)) {
            throw new IOException("MethodUtil: bad name " + string);
        }
        return this.defineClass(string, byArray, 0, byArray.length, codeSource);
    }

    @Override
    protected PermissionCollection getPermissions(CodeSource codeSource) {
        PermissionCollection permissionCollection = super.getPermissions(codeSource);
        permissionCollection.add(new AllPermission());
        return permissionCollection;
    }

    private static Class<?> getTrampolineClass() {
        try {
            return Class.forName(TRAMPOLINE, true, new MethodUtil());
        }
        catch (ClassNotFoundException classNotFoundException) {
            return null;
        }
    }

    private static class Signature {
        private final String methodName;
        private final Class<?>[] argClasses;
        private final int hashCode;

        Signature(Method method) {
            this.methodName = method.getName();
            this.argClasses = method.getParameterTypes();
            this.hashCode = this.methodName.hashCode() + Arrays.hashCode(this.argClasses);
        }

        public int hashCode() {
            return this.hashCode;
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            Signature signature = (Signature)object;
            if (!this.methodName.equals(signature.methodName)) {
                return false;
            }
            if (this.argClasses.length != signature.argClasses.length) {
                return false;
            }
            for (int i = 0; i < this.argClasses.length; ++i) {
                if (this.argClasses[i] == signature.argClasses[i]) continue;
                return false;
            }
            return true;
        }
    }
}

