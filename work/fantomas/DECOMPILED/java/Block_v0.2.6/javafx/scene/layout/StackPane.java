/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.util.Callback;

public class StackPane
extends Pane {
    private boolean biasDirty = true;
    private Orientation bias;
    private static final String MARGIN_CONSTRAINT = "stackpane-margin";
    private static final String ALIGNMENT_CONSTRAINT = "stackpane-alignment";
    private static final Callback<Node, Insets> marginAccessor = node -> StackPane.getMargin(node);
    private ObjectProperty<Pos> alignment;

    public static void setAlignment(Node node, Pos pos) {
        StackPane.setConstraint(node, ALIGNMENT_CONSTRAINT, (Object)pos);
    }

    public static Pos getAlignment(Node node) {
        return (Pos)((Object)StackPane.getConstraint(node, ALIGNMENT_CONSTRAINT));
    }

    public static void setMargin(Node node, Insets insets) {
        StackPane.setConstraint(node, MARGIN_CONSTRAINT, insets);
    }

    public static Insets getMargin(Node node) {
        return (Insets)StackPane.getConstraint(node, MARGIN_CONSTRAINT);
    }

    public static void clearConstraints(Node node) {
        StackPane.setAlignment(node, null);
        StackPane.setMargin(node, null);
    }

    public StackPane() {
    }

    public StackPane(Node ... nodeArray) {
        this.getChildren().addAll((Node[])nodeArray);
    }

    public final ObjectProperty<Pos> alignmentProperty() {
        if (this.alignment == null) {
            this.alignment = new StyleableObjectProperty<Pos>(Pos.CENTER){

                @Override
                public void invalidated() {
                    StackPane.this.requestLayout();
                }

                @Override
                public CssMetaData<StackPane, Pos> getCssMetaData() {
                    return StyleableProperties.ALIGNMENT;
                }

                @Override
                public Object getBean() {
                    return StackPane.this;
                }

                @Override
                public String getName() {
                    return "alignment";
                }
            };
        }
        return this.alignment;
    }

    public final void setAlignment(Pos pos) {
        this.alignmentProperty().set(pos);
    }

    public final Pos getAlignment() {
        return this.alignment == null ? Pos.CENTER : (Pos)((Object)this.alignment.get());
    }

    private Pos getAlignmentInternal() {
        Pos pos = this.getAlignment();
        return pos == null ? Pos.CENTER : pos;
    }

    @Override
    public Orientation getContentBias() {
        if (this.biasDirty) {
            this.bias = null;
            List list = this.getManagedChildren();
            for (Node node : list) {
                Orientation orientation = node.getContentBias();
                if (orientation == null) continue;
                this.bias = orientation;
                if (orientation != Orientation.HORIZONTAL) continue;
                break;
            }
            this.biasDirty = false;
        }
        return this.bias;
    }

    @Override
    protected double computeMinWidth(double d) {
        List<Node> list = this.getManagedChildren();
        return this.getInsets().getLeft() + this.computeMaxMinAreaWidth(list, marginAccessor, d, true) + this.getInsets().getRight();
    }

    @Override
    protected double computeMinHeight(double d) {
        List<Node> list = this.getManagedChildren();
        return this.getInsets().getTop() + this.computeMaxMinAreaHeight(list, marginAccessor, this.getAlignmentInternal().getVpos(), d) + this.getInsets().getBottom();
    }

    @Override
    protected double computePrefWidth(double d) {
        List<Node> list = this.getManagedChildren();
        Insets insets = this.getInsets();
        return insets.getLeft() + this.computeMaxPrefAreaWidth(list, marginAccessor, d == -1.0 ? -1.0 : d - insets.getTop() - insets.getBottom(), true) + insets.getRight();
    }

    @Override
    protected double computePrefHeight(double d) {
        List<Node> list = this.getManagedChildren();
        Insets insets = this.getInsets();
        return insets.getTop() + this.computeMaxPrefAreaHeight(list, marginAccessor, d == -1.0 ? -1.0 : d - insets.getLeft() - insets.getRight(), this.getAlignmentInternal().getVpos()) + insets.getBottom();
    }

    @Override
    public void requestLayout() {
        this.biasDirty = true;
        this.bias = null;
        super.requestLayout();
    }

    @Override
    protected void layoutChildren() {
        List<Node> list = this.getManagedChildren();
        Pos pos = this.getAlignmentInternal();
        HPos hPos = pos.getHpos();
        VPos vPos = pos.getVpos();
        double d = this.getWidth();
        double d2 = this.getHeight();
        double d3 = this.getInsets().getTop();
        double d4 = this.getInsets().getRight();
        double d5 = this.getInsets().getLeft();
        double d6 = this.getInsets().getBottom();
        double d7 = d - d5 - d4;
        double d8 = d2 - d3 - d6;
        double d9 = vPos == VPos.BASELINE ? this.getAreaBaselineOffset(list, marginAccessor, n -> d, d8, true) : 0.0;
        int n2 = list.size();
        for (int i = 0; i < n2; ++i) {
            Node node = list.get(i);
            Pos pos2 = StackPane.getAlignment(node);
            this.layoutInArea(node, d5, d3, d7, d8, d9, StackPane.getMargin(node), pos2 != null ? pos2.getHpos() : hPos, pos2 != null ? pos2.getVpos() : vPos);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return StackPane.getClassCssMetaData();
    }

    private static class StyleableProperties {
        private static final CssMetaData<StackPane, Pos> ALIGNMENT = new CssMetaData<StackPane, Pos>("-fx-alignment", new EnumConverter<Pos>(Pos.class), Pos.CENTER){

            @Override
            public boolean isSettable(StackPane stackPane) {
                return stackPane.alignment == null || !stackPane.alignment.isBound();
            }

            @Override
            public StyleableProperty<Pos> getStyleableProperty(StackPane stackPane) {
                return (StyleableProperty)((Object)stackPane.alignmentProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Region.getClassCssMetaData());
            arrayList.add(ALIGNMENT);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

