/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.TwoLevelFocusComboBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.KeyBinding;
import com.sun.javafx.scene.control.skin.Utils;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.DatePicker;
import javafx.scene.control.PopupControl;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class ComboBoxBaseBehavior<T>
extends BehaviorBase<ComboBoxBase<T>> {
    private final InputMap<ComboBoxBase<T>> inputMap = this.createInputMap();
    private TwoLevelFocusComboBehavior tlFocus;
    private boolean keyDown;
    private boolean showPopupOnMouseRelease = true;
    private boolean mouseInsideButton = false;

    public ComboBoxBaseBehavior(ComboBoxBase<T> comboBoxBase) {
        super(comboBoxBase);
        EventHandler<KeyEvent> eventHandler = keyEvent -> {
            this.showPopupOnMouseRelease = true;
            if (((ComboBoxBase)this.getNode()).isShowing()) {
                this.hide();
            } else {
                this.show();
            }
        };
        InputMap.Mapping[] mappingArray = new InputMap.Mapping[13];
        mappingArray[0] = new InputMap.KeyMapping(KeyCode.F4, KeyEvent.KEY_RELEASED, eventHandler);
        mappingArray[1] = new InputMap.KeyMapping(new KeyBinding(KeyCode.UP).alt(), eventHandler);
        mappingArray[2] = new InputMap.KeyMapping(new KeyBinding(KeyCode.DOWN).alt(), eventHandler);
        mappingArray[3] = new InputMap.KeyMapping(KeyCode.SPACE, KeyEvent.KEY_PRESSED, this::keyPressed);
        mappingArray[4] = new InputMap.KeyMapping(KeyCode.SPACE, KeyEvent.KEY_RELEASED, this::keyReleased);
        InputMap.KeyMapping keyMapping = new InputMap.KeyMapping(KeyCode.ENTER, KeyEvent.KEY_PRESSED, this::keyPressed);
        mappingArray[5] = keyMapping;
        InputMap.KeyMapping keyMapping2 = new InputMap.KeyMapping(KeyCode.ENTER, KeyEvent.KEY_RELEASED, this::keyReleased);
        mappingArray[6] = keyMapping2;
        mappingArray[7] = new InputMap.KeyMapping(KeyCode.ESCAPE, KeyEvent.KEY_PRESSED, this::cancelEdit);
        mappingArray[8] = new InputMap.KeyMapping(KeyCode.F10, KeyEvent.KEY_PRESSED, this::forwardToParent);
        mappingArray[9] = new InputMap.MouseMapping(MouseEvent.MOUSE_PRESSED, this::mousePressed);
        mappingArray[10] = new InputMap.MouseMapping(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
        mappingArray[11] = new InputMap.MouseMapping(MouseEvent.MOUSE_ENTERED, this::mouseEntered);
        mappingArray[12] = new InputMap.MouseMapping(MouseEvent.MOUSE_EXITED, this::mouseExited);
        this.addDefaultMapping(this.inputMap, mappingArray);
        keyMapping.setAutoConsume(false);
        keyMapping2.setAutoConsume(false);
        comboBoxBase.focusedProperty().addListener(this::focusChanged);
        if (Utils.isTwoLevelFocus()) {
            this.tlFocus = new TwoLevelFocusComboBehavior(comboBoxBase);
        }
    }

    @Override
    public void dispose() {
        if (this.tlFocus != null) {
            this.tlFocus.dispose();
        }
        ((ComboBoxBase)this.getNode()).focusedProperty().removeListener(this::focusChanged);
        super.dispose();
    }

    @Override
    public InputMap<ComboBoxBase<T>> getInputMap() {
        return this.inputMap;
    }

    protected void focusChanged(Observable observable) {
        ComboBoxBase comboBoxBase = (ComboBoxBase)this.getNode();
        if (this.keyDown && !comboBoxBase.isFocused()) {
            this.keyDown = false;
            comboBoxBase.disarm();
        }
    }

    private void keyPressed(KeyEvent keyEvent) {
        this.showPopupOnMouseRelease = true;
        if (Utils.isTwoLevelFocus()) {
            this.show();
            if (this.tlFocus != null) {
                this.tlFocus.setExternalFocus(false);
            }
        } else if (!((ComboBoxBase)this.getNode()).isPressed() && !((ComboBoxBase)this.getNode()).isArmed()) {
            this.keyDown = true;
            ((ComboBoxBase)this.getNode()).arm();
        }
    }

    private void keyReleased(KeyEvent keyEvent) {
        this.showPopupOnMouseRelease = true;
        if (!Utils.isTwoLevelFocus() && this.keyDown) {
            this.keyDown = false;
            if (((ComboBoxBase)this.getNode()).isArmed()) {
                ((ComboBoxBase)this.getNode()).disarm();
            }
        }
    }

    private void forwardToParent(KeyEvent keyEvent) {
        if (((ComboBoxBase)this.getNode()).getParent() != null) {
            ((ComboBoxBase)this.getNode()).getParent().fireEvent(keyEvent);
        }
    }

    private void cancelEdit(KeyEvent keyEvent) {
        ComboBoxBase comboBoxBase = (ComboBoxBase)this.getNode();
        TextField textField = null;
        if (comboBoxBase instanceof DatePicker) {
            textField = ((DatePicker)comboBoxBase).getEditor();
        } else if (comboBoxBase instanceof ComboBox) {
            TextField textField2 = textField = comboBoxBase.isEditable() ? ((ComboBox)comboBoxBase).getEditor() : null;
        }
        if (textField != null && textField.getTextFormatter() != null) {
            textField.cancelEdit();
        } else {
            this.forwardToParent(keyEvent);
        }
    }

    public void mousePressed(MouseEvent mouseEvent) {
        this.arm(mouseEvent);
    }

    public void mouseReleased(MouseEvent mouseEvent) {
        this.disarm();
        if (this.showPopupOnMouseRelease) {
            this.show();
        } else {
            this.showPopupOnMouseRelease = true;
            this.hide();
        }
    }

    public void mouseEntered(MouseEvent mouseEvent) {
        EventTarget eventTarget;
        this.mouseInsideButton = !((ComboBoxBase)this.getNode()).isEditable() ? true : (eventTarget = mouseEvent.getTarget()) instanceof Node && "arrow-button".equals(((Node)eventTarget).getId());
        this.arm();
    }

    public void mouseExited(MouseEvent mouseEvent) {
        this.mouseInsideButton = false;
        this.disarm();
    }

    private void arm(MouseEvent mouseEvent) {
        boolean bl;
        boolean bl2 = bl = mouseEvent.getButton() == MouseButton.PRIMARY && !mouseEvent.isMiddleButtonDown() && !mouseEvent.isSecondaryButtonDown() && !mouseEvent.isShiftDown() && !mouseEvent.isControlDown() && !mouseEvent.isAltDown() && !mouseEvent.isMetaDown();
        if (!((ComboBoxBase)this.getNode()).isArmed() && bl) {
            ((ComboBoxBase)this.getNode()).arm();
        }
    }

    public void show() {
        if (!((ComboBoxBase)this.getNode()).isShowing()) {
            if (((ComboBoxBase)this.getNode()).isFocusTraversable()) {
                ((ComboBoxBase)this.getNode()).requestFocus();
            }
            ((ComboBoxBase)this.getNode()).show();
        }
    }

    public void hide() {
        if (((ComboBoxBase)this.getNode()).isShowing()) {
            ((ComboBoxBase)this.getNode()).hide();
        }
    }

    public void onAutoHide(PopupControl popupControl) {
        this.hide();
        this.showPopupOnMouseRelease = this.mouseInsideButton ? !this.showPopupOnMouseRelease : true;
    }

    public void arm() {
        if (((ComboBoxBase)this.getNode()).isPressed()) {
            ((ComboBoxBase)this.getNode()).arm();
        }
    }

    public void disarm() {
        if (!this.keyDown && ((ComboBoxBase)this.getNode()).isArmed()) {
            ((ComboBoxBase)this.getNode()).disarm();
        }
    }
}

