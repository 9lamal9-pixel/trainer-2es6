/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.property.adapter;

import javafx.beans.property.adapter.ReadOnlyJavaBeanObjectProperty;
import javafx.beans.property.adapter.ReadOnlyJavaBeanObjectPropertyBuilder;

public final class JavaBeanQuickAccessor {
    private JavaBeanQuickAccessor() {
    }

    public static <T> ReadOnlyJavaBeanObjectProperty<T> createReadOnlyJavaBeanObjectProperty(Object object, String string) throws NoSuchMethodException {
        return ReadOnlyJavaBeanObjectPropertyBuilder.create().bean(object).name(string).build();
    }
}

