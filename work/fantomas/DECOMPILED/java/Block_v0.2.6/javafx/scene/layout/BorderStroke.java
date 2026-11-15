/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import javafx.beans.NamedArg;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.StrokeType;

public class BorderStroke {
    public static final BorderWidths THIN = new BorderWidths(1.0);
    public static final BorderWidths MEDIUM = new BorderWidths(3.0);
    public static final BorderWidths THICK = new BorderWidths(5.0);
    public static final BorderWidths DEFAULT_WIDTHS = THIN;
    final Paint topStroke;
    final Paint rightStroke;
    final Paint bottomStroke;
    final Paint leftStroke;
    final BorderStrokeStyle topStyle;
    final BorderStrokeStyle rightStyle;
    final BorderStrokeStyle bottomStyle;
    final BorderStrokeStyle leftStyle;
    final BorderWidths widths;
    final Insets insets;
    final Insets innerEdge;
    final Insets outerEdge;
    private final CornerRadii radii;
    private final boolean strokeUniform;
    private final int hash;

    public final Paint getTopStroke() {
        return this.topStroke;
    }

    public final Paint getRightStroke() {
        return this.rightStroke;
    }

    public final Paint getBottomStroke() {
        return this.bottomStroke;
    }

    public final Paint getLeftStroke() {
        return this.leftStroke;
    }

    public final BorderStrokeStyle getTopStyle() {
        return this.topStyle;
    }

    public final BorderStrokeStyle getRightStyle() {
        return this.rightStyle;
    }

    public final BorderStrokeStyle getBottomStyle() {
        return this.bottomStyle;
    }

    public final BorderStrokeStyle getLeftStyle() {
        return this.leftStyle;
    }

    public final BorderWidths getWidths() {
        return this.widths;
    }

    public final Insets getInsets() {
        return this.insets;
    }

    public final CornerRadii getRadii() {
        return this.radii;
    }

    public final boolean isStrokeUniform() {
        return this.strokeUniform;
    }

    public BorderStroke(@NamedArg(value="stroke") Paint paint, @NamedArg(value="style") BorderStrokeStyle borderStrokeStyle, @NamedArg(value="radii") CornerRadii cornerRadii, @NamedArg(value="widths") BorderWidths borderWidths) {
        this.bottomStroke = paint == null ? Color.BLACK : paint;
        this.rightStroke = this.bottomStroke;
        this.topStroke = this.bottomStroke;
        this.leftStroke = this.bottomStroke;
        this.leftStyle = borderStrokeStyle == null ? BorderStrokeStyle.NONE : borderStrokeStyle;
        this.bottomStyle = this.leftStyle;
        this.rightStyle = this.leftStyle;
        this.topStyle = this.leftStyle;
        this.radii = cornerRadii == null ? CornerRadii.EMPTY : cornerRadii;
        this.widths = borderWidths == null ? DEFAULT_WIDTHS : borderWidths;
        this.insets = Insets.EMPTY;
        this.strokeUniform = this.widths.left == this.widths.top && this.widths.left == this.widths.right && this.widths.left == this.widths.bottom;
        this.innerEdge = new Insets(this.computeInside(this.topStyle.getType(), this.widths.getTop()), this.computeInside(this.rightStyle.getType(), this.widths.getRight()), this.computeInside(this.bottomStyle.getType(), this.widths.getBottom()), this.computeInside(this.leftStyle.getType(), this.widths.getLeft()));
        this.outerEdge = new Insets(Math.max(0.0, this.computeOutside(this.topStyle.getType(), this.widths.getTop())), Math.max(0.0, this.computeOutside(this.rightStyle.getType(), this.widths.getRight())), Math.max(0.0, this.computeOutside(this.bottomStyle.getType(), this.widths.getBottom())), Math.max(0.0, this.computeOutside(this.leftStyle.getType(), this.widths.getLeft())));
        this.hash = this.preComputeHash();
    }

    public BorderStroke(@NamedArg(value="stroke") Paint paint, @NamedArg(value="style") BorderStrokeStyle borderStrokeStyle, @NamedArg(value="radii") CornerRadii cornerRadii, @NamedArg(value="widths") BorderWidths borderWidths, @NamedArg(value="insets") Insets insets) {
        this(paint, paint, paint, paint, borderStrokeStyle, borderStrokeStyle, borderStrokeStyle, borderStrokeStyle, cornerRadii, borderWidths, insets);
    }

