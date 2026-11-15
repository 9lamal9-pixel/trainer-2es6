/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.TwoLevelFocusComboBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.skin.Utils;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class ChoiceBoxBehavior<T>
extends BehaviorBase<ChoiceBox<T>> {
    private final InputMap<ChoiceBox<T>> choiceBoxInputMap = this.createInputMap();
    private TwoLevelFocusComboBehavior tlFocus;

    public ChoiceBoxBehavior(ChoiceBox<T> choiceBox) {
        super(choiceBox);
        this.addDefaultMapping(this.choiceBoxInputMap, new InputMap.KeyMapping(KeyCode.SPACE, KeyEvent.KEY_PRESSED, this::keyPressed), new InputMap.KeyMapping(KeyCode.SPACE, KeyEvent.KEY_RELEASED, this::keyReleased), new InputMap.KeyMapping(KeyCode.ESCAPE, KeyEvent.KEY_RELEASED, keyEvent -> this.cancel()), new InputMap.KeyMapping(KeyCode.DOWN, KeyEvent.KEY_RELEASED, keyEvent -> this.showPopup()), new InputMap.KeyMapping(KeyCode.CANCEL, KeyEvent.KEY_RELEASED, keyEvent -> this.cancel()), new InputMap.MouseMapping(MouseEvent.MOUSE_PRESSED, this::mousePressed), new InputMap.MouseMapping(MouseEvent.MOUSE_RELEASED, this::mouseReleased));
        InputMap<ChoiceBox<T>> inputMap = new InputMap<ChoiceBox<T>>(choiceBox);
        inputMap.setInterceptor(event -> !Utils.isTwoLevelFocus());
        inputMap.getMappings().addAll(new InputMap.KeyMapping(KeyCode.ENTER, KeyEvent.KEY_PRESSED, this::keyPressed), new InputMap.KeyMapping(KeyCode.ENTER, KeyEvent.KEY_RELEASED, this::keyReleased));
        this.addDefaultChildMap(this.choiceBoxInputMap, inputMap);
        if (Utils.isTwoLevelFocus()) {
            this.tlFocus = new TwoLevelFocusComboBehavior(choiceBox);
        }
    }

    @Override
    public InputMap<ChoiceBox<T>> getInputMap() {
        return this.choiceBoxInputMap;
    }

    @Override
    public void dispose() {
        if (this.tlFocus != null) {
            this.tlFocus.dispose();
        }
        super.dispose();
    }

    public void select(int n) {
        SingleSelectionModel singleSelectionModel = ((ChoiceBox)this.getNode()).getSelectionModel();
        if (singleSelectionModel == null) {
            return;
        }
        ((SelectionModel)singleSelectionModel).select(n);
    }

    public void close() {
        ((ChoiceBox)this.getNode()).hide();
    }

    public void showPopup() {
        ((ChoiceBox)this.getNode()).show();
    }

    public void mousePressed(MouseEvent mouseEvent) {
        ChoiceBox choiceBox = (ChoiceBox)this.getNode();
        if (choiceBox.isFocusTraversable()) {
            choiceBox.requestFocus();
        }
    }

    public void mouseReleased(MouseEvent mouseEvent) {
        ChoiceBox choiceBox = (ChoiceBox)this.getNode();
        if (choiceBox.isShowing() || !choiceBox.contains(mouseEvent.getX(), mouseEvent.getY())) {
            choiceBox.hide();
        } else if (mouseEvent.getButton() == MouseButton.PRIMARY) {
            choiceBox.show();
        }
    }

    private void keyPressed(KeyEvent keyEvent) {
        ChoiceBox choiceBox = (ChoiceBox)this.getNode();
        if (!choiceBox.isShowing()) {
            choiceBox.show();
        }
    }

    private void keyReleased(KeyEvent keyEvent) {
    }

    public void cancel() {
        ChoiceBox choiceBox = (ChoiceBox)this.getNode();
        choiceBox.hide();
    }
}

