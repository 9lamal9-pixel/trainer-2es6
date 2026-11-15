/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.media;

import javafx.event.ActionEvent;
import javafx.util.Duration;
import javafx.util.Pair;

public class MediaMarkerEvent
extends ActionEvent {
    private static final long serialVersionUID = 20121107L;
    private Pair<String, Duration> marker;

    MediaMarkerEvent(Pair<String, Duration> pair) {
        this.marker = pair;
    }

    public Pair<String, Duration> getMarker() {
        return this.marker;
    }
}

