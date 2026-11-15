/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.event;

import com.sun.javafx.event.CompositeEventTarget;
import com.sun.javafx.event.EventDispatchTree;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javafx.event.EventDispatchChain;
import javafx.event.EventTarget;

public class CompositeEventTargetImpl
implements CompositeEventTarget {
    private final Set<EventTarget> eventTargets;

    public CompositeEventTargetImpl(EventTarget ... eventTargetArray) {
        HashSet<EventTarget> hashSet = new HashSet<EventTarget>(eventTargetArray.length);
        hashSet.addAll(Arrays.asList(eventTargetArray));
        this.eventTargets = Collections.unmodifiableSet(hashSet);
    }

    @Override
    public Set<EventTarget> getTargets() {
        return this.eventTargets;
    }

    @Override
    public boolean containsTarget(EventTarget eventTarget) {
        return this.eventTargets.contains(eventTarget);
    }

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain eventDispatchChain) {
        EventDispatchTree eventDispatchTree = (EventDispatchTree)eventDispatchChain;
        for (EventTarget eventTarget : this.eventTargets) {
            EventDispatchTree eventDispatchTree2 = eventDispatchTree.createTree();
            eventDispatchTree = eventDispatchTree.mergeTree((EventDispatchTree)eventTarget.buildEventDispatchChain(eventDispatchTree2));
        }
        return eventDispatchTree;
    }
}

