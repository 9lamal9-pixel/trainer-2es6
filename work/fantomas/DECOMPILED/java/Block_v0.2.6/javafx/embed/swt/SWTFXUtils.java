/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.eclipse.swt.graphics.ImageData
 *  org.eclipse.swt.graphics.PaletteData
 *  org.eclipse.swt.graphics.RGB
 */
package javafx.embed.swt;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.security.AccessController;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.image.WritablePixelFormat;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

public class SWTFXUtils {
    private static int blitSrc;
    private static boolean blitSrcCache;
    private static int alphaOpaque;
    private static boolean alphaOpaqueCache;
    private static int msbFirst;
    private static boolean msbFirstCache;
    private static Method blitDirect;
    private static Method blitPalette;
    private static Method getByteOrderMethod;

    private SWTFXUtils() {
    }

    public static WritableImage toFXImage(ImageData imageData, WritableImage writableImage) {
        Object object;
        int n;
        byte[] byArray = SWTFXUtils.convertImage(imageData);
        if (byArray == null) {
            return null;
        }
        int n2 = imageData.width;
        int n3 = imageData.height;
        if (writableImage != null) {
            int n4 = (int)writableImage.getWidth();
            n = (int)writableImage.getHeight();
            if (n4 < n2 || n < n3) {
                writableImage = null;
            } else if (n2 < n4 || n3 < n) {
                object = new int[n4];
                PixelWriter pixelWriter = writableImage.getPixelWriter();
                WritablePixelFormat<IntBuffer> writablePixelFormat = PixelFormat.getIntArgbPreInstance();
                if (n2 < n4) {
                    pixelWriter.setPixels(n2, 0, n4 - n2, n3, writablePixelFormat, (int[])object, 0, 0);
                }
                if (n3 < n) {
                    pixelWriter.setPixels(0, n3, n4, n - n3, writablePixelFormat, (int[])object, 0, 0);
                }
            }
        }
        if (writableImage == null) {
            writableImage = new WritableImage(n2, n3);
        }
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        n = n2 * 4;
        object = PixelFormat.getByteBgraInstance();
        pixelWriter.setPixels(0, 0, n2, n3, (PixelFormat<ByteBuffer>)object, byArray, 0, n);
        return writableImage;
    }

    public static ImageData fromFXImage(Image image, ImageData imageData) {
        PixelReader pixelReader = image.getPixelReader();
        if (pixelReader == null) {
            return null;
        }
        int n = (int)image.getWidth();
        int n2 = (int)image.getHeight();
        int n3 = n * 4;
        int n4 = n3 * n2;
        byte[] byArray = new byte[n4];
        WritablePixelFormat<ByteBuffer> writablePixelFormat = PixelFormat.getByteBgraInstance();
        pixelReader.getPixels(0, 0, n, n2, writablePixelFormat, byArray, 0, n3);
        byte[] byArray2 = new byte[n * n2];
        int n5 = 0;
        int n6 = 0;
        for (int i = 0; i < n2; ++i) {
            int n7 = 0;
            while (n7 < n) {
                byte by = byArray[n5 + 3];
                byArray[n5 + 3] = 0;
                byArray2[n6++] = by;
                ++n7;
                n5 += 4;
            }
        }
        PaletteData paletteData = new PaletteData(65280, 0xFF0000, -16777216);
        imageData = new ImageData(n, n2, 32, paletteData, 4, byArray);
        imageData.alphaData = byArray2;
        return imageData;
    }

    private static int BLIT_SRC() throws Exception {
        if (!blitSrcCache) {
            blitSrc = SWTFXUtils.readValue("BLIT_SRC");
            blitSrcCache = true;
        }
        return blitSrc;
    }

    private static int ALPHA_OPAQUE() throws Exception {
        if (!alphaOpaqueCache) {
            alphaOpaque = SWTFXUtils.readValue("ALPHA_OPAQUE");
            alphaOpaqueCache = true;
        }
        return alphaOpaque;
    }

    private static int MSB_FIRST() throws Exception {
        if (!msbFirstCache) {
            msbFirst = SWTFXUtils.readValue("MSB_FIRST");
            msbFirstCache = true;
        }
        return msbFirst;
    }

