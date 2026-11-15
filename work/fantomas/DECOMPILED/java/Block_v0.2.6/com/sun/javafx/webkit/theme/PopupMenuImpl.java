/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.webkit.theme;

import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.webkit.theme.ContextMenuImpl;
import com.sun.webkit.Invoker;
import com.sun.webkit.PopupMenu;
import com.sun.webkit.WebPage;
import com.sun.webkit.WebPageClient;
import com.sun.webkit.graphics.WCFont;
import com.sun.webkit.graphics.WCPoint;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.web.WebView;

public final class PopupMenuImpl
extends PopupMenu {
    private static final PlatformLogger log = PlatformLogger.getLogger(PopupMenuImpl.class.getName());
    private final ContextMenu popupMenu = new ContextMenu();

    public PopupMenuImpl() {
        this.popupMenu.setOnHidden(windowEvent -> {
            log.finer("onHidden");
            Invoker.getInvoker().postOnEventThread(() -> {
                log.finer("onHidden: notifying");
                this.notifyPopupClosed();
            });
        });
        this.popupMenu.setOnAction(actionEvent -> {
            MenuItem menuItem = (MenuItem)actionEvent.getTarget();
            log.fine("onAction: item={0}", menuItem);
            this.notifySelectionCommited(this.popupMenu.getItems().indexOf(menuItem));
        });
    }

    @Override
    protected void show(WebPage webPage, int n, int n2, int n3) {
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine("show at [{0}, {1}], width={2}", n, n2, n3);
        }
        this.popupMenu.setPrefWidth(n3);
        this.popupMenu.setPrefHeight(this.popupMenu.getHeight());
        PopupMenuImpl.doShow(this.popupMenu, webPage, n, n2);
    }

    @Override
    protected void hide() {
        log.fine("hiding");
        this.popupMenu.hide();
    }

    @Override
    protected void appendItem(String string, boolean bl, boolean bl2, boolean bl3, int n, int n2, WCFont wCFont) {
        MenuItem menuItem;
        if (log.isLoggable(PlatformLogger.Level.FINEST)) {
            log.finest("itemText={0}, isLabel={1}, isSeparator={2}, isEnabled={3}, bgColor={4}, fgColor={5}, font={6}", string, bl, bl2, bl3, n, n2, wCFont);
        }
        if (bl2) {
            menuItem = new ContextMenuImpl.SeparatorImpl(null);
        } else {
            menuItem = new MenuItem(string);
            menuItem.setDisable(!bl3);
        }
        menuItem.setMnemonicParsing(false);
        this.popupMenu.getItems().add(menuItem);
    }

    @Override
    protected void setSelectedItem(int n) {
        log.finest("index={0}", n);
    }

    static void doShow(ContextMenu contextMenu, WebPage webPage, int n, int n2) {
        WebPageClient webPageClient = webPage.getPageClient();
        assert (webPageClient != null);
        WCPoint wCPoint = webPageClient.windowToScreen(new WCPoint(n, n2));
        contextMenu.show(((WebView)webPageClient.getContainer()).getScene().getWindow(), (double)wCPoint.getX(), (double)wCPoint.getY());
    }
}