    public BorderStroke(@NamedArg(value="topStroke") Paint paint, @NamedArg(value="rightStroke") Paint paint2, @NamedArg(value="bottomStroke") Paint paint3, @NamedArg(value="leftStroke") Paint paint4, @NamedArg(value="topStyle") BorderStrokeStyle borderStrokeStyle, @NamedArg(value="rightStyle") BorderStrokeStyle borderStrokeStyle2, @NamedArg(value="bottomStyle") BorderStrokeStyle borderStrokeStyle3, @NamedArg(value="leftStyle") BorderStrokeStyle borderStrokeStyle4, @NamedArg(value="radii") CornerRadii cornerRadii, @NamedArg(value="widths") BorderWidths borderWidths, @NamedArg(value="insets") Insets insets) {
        this.topStroke = paint == null ? Color.BLACK : paint;
        this.rightStroke = paint2 == null ? this.topStroke : paint2;
        this.bottomStroke = paint3 == null ? this.topStroke : paint3;
        this.leftStroke = paint4 == null ? this.rightStroke : paint4;
        this.topStyle = borderStrokeStyle == null ? BorderStrokeStyle.NONE : borderStrokeStyle;
        this.rightStyle = borderStrokeStyle2 == null ? this.topStyle : borderStrokeStyle2;
        this.bottomStyle = borderStrokeStyle3 == null ? this.topStyle : borderStrokeStyle3;
        this.leftStyle = borderStrokeStyle4 == null ? this.rightStyle : borderStrokeStyle4;
        this.radii = cornerRadii == null ? CornerRadii.EMPTY : cornerRadii;
        this.widths = borderWidths == null ? DEFAULT_WIDTHS : borderWidths;
        this.insets = insets == null ? Insets.EMPTY : insets;
        boolean bl = this.leftStroke.equals(this.topStroke) && this.leftStroke.equals(this.rightStroke) && this.leftStroke.equals(this.bottomStroke);
        boolean bl2 = this.widths.left == this.widths.top && this.widths.left == this.widths.right && this.widths.left == this.widths.bottom;
        boolean bl3 = this.leftStyle.equals(this.topStyle) && this.leftStyle.equals(this.rightStyle) && this.leftStyle.equals(this.bottomStyle);
        this.strokeUniform = bl && bl2 && bl3;
        this.innerEdge = new Insets(this.insets.getTop() + this.computeInside(this.topStyle.getType(), this.widths.getTop()), this.insets.getRight() + this.computeInside(this.rightStyle.getType(), this.widths.getRight()), this.insets.getBottom() + this.computeInside(this.bottomStyle.getType(), this.widths.getBottom()), this.insets.getLeft() + this.computeInside(this.leftStyle.getType(), this.widths.getLeft()));
        this.outerEdge = new Insets(Math.max(0.0, this.computeOutside(this.topStyle.getType(), this.widths.getTop()) - this.insets.getTop()), Math.max(0.0, this.computeOutside(this.rightStyle.getType(), this.widths.getRight()) - this.insets.getRight()), Math.max(0.0, this.computeOutside(this.bottomStyle.getType(), this.widths.getBottom()) - this.insets.getBottom()), Math.max(0.0, this.computeOutside(this.leftStyle.getType(), this.widths.getLeft()) - this.insets.getLeft()));
        this.hash = this.preComputeHash();
    }

    private int preComputeHash() {
        int n = this.topStroke.hashCode();
        n = 31 * n + this.rightStroke.hashCode();
        n = 31 * n + this.bottomStroke.hashCode();
        n = 31 * n + this.leftStroke.hashCode();
        n = 31 * n + this.topStyle.hashCode();
        n = 31 * n + this.rightStyle.hashCode();
        n = 31 * n + this.bottomStyle.hashCode();
        n = 31 * n + this.leftStyle.hashCode();
        n = 31 * n + this.widths.hashCode();
        n = 31 * n + this.radii.hashCode();
        n = 31 * n + this.insets.hashCode();
        return n;
    }

    private double computeInside(StrokeType strokeType, double d) {
        if (strokeType == StrokeType.OUTSIDE) {
            return 0.0;
        }
        if (strokeType == StrokeType.CENTERED) {
            return d / 2.0;
        }
        if (strokeType == StrokeType.INSIDE) {
            return d;
        }
        throw new AssertionError((Object)"Unexpected Stroke Type");
    }

    private double computeOutside(StrokeType strokeType, double d) {
        if (strokeType == StrokeType.OUTSIDE) {
            return d;
        }
        if (strokeType == StrokeType.CENTERED) {
            return d / 2.0;
        }
        if (strokeType == StrokeType.INSIDE) {
            return 0.0;
        }
        throw new AssertionError((Object)"Unexpected Stroke Type");
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        BorderStroke borderStroke = (BorderStroke)object;
        if (this.hash != borderStroke.hash) {
            return false;
        }
        if (!this.bottomStroke.equals(borderStroke.bottomStroke)) {
            return false;
        }
        if (!this.bottomStyle.equals(borderStroke.bottomStyle)) {
            return false;
        }
        if (!this.leftStroke.equals(borderStroke.leftStroke)) {
            return false;
        }
        if (!this.leftStyle.equals(borderStroke.leftStyle)) {
            return false;
        }
        if (!this.radii.equals(borderStroke.radii)) {
            return false;
        }
        if (!this.rightStroke.equals(borderStroke.rightStroke)) {
            return false;
        }
        if (!this.rightStyle.equals(borderStroke.rightStyle)) {
            return false;
        }
        if (!this.topStroke.equals(borderStroke.topStroke)) {
            return false;
        }
        if (!this.topStyle.equals(borderStroke.topStyle)) {
            return false;
        }
        if (!this.widths.equals(borderStroke.widths)) {
            return false;
        }
        return this.insets.equals(borderStroke.insets);
    }

    public int hashCode() {
        return this.hash;
    }
}

