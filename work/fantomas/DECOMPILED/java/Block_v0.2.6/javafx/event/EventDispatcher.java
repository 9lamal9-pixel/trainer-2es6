/*
 * Decompiled with CFR 0.152.
 */
package javafx.event;

import javafx.event.Event;
import javafx.event.EventDispatchChain;

public interface EventDispatcher {
    public Event dispatchEvent(Event var1, EventDispatchChain var2);
}

