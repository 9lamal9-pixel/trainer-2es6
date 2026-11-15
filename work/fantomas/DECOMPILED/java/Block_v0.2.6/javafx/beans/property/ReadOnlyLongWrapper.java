/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyLongPropertyBase;
import javafx.beans.property.SimpleLongProperty;

public class ReadOnlyLongWrapper
extends SimpleLongProperty {
    private ReadOnlyPropertyImpl readOnlyProperty;

    public ReadOnlyLongWrapper() {
    }

    public ReadOnlyLongWrapper(long l) {
        super(l);
    }

    public ReadOnlyLongWrapper(Object object, String string) {
        super(object, string);
    }

    public ReadOnlyLongWrapper(Object object, String string, long l) {
        super(object, string, l);
    }

    public ReadOnlyLongProperty getReadOnlyProperty() {
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

    private class ReadOnlyPropertyImpl
    extends ReadOnlyLongPropertyBase {
        private ReadOnlyPropertyImpl() {
        }

        @Override
        public long get() {
            return ReadOnlyLongWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyLongWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyLongWrapper.this.getName();
        }
    }
}

