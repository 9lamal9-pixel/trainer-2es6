/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.selector;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.core.LoggerContext;

public interface ContextSelector {
    public static final long DEFAULT_STOP_TIMEOUT = 50L;

    default public void shutdown(String fqcn, ClassLoader loader, boolean currentContext, boolean allContexts) {
        if (this.hasContext(fqcn, loader, currentContext)) {
            this.getContext(fqcn, loader, currentContext).stop(50L, TimeUnit.MILLISECONDS);
        }
    }

    default public boolean hasContext(String fqcn, ClassLoader loader, boolean currentContext) {
        return false;
    }

    public LoggerContext getContext(String var1, ClassLoader var2, boolean var3);

    public LoggerContext getContext(String var1, ClassLoader var2, boolean var3, URI var4);

    public List<LoggerContext> getLoggerContexts();

    public void removeContext(LoggerContext var1);
}

