/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.skin;

import com.sun.javafx.scene.control.skin.FXVKSkin;
import javafx.application.ConditionalFeature;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyEvent;

public class FXVK
extends Control {
    private final ObjectProperty<EventHandler<KeyEvent>> onAction = new SimpleObjectProperty<EventHandler<KeyEvent>>(this, "onAction");
    static final String[] VK_TYPE_NAMES = new String[]{"text", "numeric", "url", "email"};
    public static final String VK_TYPE_PROP_KEY = "vkType";
    String[] chars;
    private ObjectProperty<Node> attachedNode;
    static FXVK vk;
    private static final boolean IS_FXVK_SUPPORTED;
    private static boolean USE_FXVK;
    private static final String DEFAULT_STYLE_CLASS = "fxvk";

    public final void setOnAction(EventHandler<KeyEvent> eventHandler) {
        this.onAction.set(eventHandler);
    }

    public final EventHandler<KeyEvent> getOnAction() {
        return (EventHandler)this.onAction.get();
    }

    public final ObjectProperty<EventHandler<KeyEvent>> onActionProperty() {
        return this.onAction;
    }

    public FXVK() {
        this.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    final ObjectProperty<Node> attachedNodeProperty() {
        if (this.attachedNode == null) {
            this.attachedNode = new ObjectPropertyBase<Node>(){

                @Override
                public Object getBean() {
                    return FXVK.this;
                }

                @Override
                public String getName() {
                    return "attachedNode";
                }
            };
        }
        return this.attachedNode;
    }

    final void setAttachedNode(Node node) {
        this.attachedNodeProperty().setValue(node);
    }

    final Node getAttachedNode() {
        return this.attachedNode == null ? null : (Node)this.attachedNode.getValue();
    }

    public static void init(Node node) {
        if (vk != null) {
            return;
        }
        vk = new FXVK();
        FXVKSkin fXVKSkin = new FXVKSkin(vk);
        vk.setSkin(fXVKSkin);
        fXVKSkin.prerender(node);
    }

    public static void attach(Node node) {
        if (vk == null) {
            vk = new FXVK();
            vk.setSkin(new FXVKSkin(vk));
        }
        vk.setAttachedNode(node);
    }

    public static void detach() {
        if (vk != null) {
            vk.setAttachedNode(null);
        }
    }

    public static boolean useFXVK() {
        return USE_FXVK;
    }

    public static void toggleUseVK(TextInputControl textInputControl) {
        Integer n = (Integer)textInputControl.getProperties().get(VK_TYPE_PROP_KEY);
        if (n == null) {
            n = -1;
        }
        Integer n2 = n;
        Integer n3 = n = Integer.valueOf(n + 1);
        if (n < 4) {
            USE_FXVK = true;
            textInputControl.getProperties().put(VK_TYPE_PROP_KEY, n);
            FXVK.attach(textInputControl);
        } else {
            FXVK.detach();
            textInputControl.getProperties().put(VK_TYPE_PROP_KEY, null);
            USE_FXVK = false;
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new FXVKSkin(this);
    }

    static {
        USE_FXVK = IS_FXVK_SUPPORTED = Platform.isSupported(ConditionalFeature.VIRTUAL_KEYBOARD);
    }

    public static enum Type {
        TEXT,
        NUMERIC,
        EMAIL;

    }
}

