/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import javafx.beans.NamedArg;

public final class BorderWidths {
    public static final double AUTO = -1.0;
    public static final BorderWidths DEFAULT = new BorderWidths(1.0, 1.0, 1.0, 1.0, false, false, false, false);
    public static final BorderWidths EMPTY = new BorderWidths(0.0, 0.0, 0.0, 0.0, false, false, false, false);
    public static final BorderWidths FULL = new BorderWidths(1.0, 1.0, 1.0, 1.0, true, true, true, true);
    final double top;
    final double right;
    final double bottom;
    final double left;
    final boolean topAsPercentage;
    final boolean rightAsPercentage;
    final boolean bottomAsPercentage;
    final boolean leftAsPercentage;
    private final int hash;

    public final double getTop() {
        return this.top;
    }

    public final double getRight() {
        return this.right;
    }

    public final double getBottom() {
        return this.bottom;
    }

    public final double getLeft() {
        return this.left;
    }

    public final boolean isTopAsPercentage() {
        return this.topAsPercentage;
    }

    public final boolean isRightAsPercentage() {
        return this.rightAsPercentage;
    }

    public final boolean isBottomAsPercentage() {
        return this.bottomAsPercentage;
    }

    public final boolean isLeftAsPercentage() {
        return this.leftAsPercentage;
    }

    public BorderWidths(@NamedArg(value="width") double d) {
        this(d, d, d, d, false, false, false, false);
    }

    public BorderWidths(@NamedArg(value="top") double d, @NamedArg(value="right") double d2, @NamedArg(value="bottom") double d3, @NamedArg(value="left") double d4) {
        this(d, d2, d3, d4, false, false, false, false);
    }

    public BorderWidths(@NamedArg(value="top") double d, @NamedArg(value="right") double d2, @NamedArg(value="bottom") double d3, @NamedArg(value="left") double d4, @NamedArg(value="topAsPercentage") boolean bl, @NamedArg(value="rightAsPercentage") boolean bl2, @NamedArg(value="bottomAsPercentage") boolean bl3, @NamedArg(value="leftAsPercentage") boolean bl4) {
        if (d != -1.0 && d < 0.0 || d2 != -1.0 && d2 < 0.0 || d3 != -1.0 && d3 < 0.0 || d4 != -1.0 && d4 < 0.0) {
            throw new IllegalArgumentException("None of the widths can be < 0");
        }
        this.top = d;
        this.right = d2;
        this.bottom = d3;
        this.left = d4;
        this.topAsPercentage = bl;
        this.rightAsPercentage = bl2;
        this.bottomAsPercentage = bl3;
        this.leftAsPercentage = bl4;
        long l = this.top != 0.0 ? Double.doubleToLongBits(this.top) : 0L;
        int n = (int)(l ^ l >>> 32);
        l = this.right != 0.0 ? Double.doubleToLongBits(this.right) : 0L;
        n = 31 * n + (int)(l ^ l >>> 32);
        l = this.bottom != 0.0 ? Double.doubleToLongBits(this.bottom) : 0L;
        n = 31 * n + (int)(l ^ l >>> 32);
        l = this.left != 0.0 ? Double.doubleToLongBits(this.left) : 0L;
        n = 31 * n + (int)(l ^ l >>> 32);
        n = 31 * n + (this.topAsPercentage ? 1 : 0);
        n = 31 * n + (this.rightAsPercentage ? 1 : 0);
        n = 31 * n + (this.bottomAsPercentage ? 1 : 0);
        this.hash = n = 31 * n + (this.leftAsPercentage ? 1 : 0);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        BorderWidths borderWidths = (BorderWidths)object;
        if (this.hash != borderWidths.hash) {
            return false;
        }
        if (Double.compare(borderWidths.bottom, this.bottom) != 0) {
            return false;
        }
        if (this.bottomAsPercentage != borderWidths.bottomAsPercentage) {
            return false;
        }
        if (Double.compare(borderWidths.left, this.left) != 0) {
            return false;
        }
        if (this.leftAsPercentage != borderWidths.leftAsPercentage) {
            return false;
        }
        if (Double.compare(borderWidths.right, this.right) != 0) {
            return false;
        }
        if (this.rightAsPercentage != borderWidths.rightAsPercentage) {
            return false;
        }
        if (Double.compare(borderWidths.top, this.top) != 0) {
            return false;
        }
        return this.topAsPercentage == borderWidths.topAsPercentage;
    }

    public int hashCode() {
        return this.hash;
    }
}

