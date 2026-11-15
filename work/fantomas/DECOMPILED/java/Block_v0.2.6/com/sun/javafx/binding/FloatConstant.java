/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.binding;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableFloatValue;

public final class FloatConstant
implements ObservableFloatValue {
    private final float value;

    private FloatConstant(float f) {
        this.value = f;
    }

    public static FloatConstant valueOf(float f) {
        return new FloatConstant(f);
    }

    @Override
    public float get() {
        return this.value;
    }

    @Override
    public Float getValue() {
        return Float.valueOf(this.value);
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
        return (int)this.value;
    }

    @Override
    public long longValue() {
        return (long)this.value;
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

