/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.media;

import java.util.Locale;
import java.util.Map;
import javafx.scene.media.Track;

public final class AudioTrack
extends Track {
    @Deprecated
    public final String getLanguage() {
        Locale locale = this.getLocale();
        return null == locale ? null : locale.getLanguage();
    }

    AudioTrack(long l, Map<String, Object> map) {
        super(l, map);
    }
}

