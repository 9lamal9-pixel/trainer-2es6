/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.TableCellBehaviorBase;
import java.util.Iterator;
import javafx.scene.Node;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.MouseButton;

public class TreeTableCellBehavior<S, T>
extends TableCellBehaviorBase<TreeItem<S>, T, TreeTableColumn<S, ?>, TreeTableCell<S, T>> {
    public TreeTableCellBehavior(TreeTableCell<S, T> treeTableCell) {
        super(treeTableCell);
    }

    @Override
    protected TreeTableView<S> getCellContainer() {
        return ((TreeTableCell)this.getNode()).getTreeTableView();
    }

    @Override
    protected TreeTableColumn<S, T> getTableColumn() {
        return ((TreeTableCell)this.getNode()).getTableColumn();
    }

    @Override
    protected int getItemCount() {
        return ((TreeTableView)this.getCellContainer()).getExpandedItemCount();
    }

    @Override
    protected TreeTableView.TreeTableViewSelectionModel<S> getSelectionModel() {
        return ((TreeTableView)this.getCellContainer()).getSelectionModel();
    }

    @Override
    protected TreeTableView.TreeTableViewFocusModel<S> getFocusModel() {
        return ((TreeTableView)this.getCellContainer()).getFocusModel();
    }

    @Override
    protected TablePositionBase getFocusedCell() {
        return ((TreeTableView)this.getCellContainer()).getFocusModel().getFocusedCell();
    }

    @Override
    protected boolean isTableRowSelected() {
        return ((TreeTableCell)this.getNode()).getTreeTableRow().isSelected();
    }

    @Override
    protected int getVisibleLeafIndex(TableColumnBase tableColumnBase) {
        return ((TreeTableView)this.getCellContainer()).getVisibleLeafIndex((TreeTableColumn)tableColumnBase);
    }

    @Override
    protected void focus(int n, TableColumnBase tableColumnBase) {
        ((TreeTableView.TreeTableViewFocusModel)this.getFocusModel()).focus(n, (TreeTableColumn)tableColumnBase);
    }

    @Override
    protected void edit(TreeTableCell<S, T> treeTableCell) {
        if (treeTableCell == null) {
            ((TreeTableView)this.getCellContainer()).edit(-1, null);
        } else {
            ((TreeTableView)this.getCellContainer()).edit(treeTableCell.getIndex(), treeTableCell.getTableColumn());
        }
    }

    @Override
    protected boolean handleDisclosureNode(double d, double d2) {
        Node node;
        TreeTableColumn treeTableColumn;
        TreeItem treeItem = ((TreeTableCell)this.getNode()).getTreeTableRow().getTreeItem();
        TreeTableView treeTableView = ((TreeTableCell)this.getNode()).getTreeTableView();
        TableColumnBase tableColumnBase = this.getTableColumn();
        TreeTableColumn treeTableColumn2 = treeTableColumn = treeTableView.getTreeColumn() == null ? treeTableView.getVisibleLeafColumn(0) : treeTableView.getTreeColumn();
        if (tableColumnBase == treeTableColumn && (node = ((TreeTableCell)this.getNode()).getTreeTableRow().getDisclosureNode()) != null) {
            TreeTableColumn treeTableColumn3;
            double d3 = 0.0;
            Iterator iterator = treeTableView.getVisibleLeafColumns().iterator();
            while (iterator.hasNext() && (treeTableColumn3 = (TreeTableColumn)iterator.next()) != treeTableColumn) {
                d3 += treeTableColumn3.getWidth();
            }
            double d4 = node.getBoundsInParent().getMaxX();
            if (d < d4 - d3) {
                if (treeItem != null) {
                    treeItem.setExpanded(!treeItem.isExpanded());
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected void handleClicks(MouseButton mouseButton, int n, boolean bl) {
        TreeItem treeItem = ((TreeTableCell)this.getNode()).getTreeTableRow().getTreeItem();
        if (mouseButton == MouseButton.PRIMARY) {
            if (n == 1 && bl) {
                this.edit((TreeTableCell)this.getNode());
            } else if (n == 1) {
                this.edit((TreeTableCell<S, T>)null);
            } else if (n == 2 && treeItem.isLeaf()) {
                this.edit((TreeTableCell)this.getNode());
            } else if (n % 2 == 0) {
                treeItem.setExpanded(!treeItem.isExpanded());
            }
        }
    }
}

