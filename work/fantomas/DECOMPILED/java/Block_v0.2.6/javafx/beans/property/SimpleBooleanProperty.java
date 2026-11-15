/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.property.BooleanPropertyBase;

public class SimpleBooleanProperty
extends BooleanPropertyBase {
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

    public SimpleBooleanProperty() {
        this(DEFAULT_BEAN, DEFAULT_NAME);
    }

    public SimpleBooleanProperty(boolean bl) {
        this(DEFAULT_BEAN, DEFAULT_NAME, bl);
    }

    public SimpleBooleanProperty(Object object, String string) {
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
    }

    public SimpleBooleanProperty(Object object, String string, boolean bl) {
        super(bl);
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
    }
}

