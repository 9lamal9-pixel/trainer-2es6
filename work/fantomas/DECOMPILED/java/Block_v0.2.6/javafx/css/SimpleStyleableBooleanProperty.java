/*
 * Decompiled with CFR 0.152.
 */
package javafx.css;

import javafx.beans.NamedArg;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;

public class SimpleStyleableBooleanProperty
extends StyleableBooleanProperty {
    private static final Object DEFAULT_BEAN = null;
    private static final String DEFAULT_NAME = "";
    private final Object bean;
    private final String name;
    private final CssMetaData<? extends Styleable, Boolean> cssMetaData;

    public SimpleStyleableBooleanProperty(@NamedArg(value="cssMetaData") CssMetaData<? extends Styleable, Boolean> cssMetaData) {
        this(cssMetaData, DEFAULT_BEAN, DEFAULT_NAME);
    }

    public SimpleStyleableBooleanProperty(@NamedArg(value="cssMetaData") CssMetaData<? extends Styleable, Boolean> cssMetaData, @NamedArg(value="initialValue") boolean bl) {
        this(cssMetaData, DEFAULT_BEAN, DEFAULT_NAME, bl);
    }

    public SimpleStyleableBooleanProperty(@NamedArg(value="cssMetaData") CssMetaData<? extends Styleable, Boolean> cssMetaData, @NamedArg(value="bean") Object object, @NamedArg(value="name") String string) {
        this.bean = object;
        this.name = string == null ? DEFAULT_NAME : string;
        this.cssMetaData = cssMetaData;
    }

    public SimpleStyleableBooleanProperty(@NamedArg(value="cssMetaData") CssMetaData<? extends Styleable, Boolean> cssMetaData, @NamedArg(value="bean") Object object, @NamedArg(value="name") String string, @NamedArg(value="initialValue") boolean bl) {
        super(bl);
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
    public final CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
        return this.cssMetaData;
    }
}

