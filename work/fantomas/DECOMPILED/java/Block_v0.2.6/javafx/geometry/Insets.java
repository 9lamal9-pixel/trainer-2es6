/*
 * Decompiled with CFR 0.152.
 */
package javafx.geometry;

import javafx.beans.NamedArg;

public class Insets {
    public static final Insets EMPTY = new Insets(0.0, 0.0, 0.0, 0.0);
    private double top;
    private double right;
    private double bottom;
    private double left;
    private int hash = 0;

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

    public Insets(@NamedArg(value="top") double d, @NamedArg(value="right") double d2, @NamedArg(value="bottom") double d3, @NamedArg(value="left") double d4) {
        this.top = d;
        this.right = d2;
        this.bottom = d3;
        this.left = d4;
    }

    public Insets(@NamedArg(value="topRightBottomLeft") double d) {
        this.top = d;
        this.right = d;
        this.bottom = d;
        this.left = d;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Insets) {
            Insets insets = (Insets)object;
            return this.top == insets.top && this.right == insets.right && this.bottom == insets.bottom && this.left == insets.left;
        }
        return false;
    }

    public int hashCode() {
        if (this.hash == 0) {
            long l = 17L;
            l = 37L * l + Double.doubleToLongBits(this.top);
            l = 37L * l + Double.doubleToLongBits(this.right);
            l = 37L * l + Double.doubleToLongBits(this.bottom);
            l = 37L * l + Double.doubleToLongBits(this.left);
            this.hash = (int)(l ^ l >> 32);
        }
        return this.hash;
    }

    public String toString() {
        return "Insets [top=" + this.top + ", right=" + this.right + ", bottom=" + this.bottom + ", left=" + this.left + "]";
    }
}

