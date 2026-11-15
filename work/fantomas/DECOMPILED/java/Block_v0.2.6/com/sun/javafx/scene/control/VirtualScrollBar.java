/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control;

import com.sun.javafx.util.Utils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.skin.VirtualFlow;

public class VirtualScrollBar
extends ScrollBar {
    private final VirtualFlow flow;
    private boolean adjusting;
    private BooleanProperty virtual = new SimpleBooleanProperty(this, "virtual");

    public VirtualScrollBar(VirtualFlow virtualFlow) {
        this.flow = virtualFlow;
        super.valueProperty().addListener(observable -> {
            if (this.isVirtual() && !this.adjusting) {
                virtualFlow.setPosition(this.getValue());
            }
        });
    }

    public final void setVirtual(boolean bl) {
        this.virtual.set(bl);
    }

    public final boolean isVirtual() {
        return this.virtual.get();
    }

    public final BooleanProperty virtualProperty() {
        return this.virtual;
    }

    @Override
    public void decrement() {
        if (this.isVirtual()) {
            this.flow.scrollPixels(-10.0);
        } else {
            super.decrement();
        }
    }

    @Override
    public void increment() {
        if (this.isVirtual()) {
            this.flow.scrollPixels(10.0);
        } else {
            super.increment();
        }
    }

    @Override
    public void adjustValue(double d) {
        if (this.isVirtual()) {
            this.adjusting = true;
            double d2 = this.flow.getPosition();
            double d3 = (this.getMax() - this.getMin()) * Utils.clamp(0.0, d, 1.0) + this.getMin();
            if (d3 < d2) {
                Object t = this.flow.getFirstVisibleCell();
                if (t == null) {
                    return;
                }
                this.flow.scrollToBottom(t);
            } else if (d3 > d2) {
                Object t = this.flow.getLastVisibleCell();
                if (t == null) {
                    return;
                }
                this.flow.scrollToTop(t);
            }
            this.adjusting = false;
        } else {
            super.adjustValue(d);
        }
    }
}

