/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.property.LongPropertyBase;

public class SimpleLongProperty
extends LongPropertyBase {
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

    public SimpleLongProperty() {
        this(DEFAULT_BEAN, DEFAULT_NAME);
    }

    public SimpleLongProperty(long l) {
        this(DEFAULT_BEAN, DEFAULT_NAME, l);
    }

    public SimpleLongProperty(Object object, String string) {
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
    }

    public SimpleLongProperty(Object object, String string, long l) {
        super(l);
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
    }
}

