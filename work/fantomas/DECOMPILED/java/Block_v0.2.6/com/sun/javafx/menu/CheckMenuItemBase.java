/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.menu;

import com.sun.javafx.menu.MenuItemBase;
import javafx.beans.property.BooleanProperty;

public interface CheckMenuItemBase
extends MenuItemBase {
    public void setSelected(boolean var1);

    public boolean isSelected();

    public BooleanProperty selectedProperty();
}

