/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property.adapter;

import com.sun.javafx.property.adapter.JavaBeanPropertyBuilderHelper;
import com.sun.javafx.property.adapter.PropertyDescriptor;
import java.lang.reflect.Method;
import javafx.beans.property.adapter.JavaBeanObjectProperty;

public final class JavaBeanObjectPropertyBuilder<T> {
    private JavaBeanPropertyBuilderHelper helper = new JavaBeanPropertyBuilderHelper();

    public static JavaBeanObjectPropertyBuilder create() {
        return new JavaBeanObjectPropertyBuilder();
    }

    public JavaBeanObjectProperty<T> build() throws NoSuchMethodException {
        PropertyDescriptor propertyDescriptor = this.helper.getDescriptor();
        return new JavaBeanObjectProperty(propertyDescriptor, this.helper.getBean());
    }

    public JavaBeanObjectPropertyBuilder name(String string) {
        this.helper.name(string);
        return this;
    }

    public JavaBeanObjectPropertyBuilder bean(Object object) {
        this.helper.bean(object);
        return this;
    }

    public JavaBeanObjectPropertyBuilder beanClass(Class<?> clazz) {
        this.helper.beanClass(clazz);
        return this;
    }

    public JavaBeanObjectPropertyBuilder getter(String string) {
        this.helper.getterName(string);
        return this;
    }

    public JavaBeanObjectPropertyBuilder setter(String string) {
        this.helper.setterName(string);
        return this;
    }

    public JavaBeanObjectPropertyBuilder getter(Method method) {
        this.helper.getter(method);
        return this;
    }

    public JavaBeanObjectPropertyBuilder setter(Method method) {
        this.helper.setter(method);
        return this;
    }
}

