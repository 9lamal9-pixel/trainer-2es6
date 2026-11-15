/*
 * Decompiled with CFR 0.152.
 */
package javafx.geometry;

import javafx.beans.NamedArg;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Point3D;

public class BoundingBox
extends Bounds {
    private int hash = 0;

    public BoundingBox(@NamedArg(value="minX") double d, @NamedArg(value="minY") double d2, @NamedArg(value="minZ") double d3, @NamedArg(value="width") double d4, @NamedArg(value="height") double d5, @NamedArg(value="depth") double d6) {
        super(d, d2, d3, d4, d5, d6);
    }

    public BoundingBox(@NamedArg(value="minX") double d, @NamedArg(value="minY") double d2, @NamedArg(value="width") double d3, @NamedArg(value="height") double d4) {
        super(d, d2, 0.0, d3, d4, 0.0);
    }

    @Override
    public boolean isEmpty() {
        return this.getMaxX() < this.getMinX() || this.getMaxY() < this.getMinY() || this.getMaxZ() < this.getMinZ();
    }

    @Override
    public boolean contains(Point2D point2D) {
        if (point2D == null) {
            return false;
        }
        return this.contains(point2D.getX(), point2D.getY(), 0.0);
    }

    @Override
    public boolean contains(Point3D point3D) {
        if (point3D == null) {
            return false;
        }
        return this.contains(point3D.getX(), point3D.getY(), point3D.getZ());
    }

    @Override
    public boolean contains(double d, double d2) {
        return this.contains(d, d2, 0.0);
    }

    @Override
    public boolean contains(double d, double d2, double d3) {
        if (this.isEmpty()) {
            return false;
        }
        return d >= this.getMinX() && d <= this.getMaxX() && d2 >= this.getMinY() && d2 <= this.getMaxY() && d3 >= this.getMinZ() && d3 <= this.getMaxZ();
    }

    @Override
    public boolean contains(Bounds bounds) {
        if (bounds == null || bounds.isEmpty()) {
            return false;
        }
        return this.contains(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ(), bounds.getWidth(), bounds.getHeight(), bounds.getDepth());
    }

    @Override
    public boolean contains(double d, double d2, double d3, double d4) {
        return this.contains(d, d2) && this.contains(d + d3, d2 + d4);
    }

    @Override
    public boolean contains(double d, double d2, double d3, double d4, double d5, double d6) {
        return this.contains(d, d2, d3) && this.contains(d + d4, d2 + d5, d3 + d6);
    }

    @Override
    public boolean intersects(Bounds bounds) {
        if (bounds == null || bounds.isEmpty()) {
            return false;
        }
        return this.intersects(bounds.getMinX(), bounds.getMinY(), bounds.getMinZ(), bounds.getWidth(), bounds.getHeight(), bounds.getDepth());
    }

    @Override
    public boolean intersects(double d, double d2, double d3, double d4) {
        return this.intersects(d, d2, 0.0, d3, d4, 0.0);
    }

    @Override
    public boolean intersects(double d, double d2, double d3, double d4, double d5, double d6) {
        if (this.isEmpty() || d4 < 0.0 || d5 < 0.0 || d6 < 0.0) {
            return false;
        }
        return d + d4 >= this.getMinX() && d2 + d5 >= this.getMinY() && d3 + d6 >= this.getMinZ() && d <= this.getMaxX() && d2 <= this.getMaxY() && d3 <= this.getMaxZ();
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof BoundingBox) {
            BoundingBox boundingBox = (BoundingBox)object;
            return this.getMinX() == boundingBox.getMinX() && this.getMinY() == boundingBox.getMinY() && this.getMinZ() == boundingBox.getMinZ() && this.getWidth() == boundingBox.getWidth() && this.getHeight() == boundingBox.getHeight() && this.getDepth() == boundingBox.getDepth();
        }
        return false;
    }

    public int hashCode() {
        if (this.hash == 0) {
            long l = 7L;
            l = 31L * l + Double.doubleToLongBits(this.getMinX());
            l = 31L * l + Double.doubleToLongBits(this.getMinY());
            l = 31L * l + Double.doubleToLongBits(this.getMinZ());
            l = 31L * l + Double.doubleToLongBits(this.getWidth());
            l = 31L * l + Double.doubleToLongBits(this.getHeight());
            l = 31L * l + Double.doubleToLongBits(this.getDepth());
            this.hash = (int)(l ^ l >> 32);
        }
        return this.hash;
    }

    public String toString() {
        return "BoundingBox [minX:" + this.getMinX() + ", minY:" + this.getMinY() + ", minZ:" + this.getMinZ() + ", width:" + this.getWidth() + ", height:" + this.getHeight() + ", depth:" + this.getDepth() + ", maxX:" + this.getMaxX() + ", maxY:" + this.getMaxY() + ", maxZ:" + this.getMaxZ() + "]";
    }
}

