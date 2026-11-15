/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.util.Utils;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Duration;

public class ScrollBarBehavior
extends BehaviorBase<ScrollBar> {
    private final InputMap<ScrollBar> inputMap = this.createInputMap();
    Timeline timeline;

    public ScrollBarBehavior(ScrollBar scrollBar) {
        super(scrollBar);
        this.addDefaultMapping(this.inputMap, new InputMap.KeyMapping(KeyCode.HOME, KeyEvent.KEY_RELEASED, keyEvent -> this.home()), new InputMap.KeyMapping(KeyCode.END, KeyEvent.KEY_RELEASED, keyEvent -> this.end()));
        InputMap<ScrollBar> inputMap = new InputMap<ScrollBar>(scrollBar);
        inputMap.setInterceptor(event -> scrollBar.getOrientation() != Orientation.HORIZONTAL);
        inputMap.getMappings().addAll(new InputMap.KeyMapping(KeyCode.LEFT, keyEvent -> this.rtl(scrollBar, this::incrementValue, this::decrementValue)), new InputMap.KeyMapping(KeyCode.KP_LEFT, keyEvent -> this.rtl(scrollBar, this::incrementValue, this::decrementValue)), new InputMap.KeyMapping(KeyCode.RIGHT, keyEvent -> this.rtl(scrollBar, this::decrementValue, this::incrementValue)), new InputMap.KeyMapping(KeyCode.KP_RIGHT, keyEvent -> this.rtl(scrollBar, this::decrementValue, this::incrementValue)));
        this.addDefaultChildMap(this.inputMap, inputMap);
        InputMap<ScrollBar> inputMap2 = new InputMap<ScrollBar>(scrollBar);
        inputMap2.setInterceptor(event -> scrollBar.getOrientation() != Orientation.VERTICAL);
        inputMap2.getMappings().addAll(new InputMap.KeyMapping(KeyCode.UP, keyEvent -> this.decrementValue()), new InputMap.KeyMapping(KeyCode.KP_UP, keyEvent -> this.decrementValue()), new InputMap.KeyMapping(KeyCode.DOWN, keyEvent -> this.incrementValue()), new InputMap.KeyMapping(KeyCode.KP_DOWN, keyEvent -> this.incrementValue()));
        this.addDefaultChildMap(this.inputMap, inputMap2);
    }

    @Override
    public InputMap<ScrollBar> getInputMap() {
        return this.inputMap;
    }

    private void home() {
        ((ScrollBar)this.getNode()).setValue(((ScrollBar)this.getNode()).getMin());
    }

    private void decrementValue() {
        ((ScrollBar)this.getNode()).adjustValue(0.0);
    }

    private void end() {
        ((ScrollBar)this.getNode()).setValue(((ScrollBar)this.getNode()).getMax());
    }

    private void incrementValue() {
        ((ScrollBar)this.getNode()).adjustValue(1.0);
    }

    public void trackPress(double d) {
        double d2;
        if (this.timeline != null) {
            return;
        }
        ScrollBar scrollBar = (ScrollBar)this.getNode();
        if (!scrollBar.isFocused() && scrollBar.isFocusTraversable()) {
            scrollBar.requestFocus();
        }
        boolean bl = (d2 = d) > (scrollBar.getValue() - scrollBar.getMin()) / (scrollBar.getMax() - scrollBar.getMin());
        this.timeline = new Timeline();
        this.timeline.setCycleCount(-1);
        EventHandler<ActionEvent> eventHandler = actionEvent -> {
            boolean bl2;
            boolean bl3 = bl2 = d2 > (scrollBar.getValue() - scrollBar.getMin()) / (scrollBar.getMax() - scrollBar.getMin());
            if (bl == bl2) {
                scrollBar.adjustValue(d2);
            } else {
                this.stopTimeline();
            }
        };
        KeyFrame keyFrame = new KeyFrame(Duration.millis(200.0), eventHandler, new KeyValue[0]);
        this.timeline.getKeyFrames().add(keyFrame);
        this.timeline.play();
        eventHandler.handle(null);
    }

    public void trackRelease() {
        this.stopTimeline();
    }

    public void decButtonPressed() {
        ScrollBar scrollBar = (ScrollBar)this.getNode();
        if (!scrollBar.isFocused() && scrollBar.isFocusTraversable()) {
            scrollBar.requestFocus();
        }
        this.stopTimeline();
        this.timeline = new Timeline();
        this.timeline.setCycleCount(-1);
        EventHandler<ActionEvent> eventHandler = actionEvent -> {
            if (scrollBar.getValue() > scrollBar.getMin()) {
                scrollBar.decrement();
            } else {
                this.stopTimeline();
            }
        };
        KeyFrame keyFrame = new KeyFrame(Duration.millis(200.0), eventHandler, new KeyValue[0]);
        this.timeline.getKeyFrames().add(keyFrame);
        this.timeline.play();
        eventHandler.handle(null);
    }

    public void decButtonReleased() {
        this.stopTimeline();
    }

    public void incButtonPressed() {
        ScrollBar scrollBar = (ScrollBar)this.getNode();
        if (!scrollBar.isFocused() && scrollBar.isFocusTraversable()) {
            scrollBar.requestFocus();
        }
        this.stopTimeline();
        this.timeline = new Timeline();
        this.timeline.setCycleCount(-1);
        EventHandler<ActionEvent> eventHandler = actionEvent -> {
            if (scrollBar.getValue() < scrollBar.getMax()) {
                scrollBar.increment();
            } else {
                this.stopTimeline();
            }
        };
        KeyFrame keyFrame = new KeyFrame(Duration.millis(200.0), eventHandler, new KeyValue[0]);
        this.timeline.getKeyFrames().add(keyFrame);
        this.timeline.play();
        eventHandler.handle(null);
    }

    public void incButtonReleased() {
        this.stopTimeline();
    }

    public void thumbDragged(double d) {
        double d2;
        ScrollBar scrollBar = (ScrollBar)this.getNode();
        this.stopTimeline();
        if (!scrollBar.isFocused() && scrollBar.isFocusTraversable()) {
            scrollBar.requestFocus();
        }
        if (!Double.isNaN(d2 = d * (scrollBar.getMax() - scrollBar.getMin()) + scrollBar.getMin())) {
            scrollBar.setValue(Utils.clamp(scrollBar.getMin(), d2, scrollBar.getMax()));
        }
    }

    private void stopTimeline() {
        if (this.timeline != null) {
            this.timeline.stop();
            this.timeline = null;
        }
    }
}

