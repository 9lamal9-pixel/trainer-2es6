/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene;

import com.sun.javafx.cursor.CursorFrame;
import com.sun.javafx.cursor.ImageCursorFrame;
import com.sun.javafx.tk.Toolkit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.InvalidationListener;
import javafx.beans.NamedArg;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoublePropertyBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectPropertyBase;
import javafx.geometry.Dimension2D;
import javafx.scene.Cursor;
import javafx.scene.image.Image;

public class ImageCursor
extends Cursor {
    private ObjectPropertyImpl<Image> image;
    private DoublePropertyImpl hotspotX;
    private DoublePropertyImpl hotspotY;
    private CursorFrame currentCursorFrame;
    private ImageCursorFrame firstCursorFrame;
    private Map<Object, ImageCursorFrame> otherCursorFrames;
    private int activeCounter;
    private InvalidationListener imageListener;

    public final Image getImage() {
        return this.image == null ? null : this.image.get();
    }

    public final ReadOnlyObjectProperty<Image> imageProperty() {
        return this.imagePropertyImpl();
    }

    private ObjectPropertyImpl<Image> imagePropertyImpl() {
        if (this.image == null) {
            this.image = new ObjectPropertyImpl("image");
        }
        return this.image;
    }

    public final double getHotspotX() {
        return this.hotspotX == null ? 0.0 : this.hotspotX.get();
    }

    public final ReadOnlyDoubleProperty hotspotXProperty() {
        return this.hotspotXPropertyImpl();
    }

    private DoublePropertyImpl hotspotXPropertyImpl() {
        if (this.hotspotX == null) {
            this.hotspotX = new DoublePropertyImpl("hotspotX");
        }
        return this.hotspotX;
    }

    public final double getHotspotY() {
        return this.hotspotY == null ? 0.0 : this.hotspotY.get();
    }

    public final ReadOnlyDoubleProperty hotspotYProperty() {
        return this.hotspotYPropertyImpl();
    }

    private DoublePropertyImpl hotspotYPropertyImpl() {
        if (this.hotspotY == null) {
            this.hotspotY = new DoublePropertyImpl("hotspotY");
        }
        return this.hotspotY;
    }

    public ImageCursor() {
    }

    public ImageCursor(@NamedArg(value="image") Image image) {
        this(image, 0.0, 0.0);
    }

    public ImageCursor(@NamedArg(value="image") Image image, @NamedArg(value="hotspotX") double d, @NamedArg(value="hotspotY") double d2) {
        if (image != null && image.getProgress() < 1.0) {
            DelayedInitialization.applyTo(this, image, d, d2);
        } else {
            this.initialize(image, d, d2);
        }
    }

    public static Dimension2D getBestSize(double d, double d2) {
        return Toolkit.getToolkit().getBestCursorSize((int)d, (int)d2);
    }

    public static int getMaximumColors() {
        return Toolkit.getToolkit().getMaximumCursorColors();
    }

    public static ImageCursor chooseBestCursor(Image[] imageArray, double d, double d2) {
        ImageCursor imageCursor = new ImageCursor();
        if (ImageCursor.needsDelayedInitialization(imageArray)) {
            DelayedInitialization.applyTo(imageCursor, imageArray, d, d2);
        } else {
            imageCursor.initialize(imageArray, d, d2);
        }
        return imageCursor;
    }

    @Override
    CursorFrame getCurrentFrame() {
        if (this.currentCursorFrame != null) {
            return this.currentCursorFrame;
        }
        Image image = this.getImage();
        if (image == null) {
            this.currentCursorFrame = Cursor.DEFAULT.getCurrentFrame();
            return this.currentCursorFrame;
        }
        Object object = Toolkit.getImageAccessor().getPlatformImage(image);
        if (object == null) {
            this.currentCursorFrame = Cursor.DEFAULT.getCurrentFrame();
            return this.currentCursorFrame;
        }
        if (this.firstCursorFrame == null) {
            this.firstCursorFrame = new ImageCursorFrame(object, image.getWidth(), image.getHeight(), this.getHotspotX(), this.getHotspotY());
            this.currentCursorFrame = this.firstCursorFrame;
        } else if (this.firstCursorFrame.getPlatformImage() == object) {
            this.currentCursorFrame = this.firstCursorFrame;
        } else {
            if (this.otherCursorFrames == null) {
                this.otherCursorFrames = new HashMap<Object, ImageCursorFrame>();
            }
            this.currentCursorFrame = this.otherCursorFrames.get(object);
            if (this.currentCursorFrame == null) {
                ImageCursorFrame imageCursorFrame = new ImageCursorFrame(object, image.getWidth(), image.getHeight(), this.getHotspotX(), this.getHotspotY());
                this.otherCursorFrames.put(object, imageCursorFrame);
                this.currentCursorFrame = imageCursorFrame;
            }
        }
        return this.currentCursorFrame;
    }

    private void invalidateCurrentFrame() {
        this.currentCursorFrame = null;
    }

    @Override
    void activate() {
        if (++this.activeCounter == 1) {
            this.bindImage(this.getImage());
            this.invalidateCurrentFrame();
        }
    }

    @Override
    void deactivate() {
        if (--this.activeCounter == 0) {
            this.unbindImage(this.getImage());
        }
    }

    private void initialize(Image[] imageArray, double d, double d2) {
        Dimension2D dimension2D = ImageCursor.getBestSize(1.0, 1.0);
        if (imageArray.length == 0 || dimension2D.getWidth() == 0.0 || dimension2D.getHeight() == 0.0) {
            return;
        }
        if (imageArray.length == 1) {
            this.initialize(imageArray[0], d, d2);
            return;
        }
        Image image = ImageCursor.findBestImage(imageArray);
        double d3 = image.getWidth() / imageArray[0].getWidth();
        double d4 = image.getHeight() / imageArray[0].getHeight();
        this.initialize(image, d * d3, d2 * d4);
    }

    private void initialize(Image image, double d, double d2) {
        Image image2 = this.getImage();
        double d3 = this.getHotspotX();
        double d4 = this.getHotspotY();
        if (image == null || image.getWidth() < 1.0 || image.getHeight() < 1.0) {
            d = 0.0;
            d2 = 0.0;
        } else {
            if (d < 0.0) {
                d = 0.0;
            }
            if (d > image.getWidth() - 1.0) {
                d = image.getWidth() - 1.0;
            }
            if (d2 < 0.0) {
                d2 = 0.0;
            }
            if (d2 > image.getHeight() - 1.0) {
                d2 = image.getHeight() - 1.0;
            }
        }
        this.imagePropertyImpl().store(image);
        this.hotspotXPropertyImpl().store(d);
        this.hotspotYPropertyImpl().store(d2);
        if (image2 != image) {
            if (this.activeCounter > 0) {
                this.unbindImage(image2);
                this.bindImage(image);
            }
            this.invalidateCurrentFrame();
            this.image.fireValueChangedEvent();
        }
        if (d3 != d) {
            this.hotspotX.fireValueChangedEvent();
        }
        if (d4 != d2) {
            this.hotspotY.fireValueChangedEvent();
        }
    }

    private InvalidationListener getImageListener() {
        if (this.imageListener == null) {
            this.imageListener = observable -> this.invalidateCurrentFrame();
        }
        return this.imageListener;
    }

    private void bindImage(Image image) {
        if (image == null) {
            return;
        }
        Toolkit.getImageAccessor().getImageProperty(image).addListener(this.getImageListener());
    }

    private void unbindImage(Image image) {
        if (image == null) {
            return;
        }
        Toolkit.getImageAccessor().getImageProperty(image).removeListener(this.getImageListener());
    }

    private static boolean needsDelayedInitialization(Image[] imageArray) {
        for (Image image : imageArray) {
            if (!(image.getProgress() < 1.0)) continue;
            return true;
        }
        return false;
    }

    /*
     * WARNING - void declaration
     */
    private static Image findBestImage(Image[] imageArray) {
        void var1_5;
        void var1_3;
        double d;
        double d2;
        double d3;
        Dimension2D dimension2D;
        for (Image image : imageArray) {
            Dimension2D dimension2D2 = ImageCursor.getBestSize((int)image.getWidth(), (int)image.getHeight());
            if (dimension2D2.getWidth() != image.getWidth() || dimension2D2.getHeight() != image.getHeight()) continue;
            return image;
        }
        Object var1_2 = null;
        double d4 = Double.MAX_VALUE;
        for (Image image : imageArray) {
            if (!(image.getWidth() > 0.0) || !(image.getHeight() > 0.0)) continue;
            dimension2D = ImageCursor.getBestSize(image.getWidth(), image.getHeight());
            d3 = dimension2D.getWidth() / image.getWidth();
            d2 = dimension2D.getHeight() / image.getHeight();
            if (!(d3 >= 1.0) || !(d2 >= 1.0) || !((d = Math.max(d3, d2)) < d4)) continue;
            Image image2 = image;
            d4 = d;
        }
        if (var1_3 != null) {
            return var1_3;
        }
        for (Image image : imageArray) {
            if (!(image.getWidth() > 0.0) || !(image.getHeight() > 0.0) || !((dimension2D = ImageCursor.getBestSize(image.getWidth(), image.getHeight())).getWidth() > 0.0) || !(dimension2D.getHeight() > 0.0)) continue;
            d3 = dimension2D.getWidth() / image.getWidth();
            if (d3 < 1.0) {
                d3 = 1.0 / d3;
            }
            if ((d2 = dimension2D.getHeight() / image.getHeight()) < 1.0) {
                d2 = 1.0 / d2;
            }
            if (!((d = Math.max(d3, d2)) < d4)) continue;
            Image image3 = image;
            d4 = d;
        }
        if (var1_5 != null) {
            return var1_5;
        }
        return imageArray[0];
    }

    private static final class DelayedInitialization
    implements InvalidationListener {
        private final ImageCursor targetCursor;
        private final Image[] images;
        private final double hotspotX;
        private final double hotspotY;
        private final boolean initAsSingle;
        private int waitForImages;

        private DelayedInitialization(ImageCursor imageCursor, Image[] imageArray, double d, double d2, boolean bl) {
            this.targetCursor = imageCursor;
            this.images = imageArray;
            this.hotspotX = d;
            this.hotspotY = d2;
            this.initAsSingle = bl;
        }

        public static void applyTo(ImageCursor imageCursor, Image[] imageArray, double d, double d2) {
            DelayedInitialization delayedInitialization = new DelayedInitialization(imageCursor, Arrays.copyOf(imageArray, imageArray.length), d, d2, false);
            delayedInitialization.start();
        }

        public static void applyTo(ImageCursor imageCursor, Image image, double d, double d2) {
            DelayedInitialization delayedInitialization = new DelayedInitialization(imageCursor, new Image[]{image}, d, d2, true);
            delayedInitialization.start();
        }

        private void start() {
            for (Image image : this.images) {
                if (!(image.getProgress() < 1.0)) continue;
                ++this.waitForImages;
                image.progressProperty().addListener(this);
            }
        }

        private void cleanupAndFinishInitialization() {
            for (Image image : this.images) {
                image.progressProperty().removeListener(this);
            }
            if (this.initAsSingle) {
                this.targetCursor.initialize(this.images[0], this.hotspotX, this.hotspotY);
            } else {
                this.targetCursor.initialize(this.images, this.hotspotX, this.hotspotY);
            }
        }

        @Override
        public void invalidated(Observable observable) {
            if (((ReadOnlyDoubleProperty)observable).get() == 1.0 && --this.waitForImages == 0) {
                this.cleanupAndFinishInitialization();
            }
        }
    }

    private final class ObjectPropertyImpl<T>
    extends ReadOnlyObjectPropertyBase<T> {
        private final String name;
        private T value;

        public ObjectPropertyImpl(String string) {
            this.name = string;
        }

        public void store(T t) {
            this.value = t;
        }

        @Override
        public void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }

        @Override
        public T get() {
            return this.value;
        }

        @Override
        public Object getBean() {
            return ImageCursor.this;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }

    private final class DoublePropertyImpl
    extends ReadOnlyDoublePropertyBase {
        private final String name;
        private double value;

        public DoublePropertyImpl(String string) {
            this.name = string;
        }

        public void store(double d) {
            this.value = d;
        }

        @Override
        public void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }

        @Override
        public double get() {
            return this.value;
        }

        @Override
        public Object getBean() {
            return ImageCursor.this;
        }

        @Override
        public String getName() {
            return this.name;
        }
    }
}

