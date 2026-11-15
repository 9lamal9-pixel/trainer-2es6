/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.event;

import javafx.event.EventDispatchChain;
import javafx.event.EventDispatcher;

public interface EventDispatchTree
extends EventDispatchChain {
    public EventDispatchTree createTree();

    public EventDispatchTree mergeTree(EventDispatchTree var1);

    @Override
    public EventDispatchTree append(EventDispatcher var1);

    @Override
    public EventDispatchTree prepend(EventDispatcher var1);
}

