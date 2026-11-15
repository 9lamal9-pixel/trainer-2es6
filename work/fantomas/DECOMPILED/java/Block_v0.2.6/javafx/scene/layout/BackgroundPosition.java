/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import javafx.beans.NamedArg;
import javafx.geometry.Side;

public class BackgroundPosition {
    public static final BackgroundPosition DEFAULT = new BackgroundPosition(Side.LEFT, 0.0, true, Side.TOP, 0.0, true);
    public static final BackgroundPosition CENTER = new BackgroundPosition(Side.LEFT, 0.5, true, Side.TOP, 0.5, true);
    final Side horizontalSide;
    final Side verticalSide;
    final double horizontalPosition;
    final double verticalPosition;
    final boolean horizontalAsPercentage;
    final boolean verticalAsPercentage;
    private final int hash;

    public final Side getHorizontalSide() {
        return this.horizontalSide;
    }

    public final Side getVerticalSide() {
        return this.verticalSide;
    }

    public final double getHorizontalPosition() {
        return this.horizontalPosition;
    }

    public final double getVerticalPosition() {
        return this.verticalPosition;
    }

    public final boolean isHorizontalAsPercentage() {
        return this.horizontalAsPercentage;
    }

    public final boolean isVerticalAsPercentage() {
        return this.verticalAsPercentage;
    }

    public BackgroundPosition(@NamedArg(value="horizontalSide") Side side, @NamedArg(value="horizontalPosition") double d, @NamedArg(value="horizontalAsPercentage") boolean bl, @NamedArg(value="verticalSide") Side side2, @NamedArg(value="verticalPosition") double d2, @NamedArg(value="verticalAsPercentage") boolean bl2) {
        if (side == Side.TOP || side == Side.BOTTOM) {
            throw new IllegalArgumentException("The horizontalSide must be LEFT or RIGHT");
        }
        if (side2 == Side.LEFT || side2 == Side.RIGHT) {
            throw new IllegalArgumentException("The verticalSide must be TOP or BOTTOM");
        }
        this.horizontalSide = side == null ? Side.LEFT : side;
        this.verticalSide = side2 == null ? Side.TOP : side2;
        this.horizontalPosition = d;
        this.verticalPosition = d2;
        this.horizontalAsPercentage = bl;
        this.verticalAsPercentage = bl2;
        int n = this.horizontalSide.hashCode();
        n = 31 * n + this.verticalSide.hashCode();
        long l = this.horizontalPosition != 0.0 ? Double.doubleToLongBits(this.horizontalPosition) : 0L;
        n = 31 * n + (int)(l ^ l >>> 32);
        l = this.verticalPosition != 0.0 ? Double.doubleToLongBits(this.verticalPosition) : 0L;
        n = 31 * n + (int)(l ^ l >>> 32);
        n = 31 * n + (this.horizontalAsPercentage ? 1 : 0);
        this.hash = n = 31 * n + (this.verticalAsPercentage ? 1 : 0);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        BackgroundPosition backgroundPosition = (BackgroundPosition)object;
        if (this.hash != backgroundPosition.hash) {
            return false;
        }
        if (this.horizontalAsPercentage != backgroundPosition.horizontalAsPercentage) {
            return false;
        }
        if (Double.compare(backgroundPosition.horizontalPosition, this.horizontalPosition) != 0) {
            return false;
        }
        if (this.verticalAsPercentage != backgroundPosition.verticalAsPercentage) {
            return false;
        }
        if (Double.compare(backgroundPosition.verticalPosition, this.verticalPosition) != 0) {
            return false;
        }
        if (this.horizontalSide != backgroundPosition.horizontalSide) {
            return false;
        }
        return this.verticalSide == backgroundPosition.verticalSide;
    }

    public int hashCode() {
        return this.hash;
    }
}

