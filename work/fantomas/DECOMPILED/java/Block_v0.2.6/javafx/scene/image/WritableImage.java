/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.image;

import com.sun.javafx.tk.ImageLoader;
import com.sun.javafx.tk.PlatformImage;
import com.sun.javafx.tk.Toolkit;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import javafx.beans.NamedArg;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

public class WritableImage
extends Image {
    private ImageLoader tkImageLoader;
    private PixelWriter writer;

    public WritableImage(@NamedArg(value="width") int n, @NamedArg(value="height") int n2) {
        super(n, n2);
    }

    public WritableImage(@NamedArg(value="reader") PixelReader pixelReader, @NamedArg(value="width") int n, @NamedArg(value="height") int n2) {
        super(n, n2);
        this.getPixelWriter().setPixels(0, 0, n, n2, pixelReader, 0, 0);
    }

    public WritableImage(@NamedArg(value="reader") PixelReader pixelReader, @NamedArg(value="x") int n, @NamedArg(value="y") int n2, @NamedArg(value="width") int n3, @NamedArg(value="height") int n4) {
        super(n3, n4);
        this.getPixelWriter().setPixels(0, 0, n3, n4, pixelReader, n, n2);
    }

    @Override
    boolean isAnimation() {
        return true;
    }

    @Override
    boolean pixelsReadable() {
        return true;
    }

    public final PixelWriter getPixelWriter() {
        if (this.getProgress() < 1.0 || this.isError()) {
            return null;
        }
        if (this.writer == null) {
            this.writer = new PixelWriter(){
                ReadOnlyObjectProperty<PlatformImage> pimgprop;
                {
                    this.pimgprop = WritableImage.this.acc_platformImageProperty();
                }

                @Override
                public PixelFormat getPixelFormat() {
                    PlatformImage platformImage = WritableImage.this.getWritablePlatformImage();
                    return platformImage.getPlatformPixelFormat();
                }

                @Override
                public void setArgb(int n, int n2, int n3) {
                    WritableImage.this.getWritablePlatformImage().setArgb(n, n2, n3);
                    WritableImage.this.pixelsDirty();
                }

                @Override
                public void setColor(int n, int n2, Color color) {
                    if (color == null) {
                        throw new NullPointerException("Color cannot be null");
                    }
                    int n3 = (int)Math.round(color.getOpacity() * 255.0);
                    int n4 = (int)Math.round(color.getRed() * 255.0);
                    int n5 = (int)Math.round(color.getGreen() * 255.0);
                    int n6 = (int)Math.round(color.getBlue() * 255.0);
                    this.setArgb(n, n2, n3 << 24 | n4 << 16 | n5 << 8 | n6);
                }

                @Override
                public <T extends Buffer> void setPixels(int n, int n2, int n3, int n4, PixelFormat<T> pixelFormat, T t, int n5) {
                    if (pixelFormat == null) {
                        throw new NullPointerException("PixelFormat cannot be null");
                    }
                    if (t == null) {
                        throw new NullPointerException("Buffer cannot be null");
                    }
                    PlatformImage platformImage = WritableImage.this.getWritablePlatformImage();
                    platformImage.setPixels(n, n2, n3, n4, pixelFormat, t, n5);
                    WritableImage.this.pixelsDirty();
                }

                @Override
                public void setPixels(int n, int n2, int n3, int n4, PixelFormat<ByteBuffer> pixelFormat, byte[] byArray, int n5, int n6) {
                    if (pixelFormat == null) {
                        throw new NullPointerException("PixelFormat cannot be null");
                    }
                    if (byArray == null) {
                        throw new NullPointerException("Buffer cannot be null");
                    }
                    PlatformImage platformImage = WritableImage.this.getWritablePlatformImage();
                    platformImage.setPixels(n, n2, n3, n4, pixelFormat, byArray, n5, n6);
                    WritableImage.this.pixelsDirty();
                }

                @Override
                public void setPixels(int n, int n2, int n3, int n4, PixelFormat<IntBuffer> pixelFormat, int[] nArray, int n5, int n6) {
                    if (pixelFormat == null) {
                        throw new NullPointerException("PixelFormat cannot be null");
                    }
                    if (nArray == null) {
                        throw new NullPointerException("Buffer cannot be null");
                    }
                    PlatformImage platformImage = WritableImage.this.getWritablePlatformImage();
                    platformImage.setPixels(n, n2, n3, n4, pixelFormat, nArray, n5, n6);
                    WritableImage.this.pixelsDirty();
                }

                @Override
                public void setPixels(int n, int n2, int n3, int n4, PixelReader pixelReader, int n5, int n6) {
                    if (pixelReader == null) {
                        throw new NullPointerException("Reader cannot be null");
                    }
                    PlatformImage platformImage = WritableImage.this.getWritablePlatformImage();
                    platformImage.setPixels(n, n2, n3, n4, pixelReader, n5, n6);
                    WritableImage.this.pixelsDirty();
                }
            };
        }
        return this.writer;
    }

    private void loadTkImage(Object object) {
        if (!(object instanceof ImageLoader)) {
            throw new IllegalArgumentException("Unrecognized image loader: " + object);
        }
        ImageLoader imageLoader = (ImageLoader)object;
        if (imageLoader.getWidth() != (double)((int)this.getWidth()) || imageLoader.getHeight() != (double)((int)this.getHeight())) {
            throw new IllegalArgumentException("Size of loader does not match size of image");
        }
        super.setPlatformImage(imageLoader.getFrame(0));
        this.tkImageLoader = imageLoader;
    }

    private Object getTkImageLoader() {
        return this.tkImageLoader;
    }

    static {
        Toolkit.setWritableImageAccessor(new Toolkit.WritableImageAccessor(){

            @Override
            public void loadTkImage(WritableImage writableImage, Object object) {
                writableImage.loadTkImage(object);
            }

            @Override
            public Object getTkImageLoader(WritableImage writableImage) {
                return writableImage.getTkImageLoader();
            }
        });
    }
}

