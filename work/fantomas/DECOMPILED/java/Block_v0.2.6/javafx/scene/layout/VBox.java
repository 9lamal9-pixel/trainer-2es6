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
import javafx.scene.layout.Region;
import javafx.util.Callback;

public class VBox
extends Pane {
    private boolean biasDirty = true;
    private Orientation bias;
    private double[][] tempArray;
    private static final String MARGIN_CONSTRAINT = "vbox-margin";
    private static final String VGROW_CONSTRAINT = "vbox-vgrow";
    private static final Callback<Node, Insets> marginAccessor = node -> VBox.getMargin(node);
    private DoubleProperty spacing;
    private ObjectProperty<Pos> alignment;
    private BooleanProperty fillWidth;

    public static void setVgrow(Node node, Priority priority) {
        VBox.setConstraint(node, VGROW_CONSTRAINT, (Object)priority);
    }

    public static Priority getVgrow(Node node) {
        return (Priority)((Object)VBox.getConstraint(node, VGROW_CONSTRAINT));
    }

    public static void setMargin(Node node, Insets insets) {
        VBox.setConstraint(node, MARGIN_CONSTRAINT, insets);
    }

    public static Insets getMargin(Node node) {
        return (Insets)VBox.getConstraint(node, MARGIN_CONSTRAINT);
    }

    public static void clearConstraints(Node node) {
        VBox.setVgrow(node, null);
        VBox.setMargin(node, null);
    }

    public VBox() {
    }

    public VBox(double d) {
        this();
        this.setSpacing(d);
    }

    public VBox(Node ... nodeArray) {
        this.getChildren().addAll((Node[])nodeArray);
    }

    public VBox(double d, Node ... nodeArray) {
        this();
        this.setSpacing(d);
        this.getChildren().addAll((Node[])nodeArray);
    }

    public final DoubleProperty spacingProperty() {
        if (this.spacing == null) {
            this.spacing = new StyleableDoubleProperty(){

                @Override
                public void invalidated() {
                    VBox.this.requestLayout();
                }

                @Override
                public Object getBean() {
                    return VBox.this;
                }

                @Override
                public String getName() {
                    return "spacing";
                }

                @Override
                public CssMetaData<VBox, Number> getCssMetaData() {
                    return StyleableProperties.SPACING;
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
                    VBox.this.requestLayout();
                }

                @Override
                public Object getBean() {
                    return VBox.this;
                }

                @Override
                public String getName() {
                    return "alignment";
                }

                @Override
                public CssMetaData<VBox, Pos> getCssMetaData() {
                    return StyleableProperties.ALIGNMENT;
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

    public final BooleanProperty fillWidthProperty() {
        if (this.fillWidth == null) {
            this.fillWidth = new StyleableBooleanProperty(true){

                @Override
                public void invalidated() {
                    VBox.this.requestLayout();
                }

                @Override
                public Object getBean() {
                    return VBox.this;
                }

                @Override
                public String getName() {
                    return "fillWidth";
                }

                @Override
                public CssMetaData<VBox, Boolean> getCssMetaData() {
                    return StyleableProperties.FILL_WIDTH;
                }
            };
        }
        return this.fillWidth;
    }

    public final void setFillWidth(boolean bl) {
        this.fillWidthProperty().set(bl);
    }

    public final boolean isFillWidth() {
        return this.fillWidth == null ? true : this.fillWidth.get();
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
        List<Node> list = this.getManagedChildren();
        double d2 = 0.0;
        if (d != -1.0 && this.getContentBias() != null) {
            double[][] dArray = this.getAreaHeights(list, -1.0, false);
            this.adjustAreaHeights(list, dArray, d, -1.0);
            d2 = this.computeMaxMinAreaWidth(list, marginAccessor, dArray[0], false);
        } else {
            d2 = this.computeMaxMinAreaWidth(list, marginAccessor);
        }
        return this.snapSpaceX(insets.getLeft()) + d2 + this.snapSpaceX(insets.getRight());
    }

    @Override
    protected double computeMinHeight(double d) {
        Insets insets = this.getInsets();
        return this.snapSpaceY(insets.getTop()) + this.computeContentHeight(this.getManagedChildren(), d, true) + this.snapSpaceY(insets.getBottom());
    }

    @Override
    protected double computePrefWidth(double d) {
        Insets insets = this.getInsets();
        List<Node> list = this.getManagedChildren();
        double d2 = 0.0;
        if (d != -1.0 && this.getContentBias() != null) {
            double[][] dArray = this.getAreaHeights(list, -1.0, false);
            this.adjustAreaHeights(list, dArray, d, -1.0);
            d2 = this.computeMaxPrefAreaWidth(list, marginAccessor, dArray[0], false);
        } else {
            d2 = this.computeMaxPrefAreaWidth(list, marginAccessor);
        }
        return this.snapSpaceX(insets.getLeft()) + d2 + this.snapSpaceX(insets.getRight());
    }

    @Override
    protected double computePrefHeight(double d) {
        Insets insets = this.getInsets();
        double d2 = this.snapSpaceY(insets.getTop()) + this.computeContentHeight(this.getManagedChildren(), d, false) + this.snapSpaceY(insets.getBottom());
        return d2;
    }

    private double[][] getAreaHeights(List<Node> list, double d, boolean bl) {
        double[][] dArray = this.getTempArray(list.size());
        double d2 = d == -1.0 ? -1.0 : d - this.snapSpaceX(this.getInsets().getLeft()) - this.snapSpaceX(this.getInsets().getRight());
        boolean bl2 = this.isFillWidth();
        int n = list.size();
        for (int i = 0; i < n; ++i) {
            Node node = list.get(i);
            Insets insets = VBox.getMargin(node);
            if (bl) {
                if (d2 != -1.0 && bl2) {
                    dArray[0][i] = this.computeChildMinAreaHeight(node, -1.0, insets, d2);
                    continue;
                }
                dArray[0][i] = this.computeChildMinAreaHeight(node, -1.0, insets, -1.0);
                continue;
            }
            dArray[0][i] = d2 != -1.0 && bl2 ? this.computeChildPrefAreaHeight(node, -1.0, insets, d2) : this.computeChildPrefAreaHeight(node, -1.0, insets, -1.0);
        }
        return dArray;
    }

    private double adjustAreaHeights(List<Node> list, double[][] dArray, double d, double d2) {
        Insets insets = this.getInsets();
        double d3 = this.snapSpaceX(insets.getLeft());
        double d4 = this.snapSpaceX(insets.getRight());
        double d5 = VBox.sum(dArray[0], list.size()) + (double)(list.size() - 1) * this.snapSpaceY(this.getSpacing());
        double d6 = d - this.snapSpaceY(insets.getTop()) - this.snapSpaceY(insets.getBottom()) - d5;
        if (d6 != 0.0) {
            double d7 = this.isFillWidth() && d2 != -1.0 ? d2 - d3 - d4 : -1.0;
            double d8 = this.growOrShrinkAreaHeights(list, dArray, Priority.ALWAYS, d6, d7);
            d8 = this.growOrShrinkAreaHeights(list, dArray, Priority.SOMETIMES, d8, d7);
            d5 += d6 - d8;
        }
        return d5;
    }

    private double growOrShrinkAreaHeights(List<Node> list, double[][] dArray, Priority priority, double d, double d2) {
        Node node;
        int n;
        int n2;
        boolean bl = d < 0.0;
        int n3 = 0;
        double[] dArray2 = dArray[0];
        double[] dArray3 = dArray[1];
        if (bl) {
            n3 = list.size();
            n2 = list.size();
            for (n = 0; n < n2; ++n) {
                node = list.get(n);
                dArray3[n] = this.computeChildMinAreaHeight(node, -1.0, VBox.getMargin(node), d2);
            }
        } else {
            n2 = list.size();
            for (n = 0; n < n2; ++n) {
                node = list.get(n);
                if (VBox.getVgrow(node) == priority) {
                    dArray3[n] = this.computeChildMaxAreaHeight(node, -1.0, VBox.getMargin(node), d2);
                    ++n3;
                    continue;
                }
                dArray3[n] = -1.0;
            }
        }
        double d3 = d;
        block2: while (Math.abs(d3) > 1.0 && n3 > 0) {
            double d4 = this.snapPortionY(d3 / (double)n3);
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

    private double computeContentHeight(List<Node> list, double d, boolean bl) {
        return VBox.sum(this.getAreaHeights(list, d, bl)[0], list.size()) + (double)(list.size() - 1) * this.snapSpaceY(this.getSpacing());
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
        super.requestLayout();
    }

    @Override
    protected void layoutChildren() {
        List<Node> list = this.getManagedChildren();
        Insets insets = this.getInsets();
        double d = this.getWidth();
        double d2 = this.getHeight();
        double d3 = this.snapSpaceY(insets.getTop());
        double d4 = this.snapSpaceX(insets.getLeft());
        double d5 = this.snapSpaceY(insets.getBottom());
        double d6 = this.snapSpaceX(insets.getRight());
        double d7 = this.snapSpaceY(this.getSpacing());
        HPos hPos = this.getAlignmentInternal().getHpos();
        VPos vPos = this.getAlignmentInternal().getVpos();
        boolean bl = this.isFillWidth();
        double[][] dArray = this.getAreaHeights(list, d, false);
        double d8 = d - d4 - d6;
        double d9 = this.adjustAreaHeights(list, dArray, d2, d);
        double d10 = d4;
        double d11 = d3 + VBox.computeYOffset(d2 - d3 - d5, d9, vPos);
        int n = list.size();
        for (int i = 0; i < n; ++i) {
            Node node = list.get(i);
            this.layoutInArea(node, d10, d11, d8, dArray[0][i], dArray[0][i], VBox.getMargin(node), bl, true, hPos, vPos);
            d11 += dArray[0][i] + d7;
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
        return VBox.getClassCssMetaData();
    }

    private static class StyleableProperties {
        private static final CssMetaData<VBox, Pos> ALIGNMENT = new CssMetaData<VBox, Pos>("-fx-alignment", new EnumConverter<Pos>(Pos.class), Pos.TOP_LEFT){

            @Override
            public boolean isSettable(VBox vBox) {
                return vBox.alignment == null || !vBox.alignment.isBound();
            }

            @Override
            public StyleableProperty<Pos> getStyleableProperty(VBox vBox) {
                return (StyleableProperty)((Object)vBox.alignmentProperty());
            }
        };
        private static final CssMetaData<VBox, Boolean> FILL_WIDTH = new CssMetaData<VBox, Boolean>("-fx-fill-width", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(VBox vBox) {
                return vBox.fillWidth == null || !vBox.fillWidth.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(VBox vBox) {
                return (StyleableProperty)((Object)vBox.fillWidthProperty());
            }
        };
        private static final CssMetaData<VBox, Number> SPACING = new CssMetaData<VBox, Number>("-fx-spacing", SizeConverter.getInstance(), (Number)0.0){

            @Override
            public boolean isSettable(VBox vBox) {
                return vBox.spacing == null || !vBox.spacing.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(VBox vBox) {
                return (StyleableProperty)((Object)vBox.spacingProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Region.getClassCssMetaData());
            arrayList.add(ALIGNMENT);
            arrayList.add(FILL_WIDTH);
            arrayList.add(SPACING);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

