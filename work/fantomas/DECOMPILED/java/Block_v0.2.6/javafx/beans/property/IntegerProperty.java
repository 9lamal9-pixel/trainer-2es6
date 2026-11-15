/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import com.sun.javafx.binding.BidirectionalBinding;
import com.sun.javafx.binding.Logging;
import java.security.AccessControlContext;
import java.security.AccessController;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.value.WritableIntegerValue;

public abstract class IntegerProperty
extends ReadOnlyIntegerProperty
implements Property<Number>,
WritableIntegerValue {
    @Override
    public void setValue(Number number) {
        if (number == null) {
            Logging.getLogger().fine("Attempt to set integer property to null, using default value instead.", new NullPointerException());
            this.set(0);
        } else {
            this.set(number.intValue());
        }
    }

    @Override
    public void bindBidirectional(Property<Number> property) {
        Bindings.bindBidirectional(this, property);
    }

    @Override
    public void unbindBidirectional(Property<Number> property) {
        Bindings.unbindBidirectional(this, property);
    }

    @Override
    public String toString() {
        Object object = this.getBean();
        String string = this.getName();
        StringBuilder stringBuilder = new StringBuilder("IntegerProperty [");
        if (object != null) {
            stringBuilder.append("bean: ").append(object).append(", ");
        }
        if (string != null && !string.equals("")) {
            stringBuilder.append("name: ").append(string).append(", ");
        }
        stringBuilder.append("value: ").append(this.get()).append("]");
        return stringBuilder.toString();
    }

    public static IntegerProperty integerProperty(final Property<Integer> property) {
        if (property == null) {
            throw new NullPointerException("Property cannot be null");
        }
        return new IntegerPropertyBase(){
            private final AccessControlContext acc = AccessController.getContext();
            {
                BidirectionalBinding.bindNumber(this, property);
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return property.getName();
            }

            protected void finalize() throws Throwable {
                try {
                    AccessController.doPrivileged(() -> {
                        BidirectionalBinding.unbindNumber(property, this);
                        return null;
                    }, this.acc);
                }
                finally {
                    super.finalize();
                }
            }
        };
    }

    @Override
    public ObjectProperty<Integer> asObject() {
        return new ObjectPropertyBase<Integer>(){
            private final AccessControlContext acc = AccessController.getContext();
            {
                BidirectionalBinding.bindNumber(this, IntegerProperty.this);
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return IntegerProperty.this.getName();
            }

            protected void finalize() throws Throwable {
                try {
                    AccessController.doPrivileged(() -> {
                        BidirectionalBinding.unbindNumber(this, IntegerProperty.this);
                        return null;
                    }, this.acc);
                }
                finally {
                    super.finalize();
                }
            }
        };
    }
}

