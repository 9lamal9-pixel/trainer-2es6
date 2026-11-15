/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ColorPickerSkin;
import javafx.scene.paint.Color;

public class ColorPicker
extends ComboBoxBase<Color> {
    public static final String STYLE_CLASS_BUTTON = "button";
    public static final String STYLE_CLASS_SPLIT_BUTTON = "split-button";
    private ObservableList<Color> customColors = FXCollections.observableArrayList();
    private static final String DEFAULT_STYLE_CLASS = "color-picker";

    public final ObservableList<Color> getCustomColors() {
        return this.customColors;
    }

    public ColorPicker() {
        this(Color.WHITE);
    }

    public ColorPicker(Color color) {
        this.setValue(color);
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ColorPickerSkin(this);
    }
}

