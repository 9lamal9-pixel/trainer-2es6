/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.media;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.Affine3D;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.media.MediaViewHelper;
import com.sun.javafx.sg.prism.MediaFrameTracker;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.tk.Toolkit;
import com.sun.media.jfxmedia.control.MediaPlayerOverlay;
import com.sun.media.jfxmedia.events.VideoFrameRateListener;
import com.sun.media.jfxmediaimpl.HostUtils;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableObjectValue;
import javafx.collections.ObservableMap;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.media.Media;
import javafx.scene.media.MediaErrorEvent;
import javafx.scene.media.MediaException;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.NGMediaView;

public class MediaView
extends Node {
    private static final String VIDEO_FRAME_RATE_PROPERTY_NAME = "jfxmedia.decodedVideoFPS";
    private static final String DEFAULT_STYLE_CLASS = "media-view";
    private InvalidationListener errorListener = new MediaErrorInvalidationListener();
    private InvalidationListener mediaDimensionListener = observable -> {
        NodeHelper.markDirty(this, DirtyBits.NODE_VIEWPORT);
        NodeHelper.geomChanged(this);
    };
    private VideoFrameRateListener decodedFrameRateListener;
    private boolean registerVideoFrameRateListener = false;
    private MediaPlayerOverlay mediaPlayerOverlay = null;
    private ChangeListener<Parent> parentListener;
    private ChangeListener<Boolean> treeVisibleListener;
    private ChangeListener<Number> opacityListener;
    private ObjectProperty<MediaPlayer> mediaPlayer;
    private ObjectProperty<EventHandler<MediaErrorEvent>> onError;
    private BooleanProperty preserveRatio;
    private BooleanProperty smooth;
    private DoubleProperty x;
    private DoubleProperty y;
    private DoubleProperty fitWidth;
    private DoubleProperty fitHeight;
    private ObjectProperty<Rectangle2D> viewport;
    private int decodedFrameCount;
    private int renderedFrameCount;

    private VideoFrameRateListener createVideoFrameRateListener() {
        String string = null;
        try {
            string = System.getProperty(VIDEO_FRAME_RATE_PROPERTY_NAME);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        if (string == null || !Boolean.getBoolean(VIDEO_FRAME_RATE_PROPERTY_NAME)) {
            return null;
        }
        return d -> Platform.runLater(() -> {
            ObservableMap<Object, Object> observableMap = this.getProperties();
            observableMap.put(VIDEO_FRAME_RATE_PROPERTY_NAME, d);
        });
    }

    private void createListeners() {
        this.parentListener = (observableValue, parent, parent2) -> this.updateOverlayVisibility();
        this.treeVisibleListener = (observableValue, bl, bl2) -> this.updateOverlayVisibility();
        this.opacityListener = (observableValue, number, number2) -> this.updateOverlayOpacity();
    }

    private boolean determineVisibility() {
        return this.getParent() != null && this.isVisible();
    }

    private synchronized void updateOverlayVisibility() {
        if (this.mediaPlayerOverlay != null) {
            this.mediaPlayerOverlay.setOverlayVisible(this.determineVisibility());
        }
    }

    private synchronized void updateOverlayOpacity() {
        if (this.mediaPlayerOverlay != null) {
            this.mediaPlayerOverlay.setOverlayOpacity(this.getOpacity());
        }
    }

    private synchronized void updateOverlayX() {
        if (this.mediaPlayerOverlay != null) {
            this.mediaPlayerOverlay.setOverlayX(this.getX());
        }
    }

    private synchronized void updateOverlayY() {
        if (this.mediaPlayerOverlay != null) {
            this.mediaPlayerOverlay.setOverlayY(this.getY());
        }
    }

    private synchronized void updateOverlayWidth() {
        if (this.mediaPlayerOverlay != null) {
            this.mediaPlayerOverlay.setOverlayWidth(this.getFitWidth());
        }
    }

    private synchronized void updateOverlayHeight() {
        if (this.mediaPlayerOverlay != null) {
            this.mediaPlayerOverlay.setOverlayHeight(this.getFitHeight());
        }
    }

    private synchronized void updateOverlayPreserveRatio() {
        if (this.mediaPlayerOverlay != null) {
            this.mediaPlayerOverlay.setOverlayPreserveRatio(this.isPreserveRatio());
        }
    }

    private static Affine3D calculateNodeToSceneTransform(Node node) {
        Affine3D affine3D = new Affine3D();
        do {
            affine3D.preConcatenate(NodeHelper.getLeafTransform(node));
        } while ((node = node.getParent()) != null);
        return affine3D;
    }

    private void updateOverlayTransform() {
        if (this.mediaPlayerOverlay != null) {
            Affine3D affine3D = MediaView.calculateNodeToSceneTransform(this);
            this.mediaPlayerOverlay.setOverlayTransform(affine3D.getMxx(), affine3D.getMxy(), affine3D.getMxz(), affine3D.getMxt(), affine3D.getMyx(), affine3D.getMyy(), affine3D.getMyz(), affine3D.getMyt(), affine3D.getMzx(), affine3D.getMzy(), affine3D.getMzz(), affine3D.getMzt());
        }
    }

    private void updateMediaPlayerOverlay() {
        this.mediaPlayerOverlay.setOverlayX(this.getX());
        this.mediaPlayerOverlay.setOverlayY(this.getY());
        this.mediaPlayerOverlay.setOverlayPreserveRatio(this.isPreserveRatio());
        this.mediaPlayerOverlay.setOverlayWidth(this.getFitWidth());
        this.mediaPlayerOverlay.setOverlayHeight(this.getFitHeight());
        this.mediaPlayerOverlay.setOverlayOpacity(this.getOpacity());
        this.mediaPlayerOverlay.setOverlayVisible(this.determineVisibility());
        this.updateOverlayTransform();
    }

    private void doTransformsChanged() {
        if (this.mediaPlayerOverlay != null) {
            this.updateOverlayTransform();
        }
    }

    private MediaView getMediaView() {
        return this;
    }

    public MediaView() {
        MediaViewHelper.initHelper(this);
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
        this.setSmooth(Toolkit.getToolkit().getDefaultImageSmooth());
        this.decodedFrameRateListener = this.createVideoFrameRateListener();
        this.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
    }

    public MediaView(MediaPlayer mediaPlayer) {
        this();
        this.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        this.setMediaPlayer(mediaPlayer);
    }

    public final void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayerProperty().set(mediaPlayer);
    }

    public final MediaPlayer getMediaPlayer() {
        return this.mediaPlayer == null ? null : (MediaPlayer)this.mediaPlayer.get();
    }

    public final ObjectProperty<MediaPlayer> mediaPlayerProperty() {
        if (this.mediaPlayer == null) {
            this.mediaPlayer = new ObjectPropertyBase<MediaPlayer>(){
                MediaPlayer oldValue = null;

                @Override
                protected void invalidated() {
                    Object object;
                    if (this.oldValue != null) {
                        object = this.oldValue.getMedia();
                        if (object != null) {
                            ((Media)object).widthProperty().removeListener(MediaView.this.mediaDimensionListener);
                            ((Media)object).heightProperty().removeListener(MediaView.this.mediaDimensionListener);
                        }
                        if (MediaView.this.decodedFrameRateListener != null && MediaView.this.getMediaPlayer().retrieveJfxPlayer() != null) {
                            MediaView.this.getMediaPlayer().retrieveJfxPlayer().getVideoRenderControl().removeVideoFrameRateListener(MediaView.this.decodedFrameRateListener);
                        }
                        this.oldValue.errorProperty().removeListener(MediaView.this.errorListener);
                        this.oldValue.removeView(MediaView.this.getMediaView());
                    }
                    if ((object = (MediaPlayer)this.get()) != null) {
                        ((MediaPlayer)object).addView(MediaView.this.getMediaView());
                        ((MediaPlayer)object).errorProperty().addListener(MediaView.this.errorListener);
                        if (MediaView.this.decodedFrameRateListener != null && MediaView.this.getMediaPlayer().retrieveJfxPlayer() != null) {
                            MediaView.this.getMediaPlayer().retrieveJfxPlayer().getVideoRenderControl().addVideoFrameRateListener(MediaView.this.decodedFrameRateListener);
                        } else if (MediaView.this.decodedFrameRateListener != null) {
                            MediaView.this.registerVideoFrameRateListener = true;
                        }
                        Media media = ((MediaPlayer)object).getMedia();
                        if (media != null) {
                            media.widthProperty().addListener(MediaView.this.mediaDimensionListener);
                            media.heightProperty().addListener(MediaView.this.mediaDimensionListener);
                        }
                    }
                    NodeHelper.markDirty(MediaView.this, DirtyBits.MEDIAVIEW_MEDIA);
                    NodeHelper.geomChanged(MediaView.this);
                    this.oldValue = object;
                }

                @Override
                public Object getBean() {
                    return MediaView.this;
                }

                @Override
                public String getName() {
                    return "mediaPlayer";
                }
            };
        }
        return this.mediaPlayer;
    }

    public final void setOnError(EventHandler<MediaErrorEvent> eventHandler) {
        this.onErrorProperty().set(eventHandler);
    }

    public final EventHandler<MediaErrorEvent> getOnError() {
        return this.onError == null ? null : (EventHandler)this.onError.get();
    }

    public final ObjectProperty<EventHandler<MediaErrorEvent>> onErrorProperty() {
        if (this.onError == null) {
            this.onError = new ObjectPropertyBase<EventHandler<MediaErrorEvent>>(){

                @Override
                protected void invalidated() {
                    MediaView.this.setEventHandler(MediaErrorEvent.MEDIA_ERROR, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return MediaView.this;
                }

                @Override
                public String getName() {
                    return "onError";
                }
            };
        }
        return this.onError;
    }

    public final void setPreserveRatio(boolean bl) {
        this.preserveRatioProperty().set(bl);
    }

    public final boolean isPreserveRatio() {
        return this.preserveRatio == null ? true : this.preserveRatio.get();
    }

    public final BooleanProperty preserveRatioProperty() {
        if (this.preserveRatio == null) {
            this.preserveRatio = new BooleanPropertyBase(true){

                @Override
                protected void invalidated() {
                    if (HostUtils.isIOS()) {
                        MediaView.this.updateOverlayPreserveRatio();
                    } else {
                        NodeHelper.markDirty(MediaView.this, DirtyBits.NODE_VIEWPORT);
                        NodeHelper.geomChanged(MediaView.this);
                    }
                }

                @Override
                public Object getBean() {
                    return MediaView.this;
                }

                @Override
                public String getName() {
                    return "preserveRatio";
                }
            };
        }
        return this.preserveRatio;
    }

    public final void setSmooth(boolean bl) {
        this.smoothProperty().set(bl);
    }

    public final boolean isSmooth() {
        return this.smooth == null ? false : this.smooth.get();
    }

    public final BooleanProperty smoothProperty() {
        if (this.smooth == null) {
            this.smooth = new BooleanPropertyBase(){

                @Override
                protected void invalidated() {
                    NodeHelper.markDirty(MediaView.this, DirtyBits.NODE_SMOOTH);
                }

                @Override
                public Object getBean() {
                    return MediaView.this;
                }

                @Override
                public String getName() {
                    return "smooth";
                }
            };
        }
        return this.smooth;
    }

    public final void setX(double d) {
        this.xProperty().set(d);
    }

    public final double getX() {
        return this.x == null ? 0.0 : this.x.get();
    }

    public final DoubleProperty xProperty() {
        if (this.x == null) {
            this.x = new DoublePropertyBase(){

                @Override
                protected void invalidated() {
                    if (HostUtils.isIOS()) {
                        MediaView.this.updateOverlayX();
                    } else {
                        NodeHelper.markDirty(MediaView.this, DirtyBits.NODE_GEOMETRY);
                        NodeHelper.geomChanged(MediaView.this);
                    }
                }

                @Override
                public Object getBean() {
                    return MediaView.this;
                }

                @Override
                public String getName() {
                    return "x";
                }
            };
        }
        return this.x;
    }

    public final void setY(double d) {
        this.yProperty().set(d);
    }

    public final double getY() {
        return this.y == null ? 0.0 : this.y.get();
    }

    public final DoubleProperty yProperty() {
        if (this.y == null) {
            this.y = new DoublePropertyBase(){

                @Override
                protected void invalidated() {
                    if (HostUtils.isIOS()) {
                        MediaView.this.updateOverlayY();
                    } else {
                        NodeHelper.markDirty(MediaView.this, DirtyBits.NODE_GEOMETRY);
                        NodeHelper.geomChanged(MediaView.this);
                    }
                }

                @Override
                public Object getBean() {
                    return MediaView.this;
                }

                @Override
                public String getName() {
                    return "y";
                }
            };
        }
        return this.y;
    }

    public final void setFitWidth(double d) {
        this.fitWidthProperty().set(d);
    }

    public final double getFitWidth() {
        return this.fitWidth == null ? 0.0 : this.fitWidth.get();
    }

    public final DoubleProperty fitWidthProperty() {
        if (this.fitWidth == null) {
            this.fitWidth = new DoublePropertyBase(){

                @Override
                protected void invalidated() {
                    if (HostUtils.isIOS()) {
                        MediaView.this.updateOverlayWidth();
                    } else {
                        NodeHelper.markDirty(MediaView.this, DirtyBits.NODE_VIEWPORT);
                        NodeHelper.geomChanged(MediaView.this);
                    }
                }

                @Override
                public Object getBean() {
                    return MediaView.this;
                }

                @Override
                public String getName() {
                    return "fitWidth";
                }
            };
        }
        return this.fitWidth;
    }

    public final void setFitHeight(double d) {
        this.fitHeightProperty().set(d);
    }

    public final double getFitHeight() {
        return this.fitHeight == null ? 0.0 : this.fitHeight.get();
    }

    public final DoubleProperty fitHeightProperty() {
        if (this.fitHeight == null) {
            this.fitHeight = new DoublePropertyBase(){

                @Override
                protected void invalidated() {
                    if (HostUtils.isIOS()) {
                        MediaView.this.updateOverlayHeight();
                    } else {
                        NodeHelper.markDirty(MediaView.this, DirtyBits.NODE_VIEWPORT);
                        NodeHelper.geomChanged(MediaView.this);
                    }
                }

                @Override
                public Object getBean() {
                    return MediaView.this;
                }

                @Override
                public String getName() {
                    return "fitHeight";
                }
            };
        }
        return this.fitHeight;
    }

    public final void setViewport(Rectangle2D rectangle2D) {
        this.viewportProperty().set(rectangle2D);
    }

    public final Rectangle2D getViewport() {
        return this.viewport == null ? null : (Rectangle2D)this.viewport.get();
    }

    public final ObjectProperty<Rectangle2D> viewportProperty() {
        if (this.viewport == null) {
            this.viewport = new ObjectPropertyBase<Rectangle2D>(){

                @Override
                protected void invalidated() {
                    NodeHelper.markDirty(MediaView.this, DirtyBits.NODE_VIEWPORT);
                    NodeHelper.geomChanged(MediaView.this);
                }

                @Override
                public Object getBean() {
                    return MediaView.this;
                }

                @Override
                public String getName() {
                    return "viewport";
                }
            };
        }
        return this.viewport;
    }

    void notifyMediaChange() {
        MediaPlayer mediaPlayer = this.getMediaPlayer();
        if (mediaPlayer != null) {
            NGMediaView nGMediaView = (NGMediaView)NodeHelper.getPeer(this);
            nGMediaView.setMediaProvider(mediaPlayer);
        }
        NodeHelper.markDirty(this, DirtyBits.MEDIAVIEW_MEDIA);
        NodeHelper.geomChanged(this);
    }

    void notifyMediaSizeChange() {
        NodeHelper.markDirty(this, DirtyBits.NODE_VIEWPORT);
        NodeHelper.geomChanged(this);
    }

    void notifyMediaFrameUpdated() {
        ++this.decodedFrameCount;
        NodeHelper.markDirty(this, DirtyBits.NODE_CONTENTS);
    }

    private NGNode doCreatePeer() {
        NGMediaView nGMediaView = new NGMediaView();
        nGMediaView.setFrameTracker(new MediaViewFrameTracker());
        return nGMediaView;
    }

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        double d;
        Media media = this.getMediaPlayer() == null ? null : this.getMediaPlayer().getMedia();
        double d2 = media != null ? (double)media.getWidth() : 0.0;
        double d3 = media != null ? (double)media.getHeight() : 0.0;
        double d4 = this.getFitWidth();
        double d5 = this.getFitHeight();
        double d6 = this.getViewport() != null ? this.getViewport().getWidth() : 0.0;
        double d7 = d = this.getViewport() != null ? this.getViewport().getHeight() : 0.0;
        if (d6 > 0.0 && d > 0.0) {
            d2 = d6;
            d3 = d;
        }
        if (this.getFitWidth() <= 0.0 && this.getFitHeight() <= 0.0) {
            d4 = d2;
            d5 = d3;
        } else if (this.isPreserveRatio()) {
            if (this.getFitWidth() <= 0.0) {
                d4 = d3 > 0.0 ? d2 * (this.getFitHeight() / d3) : 0.0;
                d5 = this.getFitHeight();
            } else if (this.getFitHeight() <= 0.0) {
                d4 = this.getFitWidth();
                d5 = d2 > 0.0 ? d3 * (this.getFitWidth() / d2) : 0.0;
            } else {
                if (d2 == 0.0) {
                    d2 = this.getFitWidth();
                }
                if (d3 == 0.0) {
                    d3 = this.getFitHeight();
                }
                double d8 = Math.min(this.getFitWidth() / d2, this.getFitHeight() / d3);
                d4 = d2 * d8;
                d5 = d3 * d8;
            }
        } else if (this.getFitHeight() <= 0.0) {
            d5 = d3;
        } else if (this.getFitWidth() <= 0.0) {
            d4 = d2;
        }
        if (d5 < 1.0) {
            d5 = 1.0;
        }
        if (d4 < 1.0) {
            d4 = 1.0;
        }
        d2 = d4;
        d3 = d5;
        if (d2 <= 0.0 || d3 <= 0.0) {
            return baseBounds.makeEmpty();
        }
        baseBounds = baseBounds.deriveWithNewBounds((float)this.getX(), (float)this.getY(), 0.0f, (float)(this.getX() + d2), (float)(this.getY() + d3), 0.0f);
        baseBounds = baseTransform.transform(baseBounds, baseBounds);
        return baseBounds;
    }

    private boolean doComputeContains(double d, double d2) {
        return true;
    }

    void updateViewport() {
        if (this.getMediaPlayer() == null) {
            return;
        }
        NGMediaView nGMediaView = (NGMediaView)NodeHelper.getPeer(this);
        if (this.getViewport() != null) {
            nGMediaView.setViewport((float)this.getFitWidth(), (float)this.getFitHeight(), (float)this.getViewport().getMinX(), (float)this.getViewport().getMinY(), (float)this.getViewport().getWidth(), (float)this.getViewport().getHeight(), this.isPreserveRatio());
        } else {
            nGMediaView.setViewport((float)this.getFitWidth(), (float)this.getFitHeight(), 0.0f, 0.0f, 0.0f, 0.0f, this.isPreserveRatio());
        }
    }

    private void doUpdatePeer() {
        NGMediaView nGMediaView = (NGMediaView)NodeHelper.getPeer(this);
        if (NodeHelper.isDirty(this, DirtyBits.NODE_GEOMETRY)) {
            nGMediaView.setX((float)this.getX());
            nGMediaView.setY((float)this.getY());
        }
        if (NodeHelper.isDirty(this, DirtyBits.NODE_SMOOTH)) {
            nGMediaView.setSmooth(this.isSmooth());
        }
        if (NodeHelper.isDirty(this, DirtyBits.NODE_VIEWPORT)) {
            this.updateViewport();
        }
        if (NodeHelper.isDirty(this, DirtyBits.NODE_CONTENTS)) {
            nGMediaView.renderNextFrame();
        }
        if (NodeHelper.isDirty(this, DirtyBits.MEDIAVIEW_MEDIA)) {
            MediaPlayer mediaPlayer = this.getMediaPlayer();
            if (mediaPlayer != null) {
                nGMediaView.setMediaProvider(mediaPlayer);
                this.updateViewport();
            } else {
                nGMediaView.setMediaProvider(null);
            }
        }
    }

    void perfReset() {
        this.decodedFrameCount = 0;
        this.renderedFrameCount = 0;
    }

    int perfGetDecodedFrameCount() {
        return this.decodedFrameCount;
    }

    int perfGetRenderedFrameCount() {
        return this.renderedFrameCount;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    void _mediaPlayerOnReady() {
        com.sun.media.jfxmedia.MediaPlayer mediaPlayer = this.getMediaPlayer().retrieveJfxPlayer();
        if (mediaPlayer != null) {
            if (this.decodedFrameRateListener != null && this.registerVideoFrameRateListener) {
                mediaPlayer.getVideoRenderControl().addVideoFrameRateListener(this.decodedFrameRateListener);
                this.registerVideoFrameRateListener = false;
            }
            this.mediaPlayerOverlay = mediaPlayer.getMediaPlayerOverlay();
            if (this.mediaPlayerOverlay != null) {
                this.createListeners();
                this.parentProperty().addListener(this.parentListener);
                NodeHelper.treeVisibleProperty(this).addListener(this.treeVisibleListener);
                this.opacityProperty().addListener(this.opacityListener);
                MediaView mediaView = this;
                synchronized (mediaView) {
                    this.updateMediaPlayerOverlay();
                }
            }
        }
    }

    static {
        MediaViewHelper.setMediaViewAccessor(new MediaViewHelper.MediaViewAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((MediaView)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((MediaView)node).doUpdatePeer();
            }

            @Override
            public void doTransformsChanged(Node node) {
                ((MediaView)node).doTransformsChanged();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((MediaView)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public boolean doComputeContains(Node node, double d, double d2) {
                return ((MediaView)node).doComputeContains(d, d2);
            }
        });
    }

    private class MediaViewFrameTracker
    implements MediaFrameTracker {
        private MediaViewFrameTracker() {
        }

        @Override
        public void incrementDecodedFrameCount(int n) {
            MediaView.this.decodedFrameCount += n;
        }

        @Override
        public void incrementRenderedFrameCount(int n) {
            MediaView.this.renderedFrameCount += n;
        }
    }

    private class MediaErrorInvalidationListener
    implements InvalidationListener {
        private MediaErrorInvalidationListener() {
        }

        @Override
        public void invalidated(Observable observable) {
            ObservableObjectValue observableObjectValue = (ObservableObjectValue)observable;
            MediaView.this.fireEvent(new MediaErrorEvent((Object)MediaView.this.getMediaPlayer(), (EventTarget)MediaView.this.getMediaView(), (MediaException)observableObjectValue.get()));
        }
    }
}

