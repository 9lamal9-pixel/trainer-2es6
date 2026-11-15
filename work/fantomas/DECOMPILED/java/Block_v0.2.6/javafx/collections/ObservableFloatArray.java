/*
 * Decompiled with CFR 0.152.
 */
package javafx.collections;

import javafx.collections.ObservableArray;

public interface ObservableFloatArray
extends ObservableArray<ObservableFloatArray> {
    public void copyTo(int var1, float[] var2, int var3, int var4);

    public void copyTo(int var1, ObservableFloatArray var2, int var3, int var4);

    public float get(int var1);

    public void addAll(float ... var1);

    public void addAll(ObservableFloatArray var1);

    public void addAll(float[] var1, int var2, int var3);

    public void addAll(ObservableFloatArray var1, int var2, int var3);

    public void setAll(float ... var1);

    public void setAll(float[] var1, int var2, int var3);

    public void setAll(ObservableFloatArray var1);

    public void setAll(ObservableFloatArray var1, int var2, int var3);

    public void set(int var1, float[] var2, int var3, int var4);

    public void set(int var1, ObservableFloatArray var2, int var3, int var4);

    public void set(int var1, float var2);

    public float[] toArray(float[] var1);

    public float[] toArray(int var1, float[] var2, int var3);
}

