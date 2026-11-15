/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.property.StringPropertyBase;

public class SimpleStringProperty
extends StringPropertyBase {
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

    public SimpleStringProperty() {
        this(DEFAULT_BEAN, DEFAULT_NAME);
    }

    public SimpleStringProperty(String string) {
        this(DEFAULT_BEAN, DEFAULT_NAME, string);
    }

    public SimpleStringProperty(Object object, String string) {
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
    }

    public SimpleStringProperty(Object object, String string, String string2) {
        super(string2);
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
    }
}

