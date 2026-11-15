/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing.newimpl;

import com.sun.javafx.application.PlatformImpl;
import com.sun.javafx.embed.swing.SwingFXUtilsImplInterop;
import com.sun.javafx.tk.Toolkit;
import java.awt.EventQueue;
import java.awt.SecondaryLoop;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import jdk.swing.interop.DispatcherWrapper;

public class SwingFXUtilsImplInteropN
extends SwingFXUtilsImplInterop {
    @Override
    public void setFwDispatcher(EventQueue eventQueue) {
        DispatcherWrapper.setFwDispatcher(eventQueue, new FXDispatcher());
    }

    private static class FXDispatcher
    extends DispatcherWrapper {
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

