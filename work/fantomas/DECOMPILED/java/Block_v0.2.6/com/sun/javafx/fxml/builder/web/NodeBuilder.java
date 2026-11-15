/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.fxml.builder.web;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import javafx.event.EventDispatcher;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.DepthTest;
import javafx.scene.Node;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Effect;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.InputMethodEvent;
import javafx.scene.input.InputMethodRequests;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.RotateEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.SwipeEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.input.ZoomEvent;
import javafx.scene.transform.Transform;

@Deprecated
public abstract class NodeBuilder<B extends NodeBuilder<B>> {
    BitSet __set = new BitSet();
    private BlendMode blendMode;
    private boolean cache;
    private CacheHint cacheHint;
    private Node clip;
    private Cursor cursor;
    private DepthTest depthTest;
    private boolean disable;
    private Effect effect;
    private EventDispatcher eventDispatcher;
    private boolean focusTraversable;
    private String id;
    private InputMethodRequests inputMethodRequests;
    private double layoutX;
    private double layoutY;
    private boolean managed;
    private boolean mouseTransparent;
    private EventHandler<? super ContextMenuEvent> onContextMenuRequested;
    private EventHandler<? super MouseEvent> onDragDetected;
    private EventHandler<? super DragEvent> onDragDone;
    private EventHandler<? super DragEvent> onDragDropped;
    private EventHandler<? super DragEvent> onDragEntered;
    private EventHandler<? super DragEvent> onDragExited;
    private EventHandler<? super DragEvent> onDragOver;
    private EventHandler<? super InputMethodEvent> onInputMethodTextChanged;
    private EventHandler<? super KeyEvent> onKeyPressed;
    private EventHandler<? super KeyEvent> onKeyReleased;
    private EventHandler<? super KeyEvent> onKeyTyped;
    private EventHandler<? super MouseEvent> onMouseClicked;
    private EventHandler<? super MouseDragEvent> onMouseDragEntered;
    private EventHandler<? super MouseDragEvent> onMouseDragExited;
    private EventHandler<? super MouseEvent> onMouseDragged;
    private EventHandler<? super MouseDragEvent> onMouseDragOver;
    private EventHandler<? super MouseDragEvent> onMouseDragReleased;
    private EventHandler<? super MouseEvent> onMouseEntered;
    private EventHandler<? super MouseEvent> onMouseExited;
    private EventHandler<? super MouseEvent> onMouseMoved;
    private EventHandler<? super MouseEvent> onMousePressed;
    private EventHandler<? super MouseEvent> onMouseReleased;
    private EventHandler<? super RotateEvent> onRotate;
    private EventHandler<? super RotateEvent> onRotationFinished;
    private EventHandler<? super RotateEvent> onRotationStarted;
    private EventHandler<? super ScrollEvent> onScroll;
    private EventHandler<? super ScrollEvent> onScrollFinished;
    private EventHandler<? super ScrollEvent> onScrollStarted;
    private EventHandler<? super SwipeEvent> onSwipeDown;
    private EventHandler<? super SwipeEvent> onSwipeLeft;
    private EventHandler<? super SwipeEvent> onSwipeRight;
    private EventHandler<? super SwipeEvent> onSwipeUp;
    private EventHandler<? super TouchEvent> onTouchMoved;
    private EventHandler<? super TouchEvent> onTouchPressed;
    private EventHandler<? super TouchEvent> onTouchReleased;
    private EventHandler<? super TouchEvent> onTouchStationary;
    private EventHandler<? super ZoomEvent> onZoom;
    private EventHandler<? super ZoomEvent> onZoomFinished;
    private EventHandler<? super ZoomEvent> onZoomStarted;
    private double opacity;
    private boolean pickOnBounds;
    private double rotate;
    private Point3D rotationAxis;
    private double scaleX;
    private double scaleY;
    private double scaleZ;
    private String style;
    private Collection<? extends String> styleClass;
    private Collection<? extends Transform> transforms;
    private double translateX;
    private double translateY;
    private double translateZ;
    private Object userData;
    private boolean visible;

    protected NodeBuilder() {
    }

    private void __set(int n) {
        this.__set.set(n);
    }

