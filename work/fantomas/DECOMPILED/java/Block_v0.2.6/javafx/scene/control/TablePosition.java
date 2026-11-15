/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import java.lang.ref.WeakReference;
import javafx.beans.NamedArg;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableView;

public class TablePosition<S, T>
extends TablePositionBase<TableColumn<S, T>> {
    private final WeakReference<TableView<S>> controlRef;
    private final WeakReference<S> itemRef;
    int fixedColumnIndex = -1;
    private final int nonFixedColumnIndex;

    public TablePosition(@NamedArg(value="tableView") TableView<S> tableView, @NamedArg(value="row") int n, @NamedArg(value="tableColumn") TableColumn<S, T> tableColumn) {
        super(n, tableColumn);
        this.controlRef = new WeakReference<TableView<S>>(tableView);
        ObservableList<S> observableList = tableView.getItems();
        this.itemRef = new WeakReference<Object>((observableList != null && n >= 0 && n < observableList.size() ? (Object)observableList.get(n) : null));
        this.nonFixedColumnIndex = tableView == null || tableColumn == null ? -1 : tableView.getVisibleLeafIndex(tableColumn);
    }

    @Override
    public int getColumn() {
        if (this.fixedColumnIndex > -1) {
            return this.fixedColumnIndex;
        }
        return this.nonFixedColumnIndex;
    }

    public final TableView<S> getTableView() {
        return (TableView)this.controlRef.get();
    }

    @Override
    public final TableColumn<S, T> getTableColumn() {
        return (TableColumn)super.getTableColumn();
    }

    final S getItem() {
        return this.itemRef == null ? null : (S)this.itemRef.get();
    }

    public String toString() {
        return "TablePosition [ row: " + this.getRow() + ", column: " + (TableColumn)this.getTableColumn() + ", tableView: " + this.getTableView() + " ]";
    }
}

