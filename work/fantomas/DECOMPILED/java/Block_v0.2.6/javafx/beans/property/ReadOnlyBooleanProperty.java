/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.ReadOnlyBooleanPropertyBase;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectPropertyBase;
import javafx.beans.property.ReadOnlyProperty;

public abstract class ReadOnlyBooleanProperty
extends BooleanExpression
implements ReadOnlyProperty<Boolean> {
    public String toString() {
        Object object = this.getBean();
        String string = this.getName();
        StringBuilder stringBuilder = new StringBuilder("ReadOnlyBooleanProperty [");
        if (object != null) {
            stringBuilder.append("bean: ").append(object).append(", ");
        }
        if (string != null && !string.equals("")) {
            stringBuilder.append("name: ").append(string).append(", ");
        }
        stringBuilder.append("value: ").append(this.get()).append("]");
        return stringBuilder.toString();
    }

    public static ReadOnlyBooleanProperty readOnlyBooleanProperty(final ReadOnlyProperty<Boolean> readOnlyProperty) {
        if (readOnlyProperty == null) {
            throw new NullPointerException("Property cannot be null");
        }
        return readOnlyProperty instanceof ReadOnlyBooleanProperty ? (ReadOnlyBooleanProperty)readOnlyProperty : new ReadOnlyBooleanPropertyBase(){
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
            public boolean get() {
                this.valid = true;
                Boolean bl = (Boolean)readOnlyProperty.getValue();
                return bl == null ? false : bl;
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

    public ReadOnlyObjectProperty<Boolean> asObject() {
        return new ReadOnlyObjectPropertyBase<Boolean>(){
            private boolean valid = true;
            private final InvalidationListener listener = observable -> {
                if (this.valid) {
                    this.valid = false;
                    this.fireValueChangedEvent();
                }
            };
            {
                ReadOnlyBooleanProperty.this.addListener(new WeakInvalidationListener(this.listener));
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return ReadOnlyBooleanProperty.this.getName();
            }

            @Override
            public Boolean get() {
                this.valid = true;
                return ReadOnlyBooleanProperty.this.getValue();
            }
        };
    }
}

