/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.binding;

import com.sun.javafx.binding.BindingHelperObserver;
import com.sun.javafx.binding.ExpressionHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Binding;
import javafx.beans.binding.ObjectExpression;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class ObjectBinding<T>
extends ObjectExpression<T>
implements Binding<T> {
    private T value;
    private boolean valid = false;
    private BindingHelperObserver observer;
    private ExpressionHelper<T> helper = null;

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        this.helper = ExpressionHelper.addListener(this.helper, this, invalidationListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        this.helper = ExpressionHelper.removeListener(this.helper, invalidationListener);
    }

    @Override
    public void addListener(ChangeListener<? super T> changeListener) {
        this.helper = ExpressionHelper.addListener(this.helper, this, changeListener);
    }

    @Override
    public void removeListener(ChangeListener<? super T> changeListener) {
        this.helper = ExpressionHelper.removeListener(this.helper, changeListener);
    }

    protected final void bind(Observable ... observableArray) {
        if (observableArray != null && observableArray.length > 0) {
            if (this.observer == null) {
                this.observer = new BindingHelperObserver(this);
            }
            for (Observable observable : observableArray) {
                observable.addListener(this.observer);
            }
        }
    }

    protected final void unbind(Observable ... observableArray) {
        if (this.observer != null) {
            for (Observable observable : observableArray) {
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
    public final T get() {
        if (!this.valid) {
            this.value = this.computeValue();
            this.valid = true;
        }
        return this.value;
    }

    protected void onInvalidating() {
    }

    @Override
    public final void invalidate() {
        if (this.valid) {
            this.valid = false;
            this.onInvalidating();
            ExpressionHelper.fireValueChangedEvent(this.helper);
        }
    }

    @Override
    public final boolean isValid() {
        return this.valid;
    }

    protected abstract T computeValue();

    public String toString() {
        return this.valid ? "ObjectBinding [value: " + this.get() + "]" : "ObjectBinding [invalid]";
    }
}

