/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.media;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.media.MediaException;

public class MediaErrorEvent
extends Event {
    private static final long serialVersionUID = 20121107L;
    public static final EventType<MediaErrorEvent> MEDIA_ERROR = new EventType<Event>(Event.ANY, "Media Error Event");
    private MediaException error;

    MediaErrorEvent(Object object, EventTarget eventTarget, MediaException mediaException) {
        super(object, eventTarget, MEDIA_ERROR);
        if (mediaException == null) {
            throw new IllegalArgumentException("error == null!");
        }
        this.error = mediaException;
    }

    public MediaException getMediaError() {
        return this.error;
    }

    @Override
    public String toString() {
        return super.toString() + ": source " + this.getSource() + "; target " + this.getTarget() + "; error " + this.error;
    }

    @Override
    public MediaErrorEvent copyFor(Object object, EventTarget eventTarget) {
        return (MediaErrorEvent)super.copyFor(object, eventTarget);
    }

    public EventType<MediaErrorEvent> getEventType() {
        return super.getEventType();
    }
}

