/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import javafx.beans.NamedArg;

public class CornerRadii {
    public static final CornerRadii EMPTY = new CornerRadii(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false, false, false, false, false, false, false, false);
    private double topLeftHorizontalRadius;
    private double topLeftVerticalRadius;
    private double topRightVerticalRadius;
    private double topRightHorizontalRadius;
    private double bottomRightHorizontalRadius;
    private double bottomRightVerticalRadius;
    private double bottomLeftVerticalRadius;
    private double bottomLeftHorizontalRadius;
    private final boolean topLeftHorizontalRadiusAsPercentage;
    private final boolean topLeftVerticalRadiusAsPercentage;
    private final boolean topRightVerticalRadiusAsPercentage;
    private final boolean topRightHorizontalRadiusAsPercentage;
    private final boolean bottomRightHorizontalRadiusAsPercentage;
    private final boolean bottomRightVerticalRadiusAsPercentage;
    private final boolean bottomLeftVerticalRadiusAsPercentage;
    private final boolean bottomLeftHorizontalRadiusAsPercentage;
    final boolean hasPercentBasedRadii;
    final boolean uniform;
    private final int hash;

    public final double getTopLeftHorizontalRadius() {
        return this.topLeftHorizontalRadius;
    }

    public final double getTopLeftVerticalRadius() {
        return this.topLeftVerticalRadius;
    }

    public final double getTopRightVerticalRadius() {
        return this.topRightVerticalRadius;
    }

    public final double getTopRightHorizontalRadius() {
        return this.topRightHorizontalRadius;
    }

    public final double getBottomRightHorizontalRadius() {
        return this.bottomRightHorizontalRadius;
    }

    public final double getBottomRightVerticalRadius() {
        return this.bottomRightVerticalRadius;
    }

    public final double getBottomLeftVerticalRadius() {
        return this.bottomLeftVerticalRadius;
    }

    public final double getBottomLeftHorizontalRadius() {
        return this.bottomLeftHorizontalRadius;
    }

    public final boolean isTopLeftHorizontalRadiusAsPercentage() {
        return this.topLeftHorizontalRadiusAsPercentage;
    }

    public final boolean isTopLeftVerticalRadiusAsPercentage() {
        return this.topLeftVerticalRadiusAsPercentage;
    }

    public final boolean isTopRightVerticalRadiusAsPercentage() {
        return this.topRightVerticalRadiusAsPercentage;
    }

    public final boolean isTopRightHorizontalRadiusAsPercentage() {
        return this.topRightHorizontalRadiusAsPercentage;
    }

    public final boolean isBottomRightHorizontalRadiusAsPercentage() {
        return this.bottomRightHorizontalRadiusAsPercentage;
    }

    public final boolean isBottomRightVerticalRadiusAsPercentage() {
        return this.bottomRightVerticalRadiusAsPercentage;
    }

    public final boolean isBottomLeftVerticalRadiusAsPercentage() {
        return this.bottomLeftVerticalRadiusAsPercentage;
    }

    public final boolean isBottomLeftHorizontalRadiusAsPercentage() {
        return this.bottomLeftHorizontalRadiusAsPercentage;
    }

    public final boolean isUniform() {
        return this.uniform;
    }

    public CornerRadii(@NamedArg(value="radius") double d) {
        if (d < 0.0) {
            throw new IllegalArgumentException("The radii value may not be < 0");
        }
        this.bottomLeftVerticalRadius = this.bottomLeftHorizontalRadius = d;
        this.bottomRightVerticalRadius = this.bottomLeftHorizontalRadius;
        this.bottomRightHorizontalRadius = this.bottomLeftHorizontalRadius;
        this.topRightHorizontalRadius = this.bottomLeftHorizontalRadius;
        this.topRightVerticalRadius = this.bottomLeftHorizontalRadius;
        this.topLeftVerticalRadius = this.bottomLeftHorizontalRadius;
        this.topLeftHorizontalRadius = this.bottomLeftHorizontalRadius;
        this.bottomLeftHorizontalRadiusAsPercentage = false;
        this.bottomLeftVerticalRadiusAsPercentage = false;
        this.bottomRightVerticalRadiusAsPercentage = false;
        this.bottomRightHorizontalRadiusAsPercentage = false;
        this.topRightHorizontalRadiusAsPercentage = false;
        this.topRightVerticalRadiusAsPercentage = false;
        this.topLeftVerticalRadiusAsPercentage = false;
        this.topLeftHorizontalRadiusAsPercentage = false;
        this.hasPercentBasedRadii = false;
        this.uniform = true;
        this.hash = this.preComputeHash();
    }

