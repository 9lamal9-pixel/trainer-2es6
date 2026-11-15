/*
 * Decompiled with CFR 0.152.
 */
package com.sun.webkit.graphics;

import com.sun.javafx.logging.PlatformLogger;
import com.sun.webkit.Invoker;
import com.sun.webkit.graphics.Ref;
import com.sun.webkit.graphics.WCGraphicsContext;

public abstract class WCMediaPlayer
extends Ref {
    protected static final PlatformLogger log = PlatformLogger.getLogger("webkit.mediaplayer");
    private long nPtr;
    protected static final int NETWORK_STATE_EMPTY = 0;
    protected static final int NETWORK_STATE_IDLE = 1;
    protected static final int NETWORK_STATE_LOADING = 2;
    protected static final int NETWORK_STATE_LOADED = 3;
    protected static final int NETWORK_STATE_FORMAT_ERROR = 4;
    protected static final int NETWORK_STATE_NETWORK_ERROR = 5;
    protected static final int NETWORK_STATE_DECODE_ERROR = 6;
    protected static final int READY_STATE_HAVE_NOTHING = 0;
    protected static final int READY_STATE_HAVE_METADATA = 1;
    protected static final int READY_STATE_HAVE_CURRENT_DATA = 2;
    protected static final int READY_STATE_HAVE_FUTURE_DATA = 3;
    protected static final int READY_STATE_HAVE_ENOUGH_DATA = 4;
    protected static final int PRELOAD_NONE = 0;
    protected static final int PRELOAD_METADATA = 1;
    protected static final int PRELOAD_AUTO = 2;
    private int networkState = 0;
    private int readyState = 0;
    private int preload = 2;
    private boolean paused = true;
    private boolean seeking = false;
    private Runnable newFrameNotifier = () -> {
        if (this.nPtr != 0L) {
            this.notifyNewFrame(this.nPtr);
        }
    };
    private boolean preserve = true;

    protected WCMediaPlayer() {
    }

    void setNativePointer(long l) {
        if (l == 0L) {
            throw new IllegalArgumentException("nativePointer is 0");
        }
        if (this.nPtr != 0L) {
            throw new IllegalStateException("nPtr is not 0");
        }
        this.nPtr = l;
    }

    protected abstract void load(String var1, String var2);

    protected abstract void cancelLoad();

    protected abstract void disposePlayer();

    protected abstract void prepareToPlay();

    protected abstract void play();

    protected abstract void pause();

    protected abstract float getCurrentTime();

    protected abstract void seek(float var1);

    protected abstract void setRate(float var1);

    protected abstract void setVolume(float var1);

    protected abstract void setMute(boolean var1);

    protected abstract void setSize(int var1, int var2);

    protected abstract void setPreservesPitch(boolean var1);

    protected abstract void renderCurrentFrame(WCGraphicsContext var1, int var2, int var3, int var4, int var5);

    protected boolean getPreservesPitch() {
        return this.preserve;
    }

    protected int getNetworkState() {
        return this.networkState;
    }

    protected int getReadyState() {
        return this.readyState;
    }

    protected int getPreload() {
        return this.preload;
    }

    protected boolean isPaused() {
        return this.paused;
    }

    protected boolean isSeeking() {
        return this.seeking;
    }

    protected void notifyNetworkStateChanged(int n) {
        if (this.networkState != n) {
            this.networkState = n;
            int n2 = n;
            Invoker.getInvoker().invokeOnEventThread(() -> {
                if (this.nPtr != 0L) {
                    this.notifyNetworkStateChanged(this.nPtr, n2);
                }
            });
        }
    }

    protected void notifyReadyStateChanged(int n) {
        if (this.readyState != n) {
            this.readyState = n;
            int n2 = n;
            Invoker.getInvoker().invokeOnEventThread(() -> {
                if (this.nPtr != 0L) {
                    this.notifyReadyStateChanged(this.nPtr, n2);
                }
            });
        }
    }

    protected void notifyPaused(boolean bl) {
        log.fine("notifyPaused, {0} => {1}", this.paused, bl);
        if (this.paused != bl) {
            this.paused = bl;
            boolean bl2 = bl;
            Invoker.getInvoker().invokeOnEventThread(() -> {
                if (this.nPtr != 0L) {
                    this.notifyPaused(this.nPtr, bl2);
                }
            });
        }
    }

    protected void notifySeeking(boolean bl, int n) {
        log.fine("notifySeeking, {0} => {1}", this.seeking, bl);
        if (this.seeking != bl || this.readyState != n) {
            this.seeking = bl;
            this.readyState = n;
            boolean bl2 = bl;
            int n2 = n;
            Invoker.getInvoker().invokeOnEventThread(() -> {
                if (this.nPtr != 0L) {
                    this.notifySeeking(this.nPtr, bl2, n2);
                }
            });
        }
    }

    protected void notifyFinished() {
        Invoker.getInvoker().invokeOnEventThread(() -> {
            if (this.nPtr != 0L) {
                this.notifyFinished(this.nPtr);
            }
        });
    }

    protected void notifyReady(boolean bl, boolean bl2, float f) {
        boolean bl3 = bl;
        boolean bl4 = bl2;
        float f2 = f;
        Invoker.getInvoker().invokeOnEventThread(() -> {
            if (this.nPtr != 0L) {
                this.notifyReady(this.nPtr, bl3, bl4, f2);
            }
        });
    }

    protected void notifyDurationChanged(float f) {
        float f2 = f;
        Invoker.getInvoker().invokeOnEventThread(() -> {
            if (this.nPtr != 0L) {
                this.notifyDurationChanged(this.nPtr, f2);
            }
        });
    }

    protected void notifySizeChanged(int n, int n2) {
        int n3 = n;
        int n4 = n2;
        Invoker.getInvoker().invokeOnEventThread(() -> {
            if (this.nPtr != 0L) {
                this.notifySizeChanged(this.nPtr, n3, n4);
            }
        });
    }

    protected void notifyNewFrame() {
        Invoker.getInvoker().invokeOnEventThread(this.newFrameNotifier);
    }

    protected void notifyBufferChanged(float[] fArray, int n) {
        float[] fArray2 = fArray;
        int n2 = n;
        Invoker.getInvoker().invokeOnEventThread(() -> {
            if (this.nPtr != 0L) {
                this.notifyBufferChanged(this.nPtr, fArray2, n2);
            }
        });
    }

    private void fwkLoad(String string, String string2) {
        log.fine("fwkLoad, url={0}, userAgent={1}", string, string2);
        this.load(string, string2);
    }

    private void fwkCancelLoad() {
        log.fine("fwkCancelLoad");
        this.cancelLoad();
    }

    private void fwkPrepareToPlay() {
        log.fine("fwkPrepareToPlay");
        this.prepareToPlay();
    }

    private void fwkDispose() {
        log.fine("fwkDispose");
        this.nPtr = 0L;
        this.cancelLoad();
        this.disposePlayer();
    }

    private void fwkPlay() {
        log.fine("fwkPlay");
        this.play();
    }

    private void fwkPause() {
        log.fine("fwkPause");
        this.pause();
    }

    private float fwkGetCurrentTime() {
        float f = this.getCurrentTime();
        log.finer("fwkGetCurrentTime(), return {0}", Float.valueOf(f));
        return f;
    }

    private void fwkSeek(float f) {
        log.fine("fwkSeek({0})", Float.valueOf(f));
        this.seek(f);
    }

    private void fwkSetRate(float f) {
        log.fine("fwkSetRate({0})", Float.valueOf(f));
        this.setRate(f);
    }

    private void fwkSetVolume(float f) {
        log.fine("fwkSetVolume({0})", Float.valueOf(f));
        this.setVolume(f);
    }

    private void fwkSetMute(boolean bl) {
        log.fine("fwkSetMute({0})", bl);
        this.setMute(bl);
    }

    private void fwkSetSize(int n, int n2) {
        this.setSize(n, n2);
    }

    private void fwkSetPreservesPitch(boolean bl) {
        log.fine("setPreservesPitch({0})", bl);
        this.preserve = bl;
        this.setPreservesPitch(bl);
    }

    private void fwkSetPreload(int n) {
        log.fine("fwkSetPreload({0})", n == 0 ? "PRELOAD_NONE" : (n == 1 ? "PRELOAD_METADATA" : (n == 2 ? "PRELOAD_AUTO" : "INVALID VALUE: " + n)));
        this.preload = n;
    }

    void render(WCGraphicsContext wCGraphicsContext, int n, int n2, int n3, int n4) {
        log.finer("render(x={0}, y={1}, w={2}, h={3}", n, n2, n3, n4);
        this.renderCurrentFrame(wCGraphicsContext, n, n2, n3, n4);
    }

    private native void notifyNetworkStateChanged(long var1, int var3);

    private native void notifyReadyStateChanged(long var1, int var3);

    private native void notifyPaused(long var1, boolean var3);

    private native void notifySeeking(long var1, boolean var3, int var4);

    private native void notifyFinished(long var1);

    private native void notifyReady(long var1, boolean var3, boolean var4, float var5);

    private native void notifyDurationChanged(long var1, float var3);

    private native void notifySizeChanged(long var1, int var3, int var4);

    private native void notifyNewFrame(long var1);

    private native void notifyBufferChanged(long var1, float[] var3, int var4);
}

