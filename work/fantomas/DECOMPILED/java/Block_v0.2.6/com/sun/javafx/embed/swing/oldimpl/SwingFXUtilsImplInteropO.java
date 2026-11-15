/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing.oldimpl;

import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.embed.swing.SwingFXUtilsImplInterop;
import com.sun.javafx.tk.Toolkit;
import java.awt.EventQueue;
import java.awt.SecondaryLoop;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import sun.awt.AWTAccessor;
import sun.awt.FwDispatcher;

public class SwingFXUtilsImplInteropO
extends SwingFXUtilsImplInterop {
    @Override
    public void setFwDispatcher(EventQueue eventQueue) {
        AWTAccessor.getEventQueueAccessor().setFwDispatcher(eventQueue, new FXDispatcher());
    }

    private static class FXDispatcher
    implements FwDispatcher {
        private FXDispatcher() {
        }

        @Override
        public boolean isDispatchThread() {
            return Platform.isFxApplicationThread();
        }

        @Override
        public void scheduleDispatch(Runnable runnable) {
            Platform.runLater(runnable);
        }

        @Override
        public SecondaryLoop createSecondaryLoop() {
            return new FwSecondaryLoop();
        }
    }

    private static class FwSecondaryLoop
    implements SecondaryLoop {
        private final AtomicBoolean isRunning = new AtomicBoolean(false);

        private FwSecondaryLoop() {
        }

        @Override
        public boolean enter() {
            if (this.isRunning.compareAndSet(false, true)) {
                PlatformImpl.runAndWait(() -> Toolkit.getToolkit().enterNestedEventLoop(this));
                return true;
            }
            return false;
        }

        @Override
        public boolean exit() {
            if (this.isRunning.compareAndSet(true, false)) {
                PlatformImpl.runAndWait(() -> Toolkit.getToolkit().exitNestedEventLoop(this, null));
                return true;
            }
            return false;
        }
    }
}

