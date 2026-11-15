/*
 * Decompiled with CFR 0.152.
 */
package javafx.collections.transformation;

import com.sun.javafx.collections.NonIterableChange;
import com.sun.javafx.collections.SortHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;
import java.util.function.Predicate;
import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

public final class FilteredList<E>
extends TransformationList<E, E> {
    private int[] filtered;
    private int size;
    private SortHelper helper;
    private static final Predicate ALWAYS_TRUE = object -> true;
    private ObjectProperty<Predicate<? super E>> predicate;

    public FilteredList(@NamedArg(value="source") ObservableList<E> observableList, @NamedArg(value="predicate") Predicate<? super E> predicate) {
        super(observableList);
        this.filtered = new int[observableList.size() * 3 / 2 + 1];
        if (predicate != null) {
            this.setPredicate(predicate);
        } else {
            this.size = 0;
            while (this.size < observableList.size()) {
                this.filtered[this.size] = this.size;
                ++this.size;
            }
        }
    }

    public FilteredList(@NamedArg(value="source") ObservableList<E> observableList) {
        this(observableList, null);
    }

    public final ObjectProperty<Predicate<? super E>> predicateProperty() {
        if (this.predicate == null) {
            this.predicate = new ObjectPropertyBase<Predicate<? super E>>(){

                @Override
                protected void invalidated() {
                    FilteredList.this.refilter();
                }

                @Override
                public Object getBean() {
                    return FilteredList.this;
                }

                @Override
                public String getName() {
                    return "predicate";
                }
            };
        }
        return this.predicate;
    }

    public final Predicate<? super E> getPredicate() {
        return this.predicate == null ? null : (Predicate)this.predicate.get();
    }

    public final void setPredicate(Predicate<? super E> predicate) {
        this.predicateProperty().set(predicate);
    }

    private Predicate<? super E> getPredicateImpl() {
        if (this.getPredicate() != null) {
            return this.getPredicate();
        }
        return ALWAYS_TRUE;
    }

    @Override
    protected void sourceChanged(ListChangeListener.Change<? extends E> change) {
        this.beginChange();
        while (change.next()) {
            if (change.wasPermutated()) {
                this.permutate(change);
                continue;
            }
            if (change.wasUpdated()) {
                this.update(change);
                continue;
            }
            this.addRemove(change);
        }
        this.endChange();
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public E get(int n) {
        if (n >= this.size) {
            throw new IndexOutOfBoundsException();
        }
        return this.getSource().get(this.filtered[n]);
    }

    @Override
    public int getSourceIndex(int n) {
        if (n >= this.size) {
            throw new IndexOutOfBoundsException();
        }
        return this.filtered[n];
    }

    @Override
    public int getViewIndex(int n) {
        return Arrays.binarySearch(this.filtered, 0, this.size, n);
    }

    private SortHelper getSortHelper() {
        if (this.helper == null) {
            this.helper = new SortHelper();
        }
        return this.helper;
    }

    private int findPosition(int n) {
        if (this.filtered.length == 0) {
            return 0;
        }
        if (n == 0) {
            return 0;
        }
        int n2 = Arrays.binarySearch(this.filtered, 0, this.size, n);
        if (n2 < 0) {
            n2 ^= 0xFFFFFFFF;
        }
        return n2;
    }

    private void ensureSize(int n) {
        if (this.filtered.length < n) {
            int[] nArray = new int[n * 3 / 2 + 1];
            System.arraycopy(this.filtered, 0, nArray, 0, this.size);
            this.filtered = nArray;
        }
    }

    private void updateIndexes(int n, int n2) {
        int n3 = n;
        while (n3 < this.size) {
            int n4 = n3++;
            this.filtered[n4] = this.filtered[n4] + n2;
        }
    }

    private void permutate(ListChangeListener.Change<? extends E> change) {
        int n = this.findPosition(change.getFrom());
        int n2 = this.findPosition(change.getTo());
        if (n2 > n) {
            for (int i = n; i < n2; ++i) {
                this.filtered[i] = change.getPermutation(this.filtered[i]);
            }
            int[] nArray = this.getSortHelper().sort(this.filtered, n, n2);
            this.nextPermutation(n, n2, nArray);
        }
    }

    private void addRemove(ListChangeListener.Change<? extends E> change) {
        int n;
        Predicate<E> predicate = this.getPredicateImpl();
        this.ensureSize(this.getSource().size());
        int n2 = this.findPosition(change.getFrom());
        int n3 = this.findPosition(change.getFrom() + change.getRemovedSize());
        for (n = n2; n < n3; ++n) {
            this.nextRemove(n2, change.getRemoved().get(this.filtered[n] - change.getFrom()));
        }
        this.updateIndexes(n3, change.getAddedSize() - change.getRemovedSize());
        n = n2;
        int n4 = change.getFrom();
        ListIterator listIterator = this.getSource().listIterator(n4);
        while (n < n3 && listIterator.nextIndex() < change.getTo()) {
            if (!predicate.test(listIterator.next())) continue;
            this.filtered[n] = listIterator.previousIndex();
            this.nextAdd(n, n + 1);
            ++n;
        }
        if (n < n3) {
            System.arraycopy(this.filtered, n3, this.filtered, n, this.size - n3);
            this.size -= n3 - n;
        } else {
            while (listIterator.nextIndex() < change.getTo()) {
                if (predicate.test(listIterator.next())) {
                    System.arraycopy(this.filtered, n, this.filtered, n + 1, this.size - n);
                    this.filtered[n] = listIterator.previousIndex();
                    this.nextAdd(n, n + 1);
                    ++n;
                    ++this.size;
                }
                ++n4;
            }
        }
    }

    private void update(ListChangeListener.Change<? extends E> change) {
        int n;
        Predicate<E> predicate = this.getPredicateImpl();
        this.ensureSize(this.getSource().size());
        int n2 = change.getTo();
        int n3 = this.findPosition(n);
        int n4 = this.findPosition(n2);
        ListIterator listIterator = this.getSource().listIterator(n);
        int n5 = n3;
        for (n = change.getFrom(); n5 < n4 || n < n2; ++n) {
            Object e = listIterator.next();
            if (n5 < this.size && this.filtered[n5] == n) {
                if (!predicate.test(e)) {
                    this.nextRemove(n5, e);
                    System.arraycopy(this.filtered, n5 + 1, this.filtered, n5, this.size - n5 - 1);
                    --this.size;
                    --n4;
                    continue;
                }
                this.nextUpdate(n5);
                ++n5;
                continue;
            }
            if (!predicate.test(e)) continue;
            this.nextAdd(n5, n5 + 1);
            System.arraycopy(this.filtered, n5, this.filtered, n5 + 1, this.size - n5);
            this.filtered[n5] = n;
            ++this.size;
            ++n5;
            ++n4;
        }
    }

    private void refilter() {
        this.ensureSize(this.getSource().size());
        ArrayList arrayList = null;
        if (this.hasListeners()) {
            arrayList = new ArrayList(this);
        }
        this.size = 0;
        int n = 0;
        Predicate<E> predicate = this.getPredicateImpl();
        for (Object e : this.getSource()) {
            if (predicate.test(e)) {
                this.filtered[this.size++] = n;
            }
            ++n;
        }
        if (this.hasListeners()) {
            this.fireChange(new NonIterableChange.GenericAddRemoveChange(0, this.size, arrayList, this));
        }
    }
}

