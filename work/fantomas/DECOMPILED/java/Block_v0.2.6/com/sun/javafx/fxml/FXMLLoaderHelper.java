/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.fxml;

import com.sun.javafx.util.Utils;
import javafx.fxml.FXMLLoader;

public class FXMLLoaderHelper {
    private static FXMLLoaderAccessor fxmlLoaderAccessor;

    private FXMLLoaderHelper() {
    }

    public static void setStaticLoad(FXMLLoader fXMLLoader, boolean bl) {
        fxmlLoaderAccessor.setStaticLoad(fXMLLoader, bl);
    }

    public static void setFXMLLoaderAccessor(FXMLLoaderAccessor fXMLLoaderAccessor) {
        if (fxmlLoaderAccessor != null) {
            throw new IllegalStateException();
        }
        fxmlLoaderAccessor = fXMLLoaderAccessor;
    }

    static {
        Utils.forceInit(FXMLLoader.class);
    }

    public static interface FXMLLoaderAccessor {
        public void setStaticLoad(FXMLLoader var1, boolean var2);
    }
}

