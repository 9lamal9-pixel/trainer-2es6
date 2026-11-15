/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import com.sun.javafx.binding.ExpressionHelper;
import java.lang.ref.WeakReference;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.binding.LongBinding;
import javafx.beans.property.LongProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableLongValue;
import javafx.beans.value.ObservableNumberValue;
import javafx.beans.value.ObservableValue;

public abstract class LongPropertyBase
extends LongProperty {
    private long value;
    private ObservableLongValue observable = null;
    private InvalidationListener listener = null;
    private boolean valid = true;
    private ExpressionHelper<Number> helper = null;

    public LongPropertyBase() {
    }

    public LongPropertyBase(long l) {
        this.value = l;
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
    public long get() {
        this.valid = true;
        return this.observable == null ? this.value : this.observable.get();
    }

    @Override
    public void set(long l) {
        if (this.isBound()) {
            throw new RuntimeException((String)(this.getBean() != null && this.getName() != null ? this.getBean().getClass().getSimpleName() + "." + this.getName() + " : " : "") + "A bound value cannot be set.");
        }
        if (this.value != l) {
            this.value = l;
            this.markInvalid();
        }
    }

    @Override
    public boolean isBound() {
        return this.observable != null;
    }

    @Override
    public void bind(final ObservableValue<? extends Number> observableValue) {
        ObservableLongValue observableLongValue;
        if (observableValue == null) {
            throw new NullPointerException("Cannot bind to null");
        }
        if (observableValue instanceof ObservableLongValue) {
            observableLongValue = (ObservableLongValue)observableValue;
        } else if (observableValue instanceof ObservableNumberValue) {
            final ObservableNumberValue observableNumberValue = (ObservableNumberValue)observableValue;
            observableLongValue = new ValueWrapper(observableValue){

                @Override
                protected long computeValue() {
                    return observableNumberValue.longValue();
                }
            };
        } else {
            observableLongValue = new ValueWrapper(observableValue){

                @Override
                protected long computeValue() {
                    Number number = (Number)observableValue.getValue();
                    return number == null ? 0L : number.longValue();
                }
            };
        }
        if (!observableLongValue.equals(this.observable)) {
            this.unbind();
            this.observable = observableLongValue;
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
        StringBuilder stringBuilder = new StringBuilder("LongProperty [");
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
    extends LongBinding {
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
        private final WeakReference<LongPropertyBase> wref;

        public Listener(LongPropertyBase longPropertyBase) {
            this.wref = new WeakReference<LongPropertyBase>(longPropertyBase);
        }

        @Override
        public void invalidated(Observable observable) {
            LongPropertyBase longPropertyBase = (LongPropertyBase)this.wref.get();
            if (longPropertyBase == null) {
                observable.removeListener(this);
            } else {
                longPropertyBase.markInvalid();
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return this.wref.get() == null;
        }
    }
}

