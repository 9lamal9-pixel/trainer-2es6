/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.text;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.Path2D;
import com.sun.javafx.geom.RectBounds;
import com.sun.javafx.geom.Shape;
import com.sun.javafx.geom.TransformedShape;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.shape.ShapeHelper;
import com.sun.javafx.scene.shape.TextHelper;
import com.sun.javafx.scene.text.FontHelper;
import com.sun.javafx.scene.text.GlyphList;
import com.sun.javafx.scene.text.TextLayout;
import com.sun.javafx.scene.text.TextLayoutFactory;
import com.sun.javafx.scene.text.TextLine;
import com.sun.javafx.scene.text.TextSpan;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.sg.prism.NGShape;
import com.sun.javafx.sg.prism.NGText;
import com.sun.javafx.tk.Toolkit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.css.CssMetaData;
import javafx.css.FontCssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.EnumConverter;
import javafx.css.converter.SizeConverter;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.HitInfo;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBoundsType;
import javafx.scene.text.TextFlow;

@DefaultProperty(value="text")
public class Text
extends javafx.scene.shape.Shape {
    private TextLayout layout;
    private static final PathElement[] EMPTY_PATH_ELEMENT_ARRAY;
    private boolean isSpan;
    private TextSpan textSpan;
    private GlyphList[] textRuns;
    private BaseBounds spanBounds;
    private boolean spanBoundsInvalid;
    private StringProperty text;
    private DoubleProperty x;
    private DoubleProperty y;
    private ObjectProperty<Font> font;
    private ObjectProperty<TextBoundsType> boundsType;
    private DoubleProperty wrappingWidth;
    private ObjectProperty<FontSmoothingType> fontSmoothingType;
    private TextAttribute attributes;
    private static final VPos DEFAULT_TEXT_ORIGIN;
    private static final TextBoundsType DEFAULT_BOUNDS_TYPE;
    private static final boolean DEFAULT_UNDERLINE = false;
    private static final boolean DEFAULT_STRIKETHROUGH = false;
    private static final TextAlignment DEFAULT_TEXT_ALIGNMENT;
    private static final double DEFAULT_LINE_SPACING = 0.0;
    private static final int DEFAULT_CARET_POSITION = -1;
    private static final int DEFAULT_SELECTION_START = -1;
    private static final int DEFAULT_SELECTION_END = -1;
    private static final Color DEFAULT_SELECTION_FILL;
    private static final boolean DEFAULT_CARET_BIAS = true;

    public Text() {
        TextHelper.initHelper(this);
        this.textRuns = null;
        this.spanBounds = new RectBounds();
        this.spanBoundsInvalid = true;
        this.setAccessibleRole(AccessibleRole.TEXT);
        InvalidationListener invalidationListener = observable -> this.checkSpan();
        this.parentProperty().addListener(invalidationListener);
        this.managedProperty().addListener(invalidationListener);
        this.effectiveNodeOrientationProperty().addListener(observable -> this.checkOrientation());
        this.setPickOnBounds(true);
    }

    public Text(String string) {
        this();
        this.setText(string);
    }

    public Text(double d, double d2, String string) {
        this(string);
        this.setX(d);
        this.setY(d2);
    }

    private NGNode doCreatePeer() {
        return new NGText();
    }

    private boolean isSpan() {
        return this.isSpan;
    }

    private void checkSpan() {
        boolean bl = this.isSpan = this.isManaged() && this.getParent() instanceof TextFlow;
        if (this.isSpan() && !this.pickOnBoundsProperty().isBound()) {
            this.setPickOnBounds(false);
        }
    }

    private void checkOrientation() {
        if (!this.isSpan()) {
            NodeOrientation nodeOrientation = this.getEffectiveNodeOrientation();
            boolean bl = nodeOrientation == NodeOrientation.RIGHT_TO_LEFT;
            int n = bl ? 2048 : 1024;
            TextLayout textLayout = this.getTextLayout();
            if (textLayout.setDirection(n)) {
                this.needsTextLayout();
            }
        }
    }

    @Override
    public boolean usesMirroring() {
        return false;
    }

    private void needsFullTextLayout() {
        if (this.isSpan()) {
            this.textSpan = null;
        } else {
            TextLayout textLayout = this.getTextLayout();
            String string = this.getTextInternal();
            Object object = this.getFontInternal();
            textLayout.setContent(string, object);
        }
        this.needsTextLayout();
    }

    private void needsTextLayout() {
        this.textRuns = null;
        NodeHelper.geomChanged(this);
        NodeHelper.markDirty(this, DirtyBits.NODE_CONTENTS);
    }

    TextSpan getTextSpan() {
        if (this.textSpan == null) {
            this.textSpan = new TextSpan(){

                @Override
                public String getText() {
                    return Text.this.getTextInternal();
                }

                @Override
                public Object getFont() {
                    return Text.this.getFontInternal();
                }

                @Override
                public RectBounds getBounds() {
                    return null;
                }
            };
        }
        return this.textSpan;
    }

    private TextLayout getTextLayout() {
        if (this.isSpan()) {
            this.layout = null;
            TextFlow textFlow = (TextFlow)this.getParent();
            return textFlow.getTextLayout();
        }
        if (this.layout == null) {
            TextLayoutFactory textLayoutFactory = Toolkit.getToolkit().getTextLayoutFactory();
            this.layout = textLayoutFactory.createLayout();
            String string = this.getTextInternal();
            Object object = this.getFontInternal();
            TextAlignment textAlignment = this.getTextAlignment();
            if (textAlignment == null) {
                textAlignment = DEFAULT_TEXT_ALIGNMENT;
            }
            this.layout.setContent(string, object);
            this.layout.setAlignment(textAlignment.ordinal());
            this.layout.setLineSpacing((float)this.getLineSpacing());
            this.layout.setWrapWidth((float)this.getWrappingWidth());
            if (this.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
                this.layout.setDirection(2048);
            } else {
                this.layout.setDirection(1024);
            }
        }
        return this.layout;
    }

    void layoutSpan(GlyphList[] glyphListArray) {
        GlyphList glyphList;
        int n;
        TextSpan textSpan = this.getTextSpan();
        int n2 = 0;
        for (n = 0; n < glyphListArray.length; ++n) {
            glyphList = glyphListArray[n];
            if (glyphList.getTextSpan() != textSpan) continue;
            ++n2;
        }
        this.textRuns = new GlyphList[n2];
        n2 = 0;
        for (n = 0; n < glyphListArray.length; ++n) {
            glyphList = glyphListArray[n];
            if (glyphList.getTextSpan() != textSpan) continue;
            this.textRuns[n2++] = glyphList;
        }
        this.spanBoundsInvalid = true;
        NodeHelper.geomChanged(this);
        NodeHelper.markDirty(this, DirtyBits.NODE_CONTENTS);
    }

    BaseBounds getSpanBounds() {
        if (this.spanBoundsInvalid) {
            GlyphList[] glyphListArray = this.getRuns();
            if (glyphListArray.length != 0) {
                float f = Float.POSITIVE_INFINITY;
                float f2 = Float.POSITIVE_INFINITY;
                float f3 = 0.0f;
                float f4 = 0.0f;
                for (int i = 0; i < glyphListArray.length; ++i) {
                    GlyphList glyphList = glyphListArray[i];
                    com.sun.javafx.geom.Point2D point2D = glyphList.getLocation();
                    float f5 = glyphList.getWidth();
                    float f6 = glyphList.getLineBounds().getHeight();
                    f = Math.min(point2D.x, f);
                    f2 = Math.min(point2D.y, f2);
                    f3 = Math.max(point2D.x + f5, f3);
                    f4 = Math.max(point2D.y + f6, f4);
                }
                this.spanBounds = this.spanBounds.deriveWithNewBounds(f, f2, 0.0f, f3, f4, 0.0f);
            } else {
                this.spanBounds = this.spanBounds.makeEmpty();
            }
            this.spanBoundsInvalid = false;
        }
        return this.spanBounds;
    }

    private GlyphList[] getRuns() {
        if (this.textRuns != null) {
            return this.textRuns;
        }
        if (this.isSpan()) {
            this.getParent().layout();
        } else {
            TextLayout textLayout = this.getTextLayout();
            this.textRuns = textLayout.getRuns();
        }
        return this.textRuns;
    }

    private Shape getShape() {
        TextLayout textLayout = this.getTextLayout();
        int n = 1;
        if (this.isStrikethrough()) {
            n |= 4;
        }
        if (this.isUnderline()) {
            n |= 2;
        }
        TextSpan textSpan = null;
        if (this.isSpan()) {
            n |= 0x10;
            textSpan = this.getTextSpan();
        } else {
            n |= 8;
        }
        return textLayout.getShape(n, textSpan);
    }

    private BaseBounds getVisualBounds() {
        if (ShapeHelper.getMode(this) == NGShape.Mode.FILL || this.getStrokeType() == StrokeType.INSIDE) {
            int n = 1;
            if (this.isStrikethrough()) {
                n |= 4;
            }
            if (this.isUnderline()) {
                n |= 2;
            }
            return this.getTextLayout().getVisualBounds(n);
        }
        return this.getShape().getBounds();
    }

    private BaseBounds getLogicalBounds() {
        TextLayout textLayout = this.getTextLayout();
        return textLayout.getBounds();
    }

    public final void setText(String string) {
        if (string == null) {
            string = "";
        }
        this.textProperty().set(string);
    }

    public final String getText() {
        return this.text == null ? "" : (String)this.text.get();
    }

    private String getTextInternal() {
        String string = this.getText();
        return string == null ? "" : string;
    }

    public final StringProperty textProperty() {
        if (this.text == null) {
            this.text = new StringPropertyBase(""){

                @Override
                public Object getBean() {
                    return Text.this;
                }

                @Override
                public String getName() {
                    return "text";
                }

                @Override
                public void invalidated() {
                    Text.this.needsFullTextLayout();
                    Text.this.setSelectionStart(-1);
                    Text.this.setSelectionEnd(-1);
                    Text.this.setCaretPosition(-1);
                    Text.this.setCaretBias(true);
                    String string = this.get();
                    if (string == null && !this.isBound()) {
                        this.set("");
                    }
                    Text.this.notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
                }
            };
        }
        return this.text;
    }

    public final void setX(double d) {
        this.xProperty().set(d);
    }

    public final double getX() {
        return this.x == null ? 0.0 : this.x.get();
    }

    public final DoubleProperty xProperty() {
        if (this.x == null) {
            this.x = new DoublePropertyBase(){

                @Override
                public Object getBean() {
                    return Text.this;
                }

                @Override
                public String getName() {
                    return "x";
                }

                @Override
                public void invalidated() {
                    NodeHelper.geomChanged(Text.this);
                }
            };
        }
        return this.x;
    }

    public final void setY(double d) {
        this.yProperty().set(d);
    }

    public final double getY() {
        return this.y == null ? 0.0 : this.y.get();
    }

    public final DoubleProperty yProperty() {
        if (this.y == null) {
            this.y = new DoublePropertyBase(){

                @Override
                public Object getBean() {
                    return Text.this;
                }

                @Override
                public String getName() {
                    return "y";
                }

                @Override
                public void invalidated() {
                    NodeHelper.geomChanged(Text.this);
                }
            };
        }
        return this.y;
    }

    public final void setFont(Font font) {
        this.fontProperty().set(font);
    }

    public final Font getFont() {
        return this.font == null ? Font.getDefault() : (Font)this.font.get();
    }

    private Object getFontInternal() {
        Font font = this.getFont();
        if (font == null) {
            font = Font.getDefault();
        }
        return FontHelper.getNativeFont(font);
    }

    public final ObjectProperty<Font> fontProperty() {
        if (this.font == null) {
            this.font = new StyleableObjectProperty<Font>(Font.getDefault()){

                @Override
                public Object getBean() {
                    return Text.this;
                }

                @Override
                public String getName() {
                    return "font";
                }

                @Override
                public CssMetaData<Text, Font> getCssMetaData() {
                    return StyleableProperties.FONT;
                }

                @Override
                public void invalidated() {
                    Text.this.needsFullTextLayout();
                    NodeHelper.markDirty(Text.this, DirtyBits.TEXT_FONT);
                }
            };
        }
        return this.font;
    }

    public final void setTextOrigin(VPos vPos) {
        this.textOriginProperty().set(vPos);
    }

    public final VPos getTextOrigin() {
        if (this.attributes == null || this.attributes.textOrigin == null) {
            return DEFAULT_TEXT_ORIGIN;
        }
        return this.attributes.getTextOrigin();
    }

    public final ObjectProperty<VPos> textOriginProperty() {
        return this.getTextAttribute().textOriginProperty();
    }

    public final void setBoundsType(TextBoundsType textBoundsType) {
        this.boundsTypeProperty().set(textBoundsType);
    }

    public final TextBoundsType getBoundsType() {
        return this.boundsType == null ? DEFAULT_BOUNDS_TYPE : (TextBoundsType)((Object)this.boundsTypeProperty().get());
    }

    public final ObjectProperty<TextBoundsType> boundsTypeProperty() {
        if (this.boundsType == null) {
            this.boundsType = new StyleableObjectProperty<TextBoundsType>(DEFAULT_BOUNDS_TYPE){

                @Override
                public Object getBean() {
                    return Text.this;
                }

                @Override
                public String getName() {
                    return "boundsType";
                }

                @Override
                public CssMetaData<Text, TextBoundsType> getCssMetaData() {
                    return StyleableProperties.BOUNDS_TYPE;
                }

                @Override
                public void invalidated() {
                    TextLayout textLayout = Text.this.getTextLayout();
                    int n = 0;
                    if (Text.this.boundsType.get() == TextBoundsType.LOGICAL_VERTICAL_CENTER) {
                        n |= 0x4000;
                    }
                    if (textLayout.setBoundsType(n)) {
                        Text.this.needsTextLayout();
                    } else {
                        NodeHelper.geomChanged(Text.this);
                    }
                }
            };
        }
        return this.boundsType;
    }

    public final void setWrappingWidth(double d) {
        this.wrappingWidthProperty().set(d);
    }

    public final double getWrappingWidth() {
        return this.wrappingWidth == null ? 0.0 : this.wrappingWidth.get();
    }

    public final DoubleProperty wrappingWidthProperty() {
        if (this.wrappingWidth == null) {
            this.wrappingWidth = new DoublePropertyBase(){

                @Override
                public Object getBean() {
                    return Text.this;
                }

                @Override
                public String getName() {
                    return "wrappingWidth";
                }

                @Override
                public void invalidated() {
                    if (!Text.this.isSpan()) {
                        TextLayout textLayout = Text.this.getTextLayout();
                        if (textLayout.setWrapWidth((float)this.get())) {
                            Text.this.needsTextLayout();
                        } else {
                            NodeHelper.geomChanged(Text.this);
                        }
                    }
                }
            };
        }
        return this.wrappingWidth;
    }

    public final void setUnderline(boolean bl) {
        this.underlineProperty().set(bl);
    }

    public final boolean isUnderline() {
        if (this.attributes == null || this.attributes.underline == null) {
            return false;
        }
        return this.attributes.isUnderline();
    }

    public final BooleanProperty underlineProperty() {
        return this.getTextAttribute().underlineProperty();
    }

    public final void setStrikethrough(boolean bl) {
        this.strikethroughProperty().set(bl);
    }

    public final boolean isStrikethrough() {
        if (this.attributes == null || this.attributes.strikethrough == null) {
            return false;
        }
        return this.attributes.isStrikethrough();
    }

    public final BooleanProperty strikethroughProperty() {
        return this.getTextAttribute().strikethroughProperty();
    }

    public final void setTextAlignment(TextAlignment textAlignment) {
        this.textAlignmentProperty().set(textAlignment);
    }

    public final TextAlignment getTextAlignment() {
        if (this.attributes == null || this.attributes.textAlignment == null) {
            return DEFAULT_TEXT_ALIGNMENT;
        }
        return this.attributes.getTextAlignment();
    }

    public final ObjectProperty<TextAlignment> textAlignmentProperty() {
        return this.getTextAttribute().textAlignmentProperty();
    }

    public final void setLineSpacing(double d) {
        this.lineSpacingProperty().set(d);
    }

    public final double getLineSpacing() {
        if (this.attributes == null || this.attributes.lineSpacing == null) {
            return 0.0;
        }
        return this.attributes.getLineSpacing();
    }

    public final DoubleProperty lineSpacingProperty() {
        return this.getTextAttribute().lineSpacingProperty();
    }

    @Override
    public final double getBaselineOffset() {
        return this.baselineOffsetProperty().get();
    }

    public final ReadOnlyDoubleProperty baselineOffsetProperty() {
        return this.getTextAttribute().baselineOffsetProperty();
    }

    public final void setFontSmoothingType(FontSmoothingType fontSmoothingType) {
        this.fontSmoothingTypeProperty().set(fontSmoothingType);
    }

    public final FontSmoothingType getFontSmoothingType() {
        return this.fontSmoothingType == null ? FontSmoothingType.GRAY : (FontSmoothingType)((Object)this.fontSmoothingType.get());
    }

    public final ObjectProperty<FontSmoothingType> fontSmoothingTypeProperty() {
        if (this.fontSmoothingType == null) {
            this.fontSmoothingType = new StyleableObjectProperty<FontSmoothingType>(FontSmoothingType.GRAY){

                @Override
                public Object getBean() {
                    return Text.this;
                }

                @Override
                public String getName() {
                    return "fontSmoothingType";
                }

                @Override
                public CssMetaData<Text, FontSmoothingType> getCssMetaData() {
                    return StyleableProperties.FONT_SMOOTHING_TYPE;
                }

                @Override
                public void invalidated() {
                    NodeHelper.markDirty(Text.this, DirtyBits.TEXT_ATTRS);
                    NodeHelper.geomChanged(Text.this);
                }
            };
        }
        return this.fontSmoothingType;
    }

    private void doGeomChanged() {
        if (this.attributes != null) {
            if (this.attributes.caretBinding != null) {
                this.attributes.caretBinding.invalidate();
            }
            if (this.attributes.selectionBinding != null) {
                this.attributes.selectionBinding.invalidate();
            }
        }
        NodeHelper.markDirty(this, DirtyBits.NODE_GEOMETRY);
    }

    public final PathElement[] getSelectionShape() {
        return (PathElement[])this.selectionShapeProperty().get();
    }

    public final ReadOnlyObjectProperty<PathElement[]> selectionShapeProperty() {
        return this.getTextAttribute().selectionShapeProperty();
    }

    public final void setSelectionStart(int n) {
        if (n == -1 && (this.attributes == null || this.attributes.selectionStart == null)) {
            return;
        }
        this.selectionStartProperty().set(n);
    }

    public final int getSelectionStart() {
        if (this.attributes == null || this.attributes.selectionStart == null) {
            return -1;
        }
        return this.attributes.getSelectionStart();
    }

    public final IntegerProperty selectionStartProperty() {
        return this.getTextAttribute().selectionStartProperty();
    }

    public final void setSelectionEnd(int n) {
        if (n == -1 && (this.attributes == null || this.attributes.selectionEnd == null)) {
            return;
        }
        this.selectionEndProperty().set(n);
    }

    public final int getSelectionEnd() {
        if (this.attributes == null || this.attributes.selectionEnd == null) {
            return -1;
        }
        return this.attributes.getSelectionEnd();
    }

    public final IntegerProperty selectionEndProperty() {
        return this.getTextAttribute().selectionEndProperty();
    }

    public final ObjectProperty<Paint> selectionFillProperty() {
        return this.getTextAttribute().selectionFillProperty();
    }

    public final void setSelectionFill(Paint paint) {
        this.selectionFillProperty().set(paint);
    }

    public final Paint getSelectionFill() {
        return (Paint)this.selectionFillProperty().get();
    }

    public final PathElement[] getCaretShape() {
        return (PathElement[])this.caretShapeProperty().get();
    }

    public final ReadOnlyObjectProperty<PathElement[]> caretShapeProperty() {
        return this.getTextAttribute().caretShapeProperty();
    }

    public final void setCaretPosition(int n) {
        if (n == -1 && (this.attributes == null || this.attributes.caretPosition == null)) {
            return;
        }
        this.caretPositionProperty().set(n);
    }

    public final int getCaretPosition() {
        if (this.attributes == null || this.attributes.caretPosition == null) {
            return -1;
        }
        return this.attributes.getCaretPosition();
    }

    public final IntegerProperty caretPositionProperty() {
        return this.getTextAttribute().caretPositionProperty();
    }

    public final void setCaretBias(boolean bl) {
        if (bl && (this.attributes == null || this.attributes.caretBias == null)) {
            return;
        }
        this.caretBiasProperty().set(bl);
    }

    public final boolean isCaretBias() {
        if (this.attributes == null || this.attributes.caretBias == null) {
            return true;
        }
        return this.getTextAttribute().isCaretBias();
    }

    public final BooleanProperty caretBiasProperty() {
        return this.getTextAttribute().caretBiasProperty();
    }

    public final HitInfo hitTest(Point2D point2D) {
        if (point2D == null) {
            return null;
        }
        TextLayout textLayout = this.getTextLayout();
        double d = point2D.getX() - this.getX();
        double d2 = point2D.getY() - this.getY() + (double)this.getYRendering();
        TextLayout.Hit hit = textLayout.getHitInfo((float)d, (float)d2);
        return new HitInfo(hit.getCharIndex(), hit.getInsertionIndex(), hit.isLeading(), this.getText());
    }

    private PathElement[] getRange(int n, int n2, int n3) {
        int n4 = this.getTextInternal().length();
        if (0 <= n && n < n2 && n2 <= n4) {
            TextLayout textLayout = this.getTextLayout();
            float f = (float)this.getX();
            float f2 = (float)this.getY() - this.getYRendering();
            return textLayout.getRange(n, n2, n3, f, f2);
        }
        return EMPTY_PATH_ELEMENT_ARRAY;
    }

    public final PathElement[] caretShape(int n, boolean bl) {
        if (0 <= n && n <= this.getTextInternal().length()) {
            float f = (float)this.getX();
            float f2 = (float)this.getY() - this.getYRendering();
            return this.getTextLayout().getCaretShape(n, bl, f, f2);
        }
        return null;
    }

    public final PathElement[] rangeShape(int n, int n2) {
        return this.getRange(n, n2, 1);
    }

    public final PathElement[] underlineShape(int n, int n2) {
        return this.getRange(n, n2, 2);
    }

    private float getYAdjustment(BaseBounds baseBounds) {
        VPos vPos = this.getTextOrigin();
        if (vPos == null) {
            vPos = DEFAULT_TEXT_ORIGIN;
        }
        switch (vPos) {
            case TOP: {
                return -baseBounds.getMinY();
            }
            case BASELINE: {
                return 0.0f;
            }
            case CENTER: {
                return -baseBounds.getMinY() - baseBounds.getHeight() / 2.0f;
            }
            case BOTTOM: {
                return -baseBounds.getMinY() - baseBounds.getHeight();
            }
        }
        return 0.0f;
    }

    private float getYRendering() {
        if (this.isSpan()) {
            return 0.0f;
        }
        BaseBounds baseBounds = this.getLogicalBounds();
        VPos vPos = this.getTextOrigin();
        if (vPos == null) {
            vPos = DEFAULT_TEXT_ORIGIN;
        }
        if (this.getBoundsType() == TextBoundsType.VISUAL) {
            BaseBounds baseBounds2 = this.getVisualBounds();
            float f = baseBounds2.getMinY() - baseBounds.getMinY();
            switch (vPos) {
                case TOP: {
                    return f;
                }
                case BASELINE: {
                    return -baseBounds2.getMinY() + f;
                }
                case CENTER: {
                    return baseBounds2.getHeight() / 2.0f + f;
                }
                case BOTTOM: {
                    return baseBounds2.getHeight() + f;
                }
            }
            return 0.0f;
        }
        switch (vPos) {
            case TOP: {
                return 0.0f;
            }
            case BASELINE: {
                return -baseBounds.getMinY();
            }
            case CENTER: {
                return baseBounds.getHeight() / 2.0f;
            }
            case BOTTOM: {
                return baseBounds.getHeight();
            }
        }
        return 0.0f;
    }

    private Bounds doComputeLayoutBounds() {
        if (this.isSpan()) {
            BaseBounds baseBounds = this.getSpanBounds();
            double d = baseBounds.getWidth();
            double d2 = baseBounds.getHeight();
            return new BoundingBox(0.0, 0.0, d, d2);
        }
        if (this.getBoundsType() == TextBoundsType.VISUAL) {
            return TextHelper.superComputeLayoutBounds(this);
        }
        BaseBounds baseBounds = this.getLogicalBounds();
        double d = (double)baseBounds.getMinX() + this.getX();
        double d3 = (double)baseBounds.getMinY() + this.getY() + (double)this.getYAdjustment(baseBounds);
        double d4 = baseBounds.getWidth();
        double d5 = baseBounds.getHeight();
        double d6 = this.getWrappingWidth();
        if (d6 != 0.0) {
            d4 = d6;
        }
        return new BoundingBox(d, d3, d4, d5);
    }

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        Object object;
        if (this.isSpan()) {
            if (ShapeHelper.getMode(this) != NGShape.Mode.FILL && this.getStrokeType() != StrokeType.INSIDE) {
                return TextHelper.superComputeGeomBounds(this, baseBounds, baseTransform);
            }
            TextLayout textLayout = this.getTextLayout();
            baseBounds = textLayout.getBounds(this.getTextSpan(), baseBounds);
            BaseBounds baseBounds2 = this.getSpanBounds();
            float f = baseBounds.getMinX() - baseBounds2.getMinX();
            float f2 = baseBounds.getMinY() - baseBounds2.getMinY();
            float f3 = f + baseBounds.getWidth();
            float f4 = f2 + baseBounds.getHeight();
            baseBounds = baseBounds.deriveWithNewBounds(f, f2, 0.0f, f3, f4, 0.0f);
            return baseTransform.transform(baseBounds, baseBounds);
        }
        if (this.getBoundsType() == TextBoundsType.VISUAL) {
            if (this.getTextInternal().length() == 0 || ShapeHelper.getMode(this) == NGShape.Mode.EMPTY) {
                return baseBounds.makeEmpty();
            }
            if (ShapeHelper.getMode(this) == NGShape.Mode.FILL || this.getStrokeType() == StrokeType.INSIDE) {
                BaseBounds baseBounds3 = this.getVisualBounds();
                float f = baseBounds3.getMinX() + (float)this.getX();
                float f5 = this.getYAdjustment(baseBounds3);
                float f6 = baseBounds3.getMinY() + f5 + (float)this.getY();
                baseBounds.deriveWithNewBounds(f, f6, 0.0f, f + baseBounds3.getWidth(), f6 + baseBounds3.getHeight(), 0.0f);
                return baseTransform.transform(baseBounds, baseBounds);
            }
            return TextHelper.superComputeGeomBounds(this, baseBounds, baseTransform);
        }
        BaseBounds baseBounds4 = this.getLogicalBounds();
        float f = baseBounds4.getMinX() + (float)this.getX();
        float f7 = this.getYAdjustment(baseBounds4);
        float f8 = baseBounds4.getMinY() + f7 + (float)this.getY();
        float f9 = baseBounds4.getWidth();
        float f10 = baseBounds4.getHeight();
        float f11 = (float)this.getWrappingWidth();
        if (f11 > f9) {
            f9 = f11;
        } else if (f11 > 0.0f && (object = this.getEffectiveNodeOrientation()) == NodeOrientation.RIGHT_TO_LEFT) {
            f -= f9 - f11;
        }
        baseBounds4 = new RectBounds(f, f8, f + f9, f8 + f10);
        if (ShapeHelper.getMode(this) != NGShape.Mode.FILL && this.getStrokeType() != StrokeType.INSIDE) {
            baseBounds = TextHelper.superComputeGeomBounds(this, baseBounds, BaseTransform.IDENTITY_TRANSFORM);
        } else {
            object = this.getTextLayout();
            baseBounds = object.getBounds(null, baseBounds);
            f = baseBounds.getMinX() + (float)this.getX();
            f9 = baseBounds.getWidth();
            baseBounds = baseBounds.deriveWithNewBounds(f, f8, 0.0f, f + f9, f8 + f10, 0.0f);
        }
        baseBounds = baseBounds.deriveWithUnion(baseBounds4);
        return baseTransform.transform(baseBounds, baseBounds);
    }

    private boolean doComputeContains(double d, double d2) {
        double d3 = d + (double)this.getSpanBounds().getMinX();
        double d4 = d2 + (double)this.getSpanBounds().getMinY();
        GlyphList[] glyphListArray = this.getRuns();
        if (glyphListArray.length != 0) {
            for (int i = 0; i < glyphListArray.length; ++i) {
                GlyphList glyphList = glyphListArray[i];
                com.sun.javafx.geom.Point2D point2D = glyphList.getLocation();
                float f = glyphList.getWidth();
                RectBounds rectBounds = glyphList.getLineBounds();
                float f2 = rectBounds.getHeight();
                if (!((double)point2D.x <= d3) || !(d3 < (double)(point2D.x + f)) || !((double)point2D.y <= d4) || !(d4 < (double)(point2D.y + f2))) continue;
                return true;
            }
        }
        return false;
    }

    private Shape doConfigShape() {
        float f;
        float f2;
        if (ShapeHelper.getMode(this) == NGShape.Mode.EMPTY || this.getTextInternal().length() == 0) {
            return new Path2D();
        }
        Shape shape = this.getShape();
        if (this.isSpan()) {
            BaseBounds baseBounds = this.getSpanBounds();
            f2 = -baseBounds.getMinX();
            f = -baseBounds.getMinY();
        } else {
            f2 = (float)this.getX();
            f = this.getYAdjustment(this.getVisualBounds()) + (float)this.getY();
        }
        return TransformedShape.translatedShape(shape, f2, f);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return Text.getClassCssMetaData();
    }

    private void updatePGText() {
        Object object;
        NGText nGText = (NGText)NodeHelper.getPeer(this);
        if (NodeHelper.isDirty(this, DirtyBits.TEXT_ATTRS)) {
            nGText.setUnderline(this.isUnderline());
            nGText.setStrikethrough(this.isStrikethrough());
            object = this.getFontSmoothingType();
            if (object == null) {
                object = FontSmoothingType.GRAY;
            }
            nGText.setFontSmoothingType(((Enum)object).ordinal());
        }
        if (NodeHelper.isDirty(this, DirtyBits.TEXT_FONT)) {
            nGText.setFont(this.getFontInternal());
        }
        if (NodeHelper.isDirty(this, DirtyBits.NODE_CONTENTS)) {
            nGText.setGlyphs(this.getRuns());
        }
        if (NodeHelper.isDirty(this, DirtyBits.NODE_GEOMETRY)) {
            if (this.isSpan()) {
                object = this.getSpanBounds();
                nGText.setLayoutLocation(((BaseBounds)object).getMinX(), ((BaseBounds)object).getMinY());
            } else {
                float f = (float)this.getX();
                float f2 = (float)this.getY();
                float f3 = this.getYRendering();
                nGText.setLayoutLocation(-f, f3 - f2);
            }
        }
        if (NodeHelper.isDirty(this, DirtyBits.TEXT_SELECTION)) {
            Object object2 = null;
            int n = this.getSelectionStart();
            int n2 = this.getSelectionEnd();
            int n3 = this.getTextInternal().length();
            if (0 <= n && n < n2 && n2 <= n3) {
                Paint paint = (Paint)this.selectionFillProperty().get();
                object2 = paint != null ? Toolkit.getPaintAccessor().getPlatformPaint(paint) : null;
            }
            nGText.setSelection(n, n2, object2);
        }
    }

    private void doUpdatePeer() {
        this.updatePGText();
    }

    private TextAttribute getTextAttribute() {
        if (this.attributes == null) {
            this.attributes = new TextAttribute();
        }
        return this.attributes;
    }

    @Override
    public String toString() {
        double d;
        StringBuilder stringBuilder = new StringBuilder("Text[");
        String string = this.getId();
        if (string != null) {
            stringBuilder.append("id=").append(string).append(", ");
        }
        stringBuilder.append("text=\"").append(this.getText()).append("\"");
        stringBuilder.append(", x=").append(this.getX());
        stringBuilder.append(", y=").append(this.getY());
        stringBuilder.append(", alignment=").append((Object)this.getTextAlignment());
        stringBuilder.append(", origin=").append((Object)this.getTextOrigin());
        stringBuilder.append(", boundsType=").append((Object)this.getBoundsType());
        double d2 = this.getLineSpacing();
        if (d2 != 0.0) {
            stringBuilder.append(", lineSpacing=").append(d2);
        }
        if ((d = this.getWrappingWidth()) != 0.0) {
            stringBuilder.append(", wrappingWidth=").append(d);
        }
        stringBuilder.append(", font=").append(this.getFont());
        stringBuilder.append(", fontSmoothingType=").append((Object)this.getFontSmoothingType());
        if (this.isStrikethrough()) {
            stringBuilder.append(", strikethrough");
        }
        if (this.isUnderline()) {
            stringBuilder.append(", underline");
        }
        stringBuilder.append(", fill=").append(this.getFill());
        Paint paint = this.getStroke();
        if (paint != null) {
            stringBuilder.append(", stroke=").append(paint);
            stringBuilder.append(", strokeWidth=").append(this.getStrokeWidth());
        }
        return stringBuilder.append("]").toString();
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case TEXT: {
                String string = this.getAccessibleText();
                if (string != null && !string.isEmpty()) {
                    return string;
                }
                return this.getText();
            }
            case FONT: {
                return this.getFont();
            }
            case CARET_OFFSET: {
                int n = this.getCaretPosition();
                if (n >= 0) {
                    return n;
                }
                return this.getText().length();
            }
            case SELECTION_START: {
                int n = this.getSelectionStart();
                if (n >= 0) {
                    return n;
                }
                n = this.getCaretPosition();
                if (n >= 0) {
                    return n;
                }
                return this.getText().length();
            }
            case SELECTION_END: {
                int n = this.getSelectionEnd();
                if (n >= 0) {
                    return n;
                }
                n = this.getCaretPosition();
                if (n >= 0) {
                    return n;
                }
                return this.getText().length();
            }
            case LINE_FOR_OFFSET: {
                TextLine textLine;
                int n = (Integer)objectArray[0];
                if (n > this.getTextInternal().length()) {
                    return null;
                }
                TextLine[] textLineArray = this.getTextLayout().getLines();
                int n2 = 0;
                for (int i = 1; i < textLineArray.length && (textLine = textLineArray[i]).getStart() <= n; ++i) {
                    ++n2;
                }
                return n2;
            }
            case LINE_START: {
                int n = (Integer)objectArray[0];
                TextLine[] textLineArray = this.getTextLayout().getLines();
                if (0 <= n && n < textLineArray.length) {
                    TextLine textLine = textLineArray[n];
                    return textLine.getStart();
                }
                return null;
            }
            case LINE_END: {
                int n = (Integer)objectArray[0];
                TextLine[] textLineArray = this.getTextLayout().getLines();
                if (0 <= n && n < textLineArray.length) {
                    TextLine textLine = textLineArray[n];
                    return textLine.getStart() + textLine.getLength();
                }
                return null;
            }
            case OFFSET_AT_POINT: {
                Point2D point2D = (Point2D)objectArray[0];
                point2D = this.screenToLocal(point2D);
                return this.hitTest(point2D).getCharIndex();
            }
            case BOUNDS_FOR_RANGE: {
                int n = (Integer)objectArray[0];
                int n3 = (Integer)objectArray[1];
                PathElement[] pathElementArray = this.rangeShape(n, n3 + 1);
                Bounds[] boundsArray = new Bounds[pathElementArray.length / 5];
                int n4 = 0;
                for (int i = 0; i < boundsArray.length; ++i) {
                    MoveTo moveTo = (MoveTo)pathElementArray[n4];
                    LineTo lineTo = (LineTo)pathElementArray[n4 + 1];
                    LineTo lineTo2 = (LineTo)pathElementArray[n4 + 2];
                    BoundingBox boundingBox = new BoundingBox(moveTo.getX(), moveTo.getY(), lineTo.getX() - moveTo.getX(), lineTo2.getY() - lineTo.getY());
                    boundsArray[i] = this.localToScreen(boundingBox);
                    n4 += 5;
                }
                return boundsArray;
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    static /* synthetic */ TextBoundsType access$3100() {
        return DEFAULT_BOUNDS_TYPE;
    }

    static {
        TextHelper.setTextAccessor(new TextHelper.TextAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((Text)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((Text)node).doUpdatePeer();
            }

            @Override
            public Bounds doComputeLayoutBounds(Node node) {
                return ((Text)node).doComputeLayoutBounds();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((Text)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public boolean doComputeContains(Node node, double d, double d2) {
                return ((Text)node).doComputeContains(d, d2);
            }

            @Override
            public void doGeomChanged(Node node) {
                ((Text)node).doGeomChanged();
            }

            @Override
            public Shape doConfigShape(javafx.scene.shape.Shape shape) {
                return ((Text)shape).doConfigShape();
            }
        });
        EMPTY_PATH_ELEMENT_ARRAY = new PathElement[0];
        DEFAULT_TEXT_ORIGIN = VPos.BASELINE;
        DEFAULT_BOUNDS_TYPE = TextBoundsType.LOGICAL;
        DEFAULT_TEXT_ALIGNMENT = TextAlignment.LEFT;
        DEFAULT_SELECTION_FILL = Color.WHITE;
    }

    private final class TextAttribute {
        private ObjectProperty<VPos> textOrigin;
        private BooleanProperty underline;
        private BooleanProperty strikethrough;
        private ObjectProperty<TextAlignment> textAlignment;
        private DoubleProperty lineSpacing;
        private ReadOnlyDoubleWrapper baselineOffset;
        private ObjectProperty<PathElement[]> selectionShape;
        private ObjectBinding<PathElement[]> selectionBinding;
        private ObjectProperty<Paint> selectionFill;
        private IntegerProperty selectionStart;
        private IntegerProperty selectionEnd;
        private ObjectProperty<PathElement[]> caretShape;
        private ObjectBinding<PathElement[]> caretBinding;
        private IntegerProperty caretPosition;
        private BooleanProperty caretBias;

        private TextAttribute() {
        }

        final VPos getTextOrigin() {
            return this.textOrigin == null ? DEFAULT_TEXT_ORIGIN : (VPos)((Object)this.textOrigin.get());
        }

        public final ObjectProperty<VPos> textOriginProperty() {
            if (this.textOrigin == null) {
                this.textOrigin = new StyleableObjectProperty<VPos>(DEFAULT_TEXT_ORIGIN){

                    @Override
                    public Object getBean() {
                        return Text.this;
                    }

                    @Override
                    public String getName() {
                        return "textOrigin";
                    }

                    @Override
                    public CssMetaData getCssMetaData() {
                        return StyleableProperties.TEXT_ORIGIN;
                    }

                    @Override
                    public void invalidated() {
                        NodeHelper.geomChanged(Text.this);
                    }
                };
            }
            return this.textOrigin;
        }

        final boolean isUnderline() {
            return this.underline == null ? false : this.underline.get();
        }

        final BooleanProperty underlineProperty() {
            if (this.underline == null) {
                this.underline = new StyleableBooleanProperty(){

                    @Override
                    public Object getBean() {
                        return Text.this;
                    }

                    @Override
                    public String getName() {
                        return "underline";
                    }

                    @Override
                    public CssMetaData getCssMetaData() {
                        return StyleableProperties.UNDERLINE;
                    }

                    @Override
                    public void invalidated() {
                        NodeHelper.markDirty(Text.this, DirtyBits.TEXT_ATTRS);
                        if (Text.this.getBoundsType() == TextBoundsType.VISUAL) {
                            NodeHelper.geomChanged(Text.this);
                        }
                    }
                };
            }
            return this.underline;
        }

        final boolean isStrikethrough() {
            return this.strikethrough == null ? false : this.strikethrough.get();
        }

        final BooleanProperty strikethroughProperty() {
            if (this.strikethrough == null) {
                this.strikethrough = new StyleableBooleanProperty(){

                    @Override
                    public Object getBean() {
                        return Text.this;
                    }

                    @Override
                    public String getName() {
                        return "strikethrough";
                    }

                    @Override
                    public CssMetaData getCssMetaData() {
                        return StyleableProperties.STRIKETHROUGH;
                    }

                    @Override
                    public void invalidated() {
                        NodeHelper.markDirty(Text.this, DirtyBits.TEXT_ATTRS);
                        if (Text.this.getBoundsType() == TextBoundsType.VISUAL) {
                            NodeHelper.geomChanged(Text.this);
                        }
                    }
                };
            }
            return this.strikethrough;
        }

        final TextAlignment getTextAlignment() {
            return this.textAlignment == null ? DEFAULT_TEXT_ALIGNMENT : (TextAlignment)((Object)this.textAlignment.get());
        }

        final ObjectProperty<TextAlignment> textAlignmentProperty() {
            if (this.textAlignment == null) {
                this.textAlignment = new StyleableObjectProperty<TextAlignment>(DEFAULT_TEXT_ALIGNMENT){

                    @Override
                    public Object getBean() {
                        return Text.this;
                    }

                    @Override
                    public String getName() {
                        return "textAlignment";
                    }

                    @Override
                    public CssMetaData getCssMetaData() {
                        return StyleableProperties.TEXT_ALIGNMENT;
                    }

                    @Override
                    public void invalidated() {
                        if (!Text.this.isSpan()) {
                            TextLayout textLayout;
                            TextAlignment textAlignment = (TextAlignment)((Object)this.get());
                            if (textAlignment == null) {
                                textAlignment = DEFAULT_TEXT_ALIGNMENT;
                            }
                            if ((textLayout = Text.this.getTextLayout()).setAlignment(textAlignment.ordinal())) {
                                Text.this.needsTextLayout();
                            }
                        }
                    }
                };
            }
            return this.textAlignment;
        }

        final double getLineSpacing() {
            return this.lineSpacing == null ? 0.0 : this.lineSpacing.get();
        }

        final DoubleProperty lineSpacingProperty() {
            if (this.lineSpacing == null) {
                this.lineSpacing = new StyleableDoubleProperty(0.0){

                    @Override
                    public Object getBean() {
                        return Text.this;
                    }

                    @Override
                    public String getName() {
                        return "lineSpacing";
                    }

                    @Override
                    public CssMetaData getCssMetaData() {
                        return StyleableProperties.LINE_SPACING;
                    }

                    @Override
                    public void invalidated() {
                        TextLayout textLayout;
                        if (!Text.this.isSpan() && (textLayout = Text.this.getTextLayout()).setLineSpacing((float)this.get())) {
                            Text.this.needsTextLayout();
                        }
                    }
                };
            }
            return this.lineSpacing;
        }

        final ReadOnlyDoubleProperty baselineOffsetProperty() {
            if (this.baselineOffset == null) {
                this.baselineOffset = new ReadOnlyDoubleWrapper(Text.this, "baselineOffset"){
                    {
                        this.bind(new DoubleBinding(){
                            {
                                this.bind(Text.this.fontProperty());
                            }

                            @Override
                            protected double computeValue() {
                                BaseBounds baseBounds = Text.this.getLogicalBounds();
                                return -baseBounds.getMinY();
                            }
                        });
                    }
                };
            }
            return this.baselineOffset.getReadOnlyProperty();
        }

        final ReadOnlyObjectProperty<PathElement[]> selectionShapeProperty() {
            if (this.selectionShape == null) {
                this.selectionBinding = new ObjectBinding<PathElement[]>(){
                    {
                        this.bind(TextAttribute.this.selectionStartProperty(), TextAttribute.this.selectionEndProperty());
                    }

                    @Override
                    protected PathElement[] computeValue() {
                        int n = TextAttribute.this.getSelectionStart();
                        int n2 = TextAttribute.this.getSelectionEnd();
                        return Text.this.getRange(n, n2, 1);
                    }
                };
                this.selectionShape = new SimpleObjectProperty<PathElement[]>(Text.this, "selectionShape");
                this.selectionShape.bind(this.selectionBinding);
            }
            return this.selectionShape;
        }

        final ObjectProperty<Paint> selectionFillProperty() {
            if (this.selectionFill == null) {
                this.selectionFill = new ObjectPropertyBase<Paint>((Paint)DEFAULT_SELECTION_FILL){

                    @Override
                    public Object getBean() {
                        return Text.this;
                    }

                    @Override
                    public String getName() {
                        return "selectionFill";
                    }

                    @Override
                    protected void invalidated() {
                        NodeHelper.markDirty(Text.this, DirtyBits.TEXT_SELECTION);
                    }
                };
            }
            return this.selectionFill;
        }

        final int getSelectionStart() {
            return this.selectionStart == null ? -1 : this.selectionStart.get();
        }

        final IntegerProperty selectionStartProperty() {
            if (this.selectionStart == null) {
                this.selectionStart = new IntegerPropertyBase(-1){

                    @Override
                    public Object getBean() {
                        return Text.this;
                    }

                    @Override
                    public String getName() {
                        return "selectionStart";
                    }

                    @Override
                    protected void invalidated() {
                        NodeHelper.markDirty(Text.this, DirtyBits.TEXT_SELECTION);
                        Text.this.notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTION_START);
                    }
                };
            }
            return this.selectionStart;
        }

        final int getSelectionEnd() {
            return this.selectionEnd == null ? -1 : this.selectionEnd.get();
        }

        final IntegerProperty selectionEndProperty() {
            if (this.selectionEnd == null) {
                this.selectionEnd = new IntegerPropertyBase(-1){

                    @Override
                    public Object getBean() {
                        return Text.this;
                    }

                    @Override
                    public String getName() {
                        return "selectionEnd";
                    }

                    @Override
                    protected void invalidated() {
                        NodeHelper.markDirty(Text.this, DirtyBits.TEXT_SELECTION);
                        Text.this.notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTION_END);
                    }
                };
            }
            return this.selectionEnd;
        }

        final ReadOnlyObjectProperty<PathElement[]> caretShapeProperty() {
            if (this.caretShape == null) {
                this.caretBinding = new ObjectBinding<PathElement[]>(){
                    {
                        this.bind(TextAttribute.this.caretPositionProperty(), TextAttribute.this.caretBiasProperty());
                    }

                    @Override
                    protected PathElement[] computeValue() {
                        int n = TextAttribute.this.getCaretPosition();
                        int n2 = Text.this.getTextInternal().length();
                        if (0 <= n && n <= n2) {
                            boolean bl = TextAttribute.this.isCaretBias();
                            float f = (float)Text.this.getX();
                            float f2 = (float)Text.this.getY() - Text.this.getYRendering();
                            TextLayout textLayout = Text.this.getTextLayout();
                            return textLayout.getCaretShape(n, bl, f, f2);
                        }
                        return EMPTY_PATH_ELEMENT_ARRAY;
                    }
                };
                this.caretShape = new SimpleObjectProperty<PathElement[]>(Text.this, "caretShape");
                this.caretShape.bind(this.caretBinding);
            }
            return this.caretShape;
        }

        final int getCaretPosition() {
            return this.caretPosition == null ? -1 : this.caretPosition.get();
        }

        final IntegerProperty caretPositionProperty() {
            if (this.caretPosition == null) {
                this.caretPosition = new IntegerPropertyBase(-1){

                    @Override
                    public Object getBean() {
                        return Text.this;
                    }

                    @Override
                    public String getName() {
                        return "caretPosition";
                    }

                    @Override
                    protected void invalidated() {
                        Text.this.notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTION_END);
                    }
                };
            }
            return this.caretPosition;
        }

        final boolean isCaretBias() {
            return this.caretBias == null ? true : this.caretBias.get();
        }

        final BooleanProperty caretBiasProperty() {
            if (this.caretBias == null) {
                this.caretBias = new SimpleBooleanProperty(Text.this, "caretBias", true);
            }
            return this.caretBias;
        }
    }

    private static class StyleableProperties {
        private static final CssMetaData<Text, Font> FONT = new FontCssMetaData<Text>("-fx-font", Font.getDefault()){

            @Override
            public boolean isSettable(Text text) {
                return text.font == null || !text.font.isBound();
            }

            @Override
            public StyleableProperty<Font> getStyleableProperty(Text text) {
                return (StyleableProperty)((Object)text.fontProperty());
            }
        };
        private static final CssMetaData<Text, Boolean> UNDERLINE = new CssMetaData<Text, Boolean>("-fx-underline", BooleanConverter.getInstance(), Boolean.FALSE){

            @Override
            public boolean isSettable(Text text) {
                return text.attributes == null || text.attributes.underline == null || !text.attributes.underline.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(Text text) {
                return (StyleableProperty)((Object)text.underlineProperty());
            }
        };
        private static final CssMetaData<Text, Boolean> STRIKETHROUGH = new CssMetaData<Text, Boolean>("-fx-strikethrough", BooleanConverter.getInstance(), Boolean.FALSE){

            @Override
            public boolean isSettable(Text text) {
                return text.attributes == null || text.attributes.strikethrough == null || !text.attributes.strikethrough.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(Text text) {
                return (StyleableProperty)((Object)text.strikethroughProperty());
            }
        };
        private static final CssMetaData<Text, TextAlignment> TEXT_ALIGNMENT = new CssMetaData<Text, TextAlignment>("-fx-text-alignment", new EnumConverter<TextAlignment>(TextAlignment.class), TextAlignment.LEFT){

            @Override
            public boolean isSettable(Text text) {
                return text.attributes == null || text.attributes.textAlignment == null || !text.attributes.textAlignment.isBound();
            }

            @Override
            public StyleableProperty<TextAlignment> getStyleableProperty(Text text) {
                return (StyleableProperty)((Object)text.textAlignmentProperty());
            }
        };
        private static final CssMetaData<Text, VPos> TEXT_ORIGIN = new CssMetaData<Text, VPos>("-fx-text-origin", new EnumConverter<VPos>(VPos.class), VPos.BASELINE){

            @Override
            public boolean isSettable(Text text) {
                return text.attributes == null || text.attributes.textOrigin == null || !text.attributes.textOrigin.isBound();
            }

            @Override
            public StyleableProperty<VPos> getStyleableProperty(Text text) {
                return (StyleableProperty)((Object)text.textOriginProperty());
            }
        };
        private static final CssMetaData<Text, FontSmoothingType> FONT_SMOOTHING_TYPE = new CssMetaData<Text, FontSmoothingType>("-fx-font-smoothing-type", new EnumConverter<FontSmoothingType>(FontSmoothingType.class), FontSmoothingType.GRAY){

            @Override
            public boolean isSettable(Text text) {
                return text.fontSmoothingType == null || !text.fontSmoothingType.isBound();
            }

            @Override
            public StyleableProperty<FontSmoothingType> getStyleableProperty(Text text) {
                return (StyleableProperty)((Object)text.fontSmoothingTypeProperty());
            }
        };
        private static final CssMetaData<Text, Number> LINE_SPACING = new CssMetaData<Text, Number>("-fx-line-spacing", SizeConverter.getInstance(), (Number)0){

            @Override
            public boolean isSettable(Text text) {
                return text.attributes == null || text.attributes.lineSpacing == null || !text.attributes.lineSpacing.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Text text) {
                return (StyleableProperty)((Object)text.lineSpacingProperty());
            }
        };
        private static final CssMetaData<Text, TextBoundsType> BOUNDS_TYPE = new CssMetaData<Text, TextBoundsType>("-fx-bounds-type", new EnumConverter<TextBoundsType>(TextBoundsType.class), Text.access$3100()){

            @Override
            public boolean isSettable(Text text) {
                return text.boundsType == null || !text.boundsType.isBound();
            }

            @Override
            public StyleableProperty<TextBoundsType> getStyleableProperty(Text text) {
                return (StyleableProperty)((Object)text.boundsTypeProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(javafx.scene.shape.Shape.getClassCssMetaData());
            arrayList.add(FONT);
            arrayList.add(UNDERLINE);
            arrayList.add(STRIKETHROUGH);
            arrayList.add(TEXT_ALIGNMENT);
            arrayList.add(TEXT_ORIGIN);
            arrayList.add(FONT_SMOOTHING_TYPE);
            arrayList.add(LINE_SPACING);
            arrayList.add(BOUNDS_TYPE);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

