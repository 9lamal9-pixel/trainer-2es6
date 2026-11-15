/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.skin.CellSkinBase;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

public abstract class TableCellSkinBase<S, T, C extends IndexedCell<T>>
extends CellSkinBase<C> {
    boolean isDeferToParentForPrefWidth = false;
    private InvalidationListener columnWidthListener = observable -> ((IndexedCell)this.getSkinnable()).requestLayout();
    private WeakInvalidationListener weakColumnWidthListener = new WeakInvalidationListener(this.columnWidthListener);

    public TableCellSkinBase(C c) {
        super(c);
        Rectangle rectangle = new Rectangle();
        rectangle.widthProperty().bind(((Region)c).widthProperty());
        rectangle.heightProperty().bind(((Region)c).heightProperty());
        ((IndexedCell)this.getSkinnable()).setClip(rectangle);
        TableColumnBase<S, T> tableColumnBase = this.getTableColumn();
        if (tableColumnBase != null) {
            tableColumnBase.widthProperty().addListener(this.weakColumnWidthListener);
        }
        if (((Node)c).getProperties().containsKey("deferToParentPrefWidth")) {
            this.isDeferToParentForPrefWidth = true;
        }
    }

    public abstract ReadOnlyObjectProperty<? extends TableColumnBase<S, T>> tableColumnProperty();

    public final TableColumnBase<S, T> getTableColumn() {
        return (TableColumnBase)this.tableColumnProperty().get();
    }

    @Override
    public void dispose() {
        TableColumnBase<S, T> tableColumnBase = this.getTableColumn();
        if (tableColumnBase != null) {
            tableColumnBase.widthProperty().removeListener(this.weakColumnWidthListener);
        }
        super.dispose();
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        this.layoutLabelInArea(d, d2, d3, d4 - ((IndexedCell)this.getSkinnable()).getPadding().getBottom());
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        if (this.isDeferToParentForPrefWidth) {
            return super.computePrefWidth(d, d2, d3, d4, d5);
        }
        TableColumnBase<S, T> tableColumnBase = this.getTableColumn();
        return tableColumnBase == null ? 0.0 : this.snapSizeX(tableColumnBase.getWidth());
    }
}

