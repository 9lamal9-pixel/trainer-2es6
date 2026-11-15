/*
 * Decompiled with CFR 0.152.
 */
package com.sun.scenario.animation.shared;

import com.sun.scenario.animation.shared.ClipInterpolator;
import com.sun.scenario.animation.shared.InterpolationInterval;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;

class GeneralClipInterpolator
extends ClipInterpolator {
    private KeyFrame[] keyFrames;
    private long[] keyFrameTicks;
    private InterpolationInterval[][] interval = new InterpolationInterval[0][];
    private int[] undefinedStartValues = new int[0];
    private boolean invalid = true;

    GeneralClipInterpolator(KeyFrame[] keyFrameArray, long[] lArray) {
        this.keyFrames = keyFrameArray;
        this.keyFrameTicks = lArray;
    }

    @Override
    ClipInterpolator setKeyFrames(KeyFrame[] keyFrameArray, long[] lArray) {
        if (ClipInterpolator.getRealKeyFrameCount(keyFrameArray) == 2) {
            return ClipInterpolator.create(keyFrameArray, lArray);
        }
        this.keyFrames = keyFrameArray;
        this.keyFrameTicks = lArray;
        this.invalid = true;
        return this;
    }

    @Override
    void validate(boolean bl) {
        if (this.invalid) {
            int n;
            Object object;
            Object object2;
            int n2;
            HashMap<Object, KeyValue> hashMap = new HashMap<Object, KeyValue>();
            int n3 = this.keyFrames.length;
            for (n2 = 0; n2 < n3; ++n2) {
                object2 = this.keyFrames[n2];
                if (this.keyFrameTicks[n2] != 0L) break;
                for (KeyValue object3 : ((KeyFrame)object2).getValues()) {
                    hashMap.put(object3.getTarget(), object3);
                }
            }
            object2 = new HashMap();
            HashSet hashSet = new HashSet();
            while (n2 < n3) {
                KeyFrame keyFrame = this.keyFrames[n2];
                long l = this.keyFrameTicks[n2];
                for (KeyValue keyValue : keyFrame.getValues()) {
                    object = keyValue.getTarget();
                    ArrayList<InterpolationInterval> arrayList = (ArrayList<InterpolationInterval>)object2.get(object);
                    KeyValue keyValue2 = (KeyValue)hashMap.get(object);
                    if (arrayList == null) {
                        arrayList = new ArrayList<InterpolationInterval>();
                        object2.put(object, arrayList);
                        if (keyValue2 == null) {
                            arrayList.add(InterpolationInterval.create(keyValue, l));
                            hashSet.add(object);
                        } else {
                            arrayList.add(InterpolationInterval.create(keyValue, l, keyValue2, l));
                        }
                    } else {
                        assert (keyValue2 != null);
                        arrayList.add(InterpolationInterval.create(keyValue, l, keyValue2, l - ((InterpolationInterval)arrayList.get((int)(arrayList.size() - 1))).ticks));
                    }
                    hashMap.put(object, keyValue);
                }
                ++n2;
            }
            int n4 = object2.size();
            if (this.interval.length != n4) {
                this.interval = new InterpolationInterval[n4][];
            }
            if (this.undefinedStartValues.length != (n = hashSet.size())) {
                this.undefinedStartValues = new int[n];
            }
            int n5 = 0;
            Iterator<Object> iterator = object2.entrySet().iterator();
            for (int i = 0; i < n4; ++i) {
                object = (Map.Entry)iterator.next();
                this.interval[i] = new InterpolationInterval[((List)object.getValue()).size()];
                ((List)object.getValue()).toArray(this.interval[i]);
                if (!hashSet.contains(object.getKey())) continue;
                this.undefinedStartValues[n5++] = i;
            }
            this.invalid = false;
        } else if (bl) {
            for (int n : this.undefinedStartValues) {
                this.interval[n][0].recalculateStartValue();
            }
        }
    }

    @Override
    void interpolate(long l) {
        block0: for (InterpolationInterval[] interpolationIntervalArray : this.interval) {
            int n = interpolationIntervalArray.length;
            long l2 = 0L;
            for (int i = 0; i < n - 1; ++i) {
                InterpolationInterval interpolationInterval = interpolationIntervalArray[i];
                long l3 = interpolationInterval.ticks;
                if (l <= l3) {
                    double d = (double)(l - l2) / (double)(l3 - l2);
                    interpolationInterval.interpolate(d);
                    continue block0;
                }
                l2 = l3;
            }
            InterpolationInterval interpolationInterval = interpolationIntervalArray[n - 1];
            double d = Math.min(1.0, (double)(l - l2) / (double)(interpolationInterval.ticks - l2));
            interpolationInterval.interpolate(d);
        }
    }
}

