/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import com.sun.javafx.binding.ExpressionHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;

public abstract class ReadOnlyBooleanPropertyBase
extends ReadOnlyBooleanProperty {
    ExpressionHelper<Boolean> helper;

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
}

