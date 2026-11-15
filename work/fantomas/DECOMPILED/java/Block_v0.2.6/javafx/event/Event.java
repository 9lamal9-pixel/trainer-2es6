/*
 * Decompiled with CFR 0.152.
 */
package javafx.event;

import com.sun.javafx.event.EventUtil;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.EventObject;
import javafx.beans.NamedArg;
import javafx.event.EventTarget;
import javafx.event.EventType;

public class Event
extends EventObject
implements Cloneable {
    private static final long serialVersionUID = 20121107L;
    public static final EventTarget NULL_SOURCE_TARGET = eventDispatchChain -> eventDispatchChain;
    public static final EventType<Event> ANY = EventType.ROOT;
    protected EventType<? extends Event> eventType;
    protected transient EventTarget target;
    protected boolean consumed;

    public Event(@NamedArg(value="eventType") EventType<? extends Event> eventType) {
        this(null, null, eventType);
    }

    public Event(@NamedArg(value="source") Object object, @NamedArg(value="target") EventTarget eventTarget, @NamedArg(value="eventType") EventType<? extends Event> eventType) {
        super(object != null ? object : NULL_SOURCE_TARGET);
        this.target = eventTarget != null ? eventTarget : NULL_SOURCE_TARGET;
        this.eventType = eventType;
    }

    public EventTarget getTarget() {
        return this.target;
    }

    public EventType<? extends Event> getEventType() {
        return this.eventType;
    }

    public Event copyFor(Object object, EventTarget eventTarget) {
        Event event = (Event)this.clone();
        event.source = object != null ? object : NULL_SOURCE_TARGET;
        event.target = eventTarget != null ? eventTarget : NULL_SOURCE_TARGET;
        event.consumed = false;
        return event;
    }

    public boolean isConsumed() {
        return this.consumed;
    }

    public void consume() {
        this.consumed = true;
    }

    public Object clone() {
        try {
            return super.clone();
        }
        catch (CloneNotSupportedException cloneNotSupportedException) {
            throw new RuntimeException("Can't clone Event");
        }
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        this.source = NULL_SOURCE_TARGET;
        this.target = NULL_SOURCE_TARGET;
    }

    public static void fireEvent(EventTarget eventTarget, Event event) {
        if (eventTarget == null) {
            throw new NullPointerException("Event target must not be null!");
        }
        if (event == null) {
            throw new NullPointerException("Event must not be null!");
        }
        EventUtil.fireEvent(eventTarget, event);
    }
}

