/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.ContextMenuContent;
import com.sun.javafx.scene.control.behavior.MenuButtonBehavior;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.MenuButton;
import javafx.scene.control.skin.MenuButtonSkinBase;

public class MenuButtonSkin
extends MenuButtonSkinBase<MenuButton> {
    static final String AUTOHIDE = "autoHide";
    private final MenuButtonBehavior behavior;

    public MenuButtonSkin(MenuButton menuButton) {
        super(menuButton);
        this.behavior = new MenuButtonBehavior(menuButton);
        this.popup.setOnAutoHide(event -> {
            MenuButton menuButton = (MenuButton)this.getSkinnable();
            if (!menuButton.getProperties().containsKey(AUTOHIDE)) {
                menuButton.getProperties().put(AUTOHIDE, Boolean.TRUE);
            }
        });
        this.popup.setOnShown(windowEvent -> {
            if (this.requestFocusOnFirstMenuItem) {
                this.requestFocusOnFirstMenuItem();
                this.requestFocusOnFirstMenuItem = false;
            } else {
                ContextMenuContent contextMenuContent = (ContextMenuContent)this.popup.getSkin().getNode();
                if (contextMenuContent != null) {
                    contextMenuContent.requestFocus();
                }
            }
        });
        if (menuButton.getOnAction() == null) {
            menuButton.setOnAction(actionEvent -> menuButton.show());
        }
        this.label.setLabelFor(menuButton);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }

    MenuButtonBehavior getBehavior() {
        return this.behavior;
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case MNEMONIC: {
                return this.label.queryAccessibleAttribute(AccessibleAttribute.MNEMONIC, new Object[0]);
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }
}

