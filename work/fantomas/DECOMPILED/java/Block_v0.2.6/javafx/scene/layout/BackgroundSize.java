/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import javafx.beans.NamedArg;

public final class BackgroundSize {
    public static final double AUTO = -1.0;
    public static final BackgroundSize DEFAULT = new BackgroundSize(-1.0, -1.0, true, true, false, false);
    final double width;
    final double height;
    final boolean widthAsPercentage;
    final boolean heightAsPercentage;
    final boolean contain;
    final boolean cover;
    private final int hash;

    public final double getWidth() {
        return this.width;
    }

    public final double getHeight() {
        return this.height;
    }

    public final boolean isWidthAsPercentage() {
        return this.widthAsPercentage;
    }

    public final boolean isHeightAsPercentage() {
        return this.heightAsPercentage;
    }

    public final boolean isContain() {
        return this.contain;
    }

    public final boolean isCover() {
        return this.cover;
    }

    public BackgroundSize(@NamedArg(value="width") double d, @NamedArg(value="height") double d2, @NamedArg(value="widthAsPercentage") boolean bl, @NamedArg(value="heightAsPercentage") boolean bl2, @NamedArg(value="contain") boolean bl3, @NamedArg(value="cover") boolean bl4) {
        if (d < 0.0 && d != -1.0) {
            throw new IllegalArgumentException("Width cannot be < 0, except when AUTO");
        }
        if (d2 < 0.0 && d2 != -1.0) {
            throw new IllegalArgumentException("Height cannot be < 0, except when AUTO");
        }
        this.width = d;
        this.height = d2;
        this.widthAsPercentage = bl;
        this.heightAsPercentage = bl2;
        this.contain = bl3;
        this.cover = bl4;
        int n = this.widthAsPercentage ? 1 : 0;
        n = 31 * n + (this.heightAsPercentage ? 1 : 0);
        long l = this.width != 0.0 ? Double.doubleToLongBits(this.width) : 0L;
        n = 31 * n + (int)(l ^ l >>> 32);
        l = this.height != 0.0 ? Double.doubleToLongBits(this.height) : 0L;
        n = 31 * n + (int)(l ^ l >>> 32);
        n = 31 * n + (this.cover ? 1 : 0);
        this.hash = n = 31 * n + (this.contain ? 1 : 0);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        BackgroundSize backgroundSize = (BackgroundSize)object;
        if (this.hash != backgroundSize.hash) {
            return false;
        }
        if (this.contain != backgroundSize.contain) {
            return false;
        }
        if (this.cover != backgroundSize.cover) {
            return false;
        }
        if (Double.compare(backgroundSize.height, this.height) != 0) {
            return false;
        }
        if (this.heightAsPercentage != backgroundSize.heightAsPercentage) {
            return false;
        }
        if (this.widthAsPercentage != backgroundSize.widthAsPercentage) {
            return false;
        }
        return Double.compare(backgroundSize.width, this.width) == 0;
    }

    public int hashCode() {
        return this.hash;
    }
}

