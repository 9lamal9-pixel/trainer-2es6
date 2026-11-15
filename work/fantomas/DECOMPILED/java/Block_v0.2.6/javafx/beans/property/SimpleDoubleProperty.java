/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.property.DoublePropertyBase;

public class SimpleDoubleProperty
extends DoublePropertyBase {
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

    public SimpleDoubleProperty() {
        this(DEFAULT_BEAN, DEFAULT_NAME);
    }

    public SimpleDoubleProperty(double d) {
        this(DEFAULT_BEAN, DEFAULT_NAME, d);
    }

    public SimpleDoubleProperty(Object object, String string) {
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
    }

    public SimpleDoubleProperty(Object object, String string, double d) {
        super(d);
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
    }
}

