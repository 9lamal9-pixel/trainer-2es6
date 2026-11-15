/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.webkit.prism;

import com.sun.javafx.iio.ImageFrame;
import com.sun.javafx.iio.ImageLoadListener;
import com.sun.javafx.iio.ImageLoader;
import com.sun.javafx.iio.ImageMetadata;
import com.sun.javafx.iio.ImageStorage;
import com.sun.javafx.iio.ImageStorageException;
import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.webkit.prism.PrismImage;
import com.sun.javafx.webkit.prism.WCImageImpl;
import com.sun.webkit.graphics.WCGraphicsManager;
import com.sun.webkit.graphics.WCImage;
import com.sun.webkit.graphics.WCImageDecoder;
import com.sun.webkit.graphics.WCImageFrame;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

final class WCImageDecoderImpl
extends WCImageDecoder {
    private static final PlatformLogger log = PlatformLogger.getLogger(WCImageDecoderImpl.class.getName());
    private Service<ImageFrame[]> loader;
    private int imageWidth = 0;
    private int imageHeight = 0;
    private ImageFrame[] frames;
    private int frameCount = 0;
    private boolean fullDataReceived = false;
    private boolean framesDecoded = false;
    private PrismImage[] images;
    private volatile byte[] data;
    private volatile int dataSize = 0;
    private String fileNameExtension;
    private final ImageLoadListener readerListener = new ImageLoadListener(){

        @Override
        public void imageLoadProgress(ImageLoader imageLoader, float f) {
        }

        @Override
        public void imageLoadWarning(ImageLoader imageLoader, String string) {
        }

        @Override
        public void imageLoadMetaData(ImageLoader imageLoader, ImageMetadata imageMetadata) {
            if (log.isLoggable(PlatformLogger.Level.FINE)) {
                log.fine(String.format("%X Image size %dx%d", this.hashCode(), imageMetadata.imageWidth, imageMetadata.imageHeight));
            }
            if (WCImageDecoderImpl.this.imageWidth < imageMetadata.imageWidth) {
                WCImageDecoderImpl.this.imageWidth = imageMetadata.imageWidth;
            }
            if (WCImageDecoderImpl.this.imageHeight < imageMetadata.imageHeight) {
                WCImageDecoderImpl.this.imageHeight = imageMetadata.imageHeight;
            }
            WCImageDecoderImpl.this.fileNameExtension = imageLoader.getFormatDescription().getExtensions().get(0);
        }
    };
    private static final ThreadLocal<int[]> THREAD_LOCAL_SIZE_ARRAY = new ThreadLocal<int[]>(){

        @Override
        protected int[] initialValue() {
            return new int[2];
        }
    };

    WCImageDecoderImpl() {
    }

    @Override
    protected synchronized void destroy() {
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine(String.format("%X Destroy image decoder", this.hashCode()));
        }
        this.destroyLoader();
        this.frames = null;
        this.images = null;
        this.framesDecoded = false;
    }

    @Override
    protected String getFilenameExtension() {
        return "." + this.fileNameExtension;
    }

    private boolean imageSizeAvilable() {
        return this.imageWidth > 0 && this.imageHeight > 0;
    }

    @Override
    protected void addImageData(byte[] byArray) {
        if (byArray != null) {
            this.fullDataReceived = false;
            if (this.data == null) {
                this.data = Arrays.copyOf(byArray, byArray.length * 2);
                this.dataSize = byArray.length;
            } else {
                int n = this.dataSize + byArray.length;
                if (n > this.data.length) {
                    this.resizeDataArray(Math.max(n, this.data.length * 2));
                }
                System.arraycopy(byArray, 0, this.data, this.dataSize, byArray.length);
                this.dataSize = n;
            }
            if (!this.imageSizeAvilable()) {
                this.loadFrames();
            }
        } else if (this.data != null && !this.fullDataReceived) {
            if (this.data.length > this.dataSize) {
                this.resizeDataArray(this.dataSize);
            }
            this.fullDataReceived = true;
        }
    }

    private void destroyLoader() {
        if (this.loader != null) {
            this.loader.cancel();
            this.loader = null;
        }
    }

    private void startLoader() {
        if (this.loader == null) {
            this.loader = new Service<ImageFrame[]>(){

                @Override
                protected Task<ImageFrame[]> createTask() {
                    return new Task<ImageFrame[]>(){

                        @Override
                        protected ImageFrame[] call() throws Exception {
                            return WCImageDecoderImpl.this.loadFrames();
                        }
                    };
                }
            };
            this.loader.valueProperty().addListener((observableValue, imageFrameArray, imageFrameArray2) -> {
                if (imageFrameArray2 != null && this.loader != null) {
                    this.setFrames((ImageFrame[])imageFrameArray2);
                }
            });
        }
        if (!this.loader.isRunning()) {
            this.loader.restart();
        }
    }

    private void resizeDataArray(int n) {
        byte[] byArray = new byte[n];
        System.arraycopy(this.data, 0, byArray, 0, this.dataSize);
        this.data = byArray;
    }

    @Override
    protected void loadFromResource(String string) {
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine(String.format("%X Load image from resource '%s'", this.hashCode(), string));
        }
        String string2 = WCGraphicsManager.getResourceName(string);
        InputStream inputStream = this.getClass().getResourceAsStream(string2);
        if (inputStream == null) {
            if (log.isLoggable(PlatformLogger.Level.FINE)) {
                log.fine(String.format("%X Unable to open resource '%s'", this.hashCode(), string2));
            }
            return;
        }
        this.setFrames(this.loadFrames(inputStream));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private synchronized ImageFrame[] loadFrames(InputStream inputStream) {
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine(String.format("%X Decoding frames", this.hashCode()));
        }
        try {
            ImageFrame[] imageFrameArray = ImageStorage.loadAll(inputStream, this.readerListener, 0.0, 0.0, true, 1.0f, false);
            return imageFrameArray;
        }
        catch (ImageStorageException imageStorageException) {
            ImageFrame[] imageFrameArray = null;
            return imageFrameArray;
        }
        finally {
            try {
                inputStream.close();
            }
            catch (IOException iOException) {}
        }
    }

    private ImageFrame[] loadFrames() {
        return this.loadFrames(new ByteArrayInputStream(this.data, 0, this.dataSize));
    }

    @Override
    protected int[] getImageSize() {
        int[] nArray = THREAD_LOCAL_SIZE_ARRAY.get();
        nArray[0] = this.imageWidth;
        nArray[1] = this.imageHeight;
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine(String.format("%X image size = %dx%d", this.hashCode(), nArray[0], nArray[1]));
        }
        return nArray;
    }

    private synchronized void setFrames(ImageFrame[] imageFrameArray) {
        this.frames = imageFrameArray;
        this.images = null;
        this.frameCount = imageFrameArray == null ? 0 : imageFrameArray.length;
    }

    @Override
    protected int getFrameCount() {
        if (this.fullDataReceived) {
            this.getImageFrame(0);
        }
        return this.frameCount;
    }

    @Override
    protected synchronized WCImageFrame getFrame(int n) {
        ImageFrame imageFrame = this.getImageFrame(n);
        if (imageFrame != null) {
            Object object;
            if (log.isLoggable(PlatformLogger.Level.FINE)) {
                object = imageFrame.getImageType();
                log.fine(String.format("%X getFrame(%d): image type = %s", this.hashCode(), n, object));
            }
            object = this.getPrismImage(n, imageFrame);
            return new Frame((WCImage)object, this.fileNameExtension);
        }
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine(String.format("%X FAILED getFrame(%d)", this.hashCode(), n));
        }
        return null;
    }

    private synchronized ImageMetadata getFrameMetadata(int n) {
        return this.frames != null && this.frames.length > n && this.frames[n] != null ? this.frames[n].getMetadata() : null;
    }

    @Override
    protected int getFrameDuration(int n) {
        int n2;
        ImageMetadata imageMetadata = this.getFrameMetadata(n);
        int n3 = n2 = imageMetadata == null || imageMetadata.delayTime == null ? 0 : imageMetadata.delayTime;
        if (n2 < 11) {
            n2 = 100;
        }
        return n2;
    }

    @Override
    protected int[] getFrameSize(int n) {
        ImageMetadata imageMetadata = this.getFrameMetadata(n);
        if (imageMetadata == null) {
            return null;
        }
        int[] nArray = THREAD_LOCAL_SIZE_ARRAY.get();
        nArray[0] = imageMetadata.imageWidth;
        nArray[1] = imageMetadata.imageHeight;
        return nArray;
    }

    @Override
    protected synchronized boolean getFrameCompleteStatus(int n) {
        return this.getFrameMetadata(n) != null && this.framesDecoded;
    }

    private synchronized ImageFrame getImageFrame(int n) {
        if (!this.fullDataReceived) {
            this.startLoader();
        } else if (this.fullDataReceived && !this.framesDecoded) {
            this.destroyLoader();
            this.setFrames(this.loadFrames());
            this.framesDecoded = true;
        }
        return n >= 0 && this.frames != null && this.frames.length > n ? this.frames[n] : null;
    }

    private synchronized PrismImage getPrismImage(int n, ImageFrame imageFrame) {
        if (this.images == null) {
            this.images = new PrismImage[this.frames.length];
        }
        if (this.images[n] == null) {
            this.images[n] = new WCImageImpl(imageFrame);
        }
        return this.images[n];
    }

    private static final class Frame
    extends WCImageFrame {
        private WCImage image;

        private Frame(WCImage wCImage, String string) {
            this.image = wCImage;
            this.image.setFileExtension(string);
        }

        @Override
        public WCImage getFrame() {
            return this.image;
        }

        @Override
        public int[] getSize() {
            int[] nArray = (int[])THREAD_LOCAL_SIZE_ARRAY.get();
            nArray[0] = this.image.getWidth();
            nArray[1] = this.image.getHeight();
            return nArray;
        }

        @Override
        protected void destroyDecodedData() {
            this.image = null;
        }
    }
}