    private static int readValue(String string) throws Exception {
        Class<ImageData> clazz = ImageData.class;
        return AccessController.doPrivileged(() -> {
            Field field = clazz.getDeclaredField(string);
            field.setAccessible(true);
            return field.getInt(clazz);
        });
    }

    private static void blit(int n, byte[] byArray, int n2, int n3, int n4, int n5, int n6, int n7, int n8, int n9, int n10, int n11, int n12, byte[] byArray2, int n13, int n14, int n15, byte[] byArray3, int n16, int n17, int n18, int n19, int n20, int n21, int n22, int n23, int n24, int n25, boolean bl, boolean bl2) throws Exception {
        Class<ImageData> clazz = ImageData.class;
        if (blitDirect == null) {
            Class<Integer> clazz2 = Integer.TYPE;
            Class<Boolean> clazz3 = Boolean.TYPE;
            Class<byte[]> clazz4 = byte[].class;
            Class[] classArray = new Class[]{clazz2, clazz4, clazz2, clazz2, clazz2, clazz2, clazz2, clazz2, clazz2, clazz2, clazz2, clazz2, clazz2, clazz4, clazz2, clazz2, clazz2, clazz4, clazz2, clazz2, clazz2, clazz2, clazz2, clazz2, clazz2, clazz2, clazz2, clazz2, clazz3, clazz3};
            blitDirect = AccessController.doPrivileged(() -> {
                Method method = clazz.getDeclaredMethod("blit", classArray);
                method.setAccessible(true);
                return method;
            });
        }
        if (blitDirect != null) {
            blitDirect.invoke(clazz, n, byArray, n2, n3, n4, n5, n6, n7, n8, n9, n10, n11, n12, byArray2, n13, n14, n15, byArray3, n16, n17, n18, n19, n20, n21, n22, n23, n24, n25, bl, bl2);
        }
    }

    private static void blit(int n, byte[] byArray, int n2, int n3, int n4, int n5, int n6, int n7, int n8, byte[] byArray2, byte[] byArray3, byte[] byArray4, int n9, byte[] byArray5, int n10, int n11, int n12, byte[] byArray6, int n13, int n14, int n15, int n16, int n17, int n18, int n19, int n20, int n21, int n22, boolean bl, boolean bl2) throws Exception {
        Class<ImageData> clazz = ImageData.class;
        if (blitPalette == null) {
            Class<Integer> clazz2 = Integer.TYPE;
            Class<Boolean> clazz3 = Boolean.TYPE;
            Class<byte[]> clazz4 = byte[].class;
            Class[] classArray = new Class[]{clazz2, clazz4, clazz2, clazz2, clazz2, clazz2, clazz2, clazz2, clazz2, clazz4, clazz4, clazz4, clazz2, clazz4, clazz2, clazz2, clazz2, clazz4, clazz2, clazz2, clazz2, clazz2, clazz2, clazz2, clazz2, clazz2, clazz2, clazz2, clazz3, clazz3};
            blitPalette = AccessController.doPrivileged(() -> {
                Method method = clazz.getDeclaredMethod("blit", classArray);
                method.setAccessible(true);
                return method;
            });
        }
        if (blitPalette != null) {
            blitPalette.invoke(clazz, n, byArray, n2, n3, n4, n5, n6, n7, n8, byArray2, byArray3, byArray4, n9, byArray5, n10, n11, n12, byArray6, n13, n14, n15, n16, n17, n18, n19, n20, n21, n22, bl, bl2);
        }
    }

    private static int getByteOrder(ImageData imageData) throws Exception {
        Class<ImageData> clazz = ImageData.class;
        if (getByteOrderMethod != null) {
            getByteOrderMethod = AccessController.doPrivileged(() -> {
                Method method = clazz.getDeclaredMethod("getByteOrder", new Class[0]);
                method.setAccessible(true);
                return method;
            });
        }
        if (getByteOrderMethod != null) {
            return (Integer)getByteOrderMethod.invoke((Object)imageData, new Object[0]);
        }
        return SWTFXUtils.MSB_FIRST();
    }

