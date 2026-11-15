/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property.adapter;

import com.sun.javafx.property.adapter.JavaBeanPropertyBuilderHelper;
import com.sun.javafx.property.adapter.PropertyDescriptor;
import java.lang.reflect.Method;
import javafx.beans.property.adapter.JavaBeanIntegerProperty;

public final class JavaBeanIntegerPropertyBuilder {
    private JavaBeanPropertyBuilderHelper helper = new JavaBeanPropertyBuilderHelper();

    public static JavaBeanIntegerPropertyBuilder create() {
        return new JavaBeanIntegerPropertyBuilder();
    }

    public JavaBeanIntegerProperty build() throws NoSuchMethodException {
        PropertyDescriptor propertyDescriptor = this.helper.getDescriptor();
        if (!Integer.TYPE.equals(propertyDescriptor.getType()) && !Number.class.isAssignableFrom(propertyDescriptor.getType())) {
            throw new IllegalArgumentException("Not an int property");
        }
        return new JavaBeanIntegerProperty(propertyDescriptor, this.helper.getBean());
    }

    public JavaBeanIntegerPropertyBuilder name(String string) {
        this.helper.name(string);
        return this;
    }

    public JavaBeanIntegerPropertyBuilder bean(Object object) {
        this.helper.bean(object);
        return this;
    }

    public JavaBeanIntegerPropertyBuilder beanClass(Class<?> clazz) {
        this.helper.beanClass(clazz);
        return this;
    }

    public JavaBeanIntegerPropertyBuilder getter(String string) {
        this.helper.getterName(string);
        return this;
    }

    public JavaBeanIntegerPropertyBuilder setter(String string) {
        this.helper.setterName(string);
        return this;
    }

    public JavaBeanIntegerPropertyBuilder getter(Method method) {
        this.helper.getter(method);
        return this;
    }

    public JavaBeanIntegerPropertyBuilder setter(Method method) {
        this.helper.setter(method);
        return this;
    }
}