    public CornerRadii(@NamedArg(value="radius") double d, @NamedArg(value="asPercent") boolean bl) {
        if (d < 0.0) {
            throw new IllegalArgumentException("The radii value may not be < 0");
        }
        this.bottomLeftVerticalRadius = this.bottomLeftHorizontalRadius = d;
        this.bottomRightVerticalRadius = this.bottomLeftHorizontalRadius;
        this.bottomRightHorizontalRadius = this.bottomLeftHorizontalRadius;
        this.topRightHorizontalRadius = this.bottomLeftHorizontalRadius;
        this.topRightVerticalRadius = this.bottomLeftHorizontalRadius;
        this.topLeftVerticalRadius = this.bottomLeftHorizontalRadius;
        this.topLeftHorizontalRadius = this.bottomLeftHorizontalRadius;
        this.bottomLeftVerticalRadiusAsPercentage = this.bottomLeftHorizontalRadiusAsPercentage = bl;
        this.bottomRightVerticalRadiusAsPercentage = this.bottomLeftHorizontalRadiusAsPercentage;
        this.bottomRightHorizontalRadiusAsPercentage = this.bottomLeftHorizontalRadiusAsPercentage;
        this.topRightHorizontalRadiusAsPercentage = this.bottomLeftHorizontalRadiusAsPercentage;
        this.topRightVerticalRadiusAsPercentage = this.bottomLeftHorizontalRadiusAsPercentage;
        this.topLeftVerticalRadiusAsPercentage = this.bottomLeftHorizontalRadiusAsPercentage;
        this.topLeftHorizontalRadiusAsPercentage = this.bottomLeftHorizontalRadiusAsPercentage;
        this.uniform = true;
        this.hasPercentBasedRadii = bl;
        this.hash = this.preComputeHash();
    }

    public CornerRadii(@NamedArg(value="topLeft") double d, @NamedArg(value="topRight") double d2, @NamedArg(value="bottomRight") double d3, @NamedArg(value="bottomLeft") double d4, @NamedArg(value="asPercent") boolean bl) {
        if (d < 0.0 || d2 < 0.0 || d3 < 0.0 || d4 < 0.0) {
            throw new IllegalArgumentException("No radii value may be < 0");
        }
        this.topLeftHorizontalRadius = this.topLeftVerticalRadius = d;
        this.topRightVerticalRadius = this.topRightHorizontalRadius = d2;
        this.bottomRightHorizontalRadius = this.bottomRightVerticalRadius = d3;
        this.bottomLeftVerticalRadius = this.bottomLeftHorizontalRadius = d4;
        this.bottomLeftVerticalRadiusAsPercentage = this.bottomLeftHorizontalRadiusAsPercentage = bl;
        this.bottomRightVerticalRadiusAsPercentage = this.bottomLeftHorizontalRadiusAsPercentage;
        this.bottomRightHorizontalRadiusAsPercentage = this.bottomLeftHorizontalRadiusAsPercentage;
        this.topRightHorizontalRadiusAsPercentage = this.bottomLeftHorizontalRadiusAsPercentage;
        this.topRightVerticalRadiusAsPercentage = this.bottomLeftHorizontalRadiusAsPercentage;
        this.topLeftVerticalRadiusAsPercentage = this.bottomLeftHorizontalRadiusAsPercentage;
        this.topLeftHorizontalRadiusAsPercentage = this.bottomLeftHorizontalRadiusAsPercentage;
        this.uniform = d == d2 && d == d4 && d == d3;
        this.hasPercentBasedRadii = bl;
        this.hash = this.preComputeHash();
    }

