/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.behavior.PasswordFieldBehavior;
import com.sun.javafx.scene.control.behavior.TextFieldBehavior;
import com.sun.javafx.scene.control.behavior.TextInputControlBehavior;
import com.sun.javafx.tk.FontMetrics;
import java.util.Collection;
import java.util.List;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextInputControlSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.HitInfo;
import javafx.scene.text.Text;

public class TextFieldSkin
extends TextInputControlSkin<TextField> {
    private final TextFieldBehavior behavior;
    private Pane textGroup = new Pane();
    private Group handleGroup;
    private Rectangle clip = new Rectangle();
    private Text textNode = new Text();
    private Text promptNode;
    private Path selectionHighlightPath = new Path();
    private Path characterBoundingPath = new Path();
    private ObservableBooleanValue usePromptText;
    private DoubleProperty textTranslateX = new SimpleDoubleProperty(this, "textTranslateX");
    private double caretWidth;
    private ObservableDoubleValue textRight;
    private double pressX;
    private double pressY;
    static final char BULLET = '\u25cf';

    public TextFieldSkin(final TextField textField) {
        super(textField);
        this.behavior = textField instanceof PasswordField ? new PasswordFieldBehavior((PasswordField)textField) : new TextFieldBehavior(textField);
        this.behavior.setTextFieldSkin(this);
        textField.caretPositionProperty().addListener((observableValue, number, number2) -> {
            if (textField.getWidth() > 0.0) {
                this.updateTextNodeCaretPos(textField.getCaretPosition());
                if (!this.isForwardBias()) {
                    this.setForwardBias(true);
                }
                this.updateCaretOff();
            }
        });
        this.forwardBiasProperty().addListener(observable -> {
            if (textField.getWidth() > 0.0) {
                this.updateTextNodeCaretPos(textField.getCaretPosition());
                this.updateCaretOff();
            }
        });
        this.textRight = new DoubleBinding(){
            {
                this.bind(TextFieldSkin.this.textGroup.widthProperty());
            }

            @Override
            protected double computeValue() {
                return TextFieldSkin.this.textGroup.getWidth();
            }
        };
        this.clip.setSmooth(false);
        this.clip.setX(0.0);
        this.clip.widthProperty().bind(this.textGroup.widthProperty());
        this.clip.heightProperty().bind(this.textGroup.heightProperty());
        this.textGroup.setClip(this.clip);
        this.textGroup.getChildren().addAll((Node[])new Node[]{this.selectionHighlightPath, this.textNode, new Group(this.caretPath)});
        this.getChildren().add(this.textGroup);
        if (SHOW_HANDLES) {
            this.handleGroup = new Group();
            this.handleGroup.setManaged(false);
            this.handleGroup.getChildren().addAll((Node[])new Node[]{this.caretHandle, this.selectionHandle1, this.selectionHandle2});
            this.getChildren().add(this.handleGroup);
        }
        this.textNode.setManaged(false);
        this.textNode.getStyleClass().add("text");
        this.textNode.fontProperty().bind(textField.fontProperty());
        this.textNode.layoutXProperty().bind(this.textTranslateX);
        this.textNode.textProperty().bind(new StringBinding(){
            {
                this.bind(textField.textProperty());
            }

            @Override
            protected String computeValue() {
                return TextFieldSkin.this.maskText(textField.textProperty().getValueSafe());
            }
        });
        this.textNode.fillProperty().bind(this.textFillProperty());
        this.textNode.selectionFillProperty().bind((ObservableValue<Paint>)new ObjectBinding<Paint>(){
            {
                this.bind(TextFieldSkin.this.highlightTextFillProperty(), TextFieldSkin.this.textFillProperty(), textField.focusedProperty());
            }

            @Override
            protected Paint computeValue() {
                return textField.isFocused() ? (Paint)TextFieldSkin.this.highlightTextFillProperty().get() : (Paint)TextFieldSkin.this.textFillProperty().get();
            }
        });
        this.updateTextNodeCaretPos(textField.getCaretPosition());
        textField.selectionProperty().addListener(observable -> this.updateSelection());
        this.selectionHighlightPath.setManaged(false);
        this.selectionHighlightPath.setStroke(null);
        this.selectionHighlightPath.layoutXProperty().bind(this.textTranslateX);
        this.selectionHighlightPath.visibleProperty().bind(textField.anchorProperty().isNotEqualTo(textField.caretPositionProperty()).and(textField.focusedProperty()));
        this.selectionHighlightPath.fillProperty().bind(this.highlightFillProperty());
        this.textNode.selectionShapeProperty().addListener(observable -> this.updateSelection());
        this.caretPath.setManaged(false);
        this.caretPath.setStrokeWidth(1.0);
        this.caretPath.fillProperty().bind(this.textFillProperty());
        this.caretPath.strokeProperty().bind(this.textFillProperty());
        this.caretPath.opacityProperty().bind(new DoubleBinding(){
            {
                this.bind(TextFieldSkin.this.caretVisibleProperty());
            }

            @Override
            protected double computeValue() {
                return TextFieldSkin.this.caretVisibleProperty().get() ? 1.0 : 0.0;
            }
        });
        this.caretPath.layoutXProperty().bind(this.textTranslateX);
        this.textNode.caretShapeProperty().addListener(observable -> {
            this.caretPath.getElements().setAll((PathElement[])((PathElement[])this.textNode.caretShapeProperty().get()));
            if (this.caretPath.getElements().size() == 0) {
                this.updateTextNodeCaretPos(textField.getCaretPosition());
            } else if (this.caretPath.getElements().size() != 4) {
                this.caretWidth = Math.round(this.caretPath.getLayoutBounds().getWidth());
            }
        });
        textField.fontProperty().addListener(observable -> {
            textField.requestLayout();
            ((TextField)this.getSkinnable()).requestLayout();
        });
        this.registerChangeListener(textField.prefColumnCountProperty(), observableValue -> ((TextField)this.getSkinnable()).requestLayout());
        if (textField.isFocused()) {
            this.setCaretAnimating(true);
        }
        textField.alignmentProperty().addListener(observable -> {
            if (textField.getWidth() > 0.0) {
                this.updateTextPos();
                this.updateCaretOff();
                textField.requestLayout();
            }
        });
        this.usePromptText = new BooleanBinding(){
            {
                this.bind(textField.textProperty(), textField.promptTextProperty(), TextFieldSkin.this.promptTextFillProperty());
            }

            @Override
            protected boolean computeValue() {
                String string = textField.getText();
                String string2 = textField.getPromptText();
                return (string == null || string.isEmpty()) && string2 != null && !string2.isEmpty() && !TextFieldSkin.this.getPromptTextFill().equals(Color.TRANSPARENT);
            }
        };
        this.promptTextFillProperty().addListener(observable -> this.updateTextPos());
        textField.textProperty().addListener(observable -> {
            if (!this.behavior.isEditing()) {
                this.updateTextPos();
            }
        });
        if (this.usePromptText.get()) {
            this.createPromptNode();
        }
        this.usePromptText.addListener(observable -> {
            this.createPromptNode();
            textField.requestLayout();
        });
        if (SHOW_HANDLES) {
            this.selectionHandle1.setRotate(180.0);
            EventHandler<MouseEvent> eventHandler = mouseEvent -> {
                this.pressX = mouseEvent.getX();
                this.pressY = mouseEvent.getY();
                mouseEvent.consume();
            };
            this.caretHandle.setOnMousePressed(eventHandler);
            this.selectionHandle1.setOnMousePressed(eventHandler);
            this.selectionHandle2.setOnMousePressed(eventHandler);
            this.caretHandle.setOnMouseDragged(mouseEvent -> {
                Point2D point2D = new Point2D(this.caretHandle.getLayoutX() + mouseEvent.getX() + this.pressX - this.textNode.getLayoutX(), this.caretHandle.getLayoutY() + mouseEvent.getY() - this.pressY - 6.0);
                HitInfo hitInfo = this.textNode.hitTest(point2D);
                this.positionCaret(hitInfo, false);
                mouseEvent.consume();
            });
            this.selectionHandle1.setOnMouseDragged((EventHandler<? super MouseEvent>)new EventHandler<MouseEvent>(){

                @Override
                public void handle(MouseEvent mouseEvent) {
                    int n;
                    TextField textField = (TextField)TextFieldSkin.this.getSkinnable();
                    Point2D point2D = TextFieldSkin.this.textNode.localToScene(0.0, 0.0);
                    Point2D point2D2 = new Point2D(mouseEvent.getSceneX() - point2D.getX() + 10.0 - TextFieldSkin.this.pressX + TextFieldSkin.this.selectionHandle1.getWidth() / 2.0, mouseEvent.getSceneY() - point2D.getY() - TextFieldSkin.this.pressY - 6.0);
                    HitInfo hitInfo = TextFieldSkin.this.textNode.hitTest(point2D2);
                    if (textField.getAnchor() < textField.getCaretPosition()) {
                        textField.selectRange(textField.getCaretPosition(), textField.getAnchor());
                    }
                    if ((n = hitInfo.getInsertionIndex()) >= 0) {
                        if (n >= textField.getAnchor() - 1) {
                            n = Math.max(0, textField.getAnchor() - 1);
                        }
                        TextFieldSkin.this.positionCaret(n, hitInfo.isLeading(), true);
                    }
                    mouseEvent.consume();
                }
            });
            this.selectionHandle2.setOnMouseDragged((EventHandler<? super MouseEvent>)new EventHandler<MouseEvent>(){

                @Override
                public void handle(MouseEvent mouseEvent) {
                    int n;
                    TextField textField = (TextField)TextFieldSkin.this.getSkinnable();
                    Point2D point2D = TextFieldSkin.this.textNode.localToScene(0.0, 0.0);
                    Point2D point2D2 = new Point2D(mouseEvent.getSceneX() - point2D.getX() + 10.0 - TextFieldSkin.this.pressX + TextFieldSkin.this.selectionHandle2.getWidth() / 2.0, mouseEvent.getSceneY() - point2D.getY() - TextFieldSkin.this.pressY - 6.0);
                    HitInfo hitInfo = TextFieldSkin.this.textNode.hitTest(point2D2);
                    if (textField.getAnchor() > textField.getCaretPosition()) {
                        textField.selectRange(textField.getCaretPosition(), textField.getAnchor());
                    }
                    if ((n = hitInfo.getInsertionIndex()) > 0) {
                        if (n <= textField.getAnchor()) {
                            n = Math.min(textField.getAnchor() + 1, textField.getLength());
                        }
                        TextFieldSkin.this.positionCaret(n, hitInfo.isLeading(), true);
                    }
                    mouseEvent.consume();
                }
            });
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        TextField textField = (TextField)this.getSkinnable();
        double d6 = ((FontMetrics)this.fontMetrics.get()).getCharWidth('W');
        int n = textField.getPrefColumnCount();
        return (double)n * d6 + d5 + d3;
    }

    @Override
    protected double computeMinHeight(double d, double d2, double d3, double d4, double d5) {
        return this.computePrefHeight(d, d2, d3, d4, d5);
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        return d2 + this.textNode.getLayoutBounds().getHeight() + d4;
    }

    @Override
    protected double computeMaxHeight(double d, double d2, double d3, double d4, double d5) {
        return ((TextField)this.getSkinnable()).prefHeight(d);
    }

    @Override
    public double computeBaselineOffset(double d, double d2, double d3, double d4) {
        return d + this.textNode.getBaselineOffset();
    }

    public void replaceText(int n, int n2, String string) {
        double d = this.textNode.getBoundsInParent().getMaxX();
        double d2 = this.caretPath.getLayoutBounds().getMaxX() + this.textTranslateX.get();
        ((TextField)this.getSkinnable()).replaceText(n, n2, string);
        this.scrollAfterDelete(d, d2);
    }

    public void deleteChar(boolean bl) {
        double d = this.textNode.getBoundsInParent().getMaxX();
        double d2 = this.caretPath.getLayoutBounds().getMaxX() + this.textTranslateX.get();
        if (bl ? ((TextField)this.getSkinnable()).deletePreviousChar() : ((TextField)this.getSkinnable()).deleteNextChar()) {
            this.scrollAfterDelete(d, d2);
        }
    }

    public HitInfo getIndex(double d, double d2) {
        Point2D point2D = new Point2D(d - this.textTranslateX.get() - this.snappedLeftInset(), d2 - this.snappedTopInset());
        return this.textNode.hitTest(point2D);
    }

    public void positionCaret(HitInfo hitInfo, boolean bl) {
        this.positionCaret(hitInfo.getInsertionIndex(), hitInfo.isLeading(), bl);
    }

    private void positionCaret(int n, boolean bl, boolean bl2) {
        TextField textField = (TextField)this.getSkinnable();
        if (bl2) {
            textField.selectPositionCaret(n);
        } else {
            textField.positionCaret(n);
        }
        this.setForwardBias(bl);
    }

    @Override
    public Rectangle2D getCharacterBounds(int n) {
        double d;
        double d2;
        double d3;
        double d4;
        Bounds bounds;
        if (n == this.textNode.getText().length()) {
            bounds = this.textNode.getBoundsInLocal();
            d4 = bounds.getMaxX();
            d3 = 0.0;
            d2 = 0.0;
            d = bounds.getMaxY();
        } else {
            this.characterBoundingPath.getElements().clear();
            this.characterBoundingPath.getElements().addAll((PathElement[])this.textNode.rangeShape(n, n + 1));
            this.characterBoundingPath.setLayoutX(this.textNode.getLayoutX());
            this.characterBoundingPath.setLayoutY(this.textNode.getLayoutY());
            bounds = this.characterBoundingPath.getBoundsInLocal();
            d4 = bounds.getMinX();
            d3 = bounds.getMinY();
            d2 = bounds.isEmpty() ? 0.0 : bounds.getWidth();
            d = bounds.isEmpty() ? 0.0 : bounds.getHeight();
        }
        bounds = this.textGroup.getBoundsInParent();
        return new Rectangle2D(d4 + bounds.getMinX() + this.textTranslateX.get(), d3 + bounds.getMinY(), d2, d);
    }

    @Override
    protected PathElement[] getUnderlineShape(int n, int n2) {
        return this.textNode.underlineShape(n, n2);
    }

    @Override
    protected PathElement[] getRangeShape(int n, int n2) {
        return this.textNode.rangeShape(n, n2);
    }

    @Override
    protected void addHighlight(List<? extends Node> list, int n) {
        this.textGroup.getChildren().addAll((Collection<Node>)list);
    }

    @Override
    protected void removeHighlight(List<? extends Node> list) {
        this.textGroup.getChildren().removeAll((Collection<?>)list);
    }

    @Override
    public void moveCaret(TextInputControlSkin.TextUnit textUnit, TextInputControlSkin.Direction direction, boolean bl) {
        block0 : switch (textUnit) {
            case CHARACTER: {
                switch (direction) {
                    case LEFT: 
                    case RIGHT: {
                        this.nextCharacterVisually(direction == TextInputControlSkin.Direction.RIGHT);
                        break block0;
                    }
                }
                throw new IllegalArgumentException("" + direction);
            }
            default: {
                throw new IllegalArgumentException("" + textUnit);
            }
        }
    }

    private void nextCharacterVisually(boolean bl) {
        if (this.isRTL()) {
            bl = !bl;
        }
        Bounds bounds = this.caretPath.getLayoutBounds();
        if (this.caretPath.getElements().size() == 4) {
            bounds = new Path((PathElement)this.caretPath.getElements().get(0), (PathElement)this.caretPath.getElements().get(1)).getLayoutBounds();
        }
        double d = bl ? bounds.getMaxX() : bounds.getMinX();
        double d2 = (bounds.getMinY() + bounds.getMaxY()) / 2.0;
        HitInfo hitInfo = this.textNode.hitTest(new Point2D(d, d2));
        boolean bl2 = hitInfo.isLeading();
        Path path = new Path(this.textNode.rangeShape(hitInfo.getCharIndex(), hitInfo.getCharIndex() + 1));
        if (bl && path.getLayoutBounds().getMaxX() > bounds.getMaxX() || !bl && path.getLayoutBounds().getMinX() < bounds.getMinX()) {
            bl2 = !bl2;
        }
        this.positionCaret(hitInfo.getInsertionIndex(), bl2, false);
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        super.layoutChildren(d, d2, d3, d4);
        if (this.textNode != null) {
            double d5;
            Bounds bounds = this.textNode.getLayoutBounds();
            double d6 = this.textNode.getBaselineOffset();
            double d7 = bounds.getHeight() - d6;
            switch (((TextField)this.getSkinnable()).getAlignment().getVpos()) {
                case TOP: {
                    d5 = d6;
                    break;
                }
                case CENTER: {
                    d5 = (d6 + this.textGroup.getHeight() - d7) / 2.0;
                    break;
                }
                default: {
                    d5 = this.textGroup.getHeight() - d7;
                }
            }
            this.textNode.setY(d5);
            if (this.promptNode != null) {
                this.promptNode.setY(d5);
            }
            if (((TextField)this.getSkinnable()).getWidth() > 0.0) {
                this.updateTextPos();
                this.updateCaretOff();
            }
        }
        if (SHOW_HANDLES) {
            this.handleGroup.setLayoutX(d + this.caretWidth / 2.0);
            this.handleGroup.setLayoutY(d2);
            this.selectionHandle1.resize(this.selectionHandle1.prefWidth(-1.0), this.selectionHandle1.prefHeight(-1.0));
            this.selectionHandle2.resize(this.selectionHandle2.prefWidth(-1.0), this.selectionHandle2.prefHeight(-1.0));
            this.caretHandle.resize(this.caretHandle.prefWidth(-1.0), this.caretHandle.prefHeight(-1.0));
            Bounds bounds = this.caretPath.getBoundsInParent();
            this.caretHandle.setLayoutY(bounds.getMaxY() - 1.0);
            this.selectionHandle1.setLayoutY(bounds.getMinY() - this.selectionHandle1.getHeight() + 1.0);
            this.selectionHandle2.setLayoutY(bounds.getMaxY() - 1.0);
        }
    }

    private HPos getHAlignment() {
        HPos hPos = ((TextField)this.getSkinnable()).getAlignment().getHpos();
        return hPos;
    }

    @Override
    public Point2D getMenuPosition() {
        Point2D point2D = super.getMenuPosition();
        if (point2D != null) {
            point2D = new Point2D(Math.max(0.0, point2D.getX() - this.textNode.getLayoutX() - this.snappedLeftInset() + this.textTranslateX.get()), Math.max(0.0, point2D.getY() - this.textNode.getLayoutY() - this.snappedTopInset()));
        }
        return point2D;
    }

    @Override
    protected String maskText(String string) {
        if (this.getSkinnable() instanceof PasswordField) {
            int n = string.length();
            StringBuilder stringBuilder = new StringBuilder(n);
            for (int i = 0; i < n; ++i) {
                stringBuilder.append('\u25cf');
            }
            return stringBuilder.toString();
        }
        return string;
    }

    @Override
    protected Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case BOUNDS_FOR_RANGE: 
            case OFFSET_AT_POINT: {
                return this.textNode.queryAccessibleAttribute(accessibleAttribute, objectArray);
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    @Override
    TextInputControlBehavior getBehavior() {
        return this.behavior;
    }

    private void updateTextNodeCaretPos(int n) {
        if (n == 0 || this.isForwardBias()) {
            this.textNode.setCaretPosition(n);
        } else {
            this.textNode.setCaretPosition(n - 1);
        }
        this.textNode.caretBiasProperty().set(this.isForwardBias());
    }

    private void createPromptNode() {
        if (this.promptNode != null || !this.usePromptText.get()) {
            return;
        }
        this.promptNode = new Text();
        this.textGroup.getChildren().add(0, this.promptNode);
        this.promptNode.setManaged(false);
        this.promptNode.getStyleClass().add("text");
        this.promptNode.visibleProperty().bind(this.usePromptText);
        this.promptNode.fontProperty().bind(((TextField)this.getSkinnable()).fontProperty());
        this.promptNode.textProperty().bind(((TextField)this.getSkinnable()).promptTextProperty());
        this.promptNode.fillProperty().bind(this.promptTextFillProperty());
        this.updateSelection();
    }

    private void updateSelection() {
        TextField textField = (TextField)this.getSkinnable();
        IndexRange indexRange = textField.getSelection();
        if (indexRange == null || indexRange.getLength() == 0) {
            this.textNode.selectionStartProperty().set(-1);
            this.textNode.selectionEndProperty().set(-1);
        } else {
            this.textNode.selectionStartProperty().set(indexRange.getStart());
            this.textNode.selectionEndProperty().set(indexRange.getStart());
            this.textNode.selectionEndProperty().set(indexRange.getEnd());
        }
        PathElement[] pathElementArray = (PathElement[])this.textNode.selectionShapeProperty().get();
        if (pathElementArray == null) {
            this.selectionHighlightPath.getElements().clear();
        } else {
            this.selectionHighlightPath.getElements().setAll((PathElement[])pathElementArray);
        }
        if (SHOW_HANDLES && indexRange != null && indexRange.getLength() > 0) {
            int n = textField.getCaretPosition();
            int n2 = textField.getAnchor();
            this.updateTextNodeCaretPos(n2);
            Bounds bounds = this.caretPath.getBoundsInParent();
            if (n < n2) {
                this.selectionHandle2.setLayoutX(bounds.getMinX() - this.selectionHandle2.getWidth() / 2.0);
            } else {
                this.selectionHandle1.setLayoutX(bounds.getMinX() - this.selectionHandle1.getWidth() / 2.0);
            }
            this.updateTextNodeCaretPos(n);
            bounds = this.caretPath.getBoundsInParent();
            if (n < n2) {
                this.selectionHandle1.setLayoutX(bounds.getMinX() - this.selectionHandle1.getWidth() / 2.0);
            } else {
                this.selectionHandle2.setLayoutX(bounds.getMinX() - this.selectionHandle2.getWidth() / 2.0);
            }
        }
    }

    private void updateTextPos() {
        double d = this.textTranslateX.get();
        double d2 = this.textNode.getLayoutBounds().getWidth();
        switch (this.getHAlignment()) {
            case CENTER: {
                double d3;
                double d4 = this.textRight.get() / 2.0;
                if (this.usePromptText.get()) {
                    d3 = d4 - this.promptNode.getLayoutBounds().getWidth() / 2.0;
                    this.promptNode.setLayoutX(d3);
                } else {
                    d3 = d4 - d2 / 2.0;
                }
                if (!(d3 + d2 <= this.textRight.get())) break;
                this.textTranslateX.set(d3);
                break;
            }
            case RIGHT: {
                double d5 = this.textRight.get() - d2 - this.caretWidth / 2.0;
                if (d5 > d || d5 > 0.0) {
                    this.textTranslateX.set(d5);
                }
                if (!this.usePromptText.get()) break;
                this.promptNode.setLayoutX(this.textRight.get() - this.promptNode.getLayoutBounds().getWidth() - this.caretWidth / 2.0);
                break;
            }
            default: {
                double d6 = this.caretWidth / 2.0;
                if (d6 < d || d6 + d2 <= this.textRight.get()) {
                    this.textTranslateX.set(d6);
                }
                if (!this.usePromptText.get()) break;
                this.promptNode.layoutXProperty().set(d6);
            }
        }
    }

    private void updateCaretOff() {
        double d = 0.0;
        double d2 = this.caretPath.getLayoutBounds().getMinX() + this.textTranslateX.get();
        if (d2 < 0.0) {
            d = d2;
        } else if (d2 > this.textRight.get() - this.caretWidth) {
            d = d2 - (this.textRight.get() - this.caretWidth);
        }
        switch (this.getHAlignment()) {
            case CENTER: {
                this.textTranslateX.set(this.textTranslateX.get() - d);
                break;
            }
            case RIGHT: {
                this.textTranslateX.set(Math.max(this.textTranslateX.get() - d, this.textRight.get() - this.textNode.getLayoutBounds().getWidth() - this.caretWidth / 2.0));
                break;
            }
            default: {
                this.textTranslateX.set(Math.min(this.textTranslateX.get() - d, this.caretWidth / 2.0));
            }
        }
        if (SHOW_HANDLES) {
            this.caretHandle.setLayoutX(d2 - this.caretHandle.getWidth() / 2.0);
        }
    }

    private void scrollAfterDelete(double d, double d2) {
        Bounds bounds = this.textNode.getLayoutBounds();
        Bounds bounds2 = this.textNode.localToParent(bounds);
        Bounds bounds3 = this.clip.getBoundsInParent();
        Bounds bounds4 = this.caretPath.getLayoutBounds();
        switch (this.getHAlignment()) {
            case RIGHT: {
                if (bounds2.getMaxX() > bounds3.getMaxX()) {
                    double d3 = d2 - bounds4.getMaxX() - this.textTranslateX.get();
                    if (bounds2.getMaxX() + d3 < bounds3.getMaxX()) {
                        d3 = d <= bounds3.getMaxX() ? d - bounds2.getMaxX() : bounds3.getMaxX() - bounds2.getMaxX();
                    }
                    this.textTranslateX.set(this.textTranslateX.get() + d3);
                    break;
                }
                this.updateTextPos();
                break;
            }
            default: {
                if (!(bounds2.getMinX() < bounds3.getMinX() + this.caretWidth / 2.0) || !(bounds2.getMaxX() <= bounds3.getMaxX())) break;
                double d4 = d2 - bounds4.getMaxX() - this.textTranslateX.get();
                if (bounds2.getMaxX() + d4 < bounds3.getMaxX()) {
                    d4 = d <= bounds3.getMaxX() ? d - bounds2.getMaxX() : bounds3.getMaxX() - bounds2.getMaxX();
                }
                this.textTranslateX.set(this.textTranslateX.get() + d4);
            }
        }
        this.updateCaretOff();
    }
}

