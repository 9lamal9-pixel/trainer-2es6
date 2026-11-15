/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.NamedArg;
import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.control.TableColumnBase;

public class ScrollToEvent<T>
extends Event {
    public static final EventType<ScrollToEvent> ANY = new EventType<Event>(Event.ANY, "SCROLL_TO");
    private static final EventType<ScrollToEvent<Integer>> SCROLL_TO_TOP_INDEX = new EventType<ScrollToEvent>(ANY, "SCROLL_TO_TOP_INDEX");
    private static final EventType<?> SCROLL_TO_COLUMN = new EventType<ScrollToEvent>(ANY, "SCROLL_TO_COLUMN");
    private static final long serialVersionUID = -8557345736849482516L;
    private final T scrollTarget;

    public static EventType<ScrollToEvent<Integer>> scrollToTopIndex() {
        return SCROLL_TO_TOP_INDEX;
    }

    public static <T extends TableColumnBase<?, ?>> EventType<ScrollToEvent<T>> scrollToColumn() {
        return SCROLL_TO_COLUMN;
    }

    public ScrollToEvent(@NamedArg(value="source") Object object, @NamedArg(value="target") EventTarget eventTarget, @NamedArg(value="type") EventType<ScrollToEvent<T>> eventType, @NamedArg(value="scrollTarget") T t) {
        super(object, eventTarget, eventType);
        assert (t != null);
        this.scrollTarget = t;
    }

    public T getScrollTarget() {
        return this.scrollTarget;
    }
}

