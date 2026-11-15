/*
 * Decompiled with CFR 0.152.
 */
package com.sun.media.jfxmedia.events;

import com.sun.media.jfxmedia.events.NewFrameEvent;

public interface VideoRendererListener {
    public void videoFrameUpdated(NewFrameEvent var1);

    public void releaseVideoFrames();
}

