/*
 * Decompiled with CFR 0.152.
 */
package javafx.geometry;

import javafx.beans.NamedArg;

public class Point3D {
    public static final Point3D ZERO = new Point3D(0.0, 0.0, 0.0);
    private double x;
    private double y;
    private double z;
    private int hash = 0;

    public final double getX() {
        return this.x;
    }

    public final double getY() {
        return this.y;
    }

    public final double getZ() {
        return this.z;
    }

    public Point3D(@NamedArg(value="x") double d, @NamedArg(value="y") double d2, @NamedArg(value="z") double d3) {
        this.x = d;
        this.y = d2;
        this.z = d3;
    }

    public double distance(double d, double d2, double d3) {
        double d4 = this.getX() - d;
        double d5 = this.getY() - d2;
        double d6 = this.getZ() - d3;
        return Math.sqrt(d4 * d4 + d5 * d5 + d6 * d6);
    }

    public double distance(Point3D point3D) {
        return this.distance(point3D.getX(), point3D.getY(), point3D.getZ());
    }

    public Point3D add(double d, double d2, double d3) {
        return new Point3D(this.getX() + d, this.getY() + d2, this.getZ() + d3);
    }

    public Point3D add(Point3D point3D) {
        return this.add(point3D.getX(), point3D.getY(), point3D.getZ());
    }

    public Point3D subtract(double d, double d2, double d3) {
        return new Point3D(this.getX() - d, this.getY() - d2, this.getZ() - d3);
    }

    public Point3D subtract(Point3D point3D) {
        return this.subtract(point3D.getX(), point3D.getY(), point3D.getZ());
    }

    public Point3D multiply(double d) {
        return new Point3D(this.getX() * d, this.getY() * d, this.getZ() * d);
    }

    public Point3D normalize() {
        double d = this.magnitude();
        if (d == 0.0) {
            return new Point3D(0.0, 0.0, 0.0);
        }
        return new Point3D(this.getX() / d, this.getY() / d, this.getZ() / d);
    }

    public Point3D midpoint(double d, double d2, double d3) {
        return new Point3D(d + (this.getX() - d) / 2.0, d2 + (this.getY() - d2) / 2.0, d3 + (this.getZ() - d3) / 2.0);
    }

    public Point3D midpoint(Point3D point3D) {
        return this.midpoint(point3D.getX(), point3D.getY(), point3D.getZ());
    }

    public double angle(double d, double d2, double d3) {
        double d4;
        double d5;
        double d6 = this.getX();
        double d7 = (d6 * d + (d5 = this.getY()) * d2 + (d4 = this.getZ()) * d3) / Math.sqrt((d6 * d6 + d5 * d5 + d4 * d4) * (d * d + d2 * d2 + d3 * d3));
        if (d7 > 1.0) {
            return 0.0;
        }
        if (d7 < -1.0) {
            return 180.0;
        }
        return Math.toDegrees(Math.acos(d7));
    }

    public double angle(Point3D point3D) {
        return this.angle(point3D.getX(), point3D.getY(), point3D.getZ());
    }

    public double angle(Point3D point3D, Point3D point3D2) {
        double d;
        double d2;
        double d3 = this.getX();
        double d4 = this.getY();
        double d5 = this.getZ();
        double d6 = point3D.getX() - d3;
        double d7 = point3D.getY() - d4;
        double d8 = point3D.getZ() - d5;
        double d9 = point3D2.getX() - d3;
        double d10 = (d6 * d9 + d7 * (d2 = point3D2.getY() - d4) + d8 * (d = point3D2.getZ() - d5)) / Math.sqrt((d6 * d6 + d7 * d7 + d8 * d8) * (d9 * d9 + d2 * d2 + d * d));
        if (d10 > 1.0) {
            return 0.0;
        }
        if (d10 < -1.0) {
            return 180.0;
        }
        return Math.toDegrees(Math.acos(d10));
    }

    public double magnitude() {
        double d = this.getX();
        double d2 = this.getY();
        double d3 = this.getZ();
        return Math.sqrt(d * d + d2 * d2 + d3 * d3);
    }

    public double dotProduct(double d, double d2, double d3) {
        return this.getX() * d + this.getY() * d2 + this.getZ() * d3;
    }

    public double dotProduct(Point3D point3D) {
        return this.dotProduct(point3D.getX(), point3D.getY(), point3D.getZ());
    }

    public Point3D crossProduct(double d, double d2, double d3) {
        double d4 = this.getX();
        double d5 = this.getY();
        double d6 = this.getZ();
        return new Point3D(d5 * d3 - d6 * d2, d6 * d - d4 * d3, d4 * d2 - d5 * d);
    }

    public Point3D crossProduct(Point3D point3D) {
        return this.crossProduct(point3D.getX(), point3D.getY(), point3D.getZ());
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Point3D) {
            Point3D point3D = (Point3D)object;
            return this.getX() == point3D.getX() && this.getY() == point3D.getY() && this.getZ() == point3D.getZ();
        }
        return false;
    }

    public int hashCode() {
        if (this.hash == 0) {
            long l = 7L;
            l = 31L * l + Double.doubleToLongBits(this.getX());
            l = 31L * l + Double.doubleToLongBits(this.getY());
            l = 31L * l + Double.doubleToLongBits(this.getZ());
            this.hash = (int)(l ^ l >> 32);
        }
        return this.hash;
    }

    public String toString() {
        return "Point3D [x = " + this.getX() + ", y = " + this.getY() + ", z = " + this.getZ() + "]";
    }
}

