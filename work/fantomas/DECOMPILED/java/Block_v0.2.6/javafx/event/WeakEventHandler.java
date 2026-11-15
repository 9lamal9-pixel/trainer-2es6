/*
 * Decompiled with CFR 0.152.
 */
package javafx.event;

import java.lang.ref.WeakReference;
import javafx.beans.NamedArg;
import javafx.event.Event;
import javafx.event.EventHandler;

public final class WeakEventHandler<T extends Event>
implements EventHandler<T> {
    private final WeakReference<EventHandler<T>> weakRef;

    public WeakEventHandler(@NamedArg(value="eventHandler") EventHandler<T> eventHandler) {
        this.weakRef = new WeakReference<EventHandler<EventHandler<T>>>(eventHandler);
    }

    public boolean wasGarbageCollected() {
        return this.weakRef.get() == null;
    }

    @Override
    public void handle(T t) {
        EventHandler eventHandler = (EventHandler)this.weakRef.get();
        if (eventHandler != null) {
            eventHandler.handle(t);
        }
    }

    void clear() {
        this.weakRef.clear();
    }
}

