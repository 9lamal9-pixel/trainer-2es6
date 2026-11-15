/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control;

import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.skin.MenuBarSkin;

public class MenuBarButton
extends MenuButton {
    public ChangeListener<Boolean> menuListener;
    public MenuBarSkin menuBarSkin;
    public Menu menu;
    private final ListChangeListener<MenuItem> itemsListener;
    private final ListChangeListener<String> styleClassListener;

    public MenuBarButton(MenuBarSkin menuBarSkin, Menu menu) {
        super(menu.getText(), menu.getGraphic());
        this.menuBarSkin = menuBarSkin;
        this.setAccessibleRole(AccessibleRole.MENU);
        this.itemsListener = change -> {
            while (change.next()) {
                this.getItems().removeAll(change.getRemoved());
                this.getItems().addAll(change.getFrom(), change.getAddedSubList());
            }
        };
        menu.getItems().addListener(this.itemsListener);
        this.styleClassListener = change -> {
            while (change.next()) {
                for (int i = change.getFrom(); i < change.getTo(); ++i) {
                    this.getStyleClass().add((String)menu.getStyleClass().get(i));
                }
                for (String string : change.getRemoved()) {
                    this.getStyleClass().remove(string);
                }
            }
        };
        menu.getStyleClass().addListener(this.styleClassListener);
        this.idProperty().bind(menu.idProperty());
    }

    public MenuBarSkin getMenuBarSkin() {
        return this.menuBarSkin;
    }

    public void clearHover() {
        this.setHover(false);
    }

    public void setHover() {
        this.setHover(true);
        ((MenuBar)this.menuBarSkin.getSkinnable()).notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
    }

    public void dispose() {
        this.menu.getItems().removeListener(this.itemsListener);
        this.menu.getStyleClass().removeListener(this.styleClassListener);
        this.idProperty().unbind();
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case FOCUS_ITEM: {
                return this;
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }
}

