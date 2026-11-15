/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.property.FloatPropertyBase;

public class SimpleFloatProperty
extends FloatPropertyBase {
    private static final Object DEFAULT_BEAN = null;
    private static final String DEFAULT_NAME = "";
    private final Object bean;
    private final String name;

    @Override
    public Object getBean() {
        return this.bean;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public SimpleFloatProperty() {
        this(DEFAULT_BEAN, DEFAULT_NAME);
    }

    public SimpleFloatProperty(float f) {
        this(DEFAULT_BEAN, DEFAULT_NAME, f);
    }

    public SimpleFloatProperty(Object object, String string) {
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
    }

    public SimpleFloatProperty(Object object, String string, float f) {
        super(f);
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
    }
}

