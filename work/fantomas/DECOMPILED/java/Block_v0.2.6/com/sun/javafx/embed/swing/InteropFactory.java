/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.embed.swing;

import com.sun.javafx.embed.swing.FXDnDInterop;
import com.sun.javafx.embed.swing.JFXPanelInterop;
import com.sun.javafx.embed.swing.SwingFXUtilsImplInterop;
import com.sun.javafx.embed.swing.SwingNodeInterop;
import java.lang.reflect.Constructor;
import java.security.AccessController;

public abstract class InteropFactory {
    private static InteropFactory instance = null;
    private static boolean verbose = false;
    private static final String[] factoryNames;

    public static synchronized InteropFactory getInstance() throws Exception {
        if (instance != null) {
            return instance;
        }
        Class<?> clazz = null;
        for (String string : factoryNames) {
            try {
                clazz = Class.forName(string);
                Constructor<?> constructor = clazz.getConstructor(new Class[0]);
                instance = (InteropFactory)constructor.newInstance(new Object[0]);
                return instance;
            }
            catch (Exception exception) {
                System.err.println("InteropFactory: cannot load " + string);
                if (!verbose) continue;
                exception.printStackTrace();
            }
        }
        throw new Exception("No swing interop factory can be loaded");
    }

    public abstract SwingNodeInterop createSwingNodeImpl();

    public abstract JFXPanelInterop createJFXPanelImpl();

    public abstract FXDnDInterop createFXDnDImpl();

    public abstract SwingFXUtilsImplInterop createSwingFXUtilsImpl();

    static {
        AccessController.doPrivileged(() -> {
            verbose = Boolean.valueOf(System.getProperty("javafx.embed.swing.verbose"));
            return null;
        });
        factoryNames = new String[]{"com.sun.javafx.embed.swing.newimpl.InteropFactoryN", "com.sun.javafx.embed.swing.oldimpl.InteropFactoryO"};
    }
}

