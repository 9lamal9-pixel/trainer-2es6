/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.css.StyleableProperty;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Skin;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.skin.RadioButtonSkin;

public class RadioButton
extends ToggleButton {
    private static final String DEFAULT_STYLE_CLASS = "radio-button";

    public RadioButton() {
        this.initialize();
    }

    public RadioButton(String string) {
        this.setText(string);
        this.initialize();
    }

    private void initialize() {
        this.getStyleClass().setAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.setAccessibleRole(AccessibleRole.RADIO_BUTTON);
        ((StyleableProperty)((Object)this.alignmentProperty())).applyStyle(null, Pos.CENTER_LEFT);
    }

    @Override
    public void fire() {
        if (this.getToggleGroup() == null || !this.isSelected()) {
            super.fire();
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new RadioButtonSkin(this);
    }

    @Override
    protected Pos getInitialAlignment() {
        return Pos.CENTER_LEFT;
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case SELECTED: {
                return this.isSelected();
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }
}

