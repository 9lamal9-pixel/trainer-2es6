/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.MenuButtonBehaviorBase;
import com.sun.javafx.scene.control.inputmap.InputMap;
import javafx.scene.control.MenuButton;
import javafx.scene.input.KeyCode;

public class MenuButtonBehavior
extends MenuButtonBehaviorBase<MenuButton> {
    public MenuButtonBehavior(MenuButton menuButton) {
        super(menuButton);
        this.addDefaultMapping(new InputMap.KeyMapping(KeyCode.SPACE, keyEvent -> this.openAction()));
        this.addDefaultMapping(new InputMap.KeyMapping(KeyCode.ENTER, keyEvent -> this.openAction()));
    }
}

