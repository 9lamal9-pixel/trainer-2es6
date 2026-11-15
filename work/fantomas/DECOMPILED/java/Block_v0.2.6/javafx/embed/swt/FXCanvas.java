/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.eclipse.swt.SWT
 *  org.eclipse.swt.dnd.DragSource
 *  org.eclipse.swt.dnd.DragSourceEvent
 *  org.eclipse.swt.dnd.DragSourceListener
 *  org.eclipse.swt.dnd.DropTarget
 *  org.eclipse.swt.dnd.DropTargetEvent
 *  org.eclipse.swt.dnd.DropTargetListener
 *  org.eclipse.swt.dnd.FileTransfer
 *  org.eclipse.swt.dnd.HTMLTransfer
 *  org.eclipse.swt.dnd.ImageTransfer
 *  org.eclipse.swt.dnd.RTFTransfer
 *  org.eclipse.swt.dnd.TextTransfer
 *  org.eclipse.swt.dnd.Transfer
 *  org.eclipse.swt.dnd.TransferData
 *  org.eclipse.swt.dnd.URLTransfer
 *  org.eclipse.swt.events.ControlEvent
 *  org.eclipse.swt.events.ControlListener
 *  org.eclipse.swt.events.DisposeEvent
 *  org.eclipse.swt.events.DisposeListener
 *  org.eclipse.swt.events.FocusEvent
 *  org.eclipse.swt.events.FocusListener
 *  org.eclipse.swt.events.GestureEvent
 *  org.eclipse.swt.events.KeyEvent
 *  org.eclipse.swt.events.KeyListener
 *  org.eclipse.swt.events.MenuDetectEvent
 *  org.eclipse.swt.events.MouseEvent
 *  org.eclipse.swt.events.MouseListener
 *  org.eclipse.swt.events.MouseTrackListener
 *  org.eclipse.swt.events.PaintEvent
 *  org.eclipse.swt.graphics.Cursor
 *  org.eclipse.swt.graphics.Device
 *  org.eclipse.swt.graphics.Image
 *  org.eclipse.swt.graphics.ImageData
 *  org.eclipse.swt.graphics.PaletteData
 *  org.eclipse.swt.graphics.Point
 *  org.eclipse.swt.graphics.RGB
 *  org.eclipse.swt.graphics.Rectangle
 *  org.eclipse.swt.widgets.Canvas
 *  org.eclipse.swt.widgets.Composite
 *  org.eclipse.swt.widgets.Control
 *  org.eclipse.swt.widgets.Display
 *  org.eclipse.swt.widgets.Listener
 *  org.eclipse.swt.widgets.Shell
 */
package javafx.embed.swt;

import com.sun.glass.ui.Application;
import com.sun.glass.ui.Pixels;
import com.sun.javafx.cursor.CursorFrame;
import com.sun.javafx.cursor.CursorType;
import com.sun.javafx.embed.EmbeddedSceneDSInterface;
import com.sun.javafx.embed.EmbeddedSceneDTInterface;
import com.sun.javafx.embed.EmbeddedSceneInterface;
import com.sun.javafx.embed.EmbeddedStageInterface;
import com.sun.javafx.embed.HostInterface;
import com.sun.javafx.stage.EmbeddedWindow;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;
import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.embed.swt.CustomTransfer;
import javafx.embed.swt.SWTCursors;
import javafx.embed.swt.SWTEvents;
import javafx.scene.Scene;
import javafx.scene.input.TransferMode;
import javafx.stage.Window;
import javafx.util.FXPermission;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.HTMLTransfer;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.RTFTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.dnd.URLTransfer;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.GestureEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

