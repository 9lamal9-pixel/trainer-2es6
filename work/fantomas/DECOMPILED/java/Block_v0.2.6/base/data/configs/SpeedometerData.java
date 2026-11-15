/*
 * Decompiled with CFR 0.152.
 */
package base.data.configs;

import com.google.gson.annotations.SerializedName;

public class SpeedometerData {
    @SerializedName(value="min_value")
    private final int minValue;
    @SerializedName(value="step_value")
    private final int stepValue;
    @SerializedName(value="ticks")
    private final int ticks;

    public SpeedometerData() {
        this.minValue = 0;
        this.stepValue = 20;
        this.ticks = 12;
    }

    public int getMinValue() {
        return this.minValue;
    }

    public int getStepValue() {
        return this.stepValue;
    }

    public int getTicks() {
        return this.ticks;
    }
}

