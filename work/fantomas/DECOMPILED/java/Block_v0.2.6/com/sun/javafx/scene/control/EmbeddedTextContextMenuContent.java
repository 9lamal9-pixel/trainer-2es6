/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control;

import com.sun.javafx.scene.control.skin.Utils;
import java.util.Collection;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class EmbeddedTextContextMenuContent
extends StackPane {
    private ContextMenu contextMenu;
    private StackPane pointer;
    private HBox menuBox;

    public EmbeddedTextContextMenuContent(ContextMenu contextMenu) {
        this.contextMenu = contextMenu;
        this.menuBox = new HBox();
        this.pointer = new StackPane();
        this.pointer.getStyleClass().add("pointer");
        this.updateMenuItemContainer();
        this.getChildren().addAll((Node[])new Node[]{this.pointer, this.menuBox});
        this.contextMenu.ownerNodeProperty().addListener(observable -> {
            if (this.contextMenu.getOwnerNode() instanceof TextArea) {
                TextAreaSkin textAreaSkin = (TextAreaSkin)((TextArea)this.contextMenu.getOwnerNode()).getSkin();
                ((TextArea)textAreaSkin.getSkinnable()).getProperties().addListener(new InvalidationListener(){

                    @Override
                    public void invalidated(Observable observable) {
                        EmbeddedTextContextMenuContent.this.requestLayout();
                    }
                });
            } else if (this.contextMenu.getOwnerNode() instanceof TextField) {
                TextFieldSkin textFieldSkin = (TextFieldSkin)((TextField)this.contextMenu.getOwnerNode()).getSkin();
                ((TextField)textFieldSkin.getSkinnable()).getProperties().addListener(new InvalidationListener(){

                    @Override
                    public void invalidated(Observable observable) {
                        EmbeddedTextContextMenuContent.this.requestLayout();
                    }
                });
            }
        });
        this.contextMenu.getItems().addListener(change -> this.updateMenuItemContainer());
    }

    private void updateMenuItemContainer() {
        this.menuBox.getChildren().clear();
        for (MenuItem menuItem : this.contextMenu.getItems()) {
            MenuItemContainer menuItemContainer = new MenuItemContainer(menuItem);
            menuItemContainer.visibleProperty().bind(menuItem.visibleProperty());
            this.menuBox.getChildren().add(menuItemContainer);
        }
    }

    private void hideAllMenus(MenuItem menuItem) {
        Menu menu;
        this.contextMenu.hide();
        while ((menu = menuItem.getParentMenu()) != null) {
            menu.hide();
            menuItem = menu;
        }
        if (menu == null && menuItem.getParentPopup() != null) {
            menuItem.getParentPopup().hide();
        }
    }

    @Override
    protected double computePrefHeight(double d) {
        double d2 = this.snapSizeY(this.pointer.prefHeight(d));
        double d3 = this.snapSizeY(this.menuBox.prefHeight(d));
        return this.snappedTopInset() + d2 + d3 + this.snappedBottomInset();
    }

    @Override
    protected double computePrefWidth(double d) {
        double d2 = this.snapSizeX(this.menuBox.prefWidth(d));
        return this.snappedLeftInset() + d2 + this.snappedRightInset();
    }

    @Override
    protected void layoutChildren() {
        double d = this.snappedLeftInset();
        double d2 = this.snappedRightInset();
        double d3 = this.snappedTopInset();
        double d4 = this.getWidth() - (d + d2);
        double d5 = this.snapSizeX(Utils.boundedSize(this.pointer.prefWidth(-1.0), this.pointer.minWidth(-1.0), this.pointer.maxWidth(-1.0)));
        double d6 = this.snapSizeY(Utils.boundedSize(this.pointer.prefWidth(-1.0), this.pointer.minWidth(-1.0), this.pointer.maxWidth(-1.0)));
        double d7 = this.snapSizeX(Utils.boundedSize(this.menuBox.prefWidth(-1.0), this.menuBox.minWidth(-1.0), this.menuBox.maxWidth(-1.0)));
        double d8 = this.snapSizeY(Utils.boundedSize(this.menuBox.prefWidth(-1.0), this.menuBox.minWidth(-1.0), this.menuBox.maxWidth(-1.0)));
        double d9 = 0.0;
        double d10 = 0.0;
        double d11 = 0.0;
        ObservableMap<Object, Object> observableMap = null;
        if (this.contextMenu.getOwnerNode() instanceof TextArea) {
            observableMap = ((TextArea)this.contextMenu.getOwnerNode()).getProperties();
        } else if (this.contextMenu.getOwnerNode() instanceof TextField) {
            observableMap = ((TextField)this.contextMenu.getOwnerNode()).getProperties();
        }
        if (observableMap != null) {
            if (observableMap.containsKey("CONTEXT_MENU_SCENE_X")) {
                d9 = Double.valueOf(observableMap.get("CONTEXT_MENU_SCENE_X").toString());
                observableMap.remove("CONTEXT_MENU_SCENE_X");
            }
            if (observableMap.containsKey("CONTEXT_MENU_SCREEN_X")) {
                d10 = Double.valueOf(observableMap.get("CONTEXT_MENU_SCREEN_X").toString());
                observableMap.remove("CONTEXT_MENU_SCREEN_X");
            }
        }
        d11 = d9 == 0.0 ? d4 / 2.0 : d10 - d9 - this.contextMenu.getX() + d9;
        this.pointer.resize(d5, d6);
        this.positionInArea(this.pointer, d11, d3, d5, d6, 0.0, HPos.CENTER, VPos.CENTER);
        this.menuBox.resize(d7, d8);
        this.positionInArea(this.menuBox, d, d3 + d6, d7, d8, 0.0, HPos.CENTER, VPos.CENTER);
    }

    class MenuItemContainer
    extends Button {
        private MenuItem item;

        public MenuItemContainer(MenuItem menuItem) {
            this.getStyleClass().addAll((Collection<String>)menuItem.getStyleClass());
            this.setId(menuItem.getId());
            this.item = menuItem;
            this.setText(menuItem.getText());
            this.setStyle(menuItem.getStyle());
            this.textProperty().bind(menuItem.textProperty());
        }

        public MenuItem getItem() {
            return this.item;
        }

        @Override
        public void fire() {
            Event.fireEvent(this.item, new ActionEvent());
            if (!Boolean.TRUE.equals((Boolean)this.item.getProperties().get("refreshMenu"))) {
                EmbeddedTextContextMenuContent.this.hideAllMenus(this.item);
            }
        }
    }
}

