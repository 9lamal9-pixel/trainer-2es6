/*
 * Decompiled with CFR 0.152.
 */
package com.sun.webkit.graphics;

import com.sun.javafx.logging.PlatformLogger;
import com.sun.webkit.Invoker;
import com.sun.webkit.graphics.BufferData;
import com.sun.webkit.graphics.GraphicsDecoder;
import com.sun.webkit.graphics.Ref;
import com.sun.webkit.graphics.WCGraphicsContext;
import com.sun.webkit.graphics.WCGraphicsManager;
import com.sun.webkit.graphics.WCRectangle;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class WCRenderQueue
extends Ref {
    private static final AtomicInteger idCountObj = new AtomicInteger(0);
    private static final PlatformLogger log = PlatformLogger.getLogger(WCRenderQueue.class.getName());
    public static final int MAX_QUEUE_SIZE = 524288;
    private final LinkedList<BufferData> buffers = new LinkedList();
    private BufferData currentBuffer = new BufferData();
    private final WCRectangle clip;
    private int size = 0;
    private final boolean opaque;
    protected final WCGraphicsContext gc;

    protected WCRenderQueue(WCGraphicsContext wCGraphicsContext) {
        this.clip = null;
        this.opaque = false;
        this.gc = wCGraphicsContext;
    }

    protected WCRenderQueue(WCRectangle wCRectangle, boolean bl) {
        this.clip = wCRectangle;
        this.opaque = bl;
        this.gc = null;
    }

    public synchronized int getSize() {
        return this.size;
    }

    public synchronized void addBuffer(ByteBuffer byteBuffer) {
        if (log.isLoggable(PlatformLogger.Level.FINE) && this.buffers.isEmpty()) {
            log.fine("'{'WCRenderQueue{0}[{1}]", this.hashCode(), idCountObj.incrementAndGet());
        }
        this.currentBuffer.setBuffer(byteBuffer);
        this.buffers.addLast(this.currentBuffer);
        this.currentBuffer = new BufferData();
        this.size += byteBuffer.capacity();
        if (this.size > 524288 && this.gc != null) {
            this.flush();
        }
    }

    public synchronized boolean isEmpty() {
        return this.buffers.isEmpty();
    }

    public synchronized void decode(WCGraphicsContext wCGraphicsContext) {
        for (BufferData bufferData : this.buffers) {
            try {
                GraphicsDecoder.decode(WCGraphicsManager.getGraphicsManager(), wCGraphicsContext, bufferData);
            }
            catch (RuntimeException runtimeException) {
                runtimeException.printStackTrace(System.err);
            }
        }
        this.dispose();
    }

    public synchronized void decode() {
        assert (this.gc != null);
        this.decode(this.gc);
        this.gc.flush();
    }

    public synchronized void decode(int n) {
        assert (this.gc != null);
        this.gc.setFontSmoothingType(n);
        this.decode();
    }

    protected abstract void flush();

    private void fwkFlush() {
        this.flush();
    }

    private void fwkAddBuffer(ByteBuffer byteBuffer) {
        this.addBuffer(byteBuffer);
    }

    public WCRectangle getClip() {
        return this.clip;
    }

    public synchronized void dispose() {
        int n = this.buffers.size();
        if (n > 0) {
            int n2 = 0;
            Object[] objectArray = new Object[n];
            for (BufferData bufferData : this.buffers) {
                objectArray[n2++] = bufferData.getBuffer();
            }
            this.buffers.clear();
            Invoker.getInvoker().invokeOnEventThread(() -> this.twkRelease(objectArray));
            this.size = 0;
            if (log.isLoggable(PlatformLogger.Level.FINE)) {
                log.fine("'}'WCRenderQueue{0}[{1}]", this.hashCode(), idCountObj.decrementAndGet());
            }
        }
    }

    protected abstract void disposeGraphics();

    private void fwkDisposeGraphics() {
        this.disposeGraphics();
    }

    private native void twkRelease(Object[] var1);

    private int refString(String string) {
        return this.currentBuffer.addString(string);
    }

    private int refIntArr(int[] nArray) {
        return this.currentBuffer.addIntArray(nArray);
    }

    private int refFloatArr(float[] fArray) {
        return this.currentBuffer.addFloatArray(fArray);
    }

    public boolean isOpaque() {
        return this.opaque;
    }

    public synchronized String toString() {
        return "WCRenderQueue{clip=" + this.clip + ", size=" + this.size + ", opaque=" + this.opaque + "}";
    }
}

