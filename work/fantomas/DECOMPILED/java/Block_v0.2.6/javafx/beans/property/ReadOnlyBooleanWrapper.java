/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;

public class ReadOnlyBooleanWrapper
extends SimpleBooleanProperty {
    private ReadOnlyPropertyImpl readOnlyProperty;

    public ReadOnlyBooleanWrapper() {
    }

    public ReadOnlyBooleanWrapper(boolean bl) {
        super(bl);
    }

    public ReadOnlyBooleanWrapper(Object object, String string) {
        super(object, string);
    }

    public ReadOnlyBooleanWrapper(Object object, String string, boolean bl) {
        super(object, string, bl);
    }

    public ReadOnlyBooleanProperty getReadOnlyProperty() {
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
    extends ReadOnlyBooleanPropertyBase {
        private ReadOnlyPropertyImpl() {
        }

        @Override
        public boolean get() {
            return ReadOnlyBooleanWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyBooleanWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyBooleanWrapper.this.getName();
        }
    }
}

