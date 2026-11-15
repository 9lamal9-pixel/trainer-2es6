/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.property.ListPropertyBase;
import javafx.collections.ObservableList;

public class SimpleListProperty<E>
extends ListPropertyBase<E> {
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

    public SimpleListProperty() {
        this(DEFAULT_BEAN, DEFAULT_NAME);
    }

    public SimpleListProperty(ObservableList<E> observableList) {
        this(DEFAULT_BEAN, DEFAULT_NAME, observableList);
    }

    public SimpleListProperty(Object object, String string) {
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
    }

    public SimpleListProperty(Object object, String string, ObservableList<E> observableList) {
        super(observableList);
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
    }
}

