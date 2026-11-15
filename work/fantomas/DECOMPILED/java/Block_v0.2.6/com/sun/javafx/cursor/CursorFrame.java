/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.cursor;

import com.sun.javafx.cursor.CursorType;
import java.util.HashMap;
import java.util.Map;

public abstract class CursorFrame {
    private Class<?> firstPlatformCursorClass;
    private Object firstPlatformCursor;
    private Map<Class<?>, Object> otherPlatformCursors;

    public abstract CursorType getCursorType();

    public <T> T getPlatformCursor(Class<T> clazz) {
        if (this.firstPlatformCursorClass == clazz) {
            return (T)this.firstPlatformCursor;
        }
        if (this.otherPlatformCursors != null) {
            return (T)this.otherPlatformCursors.get(clazz);
        }
        return null;
    }

    public <T> void setPlatforCursor(Class<T> clazz, T t) {
        if (this.firstPlatformCursorClass == null || this.firstPlatformCursorClass == clazz) {
            this.firstPlatformCursorClass = clazz;
            this.firstPlatformCursor = t;
            return;
        }
        if (this.otherPlatformCursors == null) {
            this.otherPlatformCursors = new HashMap();
        }
        this.otherPlatformCursors.put(clazz, t);
    }
}