    public void applyTo(Node node) {
        BitSet bitSet = this.__set;
        int n = -1;
        while ((n = bitSet.nextSetBit(n + 1)) >= 0) {
            switch (n) {
                case 0: {
                    node.setBlendMode(this.blendMode);
                    break;
                }
                case 1: {
                    node.setCache(this.cache);
                    break;
                }
                case 2: {
                    node.setCacheHint(this.cacheHint);
                    break;
                }
                case 3: {
                    node.setClip(this.clip);
                    break;
                }
                case 4: {
                    node.setCursor(this.cursor);
                    break;
                }
                case 5: {
                    node.setDepthTest(this.depthTest);
                    break;
                }
                case 6: {
                    node.setDisable(this.disable);
                    break;
                }
                case 7: {
                    node.setEffect(this.effect);
                    break;
                }
                case 8: {
                    node.setEventDispatcher(this.eventDispatcher);
                    break;
                }
                case 9: {
                    node.setFocusTraversable(this.focusTraversable);
                    break;
                }
                case 10: {
                    node.setId(this.id);
                    break;
                }
                case 11: {
                    node.setInputMethodRequests(this.inputMethodRequests);
                    break;
                }
                case 12: {
                    node.setLayoutX(this.layoutX);
                    break;
                }
                case 13: {
                    node.setLayoutY(this.layoutY);
                    break;
                }
                case 14: {
                    node.setManaged(this.managed);
                    break;
                }
                case 15: {
                    node.setMouseTransparent(this.mouseTransparent);
                    break;
                }
                case 16: {
                    node.setOnContextMenuRequested(this.onContextMenuRequested);
                    break;
                }
                case 17: {
                    node.setOnDragDetected(this.onDragDetected);
                    break;
                }
                case 18: {
                    node.setOnDragDone(this.onDragDone);
                    break;
                }
                case 19: {
                    node.setOnDragDropped(this.onDragDropped);
                    break;
                }
                case 20: {
                    node.setOnDragEntered(this.onDragEntered);
                    break;
                }
                case 21: {
                    node.setOnDragExited(this.onDragExited);
                    break;
                }
                case 22: {
                    node.setOnDragOver(this.onDragOver);
                    break;
                }
                case 23: {
                    node.setOnInputMethodTextChanged(this.onInputMethodTextChanged);
                    break;
                }
                case 24: {
                    node.setOnKeyPressed(this.onKeyPressed);
                    break;
                }
                case 25: {
                    node.setOnKeyReleased(this.onKeyReleased);
                    break;
                }
                case 26: {
                    node.setOnKeyTyped(this.onKeyTyped);
                    break;
                }
                case 27: {
                    node.setOnMouseClicked(this.onMouseClicked);
                    break;
                }
                case 28: {
                    node.setOnMouseDragEntered(this.onMouseDragEntered);
                    break;
                }
                case 29: {
                    node.setOnMouseDragExited(this.onMouseDragExited);
                    break;
                }
                case 30: {
                    node.setOnMouseDragged(this.onMouseDragged);
                    break;
                }
                case 31: {
                    node.setOnMouseDragOver(this.onMouseDragOver);
                    break;
                }
                case 32: {
                    node.setOnMouseDragReleased(this.onMouseDragReleased);
                    break;
                }
                case 33: {
                    node.setOnMouseEntered(this.onMouseEntered);
                    break;
                }
                case 34: {
                    node.setOnMouseExited(this.onMouseExited);
                    break;
                }
                case 35: {
                    node.setOnMouseMoved(this.onMouseMoved);
                    break;
                }
                case 36: {
                    node.setOnMousePressed(this.onMousePressed);
                    break;
                }
                case 37: {
                    node.setOnMouseReleased(this.onMouseReleased);
                    break;
                }
                case 38: {
                    node.setOnRotate(this.onRotate);
                    break;
                }
                case 39: {
                    node.setOnRotationFinished(this.onRotationFinished);
                    break;
                }
                case 40: {
                    node.setOnRotationStarted(this.onRotationStarted);
                    break;
                }
                case 41: {
                    node.setOnScroll(this.onScroll);
                    break;
                }
                case 42: {
                    node.setOnScrollFinished(this.onScrollFinished);
                    break;
                }
                case 43: {
                    node.setOnScrollStarted(this.onScrollStarted);
                    break;
                }
                case 44: {
                    node.setOnSwipeDown(this.onSwipeDown);
                    break;
                }
                case 45: {
                    node.setOnSwipeLeft(this.onSwipeLeft);
                    break;
                }
                case 46: {
                    node.setOnSwipeRight(this.onSwipeRight);
                    break;
                }
                case 47: {
                    node.setOnSwipeUp(this.onSwipeUp);
                    break;
                }
                case 48: {
                    node.setOnTouchMoved(this.onTouchMoved);
                    break;
                }
                case 49: {
                    node.setOnTouchPressed(this.onTouchPressed);
                    break;
                }
                case 50: {
                    node.setOnTouchReleased(this.onTouchReleased);
                    break;
                }
                case 51: {
                    node.setOnTouchStationary(this.onTouchStationary);
                    break;
                }
                case 52: {
                    node.setOnZoom(this.onZoom);
                    break;
                }
                case 53: {
                    node.setOnZoomFinished(this.onZoomFinished);
                    break;
                }
                case 54: {
                    node.setOnZoomStarted(this.onZoomStarted);
                    break;
                }
                case 55: {
                    node.setOpacity(this.opacity);
                    break;
                }
                case 56: {
                    node.setPickOnBounds(this.pickOnBounds);
                    break;
                }
                case 57: {
                    node.setRotate(this.rotate);
                    break;
                }
                case 58: {
                    node.setRotationAxis(this.rotationAxis);
                    break;
                }
                case 59: {
                    node.setScaleX(this.scaleX);
                    break;
                }
                case 60: {
                    node.setScaleY(this.scaleY);
                    break;
                }
                case 61: {
                    node.setScaleZ(this.scaleZ);
                    break;
                }
                case 62: {
                    node.setStyle(this.style);
                    break;
                }
                case 63: {
                    node.getStyleClass().addAll(this.styleClass);
                    break;
                }
                case 64: {
                    node.getTransforms().addAll(this.transforms);
                    break;
                }
                case 65: {
                    node.setTranslateX(this.translateX);
                    break;
                }
                case 66: {
                    node.setTranslateY(this.translateY);
                    break;
                }
                case 67: {
                    node.setTranslateZ(this.translateZ);
                    break;
                }
                case 68: {
                    node.setUserData(this.userData);
                    break;
                }
                case 69: {
                    node.setVisible(this.visible);
                }
            }
        }
    }

