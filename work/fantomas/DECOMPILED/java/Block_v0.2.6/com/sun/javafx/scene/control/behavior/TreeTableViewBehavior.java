/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.TableViewBehaviorBase;
import com.sun.javafx.scene.control.behavior.TreeViewBehavior;
import com.sun.javafx.scene.control.inputmap.InputMap;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableFocusModel;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;

public class TreeTableViewBehavior<T>
extends TableViewBehaviorBase<TreeTableView<T>, TreeItem<T>, TreeTableColumn<T, ?>> {
    private final ChangeListener<TreeTableView.TreeTableViewSelectionModel<T>> selectionModelListener = (observableValue, treeTableViewSelectionModel, treeTableViewSelectionModel2) -> {
        if (treeTableViewSelectionModel != null) {
            treeTableViewSelectionModel.getSelectedCells().removeListener(this.weakSelectedCellsListener);
        }
        if (treeTableViewSelectionModel2 != null) {
            treeTableViewSelectionModel2.getSelectedCells().addListener(this.weakSelectedCellsListener);
        }
    };
    private final WeakChangeListener<TreeTableView.TreeTableViewSelectionModel<T>> weakSelectionModelListener = new WeakChangeListener<TreeTableView.TreeTableViewSelectionModel<TreeTableView.TreeTableViewSelectionModel<T>>>(this.selectionModelListener);

    public TreeTableViewBehavior(TreeTableView<T> treeTableView) {
        super(treeTableView);
        InputMap<TreeTableView<T>> inputMap = new InputMap<TreeTableView<T>>(treeTableView);
        inputMap.getMappings().addAll(new InputMap.KeyMapping(KeyCode.LEFT, keyEvent -> this.rtl(treeTableView, this::expandRow, this::collapseRow)), new InputMap.KeyMapping(KeyCode.KP_LEFT, keyEvent -> this.rtl(treeTableView, this::expandRow, this::collapseRow)), new InputMap.KeyMapping(KeyCode.RIGHT, keyEvent -> this.rtl(treeTableView, this::collapseRow, this::expandRow)), new InputMap.KeyMapping(KeyCode.KP_RIGHT, keyEvent -> this.rtl(treeTableView, this::collapseRow, this::expandRow)), new InputMap.KeyMapping(KeyCode.MULTIPLY, keyEvent -> this.expandAll()), new InputMap.KeyMapping(KeyCode.ADD, keyEvent -> this.expandRow()), new InputMap.KeyMapping(KeyCode.SUBTRACT, keyEvent -> this.collapseRow()));
        this.addDefaultChildMap(this.getInputMap(), inputMap);
        treeTableView.selectionModelProperty().addListener(this.weakSelectionModelListener);
        if (this.getSelectionModel() != null) {
            treeTableView.getSelectionModel().getSelectedCells().addListener(this.selectedCellsListener);
        }
    }

    @Override
    protected int getItemCount() {
        return ((TreeTableView)this.getNode()).getExpandedItemCount();
    }

    @Override
    protected TableFocusModel getFocusModel() {
        return ((TreeTableView)this.getNode()).getFocusModel();
    }

    @Override
    protected TableSelectionModel<TreeItem<T>> getSelectionModel() {
        return ((TreeTableView)this.getNode()).getSelectionModel();
    }

    @Override
    protected ObservableList<TreeTablePosition<T, ?>> getSelectedCells() {
        return ((TreeTableView)this.getNode()).getSelectionModel().getSelectedCells();
    }

    @Override
    protected TablePositionBase getFocusedCell() {
        return ((TreeTableView)this.getNode()).getFocusModel().getFocusedCell();
    }

    @Override
    protected int getVisibleLeafIndex(TableColumnBase tableColumnBase) {
        return ((TreeTableView)this.getNode()).getVisibleLeafIndex((TreeTableColumn)tableColumnBase);
    }

    @Override
    protected TreeTableColumn getVisibleLeafColumn(int n) {
        return ((TreeTableView)this.getNode()).getVisibleLeafColumn(n);
    }

    @Override
    protected boolean isControlEditable() {
        return ((TreeTableView)this.getNode()).isEditable();
    }

    @Override
    protected void editCell(int n, TableColumnBase tableColumnBase) {
        ((TreeTableView)this.getNode()).edit(n, (TreeTableColumn)tableColumnBase);
    }

    @Override
    protected ObservableList<TreeTableColumn<T, ?>> getVisibleLeafColumns() {
        return ((TreeTableView)this.getNode()).getVisibleLeafColumns();
    }

    @Override
    protected TablePositionBase<TreeTableColumn<T, ?>> getTablePosition(int n, TableColumnBase<TreeItem<T>, ?> tableColumnBase) {
        return new TreeTablePosition((TreeTableView)this.getNode(), n, (TreeTableColumn)tableColumnBase);
    }

    @Override
    protected void selectAllToFocus(boolean bl) {
        if (((TreeTableView)this.getNode()).getEditingCell() != null) {
            return;
        }
        super.selectAllToFocus(bl);
    }

    private void rightArrowPressed() {
        if (((TreeTableView)this.getNode()).getSelectionModel().isCellSelectionEnabled()) {
            if (this.isRTL()) {
                this.selectLeftCell();
            } else {
                this.selectRightCell();
            }
        } else {
            this.expandRow();
        }
    }

    private void leftArrowPressed() {
        if (((TreeTableView)this.getNode()).getSelectionModel().isCellSelectionEnabled()) {
            if (this.isRTL()) {
                this.selectRightCell();
            } else {
                this.selectLeftCell();
            }
        } else {
            this.collapseRow();
        }
    }

    private void expandRow() {
        Callback callback = treeItem -> ((TreeTableView)this.getNode()).getRow(treeItem);
        TreeViewBehavior.expandRow(((TreeTableView)this.getNode()).getSelectionModel(), callback);
    }

    private void expandAll() {
        TreeViewBehavior.expandAll(((TreeTableView)this.getNode()).getRoot());
    }

    private void collapseRow() {
        TreeTableView treeTableView = (TreeTableView)this.getNode();
        TreeViewBehavior.collapseRow(treeTableView.getSelectionModel(), treeTableView.getRoot(), treeTableView.isShowRoot());
    }
}

