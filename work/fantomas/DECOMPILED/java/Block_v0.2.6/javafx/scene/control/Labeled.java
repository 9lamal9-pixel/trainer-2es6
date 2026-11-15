/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.css.StyleManager;
import com.sun.javafx.scene.NodeHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.DefaultProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.CssMetaData;
import javafx.css.FontCssMetaData;
import javafx.css.StyleOrigin;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.StyleableStringProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.EnumConverter;
import javafx.css.converter.InsetsConverter;
import javafx.css.converter.PaintConverter;
import javafx.css.converter.SizeConverter;
import javafx.css.converter.StringConverter;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

@DefaultProperty(value="text")
public abstract class Labeled
extends Control {
    private static final String DEFAULT_ELLIPSIS_STRING = "...";
    private StringProperty text;
    private ObjectProperty<Pos> alignment;
    private ObjectProperty<TextAlignment> textAlignment;
    private ObjectProperty<OverrunStyle> textOverrun;
    private StringProperty ellipsisString;
    private BooleanProperty wrapText;
    private ObjectProperty<Font> font;
    private ObjectProperty<Node> graphic;
    private StyleableStringProperty imageUrl = null;
    private BooleanProperty underline;
    private DoubleProperty lineSpacing;
    private ObjectProperty<ContentDisplay> contentDisplay;
    private ObjectProperty<Insets> labelPadding;
    private DoubleProperty graphicTextGap;
    private ObjectProperty<Paint> textFill;
    private BooleanProperty mnemonicParsing;

    public Labeled() {
    }

    public Labeled(String string) {
        this.setText(string);
    }

    public Labeled(String string, Node node) {
        this.setText(string);
        ((StyleableProperty)((Object)this.graphicProperty())).applyStyle(null, node);
    }

    public final StringProperty textProperty() {
        if (this.text == null) {
            this.text = new SimpleStringProperty(this, "text", "");
        }
        return this.text;
    }

    public final void setText(String string) {
        this.textProperty().setValue(string);
    }

    public final String getText() {
        return this.text == null ? "" : this.text.getValue();
    }

    public final ObjectProperty<Pos> alignmentProperty() {
        if (this.alignment == null) {
            this.alignment = new StyleableObjectProperty<Pos>(Pos.CENTER_LEFT){

                @Override
                public CssMetaData<Labeled, Pos> getCssMetaData() {
                    return StyleableProperties.ALIGNMENT;
                }

                @Override
                public Object getBean() {
                    return Labeled.this;
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
        return this.alignment == null ? Pos.CENTER_LEFT : (Pos)((Object)this.alignment.get());
    }

    public final ObjectProperty<TextAlignment> textAlignmentProperty() {
        if (this.textAlignment == null) {
            this.textAlignment = new StyleableObjectProperty<TextAlignment>(TextAlignment.LEFT){

                @Override
                public CssMetaData<Labeled, TextAlignment> getCssMetaData() {
                    return StyleableProperties.TEXT_ALIGNMENT;
                }

                @Override
                public Object getBean() {
                    return Labeled.this;
                }

                @Override
                public String getName() {
                    return "textAlignment";
                }
            };
        }
        return this.textAlignment;
    }

    public final void setTextAlignment(TextAlignment textAlignment) {
        this.textAlignmentProperty().setValue(textAlignment);
    }

    public final TextAlignment getTextAlignment() {
        return this.textAlignment == null ? TextAlignment.LEFT : (TextAlignment)((Object)this.textAlignment.getValue());
    }

    public final ObjectProperty<OverrunStyle> textOverrunProperty() {
        if (this.textOverrun == null) {
            this.textOverrun = new StyleableObjectProperty<OverrunStyle>(OverrunStyle.ELLIPSIS){

                @Override
                public CssMetaData<Labeled, OverrunStyle> getCssMetaData() {
                    return StyleableProperties.TEXT_OVERRUN;
                }

                @Override
                public Object getBean() {
                    return Labeled.this;
                }

                @Override
                public String getName() {
                    return "textOverrun";
                }
            };
        }
        return this.textOverrun;
    }

    public final void setTextOverrun(OverrunStyle overrunStyle) {
        this.textOverrunProperty().setValue(overrunStyle);
    }

    public final OverrunStyle getTextOverrun() {
        return this.textOverrun == null ? OverrunStyle.ELLIPSIS : (OverrunStyle)((Object)this.textOverrun.getValue());
    }

    public final StringProperty ellipsisStringProperty() {
        if (this.ellipsisString == null) {
            this.ellipsisString = new StyleableStringProperty(DEFAULT_ELLIPSIS_STRING){

                @Override
                public Object getBean() {
                    return Labeled.this;
                }

                @Override
                public String getName() {
                    return "ellipsisString";
                }

                @Override
                public CssMetaData<Labeled, String> getCssMetaData() {
                    return StyleableProperties.ELLIPSIS_STRING;
                }
            };
        }
        return this.ellipsisString;
    }

    public final void setEllipsisString(String string) {
        this.ellipsisStringProperty().set(string == null ? "" : string);
    }

    public final String getEllipsisString() {
        return this.ellipsisString == null ? DEFAULT_ELLIPSIS_STRING : (String)this.ellipsisString.get();
    }

    public final BooleanProperty wrapTextProperty() {
        if (this.wrapText == null) {
            this.wrapText = new StyleableBooleanProperty(){

                @Override
                public CssMetaData<Labeled, Boolean> getCssMetaData() {
                    return StyleableProperties.WRAP_TEXT;
                }

                @Override
                public Object getBean() {
                    return Labeled.this;
                }

                @Override
                public String getName() {
                    return "wrapText";
                }
            };
        }
        return this.wrapText;
    }

    public final void setWrapText(boolean bl) {
        this.wrapTextProperty().setValue(bl);
    }

    public final boolean isWrapText() {
        return this.wrapText == null ? false : this.wrapText.getValue();
    }

    @Override
    public Orientation getContentBias() {
        return this.isWrapText() ? Orientation.HORIZONTAL : null;
    }

    public final ObjectProperty<Font> fontProperty() {
        if (this.font == null) {
            this.font = new StyleableObjectProperty<Font>(Font.getDefault()){
                private boolean fontSetByCss;
                {
                    this.fontSetByCss = false;
                }

                @Override
                public void applyStyle(StyleOrigin styleOrigin, Font font) {
                    try {
                        this.fontSetByCss = true;
                        super.applyStyle(styleOrigin, font);
                    }
                    catch (Exception exception) {
                        throw exception;
                    }
                    finally {
                        this.fontSetByCss = false;
                    }
                }

                @Override
                public void set(Font font) {
                    Font font2 = (Font)this.get();
                    if (font != null ? !font.equals(font2) : font2 != null) {
                        super.set(font);
                    }
                }

                @Override
                protected void invalidated() {
                    if (!this.fontSetByCss) {
                        NodeHelper.reapplyCSS(Labeled.this);
                    }
                }

                @Override
                public CssMetaData<Labeled, Font> getCssMetaData() {
                    return StyleableProperties.FONT;
                }

                @Override
                public Object getBean() {
                    return Labeled.this;
                }

                @Override
                public String getName() {
                    return "font";
                }
            };
        }
        return this.font;
    }

    public final void setFont(Font font) {
        this.fontProperty().setValue(font);
    }

    public final Font getFont() {
        return this.font == null ? Font.getDefault() : (Font)this.font.getValue();
    }

    public final ObjectProperty<Node> graphicProperty() {
        if (this.graphic == null) {
            this.graphic = new StyleableObjectProperty<Node>(){

                @Override
                public CssMetaData getCssMetaData() {
                    return StyleableProperties.GRAPHIC;
                }

                @Override
                public Object getBean() {
                    return Labeled.this;
                }

                @Override
                public String getName() {
                    return "graphic";
                }
            };
        }
        return this.graphic;
    }

    public final void setGraphic(Node node) {
        this.graphicProperty().setValue(node);
    }

    public final Node getGraphic() {
        return this.graphic == null ? null : (Node)this.graphic.getValue();
    }

    private StyleableStringProperty imageUrlProperty() {
        if (this.imageUrl == null) {
            this.imageUrl = new StyleableStringProperty(){
                StyleOrigin origin = StyleOrigin.USER;

                @Override
                public void applyStyle(StyleOrigin styleOrigin, String string) {
                    this.origin = styleOrigin;
                    if (Labeled.this.graphic == null || !Labeled.this.graphic.isBound()) {
                        super.applyStyle(styleOrigin, string);
                    }
                    this.origin = StyleOrigin.USER;
                }

                @Override
                protected void invalidated() {
                    String string = super.get();
                    if (string == null) {
                        ((StyleableProperty)((Object)Labeled.this.graphicProperty())).applyStyle(this.origin, null);
                    } else {
                        String string2;
                        Object object;
                        Image image;
                        Node node = Labeled.this.getGraphic();
                        if (node instanceof ImageView && (image = ((ImageView)(object = (ImageView)node)).getImage()) != null && string.equals(string2 = image.getUrl())) {
                            return;
                        }
                        object = StyleManager.getInstance().getCachedImage(string);
                        if (object != null) {
                            ((StyleableProperty)((Object)Labeled.this.graphicProperty())).applyStyle(this.origin, new ImageView((Image)object));
                        }
                    }
                }

                @Override
                public String get() {
                    Image image;
                    Node node = Labeled.this.getGraphic();
                    if (node instanceof ImageView && (image = ((ImageView)node).getImage()) != null) {
                        return image.getUrl();
                    }
                    return null;
                }

                @Override
                public StyleOrigin getStyleOrigin() {
                    return Labeled.this.graphic != null ? ((StyleableProperty)((Object)Labeled.this.graphic)).getStyleOrigin() : null;
                }

                @Override
                public Object getBean() {
                    return Labeled.this;
                }

                @Override
                public String getName() {
                    return "imageUrl";
                }

                @Override
                public CssMetaData<Labeled, String> getCssMetaData() {
                    return StyleableProperties.GRAPHIC;
                }
            };
        }
        return this.imageUrl;
    }

    public final BooleanProperty underlineProperty() {
        if (this.underline == null) {
            this.underline = new StyleableBooleanProperty(false){

                @Override
                public CssMetaData<Labeled, Boolean> getCssMetaData() {
                    return StyleableProperties.UNDERLINE;
                }

                @Override
                public Object getBean() {
                    return Labeled.this;
                }

                @Override
                public String getName() {
                    return "underline";
                }
            };
        }
        return this.underline;
    }

    public final void setUnderline(boolean bl) {
        this.underlineProperty().setValue(bl);
    }

    public final boolean isUnderline() {
        return this.underline == null ? false : this.underline.getValue();
    }

    public final DoubleProperty lineSpacingProperty() {
        if (this.lineSpacing == null) {
            this.lineSpacing = new StyleableDoubleProperty(0.0){

                @Override
                public CssMetaData<Labeled, Number> getCssMetaData() {
                    return StyleableProperties.LINE_SPACING;
                }

                @Override
                public Object getBean() {
                    return Labeled.this;
                }

                @Override
                public String getName() {
                    return "lineSpacing";
                }
            };
        }
        return this.lineSpacing;
    }

    public final void setLineSpacing(double d) {
        this.lineSpacingProperty().setValue(d);
    }

    public final double getLineSpacing() {
        return this.lineSpacing == null ? 0.0 : this.lineSpacing.getValue();
    }

    public final ObjectProperty<ContentDisplay> contentDisplayProperty() {
        if (this.contentDisplay == null) {
            this.contentDisplay = new StyleableObjectProperty<ContentDisplay>(ContentDisplay.LEFT){

                @Override
                public CssMetaData<Labeled, ContentDisplay> getCssMetaData() {
                    return StyleableProperties.CONTENT_DISPLAY;
                }

                @Override
                public Object getBean() {
                    return Labeled.this;
                }

                @Override
                public String getName() {
                    return "contentDisplay";
                }
            };
        }
        return this.contentDisplay;
    }

    public final void setContentDisplay(ContentDisplay contentDisplay) {
        this.contentDisplayProperty().setValue(contentDisplay);
    }

    public final ContentDisplay getContentDisplay() {
        return this.contentDisplay == null ? ContentDisplay.LEFT : (ContentDisplay)((Object)this.contentDisplay.getValue());
    }

    public final ReadOnlyObjectProperty<Insets> labelPaddingProperty() {
        return this.labelPaddingPropertyImpl();
    }

    private ObjectProperty<Insets> labelPaddingPropertyImpl() {
        if (this.labelPadding == null) {
            this.labelPadding = new StyleableObjectProperty<Insets>(Insets.EMPTY){
                private Insets lastValidValue;
                {
                    this.lastValidValue = Insets.EMPTY;
                }

                @Override
                public void invalidated() {
                    Insets insets = (Insets)this.get();
                    if (insets == null) {
                        this.set(this.lastValidValue);
                        throw new NullPointerException("cannot set labelPadding to null");
                    }
                    this.lastValidValue = insets;
                    Labeled.this.requestLayout();
                }

                @Override
                public CssMetaData<Labeled, Insets> getCssMetaData() {
                    return StyleableProperties.LABEL_PADDING;
                }

                @Override
                public Object getBean() {
                    return Labeled.this;
                }

                @Override
                public String getName() {
                    return "labelPadding";
                }
            };
        }
        return this.labelPadding;
    }

    private void setLabelPadding(Insets insets) {
        this.labelPaddingPropertyImpl().set(insets);
    }

    public final Insets getLabelPadding() {
        return this.labelPadding == null ? Insets.EMPTY : (Insets)this.labelPadding.get();
    }

    public final DoubleProperty graphicTextGapProperty() {
        if (this.graphicTextGap == null) {
            this.graphicTextGap = new StyleableDoubleProperty(4.0){

                @Override
                public CssMetaData<Labeled, Number> getCssMetaData() {
                    return StyleableProperties.GRAPHIC_TEXT_GAP;
                }

                @Override
                public Object getBean() {
                    return Labeled.this;
                }

                @Override
                public String getName() {
                    return "graphicTextGap";
                }
            };
        }
        return this.graphicTextGap;
    }

    public final void setGraphicTextGap(double d) {
        this.graphicTextGapProperty().setValue(d);
    }

    public final double getGraphicTextGap() {
        return this.graphicTextGap == null ? 4.0 : this.graphicTextGap.getValue();
    }

    public final void setTextFill(Paint paint) {
        this.textFillProperty().set(paint);
    }

    public final Paint getTextFill() {
        return this.textFill == null ? Color.BLACK : (Paint)this.textFill.get();
    }

    public final ObjectProperty<Paint> textFillProperty() {
        if (this.textFill == null) {
            this.textFill = new StyleableObjectProperty<Paint>((Paint)Color.BLACK){

                @Override
                public CssMetaData<Labeled, Paint> getCssMetaData() {
                    return StyleableProperties.TEXT_FILL;
                }

                @Override
                public Object getBean() {
                    return Labeled.this;
                }

                @Override
                public String getName() {
                    return "textFill";
                }
            };
        }
        return this.textFill;
    }

    public final void setMnemonicParsing(boolean bl) {
        this.mnemonicParsingProperty().set(bl);
    }

    public final boolean isMnemonicParsing() {
        return this.mnemonicParsing == null ? false : this.mnemonicParsing.get();
    }

    public final BooleanProperty mnemonicParsingProperty() {
        if (this.mnemonicParsing == null) {
            this.mnemonicParsing = new SimpleBooleanProperty(this, "mnemonicParsing");
        }
        return this.mnemonicParsing;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(super.toString()).append("'").append(this.getText()).append("'");
        return stringBuilder.toString();
    }

    protected Pos getInitialAlignment() {
        return Pos.CENTER_LEFT;
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return Labeled.getClassCssMetaData();
    }

    private static class StyleableProperties {
        private static final FontCssMetaData<Labeled> FONT = new FontCssMetaData<Labeled>("-fx-font", Font.getDefault()){

            @Override
            public boolean isSettable(Labeled labeled) {
                return labeled.font == null || !labeled.font.isBound();
            }

            @Override
            public StyleableProperty<Font> getStyleableProperty(Labeled labeled) {
                return (StyleableProperty)((Object)labeled.fontProperty());
            }
        };
        private static final CssMetaData<Labeled, Pos> ALIGNMENT = new CssMetaData<Labeled, Pos>("-fx-alignment", new EnumConverter<Pos>(Pos.class), Pos.CENTER_LEFT){

            @Override
            public boolean isSettable(Labeled labeled) {
                return labeled.alignment == null || !labeled.alignment.isBound();
            }

            @Override
            public StyleableProperty<Pos> getStyleableProperty(Labeled labeled) {
                return (StyleableProperty)((Object)labeled.alignmentProperty());
            }

            @Override
            public Pos getInitialValue(Labeled labeled) {
                return labeled.getInitialAlignment();
            }
        };
        private static final CssMetaData<Labeled, TextAlignment> TEXT_ALIGNMENT = new CssMetaData<Labeled, TextAlignment>("-fx-text-alignment", new EnumConverter<TextAlignment>(TextAlignment.class), TextAlignment.LEFT){

            @Override
            public boolean isSettable(Labeled labeled) {
                return labeled.textAlignment == null || !labeled.textAlignment.isBound();
            }

            @Override
            public StyleableProperty<TextAlignment> getStyleableProperty(Labeled labeled) {
                return (StyleableProperty)((Object)labeled.textAlignmentProperty());
            }
        };
        private static final CssMetaData<Labeled, Paint> TEXT_FILL = new CssMetaData<Labeled, Paint>("-fx-text-fill", PaintConverter.getInstance(), (Paint)Color.BLACK){

            @Override
            public boolean isSettable(Labeled labeled) {
                return labeled.textFill == null || !labeled.textFill.isBound();
            }

            @Override
            public StyleableProperty<Paint> getStyleableProperty(Labeled labeled) {
                return (StyleableProperty)((Object)labeled.textFillProperty());
            }
        };
        private static final CssMetaData<Labeled, OverrunStyle> TEXT_OVERRUN = new CssMetaData<Labeled, OverrunStyle>("-fx-text-overrun", new EnumConverter<OverrunStyle>(OverrunStyle.class), OverrunStyle.ELLIPSIS){

            @Override
            public boolean isSettable(Labeled labeled) {
                return labeled.textOverrun == null || !labeled.textOverrun.isBound();
            }

            @Override
            public StyleableProperty<OverrunStyle> getStyleableProperty(Labeled labeled) {
                return (StyleableProperty)((Object)labeled.textOverrunProperty());
            }
        };
        private static final CssMetaData<Labeled, String> ELLIPSIS_STRING = new CssMetaData<Labeled, String>("-fx-ellipsis-string", StringConverter.getInstance(), "..."){

            @Override
            public boolean isSettable(Labeled labeled) {
                return labeled.ellipsisString == null || !labeled.ellipsisString.isBound();
            }

            @Override
            public StyleableProperty<String> getStyleableProperty(Labeled labeled) {
                return (StyleableProperty)((Object)labeled.ellipsisStringProperty());
            }
        };
        private static final CssMetaData<Labeled, Boolean> WRAP_TEXT = new CssMetaData<Labeled, Boolean>("-fx-wrap-text", BooleanConverter.getInstance(), Boolean.valueOf(false)){

            @Override
            public boolean isSettable(Labeled labeled) {
                return labeled.wrapText == null || !labeled.wrapText.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(Labeled labeled) {
                return (StyleableProperty)((Object)labeled.wrapTextProperty());
            }
        };
        private static final CssMetaData<Labeled, String> GRAPHIC = new CssMetaData<Labeled, String>("-fx-graphic", StringConverter.getInstance()){

            @Override
            public boolean isSettable(Labeled labeled) {
                return labeled.graphic == null || !labeled.graphic.isBound();
            }

            @Override
            public StyleableProperty<String> getStyleableProperty(Labeled labeled) {
                return labeled.imageUrlProperty();
            }
        };
        private static final CssMetaData<Labeled, Boolean> UNDERLINE = new CssMetaData<Labeled, Boolean>("-fx-underline", BooleanConverter.getInstance(), Boolean.FALSE){

            @Override
            public boolean isSettable(Labeled labeled) {
                return labeled.underline == null || !labeled.underline.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(Labeled labeled) {
                return (StyleableProperty)((Object)labeled.underlineProperty());
            }
        };
        private static final CssMetaData<Labeled, Number> LINE_SPACING = new CssMetaData<Labeled, Number>("-fx-line-spacing", SizeConverter.getInstance(), (Number)0){

            @Override
            public boolean isSettable(Labeled labeled) {
                return labeled.lineSpacing == null || !labeled.lineSpacing.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Labeled labeled) {
                return (StyleableProperty)((Object)labeled.lineSpacingProperty());
            }
        };
        private static final CssMetaData<Labeled, ContentDisplay> CONTENT_DISPLAY = new CssMetaData<Labeled, ContentDisplay>("-fx-content-display", new EnumConverter<ContentDisplay>(ContentDisplay.class), ContentDisplay.LEFT){

            @Override
            public boolean isSettable(Labeled labeled) {
                return labeled.contentDisplay == null || !labeled.contentDisplay.isBound();
            }

            @Override
            public StyleableProperty<ContentDisplay> getStyleableProperty(Labeled labeled) {
                return (StyleableProperty)((Object)labeled.contentDisplayProperty());
            }
        };
        private static final CssMetaData<Labeled, Insets> LABEL_PADDING = new CssMetaData<Labeled, Insets>("-fx-label-padding", InsetsConverter.getInstance(), Insets.EMPTY){

            @Override
            public boolean isSettable(Labeled labeled) {
                return labeled.labelPadding == null || !labeled.labelPadding.isBound();
            }

            @Override
            public StyleableProperty<Insets> getStyleableProperty(Labeled labeled) {
                return (StyleableProperty)((Object)labeled.labelPaddingPropertyImpl());
            }
        };
        private static final CssMetaData<Labeled, Number> GRAPHIC_TEXT_GAP = new CssMetaData<Labeled, Number>("-fx-graphic-text-gap", SizeConverter.getInstance(), (Number)4.0){

            @Override
            public boolean isSettable(Labeled labeled) {
                return labeled.graphicTextGap == null || !labeled.graphicTextGap.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Labeled labeled) {
                return (StyleableProperty)((Object)labeled.graphicTextGapProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Control.getClassCssMetaData());
            Collections.addAll(arrayList, FONT, ALIGNMENT, TEXT_ALIGNMENT, TEXT_FILL, TEXT_OVERRUN, ELLIPSIS_STRING, WRAP_TEXT, GRAPHIC, UNDERLINE, LINE_SPACING, CONTENT_DISPLAY, LABEL_PADDING, GRAPHIC_TEXT_GAP);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

