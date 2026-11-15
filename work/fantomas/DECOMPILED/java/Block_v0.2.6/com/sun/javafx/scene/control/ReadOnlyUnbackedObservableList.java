/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableListBase;

public abstract class ReadOnlyUnbackedObservableList<E>
extends ObservableListBase<E> {
    @Override
    public abstract E get(int var1);

    @Override
    public abstract int size();

    public void _beginChange() {
        this.beginChange();
    }

    public void _endChange() {
        this.endChange();
    }

    public void _nextUpdate(int n) {
        this.nextUpdate(n);
    }

    public void _nextSet(int n, E e) {
        this.nextSet(n, e);
    }

    public void _nextReplace(int n, int n2, List<? extends E> list) {
        this.nextReplace(n, n2, list);
    }

    public void _nextRemove(int n, List<? extends E> list) {
        this.nextRemove(n, list);
    }

    public void _nextRemove(E e) {
        int n = this.indexOf(e);
        this._nextRemove(n, e);
    }

    public void _nextRemove(int n, E e) {
        this.nextRemove(n, e);
    }

    public void _nextPermutation(int n, int n2, int[] nArray) {
        this.nextPermutation(n, n2, nArray);
    }

    public void _nextAdd(int n, int n2) {
        this.nextAdd(n, n2);
    }

    public void fireChange(Runnable runnable) {
        this._beginChange();
        runnable.run();
        this._endChange();
    }

    public void callObservers(ListChangeListener.Change<E> change) {
        this.fireChange(change);
    }

    @Override
    public int indexOf(Object object) {
        if (object == null) {
            return -1;
        }
        for (int i = 0; i < this.size(); ++i) {
            E e = this.get(i);
            if (!object.equals(e)) continue;
            return i;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object object) {
        if (object == null) {
            return -1;
        }
        for (int i = this.size() - 1; i >= 0; --i) {
            E e = this.get(i);
            if (!object.equals(e)) continue;
            return i;
        }
        return -1;
    }

    @Override
    public boolean contains(Object object) {
        return this.indexOf(object) != -1;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        for (Object obj : collection) {
            if (this.contains(obj)) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public ListIterator<E> listIterator() {
        return new SelectionListIterator(this);
    }

    @Override
    public ListIterator<E> listIterator(int n) {
        return new SelectionListIterator(this, n);
    }

    @Override
    public Iterator<E> iterator() {
        return new SelectionListIterator(this);
    }

    @Override
    public List<E> subList(final int n, final int n2) {
        if (n < 0 || n2 > this.size() || n > n2) {
            throw new IndexOutOfBoundsException("[ fromIndex: " + n + ", toIndex: " + n2 + ", size: " + this.size() + " ]");
        }
        final ReadOnlyUnbackedObservableList readOnlyUnbackedObservableList = this;
        return new ReadOnlyUnbackedObservableList<E>(){

            @Override
            public E get(int n3) {
                return readOnlyUnbackedObservableList.get(n3 + n);
            }

            @Override
            public int size() {
                return n2 - n;
            }
        };
    }

    @Override
    public Object[] toArray() {
        Object[] objectArray = new Object[this.size()];
        for (int i = 0; i < this.size(); ++i) {
            objectArray[i] = this.get(i);
        }
        return objectArray;
    }

    @Override
    public <T> T[] toArray(T[] TArray) {
        Object[] objectArray = this.toArray();
        int n = objectArray.length;
        if (TArray.length < n) {
            return Arrays.copyOf(objectArray, n, TArray.getClass());
        }
        System.arraycopy(objectArray, 0, TArray, 0, n);
        if (TArray.length > n) {
            TArray[n] = null;
        }
        return TArray;
    }

    @Override
    public String toString() {
        Iterator<E> iterator = this.iterator();
        if (!iterator.hasNext()) {
            return "[]";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[');
        while (true) {
            E e;
            stringBuilder.append((Object)((e = iterator.next()) == this ? "(this Collection)" : e));
            if (!iterator.hasNext()) {
                return stringBuilder.append(']').toString();
            }
            stringBuilder.append(", ");
        }
    }

    @Override
    public boolean add(E e) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void add(int n, E e) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean addAll(int n, Collection<? extends E> collection) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean addAll(E ... EArray) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public E set(int n, E e) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean setAll(Collection<? extends E> collection) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean setAll(E ... EArray) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public E remove(int n) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean remove(Object object) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void remove(int n, int n2) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean removeAll(E ... EArray) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean retainAll(E ... EArray) {
        throw new UnsupportedOperationException("Not supported.");
    }

    private static class SelectionListIterator<E>
    implements ListIterator<E> {
        private int pos;
        private final ReadOnlyUnbackedObservableList<E> list;

        public SelectionListIterator(ReadOnlyUnbackedObservableList<E> readOnlyUnbackedObservableList) {
            this(readOnlyUnbackedObservableList, 0);
        }

        public SelectionListIterator(ReadOnlyUnbackedObservableList<E> readOnlyUnbackedObservableList, int n) {
            this.list = readOnlyUnbackedObservableList;
            this.pos = n;
        }

        @Override
        public boolean hasNext() {
            return this.pos < this.list.size();
        }

        @Override
        public E next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            return this.list.get(this.pos++);
        }

        @Override
        public boolean hasPrevious() {
            return this.pos > 0;
        }

        @Override
        public E previous() {
            if (!this.hasPrevious()) {
                throw new NoSuchElementException();
            }
            return this.list.get(--this.pos);
        }

        @Override
        public int nextIndex() {
            return this.pos;
        }

        @Override
        public int previousIndex() {
            return this.pos - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void set(E e) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public void add(E e) {
            throw new UnsupportedOperationException("Not supported.");
        }
    }
}

