/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.webkit.theme;

import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.webkit.theme.PopupMenuImpl;
import com.sun.webkit.ContextMenu;
import com.sun.webkit.ContextMenuItem;
import java.util.Collection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;

public final class ContextMenuImpl
extends ContextMenu {
    private static final PlatformLogger log = PlatformLogger.getLogger(ContextMenuImpl.class.getName());
    private final ObservableList<ContextMenuItem> items = FXCollections.observableArrayList();

    @Override
    protected void show(ContextMenu.ShowContext showContext, int n, int n2) {
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine("show at [{0}, {1}]", n, n2);
        }
        javafx.scene.control.ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
        contextMenu.setOnAction(actionEvent -> {
            MenuItem menuItem = (MenuItem)actionEvent.getTarget();
            log.fine("onAction: item={0}", menuItem);
            showContext.notifyItemSelected(((MenuItemPeer)((Object)menuItem)).getItemPeer().getAction());
        });
        contextMenu.getItems().addAll((Collection<MenuItem>)this.fillMenu());
        PopupMenuImpl.doShow(contextMenu, showContext.getPage(), n, n2);
    }

    @Override
    protected void appendItem(ContextMenuItem contextMenuItem) {
        this.insertItem(contextMenuItem, this.items.size());
    }

    @Override
    protected void insertItem(ContextMenuItem contextMenuItem, int n) {
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine("item={0}, index={1}", contextMenuItem, n);
        }
        if (contextMenuItem == null) {
            return;
        }
        this.items.remove(contextMenuItem);
        if (this.items.size() == 0) {
            this.items.add(contextMenuItem);
        } else {
            this.items.add(n, contextMenuItem);
        }
    }

    @Override
    protected int getItemCount() {
        return this.items.size();
    }

    private MenuItem createMenuItem(ContextMenuItem contextMenuItem) {
        log.fine("item={0}", contextMenuItem);
        if (contextMenuItem.getType() == 2) {
            MenuImpl menuImpl = new MenuImpl(contextMenuItem.getTitle());
            if (contextMenuItem.getSubmenu() != null) {
                menuImpl.getItems().addAll((Collection<MenuItem>)((ContextMenuImpl)contextMenuItem.getSubmenu()).fillMenu());
            }
            return menuImpl;
        }
        if (contextMenuItem.getType() == 0) {
            MenuItem menuItem = null;
            menuItem = contextMenuItem.isChecked() ? new CheckMenuItemImpl(contextMenuItem) : new MenuItemImpl(contextMenuItem);
            menuItem.setDisable(!contextMenuItem.isEnabled());
            return menuItem;
        }
        if (contextMenuItem.getType() == 1) {
            return new SeparatorImpl(contextMenuItem);
        }
        throw new IllegalArgumentException("unexpected item type");
    }

    private ObservableList<MenuItem> fillMenu() {
        ObservableList<MenuItem> observableList = FXCollections.observableArrayList();
        for (ContextMenuItem contextMenuItem : this.items) {
            observableList.add(this.createMenuItem(contextMenuItem));
        }
        return observableList;
    }

    static final class SeparatorImpl
    extends MenuItem
    implements MenuItemPeer {
        private final ContextMenuItem itemPeer;

        SeparatorImpl(ContextMenuItem contextMenuItem) {
            this.itemPeer = contextMenuItem;
            this.setGraphic(new Separator());
            this.setDisable(true);
        }

        @Override
        public ContextMenuItem getItemPeer() {
            return this.itemPeer;
        }
    }

    private static final class MenuImpl
    extends Menu {
        private MenuImpl(String string) {
            super(string);
        }
    }

    private static final class CheckMenuItemImpl
    extends CheckMenuItem
    implements MenuItemPeer {
        private final ContextMenuItem itemPeer;

        private CheckMenuItemImpl(ContextMenuItem contextMenuItem) {
            this.itemPeer = contextMenuItem;
        }

        @Override
        public ContextMenuItem getItemPeer() {
            return this.itemPeer;
        }
    }

    private static final class MenuItemImpl
    extends MenuItem
    implements MenuItemPeer {
        private final ContextMenuItem itemPeer;

        private MenuItemImpl(ContextMenuItem contextMenuItem) {
            super(contextMenuItem.getTitle());
            this.itemPeer = contextMenuItem;
        }

        @Override
        public ContextMenuItem getItemPeer() {
            return this.itemPeer;
        }
    }

    private static interface MenuItemPeer {
        public ContextMenuItem getItemPeer();
    }
}

