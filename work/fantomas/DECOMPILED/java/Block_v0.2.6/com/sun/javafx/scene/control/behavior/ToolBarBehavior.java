/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.KeyBinding;
import javafx.scene.Node;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;

public class ToolBarBehavior
extends BehaviorBase<ToolBar> {
    private final InputMap<ToolBar> toolBarInputMap = this.createInputMap();

    public ToolBarBehavior(ToolBar toolBar) {
        super(toolBar);
        this.addDefaultMapping(this.toolBarInputMap, new InputMap.KeyMapping(new KeyBinding(KeyCode.F5).ctrl(), keyEvent -> {
            if (!toolBar.getItems().isEmpty()) {
                ((Node)toolBar.getItems().get(0)).requestFocus();
            }
        }));
    }

    @Override
    public InputMap<ToolBar> getInputMap() {
        return this.toolBarInputMap;
    }
}

