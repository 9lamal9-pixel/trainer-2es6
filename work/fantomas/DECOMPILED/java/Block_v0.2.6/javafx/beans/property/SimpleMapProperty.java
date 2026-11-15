/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans.property;

import javafx.beans.property.MapPropertyBase;
import javafx.collections.ObservableMap;

public class SimpleMapProperty<K, V>
extends MapPropertyBase<K, V> {
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

    public SimpleMapProperty() {
        this(DEFAULT_BEAN, DEFAULT_NAME);
    }

    public SimpleMapProperty(ObservableMap<K, V> observableMap) {
        this(DEFAULT_BEAN, DEFAULT_NAME, observableMap);
    }

    public SimpleMapProperty(Object object, String string) {
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
    }

    public SimpleMapProperty(Object object, String string, ObservableMap<K, V> observableMap) {
        super(observableMap);
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
    }
}

