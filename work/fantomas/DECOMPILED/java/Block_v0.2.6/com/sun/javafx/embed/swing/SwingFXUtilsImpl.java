/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing;

import com.sun.javafx.embed.swing.InteropFactory;
import com.sun.javafx.embed.swing.SwingFXUtilsImplInterop;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.security.AccessController;

public class SwingFXUtilsImpl {
    private static SwingFXUtilsImplInterop swFXUtilIOP;

    private static EventQueue getEventQueue() {
        return AccessController.doPrivileged(() -> Toolkit.getDefaultToolkit().getSystemEventQueue());
    }

    public static void installFwEventQueue() {
        swFXUtilIOP.setFwDispatcher(SwingFXUtilsImpl.getEventQueue());
    }

    public static void removeFwEventQueue() {
        swFXUtilIOP.setFwDispatcher(SwingFXUtilsImpl.getEventQueue());
    }

    static {
        InteropFactory interopFactory = null;
        try {
            interopFactory = InteropFactory.getInstance();
        }
        catch (Exception exception) {
            throw new ExceptionInInitializerError(exception);
        }
        swFXUtilIOP = interopFactory.createSwingFXUtilsImpl();
    }
}

