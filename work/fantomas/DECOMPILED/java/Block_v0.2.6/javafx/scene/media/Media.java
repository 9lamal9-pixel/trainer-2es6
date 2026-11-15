/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.media;

import com.sun.media.jfxmedia.MediaManager;
import com.sun.media.jfxmedia.MetadataParser;
import com.sun.media.jfxmedia.events.MetadataListener;
import com.sun.media.jfxmedia.locator.Locator;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.image.Image;
import javafx.scene.media.AudioTrack;
import javafx.scene.media.MediaException;
import javafx.scene.media.SubtitleTrack;
import javafx.scene.media.Track;
import javafx.scene.media.VideoTrack;
import javafx.util.Duration;

public final class Media {
    private ReadOnlyObjectWrapper<MediaException> error;
    private ObjectProperty<Runnable> onError;
    private MetadataListener metadataListener = new _MetadataListener();
    private ObservableMap<String, Object> metadata;
    private final ObservableMap<String, Object> metadataBacking = FXCollections.observableMap(new HashMap());
    private ReadOnlyIntegerWrapper width;
    private ReadOnlyIntegerWrapper height;
    private ReadOnlyObjectWrapper<Duration> duration;
    private ObservableList<Track> tracks;
    private final ObservableList<Track> tracksBacking = FXCollections.observableArrayList();
    private ObservableMap<String, Duration> markers = FXCollections.observableMap(new HashMap());
    private final String source;
    private final Locator jfxLocator;
    private MetadataParser jfxParser;

    private void setError(MediaException mediaException) {
        if (this.getError() == null) {
            this.errorPropertyImpl().set(mediaException);
        }
    }

    public final MediaException getError() {
        return this.error == null ? null : (MediaException)this.error.get();
    }

