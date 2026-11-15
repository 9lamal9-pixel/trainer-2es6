/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.behavior.TableViewBehavior;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableViewSkinBase;
import javafx.scene.input.MouseEvent;

public class TableViewSkin<T>
extends TableViewSkinBase<T, T, TableView<T>, TableRow<T>, TableColumn<T, ?>> {
    private final TableViewBehavior<T> behavior;

    public TableViewSkin(TableView<T> tableView) {
        super(tableView);
        this.behavior = new TableViewBehavior<T>(tableView);
        this.flow.setFixedCellSize(tableView.getFixedCellSize());
        this.flow.setCellFactory(virtualFlow -> this.createCell());
        EventHandler<MouseEvent> eventHandler = mouseEvent -> {
            if (tableView.getEditingCell() != null) {
                tableView.edit(-1, null);
            }
            if (tableView.isFocusTraversable()) {
                tableView.requestFocus();
            }
        };
        this.flow.getVbar().addEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler);
        this.flow.getHbar().addEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler);
        this.behavior.setOnFocusPreviousRow(() -> this.onFocusPreviousCell());
        this.behavior.setOnFocusNextRow(() -> this.onFocusNextCell());
        this.behavior.setOnMoveToFirstCell(() -> this.onMoveToFirstCell());
        this.behavior.setOnMoveToLastCell(() -> this.onMoveToLastCell());
        this.behavior.setOnScrollPageDown(bl -> this.onScrollPageDown((boolean)bl));
        this.behavior.setOnScrollPageUp(bl -> this.onScrollPageUp((boolean)bl));
        this.behavior.setOnSelectPreviousRow(() -> this.onSelectPreviousCell());
        this.behavior.setOnSelectNextRow(() -> this.onSelectNextCell());
        this.behavior.setOnSelectLeftCell(() -> this.onSelectLeftCell());
        this.behavior.setOnSelectRightCell(() -> this.onSelectRightCell());
        this.registerChangeListener(tableView.fixedCellSizeProperty(), observableValue -> this.flow.setFixedCellSize(((TableView)this.getSkinnable()).getFixedCellSize()));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case SELECTED_ITEMS: {
                ArrayList<TableRow> arrayList = new ArrayList<TableRow>();
                TableView.TableViewSelectionModel tableViewSelectionModel = ((TableView)this.getSkinnable()).getSelectionModel();
                for (TablePosition tablePosition : tableViewSelectionModel.getSelectedCells()) {
                    TableRow tableRow = (TableRow)this.flow.getPrivateCell(tablePosition.getRow());
                    if (tableRow == null) continue;
                    arrayList.add(tableRow);
                }
                return FXCollections.observableArrayList(arrayList);
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    @Override
    protected void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
        switch (accessibleAction) {
            case SHOW_ITEM: {
                Node node = (Node)objectArray[0];
                if (!(node instanceof TableCell)) break;
                TableCell tableCell = (TableCell)node;
                this.flow.scrollTo(tableCell.getIndex());
                break;
            }
            case SET_SELECTED_ITEMS: {
                TableView.TableViewSelectionModel tableViewSelectionModel;
                ObservableList observableList = (ObservableList)objectArray[0];
                if (observableList == null || (tableViewSelectionModel = ((TableView)this.getSkinnable()).getSelectionModel()) == null) break;
                tableViewSelectionModel.clearSelection();
                for (Node node : observableList) {
                    if (!(node instanceof TableCell)) continue;
                    TableCell tableCell = (TableCell)node;
                    ((TableSelectionModel)tableViewSelectionModel).select(tableCell.getIndex(), tableCell.getTableColumn());
                }
                break;
            }
            default: {
                super.executeAccessibleAction(accessibleAction, objectArray);
            }
        }
    }

    private TableRow<T> createCell() {
        TableView tableView = (TableView)this.getSkinnable();
        TableRow<Object> tableRow = tableView.getRowFactory() != null ? tableView.getRowFactory().call(tableView) : new TableRow();
        tableRow.updateTableView(tableView);
        return tableRow;
    }

    @Override
    protected int getItemCount() {
        TableView tableView = (TableView)this.getSkinnable();
        return tableView.getItems() == null ? 0 : tableView.getItems().size();
    }

    @Override
    void horizontalScroll() {
        super.horizontalScroll();
        if (((TableView)this.getSkinnable()).getFixedCellSize() > 0.0) {
            this.flow.requestCellLayout();
        }
    }
}

