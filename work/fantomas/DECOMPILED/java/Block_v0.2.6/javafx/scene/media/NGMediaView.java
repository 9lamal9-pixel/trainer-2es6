/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.media;

import com.sun.javafx.geom.RectBounds;
import com.sun.javafx.media.PrismMediaFrameHandler;
import com.sun.javafx.sg.prism.MediaFrameTracker;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.media.jfxmedia.control.VideoDataBuffer;
import com.sun.prism.Graphics;
import com.sun.prism.Texture;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

class NGMediaView
extends NGNode {
    private boolean smooth = true;
    private final RectBounds dimension = new RectBounds();
    private final RectBounds viewport = new RectBounds();
    private PrismMediaFrameHandler handler;
    private MediaPlayer player;
    private MediaFrameTracker frameTracker;

    NGMediaView() {
    }

    public void renderNextFrame() {
        this.visualsChanged();
    }

    public boolean isSmooth() {
        return this.smooth;
    }

    public void setSmooth(boolean bl) {
        if (bl != this.smooth) {
            this.smooth = bl;
            this.visualsChanged();
        }
    }

    public void setX(float f) {
        if (f != this.dimension.getMinX()) {
            float f2 = this.dimension.getWidth();
            this.dimension.setMinX(f);
            this.dimension.setMaxX(f + f2);
            this.geometryChanged();
        }
    }

    public void setY(float f) {
        if (f != this.dimension.getMinY()) {
            float f2 = this.dimension.getHeight();
            this.dimension.setMinY(f);
            this.dimension.setMaxY(f + f2);
            this.geometryChanged();
        }
    }

    public void setMediaProvider(Object object) {
        if (object == null) {
            this.player = null;
            this.handler = null;
            this.geometryChanged();
        } else if (object instanceof MediaPlayer) {
            this.player = (MediaPlayer)object;
            this.handler = PrismMediaFrameHandler.getHandler(this.player);
            this.geometryChanged();
        }
    }

    public void setViewport(float f, float f2, float f3, float f4, float f5, float f6, boolean bl) {
        float f7 = 0.0f;
        float f8 = 0.0f;
        float f9 = f;
        float f10 = f2;
        if (null != this.player) {
            Media media = this.player.getMedia();
            f7 = media.getWidth();
            f8 = media.getHeight();
        }
        if (f5 > 0.0f && f6 > 0.0f) {
            this.viewport.setBounds(f3, f4, f3 + f5, f4 + f6);
            f7 = f5;
            f8 = f6;
        } else {
            this.viewport.setBounds(0.0f, 0.0f, f7, f8);
        }
        if (f <= 0.0f && f2 <= 0.0f) {
            f9 = f7;
            f10 = f8;
        } else if (bl) {
            if ((double)f <= 0.0) {
                f9 = f8 > 0.0f ? f7 * (f2 / f8) : 0.0f;
                f10 = f2;
            } else if ((double)f2 <= 0.0) {
                f9 = f;
                f10 = f7 > 0.0f ? f8 * (f / f7) : 0.0f;
            } else {
                if (f7 == 0.0f) {
                    f7 = f;
                }
                if (f8 == 0.0f) {
                    f8 = f2;
                }
                float f11 = Math.min(f / f7, f2 / f8);
                f9 = f7 * f11;
                f10 = f8 * f11;
            }
        } else if ((double)f2 <= 0.0) {
            f10 = f8;
        } else if ((double)f <= 0.0) {
            f9 = f7;
        }
        if (f10 < 1.0f) {
            f10 = 1.0f;
        }
        if (f9 < 1.0f) {
            f9 = 1.0f;
        }
        this.dimension.setMaxX(this.dimension.getMinX() + f9);
        this.dimension.setMaxY(this.dimension.getMinY() + f10);
        this.geometryChanged();
    }

    @Override
    protected void renderContent(Graphics graphics) {
        if (null == this.handler || null == this.player) {
            return;
        }
        VideoDataBuffer videoDataBuffer = this.player.getLatestFrame();
        if (null == videoDataBuffer) {
            return;
        }
        Texture texture = this.handler.getTexture(graphics, videoDataBuffer);
        if (texture != null) {
            float f;
            float f2;
            float f3 = this.viewport.getWidth();
            float f4 = this.viewport.getHeight();
            boolean bl = !this.dimension.isEmpty();
            boolean bl2 = bl && (f3 != this.dimension.getWidth() || f4 != this.dimension.getHeight());
            graphics.translate(this.dimension.getMinX(), this.dimension.getMinY());
            if (bl2 && f3 != 0.0f && f4 != 0.0f) {
                f2 = this.dimension.getWidth() / f3;
                f = this.dimension.getHeight() / f4;
                graphics.scale(f2, f);
            }
            f2 = this.viewport.getMinX();
            f = this.viewport.getMinY();
            float f5 = f2 + f3;
            float f6 = f + f4;
            graphics.drawTexture(texture, 0.0f, 0.0f, f3, f4, f2, f, f5, f6);
            texture.unlock();
            if (null != this.frameTracker) {
                this.frameTracker.incrementRenderedFrameCount(1);
            }
        }
        videoDataBuffer.releaseFrame();
    }

    @Override
    protected boolean hasOverlappingContents() {
        return false;
    }

    public void setFrameTracker(MediaFrameTracker mediaFrameTracker) {
        this.frameTracker = mediaFrameTracker;
    }
}

