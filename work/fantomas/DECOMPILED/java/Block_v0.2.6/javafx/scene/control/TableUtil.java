/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ResizeFeaturesBase;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TreeTableColumn;

class TableUtil {
    private TableUtil() {
    }

    static void removeTableColumnListener(List<? extends TableColumnBase> list, InvalidationListener invalidationListener, InvalidationListener invalidationListener2, InvalidationListener invalidationListener3, InvalidationListener invalidationListener4) {
        if (list == null) {
            return;
        }
        for (TableColumnBase tableColumnBase : list) {
            tableColumnBase.visibleProperty().removeListener(invalidationListener);
            tableColumnBase.sortableProperty().removeListener(invalidationListener2);
            tableColumnBase.comparatorProperty().removeListener(invalidationListener4);
            if (tableColumnBase instanceof TableColumn) {
                ((TableColumn)tableColumnBase).sortTypeProperty().removeListener(invalidationListener3);
            } else if (tableColumnBase instanceof TreeTableColumn) {
                ((TreeTableColumn)tableColumnBase).sortTypeProperty().removeListener(invalidationListener3);
            }
            TableUtil.removeTableColumnListener(tableColumnBase.getColumns(), invalidationListener, invalidationListener2, invalidationListener3, invalidationListener4);
        }
    }

    static void addTableColumnListener(List<? extends TableColumnBase> list, InvalidationListener invalidationListener, InvalidationListener invalidationListener2, InvalidationListener invalidationListener3, InvalidationListener invalidationListener4) {
        if (list == null) {
            return;
        }
        for (TableColumnBase tableColumnBase : list) {
            tableColumnBase.visibleProperty().addListener(invalidationListener);
            tableColumnBase.sortableProperty().addListener(invalidationListener2);
            tableColumnBase.comparatorProperty().addListener(invalidationListener4);
            if (tableColumnBase instanceof TableColumn) {
                ((TableColumn)tableColumnBase).sortTypeProperty().addListener(invalidationListener3);
            } else if (tableColumnBase instanceof TreeTableColumn) {
                ((TreeTableColumn)tableColumnBase).sortTypeProperty().addListener(invalidationListener3);
            }
            TableUtil.addTableColumnListener(tableColumnBase.getColumns(), invalidationListener, invalidationListener2, invalidationListener3, invalidationListener4);
        }
    }

    static void removeColumnsListener(List<? extends TableColumnBase> list, ListChangeListener listChangeListener) {
        if (list == null) {
            return;
        }
        for (TableColumnBase tableColumnBase : list) {
            tableColumnBase.getColumns().removeListener(listChangeListener);
            TableUtil.removeColumnsListener(tableColumnBase.getColumns(), listChangeListener);
        }
    }

    static void addColumnsListener(List<? extends TableColumnBase> list, ListChangeListener listChangeListener) {
        if (list == null) {
            return;
        }
        for (TableColumnBase tableColumnBase : list) {
            tableColumnBase.getColumns().addListener(listChangeListener);
            TableUtil.addColumnsListener(tableColumnBase.getColumns(), listChangeListener);
        }
    }

    static void handleSortFailure(ObservableList<? extends TableColumnBase> observableList, SortEventType sortEventType, Object ... objectArray) {
        if (sortEventType == SortEventType.COLUMN_SORT_TYPE_CHANGE) {
            TableColumnBase tableColumnBase = (TableColumnBase)objectArray[0];
            TableUtil.revertSortType(tableColumnBase);
        } else if (sortEventType == SortEventType.SORT_ORDER_CHANGE) {
            ListChangeListener.Change change = (ListChangeListener.Change)objectArray[0];
            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList();
            while (change.next()) {
                if (change.wasAdded()) {
                    arrayList.addAll(change.getAddedSubList());
                }
                if (!change.wasRemoved()) continue;
                arrayList2.addAll(change.getRemoved());
            }
            observableList.removeAll(arrayList);
            observableList.addAll(arrayList2);
        } else if (sortEventType == SortEventType.COLUMN_SORTABLE_CHANGE || sortEventType == SortEventType.COLUMN_COMPARATOR_CHANGE) {
            // empty if block
        }
    }

