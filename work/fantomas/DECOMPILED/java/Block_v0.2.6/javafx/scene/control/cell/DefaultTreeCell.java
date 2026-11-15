/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.cell;

import java.lang.ref.WeakReference;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;

class DefaultTreeCell<T>
extends TreeCell<T> {
    private HBox hbox;
    private WeakReference<TreeItem<T>> treeItemRef;
    private InvalidationListener treeItemGraphicListener = observable -> this.updateDisplay(this.getItem(), this.isEmpty());
    private InvalidationListener treeItemListener = new InvalidationListener(){

        @Override
        public void invalidated(Observable observable) {
            TreeItem treeItem;
            TreeItem treeItem2;
            TreeItem treeItem3 = treeItem2 = DefaultTreeCell.this.treeItemRef == null ? null : (TreeItem)DefaultTreeCell.this.treeItemRef.get();
            if (treeItem2 != null) {
                treeItem2.graphicProperty().removeListener(DefaultTreeCell.this.weakTreeItemGraphicListener);
            }
            if ((treeItem = DefaultTreeCell.this.getTreeItem()) != null) {
                treeItem.graphicProperty().addListener(DefaultTreeCell.this.weakTreeItemGraphicListener);
                DefaultTreeCell.this.treeItemRef = new WeakReference(treeItem);
            }
        }
    };
    private WeakInvalidationListener weakTreeItemGraphicListener = new WeakInvalidationListener(this.treeItemGraphicListener);
    private WeakInvalidationListener weakTreeItemListener = new WeakInvalidationListener(this.treeItemListener);

    public DefaultTreeCell() {
        this.treeItemProperty().addListener(this.weakTreeItemListener);
        if (this.getTreeItem() != null) {
            this.getTreeItem().graphicProperty().addListener(this.weakTreeItemGraphicListener);
        }
    }

    void updateDisplay(T t, boolean bl) {
        if (t == null || bl) {
            this.hbox = null;
            this.setText(null);
            this.setGraphic(null);
        } else {
            TreeItem treeItem = this.getTreeItem();
            if (treeItem != null && treeItem.getGraphic() != null) {
                if (t instanceof Node) {
                    this.setText(null);
                    if (this.hbox == null) {
                        this.hbox = new HBox(3.0);
                    }
                    this.hbox.getChildren().setAll((Node[])new Node[]{treeItem.getGraphic(), (Node)t});
                    this.setGraphic(this.hbox);
                } else {
                    this.hbox = null;
                    this.setText(t.toString());
                    this.setGraphic(treeItem.getGraphic());
                }
            } else {
                this.hbox = null;
                if (t instanceof Node) {
                    this.setText(null);
                    this.setGraphic((Node)t);
                } else {
                    this.setText(t.toString());
                    this.setGraphic(null);
                }
            }
        }
    }

    @Override
    public void updateItem(T t, boolean bl) {
        super.updateItem(t, bl);
        this.updateDisplay(t, bl);
    }
}

