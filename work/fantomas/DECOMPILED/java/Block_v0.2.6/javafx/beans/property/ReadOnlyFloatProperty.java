/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.FloatExpression;
import javafx.beans.property.ReadOnlyFloatPropertyBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectPropertyBase;
import javafx.beans.property.ReadOnlyProperty;

public abstract class ReadOnlyFloatProperty
extends FloatExpression
implements ReadOnlyProperty<Number> {
    public String toString() {
        Object object = this.getBean();
        String string = this.getName();
        StringBuilder stringBuilder = new StringBuilder("ReadOnlyFloatProperty [");
        if (object != null) {
            stringBuilder.append("bean: ").append(object).append(", ");
        }
        if (string != null && !string.equals("")) {
            stringBuilder.append("name: ").append(string).append(", ");
        }
        stringBuilder.append("value: ").append(this.get()).append("]");
        return stringBuilder.toString();
    }

    public static <T extends Number> ReadOnlyFloatProperty readOnlyFloatProperty(final ReadOnlyProperty<T> readOnlyProperty) {
        if (readOnlyProperty == null) {
            throw new NullPointerException("Property cannot be null");
        }
        return readOnlyProperty instanceof ReadOnlyFloatProperty ? (ReadOnlyFloatProperty)readOnlyProperty : new ReadOnlyFloatPropertyBase(){
            private boolean valid = true;
            private final InvalidationListener listener = observable -> {
                if (this.valid) {
                    this.valid = false;
                    this.fireValueChangedEvent();
                }
            };
            {
                readOnlyProperty.addListener(new WeakInvalidationListener(this.listener));
            }

            @Override
            public float get() {
                this.valid = true;
                Number number = (Number)readOnlyProperty.getValue();
                return number == null ? 0.0f : number.floatValue();
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return readOnlyProperty.getName();
            }
        };
    }

    public ReadOnlyObjectProperty<Float> asObject() {
        return new ReadOnlyObjectPropertyBase<Float>(){
            private boolean valid = true;
            private final InvalidationListener listener = observable -> {
                if (this.valid) {
                    this.valid = false;
                    this.fireValueChangedEvent();
                }
            };
            {
                ReadOnlyFloatProperty.this.addListener(new WeakInvalidationListener(this.listener));
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return ReadOnlyFloatProperty.this.getName();
            }

            @Override
            public Float get() {
                this.valid = true;
                return ReadOnlyFloatProperty.this.getValue();
            }
        };
    }
}

