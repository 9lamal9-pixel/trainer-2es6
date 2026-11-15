/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ButtonSkin;

public class Button
extends ButtonBase {
    private BooleanProperty defaultButton;
    private BooleanProperty cancelButton;
    private static final String DEFAULT_STYLE_CLASS = "button";
    private static final PseudoClass PSEUDO_CLASS_DEFAULT = PseudoClass.getPseudoClass("default");
    private static final PseudoClass PSEUDO_CLASS_CANCEL = PseudoClass.getPseudoClass("cancel");

    public Button() {
        this.initialize();
    }

    public Button(String string) {
        super(string);
        this.initialize();
    }

    public Button(String string, Node node) {
        super(string, node);
        this.initialize();
    }

    private void initialize() {
        this.getStyleClass().setAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.setAccessibleRole(AccessibleRole.BUTTON);
        this.setMnemonicParsing(true);
    }

    public final void setDefaultButton(boolean bl) {
        this.defaultButtonProperty().set(bl);
    }

    public final boolean isDefaultButton() {
        return this.defaultButton == null ? false : this.defaultButton.get();
    }

    public final BooleanProperty defaultButtonProperty() {
        if (this.defaultButton == null) {
            this.defaultButton = new BooleanPropertyBase(false){

                @Override
                protected void invalidated() {
                    Button.this.pseudoClassStateChanged(PSEUDO_CLASS_DEFAULT, this.get());
                }

                @Override
                public Object getBean() {
                    return Button.this;
                }

                @Override
                public String getName() {
                    return "defaultButton";
                }
            };
        }
        return this.defaultButton;
    }

    public final void setCancelButton(boolean bl) {
        this.cancelButtonProperty().set(bl);
    }

    public final boolean isCancelButton() {
        return this.cancelButton == null ? false : this.cancelButton.get();
    }

    public final BooleanProperty cancelButtonProperty() {
        if (this.cancelButton == null) {
            this.cancelButton = new BooleanPropertyBase(false){

                @Override
                protected void invalidated() {
                    Button.this.pseudoClassStateChanged(PSEUDO_CLASS_CANCEL, this.get());
                }

                @Override
                public Object getBean() {
                    return Button.this;
                }

                @Override
                public String getName() {
                    return "cancelButton";
                }
            };
        }
        return this.cancelButton;
    }

    @Override
    public void fire() {
        if (!this.isDisabled()) {
            this.fireEvent(new ActionEvent());
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ButtonSkin(this);
    }
}

