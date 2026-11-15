/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.value;

import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;

public interface ObservableValue<T>
extends Observable {
    public void addListener(ChangeListener<? super T> var1);

    public void removeListener(ChangeListener<? super T> var1);

    public T getValue();
}

