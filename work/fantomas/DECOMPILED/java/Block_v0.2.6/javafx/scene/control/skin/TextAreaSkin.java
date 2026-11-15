/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.scene.control.behavior.TextAreaBehavior;
import com.sun.javafx.scene.control.skin.Utils;
import com.sun.javafx.tk.FontMetrics;
import java.util.Collection;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableIntegerValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.geometry.VerticalDirection;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.IndexRange;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.skin.TextInputControlSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.HitInfo;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class TextAreaSkin
extends TextInputControlSkin<TextArea> {
    private static final Path tmpCaretPath = new Path();
    private final TextArea textArea;
    private static final boolean USE_MULTIPLE_NODES = false;
    private final TextAreaBehavior behavior;
    private double computedMinWidth = Double.NEGATIVE_INFINITY;
    private double computedMinHeight = Double.NEGATIVE_INFINITY;
    private double computedPrefWidth = Double.NEGATIVE_INFINITY;
    private double computedPrefHeight = Double.NEGATIVE_INFINITY;
    private double widthForComputedPrefHeight = Double.NEGATIVE_INFINITY;
    private double characterWidth;
    private double lineHeight;
    private ContentView contentView = new ContentView();
    private Group paragraphNodes = new Group();
    private Text promptNode;
    private ObservableBooleanValue usePromptText;
    private ObservableIntegerValue caretPosition;
    private Group selectionHighlightGroup = new Group();
    private ScrollPane scrollPane;
    private Bounds oldViewportBounds;
    private VerticalDirection scrollDirection = null;
    private Path characterBoundingPath = new Path();
    private Timeline scrollSelectionTimeline = new Timeline();
    private EventHandler<ActionEvent> scrollSelectionHandler = actionEvent -> {
        switch (this.scrollDirection) {
            case UP: {
                break;
            }
        }
    };
    private double pressX;
    private double pressY;
    private boolean handlePressed;
    double targetCaretX = -1.0;

    public TextAreaSkin(final TextArea textArea) {
        super(textArea);
        this.behavior = new TextAreaBehavior(textArea);
        this.behavior.setTextAreaSkin(this);
        this.textArea = textArea;
        this.caretPosition = new IntegerBinding(){
            {
                this.bind(textArea.caretPositionProperty());
            }

            @Override
            protected int computeValue() {
                return textArea.getCaretPosition();
            }
        };
        this.caretPosition.addListener((observableValue, number, number2) -> {
            this.targetCaretX = -1.0;
            if (textArea.getWidth() > 0.0) {
                this.setForwardBias(true);
            }
        });
        this.forwardBiasProperty().addListener(observable -> {
            if (textArea.getWidth() > 0.0) {
                this.updateTextNodeCaretPos(textArea.getCaretPosition());
            }
        });
        this.scrollPane = new ScrollPane();
        this.scrollPane.setFitToWidth(textArea.isWrapText());
        this.scrollPane.setContent(this.contentView);
        this.getChildren().add(this.scrollPane);
        ((TextArea)this.getSkinnable()).addEventFilter(ScrollEvent.ANY, scrollEvent -> {
            if (scrollEvent.isDirect() && this.handlePressed) {
                scrollEvent.consume();
            }
        });
        this.selectionHighlightGroup.setManaged(false);
        this.selectionHighlightGroup.setVisible(false);
        this.contentView.getChildren().add(this.selectionHighlightGroup);
        this.paragraphNodes.setManaged(false);
        this.contentView.getChildren().add(this.paragraphNodes);
        this.caretPath.setManaged(false);
        this.caretPath.setStrokeWidth(1.0);
        this.caretPath.fillProperty().bind(this.textFillProperty());
        this.caretPath.strokeProperty().bind(this.textFillProperty());
        this.caretPath.opacityProperty().bind(new DoubleBinding(){
            {
                this.bind(TextAreaSkin.this.caretVisibleProperty());
            }

            @Override
            protected double computeValue() {
                return TextAreaSkin.this.caretVisibleProperty().get() ? 1.0 : 0.0;
            }
        });
        this.contentView.getChildren().add(this.caretPath);
        if (SHOW_HANDLES) {
            this.contentView.getChildren().addAll((Node[])new Node[]{this.caretHandle, this.selectionHandle1, this.selectionHandle2});
        }
        this.scrollPane.hvalueProperty().addListener((observableValue, number, number2) -> ((TextArea)this.getSkinnable()).setScrollLeft(number2.doubleValue() * this.getScrollLeftMax()));
        this.scrollPane.vvalueProperty().addListener((observableValue, number, number2) -> ((TextArea)this.getSkinnable()).setScrollTop(number2.doubleValue() * this.getScrollTopMax()));
        this.scrollSelectionTimeline.setCycleCount(-1);
        ObservableList<KeyFrame> observableList = this.scrollSelectionTimeline.getKeyFrames();
        observableList.clear();
        observableList.add(new KeyFrame(Duration.millis(350.0), this.scrollSelectionHandler, new KeyValue[0]));
        int n = 1;
        for (int i = 0; i < n; ++i) {
            String string = n == 1 ? textArea.textProperty().getValueSafe() : (CharSequence)textArea.getParagraphs().get(i);
            this.addParagraphNode(i, string.toString());
        }
        textArea.selectionProperty().addListener((observableValue, indexRange, indexRange2) -> {
            textArea.requestLayout();
            this.contentView.requestLayout();
        });
        textArea.wrapTextProperty().addListener((observableValue, bl, bl2) -> {
            this.invalidateMetrics();
            this.scrollPane.setFitToWidth((boolean)bl2);
        });
        textArea.prefColumnCountProperty().addListener((observableValue, number, number2) -> {
            this.invalidateMetrics();
            this.updatePrefViewportWidth();
        });
        textArea.prefRowCountProperty().addListener((observableValue, number, number2) -> {
            this.invalidateMetrics();
            this.updatePrefViewportHeight();
        });
        this.updateFontMetrics();
        this.fontMetrics.addListener(observable -> this.updateFontMetrics());
        this.contentView.paddingProperty().addListener(observable -> {
            this.updatePrefViewportWidth();
            this.updatePrefViewportHeight();
        });
        this.scrollPane.viewportBoundsProperty().addListener(observable -> {
            if (this.scrollPane.getViewportBounds() != null) {
                Bounds bounds = this.scrollPane.getViewportBounds();
                if (this.oldViewportBounds == null || this.oldViewportBounds.getWidth() != bounds.getWidth() || this.oldViewportBounds.getHeight() != bounds.getHeight()) {
                    this.invalidateMetrics();
                    this.oldViewportBounds = bounds;
                    this.contentView.requestLayout();
                }
            }
        });
        textArea.scrollTopProperty().addListener((observableValue, number, number2) -> {
            double d = number2.doubleValue() < this.getScrollTopMax() ? number2.doubleValue() / this.getScrollTopMax() : 1.0;
            this.scrollPane.setVvalue(d);
        });
        textArea.scrollLeftProperty().addListener((observableValue, number, number2) -> {
            double d = number2.doubleValue() < this.getScrollLeftMax() ? number2.doubleValue() / this.getScrollLeftMax() : 1.0;
            this.scrollPane.setHvalue(d);
        });
        textArea.textProperty().addListener(observable -> {
            this.invalidateMetrics();
            ((Text)this.paragraphNodes.getChildren().get(0)).setText(textArea.textProperty().getValueSafe());
            this.contentView.requestLayout();
        });
        this.usePromptText = new BooleanBinding(){
            {
                this.bind(textArea.textProperty(), textArea.promptTextProperty());
            }

            @Override
            protected boolean computeValue() {
                String string = textArea.getText();
                String string2 = textArea.getPromptText();
                return (string == null || string.isEmpty()) && string2 != null && !string2.isEmpty();
            }
        };
        if (this.usePromptText.get()) {
            this.createPromptNode();
        }
        this.usePromptText.addListener(observable -> {
            this.createPromptNode();
            textArea.requestLayout();
        });
        this.updateHighlightFill();
        this.updatePrefViewportWidth();
        this.updatePrefViewportHeight();
        if (textArea.isFocused()) {
            this.setCaretAnimating(true);
        }
        if (SHOW_HANDLES) {
            this.selectionHandle1.setRotate(180.0);
            EventHandler<MouseEvent> eventHandler = mouseEvent -> {
                this.pressX = mouseEvent.getX();
                this.pressY = mouseEvent.getY();
                this.handlePressed = true;
                mouseEvent.consume();
            };
            EventHandler<MouseEvent> eventHandler2 = mouseEvent -> {
                this.handlePressed = false;
            };
            this.caretHandle.setOnMousePressed(eventHandler);
            this.selectionHandle1.setOnMousePressed(eventHandler);
            this.selectionHandle2.setOnMousePressed(eventHandler);
            this.caretHandle.setOnMouseReleased(eventHandler2);
            this.selectionHandle1.setOnMouseReleased(eventHandler2);
            this.selectionHandle2.setOnMouseReleased(eventHandler2);
            this.caretHandle.setOnMouseDragged(mouseEvent -> {
                Text text = this.getTextNode();
                Point2D point2D = text.localToScene(0.0, 0.0);
                Point2D point2D2 = new Point2D(mouseEvent.getSceneX() - point2D.getX() - this.pressX + this.caretHandle.getWidth() / 2.0, mouseEvent.getSceneY() - point2D.getY() - this.pressY - 6.0);
                HitInfo hitInfo = text.hitTest(this.translateCaretPosition(point2D2));
                this.positionCaret(hitInfo, false);
                mouseEvent.consume();
            });
            this.selectionHandle1.setOnMouseDragged(mouseEvent -> {
                int n;
                TextArea textArea = (TextArea)this.getSkinnable();
                Text text = this.getTextNode();
                Point2D point2D = text.localToScene(0.0, 0.0);
                Point2D point2D2 = new Point2D(mouseEvent.getSceneX() - point2D.getX() - this.pressX + this.selectionHandle1.getWidth() / 2.0, mouseEvent.getSceneY() - point2D.getY() - this.pressY + this.selectionHandle1.getHeight() + 5.0);
                HitInfo hitInfo = text.hitTest(this.translateCaretPosition(point2D2));
                if (textArea.getAnchor() < textArea.getCaretPosition()) {
                    textArea.selectRange(textArea.getCaretPosition(), textArea.getAnchor());
                }
                if ((n = hitInfo.getCharIndex()) > 0 && n >= textArea.getAnchor()) {
                    n = textArea.getAnchor();
                }
                this.positionCaret(hitInfo, true);
                mouseEvent.consume();
            });
            this.selectionHandle2.setOnMouseDragged(mouseEvent -> {
                int n;
                TextArea textArea = (TextArea)this.getSkinnable();
                Text text = this.getTextNode();
                Point2D point2D = text.localToScene(0.0, 0.0);
                Point2D point2D2 = new Point2D(mouseEvent.getSceneX() - point2D.getX() - this.pressX + this.selectionHandle2.getWidth() / 2.0, mouseEvent.getSceneY() - point2D.getY() - this.pressY - 6.0);
                HitInfo hitInfo = text.hitTest(this.translateCaretPosition(point2D2));
                if (textArea.getAnchor() > textArea.getCaretPosition()) {
                    textArea.selectRange(textArea.getCaretPosition(), textArea.getAnchor());
                }
                if ((n = hitInfo.getCharIndex()) > 0) {
                    if (n <= textArea.getAnchor() + 1) {
                        n = Math.min(textArea.getAnchor() + 2, textArea.getLength());
                    }
                    this.positionCaret(hitInfo, true);
                }
                mouseEvent.consume();
            });
        }
    }

    @Override
    protected void invalidateMetrics() {
        this.computedMinWidth = Double.NEGATIVE_INFINITY;
        this.computedMinHeight = Double.NEGATIVE_INFINITY;
        this.computedPrefWidth = Double.NEGATIVE_INFINITY;
        this.computedPrefHeight = Double.NEGATIVE_INFINITY;
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        this.scrollPane.resizeRelocate(d, d2, d3, d4);
    }

    @Override
    protected void updateHighlightFill() {
        for (Node node : this.selectionHighlightGroup.getChildren()) {
            Path path = (Path)node;
            path.setFill((Paint)this.highlightFillProperty().get());
        }
    }

    public HitInfo getIndex(double d, double d2) {
        Text text = this.getTextNode();
        Point2D point2D = new Point2D(d - text.getLayoutX(), d2 - this.getTextTranslateY());
        HitInfo hitInfo = text.hitTest(this.translateCaretPosition(point2D));
        return hitInfo;
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
            case LINE: {
                switch (direction) {
                    case UP: {
                        this.previousLine(bl);
                        break block0;
                    }
                    case DOWN: {
                        this.nextLine(bl);
                        break block0;
                    }
                    case BEGINNING: {
                        this.lineStart(bl, bl && PlatformUtil.isMac());
                        break block0;
                    }
                    case END: {
                        this.lineEnd(bl, bl && PlatformUtil.isMac());
                        break block0;
                    }
                }
                throw new IllegalArgumentException("" + direction);
            }
            case PAGE: {
                switch (direction) {
                    case UP: {
                        this.previousPage(bl);
                        break block0;
                    }
                    case DOWN: {
                        this.nextPage(bl);
                        break block0;
                    }
                }
                throw new IllegalArgumentException("" + direction);
            }
            case PARAGRAPH: {
                switch (direction) {
                    case UP: {
                        this.paragraphStart(true, bl);
                        break block0;
                    }
                    case DOWN: {
                        this.paragraphEnd(true, bl);
                        break block0;
                    }
                    case BEGINNING: {
                        this.paragraphStart(false, bl);
                        break block0;
                    }
                    case END: {
                        this.paragraphEnd(false, bl);
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
        Text text = this.getTextNode();
        Bounds bounds = this.caretPath.getLayoutBounds();
        if (this.caretPath.getElements().size() == 4) {
            bounds = new Path((PathElement)this.caretPath.getElements().get(0), (PathElement)this.caretPath.getElements().get(1)).getLayoutBounds();
        }
        double d = bl ? bounds.getMaxX() : bounds.getMinX();
        double d2 = (bounds.getMinY() + bounds.getMaxY()) / 2.0;
        HitInfo hitInfo = text.hitTest(new Point2D(d, d2));
        boolean bl2 = hitInfo.isLeading();
        Path path = new Path(text.rangeShape(hitInfo.getCharIndex(), hitInfo.getCharIndex() + 1));
        if (bl && path.getLayoutBounds().getMaxX() > bounds.getMaxX() || !bl && path.getLayoutBounds().getMinX() < bounds.getMinX()) {
            bl2 = !bl2;
            this.positionCaret(hitInfo.getInsertionIndex(), bl2, false, false);
        } else {
            int n = this.textArea.getCaretPosition();
            this.targetCaretX = bl ? 0.0 : Double.MAX_VALUE;
            this.downLines(bl ? 1 : -1, false, false);
            this.targetCaretX = -1.0;
            if (n == this.textArea.getCaretPosition()) {
                if (bl) {
                    this.textArea.forward();
                } else {
                    this.textArea.backward();
                }
            }
        }
    }

    private void downLines(int n, boolean bl, boolean bl2) {
        Text text = this.getTextNode();
        Bounds bounds = this.caretPath.getLayoutBounds();
        double d = (bounds.getMinY() + bounds.getMaxY()) / 2.0 + (double)n * this.lineHeight;
        if (d < 0.0) {
            d = 0.0;
        }
        double d2 = this.targetCaretX >= 0.0 ? this.targetCaretX : bounds.getMaxX();
        HitInfo hitInfo = text.hitTest(this.translateCaretPosition(new Point2D(d2, d)));
        int n2 = hitInfo.getCharIndex();
        int n3 = text.getCaretPosition();
        boolean bl3 = text.isCaretBias();
        text.setCaretBias(hitInfo.isLeading());
        text.setCaretPosition(n2);
        tmpCaretPath.getElements().clear();
        tmpCaretPath.getElements().addAll((PathElement[])text.getCaretShape());
        tmpCaretPath.setLayoutX(text.getLayoutX());
        tmpCaretPath.setLayoutY(text.getLayoutY());
        Bounds bounds2 = tmpCaretPath.getLayoutBounds();
        double d3 = (bounds2.getMinY() + bounds2.getMaxY()) / 2.0;
        text.setCaretBias(bl3);
        text.setCaretPosition(n3);
        if (n == 0 || n > 0 && d3 > bounds.getMaxY() || n < 0 && d3 < bounds.getMinY()) {
            this.positionCaret(hitInfo.getInsertionIndex(), hitInfo.isLeading(), bl, bl2);
            this.targetCaretX = d2;
        }
    }

    private void previousLine(boolean bl) {
        this.downLines(-1, bl, false);
    }

    private void nextLine(boolean bl) {
        this.downLines(1, bl, false);
    }

    private void previousPage(boolean bl) {
        this.downLines(-((int)(this.scrollPane.getViewportBounds().getHeight() / this.lineHeight)), bl, false);
    }

    private void nextPage(boolean bl) {
        this.downLines((int)(this.scrollPane.getViewportBounds().getHeight() / this.lineHeight), bl, false);
    }

    private void lineStart(boolean bl, boolean bl2) {
        this.targetCaretX = 0.0;
        this.downLines(0, bl, bl2);
        this.targetCaretX = -1.0;
    }

    private void lineEnd(boolean bl, boolean bl2) {
        this.targetCaretX = Double.MAX_VALUE;
        this.downLines(0, bl, bl2);
        this.targetCaretX = -1.0;
    }

    private void paragraphStart(boolean bl, boolean bl2) {
        TextArea textArea = (TextArea)this.getSkinnable();
        String string = textArea.textProperty().getValueSafe();
        int n = textArea.getCaretPosition();
        if (n > 0) {
            if (bl && string.codePointAt(n - 1) == 10) {
                --n;
            }
            while (n > 0 && string.codePointAt(n - 1) != 10) {
                --n;
            }
            if (bl2) {
                textArea.selectPositionCaret(n);
            } else {
                textArea.positionCaret(n);
                this.setForwardBias(true);
            }
        }
    }

    private void paragraphEnd(boolean bl, boolean bl2) {
        TextArea textArea = (TextArea)this.getSkinnable();
        String string = textArea.textProperty().getValueSafe();
        int n = textArea.getCaretPosition();
        int n2 = string.length();
        boolean bl3 = false;
        boolean bl4 = PlatformUtil.isWindows();
        if (n < n2) {
            if (bl && string.codePointAt(n) == 10) {
                ++n;
                bl3 = true;
            }
            if (!bl4 || !bl3) {
                while (n < n2 && string.codePointAt(n) != 10) {
                    ++n;
                }
                if (bl4 && n < n2) {
                    ++n;
                }
            }
            if (bl2) {
                textArea.selectPositionCaret(n);
            } else {
                textArea.positionCaret(n);
            }
        }
    }

    @Override
    protected PathElement[] getUnderlineShape(int n, int n2) {
        int n3 = 0;
        for (Node node : this.paragraphNodes.getChildren()) {
            Text text = (Text)node;
            int n4 = n3 + text.textProperty().getValueSafe().length();
            if (n4 >= n) {
                return text.underlineShape(n - n3, n2 - n3);
            }
            n3 = n4 + 1;
        }
        return null;
    }

    @Override
    protected PathElement[] getRangeShape(int n, int n2) {
        int n3 = 0;
        for (Node node : this.paragraphNodes.getChildren()) {
            Text text = (Text)node;
            int n4 = n3 + text.textProperty().getValueSafe().length();
            if (n4 >= n) {
                return text.rangeShape(n - n3, n2 - n3);
            }
            n3 = n4 + 1;
        }
        return null;
    }

    @Override
    protected void addHighlight(List<? extends Node> list, int n) {
        int n2 = 0;
        Node node = null;
        for (Node node2 : this.paragraphNodes.getChildren()) {
            Text text = (Text)node2;
            int n3 = n2 + text.textProperty().getValueSafe().length();
            if (n3 >= n) {
                node = text;
                break;
            }
            n2 = n3 + 1;
        }
        if (node != null) {
            for (Node node2 : list) {
                node2.setLayoutX(node.getLayoutX());
                node2.setLayoutY(node.getLayoutY());
            }
        }
        this.contentView.getChildren().addAll((Collection<Node>)list);
    }

    @Override
    protected void removeHighlight(List<? extends Node> list) {
        this.contentView.getChildren().removeAll((Collection<?>)list);
    }

    @Override
    public Point2D getMenuPosition() {
        this.contentView.layoutChildren();
        Point2D point2D = super.getMenuPosition();
        if (point2D != null) {
            point2D = new Point2D(Math.max(0.0, point2D.getX() - this.contentView.snappedLeftInset() - ((TextArea)this.getSkinnable()).getScrollLeft()), Math.max(0.0, point2D.getY() - this.contentView.snappedTopInset() - ((TextArea)this.getSkinnable()).getScrollTop()));
        }
        return point2D;
    }

    public Bounds getCaretBounds() {
        return ((TextArea)this.getSkinnable()).sceneToLocal(this.caretPath.localToScene(this.caretPath.getBoundsInLocal()));
    }

    @Override
    protected Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case LINE_FOR_OFFSET: 
            case LINE_START: 
            case LINE_END: 
            case BOUNDS_FOR_RANGE: 
            case OFFSET_AT_POINT: {
                Text text = this.getTextNode();
                return text.queryAccessibleAttribute(accessibleAttribute, objectArray);
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public double computeBaselineOffset(double d, double d2, double d3, double d4) {
        Text text = (Text)this.paragraphNodes.getChildren().get(0);
        return Utils.getAscent(((TextArea)this.getSkinnable()).getFont(), text.getBoundsType()) + this.contentView.snappedTopInset() + this.textArea.snappedTopInset();
    }

    private char getCharacter(int n) {
        Text text;
        int n2;
        int n3;
        int n4 = this.paragraphNodes.getChildren().size();
        int n5 = 0;
        String string = null;
        for (n2 = n; n5 < n4 && n2 >= (n3 = (string = (text = (Text)this.paragraphNodes.getChildren().get(n5)).getText()).length() + 1); n2 -= n3, ++n5) {
        }
        return n2 == string.length() ? (char)'\n' : string.charAt(n2);
    }

    @Override
    protected int getInsertionPoint(double d, double d2) {
        TextArea textArea = (TextArea)this.getSkinnable();
        int n = this.paragraphNodes.getChildren().size();
        int n2 = -1;
        if (n > 0) {
            if (d2 < this.contentView.snappedTopInset()) {
                Text text = (Text)this.paragraphNodes.getChildren().get(0);
                n2 = this.getNextInsertionPoint(text, d, -1, VerticalDirection.DOWN);
            } else if (d2 > this.contentView.snappedTopInset() + this.contentView.getHeight()) {
                int n3 = n - 1;
                Text text = (Text)this.paragraphNodes.getChildren().get(n3);
                n2 = this.getNextInsertionPoint(text, d, -1, VerticalDirection.UP) + (textArea.getLength() - text.getText().length());
            } else {
                int n4 = 0;
                for (int i = 0; i < n; ++i) {
                    Text text = (Text)this.paragraphNodes.getChildren().get(i);
                    Bounds bounds = text.getBoundsInLocal();
                    double d3 = text.getLayoutY() + bounds.getMinY();
                    if (d2 >= d3 && d2 < d3 + text.getBoundsInLocal().getHeight()) {
                        n2 = this.getInsertionPoint(text, d - text.getLayoutX(), d2 - text.getLayoutY()) + n4;
                        break;
                    }
                    n4 += text.getText().length() + 1;
                }
            }
        }
        return n2;
    }

    public void positionCaret(HitInfo hitInfo, boolean bl) {
        this.positionCaret(hitInfo.getInsertionIndex(), hitInfo.isLeading(), bl, false);
    }

    private void positionCaret(int n, boolean bl, boolean bl2, boolean bl3) {
        boolean bl4;
        boolean bl5 = bl4 = n > 0 && n <= ((TextArea)this.getSkinnable()).getLength() && ((TextArea)this.getSkinnable()).getText().codePointAt(n - 1) == 10;
        if (!bl && bl4) {
            bl = true;
            --n;
        }
        if (bl2) {
            if (bl3) {
                ((TextArea)this.getSkinnable()).extendSelection(n);
            } else {
                ((TextArea)this.getSkinnable()).selectPositionCaret(n);
            }
        } else {
            ((TextArea)this.getSkinnable()).positionCaret(n);
        }
        this.setForwardBias(bl);
    }

    @Override
    public Rectangle2D getCharacterBounds(int n) {
        double d;
        TextArea textArea = (TextArea)this.getSkinnable();
        int n2 = this.paragraphNodes.getChildren().size();
        int n3 = textArea.getLength() + 1;
        Text text = null;
        while (n < (n3 -= (text = (Text)this.paragraphNodes.getChildren().get(--n2)).getText().length() + 1)) {
        }
        int n4 = n - n3;
        boolean bl = false;
        if (n4 == text.getText().length()) {
            --n4;
            bl = true;
        }
        this.characterBoundingPath.getElements().clear();
        this.characterBoundingPath.getElements().addAll((PathElement[])text.rangeShape(n4, n4 + 1));
        this.characterBoundingPath.setLayoutX(text.getLayoutX());
        this.characterBoundingPath.setLayoutY(text.getLayoutY());
        Bounds bounds = this.characterBoundingPath.getBoundsInLocal();
        double d2 = bounds.getMinX() + text.getLayoutX() - textArea.getScrollLeft();
        double d3 = bounds.getMinY() + text.getLayoutY() - textArea.getScrollTop();
        double d4 = bounds.isEmpty() ? 0.0 : bounds.getWidth();
        double d5 = d = bounds.isEmpty() ? 0.0 : bounds.getHeight();
        if (bl) {
            d2 += d4;
            d4 = 0.0;
        }
        return new Rectangle2D(d2, d3, d4, d);
    }

    @Override
    protected void scrollCharacterToVisible(int n) {
        Platform.runLater(() -> {
            if (((TextArea)this.getSkinnable()).getLength() == 0) {
                return;
            }
            Rectangle2D rectangle2D = this.getCharacterBounds(n);
            this.scrollBoundsToVisible(rectangle2D);
        });
    }

    @Override
    TextAreaBehavior getBehavior() {
        return this.behavior;
    }

    private void createPromptNode() {
        if (this.promptNode == null && this.usePromptText.get()) {
            this.promptNode = new Text();
            this.contentView.getChildren().add(0, this.promptNode);
            this.promptNode.setManaged(false);
            this.promptNode.getStyleClass().add("text");
            this.promptNode.visibleProperty().bind(this.usePromptText);
            this.promptNode.fontProperty().bind(((TextArea)this.getSkinnable()).fontProperty());
            this.promptNode.textProperty().bind(((TextArea)this.getSkinnable()).promptTextProperty());
            this.promptNode.fillProperty().bind(this.promptTextFillProperty());
        }
    }

    private void addParagraphNode(int n, String string) {
        TextArea textArea = (TextArea)this.getSkinnable();
        Text text = new Text(string);
        text.setTextOrigin(VPos.TOP);
        text.setManaged(false);
        text.getStyleClass().add("text");
        text.boundsTypeProperty().addListener((observableValue, textBoundsType, textBoundsType2) -> {
            this.invalidateMetrics();
            this.updateFontMetrics();
        });
        this.paragraphNodes.getChildren().add(n, text);
        text.fontProperty().bind(textArea.fontProperty());
        text.fillProperty().bind(this.textFillProperty());
        text.selectionFillProperty().bind(this.highlightTextFillProperty());
    }

    private double getScrollTopMax() {
        return Math.max(0.0, this.contentView.getHeight() - this.scrollPane.getViewportBounds().getHeight());
    }

    private double getScrollLeftMax() {
        return Math.max(0.0, this.contentView.getWidth() - this.scrollPane.getViewportBounds().getWidth());
    }

    private int getInsertionPoint(Text text, double d, double d2) {
        HitInfo hitInfo = text.hitTest(new Point2D(d, d2));
        return hitInfo.getInsertionIndex();
    }

    private int getNextInsertionPoint(Text text, double d, int n, VerticalDirection verticalDirection) {
        return 0;
    }

    private void scrollCaretToVisible() {
        TextArea textArea = (TextArea)this.getSkinnable();
        Bounds bounds = this.caretPath.getLayoutBounds();
        double d = bounds.getMinX() - textArea.getScrollLeft();
        double d2 = bounds.getMinY() - textArea.getScrollTop();
        double d3 = bounds.getWidth();
        double d4 = bounds.getHeight();
        if (SHOW_HANDLES) {
            if (this.caretHandle.isVisible()) {
                d4 += this.caretHandle.getHeight();
            } else if (this.selectionHandle1.isVisible() && this.selectionHandle2.isVisible()) {
                d -= this.selectionHandle1.getWidth() / 2.0;
                d2 -= this.selectionHandle1.getHeight();
                d3 += this.selectionHandle1.getWidth() / 2.0 + this.selectionHandle2.getWidth() / 2.0;
                d4 += this.selectionHandle1.getHeight() + this.selectionHandle2.getHeight();
            }
        }
        if (d3 > 0.0 && d4 > 0.0) {
            this.scrollBoundsToVisible(new Rectangle2D(d, d2, d3, d4));
        }
    }

    private void scrollBoundsToVisible(Rectangle2D rectangle2D) {
        double d;
        TextArea textArea = (TextArea)this.getSkinnable();
        Bounds bounds = this.scrollPane.getViewportBounds();
        double d2 = bounds.getWidth();
        double d3 = bounds.getHeight();
        double d4 = textArea.getScrollTop();
        double d5 = textArea.getScrollLeft();
        double d6 = 6.0;
        if (rectangle2D.getMinY() < 0.0) {
            d = d4 + rectangle2D.getMinY();
            if (d <= this.contentView.snappedTopInset()) {
                d = 0.0;
            }
            textArea.setScrollTop(d);
        } else if (this.contentView.snappedTopInset() + rectangle2D.getMaxY() > d3) {
            d = d4 + this.contentView.snappedTopInset() + rectangle2D.getMaxY() - d3;
            if (d >= this.getScrollTopMax() - this.contentView.snappedBottomInset()) {
                d = this.getScrollTopMax();
            }
            textArea.setScrollTop(d);
        }
        if (rectangle2D.getMinX() < 0.0) {
            d = d5 + rectangle2D.getMinX() - d6;
            if (d <= this.contentView.snappedLeftInset() + d6) {
                d = 0.0;
            }
            textArea.setScrollLeft(d);
        } else if (this.contentView.snappedLeftInset() + rectangle2D.getMaxX() > d2) {
            d = d5 + this.contentView.snappedLeftInset() + rectangle2D.getMaxX() - d2 + d6;
            if (d >= this.getScrollLeftMax() - this.contentView.snappedRightInset() - d6) {
                d = this.getScrollLeftMax();
            }
            textArea.setScrollLeft(d);
        }
    }

    private void updatePrefViewportWidth() {
        int n = ((TextArea)this.getSkinnable()).getPrefColumnCount();
        this.scrollPane.setPrefViewportWidth((double)n * this.characterWidth + this.contentView.snappedLeftInset() + this.contentView.snappedRightInset());
        this.scrollPane.setMinViewportWidth(this.characterWidth + this.contentView.snappedLeftInset() + this.contentView.snappedRightInset());
    }

    private void updatePrefViewportHeight() {
        int n = ((TextArea)this.getSkinnable()).getPrefRowCount();
        this.scrollPane.setPrefViewportHeight((double)n * this.lineHeight + this.contentView.snappedTopInset() + this.contentView.snappedBottomInset());
        this.scrollPane.setMinViewportHeight(this.lineHeight + this.contentView.snappedTopInset() + this.contentView.snappedBottomInset());
    }

    private void updateFontMetrics() {
        Text text = (Text)this.paragraphNodes.getChildren().get(0);
        this.lineHeight = Utils.getLineHeight(((TextArea)this.getSkinnable()).getFont(), text.getBoundsType());
        this.characterWidth = ((FontMetrics)this.fontMetrics.get()).getCharWidth('W');
    }

    private double getTextTranslateX() {
        return this.contentView.snappedLeftInset();
    }

    private double getTextTranslateY() {
        return this.contentView.snappedTopInset();
    }

    private double getTextLeft() {
        return 0.0;
    }

    private Point2D translateCaretPosition(Point2D point2D) {
        return point2D;
    }

    private Text getTextNode() {
        return (Text)this.paragraphNodes.getChildren().get(0);
    }

    private void updateTextNodeCaretPos(int n) {
        Text text = this.getTextNode();
        if (this.isForwardBias()) {
            text.setCaretPosition(n);
        } else {
            text.setCaretPosition(n - 1);
        }
        text.caretBiasProperty().set(this.isForwardBias());
    }

    private /* synthetic */ void lambda$new$15(ListChangeListener.Change change) {
        while (change.next()) {
            int n = change.getFrom();
            int n2 = change.getTo();
            List list = change.getRemoved();
            if (n < n2) {
                int n3;
                int n4;
                if (list.isEmpty()) {
                    n4 = n2;
                    for (n3 = n; n3 < n4; ++n3) {
                        this.addParagraphNode(n3, ((CharSequence)change.getList().get(n3)).toString());
                    }
                    continue;
                }
                n4 = n2;
                for (n3 = n; n3 < n4; ++n3) {
                    Node node = (Node)this.paragraphNodes.getChildren().get(n3);
                    Text text = (Text)node;
                    text.setText(((CharSequence)change.getList().get(n3)).toString());
                }
                continue;
            }
            this.paragraphNodes.getChildren().subList(n, n + list.size()).clear();
        }
    }

    private class ContentView
    extends Region {
        private ContentView() {
            this.getStyleClass().add("content");
            this.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
                TextAreaSkin.this.behavior.mousePressed((MouseEvent)mouseEvent);
                mouseEvent.consume();
            });
            this.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
                TextAreaSkin.this.behavior.mouseReleased((MouseEvent)mouseEvent);
                mouseEvent.consume();
            });
            this.addEventHandler(MouseEvent.MOUSE_DRAGGED, mouseEvent -> {
                TextAreaSkin.this.behavior.mouseDragged((MouseEvent)mouseEvent);
                mouseEvent.consume();
            });
        }

        @Override
        protected ObservableList<Node> getChildren() {
            return super.getChildren();
        }

        @Override
        public Orientation getContentBias() {
            return Orientation.HORIZONTAL;
        }

        @Override
        protected double computePrefWidth(double d) {
            if (TextAreaSkin.this.computedPrefWidth < 0.0) {
                double d2 = 0.0;
                Object object = TextAreaSkin.this.paragraphNodes.getChildren().iterator();
                while (object.hasNext()) {
                    Node node = (Node)object.next();
                    Text text = (Text)node;
                    d2 = Math.max(d2, Utils.computeTextWidth(text.getFont(), text.getText(), 0.0));
                }
                TextAreaSkin.this.computedPrefWidth = Math.max(d2 += this.snappedLeftInset() + this.snappedRightInset(), (object = TextAreaSkin.this.scrollPane.getViewportBounds()) != null ? ((Bounds)object).getWidth() : 0.0);
            }
            return TextAreaSkin.this.computedPrefWidth;
        }

        @Override
        protected double computePrefHeight(double d) {
            if (d != TextAreaSkin.this.widthForComputedPrefHeight) {
                TextAreaSkin.this.invalidateMetrics();
                TextAreaSkin.this.widthForComputedPrefHeight = d;
            }
            if (TextAreaSkin.this.computedPrefHeight < 0.0) {
                double d2 = d == -1.0 ? 0.0 : Math.max(d - (this.snappedLeftInset() + this.snappedRightInset()), 0.0);
                double d3 = 0.0;
                Object object = TextAreaSkin.this.paragraphNodes.getChildren().iterator();
                while (object.hasNext()) {
                    Node node = (Node)object.next();
                    Text text = (Text)node;
                    d3 += Utils.computeTextHeight(text.getFont(), text.getText(), d2, text.getBoundsType());
                }
                TextAreaSkin.this.computedPrefHeight = Math.max(d3 += this.snappedTopInset() + this.snappedBottomInset(), (object = TextAreaSkin.this.scrollPane.getViewportBounds()) != null ? ((Bounds)object).getHeight() : 0.0);
            }
            return TextAreaSkin.this.computedPrefHeight;
        }

        @Override
        protected double computeMinWidth(double d) {
            if (TextAreaSkin.this.computedMinWidth < 0.0) {
                double d2 = this.snappedLeftInset() + this.snappedRightInset();
                TextAreaSkin.this.computedMinWidth = Math.min(TextAreaSkin.this.characterWidth + d2, this.computePrefWidth(d));
            }
            return TextAreaSkin.this.computedMinWidth;
        }

        @Override
        protected double computeMinHeight(double d) {
            if (TextAreaSkin.this.computedMinHeight < 0.0) {
                double d2 = this.snappedTopInset() + this.snappedBottomInset();
                TextAreaSkin.this.computedMinHeight = Math.min(TextAreaSkin.this.lineHeight + d2, this.computePrefHeight(d));
            }
            return TextAreaSkin.this.computedMinHeight;
        }

        @Override
        public void layoutChildren() {
            int n;
            Text text;
            int n2;
            int n3;
            Object object;
            TextArea textArea = (TextArea)TextAreaSkin.this.getSkinnable();
            double d = this.getWidth();
            double d2 = this.snappedTopInset();
            double d3 = this.snappedLeftInset();
            double d4 = Math.max(d - (d3 + this.snappedRightInset()), 0.0);
            double d5 = d2;
            ObservableList<Node> observableList = TextAreaSkin.this.paragraphNodes.getChildren();
            for (int i = 0; i < observableList.size(); ++i) {
                object = (Node)observableList.get(i);
                Text text2 = (Text)object;
                text2.setWrappingWidth(d4);
                Bounds bounds = text2.getBoundsInLocal();
                text2.setLayoutX(d3);
                text2.setLayoutY(d5);
                d5 += bounds.getHeight();
            }
            if (TextAreaSkin.this.promptNode != null) {
                TextAreaSkin.this.promptNode.setLayoutX(d3);
                TextAreaSkin.this.promptNode.setLayoutY(d2 + TextAreaSkin.this.promptNode.getBaselineOffset());
                TextAreaSkin.this.promptNode.setWrappingWidth(d4);
            }
            IndexRange indexRange = textArea.getSelection();
            object = TextAreaSkin.this.caretPath.getBoundsInParent();
            TextAreaSkin.this.selectionHighlightGroup.getChildren().clear();
            int n4 = textArea.getCaretPosition();
            int n5 = textArea.getAnchor();
            if (TextInputControlSkin.SHOW_HANDLES) {
                if (indexRange.getLength() > 0) {
                    TextAreaSkin.this.selectionHandle1.resize(TextAreaSkin.this.selectionHandle1.prefWidth(-1.0), TextAreaSkin.this.selectionHandle1.prefHeight(-1.0));
                    TextAreaSkin.this.selectionHandle2.resize(TextAreaSkin.this.selectionHandle2.prefWidth(-1.0), TextAreaSkin.this.selectionHandle2.prefHeight(-1.0));
                } else {
                    TextAreaSkin.this.caretHandle.resize(TextAreaSkin.this.caretHandle.prefWidth(-1.0), TextAreaSkin.this.caretHandle.prefHeight(-1.0));
                }
                if (indexRange.getLength() > 0) {
                    n3 = observableList.size();
                    n2 = textArea.getLength() + 1;
                    text = null;
                    while (n5 < (n2 -= (text = (Text)observableList.get(--n3)).getText().length() + 1)) {
                    }
                    TextAreaSkin.this.updateTextNodeCaretPos(n5 - n2);
                    TextAreaSkin.this.caretPath.getElements().clear();
                    TextAreaSkin.this.caretPath.getElements().addAll((PathElement[])text.getCaretShape());
                    TextAreaSkin.this.caretPath.setLayoutX(text.getLayoutX());
                    TextAreaSkin.this.caretPath.setLayoutY(text.getLayoutY());
                    Bounds bounds = TextAreaSkin.this.caretPath.getBoundsInParent();
                    if (n4 < n5) {
                        TextAreaSkin.this.selectionHandle2.setLayoutX(bounds.getMinX() - TextAreaSkin.this.selectionHandle2.getWidth() / 2.0);
                        TextAreaSkin.this.selectionHandle2.setLayoutY(bounds.getMaxY() - 1.0);
                    } else {
                        TextAreaSkin.this.selectionHandle1.setLayoutX(bounds.getMinX() - TextAreaSkin.this.selectionHandle1.getWidth() / 2.0);
                        TextAreaSkin.this.selectionHandle1.setLayoutY(bounds.getMinY() - TextAreaSkin.this.selectionHandle1.getHeight() + 1.0);
                    }
                }
            }
            n3 = observableList.size();
            n2 = textArea.getLength() + 1;
            text = null;
            while (n4 < (n2 -= (text = (Text)observableList.get(--n3)).getText().length() + 1)) {
            }
            TextAreaSkin.this.updateTextNodeCaretPos(n4 - n2);
            TextAreaSkin.this.caretPath.getElements().clear();
            TextAreaSkin.this.caretPath.getElements().addAll((PathElement[])text.getCaretShape());
            TextAreaSkin.this.caretPath.setLayoutX(text.getLayoutX());
            text.setLayoutX(2.0 * text.getLayoutX() - text.getBoundsInParent().getMinX());
            TextAreaSkin.this.caretPath.setLayoutY(text.getLayoutY());
            if (object == null || !object.equals(TextAreaSkin.this.caretPath.getBoundsInParent())) {
                TextAreaSkin.this.scrollCaretToVisible();
            }
            n3 = indexRange.getStart();
            n2 = indexRange.getEnd();
            int n6 = observableList.size();
            for (int i = 0; i < n6; i += 1) {
                Node node = (Node)observableList.get(i);
                Text text3 = (Text)node;
                n = text3.getText().length() + 1;
                if (n2 > n3 && n3 < n) {
                    text3.setSelectionStart(n3);
                    text3.setSelectionEnd(Math.min(n2, n));
                    Path path = new Path();
                    path.setManaged(false);
                    path.setStroke(null);
                    PathElement[] pathElementArray = text3.getSelectionShape();
                    if (pathElementArray != null) {
                        path.getElements().addAll((PathElement[])pathElementArray);
                    }
                    TextAreaSkin.this.selectionHighlightGroup.getChildren().add(path);
                    TextAreaSkin.this.selectionHighlightGroup.setVisible(true);
                    path.setLayoutX(text3.getLayoutX());
                    path.setLayoutY(text3.getLayoutY());
                    TextAreaSkin.this.updateHighlightFill();
                } else {
                    text3.setSelectionStart(-1);
                    text3.setSelectionEnd(-1);
                    TextAreaSkin.this.selectionHighlightGroup.setVisible(false);
                }
                n3 = Math.max(0, n3 - n);
                n2 = Math.max(0, n2 - n);
            }
            if (TextInputControlSkin.SHOW_HANDLES) {
                Bounds bounds = TextAreaSkin.this.caretPath.getBoundsInParent();
                if (indexRange.getLength() > 0) {
                    if (n4 < n5) {
                        TextAreaSkin.this.selectionHandle1.setLayoutX(bounds.getMinX() - TextAreaSkin.this.selectionHandle1.getWidth() / 2.0);
                        TextAreaSkin.this.selectionHandle1.setLayoutY(bounds.getMinY() - TextAreaSkin.this.selectionHandle1.getHeight() + 1.0);
                    } else {
                        TextAreaSkin.this.selectionHandle2.setLayoutX(bounds.getMinX() - TextAreaSkin.this.selectionHandle2.getWidth() / 2.0);
                        TextAreaSkin.this.selectionHandle2.setLayoutY(bounds.getMaxY() - 1.0);
                    }
                } else {
                    TextAreaSkin.this.caretHandle.setLayoutX(bounds.getMinX() - TextAreaSkin.this.caretHandle.getWidth() / 2.0 + 1.0);
                    TextAreaSkin.this.caretHandle.setLayoutY(bounds.getMaxY());
                }
            }
            if (TextAreaSkin.this.scrollPane.getPrefViewportWidth() == 0.0 || TextAreaSkin.this.scrollPane.getPrefViewportHeight() == 0.0) {
                TextAreaSkin.this.updatePrefViewportWidth();
                TextAreaSkin.this.updatePrefViewportHeight();
                if (this.getParent() != null && TextAreaSkin.this.scrollPane.getPrefViewportWidth() > 0.0 || TextAreaSkin.this.scrollPane.getPrefViewportHeight() > 0.0) {
                    this.getParent().requestLayout();
                }
            }
            Bounds bounds = TextAreaSkin.this.scrollPane.getViewportBounds();
            n6 = TextAreaSkin.this.scrollPane.isFitToWidth() ? 1 : 0;
            int n7 = TextAreaSkin.this.scrollPane.isFitToHeight();
            int n8 = textArea.isWrapText() || this.computePrefWidth(-1.0) <= bounds.getWidth() ? 1 : 0;
            int n9 = n = this.computePrefHeight(d) <= bounds.getHeight() ? 1 : 0;
            if (n6 != n8 || n7 != n) {
                Platform.runLater(() -> this.lambda$layoutChildren$3(n8 != 0, n != 0));
                this.getParent().requestLayout();
            }
        }

        private /* synthetic */ void lambda$layoutChildren$3(boolean bl, boolean bl2) {
            TextAreaSkin.this.scrollPane.setFitToWidth(bl);
            TextAreaSkin.this.scrollPane.setFitToHeight(bl2);
        }
    }
}

