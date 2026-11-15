/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.collections;

import com.sun.javafx.collections.ListListenerHelper;
import com.sun.javafx.collections.SourceAdapterChange;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public abstract class VetoableListDecorator<E>
implements ObservableList<E> {
    private final ObservableList<E> list;
    private int modCount;
    private ListListenerHelper<E> helper;

    protected abstract void onProposedChange(List<E> var1, int ... var2);

    public VetoableListDecorator(ObservableList<E> observableList) {
        this.list = observableList;
        this.list.addListener((ListChangeListener.Change<? super E> change) -> ListListenerHelper.fireValueChangedEvent(this.helper, new SourceAdapterChange(this, change)));
    }

    @Override
    public void addListener(ListChangeListener<? super E> listChangeListener) {
        this.helper = ListListenerHelper.addListener(this.helper, listChangeListener);
    }

    @Override
    public void removeListener(ListChangeListener<? super E> listChangeListener) {
        this.helper = ListListenerHelper.removeListener(this.helper, listChangeListener);
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        this.helper = ListListenerHelper.addListener(this.helper, invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        this.helper = ListListenerHelper.removeListener(this.helper, invalidationListener);
    }

    @Override
    public boolean addAll(E ... EArray) {
        return this.addAll((Collection<? extends E>)Arrays.asList(EArray));
    }

    @Override
    public boolean setAll(E ... EArray) {
        return this.setAll((Collection<? extends E>)Arrays.asList(EArray));
    }

    @Override
    public boolean setAll(Collection<? extends E> collection) {
        this.onProposedChange(Collections.unmodifiableList(new ArrayList<E>(collection)), 0, this.size());
        try {
            ++this.modCount;
            this.list.setAll(collection);
            return true;
        }
        catch (Exception exception) {
            --this.modCount;
            throw exception;
        }
    }

    private void removeFromList(List<E> list, int n, Collection<?> collection, boolean bl) {
        int[] nArray = new int[2];
        int n2 = -1;
        for (int i = 0; i < list.size(); ++i) {
            E e = list.get(i);
            if (!(collection.contains(e) ^ bl)) continue;
            if (n2 == -1) {
                nArray[n2 + 1] = n + i;
                nArray[n2 + 2] = n + i + 1;
                n2 += 2;
                continue;
            }
            if (nArray[n2 - 1] == n + i) {
                nArray[n2 - 1] = n + i + 1;
                continue;
            }
            int[] nArray2 = new int[nArray.length + 2];
            System.arraycopy(nArray, 0, nArray2, 0, nArray.length);
            nArray = nArray2;
            nArray[n2 + 1] = n + i;
            nArray[n2 + 2] = n + i + 1;
            n2 += 2;
        }
        if (n2 != -1) {
            this.onProposedChange(Collections.emptyList(), nArray);
        }
    }

    @Override
    public boolean removeAll(E ... EArray) {
        return this.removeAll((Collection<?>)Arrays.asList(EArray));
    }

    @Override
    public boolean retainAll(E ... EArray) {
        return this.retainAll((Collection<?>)Arrays.asList(EArray));
    }

    @Override
    public void remove(int n, int n2) {
        this.onProposedChange(Collections.emptyList(), n, n2);
        try {
            ++this.modCount;
            this.list.remove(n, n2);
        }
        catch (Exception exception) {
            --this.modCount;
        }
    }

    @Override
    public int size() {
        return this.list.size();
    }

    @Override
    public boolean isEmpty() {
        return this.list.isEmpty();
    }

    @Override
    public boolean contains(Object object) {
        return this.list.contains(object);
    }

    @Override
    public Iterator<E> iterator() {
        return new VetoableIteratorDecorator(new ModCountAccessorImpl(), this.list.iterator(), 0);
    }

    @Override
    public Object[] toArray() {
        return this.list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] TArray) {
        return this.list.toArray(TArray);
    }

    @Override
    public boolean add(E e) {
        this.onProposedChange(Collections.singletonList(e), this.size(), this.size());
        try {
            ++this.modCount;
            this.list.add(e);
            return true;
        }
        catch (Exception exception) {
            --this.modCount;
            throw exception;
        }
    }

    @Override
    public boolean remove(Object object) {
        int n = this.list.indexOf(object);
        if (n != -1) {
            this.remove(n);
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return this.list.containsAll(collection);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        this.onProposedChange(Collections.unmodifiableList(new ArrayList<E>(collection)), this.size(), this.size());
        try {
            ++this.modCount;
            boolean bl = this.list.addAll(collection);
            if (!bl) {
                --this.modCount;
            }
            return bl;
        }
        catch (Exception exception) {
            --this.modCount;
            throw exception;
        }
    }

    @Override
    public boolean addAll(int n, Collection<? extends E> collection) {
        this.onProposedChange(Collections.unmodifiableList(new ArrayList<E>(collection)), n, n);
        try {
            ++this.modCount;
            boolean bl = this.list.addAll(n, collection);
            if (!bl) {
                --this.modCount;
            }
            return bl;
        }
        catch (Exception exception) {
            --this.modCount;
            throw exception;
        }
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        this.removeFromList(this, 0, collection, false);
        try {
            ++this.modCount;
            boolean bl = this.list.removeAll(collection);
            if (!bl) {
                --this.modCount;
            }
            return bl;
        }
        catch (Exception exception) {
            --this.modCount;
            throw exception;
        }
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        this.removeFromList(this, 0, collection, true);
        try {
            ++this.modCount;
            boolean bl = this.list.retainAll(collection);
            if (!bl) {
                --this.modCount;
            }
            return bl;
        }
        catch (Exception exception) {
            --this.modCount;
            throw exception;
        }
    }

    @Override
    public void clear() {
        this.onProposedChange(Collections.emptyList(), 0, this.size());
        try {
            ++this.modCount;
            this.list.clear();
        }
        catch (Exception exception) {
            --this.modCount;
            throw exception;
        }
    }

    @Override
    public E get(int n) {
        return this.list.get(n);
    }

    @Override
    public E set(int n, E e) {
        this.onProposedChange(Collections.singletonList(e), n, n + 1);
        return this.list.set(n, e);
    }

    @Override
    public void add(int n, E e) {
        this.onProposedChange(Collections.singletonList(e), n, n);
        try {
            ++this.modCount;
            this.list.add(n, e);
        }
        catch (Exception exception) {
            --this.modCount;
            throw exception;
        }
    }

    @Override
    public E remove(int n) {
        this.onProposedChange(Collections.emptyList(), n, n + 1);
        try {
            ++this.modCount;
            Object e = this.list.remove(n);
            return e;
        }
        catch (Exception exception) {
            --this.modCount;
            throw exception;
        }
    }

    @Override
    public int indexOf(Object object) {
        return this.list.indexOf(object);
    }

    @Override
    public int lastIndexOf(Object object) {
        return this.list.lastIndexOf(object);
    }

    @Override
    public ListIterator<E> listIterator() {
        return new VetoableListIteratorDecorator((ModCountAccessor)new ModCountAccessorImpl(), this.list.listIterator(), 0);
    }

    @Override
    public ListIterator<E> listIterator(int n) {
        return new VetoableListIteratorDecorator((ModCountAccessor)new ModCountAccessorImpl(), this.list.listIterator(n), n);
    }

    @Override
    public List<E> subList(int n, int n2) {
        return new VetoableSubListDecorator(new ModCountAccessorImpl(), this.list.subList(n, n2), n);
    }

    public String toString() {
        return this.list.toString();
    }

    @Override
    public boolean equals(Object object) {
        return this.list.equals(object);
    }

    @Override
    public int hashCode() {
        return this.list.hashCode();
    }

    private class ModCountAccessorImpl
    implements ModCountAccessor {
        @Override
        public int get() {
            return VetoableListDecorator.this.modCount;
        }

        @Override
        public int incrementAndGet() {
            return ++VetoableListDecorator.this.modCount;
        }

        @Override
        public int decrementAndGet() {
            return --VetoableListDecorator.this.modCount;
        }
    }

    private class VetoableListIteratorDecorator
    extends VetoableIteratorDecorator
    implements ListIterator<E> {
        private final ListIterator<E> lit;

        public VetoableListIteratorDecorator(ModCountAccessor modCountAccessor, ListIterator<E> listIterator, int n) {
            super(modCountAccessor, listIterator, n);
            this.lit = listIterator;
        }

        @Override
        public boolean hasPrevious() {
            this.checkForComodification();
            return this.lit.hasPrevious();
        }

        @Override
        public E previous() {
            this.checkForComodification();
            Object e = this.lit.previous();
            this.lastReturned = --this.cursor;
            return e;
        }

        @Override
        public int nextIndex() {
            this.checkForComodification();
            return this.lit.nextIndex();
        }

        @Override
        public int previousIndex() {
            this.checkForComodification();
            return this.lit.previousIndex();
        }

        @Override
        public void set(E e) {
            this.checkForComodification();
            if (this.lastReturned == -1) {
                throw new IllegalStateException();
            }
            VetoableListDecorator.this.onProposedChange(Collections.singletonList(e), this.offset + this.lastReturned, this.offset + this.lastReturned + 1);
            this.lit.set(e);
        }

        @Override
        public void add(E e) {
            this.checkForComodification();
            VetoableListDecorator.this.onProposedChange(Collections.singletonList(e), this.offset + this.cursor, this.offset + this.cursor);
            try {
                this.incrementModCount();
                this.lit.add(e);
            }
            catch (Exception exception) {
                this.decrementModCount();
                throw exception;
            }
            ++this.cursor;
        }
    }

    private class VetoableIteratorDecorator
    implements Iterator<E> {
        private final Iterator<E> it;
        private final ModCountAccessor modCountAccessor;
        private int modCount;
        protected final int offset;
        protected int cursor;
        protected int lastReturned;

        public VetoableIteratorDecorator(ModCountAccessor modCountAccessor, Iterator<E> iterator, int n) {
            this.modCountAccessor = modCountAccessor;
            this.modCount = modCountAccessor.get();
            this.it = iterator;
            this.offset = n;
        }

        @Override
        public boolean hasNext() {
            this.checkForComodification();
            return this.it.hasNext();
        }

        @Override
        public E next() {
            this.checkForComodification();
            Object e = this.it.next();
            this.lastReturned = this.cursor++;
            return e;
        }

        @Override
        public void remove() {
            this.checkForComodification();
            if (this.lastReturned == -1) {
                throw new IllegalStateException();
            }
            VetoableListDecorator.this.onProposedChange(Collections.emptyList(), this.offset + this.lastReturned, this.offset + this.lastReturned + 1);
            try {
                this.incrementModCount();
                this.it.remove();
            }
            catch (Exception exception) {
                this.decrementModCount();
                throw exception;
            }
            this.lastReturned = -1;
            --this.cursor;
        }

        protected void checkForComodification() {
            if (this.modCount != this.modCountAccessor.get()) {
                throw new ConcurrentModificationException();
            }
        }

        protected void incrementModCount() {
            this.modCount = this.modCountAccessor.incrementAndGet();
        }

        protected void decrementModCount() {
            this.modCount = this.modCountAccessor.decrementAndGet();
        }
    }

    private class VetoableSubListDecorator
    implements List<E> {
        private final List<E> subList;
        private final int offset;
        private final ModCountAccessor modCountAccessor;
        private int modCount;

        public VetoableSubListDecorator(ModCountAccessor modCountAccessor, List<E> list, int n) {
            this.modCountAccessor = modCountAccessor;
            this.modCount = modCountAccessor.get();
            this.subList = list;
            this.offset = n;
        }

        @Override
        public int size() {
            this.checkForComodification();
            return this.subList.size();
        }

        @Override
        public boolean isEmpty() {
            this.checkForComodification();
            return this.subList.isEmpty();
        }

        @Override
        public boolean contains(Object object) {
            this.checkForComodification();
            return this.subList.contains(object);
        }

        @Override
        public Iterator<E> iterator() {
            this.checkForComodification();
            return new VetoableIteratorDecorator(new ModCountAccessorImplSub(), this.subList.iterator(), this.offset);
        }

        @Override
        public Object[] toArray() {
            this.checkForComodification();
            return this.subList.toArray();
        }

        @Override
        public <T> T[] toArray(T[] TArray) {
            this.checkForComodification();
            return this.subList.toArray(TArray);
        }

        @Override
        public boolean add(E e) {
            this.checkForComodification();
            VetoableListDecorator.this.onProposedChange(Collections.singletonList(e), this.offset + this.size(), this.offset + this.size());
            try {
                this.incrementModCount();
                this.subList.add(e);
            }
            catch (Exception exception) {
                this.decrementModCount();
                throw exception;
            }
            return true;
        }

        @Override
        public boolean remove(Object object) {
            this.checkForComodification();
            int n = this.indexOf(object);
            if (n != -1) {
                this.remove(n);
                return true;
            }
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> collection) {
            this.checkForComodification();
            return this.subList.containsAll(collection);
        }

        @Override
        public boolean addAll(Collection<? extends E> collection) {
            this.checkForComodification();
            VetoableListDecorator.this.onProposedChange(Collections.unmodifiableList(new ArrayList(collection)), this.offset + this.size(), this.offset + this.size());
            try {
                this.incrementModCount();
                boolean bl = this.subList.addAll(collection);
                if (!bl) {
                    this.decrementModCount();
                }
                return bl;
            }
            catch (Exception exception) {
                this.decrementModCount();
                throw exception;
            }
        }

        @Override
        public boolean addAll(int n, Collection<? extends E> collection) {
            this.checkForComodification();
            VetoableListDecorator.this.onProposedChange(Collections.unmodifiableList(new ArrayList(collection)), this.offset + n, this.offset + n);
            try {
                this.incrementModCount();
                boolean bl = this.subList.addAll(n, collection);
                if (!bl) {
                    this.decrementModCount();
                }
                return bl;
            }
            catch (Exception exception) {
                this.decrementModCount();
                throw exception;
            }
        }

        @Override
        public boolean removeAll(Collection<?> collection) {
            this.checkForComodification();
            VetoableListDecorator.this.removeFromList(this, this.offset, collection, false);
            try {
                this.incrementModCount();
                boolean bl = this.subList.removeAll(collection);
                if (!bl) {
                    this.decrementModCount();
                }
                return bl;
            }
            catch (Exception exception) {
                this.decrementModCount();
                throw exception;
            }
        }

        @Override
        public boolean retainAll(Collection<?> collection) {
            this.checkForComodification();
            VetoableListDecorator.this.removeFromList(this, this.offset, collection, true);
            try {
                this.incrementModCount();
                boolean bl = this.subList.retainAll(collection);
                if (!bl) {
                    this.decrementModCount();
                }
                return bl;
            }
            catch (Exception exception) {
                this.decrementModCount();
                throw exception;
            }
        }

        @Override
        public void clear() {
            this.checkForComodification();
            VetoableListDecorator.this.onProposedChange(Collections.emptyList(), this.offset, this.offset + this.size());
            try {
                this.incrementModCount();
                this.subList.clear();
            }
            catch (Exception exception) {
                this.decrementModCount();
                throw exception;
            }
        }

        @Override
        public E get(int n) {
            this.checkForComodification();
            return this.subList.get(n);
        }

        @Override
        public E set(int n, E e) {
            this.checkForComodification();
            VetoableListDecorator.this.onProposedChange(Collections.singletonList(e), this.offset + n, this.offset + n + 1);
            return this.subList.set(n, e);
        }

        @Override
        public void add(int n, E e) {
            this.checkForComodification();
            VetoableListDecorator.this.onProposedChange(Collections.singletonList(e), this.offset + n, this.offset + n);
            try {
                this.incrementModCount();
                this.subList.add(n, e);
            }
            catch (Exception exception) {
                this.decrementModCount();
                throw exception;
            }
        }

        @Override
        public E remove(int n) {
            this.checkForComodification();
            VetoableListDecorator.this.onProposedChange(Collections.emptyList(), this.offset + n, this.offset + n + 1);
            try {
                this.incrementModCount();
                Object e = this.subList.remove(n);
                return e;
            }
            catch (Exception exception) {
                this.decrementModCount();
                throw exception;
            }
        }

        @Override
        public int indexOf(Object object) {
            this.checkForComodification();
            return this.subList.indexOf(object);
        }

        @Override
        public int lastIndexOf(Object object) {
            this.checkForComodification();
            return this.subList.lastIndexOf(object);
        }

        @Override
        public ListIterator<E> listIterator() {
            this.checkForComodification();
            return new VetoableListIteratorDecorator((ModCountAccessor)new ModCountAccessorImplSub(), this.subList.listIterator(), this.offset);
        }

        @Override
        public ListIterator<E> listIterator(int n) {
            this.checkForComodification();
            return new VetoableListIteratorDecorator((ModCountAccessor)new ModCountAccessorImplSub(), this.subList.listIterator(n), this.offset + n);
        }

        @Override
        public List<E> subList(int n, int n2) {
            this.checkForComodification();
            return new VetoableSubListDecorator(new ModCountAccessorImplSub(), this.subList.subList(n, n2), this.offset + n);
        }

        public String toString() {
            this.checkForComodification();
            return this.subList.toString();
        }

        @Override
        public boolean equals(Object object) {
            this.checkForComodification();
            return this.subList.equals(object);
        }

        @Override
        public int hashCode() {
            this.checkForComodification();
            return this.subList.hashCode();
        }

        private void checkForComodification() {
            if (this.modCount != this.modCountAccessor.get()) {
                throw new ConcurrentModificationException();
            }
        }

        private void incrementModCount() {
            this.modCount = this.modCountAccessor.incrementAndGet();
        }

        private void decrementModCount() {
            this.modCount = this.modCountAccessor.decrementAndGet();
        }

        private class ModCountAccessorImplSub
        implements ModCountAccessor {
            private ModCountAccessorImplSub() {
            }

            @Override
            public int get() {
                return VetoableSubListDecorator.this.modCount;
            }

            @Override
            public int incrementAndGet() {
                return VetoableSubListDecorator.this.modCount = VetoableSubListDecorator.this.modCountAccessor.incrementAndGet();
            }

            @Override
            public int decrementAndGet() {
                return VetoableSubListDecorator.this.modCount = VetoableSubListDecorator.this.modCountAccessor.decrementAndGet();
            }
        }
    }

    private static interface ModCountAccessor {
        public int get();

        public int incrementAndGet();

        public int decrementAndGet();
    }
}

