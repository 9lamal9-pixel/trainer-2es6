/*
 * Decompiled with CFR 0.152.
 */
package javafx.event;

import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import javafx.event.Event;

public final class EventType<T extends Event>
implements Serializable {
    public static final EventType<Event> ROOT = new EventType("EVENT", null);
    private WeakHashMap<EventType<? extends T>, Void> subTypes;
    private final EventType<? super T> superType;
    private final String name;

    @Deprecated
    public EventType() {
        this(ROOT, null);
    }

    public EventType(String string) {
        this(ROOT, string);
    }

    public EventType(EventType<? super T> eventType) {
        this(eventType, null);
    }

    public EventType(EventType<? super T> eventType, String string) {
        if (eventType == null) {
            throw new NullPointerException("Event super type must not be null!");
        }
        this.superType = eventType;
        this.name = string;
        super.register(this);
    }

    EventType(String string, EventType<? super T> eventType) {
        this.superType = eventType;
        this.name = string;
        if (eventType != null) {
            if (eventType.subTypes != null) {
                Iterator<EventType<T>> iterator = eventType.subTypes.keySet().iterator();
                while (iterator.hasNext()) {
                    EventType<? extends T> eventType2 = iterator.next();
                    if ((string != null || eventType2.name != null) && (string == null || !string.equals(eventType2.name))) continue;
                    iterator.remove();
                }
            }
            super.register(this);
        }
    }

    public final EventType<? super T> getSuperType() {
        return this.superType;
    }

    public final String getName() {
        return this.name;
    }

    public String toString() {
        return this.name != null ? this.name : super.toString();
    }

    private void register(EventType<? extends T> eventType) {
        if (this.subTypes == null) {
            this.subTypes = new WeakHashMap();
        }
        for (EventType<? extends T> eventType2 : this.subTypes.keySet()) {
            if ((eventType2.name != null || eventType.name != null) && (eventType2.name == null || !eventType2.name.equals(eventType.name))) continue;
            throw new IllegalArgumentException("EventType \"" + eventType + "\"with parent \"" + eventType.getSuperType() + "\" already exists");
        }
        this.subTypes.put(eventType, null);
    }

    private Object writeReplace() throws ObjectStreamException {
        LinkedList<String> linkedList = new LinkedList<String>();
        EventType<? super T> eventType = this;
        while (eventType != ROOT) {
            linkedList.addFirst(eventType.name);
            eventType = eventType.superType;
        }
        return new EventTypeSerialization(new ArrayList<String>(linkedList));
    }

    static class EventTypeSerialization
    implements Serializable {
        private List<String> path;

        public EventTypeSerialization(List<String> list) {
            this.path = list;
        }

        private Object readResolve() throws ObjectStreamException {
            EventType eventType = ROOT;
            for (int i = 0; i < this.path.size(); ++i) {
                EventType eventType2;
                String string = this.path.get(i);
                if (eventType.subTypes != null) {
                    eventType2 = this.findSubType(eventType.subTypes.keySet(), string);
                    if (eventType2 == null) {
                        throw new InvalidObjectException("Cannot find event type \"" + string + "\" (of " + eventType + ")");
                    }
                } else {
                    throw new InvalidObjectException("Cannot find event type \"" + string + "\" (of " + eventType + ")");
                }
                eventType = eventType2;
            }
            return eventType;
        }

        private EventType findSubType(Set<EventType> set, String string) {
            for (EventType eventType : set) {
                if ((eventType.name != null || string != null) && (eventType.name == null || !eventType.name.equals(string))) continue;
                return eventType;
            }
            return null;
        }
    }
}

