/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.property.SetPropertyBase;
import javafx.collections.ObservableSet;

public class SimpleSetProperty<E>
extends SetPropertyBase<E> {
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

    public SimpleSetProperty() {
        this(DEFAULT_BEAN, DEFAULT_NAME);
    }

    public SimpleSetProperty(ObservableSet<E> observableSet) {
        this(DEFAULT_BEAN, DEFAULT_NAME, observableSet);
    }

    public SimpleSetProperty(Object object, String string) {
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
    }

    public SimpleSetProperty(Object object, String string, ObservableSet<E> observableSet) {
        super(observableSet);
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
    }
}

