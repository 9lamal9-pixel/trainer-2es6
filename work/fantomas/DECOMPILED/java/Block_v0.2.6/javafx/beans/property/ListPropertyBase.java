/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import com.sun.javafx.binding.ListExpressionHelper;
import java.lang.ref.WeakReference;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public abstract class ListPropertyBase<E>
extends ListProperty<E> {
    private final ListChangeListener<E> listChangeListener = change -> {
        this.invalidateProperties();
        this.invalidated();
        this.fireValueChangedEvent(change);
    };
    private ObservableList<E> value;
    private ObservableValue<? extends ObservableList<E>> observable = null;
    private InvalidationListener listener = null;
    private boolean valid = true;
    private ListExpressionHelper<E> helper = null;
    private SizeProperty size0;
    private EmptyProperty empty0;

    public ListPropertyBase() {
    }

    public ListPropertyBase(ObservableList<E> observableList) {
        this.value = observableList;
        if (observableList != null) {
            observableList.addListener(this.listChangeListener);
        }
    }

    @Override
    public ReadOnlyIntegerProperty sizeProperty() {
        if (this.size0 == null) {
            this.size0 = new SizeProperty();
        }
        return this.size0;
    }

    @Override
    public ReadOnlyBooleanProperty emptyProperty() {
        if (this.empty0 == null) {
            this.empty0 = new EmptyProperty();
        }
        return this.empty0;
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        this.helper = ListExpressionHelper.addListener(this.helper, this, invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        this.helper = ListExpressionHelper.removeListener(this.helper, invalidationListener);
    }

    @Override
    public void addListener(ChangeListener<? super ObservableList<E>> changeListener) {
        this.helper = ListExpressionHelper.addListener(this.helper, this, changeListener);
    }

    @Override
    public void removeListener(ChangeListener<? super ObservableList<E>> changeListener) {
        this.helper = ListExpressionHelper.removeListener(this.helper, changeListener);
    }

    @Override
    public void addListener(ListChangeListener<? super E> listChangeListener) {
        this.helper = ListExpressionHelper.addListener(this.helper, this, listChangeListener);
    }

    @Override
    public void removeListener(ListChangeListener<? super E> listChangeListener) {
        this.helper = ListExpressionHelper.removeListener(this.helper, listChangeListener);
    }

    protected void fireValueChangedEvent() {
        ListExpressionHelper.fireValueChangedEvent(this.helper);
    }

    protected void fireValueChangedEvent(ListChangeListener.Change<? extends E> change) {
        ListExpressionHelper.fireValueChangedEvent(this.helper, change);
    }

    private void invalidateProperties() {
        if (this.size0 != null) {
            this.size0.fireValueChangedEvent();
        }
        if (this.empty0 != null) {
            this.empty0.fireValueChangedEvent();
        }
    }

    private void markInvalid(ObservableList<E> observableList) {
        if (this.valid) {
            if (observableList != null) {
                observableList.removeListener(this.listChangeListener);
            }
            this.valid = false;
            this.invalidateProperties();
            this.invalidated();
            this.fireValueChangedEvent();
        }
    }

    protected void invalidated() {
    }

    @Override
    public ObservableList<E> get() {
        if (!this.valid) {
            this.value = this.observable == null ? this.value : this.observable.getValue();
            this.valid = true;
            if (this.value != null) {
                this.value.addListener(this.listChangeListener);
            }
        }
        return this.value;
    }

    @Override
    public void set(ObservableList<E> observableList) {
        if (this.isBound()) {
            throw new RuntimeException((String)(this.getBean() != null && this.getName() != null ? this.getBean().getClass().getSimpleName() + "." + this.getName() + " : " : "") + "A bound value cannot be set.");
        }
        if (this.value != observableList) {
            ObservableList<E> observableList2 = this.value;
            this.value = observableList;
            this.markInvalid(observableList2);
        }
    }

    @Override
    public boolean isBound() {
        return this.observable != null;
    }

    @Override
    public void bind(ObservableValue<? extends ObservableList<E>> observableValue) {
        if (observableValue == null) {
            throw new NullPointerException("Cannot bind to null");
        }
        if (!observableValue.equals(this.observable)) {
            this.unbind();
            this.observable = observableValue;
            if (this.listener == null) {
                this.listener = new Listener(this);
            }
            this.observable.addListener(this.listener);
            this.markInvalid(this.value);
        }
    }

    @Override
    public void unbind() {
        if (this.observable != null) {
            this.value = this.observable.getValue();
            this.observable.removeListener(this.listener);
            this.observable = null;
        }
    }

    @Override
    public String toString() {
        Object object = this.getBean();
        String string = this.getName();
        StringBuilder stringBuilder = new StringBuilder("ListProperty [");
        if (object != null) {
            stringBuilder.append("bean: ").append(object).append(", ");
        }
        if (string != null && !string.equals("")) {
            stringBuilder.append("name: ").append(string).append(", ");
        }
        if (this.isBound()) {
            stringBuilder.append("bound, ");
            if (this.valid) {
                stringBuilder.append("value: ").append(this.get());
            } else {
                stringBuilder.append("invalid");
            }
        } else {
            stringBuilder.append("value: ").append(this.get());
        }
        stringBuilder.append("]");
        return stringBuilder.toString();
    }

    private static class Listener<E>
    implements InvalidationListener,
    WeakListener {
        private final WeakReference<ListPropertyBase<E>> wref;

        public Listener(ListPropertyBase<E> listPropertyBase) {
            this.wref = new WeakReference<ListPropertyBase<E>>(listPropertyBase);
        }

        @Override
        public void invalidated(Observable observable) {
            ListPropertyBase listPropertyBase = (ListPropertyBase)this.wref.get();
            if (listPropertyBase == null) {
                observable.removeListener(this);
            } else {
                listPropertyBase.markInvalid(listPropertyBase.value);
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return this.wref.get() == null;
        }
    }

    private class EmptyProperty
    extends ReadOnlyBooleanPropertyBase {
        private EmptyProperty() {
        }

        @Override
        public boolean get() {
            return ListPropertyBase.this.isEmpty();
        }

        @Override
        public Object getBean() {
            return ListPropertyBase.this;
        }

        @Override
        public String getName() {
            return "empty";
        }

        @Override
        protected void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }
    }

    private class SizeProperty
    extends ReadOnlyIntegerPropertyBase {
        private SizeProperty() {
        }

        @Override
        public int get() {
            return ListPropertyBase.this.size();
        }

        @Override
        public Object getBean() {
            return ListPropertyBase.this;
        }

        @Override
        public String getName() {
            return "size";
        }

        @Override
        protected void fireValueChangedEvent() {
            super.fireValueChangedEvent();
        }
    }
}

