/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.binding;

import com.sun.javafx.binding.StringFormatter;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.MapBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.value.ObservableMapValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public abstract class MapExpression<K, V>
implements ObservableMapValue<K, V> {
    private static final ObservableMap EMPTY_MAP = new EmptyObservableMap();

    @Override
    public ObservableMap<K, V> getValue() {
        return (ObservableMap)this.get();
    }

    public static <K, V> MapExpression<K, V> mapExpression(final ObservableMapValue<K, V> observableMapValue) {
        if (observableMapValue == null) {
            throw new NullPointerException("Map must be specified.");
        }
        return observableMapValue instanceof MapExpression ? (MapExpression<K, V>)observableMapValue : new MapBinding<K, V>(){
            {
                super.bind(observableMapValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableMapValue);
            }

            @Override
            protected ObservableMap<K, V> computeValue() {
                return (ObservableMap)observableMapValue.get();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableMapValue);
            }
        };
    }

    public int getSize() {
        return this.size();
    }

    public abstract ReadOnlyIntegerProperty sizeProperty();

    public abstract ReadOnlyBooleanProperty emptyProperty();

    public ObjectBinding<V> valueAt(K k) {
        return Bindings.valueAt(this, k);
    }

    public ObjectBinding<V> valueAt(ObservableValue<K> observableValue) {
        return Bindings.valueAt(this, observableValue);
    }

    public BooleanBinding isEqualTo(ObservableMap<?, ?> observableMap) {
        return Bindings.equal(this, observableMap);
    }

    public BooleanBinding isNotEqualTo(ObservableMap<?, ?> observableMap) {
        return Bindings.notEqual(this, observableMap);
    }

    public BooleanBinding isNull() {
        return Bindings.isNull(this);
    }

    public BooleanBinding isNotNull() {
        return Bindings.isNotNull(this);
    }

    public StringBinding asString() {
        return (StringBinding)StringFormatter.convert(this);
    }

    @Override
    public int size() {
        ObservableMap observableMap = (ObservableMap)this.get();
        return observableMap == null ? EMPTY_MAP.size() : observableMap.size();
    }

    @Override
    public boolean isEmpty() {
        ObservableMap observableMap = (ObservableMap)this.get();
        return observableMap == null ? EMPTY_MAP.isEmpty() : observableMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object object) {
        ObservableMap observableMap = (ObservableMap)this.get();
        return observableMap == null ? EMPTY_MAP.containsKey(object) : observableMap.containsKey(object);
    }

    @Override
    public boolean containsValue(Object object) {
        ObservableMap observableMap = (ObservableMap)this.get();
        return observableMap == null ? EMPTY_MAP.containsValue(object) : observableMap.containsValue(object);
    }

    @Override
    public V put(K k, V v) {
        ObservableMap observableMap = (ObservableMap)this.get();
        return observableMap == null ? EMPTY_MAP.put(k, v) : observableMap.put(k, v);
    }

    @Override
    public V remove(Object object) {
        ObservableMap observableMap = (ObservableMap)this.get();
        return observableMap == null ? EMPTY_MAP.remove(object) : observableMap.remove(object);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        ObservableMap observableMap = (ObservableMap)this.get();
        if (observableMap == null) {
            EMPTY_MAP.putAll(map);
        } else {
            observableMap.putAll(map);
        }
    }

    @Override
    public void clear() {
        ObservableMap observableMap = (ObservableMap)this.get();
        if (observableMap == null) {
            EMPTY_MAP.clear();
        } else {
            observableMap.clear();
        }
    }

    @Override
    public Set<K> keySet() {
        ObservableMap observableMap = (ObservableMap)this.get();
        return observableMap == null ? EMPTY_MAP.keySet() : observableMap.keySet();
    }

    @Override
    public Collection<V> values() {
        ObservableMap observableMap = (ObservableMap)this.get();
        return observableMap == null ? EMPTY_MAP.values() : observableMap.values();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        ObservableMap observableMap = (ObservableMap)this.get();
        return observableMap == null ? EMPTY_MAP.entrySet() : observableMap.entrySet();
    }

    @Override
    public V get(Object object) {
        ObservableMap observableMap = (ObservableMap)this.get();
        return observableMap == null ? EMPTY_MAP.get(object) : observableMap.get(object);
    }

    private static class EmptyObservableMap<K, V>
    extends AbstractMap<K, V>
    implements ObservableMap<K, V> {
        private EmptyObservableMap() {
        }

        @Override
        public Set<Map.Entry<K, V>> entrySet() {
            return Collections.emptySet();
        }

        @Override
        public void addListener(MapChangeListener<? super K, ? super V> mapChangeListener) {
        }

        @Override
        public void removeListener(MapChangeListener<? super K, ? super V> mapChangeListener) {
        }

        @Override
        public void addListener(InvalidationListener invalidationListener) {
        }

        @Override
        public void removeListener(InvalidationListener invalidationListener) {
        }
    }
}

