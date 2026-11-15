/*
 * Decompiled with CFR 0.152.
 */
package javafx.collections;

import java.lang.ref.WeakReference;
import javafx.beans.NamedArg;
import javafx.beans.WeakListener;
import javafx.collections.SetChangeListener;

public final class WeakSetChangeListener<E>
implements SetChangeListener<E>,
WeakListener {
    private final WeakReference<SetChangeListener<E>> ref;

    public WeakSetChangeListener(@NamedArg(value="listener") SetChangeListener<E> setChangeListener) {
        if (setChangeListener == null) {
            throw new NullPointerException("Listener must be specified.");
        }
        this.ref = new WeakReference<SetChangeListener<E>>(setChangeListener);
    }

    @Override
    public boolean wasGarbageCollected() {
        return this.ref.get() == null;
    }

    @Override
    public void onChanged(SetChangeListener.Change<? extends E> change) {
        SetChangeListener setChangeListener = (SetChangeListener)this.ref.get();
        if (setChangeListener != null) {
            setChangeListener.onChanged(change);
        } else {
            change.getSet().removeListener(this);
        }
    }
}

