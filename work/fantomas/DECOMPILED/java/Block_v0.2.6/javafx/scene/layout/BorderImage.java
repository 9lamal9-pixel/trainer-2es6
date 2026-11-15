/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import com.sun.javafx.scene.layout.region.BorderImageSlices;
import javafx.beans.NamedArg;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderRepeat;
import javafx.scene.layout.BorderWidths;

public class BorderImage {
    final Image image;
    final BorderRepeat repeatX;
    final BorderRepeat repeatY;
    final BorderWidths widths;
    final BorderWidths slices;
    final boolean filled;
    final Insets insets;
    final Insets innerEdge;
    final Insets outerEdge;
    private final int hash;

    public final Image getImage() {
        return this.image;
    }

    public final BorderRepeat getRepeatX() {
        return this.repeatX;
    }

    public final BorderRepeat getRepeatY() {
        return this.repeatY;
    }

    public final BorderWidths getWidths() {
        return this.widths;
    }

    public final BorderWidths getSlices() {
        return this.slices;
    }

    public final boolean isFilled() {
        return this.filled;
    }

    public final Insets getInsets() {
        return this.insets;
    }

    public BorderImage(@NamedArg(value="image") Image image, @NamedArg(value="widths") BorderWidths borderWidths, @NamedArg(value="insets") Insets insets, @NamedArg(value="slices") BorderWidths borderWidths2, @NamedArg(value="filled") boolean bl, @NamedArg(value="repeatX") BorderRepeat borderRepeat, @NamedArg(value="repeatY") BorderRepeat borderRepeat2) {
        if (image == null) {
            throw new NullPointerException("Image cannot be null");
        }
        this.image = image;
        this.widths = borderWidths == null ? BorderWidths.DEFAULT : borderWidths;
        this.insets = insets == null ? Insets.EMPTY : insets;
        this.slices = borderWidths2 == null ? BorderImageSlices.DEFAULT.widths : borderWidths2;
        this.filled = bl;
        this.repeatX = borderRepeat == null ? BorderRepeat.STRETCH : borderRepeat;
        this.repeatY = borderRepeat2 == null ? this.repeatX : borderRepeat2;
        this.outerEdge = new Insets(Math.max(0.0, -this.insets.getTop()), Math.max(0.0, -this.insets.getRight()), Math.max(0.0, -this.insets.getBottom()), Math.max(0.0, -this.insets.getLeft()));
        this.innerEdge = new Insets(this.insets.getTop() + this.widths.getTop(), this.insets.getRight() + this.widths.getRight(), this.insets.getBottom() + this.widths.getBottom(), this.insets.getLeft() + this.widths.getLeft());
        int n = this.image.hashCode();
        n = 31 * n + this.widths.hashCode();
        n = 31 * n + this.slices.hashCode();
        n = 31 * n + this.repeatX.hashCode();
        n = 31 * n + this.repeatY.hashCode();
        this.hash = n = 31 * n + (this.filled ? 1 : 0);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        BorderImage borderImage = (BorderImage)object;
        if (this.hash != borderImage.hash) {
            return false;
        }
        if (this.filled != borderImage.filled) {
            return false;
        }
        if (!this.image.equals(borderImage.image)) {
            return false;
        }
        if (this.repeatX != borderImage.repeatX) {
            return false;
        }
        if (this.repeatY != borderImage.repeatY) {
            return false;
        }
        if (!this.slices.equals(borderImage.slices)) {
            return false;
        }
        return this.widths.equals(borderImage.widths);
    }

    public int hashCode() {
        return this.hash;
    }
}

