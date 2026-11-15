/*
 * Decompiled with CFR 0.152.
 */
package com.sun.scenario.animation.shared;

import com.sun.scenario.animation.shared.GeneralClipInterpolator;
import com.sun.scenario.animation.shared.SimpleClipInterpolator;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

public abstract class ClipInterpolator {
    static ClipInterpolator create(KeyFrame[] keyFrameArray, long[] lArray) {
        return ClipInterpolator.getRealKeyFrameCount(keyFrameArray) == 2 ? (keyFrameArray.length == 1 ? new SimpleClipInterpolator(keyFrameArray[0], lArray[0]) : new SimpleClipInterpolator(keyFrameArray[0], keyFrameArray[1], lArray[1])) : new GeneralClipInterpolator(keyFrameArray, lArray);
    }

    static int getRealKeyFrameCount(KeyFrame[] keyFrameArray) {
        int n = keyFrameArray.length;
        return n == 0 ? 0 : (keyFrameArray[0].getTime().greaterThan(Duration.ZERO) ? n + 1 : n);
    }

    abstract ClipInterpolator setKeyFrames(KeyFrame[] var1, long[] var2);

    abstract void interpolate(long var1);

    abstract void validate(boolean var1);
}

