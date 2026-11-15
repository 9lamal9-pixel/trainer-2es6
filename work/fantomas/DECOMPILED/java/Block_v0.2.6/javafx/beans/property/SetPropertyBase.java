/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import com.sun.javafx.binding.SetExpressionHelper;
import java.lang.ref.WeakReference;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerPropertyBase;
import javafx.beans.property.SetProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

public abstract class SetPropertyBase<E>
extends SetProperty<E> {
    private final SetChangeListener<E> setChangeListener = change -> {
        this.invalidateProperties();
        this.invalidated();
        this.fireValueChangedEvent(change);
    };
    private ObservableSet<E> value;
    private ObservableValue<? extends ObservableSet<E>> observable = null;
    private InvalidationListener listener = null;
    private boolean valid = true;
    private SetExpressionHelper<E> helper = null;
    private SizeProperty size0;
    private EmptyProperty empty0;

    public SetPropertyBase() {
    }

    public SetPropertyBase(ObservableSet<E> observableSet) {
        this.value = observableSet;
        if (observableSet != null) {
            observableSet.addListener(this.setChangeListener);
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

    protected void fireValueChangedEvent() {
        SetExpressionHelper.fireValueChangedEvent(this.helper);
    }

    protected void fireValueChangedEvent(SetChangeListener.Change<? extends E> change) {
        SetExpressionHelper.fireValueChangedEvent(this.helper, change);
    }

    private void invalidateProperties() {
        if (this.size0 != null) {
            this.size0.fireValueChangedEvent();
        }
        if (this.empty0 != null) {
            this.empty0.fireValueChangedEvent();
        }
    }

    private void markInvalid(ObservableSet<E> observableSet) {
        if (this.valid) {
            if (observableSet != null) {
                observableSet.removeListener(this.setChangeListener);
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
    public ObservableSet<E> get() {
        if (!this.valid) {
            this.value = this.observable == null ? this.value : this.observable.getValue();
            this.valid = true;
            if (this.value != null) {
                this.value.addListener(this.setChangeListener);
            }
        }
        return this.value;
    }

    @Override
    public void set(ObservableSet<E> observableSet) {
        if (this.isBound()) {
            throw new RuntimeException((String)(this.getBean() != null && this.getName() != null ? this.getBean().getClass().getSimpleName() + "." + this.getName() + " : " : "") + "A bound value cannot be set.");
        }
        if (this.value != observableSet) {
            ObservableSet<E> observableSet2 = this.value;
            this.value = observableSet;
            this.markInvalid(observableSet2);
        }
    }

    @Override
    public boolean isBound() {
        return this.observable != null;
    }

    @Override
    public void bind(ObservableValue<? extends ObservableSet<E>> observableValue) {
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
        StringBuilder stringBuilder = new StringBuilder("SetProperty [");
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
        private final WeakReference<SetPropertyBase<E>> wref;

        public Listener(SetPropertyBase<E> setPropertyBase) {
            this.wref = new WeakReference<SetPropertyBase<E>>(setPropertyBase);
        }

        @Override
        public void invalidated(Observable observable) {
            SetPropertyBase setPropertyBase = (SetPropertyBase)this.wref.get();
            if (setPropertyBase == null) {
                observable.removeListener(this);
            } else {
                setPropertyBase.markInvalid(setPropertyBase.value);
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
            return SetPropertyBase.this.isEmpty();
        }

        @Override
        public Object getBean() {
            return SetPropertyBase.this;
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
            return SetPropertyBase.this.size();
        }

        @Override
        public Object getBean() {
            return SetPropertyBase.this;
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

