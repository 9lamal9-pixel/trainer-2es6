/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import javafx.geometry.Orientation;
import javafx.scene.control.Separator;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;

public class SeparatorSkin
extends SkinBase<Separator> {
    private static final double DEFAULT_LENGTH = 10.0;
    private final Region line = new Region();

    public SeparatorSkin(Separator separator) {
        super(separator);
        this.line.getStyleClass().setAll((String[])new String[]{"line"});
        this.getChildren().add(this.line);
        this.registerChangeListener(separator.orientationProperty(), observableValue -> ((Separator)this.getSkinnable()).requestLayout());
        this.registerChangeListener(separator.halignmentProperty(), observableValue -> ((Separator)this.getSkinnable()).requestLayout());
        this.registerChangeListener(separator.valignmentProperty(), observableValue -> ((Separator)this.getSkinnable()).requestLayout());
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        Separator separator = (Separator)this.getSkinnable();
        if (separator.getOrientation() == Orientation.HORIZONTAL) {
            this.line.resize(d3, this.line.prefHeight(-1.0));
        } else {
            this.line.resize(this.line.prefWidth(-1.0), d4);
        }
        this.positionInArea(this.line, d, d2, d3, d4, 0.0, separator.getHalignment(), separator.getValignment());
    }

    @Override
    protected double computeMinWidth(double d, double d2, double d3, double d4, double d5) {
        return this.computePrefWidth(d, d2, d3, d4, d5);
    }

    @Override
    protected double computeMinHeight(double d, double d2, double d3, double d4, double d5) {
        return this.computePrefHeight(d, d2, d3, d4, d5);
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        Separator separator = (Separator)this.getSkinnable();
        double d6 = separator.getOrientation() == Orientation.VERTICAL ? this.line.prefWidth(-1.0) : 10.0;
        return d6 + d5 + d3;
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        Separator separator = (Separator)this.getSkinnable();
        double d6 = separator.getOrientation() == Orientation.VERTICAL ? 10.0 : this.line.prefHeight(-1.0);
        return d6 + d2 + d4;
    }

    @Override
    protected double computeMaxWidth(double d, double d2, double d3, double d4, double d5) {
        Separator separator = (Separator)this.getSkinnable();
        return separator.getOrientation() == Orientation.VERTICAL ? separator.prefWidth(d) : Double.MAX_VALUE;
    }

    @Override
    protected double computeMaxHeight(double d, double d2, double d3, double d4, double d5) {
        Separator separator = (Separator)this.getSkinnable();
        return separator.getOrientation() == Orientation.VERTICAL ? Double.MAX_VALUE : separator.prefHeight(d);
    }
}

