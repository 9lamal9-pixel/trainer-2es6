/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import java.lang.ref.WeakReference;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Skin;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableRowSkin;

public class TableRow<T>
extends IndexedCell<T> {
    private ListChangeListener<Integer> selectedListener = change -> this.updateSelection();
    private final InvalidationListener focusedListener = observable -> this.updateFocus();
    private final InvalidationListener editingListener = observable -> this.updateEditing();
    private final WeakListChangeListener<Integer> weakSelectedListener = new WeakListChangeListener<Integer>(this.selectedListener);
    private final WeakInvalidationListener weakFocusedListener = new WeakInvalidationListener(this.focusedListener);
    private final WeakInvalidationListener weakEditingListener = new WeakInvalidationListener(this.editingListener);
    private ReadOnlyObjectWrapper<TableView<T>> tableView;
    private boolean isFirstRun = true;
    private static final String DEFAULT_STYLE_CLASS = "table-row-cell";

    public TableRow() {
        this.getStyleClass().addAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.setAccessibleRole(AccessibleRole.TABLE_ROW);
    }

    private void setTableView(TableView<T> tableView) {
        this.tableViewPropertyImpl().set(tableView);
    }

    public final TableView<T> getTableView() {
        return this.tableView == null ? null : (TableView)this.tableView.get();
    }

    public final ReadOnlyObjectProperty<TableView<T>> tableViewProperty() {
        return this.tableViewPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<TableView<T>> tableViewPropertyImpl() {
        if (this.tableView == null) {
            this.tableView = new ReadOnlyObjectWrapper<TableView<T>>(){
                private WeakReference<TableView<T>> weakTableViewRef;

                @Override
                protected void invalidated() {
                    TableView.TableViewFocusModel tableViewFocusModel;
                    TableView.TableViewSelectionModel tableViewSelectionModel;
                    TableView tableView;
                    if (this.weakTableViewRef != null) {
                        tableView = (TableView)this.weakTableViewRef.get();
                        if (tableView != null) {
                            tableViewSelectionModel = tableView.getSelectionModel();
                            if (tableViewSelectionModel != null) {
                                tableViewSelectionModel.getSelectedIndices().removeListener(TableRow.this.weakSelectedListener);
                            }
                            if ((tableViewFocusModel = tableView.getFocusModel()) != null) {
                                tableViewFocusModel.focusedCellProperty().removeListener(TableRow.this.weakFocusedListener);
                            }
                            tableView.editingCellProperty().removeListener(TableRow.this.weakEditingListener);
                        }
                        this.weakTableViewRef = null;
                    }
                    if ((tableView = TableRow.this.getTableView()) != null) {
                        tableViewSelectionModel = tableView.getSelectionModel();
                        if (tableViewSelectionModel != null) {
                            tableViewSelectionModel.getSelectedIndices().addListener(TableRow.this.weakSelectedListener);
                        }
                        if ((tableViewFocusModel = tableView.getFocusModel()) != null) {
                            tableViewFocusModel.focusedCellProperty().addListener(TableRow.this.weakFocusedListener);
                        }
                        tableView.editingCellProperty().addListener(TableRow.this.weakEditingListener);
                        this.weakTableViewRef = new WeakReference<TableView>((TableView)this.get());
                    }
                }

                @Override
                public Object getBean() {
                    return TableRow.this;
                }

                @Override
                public String getName() {
                    return "tableView";
                }
            };
        }
        return this.tableView;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TableRowSkin(this);
    }

    @Override
    void indexChanged(int n, int n2) {
        super.indexChanged(n, n2);
        this.updateItem(n);
        this.updateSelection();
        this.updateFocus();
    }

    private void updateItem(int n) {
        TableView<T> tableView = this.getTableView();
        if (tableView == null || tableView.getItems() == null) {
            return;
        }
        ObservableList<T> observableList = tableView.getItems();
        int n2 = observableList == null ? -1 : observableList.size();
        int n3 = this.getIndex();
        boolean bl = n3 >= 0 && n3 < n2;
        Object t = this.getItem();
        boolean bl2 = this.isEmpty();
        if (bl) {
            Object e = observableList.get(n3);
            if (n != n3 || this.isItemChanged(t, e)) {
                this.updateItem(e, false);
            }
        } else if (!bl2 && t != null || this.isFirstRun) {
            this.updateItem(null, true);
            this.isFirstRun = false;
        }
    }

    private void updateSelection() {
        if (this.getIndex() == -1) {
            return;
        }
        TableView<T> tableView = this.getTableView();
        boolean bl = tableView != null && tableView.getSelectionModel() != null && !tableView.getSelectionModel().isCellSelectionEnabled() && tableView.getSelectionModel().isSelected(this.getIndex());
        this.updateSelected(bl);
    }

    private void updateFocus() {
        if (this.getIndex() == -1) {
            return;
        }
        TableView<T> tableView = this.getTableView();
        if (tableView == null) {
            return;
        }
        TableView.TableViewSelectionModel<T> tableViewSelectionModel = tableView.getSelectionModel();
        TableView.TableViewFocusModel<T> tableViewFocusModel = tableView.getFocusModel();
        if (tableViewSelectionModel == null || tableViewFocusModel == null) {
            return;
        }
        boolean bl = !tableViewSelectionModel.isCellSelectionEnabled() && tableViewFocusModel.isFocused(this.getIndex());
        this.setFocused(bl);
    }

    private void updateEditing() {
        boolean bl;
        if (this.getIndex() == -1) {
            return;
        }
        TableView<T> tableView = this.getTableView();
        if (tableView == null) {
            return;
        }
        TableView.TableViewSelectionModel<T> tableViewSelectionModel = tableView.getSelectionModel();
        if (tableViewSelectionModel == null || tableViewSelectionModel.isCellSelectionEnabled()) {
            return;
        }
        TablePosition<T, ?> tablePosition = tableView.getEditingCell();
        if (tablePosition != null && tablePosition.getTableColumn() != null) {
            return;
        }
        boolean bl2 = tablePosition == null ? false : (bl = tablePosition.getRow() == this.getIndex());
        if (!this.isEditing() && bl) {
            this.startEdit();
        } else if (this.isEditing() && !bl) {
            this.cancelEdit();
        }
    }

    public final void updateTableView(TableView<T> tableView) {
        this.setTableView(tableView);
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case INDEX: {
                return this.getIndex();
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }
}