    public B blendMode(BlendMode blendMode) {
        this.blendMode = blendMode;
        this.__set(0);
        return (B)this;
    }

    public B cache(boolean bl) {
        this.cache = bl;
        this.__set(1);
        return (B)this;
    }

    public B cacheHint(CacheHint cacheHint) {
        this.cacheHint = cacheHint;
        this.__set(2);
        return (B)this;
    }

    public B clip(Node node) {
        this.clip = node;
        this.__set(3);
        return (B)this;
    }

    public B cursor(Cursor cursor) {
        this.cursor = cursor;
        this.__set(4);
        return (B)this;
    }

    public B depthTest(DepthTest depthTest) {
        this.depthTest = depthTest;
        this.__set(5);
        return (B)this;
    }

    public B disable(boolean bl) {
        this.disable = bl;
        this.__set(6);
        return (B)this;
    }

    public B effect(Effect effect) {
        this.effect = effect;
        this.__set(7);
        return (B)this;
    }

    public B eventDispatcher(EventDispatcher eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
        this.__set(8);
        return (B)this;
    }

    public B focusTraversable(boolean bl) {
        this.focusTraversable = bl;
        this.__set(9);
        return (B)this;
    }

    public B id(String string) {
        this.id = string;
        this.__set(10);
        return (B)this;
    }

    public B inputMethodRequests(InputMethodRequests inputMethodRequests) {
        this.inputMethodRequests = inputMethodRequests;
        this.__set(11);
        return (B)this;
    }

    public B layoutX(double d) {
        this.layoutX = d;
        this.__set(12);
        return (B)this;
    }

    public B layoutY(double d) {
        this.layoutY = d;
        this.__set(13);
        return (B)this;
    }

    public B managed(boolean bl) {
        this.managed = bl;
        this.__set(14);
        return (B)this;
    }

    public B mouseTransparent(boolean bl) {
        this.mouseTransparent = bl;
        this.__set(15);
        return (B)this;
    }

    public B onContextMenuRequested(EventHandler<? super ContextMenuEvent> eventHandler) {
        this.onContextMenuRequested = eventHandler;
        this.__set(16);
        return (B)this;
    }

    public B onDragDetected(EventHandler<? super MouseEvent> eventHandler) {
        this.onDragDetected = eventHandler;
        this.__set(17);
        return (B)this;
    }

