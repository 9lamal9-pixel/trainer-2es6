/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property.adapter;

import com.sun.javafx.property.adapter.JavaBeanPropertyBuilderHelper;
import com.sun.javafx.property.adapter.PropertyDescriptor;
import java.lang.reflect.Method;
import javafx.beans.property.adapter.JavaBeanStringProperty;

public final class JavaBeanStringPropertyBuilder {
    private JavaBeanPropertyBuilderHelper helper = new JavaBeanPropertyBuilderHelper();

    public static JavaBeanStringPropertyBuilder create() {
        return new JavaBeanStringPropertyBuilder();
    }

    public JavaBeanStringProperty build() throws NoSuchMethodException {
        PropertyDescriptor propertyDescriptor = this.helper.getDescriptor();
        if (!String.class.equals(propertyDescriptor.getType())) {
            throw new IllegalArgumentException("Not a String property");
        }
        return new JavaBeanStringProperty(propertyDescriptor, this.helper.getBean());
    }

    public JavaBeanStringPropertyBuilder name(String string) {
        this.helper.name(string);
        return this;
    }

    public JavaBeanStringPropertyBuilder bean(Object object) {
        this.helper.bean(object);
        return this;
    }

    public JavaBeanStringPropertyBuilder beanClass(Class<?> clazz) {
        this.helper.beanClass(clazz);
        return this;
    }

    public JavaBeanStringPropertyBuilder getter(String string) {
        this.helper.getterName(string);
        return this;
    }

    public JavaBeanStringPropertyBuilder setter(String string) {
        this.helper.setterName(string);
        return this;
    }

    public JavaBeanStringPropertyBuilder getter(Method method) {
        this.helper.getter(method);
        return this;
    }

    public JavaBeanStringPropertyBuilder setter(Method method) {
        this.helper.setter(method);
        return this;
    }
}

