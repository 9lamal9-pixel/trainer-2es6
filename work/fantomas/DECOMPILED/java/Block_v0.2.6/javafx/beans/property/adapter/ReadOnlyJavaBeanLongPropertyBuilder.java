/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property.adapter;

import com.sun.javafx.property.adapter.ReadOnlyJavaBeanPropertyBuilderHelper;
import com.sun.javafx.property.adapter.ReadOnlyPropertyDescriptor;
import java.lang.reflect.Method;
import javafx.beans.property.adapter.ReadOnlyJavaBeanLongProperty;

public final class ReadOnlyJavaBeanLongPropertyBuilder {
    private final ReadOnlyJavaBeanPropertyBuilderHelper helper = new ReadOnlyJavaBeanPropertyBuilderHelper();

    public static ReadOnlyJavaBeanLongPropertyBuilder create() {
        return new ReadOnlyJavaBeanLongPropertyBuilder();
    }

    public ReadOnlyJavaBeanLongProperty build() throws NoSuchMethodException {
        ReadOnlyPropertyDescriptor readOnlyPropertyDescriptor = this.helper.getDescriptor();
        if (!Long.TYPE.equals(readOnlyPropertyDescriptor.getType()) && !Number.class.isAssignableFrom(readOnlyPropertyDescriptor.getType())) {
            throw new IllegalArgumentException("Not a long property");
        }
        return new ReadOnlyJavaBeanLongProperty(readOnlyPropertyDescriptor, this.helper.getBean());
    }

    public ReadOnlyJavaBeanLongPropertyBuilder name(String string) {
        this.helper.name(string);
        return this;
    }

    public ReadOnlyJavaBeanLongPropertyBuilder bean(Object object) {
        this.helper.bean(object);
        return this;
    }

    public ReadOnlyJavaBeanLongPropertyBuilder beanClass(Class<?> clazz) {
        this.helper.beanClass(clazz);
        return this;
    }

    public ReadOnlyJavaBeanLongPropertyBuilder getter(String string) {
        this.helper.getterName(string);
        return this;
    }

    public ReadOnlyJavaBeanLongPropertyBuilder getter(Method method) {
        this.helper.getter(method);
        return this;
    }
}

