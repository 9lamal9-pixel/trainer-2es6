/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property.adapter;

import com.sun.javafx.property.adapter.JavaBeanPropertyBuilderHelper;
import com.sun.javafx.property.adapter.PropertyDescriptor;
import java.lang.reflect.Method;
import javafx.beans.property.adapter.JavaBeanLongProperty;

public final class JavaBeanLongPropertyBuilder {
    private JavaBeanPropertyBuilderHelper helper = new JavaBeanPropertyBuilderHelper();

    public static JavaBeanLongPropertyBuilder create() {
        return new JavaBeanLongPropertyBuilder();
    }

    public JavaBeanLongProperty build() throws NoSuchMethodException {
        PropertyDescriptor propertyDescriptor = this.helper.getDescriptor();
        if (!Long.TYPE.equals(propertyDescriptor.getType()) && !Number.class.isAssignableFrom(propertyDescriptor.getType())) {
            throw new IllegalArgumentException("Not a long property");
        }
        return new JavaBeanLongProperty(propertyDescriptor, this.helper.getBean());
    }

    public JavaBeanLongPropertyBuilder name(String string) {
        this.helper.name(string);
        return this;
    }

    public JavaBeanLongPropertyBuilder bean(Object object) {
        this.helper.bean(object);
        return this;
    }

    public JavaBeanLongPropertyBuilder beanClass(Class<?> clazz) {
        this.helper.beanClass(clazz);
        return this;
    }

    public JavaBeanLongPropertyBuilder getter(String string) {
        this.helper.getterName(string);
        return this;
    }

    public JavaBeanLongPropertyBuilder setter(String string) {
        this.helper.setterName(string);
        return this;
    }

    public JavaBeanLongPropertyBuilder getter(Method method) {
        this.helper.getter(method);
        return this;
    }

    public JavaBeanLongPropertyBuilder setter(Method method) {
        this.helper.setter(method);
        return this;
    }
}

