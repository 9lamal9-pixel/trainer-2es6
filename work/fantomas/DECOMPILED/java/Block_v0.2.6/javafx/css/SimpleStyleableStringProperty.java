/*
 * Decompiled with CFR 0.152.
 */
package javafx.css;

import javafx.beans.NamedArg;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableStringProperty;

public class SimpleStyleableStringProperty
extends StyleableStringProperty {
    private static final Object DEFAULT_BEAN = null;
    private static final String DEFAULT_NAME = "";
    private final Object bean;
    private final String name;
    private final CssMetaData<? extends Styleable, String> cssMetaData;

    public SimpleStyleableStringProperty(@NamedArg(value="cssMetaData") CssMetaData<? extends Styleable, String> cssMetaData) {
        this(cssMetaData, DEFAULT_BEAN, DEFAULT_NAME);
    }

    public SimpleStyleableStringProperty(@NamedArg(value="cssMetaData") CssMetaData<? extends Styleable, String> cssMetaData, @NamedArg(value="initialValue") String string) {
        this(cssMetaData, DEFAULT_BEAN, DEFAULT_NAME, string);
    }

    public SimpleStyleableStringProperty(@NamedArg(value="cssMetaData") CssMetaData<? extends Styleable, String> cssMetaData, @NamedArg(value="bean") Object object, @NamedArg(value="name") String string) {
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
        this.cssMetaData = cssMetaData;
    }

    public SimpleStyleableStringProperty(@NamedArg(value="cssMetaData") CssMetaData<? extends Styleable, String> cssMetaData, @NamedArg(value="bean") Object object, @NamedArg(value="name") String string, @NamedArg(value="initialValue") String string2) {
        super(string2);
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
    public final CssMetaData<? extends Styleable, String> getCssMetaData() {
        return this.cssMetaData;
    }
}

