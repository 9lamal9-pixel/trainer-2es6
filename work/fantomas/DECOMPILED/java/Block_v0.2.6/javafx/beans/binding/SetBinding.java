/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.binding;

import com.sun.javafx.binding.BindingHelperObserver;
import com.sun.javafx.binding.SetExpressionHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.SetExpression;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

public abstract class SetBinding<E>
extends SetExpression<E>
implements Binding<ObservableSet<E>> {
    private final SetChangeListener<E> setChangeListener = new SetChangeListener<E>(){

        @Override
        public void onChanged(SetChangeListener.Change<? extends E> change) {
            SetBinding.this.invalidateProperties();
            SetBinding.this.onInvalidating();
            SetExpressionHelper.fireValueChangedEvent(SetBinding.this.helper, change);
        }
    };
    private ObservableSet<E> value;
    private boolean valid = false;
    private BindingHelperObserver observer;
    private SetExpressionHelper<E> helper = null;
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
        this.helper = SetExpressionHelper.addListener(this.helper, this, invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        this.helper = SetExpressionHelper.removeListener(this.helper, invalidationListener);
    }

    @Override
    public void addListener(ChangeListener<? super ObservableSet<E>> changeListener) {
        this.helper = SetExpressionHelper.addListener(this.helper, this, changeListener);
    }

    @Override
    public void removeListener(ChangeListener<? super ObservableSet<E>> changeListener) {
        this.helper = SetExpressionHelper.removeListener(this.helper, changeListener);
    }

    @Override
    public void addListener(SetChangeListener<? super E> setChangeListener) {
        this.helper = SetExpressionHelper.addListener(this.helper, this, setChangeListener);
    }

    @Override
    public void removeListener(SetChangeListener<? super E> setChangeListener) {
        this.helper = SetExpressionHelper.removeListener(this.helper, setChangeListener);
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
    public final ObservableSet<E> get() {
        if (!this.valid) {
            this.value = this.computeValue();
            this.valid = true;
            if (this.value != null) {
                this.value.addListener(this.setChangeListener);
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
                this.value.removeListener(this.setChangeListener);
            }
            this.valid = false;
            this.invalidateProperties();
            this.onInvalidating();
            SetExpressionHelper.fireValueChangedEvent(this.helper);
        }
    }

    @Override
    public final boolean isValid() {
        return this.valid;
    }

    protected abstract ObservableSet<E> computeValue();

    public String toString() {
        return this.valid ? "SetBinding [value: " + (ObservableSet)this.get() + "]" : "SetBinding [invalid]";
    }

    private class EmptyProperty
    extends ReadOnlyBooleanPropertyBase {
        private EmptyProperty() {
        }

        @Override
        public boolean get() {
            return SetBinding.this.isEmpty();
        }

        @Override
        public Object getBean() {
            return SetBinding.this;
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
            return SetBinding.this.size();
        }

        @Override
        public Object getBean() {
            return SetBinding.this;
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

