/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.TextField;

public class PasswordField
extends TextField {
    public PasswordField() {
        this.getStyleClass().add("password-field");
        this.setAccessibleRole(AccessibleRole.PASSWORD_FIELD);
    }

    @Override
    public void cut() {
    }

    @Override
    public void copy() {
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case TEXT: {
                return null;
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }
}

