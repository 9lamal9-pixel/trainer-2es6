/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.collections;

import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

public class MapAdapterChange<K, V>
extends MapChangeListener.Change<K, V> {
    private final MapChangeListener.Change<? extends K, ? extends V> change;

    public MapAdapterChange(ObservableMap<K, V> observableMap, MapChangeListener.Change<? extends K, ? extends V> change) {
        super(observableMap);
        this.change = change;
    }

    @Override
    public boolean wasAdded() {
        return this.change.wasAdded();
    }

    @Override
    public boolean wasRemoved() {
        return this.change.wasRemoved();
    }

    @Override
    public K getKey() {
        return this.change.getKey();
    }

    @Override
    public V getValueAdded() {
        return this.change.getValueAdded();
    }

    @Override
    public V getValueRemoved() {
        return this.change.getValueRemoved();
    }

    public String toString() {
        return this.change.toString();
    }
}

