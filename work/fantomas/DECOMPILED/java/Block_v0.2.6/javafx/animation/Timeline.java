/*
 * Decompiled with CFR 0.152.
 */
package javafx.animation;

import com.sun.javafx.collections.TrackableObservableList;
import com.sun.scenario.animation.AbstractMasterTimer;
import com.sun.scenario.animation.shared.TimelineClipCore;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Duration;

public final class Timeline
extends Animation {
    final TimelineClipCore clipCore;
    private final ObservableList<KeyFrame> keyFrames = new TrackableObservableList<KeyFrame>(){

        @Override
        protected void onChanged(ListChangeListener.Change<KeyFrame> change) {
            while (change.next()) {
                String string;
                if (change.wasPermutated()) continue;
                for (KeyFrame keyFrame : change.getRemoved()) {
                    string = keyFrame.getName();
                    if (string == null) continue;
                    Timeline.this.getCuePoints().remove(string);
                }
                for (KeyFrame keyFrame : change.getAddedSubList()) {
                    string = keyFrame.getName();
                    if (string == null) continue;
                    Timeline.this.getCuePoints().put(string, keyFrame.getTime());
                }
                Duration duration = Timeline.this.clipCore.setKeyFrames(Timeline.this.getKeyFrames());
                Timeline.this.setCycleDuration(duration);
            }
        }
    };

    public final ObservableList<KeyFrame> getKeyFrames() {
        return this.keyFrames;
    }

    public Timeline(double d, KeyFrame ... keyFrameArray) {
        super(d);
        this.clipCore = new TimelineClipCore(this);
        this.getKeyFrames().setAll((KeyFrame[])keyFrameArray);
    }

    public Timeline(KeyFrame ... keyFrameArray) {
        this.clipCore = new TimelineClipCore(this);
        this.getKeyFrames().setAll((KeyFrame[])keyFrameArray);
    }

    public Timeline(double d) {
        super(d);
        this.clipCore = new TimelineClipCore(this);
    }

    public Timeline() {
        this.clipCore = new TimelineClipCore(this);
    }

    Timeline(AbstractMasterTimer abstractMasterTimer) {
        super(abstractMasterTimer);
        this.clipCore = new TimelineClipCore(this);
    }

    @Override
    void doPlayTo(long l, long l2) {
        this.clipCore.playTo(l);
    }

    @Override
    void doJumpTo(long l, long l2, boolean bl) {
        this.sync(false);
        this.setCurrentTicks(l);
        this.clipCore.jumpTo(l, bl);
    }

    @Override
    void setCurrentRate(double d) {
        super.setCurrentRate(d);
        this.clipCore.notifyCurrentRateChanged();
    }

    @Override
    void doStart(boolean bl) {
        super.doStart(bl);
        this.clipCore.start(bl);
    }

    @Override
    public void stop() {
        if (this.parent != null) {
            throw new IllegalStateException("Cannot stop when embedded in another animation");
        }
        if (this.getStatus() == Animation.Status.RUNNING) {
            this.clipCore.abort();
        }
        super.stop();
    }
}

