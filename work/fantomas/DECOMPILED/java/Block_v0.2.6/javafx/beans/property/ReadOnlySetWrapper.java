/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlySetProperty;
import javafx.beans.property.ReadOnlySetPropertyBase;
import javafx.beans.property.SimpleSetProperty;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

public class ReadOnlySetWrapper<E>
extends SimpleSetProperty<E> {
    private ReadOnlyPropertyImpl readOnlyProperty;

    public ReadOnlySetWrapper() {
    }

    public ReadOnlySetWrapper(ObservableSet<E> observableSet) {
        super(observableSet);
    }

    public ReadOnlySetWrapper(Object object, String string) {
        super(object, string);
    }

    public ReadOnlySetWrapper(Object object, String string, ObservableSet<E> observableSet) {
        super(object, string, observableSet);
    }

    public ReadOnlySetProperty<E> getReadOnlyProperty() {
        if (this.readOnlyProperty == null) {
            this.readOnlyProperty = new ReadOnlyPropertyImpl();
        }
        return this.readOnlyProperty;
    }

    @Override
    protected void fireValueChangedEvent() {
        super.fireValueChangedEvent();
        if (this.readOnlyProperty != null) {
            this.readOnlyProperty.fireValueChangedEvent();
        }
    }

    @Override
    protected void fireValueChangedEvent(SetChangeListener.Change<? extends E> change) {
        super.fireValueChangedEvent(change);
        if (this.readOnlyProperty != null) {
            this.readOnlyProperty.fireValueChangedEvent(change);
        }
    }

    private class ReadOnlyPropertyImpl
    extends ReadOnlySetPropertyBase<E> {
        private ReadOnlyPropertyImpl() {
        }

        @Override
        public ObservableSet<E> get() {
            return ReadOnlySetWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlySetWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlySetWrapper.this.getName();
        }

        @Override
        public ReadOnlyIntegerProperty sizeProperty() {
            return ReadOnlySetWrapper.this.sizeProperty();
        }

        @Override
        public ReadOnlyBooleanProperty emptyProperty() {
            return ReadOnlySetWrapper.this.emptyProperty();
        }
    }
}

