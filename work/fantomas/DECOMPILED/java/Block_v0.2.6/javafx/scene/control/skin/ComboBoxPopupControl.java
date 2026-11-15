/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.ParentHelper;
import com.sun.javafx.scene.control.FakeFocusTextField;
import com.sun.javafx.scene.input.ExtendedInputMethodRequests;
import com.sun.javafx.scene.traversal.Algorithm;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import com.sun.javafx.scene.traversal.TraversalContext;
import com.sun.javafx.util.Utils;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.css.Styleable;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.control.Skinnable;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.ComboBoxBaseSkin;
import javafx.scene.input.DragEvent;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;

public abstract class ComboBoxPopupControl<T>
extends ComboBoxBaseSkin<T> {
    PopupControl popup;
    private boolean popupNeedsReconfiguring = true;
    private final ComboBoxBase<T> comboBoxBase;
    private TextField textField;
    private String initialTextFieldValue = null;
    private EventHandler<MouseEvent> textFieldMouseEventHandler = mouseEvent -> {
        ComboBoxBase comboBoxBase = (ComboBoxBase)this.getSkinnable();
        if (!mouseEvent.getTarget().equals(comboBoxBase)) {
            comboBoxBase.fireEvent(mouseEvent.copyFor(comboBoxBase, comboBoxBase));
            mouseEvent.consume();
        }
    };
    private EventHandler<DragEvent> textFieldDragEventHandler = dragEvent -> {
        ComboBoxBase comboBoxBase = (ComboBoxBase)this.getSkinnable();
        if (!dragEvent.getTarget().equals(comboBoxBase)) {
            comboBoxBase.fireEvent(dragEvent.copyFor(comboBoxBase, comboBoxBase));
            dragEvent.consume();
        }
    };

    public ComboBoxPopupControl(ComboBoxBase<T> comboBoxBase) {
        super(comboBoxBase);
        this.comboBoxBase = comboBoxBase;
        TextField textField = this.textField = this.getEditor() != null ? this.getEditableInputNode() : null;
        if (this.textField != null) {
            this.getChildren().add(this.textField);
        }
        this.comboBoxBase.focusedProperty().addListener((observableValue, bl, bl2) -> {
            if (this.getEditor() != null) {
                ((FakeFocusTextField)this.textField).setFakeFocus((boolean)bl2);
            }
        });
        this.comboBoxBase.addEventFilter(KeyEvent.ANY, keyEvent -> {
            if (this.textField == null || this.getEditor() == null) {
                this.handleKeyEvent((KeyEvent)keyEvent, false);
            } else {
                if (keyEvent.getTarget().equals(this.textField)) {
                    return;
                }
                switch (keyEvent.getCode()) {
                    case ESCAPE: 
                    case F10: {
                        break;
                    }
                    case ENTER: {
                        this.handleKeyEvent((KeyEvent)keyEvent, true);
                        break;
                    }
                    default: {
                        this.textField.fireEvent(keyEvent.copyFor(this.textField, this.textField));
                        keyEvent.consume();
                    }
                }
            }
        });
        if (this.comboBoxBase.getOnInputMethodTextChanged() == null) {
            this.comboBoxBase.setOnInputMethodTextChanged(inputMethodEvent -> {
                if (this.textField != null && this.getEditor() != null && this.comboBoxBase.getScene().getFocusOwner() == this.comboBoxBase && this.textField.getOnInputMethodTextChanged() != null) {
                    this.textField.getOnInputMethodTextChanged().handle((InputMethodEvent)inputMethodEvent);
                }
            });
        }
        ParentHelper.setTraversalEngine(this.comboBoxBase, new ParentTraversalEngine(this.comboBoxBase, new Algorithm(){

            @Override
            public Node select(Node node, Direction direction, TraversalContext traversalContext) {
                return null;
            }

            @Override
            public Node selectFirst(TraversalContext traversalContext) {
                return null;
            }

            @Override
            public Node selectLast(TraversalContext traversalContext) {
                return null;
            }
        }));
        this.updateEditable();
    }

    protected abstract Node getPopupContent();

    protected abstract TextField getEditor();

    protected abstract StringConverter<T> getConverter();

    @Override
    public void show() {
        if (this.getSkinnable() == null) {
            throw new IllegalStateException("ComboBox is null");
        }
        Node node = this.getPopupContent();
        if (node == null) {
            throw new IllegalStateException("Popup node is null");
        }
        if (this.getPopup().isShowing()) {
            return;
        }
        this.positionAndShowPopup();
    }

    @Override
    public void hide() {
        if (this.popup != null && this.popup.isShowing()) {
            this.popup.hide();
        }
    }

    PopupControl getPopup() {
        if (this.popup == null) {
            this.createPopup();
        }
        return this.popup;
    }

    TextField getEditableInputNode() {
        if (this.textField == null && this.getEditor() != null) {
            this.textField = this.getEditor();
            this.textField.setFocusTraversable(false);
            this.textField.promptTextProperty().bind(this.comboBoxBase.promptTextProperty());
            this.textField.tooltipProperty().bind(this.comboBoxBase.tooltipProperty());
            this.textField.getProperties().put("TextInputControlBehavior.disableForwardToParent", true);
            this.initialTextFieldValue = this.textField.getText();
        }
        return this.textField;
    }

    void setTextFromTextFieldIntoComboBoxValue() {
        StringConverter<T> stringConverter;
        if (this.getEditor() != null && (stringConverter = this.getConverter()) != null) {
            T t;
            T t2 = t = this.comboBoxBase.getValue();
            String string = this.textField.getText();
            if (t == null && (string == null || string.isEmpty())) {
                t2 = null;
            } else {
                try {
                    t2 = stringConverter.fromString(string);
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            if (!(t2 == null && t == null || t2 != null && t2.equals(t))) {
                this.comboBoxBase.setValue(t2);
            }
            this.updateDisplayNode();
        }
    }

    void updateDisplayNode() {
        if (this.textField != null && this.getEditor() != null) {
            T t = this.comboBoxBase.getValue();
            StringConverter<T> stringConverter = this.getConverter();
            if (this.initialTextFieldValue != null && !this.initialTextFieldValue.isEmpty()) {
                this.textField.setText(this.initialTextFieldValue);
                this.initialTextFieldValue = null;
            } else {
                String string = stringConverter.toString(t);
                if (t == null || string == null) {
                    this.textField.setText("");
                } else if (!string.equals(this.textField.getText())) {
                    this.textField.setText(string);
                }
            }
        }
    }

    void updateEditable() {
        final TextField textField = this.getEditor();
        if (this.getEditor() == null) {
            if (this.textField != null) {
                this.textField.removeEventFilter(MouseEvent.DRAG_DETECTED, this.textFieldMouseEventHandler);
                this.textField.removeEventFilter(DragEvent.ANY, this.textFieldDragEventHandler);
                this.comboBoxBase.setInputMethodRequests(null);
            }
        } else if (textField != null) {
            textField.addEventFilter(MouseEvent.DRAG_DETECTED, this.textFieldMouseEventHandler);
            textField.addEventFilter(DragEvent.ANY, this.textFieldDragEventHandler);
            this.comboBoxBase.setInputMethodRequests(new ExtendedInputMethodRequests(){

                @Override
                public Point2D getTextLocation(int n) {
                    return textField.getInputMethodRequests().getTextLocation(n);
                }

                @Override
                public int getLocationOffset(int n, int n2) {
                    return textField.getInputMethodRequests().getLocationOffset(n, n2);
                }

                @Override
                public void cancelLatestCommittedText() {
                    textField.getInputMethodRequests().cancelLatestCommittedText();
                }

                @Override
                public String getSelectedText() {
                    return textField.getInputMethodRequests().getSelectedText();
                }

                @Override
                public int getInsertPositionOffset() {
                    return ((ExtendedInputMethodRequests)textField.getInputMethodRequests()).getInsertPositionOffset();
                }

                @Override
                public String getCommittedText(int n, int n2) {
                    return ((ExtendedInputMethodRequests)textField.getInputMethodRequests()).getCommittedText(n, n2);
                }

                @Override
                public int getCommittedTextLength() {
                    return ((ExtendedInputMethodRequests)textField.getInputMethodRequests()).getCommittedTextLength();
                }
            });
        }
        this.textField = textField;
    }

    private Point2D getPrefPopupPosition() {
        return Utils.pointRelativeTo((Node)((Object)this.getSkinnable()), this.getPopupContent(), HPos.CENTER, VPos.BOTTOM, 0.0, 0.0, true);
    }

    private void positionAndShowPopup() {
        ComboBoxBase comboBoxBase = (ComboBoxBase)this.getSkinnable();
        if (comboBoxBase.getScene() == null) {
            return;
        }
        PopupControl popupControl = this.getPopup();
        popupControl.getScene().setNodeOrientation(((ComboBoxBase)this.getSkinnable()).getEffectiveNodeOrientation());
        Node node = this.getPopupContent();
        this.sizePopup();
        Point2D point2D = this.getPrefPopupPosition();
        this.popupNeedsReconfiguring = true;
        this.reconfigurePopup();
        popupControl.show(comboBoxBase.getScene().getWindow(), this.snapPositionX(point2D.getX()), this.snapPositionY(point2D.getY()));
        node.requestFocus();
        this.sizePopup();
    }

    private void sizePopup() {
        Node node = this.getPopupContent();
        if (node instanceof Region) {
            Region region = (Region)node;
            double d = this.snapSizeY(region.prefHeight(0.0));
            double d2 = this.snapSizeY(region.minHeight(0.0));
            double d3 = this.snapSizeY(region.maxHeight(0.0));
            double d4 = this.snapSizeY(Math.min(Math.max(d, d2), Math.max(d2, d3)));
            double d5 = this.snapSizeX(region.prefWidth(d4));
            double d6 = this.snapSizeX(region.minWidth(d4));
            double d7 = this.snapSizeX(region.maxWidth(d4));
            double d8 = this.snapSizeX(Math.min(Math.max(d5, d6), Math.max(d6, d7)));
            node.resize(d8, d4);
        } else {
            node.autosize();
        }
    }

    private void createPopup() {
        this.popup = new PopupControl(){
            {
                this.setSkin(new Skin<Skinnable>(){

                    @Override
                    public Skinnable getSkinnable() {
                        return ComboBoxPopupControl.this.getSkinnable();
                    }

                    @Override
                    public Node getNode() {
                        return ComboBoxPopupControl.this.getPopupContent();
                    }

                    @Override
                    public void dispose() {
                    }
                });
            }

            @Override
            public Styleable getStyleableParent() {
                return ComboBoxPopupControl.this.getSkinnable();
            }
        };
        this.popup.getStyleClass().add("combo-box-popup");
        this.popup.setConsumeAutoHidingEvents(false);
        this.popup.setAutoHide(true);
        this.popup.setAutoFix(true);
        this.popup.setHideOnEscape(true);
        this.popup.setOnAutoHide(event -> this.getBehavior().onAutoHide(this.popup));
        this.popup.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> this.getBehavior().onAutoHide(this.popup));
        this.popup.addEventHandler(WindowEvent.WINDOW_HIDDEN, windowEvent -> ((ComboBoxBase)this.getSkinnable()).notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE));
        InvalidationListener invalidationListener = observable -> {
            this.popupNeedsReconfiguring = true;
            this.reconfigurePopup();
        };
        ((ComboBoxBase)this.getSkinnable()).layoutXProperty().addListener(invalidationListener);
        ((ComboBoxBase)this.getSkinnable()).layoutYProperty().addListener(invalidationListener);
        ((ComboBoxBase)this.getSkinnable()).widthProperty().addListener(invalidationListener);
        ((ComboBoxBase)this.getSkinnable()).heightProperty().addListener(invalidationListener);
        ((ComboBoxBase)this.getSkinnable()).sceneProperty().addListener(observable -> {
            if (((ObservableValue)observable).getValue() == null) {
                this.hide();
            } else if (((ComboBoxBase)this.getSkinnable()).isShowing()) {
                this.show();
            }
        });
    }

    void reconfigurePopup() {
        double d;
        if (this.popup == null) {
            return;
        }
        boolean bl = this.popup.isShowing();
        if (!bl) {
            return;
        }
        if (!this.popupNeedsReconfiguring) {
            return;
        }
        this.popupNeedsReconfiguring = false;
        Point2D point2D = this.getPrefPopupPosition();
        Node node = this.getPopupContent();
        double d2 = node.prefWidth(-1.0);
        double d3 = node.prefHeight(-1.0);
        if (point2D.getX() > -1.0) {
            this.popup.setAnchorX(point2D.getX());
        }
        if (point2D.getY() > -1.0) {
            this.popup.setAnchorY(point2D.getY());
        }
        if (d2 > -1.0) {
            this.popup.setMinWidth(d2);
        }
        if (d3 > -1.0) {
            this.popup.setMinHeight(d3);
        }
        Bounds bounds = node.getLayoutBounds();
        double d4 = bounds.getWidth();
        double d5 = bounds.getHeight();
        double d6 = d4 < d2 ? d2 : d4;
        double d7 = d = d5 < d3 ? d3 : d5;
        if (d6 != d4 || d != d5) {
            node.resize(d6, d);
            if (node instanceof Region) {
                ((Region)node).setMinSize(d6, d);
                ((Region)node).setPrefSize(d6, d);
            }
        }
    }

    private void handleKeyEvent(KeyEvent keyEvent, boolean bl) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            if (keyEvent.isConsumed() || keyEvent.getEventType() != KeyEvent.KEY_RELEASED) {
                return;
            }
            this.setTextFromTextFieldIntoComboBoxValue();
            if (bl && this.comboBoxBase.getOnAction() != null) {
                keyEvent.consume();
            } else if (this.textField != null) {
                this.textField.fireEvent(keyEvent);
            }
        } else if (keyEvent.getCode() == KeyCode.F4) {
            if (keyEvent.getEventType() == KeyEvent.KEY_RELEASED) {
                if (this.comboBoxBase.isShowing()) {
                    this.comboBoxBase.hide();
                } else {
                    this.comboBoxBase.show();
                }
            }
            keyEvent.consume();
        } else if ((keyEvent.getCode() == KeyCode.F10 || keyEvent.getCode() == KeyCode.ESCAPE) && bl) {
            keyEvent.consume();
        }
    }
}

