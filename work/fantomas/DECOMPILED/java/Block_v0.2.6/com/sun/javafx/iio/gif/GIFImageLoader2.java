/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.iio.gif;

import com.sun.javafx.iio.ImageFrame;
import com.sun.javafx.iio.ImageMetadata;
import com.sun.javafx.iio.ImageStorage;
import com.sun.javafx.iio.common.ImageLoaderImpl;
import com.sun.javafx.iio.common.ImageTools;
import com.sun.javafx.iio.gif.GIFDescriptor;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class GIFImageLoader2
extends ImageLoaderImpl {
    static final byte[] FILE_SIG87 = new byte[]{71, 73, 70, 56, 55, 97};
    static final byte[] FILE_SIG89 = new byte[]{71, 73, 70, 56, 57, 97};
    static final byte[] NETSCAPE_SIG = new byte[]{78, 69, 84, 83, 67, 65, 80, 69, 50, 46, 48};
    static final int DEFAULT_FPS = 25;
    InputStream stream = null;
    int screenW;
    int screenH;
    int bgColor;
    byte[][] globalPalette;
    byte[] image;
    int loopCount = 1;

    public GIFImageLoader2(InputStream inputStream) throws IOException {
        super(GIFDescriptor.getInstance());
        this.stream = inputStream;
        this.readGlobalHeader();
    }

    private void readGlobalHeader() throws IOException {
        byte[] byArray = this.readBytes(new byte[6]);
        if (!Arrays.equals(FILE_SIG87, byArray) && !Arrays.equals(FILE_SIG89, byArray)) {
            throw new IOException("Bad GIF signature!");
        }
        this.screenW = this.readShort();
        this.screenH = this.readShort();
        int n = this.readByte();
        this.bgColor = this.readByte();
        int n2 = this.readByte();
        if ((n & 0x80) != 0) {
            this.globalPalette = this.readPalete(2 << (n & 7), -1);
        }
        this.image = new byte[this.screenW * this.screenH * 4];
    }

    private byte[][] readPalete(int n, int n2) throws IOException {
        byte[][] byArray = new byte[4][n];
        byte[] byArray2 = this.readBytes(new byte[n * 3]);
        int n3 = 0;
        for (int i = 0; i != n; ++i) {
            for (int j = 0; j != 3; ++j) {
                byArray[j][i] = byArray2[n3++];
            }
            byArray[3][i] = i == n2 ? 0 : -1;
        }
        return byArray;
    }

    private void consumeAnExtension() throws IOException {
        int n = this.readByte();
        while (n != 0) {
            this.skipBytes(n);
            n = this.readByte();
        }
    }

    private void readAppExtension() throws IOException {
        int n = this.readByte();
        byte[] byArray = this.readBytes(new byte[n]);
        if (Arrays.equals(NETSCAPE_SIG, byArray)) {
            int n2 = this.readByte();
            while (n2 != 0) {
                byte[] byArray2 = this.readBytes(new byte[n2]);
                byte by = byArray2[0];
                if (n2 == 3 && by == 1) {
                    this.loopCount = byArray2[1] & 0xFF | (byArray2[2] & 0xFF) << 8;
                }
                n2 = this.readByte();
            }
        } else {
            this.consumeAnExtension();
        }
    }

    private int readControlCode() throws IOException {
        int n = this.readByte();
        int n2 = this.readByte();
        int n3 = this.readShort();
        int n4 = this.readByte();
        if (n != 4 || this.readByte() != 0) {
            throw new IOException("Bad GIF GraphicControlExtension");
        }
        return ((n2 & 0x1F) << 24) + (n4 << 16) + n3;
    }

    private int waitForImageFrame() throws IOException {
        int n;
        int n2 = 0;
        block9: while (true) {
            n = this.stream.read();
            switch (n) {
                case 44: {
                    return n2;
                }
                case 33: {
                    switch (this.readByte()) {
                        case 249: {
                            n2 = this.readControlCode();
                            continue block9;
                        }
                        case 255: {
                            this.readAppExtension();
                            continue block9;
                        }
                    }
                    this.consumeAnExtension();
                    continue block9;
                }
                case -1: 
                case 59: {
                    return -1;
                }
            }
            break;
        }
        throw new IOException("Unexpected GIF control characher 0x" + String.format("%02X", n));
    }

    private void decodeImage(byte[] byArray, int n, int n2, int[] nArray) throws IOException {
        LZWDecoder lZWDecoder = new LZWDecoder();
        byte[] byArray2 = lZWDecoder.getString();
        int n3 = 0;
        int n4 = 0;
        int n5 = n;
        block0: while (true) {
            int n6;
            if ((n6 = lZWDecoder.readString()) == -1) {
                lZWDecoder.waitForTerminator();
                return;
            }
            int n7 = 0;
            while (true) {
                if (n7 == n6) continue block0;
                int n8 = n5 < n6 - n7 ? n5 : n6 - n7;
                System.arraycopy(byArray2, n7, byArray, n4, n8);
                n4 += n8;
                n7 += n8;
                if ((n5 -= n8) != 0) continue;
                if (++n3 == n2) {
                    lZWDecoder.waitForTerminator();
                    return;
                }
                int n9 = nArray == null ? n3 : nArray[n3];
                n4 = n9 * n;
                n5 = n;
            }
            break;
        }
    }

    private int[] computeInterlaceReIndex(int n) {
        int n2;
        int[] nArray = new int[n];
        int n3 = 0;
        for (n2 = 0; n2 < n; n2 += 8) {
            nArray[n3++] = n2;
        }
        for (n2 = 4; n2 < n; n2 += 8) {
            nArray[n3++] = n2;
        }
        for (n2 = 2; n2 < n; n2 += 4) {
            nArray[n3++] = n2;
        }
        for (n2 = 1; n2 < n; n2 += 2) {
            nArray[n3++] = n2;
        }
        return nArray;
    }

    @Override
    public ImageFrame load(int n, int n2, int n3, boolean bl, boolean bl2) throws IOException {
        int n4 = this.waitForImageFrame();
        if (n4 < 0) {
            return null;
        }
        int n5 = this.readShort();
        int n6 = this.readShort();
        int n7 = this.readShort();
        int n8 = this.readShort();
        if (n5 + n7 > this.screenW || n6 + n8 > this.screenH) {
            throw new IOException("Wrong GIF image frame size");
        }
        int n9 = this.readByte();
        boolean bl3 = (n4 >>> 24 & 1) == 1;
        int n10 = bl3 ? n4 >>> 16 & 0xFF : -1;
        boolean bl4 = (n9 & 0x80) != 0;
        boolean bl5 = (n9 & 0x40) != 0;
        byte[][] byArray = bl4 ? this.readPalete(2 << (n9 & 7), n10) : this.globalPalette;
        int[] nArray = ImageTools.computeDimensions(this.screenW, this.screenH, n2, n3, bl);
        n2 = nArray[0];
        n3 = nArray[1];
        ImageMetadata imageMetadata = this.updateMetadata(n2, n3, n4 & 0xFFFF);
        int n11 = n4 >>> 26 & 7;
        byte[] byArray2 = new byte[n7 * n8];
        this.decodeImage(byArray2, n7, n8, bl5 ? this.computeInterlaceReIndex(n8) : null);
        ByteBuffer byteBuffer = this.decodePalette(byArray2, byArray, n10, n5, n6, n7, n8, n11);
        if (this.screenW != n2 || this.screenH != n3) {
            byteBuffer = ImageTools.scaleImage(byteBuffer, this.screenW, this.screenH, 4, n2, n3, bl2);
        }
        return new ImageFrame(ImageStorage.ImageType.RGBA, byteBuffer, n2, n3, n2 * 4, null, imageMetadata);
    }

    private int readByte() throws IOException {
        int n = this.stream.read();
        if (n < 0) {
            throw new EOFException();
        }
        return n;
    }

    private int readShort() throws IOException {
        int n = this.readByte();
        int n2 = this.readByte();
        return n + (n2 << 8);
    }

    private byte[] readBytes(byte[] byArray) throws IOException {
        return this.readBytes(byArray, 0, byArray.length);
    }

    private byte[] readBytes(byte[] byArray, int n, int n2) throws IOException {
        while (n2 > 0) {
            int n3 = this.stream.read(byArray, n, n2);
            if (n3 < 0) {
                throw new EOFException();
            }
            n += n3;
            n2 -= n3;
        }
        return byArray;
    }

    private void skipBytes(int n) throws IOException {
        ImageTools.skipFully(this.stream, n);
    }

    @Override
    public void dispose() {
    }

    private void restoreToBackground(byte[] byArray, int n, int n2, int n3, int n4) {
        for (int i = 0; i != n4; ++i) {
            int n5 = ((n2 + i) * this.screenW + n) * 4;
            for (int j = 0; j != n3; ++j) {
                byArray[n5 + 3] = 0;
                n5 += 4;
            }
        }
    }

    private ByteBuffer decodePalette(byte[] byArray, byte[][] byArray2, int n, int n2, int n3, int n4, int n5, int n6) {
        byte[] byArray3 = n6 == 3 ? (byte[])this.image.clone() : this.image;
        for (int i = 0; i != n5; ++i) {
            int n7;
            int n8;
            int n9 = ((n3 + i) * this.screenW + n2) * 4;
            int n10 = i * n4;
            if (n < 0) {
                for (n8 = 0; n8 != n4; ++n8) {
                    n7 = 0xFF & byArray[n10 + n8];
                    byArray3[n9 + 0] = byArray2[0][n7];
                    byArray3[n9 + 1] = byArray2[1][n7];
                    byArray3[n9 + 2] = byArray2[2][n7];
                    byArray3[n9 + 3] = byArray2[3][n7];
                    n9 += 4;
                }
                continue;
            }
            for (n8 = 0; n8 != n4; ++n8) {
                n7 = 0xFF & byArray[n10 + n8];
                if (n7 != n) {
                    byArray3[n9 + 0] = byArray2[0][n7];
                    byArray3[n9 + 1] = byArray2[1][n7];
                    byArray3[n9 + 2] = byArray2[2][n7];
                    byArray3[n9 + 3] = byArray2[3][n7];
                }
                n9 += 4;
            }
        }
        if (n6 != 3) {
            byArray3 = (byte[])byArray3.clone();
        }
        if (n6 == 2) {
            this.restoreToBackground(this.image, n2, n3, n4, n5);
        }
        return ByteBuffer.wrap(byArray3);
    }

    private ImageMetadata updateMetadata(int n, int n2, int n3) {
        ImageMetadata imageMetadata = new ImageMetadata(null, true, null, null, null, n3 != 0 ? n3 * 10 : 40, this.loopCount, n, n2, null, null, null);
        this.updateImageMetadata(imageMetadata);
        return imageMetadata;
    }

    class LZWDecoder {
        private final int initCodeSize;
        private final int clearCode;
        private final int eofCode;
        private int codeSize;
        private int codeMask;
        private int tableIndex;
        private int oldCode;
        private int blockLength = 0;
        private int blockPos = 0;
        private byte[] block = new byte[255];
        private int inData = 0;
        private int inBits = 0;
        private int[] prefix = new int[4096];
        private byte[] suffix = new byte[4096];
        private byte[] initial = new byte[4096];
        private int[] length = new int[4096];
        private byte[] string = new byte[4096];

        public LZWDecoder() throws IOException {
            this.initCodeSize = GIFImageLoader2.this.readByte();
            this.clearCode = 1 << this.initCodeSize;
            this.eofCode = this.clearCode + 1;
            this.initTable();
        }

        public final int readString() throws IOException {
            int n;
            int n2;
            int n3;
            int n4 = this.getCode();
            if (n4 == this.eofCode) {
                return -1;
            }
            if (n4 == this.clearCode) {
                this.initTable();
                n4 = this.getCode();
                if (n4 == this.eofCode) {
                    return -1;
                }
            } else {
                n3 = this.tableIndex;
                if (n4 < n3) {
                    n2 = n4;
                } else {
                    n2 = this.oldCode;
                    if (n4 != n3) {
                        throw new IOException("Bad GIF LZW: Out-of-sequence code!");
                    }
                }
                this.prefix[n3] = n = this.oldCode;
                this.suffix[n3] = this.initial[n2];
                this.initial[n3] = this.initial[n];
                this.length[n3] = this.length[n] + 1;
                ++this.tableIndex;
                if (this.tableIndex == 1 << this.codeSize && this.tableIndex < 4096) {
                    ++this.codeSize;
                    this.codeMask = (1 << this.codeSize) - 1;
                }
            }
            n2 = n4;
            n3 = this.length[n2];
            for (n = n3 - 1; n >= 0; --n) {
                this.string[n] = this.suffix[n2];
                n2 = this.prefix[n2];
            }
            this.oldCode = n4;
            return n3;
        }

        public final byte[] getString() {
            return this.string;
        }

        public final void waitForTerminator() throws IOException {
            GIFImageLoader2.this.consumeAnExtension();
        }

        private void initTable() {
            int n;
            int n2 = 1 << this.initCodeSize;
            for (n = 0; n < n2; ++n) {
                this.prefix[n] = -1;
                this.suffix[n] = (byte)n;
                this.initial[n] = (byte)n;
                this.length[n] = 1;
            }
            for (n = n2; n < 4096; ++n) {
                this.prefix[n] = -1;
                this.length[n] = 1;
            }
            this.codeSize = this.initCodeSize + 1;
            this.codeMask = (1 << this.codeSize) - 1;
            this.tableIndex = n2 + 2;
            this.oldCode = 0;
        }

        private int getCode() throws IOException {
            while (this.inBits < this.codeSize) {
                this.inData |= this.nextByte() << this.inBits;
                this.inBits += 8;
            }
            int n = this.inData & this.codeMask;
            this.inBits -= this.codeSize;
            this.inData >>>= this.codeSize;
            return n;
        }

        private int nextByte() throws IOException {
            if (this.blockPos == this.blockLength) {
                this.readData();
            }
            return this.block[this.blockPos++] & 0xFF;
        }

        private void readData() throws IOException {
            this.blockPos = 0;
            this.blockLength = GIFImageLoader2.this.readByte();
            if (this.blockLength <= 0) {
                throw new EOFException();
            }
            GIFImageLoader2.this.readBytes(this.block, 0, this.blockLength);
        }
    }
}

