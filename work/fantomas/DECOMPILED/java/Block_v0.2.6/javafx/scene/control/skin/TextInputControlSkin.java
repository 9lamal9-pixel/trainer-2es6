/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.scene.control.Properties;
import com.sun.javafx.scene.control.behavior.TextInputControlBehavior;
import com.sun.javafx.scene.control.skin.FXVK;
import com.sun.javafx.scene.input.ExtendedInputMethodRequests;
import com.sun.javafx.tk.FontMetrics;
import com.sun.javafx.tk.Toolkit;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.PaintConverter;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.AccessibleAction;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.IndexRange;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.InputMethodHighlight;
import javafx.scene.input.InputMethodTextRun;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Shape;
import javafx.scene.shape.VLineTo;
import javafx.stage.Window;
import javafx.util.Duration;

public abstract class TextInputControlSkin<T extends TextInputControl>
extends SkinBase<T> {
    static boolean preload = false;
    static final boolean SHOW_HANDLES;
    private static final boolean IS_FXVK_SUPPORTED;
    final ObservableObjectValue<FontMetrics> fontMetrics;
    private ObservableBooleanValue caretVisible;
    private CaretBlinking caretBlinking = new CaretBlinking(this.blinkProperty());
    final Path caretPath = new Path();
    StackPane caretHandle = null;
    StackPane selectionHandle1 = null;
    StackPane selectionHandle2 = null;
    private int imstart;
    private int imlength;
    private List<Shape> imattrs = new ArrayList<Shape>();
    private BooleanProperty blink;
    private final ObjectProperty<Paint> textFill = new StyleableObjectProperty<Paint>((Paint)Color.BLACK){

        @Override
        protected void invalidated() {
            TextInputControlSkin.this.updateTextFill();
        }

        @Override
        public Object getBean() {
            return TextInputControlSkin.this;
        }

        @Override
        public String getName() {
            return "textFill";
        }

        @Override
        public CssMetaData<TextInputControl, Paint> getCssMetaData() {
            return StyleableProperties.TEXT_FILL;
        }
    };
    private final ObjectProperty<Paint> promptTextFill = new StyleableObjectProperty<Paint>((Paint)Color.GRAY){

        @Override
        public Object getBean() {
            return TextInputControlSkin.this;
        }

        @Override
        public String getName() {
            return "promptTextFill";
        }

        @Override
        public CssMetaData<TextInputControl, Paint> getCssMetaData() {
            return StyleableProperties.PROMPT_TEXT_FILL;
        }
    };
    private final ObjectProperty<Paint> highlightFill = new StyleableObjectProperty<Paint>((Paint)Color.DODGERBLUE){

        @Override
        protected void invalidated() {
            TextInputControlSkin.this.updateHighlightFill();
        }

        @Override
        public Object getBean() {
            return TextInputControlSkin.this;
        }

        @Override
        public String getName() {
            return "highlightFill";
        }

        @Override
        public CssMetaData<TextInputControl, Paint> getCssMetaData() {
            return StyleableProperties.HIGHLIGHT_FILL;
        }
    };
    private final ObjectProperty<Paint> highlightTextFill = new StyleableObjectProperty<Paint>((Paint)Color.WHITE){

        @Override
        protected void invalidated() {
            TextInputControlSkin.this.updateHighlightTextFill();
        }

        @Override
        public Object getBean() {
            return TextInputControlSkin.this;
        }

        @Override
        public String getName() {
            return "highlightTextFill";
        }

        @Override
        public CssMetaData<TextInputControl, Paint> getCssMetaData() {
            return StyleableProperties.HIGHLIGHT_TEXT_FILL;
        }
    };
    private final BooleanProperty displayCaret = new StyleableBooleanProperty(true){

        @Override
        public Object getBean() {
            return TextInputControlSkin.this;
        }

        @Override
        public String getName() {
            return "displayCaret";
        }

        @Override
        public CssMetaData<TextInputControl, Boolean> getCssMetaData() {
            return StyleableProperties.DISPLAY_CARET;
        }
    };
    private BooleanProperty forwardBias = new SimpleBooleanProperty(this, "forwardBias", true);

    public TextInputControlSkin(final T t) {
        super(t);
        this.fontMetrics = new ObjectBinding<FontMetrics>(){
            {
                this.bind(t.fontProperty());
            }

            @Override
            protected FontMetrics computeValue() {
                TextInputControlSkin.this.invalidateMetrics();
                return Toolkit.getToolkit().getFontLoader().getFontMetrics(t.getFont());
            }
        };
        this.caretVisible = new BooleanBinding((TextInputControl)t){
            final /* synthetic */ TextInputControl val$control;
            {
                this.val$control = textInputControl;
                this.bind(this.val$control.focusedProperty(), this.val$control.anchorProperty(), this.val$control.caretPositionProperty(), this.val$control.disabledProperty(), this.val$control.editableProperty(), TextInputControlSkin.this.displayCaret, TextInputControlSkin.this.blinkProperty());
            }

            @Override
            protected boolean computeValue() {
                return !TextInputControlSkin.this.blinkProperty().get() && TextInputControlSkin.this.displayCaret.get() && this.val$control.isFocused() && (PlatformUtil.isWindows() || this.val$control.getCaretPosition() == this.val$control.getAnchor()) && !this.val$control.isDisabled() && this.val$control.isEditable();
            }
        };
        if (SHOW_HANDLES) {
            this.caretHandle = new StackPane();
            this.selectionHandle1 = new StackPane();
            this.selectionHandle2 = new StackPane();
            this.caretHandle.setManaged(false);
            this.selectionHandle1.setManaged(false);
            this.selectionHandle2.setManaged(false);
            this.caretHandle.visibleProperty().bind(new BooleanBinding((TextInputControl)t){
                final /* synthetic */ TextInputControl val$control;
                {
                    this.val$control = textInputControl;
                    this.bind(this.val$control.focusedProperty(), this.val$control.anchorProperty(), this.val$control.caretPositionProperty(), this.val$control.disabledProperty(), this.val$control.editableProperty(), this.val$control.lengthProperty(), TextInputControlSkin.this.displayCaret);
                }

                @Override
                protected boolean computeValue() {
                    return TextInputControlSkin.this.displayCaret.get() && this.val$control.isFocused() && this.val$control.getCaretPosition() == this.val$control.getAnchor() && !this.val$control.isDisabled() && this.val$control.isEditable() && this.val$control.getLength() > 0;
                }
            });
            this.selectionHandle1.visibleProperty().bind(new BooleanBinding((TextInputControl)t){
                final /* synthetic */ TextInputControl val$control;
                {
                    this.val$control = textInputControl;
                    this.bind(this.val$control.focusedProperty(), this.val$control.anchorProperty(), this.val$control.caretPositionProperty(), this.val$control.disabledProperty(), TextInputControlSkin.this.displayCaret);
                }

                @Override
                protected boolean computeValue() {
                    return TextInputControlSkin.this.displayCaret.get() && this.val$control.isFocused() && this.val$control.getCaretPosition() != this.val$control.getAnchor() && !this.val$control.isDisabled();
                }
            });
            this.selectionHandle2.visibleProperty().bind(new BooleanBinding((TextInputControl)t){
                final /* synthetic */ TextInputControl val$control;
                {
                    this.val$control = textInputControl;
                    this.bind(this.val$control.focusedProperty(), this.val$control.anchorProperty(), this.val$control.caretPositionProperty(), this.val$control.disabledProperty(), TextInputControlSkin.this.displayCaret);
                }

                @Override
                protected boolean computeValue() {
                    return TextInputControlSkin.this.displayCaret.get() && this.val$control.isFocused() && this.val$control.getCaretPosition() != this.val$control.getAnchor() && !this.val$control.isDisabled();
                }
            });
            this.caretHandle.getStyleClass().setAll((String[])new String[]{"caret-handle"});
            this.selectionHandle1.getStyleClass().setAll((String[])new String[]{"selection-handle"});
            this.selectionHandle2.getStyleClass().setAll((String[])new String[]{"selection-handle"});
            this.selectionHandle1.setId("selection-handle-1");
            this.selectionHandle2.setId("selection-handle-2");
        }
        if (IS_FXVK_SUPPORTED) {
            Window window;
            Scene scene;
            if (preload && (scene = ((Node)t).getScene()) != null && (window = scene.getWindow()) != null) {
                FXVK.init(t);
            }
            ((Node)t).focusedProperty().addListener(observable -> {
                if (FXVK.useFXVK()) {
                    Scene scene = ((TextInputControl)this.getSkinnable()).getScene();
                    if (t.isEditable() && t.isFocused()) {
                        FXVK.attach(t);
                    } else if (!(scene != null && scene.getWindow() != null && scene.getWindow().isFocused() && scene.getFocusOwner() instanceof TextInputControl && ((TextInputControl)scene.getFocusOwner()).isEditable())) {
                        FXVK.detach();
                    }
                }
            });
        }
        if (((Node)t).getOnInputMethodTextChanged() == null) {
            ((Node)t).setOnInputMethodTextChanged(inputMethodEvent -> this.handleInputMethodEvent((InputMethodEvent)inputMethodEvent));
        }
        ((Node)t).setInputMethodRequests(new ExtendedInputMethodRequests((TextInputControl)t){
            final /* synthetic */ TextInputControl val$control;
            {
                this.val$control = textInputControl;
            }

            @Override
            public Point2D getTextLocation(int n) {
                Scene scene = ((TextInputControl)TextInputControlSkin.this.getSkinnable()).getScene();
                Window window = scene.getWindow();
                Rectangle2D rectangle2D = TextInputControlSkin.this.getCharacterBounds(this.val$control.getSelection().getStart() + n);
                Point2D point2D = ((TextInputControl)TextInputControlSkin.this.getSkinnable()).localToScene(rectangle2D.getMinX(), rectangle2D.getMaxY());
                Point2D point2D2 = new Point2D(window.getX() + scene.getX() + point2D.getX(), window.getY() + scene.getY() + point2D.getY());
                return point2D2;
            }

            @Override
            public int getLocationOffset(int n, int n2) {
                return TextInputControlSkin.this.getInsertionPoint(n, n2);
            }

            @Override
            public void cancelLatestCommittedText() {
            }

            @Override
            public String getSelectedText() {
                TextInputControl textInputControl = (TextInputControl)TextInputControlSkin.this.getSkinnable();
                IndexRange indexRange = textInputControl.getSelection();
                return textInputControl.getText(indexRange.getStart(), indexRange.getEnd());
            }

            @Override
            public int getInsertPositionOffset() {
                int n = ((TextInputControl)TextInputControlSkin.this.getSkinnable()).getCaretPosition();
                if (n < TextInputControlSkin.this.imstart) {
                    return n;
                }
                if (n < TextInputControlSkin.this.imstart + TextInputControlSkin.this.imlength) {
                    return TextInputControlSkin.this.imstart;
                }
                return n - TextInputControlSkin.this.imlength;
            }

            @Override
            public String getCommittedText(int n, int n2) {
                TextInputControl textInputControl = (TextInputControl)TextInputControlSkin.this.getSkinnable();
                if (n < TextInputControlSkin.this.imstart) {
                    if (n2 <= TextInputControlSkin.this.imstart) {
                        return textInputControl.getText(n, n2);
                    }
                    return textInputControl.getText(n, TextInputControlSkin.this.imstart) + textInputControl.getText(TextInputControlSkin.this.imstart + TextInputControlSkin.this.imlength, n2 + TextInputControlSkin.this.imlength);
                }
                return textInputControl.getText(n + TextInputControlSkin.this.imlength, n2 + TextInputControlSkin.this.imlength);
            }

            @Override
            public int getCommittedTextLength() {
                return ((TextInputControl)TextInputControlSkin.this.getSkinnable()).getText().length() - TextInputControlSkin.this.imlength;
            }
        });
    }

    private final void setBlink(boolean bl) {
        this.blinkProperty().set(bl);
    }

    private final boolean isBlink() {
        return this.blinkProperty().get();
    }

    private final BooleanProperty blinkProperty() {
        if (this.blink == null) {
            this.blink = new SimpleBooleanProperty(this, "blink", true);
        }
        return this.blink;
    }

    protected final void setTextFill(Paint paint) {
        this.textFill.set(paint);
    }

    protected final Paint getTextFill() {
        return (Paint)this.textFill.get();
    }

    protected final ObjectProperty<Paint> textFillProperty() {
        return this.textFill;
    }

    protected final void setPromptTextFill(Paint paint) {
        this.promptTextFill.set(paint);
    }

    protected final Paint getPromptTextFill() {
        return (Paint)this.promptTextFill.get();
    }

    protected final ObjectProperty<Paint> promptTextFillProperty() {
        return this.promptTextFill;
    }

    protected final void setHighlightFill(Paint paint) {
        this.highlightFill.set(paint);
    }

    protected final Paint getHighlightFill() {
        return (Paint)this.highlightFill.get();
    }

    protected final ObjectProperty<Paint> highlightFillProperty() {
        return this.highlightFill;
    }

    protected final void setHighlightTextFill(Paint paint) {
        this.highlightTextFill.set(paint);
    }

    protected final Paint getHighlightTextFill() {
        return (Paint)this.highlightTextFill.get();
    }

    protected final ObjectProperty<Paint> highlightTextFillProperty() {
        return this.highlightTextFill;
    }

    private final void setDisplayCaret(boolean bl) {
        this.displayCaret.set(bl);
    }

    private final boolean isDisplayCaret() {
        return this.displayCaret.get();
    }

    private final BooleanProperty displayCaretProperty() {
        return this.displayCaret;
    }

    protected final BooleanProperty forwardBiasProperty() {
        return this.forwardBias;
    }

    public final void setForwardBias(boolean bl) {
        this.forwardBias.set(bl);
    }

    protected final boolean isForwardBias() {
        return this.forwardBias.get();
    }

    protected abstract PathElement[] getUnderlineShape(int var1, int var2);

    protected abstract PathElement[] getRangeShape(int var1, int var2);

    protected abstract void addHighlight(List<? extends Node> var1, int var2);

    protected abstract void removeHighlight(List<? extends Node> var1);

    public abstract void moveCaret(TextUnit var1, Direction var2, boolean var3);

    public Point2D getMenuPosition() {
        if (SHOW_HANDLES) {
            if (this.caretHandle.isVisible()) {
                return new Point2D(this.caretHandle.getLayoutX() + this.caretHandle.getWidth() / 2.0, this.caretHandle.getLayoutY());
            }
            if (this.selectionHandle1.isVisible() && this.selectionHandle2.isVisible()) {
                return new Point2D((this.selectionHandle1.getLayoutX() + this.selectionHandle1.getWidth() / 2.0 + this.selectionHandle2.getLayoutX() + this.selectionHandle2.getWidth() / 2.0) / 2.0, this.selectionHandle2.getLayoutY() + this.selectionHandle2.getHeight() / 2.0);
            }
            return null;
        }
        throw new UnsupportedOperationException();
    }

    protected String maskText(String string) {
        return string;
    }

    protected int getInsertionPoint(double d, double d2) {
        return 0;
    }

    public Rectangle2D getCharacterBounds(int n) {
        return null;
    }

    protected void scrollCharacterToVisible(int n) {
    }

    protected void invalidateMetrics() {
    }

    protected void updateTextFill() {
    }

    protected void updateHighlightFill() {
    }

    protected void updateHighlightTextFill() {
    }

    protected void handleInputMethodEvent(InputMethodEvent inputMethodEvent) {
        TextInputControl textInputControl = (TextInputControl)this.getSkinnable();
        if (textInputControl.isEditable() && !textInputControl.textProperty().isBound() && !textInputControl.isDisabled()) {
            CharSequence charSequence;
            if (PlatformUtil.isIOS()) {
                textInputControl.setText(inputMethodEvent.getCommitted());
                return;
            }
            if (this.imlength != 0) {
                this.removeHighlight(this.imattrs);
                this.imattrs.clear();
                textInputControl.selectRange(this.imstart, this.imstart + this.imlength);
            }
            if (inputMethodEvent.getCommitted().length() != 0) {
                charSequence = inputMethodEvent.getCommitted();
                textInputControl.replaceText(textInputControl.getSelection(), (String)charSequence);
            }
            this.imstart = textInputControl.getSelection().getStart();
            charSequence = new StringBuilder();
            for (InputMethodTextRun object : inputMethodEvent.getComposed()) {
                ((StringBuilder)charSequence).append(object.getText());
            }
            textInputControl.replaceText(textInputControl.getSelection(), ((StringBuilder)charSequence).toString());
            this.imlength = ((StringBuilder)charSequence).length();
            if (this.imlength != 0) {
                int n = this.imstart;
                for (InputMethodTextRun inputMethodTextRun : inputMethodEvent.getComposed()) {
                    int n2 = n + inputMethodTextRun.getText().length();
                    this.createInputMethodAttributes(inputMethodTextRun.getHighlight(), n, n2);
                    n = n2;
                }
                this.addHighlight(this.imattrs, this.imstart);
                int n3 = inputMethodEvent.getCaretPosition();
                if (n3 >= 0 && n3 < this.imlength) {
                    textInputControl.selectRange(this.imstart + n3, this.imstart + n3);
                }
            }
        }
    }

    public void setCaretAnimating(boolean bl) {
        if (bl) {
            this.caretBlinking.start();
        } else {
            this.caretBlinking.stop();
            this.blinkProperty().set(true);
        }
    }

    TextInputControlBehavior getBehavior() {
        return null;
    }

    ObservableBooleanValue caretVisibleProperty() {
        return this.caretVisible;
    }

    boolean isRTL() {
        return ((TextInputControl)this.getSkinnable()).getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
    }

    private void createInputMethodAttributes(InputMethodHighlight inputMethodHighlight, int n, int n2) {
        double d = 0.0;
        double d2 = 0.0;
        double d3 = 0.0;
        double d4 = 0.0;
        PathElement[] pathElementArray = this.getUnderlineShape(n, n2);
        for (int i = 0; i < pathElementArray.length; ++i) {
            PathElement pathElement = pathElementArray[i];
            if (pathElement instanceof MoveTo) {
                d = d2 = ((MoveTo)pathElement).getX();
                d3 = d4 = ((MoveTo)pathElement).getY();
            } else if (pathElement instanceof LineTo) {
                d = d < ((LineTo)pathElement).getX() ? d : ((LineTo)pathElement).getX();
                d2 = d2 > ((LineTo)pathElement).getX() ? d2 : ((LineTo)pathElement).getX();
                d3 = d3 < ((LineTo)pathElement).getY() ? d3 : ((LineTo)pathElement).getY();
                d4 = d4 > ((LineTo)pathElement).getY() ? d4 : ((LineTo)pathElement).getY();
            } else if (pathElement instanceof HLineTo) {
                d = d < ((HLineTo)pathElement).getX() ? d : ((HLineTo)pathElement).getX();
                d2 = d2 > ((HLineTo)pathElement).getX() ? d2 : ((HLineTo)pathElement).getX();
            } else if (pathElement instanceof VLineTo) {
                d3 = d3 < ((VLineTo)pathElement).getY() ? d3 : ((VLineTo)pathElement).getY();
                double d5 = d4 = d4 > ((VLineTo)pathElement).getY() ? d4 : ((VLineTo)pathElement).getY();
            }
            if (!(pathElement instanceof ClosePath) && i != pathElementArray.length - 1 && (i >= pathElementArray.length - 1 || !(pathElementArray[i + 1] instanceof MoveTo))) continue;
            Shape shape = null;
            if (inputMethodHighlight == InputMethodHighlight.SELECTED_RAW) {
                shape = new Path();
                ((Path)shape).getElements().addAll((PathElement[])this.getRangeShape(n, n2));
                shape.setFill(Color.BLUE);
                shape.setOpacity(0.3f);
            } else if (inputMethodHighlight == InputMethodHighlight.UNSELECTED_RAW) {
                shape = new Line(d + 2.0, d4 + 1.0, d2 - 2.0, d4 + 1.0);
                shape.setStroke((Paint)this.textFill.get());
                shape.setStrokeWidth(d4 - d3);
                ObservableList<Double> observableList = shape.getStrokeDashArray();
                observableList.add(2.0);
                observableList.add(2.0);
            } else if (inputMethodHighlight == InputMethodHighlight.SELECTED_CONVERTED) {
                shape = new Line(d + 2.0, d4 + 1.0, d2 - 2.0, d4 + 1.0);
                shape.setStroke((Paint)this.textFill.get());
                shape.setStrokeWidth((d4 - d3) * 3.0);
            } else if (inputMethodHighlight == InputMethodHighlight.UNSELECTED_CONVERTED) {
                shape = new Line(d + 2.0, d4 + 1.0, d2 - 2.0, d4 + 1.0);
                shape.setStroke((Paint)this.textFill.get());
                shape.setStrokeWidth(d4 - d3);
            }
            if (shape == null) continue;
            shape.setManaged(false);
            this.imattrs.add(shape);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return TextInputControlSkin.getClassCssMetaData();
    }

    @Override
    protected void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
        switch (accessibleAction) {
            case SHOW_TEXT_RANGE: {
                Integer n = (Integer)objectArray[0];
                Integer n2 = (Integer)objectArray[1];
                if (n == null || n2 == null) break;
                this.scrollCharacterToVisible(n2);
                this.scrollCharacterToVisible(n);
                this.scrollCharacterToVisible(n2);
                break;
            }
            default: {
                super.executeAccessibleAction(accessibleAction, objectArray);
            }
        }
    }

    static {
        AccessController.doPrivileged(() -> {
            String string = System.getProperty("com.sun.javafx.virtualKeyboard.preload");
            if (string != null && string.equalsIgnoreCase("PRERENDER")) {
                preload = true;
            }
            return null;
        });
        SHOW_HANDLES = Properties.IS_TOUCH_SUPPORTED && !PlatformUtil.isIOS();
        IS_FXVK_SUPPORTED = Platform.isSupported(ConditionalFeature.VIRTUAL_KEYBOARD);
    }

    private static class StyleableProperties {
        private static final CssMetaData<TextInputControl, Paint> TEXT_FILL = new CssMetaData<TextInputControl, Paint>("-fx-text-fill", PaintConverter.getInstance(), (Paint)Color.BLACK){

            @Override
            public boolean isSettable(TextInputControl textInputControl) {
                TextInputControlSkin textInputControlSkin = (TextInputControlSkin)textInputControl.getSkin();
                return textInputControlSkin.textFill == null || !textInputControlSkin.textFill.isBound();
            }

            @Override
            public StyleableProperty<Paint> getStyleableProperty(TextInputControl textInputControl) {
                TextInputControlSkin textInputControlSkin = (TextInputControlSkin)textInputControl.getSkin();
                return (StyleableProperty)((Object)textInputControlSkin.textFill);
            }
        };
        private static final CssMetaData<TextInputControl, Paint> PROMPT_TEXT_FILL = new CssMetaData<TextInputControl, Paint>("-fx-prompt-text-fill", PaintConverter.getInstance(), (Paint)Color.GRAY){

            @Override
            public boolean isSettable(TextInputControl textInputControl) {
                TextInputControlSkin textInputControlSkin = (TextInputControlSkin)textInputControl.getSkin();
                return textInputControlSkin.promptTextFill == null || !textInputControlSkin.promptTextFill.isBound();
            }

            @Override
            public StyleableProperty<Paint> getStyleableProperty(TextInputControl textInputControl) {
                TextInputControlSkin textInputControlSkin = (TextInputControlSkin)textInputControl.getSkin();
                return (StyleableProperty)((Object)textInputControlSkin.promptTextFill);
            }
        };
        private static final CssMetaData<TextInputControl, Paint> HIGHLIGHT_FILL = new CssMetaData<TextInputControl, Paint>("-fx-highlight-fill", PaintConverter.getInstance(), (Paint)Color.DODGERBLUE){

            @Override
            public boolean isSettable(TextInputControl textInputControl) {
                TextInputControlSkin textInputControlSkin = (TextInputControlSkin)textInputControl.getSkin();
                return textInputControlSkin.highlightFill == null || !textInputControlSkin.highlightFill.isBound();
            }

            @Override
            public StyleableProperty<Paint> getStyleableProperty(TextInputControl textInputControl) {
                TextInputControlSkin textInputControlSkin = (TextInputControlSkin)textInputControl.getSkin();
                return (StyleableProperty)((Object)textInputControlSkin.highlightFill);
            }
        };
        private static final CssMetaData<TextInputControl, Paint> HIGHLIGHT_TEXT_FILL = new CssMetaData<TextInputControl, Paint>("-fx-highlight-text-fill", PaintConverter.getInstance(), (Paint)Color.WHITE){

            @Override
            public boolean isSettable(TextInputControl textInputControl) {
                TextInputControlSkin textInputControlSkin = (TextInputControlSkin)textInputControl.getSkin();
                return textInputControlSkin.highlightTextFill == null || !textInputControlSkin.highlightTextFill.isBound();
            }

            @Override
            public StyleableProperty<Paint> getStyleableProperty(TextInputControl textInputControl) {
                TextInputControlSkin textInputControlSkin = (TextInputControlSkin)textInputControl.getSkin();
                return (StyleableProperty)((Object)textInputControlSkin.highlightTextFill);
            }
        };
        private static final CssMetaData<TextInputControl, Boolean> DISPLAY_CARET = new CssMetaData<TextInputControl, Boolean>("-fx-display-caret", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(TextInputControl textInputControl) {
                TextInputControlSkin textInputControlSkin = (TextInputControlSkin)textInputControl.getSkin();
                return textInputControlSkin.displayCaret == null || !textInputControlSkin.displayCaret.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(TextInputControl textInputControl) {
                TextInputControlSkin textInputControlSkin = (TextInputControlSkin)textInputControl.getSkin();
                return (StyleableProperty)((Object)textInputControlSkin.displayCaret);
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(SkinBase.getClassCssMetaData());
            arrayList.add(TEXT_FILL);
            arrayList.add(PROMPT_TEXT_FILL);
            arrayList.add(HIGHLIGHT_FILL);
            arrayList.add(HIGHLIGHT_TEXT_FILL);
            arrayList.add(DISPLAY_CARET);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }

    private static final class CaretBlinking {
        private final Timeline caretTimeline;
        private final WeakReference<BooleanProperty> blinkPropertyRef;

        public CaretBlinking(BooleanProperty booleanProperty) {
            this.blinkPropertyRef = new WeakReference<BooleanProperty>(booleanProperty);
            this.caretTimeline = new Timeline();
            this.caretTimeline.setCycleCount(-1);
            this.caretTimeline.getKeyFrames().addAll((KeyFrame[])new KeyFrame[]{new KeyFrame(Duration.ZERO, actionEvent -> this.setBlink(false), new KeyValue[0]), new KeyFrame(Duration.seconds(0.5), actionEvent -> this.setBlink(true), new KeyValue[0]), new KeyFrame(Duration.seconds(1.0), new KeyValue[0])});
        }

        public void start() {
            this.caretTimeline.play();
        }

        public void stop() {
            this.caretTimeline.stop();
        }

        private void setBlink(boolean bl) {
            BooleanProperty booleanProperty = (BooleanProperty)this.blinkPropertyRef.get();
            if (booleanProperty == null) {
                this.caretTimeline.stop();
                return;
            }
            booleanProperty.set(bl);
        }
    }

    public static enum Direction {
        LEFT,
        RIGHT,
        UP,
        DOWN,
        BEGINNING,
        END;

    }

    public static enum TextUnit {
        CHARACTER,
        WORD,
        LINE,
        PARAGRAPH,
        PAGE;

    }
}

