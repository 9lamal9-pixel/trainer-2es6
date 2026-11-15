/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.impl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Map;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.util.IndexedStringMap;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.SortedArrayStringMap;
import org.apache.logging.log4j.util.StringMap;

public class ContextDataFactory {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final String CLASS_NAME = PropertiesUtil.getProperties().getStringProperty("log4j2.ContextData");
    private static final Class<? extends StringMap> CACHED_CLASS = ContextDataFactory.createCachedClass(CLASS_NAME);
    private static final MethodHandle DEFAULT_CONSTRUCTOR = ContextDataFactory.createDefaultConstructor(CACHED_CLASS);
    private static final MethodHandle INITIAL_CAPACITY_CONSTRUCTOR = ContextDataFactory.createInitialCapacityConstructor(CACHED_CLASS);
    private static final StringMap EMPTY_STRING_MAP = ContextDataFactory.createContextData(0);

    private static Class<? extends StringMap> createCachedClass(String className) {
        if (className == null) {
            return null;
        }
        try {
            return Loader.loadClass(className).asSubclass(IndexedStringMap.class);
        }
        catch (Exception any) {
            return null;
        }
    }

    private static MethodHandle createDefaultConstructor(Class<? extends StringMap> cachedClass) {
        if (cachedClass == null) {
            return null;
        }
        try {
            return LOOKUP.findConstructor(cachedClass, MethodType.methodType(Void.TYPE));
        }
        catch (IllegalAccessException | NoSuchMethodException ignored) {
            return null;
        }
    }

    private static MethodHandle createInitialCapacityConstructor(Class<? extends StringMap> cachedClass) {
        if (cachedClass == null) {
            return null;
        }
        try {
            return LOOKUP.findConstructor(cachedClass, MethodType.methodType(Void.TYPE, Integer.TYPE));
        }
        catch (IllegalAccessException | NoSuchMethodException ignored) {
            return null;
        }
    }

    public static StringMap createContextData() {
        if (DEFAULT_CONSTRUCTOR == null) {
            return new SortedArrayStringMap();
        }
        try {
            return DEFAULT_CONSTRUCTOR.invoke();
        }
        catch (Throwable ignored) {
            return new SortedArrayStringMap();
        }
    }

    public static StringMap createContextData(int initialCapacity) {
        if (INITIAL_CAPACITY_CONSTRUCTOR == null) {
            return new SortedArrayStringMap(initialCapacity);
        }
        try {
            return INITIAL_CAPACITY_CONSTRUCTOR.invoke(initialCapacity);
        }
        catch (Throwable ignored) {
            return new SortedArrayStringMap(initialCapacity);
        }
    }

    public static StringMap createContextData(Map<String, String> context) {
        StringMap contextData = ContextDataFactory.createContextData(context.size());
        for (Map.Entry<String, String> entry : context.entrySet()) {
            contextData.putValue(entry.getKey(), entry.getValue());
        }
        return contextData;
    }

    public static StringMap createContextData(ReadOnlyStringMap readOnlyStringMap) {
        StringMap contextData = ContextDataFactory.createContextData(readOnlyStringMap.size());
        contextData.putAll(readOnlyStringMap);
        return contextData;
    }

    public static StringMap emptyFrozenContextData() {
        return EMPTY_STRING_MAP;
    }

    static {
        EMPTY_STRING_MAP.freeze();
    }
}

