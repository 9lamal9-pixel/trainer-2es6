/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import java.lang.ref.WeakReference;
import java.util.Collection;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.WeakListChangeListener;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.ControlUtils;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.skin.TreeTableCellSkin;

public class TreeTableCell<S, T>
extends IndexedCell<T> {
    boolean lockItemOnEdit = false;
    private boolean itemDirty = false;
    private ListChangeListener<TreeTablePosition<S, ?>> selectedListener = change -> {
        while (change.next()) {
            if (!change.wasAdded() && !change.wasRemoved()) continue;
            this.updateSelection();
        }
    };
    private final InvalidationListener focusedListener = observable -> this.updateFocus();
    private final InvalidationListener tableRowUpdateObserver = observable -> {
        this.itemDirty = true;
        this.requestLayout();
    };
    private final InvalidationListener editingListener = observable -> this.updateEditing();
    private ListChangeListener<TreeTableColumn<S, ?>> visibleLeafColumnsListener = change -> this.updateColumnIndex();
    private ListChangeListener<String> columnStyleClassListener = change -> {
        while (change.next()) {
            if (change.wasRemoved()) {
                this.getStyleClass().removeAll(change.getRemoved());
            }
            if (!change.wasAdded()) continue;
            this.getStyleClass().addAll(change.getAddedSubList());
        }
    };
    private final InvalidationListener rootPropertyListener = observable -> this.updateItem(-1);
    private final InvalidationListener columnStyleListener = observable -> {
        if (this.getTableColumn() != null) {
            this.possiblySetStyle(this.getTableColumn().getStyle());
        }
    };
    private final InvalidationListener columnIdListener = observable -> {
        if (this.getTableColumn() != null) {
            this.possiblySetId(this.getTableColumn().getId());
        }
    };
    private final WeakListChangeListener<TreeTablePosition<S, ?>> weakSelectedListener = new WeakListChangeListener(this.selectedListener);
    private final WeakInvalidationListener weakFocusedListener = new WeakInvalidationListener(this.focusedListener);
    private final WeakInvalidationListener weaktableRowUpdateObserver = new WeakInvalidationListener(this.tableRowUpdateObserver);
    private final WeakInvalidationListener weakEditingListener = new WeakInvalidationListener(this.editingListener);
    private final WeakListChangeListener<TreeTableColumn<S, ?>> weakVisibleLeafColumnsListener = new WeakListChangeListener(this.visibleLeafColumnsListener);
    private final WeakListChangeListener<String> weakColumnStyleClassListener = new WeakListChangeListener<String>(this.columnStyleClassListener);
    private final WeakInvalidationListener weakColumnStyleListener = new WeakInvalidationListener(this.columnStyleListener);
    private final WeakInvalidationListener weakColumnIdListener = new WeakInvalidationListener(this.columnIdListener);
    private final WeakInvalidationListener weakRootPropertyListener = new WeakInvalidationListener(this.rootPropertyListener);
    private ReadOnlyObjectWrapper<TreeTableColumn<S, T>> treeTableColumn = new ReadOnlyObjectWrapper<TreeTableColumn<S, T>>(this, "treeTableColumn"){

        @Override
        protected void invalidated() {
            TreeTableCell.this.updateColumnIndex();
        }
    };
    private ReadOnlyObjectWrapper<TreeTableView<S>> treeTableView;
    private ReadOnlyObjectWrapper<TreeTableRow<S>> treeTableRow = new ReadOnlyObjectWrapper(this, "treeTableRow");
    private boolean isLastVisibleColumn = false;
    private int columnIndex = -1;
    private boolean updateEditingIndex = true;
    private ObservableValue<T> currentObservableValue = null;
    private boolean isFirstRun = true;
    private WeakReference<S> oldRowItemRef;
    private static final String DEFAULT_STYLE_CLASS = "tree-table-cell";
    private static final PseudoClass PSEUDO_CLASS_LAST_VISIBLE = PseudoClass.getPseudoClass("last-visible");

    public TreeTableCell() {
        this.getStyleClass().addAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.setAccessibleRole(AccessibleRole.TREE_TABLE_CELL);
        this.updateColumnIndex();
    }

    public final ReadOnlyObjectProperty<TreeTableColumn<S, T>> tableColumnProperty() {
        return this.treeTableColumn.getReadOnlyProperty();
    }

    private void setTableColumn(TreeTableColumn<S, T> treeTableColumn) {
        this.treeTableColumn.set(treeTableColumn);
    }

    public final TreeTableColumn<S, T> getTableColumn() {
        return (TreeTableColumn)this.treeTableColumn.get();
    }

    private void setTreeTableView(TreeTableView<S> treeTableView) {
        this.treeTableViewPropertyImpl().set(treeTableView);
    }

    public final TreeTableView<S> getTreeTableView() {
        return this.treeTableView == null ? null : (TreeTableView)this.treeTableView.get();
    }

    public final ReadOnlyObjectProperty<TreeTableView<S>> treeTableViewProperty() {
        return this.treeTableViewPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<TreeTableView<S>> treeTableViewPropertyImpl() {
        if (this.treeTableView == null) {
            this.treeTableView = new ReadOnlyObjectWrapper<TreeTableView<S>>(this, "treeTableView"){
                private WeakReference<TreeTableView<S>> weakTableViewRef;

                @Override
                protected void invalidated() {
                    TreeTableView.TreeTableViewFocusModel treeTableViewFocusModel;
                    TreeTableView.TreeTableViewSelectionModel treeTableViewSelectionModel;
                    TreeTableView treeTableView;
                    if (this.weakTableViewRef != null && (treeTableView = (TreeTableView)this.weakTableViewRef.get()) != null) {
                        treeTableViewSelectionModel = treeTableView.getSelectionModel();
                        if (treeTableViewSelectionModel != null) {
                            treeTableViewSelectionModel.getSelectedCells().removeListener(TreeTableCell.this.weakSelectedListener);
                        }
                        if ((treeTableViewFocusModel = treeTableView.getFocusModel()) != null) {
                            treeTableViewFocusModel.focusedCellProperty().removeListener(TreeTableCell.this.weakFocusedListener);
                        }
                        treeTableView.editingCellProperty().removeListener(TreeTableCell.this.weakEditingListener);
                        treeTableView.getVisibleLeafColumns().removeListener(TreeTableCell.this.weakVisibleLeafColumnsListener);
                        treeTableView.rootProperty().removeListener(TreeTableCell.this.weakRootPropertyListener);
                    }
                    if ((treeTableView = (TreeTableView)this.get()) != null) {
                        treeTableViewSelectionModel = treeTableView.getSelectionModel();
                        if (treeTableViewSelectionModel != null) {
                            treeTableViewSelectionModel.getSelectedCells().addListener(TreeTableCell.this.weakSelectedListener);
                        }
                        if ((treeTableViewFocusModel = treeTableView.getFocusModel()) != null) {
                            treeTableViewFocusModel.focusedCellProperty().addListener(TreeTableCell.this.weakFocusedListener);
                        }
                        treeTableView.editingCellProperty().addListener(TreeTableCell.this.weakEditingListener);
                        treeTableView.getVisibleLeafColumns().addListener(TreeTableCell.this.weakVisibleLeafColumnsListener);
                        treeTableView.rootProperty().addListener(TreeTableCell.this.weakRootPropertyListener);
                        this.weakTableViewRef = new WeakReference<TreeTableView>(treeTableView);
                    }
                    TreeTableCell.this.updateColumnIndex();
                }
            };
        }
        return this.treeTableView;
    }

    private void setTreeTableRow(TreeTableRow<S> treeTableRow) {
        this.treeTableRow.set(treeTableRow);
    }

    public final TreeTableRow<S> getTreeTableRow() {
        return (TreeTableRow)this.treeTableRow.get();
    }

    public final ReadOnlyObjectProperty<TreeTableRow<S>> tableRowProperty() {
        return this.treeTableRow;
    }

    @Override
    public void startEdit() {
        if (this.isEditing()) {
            return;
        }
        TreeTableView<S> treeTableView = this.getTreeTableView();
        TreeTableColumn<S, T> treeTableColumn = this.getTableColumn();
        if (!this.isEditable() || treeTableView != null && !treeTableView.isEditable() || treeTableColumn != null && !this.getTableColumn().isEditable()) {
            return;
        }
        if (!this.lockItemOnEdit) {
            this.updateItem(-1);
        }
        super.startEdit();
        if (treeTableColumn != null) {
            TreeTableColumn.CellEditEvent<S, Object> cellEditEvent = new TreeTableColumn.CellEditEvent<S, Object>(treeTableView, treeTableView.getEditingCell(), TreeTableColumn.editStartEvent(), null);
            Event.fireEvent(treeTableColumn, cellEditEvent);
        }
    }

    @Override
    public void commitEdit(T t) {
        if (!this.isEditing()) {
            return;
        }
        TreeTableView treeTableView = this.getTreeTableView();
        if (treeTableView != null) {
            TreeTablePosition<S, ?> treeTablePosition = treeTableView.getEditingCell();
            TreeTableColumn.CellEditEvent cellEditEvent = new TreeTableColumn.CellEditEvent(treeTableView, treeTablePosition, TreeTableColumn.editCommitEvent(), t);
            Event.fireEvent(this.getTableColumn(), cellEditEvent);
        }
        super.commitEdit(t);
        this.updateItem(t, false);
        if (treeTableView != null) {
            treeTableView.edit(-1, null);
            ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(treeTableView);
        }
    }

    @Override
    public void cancelEdit() {
        if (!this.isEditing()) {
            return;
        }
        TreeTableView treeTableView = this.getTreeTableView();
        super.cancelEdit();
        if (treeTableView != null) {
            TreeTablePosition<S, ?> treeTablePosition = treeTableView.getEditingCell();
            if (this.updateEditingIndex) {
                treeTableView.edit(-1, null);
            }
            ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(treeTableView);
            TreeTableColumn.CellEditEvent<S, Object> cellEditEvent = new TreeTableColumn.CellEditEvent<S, Object>(treeTableView, treeTablePosition, TreeTableColumn.editCancelEvent(), null);
            Event.fireEvent(this.getTableColumn(), cellEditEvent);
        }
    }

    @Override
    public void updateSelected(boolean bl) {
        if (this.getTreeTableRow() == null || this.getTreeTableRow().isEmpty()) {
            return;
        }
        this.setSelected(bl);
    }

    @Override
    void indexChanged(int n, int n2) {
        super.indexChanged(n, n2);
        if (!this.isEditing() || n2 != n) {
            this.updateItem(n);
            this.updateSelection();
            this.updateFocus();
            this.updateEditing();
        }
    }

    private void updateColumnIndex() {
        TreeTableView<S> treeTableView = this.getTreeTableView();
        TreeTableColumn<S, T> treeTableColumn = this.getTableColumn();
        this.columnIndex = treeTableView == null || treeTableColumn == null ? -1 : treeTableView.getVisibleLeafIndex(treeTableColumn);
        this.isLastVisibleColumn = this.getTableColumn() != null && this.columnIndex != -1 && this.columnIndex == treeTableView.getVisibleLeafColumns().size() - 1;
        this.pseudoClassStateChanged(PSEUDO_CLASS_LAST_VISIBLE, this.isLastVisibleColumn);
    }

    private void updateSelection() {
        if (this.isEmpty()) {
            return;
        }
        boolean bl = this.isSelected();
        if (!this.isInCellSelectionMode()) {
            if (bl) {
                this.updateSelected(false);
            }
            return;
        }
        TreeTableView<S> treeTableView = this.getTreeTableView();
        if (this.getIndex() == -1 || treeTableView == null) {
            return;
        }
        TreeTableView.TreeTableViewSelectionModel<S> treeTableViewSelectionModel = treeTableView.getSelectionModel();
        if (treeTableViewSelectionModel == null) {
            this.updateSelected(false);
            return;
        }
        boolean bl2 = treeTableViewSelectionModel.isSelected(this.getIndex(), this.getTableColumn());
        if (bl == bl2) {
            return;
        }
        this.updateSelected(bl2);
    }

    private void updateFocus() {
        boolean bl = this.isFocused();
        if (!this.isInCellSelectionMode()) {
            if (bl) {
                this.setFocused(false);
            }
            return;
        }
        TreeTableView<S> treeTableView = this.getTreeTableView();
        if (this.getIndex() == -1 || treeTableView == null) {
            return;
        }
        TreeTableView.TreeTableViewFocusModel<S> treeTableViewFocusModel = treeTableView.getFocusModel();
        if (treeTableViewFocusModel == null) {
            this.setFocused(false);
            return;
        }
        this.setFocused(treeTableViewFocusModel.isFocused(this.getIndex(), this.getTableColumn()));
    }

    private void updateEditing() {
        TreeTableView<S> treeTableView = this.getTreeTableView();
        if (this.getIndex() == -1 || treeTableView == null) {
            return;
        }
        TreeTablePosition<S, ?> treeTablePosition = treeTableView.getEditingCell();
        boolean bl = this.match(treeTablePosition);
        if (bl && !this.isEditing()) {
            this.startEdit();
        } else if (!bl && this.isEditing()) {
            this.updateEditingIndex = false;
            this.cancelEdit();
            this.updateEditingIndex = true;
        }
    }

    private boolean match(TreeTablePosition treeTablePosition) {
        return treeTablePosition != null && treeTablePosition.getRow() == this.getIndex() && treeTablePosition.getTableColumn() == this.getTableColumn();
    }

    private boolean isInCellSelectionMode() {
        TreeTableView<S> treeTableView = this.getTreeTableView();
        if (treeTableView == null) {
            return false;
        }
        TreeTableView.TreeTableViewSelectionModel<S> treeTableViewSelectionModel = treeTableView.getSelectionModel();
        return treeTableViewSelectionModel != null && treeTableViewSelectionModel.isCellSelectionEnabled();
    }

    private void updateItem(int n) {
        Object t;
        block8: {
            Object t2;
            block7: {
                Object object;
                boolean bl;
                if (this.currentObservableValue != null) {
                    this.currentObservableValue.removeListener(this.weaktableRowUpdateObserver);
                }
                TreeTableView<S> treeTableView = this.getTreeTableView();
                TreeTableColumn<S, T> treeTableColumn = this.getTableColumn();
                int n2 = treeTableView == null ? -1 : this.getTreeTableView().getExpandedItemCount();
                int n3 = this.getIndex();
                boolean bl2 = this.isEmpty();
                Object t3 = this.getItem();
                TreeTableRow<S> treeTableRow = this.getTreeTableRow();
                t = treeTableRow == null ? null : (Object)treeTableRow.getItem();
                boolean bl3 = bl = n3 >= n2;
                if (bl || n3 < 0 || this.columnIndex < 0 || !this.isVisible() || treeTableColumn == null || !treeTableColumn.isVisible() || treeTableView.getRoot() == null) {
                    if (!bl2 && t3 != null || this.isFirstRun || bl) {
                        this.updateItem(null, true);
                        this.isFirstRun = false;
                    }
                    return;
                }
                this.currentObservableValue = treeTableColumn.getCellObservableValue(n3);
                Object t4 = t2 = this.currentObservableValue == null ? null : (Object)this.currentObservableValue.getValue();
                if (n != n3 || this.isItemChanged(t3, t2)) break block7;
                Object object2 = object = this.oldRowItemRef != null ? (Object)this.oldRowItemRef.get() : null;
                if (object != null && object.equals(t)) break block8;
            }
            this.updateItem(t2, false);
        }
        this.oldRowItemRef = new WeakReference<Object>(t);
        if (this.currentObservableValue == null) {
            return;
        }
        this.currentObservableValue.addListener(this.weaktableRowUpdateObserver);
    }

    @Override
    protected void layoutChildren() {
        if (this.itemDirty) {
            this.updateItem(-1);
            this.itemDirty = false;
        }
        super.layoutChildren();
    }

    public final void updateTreeTableView(TreeTableView<S> treeTableView) {
        this.setTreeTableView(treeTableView);
    }

    public final void updateTreeTableRow(TreeTableRow<S> treeTableRow) {
        this.setTreeTableRow(treeTableRow);
    }

    public final void updateTreeTableColumn(TreeTableColumn<S, T> treeTableColumn) {
        TreeTableColumn<S, T> treeTableColumn2 = this.getTableColumn();
        if (treeTableColumn2 != null) {
            treeTableColumn2.getStyleClass().removeListener(this.weakColumnStyleClassListener);
            this.getStyleClass().removeAll((Collection<?>)treeTableColumn2.getStyleClass());
            treeTableColumn2.idProperty().removeListener(this.weakColumnIdListener);
            treeTableColumn2.styleProperty().removeListener(this.weakColumnStyleListener);
            String string = this.getId();
            String string2 = this.getStyle();
            if (string != null && string.equals(treeTableColumn2.getId())) {
                this.setId(null);
            }
            if (string2 != null && string2.equals(treeTableColumn2.getStyle())) {
                this.setStyle("");
            }
        }
        this.setTableColumn(treeTableColumn);
        if (treeTableColumn != null) {
            this.getStyleClass().addAll((Collection<String>)treeTableColumn.getStyleClass());
            treeTableColumn.getStyleClass().addListener(this.weakColumnStyleClassListener);
            treeTableColumn.idProperty().addListener(this.weakColumnIdListener);
            treeTableColumn.styleProperty().addListener(this.weakColumnStyleListener);
            this.possiblySetId(treeTableColumn.getId());
            this.possiblySetStyle(treeTableColumn.getStyle());
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TreeTableCellSkin(this);
    }

    private void possiblySetId(String string) {
        if (this.getId() == null || this.getId().isEmpty()) {
            this.setId(string);
        }
    }

    private void possiblySetStyle(String string) {
        if (this.getStyle() == null || this.getStyle().isEmpty()) {
            this.setStyle(string);
        }
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case ROW_INDEX: {
                return this.getIndex();
            }
            case COLUMN_INDEX: {
                return this.columnIndex;
            }
            case SELECTED: {
                return this.isInCellSelectionMode() ? this.isSelected() : this.getTreeTableRow().isSelected();
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    @Override
    public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
        switch (accessibleAction) {
            case REQUEST_FOCUS: {
                TreeTableView.TreeTableViewFocusModel<S> treeTableViewFocusModel;
                TreeTableView<S> treeTableView = this.getTreeTableView();
                if (treeTableView == null || (treeTableViewFocusModel = treeTableView.getFocusModel()) == null) break;
                treeTableViewFocusModel.focus(this.getIndex(), this.getTableColumn());
                break;
            }
            default: {
                super.executeAccessibleAction(accessibleAction, objectArray);
            }
        }
    }
}

