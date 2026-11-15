/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import com.sun.javafx.binding.ListExpressionHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public abstract class ReadOnlyListPropertyBase<E>
extends ReadOnlyListProperty<E> {
    private ListExpressionHelper<E> helper;

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
}

