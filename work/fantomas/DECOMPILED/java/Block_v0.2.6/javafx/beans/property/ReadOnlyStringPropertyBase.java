/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import com.sun.javafx.binding.ExpressionHelper;
import javafx.beans.InvalidationListener;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.value.ChangeListener;

public abstract class ReadOnlyStringPropertyBase
extends ReadOnlyStringProperty {
    ExpressionHelper<String> helper;

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
}

