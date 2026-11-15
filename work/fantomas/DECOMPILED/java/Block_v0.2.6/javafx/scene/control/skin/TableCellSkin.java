/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.TableCellBehavior;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.skin.TableCellSkinBase;

public class TableCellSkin<S, T>
extends TableCellSkinBase<S, T, TableCell<S, T>> {
    private final BehaviorBase<TableCell<S, T>> behavior;

    public TableCellSkin(TableCell<S, T> tableCell) {
        super(tableCell);
        this.behavior = new TableCellBehavior<S, T>(tableCell);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }

    @Override
    public ReadOnlyObjectProperty<TableColumn<S, T>> tableColumnProperty() {
        return ((TableCell)this.getSkinnable()).tableColumnProperty();
    }
}

