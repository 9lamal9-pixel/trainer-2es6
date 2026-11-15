/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.value;

import javafx.beans.value.WritableNumberValue;

public interface WritableIntegerValue
extends WritableNumberValue {
    public int get();

    public void set(int var1);

    @Override
    public void setValue(Number var1);
}

