/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.ComboBoxBaseBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.input.KeyCode;

public class ComboBoxListViewBehavior<T>
extends ComboBoxBaseBehavior<T> {
    public ComboBoxListViewBehavior(ComboBox<T> comboBox) {
        super(comboBox);
        InputMap<ComboBox<T>> inputMap = new InputMap<ComboBox<T>>(comboBox);
        inputMap.getMappings().addAll(new InputMap.KeyMapping(KeyCode.UP, keyEvent -> this.selectPrevious()), new InputMap.KeyMapping(KeyCode.DOWN, keyEvent -> this.selectNext()));
        this.addDefaultChildMap(this.getInputMap(), inputMap);
    }

    private ComboBox<T> getComboBox() {
        return (ComboBox)this.getNode();
    }

    private void selectPrevious() {
        SingleSelectionModel<T> singleSelectionModel = this.getComboBox().getSelectionModel();
        if (singleSelectionModel == null) {
            return;
        }
        ((SelectionModel)singleSelectionModel).selectPrevious();
    }

    private void selectNext() {
        SingleSelectionModel<T> singleSelectionModel = this.getComboBox().getSelectionModel();
        if (singleSelectionModel == null) {
            return;
        }
        ((SelectionModel)singleSelectionModel).selectNext();
    }
}

