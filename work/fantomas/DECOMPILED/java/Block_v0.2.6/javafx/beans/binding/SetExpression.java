/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.binding;

import com.sun.javafx.binding.StringFormatter;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.SetBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.value.ObservableSetValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

public abstract class SetExpression<E>
implements ObservableSetValue<E> {
    private static final ObservableSet EMPTY_SET = new EmptyObservableSet();

    @Override
    public ObservableSet<E> getValue() {
        return (ObservableSet)this.get();
    }

    public static <E> SetExpression<E> setExpression(final ObservableSetValue<E> observableSetValue) {
        if (observableSetValue == null) {
            throw new NullPointerException("Set must be specified.");
        }
        return observableSetValue instanceof SetExpression ? (SetExpression<E>)observableSetValue : new SetBinding<E>(){
            {
                super.bind(observableSetValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableSetValue);
            }

            @Override
            protected ObservableSet<E> computeValue() {
                return (ObservableSet)observableSetValue.get();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return FXCollections.singletonObservableList(observableSetValue);
            }
        };
    }

    public int getSize() {
        return this.size();
    }

    public abstract ReadOnlyIntegerProperty sizeProperty();

    public abstract ReadOnlyBooleanProperty emptyProperty();

    public BooleanBinding isEqualTo(ObservableSet<?> observableSet) {
        return Bindings.equal(this, observableSet);
    }

    public BooleanBinding isNotEqualTo(ObservableSet<?> observableSet) {
        return Bindings.notEqual(this, observableSet);
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
        ObservableSet observableSet = (ObservableSet)this.get();
        return observableSet == null ? EMPTY_SET.size() : observableSet.size();
    }

    @Override
    public boolean isEmpty() {
        ObservableSet observableSet = (ObservableSet)this.get();
        return observableSet == null ? EMPTY_SET.isEmpty() : observableSet.isEmpty();
    }

    @Override
    public boolean contains(Object object) {
        ObservableSet observableSet = (ObservableSet)this.get();
        return observableSet == null ? EMPTY_SET.contains(object) : observableSet.contains(object);
    }

    @Override
    public Iterator<E> iterator() {
        ObservableSet observableSet = (ObservableSet)this.get();
        return observableSet == null ? EMPTY_SET.iterator() : observableSet.iterator();
    }

    @Override
    public Object[] toArray() {
        ObservableSet observableSet = (ObservableSet)this.get();
        return observableSet == null ? EMPTY_SET.toArray() : observableSet.toArray();
    }

    @Override
    public <T> T[] toArray(T[] TArray) {
        ObservableSet observableSet = (ObservableSet)this.get();
        return observableSet == null ? EMPTY_SET.toArray(TArray) : observableSet.toArray(TArray);
    }

    @Override
    public boolean add(E e) {
        ObservableSet observableSet = (ObservableSet)this.get();
        return observableSet == null ? EMPTY_SET.add(e) : observableSet.add(e);
    }

    @Override
    public boolean remove(Object object) {
        ObservableSet observableSet = (ObservableSet)this.get();
        return observableSet == null ? EMPTY_SET.remove(object) : observableSet.remove(object);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        ObservableSet observableSet = (ObservableSet)this.get();
        return observableSet == null ? EMPTY_SET.contains(collection) : observableSet.containsAll(collection);
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        ObservableSet observableSet = (ObservableSet)this.get();
        return observableSet == null ? EMPTY_SET.addAll(collection) : observableSet.addAll(collection);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        ObservableSet observableSet = (ObservableSet)this.get();
        return observableSet == null ? EMPTY_SET.removeAll(collection) : observableSet.removeAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        ObservableSet observableSet = (ObservableSet)this.get();
        return observableSet == null ? EMPTY_SET.retainAll(collection) : observableSet.retainAll(collection);
    }

    @Override
    public void clear() {
        ObservableSet observableSet = (ObservableSet)this.get();
        if (observableSet == null) {
            EMPTY_SET.clear();
        } else {
            observableSet.clear();
        }
    }

    private static class EmptyObservableSet<E>
    extends AbstractSet<E>
    implements ObservableSet<E> {
        private static final Iterator iterator = new Iterator(){

            @Override
            public boolean hasNext() {
                return false;
            }

            public Object next() {
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };

        private EmptyObservableSet() {
        }

        @Override
        public Iterator<E> iterator() {
            return iterator;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public void addListener(SetChangeListener<? super E> setChangeListener) {
        }

        @Override
        public void removeListener(SetChangeListener<? super E> setChangeListener) {
        }

        @Override
        public void addListener(InvalidationListener invalidationListener) {
        }

        @Override
        public void removeListener(InvalidationListener invalidationListener) {
        }
    }
}

