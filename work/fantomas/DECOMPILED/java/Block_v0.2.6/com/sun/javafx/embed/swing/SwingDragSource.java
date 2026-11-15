/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing;

import com.sun.javafx.embed.EmbeddedSceneDSInterface;
import com.sun.javafx.embed.swing.CachingTransferable;
import com.sun.javafx.embed.swing.SwingDnD;
import com.sun.javafx.tk.Toolkit;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.util.Set;
import javafx.scene.input.TransferMode;

final class SwingDragSource
extends CachingTransferable
implements EmbeddedSceneDSInterface {
    private int sourceActions;

    SwingDragSource() {
    }

    void updateContents(DropTargetDragEvent dropTargetDragEvent, boolean bl) {
        this.sourceActions = dropTargetDragEvent.getSourceActions();
        this.updateData(dropTargetDragEvent.getTransferable(), bl);
    }

    void updateContents(DropTargetDropEvent dropTargetDropEvent, boolean bl) {
        this.sourceActions = dropTargetDropEvent.getSourceActions();
        this.updateData(dropTargetDropEvent.getTransferable(), bl);
    }

    @Override
    public Set<TransferMode> getSupportedActions() {
        assert (Toolkit.getToolkit().isFxUserThread());
        return SwingDnD.dropActionsToTransferModes(this.sourceActions);
    }

    @Override
    public void dragDropEnd(TransferMode transferMode) {
        throw new UnsupportedOperationException();
    }
}

