/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.ButtonBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.skin.Utils;
import java.util.Collection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class ToggleButtonBehavior<C extends ToggleButton>
extends ButtonBehavior<C> {
    public ToggleButtonBehavior(C c) {
        super(c);
        ObservableList<InputMap.Mapping> observableList = FXCollections.observableArrayList(new InputMap.KeyMapping(KeyCode.RIGHT, keyEvent -> this.traverse((KeyEvent)keyEvent, "ToggleNext-Right")), new InputMap.KeyMapping(KeyCode.LEFT, keyEvent -> this.traverse((KeyEvent)keyEvent, "TogglePrevious-Left")), new InputMap.KeyMapping(KeyCode.DOWN, keyEvent -> this.traverse((KeyEvent)keyEvent, "ToggleNext-Down")), new InputMap.KeyMapping(KeyCode.UP, keyEvent -> this.traverse((KeyEvent)keyEvent, "TogglePrevious-Up")));
        for (InputMap.Mapping mapping : observableList) {
            mapping.setAutoConsume(false);
        }
        InputMap<C> inputMap = new InputMap<C>(c);
        inputMap.getMappings().addAll((Collection<InputMap.Mapping<?>>)observableList);
        this.addDefaultChildMap(this.getInputMap(), inputMap);
    }

    private int nextToggleIndex(ObservableList<Toggle> observableList, int n) {
        Toggle toggle;
        if (n < 0 || n >= observableList.size()) {
            return 0;
        }
        int n2 = (n + 1) % observableList.size();
        while (n2 != n && (toggle = (Toggle)observableList.get(n2)) instanceof Node && ((Node)((Object)toggle)).isDisabled()) {
            n2 = (n2 + 1) % observableList.size();
        }
        return n2;
    }

    private int previousToggleIndex(ObservableList<Toggle> observableList, int n) {
        Toggle toggle;
        if (n < 0 || n >= observableList.size()) {
            return observableList.size();
        }
        int n2 = Math.floorMod(n - 1, observableList.size());
        while (n2 != n && (toggle = (Toggle)observableList.get(n2)) instanceof Node && ((Node)((Object)toggle)).isDisabled()) {
            n2 = Math.floorMod(n2 - 1, observableList.size());
        }
        return n2;
    }

    private void traverse(KeyEvent keyEvent, String string) {
        ToggleButton toggleButton = (ToggleButton)this.getNode();
        ToggleGroup toggleGroup = toggleButton.getToggleGroup();
        if (toggleGroup == null) {
            keyEvent.consume();
            return;
        }
        ObservableList<Toggle> observableList = toggleGroup.getToggles();
        int n = observableList.indexOf(toggleButton);
        boolean bl = this.traversingToNext(string, toggleButton.getEffectiveNodeOrientation());
        if (!Utils.isTwoLevelFocus()) {
            if (bl) {
                int n2 = this.nextToggleIndex(observableList, n);
                if (n2 != n) {
                    Toggle toggle = (Toggle)observableList.get(n2);
                    toggleGroup.selectToggle(toggle);
                    ((Control)((Object)toggle)).requestFocus();
                    keyEvent.consume();
                }
            } else {
                int n3 = this.previousToggleIndex(observableList, n);
                if (n3 != n) {
                    Toggle toggle = (Toggle)observableList.get(n3);
                    toggleGroup.selectToggle(toggle);
                    ((Control)((Object)toggle)).requestFocus();
                    keyEvent.consume();
                }
            }
        }
    }

    private boolean traversingToNext(String string, NodeOrientation nodeOrientation) {
        boolean bl = nodeOrientation == NodeOrientation.RIGHT_TO_LEFT;
        switch (string) {
            case "ToggleNext-Right": {
                return !bl;
            }
            case "ToggleNext-Down": {
                return true;
            }
            case "TogglePrevious-Left": {
                return bl;
            }
            case "TogglePrevious-Up": {
                return false;
            }
        }
        throw new IllegalArgumentException("Not a toggle action");
    }
}

