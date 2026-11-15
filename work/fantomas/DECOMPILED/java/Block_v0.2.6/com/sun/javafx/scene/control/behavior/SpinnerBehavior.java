/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.FocusTraversalInputMap;
import com.sun.javafx.scene.control.inputmap.InputMap;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

public class SpinnerBehavior<T>
extends BehaviorBase<Spinner<T>> {
    private static final double INITIAL_DURATION_MS = 750.0;
    private final InputMap<Spinner<T>> spinnerInputMap;
    private static final int STEP_AMOUNT = 1;
    private boolean isIncrementing = false;
    private Timeline timeline;
    final EventHandler<ActionEvent> spinningKeyFrameEventHandler = actionEvent -> {
        SpinnerValueFactory spinnerValueFactory = ((Spinner)this.getNode()).getValueFactory();
        if (spinnerValueFactory == null) {
            return;
        }
        if (this.isIncrementing) {
            this.increment(1);
        } else {
            this.decrement(1);
        }
    };

    public SpinnerBehavior(Spinner<T> spinner) {
        super(spinner);
        this.spinnerInputMap = this.createInputMap();
        this.addDefaultMapping(this.spinnerInputMap, new InputMap.KeyMapping(KeyCode.UP, KeyEvent.KEY_PRESSED, keyEvent -> {
            if (this.arrowsAreVertical()) {
                this.increment(1);
            } else {
                FocusTraversalInputMap.traverseUp(keyEvent);
            }
        }), new InputMap.KeyMapping(KeyCode.RIGHT, KeyEvent.KEY_PRESSED, keyEvent -> {
            if (!this.arrowsAreVertical()) {
                this.increment(1);
            } else {
                FocusTraversalInputMap.traverseRight(keyEvent);
            }
        }), new InputMap.KeyMapping(KeyCode.LEFT, KeyEvent.KEY_PRESSED, keyEvent -> {
            if (!this.arrowsAreVertical()) {
                this.decrement(1);
            } else {
                FocusTraversalInputMap.traverseLeft(keyEvent);
            }
        }), new InputMap.KeyMapping(KeyCode.DOWN, KeyEvent.KEY_PRESSED, keyEvent -> {
            if (this.arrowsAreVertical()) {
                this.decrement(1);
            } else {
                FocusTraversalInputMap.traverseDown(keyEvent);
            }
        }));
    }

    @Override
    public InputMap<Spinner<T>> getInputMap() {
        return this.spinnerInputMap;
    }

    public void increment(int n) {
        ((Spinner)this.getNode()).increment(n);
    }

    public void decrement(int n) {
        ((Spinner)this.getNode()).decrement(n);
    }

    public void startSpinning(boolean bl) {
        this.isIncrementing = bl;
        if (this.timeline != null) {
            this.timeline.stop();
        }
        this.timeline = new Timeline();
        this.timeline.setCycleCount(-1);
        this.timeline.setDelay(((Spinner)this.getNode()).getInitialDelay());
        KeyFrame keyFrame = new KeyFrame(Duration.ZERO, this.spinningKeyFrameEventHandler, new KeyValue[0]);
        KeyFrame keyFrame2 = new KeyFrame(((Spinner)this.getNode()).getRepeatDelay(), new KeyValue[0]);
        this.timeline.getKeyFrames().setAll((KeyFrame[])new KeyFrame[]{keyFrame, keyFrame2});
        this.timeline.playFromStart();
        this.spinningKeyFrameEventHandler.handle(null);
    }

    public void stopSpinning() {
        if (this.timeline != null) {
            this.timeline.stop();
        }
    }

    private boolean arrowsAreVertical() {
        ObservableList<String> observableList = ((Spinner)this.getNode()).getStyleClass();
        return !observableList.contains("arrows-on-left-horizontal") && !observableList.contains("arrows-on-right-horizontal") && !observableList.contains("split-arrows-horizontal");
    }
}

