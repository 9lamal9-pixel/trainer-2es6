/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import com.sun.javafx.geom.Vec2d;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class BorderPane
extends Pane {
    private static final String MARGIN = "borderpane-margin";
    private static final String ALIGNMENT = "borderpane-alignment";
    private ObjectProperty<Node> center;
    private ObjectProperty<Node> top;
    private ObjectProperty<Node> bottom;
    private ObjectProperty<Node> left;
    private ObjectProperty<Node> right;

    public static void setAlignment(Node node, Pos pos) {
        BorderPane.setConstraint(node, ALIGNMENT, (Object)pos);
    }

    public static Pos getAlignment(Node node) {
        return (Pos)((Object)BorderPane.getConstraint(node, ALIGNMENT));
    }

    public static void setMargin(Node node, Insets insets) {
        BorderPane.setConstraint(node, MARGIN, insets);
    }

    public static Insets getMargin(Node node) {
        return (Insets)BorderPane.getConstraint(node, MARGIN);
    }

    private static Insets getNodeMargin(Node node) {
        Insets insets = BorderPane.getMargin(node);
        return insets != null ? insets : Insets.EMPTY;
    }

    public static void clearConstraints(Node node) {
        BorderPane.setAlignment(node, null);
        BorderPane.setMargin(node, null);
    }

    public BorderPane() {
    }

    public BorderPane(Node node) {
        this.setCenter(node);
    }

    public BorderPane(Node node, Node node2, Node node3, Node node4, Node node5) {
        this.setCenter(node);
        this.setTop(node2);
        this.setRight(node3);
        this.setBottom(node4);
        this.setLeft(node5);
    }

    public final ObjectProperty<Node> centerProperty() {
        if (this.center == null) {
            this.center = new BorderPositionProperty("center");
        }
        return this.center;
    }

    public final void setCenter(Node node) {
        this.centerProperty().set(node);
    }

    public final Node getCenter() {
        return this.center == null ? null : (Node)this.center.get();
    }

    public final ObjectProperty<Node> topProperty() {
        if (this.top == null) {
            this.top = new BorderPositionProperty("top");
        }
        return this.top;
    }

    public final void setTop(Node node) {
        this.topProperty().set(node);
    }

    public final Node getTop() {
        return this.top == null ? null : (Node)this.top.get();
    }

    public final ObjectProperty<Node> bottomProperty() {
        if (this.bottom == null) {
            this.bottom = new BorderPositionProperty("bottom");
        }
        return this.bottom;
    }

    public final void setBottom(Node node) {
        this.bottomProperty().set(node);
    }

    public final Node getBottom() {
        return this.bottom == null ? null : (Node)this.bottom.get();
    }

    public final ObjectProperty<Node> leftProperty() {
        if (this.left == null) {
            this.left = new BorderPositionProperty("left");
        }
        return this.left;
    }

    public final void setLeft(Node node) {
        this.leftProperty().set(node);
    }

    public final Node getLeft() {
        return this.left == null ? null : (Node)this.left.get();
    }

    public final ObjectProperty<Node> rightProperty() {
        if (this.right == null) {
            this.right = new BorderPositionProperty("right");
        }
        return this.right;
    }

    public final void setRight(Node node) {
        this.rightProperty().set(node);
    }

    public final Node getRight() {
        return this.right == null ? null : (Node)this.right.get();
    }

    @Override
    public Orientation getContentBias() {
        Node node = this.getCenter();
        if (node != null && node.isManaged() && node.getContentBias() != null) {
            return node.getContentBias();
        }
        Node node2 = this.getRight();
        if (node2 != null && node2.isManaged() && node2.getContentBias() == Orientation.VERTICAL) {
            return node2.getContentBias();
        }
        Node node3 = this.getLeft();
        if (node3 != null && node3.isManaged() && node3.getContentBias() == Orientation.VERTICAL) {
            return node3.getContentBias();
        }
        Node node4 = this.getBottom();
        if (node4 != null && node4.isManaged() && node4.getContentBias() == Orientation.HORIZONTAL) {
            return node4.getContentBias();
        }
        Node node5 = this.getTop();
        if (node5 != null && node5.isManaged() && node5.getContentBias() == Orientation.HORIZONTAL) {
            return node5.getContentBias();
        }
        return null;
    }

    @Override
    protected double computeMinWidth(double d) {
        double d2;
        double d3;
        double d4;
        double d5 = this.getAreaWidth(this.getTop(), -1.0, true);
        double d6 = this.getAreaWidth(this.getBottom(), -1.0, true);
        if (d != -1.0 && (this.childHasContentBias(this.getLeft(), Orientation.VERTICAL) || this.childHasContentBias(this.getRight(), Orientation.VERTICAL) || this.childHasContentBias(this.getCenter(), Orientation.VERTICAL))) {
            double d7 = this.getAreaHeight(this.getTop(), -1.0, false);
            double d8 = this.getAreaHeight(this.getBottom(), -1.0, false);
            double d9 = Math.max(0.0, d - d7 - d8);
            d4 = this.getAreaWidth(this.getLeft(), d9, false);
            d3 = this.getAreaWidth(this.getRight(), d9, false);
            d2 = this.getAreaWidth(this.getCenter(), d9, true);
        } else {
            d4 = this.getAreaWidth(this.getLeft(), -1.0, false);
            d3 = this.getAreaWidth(this.getRight(), -1.0, false);
            d2 = this.getAreaWidth(this.getCenter(), -1.0, true);
        }
        Insets insets = this.getInsets();
        return insets.getLeft() + Math.max(d4 + d2 + d3, Math.max(d5, d6)) + insets.getRight();
    }

    @Override
    protected double computeMinHeight(double d) {
        double d2;
        double d3;
        Insets insets = this.getInsets();
        double d4 = this.getAreaHeight(this.getTop(), d, false);
        double d5 = this.getAreaHeight(this.getBottom(), d, false);
        double d6 = this.getAreaHeight(this.getLeft(), -1.0, true);
        double d7 = this.getAreaHeight(this.getRight(), -1.0, true);
        if (d != -1.0 && this.childHasContentBias(this.getCenter(), Orientation.HORIZONTAL)) {
            d3 = this.getAreaWidth(this.getLeft(), -1.0, false);
            double d8 = this.getAreaWidth(this.getRight(), -1.0, false);
            d2 = this.getAreaHeight(this.getCenter(), Math.max(0.0, d - d3 - d8), true);
        } else {
            d2 = this.getAreaHeight(this.getCenter(), -1.0, true);
        }
        d3 = Math.max(d2, Math.max(d7, d6));
        return insets.getTop() + d4 + d3 + d5 + insets.getBottom();
    }

    @Override
    protected double computePrefWidth(double d) {
        double d2;
        double d3;
        double d4;
        double d5 = this.getAreaWidth(this.getTop(), -1.0, false);
        double d6 = this.getAreaWidth(this.getBottom(), -1.0, false);
        if (d != -1.0 && (this.childHasContentBias(this.getLeft(), Orientation.VERTICAL) || this.childHasContentBias(this.getRight(), Orientation.VERTICAL) || this.childHasContentBias(this.getCenter(), Orientation.VERTICAL))) {
            double d7 = this.getAreaHeight(this.getTop(), -1.0, false);
            double d8 = this.getAreaHeight(this.getBottom(), -1.0, false);
            double d9 = Math.max(0.0, d - d7 - d8);
            d4 = this.getAreaWidth(this.getLeft(), d9, false);
            d3 = this.getAreaWidth(this.getRight(), d9, false);
            d2 = this.getAreaWidth(this.getCenter(), d9, false);
        } else {
            d4 = this.getAreaWidth(this.getLeft(), -1.0, false);
            d3 = this.getAreaWidth(this.getRight(), -1.0, false);
            d2 = this.getAreaWidth(this.getCenter(), -1.0, false);
        }
        Insets insets = this.getInsets();
        return insets.getLeft() + Math.max(d4 + d2 + d3, Math.max(d5, d6)) + insets.getRight();
    }

    @Override
    protected double computePrefHeight(double d) {
        double d2;
        double d3;
        Insets insets = this.getInsets();
        double d4 = this.getAreaHeight(this.getTop(), d, false);
        double d5 = this.getAreaHeight(this.getBottom(), d, false);
        double d6 = this.getAreaHeight(this.getLeft(), -1.0, false);
        double d7 = this.getAreaHeight(this.getRight(), -1.0, false);
        if (d != -1.0 && this.childHasContentBias(this.getCenter(), Orientation.HORIZONTAL)) {
            d3 = this.getAreaWidth(this.getLeft(), -1.0, false);
            double d8 = this.getAreaWidth(this.getRight(), -1.0, false);
            d2 = this.getAreaHeight(this.getCenter(), Math.max(0.0, d - d3 - d8), false);
        } else {
            d2 = this.getAreaHeight(this.getCenter(), -1.0, false);
        }
        d3 = Math.max(d2, Math.max(d7, d6));
        return insets.getTop() + d4 + d3 + d5 + insets.getBottom();
    }

    @Override
    protected void layoutChildren() {
        Object object;
        double d;
        double d2;
        double d3;
        Insets insets = this.getInsets();
        double d4 = this.getWidth();
        double d5 = this.getHeight();
        Orientation orientation = this.getContentBias();
        if (orientation == null) {
            d3 = this.minWidth(-1.0);
            d2 = this.minHeight(-1.0);
            d4 = d4 < d3 ? d3 : d4;
            d5 = d5 < d2 ? d2 : d5;
        } else if (orientation == Orientation.HORIZONTAL) {
            d3 = this.minWidth(-1.0);
            d4 = d4 < d3 ? d3 : d4;
            d2 = this.minHeight(d4);
            d5 = d5 < d2 ? d2 : d5;
        } else {
            d3 = this.minHeight(-1.0);
            d5 = d5 < d3 ? d3 : d5;
            d2 = this.minWidth(d5);
            d4 = d4 < d2 ? d2 : d4;
        }
        d3 = insets.getLeft();
        d2 = insets.getTop();
        double d6 = d4 - d3 - insets.getRight();
        double d7 = d5 - d2 - insets.getBottom();
        Node node = this.getCenter();
        Node node2 = this.getRight();
        Node node3 = this.getBottom();
        Node node4 = this.getLeft();
        Node node5 = this.getTop();
        double d8 = 0.0;
        if (node5 != null && node5.isManaged()) {
            Insets insets2 = BorderPane.getNodeMargin(node5);
            double d9 = this.adjustWidthByMargin(d6, insets2);
            d = this.adjustHeightByMargin(d7, insets2);
            d8 = this.snapSizeY(node5.prefHeight(d9));
            d8 = Math.min(d8, d);
            Vec2d vec2d = BorderPane.boundedNodeSizeWithBias(node5, d9, d8, true, true, TEMP_VEC2D);
            d8 = this.snapSizeY(vec2d.y);
            node5.resize(this.snapSizeX(vec2d.x), d8);
            d8 = this.snapSpaceY(insets2.getBottom()) + d8 + this.snapSpaceY(insets2.getTop());
            object = BorderPane.getAlignment(node5);
            BorderPane.positionInArea(node5, d3, d2, d6, d8, 0.0, insets2, object != null ? ((Pos)((Object)object)).getHpos() : HPos.LEFT, object != null ? ((Pos)((Object)object)).getVpos() : VPos.TOP, this.isSnapToPixel());
        }
        double d10 = 0.0;
        if (node3 != null && node3.isManaged()) {
            Insets insets3 = BorderPane.getNodeMargin(node3);
            d = this.adjustWidthByMargin(d6, insets3);
            double d11 = this.adjustHeightByMargin(d7 - d8, insets3);
            d10 = this.snapSizeY(node3.prefHeight(d));
            d10 = Math.min(d10, d11);
            Vec2d vec2d = BorderPane.boundedNodeSizeWithBias(node3, d, d10, true, true, TEMP_VEC2D);
            d10 = this.snapSizeY(vec2d.y);
            node3.resize(this.snapSizeX(vec2d.x), d10);
            d10 = this.snapSpaceY(insets3.getBottom()) + d10 + this.snapSpaceY(insets3.getTop());
            Pos pos = BorderPane.getAlignment(node3);
            BorderPane.positionInArea(node3, d3, d2 + d7 - d10, d6, d10, 0.0, insets3, pos != null ? pos.getHpos() : HPos.LEFT, pos != null ? pos.getVpos() : VPos.BOTTOM, this.isSnapToPixel());
        }
        double d12 = 0.0;
        if (node4 != null && node4.isManaged()) {
            Insets insets4 = BorderPane.getNodeMargin(node4);
            double d13 = this.adjustWidthByMargin(d6, insets4);
            double d14 = this.adjustHeightByMargin(d7 - d8 - d10, insets4);
            d12 = this.snapSizeX(node4.prefWidth(d14));
            d12 = Math.min(d12, d13);
            Vec2d vec2d = BorderPane.boundedNodeSizeWithBias(node4, d12, d14, true, true, TEMP_VEC2D);
            d12 = this.snapSizeX(vec2d.x);
            node4.resize(d12, this.snapSizeY(vec2d.y));
            d12 = this.snapSpaceX(insets4.getLeft()) + d12 + this.snapSpaceX(insets4.getRight());
            Pos pos = BorderPane.getAlignment(node4);
            BorderPane.positionInArea(node4, d3, d2 + d8, d12, d7 - d8 - d10, 0.0, insets4, pos != null ? pos.getHpos() : HPos.LEFT, pos != null ? pos.getVpos() : VPos.TOP, this.isSnapToPixel());
        }
        double d15 = 0.0;
        if (node2 != null && node2.isManaged()) {
            object = BorderPane.getNodeMargin(node2);
            double d16 = this.adjustWidthByMargin(d6 - d12, (Insets)object);
            double d17 = this.adjustHeightByMargin(d7 - d8 - d10, (Insets)object);
            d15 = this.snapSizeX(node2.prefWidth(d17));
            d15 = Math.min(d15, d16);
            Vec2d vec2d = BorderPane.boundedNodeSizeWithBias(node2, d15, d17, true, true, TEMP_VEC2D);
            d15 = this.snapSizeX(vec2d.x);
            node2.resize(d15, this.snapSizeY(vec2d.y));
            d15 = this.snapSpaceX(((Insets)object).getLeft()) + d15 + this.snapSpaceX(((Insets)object).getRight());
            Pos pos = BorderPane.getAlignment(node2);
            BorderPane.positionInArea(node2, d3 + d6 - d15, d2 + d8, d15, d7 - d8 - d10, 0.0, (Insets)object, pos != null ? pos.getHpos() : HPos.RIGHT, pos != null ? pos.getVpos() : VPos.TOP, this.isSnapToPixel());
        }
        if (node != null && node.isManaged()) {
            object = BorderPane.getAlignment(node);
            this.layoutInArea(node, d3 + d12, d2 + d8, d6 - d12 - d15, d7 - d8 - d10, 0.0, BorderPane.getNodeMargin(node), object != null ? ((Pos)((Object)object)).getHpos() : HPos.CENTER, object != null ? ((Pos)((Object)object)).getVpos() : VPos.CENTER);
        }
    }

    private double getAreaWidth(Node node, double d, boolean bl) {
        if (node != null && node.isManaged()) {
            Insets insets = BorderPane.getNodeMargin(node);
            return bl ? this.computeChildMinAreaWidth(node, -1.0, insets, d, false) : this.computeChildPrefAreaWidth(node, -1.0, insets, d, false);
        }
        return 0.0;
    }

    private double getAreaHeight(Node node, double d, boolean bl) {
        if (node != null && node.isManaged()) {
            Insets insets = BorderPane.getNodeMargin(node);
            return bl ? this.computeChildMinAreaHeight(node, -1.0, insets, d) : this.computeChildPrefAreaHeight(node, -1.0, insets, d);
        }
        return 0.0;
    }

    private boolean childHasContentBias(Node node, Orientation orientation) {
        if (node != null && node.isManaged()) {
            return node.getContentBias() == orientation;
        }
        return false;
    }

    private final class BorderPositionProperty
    extends ObjectPropertyBase<Node> {
        private Node oldValue = null;
        private final String propertyName;
        private boolean isBeingInvalidated;

        BorderPositionProperty(String string) {
            this.propertyName = string;
            BorderPane.this.getChildren().addListener(new ListChangeListener<Node>(){

                @Override
                public void onChanged(ListChangeListener.Change<? extends Node> change) {
                    if (BorderPositionProperty.this.oldValue == null || BorderPositionProperty.this.isBeingInvalidated) {
                        return;
                    }
                    while (change.next()) {
                        if (!change.wasRemoved()) continue;
                        List<? extends Node> list = change.getRemoved();
                        int n = list.size();
                        for (int i = 0; i < n; ++i) {
                            if (list.get(i) != BorderPositionProperty.this.oldValue) continue;
                            BorderPositionProperty.this.oldValue = null;
                            BorderPositionProperty.this.set(null);
                        }
                    }
                }
            });
        }

        @Override
        protected void invalidated() {
            ObservableList<Node> observableList = BorderPane.this.getChildren();
            this.isBeingInvalidated = true;
            try {
                Node node;
                if (this.oldValue != null) {
                    observableList.remove(this.oldValue);
                }
                this.oldValue = node = (Node)this.get();
                if (node != null) {
                    observableList.add(node);
                }
            }
            finally {
                this.isBeingInvalidated = false;
            }
        }

        @Override
        public Object getBean() {
            return BorderPane.this;
        }

        @Override
        public String getName() {
            return this.propertyName;
        }
    }
}

