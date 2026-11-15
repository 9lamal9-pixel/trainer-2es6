/*
 * Decompiled with CFR 0.152.
 */
package javafx.collections;

import java.text.Collator;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

public interface ObservableList<E>
extends List<E>,
Observable {
    public void addListener(ListChangeListener<? super E> var1);

    public void removeListener(ListChangeListener<? super E> var1);

    public boolean addAll(E ... var1);

    public boolean setAll(E ... var1);

    public boolean setAll(Collection<? extends E> var1);

    public boolean removeAll(E ... var1);

    public boolean retainAll(E ... var1);

    public void remove(int var1, int var2);

    default public FilteredList<E> filtered(Predicate<E> predicate) {
        return new FilteredList<E>(this, predicate);
    }

    default public SortedList<E> sorted(Comparator<E> comparator) {
        return new SortedList<E>(this, comparator);
    }

    default public SortedList<E> sorted() {
        Comparator comparator = new Comparator<E>(){

            @Override
            public int compare(E e, E e2) {
                if (e == null && e2 == null) {
                    return 0;
                }
                if (e == null) {
                    return -1;
                }
                if (e2 == null) {
                    return 1;
                }
                if (e instanceof Comparable) {
                    return ((Comparable)e).compareTo(e2);
                }
                return Collator.getInstance().compare(e.toString(), e2.toString());
            }
        };
        return this.sorted(comparator);
    }
}

