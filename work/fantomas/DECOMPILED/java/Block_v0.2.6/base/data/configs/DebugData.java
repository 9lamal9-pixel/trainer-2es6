/*
 * Decompiled with CFR 0.152.
 */
package base.data.configs;

import com.google.gson.annotations.SerializedName;

public class DebugData {
    @SerializedName(value="is_windowed")
    private final boolean isWindowed;

    public DebugData() {
        this.isWindowed = false;
    }

    public boolean getIsWindowed() {
        return this.isWindowed;
    }
}

