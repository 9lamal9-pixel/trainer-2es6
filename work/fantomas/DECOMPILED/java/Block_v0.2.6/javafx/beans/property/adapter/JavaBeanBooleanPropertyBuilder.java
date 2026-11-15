/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property.adapter;

import com.sun.javafx.property.adapter.JavaBeanPropertyBuilderHelper;
import com.sun.javafx.property.adapter.PropertyDescriptor;
import java.lang.reflect.Method;
import javafx.beans.property.adapter.JavaBeanBooleanProperty;

public final class JavaBeanBooleanPropertyBuilder {
    private final JavaBeanPropertyBuilderHelper helper = new JavaBeanPropertyBuilderHelper();

    public static JavaBeanBooleanPropertyBuilder create() {
        return new JavaBeanBooleanPropertyBuilder();
    }

    public JavaBeanBooleanProperty build() throws NoSuchMethodException {
        PropertyDescriptor propertyDescriptor = this.helper.getDescriptor();
        if (!Boolean.TYPE.equals(propertyDescriptor.getType()) && !Boolean.class.equals(propertyDescriptor.getType())) {
            throw new IllegalArgumentException("Not a boolean property");
        }
        return new JavaBeanBooleanProperty(propertyDescriptor, this.helper.getBean());
    }

    public JavaBeanBooleanPropertyBuilder name(String string) {
        this.helper.name(string);
        return this;
    }

    public JavaBeanBooleanPropertyBuilder bean(Object object) {
        this.helper.bean(object);
        return this;
    }

    public JavaBeanBooleanPropertyBuilder beanClass(Class<?> clazz) {
        this.helper.beanClass(clazz);
        return this;
    }

    public JavaBeanBooleanPropertyBuilder getter(String string) {
        this.helper.getterName(string);
        return this;
    }

    public JavaBeanBooleanPropertyBuilder setter(String string) {
        this.helper.setterName(string);
        return this;
    }

    public JavaBeanBooleanPropertyBuilder getter(Method method) {
        this.helper.getter(method);
        return this;
    }

    public JavaBeanBooleanPropertyBuilder setter(Method method) {
        this.helper.setter(method);
        return this;
    }
}

