/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing;

import java.awt.event.MouseWheelEvent;
import javafx.event.EventType;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class SwingEvents {
    public static int mouseIDToEmbedMouseType(int n) {
        switch (n) {
            case 501: {
                return 0;
            }
            case 502: {
                return 1;
            }
            case 500: {
                return 2;
            }
            case 503: {
                return 5;
            }
            case 506: {
                return 6;
            }
            case 504: {
                return 3;
            }
            case 505: {
                return 4;
            }
        }
        return 0;
    }

    public static int mouseButtonToEmbedMouseButton(int n, int n2) {
        int n3 = 0;
        switch (n) {
            case 1: {
                n3 = 1;
                break;
            }
            case 2: {
                n3 = 4;
                break;
            }
            case 3: {
                n3 = 2;
                break;
            }
        }
        if ((n2 & 0x400) != 0) {
            n3 = 1;
        } else if ((n2 & 0x800) != 0) {
            n3 = 4;
        } else if ((n2 & 0x1000) != 0) {
            n3 = 2;
        }
        return n3;
    }

    public static int getWheelRotation(java.awt.event.MouseEvent mouseEvent) {
        if (mouseEvent instanceof MouseWheelEvent) {
            return ((MouseWheelEvent)mouseEvent).getWheelRotation();
        }
        return 0;
    }

    public static int keyIDToEmbedKeyType(int n) {
        switch (n) {
            case 401: {
                return 0;
            }
            case 402: {
                return 1;
            }
            case 400: {
                return 2;
            }
        }
        return 0;
    }

    public static int keyModifiersToEmbedKeyModifiers(int n) {
        int n2 = 0;
        if ((n & 0x40) != 0) {
            n2 |= 1;
        }
        if ((n & 0x80) != 0) {
            n2 |= 2;
        }
        if ((n & 0x200) != 0) {
            n2 |= 4;
        }
        if ((n & 0x100) != 0) {
            n2 |= 8;
        }
        return n2;
    }

    public static char keyCharToEmbedKeyChar(char c) {
        return c == '\n' ? (char)'\r' : (char)c;
    }

    public static int fxMouseEventTypeToMouseID(MouseEvent mouseEvent) {
        EventType<? extends MouseEvent> eventType = mouseEvent.getEventType();
        if (eventType == MouseEvent.MOUSE_MOVED) {
            return 503;
        }
        if (eventType == MouseEvent.MOUSE_PRESSED) {
            return 501;
        }
        if (eventType == MouseEvent.MOUSE_RELEASED) {
            return 502;
        }
        if (eventType == MouseEvent.MOUSE_CLICKED) {
            return 500;
        }
        if (eventType == MouseEvent.MOUSE_ENTERED) {
            return 504;
        }
        if (eventType == MouseEvent.MOUSE_EXITED) {
            return 505;
        }
        if (eventType == MouseEvent.MOUSE_DRAGGED) {
            return 506;
        }
        if (eventType == MouseEvent.DRAG_DETECTED) {
            return -1;
        }
        throw new RuntimeException("Unknown MouseEvent type: " + eventType);
    }

    public static int fxMouseModsToMouseMods(MouseEvent mouseEvent) {
        int n = 0;
        if (mouseEvent.isAltDown()) {
            n |= 0x200;
        }
        if (mouseEvent.isControlDown()) {
            n |= 0x80;
        }
        if (mouseEvent.isMetaDown()) {
            n |= 0x100;
        }
        if (mouseEvent.isShiftDown()) {
            n |= 0x40;
        }
        if (mouseEvent.isPrimaryButtonDown()) {
            n |= 0x400;
        }
        if (mouseEvent.isSecondaryButtonDown()) {
            n |= 0x1000;
        }
        if (mouseEvent.isMiddleButtonDown()) {
            n |= 0x800;
        }
        return n;
    }

    public static int fxMouseButtonToMouseButton(MouseEvent mouseEvent) {
        switch (mouseEvent.getButton()) {
            case PRIMARY: {
                return 1;
            }
            case SECONDARY: {
                return 3;
            }
            case MIDDLE: {
                return 2;
            }
        }
        return 0;
    }

    public static int fxKeyEventTypeToKeyID(KeyEvent keyEvent) {
        EventType<KeyEvent> eventType = keyEvent.getEventType();
        if (eventType == KeyEvent.KEY_PRESSED) {
            return 401;
        }
        if (eventType == KeyEvent.KEY_RELEASED) {
            return 402;
        }
        if (eventType == KeyEvent.KEY_TYPED) {
            return 400;
        }
        throw new RuntimeException("Unknown KeyEvent type: " + eventType);
    }

    public static int fxKeyModsToKeyMods(KeyEvent keyEvent) {
        int n = 0;
        if (keyEvent.isAltDown()) {
            n |= 0x200;
        }
        if (keyEvent.isControlDown()) {
            n |= 0x80;
        }
        if (keyEvent.isMetaDown()) {
            n |= 0x100;
        }
        if (keyEvent.isShiftDown()) {
            n |= 0x40;
        }
        return n;
    }

    public static int fxScrollModsToMouseWheelMods(ScrollEvent scrollEvent) {
        int n = 0;
        if (scrollEvent.isAltDown()) {
            n |= 0x200;
        }
        if (scrollEvent.isControlDown()) {
            n |= 0x80;
        }
        if (scrollEvent.isMetaDown()) {
            n |= 0x100;
        }
        if (scrollEvent.isShiftDown()) {
            n |= 0x40;
        }
        return n;
    }
}

