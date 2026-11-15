/*
 * Decompiled with CFR 0.152.
 */
package javafx.collections;

import com.sun.javafx.collections.ArrayListenerHelper;
import javafx.beans.InvalidationListener;
import javafx.collections.ArrayChangeListener;
import javafx.collections.ObservableArray;

public abstract class ObservableArrayBase<T extends ObservableArray<T>>
implements ObservableArray<T> {
    private ArrayListenerHelper<T> listenerHelper;

    @Override
    public final void addListener(InvalidationListener invalidationListener) {
        this.listenerHelper = ArrayListenerHelper.addListener(this.listenerHelper, this, invalidationListener);
    }

    @Override
    public final void removeListener(InvalidationListener invalidationListener) {
        this.listenerHelper = ArrayListenerHelper.removeListener(this.listenerHelper, invalidationListener);
    }

    @Override
    public final void addListener(ArrayChangeListener<T> arrayChangeListener) {
        this.listenerHelper = ArrayListenerHelper.addListener(this.listenerHelper, this, arrayChangeListener);
    }

    @Override
    public final void removeListener(ArrayChangeListener<T> arrayChangeListener) {
        this.listenerHelper = ArrayListenerHelper.removeListener(this.listenerHelper, arrayChangeListener);
    }

    protected final void fireChange(boolean bl, int n, int n2) {
        ArrayListenerHelper.fireValueChangedEvent(this.listenerHelper, bl, n, n2);
    }
}

