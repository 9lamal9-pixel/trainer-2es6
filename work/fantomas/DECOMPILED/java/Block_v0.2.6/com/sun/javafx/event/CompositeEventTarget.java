/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.event;

import java.util.Set;
import javafx.event.EventTarget;

public interface CompositeEventTarget
extends EventTarget {
    public Set<EventTarget> getTargets();

    public boolean containsTarget(EventTarget var1);
}

