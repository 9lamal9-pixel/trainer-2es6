/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.traversal;

import com.sun.javafx.scene.traversal.TopMostTraversalEngine;
import javafx.scene.Parent;
import javafx.scene.Scene;

public final class SceneTraversalEngine
extends TopMostTraversalEngine {
    private final Scene scene;

    public SceneTraversalEngine(Scene scene) {
        this.scene = scene;
    }

    @Override
    protected Parent getRoot() {
        return this.scene.getRoot();
    }
}

