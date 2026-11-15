/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.CheckBoxSkin;

public class CheckBox
extends ButtonBase {
    private BooleanProperty indeterminate;
    private BooleanProperty selected;
    private BooleanProperty allowIndeterminate;
    private static final String DEFAULT_STYLE_CLASS = "check-box";
    private static final PseudoClass PSEUDO_CLASS_DETERMINATE = PseudoClass.getPseudoClass("determinate");
    private static final PseudoClass PSEUDO_CLASS_INDETERMINATE = PseudoClass.getPseudoClass("indeterminate");
    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected");

    public CheckBox() {
        this.initialize();
    }

    public CheckBox(String string) {
        this.setText(string);
        this.initialize();
    }

    private void initialize() {
        this.getStyleClass().setAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.setAccessibleRole(AccessibleRole.CHECK_BOX);
        this.setAlignment(Pos.CENTER_LEFT);
        this.setMnemonicParsing(true);
        this.pseudoClassStateChanged(PSEUDO_CLASS_DETERMINATE, true);
    }

    public final void setIndeterminate(boolean bl) {
        this.indeterminateProperty().set(bl);
    }

    public final boolean isIndeterminate() {
        return this.indeterminate == null ? false : this.indeterminate.get();
    }

    public final BooleanProperty indeterminateProperty() {
        if (this.indeterminate == null) {
            this.indeterminate = new BooleanPropertyBase(false){

                @Override
                protected void invalidated() {
                    boolean bl = this.get();
                    CheckBox.this.pseudoClassStateChanged(PSEUDO_CLASS_DETERMINATE, !bl);
                    CheckBox.this.pseudoClassStateChanged(PSEUDO_CLASS_INDETERMINATE, bl);
                    CheckBox.this.notifyAccessibleAttributeChanged(AccessibleAttribute.INDETERMINATE);
                }

                @Override
                public Object getBean() {
                    return CheckBox.this;
                }

                @Override
                public String getName() {
                    return "indeterminate";
                }
            };
        }
        return this.indeterminate;
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
                    Boolean bl = this.get();
                    CheckBox.this.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, bl);
                    CheckBox.this.notifyAccessibleAttributeChanged(AccessibleAttribute.SELECTED);
                }

                @Override
                public Object getBean() {
                    return CheckBox.this;
                }

                @Override
                public String getName() {
                    return "selected";
                }
            };
        }
        return this.selected;
    }

    public final void setAllowIndeterminate(boolean bl) {
        this.allowIndeterminateProperty().set(bl);
    }

    public final boolean isAllowIndeterminate() {
        return this.allowIndeterminate == null ? false : this.allowIndeterminate.get();
    }

    public final BooleanProperty allowIndeterminateProperty() {
        if (this.allowIndeterminate == null) {
            this.allowIndeterminate = new SimpleBooleanProperty(this, "allowIndeterminate");
        }
        return this.allowIndeterminate;
    }

    @Override
    public void fire() {
        if (!this.isDisabled()) {
            if (this.isAllowIndeterminate()) {
                if (!this.isSelected() && !this.isIndeterminate()) {
                    this.setIndeterminate(true);
                } else if (this.isSelected() && !this.isIndeterminate()) {
                    this.setSelected(false);
                } else if (this.isIndeterminate()) {
                    this.setSelected(true);
                    this.setIndeterminate(false);
                }
            } else {
                this.setSelected(!this.isSelected());
                this.setIndeterminate(false);
            }
            this.fireEvent(new ActionEvent());
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CheckBoxSkin(this);
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case SELECTED: {
                return this.isSelected();
            }
            case INDETERMINATE: {
                return this.isIndeterminate();
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }
}

