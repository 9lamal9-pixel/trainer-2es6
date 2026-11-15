/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.TableViewBehaviorBase;
import com.sun.javafx.scene.control.behavior.TwoLevelFocusBehavior;
import com.sun.javafx.scene.control.skin.Utils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableFocusModel;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TableView;

public class TableViewBehavior<T>
extends TableViewBehaviorBase<TableView<T>, T, TableColumn<T, ?>> {
    private final ChangeListener<TableView.TableViewSelectionModel<T>> selectionModelListener = (observableValue, tableViewSelectionModel, tableViewSelectionModel2) -> {
        if (tableViewSelectionModel != null) {
            tableViewSelectionModel.getSelectedCells().removeListener(this.weakSelectedCellsListener);
        }
        if (tableViewSelectionModel2 != null) {
            tableViewSelectionModel2.getSelectedCells().addListener(this.weakSelectedCellsListener);
        }
    };
    private final WeakChangeListener<TableView.TableViewSelectionModel<T>> weakSelectionModelListener = new WeakChangeListener<TableView.TableViewSelectionModel<TableView.TableViewSelectionModel<T>>>(this.selectionModelListener);
    private TwoLevelFocusBehavior tlFocus;

    public TableViewBehavior(TableView<T> tableView) {
        super(tableView);
        tableView.selectionModelProperty().addListener(this.weakSelectionModelListener);
        TableView.TableViewSelectionModel<T> tableViewSelectionModel3 = tableView.getSelectionModel();
        if (tableViewSelectionModel3 != null) {
            tableViewSelectionModel3.getSelectedCells().addListener(this.selectedCellsListener);
        }
        if (Utils.isTwoLevelFocus()) {
            this.tlFocus = new TwoLevelFocusBehavior(tableView);
        }
    }

    @Override
    public void dispose() {
        if (this.tlFocus != null) {
            this.tlFocus.dispose();
        }
        super.dispose();
    }

    @Override
    protected int getItemCount() {
        return ((TableView)this.getNode()).getItems() == null ? 0 : ((TableView)this.getNode()).getItems().size();
    }

    @Override
    protected TableFocusModel getFocusModel() {
        return ((TableView)this.getNode()).getFocusModel();
    }

    @Override
    protected TableSelectionModel<T> getSelectionModel() {
        return ((TableView)this.getNode()).getSelectionModel();
    }

    @Override
    protected ObservableList<TablePosition> getSelectedCells() {
        return ((TableView)this.getNode()).getSelectionModel().getSelectedCells();
    }

    @Override
    protected TablePositionBase getFocusedCell() {
        return ((TableView)this.getNode()).getFocusModel().getFocusedCell();
    }

    @Override
    protected int getVisibleLeafIndex(TableColumnBase tableColumnBase) {
        return ((TableView)this.getNode()).getVisibleLeafIndex((TableColumn)tableColumnBase);
    }

    @Override
    protected TableColumn<T, ?> getVisibleLeafColumn(int n) {
        return ((TableView)this.getNode()).getVisibleLeafColumn(n);
    }

    @Override
    protected boolean isControlEditable() {
        return ((TableView)this.getNode()).isEditable();
    }

    @Override
    protected void editCell(int n, TableColumnBase tableColumnBase) {
        ((TableView)this.getNode()).edit(n, (TableColumn)tableColumnBase);
    }

    @Override
    protected ObservableList<TableColumn<T, ?>> getVisibleLeafColumns() {
        return ((TableView)this.getNode()).getVisibleLeafColumns();
    }

    @Override
    protected TablePositionBase<TableColumn<T, ?>> getTablePosition(int n, TableColumnBase<T, ?> tableColumnBase) {
        return new TablePosition((TableView)this.getNode(), n, (TableColumn)tableColumnBase);
    }

    @Override
    protected void selectAllToFocus(boolean bl) {
        if (((TableView)this.getNode()).getEditingCell() != null) {
            return;
        }
        super.selectAllToFocus(bl);
    }
}

