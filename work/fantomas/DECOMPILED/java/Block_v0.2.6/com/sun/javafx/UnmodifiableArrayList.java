/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx;

import java.util.AbstractList;
import java.util.RandomAccess;

public class UnmodifiableArrayList<T>
extends AbstractList<T>
implements RandomAccess {
    private T[] elements;
    private final int size;

    public UnmodifiableArrayList(T[] TArray, int n) {
        assert (TArray != null ? n <= TArray.length : n == 0);
        this.size = n;
        this.elements = TArray;
    }

    @Override
    public T get(int n) {
        return this.elements[n];
    }

    @Override
    public int size() {
        return this.size;
    }
}

