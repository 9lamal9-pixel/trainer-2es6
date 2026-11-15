/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.value;

import com.sun.javafx.binding.ExpressionHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public abstract class ObservableValueBase<T>
implements ObservableValue<T> {
    private ExpressionHelper<T> helper;

    @Override
    public void addListener(InvalidationListener invalidationListener) {
        this.helper = ExpressionHelper.addListener(this.helper, this, invalidationListener);
    }

    @Override
    public void addListener(ChangeListener<? super T> changeListener) {
        this.helper = ExpressionHelper.addListener(this.helper, this, changeListener);
    }

    @Override
    public void removeListener(InvalidationListener invalidationListener) {
        this.helper = ExpressionHelper.removeListener(this.helper, invalidationListener);
    }

    @Override
    public void removeListener(ChangeListener<? super T> changeListener) {
        this.helper = ExpressionHelper.removeListener(this.helper, changeListener);
    }

    protected void fireValueChangedEvent() {
        ExpressionHelper.fireValueChangedEvent(this.helper);
    }
}

