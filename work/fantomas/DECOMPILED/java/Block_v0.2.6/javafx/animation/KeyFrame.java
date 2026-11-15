/*
 * Decompiled with CFR 0.152.
 */
package javafx.animation;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javafx.animation.KeyValue;
import javafx.beans.NamedArg;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.util.Duration;

public final class KeyFrame {
    private static final EventHandler<ActionEvent> DEFAULT_ON_FINISHED = null;
    private static final String DEFAULT_NAME = null;
    private final Duration time;
    private final Set<KeyValue> values;
    private final EventHandler<ActionEvent> onFinished;
    private final String name;

    public Duration getTime() {
        return this.time;
    }

    public Set<KeyValue> getValues() {
        return this.values;
    }

    public EventHandler<ActionEvent> getOnFinished() {
        return this.onFinished;
    }

    public String getName() {
        return this.name;
    }

    public KeyFrame(@NamedArg(value="time") Duration duration, @NamedArg(value="name") String string, @NamedArg(value="onFinished") EventHandler<ActionEvent> eventHandler, @NamedArg(value="values") Collection<KeyValue> collection) {
        if (duration == null) {
            throw new NullPointerException("The time has to be specified");
        }
        if (duration.lessThan(Duration.ZERO) || duration.equals(Duration.UNKNOWN)) {
            throw new IllegalArgumentException("The time is invalid.");
        }
        this.time = duration;
        this.name = string;
        if (collection != null) {
            CopyOnWriteArraySet<KeyValue> copyOnWriteArraySet = new CopyOnWriteArraySet<KeyValue>(collection);
            copyOnWriteArraySet.remove(null);
            this.values = copyOnWriteArraySet.size() == 0 ? Collections.emptySet() : (copyOnWriteArraySet.size() == 1 ? Collections.singleton((KeyValue)copyOnWriteArraySet.iterator().next()) : Collections.unmodifiableSet(copyOnWriteArraySet));
        } else {
            this.values = Collections.emptySet();
        }
        this.onFinished = eventHandler;
    }

    public KeyFrame(@NamedArg(value="time") Duration duration, @NamedArg(value="name") String string, @NamedArg(value="onFinished") EventHandler<ActionEvent> eventHandler, KeyValue ... keyValueArray) {
        if (duration == null) {
            throw new NullPointerException("The time has to be specified");
        }
        if (duration.lessThan(Duration.ZERO) || duration.equals(Duration.UNKNOWN)) {
            throw new IllegalArgumentException("The time is invalid.");
        }
        this.time = duration;
        this.name = string;
        if (keyValueArray != null) {
            CopyOnWriteArraySet<KeyValue> copyOnWriteArraySet = new CopyOnWriteArraySet<KeyValue>();
            for (KeyValue keyValue : keyValueArray) {
                if (keyValue == null) continue;
                copyOnWriteArraySet.add(keyValue);
            }
            this.values = copyOnWriteArraySet.size() == 0 ? Collections.emptySet() : (copyOnWriteArraySet.size() == 1 ? Collections.singleton((KeyValue)copyOnWriteArraySet.iterator().next()) : Collections.unmodifiableSet(copyOnWriteArraySet));
        } else {
            this.values = Collections.emptySet();
        }
        this.onFinished = eventHandler;
    }

    public KeyFrame(@NamedArg(value="time") Duration duration, @NamedArg(value="onFinished") EventHandler<ActionEvent> eventHandler, KeyValue ... keyValueArray) {
        this(duration, DEFAULT_NAME, eventHandler, keyValueArray);
    }

    public KeyFrame(@NamedArg(value="time") Duration duration, @NamedArg(value="name") String string, KeyValue ... keyValueArray) {
        this(duration, string, DEFAULT_ON_FINISHED, keyValueArray);
    }

    public KeyFrame(@NamedArg(value="time") Duration duration, KeyValue ... keyValueArray) {
        this(duration, DEFAULT_NAME, DEFAULT_ON_FINISHED, keyValueArray);
    }

    public String toString() {
        return "KeyFrame [time=" + this.time + ", values=" + this.values + ", onFinished=" + this.onFinished + ", name=" + this.name + "]";
    }

    public int hashCode() {
        assert (this.time != null && this.values != null);
        int n = 1;
        n = 31 * n + this.time.hashCode();
        n = 31 * n + (this.name == null ? 0 : this.name.hashCode());
        n = 31 * n + (this.onFinished == null ? 0 : this.onFinished.hashCode());
        n = 31 * n + this.values.hashCode();
        return n;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof KeyFrame) {
            KeyFrame keyFrame = (KeyFrame)object;
            assert (this.time != null && this.values != null && keyFrame.time != null && keyFrame.values != null);
            return this.time.equals(keyFrame.time) && (this.name == null ? keyFrame.name == null : this.name.equals(keyFrame.name)) && (this.onFinished == null ? keyFrame.onFinished == null : this.onFinished.equals(keyFrame.onFinished)) && this.values.equals(keyFrame.values);
        }
        return false;
    }
}

