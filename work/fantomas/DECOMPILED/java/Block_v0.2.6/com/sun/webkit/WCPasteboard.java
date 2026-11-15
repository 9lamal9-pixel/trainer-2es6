/*
 * Decompiled with CFR 0.152.
 */
package com.sun.webkit;

import com.sun.javafx.logging.PlatformLogger;
import com.sun.webkit.Pasteboard;
import com.sun.webkit.Utilities;
import com.sun.webkit.graphics.WCImageFrame;

final class WCPasteboard {
    private static final PlatformLogger log = PlatformLogger.getLogger(WCPasteboard.class.getName());
    private static final Pasteboard pasteboard = Utilities.getUtilities().createPasteboard();

    private WCPasteboard() {
    }

    private static String getPlainText() {
        log.fine("getPlainText()");
        return pasteboard.getPlainText();
    }

    private static String getHtml() {
        log.fine("getHtml()");
        return pasteboard.getHtml();
    }

    private static void writePlainText(String string) {
        log.fine("writePlainText(): text = {0}", string);
        pasteboard.writePlainText(string);
    }

    private static void writeSelection(boolean bl, String string, String string2) {
        log.fine("writeSelection(): canSmartCopyOrDelete = {0},\n text = \n{1}\n html=\n{2}", bl, string, string2);
        pasteboard.writeSelection(bl, string, string2);
    }

    private static void writeImage(WCImageFrame wCImageFrame) {
        log.fine("writeImage(): img = {0}", wCImageFrame);
        pasteboard.writeImage(wCImageFrame);
    }

    private static void writeUrl(String string, String string2) {
        log.fine("writeUrl(): url = {0}, markup = {1}", string, string2);
        pasteboard.writeUrl(string, string2);
    }
}

