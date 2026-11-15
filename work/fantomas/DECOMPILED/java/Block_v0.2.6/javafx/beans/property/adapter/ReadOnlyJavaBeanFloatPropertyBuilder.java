/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property.adapter;

import com.sun.javafx.property.adapter.ReadOnlyJavaBeanPropertyBuilderHelper;
import com.sun.javafx.property.adapter.ReadOnlyPropertyDescriptor;
import java.lang.reflect.Method;
import javafx.beans.property.adapter.ReadOnlyJavaBeanFloatProperty;

public final class ReadOnlyJavaBeanFloatPropertyBuilder {
    private final ReadOnlyJavaBeanPropertyBuilderHelper helper = new ReadOnlyJavaBeanPropertyBuilderHelper();

    public static ReadOnlyJavaBeanFloatPropertyBuilder create() {
        return new ReadOnlyJavaBeanFloatPropertyBuilder();
    }

    public ReadOnlyJavaBeanFloatProperty build() throws NoSuchMethodException {
        ReadOnlyPropertyDescriptor readOnlyPropertyDescriptor = this.helper.getDescriptor();
        if (!Float.TYPE.equals(readOnlyPropertyDescriptor.getType()) && !Number.class.isAssignableFrom(readOnlyPropertyDescriptor.getType())) {
            throw new IllegalArgumentException("Not a float property");
        }
        return new ReadOnlyJavaBeanFloatProperty(readOnlyPropertyDescriptor, this.helper.getBean());
    }

    public ReadOnlyJavaBeanFloatPropertyBuilder name(String string) {
        this.helper.name(string);
        return this;
    }

    public ReadOnlyJavaBeanFloatPropertyBuilder bean(Object object) {
        this.helper.bean(object);
        return this;
    }

    public ReadOnlyJavaBeanFloatPropertyBuilder beanClass(Class<?> clazz) {
        this.helper.beanClass(clazz);
        return this;
    }

    public ReadOnlyJavaBeanFloatPropertyBuilder getter(String string) {
        this.helper.getterName(string);
        return this;
    }

    public ReadOnlyJavaBeanFloatPropertyBuilder getter(Method method) {
        this.helper.getter(method);
        return this;
    }
}

