/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.cell;

import java.util.Map;
import javafx.beans.NamedArg;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyFloatWrapper;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyLongWrapper;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class MapValueFactory<T>
implements Callback<TableColumn.CellDataFeatures<Map, T>, ObservableValue<T>> {
    private final Object key;

    public MapValueFactory(@NamedArg(value="key") Object object) {
        this.key = object;
    }

    @Override
    public ObservableValue<T> call(TableColumn.CellDataFeatures<Map, T> cellDataFeatures) {
        Map map = cellDataFeatures.getValue();
        Object v = map.get(this.key);
        if (v instanceof ObservableValue) {
            return (ObservableValue)v;
        }
        if (v instanceof Boolean) {
            return new ReadOnlyBooleanWrapper((Boolean)v);
        }
        if (v instanceof Integer) {
            return new ReadOnlyIntegerWrapper((Integer)v);
        }
        if (v instanceof Float) {
            return new ReadOnlyFloatWrapper(((Float)v).floatValue());
        }
        if (v instanceof Long) {
            return new ReadOnlyLongWrapper((Long)v);
        }
        if (v instanceof Double) {
            return new ReadOnlyDoubleWrapper((Double)v);
        }
        if (v instanceof String) {
            return new ReadOnlyStringWrapper((String)v);
        }
        return new ReadOnlyObjectWrapper(v);
    }
}

