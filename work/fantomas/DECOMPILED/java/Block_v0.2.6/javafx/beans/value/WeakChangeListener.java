/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.value;

import java.lang.ref.WeakReference;
import javafx.beans.NamedArg;
import javafx.beans.WeakListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public final class WeakChangeListener<T>
implements ChangeListener<T>,
WeakListener {
    private final WeakReference<ChangeListener<T>> ref;

    public WeakChangeListener(@NamedArg(value="listener") ChangeListener<T> changeListener) {
        if (changeListener == null) {
            throw new NullPointerException("Listener must be specified.");
        }
        this.ref = new WeakReference<ChangeListener<ChangeListener<T>>>(changeListener);
    }

    @Override
    public boolean wasGarbageCollected() {
        return this.ref.get() == null;
    }

    @Override
    public void changed(ObservableValue<? extends T> observableValue, T t, T t2) {
        ChangeListener changeListener = (ChangeListener)this.ref.get();
        if (changeListener != null) {
            changeListener.changed(observableValue, t, t2);
        } else {
            observableValue.removeListener(this);
        }
    }
}

