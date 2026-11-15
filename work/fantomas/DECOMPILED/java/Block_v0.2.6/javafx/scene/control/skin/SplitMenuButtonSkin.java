/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.behavior.SplitMenuButtonBehavior;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.skin.MenuButtonSkinBase;
import javafx.scene.input.MouseEvent;

public class SplitMenuButtonSkin
extends MenuButtonSkinBase<SplitMenuButton> {
    private final SplitMenuButtonBehavior behavior;

    public SplitMenuButtonSkin(SplitMenuButton splitMenuButton) {
        super(splitMenuButton);
        this.behavior = new SplitMenuButtonBehavior(splitMenuButton);
        this.behaveLikeButton = true;
        this.arrowButton.addEventHandler(MouseEvent.ANY, mouseEvent -> mouseEvent.consume());
        this.arrowButton.setOnMousePressed(mouseEvent -> {
            this.getBehavior().mousePressed((MouseEvent)mouseEvent, false);
            mouseEvent.consume();
        });
        this.arrowButton.setOnMouseReleased(mouseEvent -> {
            this.getBehavior().mouseReleased((MouseEvent)mouseEvent, false);
            mouseEvent.consume();
        });
        this.label.setLabelFor(splitMenuButton);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }

    SplitMenuButtonBehavior getBehavior() {
        return this.behavior;
    }
}

