/*
 * Decompiled with CFR 0.152.
 */
package javafx.animation;

import com.sun.javafx.animation.TickCalculation;
import com.sun.javafx.collections.TrackableObservableList;
import com.sun.javafx.collections.VetoableListDecorator;
import com.sun.scenario.animation.AbstractMasterTimer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.Duration;

public final class ParallelTransition
extends Transition {
    private static final Animation[] EMPTY_ANIMATION_ARRAY = new Animation[0];
    private static final double EPSILON = 1.0E-12;
    private Animation[] cachedChildren = EMPTY_ANIMATION_ARRAY;
    private long[] durations;
    private long[] delays;
    private double[] rates;
    private long[] offsetTicks;
    private boolean[] forceChildSync;
    private long oldTicks;
    private long cycleTime;
    private boolean childrenChanged = true;
    private boolean toggledRate;
    private final InvalidationListener childrenListener = observable -> {
        this.childrenChanged = true;
        if (this.getStatus() == Animation.Status.STOPPED) {
            this.setCycleDuration(this.computeCycleDuration());
        }
    };
    private final ChangeListener<Number> rateListener = new ChangeListener<Number>(){

        @Override
        public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
            if (number.doubleValue() * number2.doubleValue() < 0.0) {
                for (int i = 0; i < ParallelTransition.this.cachedChildren.length; ++i) {
                    Animation animation = ParallelTransition.this.cachedChildren[i];
                    animation.clipEnvelope.setRate(ParallelTransition.this.rates[i] * Math.signum(ParallelTransition.this.getCurrentRate()));
                }
                ParallelTransition.this.toggledRate = true;
            }
        }
    };
    private ObjectProperty<Node> node;
    private static final Node DEFAULT_NODE = null;
    private final Set<Animation> childrenSet = new HashSet<Animation>();
    private final ObservableList<Animation> children = new VetoableListDecorator<Animation>((ObservableList)new TrackableObservableList<Animation>(){

        @Override
        protected void onChanged(ListChangeListener.Change<Animation> change) {
            while (change.next()) {
                for (Animation animation : change.getRemoved()) {
                    animation.parent = null;
                    animation.rateProperty().removeListener(ParallelTransition.this.childrenListener);
                    animation.totalDurationProperty().removeListener(ParallelTransition.this.childrenListener);
                    animation.delayProperty().removeListener(ParallelTransition.this.childrenListener);
                }
                for (Animation animation : change.getAddedSubList()) {
                    animation.parent = ParallelTransition.this;
                    animation.rateProperty().addListener(ParallelTransition.this.childrenListener);
                    animation.totalDurationProperty().addListener(ParallelTransition.this.childrenListener);
                    animation.delayProperty().addListener(ParallelTransition.this.childrenListener);
                }
            }
            ParallelTransition.this.childrenListener.invalidated(ParallelTransition.this.children);
        }
    }){

        @Override
        protected void onProposedChange(List<Animation> list, int ... nArray) {
            IllegalArgumentException illegalArgumentException = null;
            for (int i = 0; i < nArray.length; i += 2) {
                for (int j = nArray[i]; j < nArray[i + 1]; ++j) {
                    ParallelTransition.this.childrenSet.remove(ParallelTransition.this.children.get(j));
                }
            }
            for (Animation animation : list) {
                if (animation == null) {
                    illegalArgumentException = new IllegalArgumentException("Child cannot be null");
                    break;
                }
                if (!ParallelTransition.this.childrenSet.add(animation)) {
                    illegalArgumentException = new IllegalArgumentException("Attempting to add a duplicate to the list of children");
                    break;
                }
                if (!ParallelTransition.checkCycle(animation, ParallelTransition.this)) continue;
                illegalArgumentException = new IllegalArgumentException("This change would create cycle");
                break;
            }
            if (illegalArgumentException != null) {
                ParallelTransition.this.childrenSet.clear();
                ParallelTransition.this.childrenSet.addAll(ParallelTransition.this.children);
                throw illegalArgumentException;
            }
        }
    };

    public final void setNode(Node node) {
        if (this.node != null || node != null) {
            this.nodeProperty().set(node);
        }
    }

    public final Node getNode() {
        return this.node == null ? DEFAULT_NODE : (Node)this.node.get();
    }

    public final ObjectProperty<Node> nodeProperty() {
        if (this.node == null) {
            this.node = new SimpleObjectProperty<Node>(this, "node", DEFAULT_NODE);
        }
        return this.node;
    }

    private static boolean checkCycle(Animation animation, Animation animation2) {
        Animation animation3 = animation2;
        while (animation3 != animation) {
            if (animation3.parent != null) {
                animation3 = animation3.parent;
                continue;
            }
            return false;
        }
        return true;
    }

    public final ObservableList<Animation> getChildren() {
        return this.children;
    }

    public ParallelTransition(Node node, Animation ... animationArray) {
        this.setInterpolator(Interpolator.LINEAR);
        this.setNode(node);
        this.getChildren().setAll((Animation[])animationArray);
    }

    public ParallelTransition(Animation ... animationArray) {
        this((Node)null, animationArray);
    }

    public ParallelTransition(Node node) {
        this.setInterpolator(Interpolator.LINEAR);
        this.setNode(node);
    }

    public ParallelTransition() {
        this((Node)null);
    }

    ParallelTransition(AbstractMasterTimer abstractMasterTimer) {
        super(abstractMasterTimer);
        this.setInterpolator(Interpolator.LINEAR);
    }

    @Override
    protected Node getParentTargetNode() {
        Node node = this.getNode();
        return node != null ? node : (this.parent != null && this.parent instanceof Transition ? ((Transition)this.parent).getParentTargetNode() : null);
    }

    private Duration computeCycleDuration() {
        Duration duration = Duration.ZERO;
        for (Animation animation : this.getChildren()) {
            double d = Math.abs(animation.getRate());
            Duration duration2 = d < 1.0E-12 ? animation.getTotalDuration() : animation.getTotalDuration().divide(d);
            Duration duration3 = duration2.add(animation.getDelay());
            if (duration3.isIndefinite()) {
                return Duration.INDEFINITE;
            }
            if (!duration3.greaterThan(duration)) continue;
            duration = duration3;
        }
        return duration;
    }

    private double calculateFraction(long l, long l2) {
        double d = (double)l / (double)l2;
        return d <= 0.0 ? 0.0 : (d >= 1.0 ? 1.0 : d);
    }

    private boolean startChild(Animation animation, int n) {
        boolean bl = this.forceChildSync[n];
        if (animation.startable(bl)) {
            animation.clipEnvelope.setRate(this.rates[n] * Math.signum(this.getCurrentRate()));
            animation.doStart(bl);
            this.forceChildSync[n] = false;
            return true;
        }
        return false;
    }

    @Override
    void sync(boolean bl) {
        super.sync(bl);
        if (bl && this.childrenChanged || this.durations == null) {
            this.cachedChildren = this.getChildren().toArray(EMPTY_ANIMATION_ARRAY);
            int n = this.cachedChildren.length;
            this.durations = new long[n];
            this.delays = new long[n];
            this.rates = new double[n];
            this.offsetTicks = new long[n];
            this.forceChildSync = new boolean[n];
            this.cycleTime = 0L;
            int n2 = 0;
            for (Animation animation : this.cachedChildren) {
                this.rates[n2] = Math.abs(animation.getRate());
                if (this.rates[n2] < 1.0E-12) {
                    this.rates[n2] = 1.0;
                }
                this.durations[n2] = TickCalculation.fromDuration(animation.getTotalDuration(), this.rates[n2]);
                this.delays[n2] = TickCalculation.fromDuration(animation.getDelay());
                this.cycleTime = Math.max(this.cycleTime, TickCalculation.add(this.durations[n2], this.delays[n2]));
                this.forceChildSync[n2] = true;
                ++n2;
            }
            this.childrenChanged = false;
        } else if (bl) {
            int n = this.forceChildSync.length;
            for (int i = 0; i < n; ++i) {
                this.forceChildSync[i] = true;
            }
        }
    }

    @Override
    void doPause() {
        super.doPause();
        for (Animation animation : this.cachedChildren) {
            if (animation.getStatus() != Animation.Status.RUNNING) continue;
            animation.doPause();
        }
    }

    @Override
    void doResume() {
        super.doResume();
        int n = 0;
        for (Animation animation : this.cachedChildren) {
            if (animation.getStatus() == Animation.Status.PAUSED) {
                animation.doResume();
                animation.clipEnvelope.setRate(this.rates[n] * Math.signum(this.getCurrentRate()));
            }
            ++n;
        }
    }

    @Override
    void doStart(boolean bl) {
        super.doStart(bl);
        this.toggledRate = false;
        this.rateProperty().addListener(this.rateListener);
        double d = this.getCurrentRate();
        long l = TickCalculation.fromDuration(this.getCurrentTime());
        if (d < 0.0) {
            this.jumpToEnd();
            if (l < this.cycleTime) {
                this.doJumpTo(l, this.cycleTime, false);
            }
        } else {
            this.jumpToStart();
            if (l > 0L) {
                this.doJumpTo(l, this.cycleTime, false);
            }
        }
    }

    @Override
    void doStop() {
        super.doStop();
        for (Animation animation : this.cachedChildren) {
            if (animation.getStatus() == Animation.Status.STOPPED) continue;
            animation.doStop();
        }
        if (this.childrenChanged) {
            this.setCycleDuration(this.computeCycleDuration());
        }
        this.rateProperty().removeListener(this.rateListener);
    }

    @Override
    void doPlayTo(long l, long l2) {
        int n;
        this.setCurrentTicks(l);
        double d = this.calculateFraction(l, l2);
        long l3 = Math.max(0L, Math.min(this.getCachedInterpolator().interpolate(0L, l2, d), l2));
        if (this.toggledRate) {
            for (n = 0; n < this.cachedChildren.length; ++n) {
                if (this.cachedChildren[n].getStatus() != Animation.Status.RUNNING) continue;
                int n2 = n;
                this.offsetTicks[n2] = (long)((double)this.offsetTicks[n2] - Math.signum(this.getCurrentRate()) * (double)(this.durations[n] - 2L * (this.oldTicks - this.delays[n])));
            }
            this.toggledRate = false;
        }
        if (this.getCurrentRate() > 0.0) {
            n = 0;
            for (Animation animation : this.cachedChildren) {
                if (l3 >= this.delays[n] && (this.oldTicks <= this.delays[n] || l3 < TickCalculation.add(this.delays[n], this.durations[n]) && animation.getStatus() == Animation.Status.STOPPED)) {
                    boolean bl;
                    boolean bl2 = bl = this.oldTicks <= this.delays[n];
                    if (this.startChild(animation, n)) {
                        animation.clipEnvelope.jumpTo(0L);
                    } else {
                        EventHandler<ActionEvent> eventHandler;
                        if (!bl || (eventHandler = animation.getOnFinished()) == null) continue;
                        eventHandler.handle(new ActionEvent(this, null));
                        continue;
                    }
                }
                if (l3 >= TickCalculation.add(this.durations[n], this.delays[n])) {
                    if (animation.getStatus() == Animation.Status.RUNNING) {
                        animation.doTimePulse(TickCalculation.sub(this.durations[n], this.offsetTicks[n]));
                        this.offsetTicks[n] = 0L;
                    }
                } else if (l3 > this.delays[n]) {
                    animation.doTimePulse(TickCalculation.sub(l3 - this.delays[n], this.offsetTicks[n]));
                }
                ++n;
            }
        } else {
            n = 0;
            for (Animation animation : this.cachedChildren) {
                if (l3 < TickCalculation.add(this.durations[n], this.delays[n])) {
                    if (this.oldTicks >= TickCalculation.add(this.durations[n], this.delays[n]) || l3 >= this.delays[n] && animation.getStatus() == Animation.Status.STOPPED) {
                        boolean bl;
                        boolean bl3 = bl = this.oldTicks >= TickCalculation.add(this.durations[n], this.delays[n]);
                        if (this.startChild(animation, n)) {
                            animation.clipEnvelope.jumpTo(Math.round((double)this.durations[n] * this.rates[n]));
                        } else {
                            EventHandler<ActionEvent> eventHandler;
                            if (!bl || (eventHandler = animation.getOnFinished()) == null) continue;
                            eventHandler.handle(new ActionEvent(this, null));
                            continue;
                        }
                    }
                    if (l3 <= this.delays[n]) {
                        if (animation.getStatus() == Animation.Status.RUNNING) {
                            animation.doTimePulse(TickCalculation.sub(this.durations[n], this.offsetTicks[n]));
                            this.offsetTicks[n] = 0L;
                        }
                    } else {
                        animation.doTimePulse(TickCalculation.sub(TickCalculation.add(this.durations[n], this.delays[n]) - l3, this.offsetTicks[n]));
                    }
                }
                ++n;
            }
        }
        this.oldTicks = l3;
    }

    @Override
    void doJumpTo(long l, long l2, boolean bl) {
        this.setCurrentTicks(l);
        if (this.getStatus() == Animation.Status.STOPPED && !bl) {
            return;
        }
        this.sync(false);
        double d = this.calculateFraction(l, l2);
        long l3 = Math.max(0L, Math.min(this.getCachedInterpolator().interpolate(0L, l2, d), l2));
        int n = 0;
        for (Animation animation : this.cachedChildren) {
            Animation.Status status = animation.getStatus();
            if (l3 <= this.delays[n]) {
                this.offsetTicks[n] = 0L;
                if (status != Animation.Status.STOPPED) {
                    animation.clipEnvelope.jumpTo(0L);
                    animation.doStop();
                } else if (TickCalculation.fromDuration(animation.getCurrentTime()) != 0L) {
                    animation.doJumpTo(0L, this.durations[n], true);
                }
            } else if (l3 >= TickCalculation.add(this.durations[n], this.delays[n])) {
                this.offsetTicks[n] = 0L;
                if (status != Animation.Status.STOPPED) {
                    animation.clipEnvelope.jumpTo(Math.round((double)this.durations[n] * this.rates[n]));
                    animation.doStop();
                } else if (TickCalculation.fromDuration(animation.getCurrentTime()) != this.durations[n]) {
                    animation.doJumpTo(this.durations[n], this.durations[n], true);
                }
            } else {
                if (status == Animation.Status.STOPPED) {
                    this.startChild(animation, n);
                    if (this.getStatus() == Animation.Status.PAUSED) {
                        animation.doPause();
                    }
                    this.offsetTicks[n] = this.getCurrentRate() > 0.0 ? l3 - this.delays[n] : TickCalculation.add(this.durations[n], this.delays[n]) - l3;
                } else if (status == Animation.Status.PAUSED) {
                    int n2 = n;
                    this.offsetTicks[n2] = (long)((double)this.offsetTicks[n2] + (double)(l3 - this.oldTicks) * Math.signum(this.clipEnvelope.getCurrentRate()));
                } else {
                    int n3 = n;
                    this.offsetTicks[n3] = this.offsetTicks[n3] + (this.getCurrentRate() > 0.0 ? l3 - this.oldTicks : this.oldTicks - l3);
                }
                animation.clipEnvelope.jumpTo(Math.round((double)TickCalculation.sub(l3, this.delays[n]) * this.rates[n]));
            }
            ++n;
        }
        this.oldTicks = l3;
    }

    @Override
    protected void interpolate(double d) {
    }

    private void jumpToEnd() {
        for (int i = 0; i < this.cachedChildren.length; ++i) {
            if (this.forceChildSync[i]) {
                this.cachedChildren[i].sync(true);
            }
            this.cachedChildren[i].doJumpTo(this.durations[i], this.durations[i], true);
        }
    }

    private void jumpToStart() {
        for (int i = this.cachedChildren.length - 1; i >= 0; --i) {
            if (this.forceChildSync[i]) {
                this.cachedChildren[i].sync(true);
            }
            this.cachedChildren[i].doJumpTo(0L, this.durations[i], true);
        }
    }
}

