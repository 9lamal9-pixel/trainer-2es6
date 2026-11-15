/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.FocusTraversalInputMap;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.KeyBinding;
import javafx.beans.Observable;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public class ButtonBehavior<C extends ButtonBase>
extends BehaviorBase<C> {
    private final InputMap<C> buttonInputMap = this.createInputMap();
    private boolean keyDown;

    public ButtonBehavior(C c) {
        super(c);
        this.addDefaultMapping(this.buttonInputMap, FocusTraversalInputMap.getFocusTraversalMappings());
        this.addDefaultMapping(this.buttonInputMap, new InputMap.KeyMapping(KeyCode.SPACE, KeyEvent.KEY_PRESSED, this::keyPressed), new InputMap.KeyMapping(KeyCode.SPACE, KeyEvent.KEY_RELEASED, this::keyReleased), new InputMap.MouseMapping(MouseEvent.MOUSE_PRESSED, this::mousePressed), new InputMap.MouseMapping(MouseEvent.MOUSE_RELEASED, this::mouseReleased), new InputMap.MouseMapping(MouseEvent.MOUSE_ENTERED, this::mouseEntered), new InputMap.MouseMapping(MouseEvent.MOUSE_EXITED, this::mouseExited), new InputMap.KeyMapping(new KeyBinding(KeyCode.ENTER, KeyEvent.KEY_PRESSED), this::keyPressed, keyEvent -> PlatformUtil.isMac()), new InputMap.KeyMapping(new KeyBinding(KeyCode.ENTER, KeyEvent.KEY_RELEASED), this::keyReleased, keyEvent -> PlatformUtil.isMac()));
        ((Node)c).focusedProperty().addListener(this::focusChanged);
    }

    @Override
    public InputMap<C> getInputMap() {
        return this.buttonInputMap;
    }

    @Override
    public void dispose() {
        super.dispose();
        ((ButtonBase)this.getNode()).focusedProperty().removeListener(this::focusChanged);
    }

    private void focusChanged(Observable observable) {
        ButtonBase buttonBase = (ButtonBase)this.getNode();
        if (this.keyDown && !buttonBase.isFocused()) {
            this.keyDown = false;
            buttonBase.disarm();
        }
    }

    protected void keyPressed(KeyEvent keyEvent) {
        if (!((ButtonBase)this.getNode()).isPressed() && !((ButtonBase)this.getNode()).isArmed()) {
            this.keyDown = true;
            ((ButtonBase)this.getNode()).arm();
        }
    }

    protected void keyReleased(KeyEvent keyEvent) {
        if (this.keyDown) {
            this.keyDown = false;
            if (((ButtonBase)this.getNode()).isArmed()) {
                ((ButtonBase)this.getNode()).disarm();
                ((ButtonBase)this.getNode()).fire();
            }
        }
    }

    protected void mousePressed(MouseEvent mouseEvent) {
        boolean bl;
        if (!((ButtonBase)this.getNode()).isFocused() && ((ButtonBase)this.getNode()).isFocusTraversable()) {
            ((ButtonBase)this.getNode()).requestFocus();
        }
        boolean bl2 = bl = mouseEvent.getButton() == MouseButton.PRIMARY && !mouseEvent.isMiddleButtonDown() && !mouseEvent.isSecondaryButtonDown() && !mouseEvent.isShiftDown() && !mouseEvent.isControlDown() && !mouseEvent.isAltDown() && !mouseEvent.isMetaDown();
        if (!((ButtonBase)this.getNode()).isArmed() && bl) {
            ((ButtonBase)this.getNode()).arm();
        }
    }

    protected void mouseReleased(MouseEvent mouseEvent) {
        if (!this.keyDown && ((ButtonBase)this.getNode()).isArmed()) {
            ((ButtonBase)this.getNode()).fire();
            ((ButtonBase)this.getNode()).disarm();
        }
    }

    protected void mouseEntered(MouseEvent mouseEvent) {
        if (!this.keyDown && ((ButtonBase)this.getNode()).isPressed()) {
            ((ButtonBase)this.getNode()).arm();
        }
    }

    protected void mouseExited(MouseEvent mouseEvent) {
        if (!this.keyDown && ((ButtonBase)this.getNode()).isArmed()) {
            ((ButtonBase)this.getNode()).disarm();
        }
    }
}

