/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.binding;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableIntegerValue;

public final class IntegerConstant
implements ObservableIntegerValue {
    private final int value;

    private IntegerConstant(int n) {
        this.value = n;
    }

    public static IntegerConstant valueOf(int n) {
        return new IntegerConstant(n);
    }

    @Override
    public int get() {
        return this.value;
    }

    @Override
    public Integer getValue() {
        return this.value;
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
    }

    @Override
    public void addListener(ChangeListener<? super Number> changeListener) {
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
    }

    @Override
    public void removeListener(ChangeListener<? super Number> changeListener) {
    }

    @Override
    public int intValue() {
        return this.value;
    }

    @Override
    public long longValue() {
        return this.value;
    }

    @Override
    public float floatValue() {
        return this.value;
    }

    @Override
    public double doubleValue() {
        return this.value;
    }
}

