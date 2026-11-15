/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.TwoLevelFocusBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.skin.Utils;
import javafx.geometry.Orientation;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class SliderBehavior
extends BehaviorBase<Slider> {
    private final InputMap<Slider> sliderInputMap = this.createInputMap();
    private TwoLevelFocusBehavior tlFocus;

    public SliderBehavior(Slider slider) {
        super(slider);
        this.addDefaultMapping(this.sliderInputMap, new InputMap.KeyMapping(KeyCode.HOME, KeyEvent.KEY_RELEASED, keyEvent -> this.home()), new InputMap.KeyMapping(KeyCode.END, KeyEvent.KEY_RELEASED, keyEvent -> this.end()));
        InputMap<Slider> inputMap = new InputMap<Slider>(slider);
        inputMap.setInterceptor(event -> slider.getOrientation() != Orientation.HORIZONTAL);
        inputMap.getMappings().addAll(new InputMap.KeyMapping(KeyCode.LEFT, keyEvent -> this.rtl(slider, this::incrementValue, this::decrementValue)), new InputMap.KeyMapping(KeyCode.KP_LEFT, keyEvent -> this.rtl(slider, this::incrementValue, this::decrementValue)), new InputMap.KeyMapping(KeyCode.RIGHT, keyEvent -> this.rtl(slider, this::decrementValue, this::incrementValue)), new InputMap.KeyMapping(KeyCode.KP_RIGHT, keyEvent -> this.rtl(slider, this::decrementValue, this::incrementValue)));
        this.addDefaultChildMap(this.sliderInputMap, inputMap);
        InputMap<Slider> inputMap2 = new InputMap<Slider>(slider);
        inputMap2.setInterceptor(event -> slider.getOrientation() != Orientation.VERTICAL);
        inputMap2.getMappings().addAll(new InputMap.KeyMapping(KeyCode.DOWN, keyEvent -> this.decrementValue()), new InputMap.KeyMapping(KeyCode.KP_DOWN, keyEvent -> this.decrementValue()), new InputMap.KeyMapping(KeyCode.UP, keyEvent -> this.incrementValue()), new InputMap.KeyMapping(KeyCode.KP_UP, keyEvent -> this.incrementValue()));
        this.addDefaultChildMap(this.sliderInputMap, inputMap2);
        if (Utils.isTwoLevelFocus()) {
            this.tlFocus = new TwoLevelFocusBehavior(slider);
        }
    }

    @Override
    public void dispose() {
        if (this.tlFocus != null) {
            this.tlFocus.dispose();
        }
        super.dispose();
    }

    @Override
    public InputMap<Slider> getInputMap() {
        return this.sliderInputMap;
    }

    public void trackPress(MouseEvent mouseEvent, double d) {
        Slider slider = (Slider)this.getNode();
        if (!slider.isFocused()) {
            slider.requestFocus();
        }
        if (slider.getOrientation().equals((Object)Orientation.HORIZONTAL)) {
            slider.adjustValue(d * (slider.getMax() - slider.getMin()) + slider.getMin());
        } else {
            slider.adjustValue((1.0 - d) * (slider.getMax() - slider.getMin()) + slider.getMin());
        }
    }

    public void thumbPressed(MouseEvent mouseEvent, double d) {
        Slider slider = (Slider)this.getNode();
        if (!slider.isFocused()) {
            slider.requestFocus();
        }
        slider.setValueChanging(true);
    }

    public void thumbDragged(MouseEvent mouseEvent, double d) {
        Slider slider = (Slider)this.getNode();
        slider.setValue(com.sun.javafx.util.Utils.clamp(slider.getMin(), d * (slider.getMax() - slider.getMin()) + slider.getMin(), slider.getMax()));
    }

    public void thumbReleased(MouseEvent mouseEvent) {
        Slider slider = (Slider)this.getNode();
        slider.setValueChanging(false);
        slider.adjustValue(slider.getValue());
    }

    void home() {
        Slider slider = (Slider)this.getNode();
        slider.adjustValue(slider.getMin());
    }

    void decrementValue() {
        Slider slider = (Slider)this.getNode();
        if (slider.isSnapToTicks()) {
            slider.adjustValue(slider.getValue() - this.computeIncrement());
        } else {
            slider.decrement();
        }
    }

    void end() {
        Slider slider = (Slider)this.getNode();
        slider.adjustValue(slider.getMax());
    }

    void incrementValue() {
        Slider slider = (Slider)this.getNode();
        if (slider.isSnapToTicks()) {
            slider.adjustValue(slider.getValue() + this.computeIncrement());
        } else {
            slider.increment();
        }
    }

    double computeIncrement() {
        Slider slider = (Slider)this.getNode();
        double d = 0.0;
        d = slider.getMinorTickCount() != 0 ? slider.getMajorTickUnit() / (double)(Math.max(slider.getMinorTickCount(), 0) + 1) : slider.getMajorTickUnit();
        if (slider.getBlockIncrement() > 0.0 && slider.getBlockIncrement() < d) {
            return d;
        }
        return slider.getBlockIncrement();
    }
}

