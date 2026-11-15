/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.ButtonBehavior;
import com.sun.javafx.scene.control.skin.Utils;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.skin.LabeledSkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class ButtonSkin
extends LabeledSkinBase<Button> {
    private KeyCodeCombination defaultAcceleratorKeyCodeCombination;
    private KeyCodeCombination cancelAcceleratorKeyCodeCombination;
    private final BehaviorBase<Button> behavior;
    Runnable defaultButtonRunnable = () -> {
        if (((Button)this.getSkinnable()).getScene() != null && NodeHelper.isTreeVisible((Node)((Object)this.getSkinnable())) && !((Button)this.getSkinnable()).isDisabled()) {
            ((Button)this.getSkinnable()).fire();
        }
    };
    Runnable cancelButtonRunnable = () -> {
        if (((Button)this.getSkinnable()).getScene() != null && NodeHelper.isTreeVisible((Node)((Object)this.getSkinnable())) && !((Button)this.getSkinnable()).isDisabled()) {
            ((Button)this.getSkinnable()).fire();
        }
    };

    public ButtonSkin(Button button) {
        super(button);
        this.behavior = new ButtonBehavior<Button>(button);
        this.registerChangeListener(button.defaultButtonProperty(), observableValue -> this.setDefaultButton(((Button)this.getSkinnable()).isDefaultButton()));
        this.registerChangeListener(button.cancelButtonProperty(), observableValue -> this.setCancelButton(((Button)this.getSkinnable()).isCancelButton()));
        this.registerChangeListener(button.focusedProperty(), observableValue -> {
            ContextMenu contextMenu;
            if (!((Button)this.getSkinnable()).isFocused() && (contextMenu = ((Button)this.getSkinnable()).getContextMenu()) != null && contextMenu.isShowing()) {
                contextMenu.hide();
                Utils.removeMnemonics(contextMenu, ((Button)this.getSkinnable()).getScene());
            }
        });
        this.registerChangeListener(button.parentProperty(), observableValue -> {
            if (((Button)this.getSkinnable()).getParent() == null && ((Button)this.getSkinnable()).getScene() != null) {
                if (((Button)this.getSkinnable()).isDefaultButton()) {
                    ((Button)this.getSkinnable()).getScene().getAccelerators().remove(this.defaultAcceleratorKeyCodeCombination);
                }
                if (((Button)this.getSkinnable()).isCancelButton()) {
                    ((Button)this.getSkinnable()).getScene().getAccelerators().remove(this.cancelAcceleratorKeyCodeCombination);
                }
            }
        });
        if (((Button)this.getSkinnable()).isDefaultButton()) {
            this.setDefaultButton(true);
        }
        if (((Button)this.getSkinnable()).isCancelButton()) {
            this.setCancelButton(true);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }

    private void setDefaultButton(boolean bl) {
        Scene scene = ((Button)this.getSkinnable()).getScene();
        if (scene != null) {
            KeyCode keyCode = KeyCode.ENTER;
            this.defaultAcceleratorKeyCodeCombination = new KeyCodeCombination(keyCode, new KeyCombination.Modifier[0]);
            Runnable runnable = (Runnable)scene.getAccelerators().get(this.defaultAcceleratorKeyCodeCombination);
            if (!bl) {
                if (this.defaultButtonRunnable.equals(runnable)) {
                    scene.getAccelerators().remove(this.defaultAcceleratorKeyCodeCombination);
                }
            } else if (!this.defaultButtonRunnable.equals(runnable)) {
                scene.getAccelerators().remove(this.defaultAcceleratorKeyCodeCombination);
                scene.getAccelerators().put(this.defaultAcceleratorKeyCodeCombination, this.defaultButtonRunnable);
            }
        }
    }

    private void setCancelButton(boolean bl) {
        Scene scene = ((Button)this.getSkinnable()).getScene();
        if (scene != null) {
            KeyCode keyCode = KeyCode.ESCAPE;
            this.cancelAcceleratorKeyCodeCombination = new KeyCodeCombination(keyCode, new KeyCombination.Modifier[0]);
            Runnable runnable = (Runnable)scene.getAccelerators().get(this.cancelAcceleratorKeyCodeCombination);
            if (!bl) {
                if (this.cancelButtonRunnable.equals(runnable)) {
                    scene.getAccelerators().remove(this.cancelAcceleratorKeyCodeCombination);
                }
            } else if (!this.cancelButtonRunnable.equals(runnable)) {
                scene.getAccelerators().remove(this.cancelAcceleratorKeyCodeCombination);
                scene.getAccelerators().put(this.cancelAcceleratorKeyCodeCombination, this.cancelButtonRunnable);
            }
        }
    }
}

