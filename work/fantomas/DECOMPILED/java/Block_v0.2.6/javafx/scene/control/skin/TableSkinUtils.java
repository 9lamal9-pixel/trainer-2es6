/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.TableColumnBaseHelper;
import com.sun.javafx.scene.control.TreeTableViewBackingList;
import com.sun.javafx.scene.control.skin.Utils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ResizeFeaturesBase;
import javafx.scene.control.Skinnable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableFocusModel;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableViewSkinBase;
import javafx.scene.control.skin.TreeTableViewSkin;
import javafx.scene.layout.Region;
import javafx.util.Callback;

class TableSkinUtils {
    private TableSkinUtils() {
    }

    public static boolean resizeColumn(TableViewSkinBase<?, ?, ?, ?, ?> tableViewSkinBase, TableColumnBase<?, ?> tableColumnBase, double d) {
        if (!tableColumnBase.isResizable()) {
            return false;
        }
        Skinnable skinnable = tableViewSkinBase.getSkinnable();
        if (skinnable instanceof TableView) {
            return ((TableView)skinnable).resizeColumn((TableColumn)tableColumnBase, d);
        }
        if (skinnable instanceof TreeTableView) {
            return ((TreeTableView)skinnable).resizeColumn((TreeTableColumn)tableColumnBase, d);
        }
        return false;
    }

    public static void resizeColumnToFitContent(TableViewSkinBase<?, ?, ?, ?, ?> tableViewSkinBase, TableColumnBase<?, ?> tableColumnBase, int n) {
        if (!tableColumnBase.isResizable()) {
            return;
        }
        Skinnable skinnable = tableViewSkinBase.getSkinnable();
        if (skinnable instanceof TableView) {
            TableSkinUtils.resizeColumnToFitContent((TableView)skinnable, (TableColumn)tableColumnBase, tableViewSkinBase, n);
        } else if (skinnable instanceof TreeTableView) {
            TableSkinUtils.resizeColumnToFitContent((TreeTableView)skinnable, (TreeTableColumn)tableColumnBase, tableViewSkinBase, n);
        }
    }

    private static <T, S> void resizeColumnToFitContent(TableView<T> tableView, TableColumn<T, S> tableColumn, TableViewSkinBase tableViewSkinBase, int n) {
        Node node;
        ObservableList<T> observableList = tableView.getItems();
        if (observableList == null || observableList.isEmpty()) {
            return;
        }
        Callback<TableColumn<T, S>, TableCell<T, S>> callback = tableColumn.getCellFactory();
        if (callback == null) {
            return;
        }
        TableCell<T, S> tableCell = callback.call(tableColumn);
        if (tableCell == null) {
            return;
        }
        tableCell.getProperties().put("deferToParentPrefWidth", Boolean.TRUE);
        double d = 10.0;
        Node node2 = node = tableCell.getSkin() == null ? null : tableCell.getSkin().getNode();
        if (node instanceof Region) {
            Region region = (Region)node;
            d = region.snappedLeftInset() + region.snappedRightInset();
        }
        int n2 = n == -1 ? observableList.size() : Math.min(observableList.size(), n);
        double d2 = 0.0;
        for (int i = 0; i < n2; ++i) {
            tableCell.updateTableColumn(tableColumn);
            tableCell.updateTableView(tableView);
            tableCell.updateIndex(i);
            if ((tableCell.getText() == null || tableCell.getText().isEmpty()) && tableCell.getGraphic() == null) continue;
            tableViewSkinBase.getChildren().add(tableCell);
            tableCell.applyCss();
            d2 = Math.max(d2, tableCell.prefWidth(-1.0));
            tableViewSkinBase.getChildren().remove(tableCell);
        }
        tableCell.updateIndex(-1);
        TableColumnHeader tableColumnHeader = tableViewSkinBase.getTableHeaderRow().getColumnHeaderFor(tableColumn);
        double d3 = Utils.computeTextWidth(tableColumnHeader.label.getFont(), tableColumn.getText(), -1.0);
        Node node3 = tableColumnHeader.label.getGraphic();
        double d4 = node3 == null ? 0.0 : node3.prefWidth(-1.0) + tableColumnHeader.label.getGraphicTextGap();
        double d5 = d3 + d4 + 10.0 + tableColumnHeader.snappedLeftInset() + tableColumnHeader.snappedRightInset();
        d2 = Math.max(d2, d5);
        d2 += d;
        if (tableView.getColumnResizePolicy() == TableView.CONSTRAINED_RESIZE_POLICY && tableView.getWidth() > 0.0) {
            int n3;
            if (d2 > tableColumn.getMaxWidth()) {
                d2 = tableColumn.getMaxWidth();
            }
            if ((n3 = tableColumn.getColumns().size()) > 0) {
                TableSkinUtils.resizeColumnToFitContent(tableViewSkinBase, (TableColumnBase)tableColumn.getColumns().get(n3 - 1), n);
                return;
            }
            TableSkinUtils.resizeColumn(tableViewSkinBase, tableColumn, Math.round(d2 - tableColumn.getWidth()));
        } else {
            TableColumnBaseHelper.setWidth(tableColumn, d2);
        }
    }