    public B onDragDone(EventHandler<? super DragEvent> eventHandler) {
        this.onDragDone = eventHandler;
        this.__set(18);
        return (B)this;
    }

    public B onDragDropped(EventHandler<? super DragEvent> eventHandler) {
        this.onDragDropped = eventHandler;
        this.__set(19);
        return (B)this;
    }

    public B onDragEntered(EventHandler<? super DragEvent> eventHandler) {
        this.onDragEntered = eventHandler;
        this.__set(20);
        return (B)this;
    }

    public B onDragExited(EventHandler<? super DragEvent> eventHandler) {
        this.onDragExited = eventHandler;
        this.__set(21);
        return (B)this;
    }

    public B onDragOver(EventHandler<? super DragEvent> eventHandler) {
        this.onDragOver = eventHandler;
        this.__set(22);
        return (B)this;
    }

    public B onInputMethodTextChanged(EventHandler<? super InputMethodEvent> eventHandler) {
        this.onInputMethodTextChanged = eventHandler;
        this.__set(23);
        return (B)this;
    }

    public B onKeyPressed(EventHandler<? super KeyEvent> eventHandler) {
        this.onKeyPressed = eventHandler;
        this.__set(24);
        return (B)this;
    }

    public B onKeyReleased(EventHandler<? super KeyEvent> eventHandler) {
        this.onKeyReleased = eventHandler;
        this.__set(25);
        return (B)this;
    }

    public B onKeyTyped(EventHandler<? super KeyEvent> eventHandler) {
        this.onKeyTyped = eventHandler;
        this.__set(26);
        return (B)this;
    }

    public B onMouseClicked(EventHandler<? super MouseEvent> eventHandler) {
        this.onMouseClicked = eventHandler;
        this.__set(27);
        return (B)this;
    }

    public B onMouseDragEntered(EventHandler<? super MouseDragEvent> eventHandler) {
        this.onMouseDragEntered = eventHandler;
        this.__set(28);
        return (B)this;
    }

    public B onMouseDragExited(EventHandler<? super MouseDragEvent> eventHandler) {
        this.onMouseDragExited = eventHandler;
        this.__set(29);
        return (B)this;
    }

    public B onMouseDragged(EventHandler<? super MouseEvent> eventHandler) {
        this.onMouseDragged = eventHandler;
        this.__set(30);
        return (B)this;
    }

    public B onMouseDragOver(EventHandler<? super MouseDragEvent> eventHandler) {
        this.onMouseDragOver = eventHandler;
        this.__set(31);
        return (B)this;
    }

    public B onMouseDragReleased(EventHandler<? super MouseDragEvent> eventHandler) {
        this.onMouseDragReleased = eventHandler;
        this.__set(32);
        return (B)this;
    }

    public B onMouseEntered(EventHandler<? super MouseEvent> eventHandler) {
        this.onMouseEntered = eventHandler;
        this.__set(33);
        return (B)this;
    }

    public B onMouseExited(EventHandler<? super MouseEvent> eventHandler) {
        this.onMouseExited = eventHandler;
        this.__set(34);
        return (B)this;
    }

    public B onMouseMoved(EventHandler<? super MouseEvent> eventHandler) {
        this.onMouseMoved = eventHandler;
        this.__set(35);
        return (B)this;
    }

    public B onMousePressed(EventHandler<? super MouseEvent> eventHandler) {
        this.onMousePressed = eventHandler;
        this.__set(36);
        return (B)this;
    }

    public B onMouseReleased(EventHandler<? super MouseEvent> eventHandler) {
        this.onMouseReleased = eventHandler;
        this.__set(37);
        return (B)this;
    }

    public B onRotate(EventHandler<? super RotateEvent> eventHandler) {
        this.onRotate = eventHandler;
        this.__set(38);
        return (B)this;
    }

    public B onRotationFinished(EventHandler<? super RotateEvent> eventHandler) {
        this.onRotationFinished = eventHandler;
        this.__set(39);
        return (B)this;
    }

    public B onRotationStarted(EventHandler<? super RotateEvent> eventHandler) {
        this.onRotationStarted = eventHandler;
        this.__set(40);
        return (B)this;
    }

    public B onScroll(EventHandler<? super ScrollEvent> eventHandler) {
        this.onScroll = eventHandler;
        this.__set(41);
        return (B)this;
    }

