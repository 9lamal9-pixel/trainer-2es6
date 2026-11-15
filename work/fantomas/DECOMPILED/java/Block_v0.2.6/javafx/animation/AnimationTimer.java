/*
 * Decompiled with CFR 0.152.
 */
package javafx.animation;

import com.sun.javafx.tk.Toolkit;
import com.sun.scenario.animation.AbstractMasterTimer;
import com.sun.scenario.animation.shared.TimerReceiver;
import java.security.AccessControlContext;
import java.security.AccessController;

public abstract class AnimationTimer {
    private final AbstractMasterTimer timer;
    private final AnimationTimerReceiver timerReceiver = new AnimationTimerReceiver();
    private boolean active;
    private AccessControlContext accessCtrlCtx = null;

    public AnimationTimer() {
        this.timer = Toolkit.getToolkit().getMasterTimer();
    }

    AnimationTimer(AbstractMasterTimer abstractMasterTimer) {
        this.timer = abstractMasterTimer;
    }

    public abstract void handle(long var1);

    public void start() {
        if (!this.active) {
            this.accessCtrlCtx = AccessController.getContext();
            this.timer.addAnimationTimer(this.timerReceiver);
            this.active = true;
        }
    }

    public void stop() {
        if (this.active) {
            this.timer.removeAnimationTimer(this.timerReceiver);
            this.active = false;
        }
    }

    private class AnimationTimerReceiver
    implements TimerReceiver {
        private AnimationTimerReceiver() {
        }

        @Override
        public void handle(long l) {
            if (AnimationTimer.this.accessCtrlCtx == null) {
                throw new IllegalStateException("Error: AccessControlContext not captured");
            }
            AccessController.doPrivileged(() -> {
                AnimationTimer.this.handle(l);
                return null;
            }, AnimationTimer.this.accessCtrlCtx);
        }
    }
}

