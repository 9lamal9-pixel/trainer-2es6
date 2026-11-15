/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property.adapter;

import com.sun.javafx.property.adapter.ReadOnlyJavaBeanPropertyBuilderHelper;
import com.sun.javafx.property.adapter.ReadOnlyPropertyDescriptor;
import java.lang.reflect.Method;
import javafx.beans.property.adapter.ReadOnlyJavaBeanDoubleProperty;

public final class ReadOnlyJavaBeanDoublePropertyBuilder {
    private final ReadOnlyJavaBeanPropertyBuilderHelper helper = new ReadOnlyJavaBeanPropertyBuilderHelper();

    public static ReadOnlyJavaBeanDoublePropertyBuilder create() {
        return new ReadOnlyJavaBeanDoublePropertyBuilder();
    }

    public ReadOnlyJavaBeanDoubleProperty build() throws NoSuchMethodException {
        ReadOnlyPropertyDescriptor readOnlyPropertyDescriptor = this.helper.getDescriptor();
        if (!Double.TYPE.equals(readOnlyPropertyDescriptor.getType()) && !Number.class.isAssignableFrom(readOnlyPropertyDescriptor.getType())) {
            throw new IllegalArgumentException("Not a double property");
        }
        return new ReadOnlyJavaBeanDoubleProperty(readOnlyPropertyDescriptor, this.helper.getBean());
    }

    public ReadOnlyJavaBeanDoublePropertyBuilder name(String string) {
        this.helper.name(string);
        return this;
    }

    public ReadOnlyJavaBeanDoublePropertyBuilder bean(Object object) {
        this.helper.bean(object);
        return this;
    }

    public ReadOnlyJavaBeanDoublePropertyBuilder beanClass(Class<?> clazz) {
        this.helper.beanClass(clazz);
        return this;
    }

    public ReadOnlyJavaBeanDoublePropertyBuilder getter(String string) {
        this.helper.getterName(string);
        return this;
    }

    public ReadOnlyJavaBeanDoublePropertyBuilder getter(Method method) {
        this.helper.getter(method);
        return this;
    }
}

