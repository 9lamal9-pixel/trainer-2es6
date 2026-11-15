/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control.behavior;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.inputmap.InputMap;
import com.sun.javafx.scene.control.inputmap.KeyBinding;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;

public class TabPaneBehavior
extends BehaviorBase<TabPane> {
    private final InputMap<TabPane> tabPaneInputMap = this.createInputMap();

    public TabPaneBehavior(TabPane tabPane) {
        super(tabPane);
        this.addDefaultMapping(this.tabPaneInputMap, new InputMap.KeyMapping(KeyCode.UP, keyEvent -> this.selectPreviousTab()), new InputMap.KeyMapping(KeyCode.DOWN, keyEvent -> this.selectNextTab()), new InputMap.KeyMapping(KeyCode.LEFT, keyEvent -> this.rtl(tabPane, this::selectNextTab, this::selectPreviousTab)), new InputMap.KeyMapping(KeyCode.RIGHT, keyEvent -> this.rtl(tabPane, this::selectPreviousTab, this::selectNextTab)), new InputMap.KeyMapping(KeyCode.HOME, keyEvent -> {
            if (((TabPane)this.getNode()).isFocused()) {
                this.moveSelection(-1, 1);
            }
        }), new InputMap.KeyMapping(KeyCode.END, keyEvent -> {
            if (((TabPane)this.getNode()).isFocused()) {
                this.moveSelection(((TabPane)this.getNode()).getTabs().size(), -1);
            }
        }), new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_UP).ctrl(), keyEvent -> this.selectPreviousTab()), new InputMap.KeyMapping(new KeyBinding(KeyCode.PAGE_DOWN).ctrl(), keyEvent -> this.selectNextTab()), new InputMap.KeyMapping(new KeyBinding(KeyCode.TAB).ctrl(), keyEvent -> this.selectNextTab()), new InputMap.KeyMapping(new KeyBinding(KeyCode.TAB).ctrl().shift(), keyEvent -> this.selectPreviousTab()), new InputMap.MouseMapping(MouseEvent.MOUSE_PRESSED, mouseEvent -> ((TabPane)this.getNode()).requestFocus()));
    }

    @Override
    public InputMap<TabPane> getInputMap() {
        return this.tabPaneInputMap;
    }

    public void selectTab(Tab tab) {
        ((TabPane)this.getNode()).getSelectionModel().select(tab);
    }

    public boolean canCloseTab(Tab tab) {
        Event event = new Event(tab, tab, Tab.TAB_CLOSE_REQUEST_EVENT);
        Event.fireEvent(tab, event);
        return !event.isConsumed();
    }

    public void closeTab(Tab tab) {
        TabPane tabPane = (TabPane)this.getNode();
        int n = tabPane.getTabs().indexOf(tab);
        if (n != -1) {
            tabPane.getTabs().remove(n);
        }
        if (tab.getOnClosed() != null) {
            Event.fireEvent(tab, new Event(Tab.CLOSED_EVENT));
        }
    }

    public void selectNextTab() {
        this.moveSelection(1);
    }

    public void selectPreviousTab() {
        this.moveSelection(-1);
    }

    private void moveSelection(int n) {
        this.moveSelection(((TabPane)this.getNode()).getSelectionModel().getSelectedIndex(), n);
    }

    private void moveSelection(int n, int n2) {
        TabPane tabPane = (TabPane)this.getNode();
        if (tabPane.getTabs().isEmpty()) {
            return;
        }
        int n3 = this.findValidTab(n, n2);
        if (n3 > -1) {
            SingleSelectionModel<Tab> singleSelectionModel = tabPane.getSelectionModel();
            ((SelectionModel)singleSelectionModel).select(n3);
        }
        tabPane.requestFocus();
    }

    private int findValidTab(int n, int n2) {
        TabPane tabPane = (TabPane)this.getNode();
        ObservableList<Tab> observableList = tabPane.getTabs();
        int n3 = observableList.size();
        int n4 = n;
        do {
            Tab tab;
            if ((tab = (Tab)observableList.get(n4 = this.nextIndex(n4 + n2, n3))) == null || tab.isDisable()) continue;
            return n4;
        } while (n4 != n);
        return -1;
    }

    private int nextIndex(int n, int n2) {
        int n3 = n % n2;
        if (n3 > 0 && n2 < 0) {
            n3 = n3 + n2 - 0;
        } else if (n3 < 0 && n2 > 0) {
            n3 = n3 + n2 - 0;
        }
        return n3;
    }
}