    public CornerRadii(@NamedArg(value="topLeftHorizontalRadius") double d, @NamedArg(value="topLeftVerticalRadius") double d2, @NamedArg(value="topRightVerticalRadius") double d3, @NamedArg(value="topRightHorizontalRadius") double d4, @NamedArg(value="bottomRightHorizontalRadius") double d5, @NamedArg(value="bottomRightVerticalRadius") double d6, @NamedArg(value="bottomLeftVerticalRadius") double d7, @NamedArg(value="bottomLeftHorizontalRadius") double d8, @NamedArg(value="topLeftHorizontalRadiusAsPercent") boolean bl, @NamedArg(value="topLeftVerticalRadiusAsPercent") boolean bl2, @NamedArg(value="topRightVerticalRadiusAsPercent") boolean bl3, @NamedArg(value="topRightHorizontalRadiusAsPercent") boolean bl4, @NamedArg(value="bottomRightHorizontalRadiusAsPercent") boolean bl5, @NamedArg(value="bottomRightVerticalRadiusAsPercent") boolean bl6, @NamedArg(value="bottomLeftVerticalRadiusAsPercent") boolean bl7, @NamedArg(value="bottomLeftHorizontalRadiusAsPercent") boolean bl8) {
        if (d < 0.0 || d2 < 0.0 || d3 < 0.0 || d4 < 0.0 || d5 < 0.0 || d6 < 0.0 || d7 < 0.0 || d8 < 0.0) {
            throw new IllegalArgumentException("No radii value may be < 0");
        }
        this.topLeftHorizontalRadius = d;
        this.topLeftVerticalRadius = d2;
        this.topRightVerticalRadius = d3;
        this.topRightHorizontalRadius = d4;
        this.bottomRightHorizontalRadius = d5;
        this.bottomRightVerticalRadius = d6;
        this.bottomLeftVerticalRadius = d7;
        this.bottomLeftHorizontalRadius = d8;
        this.topLeftHorizontalRadiusAsPercentage = bl;
        this.topLeftVerticalRadiusAsPercentage = bl2;
        this.topRightVerticalRadiusAsPercentage = bl3;
        this.topRightHorizontalRadiusAsPercentage = bl4;
        this.bottomRightHorizontalRadiusAsPercentage = bl5;
        this.bottomRightVerticalRadiusAsPercentage = bl6;
        this.bottomLeftVerticalRadiusAsPercentage = bl7;
        this.bottomLeftHorizontalRadiusAsPercentage = bl8;
        this.hash = this.preComputeHash();
        this.hasPercentBasedRadii = bl || bl2 || bl3 || bl4 || bl5 || bl6 || bl7 || bl8;
        this.uniform = d == d4 && d2 == d3 && d == d5 && d2 == d6 && d == d8 && d2 == d7 && bl == bl4 && bl2 == bl3 && bl == bl5 && bl2 == bl6 && bl == bl8 && bl2 == bl7;
    }

