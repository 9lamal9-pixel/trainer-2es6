/*
 * Decompiled with CFR 0.152.
 */
package javafx.collections;

import javafx.collections.ObservableMap;

@FunctionalInterface
public interface MapChangeListener<K, V> {
    public void onChanged(Change<? extends K, ? extends V> var1);

    public static abstract class Change<K, V> {
        private final ObservableMap<K, V> map;

        public Change(ObservableMap<K, V> observableMap) {
            this.map = observableMap;
        }

        public ObservableMap<K, V> getMap() {
            return this.map;
        }

        public abstract boolean wasAdded();

        public abstract boolean wasRemoved();

        public abstract K getKey();

        public abstract V getValueAdded();

        public abstract V getValueRemoved();
    }
}

