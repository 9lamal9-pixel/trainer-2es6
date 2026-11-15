/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.collections;

import com.sun.javafx.collections.ElementObserver;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.collections.WeakListChangeListener;
import javafx.util.Callback;

public final class ElementObservableListDecorator<E>
extends ObservableListBase<E>
implements ObservableList<E> {
    private final ObservableList<E> decoratedList;
    private final ListChangeListener<E> listener;
    private ElementObserver<E> observer;

    public ElementObservableListDecorator(ObservableList<E> observableList, Callback<E, Observable[]> callback) {
        this.observer = new ElementObserver<E>(callback, new Callback<E, InvalidationListener>(){

            @Override
            public InvalidationListener call(final E e) {
                return new InvalidationListener(){

                    @Override
                    public void invalidated(Observable observable) {
                        int n;
                        ElementObservableListDecorator.this.beginChange();
                        if (ElementObservableListDecorator.this.decoratedList instanceof RandomAccess) {
                            int n2 = ElementObservableListDecorator.this.size();
                            for (n = 0; n < n2; ++n) {
                                if (ElementObservableListDecorator.this.get(n) != e) continue;
                                ElementObservableListDecorator.this.nextUpdate(n);
                            }
                        } else {
                            Iterator iterator = ElementObservableListDecorator.this.iterator();
                            while (iterator.hasNext()) {
                                if (iterator.next() == e) {
                                    ElementObservableListDecorator.this.nextUpdate(n);
                                }
                                ++n;
                            }
                        }
                        ElementObservableListDecorator.this.endChange();
                    }
                };
            }
        }, this);
        this.decoratedList = observableList;
        int n = this.decoratedList.size();
        for (int i = 0; i < n; ++i) {
            this.observer.attachListener(this.decoratedList.get(i));
        }
        this.listener = new ListChangeListener<E>(){

            @Override
            public void onChanged(ListChangeListener.Change<? extends E> change) {
                while (change.next()) {
                    int n;
                    if (!change.wasAdded() && !change.wasRemoved()) continue;
                    int n2 = change.getRemovedSize();
                    List list = change.getRemoved();
                    for (n = 0; n < n2; ++n) {
                        ElementObservableListDecorator.this.observer.detachListener(list.get(n));
                    }
                    if (ElementObservableListDecorator.this.decoratedList instanceof RandomAccess) {
                        n = change.getTo();
                        for (int i = change.getFrom(); i < n; ++i) {
                            ElementObservableListDecorator.this.observer.attachListener(ElementObservableListDecorator.this.decoratedList.get(i));
                        }
                        continue;
                    }
                    for (Object e : change.getAddedSubList()) {
                        ElementObservableListDecorator.this.observer.attachListener(e);
                    }
                }
                change.reset();
                ElementObservableListDecorator.this.fireChange(change);
            }
        };
        this.decoratedList.addListener(new WeakListChangeListener<E>(this.listener));
    }

    @Override
    public <T> T[] toArray(T[] TArray) {
        return this.decoratedList.toArray(TArray);
    }

    @Override
    public Object[] toArray() {
        return this.decoratedList.toArray();
    }

    @Override
    public List<E> subList(int n, int n2) {
        return this.decoratedList.subList(n, n2);
    }

    @Override
    public int size() {
        return this.decoratedList.size();
    }

    @Override
    public E set(int n, E e) {
        return this.decoratedList.set(n, e);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return this.decoratedList.retainAll(collection);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return this.decoratedList.removeAll(collection);
    }

    @Override
    public E remove(int n) {
        return this.decoratedList.remove(n);
    }

    @Override
    public boolean remove(Object object) {
        return this.decoratedList.remove(object);
    }

    @Override
    public ListIterator<E> listIterator(int n) {
        return this.decoratedList.listIterator(n);
    }

    @Override
    public ListIterator<E> listIterator() {
        return this.decoratedList.listIterator();
    }

    @Override
    public int lastIndexOf(Object object) {
        return this.decoratedList.lastIndexOf(object);
    }

    @Override
    public Iterator<E> iterator() {
        return this.decoratedList.iterator();
    }

    @Override
    public boolean isEmpty() {
        return this.decoratedList.isEmpty();
    }

    @Override
    public int indexOf(Object object) {
        return this.decoratedList.indexOf(object);
    }

    @Override
    public E get(int n) {
        return this.decoratedList.get(n);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return this.decoratedList.containsAll(collection);
    }

    @Override
    public boolean contains(Object object) {
        return this.decoratedList.contains(object);
    }

    @Override
    public void clear() {
        this.decoratedList.clear();
    }

    @Override
    public boolean addAll(int n, Collection<? extends E> collection) {
        return this.decoratedList.addAll(n, collection);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        return this.decoratedList.addAll(collection);
    }

    @Override
    public void add(int n, E e) {
        this.decoratedList.add(n, e);
    }

    @Override
    public boolean add(E e) {
        return this.decoratedList.add(e);
    }

    @Override
    public boolean setAll(Collection<? extends E> collection) {
        return this.decoratedList.setAll(collection);
    }

    @Override
    public boolean setAll(E ... EArray) {
        return this.decoratedList.setAll(EArray);
    }

    @Override
    public boolean retainAll(E ... EArray) {
        return this.decoratedList.retainAll(EArray);
    }

    @Override
    public boolean removeAll(E ... EArray) {
        return this.decoratedList.removeAll(EArray);
    }

    @Override
    public void remove(int n, int n2) {
        this.decoratedList.remove(n, n2);
    }

    @Override
    public boolean addAll(E ... EArray) {
        return this.decoratedList.addAll(EArray);
    }
}

