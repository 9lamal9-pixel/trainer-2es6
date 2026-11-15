/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control;

import java.util.Collections;
import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class MultipleAdditionAndRemovedChange<E>
extends ListChangeListener.Change<E> {
    private static final int[] EMPTY_PERM = new int[0];
    private boolean invalid = true;
    private final List<E> addedElements;
    private final List<E> removedElements;
    private boolean iteratingThroughAdded = true;
    private boolean returnedRemovedElements = false;
    private int addedIndex = 0;
    private int from;
    private int to;

    public MultipleAdditionAndRemovedChange(List<E> list, List<E> list2, ObservableList<E> observableList) {
        super(observableList);
        this.addedElements = list;
        this.removedElements = list2;
    }

    @Override
    public boolean next() {
        if (this.invalid) {
            this.invalid = false;
        }
        if (this.addedIndex < this.addedElements.size()) {
            this.from = this.getList().indexOf(this.addedElements.get(this.addedIndex));
            this.to = this.from + 1;
            ++this.addedIndex;
            int n = 0;
            while (n + this.addedIndex < this.addedElements.size()) {
                E e = this.addedElements.get(n + this.addedIndex);
                if (e != this.getList().get(this.from + n)) {
                    this.to = this.from + 1 + n;
                    this.addedIndex += n;
                    break;
                }
                ++n;
            }
            return true;
        }
        if (!this.returnedRemovedElements) {
            this.returnedRemovedElements = true;
            this.iteratingThroughAdded = false;
            this.from = 0;
            this.to = 0;
            return !this.removedElements.isEmpty();
        }
        return false;
    }

    @Override
    public void reset() {
        this.invalid = true;
        this.from = 0;
        this.to = 0;
        this.addedIndex = 0;
        this.iteratingThroughAdded = true;
        this.returnedRemovedElements = false;
    }

    @Override
    public int getFrom() {
        this.checkState();
        return this.from;
    }

    @Override
    public int getTo() {
        this.checkState();
        return this.to;
    }

    @Override
    public List<E> getRemoved() {
        return this.iteratingThroughAdded ? Collections.emptyList() : this.removedElements;
    }

    @Override
    protected int[] getPermutation() {
        return EMPTY_PERM;
    }

    @Override
    public boolean wasAdded() {
        return this.iteratingThroughAdded && !this.addedElements.isEmpty();
    }

    @Override
    public boolean wasRemoved() {
        return !this.iteratingThroughAdded && !this.removedElements.isEmpty();
    }

    private void checkState() {
        if (this.invalid) {
            throw new IllegalStateException("Invalid Change state: next() must be called before inspecting the Change.");
        }
    }
}

