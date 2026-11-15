/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.text;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.RectBounds;
import com.sun.javafx.scene.text.GlyphList;
import com.sun.javafx.scene.text.TextLayout;
import com.sun.javafx.scene.text.TextSpan;
import com.sun.javafx.tk.Toolkit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.css.converter.SizeConverter;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.PathElement;
import javafx.scene.text.HitInfo;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class TextFlow
extends Pane {
    private TextLayout layout;
    private boolean needsContent;
    private boolean inLayout;
    private ObjectProperty<TextAlignment> textAlignment;
    private DoubleProperty lineSpacing;

    public TextFlow() {
        this.effectiveNodeOrientationProperty().addListener(observable -> this.checkOrientation());
        this.setAccessibleRole(AccessibleRole.TEXT);
    }

    public TextFlow(Node ... nodeArray) {
        this();
        this.getChildren().addAll((Node[])nodeArray);
    }

    private void checkOrientation() {
        NodeOrientation nodeOrientation = this.getEffectiveNodeOrientation();
        boolean bl = nodeOrientation == NodeOrientation.RIGHT_TO_LEFT;
        int n = bl ? 2048 : 1024;
        TextLayout textLayout = this.getTextLayout();
        if (textLayout.setDirection(n)) {
            this.requestLayout();
        }
    }

    public final HitInfo hitTest(Point2D point2D) {
        if (point2D != null) {
            TextLayout textLayout = this.getTextLayout();
            double d = point2D.getX();
            double d2 = point2D.getY();
            TextLayout.Hit hit = textLayout.getHitInfo((float)d, (float)d2);
            return new HitInfo(hit.getCharIndex(), hit.getInsertionIndex(), hit.isLeading(), null);
        }
        return null;
    }

    public PathElement[] caretShape(int n, boolean bl) {
        return this.getTextLayout().getCaretShape(n, bl, 0.0f, 0.0f);
    }

    public final PathElement[] rangeShape(int n, int n2) {
        return this.getRange(n, n2, 1);
    }

    @Override
    public boolean usesMirroring() {
        return false;
    }

    @Override
    protected void setWidth(double d) {
        if (d != this.getWidth()) {
            TextLayout textLayout = this.getTextLayout();
            Insets insets = this.getInsets();
            double d2 = this.snapSpaceX(insets.getLeft());
            double d3 = this.snapSpaceX(insets.getRight());
            double d4 = Math.max(1.0, d - d2 - d3);
            textLayout.setWrapWidth((float)d4);
            super.setWidth(d);
        }
    }

    @Override
    protected double computePrefWidth(double d) {
        TextLayout textLayout = this.getTextLayout();
        textLayout.setWrapWidth(0.0f);
        double d2 = textLayout.getBounds().getWidth();
        Insets insets = this.getInsets();
        double d3 = this.snapSpaceX(insets.getLeft());
        double d4 = this.snapSpaceX(insets.getRight());
        double d5 = Math.max(1.0, this.getWidth() - d3 - d4);
        textLayout.setWrapWidth((float)d5);
        return d3 + d2 + d4;
    }

    @Override
    protected double computePrefHeight(double d) {
        double d2;
        TextLayout textLayout = this.getTextLayout();
        Insets insets = this.getInsets();
        double d3 = this.snapSpaceX(insets.getLeft());
        double d4 = this.snapSpaceX(insets.getRight());
        if (d == -1.0) {
            textLayout.setWrapWidth(0.0f);
        } else {
            d2 = Math.max(1.0, d - d3 - d4);
            textLayout.setWrapWidth((float)d2);
        }
        d2 = textLayout.getBounds().getHeight();
        double d5 = Math.max(1.0, this.getWidth() - d3 - d4);
        textLayout.setWrapWidth((float)d5);
        double d6 = this.snapSpaceY(insets.getTop());
        double d7 = this.snapSpaceY(insets.getBottom());
        return d6 + d2 + d7;
    }

    @Override
    protected double computeMinHeight(double d) {
        return this.computePrefHeight(d);
    }

    @Override
    public void requestLayout() {
        if (this.inLayout) {
            return;
        }
        this.needsContent = true;
        super.requestLayout();
    }

    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    @Override
    protected void layoutChildren() {
        Object object;
        Node node;
        this.inLayout = true;
        Insets insets = this.getInsets();
        double d = this.snapSpaceY(insets.getTop());
        double d2 = this.snapSpaceX(insets.getLeft());
        GlyphList[] glyphListArray = this.getTextLayout().getRuns();
        for (int i = 0; i < glyphListArray.length; ++i) {
            GlyphList glyphList = glyphListArray[i];
            Object object2 = glyphList.getTextSpan();
            if (!(object2 instanceof EmbeddedSpan)) continue;
            node = ((EmbeddedSpan)object2).getNode();
            object = glyphList.getLocation();
            double d3 = -glyphList.getLineBounds().getMinY();
            this.layoutInArea(node, d2 + (double)((com.sun.javafx.geom.Point2D)object).x, d + (double)((com.sun.javafx.geom.Point2D)object).y, glyphList.getWidth(), glyphList.getHeight(), d3, null, true, true, HPos.CENTER, VPos.BASELINE);
        }
        List list = this.getManagedChildren();
        for (Object object2 : list) {
            if (!(object2 instanceof Text)) continue;
            node = (Text)object2;
            ((Text)node).layoutSpan(glyphListArray);
            object = ((Text)node).getSpanBounds();
            node.relocate(d2 + (double)((BaseBounds)object).getMinX(), d + (double)((BaseBounds)object).getMinY());
        }
        this.inLayout = false;
    }

    private PathElement[] getRange(int n, int n2, int n3) {
        TextLayout textLayout = this.getTextLayout();
        return textLayout.getRange(n, n2, n3, 0.0f, 0.0f);
    }

    TextLayout getTextLayout() {
        Object object;
        if (this.layout == null) {
            object = Toolkit.getToolkit().getTextLayoutFactory();
            this.layout = object.createLayout();
            this.needsContent = true;
        }
        if (this.needsContent) {
            object = this.getManagedChildren();
            TextSpan[] textSpanArray = new TextSpan[object.size()];
            for (int i = 0; i < textSpanArray.length; ++i) {
                Node node = (Node)object.get(i);
                if (node instanceof Text) {
                    textSpanArray[i] = ((Text)node).getTextSpan();
                    continue;
                }
                double d = node.getBaselineOffset();
                if (d == Double.NEGATIVE_INFINITY) {
                    d = node.getLayoutBounds().getHeight();
                }
                double d2 = this.computeChildPrefAreaWidth(node, null);
                double d3 = this.computeChildPrefAreaHeight(node, null);
                textSpanArray[i] = new EmbeddedSpan(node, d, d2, d3);
            }
            this.layout.setContent(textSpanArray);
            this.needsContent = false;
        }
        return this.layout;
    }

    public final void setTextAlignment(TextAlignment textAlignment) {
        this.textAlignmentProperty().set(textAlignment);
    }

    public final TextAlignment getTextAlignment() {
        return this.textAlignment == null ? TextAlignment.LEFT : (TextAlignment)((Object)this.textAlignment.get());
    }

    public final ObjectProperty<TextAlignment> textAlignmentProperty() {
        if (this.textAlignment == null) {
            this.textAlignment = new StyleableObjectProperty<TextAlignment>(TextAlignment.LEFT){

                @Override
                public Object getBean() {
                    return TextFlow.this;
                }

                @Override
                public String getName() {
                    return "textAlignment";
                }

                @Override
                public CssMetaData<TextFlow, TextAlignment> getCssMetaData() {
                    return StyleableProperties.TEXT_ALIGNMENT;
                }

                @Override
                public void invalidated() {
                    TextAlignment textAlignment = (TextAlignment)((Object)this.get());
                    if (textAlignment == null) {
                        textAlignment = TextAlignment.LEFT;
                    }
                    TextLayout textLayout = TextFlow.this.getTextLayout();
                    textLayout.setAlignment(textAlignment.ordinal());
                    TextFlow.this.requestLayout();
                }
            };
        }
        return this.textAlignment;
    }

    public final void setLineSpacing(double d) {
        this.lineSpacingProperty().set(d);
    }

    public final double getLineSpacing() {
        return this.lineSpacing == null ? 0.0 : this.lineSpacing.get();
    }

    public final DoubleProperty lineSpacingProperty() {
        if (this.lineSpacing == null) {
            this.lineSpacing = new StyleableDoubleProperty(0.0){

                @Override
                public Object getBean() {
                    return TextFlow.this;
                }

                @Override
                public String getName() {
                    return "lineSpacing";
                }

                @Override
                public CssMetaData<TextFlow, Number> getCssMetaData() {
                    return StyleableProperties.LINE_SPACING;
                }

                @Override
                public void invalidated() {
                    TextLayout textLayout = TextFlow.this.getTextLayout();
                    if (textLayout.setLineSpacing((float)this.get())) {
                        TextFlow.this.requestLayout();
                    }
                }
            };
        }
        return this.lineSpacing;
    }

    @Override
    public final double getBaselineOffset() {
        Insets insets = this.getInsets();
        double d = this.snapSpaceY(insets.getTop());
        return d - (double)this.getTextLayout().getBounds().getMinY();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return TextFlow.getClassCssMetaData();
    }

    private static double snapSpace(double d, boolean bl) {
        return bl ? (double)Math.round(d) : d;
    }

    static double boundedSize(double d, double d2, double d3) {
        double d4 = d2 >= d ? d2 : d;
        double d5 = d >= d3 ? d : d3;
        return d4 <= d5 ? d4 : d5;
    }

    double computeChildPrefAreaWidth(Node node, Insets insets) {
        return this.computeChildPrefAreaWidth(node, insets, -1.0);
    }

    double computeChildPrefAreaWidth(Node node, Insets insets, double d) {
        boolean bl = this.isSnapToPixel();
        double d2 = insets != null ? TextFlow.snapSpace(insets.getTop(), bl) : 0.0;
        double d3 = insets != null ? TextFlow.snapSpace(insets.getBottom(), bl) : 0.0;
        double d4 = insets != null ? TextFlow.snapSpace(insets.getLeft(), bl) : 0.0;
        double d5 = insets != null ? TextFlow.snapSpace(insets.getRight(), bl) : 0.0;
        double d6 = -1.0;
        if (node.getContentBias() == Orientation.VERTICAL) {
            d6 = this.snapSizeY(TextFlow.boundedSize(node.minHeight(-1.0), d != -1.0 ? d - d2 - d3 : node.prefHeight(-1.0), node.maxHeight(-1.0)));
        }
        return d4 + this.snapSizeX(TextFlow.boundedSize(node.minWidth(d6), node.prefWidth(d6), node.maxWidth(d6))) + d5;
    }

    double computeChildPrefAreaHeight(Node node, Insets insets) {
        return this.computeChildPrefAreaHeight(node, insets, -1.0);
    }

    double computeChildPrefAreaHeight(Node node, Insets insets, double d) {
        boolean bl = this.isSnapToPixel();
        double d2 = insets != null ? TextFlow.snapSpace(insets.getTop(), bl) : 0.0;
        double d3 = insets != null ? TextFlow.snapSpace(insets.getBottom(), bl) : 0.0;
        double d4 = insets != null ? TextFlow.snapSpace(insets.getLeft(), bl) : 0.0;
        double d5 = insets != null ? TextFlow.snapSpace(insets.getRight(), bl) : 0.0;
        double d6 = -1.0;
        if (node.getContentBias() == Orientation.HORIZONTAL) {
            d6 = this.snapSizeX(TextFlow.boundedSize(node.minWidth(-1.0), d != -1.0 ? d - d4 - d5 : node.prefWidth(-1.0), node.maxWidth(-1.0)));
        }
        return d2 + this.snapSizeY(TextFlow.boundedSize(node.minHeight(d6), node.prefHeight(d6), node.maxHeight(d6))) + d3;
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case TEXT: {
                String string = this.getAccessibleText();
                if (string != null && !string.isEmpty()) {
                    return string;
                }
                StringBuilder stringBuilder = new StringBuilder();
                for (Node node : this.getChildren()) {
                    Object object = node.queryAccessibleAttribute(AccessibleAttribute.TEXT, objectArray);
                    if (object == null) continue;
                    stringBuilder.append(object.toString());
                }
                return stringBuilder.toString();
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    private static class StyleableProperties {
        private static final CssMetaData<TextFlow, TextAlignment> TEXT_ALIGNMENT = new CssMetaData<TextFlow, TextAlignment>("-fx-text-alignment", new EnumConverter<TextAlignment>(TextAlignment.class), TextAlignment.LEFT){

            @Override
            public boolean isSettable(TextFlow textFlow) {
                return textFlow.textAlignment == null || !textFlow.textAlignment.isBound();
            }

            @Override
            public StyleableProperty<TextAlignment> getStyleableProperty(TextFlow textFlow) {
                return (StyleableProperty)((Object)textFlow.textAlignmentProperty());
            }
        };
        private static final CssMetaData<TextFlow, Number> LINE_SPACING = new CssMetaData<TextFlow, Number>("-fx-line-spacing", SizeConverter.getInstance(), (Number)0){

            @Override
            public boolean isSettable(TextFlow textFlow) {
                return textFlow.lineSpacing == null || !textFlow.lineSpacing.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(TextFlow textFlow) {
                return (StyleableProperty)((Object)textFlow.lineSpacingProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Pane.getClassCssMetaData());
            arrayList.add(TEXT_ALIGNMENT);
            arrayList.add(LINE_SPACING);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }

    private static class EmbeddedSpan
    implements TextSpan {
        RectBounds bounds;
        Node node;

        public EmbeddedSpan(Node node, double d, double d2, double d3) {
            this.node = node;
            this.bounds = new RectBounds(0.0f, (float)(-d), (float)d2, (float)(d3 - d));
        }

        @Override
        public String getText() {
            return "\ufffc";
        }

        @Override
        public Object getFont() {
            return null;
        }

        @Override
        public RectBounds getBounds() {
            return this.bounds;
        }

        public Node getNode() {
            return this.node;
        }
    }
}

