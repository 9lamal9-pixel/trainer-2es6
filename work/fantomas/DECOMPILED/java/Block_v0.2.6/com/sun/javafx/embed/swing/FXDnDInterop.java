/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing;

import java.awt.Component;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSource;
import java.awt.dnd.DropTarget;
import javafx.embed.swing.SwingNode;

public abstract class FXDnDInterop {
    public abstract Component findComponentAt(Object var1, int var2, int var3, boolean var4);

    public abstract boolean isCompEqual(Component var1, Object var2);

    public abstract int convertModifiersToDropAction(int var1, int var2);

    public abstract Object createDragSourceContext(DragGestureEvent var1);

    public abstract <T extends DragGestureRecognizer> T createDragGestureRecognizer(DragSource var1, Component var2, int var3, DragGestureListener var4);

    public abstract void addDropTarget(DropTarget var1, SwingNode var2);

    public abstract void removeDropTarget(DropTarget var1, SwingNode var2);

    public abstract void setNode(SwingNode var1);
}

