/*
 * Decompiled with CFR 0.152.
 */
package javafx.collections;

import javafx.collections.ObservableArray;

public interface ArrayChangeListener<T extends ObservableArray<T>> {
    public void onChanged(T var1, boolean var2, int var3, int var4);
}

