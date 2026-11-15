/*
 * Decompiled with CFR 0.152.
 */
package com.sun.webkit.graphics;

import com.sun.webkit.graphics.Ref;
import com.sun.webkit.graphics.WCImage;

public abstract class WCImageFrame
extends Ref {
    public abstract WCImage getFrame();

    public abstract int[] getSize();

    protected void destroyDecodedData() {
    }
}

