/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property.adapter;

import com.sun.javafx.property.adapter.ReadOnlyJavaBeanPropertyBuilderHelper;
import com.sun.javafx.property.adapter.ReadOnlyPropertyDescriptor;
import java.lang.reflect.Method;
import javafx.beans.property.adapter.ReadOnlyJavaBeanStringProperty;

public final class ReadOnlyJavaBeanStringPropertyBuilder {
    private final ReadOnlyJavaBeanPropertyBuilderHelper helper = new ReadOnlyJavaBeanPropertyBuilderHelper();

    public static ReadOnlyJavaBeanStringPropertyBuilder create() {
        return new ReadOnlyJavaBeanStringPropertyBuilder();
    }

    public ReadOnlyJavaBeanStringProperty build() throws NoSuchMethodException {
        ReadOnlyPropertyDescriptor readOnlyPropertyDescriptor = this.helper.getDescriptor();
        if (!String.class.equals(readOnlyPropertyDescriptor.getType())) {
            throw new IllegalArgumentException("Not a String property");
        }
        return new ReadOnlyJavaBeanStringProperty(readOnlyPropertyDescriptor, this.helper.getBean());
    }

    public ReadOnlyJavaBeanStringPropertyBuilder name(String string) {
        this.helper.name(string);
        return this;
    }

    public ReadOnlyJavaBeanStringPropertyBuilder bean(Object object) {
        this.helper.bean(object);
        return this;
    }

    public ReadOnlyJavaBeanStringPropertyBuilder beanClass(Class<?> clazz) {
        this.helper.beanClass(clazz);
        return this;
    }

    public ReadOnlyJavaBeanStringPropertyBuilder getter(String string) {
        this.helper.getterName(string);
        return this;
    }

    public ReadOnlyJavaBeanStringPropertyBuilder getter(Method method) {
        this.helper.getter(method);
        return this;
    }
}