    private int preComputeHash() {
        long l = this.topLeftHorizontalRadius != 0.0 ? Double.doubleToLongBits(this.topLeftHorizontalRadius) : 0L;
        int n = (int)(l ^ l >>> 32);
        l = this.topLeftVerticalRadius != 0.0 ? Double.doubleToLongBits(this.topLeftVerticalRadius) : 0L;
        n = 31 * n + (int)(l ^ l >>> 32);
        l = this.topRightVerticalRadius != 0.0 ? Double.doubleToLongBits(this.topRightVerticalRadius) : 0L;
        n = 31 * n + (int)(l ^ l >>> 32);
        l = this.topRightHorizontalRadius != 0.0 ? Double.doubleToLongBits(this.topRightHorizontalRadius) : 0L;
        n = 31 * n + (int)(l ^ l >>> 32);
        l = this.bottomRightHorizontalRadius != 0.0 ? Double.doubleToLongBits(this.bottomRightHorizontalRadius) : 0L;
        n = 31 * n + (int)(l ^ l >>> 32);
        l = this.bottomRightVerticalRadius != 0.0 ? Double.doubleToLongBits(this.bottomRightVerticalRadius) : 0L;
        n = 31 * n + (int)(l ^ l >>> 32);
        l = this.bottomLeftVerticalRadius != 0.0 ? Double.doubleToLongBits(this.bottomLeftVerticalRadius) : 0L;
        n = 31 * n + (int)(l ^ l >>> 32);
        l = this.bottomLeftHorizontalRadius != 0.0 ? Double.doubleToLongBits(this.bottomLeftHorizontalRadius) : 0L;
        n = 31 * n + (int)(l ^ l >>> 32);
        n = 31 * n + (this.topLeftHorizontalRadiusAsPercentage ? 1 : 0);
        n = 31 * n + (this.topLeftVerticalRadiusAsPercentage ? 1 : 0);
        n = 31 * n + (this.topRightVerticalRadiusAsPercentage ? 1 : 0);
        n = 31 * n + (this.topRightHorizontalRadiusAsPercentage ? 1 : 0);
        n = 31 * n + (this.bottomRightHorizontalRadiusAsPercentage ? 1 : 0);
        n = 31 * n + (this.bottomRightVerticalRadiusAsPercentage ? 1 : 0);
        n = 31 * n + (this.bottomLeftVerticalRadiusAsPercentage ? 1 : 0);
        n = 31 * n + (this.bottomLeftHorizontalRadiusAsPercentage ? 1 : 0);
        n = 31 * n + n;
        return n;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        CornerRadii cornerRadii = (CornerRadii)object;
        if (this.hash != cornerRadii.hash) {
            return false;
        }
        if (Double.compare(cornerRadii.bottomLeftHorizontalRadius, this.bottomLeftHorizontalRadius) != 0) {
            return false;
        }
        if (this.bottomLeftHorizontalRadiusAsPercentage != cornerRadii.bottomLeftHorizontalRadiusAsPercentage) {
            return false;
        }
        if (Double.compare(cornerRadii.bottomLeftVerticalRadius, this.bottomLeftVerticalRadius) != 0) {
            return false;
        }
        if (this.bottomLeftVerticalRadiusAsPercentage != cornerRadii.bottomLeftVerticalRadiusAsPercentage) {
            return false;
        }
        if (Double.compare(cornerRadii.bottomRightVerticalRadius, this.bottomRightVerticalRadius) != 0) {
            return false;
        }
        if (this.bottomRightVerticalRadiusAsPercentage != cornerRadii.bottomRightVerticalRadiusAsPercentage) {
            return false;
        }
        if (Double.compare(cornerRadii.bottomRightHorizontalRadius, this.bottomRightHorizontalRadius) != 0) {
            return false;
        }
        if (this.bottomRightHorizontalRadiusAsPercentage != cornerRadii.bottomRightHorizontalRadiusAsPercentage) {
            return false;
        }
        if (Double.compare(cornerRadii.topLeftVerticalRadius, this.topLeftVerticalRadius) != 0) {
            return false;
        }
        if (this.topLeftVerticalRadiusAsPercentage != cornerRadii.topLeftVerticalRadiusAsPercentage) {
            return false;
        }
        if (Double.compare(cornerRadii.topLeftHorizontalRadius, this.topLeftHorizontalRadius) != 0) {
            return false;
        }
        if (this.topLeftHorizontalRadiusAsPercentage != cornerRadii.topLeftHorizontalRadiusAsPercentage) {
            return false;
        }
        if (Double.compare(cornerRadii.topRightHorizontalRadius, this.topRightHorizontalRadius) != 0) {
            return false;
        }
        if (this.topRightHorizontalRadiusAsPercentage != cornerRadii.topRightHorizontalRadiusAsPercentage) {
            return false;
        }
        if (Double.compare(cornerRadii.topRightVerticalRadius, this.topRightVerticalRadius) != 0) {
            return false;
        }
        return this.topRightVerticalRadiusAsPercentage == cornerRadii.topRightVerticalRadiusAsPercentage;
    }

    public int hashCode() {
        return this.hash;
    }

    public String toString() {
        if (this.isUniform()) {
            return "CornerRadii [uniform radius = " + this.topLeftHorizontalRadius + "]";
        }
        return "CornerRadii [" + (this.topLeftHorizontalRadius == this.topLeftVerticalRadius ? "topLeft=" + this.topLeftHorizontalRadius : "topLeftHorizontalRadius=" + this.topLeftHorizontalRadius + ", topLeftVerticalRadius=" + this.topLeftVerticalRadius) + (this.topRightHorizontalRadius == this.topRightVerticalRadius ? ", topRight=" + this.topRightHorizontalRadius : ", topRightVerticalRadius=" + this.topRightVerticalRadius + ", topRightHorizontalRadius=" + this.topRightHorizontalRadius) + (this.bottomRightHorizontalRadius == this.bottomRightVerticalRadius ? ", bottomRight=" + this.bottomRightHorizontalRadius : ", bottomRightHorizontalRadius=" + this.bottomRightHorizontalRadius + ", bottomRightVerticalRadius=" + this.bottomRightVerticalRadius) + (this.bottomLeftHorizontalRadius == this.bottomLeftVerticalRadius ? ", bottomLeft=" + this.bottomLeftHorizontalRadius : ", bottomLeftVerticalRadius=" + this.bottomLeftVerticalRadius + ", bottomLeftHorizontalRadius=" + this.bottomLeftHorizontalRadius) + "]";
    }
}

