/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TreeTableColumn;

public abstract class TableColumnComparatorBase<S, T>
implements Comparator<S> {
    private final List<? extends TableColumnBase> columns;

    public TableColumnComparatorBase(TableColumnBase<S, T> ... tableColumnBaseArray) {
        this(Arrays.asList(tableColumnBaseArray));
    }

    public TableColumnComparatorBase(List<? extends TableColumnBase> list) {
        this.columns = new ArrayList<TableColumnBase>(list);
    }

    public List<? extends TableColumnBase> getColumns() {
        return Collections.unmodifiableList(this.columns);
    }

    @Override
    public int compare(S s, S s2) {
        for (TableColumnBase tableColumnBase : this.columns) {
            Object t;
            Object t2;
            int n;
            if (!this.isSortable(tableColumnBase) || (n = this.doCompare(tableColumnBase, t2 = tableColumnBase.getCellData(s), t = tableColumnBase.getCellData(s2))) == 0) continue;
            return n;
        }
        return 0;
    }

    public int hashCode() {
        int n = 7;
        n = 59 * n + (this.columns != null ? this.columns.hashCode() : 0);
        return n;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (this.getClass() != object.getClass()) {
            return false;
        }
        TableColumnComparatorBase tableColumnComparatorBase = (TableColumnComparatorBase)object;
        return this.columns == tableColumnComparatorBase.columns || this.columns != null && this.columns.equals(tableColumnComparatorBase.columns);
    }

    public String toString() {
        return "TableColumnComparatorBase [ columns: " + this.getColumns() + "] ";
    }

    public abstract boolean isSortable(TableColumnBase<S, T> var1);

    public abstract int doCompare(TableColumnBase<S, T> var1, T var2, T var3);

    public static final class TreeTableColumnComparator<S, T>
    extends TableColumnComparatorBase<S, T> {
        public TreeTableColumnComparator(TreeTableColumn<S, T> ... treeTableColumnArray) {
            super(Arrays.asList(treeTableColumnArray));
        }

        public TreeTableColumnComparator(List<TreeTableColumn<S, T>> list) {
            super(list);
        }

        @Override
        public boolean isSortable(TableColumnBase<S, T> tableColumnBase) {
            TreeTableColumn treeTableColumn = (TreeTableColumn)tableColumnBase;
            return treeTableColumn.getSortType() != null && treeTableColumn.isSortable();
        }

        @Override
        public int doCompare(TableColumnBase<S, T> tableColumnBase, T t, T t2) {
            TreeTableColumn treeTableColumn = (TreeTableColumn)tableColumnBase;
            Comparator<T> comparator = treeTableColumn.getComparator();
            switch (treeTableColumn.getSortType()) {
                case ASCENDING: {
                    return comparator.compare(t, t2);
                }
                case DESCENDING: {
                    return comparator.compare(t2, t);
                }
            }
            return 0;
        }
    }

    public static final class TableColumnComparator<S, T>
    extends TableColumnComparatorBase<S, T> {
        public TableColumnComparator(TableColumn<S, T> ... tableColumnArray) {
            super(Arrays.asList(tableColumnArray));
        }

        public TableColumnComparator(List<TableColumn<S, T>> list) {
            super(list);
        }

        @Override
        public boolean isSortable(TableColumnBase<S, T> tableColumnBase) {
            TableColumn tableColumn = (TableColumn)tableColumnBase;
            return tableColumn.getSortType() != null && tableColumn.isSortable();
        }

        @Override
        public int doCompare(TableColumnBase<S, T> tableColumnBase, T t, T t2) {
            TableColumn tableColumn = (TableColumn)tableColumnBase;
            Comparator<T> comparator = tableColumn.getComparator();
            switch (tableColumn.getSortType()) {
                case ASCENDING: {
                    return comparator.compare(t, t2);
                }
                case DESCENDING: {
                    return comparator.compare(t2, t);
                }
            }
            return 0;
        }
    }
}

