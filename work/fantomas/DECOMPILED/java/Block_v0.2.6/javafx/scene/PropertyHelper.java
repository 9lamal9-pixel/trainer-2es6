/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene;

import java.security.AccessController;

class PropertyHelper {
    PropertyHelper() {
    }

    static boolean getBooleanProperty(String string) {
        try {
            boolean bl = AccessController.doPrivileged(() -> {
                String string2 = System.getProperty(string);
                return "true".equals(string2.toLowerCase());
            });
            return bl;
        }
        catch (Exception exception) {
            return false;
        }
    }
}

