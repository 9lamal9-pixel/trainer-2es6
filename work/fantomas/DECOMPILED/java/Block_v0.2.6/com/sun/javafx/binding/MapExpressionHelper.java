/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.binding;

import com.sun.javafx.binding.ExpressionHelperBase;
import java.util.Arrays;
import java.util.Map;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableMapValue;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

public abstract class MapExpressionHelper<K, V>
extends ExpressionHelperBase {
    protected final ObservableMapValue<K, V> observable;

    public static <K, V> MapExpressionHelper<K, V> addListener(MapExpressionHelper<K, V> mapExpressionHelper, ObservableMapValue<K, V> observableMapValue, InvalidationListener invalidationListener) {
        if (observableMapValue == null || invalidationListener == null) {
            throw new NullPointerException();
        }
        observableMapValue.getValue();
        return mapExpressionHelper == null ? new SingleInvalidation(observableMapValue, invalidationListener) : mapExpressionHelper.addListener(invalidationListener);
    }

    public static <K, V> MapExpressionHelper<K, V> removeListener(MapExpressionHelper<K, V> mapExpressionHelper, InvalidationListener invalidationListener) {
        if (invalidationListener == null) {
            throw new NullPointerException();
        }
        return mapExpressionHelper == null ? null : mapExpressionHelper.removeListener(invalidationListener);
    }

    public static <K, V> MapExpressionHelper<K, V> addListener(MapExpressionHelper<K, V> mapExpressionHelper, ObservableMapValue<K, V> observableMapValue, ChangeListener<? super ObservableMap<K, V>> changeListener) {
        if (observableMapValue == null || changeListener == null) {
            throw new NullPointerException();
        }
        return mapExpressionHelper == null ? new SingleChange(observableMapValue, changeListener) : mapExpressionHelper.addListener(changeListener);
    }

    public static <K, V> MapExpressionHelper<K, V> removeListener(MapExpressionHelper<K, V> mapExpressionHelper, ChangeListener<? super ObservableMap<K, V>> changeListener) {
        if (changeListener == null) {
            throw new NullPointerException();
        }
        return mapExpressionHelper == null ? null : mapExpressionHelper.removeListener(changeListener);
    }

    public static <K, V> MapExpressionHelper<K, V> addListener(MapExpressionHelper<K, V> mapExpressionHelper, ObservableMapValue<K, V> observableMapValue, MapChangeListener<? super K, ? super V> mapChangeListener) {
        if (observableMapValue == null || mapChangeListener == null) {
            throw new NullPointerException();
        }
        return mapExpressionHelper == null ? new SingleMapChange(observableMapValue, mapChangeListener) : mapExpressionHelper.addListener(mapChangeListener);
    }

    public static <K, V> MapExpressionHelper<K, V> removeListener(MapExpressionHelper<K, V> mapExpressionHelper, MapChangeListener<? super K, ? super V> mapChangeListener) {
        if (mapChangeListener == null) {
            throw new NullPointerException();
        }
        return mapExpressionHelper == null ? null : mapExpressionHelper.removeListener(mapChangeListener);
    }

    public static <K, V> void fireValueChangedEvent(MapExpressionHelper<K, V> mapExpressionHelper) {
        if (mapExpressionHelper != null) {
            mapExpressionHelper.fireValueChangedEvent();
        }
    }

    public static <K, V> void fireValueChangedEvent(MapExpressionHelper<K, V> mapExpressionHelper, MapChangeListener.Change<? extends K, ? extends V> change) {
        if (mapExpressionHelper != null) {
            mapExpressionHelper.fireValueChangedEvent(change);
        }
    }

    protected MapExpressionHelper(ObservableMapValue<K, V> observableMapValue) {
        this.observable = observableMapValue;
    }

    protected abstract MapExpressionHelper<K, V> addListener(InvalidationListener var1);

    protected abstract MapExpressionHelper<K, V> removeListener(InvalidationListener var1);

    protected abstract MapExpressionHelper<K, V> addListener(ChangeListener<? super ObservableMap<K, V>> var1);

    protected abstract MapExpressionHelper<K, V> removeListener(ChangeListener<? super ObservableMap<K, V>> var1);

    protected abstract MapExpressionHelper<K, V> addListener(MapChangeListener<? super K, ? super V> var1);

    protected abstract MapExpressionHelper<K, V> removeListener(MapChangeListener<? super K, ? super V> var1);

    protected abstract void fireValueChangedEvent();

    protected abstract void fireValueChangedEvent(MapChangeListener.Change<? extends K, ? extends V> var1);

    public static class SimpleChange<K, V>
    extends MapChangeListener.Change<K, V> {
        private K key;
        private V old;
        private V added;
        private boolean removeOp;
        private boolean addOp;

        public SimpleChange(ObservableMap<K, V> observableMap) {
            super(observableMap);
        }

        public SimpleChange(ObservableMap<K, V> observableMap, MapChangeListener.Change<? extends K, ? extends V> change) {
            super(observableMap);
            this.key = change.getKey();
            this.old = change.getValueRemoved();
            this.added = change.getValueAdded();
            this.addOp = change.wasAdded();
            this.removeOp = change.wasRemoved();
        }

        public SimpleChange<K, V> setRemoved(K k, V v) {
            this.key = k;
            this.old = v;
            this.added = null;
            this.addOp = false;
            this.removeOp = true;
            return this;
        }

        public SimpleChange<K, V> setAdded(K k, V v) {
            this.key = k;
            this.old = null;
            this.added = v;
            this.addOp = true;
            this.removeOp = false;
            return this;
        }

        public SimpleChange<K, V> setPut(K k, V v, V v2) {
            this.key = k;
            this.old = v;
            this.added = v2;
            this.addOp = true;
            this.removeOp = true;
            return this;
        }

        @Override
        public boolean wasAdded() {
            return this.addOp;
        }

        @Override
        public boolean wasRemoved() {
            return this.removeOp;
        }

        @Override
        public K getKey() {
            return this.key;
        }

        @Override
        public V getValueAdded() {
            return this.added;
        }

        @Override
        public V getValueRemoved() {
            return this.old;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            if (this.addOp) {
                if (this.removeOp) {
                    stringBuilder.append("replaced ").append(this.old).append(" by ").append(this.added);
                } else {
                    stringBuilder.append("added ").append(this.added);
                }
            } else {
                stringBuilder.append("removed ").append(this.old);
            }
            stringBuilder.append(" at key ").append(this.key);
            return stringBuilder.toString();
        }
    }

    private static class Generic<K, V>
    extends MapExpressionHelper<K, V> {
        private InvalidationListener[] invalidationListeners;
        private ChangeListener<? super ObservableMap<K, V>>[] changeListeners;
        private MapChangeListener<? super K, ? super V>[] mapChangeListeners;
        private int invalidationSize;
        private int changeSize;
        private int mapChangeSize;
        private boolean locked;
        private ObservableMap<K, V> currentValue;

        private Generic(ObservableMapValue<K, V> observableMapValue, InvalidationListener invalidationListener, InvalidationListener invalidationListener2) {
            super(observableMapValue);
            this.invalidationListeners = new InvalidationListener[]{invalidationListener, invalidationListener2};
            this.invalidationSize = 2;
        }

        private Generic(ObservableMapValue<K, V> observableMapValue, ChangeListener<? super ObservableMap<K, V>> changeListener, ChangeListener<? super ObservableMap<K, V>> changeListener2) {
            super(observableMapValue);
            this.changeListeners = new ChangeListener[]{changeListener, changeListener2};
            this.changeSize = 2;
            this.currentValue = (ObservableMap)observableMapValue.getValue();
        }

        private Generic(ObservableMapValue<K, V> observableMapValue, MapChangeListener<? super K, ? super V> mapChangeListener, MapChangeListener<? super K, ? super V> mapChangeListener2) {
            super(observableMapValue);
            this.mapChangeListeners = new MapChangeListener[]{mapChangeListener, mapChangeListener2};
            this.mapChangeSize = 2;
            this.currentValue = (ObservableMap)observableMapValue.getValue();
        }

        private Generic(ObservableMapValue<K, V> observableMapValue, InvalidationListener invalidationListener, ChangeListener<? super ObservableMap<K, V>> changeListener) {
            super(observableMapValue);
            this.invalidationListeners = new InvalidationListener[]{invalidationListener};
            this.invalidationSize = 1;
            this.changeListeners = new ChangeListener[]{changeListener};
            this.changeSize = 1;
            this.currentValue = (ObservableMap)observableMapValue.getValue();
        }

        private Generic(ObservableMapValue<K, V> observableMapValue, InvalidationListener invalidationListener, MapChangeListener<? super K, ? super V> mapChangeListener) {
            super(observableMapValue);
            this.invalidationListeners = new InvalidationListener[]{invalidationListener};
            this.invalidationSize = 1;
            this.mapChangeListeners = new MapChangeListener[]{mapChangeListener};
            this.mapChangeSize = 1;
            this.currentValue = (ObservableMap)observableMapValue.getValue();
        }

        private Generic(ObservableMapValue<K, V> observableMapValue, ChangeListener<? super ObservableMap<K, V>> changeListener, MapChangeListener<? super K, ? super V> mapChangeListener) {
            super(observableMapValue);
            this.changeListeners = new ChangeListener[]{changeListener};
            this.changeSize = 1;
            this.mapChangeListeners = new MapChangeListener[]{mapChangeListener};
            this.mapChangeSize = 1;
            this.currentValue = (ObservableMap)observableMapValue.getValue();
        }

        @Override
        protected MapExpressionHelper<K, V> addListener(InvalidationListener invalidationListener) {
            if (this.invalidationListeners == null) {
                this.invalidationListeners = new InvalidationListener[]{invalidationListener};
                this.invalidationSize = 1;
            } else {
                int n = this.invalidationListeners.length;
                if (this.locked) {
                    int n2 = this.invalidationSize < n ? n : n * 3 / 2 + 1;
                    this.invalidationListeners = Arrays.copyOf(this.invalidationListeners, n2);
                } else if (this.invalidationSize == n) {
                    this.invalidationSize = Generic.trim(this.invalidationSize, this.invalidationListeners);
                    if (this.invalidationSize == n) {
                        int n3 = n * 3 / 2 + 1;
                        this.invalidationListeners = Arrays.copyOf(this.invalidationListeners, n3);
                    }
                }
                this.invalidationListeners[this.invalidationSize++] = invalidationListener;
            }
            return this;
        }

        @Override
        protected MapExpressionHelper<K, V> removeListener(InvalidationListener invalidationListener) {
            if (this.invalidationListeners != null) {
                for (int i = 0; i < this.invalidationSize; ++i) {
                    if (!invalidationListener.equals(this.invalidationListeners[i])) continue;
                    if (this.invalidationSize == 1) {
                        if (this.changeSize == 1 && this.mapChangeSize == 0) {
                            return new SingleChange(this.observable, this.changeListeners[0]);
                        }
                        if (this.changeSize == 0 && this.mapChangeSize == 1) {
                            return new SingleMapChange(this.observable, this.mapChangeListeners[0]);
                        }
                        this.invalidationListeners = null;
                        this.invalidationSize = 0;
                        break;
                    }
                    if (this.invalidationSize == 2 && this.changeSize == 0 && this.mapChangeSize == 0) {
                        return new SingleInvalidation(this.observable, this.invalidationListeners[1 - i]);
                    }
                    int n = this.invalidationSize - i - 1;
                    InvalidationListener[] invalidationListenerArray = this.invalidationListeners;
                    if (this.locked) {
                        this.invalidationListeners = new InvalidationListener[this.invalidationListeners.length];
                        System.arraycopy(invalidationListenerArray, 0, this.invalidationListeners, 0, i + 1);
                    }
                    if (n > 0) {
                        System.arraycopy(invalidationListenerArray, i + 1, this.invalidationListeners, i, n);
                    }
                    --this.invalidationSize;
                    if (this.locked) break;
                    this.invalidationListeners[this.invalidationSize] = null;
                    break;
                }
            }
            return this;
        }

        @Override
        protected MapExpressionHelper<K, V> addListener(ChangeListener<? super ObservableMap<K, V>> changeListener) {
            if (this.changeListeners == null) {
                this.changeListeners = new ChangeListener[]{changeListener};
                this.changeSize = 1;
            } else {
                int n = this.changeListeners.length;
                if (this.locked) {
                    int n2 = this.changeSize < n ? n : n * 3 / 2 + 1;
                    this.changeListeners = Arrays.copyOf(this.changeListeners, n2);
                } else if (this.changeSize == n) {
                    this.changeSize = Generic.trim(this.changeSize, this.changeListeners);
                    if (this.changeSize == n) {
                        int n3 = n * 3 / 2 + 1;
                        this.changeListeners = Arrays.copyOf(this.changeListeners, n3);
                    }
                }
                this.changeListeners[this.changeSize++] = changeListener;
            }
            if (this.changeSize == 1) {
                this.currentValue = (ObservableMap)this.observable.getValue();
            }
            return this;
        }

        @Override
        protected MapExpressionHelper<K, V> removeListener(ChangeListener<? super ObservableMap<K, V>> changeListener) {
            if (this.changeListeners != null) {
                for (int i = 0; i < this.changeSize; ++i) {
                    if (!changeListener.equals(this.changeListeners[i])) continue;
                    if (this.changeSize == 1) {
                        if (this.invalidationSize == 1 && this.mapChangeSize == 0) {
                            return new SingleInvalidation(this.observable, this.invalidationListeners[0]);
                        }
                        if (this.invalidationSize == 0 && this.mapChangeSize == 1) {
                            return new SingleMapChange(this.observable, this.mapChangeListeners[0]);
                        }
                        this.changeListeners = null;
                        this.changeSize = 0;
                        break;
                    }
                    if (this.changeSize == 2 && this.invalidationSize == 0 && this.mapChangeSize == 0) {
                        return new SingleChange(this.observable, this.changeListeners[1 - i]);
                    }
                    int n = this.changeSize - i - 1;
                    ChangeListener<? super ObservableMap<K, V>>[] changeListenerArray = this.changeListeners;
                    if (this.locked) {
                        this.changeListeners = new ChangeListener[this.changeListeners.length];
                        System.arraycopy(changeListenerArray, 0, this.changeListeners, 0, i + 1);
                    }
                    if (n > 0) {
                        System.arraycopy(changeListenerArray, i + 1, this.changeListeners, i, n);
                    }
                    --this.changeSize;
                    if (this.locked) break;
                    this.changeListeners[this.changeSize] = null;
                    break;
                }
            }
            return this;
        }

        @Override
        protected MapExpressionHelper<K, V> addListener(MapChangeListener<? super K, ? super V> mapChangeListener) {
            if (this.mapChangeListeners == null) {
                this.mapChangeListeners = new MapChangeListener[]{mapChangeListener};
                this.mapChangeSize = 1;
            } else {
                int n = this.mapChangeListeners.length;
                if (this.locked) {
                    int n2 = this.mapChangeSize < n ? n : n * 3 / 2 + 1;
                    this.mapChangeListeners = Arrays.copyOf(this.mapChangeListeners, n2);
                } else if (this.mapChangeSize == n) {
                    this.mapChangeSize = Generic.trim(this.mapChangeSize, this.mapChangeListeners);
                    if (this.mapChangeSize == n) {
                        int n3 = n * 3 / 2 + 1;
                        this.mapChangeListeners = Arrays.copyOf(this.mapChangeListeners, n3);
                    }
                }
                this.mapChangeListeners[this.mapChangeSize++] = mapChangeListener;
            }
            if (this.mapChangeSize == 1) {
                this.currentValue = (ObservableMap)this.observable.getValue();
            }
            return this;
        }

        @Override
        protected MapExpressionHelper<K, V> removeListener(MapChangeListener<? super K, ? super V> mapChangeListener) {
            if (this.mapChangeListeners != null) {
                for (int i = 0; i < this.mapChangeSize; ++i) {
                    if (!mapChangeListener.equals(this.mapChangeListeners[i])) continue;
                    if (this.mapChangeSize == 1) {
                        if (this.invalidationSize == 1 && this.changeSize == 0) {
                            return new SingleInvalidation(this.observable, this.invalidationListeners[0]);
                        }
                        if (this.invalidationSize == 0 && this.changeSize == 1) {
                            return new SingleChange(this.observable, this.changeListeners[0]);
                        }
                        this.mapChangeListeners = null;
                        this.mapChangeSize = 0;
                        break;
                    }
                    if (this.mapChangeSize == 2 && this.invalidationSize == 0 && this.changeSize == 0) {
                        return new SingleMapChange(this.observable, this.mapChangeListeners[1 - i]);
                    }
                    int n = this.mapChangeSize - i - 1;
                    MapChangeListener<? super K, ? super V>[] mapChangeListenerArray = this.mapChangeListeners;
                    if (this.locked) {
                        this.mapChangeListeners = new MapChangeListener[this.mapChangeListeners.length];
                        System.arraycopy(mapChangeListenerArray, 0, this.mapChangeListeners, 0, i + 1);
                    }
                    if (n > 0) {
                        System.arraycopy(mapChangeListenerArray, i + 1, this.mapChangeListeners, i, n);
                    }
                    --this.mapChangeSize;
                    if (this.locked) break;
                    this.mapChangeListeners[this.mapChangeSize] = null;
                    break;
                }
            }
            return this;
        }

        @Override
        protected void fireValueChangedEvent() {
            if (this.changeSize == 0 && this.mapChangeSize == 0) {
                this.notifyListeners(this.currentValue, null);
            } else {
                ObservableMap<K, V> observableMap = this.currentValue;
                this.currentValue = (ObservableMap)this.observable.getValue();
                this.notifyListeners(observableMap, null);
            }
        }

        @Override
        protected void fireValueChangedEvent(MapChangeListener.Change<? extends K, ? extends V> change) {
            SimpleChange<? extends K, ? extends V> simpleChange = this.mapChangeSize == 0 ? null : new SimpleChange<K, V>(this.observable, change);
            this.notifyListeners(this.currentValue, simpleChange);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private void notifyListeners(ObservableMap<K, V> observableMap, SimpleChange<K, V> simpleChange) {
            block19: {
                InvalidationListener[] invalidationListenerArray = this.invalidationListeners;
                int n = this.invalidationSize;
                ChangeListener<? super ObservableMap<K, V>>[] changeListenerArray = this.changeListeners;
                int n2 = this.changeSize;
                MapChangeListener<? super K, ? super V>[] mapChangeListenerArray = this.mapChangeListeners;
                int n3 = this.mapChangeSize;
                try {
                    Object k;
                    int n4;
                    this.locked = true;
                    for (n4 = 0; n4 < n; ++n4) {
                        invalidationListenerArray[n4].invalidated(this.observable);
                    }
                    if (this.currentValue == observableMap && simpleChange == null) break block19;
                    for (n4 = 0; n4 < n2; ++n4) {
                        changeListenerArray[n4].changed(this.observable, observableMap, this.currentValue);
                    }
                    if (n3 <= 0) break block19;
                    if (simpleChange != null) {
                        for (n4 = 0; n4 < n3; ++n4) {
                            mapChangeListenerArray[n4].onChanged(simpleChange);
                        }
                        break block19;
                    }
                    simpleChange = new SimpleChange(this.observable);
                    if (this.currentValue == null) {
                        for (Map.Entry entry : observableMap.entrySet()) {
                            simpleChange.setRemoved(entry.getKey(), entry.getValue());
                            for (int i = 0; i < n3; ++i) {
                                mapChangeListenerArray[i].onChanged(simpleChange);
                            }
                        }
                        break block19;
                    }
                    if (observableMap == null) {
                        for (Map.Entry entry : this.currentValue.entrySet()) {
                            simpleChange.setAdded(entry.getKey(), entry.getValue());
                            for (int i = 0; i < n3; ++i) {
                                mapChangeListenerArray[i].onChanged(simpleChange);
                            }
                        }
                        break block19;
                    }
                    for (Map.Entry entry : observableMap.entrySet()) {
                        k = entry.getKey();
                        Object v = entry.getValue();
                        if (this.currentValue.containsKey(k)) {
                            Object v2 = this.currentValue.get(k);
                            if (!(v == null ? v2 != null : !v2.equals(v))) continue;
                            simpleChange.setPut(k, v, v2);
                            for (int i = 0; i < n3; ++i) {
                                mapChangeListenerArray[i].onChanged(simpleChange);
                            }
                            continue;
                        }
                        simpleChange.setRemoved(k, v);
                        for (int i = 0; i < n3; ++i) {
                            mapChangeListenerArray[i].onChanged(simpleChange);
                        }
                    }
                    for (Map.Entry entry : this.currentValue.entrySet()) {
                        k = entry.getKey();
                        if (observableMap.containsKey(k)) continue;
                        simpleChange.setAdded(k, entry.getValue());
                        for (int i = 0; i < n3; ++i) {
                            mapChangeListenerArray[i].onChanged(simpleChange);
                        }
                    }
                }
                finally {
                    this.locked = false;
                }
            }
        }
    }

    private static class SingleMapChange<K, V>
    extends MapExpressionHelper<K, V> {
        private final MapChangeListener<? super K, ? super V> listener;
        private ObservableMap<K, V> currentValue;

        private SingleMapChange(ObservableMapValue<K, V> observableMapValue, MapChangeListener<? super K, ? super V> mapChangeListener) {
            super(observableMapValue);
            this.listener = mapChangeListener;
            this.currentValue = (ObservableMap)observableMapValue.getValue();
        }

        @Override
        protected MapExpressionHelper<K, V> addListener(InvalidationListener invalidationListener) {
            return new Generic(this.observable, invalidationListener, this.listener);
        }

        @Override
        protected MapExpressionHelper<K, V> removeListener(InvalidationListener invalidationListener) {
            return this;
        }

        @Override
        protected MapExpressionHelper<K, V> addListener(ChangeListener<? super ObservableMap<K, V>> changeListener) {
            return new Generic(this.observable, changeListener, this.listener);
        }

        @Override
        protected MapExpressionHelper<K, V> removeListener(ChangeListener<? super ObservableMap<K, V>> changeListener) {
            return this;
        }

        @Override
        protected MapExpressionHelper<K, V> addListener(MapChangeListener<? super K, ? super V> mapChangeListener) {
            return new Generic(this.observable, this.listener, mapChangeListener);
        }

        @Override
        protected MapExpressionHelper<K, V> removeListener(MapChangeListener<? super K, ? super V> mapChangeListener) {
            return mapChangeListener.equals(this.listener) ? null : this;
        }

        @Override
        protected void fireValueChangedEvent() {
            block9: {
                ObservableMap<K, V> observableMap = this.currentValue;
                this.currentValue = (ObservableMap)this.observable.getValue();
                if (this.currentValue == observableMap) break block9;
                SimpleChange simpleChange = new SimpleChange(this.observable);
                if (this.currentValue == null) {
                    for (Map.Entry entry : observableMap.entrySet()) {
                        this.listener.onChanged(simpleChange.setRemoved(entry.getKey(), entry.getValue()));
                    }
                } else if (observableMap == null) {
                    for (Map.Entry entry : this.currentValue.entrySet()) {
                        this.listener.onChanged(simpleChange.setAdded(entry.getKey(), entry.getValue()));
                    }
                } else {
                    Object k;
                    for (Map.Entry entry : observableMap.entrySet()) {
                        k = entry.getKey();
                        Object v = entry.getValue();
                        if (this.currentValue.containsKey(k)) {
                            Object v2 = this.currentValue.get(k);
                            if (!(v == null ? v2 != null : !v2.equals(v))) continue;
                            this.listener.onChanged(simpleChange.setPut(k, v, v2));
                            continue;
                        }
                        this.listener.onChanged(simpleChange.setRemoved(k, v));
                    }
                    for (Map.Entry entry : this.currentValue.entrySet()) {
                        k = entry.getKey();
                        if (observableMap.containsKey(k)) continue;
                        this.listener.onChanged(simpleChange.setAdded(k, entry.getValue()));
                    }
                }
            }
        }

        @Override
        protected void fireValueChangedEvent(MapChangeListener.Change<? extends K, ? extends V> change) {
            this.listener.onChanged(new SimpleChange<K, V>(this.observable, change));
        }
    }

    private static class SingleChange<K, V>
    extends MapExpressionHelper<K, V> {
        private final ChangeListener<? super ObservableMap<K, V>> listener;
        private ObservableMap<K, V> currentValue;

        private SingleChange(ObservableMapValue<K, V> observableMapValue, ChangeListener<? super ObservableMap<K, V>> changeListener) {
            super(observableMapValue);
            this.listener = changeListener;
            this.currentValue = (ObservableMap)observableMapValue.getValue();
        }

        @Override
        protected MapExpressionHelper<K, V> addListener(InvalidationListener invalidationListener) {
            return new Generic(this.observable, invalidationListener, this.listener);
        }

        @Override
        protected MapExpressionHelper<K, V> removeListener(InvalidationListener invalidationListener) {
            return this;
        }

        @Override
        protected MapExpressionHelper<K, V> addListener(ChangeListener<? super ObservableMap<K, V>> changeListener) {
            return new Generic(this.observable, this.listener, changeListener);
        }

        @Override
        protected MapExpressionHelper<K, V> removeListener(ChangeListener<? super ObservableMap<K, V>> changeListener) {
            return changeListener.equals(this.listener) ? null : this;
        }

        @Override
        protected MapExpressionHelper<K, V> addListener(MapChangeListener<? super K, ? super V> mapChangeListener) {
            return new Generic(this.observable, this.listener, mapChangeListener);
        }

        @Override
        protected MapExpressionHelper<K, V> removeListener(MapChangeListener<? super K, ? super V> mapChangeListener) {
            return this;
        }

        @Override
        protected void fireValueChangedEvent() {
            ObservableMap<K, V> observableMap = this.currentValue;
            this.currentValue = (ObservableMap)this.observable.getValue();
            if (this.currentValue != observableMap) {
                this.listener.changed(this.observable, observableMap, this.currentValue);
            }
        }

        @Override
        protected void fireValueChangedEvent(MapChangeListener.Change<? extends K, ? extends V> change) {
            this.listener.changed(this.observable, this.currentValue, this.currentValue);
        }
    }

    private static class SingleInvalidation<K, V>
    extends MapExpressionHelper<K, V> {
        private final InvalidationListener listener;

        private SingleInvalidation(ObservableMapValue<K, V> observableMapValue, InvalidationListener invalidationListener) {
            super(observableMapValue);
            this.listener = invalidationListener;
        }

        @Override
        protected MapExpressionHelper<K, V> addListener(InvalidationListener invalidationListener) {
            return new Generic(this.observable, this.listener, invalidationListener);
        }

        @Override
        protected MapExpressionHelper<K, V> removeListener(InvalidationListener invalidationListener) {
            return invalidationListener.equals(this.listener) ? null : this;
        }

        @Override
        protected MapExpressionHelper<K, V> addListener(ChangeListener<? super ObservableMap<K, V>> changeListener) {
            return new Generic(this.observable, this.listener, changeListener);
        }

        @Override
        protected MapExpressionHelper<K, V> removeListener(ChangeListener<? super ObservableMap<K, V>> changeListener) {
            return this;
        }

        @Override
        protected MapExpressionHelper<K, V> addListener(MapChangeListener<? super K, ? super V> mapChangeListener) {
            return new Generic(this.observable, this.listener, mapChangeListener);
        }

        @Override
        protected MapExpressionHelper<K, V> removeListener(MapChangeListener<? super K, ? super V> mapChangeListener) {
            return this;
        }

        @Override
        protected void fireValueChangedEvent() {
            this.listener.invalidated(this.observable);
        }

        @Override
        protected void fireValueChangedEvent(MapChangeListener.Change<? extends K, ? extends V> change) {
            this.listener.invalidated(this.observable);
        }
    }
}

