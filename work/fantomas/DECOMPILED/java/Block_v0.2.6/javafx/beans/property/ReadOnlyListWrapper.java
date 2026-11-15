/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListPropertyBase;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public class ReadOnlyListWrapper<E>
extends SimpleListProperty<E> {
    private ReadOnlyPropertyImpl readOnlyProperty;

    public ReadOnlyListWrapper() {
    }

    public ReadOnlyListWrapper(ObservableList<E> observableList) {
        super(observableList);
    }

    public ReadOnlyListWrapper(Object object, String string) {
        super(object, string);
    }

    public ReadOnlyListWrapper(Object object, String string, ObservableList<E> observableList) {
        super(object, string, observableList);
    }

    public ReadOnlyListProperty<E> getReadOnlyProperty() {
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
    protected void fireValueChangedEvent(ListChangeListener.Change<? extends E> change) {
        super.fireValueChangedEvent(change);
        if (this.readOnlyProperty != null) {
            change.reset();
            this.readOnlyProperty.fireValueChangedEvent(change);
        }
    }

    private class ReadOnlyPropertyImpl
    extends ReadOnlyListPropertyBase<E> {
        private ReadOnlyPropertyImpl() {
        }

        @Override
        public ObservableList<E> get() {
            return ReadOnlyListWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyListWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyListWrapper.this.getName();
        }

        @Override
        public ReadOnlyIntegerProperty sizeProperty() {
            return ReadOnlyListWrapper.this.sizeProperty();
        }

        @Override
        public ReadOnlyBooleanProperty emptyProperty() {
            return ReadOnlyListWrapper.this.emptyProperty();
        }
    }
}