    private static void revertSortType(TableColumnBase tableColumnBase) {
        if (tableColumnBase instanceof TableColumn) {
            TableColumn tableColumn = (TableColumn)tableColumnBase;
            TableColumn.SortType sortType = tableColumn.getSortType();
            if (sortType == TableColumn.SortType.ASCENDING) {
                tableColumn.setSortType(null);
            } else if (sortType == TableColumn.SortType.DESCENDING) {
                tableColumn.setSortType(TableColumn.SortType.ASCENDING);
            } else if (sortType == null) {
                tableColumn.setSortType(TableColumn.SortType.DESCENDING);
            }
        } else if (tableColumnBase instanceof TreeTableColumn) {
            TreeTableColumn treeTableColumn = (TreeTableColumn)tableColumnBase;
            TreeTableColumn.SortType sortType = treeTableColumn.getSortType();
            if (sortType == TreeTableColumn.SortType.ASCENDING) {
                treeTableColumn.setSortType(null);
            } else if (sortType == TreeTableColumn.SortType.DESCENDING) {
                treeTableColumn.setSortType(TreeTableColumn.SortType.ASCENDING);
            } else if (sortType == null) {
                treeTableColumn.setSortType(TreeTableColumn.SortType.DESCENDING);
            }
        }
    }

    static boolean constrainedResize(ResizeFeaturesBase resizeFeaturesBase, boolean bl, double d, List<? extends TableColumnBase<?, ?>> list) {
        double d2;
        boolean bl2;
        TableColumnBase<?, ?> tableColumnBase = resizeFeaturesBase.getColumn();
        double d3 = resizeFeaturesBase.getDelta();
        double d4 = 0.0;
        double d5 = 0.0;
        if (d == 0.0) {
            return false;
        }
        double d6 = 0.0;
        for (TableColumnBase<?, ?> tableColumnBase2 : list) {
            d6 += tableColumnBase2.getWidth();
        }
        if (Math.abs(d6 - d) > 1.0) {
            bl2 = d6 > d;
            double d7 = d;
            if (bl) {
                for (TableColumnBase<?, ?> tableColumnBase2 : list) {
                    d4 += tableColumnBase2.getMinWidth();
                    d5 += tableColumnBase2.getMaxWidth();
                }
                d5 = d5 == Double.POSITIVE_INFINITY ? Double.MAX_VALUE : (d5 == Double.NEGATIVE_INFINITY ? Double.MIN_VALUE : d5);
                for (TableColumnBase<?, ?> tableColumnBase2 : list) {
                    double d8;
                    double d9 = tableColumnBase2.getMinWidth();
                    double d10 = tableColumnBase2.getMaxWidth();
                    if (Math.abs(d4 - d5) < 1.0E-7) {
                        d8 = d9;
                    } else {
                        d2 = (d7 - d4) / (d5 - d4);
                        d8 = Math.round(d9 + d2 * (d10 - d9));
                    }
                    d2 = TableUtil.resize(tableColumnBase2, d8 - tableColumnBase2.getWidth());
                    d7 -= d8 + d2;
                    d4 -= d9;
                    d5 -= d10;
                }
                bl = false;
            } else {
                double d11 = d - d6;
                List<TableColumnBase<?, ?>> list2 = list;
                TableUtil.resizeColumns(list2, d11);
            }
        }
        if (tableColumnBase == null) {
            return false;
        }
        bl2 = d3 < 0.0;
        TableColumnBase<?, ?> tableColumnBase3 = tableColumnBase;
        while (tableColumnBase3.getColumns().size() > 0) {
            tableColumnBase3 = (TableColumnBase<?, ?>)tableColumnBase3.getColumns().get(tableColumnBase3.getColumns().size() - 1);
        }
        int n = list.indexOf(tableColumnBase3);
        int n2 = list.size() - 1;
        double d12 = d3;
        while (n2 > n && d12 != 0.0) {
            TableColumnBase<?, ?> tableColumnBase4;
            TableColumnBase<?, ?> tableColumnBase5 = list.get(n2);
            --n2;
            if (!tableColumnBase5.isResizable()) continue;
            TableColumnBase<?, ?> tableColumnBase6 = bl2 ? tableColumnBase3 : tableColumnBase5;
            TableColumnBase<?, ?> tableColumnBase7 = tableColumnBase4 = !bl2 ? tableColumnBase3 : tableColumnBase5;
            if (tableColumnBase4.getWidth() > tableColumnBase4.getPrefWidth()) {
                List<TableColumnBase<?, ?>> list3 = list.subList(n + 1, n2 + 1);
                for (int i = list3.size() - 1; i >= 0; --i) {
                    TableColumnBase<?, ?> tableColumnBase8 = list3.get(i);
                    if (!(tableColumnBase8.getWidth() < tableColumnBase8.getPrefWidth())) continue;
                    tableColumnBase4 = tableColumnBase8;
                    break;
                }
            }
            d2 = Math.min(Math.abs(d12), tableColumnBase6.getWidth() - tableColumnBase6.getMinWidth());
            double d13 = TableUtil.resize(tableColumnBase6, -d2);
            double d14 = TableUtil.resize(tableColumnBase4, d2);
            d12 += bl2 ? d2 : -d2;
        }
        return d12 == 0.0;
    }

