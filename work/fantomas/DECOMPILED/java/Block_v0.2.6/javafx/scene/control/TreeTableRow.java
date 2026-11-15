/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import java.lang.ref.WeakReference;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.skin.TreeTableRowSkin;

public class TreeTableRow<T>
extends IndexedCell<T> {
    private final ListChangeListener<Integer> selectedListener = change -> this.updateSelection();
    private final InvalidationListener focusedListener = observable -> this.updateFocus();
    private final InvalidationListener editingListener = observable -> this.updateEditing();
    private final InvalidationListener leafListener = new InvalidationListener(){

        @Override
        public void invalidated(Observable observable) {
            TreeItem treeItem = TreeTableRow.this.getTreeItem();
            if (treeItem != null) {
                TreeTableRow.this.requestLayout();
            }
        }
    };
    private boolean oldExpanded;
    private final InvalidationListener treeItemExpandedInvalidationListener = observable -> {
        boolean bl = ((BooleanProperty)observable).get();
        this.pseudoClassStateChanged(EXPANDED_PSEUDOCLASS_STATE, bl);
        this.pseudoClassStateChanged(COLLAPSED_PSEUDOCLASS_STATE, !bl);
        if (bl != this.oldExpanded) {
            this.notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        }
        this.oldExpanded = bl;
    };
    private final WeakListChangeListener<Integer> weakSelectedListener = new WeakListChangeListener<Integer>(this.selectedListener);
    private final WeakInvalidationListener weakFocusedListener = new WeakInvalidationListener(this.focusedListener);
    private final WeakInvalidationListener weakEditingListener = new WeakInvalidationListener(this.editingListener);
    private final WeakInvalidationListener weakLeafListener = new WeakInvalidationListener(this.leafListener);
    private final WeakInvalidationListener weakTreeItemExpandedInvalidationListener = new WeakInvalidationListener(this.treeItemExpandedInvalidationListener);
    private ReadOnlyObjectWrapper<TreeItem<T>> treeItem = new ReadOnlyObjectWrapper<TreeItem<T>>(this, "treeItem"){
        TreeItem<T> oldValue;
        {
            this.oldValue = null;
        }

        @Override
        protected void invalidated() {
            if (this.oldValue != null) {
                this.oldValue.expandedProperty().removeListener(TreeTableRow.this.weakTreeItemExpandedInvalidationListener);
            }
            this.oldValue = (TreeItem)this.get();
            if (this.oldValue != null) {
                TreeTableRow.this.oldExpanded = this.oldValue.isExpanded();
                this.oldValue.expandedProperty().addListener(TreeTableRow.this.weakTreeItemExpandedInvalidationListener);
                TreeTableRow.this.weakTreeItemExpandedInvalidationListener.invalidated(this.oldValue.expandedProperty());
            }
        }
    };
    private ObjectProperty<Node> disclosureNode = new SimpleObjectProperty<Node>(this, "disclosureNode");
    private ReadOnlyObjectWrapper<TreeTableView<T>> treeTableView = new ReadOnlyObjectWrapper<TreeTableView<T>>(this, "treeTableView"){
        private WeakReference<TreeTableView<T>> weakTreeTableViewRef;

        @Override
        protected void invalidated() {
            TreeTableView.TreeTableViewFocusModel treeTableViewFocusModel;
            TreeTableView.TreeTableViewSelectionModel treeTableViewSelectionModel;
            if (this.weakTreeTableViewRef != null) {
                TreeTableView treeTableView = (TreeTableView)this.weakTreeTableViewRef.get();
                if (treeTableView != null) {
                    treeTableViewSelectionModel = treeTableView.getSelectionModel();
                    if (treeTableViewSelectionModel != null) {
                        treeTableViewSelectionModel.getSelectedIndices().removeListener(TreeTableRow.this.weakSelectedListener);
                    }
                    if ((treeTableViewFocusModel = treeTableView.getFocusModel()) != null) {
                        treeTableViewFocusModel.focusedIndexProperty().removeListener(TreeTableRow.this.weakFocusedListener);
                    }
                    treeTableView.editingCellProperty().removeListener(TreeTableRow.this.weakEditingListener);
                }
                this.weakTreeTableViewRef = null;
            }
            if (this.get() != null) {
                treeTableViewSelectionModel = ((TreeTableView)this.get()).getSelectionModel();
                if (treeTableViewSelectionModel != null) {
                    treeTableViewSelectionModel.getSelectedIndices().addListener(TreeTableRow.this.weakSelectedListener);
                }
                if ((treeTableViewFocusModel = ((TreeTableView)this.get()).getFocusModel()) != null) {
                    treeTableViewFocusModel.focusedIndexProperty().addListener(TreeTableRow.this.weakFocusedListener);
                }
                ((TreeTableView)this.get()).editingCellProperty().addListener(TreeTableRow.this.weakEditingListener);
                this.weakTreeTableViewRef = new WeakReference<TreeTableView>((TreeTableView)this.get());
            }
            TreeTableRow.this.updateItem();
            TreeTableRow.this.requestLayout();
        }
    };
    private int index = -1;
    private boolean isFirstRun = true;
    private static final String DEFAULT_STYLE_CLASS = "tree-table-row-cell";
    private static final PseudoClass EXPANDED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("expanded");
    private static final PseudoClass COLLAPSED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("collapsed");

    public TreeTableRow() {
        this.getStyleClass().addAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.setAccessibleRole(AccessibleRole.TREE_TABLE_ROW);
    }

    private void setTreeItem(TreeItem<T> treeItem) {
        this.treeItem.set(treeItem);
    }

    public final TreeItem<T> getTreeItem() {
        return (TreeItem)this.treeItem.get();
    }

    public final ReadOnlyObjectProperty<TreeItem<T>> treeItemProperty() {
        return this.treeItem.getReadOnlyProperty();
    }

    public final void setDisclosureNode(Node node) {
        this.disclosureNodeProperty().set(node);
    }

    public final Node getDisclosureNode() {
        return (Node)this.disclosureNode.get();
    }

    public final ObjectProperty<Node> disclosureNodeProperty() {
        return this.disclosureNode;
    }

    private void setTreeTableView(TreeTableView<T> treeTableView) {
        this.treeTableView.set(treeTableView);
    }

    public final TreeTableView<T> getTreeTableView() {
        return (TreeTableView)this.treeTableView.get();
    }

    public final ReadOnlyObjectProperty<TreeTableView<T>> treeTableViewProperty() {
        return this.treeTableView.getReadOnlyProperty();
    }

    @Override
    void indexChanged(int n, int n2) {
        this.index = this.getIndex();
        this.updateItem();
        this.updateSelection();
        this.updateFocus();
    }

    @Override
    public void startEdit() {
        TreeTableView<T> treeTableView = this.getTreeTableView();
        if (!this.isEditable() || treeTableView != null && !treeTableView.isEditable()) {
            return;
        }
        super.startEdit();
        if (treeTableView != null) {
            treeTableView.fireEvent(new TreeTableView.EditEvent<Object>((TreeTableView<Object>)treeTableView, TreeTableView.editStartEvent(), (TreeItem<Object>)this.getTreeItem(), this.getItem(), null));
            treeTableView.requestFocus();
        }
    }

    @Override
    public void commitEdit(T t) {
        if (!this.isEditing()) {
            return;
        }
        TreeItem<T> treeItem = this.getTreeItem();
        TreeTableView<T> treeTableView = this.getTreeTableView();
        if (treeTableView != null) {
            treeTableView.fireEvent(new TreeTableView.EditEvent<T>(treeTableView, TreeTableView.editCommitEvent(), treeItem, this.getItem(), t));
        }
        if (treeItem != null) {
            treeItem.setValue(t);
            this.updateTreeItem(treeItem);
            this.updateItem(t, false);
        }
        super.commitEdit(t);
        if (treeTableView != null) {
            treeTableView.edit(-1, null);
            treeTableView.requestFocus();
        }
    }

    @Override
    public void cancelEdit() {
        if (!this.isEditing()) {
            return;
        }
        TreeTableView<T> treeTableView = this.getTreeTableView();
        if (treeTableView != null) {
            treeTableView.fireEvent(new TreeTableView.EditEvent<Object>((TreeTableView<Object>)treeTableView, TreeTableView.editCancelEvent(), (TreeItem<Object>)this.getTreeItem(), this.getItem(), null));
        }
        super.cancelEdit();
        if (treeTableView != null) {
            treeTableView.edit(-1, null);
            treeTableView.requestFocus();
        }
    }

    private void updateItem() {
        TreeTableView<T> treeTableView = this.getTreeTableView();
        if (treeTableView == null) {
            return;
        }
        boolean bl = this.index >= 0 && this.index < treeTableView.getExpandedItemCount();
        TreeItem<T> treeItem = this.getTreeItem();
        boolean bl2 = this.isEmpty();
        if (bl) {
            TreeItem<T> treeItem2 = treeTableView.getTreeItem(this.index);
            Object t = treeItem2 == null ? null : (Object)treeItem2.getValue();
            this.updateTreeItem(treeItem2);
            this.updateItem(t, false);
        } else if (!bl2 && treeItem != null || this.isFirstRun) {
            this.updateTreeItem(null);
            this.updateItem(null, true);
            this.isFirstRun = false;
        }
    }

    private void updateSelection() {
        if (this.isEmpty()) {
            return;
        }
        if (this.index == -1 || this.getTreeTableView() == null) {
            return;
        }
        if (this.getTreeTableView().getSelectionModel() == null) {
            return;
        }
        boolean bl = this.getTreeTableView().getSelectionModel().isSelected(this.index);
        if (this.isSelected() == bl) {
            return;
        }
        this.updateSelected(bl);
    }

    private void updateFocus() {
        if (this.getIndex() == -1 || this.getTreeTableView() == null) {
            return;
        }
        if (this.getTreeTableView().getFocusModel() == null) {
            return;
        }
        this.setFocused(this.getTreeTableView().getFocusModel().isFocused(this.getIndex()));
    }

    private void updateEditing() {
        TreeItem<T> treeItem;
        if (this.getIndex() == -1 || this.getTreeTableView() == null || this.getTreeItem() == null) {
            return;
        }
        TreeTablePosition<T, ?> treeTablePosition = this.getTreeTableView().getEditingCell();
        if (treeTablePosition != null && treeTablePosition.getTableColumn() != null) {
            return;
        }
        TreeItem<T> treeItem2 = treeItem = treeTablePosition == null ? null : treeTablePosition.getTreeItem();
        if (!this.isEditing() && this.getTreeItem().equals(treeItem)) {
            this.startEdit();
        } else if (this.isEditing() && !this.getTreeItem().equals(treeItem)) {
            this.cancelEdit();
        }
    }

    public final void updateTreeTableView(TreeTableView<T> treeTableView) {
        this.setTreeTableView(treeTableView);
    }

    public final void updateTreeItem(TreeItem<T> treeItem) {
        TreeItem<T> treeItem2 = this.getTreeItem();
        if (treeItem2 != null) {
            treeItem2.leafProperty().removeListener(this.weakLeafListener);
        }
        this.setTreeItem(treeItem);
        if (treeItem != null) {
            treeItem.leafProperty().addListener(this.weakLeafListener);
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TreeTableRowSkin(this);
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        TreeItem<T> treeItem = this.getTreeItem();
        TreeTableView<T> treeTableView = this.getTreeTableView();
        switch (accessibleAttribute) {
            case TREE_ITEM_PARENT: {
                if (treeItem == null) {
                    return null;
                }
                TreeItem<T> treeItem2 = treeItem.getParent();
                if (treeItem2 == null) {
                    return null;
                }
                int n = treeTableView.getRow(treeItem2);
                return treeTableView.queryAccessibleAttribute(AccessibleAttribute.ROW_AT_INDEX, n);
            }
            case TREE_ITEM_COUNT: {
                if (treeItem == null) {
                    return 0;
                }
                if (!treeItem.isExpanded()) {
                    return 0;
                }
                return treeItem.getChildren().size();
            }
            case TREE_ITEM_AT_INDEX: {
                if (treeItem == null) {
                    return null;
                }
                if (!treeItem.isExpanded()) {
                    return null;
                }
                int n = (Integer)objectArray[0];
                if (n >= treeItem.getChildren().size()) {
                    return null;
                }
                TreeItem treeItem3 = (TreeItem)treeItem.getChildren().get(n);
                if (treeItem3 == null) {
                    return null;
                }
                int n2 = treeTableView.getRow(treeItem3);
                return treeTableView.queryAccessibleAttribute(AccessibleAttribute.ROW_AT_INDEX, n2);
            }
            case LEAF: {
                return treeItem == null ? true : treeItem.isLeaf();
            }
            case EXPANDED: {
                return treeItem == null ? false : treeItem.isExpanded();
            }
            case INDEX: {
                return this.getIndex();
            }
            case DISCLOSURE_LEVEL: {
                return treeTableView == null ? 0 : treeTableView.getTreeItemLevel(treeItem);
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    @Override
    public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
        switch (accessibleAction) {
            case EXPAND: {
                TreeItem<T> treeItem = this.getTreeItem();
                if (treeItem == null) break;
                treeItem.setExpanded(true);
                break;
            }
            case COLLAPSE: {
                TreeItem<T> treeItem = this.getTreeItem();
                if (treeItem == null) break;
                treeItem.setExpanded(false);
                break;
            }
            default: {
                super.executeAccessibleAction(accessibleAction, new Object[0]);
            }
        }
    }
}

