/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing;

import com.sun.javafx.cursor.CursorFrame;
import com.sun.javafx.cursor.ImageCursorFrame;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Cursor;

public class SwingCursors {
    private static java.awt.Cursor createCustomCursor(ImageCursorFrame imageCursorFrame) {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        double d = imageCursorFrame.getWidth();
        double d2 = imageCursorFrame.getHeight();
        Dimension dimension = toolkit.getBestCursorSize((int)d, (int)d2);
        double d3 = imageCursorFrame.getHotspotX() * dimension.getWidth() / d;
        double d4 = imageCursorFrame.getHotspotY() * dimension.getHeight() / d2;
        Point point = new Point((int)d3, (int)d4);
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(com.sun.javafx.tk.Toolkit.getImageAccessor().fromPlatformImage(imageCursorFrame.getPlatformImage()), null);
        return toolkit.createCustomCursor(bufferedImage, point, null);
    }

    public static java.awt.Cursor embedCursorToCursor(CursorFrame cursorFrame) {
        switch (cursorFrame.getCursorType()) {
            case DEFAULT: {
                return java.awt.Cursor.getPredefinedCursor(0);
            }
            case CROSSHAIR: {
                return java.awt.Cursor.getPredefinedCursor(1);
            }
            case TEXT: {
                return java.awt.Cursor.getPredefinedCursor(2);
            }
            case WAIT: {
                return java.awt.Cursor.getPredefinedCursor(3);
            }
            case SW_RESIZE: {
                return java.awt.Cursor.getPredefinedCursor(4);
            }
            case SE_RESIZE: {
                return java.awt.Cursor.getPredefinedCursor(5);
            }
            case NW_RESIZE: {
                return java.awt.Cursor.getPredefinedCursor(6);
            }
            case NE_RESIZE: {
                return java.awt.Cursor.getPredefinedCursor(7);
            }
            case N_RESIZE: 
            case V_RESIZE: {
                return java.awt.Cursor.getPredefinedCursor(8);
            }
            case S_RESIZE: {
                return java.awt.Cursor.getPredefinedCursor(9);
            }
            case W_RESIZE: 
            case H_RESIZE: {
                return java.awt.Cursor.getPredefinedCursor(10);
            }
            case E_RESIZE: {
                return java.awt.Cursor.getPredefinedCursor(11);
            }
            case OPEN_HAND: 
            case CLOSED_HAND: 
            case HAND: {
                return java.awt.Cursor.getPredefinedCursor(12);
            }
            case MOVE: {
                return java.awt.Cursor.getPredefinedCursor(13);
            }
            case DISAPPEAR: {
                return java.awt.Cursor.getPredefinedCursor(0);
            }
            case NONE: {
                return null;
            }
            case IMAGE: {
                return SwingCursors.createCustomCursor((ImageCursorFrame)cursorFrame);
            }
        }
        return java.awt.Cursor.getPredefinedCursor(0);
    }

    public static Cursor embedCursorToCursor(java.awt.Cursor cursor) {
        if (cursor == null) {
            return Cursor.DEFAULT;
        }
        switch (cursor.getType()) {
            case 0: {
                return Cursor.DEFAULT;
            }
            case 1: {
                return Cursor.CROSSHAIR;
            }
            case 11: {
                return Cursor.E_RESIZE;
            }
            case 12: {
                return Cursor.HAND;
            }
            case 13: {
                return Cursor.MOVE;
            }
            case 8: {
                return Cursor.N_RESIZE;
            }
            case 7: {
                return Cursor.NE_RESIZE;
            }
            case 6: {
                return Cursor.NW_RESIZE;
            }
            case 9: {
                return Cursor.S_RESIZE;
            }
            case 5: {
                return Cursor.SE_RESIZE;
            }
            case 4: {
                return Cursor.SW_RESIZE;
            }
            case 2: {
                return Cursor.TEXT;
            }
            case 10: {
                return Cursor.W_RESIZE;
            }
            case 3: {
                return Cursor.WAIT;
            }
        }
        return Cursor.DEFAULT;
    }
}

