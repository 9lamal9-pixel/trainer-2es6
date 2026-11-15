/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;

public class CheckMenuItem
extends MenuItem {
    private BooleanProperty selected;
    private static final String DEFAULT_STYLE_CLASS = "check-menu-item";
    private static final String STYLE_CLASS_SELECTED = "selected";

    public CheckMenuItem() {
        this(null, null);
    }

    public CheckMenuItem(String string) {
        this(string, null);
    }

    public CheckMenuItem(String string, Node node) {
        super(string, node);
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    public final void setSelected(boolean bl) {
        this.selectedProperty().set(bl);
    }

    public final boolean isSelected() {
        return this.selected == null ? false : this.selected.get();
    }

    public final BooleanProperty selectedProperty() {
        if (this.selected == null) {
            this.selected = new BooleanPropertyBase(){

                @Override
                protected void invalidated() {
                    this.get();
                    if (CheckMenuItem.this.isSelected()) {
                        CheckMenuItem.this.getStyleClass().add(CheckMenuItem.STYLE_CLASS_SELECTED);
                    } else {
                        CheckMenuItem.this.getStyleClass().remove(CheckMenuItem.STYLE_CLASS_SELECTED);
                    }
                }

                @Override
                public Object getBean() {
                    return CheckMenuItem.this;
                }

                @Override
                public String getName() {
                    return CheckMenuItem.STYLE_CLASS_SELECTED;
                }
            };
        }
        return this.selected;
    }
}

