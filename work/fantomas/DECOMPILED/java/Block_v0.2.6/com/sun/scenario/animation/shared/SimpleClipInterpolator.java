/*
 * Decompiled with CFR 0.152.
 */
package com.sun.scenario.animation.shared;

import com.sun.scenario.animation.shared.ClipInterpolator;
import com.sun.scenario.animation.shared.InterpolationInterval;
import java.util.HashMap;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.beans.value.WritableValue;
import javafx.util.Duration;

class SimpleClipInterpolator
extends ClipInterpolator {
    private static final KeyFrame ZERO_FRAME = new KeyFrame(Duration.ZERO, new KeyValue[0]);
    private KeyFrame startKeyFrame;
    private KeyFrame endKeyFrame;
    private long endTicks;
    private InterpolationInterval[] interval;
    private int undefinedStartValueCount;
    private long ticks;
    private boolean invalid = true;

    SimpleClipInterpolator(KeyFrame keyFrame, KeyFrame keyFrame2, long l) {
        this.startKeyFrame = keyFrame;
        this.endKeyFrame = keyFrame2;
        this.endTicks = l;
    }

    SimpleClipInterpolator(KeyFrame keyFrame, long l) {
        this.startKeyFrame = ZERO_FRAME;
        this.endKeyFrame = keyFrame;
        this.endTicks = l;
    }

    @Override
    ClipInterpolator setKeyFrames(KeyFrame[] keyFrameArray, long[] lArray) {
        if (ClipInterpolator.getRealKeyFrameCount(keyFrameArray) != 2) {
            return ClipInterpolator.create(keyFrameArray, lArray);
        }
        if (keyFrameArray.length == 1) {
            this.startKeyFrame = ZERO_FRAME;
            this.endKeyFrame = keyFrameArray[0];
            this.endTicks = lArray[0];
        } else {
            this.startKeyFrame = keyFrameArray[0];
            this.endKeyFrame = keyFrameArray[1];
            this.endTicks = lArray[1];
        }
        this.invalid = true;
        return this;
    }

    @Override
    void validate(boolean bl) {
        if (this.invalid) {
            this.ticks = this.endTicks;
            HashMap hashMap = new HashMap();
            for (KeyValue keyValue : this.endKeyFrame.getValues()) {
                hashMap.put(keyValue.getTarget(), keyValue);
            }
            int n = hashMap.size();
            this.interval = new InterpolationInterval[n];
            int n2 = 0;
            for (KeyValue keyValue : this.startKeyFrame.getValues()) {
                WritableValue<?> writableValue = keyValue.getTarget();
                KeyValue keyValue2 = (KeyValue)hashMap.get(writableValue);
                if (keyValue2 == null) continue;
                this.interval[n2++] = InterpolationInterval.create(keyValue2, this.ticks, keyValue, this.ticks);
                hashMap.remove(writableValue);
            }
            this.undefinedStartValueCount = hashMap.values().size();
            for (KeyValue keyValue : hashMap.values()) {
                this.interval[n2++] = InterpolationInterval.create(keyValue, this.ticks);
            }
            this.invalid = false;
        } else if (bl) {
            int n = this.interval.length;
            for (int i = n - this.undefinedStartValueCount; i < n; ++i) {
                this.interval[i].recalculateStartValue();
            }
        }
    }

    @Override
    void interpolate(long l) {
        double d = (double)l / (double)this.ticks;
        int n = this.interval.length;
        for (int i = 0; i < n; ++i) {
            this.interval[i].interpolate(d);
        }
    }
}

