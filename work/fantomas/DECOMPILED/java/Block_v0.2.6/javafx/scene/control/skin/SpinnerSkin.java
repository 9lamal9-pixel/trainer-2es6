/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.ParentHelper;
import com.sun.javafx.scene.control.FakeFocusTextField;
import com.sun.javafx.scene.control.behavior.SpinnerBehavior;
import com.sun.javafx.scene.traversal.Algorithm;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import com.sun.javafx.scene.traversal.TraversalContext;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class SpinnerSkin<T>
extends SkinBase<Spinner<T>> {
    private TextField textField;
    private Region incrementArrow;
    private StackPane incrementArrowButton;
    private Region decrementArrow;
    private StackPane decrementArrowButton;
    private static final int ARROWS_ON_RIGHT_VERTICAL = 0;
    private static final int ARROWS_ON_LEFT_VERTICAL = 1;
    private static final int ARROWS_ON_RIGHT_HORIZONTAL = 2;
    private static final int ARROWS_ON_LEFT_HORIZONTAL = 3;
    private static final int SPLIT_ARROWS_VERTICAL = 4;
    private static final int SPLIT_ARROWS_HORIZONTAL = 5;
    private int layoutMode = 0;
    private final SpinnerBehavior behavior;
    private static PseudoClass CONTAINS_FOCUS_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("contains-focus");

    public SpinnerSkin(Spinner<T> spinner) {
        super(spinner);
        this.behavior = new SpinnerBehavior<T>(spinner);
        this.textField = spinner.getEditor();
        this.getChildren().add(this.textField);
        this.updateStyleClass();
        spinner.getStyleClass().addListener(change -> this.updateStyleClass());
        this.incrementArrow = new Region();
        this.incrementArrow.setFocusTraversable(false);
        this.incrementArrow.getStyleClass().setAll((String[])new String[]{"increment-arrow"});
        this.incrementArrow.setMaxWidth(Double.NEGATIVE_INFINITY);
        this.incrementArrow.setMaxHeight(Double.NEGATIVE_INFINITY);
        this.incrementArrow.setMouseTransparent(true);
        this.incrementArrowButton = new StackPane(){

            @Override
            public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
                switch (accessibleAction) {
                    case FIRE: {
                        ((Spinner)SpinnerSkin.this.getSkinnable()).increment();
                        break;
                    }
                    default: {
                        super.executeAccessibleAction(accessibleAction, objectArray);
                    }
                }
            }
        };
        this.incrementArrowButton.setAccessibleRole(AccessibleRole.INCREMENT_BUTTON);
        this.incrementArrowButton.setFocusTraversable(false);
        this.incrementArrowButton.getStyleClass().setAll((String[])new String[]{"increment-arrow-button"});
        this.incrementArrowButton.getChildren().add(this.incrementArrow);
        this.incrementArrowButton.setOnMousePressed(mouseEvent -> {
            ((Spinner)this.getSkinnable()).requestFocus();
            this.behavior.startSpinning(true);
        });
        this.incrementArrowButton.setOnMouseReleased(mouseEvent -> this.behavior.stopSpinning());
        this.decrementArrow = new Region();
        this.decrementArrow.setFocusTraversable(false);
        this.decrementArrow.getStyleClass().setAll((String[])new String[]{"decrement-arrow"});
        this.decrementArrow.setMaxWidth(Double.NEGATIVE_INFINITY);
        this.decrementArrow.setMaxHeight(Double.NEGATIVE_INFINITY);
        this.decrementArrow.setMouseTransparent(true);
        this.decrementArrowButton = new StackPane(){

            @Override
            public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
                switch (accessibleAction) {
                    case FIRE: {
                        ((Spinner)SpinnerSkin.this.getSkinnable()).decrement();
                        break;
                    }
                    default: {
                        super.executeAccessibleAction(accessibleAction, objectArray);
                    }
                }
            }
        };
        this.decrementArrowButton.setAccessibleRole(AccessibleRole.DECREMENT_BUTTON);
        this.decrementArrowButton.setFocusTraversable(false);
        this.decrementArrowButton.getStyleClass().setAll((String[])new String[]{"decrement-arrow-button"});
        this.decrementArrowButton.getChildren().add(this.decrementArrow);
        this.decrementArrowButton.setOnMousePressed(mouseEvent -> {
            ((Spinner)this.getSkinnable()).requestFocus();
            this.behavior.startSpinning(false);
        });
        this.decrementArrowButton.setOnMouseReleased(mouseEvent -> this.behavior.stopSpinning());
        this.getChildren().addAll((Node[])new Node[]{this.incrementArrowButton, this.decrementArrowButton});
        spinner.focusedProperty().addListener((observableValue, bl, bl2) -> ((FakeFocusTextField)this.textField).setFakeFocus((boolean)bl2));
        spinner.addEventFilter(KeyEvent.ANY, keyEvent -> {
            if (spinner.isEditable()) {
                if (keyEvent.getTarget().equals(this.textField)) {
                    return;
                }
                if (keyEvent.getCode() == KeyCode.ESCAPE) {
                    return;
                }
                this.textField.fireEvent(keyEvent.copyFor(this.textField, this.textField));
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    return;
                }
                keyEvent.consume();
            }
        });
        this.textField.addEventFilter(KeyEvent.ANY, keyEvent -> {
            if (!spinner.isEditable()) {
                spinner.fireEvent(keyEvent.copyFor(spinner, spinner));
                keyEvent.consume();
            }
        });
        this.textField.focusedProperty().addListener((observableValue, bl, bl2) -> {
            spinner.getProperties().put("FOCUSED", bl2);
            if (!bl2.booleanValue()) {
                this.pseudoClassStateChanged(CONTAINS_FOCUS_PSEUDOCLASS_STATE, false);
            } else {
                this.pseudoClassStateChanged(CONTAINS_FOCUS_PSEUDOCLASS_STATE, true);
            }
        });
        this.textField.focusTraversableProperty().bind(spinner.editableProperty());
        ParentHelper.setTraversalEngine(spinner, new ParentTraversalEngine(spinner, new Algorithm(){

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
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        double d5 = this.incrementArrowButton.snappedLeftInset() + this.snapSizeX(this.incrementArrow.prefWidth(-1.0)) + this.incrementArrowButton.snappedRightInset();
        double d6 = this.decrementArrowButton.snappedLeftInset() + this.snapSizeX(this.decrementArrow.prefWidth(-1.0)) + this.decrementArrowButton.snappedRightInset();
        double d7 = Math.max(d5, d6);
        if (this.layoutMode == 0 || this.layoutMode == 1) {
            double d8 = this.layoutMode == 0 ? d : d + d7;
            double d9 = this.layoutMode == 0 ? d + d3 - d7 : d;
            double d10 = Math.floor(d4 / 2.0);
            this.textField.resizeRelocate(d8, d2, d3 - d7, d4);
            this.incrementArrowButton.resize(d7, d10);
            this.positionInArea(this.incrementArrowButton, d9, d2, d7, d10, 0.0, HPos.CENTER, VPos.CENTER);
            this.decrementArrowButton.resize(d7, d10);
            this.positionInArea(this.decrementArrowButton, d9, d2 + d10, d7, d4 - d10, 0.0, HPos.CENTER, VPos.BOTTOM);
        } else if (this.layoutMode == 2 || this.layoutMode == 3) {
            double d11 = d5 + d6;
            double d12 = this.layoutMode == 2 ? d : d + d11;
            double d13 = this.layoutMode == 2 ? d + d3 - d11 : d;
            this.textField.resizeRelocate(d12, d2, d3 - d11, d4);
            this.decrementArrowButton.resize(d6, d4);
            this.positionInArea(this.decrementArrowButton, d13, d2, d6, d4, 0.0, HPos.CENTER, VPos.CENTER);
            this.incrementArrowButton.resize(d5, d4);
            this.positionInArea(this.incrementArrowButton, d13 + d6, d2, d5, d4, 0.0, HPos.CENTER, VPos.CENTER);
        } else if (this.layoutMode == 4) {
            double d14 = this.incrementArrowButton.snappedTopInset() + this.snapSizeY(this.incrementArrow.prefHeight(-1.0)) + this.incrementArrowButton.snappedBottomInset();
            double d15 = this.decrementArrowButton.snappedTopInset() + this.snapSizeY(this.decrementArrow.prefHeight(-1.0)) + this.decrementArrowButton.snappedBottomInset();
            double d16 = Math.max(d14, d15);
            this.incrementArrowButton.resize(d3, d16);
            this.positionInArea(this.incrementArrowButton, d, d2, d3, d16, 0.0, HPos.CENTER, VPos.CENTER);
            this.textField.resizeRelocate(d, d2 + d16, d3, d4 - 2.0 * d16);
            this.decrementArrowButton.resize(d3, d16);
            this.positionInArea(this.decrementArrowButton, d, d4 - d16, d3, d16, 0.0, HPos.CENTER, VPos.CENTER);
        } else if (this.layoutMode == 5) {
            this.decrementArrowButton.resize(d7, d4);
            this.positionInArea(this.decrementArrowButton, d, d2, d7, d4, 0.0, HPos.CENTER, VPos.CENTER);
            this.textField.resizeRelocate(d + d7, d2, d3 - 2.0 * d7, d4);
            this.incrementArrowButton.resize(d7, d4);
            this.positionInArea(this.incrementArrowButton, d3 - d7, d2, d7, d4, 0.0, HPos.CENTER, VPos.CENTER);
        }
    }

    @Override
    protected double computeMinWidth(double d, double d2, double d3, double d4, double d5) {
        return this.textField.minWidth(d);
    }

    @Override
    protected double computeMinHeight(double d, double d2, double d3, double d4, double d5) {
        return this.computePrefHeight(d, d2, d3, d4, d5);
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        double d6 = this.textField.prefWidth(d);
        return d5 + d6 + d3;
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        double d6 = this.textField.prefHeight(d);
        double d7 = this.layoutMode == 4 ? d2 + this.incrementArrowButton.prefHeight(d) + d6 + this.decrementArrowButton.prefHeight(d) + d4 : d2 + d6 + d4;
        return d7;
    }

    @Override
    protected double computeMaxWidth(double d, double d2, double d3, double d4, double d5) {
        return ((Spinner)this.getSkinnable()).prefWidth(d);
    }

    @Override
    protected double computeMaxHeight(double d, double d2, double d3, double d4, double d5) {
        return ((Spinner)this.getSkinnable()).prefHeight(d);
    }

    @Override
    protected double computeBaselineOffset(double d, double d2, double d3, double d4) {
        return this.textField.getLayoutBounds().getMinY() + this.textField.getLayoutY() + this.textField.getBaselineOffset();
    }

    private void updateStyleClass() {
        ObservableList<String> observableList = ((Spinner)this.getSkinnable()).getStyleClass();
        this.layoutMode = observableList.contains("arrows-on-left-vertical") ? 1 : (observableList.contains("arrows-on-left-horizontal") ? 3 : (observableList.contains("arrows-on-right-horizontal") ? 2 : (observableList.contains("split-arrows-vertical") ? 4 : (observableList.contains("split-arrows-horizontal") ? 5 : 0))));
    }
}

