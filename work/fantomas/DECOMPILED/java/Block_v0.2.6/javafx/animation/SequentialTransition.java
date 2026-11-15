/*
 * Decompiled with CFR 0.152.
 */
package javafx.animation;

import com.sun.javafx.animation.TickCalculation;
import com.sun.javafx.collections.TrackableObservableList;
import com.sun.javafx.collections.VetoableListDecorator;
import com.sun.scenario.animation.AbstractMasterTimer;
import java.util.Arrays;
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

public final class SequentialTransition
extends Transition {
    private static final Animation[] EMPTY_ANIMATION_ARRAY = new Animation[0];
    private static final int BEFORE = -1;
    private static final double EPSILON = 1.0E-12;
    private Animation[] cachedChildren = EMPTY_ANIMATION_ARRAY;
    private long[] startTimes;
    private long[] durations;
    private long[] delays;
    private double[] rates;
    private boolean[] forceChildSync;
    private int end;
    private int curIndex = -1;
    private long oldTicks = 0L;
    private long offsetTicks;
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
                for (int i = 0; i < SequentialTransition.this.cachedChildren.length; ++i) {
                    Animation animation = SequentialTransition.this.cachedChildren[i];
                    animation.clipEnvelope.setRate(SequentialTransition.this.rates[i] * Math.signum(SequentialTransition.this.getCurrentRate()));
                }
                SequentialTransition.this.toggledRate = true;
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
                    animation.rateProperty().removeListener(SequentialTransition.this.childrenListener);
                    animation.totalDurationProperty().removeListener(SequentialTransition.this.childrenListener);
                    animation.delayProperty().removeListener(SequentialTransition.this.childrenListener);
                }
                for (Animation animation : change.getAddedSubList()) {
                    animation.parent = SequentialTransition.this;
                    animation.rateProperty().addListener(SequentialTransition.this.childrenListener);
                    animation.totalDurationProperty().addListener(SequentialTransition.this.childrenListener);
                    animation.delayProperty().addListener(SequentialTransition.this.childrenListener);
                }
            }
            SequentialTransition.this.childrenListener.invalidated(SequentialTransition.this.children);
        }
    }){

        @Override
        protected void onProposedChange(List<Animation> list, int ... nArray) {
            IllegalArgumentException illegalArgumentException = null;
            for (int i = 0; i < nArray.length; i += 2) {
                for (int j = nArray[i]; j < nArray[i + 1]; ++j) {
                    SequentialTransition.this.childrenSet.remove(SequentialTransition.this.children.get(j));
                }
            }
            for (Animation animation : list) {
                if (animation == null) {
                    illegalArgumentException = new IllegalArgumentException("Child cannot be null");
                    break;
                }
                if (!SequentialTransition.this.childrenSet.add(animation)) {
                    illegalArgumentException = new IllegalArgumentException("Attempting to add a duplicate to the list of children");
                    break;
                }
                if (!SequentialTransition.checkCycle(animation, SequentialTransition.this)) continue;
                illegalArgumentException = new IllegalArgumentException("This change would create cycle");
                break;
            }
            if (illegalArgumentException != null) {
                SequentialTransition.this.childrenSet.clear();
                SequentialTransition.this.childrenSet.addAll(SequentialTransition.this.children);
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

    public SequentialTransition(Node node, Animation ... animationArray) {
        this.setInterpolator(Interpolator.LINEAR);
        this.setNode(node);
        this.getChildren().setAll((Animation[])animationArray);
    }

    public SequentialTransition(Animation ... animationArray) {
        this((Node)null, animationArray);
    }

    public SequentialTransition(Node node) {
        this.setInterpolator(Interpolator.LINEAR);
        this.setNode(node);
    }

    public SequentialTransition() {
        this((Node)null);
    }

    SequentialTransition(AbstractMasterTimer abstractMasterTimer) {
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
            duration = duration.add(animation.getDelay());
            double d = Math.abs(animation.getRate());
            if (!(duration = duration.add(d < 1.0E-12 ? animation.getTotalDuration() : animation.getTotalDuration().divide(d))).isIndefinite()) continue;
            break;
        }
        return duration;
    }

    private double calculateFraction(long l, long l2) {
        double d = (double)l / (double)l2;
        return d <= 0.0 ? 0.0 : (d >= 1.0 ? 1.0 : d);
    }

    private int findNewIndex(long l) {
        int n;
        if (this.curIndex != -1 && this.curIndex != this.end && this.startTimes[this.curIndex] <= l && l <= this.startTimes[this.curIndex + 1]) {
            return this.curIndex;
        }
        boolean bl = this.curIndex == -1 || this.curIndex == this.end;
        int n2 = bl || l < this.oldTicks ? 0 : this.curIndex + 1;
        int n3 = Arrays.binarySearch(this.startTimes, n2, n = bl || this.oldTicks < l ? this.end : this.curIndex, l);
        return n3 < 0 ? -n3 - 2 : (n3 > 0 ? n3 - 1 : 0);
    }

    @Override
    void sync(boolean bl) {
        super.sync(bl);
        if (bl && this.childrenChanged || this.startTimes == null) {
            this.cachedChildren = this.getChildren().toArray(EMPTY_ANIMATION_ARRAY);
            this.end = this.cachedChildren.length;
            this.startTimes = new long[this.end + 1];
            this.durations = new long[this.end];
            this.delays = new long[this.end];
            this.rates = new double[this.end];
            this.forceChildSync = new boolean[this.end];
            long l = 0L;
            int n = 0;
            for (Animation animation : this.cachedChildren) {
                this.startTimes[n] = l;
                this.rates[n] = Math.abs(animation.getRate());
                if (this.rates[n] < 1.0E-12) {
                    this.rates[n] = 1.0;
                }
                this.durations[n] = TickCalculation.fromDuration(animation.getTotalDuration(), this.rates[n]);
                this.delays[n] = TickCalculation.fromDuration(animation.getDelay());
                l = this.durations[n] == Long.MAX_VALUE || this.delays[n] == Long.MAX_VALUE || l == Long.MAX_VALUE ? Long.MAX_VALUE : TickCalculation.add(l, TickCalculation.add(this.durations[n], this.delays[n]));
                this.forceChildSync[n] = true;
                ++n;
            }
            this.startTimes[this.end] = l;
            this.childrenChanged = false;
        } else if (bl) {
            int n = this.forceChildSync.length;
            for (int i = 0; i < n; ++i) {
                this.forceChildSync[i] = true;
            }
        }
    }

    @Override
    void doStart(boolean bl) {
        super.doStart(bl);
        this.toggledRate = false;
        this.rateProperty().addListener(this.rateListener);
        this.offsetTicks = 0L;
        double d = this.getCurrentRate();
        long l = TickCalculation.fromDuration(this.getCurrentTime());
        if (d < 0.0) {
            this.jumpToEnd();
            this.curIndex = this.end;
            if (l < this.startTimes[this.end]) {
                this.doJumpTo(l, this.startTimes[this.end], false);
            }
        } else {
            this.jumpToBefore();
            this.curIndex = -1;
            if (l > 0L) {
                this.doJumpTo(l, this.startTimes[this.end], false);
            }
        }
    }

    @Override
    void doPause() {
        Animation animation;
        super.doPause();
        if (this.curIndex != -1 && this.curIndex != this.end && (animation = this.cachedChildren[this.curIndex]).getStatus() == Animation.Status.RUNNING) {
            animation.doPause();
        }
    }

    @Override
    void doResume() {
        Animation animation;
        super.doResume();
        if (this.curIndex != -1 && this.curIndex != this.end && (animation = this.cachedChildren[this.curIndex]).getStatus() == Animation.Status.PAUSED) {
            animation.doResume();
            animation.clipEnvelope.setRate(this.rates[this.curIndex] * Math.signum(this.getCurrentRate()));
        }
    }

    @Override
    void doStop() {
        Animation animation;
        super.doStop();
        if (this.curIndex != -1 && this.curIndex != this.end && (animation = this.cachedChildren[this.curIndex]).getStatus() != Animation.Status.STOPPED) {
            animation.doStop();
        }
        if (this.childrenChanged) {
            this.setCycleDuration(this.computeCycleDuration());
        }
        this.rateProperty().removeListener(this.rateListener);
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
    void doPlayTo(long l, long l2) {
        Animation animation;
        this.setCurrentTicks(l);
        double d = this.calculateFraction(l, l2);
        long l3 = Math.max(0L, Math.min(this.getCachedInterpolator().interpolate(0L, l2, d), l2));
        int n = this.findNewIndex(l3);
        Animation animation2 = animation = this.curIndex == -1 || this.curIndex == this.end ? null : this.cachedChildren[this.curIndex];
        if (this.toggledRate) {
            if (animation != null && animation.getStatus() == Animation.Status.RUNNING) {
                this.offsetTicks = (long)((double)this.offsetTicks - Math.signum(this.getCurrentRate()) * (double)(this.durations[this.curIndex] - 2L * (this.oldTicks - this.delays[this.curIndex] - this.startTimes[this.curIndex])));
            }
            this.toggledRate = false;
        }
        if (this.curIndex == n) {
            if (this.getCurrentRate() > 0.0) {
                long l4 = TickCalculation.add(this.startTimes[this.curIndex], this.delays[this.curIndex]);
                if (l3 >= l4) {
                    if (this.oldTicks <= l4 || animation.getStatus() == Animation.Status.STOPPED) {
                        boolean bl;
                        boolean bl2 = bl = this.oldTicks <= l4;
                        if (bl) {
                            animation.clipEnvelope.jumpTo(0L);
                        }
                        if (!this.startChild(animation, this.curIndex)) {
                            EventHandler<ActionEvent> eventHandler;
                            if (bl && (eventHandler = animation.getOnFinished()) != null) {
                                eventHandler.handle(new ActionEvent(this, null));
                            }
                            this.oldTicks = l3;
                            return;
                        }
                    }
                    if (l3 >= this.startTimes[this.curIndex + 1]) {
                        animation.doTimePulse(TickCalculation.sub(this.durations[this.curIndex], this.offsetTicks));
                        if (l3 == l2) {
                            this.curIndex = this.end;
                        }
                    } else {
                        long l5 = TickCalculation.sub(l3 - l4, this.offsetTicks);
                        animation.doTimePulse(l5);
                    }
                }
            } else {
                long l6 = TickCalculation.add(this.startTimes[this.curIndex], this.delays[this.curIndex]);
                if (this.oldTicks >= this.startTimes[this.curIndex + 1] || this.oldTicks >= l6 && animation.getStatus() == Animation.Status.STOPPED) {
                    boolean bl;
                    boolean bl3 = bl = this.oldTicks >= this.startTimes[this.curIndex + 1];
                    if (bl) {
                        animation.clipEnvelope.jumpTo(Math.round((double)this.durations[this.curIndex] * this.rates[this.curIndex]));
                    }
                    if (!this.startChild(animation, this.curIndex)) {
                        EventHandler<ActionEvent> eventHandler;
                        if (bl && (eventHandler = animation.getOnFinished()) != null) {
                            eventHandler.handle(new ActionEvent(this, null));
                        }
                        this.oldTicks = l3;
                        return;
                    }
                }
                if (l3 <= l6) {
                    animation.doTimePulse(TickCalculation.sub(this.durations[this.curIndex], this.offsetTicks));
                    if (l3 == 0L) {
                        this.curIndex = -1;
                    }
                } else {
                    long l7 = TickCalculation.sub(this.startTimes[this.curIndex + 1] - l3, this.offsetTicks);
                    animation.doTimePulse(l7);
                }
            }
        } else if (this.curIndex < n) {
            EventHandler<ActionEvent> eventHandler;
            if (animation != null) {
                long l8 = TickCalculation.add(this.startTimes[this.curIndex], this.delays[this.curIndex]);
                if (this.oldTicks <= l8 || animation.getStatus() == Animation.Status.STOPPED && this.oldTicks != this.startTimes[this.curIndex + 1]) {
                    EventHandler<ActionEvent> eventHandler2;
                    boolean bl;
                    boolean bl4 = bl = this.oldTicks <= l8;
                    if (bl) {
                        animation.clipEnvelope.jumpTo(0L);
                    }
                    if (!this.startChild(animation, this.curIndex) && bl && (eventHandler2 = animation.getOnFinished()) != null) {
                        eventHandler2.handle(new ActionEvent(this, null));
                    }
                }
                if (animation.getStatus() == Animation.Status.RUNNING) {
                    animation.doTimePulse(TickCalculation.sub(this.durations[this.curIndex], this.offsetTicks));
                }
                this.oldTicks = this.startTimes[this.curIndex + 1];
            }
            this.offsetTicks = 0L;
            ++this.curIndex;
            while (this.curIndex < n) {
                Animation animation3 = this.cachedChildren[this.curIndex];
                animation3.clipEnvelope.jumpTo(0L);
                if (this.startChild(animation3, this.curIndex)) {
                    animation3.doTimePulse(this.durations[this.curIndex]);
                } else {
                    eventHandler = animation3.getOnFinished();
                    if (eventHandler != null) {
                        eventHandler.handle(new ActionEvent(this, null));
                    }
                }
                this.oldTicks = this.startTimes[this.curIndex + 1];
                ++this.curIndex;
            }
            Animation animation4 = this.cachedChildren[this.curIndex];
            animation4.clipEnvelope.jumpTo(0L);
            if (this.startChild(animation4, this.curIndex)) {
                if (l3 >= this.startTimes[this.curIndex + 1]) {
                    animation4.doTimePulse(this.durations[this.curIndex]);
                    if (l3 == l2) {
                        this.curIndex = this.end;
                    }
                } else {
                    long l9 = TickCalculation.sub(l3, TickCalculation.add(this.startTimes[this.curIndex], this.delays[this.curIndex]));
                    animation4.doTimePulse(l9);
                }
            } else {
                eventHandler = animation4.getOnFinished();
                if (eventHandler != null) {
                    eventHandler.handle(new ActionEvent(this, null));
                }
            }
        } else {
            EventHandler<ActionEvent> eventHandler;
            if (animation != null) {
                long l10 = TickCalculation.add(this.startTimes[this.curIndex], this.delays[this.curIndex]);
                if (this.oldTicks >= this.startTimes[this.curIndex + 1] || this.oldTicks > l10 && animation.getStatus() == Animation.Status.STOPPED) {
                    EventHandler<ActionEvent> eventHandler3;
                    boolean bl;
                    boolean bl5 = bl = this.oldTicks >= this.startTimes[this.curIndex + 1];
                    if (bl) {
                        animation.clipEnvelope.jumpTo(Math.round((double)this.durations[this.curIndex] * this.rates[this.curIndex]));
                    }
                    if (!this.startChild(animation, this.curIndex) && bl && (eventHandler3 = animation.getOnFinished()) != null) {
                        eventHandler3.handle(new ActionEvent(this, null));
                    }
                }
                if (animation.getStatus() == Animation.Status.RUNNING) {
                    animation.doTimePulse(TickCalculation.sub(this.durations[this.curIndex], this.offsetTicks));
                }
                this.oldTicks = this.startTimes[this.curIndex];
            }
            this.offsetTicks = 0L;
            --this.curIndex;
            while (this.curIndex > n) {
                Animation animation5 = this.cachedChildren[this.curIndex];
                animation5.clipEnvelope.jumpTo(Math.round((double)this.durations[this.curIndex] * this.rates[this.curIndex]));
                if (this.startChild(animation5, this.curIndex)) {
                    animation5.doTimePulse(this.durations[this.curIndex]);
                } else {
                    eventHandler = animation5.getOnFinished();
                    if (eventHandler != null) {
                        eventHandler.handle(new ActionEvent(this, null));
                    }
                }
                this.oldTicks = this.startTimes[this.curIndex];
                --this.curIndex;
            }
            Animation animation6 = this.cachedChildren[this.curIndex];
            animation6.clipEnvelope.jumpTo(Math.round((double)this.durations[this.curIndex] * this.rates[this.curIndex]));
            if (this.startChild(animation6, this.curIndex)) {
                if (l3 <= TickCalculation.add(this.startTimes[this.curIndex], this.delays[this.curIndex])) {
                    animation6.doTimePulse(this.durations[this.curIndex]);
                    if (l3 == 0L) {
                        this.curIndex = -1;
                    }
                } else {
                    long l11 = TickCalculation.sub(this.startTimes[this.curIndex + 1], l3);
                    animation6.doTimePulse(l11);
                }
            } else {
                eventHandler = animation6.getOnFinished();
                if (eventHandler != null) {
                    eventHandler.handle(new ActionEvent(this, null));
                }
            }
        }
        this.oldTicks = l3;
    }

    @Override
    void doJumpTo(long l, long l2, boolean bl) {
        this.setCurrentTicks(l);
        Animation.Status status = this.getStatus();
        if (status == Animation.Status.STOPPED && !bl) {
            return;
        }
        this.sync(false);
        double d = this.calculateFraction(l, l2);
        long l3 = Math.max(0L, Math.min(this.getCachedInterpolator().interpolate(0L, l2, d), l2));
        int n = this.curIndex;
        this.curIndex = this.findNewIndex(l3);
        Animation animation = this.cachedChildren[this.curIndex];
        double d2 = this.getCurrentRate();
        long l4 = TickCalculation.add(this.startTimes[this.curIndex], this.delays[this.curIndex]);
        if (this.curIndex != n && status != Animation.Status.STOPPED) {
            Animation animation2;
            if (n != -1 && n != this.end && (animation2 = this.cachedChildren[n]).getStatus() != Animation.Status.STOPPED) {
                this.cachedChildren[n].doStop();
            }
            if (this.curIndex < n) {
                int n2;
                int n3 = n2 = n == this.end ? this.end - 1 : n;
                while (n2 > this.curIndex) {
                    this.cachedChildren[n2].doJumpTo(0L, this.durations[n2], true);
                    --n2;
                }
            } else {
                int n4;
                int n5 = n4 = n == -1 ? 0 : n;
                while (n4 < this.curIndex) {
                    this.cachedChildren[n4].doJumpTo(this.durations[n4], this.durations[n4], true);
                    ++n4;
                }
            }
            if (l3 >= l4) {
                this.startChild(animation, this.curIndex);
                if (status == Animation.Status.PAUSED) {
                    animation.doPause();
                }
            }
        }
        this.offsetTicks = n == this.curIndex ? (d2 == 0.0 ? (long)((double)this.offsetTicks + (double)(l3 - this.oldTicks) * Math.signum(this.clipEnvelope.getCurrentRate())) : (this.offsetTicks += d2 > 0.0 ? l3 - this.oldTicks : this.oldTicks - l3)) : (d2 == 0.0 ? (this.clipEnvelope.getCurrentRate() > 0.0 ? Math.max(0L, l3 - l4) : this.startTimes[this.curIndex] + this.durations[this.curIndex] - l3) : (d2 > 0.0 ? Math.max(0L, l3 - l4) : this.startTimes[this.curIndex + 1] - l3));
        animation.clipEnvelope.jumpTo(Math.round((double)TickCalculation.sub(l3, l4) * this.rates[this.curIndex]));
        this.oldTicks = l3;
    }

    private void jumpToEnd() {
        for (int i = 0; i < this.end; ++i) {
            if (this.forceChildSync[i]) {
                this.cachedChildren[i].sync(true);
            }
            this.cachedChildren[i].doJumpTo(this.durations[i], this.durations[i], true);
        }
    }

    private void jumpToBefore() {
        for (int i = this.end - 1; i >= 0; --i) {
            if (this.forceChildSync[i]) {
                this.cachedChildren[i].sync(true);
            }
            this.cachedChildren[i].doJumpTo(0L, this.durations[i], true);
        }
    }

    @Override
    protected void interpolate(double d) {
    }
}

