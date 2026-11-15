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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ControlUtils;
import javafx.scene.control.FocusModel;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.TreeCellSkin;

public class TreeCell<T>
extends IndexedCell<T> {
    private final ListChangeListener<Integer> selectedListener = change -> this.updateSelection();
    private final ChangeListener<MultipleSelectionModel<TreeItem<T>>> selectionModelPropertyListener = new ChangeListener<MultipleSelectionModel<TreeItem<T>>>(){

        @Override
        public void changed(ObservableValue<? extends MultipleSelectionModel<TreeItem<T>>> observableValue, MultipleSelectionModel<TreeItem<T>> multipleSelectionModel, MultipleSelectionModel<TreeItem<T>> multipleSelectionModel2) {
            if (multipleSelectionModel != null) {
                multipleSelectionModel.getSelectedIndices().removeListener(TreeCell.this.weakSelectedListener);
            }
            if (multipleSelectionModel2 != null) {
                multipleSelectionModel2.getSelectedIndices().addListener(TreeCell.this.weakSelectedListener);
            }
            TreeCell.this.updateSelection();
        }
    };
    private final InvalidationListener focusedListener = observable -> this.updateFocus();
    private final ChangeListener<FocusModel<TreeItem<T>>> focusModelPropertyListener = new ChangeListener<FocusModel<TreeItem<T>>>(){

        @Override
        public void changed(ObservableValue<? extends FocusModel<TreeItem<T>>> observableValue, FocusModel<TreeItem<T>> focusModel, FocusModel<TreeItem<T>> focusModel2) {
            if (focusModel != null) {
                focusModel.focusedIndexProperty().removeListener(TreeCell.this.weakFocusedListener);
            }
            if (focusModel2 != null) {
                focusModel2.focusedIndexProperty().addListener(TreeCell.this.weakFocusedListener);
            }
            TreeCell.this.updateFocus();
        }
    };
    private final InvalidationListener editingListener = observable -> this.updateEditing();
    private final InvalidationListener leafListener = new InvalidationListener(){

        @Override
        public void invalidated(Observable observable) {
            TreeItem treeItem = TreeCell.this.getTreeItem();
            if (treeItem != null) {
                TreeCell.this.requestLayout();
            }
        }
    };
    private boolean oldIsExpanded;
    private final InvalidationListener treeItemExpandedInvalidationListener = new InvalidationListener(){

        @Override
        public void invalidated(Observable observable) {
            boolean bl = ((BooleanProperty)observable).get();
            TreeCell.this.pseudoClassStateChanged(EXPANDED_PSEUDOCLASS_STATE, bl);
            TreeCell.this.pseudoClassStateChanged(COLLAPSED_PSEUDOCLASS_STATE, !bl);
            if (bl != TreeCell.this.oldIsExpanded) {
                TreeCell.this.notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
            }
            TreeCell.this.oldIsExpanded = bl;
        }
    };
    private final InvalidationListener rootPropertyListener = observable -> this.updateItem(-1);
    private final WeakListChangeListener<Integer> weakSelectedListener = new WeakListChangeListener<Integer>(this.selectedListener);
    private final WeakChangeListener<MultipleSelectionModel<TreeItem<T>>> weakSelectionModelPropertyListener = new WeakChangeListener<MultipleSelectionModel<TreeItem<MultipleSelectionModel<TreeItem<T>>>>>(this.selectionModelPropertyListener);
    private final WeakInvalidationListener weakFocusedListener = new WeakInvalidationListener(this.focusedListener);
    private final WeakChangeListener<FocusModel<TreeItem<T>>> weakFocusModelPropertyListener = new WeakChangeListener<FocusModel<TreeItem<FocusModel<TreeItem<T>>>>>(this.focusModelPropertyListener);
    private final WeakInvalidationListener weakEditingListener = new WeakInvalidationListener(this.editingListener);
    private final WeakInvalidationListener weakLeafListener = new WeakInvalidationListener(this.leafListener);
    private final WeakInvalidationListener weakTreeItemExpandedInvalidationListener = new WeakInvalidationListener(this.treeItemExpandedInvalidationListener);
    private final WeakInvalidationListener weakRootPropertyListener = new WeakInvalidationListener(this.rootPropertyListener);
    private ReadOnlyObjectWrapper<TreeItem<T>> treeItem = new ReadOnlyObjectWrapper<TreeItem<T>>(this, "treeItem"){
        TreeItem<T> oldValue;
        {
            this.oldValue = null;
        }

        @Override
        protected void invalidated() {
            if (this.oldValue != null) {
                this.oldValue.expandedProperty().removeListener(TreeCell.this.weakTreeItemExpandedInvalidationListener);
            }
            this.oldValue = (TreeItem)this.get();
            if (this.oldValue != null) {
                TreeCell.this.oldIsExpanded = this.oldValue.isExpanded();
                this.oldValue.expandedProperty().addListener(TreeCell.this.weakTreeItemExpandedInvalidationListener);
                TreeCell.this.weakTreeItemExpandedInvalidationListener.invalidated(this.oldValue.expandedProperty());
            }
        }
    };
    private ObjectProperty<Node> disclosureNode = new SimpleObjectProperty<Node>(this, "disclosureNode");
    private ReadOnlyObjectWrapper<TreeView<T>> treeView = new ReadOnlyObjectWrapper<TreeView<T>>(){
        private WeakReference<TreeView<T>> weakTreeViewRef;

        @Override
        protected void invalidated() {
            FocusModel focusModel;
            MultipleSelectionModel<TreeItem<Object>> multipleSelectionModel;
            TreeView treeView;
            if (this.weakTreeViewRef != null) {
                treeView = (TreeView)this.weakTreeViewRef.get();
                if (treeView != null) {
                    multipleSelectionModel = treeView.getSelectionModel();
                    if (multipleSelectionModel != null) {
                        multipleSelectionModel.getSelectedIndices().removeListener(TreeCell.this.weakSelectedListener);
                    }
                    if ((focusModel = treeView.getFocusModel()) != null) {
                        focusModel.focusedIndexProperty().removeListener(TreeCell.this.weakFocusedListener);
                    }
                    treeView.editingItemProperty().removeListener(TreeCell.this.weakEditingListener);
                    treeView.focusModelProperty().removeListener(TreeCell.this.weakFocusModelPropertyListener);
                    treeView.selectionModelProperty().removeListener(TreeCell.this.weakSelectionModelPropertyListener);
                    treeView.rootProperty().removeListener(TreeCell.this.weakRootPropertyListener);
                }
                this.weakTreeViewRef = null;
            }
            if ((treeView = (TreeView)this.get()) != null) {
                multipleSelectionModel = treeView.getSelectionModel();
                if (multipleSelectionModel != null) {
                    multipleSelectionModel.getSelectedIndices().addListener(TreeCell.this.weakSelectedListener);
                }
                if ((focusModel = treeView.getFocusModel()) != null) {
                    focusModel.focusedIndexProperty().addListener(TreeCell.this.weakFocusedListener);
                }
                treeView.editingItemProperty().addListener(TreeCell.this.weakEditingListener);
                treeView.focusModelProperty().addListener(TreeCell.this.weakFocusModelPropertyListener);
                treeView.selectionModelProperty().addListener(TreeCell.this.weakSelectionModelPropertyListener);
                treeView.rootProperty().addListener(TreeCell.this.weakRootPropertyListener);
                this.weakTreeViewRef = new WeakReference<TreeView>(treeView);
            }
            TreeCell.this.updateItem(-1);
            TreeCell.this.requestLayout();
        }

        @Override
        public Object getBean() {
            return TreeCell.this;
        }

        @Override
        public String getName() {
            return "treeView";
        }
    };
    private boolean isFirstRun = true;
    private boolean updateEditingIndex = true;
    private static final String DEFAULT_STYLE_CLASS = "tree-cell";
    private static final PseudoClass EXPANDED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("expanded");
    private static final PseudoClass COLLAPSED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("collapsed");

    public TreeCell() {
        this.getStyleClass().addAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.setAccessibleRole(AccessibleRole.TREE_ITEM);
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

    private void setTreeView(TreeView<T> treeView) {
        this.treeView.set(treeView);
    }

    public final TreeView<T> getTreeView() {
        return (TreeView)this.treeView.get();
    }

    public final ReadOnlyObjectProperty<TreeView<T>> treeViewProperty() {
        return this.treeView.getReadOnlyProperty();
    }

    @Override
    public void startEdit() {
        if (this.isEditing()) {
            return;
        }
        TreeView<T> treeView = this.getTreeView();
        if (!this.isEditable() || treeView != null && !treeView.isEditable()) {
            return;
        }
        this.updateItem(-1);
        super.startEdit();
        if (treeView != null) {
            treeView.fireEvent(new TreeView.EditEvent<Object>((TreeView<Object>)treeView, TreeView.editStartEvent(), (TreeItem<Object>)this.getTreeItem(), this.getItem(), null));
            treeView.requestFocus();
        }
    }

    @Override
    public void commitEdit(T t) {
        if (!this.isEditing()) {
            return;
        }
        TreeItem<T> treeItem = this.getTreeItem();
        TreeView treeView = this.getTreeView();
        if (treeView != null) {
            treeView.fireEvent(new TreeView.EditEvent<T>(treeView, TreeView.editCommitEvent(), treeItem, this.getItem(), t));
        }
        super.commitEdit(t);
        if (treeItem != null) {
            treeItem.setValue(t);
            this.updateTreeItem(treeItem);
            this.updateItem(t, false);
        }
        if (treeView != null) {
            treeView.edit(null);
            ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(treeView);
        }
    }

    @Override
    public void cancelEdit() {
        if (!this.isEditing()) {
            return;
        }
        TreeView treeView = this.getTreeView();
        super.cancelEdit();
        if (treeView != null) {
            if (this.updateEditingIndex) {
                treeView.edit(null);
            }
            ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(treeView);
            treeView.fireEvent(new TreeView.EditEvent<Object>(treeView, TreeView.editCancelEvent(), (TreeItem<Object>)this.getTreeItem(), this.getItem(), null));
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TreeCellSkin(this);
    }

    @Override
    void indexChanged(int n, int n2) {
        super.indexChanged(n, n2);
        if (!this.isEditing() || n2 != n) {
            this.updateItem(n);
            this.updateSelection();
            this.updateFocus();
        }
    }

    private void updateItem(int n) {
        TreeView<T> treeView = this.getTreeView();
        if (treeView == null) {
            return;
        }
        int n2 = this.getIndex();
        boolean bl = n2 >= 0 && n2 < treeView.getExpandedItemCount();
        boolean bl2 = this.isEmpty();
        TreeItem<T> treeItem = this.getTreeItem();
        if (bl) {
            Object t;
            TreeItem<T> treeItem2 = treeView.getTreeItem(n2);
            Object t2 = treeItem2 == null ? null : (Object)treeItem2.getValue();
            Object t3 = t = treeItem == null ? null : (Object)treeItem.getValue();
            if (n != n2 || this.isItemChanged(t, t2)) {
                this.updateTreeItem(treeItem2);
                this.updateItem(t2, false);
            }
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
        if (this.getIndex() == -1 || this.getTreeView() == null) {
            return;
        }
        MultipleSelectionModel<TreeItem<T>> multipleSelectionModel = this.getTreeView().getSelectionModel();
        if (multipleSelectionModel == null) {
            this.updateSelected(false);
            return;
        }
        boolean bl = multipleSelectionModel.isSelected(this.getIndex());
        if (this.isSelected() == bl) {
            return;
        }
        this.updateSelected(bl);
    }

    private void updateFocus() {
        if (this.getIndex() == -1 || this.getTreeView() == null) {
            return;
        }
        FocusModel<TreeItem<T>> focusModel = this.getTreeView().getFocusModel();
        if (focusModel == null) {
            this.setFocused(false);
            return;
        }
        this.setFocused(focusModel.isFocused(this.getIndex()));
    }

    private void updateEditing() {
        int n = this.getIndex();
        TreeView<T> treeView = this.getTreeView();
        TreeItem<T> treeItem = this.getTreeItem();
        TreeItem<T> treeItem2 = treeView == null ? null : treeView.getEditingItem();
        boolean bl = this.isEditing();
        if (n == -1 || treeView == null || treeItem == null) {
            return;
        }
        boolean bl2 = treeItem.equals(treeItem2);
        if (bl2 && !bl) {
            this.startEdit();
        } else if (!bl2 && bl) {
            this.updateEditingIndex = false;
            this.cancelEdit();
            this.updateEditingIndex = true;
        }
    }

    public final void updateTreeView(TreeView<T> treeView) {
        this.setTreeView(treeView);
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
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        TreeItem<T> treeItem = this.getTreeItem();
        TreeView<T> treeView = this.getTreeView();
        switch (accessibleAttribute) {
            case TREE_ITEM_PARENT: {
                if (treeView == null) {
                    return null;
                }
                if (treeItem == null) {
                    return null;
                }
                TreeItem<T> treeItem2 = treeItem.getParent();
                if (treeItem2 == null) {
                    return null;
                }
                int n = treeView.getRow(treeItem2);
                return treeView.queryAccessibleAttribute(AccessibleAttribute.ROW_AT_INDEX, n);
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
                int n2 = treeView.getRow(treeItem3);
                return treeView.queryAccessibleAttribute(AccessibleAttribute.ROW_AT_INDEX, n2);
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
            case SELECTED: {
                return this.isSelected();
            }
            case DISCLOSURE_LEVEL: {
                return treeView == null ? 0 : treeView.getTreeItemLevel(treeItem);
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
            case REQUEST_FOCUS: {
                FocusModel<TreeItem<T>> focusModel;
                TreeView<T> treeView = this.getTreeView();
                if (treeView == null || (focusModel = treeView.getFocusModel()) == null) break;
                focusModel.focus(this.getIndex());
                break;
            }
            default: {
                super.executeAccessibleAction(accessibleAction, new Object[0]);
            }
        }
    }
}

