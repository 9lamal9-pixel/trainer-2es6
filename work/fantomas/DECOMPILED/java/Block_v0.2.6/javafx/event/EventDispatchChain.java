/*
 * Decompiled with CFR 0.152.
 */
package javafx.event;

import javafx.event.Event;
import javafx.event.EventDispatcher;

public interface EventDispatchChain {
    public EventDispatchChain append(EventDispatcher var1);

    public EventDispatchChain prepend(EventDispatcher var1);

    public Event dispatchEvent(Event var1);
}

