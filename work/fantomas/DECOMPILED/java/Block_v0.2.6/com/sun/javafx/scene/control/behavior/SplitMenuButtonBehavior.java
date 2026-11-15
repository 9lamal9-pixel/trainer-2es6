/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.MenuButtonBehaviorBase;
import com.sun.javafx.scene.control.inputmap.InputMap;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class SplitMenuButtonBehavior
extends MenuButtonBehaviorBase<SplitMenuButton> {
    public SplitMenuButtonBehavior(SplitMenuButton splitMenuButton) {
        super(splitMenuButton);
        this.addDefaultMapping(new InputMap.KeyMapping(KeyCode.SPACE, KeyEvent.KEY_PRESSED, this::keyPressed));
        this.addDefaultMapping(new InputMap.KeyMapping(KeyCode.SPACE, KeyEvent.KEY_RELEASED, this::keyReleased));
        this.addDefaultMapping(new InputMap.KeyMapping(KeyCode.ENTER, KeyEvent.KEY_PRESSED, this::keyPressed));
        this.addDefaultMapping(new InputMap.KeyMapping(KeyCode.ENTER, KeyEvent.KEY_RELEASED, this::keyReleased));
    }
}

