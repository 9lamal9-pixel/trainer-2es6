/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.ComboBoxBaseBehavior;
import java.time.LocalDate;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PopupControl;

public class DatePickerBehavior
extends ComboBoxBaseBehavior<LocalDate> {
    public DatePickerBehavior(DatePicker datePicker) {
        super(datePicker);
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

