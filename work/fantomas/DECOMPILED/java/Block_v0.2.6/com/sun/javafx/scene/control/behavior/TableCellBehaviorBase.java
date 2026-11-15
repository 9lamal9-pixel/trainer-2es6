/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.CellBehaviorBase;
import javafx.scene.control.Control;
import javafx.scene.control.FocusModel;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableFocusModel;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.input.MouseButton;

public abstract class TableCellBehaviorBase<S, T, TC extends TableColumnBase<S, ?>, C extends IndexedCell<T>>
extends CellBehaviorBase<C> {
    public TableCellBehaviorBase(C c) {
        super(c);
    }

    protected abstract TableColumnBase<S, T> getTableColumn();

    protected abstract int getItemCount();

    @Override
    protected abstract TableSelectionModel<S> getSelectionModel();

    protected abstract TableFocusModel<S, TC> getFocusModel();

    protected abstract TablePositionBase getFocusedCell();

    protected abstract boolean isTableRowSelected();

    protected abstract int getVisibleLeafIndex(TableColumnBase<S, T> var1);

    protected abstract void focus(int var1, TableColumnBase<S, T> var2);

    @Override
    protected void doSelect(double d, double d2, MouseButton mouseButton, int n, boolean bl, boolean bl2) {
        IndexedCell indexedCell = (IndexedCell)this.getNode();
        if (!indexedCell.contains(d, d2)) {
            return;
        }
        Control control = this.getCellContainer();
        if (control == null) {
            return;
        }
        int n2 = this.getItemCount();
        if (indexedCell.getIndex() >= n2) {
            return;
        }
        MultipleSelectionModel multipleSelectionModel = this.getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        boolean bl3 = this.isSelected();
        int n3 = indexedCell.getIndex();
        int n4 = this.getColumn();
        TableColumnBase<S, T> tableColumnBase = this.getTableColumn();
        FocusModel focusModel = this.getFocusModel();
        if (focusModel == null) {
            return;
        }
        TablePositionBase tablePositionBase = this.getFocusedCell();
        if (this.handleDisclosureNode(d, d2)) {
            return;
        }
        if (bl) {
            if (!TableCellBehaviorBase.hasNonDefaultAnchor(control)) {
                TableCellBehaviorBase.setAnchor(control, tablePositionBase, false);
            }
        } else {
            TableCellBehaviorBase.removeAnchor(control);
        }
        if (mouseButton == MouseButton.PRIMARY || mouseButton == MouseButton.SECONDARY && !bl3) {
            if (multipleSelectionModel.getSelectionMode() == SelectionMode.SINGLE) {
                this.simpleSelect(mouseButton, n, bl2);
            } else if (bl2) {
                if (bl3) {
                    ((TableSelectionModel)multipleSelectionModel).clearSelection(n3, tableColumnBase);
                    ((TableFocusModel)focusModel).focus(n3, tableColumnBase);
                } else {
                    ((TableSelectionModel)multipleSelectionModel).select(n3, tableColumnBase);
                }
            } else if (bl) {
                TableColumnBase<S, T> tableColumnBase2;
                TablePositionBase tablePositionBase2 = TableCellBehaviorBase.getAnchor(control, tablePositionBase);
                int n5 = tablePositionBase2.getRow();
                boolean bl4 = n5 < n3;
                ((TableSelectionModel)multipleSelectionModel).clearSelection();
                int n6 = Math.min(n5, n3);
                int n7 = Math.max(n5, n3);
                TableColumnBase<S, T> tableColumnBase3 = tablePositionBase2.getColumn() < n4 ? tablePositionBase2.getTableColumn() : tableColumnBase;
                TableColumnBase<S, T> tableColumnBase4 = tableColumnBase2 = tablePositionBase2.getColumn() >= n4 ? tablePositionBase2.getTableColumn() : tableColumnBase;
                if (bl4) {
                    ((TableSelectionModel)multipleSelectionModel).selectRange(n6, tableColumnBase3, n7, tableColumnBase2);
                } else {
                    ((TableSelectionModel)multipleSelectionModel).selectRange(n7, tableColumnBase3, n6, tableColumnBase2);
                }
            } else {
                this.simpleSelect(mouseButton, n, bl2);
            }
        }
    }

    @Override
    protected void simpleSelect(MouseButton mouseButton, int n, boolean bl) {
        TableColumnBase<S, T> tableColumnBase;
        int n2;
        MultipleSelectionModel multipleSelectionModel = this.getSelectionModel();
        boolean bl2 = ((TableSelectionModel)multipleSelectionModel).isSelected(n2 = ((IndexedCell)this.getNode()).getIndex(), tableColumnBase = this.getTableColumn());
        if (bl2 && bl) {
            ((TableSelectionModel)multipleSelectionModel).clearSelection(n2, tableColumnBase);
            ((TableFocusModel)this.getFocusModel()).focus(n2, tableColumnBase);
            bl2 = false;
        } else {
            ((TableSelectionModel)multipleSelectionModel).clearAndSelect(n2, tableColumnBase);
        }
        this.handleClicks(mouseButton, n, bl2);
    }

    private int getColumn() {
        if (((TableSelectionModel)this.getSelectionModel()).isCellSelectionEnabled()) {
            TableColumnBase<S, T> tableColumnBase = this.getTableColumn();
            return this.getVisibleLeafIndex(tableColumnBase);
        }
        return -1;
    }

    @Override
    protected boolean isSelected() {
        MultipleSelectionModel multipleSelectionModel = this.getSelectionModel();
        if (multipleSelectionModel == null) {
            return false;
        }
        if (((TableSelectionModel)multipleSelectionModel).isCellSelectionEnabled()) {
            IndexedCell indexedCell = (IndexedCell)this.getNode();
            return indexedCell.isSelected();
        }
        return this.isTableRowSelected();
    }
}

