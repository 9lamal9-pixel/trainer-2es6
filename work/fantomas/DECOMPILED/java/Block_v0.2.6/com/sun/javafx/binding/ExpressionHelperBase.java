/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.binding;

import java.util.function.Predicate;
import javafx.beans.WeakListener;

public class ExpressionHelperBase {
    protected static int trim(int n, Object[] objectArray) {
        int n2;
        Predicate<Object> predicate = object -> object instanceof WeakListener && ((WeakListener)object).wasGarbageCollected();
        for (n2 = 0; n2 < n && !predicate.test(objectArray[n2]); ++n2) {
        }
        if (n2 < n) {
            int n3;
            for (n3 = n2 + 1; n3 < n; ++n3) {
                if (predicate.test(objectArray[n3])) continue;
                objectArray[n2++] = objectArray[n3];
            }
            n3 = n;
            n = n2;
            while (n2 < n3) {
                objectArray[n2] = null;
                ++n2;
            }
        }
        return n;
    }
}

