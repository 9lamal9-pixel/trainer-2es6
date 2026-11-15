/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.eclipse.swt.graphics.Cursor
 *  org.eclipse.swt.graphics.Device
 *  org.eclipse.swt.graphics.ImageData
 *  org.eclipse.swt.widgets.Display
 */
package javafx.embed.swt;

import com.sun.javafx.cursor.CursorFrame;
import com.sun.javafx.cursor.ImageCursorFrame;
import com.sun.javafx.tk.Toolkit;
import javafx.embed.swt.SWTFXUtils;
import javafx.scene.image.Image;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;

class SWTCursors {
    SWTCursors() {
    }

    private static Cursor createCustomCursor(Display display, ImageCursorFrame imageCursorFrame) {
        Image image = Toolkit.getImageAccessor().fromPlatformImage(imageCursorFrame.getPlatformImage());
        ImageData imageData = SWTFXUtils.fromFXImage(image, null);
        return new Cursor((Device)display, imageData, (int)imageCursorFrame.getHotspotX(), (int)imageCursorFrame.getHotspotY());
    }

    static Cursor embedCursorToCursor(CursorFrame cursorFrame) {
        Display display = Display.getCurrent();
        if (display == null) {
            return null;
        }
        int n = 0;
        switch (cursorFrame.getCursorType()) {
            case DEFAULT: {
                n = 0;
                break;
            }
            case CROSSHAIR: {
                n = 2;
                break;
            }
            case TEXT: {
                n = 19;
                break;
            }
            case WAIT: {
                n = 1;
                break;
            }
            case SW_RESIZE: {
                n = 16;
                break;
            }
            case SE_RESIZE: {
                n = 15;
                break;
            }
            case NW_RESIZE: {
                n = 17;
                break;
            }
            case NE_RESIZE: {
                n = 14;
                break;
            }
            case N_RESIZE: {
                n = 10;
                break;
            }
            case S_RESIZE: {
                n = 11;
                break;
            }
            case W_RESIZE: {
                n = 13;
                break;
            }
            case E_RESIZE: {
                n = 12;
                break;
            }
            case OPEN_HAND: 
            case CLOSED_HAND: 
            case HAND: {
                n = 21;
                break;
            }
            case MOVE: {
                n = 5;
                break;
            }
            case DISAPPEAR: {
                break;
            }
            case H_RESIZE: {
                n = 9;
                break;
            }
            case V_RESIZE: {
                n = 7;
                break;
            }
            case NONE: {
                return null;
            }
            case IMAGE: {
                return SWTCursors.createCustomCursor(display, (ImageCursorFrame)cursorFrame);
            }
        }
        return display.getSystemCursor(n);
    }
}

