/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.css.StyleableProperty;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ProgressBarSkin;

public class ProgressBar
extends ProgressIndicator {
    private static final String DEFAULT_STYLE_CLASS = "progress-bar";

    public ProgressBar() {
        this(-1.0);
    }

    public ProgressBar(double d) {
        ((StyleableProperty)((Object)this.focusTraversableProperty())).applyStyle(null, Boolean.FALSE);
        this.setProgress(d);
        this.getStyleClass().setAll((String[])new String[]{DEFAULT_STYLE_CLASS});
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ProgressBarSkin(this);
    }

    @Override
    protected Boolean getInitialFocusTraversable() {
        return Boolean.FALSE;
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case ORIENTATION: {
                return Orientation.HORIZONTAL;
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }
}

