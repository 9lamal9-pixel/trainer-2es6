/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import com.sun.javafx.binding.ExpressionHelper;
import java.lang.ref.WeakReference;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakListener;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public abstract class StringPropertyBase
extends StringProperty {
    private String value;
    private ObservableValue<? extends String> observable = null;
    private InvalidationListener listener = null;
    private boolean valid = true;
    private ExpressionHelper<String> helper = null;

    public StringPropertyBase() {
    }

    public StringPropertyBase(String string) {
        this.value = string;
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
    public void addListener(ChangeListener<? super String> changeListener) {
        this.helper = ExpressionHelper.addListener(this.helper, this, changeListener);
    }

    @Override
    public void removeListener(ChangeListener<? super String> changeListener) {
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
    public String get() {
        this.valid = true;
        return this.observable == null ? this.value : this.observable.getValue();
    }

    @Override
    public void set(String string) {
        if (this.isBound()) {
            throw new RuntimeException((String)(this.getBean() != null && this.getName() != null ? this.getBean().getClass().getSimpleName() + "." + this.getName() + " : " : "") + "A bound value cannot be set.");
        }
        if (this.value == null ? string != null : !this.value.equals(string)) {
            this.value = string;
            this.markInvalid();
        }
    }

    @Override
    public boolean isBound() {
        return this.observable != null;
    }

    @Override
    public void bind(ObservableValue<? extends String> observableValue) {
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
            this.markInvalid();
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
        StringBuilder stringBuilder = new StringBuilder("StringProperty [");
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

    private static class Listener
    implements InvalidationListener,
    WeakListener {
        private final WeakReference<StringPropertyBase> wref;

        public Listener(StringPropertyBase stringPropertyBase) {
            this.wref = new WeakReference<StringPropertyBase>(stringPropertyBase);
        }

        @Override
        public void invalidated(Observable observable) {
            StringPropertyBase stringPropertyBase = (StringPropertyBase)this.wref.get();
            if (stringPropertyBase == null) {
                observable.removeListener(this);
            } else {
                stringPropertyBase.markInvalid();
            }
        }

        @Override
        public boolean wasGarbageCollected() {
            return this.wref.get() == null;
        }
    }
}

