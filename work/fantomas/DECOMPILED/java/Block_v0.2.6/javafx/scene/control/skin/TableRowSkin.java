/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.TableRowBehavior;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Iterator;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableRowSkinBase;
import javafx.scene.control.skin.TableViewSkin;

public class TableRowSkin<T>
extends TableRowSkinBase<T, TableRow<T>, TableCell<T, ?>> {
    private TableViewSkin<T> tableViewSkin;
    private final BehaviorBase<TableRow<T>> behavior;

    public TableRowSkin(TableRow<T> tableRow) {
        super(tableRow);
        this.behavior = new TableRowBehavior<T>(tableRow);
        this.updateTableViewSkin();
        this.registerChangeListener(tableRow.tableViewProperty(), observableValue -> {
            this.updateTableViewSkin();
            int n = this.cells.size();
            for (int i = 0; i < n; ++i) {
                Node node = (Node)this.cells.get(i);
                if (!(node instanceof TableCell)) continue;
                ((TableCell)node).updateTableView(((TableRow)this.getSkinnable()).getTableView());
            }
        });
        this.setupTreeTableViewListeners();
    }

    private void setupTreeTableViewListeners() {
        TableView tableView = ((TableRow)this.getSkinnable()).getTableView();
        if (tableView == null) {
            ((TableRow)this.getSkinnable()).tableViewProperty().addListener(new InvalidationListener(){

                @Override
                public void invalidated(Observable observable) {
                    ((TableRow)TableRowSkin.this.getSkinnable()).tableViewProperty().removeListener(this);
                    TableRowSkin.this.setupTreeTableViewListeners();
                }
            });
        } else {
            DoubleProperty doubleProperty = tableView.fixedCellSizeProperty();
            if (doubleProperty != null) {
                this.registerChangeListener(doubleProperty, observableValue -> {
                    this.fixedCellSize = doubleProperty.get();
                    this.fixedCellSizeEnabled = this.fixedCellSize > 0.0;
                });
                this.fixedCellSize = doubleProperty.get();
                this.fixedCellSizeEnabled = this.fixedCellSize > 0.0;
                this.registerChangeListener(this.getVirtualFlow().widthProperty(), observableValue -> tableView.requestLayout());
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }

    @Override
    protected Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case SELECTED_ITEMS: {
                ArrayList<TableCell> arrayList = new ArrayList<TableCell>();
                int n = ((TableRow)this.getSkinnable()).getIndex();
                Iterator iterator = this.getTableView().getSelectionModel().getSelectedCells().iterator();
                if (iterator.hasNext()) {
                    TablePosition tablePosition = (TablePosition)iterator.next();
                    if (tablePosition.getRow() == n) {
                        TableCell tableCell;
                        TableColumn<T, ?> tableColumn = tablePosition.getTableColumn();
                        if (tableColumn == null) {
                            tableColumn = this.getTableView().getVisibleLeafColumn(0);
                        }
                        if ((tableCell = (TableCell)((Reference)this.cellsMap.get(tableColumn)).get()) != null) {
                            arrayList.add(tableCell);
                        }
                    }
                    return FXCollections.observableArrayList(arrayList);
                }
            }
            case CELL_AT_ROW_COLUMN: {
                int n = (Integer)objectArray[1];
                TableColumn<T, ?> tableColumn = this.getTableView().getVisibleLeafColumn(n);
                if (this.cellsMap.containsKey(tableColumn)) {
                    return ((Reference)this.cellsMap.get(tableColumn)).get();
                }
                return null;
            }
            case FOCUS_ITEM: {
                TableView.TableViewFocusModel<T> tableViewFocusModel = this.getTableView().getFocusModel();
                TablePosition tablePosition = tableViewFocusModel.getFocusedCell();
                TableColumn<T, ?> tableColumn = tablePosition.getTableColumn();
                if (tableColumn == null) {
                    tableColumn = this.getTableView().getVisibleLeafColumn(0);
                }
                if (this.cellsMap.containsKey(tableColumn)) {
                    return ((Reference)this.cellsMap.get(tableColumn)).get();
                }
                return null;
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    @Override
    protected TableCell<T, ?> createCell(TableColumnBase tableColumnBase) {
        TableColumn tableColumn = (TableColumn)tableColumnBase;
        TableCell tableCell = tableColumn.getCellFactory().call(tableColumn);
        tableCell.updateTableColumn(tableColumn);
        tableCell.updateTableView(tableColumn.getTableView());
        tableCell.updateTableRow((TableRow)this.getSkinnable());
        return tableCell;
    }

    @Override
    protected ObservableList<TableColumn<T, ?>> getVisibleLeafColumns() {
        return this.getTableView() == null ? FXCollections.emptyObservableList() : this.getTableView().getVisibleLeafColumns();
    }

    @Override
    protected void updateCell(TableCell<T, ?> tableCell, TableRow<T> tableRow) {
        tableCell.updateTableRow(tableRow);
    }

    @Override
    protected TableColumn<T, ?> getTableColumn(TableCell<T, ?> tableCell) {
        return tableCell.getTableColumn();
    }

    private TableView<T> getTableView() {
        return ((TableRow)this.getSkinnable()).getTableView();
    }

    private void updateTableViewSkin() {
        TableView tableView = ((TableRow)this.getSkinnable()).getTableView();
        if (tableView != null && tableView.getSkin() instanceof TableViewSkin) {
            this.tableViewSkin = (TableViewSkin)tableView.getSkin();
        }
    }
}

