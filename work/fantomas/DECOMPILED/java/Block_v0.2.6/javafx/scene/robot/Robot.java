/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.robot;

import com.sun.glass.ui.Application;
import com.sun.glass.ui.GlassRobot;
import com.sun.javafx.FXPermissions;
import com.sun.javafx.tk.Toolkit;
import java.util.Objects;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;

public final class Robot {
    private final GlassRobot peer;

    public Robot() {
        Application.checkEventThread();
        SecurityManager securityManager = System.getSecurityManager();
        if (securityManager != null) {
            securityManager.checkPermission(FXPermissions.CREATE_ROBOT_PERMISSION);
        }
        this.peer = Toolkit.getToolkit().createRobot();
        this.peer.create();
    }

    public void keyPress(KeyCode keyCode) {
        Objects.requireNonNull(keyCode, "keyCode must not be null");
        this.peer.keyPress(keyCode);
    }

    public void keyRelease(KeyCode keyCode) {
        Objects.requireNonNull(keyCode, "keyCode must not be null");
        this.peer.keyRelease(keyCode);
    }

    public void keyType(KeyCode keyCode) {
        Objects.requireNonNull(keyCode, "keyCode must not be null");
        this.keyPress(keyCode);
        this.keyRelease(keyCode);
    }

    public double getMouseX() {
        return this.peer.getMouseX();
    }

    public double getMouseY() {
        return this.peer.getMouseY();
    }

    public Point2D getMousePosition() {
        return new Point2D(this.getMouseX(), this.getMouseY());
    }

    public void mouseMove(double d, double d2) {
        this.peer.mouseMove(d, d2);
    }

    public final void mouseMove(Point2D point2D) {
        Objects.requireNonNull(point2D);
        this.mouseMove(point2D.getX(), point2D.getY());
    }

    public void mousePress(MouseButton ... mouseButtonArray) {
        Objects.requireNonNull(mouseButtonArray, "buttons must not be null");
        this.peer.mousePress(mouseButtonArray);
    }

    public void mouseRelease(MouseButton ... mouseButtonArray) {
        Objects.requireNonNull(mouseButtonArray, "buttons must not be null");
        this.peer.mouseRelease(mouseButtonArray);
    }

    public void mouseClick(MouseButton ... mouseButtonArray) {
        Objects.requireNonNull(mouseButtonArray, "buttons must not be null");
        this.mousePress(mouseButtonArray);
        this.mouseRelease(mouseButtonArray);
    }

    public void mouseWheel(int n) {
        this.peer.mouseWheel(n);
    }

    public Color getPixelColor(double d, double d2) {
        return this.peer.getPixelColor(d, d2);
    }

    public Color getPixelColor(Point2D point2D) {
        return this.getPixelColor(point2D.getX(), point2D.getY());
    }

    public WritableImage getScreenCapture(WritableImage writableImage, double d, double d2, double d3, double d4, boolean bl) {
        return this.peer.getScreenCapture(writableImage, d, d2, d3, d4, bl);
    }

    public WritableImage getScreenCapture(WritableImage writableImage, double d, double d2, double d3, double d4) {
        return this.getScreenCapture(writableImage, d, d2, d3, d4, true);
    }

    public WritableImage getScreenCapture(WritableImage writableImage, Rectangle2D rectangle2D) {
        Objects.requireNonNull(rectangle2D);
        return this.getScreenCapture(writableImage, rectangle2D.getMinX(), rectangle2D.getMinY(), rectangle2D.getWidth(), rectangle2D.getHeight(), true);
    }

    public WritableImage getScreenCapture(WritableImage writableImage, Rectangle2D rectangle2D, boolean bl) {
        Objects.requireNonNull(rectangle2D);
        return this.getScreenCapture(writableImage, rectangle2D.getMinX(), rectangle2D.getMinY(), rectangle2D.getWidth(), rectangle2D.getHeight(), bl);
    }
}

