/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.image.impl;

import com.sun.javafx.image.AlphaType;
import com.sun.javafx.image.BytePixelAccessor;
import com.sun.javafx.image.BytePixelGetter;
import com.sun.javafx.image.BytePixelSetter;
import com.sun.javafx.image.PixelUtils;
import java.nio.ByteBuffer;

public class ByteArgb {
    public static final BytePixelGetter getter = Accessor.instance;
    public static final BytePixelSetter setter = Accessor.instance;
    public static final BytePixelAccessor accessor = Accessor.instance;

    static class Accessor
    implements BytePixelAccessor {
        static final BytePixelAccessor instance = new Accessor();

        private Accessor() {
        }

        @Override
        public AlphaType getAlphaType() {
            return AlphaType.NONPREMULTIPLIED;
        }

        @Override
        public int getNumElements() {
            return 4;
        }

        @Override
        public int getArgb(byte[] byArray, int n) {
            return byArray[n] << 24 | (byArray[n + 1] & 0xFF) << 16 | (byArray[n + 2] & 0xFF) << 8 | byArray[n + 3] & 0xFF;
        }

        @Override
        public int getArgbPre(byte[] byArray, int n) {
            return PixelUtils.NonPretoPre(this.getArgb(byArray, n));
        }

        @Override
        public int getArgb(ByteBuffer byteBuffer, int n) {
            return byteBuffer.get(n) << 24 | (byteBuffer.get(n + 1) & 0xFF) << 16 | (byteBuffer.get(n + 2) & 0xFF) << 8 | byteBuffer.get(n + 3) & 0xFF;
        }

        @Override
        public int getArgbPre(ByteBuffer byteBuffer, int n) {
            return PixelUtils.NonPretoPre(this.getArgb(byteBuffer, n));
        }

        @Override
        public void setArgb(byte[] byArray, int n, int n2) {
            byArray[n] = (byte)(n2 >> 24);
            byArray[n + 1] = (byte)(n2 >> 16);
            byArray[n + 2] = (byte)(n2 >> 8);
            byArray[n + 3] = (byte)n2;
        }

        @Override
        public void setArgbPre(byte[] byArray, int n, int n2) {
            this.setArgb(byArray, n, PixelUtils.PretoNonPre(n2));
        }

        @Override
        public void setArgb(ByteBuffer byteBuffer, int n, int n2) {
            byteBuffer.put(n, (byte)(n2 >> 24));
            byteBuffer.put(n + 1, (byte)(n2 >> 16));
            byteBuffer.put(n + 2, (byte)(n2 >> 8));
            byteBuffer.put(n + 3, (byte)n2);
        }

        @Override
        public void setArgbPre(ByteBuffer byteBuffer, int n, int n2) {
            this.setArgb(byteBuffer, n, PixelUtils.PretoNonPre(n2));
        }
    }
}

