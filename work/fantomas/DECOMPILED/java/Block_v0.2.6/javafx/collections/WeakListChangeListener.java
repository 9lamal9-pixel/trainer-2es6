/*
 * Decompiled with CFR 0.152.
 */
package javafx.collections;

import java.lang.ref.WeakReference;
import javafx.beans.NamedArg;
import javafx.beans.WeakListener;
import javafx.collections.ListChangeListener;

public final class WeakListChangeListener<E>
implements ListChangeListener<E>,
WeakListener {
    private final WeakReference<ListChangeListener<E>> ref;

    public WeakListChangeListener(@NamedArg(value="listener") ListChangeListener<E> listChangeListener) {
        if (listChangeListener == null) {
            throw new NullPointerException("Listener must be specified.");
        }
        this.ref = new WeakReference<ListChangeListener<E>>(listChangeListener);
    }

    @Override
    public boolean wasGarbageCollected() {
        return this.ref.get() == null;
    }

    @Override
    public void onChanged(ListChangeListener.Change<? extends E> change) {
        ListChangeListener listChangeListener = (ListChangeListener)this.ref.get();
        if (listChangeListener != null) {
            listChangeListener.onChanged(change);
        } else {
            change.getList().removeListener(this);
        }
    }
}

