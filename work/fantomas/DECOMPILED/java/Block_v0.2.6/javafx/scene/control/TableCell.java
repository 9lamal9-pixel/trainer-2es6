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
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.ControlUtils;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableCellSkin;

public class TableCell<S, T>
extends IndexedCell<T> {
    boolean lockItemOnEdit = false;
    private boolean itemDirty = false;
    private ListChangeListener<TablePosition> selectedListener = change -> {
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
    private ListChangeListener<TableColumn<S, ?>> visibleLeafColumnsListener = change -> this.updateColumnIndex();
    private ListChangeListener<String> columnStyleClassListener = change -> {
        while (change.next()) {
            if (change.wasRemoved()) {
                this.getStyleClass().removeAll(change.getRemoved());
            }
            if (!change.wasAdded()) continue;
            this.getStyleClass().addAll(change.getAddedSubList());
        }
    };
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
    private final WeakListChangeListener<TablePosition> weakSelectedListener = new WeakListChangeListener<TablePosition>(this.selectedListener);
    private final WeakInvalidationListener weakFocusedListener = new WeakInvalidationListener(this.focusedListener);
    private final WeakInvalidationListener weaktableRowUpdateObserver = new WeakInvalidationListener(this.tableRowUpdateObserver);
    private final WeakInvalidationListener weakEditingListener = new WeakInvalidationListener(this.editingListener);
    private final WeakInvalidationListener weakColumnStyleListener = new WeakInvalidationListener(this.columnStyleListener);
    private final WeakInvalidationListener weakColumnIdListener = new WeakInvalidationListener(this.columnIdListener);
    private final WeakListChangeListener<TableColumn<S, ?>> weakVisibleLeafColumnsListener = new WeakListChangeListener(this.visibleLeafColumnsListener);
    private final WeakListChangeListener<String> weakColumnStyleClassListener = new WeakListChangeListener<String>(this.columnStyleClassListener);
    private ReadOnlyObjectWrapper<TableColumn<S, T>> tableColumn = new ReadOnlyObjectWrapper<TableColumn<S, T>>(){

        @Override
        protected void invalidated() {
            TableCell.this.updateColumnIndex();
        }

        @Override
        public Object getBean() {
            return TableCell.this;
        }

        @Override
        public String getName() {
            return "tableColumn";
        }
    };
    private ReadOnlyObjectWrapper<TableView<S>> tableView;
    private ReadOnlyObjectWrapper<TableRow<S>> tableRow = new ReadOnlyObjectWrapper(this, "tableRow");
    private boolean isLastVisibleColumn = false;
    private int columnIndex = -1;
    private boolean updateEditingIndex = true;
    private ObservableValue<T> currentObservableValue = null;
    private boolean isFirstRun = true;
    private WeakReference<S> oldRowItemRef;
    private static final String DEFAULT_STYLE_CLASS = "table-cell";
    private static final PseudoClass PSEUDO_CLASS_LAST_VISIBLE = PseudoClass.getPseudoClass("last-visible");

    public TableCell() {
        this.getStyleClass().addAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.setAccessibleRole(AccessibleRole.TABLE_CELL);
        this.updateColumnIndex();
    }

    public final ReadOnlyObjectProperty<TableColumn<S, T>> tableColumnProperty() {
        return this.tableColumn.getReadOnlyProperty();
    }

    private void setTableColumn(TableColumn<S, T> tableColumn) {
        this.tableColumn.set(tableColumn);
    }

    public final TableColumn<S, T> getTableColumn() {
        return (TableColumn)this.tableColumn.get();
    }

    private void setTableView(TableView<S> tableView) {
        this.tableViewPropertyImpl().set(tableView);
    }

    public final TableView<S> getTableView() {
        return this.tableView == null ? null : (TableView)this.tableView.get();
    }

    public final ReadOnlyObjectProperty<TableView<S>> tableViewProperty() {
        return this.tableViewPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<TableView<S>> tableViewPropertyImpl() {
        if (this.tableView == null) {
            this.tableView = new ReadOnlyObjectWrapper<TableView<S>>(){
                private WeakReference<TableView<S>> weakTableViewRef;

                @Override
                protected void invalidated() {
                    if (this.weakTableViewRef != null) {
                        TableCell.this.cleanUpTableViewListeners((TableView)this.weakTableViewRef.get());
                    }
                    if (this.get() != null) {
                        TableView.TableViewFocusModel tableViewFocusModel;
                        TableView.TableViewSelectionModel tableViewSelectionModel = ((TableView)this.get()).getSelectionModel();
                        if (tableViewSelectionModel != null) {
                            tableViewSelectionModel.getSelectedCells().addListener(TableCell.this.weakSelectedListener);
                        }
                        if ((tableViewFocusModel = ((TableView)this.get()).getFocusModel()) != null) {
                            tableViewFocusModel.focusedCellProperty().addListener(TableCell.this.weakFocusedListener);
                        }
                        ((TableView)this.get()).editingCellProperty().addListener(TableCell.this.weakEditingListener);
                        ((TableView)this.get()).getVisibleLeafColumns().addListener(TableCell.this.weakVisibleLeafColumnsListener);
                        this.weakTableViewRef = new WeakReference<TableView>((TableView)this.get());
                    }
                    TableCell.this.updateColumnIndex();
                }

                @Override
                public Object getBean() {
                    return TableCell.this;
                }

                @Override
                public String getName() {
                    return "tableView";
                }
            };
        }
        return this.tableView;
    }

    private void setTableRow(TableRow<S> tableRow) {
        this.tableRow.set(tableRow);
    }

    public final TableRow<S> getTableRow() {
        return (TableRow)this.tableRow.get();
    }

    public final ReadOnlyObjectProperty<TableRow<S>> tableRowProperty() {
        return this.tableRow;
    }

    @Override
    public void startEdit() {
        TableView<S> tableView = this.getTableView();
        TableColumn<S, T> tableColumn = this.getTableColumn();
        if (!this.isEditable() || tableView != null && !tableView.isEditable() || tableColumn != null && !this.getTableColumn().isEditable()) {
            return;
        }
        if (!this.lockItemOnEdit) {
            this.updateItem(-1);
        }
        super.startEdit();
        if (tableColumn != null) {
            TableColumn.CellEditEvent<S, Object> cellEditEvent = new TableColumn.CellEditEvent<S, Object>(tableView, tableView.getEditingCell(), TableColumn.editStartEvent(), null);
            Event.fireEvent(tableColumn, cellEditEvent);
        }
    }

    @Override
    public void commitEdit(T t) {
        if (!this.isEditing()) {
            return;
        }
        TableView tableView = this.getTableView();
        if (tableView != null) {
            TableColumn.CellEditEvent cellEditEvent = new TableColumn.CellEditEvent(tableView, tableView.getEditingCell(), TableColumn.editCommitEvent(), t);
            Event.fireEvent(this.getTableColumn(), cellEditEvent);
        }
        super.commitEdit(t);
        this.updateItem(t, false);
        if (tableView != null) {
            tableView.edit(-1, null);
            ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(tableView);
        }
    }

    @Override
    public void cancelEdit() {
        if (!this.isEditing()) {
            return;
        }
        TableView tableView = this.getTableView();
        super.cancelEdit();
        if (tableView != null) {
            TablePosition<S, ?> tablePosition = tableView.getEditingCell();
            if (this.updateEditingIndex) {
                tableView.edit(-1, null);
            }
            ControlUtils.requestFocusOnControlOnlyIfCurrentFocusOwnerIsChild(tableView);
            TableColumn.CellEditEvent<S, Object> cellEditEvent = new TableColumn.CellEditEvent<S, Object>(tableView, tablePosition, TableColumn.editCancelEvent(), null);
            Event.fireEvent(this.getTableColumn(), cellEditEvent);
        }
    }

    @Override
    public void updateSelected(boolean bl) {
        if (this.getTableRow() == null || this.getTableRow().isEmpty()) {
            return;
        }
        this.setSelected(bl);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TableCellSkin(this);
    }

    private void cleanUpTableViewListeners(TableView<S> tableView) {
        if (tableView != null) {
            TableView.TableViewFocusModel<S> tableViewFocusModel;
            TableView.TableViewSelectionModel<S> tableViewSelectionModel = tableView.getSelectionModel();
            if (tableViewSelectionModel != null) {
                tableViewSelectionModel.getSelectedCells().removeListener(this.weakSelectedListener);
            }
            if ((tableViewFocusModel = tableView.getFocusModel()) != null) {
                tableViewFocusModel.focusedCellProperty().removeListener(this.weakFocusedListener);
            }
            tableView.editingCellProperty().removeListener(this.weakEditingListener);
            tableView.getVisibleLeafColumns().removeListener(this.weakVisibleLeafColumnsListener);
        }
    }

    @Override
    void indexChanged(int n, int n2) {
        super.indexChanged(n, n2);
        this.updateItem(n);
        this.updateSelection();
        this.updateFocus();
        this.updateEditing();
    }

    private void updateColumnIndex() {
        TableView<S> tableView = this.getTableView();
        TableColumn<S, T> tableColumn = this.getTableColumn();
        this.columnIndex = tableView == null || tableColumn == null ? -1 : tableView.getVisibleLeafIndex(tableColumn);
        this.isLastVisibleColumn = this.getTableColumn() != null && this.columnIndex != -1 && this.columnIndex == this.getTableView().getVisibleLeafColumns().size() - 1;
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
        TableView<S> tableView = this.getTableView();
        if (this.getIndex() == -1 || tableView == null) {
            return;
        }
        TableView.TableViewSelectionModel<S> tableViewSelectionModel = tableView.getSelectionModel();
        if (tableViewSelectionModel == null) {
            this.updateSelected(false);
            return;
        }
        boolean bl2 = ((TableSelectionModel)tableViewSelectionModel).isSelected(this.getIndex(), this.getTableColumn());
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
        TableView<S> tableView = this.getTableView();
        TableRow<S> tableRow = this.getTableRow();
        int n = this.getIndex();
        if (n == -1 || tableView == null || tableRow == null) {
            return;
        }
        TableView.TableViewFocusModel<S> tableViewFocusModel = tableView.getFocusModel();
        if (tableViewFocusModel == null) {
            this.setFocused(false);
            return;
        }
        this.setFocused(tableViewFocusModel.isFocused(n, this.getTableColumn()));
    }

    private void updateEditing() {
        if (this.getIndex() == -1 || this.getTableView() == null) {
            return;
        }
        TablePosition<S, ?> tablePosition = this.getTableView().getEditingCell();
        boolean bl = this.match(tablePosition);
        if (bl && !this.isEditing()) {
            this.startEdit();
        } else if (!bl && this.isEditing()) {
            this.updateEditingIndex = false;
            this.cancelEdit();
            this.updateEditingIndex = true;
        }
    }

    private boolean match(TablePosition<S, ?> tablePosition) {
        return tablePosition != null && tablePosition.getRow() == this.getIndex() && tablePosition.getTableColumn() == this.getTableColumn();
    }

    private boolean isInCellSelectionMode() {
        TableView<S> tableView = this.getTableView();
        if (tableView == null) {
            return false;
        }
        TableView.TableViewSelectionModel<S> tableViewSelectionModel = tableView.getSelectionModel();
        return tableViewSelectionModel != null && tableViewSelectionModel.isCellSelectionEnabled();
    }

    private void updateItem(int n) {
        Object t;
        block8: {
            Object t2;
            block7: {
                Object object;
                boolean bl;
                TableView<S> tableView;
                if (this.currentObservableValue != null) {
                    this.currentObservableValue.removeListener(this.weaktableRowUpdateObserver);
                }
                ObservableList<Object> observableList = (tableView = this.getTableView()) == null ? FXCollections.emptyObservableList() : tableView.getItems();
                TableColumn<int, T> tableColumn = this.getTableColumn();
                int n2 = observableList == null ? -1 : observableList.size();
                int n3 = this.getIndex();
                boolean bl2 = this.isEmpty();
                Object t3 = this.getItem();
                TableRow<S> tableRow = this.getTableRow();
                t = tableRow == null ? null : (Object)tableRow.getItem();
                boolean bl3 = bl = n3 >= n2;
                if (bl || n3 < 0 || this.columnIndex < 0 || !this.isVisible() || tableColumn == null || !tableColumn.isVisible()) {
                    if (!bl2 && t3 != null || this.isFirstRun || bl) {
                        this.updateItem(null, true);
                        this.isFirstRun = false;
                    }
                    return;
                }
                this.currentObservableValue = tableColumn.getCellObservableValue(n3);
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

    public final void updateTableView(TableView tableView) {
        this.setTableView(tableView);
    }

    public final void updateTableRow(TableRow tableRow) {
        this.setTableRow(tableRow);
    }

    public final void updateTableColumn(TableColumn tableColumn) {
        TableColumn<S, T> tableColumn2 = this.getTableColumn();
        if (tableColumn2 != null) {
            tableColumn2.getStyleClass().removeListener(this.weakColumnStyleClassListener);
            this.getStyleClass().removeAll((Collection<?>)tableColumn2.getStyleClass());
            tableColumn2.idProperty().removeListener(this.weakColumnIdListener);
            tableColumn2.styleProperty().removeListener(this.weakColumnStyleListener);
            String string = this.getId();
            String string2 = this.getStyle();
            if (string != null && string.equals(tableColumn2.getId())) {
                this.setId(null);
            }
            if (string2 != null && string2.equals(tableColumn2.getStyle())) {
                this.setStyle("");
            }
        }
        this.setTableColumn(tableColumn);
        if (tableColumn != null) {
            this.getStyleClass().addAll((Collection<String>)tableColumn.getStyleClass());
            tableColumn.getStyleClass().addListener(this.weakColumnStyleClassListener);
            tableColumn.idProperty().addListener(this.weakColumnIdListener);
            tableColumn.styleProperty().addListener(this.weakColumnStyleListener);
            this.possiblySetId(tableColumn.getId());
            this.possiblySetStyle(tableColumn.getStyle());
        }
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
                return this.isInCellSelectionMode() ? this.isSelected() : this.getTableRow().isSelected();
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    @Override
    public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
        switch (accessibleAction) {
            case REQUEST_FOCUS: {
                TableView.TableViewFocusModel<S> tableViewFocusModel;
                TableView<S> tableView = this.getTableView();
                if (tableView == null || (tableViewFocusModel = tableView.getFocusModel()) == null) break;
                tableViewFocusModel.focus(this.getIndex(), this.getTableColumn());
                break;
            }
            default: {
                super.executeAccessibleAction(accessibleAction, objectArray);
            }
        }
    }
}

