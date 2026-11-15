/*
 * Decompiled with CFR 0.152.
 */
package javafx.css;

import javafx.beans.NamedArg;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;

public class SimpleStyleableDoubleProperty
extends StyleableDoubleProperty {
    private static final Object DEFAULT_BEAN = null;
    private static final String DEFAULT_NAME = "";
    private final Object bean;
    private final String name;
    private final CssMetaData<? extends Styleable, Number> cssMetaData;

    public SimpleStyleableDoubleProperty(@NamedArg(value="cssMetaData") CssMetaData<? extends Styleable, Number> cssMetaData) {
        this(cssMetaData, DEFAULT_BEAN, DEFAULT_NAME);
    }

    public SimpleStyleableDoubleProperty(@NamedArg(value="cssMetaData") CssMetaData<? extends Styleable, Number> cssMetaData, @NamedArg(value="initialValue") Double d) {
        this(cssMetaData, DEFAULT_BEAN, DEFAULT_NAME, d);
    }

    public SimpleStyleableDoubleProperty(@NamedArg(value="cssMetaData") CssMetaData<? extends Styleable, Number> cssMetaData, @NamedArg(value="bean") Object object, @NamedArg(value="name") String string) {
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
        this.cssMetaData = cssMetaData;
    }

    public SimpleStyleableDoubleProperty(@NamedArg(value="cssMetaData") CssMetaData<? extends Styleable, Number> cssMetaData, @NamedArg(value="bean") Object object, @NamedArg(value="name") String string, @NamedArg(value="initialValue") Double d) {
        super(d);
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
    public final CssMetaData<? extends Styleable, Number> getCssMetaData() {
        return this.cssMetaData;
    }
}

