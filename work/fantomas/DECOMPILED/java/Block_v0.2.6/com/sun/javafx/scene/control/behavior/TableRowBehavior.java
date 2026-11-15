/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.TableRowBehaviorBase;
import javafx.collections.ObservableList;
import javafx.scene.control.FocusModel;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TableView;

public class TableRowBehavior<T>
extends TableRowBehaviorBase<TableRow<T>> {
    public TableRowBehavior(TableRow<T> tableRow) {
        super(tableRow);
    }

    @Override
    protected TableSelectionModel<T> getSelectionModel() {
        return ((TableView)this.getCellContainer()).getSelectionModel();
    }

    @Override
    protected TablePositionBase<?> getFocusedCell() {
        return ((TableView)this.getCellContainer()).getFocusModel().getFocusedCell();
    }

    @Override
    protected FocusModel<T> getFocusModel() {
        return ((TableView)this.getCellContainer()).getFocusModel();
    }

    @Override
    protected ObservableList getVisibleLeafColumns() {
        return ((TableView)this.getCellContainer()).getVisibleLeafColumns();
    }

    @Override
    protected TableView<T> getCellContainer() {
        return ((TableRow)this.getNode()).getTableView();
    }

    @Override
    protected void edit(TableRow<T> tableRow) {
    }
}

