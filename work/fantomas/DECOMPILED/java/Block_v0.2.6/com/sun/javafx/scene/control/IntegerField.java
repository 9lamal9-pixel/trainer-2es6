/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control;

import com.sun.javafx.scene.control.InputField;
import com.sun.javafx.scene.control.skin.IntegerFieldSkin;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Skin;

public class IntegerField
extends InputField {
    private IntegerProperty value = new SimpleIntegerProperty(this, "value");
    private IntegerProperty maxValue = new SimpleIntegerProperty(this, "maxValue", -1);

    public final int getValue() {
        return this.value.get();
    }

    public final void setValue(int n) {
        this.value.set(n);
    }

    public final IntegerProperty valueProperty() {
        return this.value;
    }

    public final int getMaxValue() {
        return this.maxValue.get();
    }

    public final void setMaxValue(int n) {
        this.maxValue.set(n);
    }

    public final IntegerProperty maxValueProperty() {
        return this.maxValue;
    }

    public IntegerField() {
        this(-1);
    }

    public IntegerField(int n) {
        this.getStyleClass().setAll((String[])new String[]{"integer-field"});
        this.setMaxValue(n);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new IntegerFieldSkin(this);
    }
}