public class FXCanvas
extends Canvas {
    private static final FXPermission FXCANVAS_PERMISSION = new FXPermission("accessFXCanvasInternals");
    private HostContainer hostContainer;
    private volatile EmbeddedWindow stage;
    private volatile Scene scene;
    private EmbeddedStageInterface stagePeer;
    private EmbeddedSceneInterface scenePeer;
    private int pWidth = 0;
    private int pHeight = 0;
    private volatile int pPreferredWidth = -1;
    private volatile int pPreferredHeight = -1;
    private IntBuffer pixelsBuf = null;
    Listener moveFilter = event -> {
        for (FXCanvas fXCanvas = this; fXCanvas != null; fXCanvas = fXCanvas.getParent()) {
            if (fXCanvas != event.widget) continue;
            this.sendMoveEventToFX();
            break;
        }
    };
    private DropTarget dropTarget;
    static Transfer[] StandardTransfers = new Transfer[]{TextTransfer.getInstance(), RTFTransfer.getInstance(), HTMLTransfer.getInstance(), URLTransfer.getInstance(), ImageTransfer.getInstance(), FileTransfer.getInstance()};
    static Transfer[] CustomTransfers = new Transfer[0];
    private static Field windowField;
    private static Method windowMethod;
    private static Method screenMethod;
    private static Method backingScaleFactorMethod;
    private static Method swtDPIUtilMethod;
    static ArrayList<DropTarget> targets;
    double lastScaleFactor = 1.0;
    int lastWidth;
    int lastHeight;
    IntBuffer lastPixelsBuf = null;
    double totalScrollX = 0.0;
    double totalScrollY = 0.0;
    private boolean gestureActive = false;
    private boolean panGestureInertiaActive = false;
    private GestureEvent lastGestureEvent;
    private Stack<Integer> nestedGestures = new Stack();
    private long inertiaTime = 0L;
    private double inertiaXScroll = 0.0;
    private double inertiaYScroll = 0.0;
    private double lastTotalZoom = 0.0;
    private double lastTotalAngle = 0.0;

    private double getScaleFactor() {
        if (SWT.getPlatform().equals("cocoa")) {
            if (windowField == null || screenMethod == null || backingScaleFactorMethod == null) {
                return 1.0;
            }
            try {
                Object object = windowField.get(this.getShell());
                Object object2 = screenMethod.invoke(object, new Object[0]);
                Object object3 = backingScaleFactorMethod.invoke(object2, new Object[0]);
                return (Double)object3;
            }
            catch (Exception exception) {}
        } else if (SWT.getPlatform().equals("win32")) {
            if (swtDPIUtilMethod == null) {
                return 1.0;
            }
            try {
                Integer n = (Integer)swtDPIUtilMethod.invoke(null, new Object[0]);
                return (double)n.intValue() / 100.0;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        return 1.0;
    }

    static Transfer[] getAllTransfers() {
        Transfer[] transferArray = new Transfer[StandardTransfers.length + CustomTransfers.length];
        System.arraycopy(StandardTransfers, 0, transferArray, 0, StandardTransfers.length);
        System.arraycopy(CustomTransfers, 0, transferArray, StandardTransfers.length, CustomTransfers.length);
        return transferArray;
    }

    static Transfer getCustomTransfer(String string) {
        for (int i = 0; i < CustomTransfers.length; ++i) {
            if (!((CustomTransfer)CustomTransfers[i]).getMime().equals(string)) continue;
            return CustomTransfers[i];
        }
        CustomTransfer customTransfer = new CustomTransfer(string, string);
        Transfer[] transferArray = new Transfer[CustomTransfers.length + 1];
        System.arraycopy(CustomTransfers, 0, transferArray, 0, CustomTransfers.length);
        transferArray[FXCanvas.CustomTransfers.length] = customTransfer;
        CustomTransfers = transferArray;
        return customTransfer;
    }

    public FXCanvas(@NamedArg(value="parent") Composite composite, @NamedArg(value="style") int n) {
        super(composite, n | 0x40000);
        this.setApplicationName(Display.getAppName());
        this.hostContainer = new HostContainer();
        this.registerEventListeners();
        Display display = composite.getDisplay();
        display.addFilter(10, this.moveFilter);
    }

    public static FXCanvas getFXCanvas(Scene scene) {
        HostInterface hostInterface;
        Window window = scene.getWindow();
        if (window != null && window instanceof EmbeddedWindow && (hostInterface = ((EmbeddedWindow)window).getHost()) instanceof HostContainer) {
            return ((HostContainer)hostInterface).fxCanvas;
        }
        return null;
    }

    private static void initFx() {
        Object object;
        long l = 0L;
        try {
            object = Display.class.getDeclaredField("eventProc");
            ((Field)object).setAccessible(true);
            if (((Field)object).getType() == Integer.TYPE) {
                l = ((Field)object).getInt(Display.getDefault());
            } else if (((Field)object).getType() == Long.TYPE) {
                l = ((Field)object).getLong(Display.getDefault());
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        object = String.valueOf(l);
        AccessController.doPrivileged(() -> FXCanvas.lambda$initFx$2((String)object));
        CountDownLatch countDownLatch = new CountDownLatch(1);
        AccessController.doPrivileged(() -> {
            Platform.startup(() -> countDownLatch.countDown());
            return null;
        }, null, FXCANVAS_PERMISSION);
        try {
            countDownLatch.await();
        }
        catch (InterruptedException interruptedException) {
            throw new RuntimeException(interruptedException);
        }
    }

    private void setApplicationName(String string) {
        Platform.runLater(() -> Application.GetApplication().setName(string));
    }

    public void reskin(int n) {
        super.reskin(n);
        if (n == 1) {
            this.sendMoveEventToFX();
        }
    }

    DropTarget getDropTarget() {
        return this.dropTarget;
    }

    void setDropTarget(DropTarget dropTarget) {
        if (this.dropTarget != null) {
            targets.remove(this.dropTarget);
            this.dropTarget.dispose();
        }
        this.dropTarget = dropTarget;
        if (this.dropTarget != null) {
            targets.add(this.dropTarget);
        }
    }

    static void updateDropTarget() {
        for (DropTarget dropTarget : targets) {
            dropTarget.setTransfer(FXCanvas.getAllTransfers());
        }
    }

    public Point computeSize(int n, int n2, boolean bl) {
        this.checkWidget();
        if (n == -1 && n2 == -1 && this.pPreferredWidth != -1 && this.pPreferredHeight != -1) {
            return new Point(this.pPreferredWidth, this.pPreferredHeight);
        }
        return super.computeSize(n, n2, bl);
    }

    public Scene getScene() {
        this.checkWidget();
        return this.scene;
    }

    public void setScene(Scene scene) {
        this.checkWidget();
        if (this.stage == null && scene != null) {
            this.stage = new EmbeddedWindow(this.hostContainer);
            this.stage.show();
        }
        this.scene = scene;
        if (this.stage != null) {
            this.stage.setScene(scene);
        }
        if (this.stage != null && scene == null) {
            this.stage.hide();
            this.stage = null;
        }
    }

    private void registerEventListeners() {
        this.addDisposeListener(new DisposeListener(){

            public void widgetDisposed(DisposeEvent disposeEvent) {
                Display display = FXCanvas.this.getDisplay();
                display.removeFilter(10, FXCanvas.this.moveFilter);
                FXCanvas.this.widgetDisposed(disposeEvent);
            }
        });
        this.addPaintListener(paintEvent -> this.paintControl(paintEvent));
        this.addMouseListener(new MouseListener(){

            public void mouseDoubleClick(MouseEvent mouseEvent) {
            }

            public void mouseDown(MouseEvent mouseEvent) {
                if (mouseEvent.button > 3) {
                    return;
                }
                FXCanvas.this.sendMouseEventToFX(mouseEvent, 0);
            }

            public void mouseUp(MouseEvent mouseEvent) {
                if (mouseEvent.button > 3) {
                    return;
                }
                FXCanvas.this.sendMouseEventToFX(mouseEvent, 1);
            }
        });
        this.addMouseMoveListener(mouseEvent -> {
            if ((mouseEvent.stateMask & SWT.BUTTON_MASK) != 0) {
                if ((mouseEvent.stateMask & 0x380000) != 0) {
                    this.sendMouseEventToFX(mouseEvent, 6);
                } else {
                    this.sendMouseEventToFX(mouseEvent, 5);
                }
            } else {
                this.sendMouseEventToFX(mouseEvent, 5);
            }
        });
        this.addListener(37, event -> {
            if (!(this.gestureActive || this.panGestureInertiaActive && this.lastGestureEvent != null && event.time == this.lastGestureEvent.time)) {
                this.sendScrollEventToFX(7, 0.0, SWTEvents.getWheelRotation(event), event.x, event.y, event.stateMask, false);
            }
        });
        this.addListener(38, event -> {
            if (!(this.gestureActive || this.panGestureInertiaActive && this.lastGestureEvent != null && event.time == this.lastGestureEvent.time)) {
                this.sendScrollEventToFX(8, SWTEvents.getWheelRotation(event), 0.0, event.x, event.y, event.stateMask, false);
            }
        });
        this.addMouseTrackListener(new MouseTrackListener(){

            public void mouseEnter(MouseEvent mouseEvent) {
                FXCanvas.this.sendMouseEventToFX(mouseEvent, 3);
            }

            public void mouseExit(MouseEvent mouseEvent) {
                FXCanvas.this.sendMouseEventToFX(mouseEvent, 4);
            }

            public void mouseHover(MouseEvent mouseEvent) {
            }
        });
        this.addControlListener(new ControlListener(){

            public void controlMoved(ControlEvent controlEvent) {
                FXCanvas.this.sendMoveEventToFX();
            }

            public void controlResized(ControlEvent controlEvent) {
                FXCanvas.this.sendResizeEventToFX();
            }
        });
        this.addFocusListener(new FocusListener(){

            public void focusGained(FocusEvent focusEvent) {
                FXCanvas.this.sendFocusEventToFX(focusEvent, true);
            }

            public void focusLost(FocusEvent focusEvent) {
                FXCanvas.this.sendFocusEventToFX(focusEvent, false);
            }
        });
        this.addKeyListener(new KeyListener(){

            public void keyPressed(KeyEvent keyEvent) {
                FXCanvas.this.sendKeyEventToFX(keyEvent, 1);
            }

            public void keyReleased(KeyEvent keyEvent) {
                FXCanvas.this.sendKeyEventToFX(keyEvent, 2);
            }
        });
        this.addGestureListener(gestureEvent -> this.sendGestureEventToFX(gestureEvent));
        this.addMenuDetectListener(menuDetectEvent -> {
            Runnable runnable = () -> {
                if (this.isDisposed()) {
                    return;
                }
                this.sendMenuEventToFX(menuDetectEvent);
            };
            if ("cocoa".equals(SWT.getPlatform())) {
                this.getDisplay().asyncExec(runnable);
            } else {
                runnable.run();
            }
        });
    }

    private void widgetDisposed(DisposeEvent disposeEvent) {
        this.setDropTarget(null);
        if (this.stage != null) {
            this.stage.hide();
        }
    }

    private void paintControl(PaintEvent paintEvent) {
        PaletteData paletteData;
        if (this.scenePeer == null || this.pixelsBuf == null) {
            return;
        }
        double d = this.getScaleFactor();
        if (this.lastScaleFactor != d) {
            this.resizePixelBuffer(d);
            this.lastScaleFactor = d;
            this.scenePeer.setPixelScaleFactors((float)d, (float)d);
        }
        IntBuffer intBuffer = this.pixelsBuf;
        int n = this.pWidth;
        int n2 = this.pHeight;
        if (this.scenePeer.getPixels(this.pixelsBuf, this.pWidth, this.pHeight)) {
            n = this.lastWidth = this.pWidth;
            n2 = this.lastHeight = this.pHeight;
            intBuffer = this.lastPixelsBuf = this.pixelsBuf;
        } else {
            if (this.lastPixelsBuf == null) {
                return;
            }
            n = this.lastWidth;
            n2 = this.lastHeight;
            intBuffer = this.lastPixelsBuf;
        }
        n = (int)Math.round((double)n * d);
        n2 = (int)Math.round((double)n2 * d);
        ImageData imageData = null;
        if ("win32".equals(SWT.getPlatform())) {
            paletteData = new PaletteData(65280, 0xFF0000, -16777216);
            int n3 = n * 4;
            byte[] byArray = new byte[n3 * n2];
            int[] nArray = intBuffer.array();
            int n4 = 0;
            int n5 = 0;
            for (int i = 0; i < n2; ++i) {
                for (int j = 0; j < n; ++j) {
                    int n6 = nArray[n5++];
                    byArray[n4++] = (byte)(n6 & 0xFF);
                    byArray[n4++] = (byte)(n6 >> 8 & 0xFF);
                    byArray[n4++] = (byte)(n6 >> 16 & 0xFF);
                    byArray[n4++] = 0;
                }
            }
            imageData = new ImageData(n, n2, 32, paletteData, 4, byArray);
        } else {
            if (n * n2 > intBuffer.array().length) {
                System.err.println("FXCanvas.paintControl: scale mismatch!");
                return;
            }
            paletteData = new PaletteData(0xFF0000, 65280, 255);
            imageData = new ImageData(n, n2, 32, paletteData);
            imageData.setPixels(0, 0, n * n2, intBuffer.array(), 0);
        }
        paletteData = new Image((Device)Display.getDefault(), imageData);
        paintEvent.gc.drawImage((Image)paletteData, 0, 0, n, n2, 0, 0, this.pWidth, this.pHeight);
        paletteData.dispose();
    }

    private void sendMoveEventToFX() {
        if (this.stagePeer == null) {
            return;
        }
        Rectangle rectangle = this.getClientArea();
        Point point = this.toDisplay(rectangle.x, rectangle.y);
        this.stagePeer.setLocation(point.x, point.y);
    }

    private void sendMouseEventToFX(MouseEvent mouseEvent, int n) {
        if (this.scenePeer == null) {
            return;
        }
        Point point = this.toDisplay(mouseEvent.x, mouseEvent.y);
        boolean bl = (mouseEvent.stateMask & 0x80000) != 0;
        boolean bl2 = (mouseEvent.stateMask & 0x100000) != 0;
        boolean bl3 = (mouseEvent.stateMask & 0x200000) != 0;
        boolean bl4 = (mouseEvent.stateMask & 0x20000) != 0;
        boolean bl5 = (mouseEvent.stateMask & 0x40000) != 0;
        boolean bl6 = (mouseEvent.stateMask & 0x10000) != 0;
        boolean bl7 = (mouseEvent.stateMask & 0x400000) != 0;
        int n2 = mouseEvent.button;
        switch (n) {
            case 0: {
                bl |= mouseEvent.button == 1;
                bl2 |= mouseEvent.button == 2;
                bl3 |= mouseEvent.button == 3;
                break;
            }
            case 1: {
                bl &= mouseEvent.button != 1;
                bl2 &= mouseEvent.button != 2;
                bl3 &= mouseEvent.button != 3;
                break;
            }
            case 2: {
                return;
            }
            case 3: 
            case 4: 
            case 5: 
            case 6: {
                if (n2 != 0) break;
                if ((mouseEvent.stateMask & 0x80000) != 0) {
                    n2 = 1;
                    break;
                }
                if ((mouseEvent.stateMask & 0x100000) != 0) {
                    n2 = 2;
                    break;
                }
                if ((mouseEvent.stateMask & 0x200000) == 0) break;
                n2 = 3;
                break;
            }
        }
        this.scenePeer.mouseEvent(n, SWTEvents.mouseButtonToEmbedMouseButton(n2, mouseEvent.stateMask), bl, bl2, bl3, mouseEvent.x, mouseEvent.y, point.x, point.y, bl4, bl5, bl6, bl7, false);
    }

    private void sendScrollEventToFX(int n, double d, double d2, int n2, int n3, int n4, boolean bl) {
        if (this.scenePeer == null) {
            return;
        }
        double d3 = 5.0;
        if (n == 8 || n == 7) {
            d3 = 40.0;
            this.totalScrollX = d;
            this.totalScrollY = d2;
        } else {
            if ("cocoa".equals(SWT.getPlatform()) && SWT.getVersion() < 4600) {
                d3 *= -1.0;
            }
            if (n == 0) {
                this.totalScrollX = 0.0;
                this.totalScrollY = 0.0;
            } else if (bl) {
                this.totalScrollX = d;
                this.totalScrollY = d2;
            } else {
                this.totalScrollX += d;
                this.totalScrollY += d2;
            }
        }
        Point point = this.toDisplay(n2, n3);
        this.scenePeer.scrollEvent(n, d, d2, this.totalScrollX, this.totalScrollY, d3, d3, n2, n3, point.x, point.y, (n4 & 0x20000) != 0, (n4 & 0x40000) != 0, (n4 & 0x10000) != 0, (n4 & 0x400000) != 0, bl);
    }

    private void sendKeyEventToFX(KeyEvent keyEvent, int n) {
        if (this.scenePeer == null) {
            return;
        }
        int n2 = keyEvent.stateMask;
        if (n == 1) {
            if (keyEvent.keyCode == 131072) {
                n2 |= 0x20000;
            }
            if (keyEvent.keyCode == 262144) {
                n2 |= 0x40000;
            }
            if (keyEvent.keyCode == 65536) {
                n2 |= 0x10000;
            }
            if (keyEvent.keyCode == 0x400000) {
                n2 |= 0x400000;
            }
        } else {
            if (keyEvent.keyCode == 131072) {
                n2 &= 0xFFFDFFFF;
            }
            if (keyEvent.keyCode == 262144) {
                n2 &= 0xFFFBFFFF;
            }
            if (keyEvent.keyCode == 65536) {
                n2 &= 0xFFFEFFFF;
            }
            if (keyEvent.keyCode == 0x400000) {
                n2 &= 0xFFBFFFFF;
            }
        }
        int n3 = SWTEvents.keyCodeToEmbedKeyCode(keyEvent.keyCode);
        this.scenePeer.keyEvent(SWTEvents.keyIDToEmbedKeyType(n), n3, new char[0], SWTEvents.keyModifiersToEmbedKeyModifiers(n2));
        if (keyEvent.character != '\u0000' && n == 1) {
            char[] cArray = new char[]{keyEvent.character};
            this.scenePeer.keyEvent(2, keyEvent.keyCode, cArray, SWTEvents.keyModifiersToEmbedKeyModifiers(n2));
        }
    }

    private void sendGestureEventToFX(GestureEvent gestureEvent) {
        if (this.scenePeer == null) {
            return;
        }
        switch (gestureEvent.detail) {
            case 2: {
                this.gestureActive = true;
                this.panGestureInertiaActive = false;
                break;
            }
            case 32: {
                if (this.gestureActive && !this.nestedGestures.contains(32)) {
                    this.sendZoomEventToFX(0, gestureEvent);
                    this.nestedGestures.push(32);
                }
                this.sendZoomEventToFX(1, gestureEvent);
                break;
            }
            case 64: {
                if (this.gestureActive && !this.nestedGestures.contains(64)) {
                    this.sendScrollEventToFX(0, gestureEvent.xDirection, gestureEvent.yDirection, gestureEvent.x, gestureEvent.y, gestureEvent.stateMask, false);
                    this.nestedGestures.push(64);
                }
                if (this.panGestureInertiaActive && gestureEvent.time > this.lastGestureEvent.time + 250) {
                    this.panGestureInertiaActive = false;
                }
                if (!this.gestureActive && !this.panGestureInertiaActive) break;
                double d = gestureEvent.xDirection;
                double d2 = gestureEvent.yDirection;
                if (this.panGestureInertiaActive && d == 0.0 && d2 == 0.0) {
                    double d3 = Math.max(0.0, Math.min(1.0, (double)((long)gestureEvent.time - this.inertiaTime) / 1500.0));
                    d = (1.0 - d3) * this.inertiaXScroll;
                    d2 = (1.0 - d3) * this.inertiaYScroll;
                }
                this.sendScrollEventToFX(1, d, d2, gestureEvent.x, gestureEvent.y, gestureEvent.stateMask, this.panGestureInertiaActive);
                break;
            }
            case 8: {
                if (this.gestureActive && !this.nestedGestures.contains(8)) {
                    this.sendRotateEventToFX(0, gestureEvent);
                    this.nestedGestures.push(8);
                }
                this.sendRotateEventToFX(1, gestureEvent);
                break;
            }
            case 16: {
                this.sendSwipeEventToFX(gestureEvent);
                break;
            }
            case 4: {
                while (!this.nestedGestures.isEmpty()) {
                    switch (this.nestedGestures.pop()) {
                        case 32: {
                            this.sendZoomEventToFX(2, gestureEvent);
                            break;
                        }
                        case 64: {
                            this.sendScrollEventToFX(2, gestureEvent.xDirection, gestureEvent.yDirection, gestureEvent.x, gestureEvent.y, gestureEvent.stateMask, false);
                            this.inertiaXScroll = this.lastGestureEvent.xDirection;
                            this.inertiaYScroll = this.lastGestureEvent.yDirection;
                            this.inertiaTime = gestureEvent.time;
                            this.panGestureInertiaActive = true;
                            break;
                        }
                        case 8: {
                            this.sendRotateEventToFX(2, gestureEvent);
                        }
                    }
                }
                this.gestureActive = false;
                break;
            }
        }
        this.lastGestureEvent = gestureEvent;
    }

    private void sendZoomEventToFX(int n, GestureEvent gestureEvent) {
        Point point = this.toDisplay(gestureEvent.x, gestureEvent.y);
        double d = gestureEvent.magnification;
        if (n == 0) {
            this.lastTotalZoom = 1.0;
            d = 1.0;
        } else if (n == 2) {
            d = this.lastTotalZoom;
        }
        double d2 = n == 2 ? 1.0 : d / this.lastTotalZoom;
        this.lastTotalZoom = d;
        this.scenePeer.zoomEvent(n, d2, d, gestureEvent.x, gestureEvent.y, point.x, point.y, (gestureEvent.stateMask & 0x20000) != 0, (gestureEvent.stateMask & 0x40000) != 0, (gestureEvent.stateMask & 0x10000) != 0, (gestureEvent.stateMask & 0x400000) != 0, !this.gestureActive);
    }

    private void sendRotateEventToFX(int n, GestureEvent gestureEvent) {
        Point point = this.toDisplay(gestureEvent.x, gestureEvent.y);
        double d = -gestureEvent.rotation;
        if (n == 0) {
            this.lastTotalAngle = 0.0;
            d = 0.0;
        } else if (n == 2) {
            d = this.lastTotalAngle;
        }
        double d2 = n == 2 ? 0.0 : d - this.lastTotalAngle;
        this.lastTotalAngle = d;
        this.scenePeer.rotateEvent(n, d2, d, gestureEvent.x, gestureEvent.y, point.x, point.y, (gestureEvent.stateMask & 0x20000) != 0, (gestureEvent.stateMask & 0x40000) != 0, (gestureEvent.stateMask & 0x10000) != 0, (gestureEvent.stateMask & 0x400000) != 0, !this.gestureActive);
    }

    private void sendSwipeEventToFX(GestureEvent gestureEvent) {
        Point point = this.toDisplay(gestureEvent.x, gestureEvent.y);
        int n = -1;
        if (gestureEvent.yDirection > 0) {
            n = 0;
        } else if (gestureEvent.yDirection < 0) {
            n = 1;
        } else if (gestureEvent.xDirection > 0) {
            n = 3;
        } else if (gestureEvent.xDirection < 0) {
            n = 2;
        }
        this.scenePeer.swipeEvent(n, gestureEvent.x, gestureEvent.y, point.x, point.y, (gestureEvent.stateMask & 0x20000) != 0, (gestureEvent.stateMask & 0x40000) != 0, (gestureEvent.stateMask & 0x10000) != 0, (gestureEvent.stateMask & 0x400000) != 0);
    }

    private void sendMenuEventToFX(MenuDetectEvent menuDetectEvent) {
        if (this.scenePeer == null) {
            return;
        }
        Point point = this.toControl(menuDetectEvent.x, menuDetectEvent.y);
        this.scenePeer.menuEvent(point.x, point.y, menuDetectEvent.x, menuDetectEvent.y, false);
    }

    private void sendResizeEventToFX() {
        this.redraw();
        this.update();
        this.pWidth = this.getClientArea().width;
        this.pHeight = this.getClientArea().height;
        this.resizePixelBuffer(this.lastScaleFactor);
        if (this.scenePeer == null) {
            return;
        }
        this.stagePeer.setSize(this.pWidth, this.pHeight);
        this.scenePeer.setSize(this.pWidth, this.pHeight);
    }

    private void resizePixelBuffer(double d) {
        this.lastPixelsBuf = null;
        if (this.pWidth <= 0 || this.pHeight <= 0) {
            this.pixelsBuf = null;
        } else {
            this.pixelsBuf = IntBuffer.allocate((int)Math.round((double)this.pWidth * d) * (int)Math.round((double)this.pHeight * d));
            RGB rGB = this.getBackground().getRGB();
            Arrays.fill(this.pixelsBuf.array(), rGB.red << 16 | rGB.green << 8 | rGB.blue);
        }
    }

    private void sendFocusEventToFX(FocusEvent focusEvent, boolean bl) {
        if (this.stage == null || this.stagePeer == null) {
            return;
        }
        int n = bl ? 0 : 3;
        this.stagePeer.setFocused(bl, n);
    }

    private static /* synthetic */ Void lambda$initFx$2(String string) {
        System.setProperty("com.sun.javafx.application.type", "FXCanvas");
        System.setProperty("javafx.embed.isEventThread", "true");
        if (swtDPIUtilMethod == null) {
            System.setProperty("glass.win.uiScale", "100%");
            System.setProperty("glass.win.renderScale", "100%");
        } else {
            Integer n = 100;
            try {
                n = (Integer)swtDPIUtilMethod.invoke(null, new Object[0]);
            }
            catch (Exception exception) {
                // empty catch block
            }
            System.setProperty("glass.win.uiScale", n + "%");
            System.setProperty("glass.win.renderScale", n + "%");
        }
        System.setProperty("javafx.embed.eventProc", string);
        return null;
    }

    static {
        if (SWT.getPlatform().equals("cocoa")) {
            try {
                windowField = Shell.class.getDeclaredField("window");
                windowField.setAccessible(true);
                Class<?> clazz = Class.forName("org.eclipse.swt.internal.cocoa.NSView");
                windowMethod = clazz.getDeclaredMethod("window", new Class[0]);
                windowMethod.setAccessible(true);
                Class<?> clazz2 = Class.forName("org.eclipse.swt.internal.cocoa.NSWindow");
                screenMethod = clazz2.getDeclaredMethod("screen", new Class[0]);
                screenMethod.setAccessible(true);
                Class<?> clazz3 = Class.forName("org.eclipse.swt.internal.cocoa.NSScreen");
                backingScaleFactorMethod = clazz3.getDeclaredMethod("backingScaleFactor", new Class[0]);
                backingScaleFactorMethod.setAccessible(true);
            }
            catch (Exception exception) {}
        } else if (SWT.getPlatform().equals("win32")) {
            try {
                String string = AccessController.doPrivileged(() -> System.getProperty("swt.autoScale"));
                if (string == null || !"false".equalsIgnoreCase(string)) {
                    Class<?> clazz = Class.forName("org.eclipse.swt.internal.DPIUtil");
                    swtDPIUtilMethod = clazz.getMethod("getDeviceZoom", new Class[0]);
                }
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        FXCanvas.initFx();
        targets = new ArrayList();
    }

    private class HostContainer
    implements HostInterface {
        final FXCanvas fxCanvas;
        Object lock;
        boolean queued;

        private HostContainer() {
            this.fxCanvas = FXCanvas.this;
            this.lock = new Object();
            this.queued = false;
        }

        @Override
        public void setEmbeddedStage(EmbeddedStageInterface embeddedStageInterface) {
            FXCanvas.this.stagePeer = embeddedStageInterface;
            if (FXCanvas.this.stagePeer == null) {
                return;
            }
            if (FXCanvas.this.pWidth > 0 && FXCanvas.this.pHeight > 0) {
                FXCanvas.this.stagePeer.setSize(FXCanvas.this.pWidth, FXCanvas.this.pHeight);
            }
            if (FXCanvas.this.isFocusControl()) {
                FXCanvas.this.stagePeer.setFocused(true, 0);
            }
            FXCanvas.this.sendMoveEventToFX();
            FXCanvas.this.sendResizeEventToFX();
        }

        TransferMode getTransferMode(int n) {
            switch (n) {
                case 1: {
                    return TransferMode.COPY;
                }
                case 2: 
                case 8: {
                    return TransferMode.MOVE;
                }
                case 4: {
                    return TransferMode.LINK;
                }
            }
            return null;
        }

        Set<TransferMode> getTransferModes(int n) {
            HashSet<TransferMode> hashSet = new HashSet<TransferMode>();
            if ((n & 1) != 0) {
                hashSet.add(TransferMode.COPY);
            }
            if ((n & 2) != 0) {
                hashSet.add(TransferMode.MOVE);
            }
            if ((n & 8) != 0) {
                hashSet.add(TransferMode.MOVE);
            }
            if ((n & 4) != 0) {
                hashSet.add(TransferMode.LINK);
            }
            return hashSet;
        }

        ImageData createImageData(Pixels pixels) {
            int n;
            Buffer buffer;
            if (pixels == null) {
                return null;
            }
            int n2 = pixels.getWidth();
            int n3 = pixels.getHeight();
            int n4 = n2 * 4;
            int n5 = n4 * n3;
            byte[] byArray = new byte[n5];
            byte[] byArray2 = new byte[n2 * n3];
            if (pixels.getBytesPerComponent() == 1) {
                buffer = (ByteBuffer)pixels.getPixels();
                int n6 = 0;
                int n7 = 0;
                for (n = 0; n < n3; ++n) {
                    int n8 = 0;
                    while (n8 < n2) {
                        byte by = ((ByteBuffer)buffer).get();
                        byte by2 = ((ByteBuffer)buffer).get();
                        byte by3 = ((ByteBuffer)buffer).get();
                        byte by4 = ((ByteBuffer)buffer).get();
                        byArray2[n7++] = by4;
                        byArray[n6] = by;
                        byArray[n6 + 1] = by2;
                        byArray[n6 + 2] = by3;
                        byArray[n6 + 3] = 0;
                        ++n8;
                        n6 += 4;
                    }
                }
            } else if (pixels.getBytesPerComponent() == 4) {
                buffer = (IntBuffer)pixels.getPixels();
                int n9 = 0;
                int n10 = 0;
                for (n = 0; n < n3; ++n) {
                    int n11 = 0;
                    while (n11 < n2) {
                        int n12 = ((IntBuffer)buffer).get();
                        byte by = (byte)(n12 & 0xFF);
                        byte by5 = (byte)(n12 >> 8 & 0xFF);
                        byte by6 = (byte)(n12 >> 16 & 0xFF);
                        byte by7 = (byte)(n12 >> 24 & 0xFF);
                        byArray2[n10++] = by7;
                        byArray[n9] = by;
                        byArray[n9 + 1] = by5;
                        byArray[n9 + 2] = by6;
                        byArray[n9 + 3] = 0;
                        ++n11;
                        n9 += 4;
                    }
                }
            } else {
                return null;
            }
            buffer = new PaletteData(65280, 0xFF0000, -16777216);
            ImageData imageData = new ImageData(n2, n3, 32, (PaletteData)buffer, 4, byArray);
            imageData.alphaData = byArray2;
            return imageData;
        }

        private DragSource createDragSource(final EmbeddedSceneDSInterface embeddedSceneDSInterface, TransferMode transferMode) {
            Transfer[] transferArray = this.getTransferTypes(embeddedSceneDSInterface.getMimeTypes());
            if (transferArray.length == 0) {
                return null;
            }
            int n = this.getDragActions(embeddedSceneDSInterface.getSupportedActions());
            final DragSource dragSource = new DragSource((Control)FXCanvas.this, n);
            dragSource.setTransfer(transferArray);
            dragSource.addDragListener(new DragSourceListener(){

                public void dragFinished(DragSourceEvent dragSourceEvent) {
                    dragSource.dispose();
                    embeddedSceneDSInterface.dragDropEnd(HostContainer.this.getTransferMode(dragSourceEvent.detail));
                }

                public void dragSetData(DragSourceEvent dragSourceEvent) {
                    Transfer[] transferArray = dragSource.getTransfer();
                    for (int i = 0; i < transferArray.length; ++i) {
                        String string;
                        if (transferArray[i].isSupportedType(dragSourceEvent.dataType) && (string = HostContainer.this.getMime(transferArray[i])) != null) {
                            dragSourceEvent.doit = true;
                            dragSourceEvent.data = embeddedSceneDSInterface.getData(string);
                            if (dragSourceEvent.data instanceof Pixels) {
                                dragSourceEvent.data = HostContainer.this.createImageData((Pixels)dragSourceEvent.data);
                            }
                            return;
                        }
                        dragSourceEvent.doit = false;
                    }
                }

                public void dragStart(DragSourceEvent dragSourceEvent) {
                }
            });
            return dragSource;
        }

        int getDragAction(TransferMode transferMode) {
            if (transferMode == null) {
                return 0;
            }
            switch (transferMode) {
                case COPY: {
                    return 1;
                }
                case MOVE: {
                    return 2;
                }
                case LINK: {
                    return 4;
                }
            }
            throw new IllegalArgumentException("Invalid transfer mode");
        }

        int getDragActions(Set<TransferMode> set) {
            int n = 0;
            for (TransferMode transferMode : set) {
                n |= this.getDragAction(transferMode);
            }
            return n;
        }

        Transfer getTransferType(String string) {
            if (string.equals("text/plain")) {
                return TextTransfer.getInstance();
            }
            if (string.equals("text/rtf")) {
                return RTFTransfer.getInstance();
            }
            if (string.equals("text/html")) {
                return HTMLTransfer.getInstance();
            }
            if (string.equals("text/uri-list")) {
                return URLTransfer.getInstance();
            }
            if (string.equals("application/x-java-rawimage")) {
                return ImageTransfer.getInstance();
            }
            if (string.equals("application/x-java-file-list") || string.equals("java.file-list")) {
                return FileTransfer.getInstance();
            }
            return FXCanvas.getCustomTransfer(string);
        }

        Transfer[] getTransferTypes(String[] stringArray) {
            int n = 0;
            Transfer[] transferArray = new Transfer[stringArray.length];
            for (int i = 0; i < stringArray.length; ++i) {
                Transfer transfer = this.getTransferType(stringArray[i]);
                if (transfer == null) continue;
                transferArray[n++] = transfer;
            }
            if (n != stringArray.length) {
                Transfer[] transferArray2 = new Transfer[n];
                System.arraycopy(transferArray, 0, transferArray2, 0, n);
                transferArray = transferArray2;
            }
            return transferArray;
        }

        String getMime(Transfer transfer) {
            if (transfer.equals(TextTransfer.getInstance())) {
                return "text/plain";
            }
            if (transfer.equals(RTFTransfer.getInstance())) {
                return "text/rtf";
            }
            if (transfer.equals(HTMLTransfer.getInstance())) {
                return "text/html";
            }
            if (transfer.equals(URLTransfer.getInstance())) {
                return "text/uri-list";
            }
            if (transfer.equals(ImageTransfer.getInstance())) {
                return "application/x-java-rawimage";
            }
            if (transfer.equals(FileTransfer.getInstance())) {
                return "application/x-java-file-list";
            }
            if (transfer instanceof CustomTransfer) {
                return ((CustomTransfer)transfer).getMime();
            }
            return null;
        }

        String[] getMimes(Transfer[] transferArray, TransferData transferData) {
            int n = 0;
            String[] stringArray = new String[transferArray.length];
            for (int i = 0; i < transferArray.length; ++i) {
                if (!transferArray[i].isSupportedType(transferData)) continue;
                stringArray[n++] = this.getMime(transferArray[i]);
            }
            if (n != stringArray.length) {
                String[] stringArray2 = new String[n];
                System.arraycopy(stringArray, 0, stringArray2, 0, n);
                stringArray = stringArray2;
            }
            return stringArray;
        }

        DropTarget createDropTarget(EmbeddedSceneInterface embeddedSceneInterface) {
            final DropTarget dropTarget = new DropTarget((Control)FXCanvas.this, 7);
            final EmbeddedSceneDTInterface embeddedSceneDTInterface = embeddedSceneInterface.createDropTarget();
            dropTarget.setTransfer(FXCanvas.getAllTransfers());
            dropTarget.addDropListener(new DropTargetListener(){
                Object data;
                TransferData currentTransferData;
                boolean ignoreLeave;
                int detail = 0;
                int operations = 0;
                EmbeddedSceneDSInterface fxDragSource = new EmbeddedSceneDSInterface(){

                    @Override
                    public Set<TransferMode> getSupportedActions() {
                        return HostContainer.this.getTransferModes(operations);
                    }

                    @Override
                    public Object getData(String string) {
                        return data;
                    }

                    @Override
                    public String[] getMimeTypes() {
                        if (currentTransferData == null) {
                            return new String[0];
                        }
                        return HostContainer.this.getMimes(FXCanvas.getAllTransfers(), currentTransferData);
                    }

                    @Override
                    public boolean isMimeTypeAvailable(String string) {
                        String[] stringArray = this.getMimeTypes();
                        for (int i = 0; i < stringArray.length; ++i) {
                            if (!stringArray[i].equals(string)) continue;
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void dragDropEnd(TransferMode transferMode) {
                        data = null;
                        currentTransferData = null;
                    }
                };

                public void dragEnter(DropTargetEvent dropTargetEvent) {
                    this.ignoreLeave = false;
                    dropTarget.setTransfer(FXCanvas.getAllTransfers());
                    this.detail = dropTargetEvent.detail;
                    this.operations = dropTargetEvent.operations;
                    this.dragOver(dropTargetEvent, true, this.detail);
                }

                public void dragLeave(DropTargetEvent dropTargetEvent) {
                    this.operations = 0;
                    this.detail = 0;
                    this.data = null;
                    this.currentTransferData = null;
                    FXCanvas.this.getDisplay().asyncExec(() -> {
                        if (this.ignoreLeave) {
                            return;
                        }
                        embeddedSceneDTInterface.handleDragLeave();
                    });
                }

                public void dragOperationChanged(DropTargetEvent dropTargetEvent) {
                    this.detail = dropTargetEvent.detail;
                    this.operations = dropTargetEvent.operations;
                    this.dragOver(dropTargetEvent, false, this.detail);
                }

                public void dragOver(DropTargetEvent dropTargetEvent) {
                    this.operations = dropTargetEvent.operations;
                    this.dragOver(dropTargetEvent, false, this.detail);
                }

                public void dragOver(DropTargetEvent dropTargetEvent, boolean bl, int n) {
                    this.currentTransferData = dropTargetEvent.currentDataType;
                    Point point = FXCanvas.this.toControl(dropTargetEvent.x, dropTargetEvent.y);
                    if (n == 0) {
                        n = 1;
                    }
                    TransferMode transferMode = HostContainer.this.getTransferMode(n);
                    TransferMode transferMode2 = bl ? embeddedSceneDTInterface.handleDragEnter(point.x, point.y, dropTargetEvent.x, dropTargetEvent.y, transferMode, this.fxDragSource) : embeddedSceneDTInterface.handleDragOver(point.x, point.y, dropTargetEvent.x, dropTargetEvent.y, transferMode);
                    dropTargetEvent.detail = HostContainer.this.getDragAction(transferMode2);
                }

                public void drop(DropTargetEvent dropTargetEvent) {
                    this.detail = dropTargetEvent.detail;
                    this.operations = dropTargetEvent.operations;
                    this.data = dropTargetEvent.data;
                    this.currentTransferData = dropTargetEvent.currentDataType;
                    Point point = FXCanvas.this.toControl(dropTargetEvent.x, dropTargetEvent.y);
                    TransferMode transferMode = HostContainer.this.getTransferMode(dropTargetEvent.detail);
                    TransferMode transferMode2 = embeddedSceneDTInterface.handleDragDrop(point.x, point.y, dropTargetEvent.x, dropTargetEvent.y, transferMode);
                    dropTargetEvent.detail = HostContainer.this.getDragAction(transferMode2);
                    this.data = null;
                    this.currentTransferData = null;
                }

                public void dropAccept(DropTargetEvent dropTargetEvent) {
                    this.ignoreLeave = true;
                }
            });
            return dropTarget;
        }

        @Override
        public void setEmbeddedScene(EmbeddedSceneInterface embeddedSceneInterface) {
            FXCanvas.this.scenePeer = embeddedSceneInterface;
            if (FXCanvas.this.scenePeer == null) {
                return;
            }
            if (FXCanvas.this.pWidth > 0 && FXCanvas.this.pHeight > 0) {
                FXCanvas.this.scenePeer.setSize(FXCanvas.this.pWidth, FXCanvas.this.pHeight);
            }
            double d = FXCanvas.this.getScaleFactor();
            FXCanvas.this.resizePixelBuffer(d);
            FXCanvas.this.lastScaleFactor = d;
            FXCanvas.this.scenePeer.setPixelScaleFactors((float)d, (float)d);
            FXCanvas.this.scenePeer.setDragStartListener((embeddedSceneDSInterface, transferMode) -> Platform.runLater(() -> {
                DragSource dragSource = this.createDragSource(embeddedSceneDSInterface, transferMode);
                if (dragSource == null) {
                    embeddedSceneDSInterface.dragDropEnd(null);
                } else {
                    FXCanvas.updateDropTarget();
                    FXCanvas.this.notifyListeners(29, null);
                }
            }));
            FXCanvas.this.setDropTarget(null);
            FXCanvas.this.setDropTarget(this.createDropTarget(embeddedSceneInterface));
        }

        @Override
        public boolean requestFocus() {
            Display.getDefault().asyncExec(() -> {
                if (FXCanvas.this.isDisposed()) {
                    return;
                }
                FXCanvas.this.forceFocus();
            });
            return true;
        }

        @Override
        public boolean traverseFocusOut(boolean bl) {
            return true;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void repaint() {
            Object object = this.lock;
            synchronized (object) {
                if (this.queued) {
                    return;
                }
                this.queued = true;
                Display.getDefault().asyncExec(() -> {
                    try {
                        if (FXCanvas.this.isDisposed()) {
                            return;
                        }
                        FXCanvas.this.redraw();
                    }
                    finally {
                        Object object = this.lock;
                        synchronized (object) {
                            this.queued = false;
                        }
                    }
                });
            }
        }

        @Override
        public void setPreferredSize(int n, int n2) {
            FXCanvas.this.pPreferredWidth = n;
            FXCanvas.this.pPreferredHeight = n2;
        }

        @Override
        public void setEnabled(boolean bl) {
            FXCanvas.this.setEnabled(bl);
        }

        @Override
        public void setCursor(CursorFrame cursorFrame) {
            FXCanvas.this.setCursor(this.getPlatformCursor(cursorFrame));
        }

        private Cursor getPlatformCursor(CursorFrame cursorFrame) {
            if (cursorFrame.getCursorType() == CursorType.DEFAULT) {
                return null;
            }
            Cursor cursor = cursorFrame.getPlatformCursor(Cursor.class);
            if (cursor != null) {
                return cursor;
            }
            Cursor cursor2 = SWTCursors.embedCursorToCursor(cursorFrame);
            cursorFrame.setPlatforCursor(Cursor.class, cursor2);
            return cursor2;
        }

        @Override
        public boolean grabFocus() {
            return true;
        }

        @Override
        public void ungrabFocus() {
        }
    }
}

