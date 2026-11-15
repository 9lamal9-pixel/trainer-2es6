/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.binding;

import com.sun.javafx.binding.StringFormatter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ListBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableListValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class ListExpression<E>
implements ObservableListValue<E> {
    private static final ObservableList EMPTY_LIST = FXCollections.emptyObservableList();

    @Override
    public ObservableList<E> getValue() {
        return (ObservableList)this.get();
    }

    public static <E> ListExpression<E> listExpression(final ObservableListValue<E> observableListValue) {
        if (observableListValue == null) {
            throw new NullPointerException("List must be specified.");
        }
        return observableListValue instanceof ListExpression ? (ListExpression<E>)observableListValue : new ListBinding<E>(){
            {
                super.bind(observableListValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableListValue);
            }

            @Override
            protected ObservableList<E> computeValue() {
                return (ObservableList)observableListValue.get();
            }

            @Override
            public ObservableList<ObservableListValue<E>> getDependencies() {
                return FXCollections.singletonObservableList(observableListValue);
            }
        };
    }

    public int getSize() {
        return this.size();
    }

    public abstract ReadOnlyIntegerProperty sizeProperty();

    public abstract ReadOnlyBooleanProperty emptyProperty();

    public ObjectBinding<E> valueAt(int n) {
        return Bindings.valueAt(this, n);
    }

    public ObjectBinding<E> valueAt(ObservableIntegerValue observableIntegerValue) {
        return Bindings.valueAt(this, observableIntegerValue);
    }

    public BooleanBinding isEqualTo(ObservableList<?> observableList) {
        return Bindings.equal(this, observableList);
    }

    public BooleanBinding isNotEqualTo(ObservableList<?> observableList) {
        return Bindings.notEqual(this, observableList);
    }

    public BooleanBinding isNull() {
        return Bindings.isNull(this);
    }

    public BooleanBinding isNotNull() {
        return Bindings.isNotNull(this);
    }

    public StringBinding asString() {
        return (StringBinding)StringFormatter.convert(this);
    }

    @Override
    public int size() {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.size() : observableList.size();
    }

    @Override
    public boolean isEmpty() {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.isEmpty() : observableList.isEmpty();
    }

    @Override
    public boolean contains(Object object) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.contains(object) : observableList.contains(object);
    }

    @Override
    public Iterator<E> iterator() {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.iterator() : observableList.iterator();
    }

    @Override
    public Object[] toArray() {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.toArray() : observableList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] TArray) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.toArray(TArray) : observableList.toArray(TArray);
    }

    @Override
    public boolean add(E e) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.add(e) : observableList.add(e);
    }

    @Override
    public boolean remove(Object object) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.remove(object) : observableList.remove(object);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.contains(collection) : observableList.containsAll(collection);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.addAll(collection) : observableList.addAll(collection);
    }

    @Override
    public boolean addAll(int n, Collection<? extends E> collection) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.addAll(n, collection) : observableList.addAll(n, collection);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.removeAll(collection) : observableList.removeAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.retainAll(collection) : observableList.retainAll(collection);
    }

    @Override
    public void clear() {
        ObservableList observableList = (ObservableList)this.get();
        if (observableList == null) {
            EMPTY_LIST.clear();
        } else {
            observableList.clear();
        }
    }

    @Override
    public E get(int n) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.get(n) : observableList.get(n);
    }

    @Override
    public E set(int n, E e) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.set(n, e) : observableList.set(n, e);
    }

    @Override
    public void add(int n, E e) {
        ObservableList observableList = (ObservableList)this.get();
        if (observableList == null) {
            EMPTY_LIST.add(n, e);
        } else {
            observableList.add(n, e);
        }
    }

    @Override
    public E remove(int n) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.remove(n) : observableList.remove(n);
    }

    @Override
    public int indexOf(Object object) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.indexOf(object) : observableList.indexOf(object);
    }

    @Override
    public int lastIndexOf(Object object) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.lastIndexOf(object) : observableList.lastIndexOf(object);
    }

    @Override
    public ListIterator<E> listIterator() {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.listIterator() : observableList.listIterator();
    }

    @Override
    public ListIterator<E> listIterator(int n) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.listIterator(n) : observableList.listIterator(n);
    }

    @Override
    public List<E> subList(int n, int n2) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.subList(n, n2) : observableList.subList(n, n2);
    }

    @Override
    public boolean addAll(E ... EArray) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.addAll(EArray) : observableList.addAll(EArray);
    }

    @Override
    public boolean setAll(E ... EArray) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.setAll(EArray) : observableList.setAll(EArray);
    }

    @Override
    public boolean setAll(Collection<? extends E> collection) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.setAll(collection) : observableList.setAll(collection);
    }

    @Override
    public boolean removeAll(E ... EArray) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.removeAll(EArray) : observableList.removeAll(EArray);
    }

    @Override
    public boolean retainAll(E ... EArray) {
        ObservableList observableList = (ObservableList)this.get();
        return observableList == null ? EMPTY_LIST.retainAll(EArray) : observableList.retainAll(EArray);
    }

    @Override
    public void remove(int n, int n2) {
        ObservableList observableList = (ObservableList)this.get();
        if (observableList == null) {
            EMPTY_LIST.remove(n, n2);
        } else {
            observableList.remove(n, n2);
        }
    }
}

