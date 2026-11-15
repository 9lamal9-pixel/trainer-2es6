/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.image.impl;

import com.sun.javafx.image.BytePixelSetter;
import com.sun.javafx.image.IntPixelGetter;
import com.sun.javafx.image.IntToBytePixelConverter;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public abstract class BaseIntToByteConverter
implements IntToBytePixelConverter {
    protected final IntPixelGetter getter;
    protected final BytePixelSetter setter;
    protected final int nDstElems;

    BaseIntToByteConverter(IntPixelGetter intPixelGetter, BytePixelSetter bytePixelSetter) {
        this.getter = intPixelGetter;
        this.setter = bytePixelSetter;
        this.nDstElems = bytePixelSetter.getNumElements();
    }

    public final IntPixelGetter getGetter() {
        return this.getter;
    }

    public final BytePixelSetter getSetter() {
        return this.setter;
    }

    abstract void doConvert(int[] var1, int var2, int var3, byte[] var4, int var5, int var6, int var7, int var8);

    abstract void doConvert(IntBuffer var1, int var2, int var3, ByteBuffer var4, int var5, int var6, int var7, int var8);

    @Override
    public final void convert(int[] nArray, int n, int n2, byte[] byArray, int n3, int n4, int n5, int n6) {
        if (n5 <= 0 || n6 <= 0) {
            return;
        }
        if (n2 == n5 && n4 == n5 * this.nDstElems) {
            n5 *= n6;
            n6 = 1;
        }
        this.doConvert(nArray, n, n2, byArray, n3, n4, n5, n6);
    }

    @Override
    public final void convert(IntBuffer intBuffer, int n, int n2, ByteBuffer byteBuffer, int n3, int n4, int n5, int n6) {
        if (n5 <= 0 || n6 <= 0) {
            return;
        }
        if (n2 == n5 && n4 == n5 * this.nDstElems) {
            n5 *= n6;
            n6 = 1;
        }
        if (intBuffer.hasArray() && byteBuffer.hasArray()) {
            this.doConvert(intBuffer.array(), n += intBuffer.arrayOffset(), n2, byteBuffer.array(), n3 += byteBuffer.arrayOffset(), n4, n5, n6);
        } else {
            this.doConvert(intBuffer, n, n2, byteBuffer, n3, n4, n5, n6);
        }
    }

    @Override
    public final void convert(IntBuffer intBuffer, int n, int n2, byte[] byArray, int n3, int n4, int n5, int n6) {
        if (n5 <= 0 || n6 <= 0) {
            return;
        }
        if (n2 == n5 && n4 == n5 * this.nDstElems) {
            n5 *= n6;
            n6 = 1;
        }
        if (intBuffer.hasArray()) {
            int[] nArray = intBuffer.array();
            this.doConvert(nArray, n += intBuffer.arrayOffset(), n2, byArray, n3, n4, n5, n6);
        } else {
            ByteBuffer byteBuffer = ByteBuffer.wrap(byArray);
            this.doConvert(intBuffer, n, n2, byteBuffer, n3, n4, n5, n6);
        }
    }

    @Override
    public final void convert(int[] nArray, int n, int n2, ByteBuffer byteBuffer, int n3, int n4, int n5, int n6) {
        if (n5 <= 0 || n6 <= 0) {
            return;
        }
        if (n2 == n5 && n4 == n5 * this.nDstElems) {
            n5 *= n6;
            n6 = 1;
        }
        if (byteBuffer.hasArray()) {
            byte[] byArray = byteBuffer.array();
            this.doConvert(nArray, n, n2, byArray, n3 += byteBuffer.arrayOffset(), n4, n5, n6);
        } else {
            IntBuffer intBuffer = IntBuffer.wrap(nArray);
            this.doConvert(intBuffer, n, n2, byteBuffer, n3, n4, n5, n6);
        }
    }
}

