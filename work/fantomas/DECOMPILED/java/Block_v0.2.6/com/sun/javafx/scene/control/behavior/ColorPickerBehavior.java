/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.ComboBoxBaseBehavior;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.PopupControl;
import javafx.scene.paint.Color;

public class ColorPickerBehavior
extends ComboBoxBaseBehavior<Color> {
    public ColorPickerBehavior(ColorPicker colorPicker) {
        super(colorPicker);
    }

    @Override
    public void onAutoHide(PopupControl popupControl) {
        if (!popupControl.isShowing() && ((ComboBoxBase)this.getNode()).isShowing()) {
            ((ComboBoxBase)this.getNode()).hide();
        }
        if (!((ComboBoxBase)this.getNode()).isShowing()) {
            super.onAutoHide(popupControl);
        }
    }
}

