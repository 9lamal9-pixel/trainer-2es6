/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.image;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import javafx.scene.image.PixelFormat;

public abstract class WritablePixelFormat<T extends Buffer>
extends PixelFormat<T> {
    WritablePixelFormat(PixelFormat.Type type) {
        super(type);
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    public abstract void setArgb(T var1, int var2, int var3, int var4, int var5);

    static class ByteBgraPre
    extends WritablePixelFormat<ByteBuffer> {
        static final ByteBgraPre INSTANCE = new ByteBgraPre();

        private ByteBgraPre() {
            super(PixelFormat.Type.BYTE_BGRA_PRE);
        }

        @Override
        public boolean isPremultiplied() {
            return true;
        }

        @Override
        public int getArgb(ByteBuffer byteBuffer, int n, int n2, int n3) {
            int n4 = n2 * n3 + n * 4;
            int n5 = byteBuffer.get(n4) & 0xFF;
            int n6 = byteBuffer.get(n4 + 1) & 0xFF;
            int n7 = byteBuffer.get(n4 + 2) & 0xFF;
            int n8 = byteBuffer.get(n4 + 3) & 0xFF;
            if (n8 > 0 && n8 < 255) {
                int n9 = n8 >> 1;
                n7 = n7 >= n8 ? 255 : (n7 * 255 + n9) / n8;
                n6 = n6 >= n8 ? 255 : (n6 * 255 + n9) / n8;
                n5 = n5 >= n8 ? 255 : (n5 * 255 + n9) / n8;
            }
            return n8 << 24 | n7 << 16 | n6 << 8 | n5;
        }

        @Override
        public void setArgb(ByteBuffer byteBuffer, int n, int n2, int n3, int n4) {
            int n5;
            int n6;
            int n7;
            int n8 = n2 * n3 + n * 4;
            int n9 = n4 >>> 24;
            if (n9 > 0) {
                n7 = n4 >> 16 & 0xFF;
                n6 = n4 >> 8 & 0xFF;
                n5 = n4 & 0xFF;
                if (n9 < 255) {
                    n7 = (n7 * n9 + 127) / 255;
                    n6 = (n6 * n9 + 127) / 255;
                    n5 = (n5 * n9 + 127) / 255;
                }
            } else {
                n5 = 0;
                n6 = 0;
                n7 = 0;
                n9 = 0;
            }
            byteBuffer.put(n8, (byte)n5);
            byteBuffer.put(n8 + 1, (byte)n6);
            byteBuffer.put(n8 + 2, (byte)n7);
            byteBuffer.put(n8 + 3, (byte)n9);
        }
    }

    static class ByteBgra
    extends WritablePixelFormat<ByteBuffer> {
        static final ByteBgra INSTANCE = new ByteBgra();

        private ByteBgra() {
            super(PixelFormat.Type.BYTE_BGRA);
        }

        @Override
        public boolean isPremultiplied() {
            return false;
        }

        @Override
        public int getArgb(ByteBuffer byteBuffer, int n, int n2, int n3) {
            int n4 = n2 * n3 + n * 4;
            int n5 = byteBuffer.get(n4) & 0xFF;
            int n6 = byteBuffer.get(n4 + 1) & 0xFF;
            int n7 = byteBuffer.get(n4 + 2) & 0xFF;
            int n8 = byteBuffer.get(n4 + 3) & 0xFF;
            return n8 << 24 | n7 << 16 | n6 << 8 | n5;
        }

        @Override
        public void setArgb(ByteBuffer byteBuffer, int n, int n2, int n3, int n4) {
            int n5 = n2 * n3 + n * 4;
            byteBuffer.put(n5, (byte)n4);
            byteBuffer.put(n5 + 1, (byte)(n4 >> 8));
            byteBuffer.put(n5 + 2, (byte)(n4 >> 16));
            byteBuffer.put(n5 + 3, (byte)(n4 >> 24));
        }
    }

    static class IntArgbPre
    extends WritablePixelFormat<IntBuffer> {
        static final IntArgbPre INSTANCE = new IntArgbPre();

        private IntArgbPre() {
            super(PixelFormat.Type.INT_ARGB_PRE);
        }

        @Override
        public boolean isPremultiplied() {
            return true;
        }

        @Override
        public int getArgb(IntBuffer intBuffer, int n, int n2, int n3) {
            return IntArgbPre.PretoNonPre(intBuffer.get(n2 * n3 + n));
        }

        @Override
        public void setArgb(IntBuffer intBuffer, int n, int n2, int n3, int n4) {
            intBuffer.put(n2 * n3 + n, IntArgbPre.NonPretoPre(n4));
        }
    }

    static class IntArgb
    extends WritablePixelFormat<IntBuffer> {
        static final IntArgb INSTANCE = new IntArgb();

        private IntArgb() {
            super(PixelFormat.Type.INT_ARGB);
        }

        @Override
        public boolean isPremultiplied() {
            return false;
        }

        @Override
        public int getArgb(IntBuffer intBuffer, int n, int n2, int n3) {
            return intBuffer.get(n2 * n3 + n);
        }

        @Override
        public void setArgb(IntBuffer intBuffer, int n, int n2, int n3, int n4) {
            intBuffer.put(n2 * n3 + n, n4);
        }
    }
}