    public ReadOnlyObjectProperty<MediaException> errorProperty() {
        return this.errorPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<MediaException> errorPropertyImpl() {
        if (this.error == null) {
            this.error = new ReadOnlyObjectWrapper<MediaException>(){

                @Override
                protected void invalidated() {
                    if (Media.this.getOnError() != null) {
                        Platform.runLater(Media.this.getOnError());
                    }
                }

                @Override
                public Object getBean() {
                    return Media.this;
                }

                @Override
                public String getName() {
                    return "error";
                }
            };
        }
        return this.error;
    }

    public final void setOnError(Runnable runnable) {
        this.onErrorProperty().set(runnable);
    }

    public final Runnable getOnError() {
        return this.onError == null ? null : (Runnable)this.onError.get();
    }

    public ObjectProperty<Runnable> onErrorProperty() {
        if (this.onError == null) {
            this.onError = new ObjectPropertyBase<Runnable>(){

                @Override
                protected void invalidated() {
                    if (this.get() != null && Media.this.getError() != null) {
                        Platform.runLater((Runnable)this.get());
                    }
                }

                @Override
                public Object getBean() {
                    return Media.this;
                }

                @Override
                public String getName() {
                    return "onError";
                }
            };
        }
        return this.onError;
    }

    public final ObservableMap<String, Object> getMetadata() {
        return this.metadata;
    }

    final void setWidth(int n) {
        this.widthPropertyImpl().set(n);
    }

    public final int getWidth() {
        return this.width == null ? 0 : this.width.get();
    }

    public ReadOnlyIntegerProperty widthProperty() {
        return this.widthPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyIntegerWrapper widthPropertyImpl() {
        if (this.width == null) {
            this.width = new ReadOnlyIntegerWrapper(this, "width");
        }
        return this.width;
    }

    final void setHeight(int n) {
        this.heightPropertyImpl().set(n);
    }

    public final int getHeight() {
        return this.height == null ? 0 : this.height.get();
    }

    public ReadOnlyIntegerProperty heightProperty() {
        return this.heightPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyIntegerWrapper heightPropertyImpl() {
        if (this.height == null) {
            this.height = new ReadOnlyIntegerWrapper(this, "height");
        }
        return this.height;
    }

    final void setDuration(Duration duration) {
        this.durationPropertyImpl().set(duration);
    }

    public final Duration getDuration() {
        return this.duration == null || this.duration.get() == null ? Duration.UNKNOWN : (Duration)this.duration.get();
    }

    public ReadOnlyObjectProperty<Duration> durationProperty() {
        return this.durationPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<Duration> durationPropertyImpl() {
        if (this.duration == null) {
            this.duration = new ReadOnlyObjectWrapper(this, "duration");
        }
        return this.duration;
    }

    public final ObservableList<Track> getTracks() {
        return this.tracks;
    }

    public final ObservableMap<String, Duration> getMarkers() {
        return this.markers;
    }

    public Media(@NamedArg(value="source") String string) {
        this.source = string;
        URI uRI = null;
        try {
            uRI = new URI(string);
        }
        catch (URISyntaxException uRISyntaxException) {
            throw new IllegalArgumentException(uRISyntaxException);
        }
        this.metadata = FXCollections.unmodifiableObservableMap(this.metadataBacking);
        this.tracks = FXCollections.unmodifiableObservableList(this.tracksBacking);
        Locator locator = null;
        try {
            this.jfxLocator = locator = new Locator(uRI);
            if (locator.canBlock()) {
                InitLocator initLocator = new InitLocator();
                Thread thread = new Thread(initLocator);
                thread.setDaemon(true);
                thread.start();
            } else {
                locator.init();
                this.runMetadataParser();
            }
        }
        catch (URISyntaxException uRISyntaxException) {
            throw new IllegalArgumentException(uRISyntaxException);
        }
        catch (FileNotFoundException fileNotFoundException) {
            throw new MediaException(MediaException.Type.MEDIA_UNAVAILABLE, fileNotFoundException.getMessage());
        }
        catch (IOException iOException) {
            throw new MediaException(MediaException.Type.MEDIA_INACCESSIBLE, iOException.getMessage());
        }
        catch (com.sun.media.jfxmedia.MediaException mediaException) {
            throw new MediaException(MediaException.Type.MEDIA_UNSUPPORTED, mediaException.getMessage());
        }
    }

    private void runMetadataParser() {
        try {
            this.jfxParser = MediaManager.getMetadataParser(this.jfxLocator);
            this.jfxParser.addListener(this.metadataListener);
            this.jfxParser.startParser();
        }
        catch (Exception exception) {
            this.jfxParser = null;
        }
    }

    public String getSource() {
        return this.source;
    }

    Locator retrieveJfxLocator() {
        return this.jfxLocator;
    }

    private Track getTrackWithID(long l) {
        for (Track track : this.tracksBacking) {
            if (track.getTrackID() != l) continue;
            return track;
        }
        return null;
    }

    void _updateMedia(com.sun.media.jfxmedia.Media media) {
        try {
            List<com.sun.media.jfxmedia.track.Track> list = media.getTracks();
            if (list != null) {
                for (com.sun.media.jfxmedia.track.Track track : list) {
                    long l = track.getTrackID();
                    if (this.getTrackWithID(l) != null) continue;
                    Track track2 = null;
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    if (null != track.getName()) {
                        hashMap.put("name", track.getName());
                    }
                    if (null != track.getLocale()) {
                        hashMap.put("locale", track.getLocale());
                    }
                    hashMap.put("encoding", track.getEncodingType().toString());
                    hashMap.put("enabled", track.isEnabled());
                    if (track instanceof com.sun.media.jfxmedia.track.VideoTrack) {
                        com.sun.media.jfxmedia.track.VideoTrack videoTrack = (com.sun.media.jfxmedia.track.VideoTrack)track;
                        int n = videoTrack.getFrameSize().getWidth();
                        int n2 = videoTrack.getFrameSize().getHeight();
                        this.setWidth(n);
                        this.setHeight(n2);
                        hashMap.put("video width", n);
                        hashMap.put("video height", n2);
                        track2 = new VideoTrack(track.getTrackID(), hashMap);
                    } else if (track instanceof com.sun.media.jfxmedia.track.AudioTrack) {
                        track2 = new AudioTrack(track.getTrackID(), hashMap);
                    } else if (track instanceof com.sun.media.jfxmedia.track.SubtitleTrack) {
                        track2 = new SubtitleTrack(l, hashMap);
                    }
                    if (null == track2) continue;
                    this.tracksBacking.add(track2);
                }
            }
        }
        catch (Exception exception) {
            this.setError(new MediaException(MediaException.Type.UNKNOWN, (Throwable)exception));
        }
    }

    void _setError(MediaException.Type type, String string) {
        this.setError(new MediaException(type, string));
    }

    private synchronized void updateMetadata(Map<String, Object> map) {
        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                Object object;
                String string = entry.getKey();
                Object object2 = entry.getValue();
                if (string.equals("image") && object2 instanceof byte[]) {
                    object = (byte[])object2;
                    Image image = new Image(new ByteArrayInputStream((byte[])object));
                    if (image.isError()) continue;
                    this.metadataBacking.put("image", image);
                    continue;
                }
                if (string.equals("duration") && object2 instanceof Long) {
                    object = new Duration(((Long)object2).longValue());
                    if (object == null) continue;
                    this.metadataBacking.put("duration", object);
                    continue;
                }
                this.metadataBacking.put(string, object2);
            }
        }
    }

    private class InitLocator
    implements Runnable {
        private InitLocator() {
        }

        @Override
        public void run() {
            try {
                Media.this.jfxLocator.init();
                Media.this.runMetadataParser();
            }
            catch (URISyntaxException uRISyntaxException) {
                Media.this._setError(MediaException.Type.OPERATION_UNSUPPORTED, uRISyntaxException.getMessage());
            }
            catch (FileNotFoundException fileNotFoundException) {
                Media.this._setError(MediaException.Type.MEDIA_UNAVAILABLE, fileNotFoundException.getMessage());
            }
            catch (IOException iOException) {
                Media.this._setError(MediaException.Type.MEDIA_INACCESSIBLE, iOException.getMessage());
            }
            catch (com.sun.media.jfxmedia.MediaException mediaException) {
                Media.this._setError(MediaException.Type.MEDIA_UNSUPPORTED, mediaException.getMessage());
            }
            catch (Exception exception) {
                Media.this._setError(MediaException.Type.UNKNOWN, exception.getMessage());
            }
        }
    }

    private class _MetadataListener
    implements MetadataListener {
        private _MetadataListener() {
        }

        @Override
        public void onMetadata(Map<String, Object> map) {
            Platform.runLater(() -> {
                Media.this.updateMetadata(map);
                Media.this.jfxParser.removeListener(Media.this.metadataListener);
                Media.this.jfxParser.stopParser();
                Media.this.jfxParser = null;
            });
        }
    }
}

