/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.property;

import com.sun.javafx.reflect.MethodUtil;
import com.sun.javafx.reflect.ReflectUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;

public class MethodHelper {
    private static final boolean logAccessErrors = AccessController.doPrivileged(() -> Boolean.getBoolean("sun.reflect.debugModuleAccessChecks"));
    private static final Module trampolineModule = MethodUtil.getTrampolineModule();

    public static Object invoke(Method method, Object object, Object[] objectArray) throws InvocationTargetException, IllegalAccessException {
        Class<?> clazz = method.getDeclaringClass();
        String string = clazz.getPackage().getName();
        Module module = clazz.getModule();
        Module module2 = MethodHelper.class.getModule();
        try {
            if (!module.isExported(string)) {
                if (!module.isOpen(string, module2)) {
                    throw new IllegalAccessException("module " + module2.getName() + " cannot access class " + clazz.getName() + " (in module " + module.getName() + ") because module " + module.getName() + " does not open " + string + " to " + module2.getName());
                }
                if (!module.isOpen(string, trampolineModule)) {
                    ReflectUtil.checkPackageAccess(string);
                    module.addOpens(string, trampolineModule);
                }
            }
        }
        catch (IllegalAccessException illegalAccessException) {
            if (logAccessErrors) {
                illegalAccessException.printStackTrace(System.err);
            }
            throw illegalAccessException;
        }
        return MethodUtil.invoke(method, object, objectArray);
    }

    private MethodHelper() {
    }
}

