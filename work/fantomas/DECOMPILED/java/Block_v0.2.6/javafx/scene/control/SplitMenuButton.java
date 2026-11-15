/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.event.ActionEvent;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.SplitMenuButtonSkin;

public class SplitMenuButton
extends MenuButton {
    private static final String DEFAULT_STYLE_CLASS = "split-menu-button";

    public SplitMenuButton() {
        this((MenuItem[])null);
    }

    public SplitMenuButton(MenuItem ... menuItemArray) {
        if (menuItemArray != null) {
            this.getItems().addAll((MenuItem[])menuItemArray);
        }
        this.getStyleClass().setAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.setAccessibleRole(AccessibleRole.SPLIT_MENU_BUTTON);
        this.setMnemonicParsing(true);
    }

    @Override
    public void fire() {
        if (!this.isDisabled()) {
            this.fireEvent(new ActionEvent());
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SplitMenuButtonSkin(this);
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case EXPANDED: {
                return this.isShowing();
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    @Override
    public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
        switch (accessibleAction) {
            case FIRE: {
                this.fire();
                break;
            }
            case EXPAND: {
                this.show();
                break;
            }
            case COLLAPSE: {
                this.hide();
                break;
            }
            default: {
                super.executeAccessibleAction(accessibleAction, new Object[0]);
            }
        }
    }
}

