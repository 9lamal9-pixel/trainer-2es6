/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property.adapter;

import com.sun.javafx.property.adapter.JavaBeanPropertyBuilderHelper;
import com.sun.javafx.property.adapter.PropertyDescriptor;
import java.lang.reflect.Method;
import javafx.beans.property.adapter.JavaBeanDoubleProperty;

public final class JavaBeanDoublePropertyBuilder {
    private final JavaBeanPropertyBuilderHelper helper = new JavaBeanPropertyBuilderHelper();

    public static JavaBeanDoublePropertyBuilder create() {
        return new JavaBeanDoublePropertyBuilder();
    }

    public JavaBeanDoubleProperty build() throws NoSuchMethodException {
        PropertyDescriptor propertyDescriptor = this.helper.getDescriptor();
        if (!Double.TYPE.equals(propertyDescriptor.getType()) && !Number.class.isAssignableFrom(propertyDescriptor.getType())) {
            throw new IllegalArgumentException("Not a double property");
        }
        return new JavaBeanDoubleProperty(propertyDescriptor, this.helper.getBean());
    }

    public JavaBeanDoublePropertyBuilder name(String string) {
        this.helper.name(string);
        return this;
    }

    public JavaBeanDoublePropertyBuilder bean(Object object) {
        this.helper.bean(object);
        return this;
    }

    public JavaBeanDoublePropertyBuilder beanClass(Class<?> clazz) {
        this.helper.beanClass(clazz);
        return this;
    }

    public JavaBeanDoublePropertyBuilder getter(String string) {
        this.helper.getterName(string);
        return this;
    }

    public JavaBeanDoublePropertyBuilder setter(String string) {
        this.helper.setterName(string);
        return this;
    }

    public JavaBeanDoublePropertyBuilder getter(Method method) {
        this.helper.getter(method);
        return this;
    }

    public JavaBeanDoublePropertyBuilder setter(Method method) {
        this.helper.setter(method);
        return this;
    }
}

