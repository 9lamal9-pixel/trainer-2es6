/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.fxml.builder.web;

import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.web.PopupFeatures;
import javafx.scene.web.PromptData;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.util.Builder;
import javafx.util.Callback;

@Deprecated
public final class WebEngineBuilder
implements Builder<WebEngine> {
    private Callback<String, Boolean> confirmHandler;
    private boolean confirmHandlerSet;
    private Callback<PopupFeatures, WebEngine> createPopupHandler;
    private boolean createPopupHandlerSet;
    private EventHandler<WebEvent<String>> onAlert;
    private boolean onAlertSet;
    private EventHandler<WebEvent<Rectangle2D>> onResized;
    private boolean onResizedSet;
    private EventHandler<WebEvent<String>> onStatusChanged;
    private boolean onStatusChangedSet;
    private EventHandler<WebEvent<Boolean>> onVisibilityChanged;
    private boolean onVisibilityChangedSet;
    private Callback<PromptData, String> promptHandler;
    private boolean promptHandlerSet;
    private String location;
    private boolean locationSet;

    public static WebEngineBuilder create() {
        return new WebEngineBuilder();
    }

    @Override
    public WebEngine build() {
        WebEngine webEngine = new WebEngine();
        this.applyTo(webEngine);
        return webEngine;
    }

    public void applyTo(WebEngine webEngine) {
        if (this.confirmHandlerSet) {
            webEngine.setConfirmHandler(this.confirmHandler);
        }
        if (this.createPopupHandlerSet) {
            webEngine.setCreatePopupHandler(this.createPopupHandler);
        }
        if (this.onAlertSet) {
            webEngine.setOnAlert(this.onAlert);
        }
        if (this.onResizedSet) {
            webEngine.setOnResized(this.onResized);
        }
        if (this.onStatusChangedSet) {
            webEngine.setOnStatusChanged(this.onStatusChanged);
        }
        if (this.onVisibilityChangedSet) {
            webEngine.setOnVisibilityChanged(this.onVisibilityChanged);
        }
        if (this.promptHandlerSet) {
            webEngine.setPromptHandler(this.promptHandler);
        }
        if (this.locationSet) {
            webEngine.load(this.location);
        }
    }

    public WebEngineBuilder confirmHandler(Callback<String, Boolean> callback) {
        this.confirmHandler = callback;
        this.confirmHandlerSet = true;
        return this;
    }

    public WebEngineBuilder createPopupHandler(Callback<PopupFeatures, WebEngine> callback) {
        this.createPopupHandler = callback;
        this.createPopupHandlerSet = true;
        return this;
    }

    public WebEngineBuilder onAlert(EventHandler<WebEvent<String>> eventHandler) {
        this.onAlert = eventHandler;
        this.onAlertSet = true;
        return this;
    }

    public WebEngineBuilder onResized(EventHandler<WebEvent<Rectangle2D>> eventHandler) {
        this.onResized = eventHandler;
        this.onResizedSet = true;
        return this;
    }

    public WebEngineBuilder onStatusChanged(EventHandler<WebEvent<String>> eventHandler) {
        this.onStatusChanged = eventHandler;
        this.onStatusChangedSet = true;
        return this;
    }

    public WebEngineBuilder onVisibilityChanged(EventHandler<WebEvent<Boolean>> eventHandler) {
        this.onVisibilityChanged = eventHandler;
        this.onVisibilityChangedSet = true;
        return this;
    }

    public WebEngineBuilder promptHandler(Callback<PromptData, String> callback) {
        this.promptHandler = callback;
        this.promptHandlerSet = true;
        return this;
    }

    public WebEngineBuilder location(String string) {
        this.location = string;
        this.locationSet = true;
        return this;
    }
}

