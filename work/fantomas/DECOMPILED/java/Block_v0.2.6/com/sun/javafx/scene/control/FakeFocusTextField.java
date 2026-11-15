/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control;

import javafx.scene.AccessibleAttribute;
import javafx.scene.control.TextField;

public final class FakeFocusTextField
extends TextField {
    @Override
    public void requestFocus() {
        if (this.getParent() != null) {
            this.getParent().requestFocus();
        }
    }

    public void setFakeFocus(boolean bl) {
        this.setFocused(bl);
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case FOCUS_ITEM: {
                return this.getParent();
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }
}

