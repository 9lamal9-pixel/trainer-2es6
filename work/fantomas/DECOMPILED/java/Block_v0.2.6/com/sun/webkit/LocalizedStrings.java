/*
 * Decompiled with CFR 0.152.
 */
package com.sun.webkit;

import com.sun.javafx.logging.PlatformLogger;
import java.util.Locale;
import java.util.ResourceBundle;

final class LocalizedStrings {
    private static final PlatformLogger log = PlatformLogger.getLogger(LocalizedStrings.class.getName());
    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("com.sun.webkit.LocalizedStrings", Locale.getDefault());

    private LocalizedStrings() {
    }

    private static String getLocalizedProperty(String string) {
        log.fine("Get property: " + string);
        String string2 = BUNDLE.getString(string);
        if (string2 != null && string2.trim().length() > 0) {
            log.fine("Property value: " + string2);
            return string2.trim();
        }
        log.fine("Unknown property value");
        return null;
    }
}

