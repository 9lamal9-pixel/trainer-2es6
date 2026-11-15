/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.scene.control.FocusModel;
import javafx.scene.control.TableColumnBase;

public abstract class TableFocusModel<T, TC extends TableColumnBase<T, ?>>
extends FocusModel<T> {
    public abstract void focus(int var1, TC var2);

    public abstract boolean isFocused(int var1, TC var2);

    public abstract void focusAboveCell();

    public abstract void focusBelowCell();

    public abstract void focusLeftCell();

    public abstract void focusRightCell();
}

