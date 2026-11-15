/*
 * Decompiled with CFR 0.152.
 */
package javafx.collections.transformation;

import com.sun.javafx.collections.NonIterableChange;
import com.sun.javafx.collections.SortHelper;
import com.sun.javafx.collections.SourceAdapterChange;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javafx.beans.NamedArg;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.TransformationList;

public final class SortedList<E>
extends TransformationList<E, E> {
    private Comparator<Element<E>> elementComparator;
    private Element<E>[] sorted;
    private int[] perm;
    private int size;
    private final SortHelper helper = new SortHelper();
    private final Element<E> tempElement = new Element<Object>(null, -1);
    private ObjectProperty<Comparator<? super E>> comparator;

    public SortedList(@NamedArg(value="source") ObservableList<? extends E> observableList, @NamedArg(value="comparator") Comparator<? super E> comparator) {
        super(observableList);
        this.sorted = new Element[observableList.size() * 3 / 2 + 1];
        this.perm = new int[this.sorted.length];
        this.size = observableList.size();
        for (int i = 0; i < this.size; ++i) {
            this.sorted[i] = new Element(observableList.get(i), i);
            this.perm[i] = i;
        }
        if (comparator != null) {
            this.setComparator(comparator);
        }
    }

    public SortedList(@NamedArg(value="source") ObservableList<? extends E> observableList) {
        this(observableList, null);
    }

    @Override
    protected void sourceChanged(ListChangeListener.Change<? extends E> change) {
        if (this.elementComparator != null) {
            this.beginChange();
            while (change.next()) {
                if (change.wasPermutated()) {
                    this.updatePermutationIndexes(change);
                    continue;
                }
                if (change.wasUpdated()) {
                    this.update(change);
                    continue;
                }
                this.addRemove(change);
            }
            this.endChange();
        } else {
            this.updateUnsorted(change);
            this.fireChange(new SourceAdapterChange<E>(this, change));
        }
    }

    public final ObjectProperty<Comparator<? super E>> comparatorProperty() {
        if (this.comparator == null) {
            this.comparator = new ObjectPropertyBase<Comparator<? super E>>(){

                @Override
                protected void invalidated() {
                    Comparator comparator = (Comparator)this.get();
                    SortedList.this.elementComparator = comparator != null ? new ElementComparator(comparator) : null;
                    SortedList.this.doSortWithPermutationChange();
                }

                @Override
                public Object getBean() {
                    return SortedList.this;
                }

                @Override
                public String getName() {
                    return "comparator";
                }
            };
        }
        return this.comparator;
    }

    public final Comparator<? super E> getComparator() {
        return this.comparator == null ? null : (Comparator)this.comparator.get();
    }

    public final void setComparator(Comparator<? super E> comparator) {
        this.comparatorProperty().set(comparator);
    }

    @Override
    public E get(int n) {
        if (n >= this.size) {
            throw new IndexOutOfBoundsException();
        }
        return (E)((Element)this.sorted[n]).e;
    }

    @Override
    public int size() {
        return this.size;
    }

    private void doSortWithPermutationChange() {
        if (this.elementComparator != null) {
            int[] nArray = this.helper.sort(this.sorted, 0, this.size, this.elementComparator);
            for (int i = 0; i < this.size; ++i) {
                this.perm[((Element)this.sorted[i]).index] = i;
            }
            this.fireChange(new NonIterableChange.SimplePermutationChange(0, this.size, nArray, this));
        } else {
            int n;
            int[] nArray = new int[this.size];
            int[] nArray2 = new int[this.size];
            for (n = 0; n < this.size; ++n) {
                nArray[n] = nArray2[n] = n;
            }
            n = 0;
            int n2 = 0;
            while (n2 < this.size) {
                int n3 = ((Element)this.sorted[n2]).index;
                if (n3 == n2) {
                    ++n2;
                    continue;
                }
                Element<E> element = this.sorted[n3];
                this.sorted[n3] = this.sorted[n2];
                this.sorted[n2] = element;
                this.perm[n2] = n2;
                this.perm[n3] = n3;
                nArray[nArray2[n2]] = n3;
                nArray[nArray2[n3]] = n2;
                int n4 = nArray2[n2];
                nArray2[n2] = nArray2[n3];
                nArray2[n3] = n4;
                n = 1;
            }
            if (n != 0) {
                this.fireChange(new NonIterableChange.SimplePermutationChange(0, this.size, nArray, this));
            }
        }
    }

    @Override
    public int getSourceIndex(int n) {
        return ((Element)this.sorted[n]).index;
    }

    @Override
    public int getViewIndex(int n) {
        return this.perm[n];
    }

    private void updatePermutationIndexes(ListChangeListener.Change<? extends E> change) {
        int n = 0;
        while (n < this.size) {
            int n2 = change.getPermutation(((Element)this.sorted[n]).index);
            ((Element)this.sorted[n]).index = n2;
            this.perm[n2] = n++;
        }
    }

    private void updateUnsorted(ListChangeListener.Change<? extends E> change) {
        while (change.next()) {
            if (change.wasPermutated()) {
                Element[] elementArray = new Element[this.sorted.length];
                for (int i = 0; i < this.size; ++i) {
                    if (i >= change.getFrom() && i < change.getTo()) {
                        int n = change.getPermutation(i);
                        elementArray[n] = this.sorted[i];
                        elementArray[n].index = n;
                        this.perm[i] = i;
                        continue;
                    }
                    elementArray[i] = this.sorted[i];
                }
                this.sorted = elementArray;
            }
            if (change.wasRemoved()) {
                int n = change.getFrom() + change.getRemovedSize();
                System.arraycopy(this.sorted, n, this.sorted, change.getFrom(), this.size - n);
                System.arraycopy(this.perm, n, this.perm, change.getFrom(), this.size - n);
                this.size -= change.getRemovedSize();
                this.updateIndices(n, n, -change.getRemovedSize());
            }
            if (!change.wasAdded()) continue;
            this.ensureSize(this.size + change.getAddedSize());
            this.updateIndices(change.getFrom(), change.getFrom(), change.getAddedSize());
            System.arraycopy(this.sorted, change.getFrom(), this.sorted, change.getTo(), this.size - change.getFrom());
            System.arraycopy(this.perm, change.getFrom(), this.perm, change.getTo(), this.size - change.getFrom());
            this.size += change.getAddedSize();
            for (int i = change.getFrom(); i < change.getTo(); ++i) {
                this.sorted[i] = new Element(change.getList().get(i), i);
                this.perm[i] = i;
            }
        }
    }

    private void ensureSize(int n) {
        if (this.sorted.length < n) {
            Element[] elementArray = new Element[n * 3 / 2 + 1];
            System.arraycopy(this.sorted, 0, elementArray, 0, this.size);
            this.sorted = elementArray;
            int[] nArray = new int[n * 3 / 2 + 1];
            System.arraycopy(this.perm, 0, nArray, 0, this.size);
            this.perm = nArray;
        }
    }

    private void updateIndices(int n, int n2, int n3) {
        for (int i = 0; i < this.size; ++i) {
            if (((Element)this.sorted[i]).index >= n) {
                ((Element)this.sorted[i]).index += n3;
            }
            if (this.perm[i] < n2) continue;
            int n4 = i;
            this.perm[n4] = this.perm[n4] + n3;
        }
    }

    private int findPosition(E e) {
        if (this.sorted.length == 0) {
            return 0;
        }
        ((Element)this.tempElement).e = e;
        int n = Arrays.binarySearch(this.sorted, 0, this.size, this.tempElement, this.elementComparator);
        return n;
    }

    private void insertToMapping(E e, int n) {
        int n2 = this.findPosition(e);
        if (n2 < 0) {
            n2 ^= 0xFFFFFFFF;
        }
        this.ensureSize(this.size + 1);
        this.updateIndices(n, n2, 1);
        System.arraycopy(this.sorted, n2, this.sorted, n2 + 1, this.size - n2);
        this.sorted[n2] = new Element<E>(e, n);
        System.arraycopy(this.perm, n, this.perm, n + 1, this.size - n);
        this.perm[n] = n2;
        ++this.size;
        this.nextAdd(n2, n2 + 1);
    }

    private void setAllToMapping(List<? extends E> list, int n) {
        this.ensureSize(n);
        this.size = n;
        for (int i = 0; i < n; ++i) {
            this.sorted[i] = new Element<E>(list.get(i), i);
        }
        int[] nArray = this.helper.sort(this.sorted, 0, this.size, this.elementComparator);
        System.arraycopy(nArray, 0, this.perm, 0, this.size);
        this.nextAdd(0, this.size);
    }

    private void removeFromMapping(int n, E e) {
        int n2 = this.perm[n];
        System.arraycopy(this.sorted, n2 + 1, this.sorted, n2, this.size - n2 - 1);
        System.arraycopy(this.perm, n + 1, this.perm, n, this.size - n - 1);
        --this.size;
        this.sorted[this.size] = null;
        this.updateIndices(n + 1, n2, -1);
        this.nextRemove(n2, e);
    }

    private void removeAllFromMapping() {
        ArrayList arrayList = new ArrayList(this);
        for (int i = 0; i < this.size; ++i) {
            this.sorted[i] = null;
        }
        this.size = 0;
        this.nextRemove(0, arrayList);
    }

    private void update(ListChangeListener.Change<? extends E> change) {
        int n;
        int[] nArray = this.helper.sort(this.sorted, 0, this.size, this.elementComparator);
        for (n = 0; n < this.size; ++n) {
            this.perm[((Element)this.sorted[n]).index] = n;
        }
        this.nextPermutation(0, this.size, nArray);
        int n2 = change.getTo();
        for (n = change.getFrom(); n < n2; ++n) {
            this.nextUpdate(this.perm[n]);
        }
    }

    private void addRemove(ListChangeListener.Change<? extends E> change) {
        int n;
        int n2;
        if (change.getFrom() == 0 && change.getRemovedSize() == this.size) {
            this.removeAllFromMapping();
        } else {
            n2 = change.getRemovedSize();
            for (n = 0; n < n2; ++n) {
                this.removeFromMapping(change.getFrom(), change.getRemoved().get(n));
            }
        }
        if (this.size == 0) {
            this.setAllToMapping(change.getList(), change.getTo());
        } else {
            n2 = change.getTo();
            for (n = change.getFrom(); n < n2; ++n) {
                this.insertToMapping(change.getList().get(n), n);
            }
        }
    }

    private static class ElementComparator<E>
    implements Comparator<Element<E>> {
        private final Comparator<? super E> comparator;

        public ElementComparator(Comparator<? super E> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(Element<E> element, Element<E> element2) {
            return this.comparator.compare(((Element)element).e, ((Element)element2).e);
        }
    }

    private static class Element<E> {
        private E e;
        private int index;

        public Element(E e, int n) {
            this.e = e;
            this.index = n;
        }
    }
}

