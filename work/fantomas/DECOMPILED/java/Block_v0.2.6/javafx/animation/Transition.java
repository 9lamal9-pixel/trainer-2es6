/*
 * Decompiled with CFR 0.152.
 */
package javafx.animation;

import com.sun.scenario.animation.AbstractMasterTimer;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;

public abstract class Transition
extends Animation {
    private ObjectProperty<Interpolator> interpolator;
    private static final Interpolator DEFAULT_INTERPOLATOR = Interpolator.EASE_BOTH;
    private Interpolator cachedInterpolator;

    public final void setInterpolator(Interpolator interpolator) {
        if (this.interpolator != null || !DEFAULT_INTERPOLATOR.equals(interpolator)) {
            this.interpolatorProperty().set(interpolator);
        }
    }

    public final Interpolator getInterpolator() {
        return this.interpolator == null ? DEFAULT_INTERPOLATOR : (Interpolator)this.interpolator.get();
    }

    public final ObjectProperty<Interpolator> interpolatorProperty() {
        if (this.interpolator == null) {
            this.interpolator = new SimpleObjectProperty<Interpolator>(this, "interpolator", DEFAULT_INTERPOLATOR);
        }
        return this.interpolator;
    }

    protected Interpolator getCachedInterpolator() {
        return this.cachedInterpolator;
    }

    public Transition(double d) {
        super(d);
    }

    public Transition() {
    }

    Transition(AbstractMasterTimer abstractMasterTimer) {
        super(abstractMasterTimer);
    }

    protected Node getParentTargetNode() {
        return this.parent != null && this.parent instanceof Transition ? ((Transition)this.parent).getParentTargetNode() : null;
    }

    protected abstract void interpolate(double var1);

    private double calculateFraction(long l, long l2) {
        double d = l2 <= 0L ? 1.0 : (double)l / (double)l2;
        return this.cachedInterpolator.interpolate(0.0, 1.0, d);
    }

    @Override
    boolean startable(boolean bl) {
        return super.startable(bl) && (this.getInterpolator() != null || !bl && this.cachedInterpolator != null);
    }

    @Override
    void sync(boolean bl) {
        super.sync(bl);
        if (bl || this.cachedInterpolator == null) {
            this.cachedInterpolator = this.getInterpolator();
        }
    }

    @Override
    void doPlayTo(long l, long l2) {
        this.setCurrentTicks(l);
        this.interpolate(this.calculateFraction(l, l2));
    }

    @Override
    void doJumpTo(long l, long l2, boolean bl) {
        this.setCurrentTicks(l);
        if (this.getStatus() != Animation.Status.STOPPED || bl) {
            this.sync(false);
            this.interpolate(this.calculateFraction(l, l2));
        }
    }
}

