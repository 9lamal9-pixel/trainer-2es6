/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.collections;

import com.sun.javafx.collections.SetListenerHelper;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import javafx.beans.InvalidationListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

public class ObservableSetWrapper<E>
implements ObservableSet<E> {
    private final Set<E> backingSet;
    private SetListenerHelper<E> listenerHelper;

    public ObservableSetWrapper(Set<E> set) {
        this.backingSet = set;
    }

    private void callObservers(SetChangeListener.Change<E> change) {
        SetListenerHelper.fireValueChangedEvent(this.listenerHelper, change);
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        this.listenerHelper = SetListenerHelper.addListener(this.listenerHelper, invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        this.listenerHelper = SetListenerHelper.removeListener(this.listenerHelper, invalidationListener);
    }

    @Override
    public void addListener(SetChangeListener<? super E> setChangeListener) {
        this.listenerHelper = SetListenerHelper.addListener(this.listenerHelper, setChangeListener);
    }

    @Override
    public void removeListener(SetChangeListener<? super E> setChangeListener) {
        this.listenerHelper = SetListenerHelper.removeListener(this.listenerHelper, setChangeListener);
    }

    @Override
    public int size() {
        return this.backingSet.size();
    }

    @Override
    public boolean isEmpty() {
        return this.backingSet.isEmpty();
    }

    @Override
    public boolean contains(Object object) {
        return this.backingSet.contains(object);
    }

    @Override
    public Iterator iterator() {
        return new Iterator<E>(){
            private final Iterator<E> backingIt;
            private E lastElement;
            {
                this.backingIt = ObservableSetWrapper.this.backingSet.iterator();
            }

            @Override
            public boolean hasNext() {
                return this.backingIt.hasNext();
            }

            @Override
            public E next() {
                this.lastElement = this.backingIt.next();
                return this.lastElement;
            }

            @Override
            public void remove() {
                this.backingIt.remove();
                ObservableSetWrapper.this.callObservers(new SimpleRemoveChange(this.lastElement));
            }
        };
    }

    @Override
    public Object[] toArray() {
        return this.backingSet.toArray();
    }

    @Override
    public <T> T[] toArray(T[] TArray) {
        return this.backingSet.toArray(TArray);
    }

    @Override
    public boolean add(E e) {
        boolean bl = this.backingSet.add(e);
        if (bl) {
            this.callObservers(new SimpleAddChange(e));
        }
        return bl;
    }

    @Override
    public boolean remove(Object object) {
        boolean bl = this.backingSet.remove(object);
        if (bl) {
            this.callObservers(new SimpleRemoveChange(object));
        }
        return bl;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return this.backingSet.containsAll(collection);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        boolean bl = false;
        for (E e : collection) {
            bl |= this.add(e);
        }
        return bl;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return this.removeRetain(collection, false);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return this.removeRetain(collection, true);
    }

    private boolean removeRetain(Collection<?> collection, boolean bl) {
        boolean bl2 = false;
        Iterator<E> iterator = this.backingSet.iterator();
        while (iterator.hasNext()) {
            E e = iterator.next();
            if (bl != collection.contains(e)) continue;
            bl2 = true;
            iterator.remove();
            this.callObservers(new SimpleRemoveChange(e));
        }
        return bl2;
    }

    @Override
    public void clear() {
        Iterator<E> iterator = this.backingSet.iterator();
        while (iterator.hasNext()) {
            E e = iterator.next();
            iterator.remove();
            this.callObservers(new SimpleRemoveChange(e));
        }
    }

    public String toString() {
        return this.backingSet.toString();
    }

    @Override
    public boolean equals(Object object) {
        return this.backingSet.equals(object);
    }

    @Override
    public int hashCode() {
        return this.backingSet.hashCode();
    }

    private class SimpleRemoveChange
    extends SetChangeListener.Change<E> {
        private final E removed;

        public SimpleRemoveChange(E e) {
            super(ObservableSetWrapper.this);
            this.removed = e;
        }

        @Override
        public boolean wasAdded() {
            return false;
        }

        @Override
        public boolean wasRemoved() {
            return true;
        }

        @Override
        public E getElementAdded() {
            return null;
        }

        @Override
        public E getElementRemoved() {
            return this.removed;
        }

        public String toString() {
            return "removed " + this.removed;
        }
    }

    private class SimpleAddChange
    extends SetChangeListener.Change<E> {
        private final E added;

        public SimpleAddChange(E e) {
            super(ObservableSetWrapper.this);
            this.added = e;
        }

        @Override
        public boolean wasAdded() {
            return true;
        }

        @Override
        public boolean wasRemoved() {
            return false;
        }

        @Override
        public E getElementAdded() {
            return this.added;
        }

        @Override
        public E getElementRemoved() {
            return null;
        }

        public String toString() {
            return "added " + this.added;
        }
    }
}

