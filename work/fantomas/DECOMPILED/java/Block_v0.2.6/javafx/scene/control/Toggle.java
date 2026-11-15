/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableMap;
import javafx.scene.control.ToggleGroup;

public interface Toggle {
    public ToggleGroup getToggleGroup();

    public void setToggleGroup(ToggleGroup var1);

    public ObjectProperty<ToggleGroup> toggleGroupProperty();

    public boolean isSelected();

    public void setSelected(boolean var1);

    public BooleanProperty selectedProperty();

    public Object getUserData();

    public void setUserData(Object var1);

    public ObservableMap<Object, Object> getProperties();
}

