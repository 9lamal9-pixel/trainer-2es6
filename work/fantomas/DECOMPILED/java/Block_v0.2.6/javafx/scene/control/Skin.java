/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.scene.Node;
import javafx.scene.control.Skinnable;

public interface Skin<C extends Skinnable> {
    public C getSkinnable();

    public Node getNode();

    public void dispose();
}

