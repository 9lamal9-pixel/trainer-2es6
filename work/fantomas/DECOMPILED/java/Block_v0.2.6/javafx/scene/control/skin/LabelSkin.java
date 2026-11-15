/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import javafx.scene.control.Label;
import javafx.scene.control.skin.LabeledSkinBase;

public class LabelSkin
extends LabeledSkinBase<Label> {
    public LabelSkin(Label label) {
        super(label);
        this.consumeMouseEvents(false);
        this.registerChangeListener(label.labelForProperty(), observableValue -> this.mnemonicTargetChanged());
    }
}

