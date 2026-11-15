/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import java.time.LocalDate;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Cell;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.DateCellSkin;

public class DateCell
extends Cell<LocalDate> {
    private static final String DEFAULT_STYLE_CLASS = "date-cell";

    public DateCell() {
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
        this.setAccessibleRole(AccessibleRole.TEXT);
    }

    @Override
    public void updateItem(LocalDate localDate, boolean bl) {
        super.updateItem(localDate, bl);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DateCellSkin(this);
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case TEXT: {
                if (this.isFocused()) {
                    return this.getText();
                }
                return "";
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }
}

