/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control;

import com.sun.javafx.collections.NonIterableChange;
import com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList;
import javafx.collections.FXCollections;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;

public class TreeTableViewBackingList<T>
extends ReadOnlyUnbackedObservableList<TreeItem<T>> {
    private final TreeTableView<T> treeTable;
    private int size = -1;

    public TreeTableViewBackingList(TreeTableView<T> treeTableView) {
        this.treeTable = treeTableView;
    }

    public void resetSize() {
        int n = this.size;
        this.size = -1;
        this.callObservers(new NonIterableChange.GenericAddRemoveChange(0, n, FXCollections.emptyObservableList(), this));
    }

    @Override
    public TreeItem<T> get(int n) {
        return this.treeTable.getTreeItem(n);
    }

    @Override
    public int size() {
        if (this.size == -1) {
            this.size = this.treeTable.getExpandedItemCount();
        }
        return this.size;
    }
}

