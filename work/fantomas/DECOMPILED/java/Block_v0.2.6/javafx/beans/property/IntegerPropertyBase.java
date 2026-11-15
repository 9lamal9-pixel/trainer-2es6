/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import com.sun.javafx.binding.ExpressionHelper;
import java.lang.ref.WeakReference;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.binding.IntegerBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableIntegerValue;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;

public abstract class IntegerPropertyBase
extends IntegerProperty {
    private int value;
    private ObservableIntegerValue observable = null;
    private InvalidationListener listener = null;
    private boolean valid = true;
    private ExpressionHelper<Number> helper = null;

    public IntegerPropertyBase() {
    }

    public IntegerPropertyBase(int n) {
        this.value = n;
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
    public void addListener(ChangeListener<? super Number> changeListener) {
        this.helper = ExpressionHelper.addListener(this.helper, this, changeListener);
    }

    @Override
    public void removeListener(ChangeListener<? super Number> changeListener) {
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
    public int get() {
        this.valid = true;
        return this.observable == null ? this.value : this.observable.get();
    }

    @Override
    public void set(int n) {
        if (this.isBound()) {
            throw new RuntimeException((String)(this.getBean() != null && this.getName() != null ? this.getBean().getClass().getSimpleName() + "." + this.getName() + " : " : "") + "A bound value cannot be set.");
        }
        if (this.value != n) {
            this.value = n;
            this.markInvalid();
        }
    }

    @Override
    public boolean isBound() {
        return this.observable != null;
    }

    @Override
    public void bind(final ObservableValue<? extends Number> observableValue) {
        ObservableIntegerValue observableIntegerValue;
        if (observableValue == null) {
            throw new NullPointerException("Cannot bind to null");
        }
        if (observableValue instanceof ObservableIntegerValue) {
            observableIntegerValue = (ObservableIntegerValue)observableValue;
        } else if (observableValue instanceof ObservableNumberValue) {
            final ObservableNumberValue observableNumberValue = (ObservableNumberValue)observableValue;
            observableIntegerValue = new ValueWrapper(observableValue){

                @Override
                protected int computeValue() {
                    return observableNumberValue.intValue();
                }
            };
        } else {
            observableIntegerValue = new ValueWrapper(observableValue){

                @Override
                protected int computeValue() {
                    Number number = (Number)observableValue.getValue();
                    return number == null ? 0 : number.intValue();
                }
            };
        }
        if (!observableIntegerValue.equals(this.observable)) {
            this.unbind();
            this.observable = observableIntegerValue;
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
        StringBuilder stringBuilder = new StringBuilder("IntegerProperty [");
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

    private abstract class ValueWrapper
    extends IntegerBinding {
        private ObservableValue<? extends Number> observable;

        public ValueWrapper(ObservableValue<? extends Number> observableValue) {
            this.observable = observableValue;
            this.bind(observableValue);
        }

        @Override
        public void dispose() {
            this.unbind(this.observable);
        }
    }

    private static class Listener
    implements InvalidationListener,
    WeakListener {
        private final WeakReference<IntegerPropertyBase> wref;

        public Listener(IntegerPropertyBase integerPropertyBase) {
            this.wref = new WeakReference<IntegerPropertyBase>(integerPropertyBase);
        }

        @Override
        public void invalidated(Observable observable) {
            IntegerPropertyBase integerPropertyBase = (IntegerPropertyBase)this.wref.get();
            if (integerPropertyBase == null) {
                observable.removeListener(this);
            } else {
                integerPropertyBase.markInvalid();
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return this.wref.get() == null;
        }
    }
}

