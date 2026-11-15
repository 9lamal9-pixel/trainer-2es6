/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.behavior.ComboBoxBaseBehavior;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.SkinBase;
import javafx.scene.control.skin.ComboBoxMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public abstract class ComboBoxBaseSkin<T>
extends SkinBase<ComboBoxBase<T>> {
    private Node displayNode;
    StackPane arrowButton;
    Region arrow;
    private ComboBoxMode mode = ComboBoxMode.COMBOBOX;
    private final EventHandler<MouseEvent> mouseEnteredEventHandler = mouseEvent -> this.getBehavior().mouseEntered((MouseEvent)mouseEvent);
    private final EventHandler<MouseEvent> mousePressedEventHandler = mouseEvent -> {
        this.getBehavior().mousePressed((MouseEvent)mouseEvent);
        mouseEvent.consume();
    };
    private final EventHandler<MouseEvent> mouseReleasedEventHandler = mouseEvent -> {
        this.getBehavior().mouseReleased((MouseEvent)mouseEvent);
        mouseEvent.consume();
    };
    private final EventHandler<MouseEvent> mouseExitedEventHandler = mouseEvent -> this.getBehavior().mouseExited((MouseEvent)mouseEvent);

    final ComboBoxMode getMode() {
        return this.mode;
    }

    final void setMode(ComboBoxMode comboBoxMode) {
        this.mode = comboBoxMode;
    }

    public ComboBoxBaseSkin(ComboBoxBase<T> comboBoxBase) {
        super(comboBoxBase);
        this.getChildren().clear();
        this.arrow = new Region();
        this.arrow.setFocusTraversable(false);
        this.arrow.getStyleClass().setAll((String[])new String[]{"arrow"});
        this.arrow.setId("arrow");
        this.arrow.setMaxWidth(Double.NEGATIVE_INFINITY);
        this.arrow.setMaxHeight(Double.NEGATIVE_INFINITY);
        this.arrow.setMouseTransparent(true);
        this.arrowButton = new StackPane();
        this.arrowButton.setFocusTraversable(false);
        this.arrowButton.setId("arrow-button");
        this.arrowButton.getStyleClass().setAll((String[])new String[]{"arrow-button"});
        this.arrowButton.getChildren().add(this.arrow);
        this.getChildren().add(this.arrowButton);
        ((ComboBoxBase)this.getSkinnable()).focusedProperty().addListener((observableValue, bl, bl2) -> {
            if (!bl2.booleanValue()) {
                this.focusLost();
            }
        });
        this.updateArrowButtonListeners();
        this.registerChangeListener(comboBoxBase.editableProperty(), observableValue -> {
            this.updateArrowButtonListeners();
            this.updateDisplayArea();
        });
        this.registerChangeListener(comboBoxBase.showingProperty(), observableValue -> {
            if (((ComboBoxBase)this.getSkinnable()).isShowing()) {
                this.show();
            } else {
                this.hide();
            }
        });
        this.registerChangeListener(comboBoxBase.valueProperty(), observableValue -> this.updateDisplayArea());
    }

    public abstract Node getDisplayNode();

    public abstract void show();

    public abstract void hide();

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        double d5;
        if (this.displayNode == null) {
            this.updateDisplayArea();
        }
        double d6 = this.snapSizeX(this.arrow.prefWidth(-1.0));
        double d7 = d5 = this.isButton() ? 0.0 : this.arrowButton.snappedLeftInset() + d6 + this.arrowButton.snappedRightInset();
        if (this.displayNode != null) {
            this.displayNode.resizeRelocate(d, d2, d3 - d5, d4);
        }
        this.arrowButton.setVisible(!this.isButton());
        if (!this.isButton()) {
            this.arrowButton.resize(d5, d4);
            this.positionInArea(this.arrowButton, d + d3 - d5, d2, d5, d4, 0.0, HPos.CENTER, VPos.CENTER);
        }
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        if (this.displayNode == null) {
            this.updateDisplayArea();
        }
        double d6 = this.snapSizeX(this.arrow.prefWidth(-1.0));
        double d7 = this.isButton() ? 0.0 : this.arrowButton.snappedLeftInset() + d6 + this.arrowButton.snappedRightInset();
        double d8 = this.displayNode == null ? 0.0 : this.displayNode.prefWidth(d);
        double d9 = d8 + d7;
        return d5 + d9 + d3;
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        double d6;
        if (this.displayNode == null) {
            this.updateDisplayArea();
        }
        if (this.displayNode == null) {
            double d7 = this.isButton() ? 0.0 : this.arrowButton.snappedTopInset() + this.arrow.prefHeight(-1.0) + this.arrowButton.snappedBottomInset();
            d6 = Math.max(21.0, d7);
        } else {
            d6 = this.displayNode.prefHeight(d);
        }
        return d2 + d6 + d4;
    }

    @Override
    protected double computeMaxWidth(double d, double d2, double d3, double d4, double d5) {
        return ((ComboBoxBase)this.getSkinnable()).prefWidth(d);
    }

    @Override
    protected double computeMaxHeight(double d, double d2, double d3, double d4, double d5) {
        return ((ComboBoxBase)this.getSkinnable()).prefHeight(d);
    }

    @Override
    protected double computeBaselineOffset(double d, double d2, double d3, double d4) {
        if (this.displayNode == null) {
            this.updateDisplayArea();
        }
        if (this.displayNode != null) {
            return this.displayNode.getLayoutBounds().getMinY() + this.displayNode.getLayoutY() + this.displayNode.getBaselineOffset();
        }
        return super.computeBaselineOffset(d, d2, d3, d4);
    }

    ComboBoxBaseBehavior getBehavior() {
        return null;
    }

    void focusLost() {
        ((ComboBoxBase)this.getSkinnable()).hide();
    }

    private boolean isButton() {
        return this.getMode() == ComboBoxMode.BUTTON;
    }

    private void updateArrowButtonListeners() {
        if (((ComboBoxBase)this.getSkinnable()).isEditable()) {
            this.arrowButton.addEventHandler(MouseEvent.MOUSE_ENTERED, this.mouseEnteredEventHandler);
            this.arrowButton.addEventHandler(MouseEvent.MOUSE_PRESSED, this.mousePressedEventHandler);
            this.arrowButton.addEventHandler(MouseEvent.MOUSE_RELEASED, this.mouseReleasedEventHandler);
            this.arrowButton.addEventHandler(MouseEvent.MOUSE_EXITED, this.mouseExitedEventHandler);
        } else {
            this.arrowButton.removeEventHandler(MouseEvent.MOUSE_ENTERED, this.mouseEnteredEventHandler);
            this.arrowButton.removeEventHandler(MouseEvent.MOUSE_PRESSED, this.mousePressedEventHandler);
            this.arrowButton.removeEventHandler(MouseEvent.MOUSE_RELEASED, this.mouseReleasedEventHandler);
            this.arrowButton.removeEventHandler(MouseEvent.MOUSE_EXITED, this.mouseExitedEventHandler);
        }
    }

    void updateDisplayArea() {
        ObservableList<Node> observableList = this.getChildren();
        Node node = this.displayNode;
        this.displayNode = this.getDisplayNode();
        if (node != null && node != this.displayNode) {
            observableList.remove(node);
        }
        if (this.displayNode != null && !observableList.contains(this.displayNode)) {
            observableList.add(this.displayNode);
            this.displayNode.applyCss();
        }
    }
}

