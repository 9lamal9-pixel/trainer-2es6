/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.value;

import javafx.beans.value.WritableNumberValue;

public interface WritableLongValue
extends WritableNumberValue {
    public long get();

    public void set(long var1);

    @Override
    public void setValue(Number var1);
}

