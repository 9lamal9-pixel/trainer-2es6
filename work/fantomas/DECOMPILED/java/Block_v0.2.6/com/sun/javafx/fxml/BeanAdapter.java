/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.fxml;

import com.sun.javafx.fxml.ModuleHelper;
import com.sun.javafx.fxml.PropertyNotFoundException;
import com.sun.javafx.reflect.FieldUtil;
import com.sun.javafx.reflect.MethodUtil;
import com.sun.javafx.reflect.ReflectUtil;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.beans.value.ObservableValue;

public class BeanAdapter
extends AbstractMap<String, Object> {
    private final Object bean;
    private static final HashMap<Class<?>, MethodCache> globalMethodCache = new HashMap();
    private final MethodCache localCache;
    public static final String GET_PREFIX = "get";
    public static final String IS_PREFIX = "is";
    public static final String SET_PREFIX = "set";
    public static final String PROPERTY_SUFFIX = "Property";
    public static final String VALUE_OF_METHOD_NAME = "valueOf";

    public BeanAdapter(Object object) {
        this.bean = object;
        this.localCache = BeanAdapter.getClassMethodCache(object.getClass());
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static MethodCache getClassMethodCache(final Class<?> clazz) {
        if (clazz == Object.class) {
            return null;
        }
        HashMap<Class<?>, MethodCache> hashMap = globalMethodCache;
        synchronized (hashMap) {
            Object object;
            MethodCache methodCache = globalMethodCache.get(clazz);
            if (methodCache != null) {
                return methodCache;
            }
            HashMap<String, ArrayList<Method>> hashMap2 = new HashMap<String, ArrayList<Method>>();
            ReflectUtil.checkPackageAccess(clazz);
            if (Modifier.isPublic(clazz.getModifiers())) {
                object = AccessController.doPrivileged(new PrivilegedAction<Method[]>(){

                    @Override
                    public Method[] run() {
                        return clazz.getDeclaredMethods();
                    }
                });
                for (int i = 0; i < ((Method[])object).length; ++i) {
                    Method method = object[i];
                    int n = method.getModifiers();
                    if (!Modifier.isPublic(n) || Modifier.isStatic(n)) continue;
                    String string = method.getName();
                    ArrayList<Method> arrayList = (ArrayList<Method>)hashMap2.get(string);
                    if (arrayList == null) {
                        arrayList = new ArrayList<Method>();
                        hashMap2.put(string, arrayList);
                    }
                    arrayList.add(method);
                }
            }
            object = new MethodCache(hashMap2, BeanAdapter.getClassMethodCache(clazz.getSuperclass()));
            globalMethodCache.put(clazz, (MethodCache)object);
            return object;
        }
    }

    public Object getBean() {
        return this.bean;
    }

    private Method getGetterMethod(String string) {
        Method method = this.localCache.getMethod(BeanAdapter.getMethodName(GET_PREFIX, string), new Class[0]);
        if (method == null) {
            method = this.localCache.getMethod(BeanAdapter.getMethodName(IS_PREFIX, string), new Class[0]);
        }
        return method;
    }

    private Method getSetterMethod(String string) {
        Class<?> clazz = this.getType(string);
        if (clazz == null) {
            throw new UnsupportedOperationException("Cannot determine type for property.");
        }
        return this.localCache.getMethod(BeanAdapter.getMethodName(SET_PREFIX, string), new Class[]{clazz});
    }

    private static String getMethodName(String string, String string2) {
        return string + Character.toUpperCase(string2.charAt(0)) + string2.substring(1);
    }

    @Override
    public Object get(Object object) {
        if (object == null) {
            throw new NullPointerException();
        }
        return this.get(object.toString());
    }

    private Object get(String string) {
        Object object;
        Method method;
        Method method2 = method = string.endsWith(PROPERTY_SUFFIX) ? this.localCache.getMethod(string, new Class[0]) : this.getGetterMethod(string);
        if (method != null) {
            try {
                object = ModuleHelper.invoke(method, this.bean, null);
            }
            catch (IllegalAccessException illegalAccessException) {
                throw new RuntimeException(illegalAccessException);
            }
            catch (InvocationTargetException invocationTargetException) {
                throw new RuntimeException(invocationTargetException);
            }
        } else {
            object = null;
        }
        return object;
    }

    @Override
    public Object put(String string, Object object) {
        if (string == null) {
            throw new NullPointerException();
        }
        Method method = this.getSetterMethod(string);
        if (method == null) {
            throw new PropertyNotFoundException("Property \"" + string + "\" does not exist or is read-only.");
        }
        try {
            ModuleHelper.invoke(method, this.bean, new Object[]{BeanAdapter.coerce(object, this.getType(string))});
        }
        catch (IllegalAccessException illegalAccessException) {
            throw new RuntimeException(illegalAccessException);
        }
        catch (InvocationTargetException invocationTargetException) {
            throw new RuntimeException(invocationTargetException);
        }
        return null;
    }

    @Override
    public boolean containsKey(Object object) {
        if (object == null) {
            throw new NullPointerException();
        }
        return this.getType(object.toString()) != null;
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException();
    }

    public boolean isReadOnly(String string) {
        if (string == null) {
            throw new NullPointerException();
        }
        return this.getSetterMethod(string) == null;
    }

    public <T> ObservableValue<T> getPropertyModel(String string) {
        if (string == null) {
            throw new NullPointerException();
        }
        return (ObservableValue)this.get(string + PROPERTY_SUFFIX);
    }

    public Class<?> getType(String string) {
        if (string == null) {
            throw new NullPointerException();
        }
        Method method = this.getGetterMethod(string);
        return method == null ? null : method.getReturnType();
    }

    public Type getGenericType(String string) {
        if (string == null) {
            throw new NullPointerException();
        }
        Method method = this.getGetterMethod(string);
        return method == null ? null : method.getGenericReturnType();
    }

    @Override
    public boolean equals(Object object) {
        boolean bl = false;
        if (object instanceof BeanAdapter) {
            BeanAdapter beanAdapter = (BeanAdapter)object;
            bl = this.bean == beanAdapter.bean;
        }
        return bl;
    }

    @Override
    public int hashCode() {
        return this.bean == null ? -1 : this.bean.hashCode();
    }

    public static <T> T coerce(Object object, Class<? extends T> clazz) {
        if (clazz == null) {
            throw new NullPointerException();
        }
        Object object2 = null;
        if (object == null) {
            object2 = null;
        } else if (clazz.isAssignableFrom(object.getClass())) {
            object2 = object;
        } else if (clazz == Boolean.class || clazz == Boolean.TYPE) {
            object2 = Boolean.valueOf(object.toString());
        } else if (clazz == Character.class || clazz == Character.TYPE) {
            object2 = Character.valueOf(object.toString().charAt(0));
        } else if (clazz == Byte.class || clazz == Byte.TYPE) {
            object2 = object instanceof Number ? Byte.valueOf(((Number)object).byteValue()) : Byte.valueOf(object.toString());
        } else if (clazz == Short.class || clazz == Short.TYPE) {
            object2 = object instanceof Number ? Short.valueOf(((Number)object).shortValue()) : Short.valueOf(object.toString());
        } else if (clazz == Integer.class || clazz == Integer.TYPE) {
            object2 = object instanceof Number ? Integer.valueOf(((Number)object).intValue()) : Integer.valueOf(object.toString());
        } else if (clazz == Long.class || clazz == Long.TYPE) {
            object2 = object instanceof Number ? Long.valueOf(((Number)object).longValue()) : Long.valueOf(object.toString());
        } else if (clazz == BigInteger.class) {
            object2 = object instanceof Number ? BigInteger.valueOf(((Number)object).longValue()) : new BigInteger(object.toString());
        } else if (clazz == Float.class || clazz == Float.TYPE) {
            object2 = object instanceof Number ? Float.valueOf(((Number)object).floatValue()) : Float.valueOf(object.toString());
        } else if (clazz == Double.class || clazz == Double.TYPE) {
            object2 = object instanceof Number ? Double.valueOf(((Number)object).doubleValue()) : Double.valueOf(object.toString());
        } else if (clazz == Number.class) {
            String string = object.toString();
            object2 = string.contains(".") ? (Number)Double.valueOf(string) : (Number)Long.valueOf(string);
        } else if (clazz == BigDecimal.class) {
            object2 = object instanceof Number ? BigDecimal.valueOf(((Number)object).doubleValue()) : new BigDecimal(object.toString());
        } else {
            if (clazz == Class.class) {
                try {
                    String string = object.toString();
                    ReflectUtil.checkPackageAccess(string);
                    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                    object2 = Class.forName(string, false, classLoader);
                }
                catch (ClassNotFoundException classNotFoundException) {
                    throw new IllegalArgumentException(classNotFoundException);
                }
            }
            Class<?> clazz2 = object.getClass();
            Method method = null;
            while (method == null && clazz2 != null) {
                try {
                    ReflectUtil.checkPackageAccess(clazz);
                    method = clazz.getDeclaredMethod(VALUE_OF_METHOD_NAME, clazz2);
                }
                catch (NoSuchMethodException noSuchMethodException) {
                    // empty catch block
                }
                if (method != null) continue;
                clazz2 = clazz2.getSuperclass();
            }
            if (method == null) {
                throw new IllegalArgumentException("Unable to coerce " + object + " to " + clazz + ".");
            }
            if (clazz.isEnum() && object instanceof String && Character.isLowerCase(((String)object).charAt(0))) {
                object = BeanAdapter.toAllCaps((String)object);
            }
            try {
                object2 = ModuleHelper.invoke(method, null, new Object[]{object});
            }
            catch (IllegalAccessException illegalAccessException) {
                throw new RuntimeException(illegalAccessException);
            }
            catch (InvocationTargetException invocationTargetException) {
                throw new RuntimeException(invocationTargetException);
            }
            catch (SecurityException securityException) {
                throw new RuntimeException(securityException);
            }
        }
        return (T)object2;
    }

    public static <T> T get(Object object, Class<?> clazz, String string) {
        Object object2 = null;
        Class<?> clazz2 = object.getClass();
        Method method = BeanAdapter.getStaticGetterMethod(clazz, string, clazz2);
        if (method != null) {
            try {
                object2 = ModuleHelper.invoke(method, null, new Object[]{object});
            }
            catch (InvocationTargetException invocationTargetException) {
                throw new RuntimeException(invocationTargetException);
            }
            catch (IllegalAccessException illegalAccessException) {
                throw new RuntimeException(illegalAccessException);
            }
        }
        return (T)object2;
    }

    public static void put(Object object, Class<?> clazz, String string, Object object2) {
        Class<?> clazz2;
        Class<?> clazz3 = object.getClass();
        Method method = null;
        if (object2 != null) {
            method = BeanAdapter.getStaticSetterMethod(clazz, string, object2.getClass(), clazz3);
        }
        if (method == null && (clazz2 = BeanAdapter.getType(clazz, string, clazz3)) != null) {
            method = BeanAdapter.getStaticSetterMethod(clazz, string, clazz2, clazz3);
            object2 = BeanAdapter.coerce(object2, clazz2);
        }
        if (method == null) {
            throw new PropertyNotFoundException("Static property \"" + string + "\" does not exist or is read-only.");
        }
        try {
            ModuleHelper.invoke(method, null, new Object[]{object, object2});
        }
        catch (InvocationTargetException invocationTargetException) {
            throw new RuntimeException(invocationTargetException);
        }
        catch (IllegalAccessException illegalAccessException) {
            throw new RuntimeException(illegalAccessException);
        }
    }

    public static boolean isDefined(Class<?> clazz, String string, Class<?> clazz2) {
        return BeanAdapter.getStaticGetterMethod(clazz, string, clazz2) != null;
    }

    public static Class<?> getType(Class<?> clazz, String string, Class<?> clazz2) {
        Method method = BeanAdapter.getStaticGetterMethod(clazz, string, clazz2);
        return method == null ? null : method.getReturnType();
    }

    public static Type getGenericType(Class<?> clazz, String string, Class<?> clazz2) {
        Method method = BeanAdapter.getStaticGetterMethod(clazz, string, clazz2);
        return method == null ? null : method.getGenericReturnType();
    }

    public static Class<?> getListItemType(Type type) {
        Type type2 = BeanAdapter.getGenericListItemType(type);
        if (type2 instanceof ParameterizedType) {
            type2 = ((ParameterizedType)type2).getRawType();
        }
        return (Class)type2;
    }

    public static Class<?> getMapValueType(Type type) {
        Type type2 = BeanAdapter.getGenericMapValueType(type);
        if (type2 instanceof ParameterizedType) {
            type2 = ((ParameterizedType)type2).getRawType();
        }
        return (Class)type2;
    }

    public static Type getGenericListItemType(Type type) {
        Object object = null;
        Type type2 = type;
        while (type2 != null) {
            Object object2;
            Type type3;
            if (type2 instanceof ParameterizedType) {
                type3 = (ParameterizedType)type2;
                object2 = (Class)type3.getRawType();
                if (!List.class.isAssignableFrom((Class<?>)object2)) break;
                object = type3.getActualTypeArguments()[0];
                break;
            }
            type3 = (Class)type2;
            object2 = ((Class)type3).getGenericInterfaces();
            for (int i = 0; i < ((Type[])object2).length; ++i) {
                ParameterizedType parameterizedType;
                Class clazz;
                Type type4 = object2[i];
                if (!(type4 instanceof ParameterizedType) || !List.class.isAssignableFrom(clazz = (Class)(parameterizedType = (ParameterizedType)type4).getRawType())) continue;
                object = parameterizedType.getActualTypeArguments()[0];
                break;
            }
            if (object != null) break;
            type2 = ((Class)type3).getGenericSuperclass();
        }
        if (object != null && object instanceof TypeVariable) {
            object = Object.class;
        }
        return object;
    }

    public static Type getGenericMapValueType(Type type) {
        Object object = null;
        Type type2 = type;
        while (type2 != null) {
            Object object2;
            Type type3;
            if (type2 instanceof ParameterizedType) {
                type3 = (ParameterizedType)type2;
                object2 = (Class)type3.getRawType();
                if (!Map.class.isAssignableFrom((Class<?>)object2)) break;
                object = type3.getActualTypeArguments()[1];
                break;
            }
            type3 = (Class)type2;
            object2 = ((Class)type3).getGenericInterfaces();
            for (int i = 0; i < ((Type[])object2).length; ++i) {
                ParameterizedType parameterizedType;
                Class clazz;
                Type type4 = object2[i];
                if (!(type4 instanceof ParameterizedType) || !Map.class.isAssignableFrom(clazz = (Class)(parameterizedType = (ParameterizedType)type4).getRawType())) continue;
                object = parameterizedType.getActualTypeArguments()[1];
                break;
            }
            if (object != null) break;
            type2 = ((Class)type3).getGenericSuperclass();
        }
        if (object != null && object instanceof TypeVariable) {
            object = Object.class;
        }
        return object;
    }

    public static Object getConstantValue(Class<?> clazz, String string) {
        Object object;
        Field field;
        if (clazz == null) {
            throw new IllegalArgumentException();
        }
        if (string == null) {
            throw new IllegalArgumentException();
        }
        try {
            field = FieldUtil.getField(clazz, string);
        }
        catch (NoSuchFieldException noSuchFieldException) {
            throw new IllegalArgumentException(noSuchFieldException);
        }
        int n = field.getModifiers();
        if ((n & 8) == 0 || (n & 0x10) == 0) {
            throw new IllegalArgumentException("Field is not a constant.");
        }
        try {
            object = field.get(null);
        }
        catch (IllegalAccessException illegalAccessException) {
            throw new IllegalArgumentException(illegalAccessException);
        }
        return object;
    }

    private static Method getStaticGetterMethod(Class<?> clazz, String object, Class<?> clazz2) {
        if (clazz == null) {
            throw new NullPointerException();
        }
        if (object == null) {
            throw new NullPointerException();
        }
        Method method = null;
        if (clazz2 != null) {
            object = Character.toUpperCase(((String)object).charAt(0)) + ((String)object).substring(1);
            String string = GET_PREFIX + (String)object;
            String string2 = IS_PREFIX + (String)object;
            try {
                method = MethodUtil.getMethod(clazz, string, new Class[]{clazz2});
            }
            catch (NoSuchMethodException noSuchMethodException) {
                // empty catch block
            }
            if (method == null) {
                try {
                    method = MethodUtil.getMethod(clazz, string2, new Class[]{clazz2});
                }
                catch (NoSuchMethodException noSuchMethodException) {
                    // empty catch block
                }
            }
            if (method == null) {
                Class<?>[] classArray = clazz2.getInterfaces();
                for (int i = 0; i < classArray.length; ++i) {
                    try {
                        method = MethodUtil.getMethod(clazz, string, new Class[]{classArray[i]});
                    }
                    catch (NoSuchMethodException noSuchMethodException) {
                        // empty catch block
                    }
                    if (method == null) {
                        try {
                            method = MethodUtil.getMethod(clazz, string2, new Class[]{classArray[i]});
                        }
                        catch (NoSuchMethodException noSuchMethodException) {
                            // empty catch block
                        }
                    }
                    if (method != null) break;
                }
            }
            if (method == null) {
                method = BeanAdapter.getStaticGetterMethod(clazz, (String)object, clazz2.getSuperclass());
            }
        }
        return method;
    }

    private static Method getStaticSetterMethod(Class<?> clazz, String object, Class<?> clazz2, Class<?> clazz3) {
        if (clazz == null) {
            throw new NullPointerException();
        }
        if (object == null) {
            throw new NullPointerException();
        }
        if (clazz2 == null) {
            throw new NullPointerException();
        }
        Method method = null;
        if (clazz3 != null) {
            object = Character.toUpperCase(((String)object).charAt(0)) + ((String)object).substring(1);
            String string = SET_PREFIX + (String)object;
            try {
                method = MethodUtil.getMethod(clazz, string, new Class[]{clazz3, clazz2});
            }
            catch (NoSuchMethodException noSuchMethodException) {
                // empty catch block
            }
            if (method == null) {
                Class<?>[] classArray = clazz3.getInterfaces();
                for (int i = 0; i < classArray.length; ++i) {
                    try {
                        method = MethodUtil.getMethod(clazz, string, new Class[]{classArray[i], clazz2});
                    }
                    catch (NoSuchMethodException noSuchMethodException) {
                        // empty catch block
                    }
                    if (method != null) break;
                }
            }
            if (method == null) {
                method = BeanAdapter.getStaticSetterMethod(clazz, (String)object, clazz2, clazz3.getSuperclass());
            }
        }
        return method;
    }

    private static String toAllCaps(String string) {
        if (string == null) {
            throw new NullPointerException();
        }
        StringBuilder stringBuilder = new StringBuilder();
        int n = string.length();
        for (int i = 0; i < n; ++i) {
            char c = string.charAt(i);
            if (Character.isUpperCase(c)) {
                stringBuilder.append('_');
            }
            stringBuilder.append(Character.toUpperCase(c));
        }
        return stringBuilder.toString();
    }

    private static class MethodCache {
        private final Map<String, List<Method>> methods;
        private final MethodCache nextClassCache;

        private MethodCache(Map<String, List<Method>> map, MethodCache methodCache) {
            this.methods = map;
            this.nextClassCache = methodCache;
        }

        private Method getMethod(String string, Class<?> ... classArray) {
            List<Method> list = this.methods.get(string);
            if (list != null) {
                for (int i = 0; i < list.size(); ++i) {
                    Method method = list.get(i);
                    if (!method.getName().equals(string) || !Arrays.equals(method.getParameterTypes(), classArray)) continue;
                    return method;
                }
            }
            return this.nextClassCache != null ? this.nextClassCache.getMethod(string, classArray) : null;
        }
    }
}

