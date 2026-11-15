/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyMapProperty;
import javafx.beans.property.ReadOnlyMapPropertyBase;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

public class ReadOnlyMapWrapper<K, V>
extends SimpleMapProperty<K, V> {
    private ReadOnlyPropertyImpl readOnlyProperty;

    public ReadOnlyMapWrapper() {
    }

    public ReadOnlyMapWrapper(ObservableMap<K, V> observableMap) {
        super(observableMap);
    }

    public ReadOnlyMapWrapper(Object object, String string) {
        super(object, string);
    }

    public ReadOnlyMapWrapper(Object object, String string, ObservableMap<K, V> observableMap) {
        super(object, string, observableMap);
    }

    public ReadOnlyMapProperty<K, V> getReadOnlyProperty() {
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
    protected void fireValueChangedEvent(MapChangeListener.Change<? extends K, ? extends V> change) {
        super.fireValueChangedEvent(change);
        if (this.readOnlyProperty != null) {
            this.readOnlyProperty.fireValueChangedEvent(change);
        }
    }

    private class ReadOnlyPropertyImpl
    extends ReadOnlyMapPropertyBase<K, V> {
        private ReadOnlyPropertyImpl() {
        }

        @Override
        public ObservableMap<K, V> get() {
            return ReadOnlyMapWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyMapWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyMapWrapper.this.getName();
        }

        @Override
        public ReadOnlyIntegerProperty sizeProperty() {
            return ReadOnlyMapWrapper.this.sizeProperty();
        }

        @Override
        public ReadOnlyBooleanProperty emptyProperty() {
            return ReadOnlyMapWrapper.this.emptyProperty();
        }
    }
}

