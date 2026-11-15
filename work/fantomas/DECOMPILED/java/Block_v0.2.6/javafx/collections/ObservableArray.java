/*
 * Decompiled with CFR 0.152.
 */
package javafx.collections;

import javafx.beans.Observable;
import javafx.collections.ArrayChangeListener;

public interface ObservableArray<T extends ObservableArray<T>>
extends Observable {
    public void addListener(ArrayChangeListener<T> var1);

    public void removeListener(ArrayChangeListener<T> var1);

    public void resize(int var1);

    public void ensureCapacity(int var1);

    public void trimToSize();

    public void clear();

    public int size();
}

