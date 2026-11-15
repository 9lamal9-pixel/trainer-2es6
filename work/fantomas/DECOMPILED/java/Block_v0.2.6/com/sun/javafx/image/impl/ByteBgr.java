/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.image.impl;

import com.sun.javafx.image.AlphaType;
import com.sun.javafx.image.BytePixelAccessor;
import com.sun.javafx.image.BytePixelGetter;
import com.sun.javafx.image.BytePixelSetter;
import com.sun.javafx.image.ByteToBytePixelConverter;
import com.sun.javafx.image.ByteToIntPixelConverter;
import com.sun.javafx.image.IntPixelSetter;
import com.sun.javafx.image.PixelUtils;
import com.sun.javafx.image.impl.BaseByteToByteConverter;
import com.sun.javafx.image.impl.BaseByteToIntConverter;
import com.sun.javafx.image.impl.ByteArgb;
import com.sun.javafx.image.impl.ByteBgra;
import com.sun.javafx.image.impl.ByteBgraPre;
import com.sun.javafx.image.impl.IntArgb;
import com.sun.javafx.image.impl.IntArgbPre;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class ByteBgr {
    public static final BytePixelGetter getter = Accessor.instance;
    public static final BytePixelSetter setter = Accessor.instance;
    public static final BytePixelAccessor accessor = Accessor.instance;
    private static ByteToBytePixelConverter ToByteBgrObj;

    public static ByteToBytePixelConverter ToByteBgrConverter() {
        if (ToByteBgrObj == null) {
            ToByteBgrObj = BaseByteToByteConverter.create(accessor);
        }
        return ToByteBgrObj;
    }

    public static ByteToBytePixelConverter ToByteBgraConverter() {
        return ToByteBgrfConv.nonpremult;
    }

    public static ByteToBytePixelConverter ToByteBgraPreConverter() {
        return ToByteBgrfConv.premult;
    }

    public static ByteToIntPixelConverter ToIntArgbConverter() {
        return ToIntFrgbConv.nonpremult;
    }

    public static ByteToIntPixelConverter ToIntArgbPreConverter() {
        return ToIntFrgbConv.premult;
    }

    public static ByteToBytePixelConverter ToByteArgbConverter() {
        return ToByteFrgbConv.nonpremult;
    }

    static class ToByteFrgbConv
    extends BaseByteToByteConverter {
        static final ByteToBytePixelConverter nonpremult = new ToByteFrgbConv(ByteArgb.setter);

        private ToByteFrgbConv(BytePixelSetter bytePixelSetter) {
            super(getter, bytePixelSetter);
        }

        @Override
        void doConvert(byte[] byArray, int n, int n2, byte[] byArray2, int n3, int n4, int n5, int n6) {
            n2 -= n5 * 3;
            n2 -= n5 * 4;
            while (--n6 >= 0) {
                for (int i = 0; i < n5; ++i) {
                    byArray2[n3++] = -1;
                    byArray2[n3++] = byArray[n + 2];
                    byArray2[n3++] = byArray[n + 1];
                    byArray2[n3++] = byArray[n];
                    n += 3;
                }
                n += n2;
                n3 += n4;
            }
        }

        @Override
        void doConvert(ByteBuffer byteBuffer, int n, int n2, ByteBuffer byteBuffer2, int n3, int n4, int n5, int n6) {
            n2 -= n5 * 3;
            n2 -= n5 * 4;
            while (--n6 >= 0) {
                for (int i = 0; i < n5; ++i) {
                    byteBuffer2.put(n3++, (byte)-1);
                    byteBuffer2.put(n3++, byteBuffer.get(n + 2));
                    byteBuffer2.put(n3++, byteBuffer.get(n + 1));
                    byteBuffer2.put(n3++, byteBuffer.get(n));
                    n += 3;
                }
                n += n2;
                n3 += n4;
            }
        }
    }

    static class ToIntFrgbConv
    extends BaseByteToIntConverter {
        public static final ByteToIntPixelConverter nonpremult = new ToIntFrgbConv(IntArgb.setter);
        public static final ByteToIntPixelConverter premult = new ToIntFrgbConv(IntArgbPre.setter);

        private ToIntFrgbConv(IntPixelSetter intPixelSetter) {
            super(getter, intPixelSetter);
        }

        @Override
        void doConvert(byte[] byArray, int n, int n2, int[] nArray, int n3, int n4, int n5, int n6) {
            n2 -= n5 * 3;
            while (--n6 >= 0) {
                for (int i = 0; i < n5; ++i) {
                    int n7 = byArray[n++] & 0xFF;
                    int n8 = byArray[n++] & 0xFF;
                    int n9 = byArray[n++] & 0xFF;
                    nArray[n3 + i] = 0xFF000000 | n9 << 16 | n8 << 8 | n7;
                }
                n += n2;
                n3 += n4;
            }
        }

        @Override
        void doConvert(ByteBuffer byteBuffer, int n, int n2, IntBuffer intBuffer, int n3, int n4, int n5, int n6) {
            n2 -= n5 * 3;
            while (--n6 >= 0) {
                for (int i = 0; i < n5; ++i) {
                    int n7 = byteBuffer.get(n++) & 0xFF;
                    int n8 = byteBuffer.get(n++) & 0xFF;
                    int n9 = byteBuffer.get(n++) & 0xFF;
                    intBuffer.put(n3 + i, 0xFF000000 | n9 << 16 | n8 << 8 | n7);
                }
                n += n2;
                n3 += n4;
            }
        }
    }

    static class ToByteBgrfConv
    extends BaseByteToByteConverter {
        public static final ByteToBytePixelConverter nonpremult = new ToByteBgrfConv(ByteBgra.setter);
        public static final ByteToBytePixelConverter premult = new ToByteBgrfConv(ByteBgraPre.setter);

        private ToByteBgrfConv(BytePixelSetter bytePixelSetter) {
            super(getter, bytePixelSetter);
        }

        @Override
        void doConvert(byte[] byArray, int n, int n2, byte[] byArray2, int n3, int n4, int n5, int n6) {
            n2 -= n5 * 3;
            n4 -= n5 * 4;
            while (--n6 >= 0) {
                for (int i = 0; i < n5; ++i) {
                    byArray2[n3++] = byArray[n++];
                    byArray2[n3++] = byArray[n++];
                    byArray2[n3++] = byArray[n++];
                    byArray2[n3++] = -1;
                }
                n += n2;
                n3 += n4;
            }
        }

        @Override
        void doConvert(ByteBuffer byteBuffer, int n, int n2, ByteBuffer byteBuffer2, int n3, int n4, int n5, int n6) {
            n2 -= n5 * 3;
            n4 -= n5 * 4;
            while (--n6 >= 0) {
                for (int i = 0; i < n5; ++i) {
                    byteBuffer2.put(n3++, byteBuffer.get(n++));
                    byteBuffer2.put(n3++, byteBuffer.get(n++));
                    byteBuffer2.put(n3++, byteBuffer.get(n++));
                    byteBuffer2.put(n3++, (byte)-1);
                }
                n += n2;
                n3 += n4;
            }
        }
    }

    static class Accessor
    implements BytePixelAccessor {
        static final BytePixelAccessor instance = new Accessor();

        private Accessor() {
        }

        @Override
        public AlphaType getAlphaType() {
            return AlphaType.OPAQUE;
        }

        @Override
        public int getNumElements() {
            return 3;
        }

        @Override
        public int getArgb(byte[] byArray, int n) {
            return byArray[n] & 0xFF | (byArray[n + 1] & 0xFF) << 8 | (byArray[n + 2] & 0xFF) << 16 | 0xFF000000;
        }

        @Override
        public int getArgbPre(byte[] byArray, int n) {
            return byArray[n] & 0xFF | (byArray[n + 1] & 0xFF) << 8 | (byArray[n + 2] & 0xFF) << 16 | 0xFF000000;
        }

        @Override
        public int getArgb(ByteBuffer byteBuffer, int n) {
            return byteBuffer.get(n) & 0xFF | (byteBuffer.get(n + 1) & 0xFF) << 8 | (byteBuffer.get(n + 2) & 0xFF) << 16 | 0xFF000000;
        }

        @Override
        public int getArgbPre(ByteBuffer byteBuffer, int n) {
            return byteBuffer.get(n) & 0xFF | (byteBuffer.get(n + 1) & 0xFF) << 8 | (byteBuffer.get(n + 2) & 0xFF) << 16 | 0xFF000000;
        }

        @Override
        public void setArgb(byte[] byArray, int n, int n2) {
            byArray[n] = (byte)n2;
            byArray[n + 1] = (byte)(n2 >> 8);
            byArray[n + 2] = (byte)(n2 >> 16);
        }

        @Override
        public void setArgbPre(byte[] byArray, int n, int n2) {
            this.setArgb(byArray, n, PixelUtils.PretoNonPre(n2));
        }

        @Override
        public void setArgb(ByteBuffer byteBuffer, int n, int n2) {
            byteBuffer.put(n, (byte)n2);
            byteBuffer.put(n + 1, (byte)(n2 >> 8));
            byteBuffer.put(n + 2, (byte)(n2 >> 16));
        }

        @Override
        public void setArgbPre(ByteBuffer byteBuffer, int n, int n2) {
            this.setArgb(byteBuffer, n, PixelUtils.PretoNonPre(n2));
        }
    }
}

