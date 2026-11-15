/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property.adapter;

import com.sun.javafx.property.adapter.ReadOnlyJavaBeanPropertyBuilderHelper;
import com.sun.javafx.property.adapter.ReadOnlyPropertyDescriptor;
import java.lang.reflect.Method;
import javafx.beans.property.adapter.ReadOnlyJavaBeanBooleanProperty;

public final class ReadOnlyJavaBeanBooleanPropertyBuilder {
    private final ReadOnlyJavaBeanPropertyBuilderHelper helper = new ReadOnlyJavaBeanPropertyBuilderHelper();

    public static ReadOnlyJavaBeanBooleanPropertyBuilder create() {
        return new ReadOnlyJavaBeanBooleanPropertyBuilder();
    }

    public ReadOnlyJavaBeanBooleanProperty build() throws NoSuchMethodException {
        ReadOnlyPropertyDescriptor readOnlyPropertyDescriptor = this.helper.getDescriptor();
        if (!Boolean.TYPE.equals(readOnlyPropertyDescriptor.getType()) && !Boolean.class.equals(readOnlyPropertyDescriptor.getType())) {
            throw new IllegalArgumentException("Not a boolean property");
        }
        return new ReadOnlyJavaBeanBooleanProperty(readOnlyPropertyDescriptor, this.helper.getBean());
    }

    public ReadOnlyJavaBeanBooleanPropertyBuilder name(String string) {
        this.helper.name(string);
        return this;
    }

    public ReadOnlyJavaBeanBooleanPropertyBuilder bean(Object object) {
        this.helper.bean(object);
        return this;
    }

    public ReadOnlyJavaBeanBooleanPropertyBuilder beanClass(Class<?> clazz) {
        this.helper.beanClass(clazz);
        return this;
    }

    public ReadOnlyJavaBeanBooleanPropertyBuilder getter(String string) {
        this.helper.getterName(string);
        return this;
    }

    public ReadOnlyJavaBeanBooleanPropertyBuilder getter(Method method) {
        this.helper.getter(method);
        return this;
    }
}

