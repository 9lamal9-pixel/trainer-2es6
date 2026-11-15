/*
 * Decompiled with CFR 0.152.
 */
package com.sun.webkit.graphics;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

final class BufferData {
    private final AtomicInteger idCount = new AtomicInteger(0);
    private final HashMap<Integer, String> strMap = new HashMap();
    private final HashMap<Integer, int[]> intArrMap = new HashMap();
    private final HashMap<Integer, float[]> floatArrMap = new HashMap();
    private ByteBuffer buffer;

    BufferData() {
    }

    private int createID() {
        return this.idCount.incrementAndGet();
    }

    int addIntArray(int[] nArray) {
        int n = this.createID();
        this.intArrMap.put(n, nArray);
        return n;
    }

    int[] getIntArray(int n) {
        return this.intArrMap.get(n);
    }

    int addFloatArray(float[] fArray) {
        int n = this.createID();
        this.floatArrMap.put(n, fArray);
        return n;
    }

    float[] getFloatArray(int n) {
        return this.floatArrMap.get(n);
    }

    int addString(String string) {
        int n = this.createID();
        this.strMap.put(n, string);
        return n;
    }

    String getString(int n) {
        return this.strMap.get(n);
    }

    ByteBuffer getBuffer() {
        return this.buffer;
    }

    void setBuffer(ByteBuffer byteBuffer) {
        this.buffer = byteBuffer;
    }
}

