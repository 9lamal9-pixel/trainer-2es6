/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import java.lang.ref.WeakReference;
import javafx.beans.NamedArg;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

public class TreeTablePosition<S, T>
extends TablePositionBase<TreeTableColumn<S, T>> {
    private final WeakReference<TreeTableView<S>> controlRef;
    private final WeakReference<TreeItem<S>> treeItemRef;
    int fixedColumnIndex = -1;
    private final int nonFixedColumnIndex;

    public TreeTablePosition(@NamedArg(value="treeTableView") TreeTableView<S> treeTableView, @NamedArg(value="row") int n, @NamedArg(value="tableColumn") TreeTableColumn<S, T> treeTableColumn) {
        this(treeTableView, n, treeTableColumn, true);
    }

    TreeTablePosition(@NamedArg(value="treeTableView") TreeTableView<S> treeTableView, @NamedArg(value="row") int n, @NamedArg(value="tableColumn") TreeTableColumn<S, T> treeTableColumn, boolean bl) {
        super(n, treeTableColumn);
        this.controlRef = new WeakReference<TreeTableView<S>>(treeTableView);
        this.treeItemRef = new WeakReference<Object>((bl ? treeTableView.getTreeItem(n) : null));
        this.nonFixedColumnIndex = treeTableView == null || treeTableColumn == null ? -1 : treeTableView.getVisibleLeafIndex(treeTableColumn);
    }

    @Override
    public int getColumn() {
        if (this.fixedColumnIndex > -1) {
            return this.fixedColumnIndex;
        }
        return this.nonFixedColumnIndex;
    }

    public final TreeTableView<S> getTreeTableView() {
        return (TreeTableView)this.controlRef.get();
    }

    @Override
    public final TreeTableColumn<S, T> getTableColumn() {
        return (TreeTableColumn)super.getTableColumn();
    }

    public final TreeItem<S> getTreeItem() {
        return (TreeItem)this.treeItemRef.get();
    }
}