    public B onScrollFinished(EventHandler<? super ScrollEvent> eventHandler) {
        this.onScrollFinished = eventHandler;
        this.__set(42);
        return (B)this;
    }

    public B onScrollStarted(EventHandler<? super ScrollEvent> eventHandler) {
        this.onScrollStarted = eventHandler;
        this.__set(43);
        return (B)this;
    }

    public B onSwipeDown(EventHandler<? super SwipeEvent> eventHandler) {
        this.onSwipeDown = eventHandler;
        this.__set(44);
        return (B)this;
    }

    public B onSwipeLeft(EventHandler<? super SwipeEvent> eventHandler) {
        this.onSwipeLeft = eventHandler;
        this.__set(45);
        return (B)this;
    }

    public B onSwipeRight(EventHandler<? super SwipeEvent> eventHandler) {
        this.onSwipeRight = eventHandler;
        this.__set(46);
        return (B)this;
    }

    public B onSwipeUp(EventHandler<? super SwipeEvent> eventHandler) {
        this.onSwipeUp = eventHandler;
        this.__set(47);
        return (B)this;
    }

    public B onTouchMoved(EventHandler<? super TouchEvent> eventHandler) {
        this.onTouchMoved = eventHandler;
        this.__set(48);
        return (B)this;
    }

    public B onTouchPressed(EventHandler<? super TouchEvent> eventHandler) {
        this.onTouchPressed = eventHandler;
        this.__set(49);
        return (B)this;
    }

    public B onTouchReleased(EventHandler<? super TouchEvent> eventHandler) {
        this.onTouchReleased = eventHandler;
        this.__set(50);
        return (B)this;
    }

    public B onTouchStationary(EventHandler<? super TouchEvent> eventHandler) {
        this.onTouchStationary = eventHandler;
        this.__set(51);
        return (B)this;
    }

    public B onZoom(EventHandler<? super ZoomEvent> eventHandler) {
        this.onZoom = eventHandler;
        this.__set(52);
        return (B)this;
    }

    public B onZoomFinished(EventHandler<? super ZoomEvent> eventHandler) {
        this.onZoomFinished = eventHandler;
        this.__set(53);
        return (B)this;
    }

    public B onZoomStarted(EventHandler<? super ZoomEvent> eventHandler) {
        this.onZoomStarted = eventHandler;
        this.__set(54);
        return (B)this;
    }

    public B opacity(double d) {
        this.opacity = d;
        this.__set(55);
        return (B)this;
    }

    public B pickOnBounds(boolean bl) {
        this.pickOnBounds = bl;
        this.__set(56);
        return (B)this;
    }

    public B rotate(double d) {
        this.rotate = d;
        this.__set(57);
        return (B)this;
    }

    public B rotationAxis(Point3D point3D) {
        this.rotationAxis = point3D;
        this.__set(58);
        return (B)this;
    }

    public B scaleX(double d) {
        this.scaleX = d;
        this.__set(59);
        return (B)this;
    }

    public B scaleY(double d) {
        this.scaleY = d;
        this.__set(60);
        return (B)this;
    }

    public B scaleZ(double d) {
        this.scaleZ = d;
        this.__set(61);
        return (B)this;
    }

    public B style(String string) {
        this.style = string;
        this.__set(62);
        return (B)this;
    }

    public B styleClass(Collection<? extends String> collection) {
        this.styleClass = collection;
        this.__set(63);
        return (B)this;
    }

    public B styleClass(String ... stringArray) {
        return this.styleClass(Arrays.asList(stringArray));
    }

    public B transforms(Collection<? extends Transform> collection) {
        this.transforms = collection;
        this.__set(64);
        return (B)this;
    }

    public B transforms(Transform ... transformArray) {
        return this.transforms(Arrays.asList(transformArray));
    }

    public B translateX(double d) {
        this.translateX = d;
        this.__set(65);
        return (B)this;
    }

    public B translateY(double d) {
        this.translateY = d;
        this.__set(66);
        return (B)this;
    }

    public B translateZ(double d) {
        this.translateZ = d;
        this.__set(67);
        return (B)this;
    }

    public B userData(Object object) {
        this.userData = object;
        this.__set(68);
        return (B)this;
    }

    public B visible(boolean bl) {
        this.visible = bl;
        this.__set(69);
        return (B)this;
    }
}

