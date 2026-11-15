/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.value.ObservableValue;

public interface ReadOnlyProperty<T>
extends ObservableValue<T> {
    public Object getBean();

    public String getName();
}

