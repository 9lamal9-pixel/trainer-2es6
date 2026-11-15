/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.css;

import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.StyleableProperty;
import javafx.scene.Node;

public class SubCssMetaData<T>
extends CssMetaData<Node, T> {
    public SubCssMetaData(String string, StyleConverter styleConverter, T t) {
        super(string, styleConverter, t);
    }

    public SubCssMetaData(String string, StyleConverter styleConverter) {
        super(string, styleConverter);
    }

    @Override
    public boolean isSettable(Node node) {
        return false;
    }

    @Override
    public StyleableProperty<T> getStyleableProperty(Node node) {
        return null;
    }
}

