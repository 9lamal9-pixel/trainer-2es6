/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollToEvent;
import javafx.scene.control.SkinBase;
import javafx.scene.control.skin.VirtualFlow;

public abstract class VirtualContainerBase<C extends Control, I extends IndexedCell>
extends SkinBase<C> {
    private boolean itemCountDirty;
    private final VirtualFlow<I> flow = this.createVirtualFlow();

    public VirtualContainerBase(C c) {
        super(c);
        ((Node)c).addEventHandler(ScrollToEvent.scrollToTopIndex(), scrollToEvent -> {
            if (this.itemCountDirty) {
                this.updateItemCount();
                this.itemCountDirty = false;
            }
            this.flow.scrollToTop((Integer)scrollToEvent.getScrollTarget());
        });
    }

    protected abstract int getItemCount();

    protected abstract void updateItemCount();

    protected VirtualFlow<I> createVirtualFlow() {
        return new VirtualFlow();
    }

    protected final VirtualFlow<I> getVirtualFlow() {
        return this.flow;
    }

    protected final void markItemCountDirty() {
        this.itemCountDirty = true;
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        this.checkState();
    }

    double getMaxCellWidth(int n) {
        return this.snappedLeftInset() + this.flow.getMaxCellWidth(n) + this.snappedRightInset();
    }

    double getVirtualFlowPreferredHeight(int n) {
        double d = 1.0;
        for (int i = 0; i < n && i < this.getItemCount(); ++i) {
            d += this.flow.getCellLength(i);
        }
        return d + this.snappedTopInset() + this.snappedBottomInset();
    }

    void checkState() {
        if (this.itemCountDirty) {
            this.updateItemCount();
            this.itemCountDirty = false;
        }
    }

    void requestRebuildCells() {
        this.flow.rebuildCells();
    }
}

