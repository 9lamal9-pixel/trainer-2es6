/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.css;

import com.sun.javafx.collections.SetListenerHelper;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

abstract class BitSet<T>
implements ObservableSet<T> {
    private static final long[] EMPTY_SET = new long[0];
    private long[] bits = EMPTY_SET;
    private SetListenerHelper<T> listenerHelper;

    protected BitSet() {
    }

    @Override
    public int size() {
        int n = 0;
        if (this.bits.length > 0) {
            for (int i = 0; i < this.bits.length; ++i) {
                long l = this.bits[i];
                if (l == 0L) continue;
                n += Long.bitCount(l);
            }
        }
        return n;
    }

    @Override
    public boolean isEmpty() {
        if (this.bits.length > 0) {
            for (int i = 0; i < this.bits.length; ++i) {
                long l = this.bits[i];
                if (l == 0L) continue;
                return false;
            }
        }
        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>(){
            int next = -1;
            int element = 0;
            int index = -1;

            @Override
            public boolean hasNext() {
                long l;
                if (BitSet.this.bits == null || BitSet.this.bits.length == 0) {
                    return false;
                }
                boolean bl = false;
                do {
                    if (++this.next < 64) continue;
                    if (++this.element < BitSet.this.bits.length) {
                        this.next = 0;
                        continue;
                    }
                    return false;
                } while (!(bl = ((l = 1L << this.next) & BitSet.this.bits[this.element]) == l));
                if (bl) {
                    this.index = 64 * this.element + this.next;
                }
                return bl;
            }

            @Override
            public T next() {
                try {
                    return BitSet.this.getT(this.index);
                }
                catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    throw new NoSuchElementException("[" + this.element + "][" + this.next + "]");
                }
            }

            @Override
            public void remove() {
                try {
                    Object t = BitSet.this.getT(this.index);
                    BitSet.this.remove(t);
                }
                catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    throw new NoSuchElementException("[" + this.element + "][" + this.next + "]");
                }
            }
        };
    }

    @Override
    public boolean add(T t) {
        boolean bl;
        if (t == null) {
            return false;
        }
        int n = this.getIndex(t) / 64;
        long l = 1L << this.getIndex(t) % 64;
        if (n >= this.bits.length) {
            long[] lArray = new long[n + 1];
            System.arraycopy(this.bits, 0, lArray, 0, this.bits.length);
            this.bits = lArray;
        }
        long l2 = this.bits[n];
        this.bits[n] = l2 | l;
        boolean bl2 = bl = this.bits[n] != l2;
        if (bl && SetListenerHelper.hasListeners(this.listenerHelper)) {
            this.notifyObservers(t, false);
        }
        return bl;
    }

    @Override
    public boolean remove(Object object) {
        boolean bl;
        if (object == null) {
            return false;
        }
        T t = this.cast(object);
        int n = this.getIndex(t) / 64;
        long l = 1L << this.getIndex(t) % 64;
        if (n >= this.bits.length) {
            return false;
        }
        long l2 = this.bits[n];
        this.bits[n] = l2 & (l ^ 0xFFFFFFFFFFFFFFFFL);
        boolean bl2 = bl = this.bits[n] != l2;
        if (bl) {
            if (SetListenerHelper.hasListeners(this.listenerHelper)) {
                this.notifyObservers(t, true);
            }
            boolean bl3 = true;
            for (int i = 0; i < this.bits.length && bl3; bl3 &= this.bits[i] == 0L, ++i) {
            }
            if (bl3) {
                this.bits = EMPTY_SET;
            }
        }
        return bl;
    }

    @Override
    public boolean contains(Object object) {
        if (object == null) {
            return false;
        }
        T t = this.cast(object);
        int n = this.getIndex(t) / 64;
        long l = 1L << this.getIndex(t) % 64;
        return n < this.bits.length && (this.bits[n] & l) == l;
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        if (collection == null || this.getClass() != collection.getClass()) {
            return false;
        }
        BitSet bitSet = (BitSet)collection;
        if (this.bits.length == 0 && bitSet.bits.length == 0) {
            return true;
        }
        if (this.bits.length < bitSet.bits.length) {
            return false;
        }
        int n = bitSet.bits.length;
        for (int i = 0; i < n; ++i) {
            if ((this.bits[i] & bitSet.bits[i]) == bitSet.bits[i]) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> collection) {
        int n;
        if (collection == null || this.getClass() != collection.getClass()) {
            return false;
        }
        boolean bl = false;
        BitSet bitSet = (BitSet)collection;
        long[] lArray = this.bits;
        int n2 = lArray.length;
        long[] lArray2 = bitSet.bits;
        int n3 = lArray2.length;
        int n4 = n2 < n3 ? n3 : n2;
        long[] lArray3 = n4 > 0 ? new long[n4] : EMPTY_SET;
        for (n = 0; n < n4; ++n) {
            if (n < lArray.length && n < lArray2.length) {
                lArray3[n] = lArray[n] | lArray2[n];
                bl |= lArray3[n] != lArray[n];
                continue;
            }
            if (n < lArray.length) {
                lArray3[n] = lArray[n];
                bl |= false;
                continue;
            }
            lArray3[n] = lArray2[n];
            bl = true;
        }
        if (bl) {
            if (SetListenerHelper.hasListeners(this.listenerHelper)) {
                for (n = 0; n < n4; ++n) {
                    long l = 0L;
                    if (n < lArray.length && n < lArray2.length) {
                        l = (lArray[n] ^ 0xFFFFFFFFFFFFFFFFL) & lArray2[n];
                    } else {
                        if (n < lArray.length) continue;
                        l = lArray2[n];
                    }
                    for (int i = 0; i < 64; ++i) {
                        long l2 = 1L << i;
                        if ((l2 & l) != l2) continue;
                        T t = this.getT(n * 64 + i);
                        this.notifyObservers(t, false);
                    }
                }
            }
            this.bits = lArray3;
        }
        return bl;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        int n;
        if (collection == null || this.getClass() != collection.getClass()) {
            this.clear();
            return true;
        }
        boolean bl = false;
        BitSet bitSet = (BitSet)collection;
        long[] lArray = this.bits;
        int n2 = lArray.length;
        long[] lArray2 = bitSet.bits;
        int n3 = lArray2.length;
        int n4 = n2 < n3 ? n2 : n3;
        long[] lArray3 = n4 > 0 ? new long[n4] : EMPTY_SET;
        bl |= lArray.length > n4;
        boolean bl2 = true;
        for (n = 0; n < n4; ++n) {
            lArray3[n] = lArray[n] & lArray2[n];
            bl |= lArray3[n] != lArray[n];
            bl2 &= lArray3[n] == 0L;
        }
        if (bl) {
            if (SetListenerHelper.hasListeners(this.listenerHelper)) {
                for (n = 0; n < lArray.length; ++n) {
                    long l = 0L;
                    l = n < lArray2.length ? lArray[n] & (lArray2[n] ^ 0xFFFFFFFFFFFFFFFFL) : lArray[n];
                    for (int i = 0; i < 64; ++i) {
                        long l2 = 1L << i;
                        if ((l2 & l) != l2) continue;
                        T t = this.getT(n * 64 + i);
                        this.notifyObservers(t, true);
                    }
                }
            }
            this.bits = !bl2 ? lArray3 : EMPTY_SET;
        }
        return bl;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        int n;
        if (collection == null || this.getClass() != collection.getClass()) {
            return false;
        }
        boolean bl = false;
        BitSet bitSet = (BitSet)collection;
        long[] lArray = this.bits;
        int n2 = lArray.length;
        long[] lArray2 = bitSet.bits;
        int n3 = lArray2.length;
        int n4 = n2 < n3 ? n2 : n3;
        long[] lArray3 = n4 > 0 ? new long[n4] : EMPTY_SET;
        boolean bl2 = true;
        for (n = 0; n < n4; ++n) {
            lArray3[n] = lArray[n] & (lArray2[n] ^ 0xFFFFFFFFFFFFFFFFL);
            bl |= lArray3[n] != lArray[n];
            bl2 &= lArray3[n] == 0L;
        }
        if (bl) {
            if (SetListenerHelper.hasListeners(this.listenerHelper)) {
                for (n = 0; n < n4; ++n) {
                    long l = lArray[n] & lArray2[n];
                    for (int i = 0; i < 64; ++i) {
                        long l2 = 1L << i;
                        if ((l2 & l) != l2) continue;
                        T t = this.getT(n * 64 + i);
                        this.notifyObservers(t, true);
                    }
                }
            }
            this.bits = !bl2 ? lArray3 : EMPTY_SET;
        }
        return bl;
    }

    @Override
    public void clear() {
        for (int i = 0; i < this.bits.length; ++i) {
            long l = this.bits[i];
            for (int j = 0; j < 64; ++j) {
                long l2 = 1L << j;
                if ((l2 & l) != l2) continue;
                T t = this.getT(i * 64 + j);
                this.notifyObservers(t, true);
            }
        }
        this.bits = EMPTY_SET;
    }

    @Override
    public int hashCode() {
        int n = 7;
        if (this.bits.length > 0) {
            for (int i = 0; i < this.bits.length; ++i) {
                long l = this.bits[i];
                n = 71 * n + (int)(l ^ l >>> 32);
            }
        }
        return n;
    }

    @Override
    public boolean equals(Object object) {
        int n;
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        BitSet bitSet = (BitSet)object;
        int n2 = this.bits != null ? this.bits.length : 0;
        int n3 = n = bitSet.bits != null ? bitSet.bits.length : 0;
        if (n2 != n) {
            return false;
        }
        for (int i = 0; i < n2; ++i) {
            long l = this.bits[i];
            long l2 = bitSet.bits[i];
            if (l == l2) continue;
            return false;
        }
        return true;
    }

    protected abstract T getT(int var1);

    protected abstract int getIndex(T var1);

    protected abstract T cast(Object var1);

    protected long[] getBits() {
        return this.bits;
    }

    @Override
    public void addListener(SetChangeListener<? super T> setChangeListener) {
        if (setChangeListener != null) {
            this.listenerHelper = SetListenerHelper.addListener(this.listenerHelper, setChangeListener);
        }
    }

    @Override
    public void removeListener(SetChangeListener<? super T> setChangeListener) {
        if (setChangeListener != null) {
            SetListenerHelper.removeListener(this.listenerHelper, setChangeListener);
        }
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        if (invalidationListener != null) {
            this.listenerHelper = SetListenerHelper.addListener(this.listenerHelper, invalidationListener);
        }
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        if (invalidationListener != null) {
            SetListenerHelper.removeListener(this.listenerHelper, invalidationListener);
        }
    }

    private void notifyObservers(T t, boolean bl) {
        if (t != null && SetListenerHelper.hasListeners(this.listenerHelper)) {
            Change change = new Change(t, bl);
            SetListenerHelper.fireValueChangedEvent(this.listenerHelper, change);
        }
    }

    private class Change
    extends SetChangeListener.Change<T> {
        private static final boolean ELEMENT_ADDED = false;
        private static final boolean ELEMENT_REMOVED = true;
        private final T element;
        private final boolean removed;

        public Change(T t, boolean bl) {
            super(FXCollections.unmodifiableObservableSet(BitSet.this));
            this.element = t;
            this.removed = bl;
        }

        @Override
        public boolean wasAdded() {
            return !this.removed;
        }

        @Override
        public boolean wasRemoved() {
            return this.removed;
        }

        @Override
        public T getElementAdded() {
            return this.removed ? null : (Object)this.element;
        }

        @Override
        public T getElementRemoved() {
            return this.removed ? (Object)this.element : null;
        }
    }
}

