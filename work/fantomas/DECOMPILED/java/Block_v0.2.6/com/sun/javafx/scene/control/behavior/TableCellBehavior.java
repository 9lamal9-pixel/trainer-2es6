/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.TableCellBehaviorBase;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableView;

public class TableCellBehavior<S, T>
extends TableCellBehaviorBase<S, T, TableColumn<S, ?>, TableCell<S, T>> {
    public TableCellBehavior(TableCell<S, T> tableCell) {
        super(tableCell);
    }

    @Override
    protected TableView<S> getCellContainer() {
        return ((TableCell)this.getNode()).getTableView();
    }

    @Override
    protected TableColumn<S, T> getTableColumn() {
        return ((TableCell)this.getNode()).getTableColumn();
    }

    @Override
    protected int getItemCount() {
        return ((TableView)this.getCellContainer()).getItems().size();
    }

    @Override
    protected TableView.TableViewSelectionModel<S> getSelectionModel() {
        return ((TableView)this.getCellContainer()).getSelectionModel();
    }

    @Override
    protected TableView.TableViewFocusModel<S> getFocusModel() {
        return ((TableView)this.getCellContainer()).getFocusModel();
    }

    @Override
    protected TablePositionBase getFocusedCell() {
        return ((TableView)this.getCellContainer()).getFocusModel().getFocusedCell();
    }

    @Override
    protected boolean isTableRowSelected() {
        return ((TableCell)this.getNode()).getTableRow().isSelected();
    }

    @Override
    protected int getVisibleLeafIndex(TableColumnBase tableColumnBase) {
        return ((TableView)this.getCellContainer()).getVisibleLeafIndex((TableColumn)tableColumnBase);
    }

    @Override
    protected void focus(int n, TableColumnBase tableColumnBase) {
        ((TableView.TableViewFocusModel)this.getFocusModel()).focus(n, (TableColumn)tableColumnBase);
    }

    @Override
    protected void edit(TableCell<S, T> tableCell) {
        if (tableCell == null) {
            ((TableView)this.getCellContainer()).edit(-1, null);
        } else {
            ((TableView)this.getCellContainer()).edit(tableCell.getIndex(), tableCell.getTableColumn());
        }
    }
}