    private static <T, S> void resizeColumnToFitContent(TreeTableView<T> treeTableView, TreeTableColumn<T, S> treeTableColumn, TableViewSkinBase tableViewSkinBase, int n) {
        double d;
        TreeTableRow<T> treeTableRow;
        Node node;
        TreeTableViewBackingList<T> treeTableViewBackingList = new TreeTableViewBackingList<T>(treeTableView);
        if (treeTableViewBackingList == null || treeTableViewBackingList.isEmpty()) {
            return;
        }
        Callback<TreeTableColumn<T, S>, TreeTableCell<T, S>> callback = treeTableColumn.getCellFactory();
        if (callback == null) {
            return;
        }
        TreeTableCell treeTableCell = callback.call(treeTableColumn);
        if (treeTableCell == null) {
            return;
        }
        treeTableCell.getProperties().put("deferToParentPrefWidth", Boolean.TRUE);
        double d2 = 10.0;
        Node node2 = node = treeTableCell.getSkin() == null ? null : treeTableCell.getSkin().getNode();
        if (node instanceof Region) {
            treeTableRow = (TreeTableRow<T>)node;
            d2 = treeTableRow.snappedLeftInset() + treeTableRow.snappedRightInset();
        }
        treeTableRow = new TreeTableRow<T>();
        treeTableRow.updateTreeTableView(treeTableView);
        int n2 = n == -1 ? treeTableViewBackingList.size() : Math.min(treeTableViewBackingList.size(), n);
        double d3 = 0.0;
        for (int i = 0; i < n2; ++i) {
            treeTableRow.updateIndex(i);
            treeTableRow.updateTreeItem(treeTableView.getTreeItem(i));
            treeTableCell.updateTreeTableColumn(treeTableColumn);
            treeTableCell.updateTreeTableView(treeTableView);
            treeTableCell.updateTreeTableRow(treeTableRow);
            treeTableCell.updateIndex(i);
            if ((treeTableCell.getText() == null || treeTableCell.getText().isEmpty()) && treeTableCell.getGraphic() == null) continue;
            tableViewSkinBase.getChildren().add(treeTableCell);
            treeTableCell.applyCss();
            d = treeTableCell.prefWidth(-1.0);
            d3 = Math.max(d3, d);
            tableViewSkinBase.getChildren().remove(treeTableCell);
        }
        treeTableCell.updateIndex(-1);
        TableColumnHeader tableColumnHeader = tableViewSkinBase.getTableHeaderRow().getColumnHeaderFor(treeTableColumn);
        d = Utils.computeTextWidth(tableColumnHeader.label.getFont(), treeTableColumn.getText(), -1.0);
        Node node3 = tableColumnHeader.label.getGraphic();
        double d4 = node3 == null ? 0.0 : node3.prefWidth(-1.0) + tableColumnHeader.label.getGraphicTextGap();
        double d5 = d + d4 + 10.0 + tableColumnHeader.snappedLeftInset() + tableColumnHeader.snappedRightInset();
        d3 = Math.max(d3, d5);
        d3 += d2;
        if (treeTableView.getColumnResizePolicy() == TreeTableView.CONSTRAINED_RESIZE_POLICY && treeTableView.getWidth() > 0.0) {
            int n3;
            if (d3 > treeTableColumn.getMaxWidth()) {
                d3 = treeTableColumn.getMaxWidth();
            }
            if ((n3 = treeTableColumn.getColumns().size()) > 0) {
                TableSkinUtils.resizeColumnToFitContent(tableViewSkinBase, (TableColumnBase)treeTableColumn.getColumns().get(n3 - 1), n);
                return;
            }
            TableSkinUtils.resizeColumn(tableViewSkinBase, treeTableColumn, Math.round(d3 - treeTableColumn.getWidth()));
        } else {
            TableColumnBaseHelper.setWidth(treeTableColumn, d3);
        }
    }

