/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.ButtonBehavior;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.skin.LabeledSkinBase;

public class HyperlinkSkin
extends LabeledSkinBase<Hyperlink> {
    private final BehaviorBase<Hyperlink> behavior;

    public HyperlinkSkin(Hyperlink hyperlink) {
        super(hyperlink);
        this.behavior = new ButtonBehavior<Hyperlink>(hyperlink);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }
}

