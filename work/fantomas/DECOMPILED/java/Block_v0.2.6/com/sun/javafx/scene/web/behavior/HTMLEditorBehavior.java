/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.web.behavior;

import com.sun.javafx.scene.ParentHelper;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.FocusTraversalInputMap;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.KeyBinding;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.HTMLEditorSkin;

public class HTMLEditorBehavior
extends BehaviorBase<HTMLEditor> {
    private final InputMap<HTMLEditor> inputMap = this.createInputMap();

    public HTMLEditorBehavior(HTMLEditor hTMLEditor) {
        super(hTMLEditor);
        this.addDefaultMapping(this.inputMap, new InputMap.KeyMapping(new KeyBinding(KeyCode.B).shortcut(), keyEvent -> this.keyboardShortcuts(HTMLEditorSkin.Command.BOLD)), new InputMap.KeyMapping(new KeyBinding(KeyCode.I).shortcut(), keyEvent -> this.keyboardShortcuts(HTMLEditorSkin.Command.ITALIC)), new InputMap.KeyMapping(new KeyBinding(KeyCode.U).shortcut(), keyEvent -> this.keyboardShortcuts(HTMLEditorSkin.Command.UNDERLINE)), new InputMap.KeyMapping(new KeyBinding(KeyCode.F12), keyEvent -> ParentHelper.getTraversalEngine((Parent)this.getNode()).selectFirst().requestFocus()), new InputMap.KeyMapping(new KeyBinding(KeyCode.TAB).ctrl(), FocusTraversalInputMap::traverseNext), new InputMap.KeyMapping(new KeyBinding(KeyCode.TAB).ctrl().shift(), FocusTraversalInputMap::traversePrevious));
    }

    @Override
    public InputMap<HTMLEditor> getInputMap() {
        return this.inputMap;
    }

    private void keyboardShortcuts(HTMLEditorSkin.Command command) {
        HTMLEditor hTMLEditor = (HTMLEditor)this.getNode();
        HTMLEditorSkin hTMLEditorSkin = (HTMLEditorSkin)hTMLEditor.getSkin();
        hTMLEditorSkin.performCommand(command);
    }
}

