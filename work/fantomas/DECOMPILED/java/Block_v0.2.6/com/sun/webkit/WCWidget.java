/*
 * Decompiled with CFR 0.152.
 */
package com.sun.webkit;

import com.sun.javafx.logging.PlatformLogger;
import com.sun.webkit.WebPage;
import com.sun.webkit.WebPageClient;
import com.sun.webkit.graphics.WCRectangle;

class WCWidget {
    private static final PlatformLogger log = PlatformLogger.getLogger(WCWidget.class.getName());
    private int x;
    private int y;
    private int width;
    private int height;
    private final WebPage page;

    WCWidget(WebPage webPage) {
        this.page = webPage;
    }

    WebPage getPage() {
        return this.page;
    }

    WCRectangle getBounds() {
        return new WCRectangle(this.x, this.y, this.width, this.height);
    }

    void setBounds(int n, int n2, int n3, int n4) {
        this.x = n;
        this.y = n2;
        this.width = n3;
        this.height = n4;
    }

    protected void destroy() {
    }

    protected void requestFocus() {
    }

    protected void setCursor(long l) {
    }

    protected void setVisible(boolean bl) {
    }

    private void fwkDestroy() {
        log.finer("destroy");
        this.destroy();
    }

    private void fwkSetBounds(int n, int n2, int n3, int n4) {
        if (log.isLoggable(PlatformLogger.Level.FINER)) {
            log.finer("setBounds({0}, {1}, {2}, {3})", n, n2, n3, n4);
        }
        this.setBounds(n, n2, n3, n4);
    }

    private void fwkRequestFocus() {
        log.finer("requestFocus");
        this.requestFocus();
    }

    private void fwkSetCursor(long l) {
        if (log.isLoggable(PlatformLogger.Level.FINER)) {
            log.finer("setCursor({0})", l);
        }
        this.setCursor(l);
    }

    private void fwkSetVisible(boolean bl) {
        if (log.isLoggable(PlatformLogger.Level.FINER)) {
            log.finer("setVisible({0})", bl);
        }
        this.setVisible(bl);
    }

    protected int fwkGetScreenDepth() {
        log.finer("getScreenDepth");
        WebPageClient webPageClient = this.page.getPageClient();
        return webPageClient != null ? webPageClient.getScreenDepth() : 24;
    }

    protected WCRectangle fwkGetScreenRect(boolean bl) {
        WebPageClient webPageClient;
        if (log.isLoggable(PlatformLogger.Level.FINER)) {
            log.finer("getScreenRect({0})", bl);
        }
        return (webPageClient = this.page.getPageClient()) != null ? webPageClient.getScreenBounds(bl) : null;
    }

    private static native void initIDs();

    static {
        WCWidget.initIDs();
    }
}

