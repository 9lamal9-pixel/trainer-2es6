/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.value;

import javafx.beans.value.ObservableValue;

@FunctionalInterface
public interface ChangeListener<T> {
    public void changed(ObservableValue<? extends T> var1, T var2, T var3);
}

