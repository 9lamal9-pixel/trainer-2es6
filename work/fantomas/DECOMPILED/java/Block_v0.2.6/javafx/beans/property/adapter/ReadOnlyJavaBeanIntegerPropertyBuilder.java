/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property.adapter;

import com.sun.javafx.property.adapter.ReadOnlyJavaBeanPropertyBuilderHelper;
import com.sun.javafx.property.adapter.ReadOnlyPropertyDescriptor;
import java.lang.reflect.Method;
import javafx.beans.property.adapter.ReadOnlyJavaBeanIntegerProperty;

public final class ReadOnlyJavaBeanIntegerPropertyBuilder {
    private final ReadOnlyJavaBeanPropertyBuilderHelper helper = new ReadOnlyJavaBeanPropertyBuilderHelper();

    public static ReadOnlyJavaBeanIntegerPropertyBuilder create() {
        return new ReadOnlyJavaBeanIntegerPropertyBuilder();
    }

    public ReadOnlyJavaBeanIntegerProperty build() throws NoSuchMethodException {
        ReadOnlyPropertyDescriptor readOnlyPropertyDescriptor = this.helper.getDescriptor();
        if (!Integer.TYPE.equals(readOnlyPropertyDescriptor.getType()) && !Number.class.isAssignableFrom(readOnlyPropertyDescriptor.getType())) {
            throw new IllegalArgumentException("Not an int property");
        }
        return new ReadOnlyJavaBeanIntegerProperty(readOnlyPropertyDescriptor, this.helper.getBean());
    }

    public ReadOnlyJavaBeanIntegerPropertyBuilder name(String string) {
        this.helper.name(string);
        return this;
    }

    public ReadOnlyJavaBeanIntegerPropertyBuilder bean(Object object) {
        this.helper.bean(object);
        return this;
    }

    public ReadOnlyJavaBeanIntegerPropertyBuilder beanClass(Class<?> clazz) {
        this.helper.beanClass(clazz);
        return this;
    }

    public ReadOnlyJavaBeanIntegerPropertyBuilder getter(String string) {
        this.helper.getterName(string);
        return this;
    }

    public ReadOnlyJavaBeanIntegerPropertyBuilder getter(Method method) {
        this.helper.getter(method);
        return this;
    }
}

