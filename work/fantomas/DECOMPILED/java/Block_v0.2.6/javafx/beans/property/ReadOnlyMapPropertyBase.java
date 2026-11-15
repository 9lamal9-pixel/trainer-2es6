/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import com.sun.javafx.binding.MapExpressionHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

public abstract class ReadOnlyMapPropertyBase<K, V>
extends ReadOnlyMapProperty<K, V> {
    private MapExpressionHelper<K, V> helper;

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        this.helper = MapExpressionHelper.addListener(this.helper, this, invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        this.helper = MapExpressionHelper.removeListener(this.helper, invalidationListener);
    }

    @Override
    public void addListener(ChangeListener<? super ObservableMap<K, V>> changeListener) {
        this.helper = MapExpressionHelper.addListener(this.helper, this, changeListener);
    }

    @Override
    public void removeListener(ChangeListener<? super ObservableMap<K, V>> changeListener) {
        this.helper = MapExpressionHelper.removeListener(this.helper, changeListener);
    }

    @Override
    public void addListener(MapChangeListener<? super K, ? super V> mapChangeListener) {
        this.helper = MapExpressionHelper.addListener(this.helper, this, mapChangeListener);
    }

    @Override
    public void removeListener(MapChangeListener<? super K, ? super V> mapChangeListener) {
        this.helper = MapExpressionHelper.removeListener(this.helper, mapChangeListener);
    }

    protected void fireValueChangedEvent() {
        MapExpressionHelper.fireValueChangedEvent(this.helper);
    }

    protected void fireValueChangedEvent(MapChangeListener.Change<? extends K, ? extends V> change) {
        MapExpressionHelper.fireValueChangedEvent(this.helper, change);
    }
}

