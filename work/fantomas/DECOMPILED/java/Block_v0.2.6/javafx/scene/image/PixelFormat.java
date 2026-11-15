/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.image;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import javafx.scene.image.WritablePixelFormat;

public abstract class PixelFormat<T extends Buffer> {
    private Type type;

    PixelFormat(Type type) {
        this.type = type;
    }

    public static WritablePixelFormat<IntBuffer> getIntArgbInstance() {
        return WritablePixelFormat.IntArgb.INSTANCE;
    }

    public static WritablePixelFormat<IntBuffer> getIntArgbPreInstance() {
        return WritablePixelFormat.IntArgbPre.INSTANCE;
    }

    public static WritablePixelFormat<ByteBuffer> getByteBgraInstance() {
        return WritablePixelFormat.ByteBgra.INSTANCE;
    }

    public static WritablePixelFormat<ByteBuffer> getByteBgraPreInstance() {
        return WritablePixelFormat.ByteBgraPre.INSTANCE;
    }

    public static PixelFormat<ByteBuffer> getByteRgbInstance() {
        return ByteRgb.instance;
    }

    public static PixelFormat<ByteBuffer> createByteIndexedPremultipliedInstance(int[] nArray) {
        return IndexedPixelFormat.createByte(nArray, true);
    }

    public static PixelFormat<ByteBuffer> createByteIndexedInstance(int[] nArray) {
        return IndexedPixelFormat.createByte(nArray, false);
    }

    public Type getType() {
        return this.type;
    }

    public abstract boolean isWritable();

    public abstract boolean isPremultiplied();

    static int NonPretoPre(int n) {
        int n2 = n >>> 24;
        if (n2 == 255) {
            return n;
        }
        if (n2 == 0) {
            return 0;
        }
        int n3 = n >> 16 & 0xFF;
        int n4 = n >> 8 & 0xFF;
        int n5 = n & 0xFF;
        n3 = (n3 * n2 + 127) / 255;
        n4 = (n4 * n2 + 127) / 255;
        n5 = (n5 * n2 + 127) / 255;
        return n2 << 24 | n3 << 16 | n4 << 8 | n5;
    }

    static int PretoNonPre(int n) {
        int n2 = n >>> 24;
        if (n2 == 255 || n2 == 0) {
            return n;
        }
        int n3 = n >> 16 & 0xFF;
        int n4 = n >> 8 & 0xFF;
        int n5 = n & 0xFF;
        int n6 = n2 >> 1;
        n3 = n3 >= n2 ? 255 : (n3 * 255 + n6) / n2;
        n4 = n4 >= n2 ? 255 : (n4 * 255 + n6) / n2;
        n5 = n5 >= n2 ? 255 : (n5 * 255 + n6) / n2;
        return n2 << 24 | n3 << 16 | n4 << 8 | n5;
    }

    public abstract int getArgb(T var1, int var2, int var3, int var4);

    static class IndexedPixelFormat
    extends PixelFormat<ByteBuffer> {
        int[] precolors;
        int[] nonprecolors;
        boolean premult;

        static PixelFormat createByte(int[] nArray, boolean bl) {
            return new IndexedPixelFormat(Type.BYTE_INDEXED, bl, Arrays.copyOf(nArray, 256));
        }

        private IndexedPixelFormat(Type type, boolean bl, int[] nArray) {
            super(type);
            if (bl) {
                this.precolors = nArray;
            } else {
                this.nonprecolors = nArray;
            }
            this.premult = bl;
        }

        @Override
        public boolean isWritable() {
            return false;
        }

        @Override
        public boolean isPremultiplied() {
            return this.premult;
        }

        int[] getPreColors() {
            if (this.precolors == null) {
                int[] nArray = new int[this.nonprecolors.length];
                for (int i = 0; i < nArray.length; ++i) {
                    nArray[i] = IndexedPixelFormat.NonPretoPre(this.nonprecolors[i]);
                }
                this.precolors = nArray;
            }
            return this.precolors;
        }

        int[] getNonPreColors() {
            if (this.nonprecolors == null) {
                int[] nArray = new int[this.precolors.length];
                for (int i = 0; i < nArray.length; ++i) {
                    nArray[i] = IndexedPixelFormat.PretoNonPre(this.precolors[i]);
                }
                this.nonprecolors = nArray;
            }
            return this.nonprecolors;
        }

        @Override
        public int getArgb(ByteBuffer byteBuffer, int n, int n2, int n3) {
            return this.getNonPreColors()[byteBuffer.get(n2 * n3 + n) & 0xFF];
        }
    }

    static class ByteRgb
    extends PixelFormat<ByteBuffer> {
        static final ByteRgb instance = new ByteRgb();

        private ByteRgb() {
            super(Type.BYTE_RGB);
        }

        @Override
        public boolean isWritable() {
            return true;
        }

        @Override
        public boolean isPremultiplied() {
            return false;
        }

        @Override
        public int getArgb(ByteBuffer byteBuffer, int n, int n2, int n3) {
            int n4 = n2 * n3 + n * 3;
            int n5 = byteBuffer.get(n4) & 0xFF;
            int n6 = byteBuffer.get(n4 + 1) & 0xFF;
            int n7 = byteBuffer.get(n4 + 2) & 0xFF;
            return 0xFF000000 | n5 << 16 | n6 << 8 | n7;
        }
    }

    public static enum Type {
        INT_ARGB_PRE,
        INT_ARGB,
        BYTE_BGRA_PRE,
        BYTE_BGRA,
        BYTE_RGB,
        BYTE_INDEXED;

    }
}

