/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.collections;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class ImmutableObservableList<E>
extends AbstractList<E>
implements ObservableList<E> {
    private final E[] elements;

    public ImmutableObservableList(E ... EArray) {
        this.elements = EArray == null || EArray.length == 0 ? null : Arrays.copyOf(EArray, EArray.length);
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
    }

    @Override
    public void addListener(ListChangeListener<? super E> listChangeListener) {
    }

    @Override
    public void removeListener(ListChangeListener<? super E> listChangeListener) {
    }

    @Override
    public boolean addAll(E ... EArray) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setAll(E ... EArray) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean setAll(Collection<? extends E> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(E ... EArray) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(E ... EArray) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(int n, int n2) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E get(int n) {
        if (n < 0 || n >= this.size()) {
            throw new IndexOutOfBoundsException();
        }
        return this.elements[n];
    }

    @Override
    public int size() {
        return this.elements == null ? 0 : this.elements.length;
    }
}

