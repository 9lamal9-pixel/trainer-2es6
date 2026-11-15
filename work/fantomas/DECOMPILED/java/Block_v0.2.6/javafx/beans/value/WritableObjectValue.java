/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.value;

import javafx.beans.value.WritableValue;

public interface WritableObjectValue<T>
extends WritableValue<T> {
    public T get();

    public void set(T var1);
}

