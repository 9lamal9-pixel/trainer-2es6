/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.DoubleExpression;
import javafx.beans.property.ReadOnlyDoublePropertyBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectPropertyBase;
import javafx.beans.property.ReadOnlyProperty;

public abstract class ReadOnlyDoubleProperty
extends DoubleExpression
implements ReadOnlyProperty<Number> {
    public String toString() {
        Object object = this.getBean();
        String string = this.getName();
        StringBuilder stringBuilder = new StringBuilder("ReadOnlyDoubleProperty [");
        if (object != null) {
            stringBuilder.append("bean: ").append(object).append(", ");
        }
        if (string != null && !string.equals("")) {
            stringBuilder.append("name: ").append(string).append(", ");
        }
        stringBuilder.append("value: ").append(this.get()).append("]");
        return stringBuilder.toString();
    }

    public static <T extends Number> ReadOnlyDoubleProperty readOnlyDoubleProperty(final ReadOnlyProperty<T> readOnlyProperty) {
        if (readOnlyProperty == null) {
            throw new NullPointerException("Property cannot be null");
        }
        return readOnlyProperty instanceof ReadOnlyDoubleProperty ? (ReadOnlyDoubleProperty)readOnlyProperty : new ReadOnlyDoublePropertyBase(){
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
            public double get() {
                this.valid = true;
                Number number = (Number)readOnlyProperty.getValue();
                return number == null ? 0.0 : number.doubleValue();
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

    public ReadOnlyObjectProperty<Double> asObject() {
        return new ReadOnlyObjectPropertyBase<Double>(){
            private boolean valid = true;
            private final InvalidationListener listener = observable -> {
                if (this.valid) {
                    this.valid = false;
                    this.fireValueChangedEvent();
                }
            };
            {
                ReadOnlyDoubleProperty.this.addListener(new WeakInvalidationListener(this.listener));
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return ReadOnlyDoubleProperty.this.getName();
            }

            @Override
            public Double get() {
                this.valid = true;
                return ReadOnlyDoubleProperty.this.getValue();
            }
        };
    }
}

