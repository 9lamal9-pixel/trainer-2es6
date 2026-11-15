/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.FocusTraversalInputMap;
import com.sun.javafx.scene.control.inputmap.InputMap;
import javafx.scene.control.TitledPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

public class TitledPaneBehavior
extends BehaviorBase<TitledPane> {
    private final TitledPane titledPane;
    private final InputMap<TitledPane> inputMap;

    public TitledPaneBehavior(TitledPane titledPane) {
        super(titledPane);
        this.titledPane = titledPane;
        this.inputMap = this.createInputMap();
        this.addDefaultMapping(this.inputMap, FocusTraversalInputMap.getFocusTraversalMappings());
        this.addDefaultMapping(new InputMap.KeyMapping(KeyCode.SPACE, keyEvent -> {
            if (this.titledPane.isCollapsible() && this.titledPane.isFocused()) {
                this.titledPane.setExpanded(!this.titledPane.isExpanded());
                this.titledPane.requestFocus();
            }
        }), new InputMap.MouseMapping(MouseEvent.MOUSE_PRESSED, this::mousePressed));
    }

    @Override
    public InputMap<TitledPane> getInputMap() {
        return this.inputMap;
    }

    public void mousePressed(MouseEvent mouseEvent) {
        ((TitledPane)this.getNode()).requestFocus();
    }

    public void expand() {
        this.titledPane.setExpanded(true);
    }

    public void collapse() {
        this.titledPane.setExpanded(false);
    }

    public void toggle() {
        this.titledPane.setExpanded(!this.titledPane.isExpanded());
    }
}

