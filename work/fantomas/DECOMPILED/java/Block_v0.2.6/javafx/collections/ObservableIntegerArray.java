/*
 * Decompiled with CFR 0.152.
 */
package javafx.collections;

import javafx.collections.ObservableArray;

public interface ObservableIntegerArray
extends ObservableArray<ObservableIntegerArray> {
    public void copyTo(int var1, int[] var2, int var3, int var4);

    public void copyTo(int var1, ObservableIntegerArray var2, int var3, int var4);

    public int get(int var1);

    public void addAll(int ... var1);

    public void addAll(ObservableIntegerArray var1);

    public void addAll(int[] var1, int var2, int var3);

    public void addAll(ObservableIntegerArray var1, int var2, int var3);

    public void setAll(int ... var1);

    public void setAll(int[] var1, int var2, int var3);

    public void setAll(ObservableIntegerArray var1);

    public void setAll(ObservableIntegerArray var1, int var2, int var3);

    public void set(int var1, int[] var2, int var3, int var4);

    public void set(int var1, ObservableIntegerArray var2, int var3, int var4);

    public void set(int var1, int var2);

    public int[] toArray(int[] var1);

    public int[] toArray(int var1, int[] var2, int var3);
}

