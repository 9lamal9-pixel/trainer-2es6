/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import com.sun.javafx.binding.ExpressionHelper;
import java.lang.ref.WeakReference;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;

public abstract class BooleanPropertyBase
extends BooleanProperty {
    private boolean value;
    private ObservableBooleanValue observable = null;
    private InvalidationListener listener = null;
    private boolean valid = true;
    private ExpressionHelper<Boolean> helper = null;

    public BooleanPropertyBase() {
    }

    public BooleanPropertyBase(boolean bl) {
        this.value = bl;
    }

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        this.helper = ExpressionHelper.addListener(this.helper, this, invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        this.helper = ExpressionHelper.removeListener(this.helper, invalidationListener);
    }

    @Override
    public void addListener(ChangeListener<? super Boolean> changeListener) {
        this.helper = ExpressionHelper.addListener(this.helper, this, changeListener);
    }

    @Override
    public void removeListener(ChangeListener<? super Boolean> changeListener) {
        this.helper = ExpressionHelper.removeListener(this.helper, changeListener);
    }

    protected void fireValueChangedEvent() {
        ExpressionHelper.fireValueChangedEvent(this.helper);
    }

    private void markInvalid() {
        if (this.valid) {
            this.valid = false;
            this.invalidated();
            this.fireValueChangedEvent();
        }
    }

    protected void invalidated() {
    }

    @Override
    public boolean get() {
        this.valid = true;
        return this.observable == null ? this.value : this.observable.get();
    }

    @Override
    public void set(boolean bl) {
        if (this.isBound()) {
            throw new RuntimeException((String)(this.getBean() != null && this.getName() != null ? this.getBean().getClass().getSimpleName() + "." + this.getName() + " : " : "") + "A bound value cannot be set.");
        }
        if (this.value != bl) {
            this.value = bl;
            this.markInvalid();
        }
    }

    @Override
    public boolean isBound() {
        return this.observable != null;
    }

    @Override
    public void bind(ObservableValue<? extends Boolean> observableValue) {
        ObservableBooleanValue observableBooleanValue;
        if (observableValue == null) {
            throw new NullPointerException("Cannot bind to null");
        }
        ObservableBooleanValue observableBooleanValue2 = observableBooleanValue = observableValue instanceof ObservableBooleanValue ? (ObservableBooleanValue)observableValue : new ValueWrapper(observableValue);
        if (!observableBooleanValue.equals(this.observable)) {
            this.unbind();
            this.observable = observableBooleanValue;
            if (this.listener == null) {
                this.listener = new Listener(this);
            }
            this.observable.addListener(this.listener);
            this.markInvalid();
        }
    }

    @Override
    public void unbind() {
        if (this.observable != null) {
            this.value = this.observable.get();
            this.observable.removeListener(this.listener);
            if (this.observable instanceof ValueWrapper) {
                ((ValueWrapper)this.observable).dispose();
            }
            this.observable = null;
        }
    }

    @Override
    public String toString() {
        Object object = this.getBean();
        String string = this.getName();
        StringBuilder stringBuilder = new StringBuilder("BooleanProperty [");
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

    private class ValueWrapper
    extends BooleanBinding {
        private ObservableValue<? extends Boolean> observable;

        public ValueWrapper(ObservableValue<? extends Boolean> observableValue) {
            this.observable = observableValue;
            this.bind(observableValue);
        }

        @Override
        protected boolean computeValue() {
            Boolean bl = this.observable.getValue();
            return bl == null ? false : bl;
        }

        @Override
        public void dispose() {
            this.unbind(this.observable);
        }
    }

    private static class Listener
    implements InvalidationListener,
    WeakListener {
        private final WeakReference<BooleanPropertyBase> wref;

        public Listener(BooleanPropertyBase booleanPropertyBase) {
            this.wref = new WeakReference<BooleanPropertyBase>(booleanPropertyBase);
        }

        @Override
        public void invalidated(Observable observable) {
            BooleanPropertyBase booleanPropertyBase = (BooleanPropertyBase)this.wref.get();
            if (booleanPropertyBase == null) {
                observable.removeListener(this);
            } else {
                booleanPropertyBase.markInvalid();
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return this.wref.get() == null;
        }
    }
}

