/*
 * Decompiled with CFR 0.152.
 */
package javafx.geometry;

import javafx.beans.NamedArg;
import javafx.geometry.Point3D;

public class Point2D {
    public static final Point2D ZERO = new Point2D(0.0, 0.0);
    private double x;
    private double y;
    private int hash = 0;

    public final double getX() {
        return this.x;
    }

    public final double getY() {
        return this.y;
    }

    public Point2D(@NamedArg(value="x") double d, @NamedArg(value="y") double d2) {
        this.x = d;
        this.y = d2;
    }

    public double distance(double d, double d2) {
        double d3 = this.getX() - d;
        double d4 = this.getY() - d2;
        return Math.sqrt(d3 * d3 + d4 * d4);
    }

    public double distance(Point2D point2D) {
        return this.distance(point2D.getX(), point2D.getY());
    }

    public Point2D add(double d, double d2) {
        return new Point2D(this.getX() + d, this.getY() + d2);
    }

    public Point2D add(Point2D point2D) {
        return this.add(point2D.getX(), point2D.getY());
    }

    public Point2D subtract(double d, double d2) {
        return new Point2D(this.getX() - d, this.getY() - d2);
    }

    public Point2D multiply(double d) {
        return new Point2D(this.getX() * d, this.getY() * d);
    }

    public Point2D subtract(Point2D point2D) {
        return this.subtract(point2D.getX(), point2D.getY());
    }

    public Point2D normalize() {
        double d = this.magnitude();
        if (d == 0.0) {
            return new Point2D(0.0, 0.0);
        }
        return new Point2D(this.getX() / d, this.getY() / d);
    }

    public Point2D midpoint(double d, double d2) {
        return new Point2D(d + (this.getX() - d) / 2.0, d2 + (this.getY() - d2) / 2.0);
    }

    public Point2D midpoint(Point2D point2D) {
        return this.midpoint(point2D.getX(), point2D.getY());
    }

    public double angle(double d, double d2) {
        double d3;
        double d4 = this.getX();
        double d5 = (d4 * d + (d3 = this.getY()) * d2) / Math.sqrt((d4 * d4 + d3 * d3) * (d * d + d2 * d2));
        if (d5 > 1.0) {
            return 0.0;
        }
        if (d5 < -1.0) {
            return 180.0;
        }
        return Math.toDegrees(Math.acos(d5));
    }

    public double angle(Point2D point2D) {
        return this.angle(point2D.getX(), point2D.getY());
    }

    public double angle(Point2D point2D, Point2D point2D2) {
        double d;
        double d2 = this.getX();
        double d3 = this.getY();
        double d4 = point2D.getX() - d2;
        double d5 = point2D.getY() - d3;
        double d6 = point2D2.getX() - d2;
        double d7 = (d4 * d6 + d5 * (d = point2D2.getY() - d3)) / Math.sqrt((d4 * d4 + d5 * d5) * (d6 * d6 + d * d));
        if (d7 > 1.0) {
            return 0.0;
        }
        if (d7 < -1.0) {
            return 180.0;
        }
        return Math.toDegrees(Math.acos(d7));
    }

    public double magnitude() {
        double d = this.getX();
        double d2 = this.getY();
        return Math.sqrt(d * d + d2 * d2);
    }

    public double dotProduct(double d, double d2) {
        return this.getX() * d + this.getY() * d2;
    }

    public double dotProduct(Point2D point2D) {
        return this.dotProduct(point2D.getX(), point2D.getY());
    }

    public Point3D crossProduct(double d, double d2) {
        double d3 = this.getX();
        double d4 = this.getY();
        return new Point3D(0.0, 0.0, d3 * d2 - d4 * d);
    }

    public Point3D crossProduct(Point2D point2D) {
        return this.crossProduct(point2D.getX(), point2D.getY());
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof Point2D) {
            Point2D point2D = (Point2D)object;
            return this.getX() == point2D.getX() && this.getY() == point2D.getY();
        }
        return false;
    }

    public int hashCode() {
        if (this.hash == 0) {
            long l = 7L;
            l = 31L * l + Double.doubleToLongBits(this.getX());
            l = 31L * l + Double.doubleToLongBits(this.getY());
            this.hash = (int)(l ^ l >> 32);
        }
        return this.hash;
    }

    public String toString() {
        return "Point2D [x = " + this.getX() + ", y = " + this.getY() + "]";
    }
}

