/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import java.lang.ref.WeakReference;
import javafx.scene.control.TableColumnBase;

public abstract class TablePositionBase<TC extends TableColumnBase> {
    private final int row;
    private final WeakReference<TC> tableColumnRef;

    protected TablePositionBase(int n, TC TC) {
        this.row = n;
        this.tableColumnRef = new WeakReference<TC>(TC);
    }

    public int getRow() {
        return this.row;
    }

    public abstract int getColumn();

    public TC getTableColumn() {
        return (TC)((TableColumnBase)this.tableColumnRef.get());
    }

    public boolean equals(Object object) {
        TC TC;
        if (object == null) {
            return false;
        }
        if (this.getClass() != object.getClass()) {
            return false;
        }
        TablePositionBase tablePositionBase = (TablePositionBase)object;
        if (this.row != tablePositionBase.row) {
            return false;
        }
        TC TC2 = this.getTableColumn();
        return TC2 == (TC = tablePositionBase.getTableColumn()) || TC2 != null && TC2.equals(TC);
    }

    public int hashCode() {
        int n = 5;
        n = 79 * n + this.row;
        TC TC = this.getTableColumn();
        n = 79 * n + (TC != null ? TC.hashCode() : 0);
        return n;
    }
}

