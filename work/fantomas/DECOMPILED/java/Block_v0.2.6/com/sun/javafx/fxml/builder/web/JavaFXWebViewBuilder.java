/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.fxml.builder.web;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Builder;

public class JavaFXWebViewBuilder
extends AbstractMap<String, Object>
implements Builder<WebView> {
    private final WebView view = new WebView();
    private String location = "";

    @Override
    public WebView build() {
        WebEngine webEngine = this.view.getEngine();
        if (this.location != null) {
            webEngine.load(this.location);
        }
        return this.view;
    }

    @Override
    public Object put(String string, Object object) {
        if (object != null) {
            String string2 = object.toString();
            if ("location".equals(string)) {
                this.location = string2;
            } else if ("onAlert".equals(string)) {
                // empty if block
            }
        }
        return null;
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

