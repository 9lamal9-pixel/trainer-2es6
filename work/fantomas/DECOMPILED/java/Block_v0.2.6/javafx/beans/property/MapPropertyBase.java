/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import com.sun.javafx.binding.MapExpressionHelper;
import java.lang.ref.WeakReference;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.property.MapProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

public abstract class MapPropertyBase<K, V>
extends MapProperty<K, V> {
    private final MapChangeListener<K, V> mapChangeListener = change -> {
        this.invalidateProperties();
        this.invalidated();
        this.fireValueChangedEvent(change);
    };
    private ObservableMap<K, V> value;
    private ObservableValue<? extends ObservableMap<K, V>> observable = null;
    private InvalidationListener listener = null;
    private boolean valid = true;
    private MapExpressionHelper<K, V> helper = null;
    private SizeProperty size0;
    private EmptyProperty empty0;

    public MapPropertyBase() {
    }

    public MapPropertyBase(ObservableMap<K, V> observableMap) {
        this.value = observableMap;
        if (observableMap != null) {
            observableMap.addListener(this.mapChangeListener);
        }
    }

    @Override
    public ReadOnlyIntegerProperty sizeProperty() {
        if (this.size0 == null) {
            this.size0 = new SizeProperty();
        }
        return this.size0;
    }

    @Override
    public ReadOnlyBooleanProperty emptyProperty() {
        if (this.empty0 == null) {
            this.empty0 = new EmptyProperty();
        }
        return this.empty0;
    }

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

    private void invalidateProperties() {
        if (this.size0 != null) {
            this.size0.fireValueChangedEvent();
        }
        if (this.empty0 != null) {
            this.empty0.fireValueChangedEvent();
        }
    }

    private void markInvalid(ObservableMap<K, V> observableMap) {
        if (this.valid) {
            if (observableMap != null) {
                observableMap.removeListener(this.mapChangeListener);
            }
            this.valid = false;
            this.invalidateProperties();
            this.invalidated();
            this.fireValueChangedEvent();
        }
    }

    protected void invalidated() {
    }

    @Override
    public ObservableMap<K, V> get() {
        if (!this.valid) {
            this.value = this.observable == null ? this.value : this.observable.getValue();
            this.valid = true;
            if (this.value != null) {
                this.value.addListener(this.mapChangeListener);
            }
        }
        return this.value;
    }

    @Override
    public void set(ObservableMap<K, V> observableMap) {
        if (this.isBound()) {
            throw new RuntimeException((String)(this.getBean() != null && this.getName() != null ? this.getBean().getClass().getSimpleName() + "." + this.getName() + " : " : "") + "A bound value cannot be set.");
        }
        if (this.value != observableMap) {
            ObservableMap<K, V> observableMap2 = this.value;
            this.value = observableMap;
            this.markInvalid(observableMap2);
        }
    }

    @Override
    public boolean isBound() {
        return this.observable != null;
    }

    @Override
    public void bind(ObservableValue<? extends ObservableMap<K, V>> observableValue) {
        if (observableValue == null) {
            throw new NullPointerException("Cannot bind to null");
        }
        if (!observableValue.equals(this.observable)) {
            this.unbind();
            this.observable = observableValue;
            if (this.listener == null) {
                this.listener = new Listener(this);
            }
            this.observable.addListener(this.listener);
            this.markInvalid(this.value);
        }
    }

    @Override
    public void unbind() {
        if (this.observable != null) {
            this.value = this.observable.getValue();
            this.observable.removeListener(this.listener);
            this.observable = null;
        }
    }

    @Override
    public String toString() {
        Object object = this.getBean();
        String string = this.getName();
        StringBuilder stringBuilder = new StringBuilder("MapProperty [");
        if (object != null) {
            stringBuilder.append("bean: ").append(object).append(", ");
        }
        if (string != null && !string.equals("")) {
            stringBuilder.append("name: ").append(string).append(", ");
        }
        if (this.isBound()) {
            stringBuilder.append("bound, ");
            if (this.valid) {
                stringBuilder.append("value: ").append(this.get());
            } else {
                stringBuilder.append("invalid");
            }
        } else {
            stringBuilder.append("value: ").append(this.get());
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    private static class Listener<K, V>
    implements InvalidationListener,
    WeakListener {
        private final WeakReference<MapPropertyBase<K, V>> wref;

        public Listener(MapPropertyBase<K, V> mapPropertyBase) {
            this.wref = new WeakReference<MapPropertyBase<K, V>>(mapPropertyBase);
        }

        @Override
        public void invalidated(Observable observable) {
            MapPropertyBase mapPropertyBase = (MapPropertyBase)this.wref.get();
            if (mapPropertyBase == null) {
                observable.removeListener(this);
            } else {
                mapPropertyBase.markInvalid(mapPropertyBase.value);
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return this.wref.get() == null;
        }
    }

    private class EmptyProperty
    extends ReadOnlyBooleanPropertyBase {
        private EmptyProperty() {
        }

        @Override
        public boolean get() {
            return MapPropertyBase.this.isEmpty();
        }

        @Override
        public Object getBean() {
            return MapPropertyBase.this;
        }

        @Override
        public String getName() {
            return "empty";
        }

        @Override
        protected void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }
    }

    private class SizeProperty
    extends ReadOnlyIntegerPropertyBase {
        private SizeProperty() {
        }

        @Override
        public int get() {
            return MapPropertyBase.this.size();
        }

        @Override
        public Object getBean() {
            return MapPropertyBase.this;
        }

        @Override
        public String getName() {
            return "size";
        }

        @Override
        protected void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }
    }
}