    public static ObjectProperty<Callback<ResizeFeaturesBase, Boolean>> columnResizePolicyProperty(TableViewSkinBase<?, ?, ?, ?, ?> tableViewSkinBase) {
        Skinnable skinnable = tableViewSkinBase.getSkinnable();
        if (skinnable instanceof TableView) {
            return ((TableView)skinnable).columnResizePolicyProperty();
        }
        if (skinnable instanceof TreeTableView) {
            return ((TreeTableView)skinnable).columnResizePolicyProperty();
        }
        return null;
    }

    public static BooleanProperty tableMenuButtonVisibleProperty(TableViewSkinBase<?, ?, ?, ?, ?> tableViewSkinBase) {
        Skinnable skinnable = tableViewSkinBase.getSkinnable();
        if (skinnable instanceof TableView) {
            return ((TableView)skinnable).tableMenuButtonVisibleProperty();
        }
        if (skinnable instanceof TreeTableView) {
            return ((TreeTableView)skinnable).tableMenuButtonVisibleProperty();
        }
        return null;
    }

    public static ObjectProperty<Node> placeholderProperty(TableViewSkinBase<?, ?, ?, ?, ?> tableViewSkinBase) {
        Skinnable skinnable = tableViewSkinBase.getSkinnable();
        if (skinnable instanceof TableView) {
            return ((TableView)skinnable).placeholderProperty();
        }
        if (skinnable instanceof TreeTableView) {
            return ((TreeTableView)skinnable).placeholderProperty();
        }
        return null;
    }

    public static <C extends Control, I extends IndexedCell<?>> ObjectProperty<Callback<C, I>> rowFactoryProperty(TableViewSkinBase<?, ?, C, I, ?> tableViewSkinBase) {
        Skinnable skinnable = tableViewSkinBase.getSkinnable();
        if (skinnable instanceof TableView) {
            return ((TableView)skinnable).rowFactoryProperty();
        }
        if (skinnable instanceof TreeTableView) {
            return ((TreeTableView)skinnable).rowFactoryProperty();
        }
        return null;
    }

    public static ObservableList<TableColumnBase<?, ?>> getSortOrder(TableViewSkinBase<?, ?, ?, ?, ?> tableViewSkinBase) {
        Skinnable skinnable = tableViewSkinBase.getSkinnable();
        if (skinnable instanceof TableView) {
            return ((TableView)skinnable).getSortOrder();
        }
        if (skinnable instanceof TreeTableView) {
            return ((TreeTableView)skinnable).getSortOrder();
        }
        return FXCollections.emptyObservableList();
    }

    public static ObservableList<TableColumnBase<?, ?>> getColumns(TableViewSkinBase<?, ?, ?, ?, ?> tableViewSkinBase) {
        Skinnable skinnable = tableViewSkinBase.getSkinnable();
        if (skinnable instanceof TableView) {
            return ((TableView)skinnable).getColumns();
        }
        if (skinnable instanceof TreeTableView) {
            return ((TreeTableView)skinnable).getColumns();
        }
        return FXCollections.emptyObservableList();
    }

