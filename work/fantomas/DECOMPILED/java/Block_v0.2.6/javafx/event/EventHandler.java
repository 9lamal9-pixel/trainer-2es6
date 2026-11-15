/*
 * Decompiled with CFR 0.152.
 */
package javafx.event;

import java.util.EventListener;
import javafx.event.Event;

@FunctionalInterface
public interface EventHandler<T extends Event>
extends EventListener {
    public void handle(T var1);
}

