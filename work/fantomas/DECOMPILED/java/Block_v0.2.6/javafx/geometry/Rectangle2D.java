/*
 * Decompiled with CFR 0.152.
 */
package javafx.geometry;

import javafx.beans.NamedArg;
import javafx.geometry.Point2D;

public class Rectangle2D {
    public static final Rectangle2D EMPTY = new Rectangle2D(0.0, 0.0, 0.0, 0.0);
    private double minX;
    private double minY;
    private double width;
    private double height;
    private double maxX;
    private double maxY;
    private int hash = 0;

    public double getMinX() {
        return this.minX;
    }

    public double getMinY() {
        return this.minY;
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }

    public double getMaxX() {
        return this.maxX;
    }

    public double getMaxY() {
        return this.maxY;
    }

    public Rectangle2D(@NamedArg(value="minX") double d, @NamedArg(value="minY") double d2, @NamedArg(value="width") double d3, @NamedArg(value="height") double d4) {
        if (d3 < 0.0 || d4 < 0.0) {
            throw new IllegalArgumentException("Both width and height must be >= 0");
        }
        this.minX = d;
        this.minY = d2;
        this.width = d3;
        this.height = d4;
        this.maxX = d + d3;
        this.maxY = d2 + d4;
    }

    public boolean contains(Point2D point2D) {
        if (point2D == null) {
            return false;
        }
        return this.contains(point2D.getX(), point2D.getY());
    }

    public boolean contains(double d, double d2) {
        return d >= this.minX && d <= this.maxX && d2 >= this.minY && d2 <= this.maxY;
    }

    public boolean contains(Rectangle2D rectangle2D) {
        if (rectangle2D == null) {
            return false;
        }
        return rectangle2D.minX >= this.minX && rectangle2D.minY >= this.minY && rectangle2D.maxX <= this.maxX && rectangle2D.maxY <= this.maxY;
    }

    public boolean contains(double d, double d2, double d3, double d4) {
        return d >= this.minX && d2 >= this.minY && d3 <= this.maxX - d && d4 <= this.maxY - d2;
    }

    public boolean intersects(Rectangle2D rectangle2D) {
        if (rectangle2D == null) {
            return false;
        }
        return rectangle2D.maxX > this.minX && rectangle2D.maxY > this.minY && rectangle2D.minX < this.maxX && rectangle2D.minY < this.maxY;
    }

    public boolean intersects(double d, double d2, double d3, double d4) {
        return d < this.maxX && d2 < this.maxY && d + d3 > this.minX && d2 + d4 > this.minY;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Rectangle2D) {
            Rectangle2D rectangle2D = (Rectangle2D)object;
            return this.minX == rectangle2D.minX && this.minY == rectangle2D.minY && this.width == rectangle2D.width && this.height == rectangle2D.height;
        }
        return false;
    }

    public int hashCode() {
        if (this.hash == 0) {
            long l = 7L;
            l = 31L * l + Double.doubleToLongBits(this.minX);
            l = 31L * l + Double.doubleToLongBits(this.minY);
            l = 31L * l + Double.doubleToLongBits(this.width);
            l = 31L * l + Double.doubleToLongBits(this.height);
            this.hash = (int)(l ^ l >> 32);
        }
        return this.hash;
    }

    public String toString() {
        return "Rectangle2D [minX = " + this.minX + ", minY=" + this.minY + ", maxX=" + this.maxX + ", maxY=" + this.maxY + ", width=" + this.width + ", height=" + this.height + "]";
    }
}

