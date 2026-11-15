/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import java.util.List;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class AnchorPane
extends Pane {
    private static final String TOP_ANCHOR = "pane-top-anchor";
    private static final String LEFT_ANCHOR = "pane-left-anchor";
    private static final String BOTTOM_ANCHOR = "pane-bottom-anchor";
    private static final String RIGHT_ANCHOR = "pane-right-anchor";

    public static void setTopAnchor(Node node, Double d) {
        AnchorPane.setConstraint(node, TOP_ANCHOR, d);
    }

    public static Double getTopAnchor(Node node) {
        return (Double)AnchorPane.getConstraint(node, TOP_ANCHOR);
    }

    public static void setLeftAnchor(Node node, Double d) {
        AnchorPane.setConstraint(node, LEFT_ANCHOR, d);
    }

    public static Double getLeftAnchor(Node node) {
        return (Double)AnchorPane.getConstraint(node, LEFT_ANCHOR);
    }

    public static void setBottomAnchor(Node node, Double d) {
        AnchorPane.setConstraint(node, BOTTOM_ANCHOR, d);
    }

    public static Double getBottomAnchor(Node node) {
        return (Double)AnchorPane.getConstraint(node, BOTTOM_ANCHOR);
    }

    public static void setRightAnchor(Node node, Double d) {
        AnchorPane.setConstraint(node, RIGHT_ANCHOR, d);
    }

    public static Double getRightAnchor(Node node) {
        return (Double)AnchorPane.getConstraint(node, RIGHT_ANCHOR);
    }

    public static void clearConstraints(Node node) {
        AnchorPane.setTopAnchor(node, null);
        AnchorPane.setRightAnchor(node, null);
        AnchorPane.setBottomAnchor(node, null);
        AnchorPane.setLeftAnchor(node, null);
    }

    public AnchorPane() {
    }

    public AnchorPane(Node ... nodeArray) {
        this.getChildren().addAll((Node[])nodeArray);
    }

    @Override
    protected double computeMinWidth(double d) {
        return this.computeWidth(true, d);
    }

    @Override
    protected double computeMinHeight(double d) {
        return this.computeHeight(true, d);
    }

    @Override
    protected double computePrefWidth(double d) {
        return this.computeWidth(false, d);
    }

    @Override
    protected double computePrefHeight(double d) {
        return this.computeHeight(false, d);
    }

    private double computeWidth(boolean bl, double d) {
        double d2 = 0.0;
        double d3 = d != -1.0 ? d - this.getInsets().getTop() - this.getInsets().getBottom() : -1.0;
        List list = this.getManagedChildren();
        for (Node node : list) {
            Double d4 = AnchorPane.getLeftAnchor(node);
            Double d5 = AnchorPane.getRightAnchor(node);
            double d6 = d4 != null ? d4 : (d5 != null ? 0.0 : node.getLayoutBounds().getMinX() + node.getLayoutX());
            double d7 = d5 != null ? d5 : 0.0;
            double d8 = -1.0;
            if (node.getContentBias() == Orientation.VERTICAL && d3 != -1.0) {
                d8 = this.computeChildHeight(node, AnchorPane.getTopAnchor(node), AnchorPane.getBottomAnchor(node), d3, -1.0);
            }
            d2 = Math.max(d2, d6 + (bl && d4 != null && d5 != null ? node.minWidth(d8) : this.computeChildPrefAreaWidth(node, -1.0, null, d8, false)) + d7);
        }
        Insets insets = this.getInsets();
        return insets.getLeft() + d2 + insets.getRight();
    }

    private double computeHeight(boolean bl, double d) {
        double d2 = 0.0;
        double d3 = d != -1.0 ? d - this.getInsets().getLeft() - this.getInsets().getRight() : -1.0;
        List list = this.getManagedChildren();
        for (Node node : list) {
            Double d4 = AnchorPane.getTopAnchor(node);
            Double d5 = AnchorPane.getBottomAnchor(node);
            double d6 = d4 != null ? d4 : (d5 != null ? 0.0 : node.getLayoutBounds().getMinY() + node.getLayoutY());
            double d7 = d5 != null ? d5 : 0.0;
            double d8 = -1.0;
            if (node.getContentBias() == Orientation.HORIZONTAL && d3 != -1.0) {
                d8 = this.computeChildWidth(node, AnchorPane.getLeftAnchor(node), AnchorPane.getRightAnchor(node), d3, -1.0);
            }
            d2 = Math.max(d2, d6 + (bl && d4 != null && d5 != null ? node.minHeight(d8) : this.computeChildPrefAreaHeight(node, -1.0, null, d8)) + d7);
        }
        Insets insets = this.getInsets();
        return insets.getTop() + d2 + insets.getBottom();
    }

    private double computeChildWidth(Node node, Double d, Double d2, double d3, double d4) {
        if (d != null && d2 != null && node.isResizable()) {
            Insets insets = this.getInsets();
            return d3 - insets.getLeft() - insets.getRight() - d - d2;
        }
        return this.computeChildPrefAreaWidth(node, -1.0, Insets.EMPTY, d4, true);
    }

    private double computeChildHeight(Node node, Double d, Double d2, double d3, double d4) {
        if (d != null && d2 != null && node.isResizable()) {
            Insets insets = this.getInsets();
            return d3 - insets.getTop() - insets.getBottom() - d - d2;
        }
        return this.computeChildPrefAreaHeight(node, -1.0, Insets.EMPTY, d4);
    }

    @Override
    protected void layoutChildren() {
        Insets insets = this.getInsets();
        List list = this.getManagedChildren();
        for (Node node : list) {
            double d;
            double d2;
            Double d3 = AnchorPane.getTopAnchor(node);
            Double d4 = AnchorPane.getBottomAnchor(node);
            Double d5 = AnchorPane.getLeftAnchor(node);
            Double d6 = AnchorPane.getRightAnchor(node);
            Bounds bounds = node.getLayoutBounds();
            Orientation orientation = node.getContentBias();
            double d7 = node.getLayoutX() + bounds.getMinX();
            double d8 = node.getLayoutY() + bounds.getMinY();
            if (orientation == Orientation.VERTICAL) {
                d2 = this.computeChildHeight(node, d3, d4, this.getHeight(), -1.0);
                d = this.computeChildWidth(node, d5, d6, this.getWidth(), d2);
            } else if (orientation == Orientation.HORIZONTAL) {
                d = this.computeChildWidth(node, d5, d6, this.getWidth(), -1.0);
                d2 = this.computeChildHeight(node, d3, d4, this.getHeight(), d);
            } else {
                d = this.computeChildWidth(node, d5, d6, this.getWidth(), -1.0);
                d2 = this.computeChildHeight(node, d3, d4, this.getHeight(), -1.0);
            }
            if (d5 != null) {
                d7 = insets.getLeft() + d5;
            } else if (d6 != null) {
                d7 = this.getWidth() - insets.getRight() - d6 - d;
            }
            if (d3 != null) {
                d8 = insets.getTop() + d3;
            } else if (d4 != null) {
                d8 = this.getHeight() - insets.getBottom() - d4 - d2;
            }
            node.resizeRelocate(d7, d8, d, d2);
        }
    }
}

