/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.ButtonBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import javafx.geometry.Side;
import javafx.scene.control.MenuButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public abstract class MenuButtonBehaviorBase<C extends MenuButton>
extends ButtonBehavior<C> {
    private final InputMap<C> buttonInputMap = super.getInputMap();

    public MenuButtonBehaviorBase(C c) {
        super(c);
        this.removeMapping(MouseEvent.MOUSE_RELEASED);
        this.addDefaultMapping(new InputMap.KeyMapping(KeyCode.ESCAPE, keyEvent -> ((MenuButton)this.getNode()).hide()));
        this.addDefaultMapping(new InputMap.KeyMapping(KeyCode.CANCEL, keyEvent -> ((MenuButton)this.getNode()).hide()));
        InputMap<C> inputMap = new InputMap<C>(c);
        this.addDefaultMapping(inputMap, new InputMap.KeyMapping(KeyCode.UP, this::overrideTraversalInput));
        this.addDefaultMapping(inputMap, new InputMap.KeyMapping(KeyCode.DOWN, this::overrideTraversalInput));
        this.addDefaultMapping(inputMap, new InputMap.KeyMapping(KeyCode.LEFT, this::overrideTraversalInput));
        this.addDefaultMapping(inputMap, new InputMap.KeyMapping(KeyCode.RIGHT, this::overrideTraversalInput));
        this.addDefaultChildMap(this.buttonInputMap, inputMap);
    }

    private void overrideTraversalInput(KeyEvent keyEvent) {
        MenuButton menuButton = (MenuButton)this.getNode();
        Side side = menuButton.getPopupSide();
        if (!menuButton.isShowing() && keyEvent.getCode() == KeyCode.UP && side == Side.TOP || keyEvent.getCode() == KeyCode.DOWN && (side == Side.BOTTOM || side == Side.TOP) || keyEvent.getCode() == KeyCode.LEFT && (side == Side.RIGHT || side == Side.LEFT) || keyEvent.getCode() == KeyCode.RIGHT && (side == Side.RIGHT || side == Side.LEFT)) {
            menuButton.show();
        }
    }

    protected void openAction() {
        if (((MenuButton)this.getNode()).isShowing()) {
            ((MenuButton)this.getNode()).hide();
        } else {
            ((MenuButton)this.getNode()).show();
        }
    }

    public void mousePressed(MouseEvent mouseEvent, boolean bl) {
        MenuButton menuButton = (MenuButton)this.getNode();
        if (bl) {
            if (menuButton.isShowing()) {
                menuButton.hide();
            }
            super.mousePressed(mouseEvent);
        } else {
            if (!menuButton.isFocused() && menuButton.isFocusTraversable()) {
                menuButton.requestFocus();
            }
            if (menuButton.isShowing()) {
                menuButton.hide();
            } else if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                menuButton.show();
            }
        }
    }

    public void mouseReleased(MouseEvent mouseEvent, boolean bl) {
        if (bl) {
            super.mouseReleased(mouseEvent);
        } else {
            if (((MenuButton)this.getNode()).isShowing() && !((MenuButton)this.getNode()).contains(mouseEvent.getX(), mouseEvent.getY())) {
                ((MenuButton)this.getNode()).hide();
            }
            ((MenuButton)this.getNode()).disarm();
        }
    }
}

