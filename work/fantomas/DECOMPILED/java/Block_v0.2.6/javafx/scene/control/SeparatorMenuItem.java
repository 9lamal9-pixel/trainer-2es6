/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.geometry.Orientation;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Separator;

public class SeparatorMenuItem
extends CustomMenuItem {
    private static final String DEFAULT_STYLE_CLASS = "separator-menu-item";

    public SeparatorMenuItem() {
        super(new Separator(Orientation.HORIZONTAL), false);
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
    }
}

