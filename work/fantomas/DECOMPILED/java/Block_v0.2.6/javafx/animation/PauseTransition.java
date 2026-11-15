/*
 * Decompiled with CFR 0.152.
 */
package javafx.animation;

import javafx.animation.Transition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.util.Duration;

public final class PauseTransition
extends Transition {
    private ObjectProperty<Duration> duration;
    private static final Duration DEFAULT_DURATION = Duration.millis(400.0);

    public final void setDuration(Duration duration) {
        if (this.duration != null || !DEFAULT_DURATION.equals(duration)) {
            this.durationProperty().set(duration);
        }
    }

    public final Duration getDuration() {
        return this.duration == null ? DEFAULT_DURATION : (Duration)this.duration.get();
    }

    public final ObjectProperty<Duration> durationProperty() {
        if (this.duration == null) {
            this.duration = new ObjectPropertyBase<Duration>(DEFAULT_DURATION){

                @Override
                public void invalidated() {
                    try {
                        PauseTransition.this.setCycleDuration(PauseTransition.this.getDuration());
                    }
                    catch (IllegalArgumentException illegalArgumentException) {
                        if (this.isBound()) {
                            this.unbind();
                        }
                        this.set(PauseTransition.this.getCycleDuration());
                        throw illegalArgumentException;
                    }
                }

                @Override
                public Object getBean() {
                    return PauseTransition.this;
                }

                @Override
                public String getName() {
                    return "duration";
                }
            };
        }
        return this.duration;
    }

    public PauseTransition(Duration duration) {
        this.setDuration(duration);
        this.setCycleDuration(duration);
    }

    public PauseTransition() {
        this(DEFAULT_DURATION);
    }

    @Override
    public void interpolate(double d) {
    }
}

