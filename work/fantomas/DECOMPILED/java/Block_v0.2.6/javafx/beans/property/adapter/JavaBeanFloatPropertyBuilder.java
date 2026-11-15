/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property.adapter;

import com.sun.javafx.property.adapter.JavaBeanPropertyBuilderHelper;
import com.sun.javafx.property.adapter.PropertyDescriptor;
import java.lang.reflect.Method;
import javafx.beans.property.adapter.JavaBeanFloatProperty;

public final class JavaBeanFloatPropertyBuilder {
    private JavaBeanPropertyBuilderHelper helper = new JavaBeanPropertyBuilderHelper();

    public static JavaBeanFloatPropertyBuilder create() {
        return new JavaBeanFloatPropertyBuilder();
    }

    public JavaBeanFloatProperty build() throws NoSuchMethodException {
        PropertyDescriptor propertyDescriptor = this.helper.getDescriptor();
        if (!Float.TYPE.equals(propertyDescriptor.getType()) && !Number.class.isAssignableFrom(propertyDescriptor.getType())) {
            throw new IllegalArgumentException("Not a float property");
        }
        return new JavaBeanFloatProperty(propertyDescriptor, this.helper.getBean());
    }

    public JavaBeanFloatPropertyBuilder name(String string) {
        this.helper.name(string);
        return this;
    }

    public JavaBeanFloatPropertyBuilder bean(Object object) {
        this.helper.bean(object);
        return this;
    }

    public JavaBeanFloatPropertyBuilder beanClass(Class<?> clazz) {
        this.helper.beanClass(clazz);
        return this;
    }

    public JavaBeanFloatPropertyBuilder getter(String string) {
        this.helper.getterName(string);
        return this;
    }

    public JavaBeanFloatPropertyBuilder setter(String string) {
        this.helper.setterName(string);
        return this;
    }

    public JavaBeanFloatPropertyBuilder getter(Method method) {
        this.helper.getter(method);
        return this;
    }

    public JavaBeanFloatPropertyBuilder setter(Method method) {
        this.helper.setter(method);
        return this;
    }
}

