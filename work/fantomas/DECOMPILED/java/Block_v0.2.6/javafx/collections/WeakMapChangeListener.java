/*
 * Decompiled with CFR 0.152.
 */
package javafx.collections;

import java.lang.ref.WeakReference;
import javafx.beans.NamedArg;
import javafx.beans.WeakListener;
import javafx.collections.MapChangeListener;

public final class WeakMapChangeListener<K, V>
implements MapChangeListener<K, V>,
WeakListener {
    private final WeakReference<MapChangeListener<K, V>> ref;

    public WeakMapChangeListener(@NamedArg(value="listener") MapChangeListener<K, V> mapChangeListener) {
        if (mapChangeListener == null) {
            throw new NullPointerException("Listener must be specified.");
        }
        this.ref = new WeakReference<MapChangeListener<K, V>>(mapChangeListener);
    }

    @Override
    public boolean wasGarbageCollected() {
        return this.ref.get() == null;
    }

    @Override
    public void onChanged(MapChangeListener.Change<? extends K, ? extends V> change) {
        MapChangeListener mapChangeListener = (MapChangeListener)this.ref.get();
        if (mapChangeListener != null) {
            mapChangeListener.onChanged(change);
        } else {
            change.getMap().removeListener(this);
        }
    }
}