    static double resize(TableColumnBase tableColumnBase, double d) {
        if (d == 0.0) {
            return 0.0;
        }
        if (!tableColumnBase.isResizable()) {
            return d;
        }
        boolean bl = d < 0.0;
        List<TableColumnBase<?, ?>> list = TableUtil.getResizableChildren(tableColumnBase, bl);
        if (list.size() > 0) {
            return TableUtil.resizeColumns(list, d);
        }
        double d2 = tableColumnBase.getWidth() + d;
        if (d2 > tableColumnBase.getMaxWidth()) {
            tableColumnBase.doSetWidth(tableColumnBase.getMaxWidth());
            return d2 - tableColumnBase.getMaxWidth();
        }
        if (d2 < tableColumnBase.getMinWidth()) {
            tableColumnBase.doSetWidth(tableColumnBase.getMinWidth());
            return d2 - tableColumnBase.getMinWidth();
        }
        tableColumnBase.doSetWidth(d2);
        return 0.0;
    }

    private static List<TableColumnBase<?, ?>> getResizableChildren(TableColumnBase<?, ?> tableColumnBase, boolean bl) {
        if (tableColumnBase == null || tableColumnBase.getColumns().isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList arrayList = new ArrayList();
        for (TableColumnBase tableColumnBase2 : tableColumnBase.getColumns()) {
            if (!tableColumnBase2.isVisible() || !tableColumnBase2.isResizable()) continue;
            if (bl && tableColumnBase2.getWidth() > tableColumnBase2.getMinWidth()) {
                arrayList.add(tableColumnBase2);
                continue;
            }
            if (bl || !(tableColumnBase2.getWidth() < tableColumnBase2.getMaxWidth())) continue;
            arrayList.add(tableColumnBase2);
        }
        return arrayList;
    }

    private static double resizeColumns(List<? extends TableColumnBase<?, ?>> list, double d) {
        int n = list.size();
        double d2 = d / (double)n;
        double d3 = d;
        int n2 = 0;
        boolean bl = true;
        for (TableColumnBase<?, ?> tableColumnBase : list) {
            ++n2;
            double d4 = TableUtil.resize(tableColumnBase, d2);
            d3 = d3 - d2 + d4;
            if (d4 == 0.0) continue;
            bl = false;
            d2 = d3 / (double)(n - n2);
        }
        return bl ? 0.0 : d3;
    }

    static enum SortEventType {
        SORT_ORDER_CHANGE,
        COLUMN_SORT_TYPE_CHANGE,
        COLUMN_SORTABLE_CHANGE,
        COLUMN_COMPARATOR_CHANGE;

    }
}

