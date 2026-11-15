/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.collections;

import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

public class SetAdapterChange<E>
extends SetChangeListener.Change<E> {
    private final SetChangeListener.Change<? extends E> change;

    public SetAdapterChange(ObservableSet<E> observableSet, SetChangeListener.Change<? extends E> change) {
        super(observableSet);
        this.change = change;
    }

    public String toString() {
        return this.change.toString();
    }

    @Override
    public boolean wasAdded() {
        return this.change.wasAdded();
    }

    @Override
    public boolean wasRemoved() {
        return this.change.wasRemoved();
    }

    @Override
    public E getElementAdded() {
        return this.change.getElementAdded();
    }

    @Override
    public E getElementRemoved() {
        return this.change.getElementRemoved();
    }
}

