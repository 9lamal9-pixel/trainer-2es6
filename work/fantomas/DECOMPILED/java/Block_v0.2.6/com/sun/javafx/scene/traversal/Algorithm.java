/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.traversal;

import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.TraversalContext;
import javafx.scene.Node;

public interface Algorithm {
    public Node select(Node var1, Direction var2, TraversalContext var3);

    public Node selectFirst(TraversalContext var1);

    public Node selectLast(TraversalContext var1);
}