    public static <T> TableSelectionModel<T> getSelectionModel(TableViewSkinBase<?, ?, ?, ?, ?> tableViewSkinBase) {
        Skinnable skinnable = tableViewSkinBase.getSkinnable();
        if (skinnable instanceof TableView) {
            return ((TableView)skinnable).getSelectionModel();
        }
        if (skinnable instanceof TreeTableView) {
            return ((TreeTableView)skinnable).getSelectionModel();
        }
        return null;
    }

    public static <T> TableFocusModel<T, ?> getFocusModel(TableViewSkinBase<T, ?, ?, ?, ?> tableViewSkinBase) {
        Skinnable skinnable = tableViewSkinBase.getSkinnable();
        if (skinnable instanceof TableView) {
            return ((TableView)skinnable).getFocusModel();
        }
        if (skinnable instanceof TreeTableView) {
            return ((TreeTableView)skinnable).getFocusModel();
        }
        return null;
    }

    public static <T, TC extends TableColumnBase<T, ?>> TablePositionBase<? extends TC> getFocusedCell(TableViewSkinBase<?, T, ?, ?, TC> tableViewSkinBase) {
        Skinnable skinnable = tableViewSkinBase.getSkinnable();
        if (skinnable instanceof TableView) {
            return ((TableView)skinnable).getFocusModel().getFocusedCell();
        }
        if (skinnable instanceof TreeTableView) {
            return ((TreeTableView)skinnable).getFocusModel().getFocusedCell();
        }
        return null;
    }

    public static <TC extends TableColumnBase<?, ?>> ObservableList<TC> getVisibleLeafColumns(TableViewSkinBase<?, ?, ?, ?, TC> tableViewSkinBase) {
        Skinnable skinnable = tableViewSkinBase.getSkinnable();
        if (skinnable instanceof TableView) {
            return ((TableView)skinnable).getVisibleLeafColumns();
        }
        if (skinnable instanceof TreeTableView) {
            return ((TreeTableView)skinnable).getVisibleLeafColumns();
        }
        return FXCollections.emptyObservableList();
    }

    public static int getVisibleLeafIndex(TableViewSkinBase<?, ?, ?, ?, ?> tableViewSkinBase, TableColumnBase tableColumnBase) {
        Skinnable skinnable = tableViewSkinBase.getSkinnable();
        if (skinnable instanceof TableView) {
            return ((TableView)skinnable).getVisibleLeafIndex((TableColumn)tableColumnBase);
        }
        if (skinnable instanceof TreeTableView) {
            return ((TreeTableView)skinnable).getVisibleLeafIndex((TreeTableColumn)tableColumnBase);
        }
        return -1;
    }

    public static <T, TC extends TableColumnBase<T, ?>> TC getVisibleLeafColumn(TableViewSkinBase<?, T, ?, ?, TC> tableViewSkinBase, int n) {
        Skinnable skinnable = tableViewSkinBase.getSkinnable();
        if (skinnable instanceof TableView) {
            return (TC)((TableView)skinnable).getVisibleLeafColumn(n);
        }
        if (skinnable instanceof TreeTableView) {
            return (TC)((TreeTableView)skinnable).getVisibleLeafColumn(n);
        }
        return null;
    }

    public static <T> ObjectProperty<ObservableList<T>> itemsProperty(TableViewSkinBase<?, ?, ?, ?, ?> tableViewSkinBase) {
        Skinnable skinnable = tableViewSkinBase.getSkinnable();
        if (skinnable instanceof TableView) {
            return ((TableView)skinnable).itemsProperty();
        }
        if (skinnable instanceof TreeTableView && tableViewSkinBase instanceof TreeTableViewSkin) {
            TreeTableViewSkin treeTableViewSkin = (TreeTableViewSkin)tableViewSkinBase;
            if (treeTableViewSkin.tableBackingListProperty == null) {
                treeTableViewSkin.tableBackingList = new TreeTableViewBackingList((TreeTableView)skinnable);
                treeTableViewSkin.tableBackingListProperty = new SimpleObjectProperty(treeTableViewSkin.tableBackingList);
            }
            return treeTableViewSkin.tableBackingListProperty;
        }
        return null;
    }
}