    private static byte[] convertImage(ImageData imageData) {
        byte[] byArray = null;
        try {
            int n;
            byte[] byArray2;
            Object object;
            int n2;
            PaletteData paletteData = imageData.palette;
            if ((imageData.depth != 1 && imageData.depth != 2 && imageData.depth != 4 && imageData.depth != 8 || paletteData.isDirect) && imageData.depth != 8 && (imageData.depth != 16 && imageData.depth != 24 && imageData.depth != 32 || !paletteData.isDirect)) {
                return null;
            }
            int n3 = SWTFXUtils.BLIT_SRC();
            int n4 = SWTFXUtils.ALPHA_OPAQUE();
            int n5 = SWTFXUtils.MSB_FIRST();
            int n6 = imageData.width;
            int n7 = imageData.height;
            int n8 = SWTFXUtils.getByteOrder(imageData);
            int n9 = 3;
            int n10 = 65280;
            int n11 = 0xFF0000;
            int n12 = -16777216;
            int n13 = n6 * n7 * 4;
            int n14 = n6 * 4;
            byArray = new byte[n13];
            if (paletteData.isDirect) {
                SWTFXUtils.blit(n3, imageData.data, imageData.depth, imageData.bytesPerLine, n8, 0, 0, n6, n7, paletteData.redMask, paletteData.greenMask, paletteData.blueMask, n4, null, 0, 0, 0, byArray, 32, n14, n5, 0, 0, n6, n7, n10, n11, n12, false, false);
            } else {
                RGB[] rGBArray = paletteData.getRGBs();
                n2 = rGBArray.length;
                object = new byte[n2];
                byArray2 = new byte[n2];
                byte[] byArray3 = new byte[n2];
                for (n = 0; n < rGBArray.length; ++n) {
                    RGB rGB = rGBArray[n];
                    if (rGB == null) continue;
                    object[n] = (byte)rGB.red;
                    byArray2[n] = (byte)rGB.green;
                    byArray3[n] = (byte)rGB.blue;
                }
                SWTFXUtils.blit(n3, imageData.data, imageData.depth, imageData.bytesPerLine, n8, 0, 0, n6, n7, object, byArray2, byArray3, n4, null, 0, 0, 0, byArray, 32, n14, n5, 0, 0, n6, n7, n10, n11, n12, false, false);
            }
            int n15 = imageData.getTransparencyType();
            int n16 = n2 = n15 != 0 ? 1 : 0;
            if (n15 == 2 || imageData.transparentPixel != -1) {
                object = imageData.getTransparencyMask();
                byArray2 = object.data;
                int n17 = object.bytesPerLine;
                n = 0;
                int n18 = 0;
                for (int i = 0; i < n7; ++i) {
                    for (int j = 0; j < n6; ++j) {
                        byte by = byArray2[n18 + (j >> 3)];
                        int n19 = 1 << 7 - (j & 7);
                        byArray[n + n9] = (by & n19) != 0 ? -1 : 0;
                        n += 4;
                    }
                    n18 += n17;
                }
            } else if (imageData.alpha != -1) {
                n2 = 1;
                int n20 = imageData.alpha;
                byte by = (byte)n20;
                for (int i = 0; i < byArray.length; i += 4) {
                    byArray[i + n9] = by;
                }
            } else if (imageData.alphaData != null) {
                n2 = 1;
                object = new byte[imageData.alphaData.length];
                System.arraycopy(imageData.alphaData, 0, object, 0, ((byte[])object).length);
                int n21 = 0;
                int n22 = 0;
                for (n = 0; n < n7; ++n) {
                    for (int i = 0; i < n6; ++i) {
                        byArray[n21 + n9] = object[n22];
                        n21 += 4;
                        ++n22;
                    }
                }
            }
            if (n2 == 0) {
                for (int i = 0; i < byArray.length; i += 4) {
                    byArray[i + n9] = -1;
                }
            }
        }
        catch (Exception exception) {
            return null;
        }
        return byArray;
    }
}

