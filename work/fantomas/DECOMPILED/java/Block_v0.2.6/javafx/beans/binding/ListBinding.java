/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.binding;

import com.sun.javafx.binding.BindingHelperObserver;
import com.sun.javafx.binding.ListExpressionHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.ListExpression;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public abstract class ListBinding<E>
extends ListExpression<E>
implements Binding<ObservableList<E>> {
    private final ListChangeListener<E> listChangeListener = new ListChangeListener<E>(){

        @Override
        public void onChanged(ListChangeListener.Change<? extends E> change) {
            ListBinding.this.invalidateProperties();
            ListBinding.this.onInvalidating();
            ListExpressionHelper.fireValueChangedEvent(ListBinding.this.helper, change);
        }
    };
    private ObservableList<E> value;
    private boolean valid = false;
    private BindingHelperObserver observer;
    private ListExpressionHelper<E> helper = null;
    private SizeProperty size0;
    private EmptyProperty empty0;

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

    protected final void bind(Observable ... observableArray) {
        if (observableArray != null && observableArray.length > 0) {
            if (this.observer == null) {
                this.observer = new BindingHelperObserver(this);
            }
            for (Observable observable : observableArray) {
                if (observable == null) continue;
                observable.addListener(this.observer);
            }
        }
    }

    protected final void unbind(Observable ... observableArray) {
        if (this.observer != null) {
            for (Observable observable : observableArray) {
                if (observable == null) continue;
                observable.removeListener(this.observer);
            }
            this.observer = null;
        }
    }

    @Override
    public void dispose() {
    }

    @Override
    public ObservableList<?> getDependencies() {
        return FXCollections.emptyObservableList();
    }

    @Override
    public final ObservableList<E> get() {
        if (!this.valid) {
            this.value = this.computeValue();
            this.valid = true;
            if (this.value != null) {
                this.value.addListener(this.listChangeListener);
            }
        }
        return this.value;
    }

    protected void onInvalidating() {
    }

    private void invalidateProperties() {
        if (this.size0 != null) {
            this.size0.fireValueChangedEvent();
        }
        if (this.empty0 != null) {
            this.empty0.fireValueChangedEvent();
        }
    }

    @Override
    public final void invalidate() {
        if (this.valid) {
            if (this.value != null) {
                this.value.removeListener(this.listChangeListener);
            }
            this.valid = false;
            this.invalidateProperties();
            this.onInvalidating();
            ListExpressionHelper.fireValueChangedEvent(this.helper);
        }
    }

    @Override
    public final boolean isValid() {
        return this.valid;
    }

    protected abstract ObservableList<E> computeValue();

    public String toString() {
        return this.valid ? "ListBinding [value: " + (ObservableList)this.get() + "]" : "ListBinding [invalid]";
    }

    private class EmptyProperty
    extends ReadOnlyBooleanPropertyBase {
        private EmptyProperty() {
        }

        @Override
        public boolean get() {
            return ListBinding.this.isEmpty();
        }

        @Override
        public Object getBean() {
            return ListBinding.this;
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
            return ListBinding.this.size();
        }

        @Override
        public Object getBean() {
            return ListBinding.this;
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

