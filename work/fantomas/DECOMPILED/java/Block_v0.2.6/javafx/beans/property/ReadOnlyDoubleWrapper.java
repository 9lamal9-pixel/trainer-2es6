/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoublePropertyBase;
import javafx.beans.property.SimpleDoubleProperty;

public class ReadOnlyDoubleWrapper
extends SimpleDoubleProperty {
    private ReadOnlyPropertyImpl readOnlyProperty;

    public ReadOnlyDoubleWrapper() {
    }

    public ReadOnlyDoubleWrapper(double d) {
        super(d);
    }

    public ReadOnlyDoubleWrapper(Object object, String string) {
        super(object, string);
    }

    public ReadOnlyDoubleWrapper(Object object, String string, double d) {
        super(object, string, d);
    }

    public ReadOnlyDoubleProperty getReadOnlyProperty() {
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
    extends ReadOnlyDoublePropertyBase {
        private ReadOnlyPropertyImpl() {
        }

        @Override
        public double get() {
            return ReadOnlyDoubleWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyDoubleWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyDoubleWrapper.this.getName();
        }
    }
}

