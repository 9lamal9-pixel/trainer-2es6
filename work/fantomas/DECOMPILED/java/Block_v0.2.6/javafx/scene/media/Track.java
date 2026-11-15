/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.media;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;

public abstract class Track {
    private String name;
    private long trackID;
    private Locale locale;
    private Map<String, Object> metadata;
    private String description;

    public final String getName() {
        return this.name;
    }

    public final Locale getLocale() {
        return this.locale;
    }

    public final long getTrackID() {
        return this.trackID;
    }

    public final Map<String, Object> getMetadata() {
        return this.metadata;
    }

    Track(long l, Map<String, Object> map) {
        this.trackID = l;
        Object object = map.get("name");
        if (null != object && object instanceof String) {
            this.name = (String)object;
        }
        if (null != (object = map.get("locale")) && object instanceof Locale) {
            this.locale = (Locale)object;
        }
        this.metadata = Collections.unmodifiableMap(map);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public final String toString() {
        Track track = this;
        synchronized (track) {
            if (null == this.description) {
                StringBuilder stringBuilder = new StringBuilder();
                Map<String, Object> map = this.getMetadata();
                stringBuilder.append(this.getClass().getName());
                stringBuilder.append("[ track id = ");
                stringBuilder.append(this.trackID);
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    Object object = entry.getValue();
                    if (null == object) continue;
                    stringBuilder.append(", ");
                    stringBuilder.append(entry.getKey());
                    stringBuilder.append(" = ");
                    stringBuilder.append(object.toString());
                }
                stringBuilder.append("]");
                this.description = stringBuilder.toString();
            }
        }
        return this.description;
    }
}

