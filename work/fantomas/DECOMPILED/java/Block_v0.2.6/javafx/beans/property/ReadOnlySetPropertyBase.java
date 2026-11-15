/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import com.sun.javafx.binding.SetExpressionHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

public abstract class ReadOnlySetPropertyBase<E>
extends ReadOnlySetProperty<E> {
    private SetExpressionHelper<E> helper;

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
}

