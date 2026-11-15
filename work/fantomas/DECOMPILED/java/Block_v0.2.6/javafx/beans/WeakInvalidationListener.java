/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans;

import java.lang.ref.WeakReference;
import javafx.beans.InvalidationListener;
import javafx.beans.NamedArg;
import javafx.beans.Observable;
import javafx.beans.WeakListener;

public final class WeakInvalidationListener
implements InvalidationListener,
WeakListener {
    private final WeakReference<InvalidationListener> ref;

    public WeakInvalidationListener(@NamedArg(value="listener") InvalidationListener invalidationListener) {
        if (invalidationListener == null) {
            throw new NullPointerException("Listener must be specified.");
        }
        this.ref = new WeakReference<InvalidationListener>(invalidationListener);
    }

    @Override
    public boolean wasGarbageCollected() {
        return this.ref.get() == null;
    }

    @Override
    public void invalidated(Observable observable) {
        InvalidationListener invalidationListener = (InvalidationListener)this.ref.get();
        if (invalidationListener != null) {
            invalidationListener.invalidated(observable);
        } else {
            observable.removeListener(this);
        }
    }
}

