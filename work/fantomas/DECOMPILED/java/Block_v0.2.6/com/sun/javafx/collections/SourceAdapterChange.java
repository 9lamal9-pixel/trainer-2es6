/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.collections;

import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class SourceAdapterChange<E>
extends ListChangeListener.Change<E> {
    private final ListChangeListener.Change<? extends E> change;
    private int[] perm;

    public SourceAdapterChange(ObservableList<E> observableList, ListChangeListener.Change<? extends E> change) {
        super(observableList);
        this.change = change;
    }

    @Override
    public boolean next() {
        this.perm = null;
        return this.change.next();
    }

    @Override
    public void reset() {
        this.change.reset();
    }

    @Override
    public int getTo() {
        return this.change.getTo();
    }

    @Override
    public List<E> getRemoved() {
        return this.change.getRemoved();
    }

    @Override
    public int getFrom() {
        return this.change.getFrom();
    }

    @Override
    public boolean wasUpdated() {
        return this.change.wasUpdated();
    }

    @Override
    protected int[] getPermutation() {
        if (this.perm == null) {
            if (this.change.wasPermutated()) {
                int n = this.change.getFrom();
                int n2 = this.change.getTo() - n;
                this.perm = new int[n2];
                for (int i = 0; i < n2; ++i) {
                    this.perm[i] = this.change.getPermutation(n + i);
                }
            } else {
                this.perm = new int[0];
            }
        }
        return this.perm;
    }

    public String toString() {
        return this.change.toString();
    }
}

