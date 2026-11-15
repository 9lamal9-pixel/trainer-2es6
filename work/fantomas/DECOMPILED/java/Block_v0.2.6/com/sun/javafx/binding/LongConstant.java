/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.binding;

import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableLongValue;

public final class LongConstant
implements ObservableLongValue {
    private final long value;

    private LongConstant(long l) {
        this.value = l;
    }

    public static LongConstant valueOf(long l) {
        return new LongConstant(l);
    }

    @Override
    public long get() {
        return this.value;
    }

    @Override
    public Long getValue() {
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
        return (int)this.value;
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

