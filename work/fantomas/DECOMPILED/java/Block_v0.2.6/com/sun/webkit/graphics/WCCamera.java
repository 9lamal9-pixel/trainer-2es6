/*
 * Decompiled with CFR 0.152.
 */
package com.sun.webkit.graphics;

import com.sun.javafx.sg.prism.NGCamera;
import com.sun.javafx.sg.prism.NGDefaultCamera;

public class WCCamera
extends NGDefaultCamera {
    public static final NGCamera INSTANCE = new WCCamera();

    @Override
    public void validate(int n, int n2) {
        if ((double)n != this.viewWidth || (double)n2 != this.viewHeight) {
            this.setViewWidth(n);
            this.setViewHeight(n2);
            this.projViewTx.ortho(0.0, n, n2, 0.0, -9999999.0, 99999.0);
        }
    }
}

