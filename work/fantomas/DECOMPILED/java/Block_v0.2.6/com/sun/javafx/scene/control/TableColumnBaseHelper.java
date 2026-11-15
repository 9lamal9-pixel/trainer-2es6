/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control;

import com.sun.javafx.util.Utils;
import javafx.scene.control.TableColumnBase;

public class TableColumnBaseHelper {
    private static TableColumnBaseAccessor tableColumnBaseAccessor;

    private TableColumnBaseHelper() {
    }

    public static void setWidth(TableColumnBase tableColumnBase, double d) {
        tableColumnBaseAccessor.setWidth(tableColumnBase, d);
    }

    public static void setTableColumnBaseAccessor(TableColumnBaseAccessor tableColumnBaseAccessor) {
        if (TableColumnBaseHelper.tableColumnBaseAccessor != null) {
            throw new IllegalStateException();
        }
        TableColumnBaseHelper.tableColumnBaseAccessor = tableColumnBaseAccessor;
    }

    static {
        Utils.forceInit(TableColumnBase.class);
    }

    public static interface TableColumnBaseAccessor {
        public void setWidth(TableColumnBase var1, double var2);
    }
}

