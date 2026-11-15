/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.async;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.async.AsyncQueueFullPolicy;
import org.apache.logging.log4j.core.async.EventRoute;

public class DefaultAsyncQueueFullPolicy
implements AsyncQueueFullPolicy {
    @Override
    public EventRoute getRoute(long backgroundThreadId, Level level) {
        if (Thread.currentThread().getId() == backgroundThreadId) {
            return EventRoute.SYNCHRONOUS;
        }
        return EventRoute.ENQUEUE;
    }
}

