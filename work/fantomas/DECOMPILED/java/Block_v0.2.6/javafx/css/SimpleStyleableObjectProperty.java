/*
 * Decompiled with CFR 0.152.
 */
package javafx.css;

import javafx.beans.NamedArg;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;

public class SimpleStyleableObjectProperty<T>
extends StyleableObjectProperty<T> {
    private static final Object DEFAULT_BEAN = null;
    private static final String DEFAULT_NAME = "";
    private final Object bean;
    private final String name;
    private final CssMetaData<? extends Styleable, T> cssMetaData;

    public SimpleStyleableObjectProperty(@NamedArg(value="cssMetaData") CssMetaData<? extends Styleable, T> cssMetaData) {
        this(cssMetaData, DEFAULT_BEAN, DEFAULT_NAME);
    }

    public SimpleStyleableObjectProperty(@NamedArg(value="cssMetaData") CssMetaData<? extends Styleable, T> cssMetaData, @NamedArg(value="initialValue") T t) {
        this(cssMetaData, DEFAULT_BEAN, DEFAULT_NAME, t);
    }

    public SimpleStyleableObjectProperty(@NamedArg(value="cssMetaData") CssMetaData<? extends Styleable, T> cssMetaData, @NamedArg(value="bean") Object object, @NamedArg(value="name") String string) {
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
        this.cssMetaData = cssMetaData;
    }

    public SimpleStyleableObjectProperty(@NamedArg(value="cssMetaData") CssMetaData<? extends Styleable, T> cssMetaData, @NamedArg(value="bean") Object object, @NamedArg(value="name") String string, @NamedArg(value="initialValue") T t) {
        super(t);
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
        this.cssMetaData = cssMetaData;
    }

    @Override
    public Object getBean() {
        return this.bean;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public final CssMetaData<? extends Styleable, T> getCssMetaData() {
        return this.cssMetaData;
    }
}

