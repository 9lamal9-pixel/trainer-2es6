/*
 * Decompiled with CFR 0.152.
 */
package javafx.animation;

import com.sun.scenario.animation.shared.AnimationAccessor;
import javafx.animation.Animation;

final class AnimationAccessorImpl
extends AnimationAccessor {
    AnimationAccessorImpl() {
    }

    @Override
    public void setCurrentRate(Animation animation, double d) {
        animation.setCurrentRate(d);
    }

    @Override
    public void playTo(Animation animation, long l, long l2) {
        animation.doPlayTo(l, l2);
    }

    @Override
    public void jumpTo(Animation animation, long l, long l2, boolean bl) {
        animation.doJumpTo(l, l2, bl);
    }

    @Override
    public void finished(Animation animation) {
        animation.finished();
    }

    @Override
    public void setCurrentTicks(Animation animation, long l) {
        animation.setCurrentTicks(l);
    }
}

