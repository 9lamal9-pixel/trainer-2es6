/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;

public class RadioMenuItem
extends MenuItem
implements Toggle {
    private ObjectProperty<ToggleGroup> toggleGroup;
    private BooleanProperty selected;
    private static final String DEFAULT_STYLE_CLASS = "radio-menu-item";
    private static final String STYLE_CLASS_SELECTED = "selected";

    public RadioMenuItem() {
        this(null, null);
    }

    public RadioMenuItem(String string) {
        this(string, null);
    }

    public RadioMenuItem(String string, Node node) {
        super(string, node);
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    @Override
    public final void setToggleGroup(ToggleGroup toggleGroup) {
        this.toggleGroupProperty().set(toggleGroup);
    }

    @Override
    public final ToggleGroup getToggleGroup() {
        return this.toggleGroup == null ? null : (ToggleGroup)this.toggleGroup.get();
    }

    @Override
    public final ObjectProperty<ToggleGroup> toggleGroupProperty() {
        if (this.toggleGroup == null) {
            this.toggleGroup = new ObjectPropertyBase<ToggleGroup>(){
                private ToggleGroup old;

                @Override
                protected void invalidated() {
                    if (this.old != null) {
                        this.old.getToggles().remove(RadioMenuItem.this);
                    }
                    this.old = (ToggleGroup)this.get();
                    if (this.get() != null && !((ToggleGroup)this.get()).getToggles().contains(RadioMenuItem.this)) {
                        ((ToggleGroup)this.get()).getToggles().add(RadioMenuItem.this);
                    }
                }

                @Override
                public Object getBean() {
                    return RadioMenuItem.this;
                }

                @Override
                public String getName() {
                    return "toggleGroup";
                }
            };
        }
        return this.toggleGroup;
    }

    @Override
    public final void setSelected(boolean bl) {
        this.selectedProperty().set(bl);
    }

    @Override
    public final boolean isSelected() {
        return this.selected == null ? false : this.selected.get();
    }

    @Override
    public final BooleanProperty selectedProperty() {
        if (this.selected == null) {
            this.selected = new BooleanPropertyBase(){

                @Override
                protected void invalidated() {
                    if (RadioMenuItem.this.getToggleGroup() != null) {
                        if (this.get()) {
                            RadioMenuItem.this.getToggleGroup().selectToggle(RadioMenuItem.this);
                        } else if (RadioMenuItem.this.getToggleGroup().getSelectedToggle() == RadioMenuItem.this) {
                            RadioMenuItem.this.getToggleGroup().clearSelectedToggle();
                        }
                    }
                    if (RadioMenuItem.this.isSelected()) {
                        RadioMenuItem.this.getStyleClass().add(RadioMenuItem.STYLE_CLASS_SELECTED);
                    } else {
                        RadioMenuItem.this.getStyleClass().remove(RadioMenuItem.STYLE_CLASS_SELECTED);
                    }
                }

                @Override
                public Object getBean() {
                    return RadioMenuItem.this;
                }

                @Override
                public String getName() {
                    return RadioMenuItem.STYLE_CLASS_SELECTED;
                }
            };
        }
        return this.selected;
    }
}

