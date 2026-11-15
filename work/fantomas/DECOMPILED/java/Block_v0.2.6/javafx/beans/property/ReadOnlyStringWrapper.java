/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringPropertyBase;
import javafx.beans.property.SimpleStringProperty;

public class ReadOnlyStringWrapper
extends SimpleStringProperty {
    private ReadOnlyPropertyImpl readOnlyProperty;

    public ReadOnlyStringWrapper() {
    }

    public ReadOnlyStringWrapper(String string) {
        super(string);
    }

    public ReadOnlyStringWrapper(Object object, String string) {
        super(object, string);
    }

    public ReadOnlyStringWrapper(Object object, String string, String string2) {
        super(object, string, string2);
    }

    public ReadOnlyStringProperty getReadOnlyProperty() {
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
    extends ReadOnlyStringPropertyBase {
        private ReadOnlyPropertyImpl() {
        }

        @Override
        public String get() {
            return ReadOnlyStringWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyStringWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyStringWrapper.this.getName();
        }
    }
}

