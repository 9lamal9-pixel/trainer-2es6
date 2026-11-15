/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.EnumConverter;
import javafx.css.converter.SizeConverter;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

public class HBox
extends Pane {
    private boolean biasDirty = true;
    private double minBaselineComplement = Double.NaN;
    private double prefBaselineComplement = Double.NaN;
    private Orientation bias;
    private double[][] tempArray;
    private static final String MARGIN_CONSTRAINT = "hbox-margin";
    private static final String HGROW_CONSTRAINT = "hbox-hgrow";
    private static final Callback<Node, Insets> marginAccessor = node -> HBox.getMargin(node);
    private DoubleProperty spacing;
    private ObjectProperty<Pos> alignment;
    private BooleanProperty fillHeight;
    private double baselineOffset = Double.NaN;

    public static void setHgrow(Node node, Priority priority) {
        HBox.setConstraint(node, HGROW_CONSTRAINT, (Object)priority);
    }

    public static Priority getHgrow(Node node) {
        return (Priority)((Object)HBox.getConstraint(node, HGROW_CONSTRAINT));
    }

    public static void setMargin(Node node, Insets insets) {
        HBox.setConstraint(node, MARGIN_CONSTRAINT, insets);
    }

    public static Insets getMargin(Node node) {
        return (Insets)HBox.getConstraint(node, MARGIN_CONSTRAINT);
    }

    public static void clearConstraints(Node node) {
        HBox.setHgrow(node, null);
        HBox.setMargin(node, null);
    }

    public HBox() {
    }

    public HBox(double d) {
        this();
        this.setSpacing(d);
    }

    public HBox(Node ... nodeArray) {
        this.getChildren().addAll((Node[])nodeArray);
    }

    public HBox(double d, Node ... nodeArray) {
        this();
        this.setSpacing(d);
        this.getChildren().addAll((Node[])nodeArray);
    }

    public final DoubleProperty spacingProperty() {
        if (this.spacing == null) {
            this.spacing = new StyleableDoubleProperty(){

                @Override
                public void invalidated() {
                    HBox.this.requestLayout();
                }

                @Override
                public CssMetaData getCssMetaData() {
                    return StyleableProperties.SPACING;
                }

                @Override
                public Object getBean() {
                    return HBox.this;
                }

                @Override
                public String getName() {
                    return "spacing";
                }
            };
        }
        return this.spacing;
    }

    public final void setSpacing(double d) {
        this.spacingProperty().set(d);
    }

    public final double getSpacing() {
        return this.spacing == null ? 0.0 : this.spacing.get();
    }

    public final ObjectProperty<Pos> alignmentProperty() {
        if (this.alignment == null) {
            this.alignment = new StyleableObjectProperty<Pos>(Pos.TOP_LEFT){

                @Override
                public void invalidated() {
                    HBox.this.requestLayout();
                }

                @Override
                public CssMetaData<HBox, Pos> getCssMetaData() {
                    return StyleableProperties.ALIGNMENT;
                }

                @Override
                public Object getBean() {
                    return HBox.this;
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
        return this.alignment == null ? Pos.TOP_LEFT : (Pos)((Object)this.alignment.get());
    }

    private Pos getAlignmentInternal() {
        Pos pos = this.getAlignment();
        return pos == null ? Pos.TOP_LEFT : pos;
    }

    public final BooleanProperty fillHeightProperty() {
        if (this.fillHeight == null) {
            this.fillHeight = new StyleableBooleanProperty(true){

                @Override
                public void invalidated() {
                    HBox.this.requestLayout();
                }

                @Override
                public CssMetaData<HBox, Boolean> getCssMetaData() {
                    return StyleableProperties.FILL_HEIGHT;
                }

                @Override
                public Object getBean() {
                    return HBox.this;
                }

                @Override
                public String getName() {
                    return "fillHeight";
                }
            };
        }
        return this.fillHeight;
    }

    public final void setFillHeight(boolean bl) {
        this.fillHeightProperty().set(bl);
    }

    public final boolean isFillHeight() {
        return this.fillHeight == null ? true : this.fillHeight.get();
    }

    private boolean shouldFillHeight() {
        return this.isFillHeight() && this.getAlignmentInternal().getVpos() != VPos.BASELINE;
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
        Insets insets = this.getInsets();
        return this.snapSpaceX(insets.getLeft()) + this.computeContentWidth(this.getManagedChildren(), d, true) + this.snapSpaceX(insets.getRight());
    }

    @Override
    protected double computeMinHeight(double d) {
        Insets insets = this.getInsets();
        List<Node> list = this.getManagedChildren();
        double d2 = 0.0;
        if (d != -1.0 && this.getContentBias() != null) {
            double[][] dArray = this.getAreaWidths(list, -1.0, false);
            this.adjustAreaWidths(list, dArray, d, -1.0);
            d2 = this.computeMaxMinAreaHeight(list, marginAccessor, dArray[0], this.getAlignmentInternal().getVpos());
        } else {
            d2 = this.computeMaxMinAreaHeight(list, marginAccessor, this.getAlignmentInternal().getVpos());
        }
        return this.snapSpaceY(insets.getTop()) + d2 + this.snapSpaceY(insets.getBottom());
    }

    @Override
    protected double computePrefWidth(double d) {
        Insets insets = this.getInsets();
        return this.snapSpaceX(insets.getLeft()) + this.computeContentWidth(this.getManagedChildren(), d, false) + this.snapSpaceX(insets.getRight());
    }

    @Override
    protected double computePrefHeight(double d) {
        Insets insets = this.getInsets();
        List<Node> list = this.getManagedChildren();
        double d2 = 0.0;
        if (d != -1.0 && this.getContentBias() != null) {
            double[][] dArray = this.getAreaWidths(list, -1.0, false);
            this.adjustAreaWidths(list, dArray, d, -1.0);
            d2 = this.computeMaxPrefAreaHeight(list, marginAccessor, dArray[0], this.getAlignmentInternal().getVpos());
        } else {
            d2 = this.computeMaxPrefAreaHeight(list, marginAccessor, this.getAlignmentInternal().getVpos());
        }
        return this.snapSpaceY(insets.getTop()) + d2 + this.snapSpaceY(insets.getBottom());
    }

    private double[][] getAreaWidths(List<Node> list, double d, boolean bl) {
        double[][] dArray = this.getTempArray(list.size());
        double d2 = d == -1.0 ? -1.0 : d - this.snapSpaceY(this.getInsets().getTop()) - this.snapSpaceY(this.getInsets().getBottom());
        boolean bl2 = this.shouldFillHeight();
        int n = list.size();
        for (int i = 0; i < n; ++i) {
            Node node = list.get(i);
            Insets insets = HBox.getMargin(node);
            dArray[0][i] = bl ? this.computeChildMinAreaWidth(node, this.getMinBaselineComplement(), insets, d2, bl2) : this.computeChildPrefAreaWidth(node, this.getPrefBaselineComplement(), insets, d2, bl2);
        }
        return dArray;
    }

    private double adjustAreaWidths(List<Node> list, double[][] dArray, double d, double d2) {
        Insets insets = this.getInsets();
        double d3 = this.snapSpaceY(insets.getTop());
        double d4 = this.snapSpaceY(insets.getBottom());
        double d5 = HBox.sum(dArray[0], list.size()) + (double)(list.size() - 1) * this.snapSpaceX(this.getSpacing());
        double d6 = d - this.snapSpaceX(insets.getLeft()) - this.snapSpaceX(insets.getRight()) - d5;
        if (d6 != 0.0) {
            double d7 = this.shouldFillHeight() && d2 != -1.0 ? d2 - d3 - d4 : -1.0;
            double d8 = this.growOrShrinkAreaWidths(list, dArray, Priority.ALWAYS, d6, d7);
            d8 = this.growOrShrinkAreaWidths(list, dArray, Priority.SOMETIMES, d8, d7);
            d5 += d6 - d8;
        }
        return d5;
    }

    private double growOrShrinkAreaWidths(List<Node> list, double[][] dArray, Priority priority, double d, double d2) {
        Node node;
        int n;
        int n2;
        boolean bl = d < 0.0;
        int n3 = 0;
        double[] dArray2 = dArray[0];
        double[] dArray3 = dArray[1];
        boolean bl2 = this.shouldFillHeight();
        if (bl) {
            n3 = list.size();
            n2 = list.size();
            for (n = 0; n < n2; ++n) {
                node = list.get(n);
                dArray3[n] = this.computeChildMinAreaWidth(node, this.getMinBaselineComplement(), HBox.getMargin(node), d2, bl2);
            }
        } else {
            n2 = list.size();
            for (n = 0; n < n2; ++n) {
                node = list.get(n);
                if (HBox.getHgrow(node) == priority) {
                    dArray3[n] = this.computeChildMaxAreaWidth(node, this.getMinBaselineComplement(), HBox.getMargin(node), d2, bl2);
                    ++n3;
                    continue;
                }
                dArray3[n] = -1.0;
            }
        }
        double d3 = d;
        block2: while (Math.abs(d3) > 1.0 && n3 > 0) {
            double d4 = this.snapPortionX(d3 / (double)n3);
            int n4 = list.size();
            for (int i = 0; i < n4; ++i) {
                if (dArray3[i] == -1.0) continue;
                double d5 = dArray3[i] - dArray2[i];
                double d6 = Math.abs(d5) <= Math.abs(d4) ? d5 : d4;
                int n5 = i;
                dArray2[n5] = dArray2[n5] + d6;
                if (Math.abs(d3 -= d6) < 1.0) break block2;
                if (!(Math.abs(d6) < Math.abs(d4))) continue;
                dArray3[i] = -1.0;
                --n3;
            }
        }
        return d3;
    }

    private double computeContentWidth(List<Node> list, double d, boolean bl) {
        return HBox.sum(this.getAreaWidths(list, d, bl)[0], list.size()) + (double)(list.size() - 1) * this.snapSpaceX(this.getSpacing());
    }

    private static double sum(double[] dArray, int n) {
        int n2 = 0;
        double d = 0.0;
        while (n2 != n) {
            d += dArray[n2++];
        }
        return d;
    }

    @Override
    public void requestLayout() {
        this.biasDirty = true;
        this.bias = null;
        this.minBaselineComplement = Double.NaN;
        this.prefBaselineComplement = Double.NaN;
        this.baselineOffset = Double.NaN;
        super.requestLayout();
    }

    private double getMinBaselineComplement() {
        if (Double.isNaN(this.minBaselineComplement)) {
            this.minBaselineComplement = this.getAlignmentInternal().getVpos() == VPos.BASELINE ? HBox.getMinBaselineComplement(this.getManagedChildren()) : -1.0;
        }
        return this.minBaselineComplement;
    }

    private double getPrefBaselineComplement() {
        if (Double.isNaN(this.prefBaselineComplement)) {
            this.prefBaselineComplement = this.getAlignmentInternal().getVpos() == VPos.BASELINE ? HBox.getPrefBaselineComplement(this.getManagedChildren()) : -1.0;
        }
        return this.prefBaselineComplement;
    }

    @Override
    public double getBaselineOffset() {
        List list = this.getManagedChildren();
        if (list.isEmpty()) {
            return Double.NEGATIVE_INFINITY;
        }
        if (Double.isNaN(this.baselineOffset)) {
            VPos vPos = this.getAlignmentInternal().getVpos();
            if (vPos == VPos.BASELINE) {
                double d = 0.0;
                int n = list.size();
                for (int i = 0; i < n; ++i) {
                    Node node = (Node)list.get(i);
                    double d2 = node.getBaselineOffset();
                    if (d2 == Double.NEGATIVE_INFINITY) {
                        this.baselineOffset = Double.NEGATIVE_INFINITY;
                        break;
                    }
                    Insets insets = HBox.getMargin(node);
                    double d3 = insets != null ? insets.getTop() : 0.0;
                    d = Math.max(d, d3 + node.getLayoutBounds().getMinY() + d2);
                }
                this.baselineOffset = d + this.snappedTopInset();
            } else {
                this.baselineOffset = Double.NEGATIVE_INFINITY;
            }
        }
        return this.baselineOffset;
    }

    @Override
    protected void layoutChildren() {
        List<Node> list = this.getManagedChildren();
        Insets insets = this.getInsets();
        Pos pos = this.getAlignmentInternal();
        HPos hPos = pos.getHpos();
        VPos vPos = pos.getVpos();
        double d = this.getWidth();
        double d2 = this.getHeight();
        double d3 = this.snapSpaceY(insets.getTop());
        double d4 = this.snapSpaceX(insets.getLeft());
        double d5 = this.snapSpaceY(insets.getBottom());
        double d6 = this.snapSpaceX(insets.getRight());
        double d7 = this.snapSpaceX(this.getSpacing());
        boolean bl = this.shouldFillHeight();
        double[][] dArray = this.getAreaWidths(list, d2, false);
        double d8 = this.adjustAreaWidths(list, dArray, d, d2);
        double d9 = d2 - d3 - d5;
        double d10 = d4 + HBox.computeXOffset(d - d4 - d6, d8, pos.getHpos());
        double d11 = d3;
        double d12 = -1.0;
        if (vPos == VPos.BASELINE) {
            double d13 = this.getMinBaselineComplement();
            d12 = this.getAreaBaselineOffset(list, marginAccessor, (Integer n) -> dArray[0][n], d9, bl, d13);
        }
        int n2 = list.size();
        for (int i = 0; i < n2; ++i) {
            Node node = list.get(i);
            Insets insets2 = HBox.getMargin(node);
            this.layoutInArea(node, d10, d11, dArray[0][i], d9, d12, insets2, true, bl, hPos, vPos);
            d10 += dArray[0][i] + d7;
        }
    }

    private double[][] getTempArray(int n) {
        if (this.tempArray == null) {
            this.tempArray = new double[2][n];
        } else if (this.tempArray[0].length < n) {
            this.tempArray = new double[2][Math.max(this.tempArray.length * 3, n)];
        }
        return this.tempArray;
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return HBox.getClassCssMetaData();
    }

    private static class StyleableProperties {
        private static final CssMetaData<HBox, Pos> ALIGNMENT = new CssMetaData<HBox, Pos>("-fx-alignment", new EnumConverter<Pos>(Pos.class), Pos.TOP_LEFT){

            @Override
            public boolean isSettable(HBox hBox) {
                return hBox.alignment == null || !hBox.alignment.isBound();
            }

            @Override
            public StyleableProperty<Pos> getStyleableProperty(HBox hBox) {
                return (StyleableProperty)((Object)hBox.alignmentProperty());
            }
        };
        private static final CssMetaData<HBox, Boolean> FILL_HEIGHT = new CssMetaData<HBox, Boolean>("-fx-fill-height", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(HBox hBox) {
                return hBox.fillHeight == null || !hBox.fillHeight.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(HBox hBox) {
                return (StyleableProperty)((Object)hBox.fillHeightProperty());
            }
        };
        private static final CssMetaData<HBox, Number> SPACING = new CssMetaData<HBox, Number>("-fx-spacing", SizeConverter.getInstance(), (Number)0.0){

            @Override
            public boolean isSettable(HBox hBox) {
                return hBox.spacing == null || !hBox.spacing.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(HBox hBox) {
                return (StyleableProperty)((Object)hBox.spacingProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Pane.getClassCssMetaData());
            arrayList.add(FILL_HEIGHT);
            arrayList.add(ALIGNMENT);
            arrayList.add(SPACING);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

