/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.binding;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.StringExpression;
import javafx.beans.value.ChangeListener;

public class StringConstant
extends StringExpression {
    private final String value;

    private StringConstant(String string) {
        this.value = string;
    }

    public static StringConstant valueOf(String string) {
        return new StringConstant(string);
    }

    @Override
    public String get() {
        return this.value;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
    }

    @Override
    public void addListener(ChangeListener<? super String> changeListener) {
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
    }

    @Override
    public void removeListener(ChangeListener<? super String> changeListener) {
    }
}

