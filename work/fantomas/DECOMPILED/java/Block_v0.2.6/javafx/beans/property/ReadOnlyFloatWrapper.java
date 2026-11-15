/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.property.ReadOnlyFloatProperty;
import javafx.beans.property.ReadOnlyFloatPropertyBase;
import javafx.beans.property.SimpleFloatProperty;

public class ReadOnlyFloatWrapper
extends SimpleFloatProperty {
    private ReadOnlyPropertyImpl readOnlyProperty;

    public ReadOnlyFloatWrapper() {
    }

    public ReadOnlyFloatWrapper(float f) {
        super(f);
    }

    public ReadOnlyFloatWrapper(Object object, String string) {
        super(object, string);
    }

    public ReadOnlyFloatWrapper(Object object, String string, float f) {
        super(object, string, f);
    }

    public ReadOnlyFloatProperty getReadOnlyProperty() {
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
    extends ReadOnlyFloatPropertyBase {
        private ReadOnlyPropertyImpl() {
        }

        @Override
        public float get() {
            return ReadOnlyFloatWrapper.this.get();
        }

        @Override
        public Object getBean() {
            return ReadOnlyFloatWrapper.this.getBean();
        }

        @Override
        public String getName() {
            return ReadOnlyFloatWrapper.this.getName();
        }
    }
}

