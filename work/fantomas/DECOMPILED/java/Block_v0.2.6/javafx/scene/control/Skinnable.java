/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.property.ObjectProperty;
import javafx.scene.control.Skin;

public interface Skinnable {
    public ObjectProperty<Skin<?>> skinProperty();

    public void setSkin(Skin<?> var1);

    public Skin<?> getSkin();
}

