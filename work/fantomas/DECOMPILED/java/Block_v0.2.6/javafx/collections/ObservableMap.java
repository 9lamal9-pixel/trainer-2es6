/*
 * Decompiled with CFR 0.152.
 */
package javafx.collections;

import java.util.Map;
import javafx.beans.Observable;
import javafx.collections.MapChangeListener;

public interface ObservableMap<K, V>
extends Map<K, V>,
Observable {
    public void addListener(MapChangeListener<? super K, ? super V> var1);

    public void removeListener(MapChangeListener<? super K, ? super V> var1);
}

