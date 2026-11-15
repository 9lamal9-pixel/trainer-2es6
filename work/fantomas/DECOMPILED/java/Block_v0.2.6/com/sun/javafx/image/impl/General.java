/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.image.impl;

import com.sun.javafx.image.AlphaType;
import com.sun.javafx.image.BytePixelGetter;
import com.sun.javafx.image.BytePixelSetter;
import com.sun.javafx.image.ByteToBytePixelConverter;
import com.sun.javafx.image.ByteToIntPixelConverter;
import com.sun.javafx.image.IntPixelGetter;
import com.sun.javafx.image.IntPixelSetter;
import com.sun.javafx.image.IntToBytePixelConverter;
import com.sun.javafx.image.IntToIntPixelConverter;
import com.sun.javafx.image.impl.BaseByteToByteConverter;
import com.sun.javafx.image.impl.BaseByteToIntConverter;
import com.sun.javafx.image.impl.BaseIntToByteConverter;
import com.sun.javafx.image.impl.BaseIntToIntConverter;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class General {
    public static ByteToBytePixelConverter create(BytePixelGetter bytePixelGetter, BytePixelSetter bytePixelSetter) {
        return new ByteToByteGeneralConverter(bytePixelGetter, bytePixelSetter);
    }

    public static ByteToIntPixelConverter create(BytePixelGetter bytePixelGetter, IntPixelSetter intPixelSetter) {
        return new ByteToIntGeneralConverter(bytePixelGetter, intPixelSetter);
    }

    public static IntToBytePixelConverter create(IntPixelGetter intPixelGetter, BytePixelSetter bytePixelSetter) {
        return new IntToByteGeneralConverter(intPixelGetter, bytePixelSetter);
    }

    public static IntToIntPixelConverter create(IntPixelGetter intPixelGetter, IntPixelSetter intPixelSetter) {
        return new IntToIntGeneralConverter(intPixelGetter, intPixelSetter);
    }

    static class IntToIntGeneralConverter
    extends BaseIntToIntConverter {
        boolean usePremult;

        public IntToIntGeneralConverter(IntPixelGetter intPixelGetter, IntPixelSetter intPixelSetter) {
            super(intPixelGetter, intPixelSetter);
            this.usePremult = intPixelGetter.getAlphaType() != AlphaType.NONPREMULTIPLIED && intPixelSetter.getAlphaType() != AlphaType.NONPREMULTIPLIED;
        }

        @Override
        void doConvert(int[] nArray, int n, int n2, int[] nArray2, int n3, int n4, int n5, int n6) {
            n2 -= n5;
            n4 -= n5;
            while (--n6 >= 0) {
                for (int i = 0; i < n5; ++i) {
                    if (this.usePremult) {
                        this.setter.setArgbPre(nArray2, n3, this.getter.getArgbPre(nArray, n));
                    } else {
                        this.setter.setArgb(nArray2, n3, this.getter.getArgb(nArray, n));
                    }
                    ++n;
                    ++n3;
                }
                n += n2;
                n3 += n4;
            }
        }

        @Override
        void doConvert(IntBuffer intBuffer, int n, int n2, IntBuffer intBuffer2, int n3, int n4, int n5, int n6) {
            n2 -= n5;
            n4 -= n5;
            while (--n6 >= 0) {
                for (int i = 0; i < n5; ++i) {
                    if (this.usePremult) {
                        this.setter.setArgbPre(intBuffer2, n3, this.getter.getArgbPre(intBuffer, n));
                    } else {
                        this.setter.setArgb(intBuffer2, n3, this.getter.getArgb(intBuffer, n));
                    }
                    ++n;
                    ++n3;
                }
                n += n2;
                n3 += n4;
            }
        }
    }

    static class IntToByteGeneralConverter
    extends BaseIntToByteConverter {
        boolean usePremult;

        public IntToByteGeneralConverter(IntPixelGetter intPixelGetter, BytePixelSetter bytePixelSetter) {
            super(intPixelGetter, bytePixelSetter);
            this.usePremult = intPixelGetter.getAlphaType() != AlphaType.NONPREMULTIPLIED && bytePixelSetter.getAlphaType() != AlphaType.NONPREMULTIPLIED;
        }

        @Override
        void doConvert(int[] nArray, int n, int n2, byte[] byArray, int n3, int n4, int n5, int n6) {
            n2 -= n5;
            n4 -= this.nDstElems * n5;
            while (--n6 >= 0) {
                for (int i = 0; i < n5; ++i) {
                    if (this.usePremult) {
                        this.setter.setArgbPre(byArray, n3, this.getter.getArgbPre(nArray, n));
                    } else {
                        this.setter.setArgb(byArray, n3, this.getter.getArgb(nArray, n));
                    }
                    ++n;
                    n3 += this.nDstElems;
                }
                n += n2;
                n3 += n4;
            }
        }

        @Override
        void doConvert(IntBuffer intBuffer, int n, int n2, ByteBuffer byteBuffer, int n3, int n4, int n5, int n6) {
            n2 -= n5;
            n4 -= this.nDstElems * n5;
            while (--n6 >= 0) {
                for (int i = 0; i < n5; ++i) {
                    if (this.usePremult) {
                        this.setter.setArgbPre(byteBuffer, n3, this.getter.getArgbPre(intBuffer, n));
                    } else {
                        this.setter.setArgb(byteBuffer, n3, this.getter.getArgb(intBuffer, n));
                    }
                    ++n;
                    n3 += this.nDstElems;
                }
                n += n2;
                n3 += n4;
            }
        }
    }

    static class ByteToIntGeneralConverter
    extends BaseByteToIntConverter {
        boolean usePremult;

        ByteToIntGeneralConverter(BytePixelGetter bytePixelGetter, IntPixelSetter intPixelSetter) {
            super(bytePixelGetter, intPixelSetter);
            this.usePremult = bytePixelGetter.getAlphaType() != AlphaType.NONPREMULTIPLIED && intPixelSetter.getAlphaType() != AlphaType.NONPREMULTIPLIED;
        }

        @Override
        void doConvert(byte[] byArray, int n, int n2, int[] nArray, int n3, int n4, int n5, int n6) {
            n2 -= this.nSrcElems * n5;
            n4 -= n5;
            while (--n6 >= 0) {
                for (int i = 0; i < n5; ++i) {
                    if (this.usePremult) {
                        this.setter.setArgbPre(nArray, n3, this.getter.getArgbPre(byArray, n));
                    } else {
                        this.setter.setArgb(nArray, n3, this.getter.getArgb(byArray, n));
                    }
                    n += this.nSrcElems;
                    ++n3;
                }
                n += n2;
                n3 += n4;
            }
        }

        @Override
        void doConvert(ByteBuffer byteBuffer, int n, int n2, IntBuffer intBuffer, int n3, int n4, int n5, int n6) {
            n2 -= this.nSrcElems * n5;
            n4 -= n5;
            while (--n6 >= 0) {
                for (int i = 0; i < n5; ++i) {
                    if (this.usePremult) {
                        this.setter.setArgbPre(intBuffer, n3, this.getter.getArgbPre(byteBuffer, n));
                    } else {
                        this.setter.setArgb(intBuffer, n3, this.getter.getArgb(byteBuffer, n));
                    }
                    n += this.nSrcElems;
                    ++n3;
                }
                n += n2;
                n3 += n4;
            }
        }
    }

    static class ByteToByteGeneralConverter
    extends BaseByteToByteConverter {
        boolean usePremult;

        ByteToByteGeneralConverter(BytePixelGetter bytePixelGetter, BytePixelSetter bytePixelSetter) {
            super(bytePixelGetter, bytePixelSetter);
            this.usePremult = bytePixelGetter.getAlphaType() != AlphaType.NONPREMULTIPLIED && bytePixelSetter.getAlphaType() != AlphaType.NONPREMULTIPLIED;
        }

        @Override
        void doConvert(byte[] byArray, int n, int n2, byte[] byArray2, int n3, int n4, int n5, int n6) {
            n2 -= this.nSrcElems * n5;
            n4 -= this.nDstElems * n5;
            while (--n6 >= 0) {
                for (int i = 0; i < n5; ++i) {
                    if (this.usePremult) {
                        this.setter.setArgbPre(byArray2, n3, this.getter.getArgbPre(byArray, n));
                    } else {
                        this.setter.setArgb(byArray2, n3, this.getter.getArgb(byArray, n));
                    }
                    n += this.nSrcElems;
                    n3 += this.nDstElems;
                }
                n += n2;
                n3 += n4;
            }
        }

        @Override
        void doConvert(ByteBuffer byteBuffer, int n, int n2, ByteBuffer byteBuffer2, int n3, int n4, int n5, int n6) {
            n2 -= this.nSrcElems * n5;
            n4 -= this.nDstElems * n5;
            while (--n6 >= 0) {
                for (int i = 0; i < n5; ++i) {
                    if (this.usePremult) {
                        this.setter.setArgbPre(byteBuffer2, n3, this.getter.getArgbPre(byteBuffer, n));
                    } else {
                        this.setter.setArgb(byteBuffer2, n3, this.getter.getArgb(byteBuffer, n));
                    }
                    n += this.nSrcElems;
                    n3 += this.nDstElems;
                }
                n += n2;
                n3 += n4;
            }
        }
    }
}

