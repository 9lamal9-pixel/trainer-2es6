/*
 * Decompiled with CFR 0.152.
 */
package javafx.embed.swing;

import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.SampleModel;
import java.awt.image.SinglePixelPackedSampleModel;
import java.nio.IntBuffer;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import javafx.scene.paint.Color;

public class SwingFXUtils {
    private SwingFXUtils() {
    }

    public static WritableImage toFXImage(BufferedImage object, WritableImage writableImage) {
        int[] nArray;
        Object object2;
        Object object3;
        int n = ((BufferedImage)object).getWidth();
        int n2 = ((BufferedImage)object).getHeight();
        switch (((BufferedImage)object).getType()) {
            case 2: 
            case 3: {
                break;
            }
            default: {
                object3 = new BufferedImage(n, n2, 3);
                object2 = ((BufferedImage)object3).createGraphics();
                ((Graphics)object2).drawImage((Image)object, 0, 0, null);
                ((Graphics)object2).dispose();
                object = object3;
            }
        }
        if (writableImage != null) {
            int n3 = (int)writableImage.getWidth();
            int n4 = (int)writableImage.getHeight();
            if (n3 < n || n4 < n2) {
                writableImage = null;
            } else if (n < n3 || n2 < n4) {
                nArray = new int[n3];
                PixelWriter pixelWriter = writableImage.getPixelWriter();
                WritablePixelFormat<IntBuffer> writablePixelFormat = PixelFormat.getIntArgbPreInstance();
                if (n < n3) {
                    pixelWriter.setPixels(n, 0, n3 - n, n2, writablePixelFormat, nArray, 0, 0);
                }
                if (n2 < n4) {
                    pixelWriter.setPixels(0, n2, n3, n4 - n2, writablePixelFormat, nArray, 0, 0);
                }
            }
        }
        if (writableImage == null) {
            writableImage = new WritableImage(n, n2);
        }
        object3 = writableImage.getPixelWriter();
        object2 = (DataBufferInt)((BufferedImage)object).getRaster().getDataBuffer();
        nArray = ((DataBufferInt)object2).getData();
        int n5 = ((BufferedImage)object).getRaster().getDataBuffer().getOffset();
        int n6 = 0;
        SampleModel sampleModel = ((BufferedImage)object).getRaster().getSampleModel();
        if (sampleModel instanceof SinglePixelPackedSampleModel) {
            n6 = ((SinglePixelPackedSampleModel)sampleModel).getScanlineStride();
        }
        WritablePixelFormat<IntBuffer> writablePixelFormat = ((BufferedImage)object).isAlphaPremultiplied() ? PixelFormat.getIntArgbPreInstance() : PixelFormat.getIntArgbInstance();
        object3.setPixels(0, 0, n, n2, writablePixelFormat, nArray, n5, n6);
        return writableImage;
    }

    static int getBestBufferedImageType(PixelFormat<?> pixelFormat, BufferedImage bufferedImage, boolean bl) {
        int n;
        if (bufferedImage != null && ((n = bufferedImage.getType()) == 2 || n == 3 || bl && (n == 4 || n == 1))) {
            return n;
        }
        switch (pixelFormat.getType()) {
            default: {
                return 3;
            }
            case BYTE_BGRA: 
            case INT_ARGB: {
                return 2;
            }
            case BYTE_RGB: {
                return 1;
            }
            case BYTE_INDEXED: 
        }
        return pixelFormat.isPremultiplied() ? 3 : 2;
    }

    private static WritablePixelFormat<IntBuffer> getAssociatedPixelFormat(BufferedImage bufferedImage) {
        switch (bufferedImage.getType()) {
            case 1: 
            case 3: {
                return PixelFormat.getIntArgbPreInstance();
            }
            case 2: {
                return PixelFormat.getIntArgbInstance();
            }
        }
        throw new InternalError("Failed to validate BufferedImage type");
    }

    private static boolean checkFXImageOpaque(PixelReader pixelReader, int n, int n2) {
        for (int i = 0; i < n; ++i) {
            for (int j = 0; j < n2; ++j) {
                Color color = pixelReader.getColor(i, j);
                if (color.getOpacity() == 1.0) continue;
                return false;
            }
        }
        return true;
    }

    public static BufferedImage fromFXImage(javafx.scene.image.Image image, BufferedImage bufferedImage) {
        PixelReader pixelReader = image.getPixelReader();
        if (pixelReader == null) {
            return null;
        }
        int n = (int)image.getWidth();
        int n2 = (int)image.getHeight();
        PixelFormat pixelFormat = pixelReader.getPixelFormat();
        boolean bl = false;
        switch (pixelFormat.getType()) {
            case BYTE_BGRA_PRE: 
            case INT_ARGB_PRE: 
            case BYTE_BGRA: 
            case INT_ARGB: {
                if (bufferedImage == null || bufferedImage.getType() != 4 && bufferedImage.getType() != 1) break;
                bl = SwingFXUtils.checkFXImageOpaque(pixelReader, n, n2);
                break;
            }
            case BYTE_RGB: {
                bl = true;
            }
        }
        int n3 = SwingFXUtils.getBestBufferedImageType(pixelReader.getPixelFormat(), bufferedImage, bl);
        if (bufferedImage != null) {
            int n4 = bufferedImage.getWidth();
            int n5 = bufferedImage.getHeight();
            if (n4 < n || n5 < n2 || bufferedImage.getType() != n3) {
                bufferedImage = null;
            } else if (n < n4 || n2 < n5) {
                Graphics2D graphics2D = bufferedImage.createGraphics();
                graphics2D.setComposite(AlphaComposite.Clear);
                graphics2D.fillRect(0, 0, n4, n5);
                graphics2D.dispose();
            }
        }
        if (bufferedImage == null) {
            bufferedImage = new BufferedImage(n, n2, n3);
        }
        DataBufferInt dataBufferInt = (DataBufferInt)bufferedImage.getRaster().getDataBuffer();
        int[] nArray = dataBufferInt.getData();
        int n6 = bufferedImage.getRaster().getDataBuffer().getOffset();
        int n7 = 0;
        SampleModel sampleModel = bufferedImage.getRaster().getSampleModel();
        if (sampleModel instanceof SinglePixelPackedSampleModel) {
            n7 = ((SinglePixelPackedSampleModel)sampleModel).getScanlineStride();
        }
        WritablePixelFormat<IntBuffer> writablePixelFormat = SwingFXUtils.getAssociatedPixelFormat(bufferedImage);
        pixelReader.getPixels(0, 0, n, n2, writablePixelFormat, nArray, n6, n7);
        return bufferedImage;
    }
}

