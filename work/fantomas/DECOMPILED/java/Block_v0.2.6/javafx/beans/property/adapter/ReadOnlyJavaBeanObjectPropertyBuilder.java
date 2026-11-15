/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property.adapter;

import com.sun.javafx.property.adapter.ReadOnlyJavaBeanPropertyBuilderHelper;
import com.sun.javafx.property.adapter.ReadOnlyPropertyDescriptor;
import java.lang.reflect.Method;
import javafx.beans.property.adapter.ReadOnlyJavaBeanObjectProperty;

public final class ReadOnlyJavaBeanObjectPropertyBuilder<T> {
    private final ReadOnlyJavaBeanPropertyBuilderHelper helper = new ReadOnlyJavaBeanPropertyBuilderHelper();

    public static <T> ReadOnlyJavaBeanObjectPropertyBuilder<T> create() {
        return new ReadOnlyJavaBeanObjectPropertyBuilder<T>();
    }

    public ReadOnlyJavaBeanObjectProperty<T> build() throws NoSuchMethodException {
        ReadOnlyPropertyDescriptor readOnlyPropertyDescriptor = this.helper.getDescriptor();
        return new ReadOnlyJavaBeanObjectProperty(readOnlyPropertyDescriptor, this.helper.getBean());
    }

    public ReadOnlyJavaBeanObjectPropertyBuilder<T> name(String string) {
        this.helper.name(string);
        return this;
    }

    public ReadOnlyJavaBeanObjectPropertyBuilder<T> bean(Object object) {
        this.helper.bean(object);
        return this;
    }

    public ReadOnlyJavaBeanObjectPropertyBuilder<T> beanClass(Class<?> clazz) {
        this.helper.beanClass(clazz);
        return this;
    }

    public ReadOnlyJavaBeanObjectPropertyBuilder<T> getter(String string) {
        this.helper.getterName(string);
        return this;
    }

    public ReadOnlyJavaBeanObjectPropertyBuilder<T> getter(Method method) {
        this.helper.getter(method);
        return this;
    }
}

