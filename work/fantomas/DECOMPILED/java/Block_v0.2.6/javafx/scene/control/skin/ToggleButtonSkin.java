/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.ToggleButtonBehavior;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.skin.LabeledSkinBase;

public class ToggleButtonSkin
extends LabeledSkinBase<ToggleButton> {
    private final BehaviorBase<ToggleButton> behavior;

    public ToggleButtonSkin(ToggleButton toggleButton) {
        super(toggleButton);
        this.behavior = new ToggleButtonBehavior<ToggleButton>(toggleButton);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }
}

