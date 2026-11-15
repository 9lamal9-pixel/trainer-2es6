/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.paint;

import com.sun.javafx.beans.event.AbstractNotifyListener;
import com.sun.javafx.tk.Toolkit;
import javafx.beans.NamedArg;
import javafx.scene.image.Image;
import javafx.scene.paint.Paint;

public final class ImagePattern
extends Paint {
    private Image image;
    private double x;
    private double y;
    private double width = 1.0;
    private double height = 1.0;
    private boolean proportional = true;
    private Object platformPaint;

    public final Image getImage() {
        return this.image;
    }

    public final double getX() {
        return this.x;
    }

    public final double getY() {
        return this.y;
    }

    public final double getWidth() {
        return this.width;
    }

    public final double getHeight() {
        return this.height;
    }

    public final boolean isProportional() {
        return this.proportional;
    }

    @Override
    public final boolean isOpaque() {
        return ((com.sun.prism.paint.ImagePattern)this.acc_getPlatformPaint()).isOpaque();
    }

    public ImagePattern(@NamedArg(value="image") Image image) {
        if (image == null) {
            throw new NullPointerException("Image must be non-null.");
        }
        if (image.getProgress() < 1.0) {
            throw new IllegalArgumentException("Image not yet loaded");
        }
        this.image = image;
    }

    public ImagePattern(@NamedArg(value="image") Image image, @NamedArg(value="x") double d, @NamedArg(value="y") double d2, @NamedArg(value="width") double d3, @NamedArg(value="height") double d4, @NamedArg(value="proportional") boolean bl) {
        if (image == null) {
            throw new NullPointerException("Image must be non-null.");
        }
        if (image.getProgress() < 1.0) {
            throw new IllegalArgumentException("Image not yet loaded");
        }
        this.image = image;
        this.x = d;
        this.y = d2;
        this.width = d3;
        this.height = d4;
        this.proportional = bl;
    }

    @Override
    boolean acc_isMutable() {
        return Toolkit.getImageAccessor().isAnimation(this.image);
    }

    @Override
    void acc_addListener(AbstractNotifyListener abstractNotifyListener) {
        Toolkit.getImageAccessor().getImageProperty(this.image).addListener(abstractNotifyListener);
    }

    @Override
    void acc_removeListener(AbstractNotifyListener abstractNotifyListener) {
        Toolkit.getImageAccessor().getImageProperty(this.image).removeListener(abstractNotifyListener);
    }

    @Override
    Object acc_getPlatformPaint() {
        if (this.acc_isMutable() || this.platformPaint == null) {
            this.platformPaint = Toolkit.getToolkit().getPaint(this);
            assert (this.platformPaint != null);
        }
        return this.platformPaint;
    }
}

