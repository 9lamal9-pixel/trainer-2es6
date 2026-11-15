/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control;

import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.control.LambdaMultiplePropertyChangeListenerHandler;
import com.sun.javafx.scene.control.MenuBarButton;
import com.sun.javafx.scene.control.behavior.TwoLevelFocusPopupBehavior;
import com.sun.javafx.scene.control.skin.Utils;
import com.sun.javafx.scene.traversal.Direction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Window;
import javafx.util.Duration;

public class ContextMenuContent
extends Region {
    private static final String ITEM_STYLE_CLASS_LISTENER = "itemStyleClassListener";
    private ContextMenu contextMenu;
    private double maxGraphicWidth = 0.0;
    private double maxRightWidth = 0.0;
    private double maxLabelWidth = 0.0;
    private double maxRowHeight = 0.0;
    private double maxLeftWidth = 0.0;
    private double oldWidth = 0.0;
    private Rectangle clipRect;
    MenuBox itemsContainer;
    private ArrowMenuItem upArrow;
    private ArrowMenuItem downArrow;
    private int currentFocusedIndex = -1;
    private boolean itemsDirty = true;
    private InvalidationListener popupShowingListener = observable -> this.updateItems();
    private WeakInvalidationListener weakPopupShowingListener = new WeakInvalidationListener(this.popupShowingListener);
    private boolean isFirstShow = true;
    private double ty;
    private ChangeListener<Boolean> menuShowingListener = (observableValue, bl, bl2) -> {
        ReadOnlyBooleanProperty readOnlyBooleanProperty = (ReadOnlyBooleanProperty)observableValue;
        Menu menu = (Menu)readOnlyBooleanProperty.getBean();
        if (bl.booleanValue() && !bl2.booleanValue()) {
            this.hideSubmenu();
        } else if (!bl.booleanValue() && bl2.booleanValue()) {
            this.showSubmenu(menu);
        }
    };
    private ListChangeListener<MenuItem> contextMenuItemsListener = change -> {
        while (change.next()) {
            this.updateMenuShowingListeners(change.getRemoved(), false);
            this.updateMenuShowingListeners(change.getAddedSubList(), true);
        }
        this.itemsDirty = true;
        this.updateItems();
    };
    private ChangeListener<Boolean> menuItemVisibleListener = (observableValue, bl, bl2) -> this.requestLayout();
    private Menu openSubmenu;
    private ContextMenu submenu;
    Region selectedBackground;
    private static final PseudoClass SELECTED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("selected");
    private static final PseudoClass DISABLED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("disabled");
    private static final PseudoClass CHECKED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("checked");

    public ContextMenuContent(ContextMenu contextMenu) {
        this.contextMenu = contextMenu;
        this.clipRect = new Rectangle();
        this.clipRect.setSmooth(false);
        this.itemsContainer = new MenuBox();
        this.itemsContainer.setClip(this.clipRect);
        this.upArrow = new ArrowMenuItem(this);
        this.upArrow.setUp(true);
        this.upArrow.setFocusTraversable(false);
        this.downArrow = new ArrowMenuItem(this);
        this.downArrow.setUp(false);
        this.downArrow.setFocusTraversable(false);
        this.getChildren().add(this.itemsContainer);
        this.getChildren().add(this.upArrow);
        this.getChildren().add(this.downArrow);
        this.initialize();
        this.setUpBinds();
        this.updateItems();
        contextMenu.showingProperty().addListener(this.weakPopupShowingListener);
        if (Utils.isTwoLevelFocus()) {
            new TwoLevelFocusPopupBehavior(this);
        }
    }

    public VBox getItemsContainer() {
        return this.itemsContainer;
    }

    int getCurrentFocusIndex() {
        return this.currentFocusedIndex;
    }

    void setCurrentFocusedIndex(int n) {
        if (n < this.itemsContainer.getChildren().size()) {
            this.currentFocusedIndex = n;
        }
    }

    private void updateItems() {
        if (this.itemsDirty) {
            this.updateVisualItems();
            this.itemsDirty = false;
        }
    }

    private void computeVisualMetrics() {
        EventTarget eventTarget;
        this.maxRightWidth = 0.0;
        this.maxLabelWidth = 0.0;
        this.maxRowHeight = 0.0;
        this.maxGraphicWidth = 0.0;
        this.maxLeftWidth = 0.0;
        for (int i = 0; i < this.itemsContainer.getChildren().size(); ++i) {
            Node node = (Node)this.itemsContainer.getChildren().get(i);
            if (!(node instanceof MenuItemContainer) || !((Node)(eventTarget = (MenuItemContainer)this.itemsContainer.getChildren().get(i))).isVisible()) continue;
            double d = -1.0;
            Node node2 = ((MenuItemContainer)eventTarget).left;
            if (node2 != null) {
                d = node2.getContentBias() == Orientation.VERTICAL ? this.snapSizeY(node2.prefHeight(-1.0)) : -1.0;
                this.maxLeftWidth = Math.max(this.maxLeftWidth, this.snapSizeX(node2.prefWidth(d)));
                this.maxRowHeight = Math.max(this.maxRowHeight, node2.prefHeight(-1.0));
            }
            if ((node2 = ((MenuItemContainer)eventTarget).graphic) != null) {
                d = node2.getContentBias() == Orientation.VERTICAL ? this.snapSizeY(node2.prefHeight(-1.0)) : -1.0;
                this.maxGraphicWidth = Math.max(this.maxGraphicWidth, this.snapSizeX(node2.prefWidth(d)));
                this.maxRowHeight = Math.max(this.maxRowHeight, node2.prefHeight(-1.0));
            }
            if ((node2 = ((MenuItemContainer)eventTarget).label) != null) {
                d = node2.getContentBias() == Orientation.VERTICAL ? this.snapSizeY(node2.prefHeight(-1.0)) : -1.0;
                this.maxLabelWidth = Math.max(this.maxLabelWidth, this.snapSizeX(node2.prefWidth(d)));
                this.maxRowHeight = Math.max(this.maxRowHeight, node2.prefHeight(-1.0));
            }
            if ((node2 = ((MenuItemContainer)eventTarget).right) == null) continue;
            d = node2.getContentBias() == Orientation.VERTICAL ? this.snapSizeY(node2.prefHeight(-1.0)) : -1.0;
            this.maxRightWidth = Math.max(this.maxRightWidth, this.snapSizeX(node2.prefWidth(d)));
            this.maxRowHeight = Math.max(this.maxRowHeight, node2.prefHeight(-1.0));
        }
        double d = this.maxRightWidth + this.maxLabelWidth + this.maxGraphicWidth + this.maxLeftWidth;
        eventTarget = this.contextMenu.getOwnerWindow();
        if (eventTarget instanceof ContextMenu && this.contextMenu.getX() < ((Window)eventTarget).getX() && this.oldWidth != d) {
            this.contextMenu.setX(this.contextMenu.getX() + this.oldWidth - d);
        }
        this.oldWidth = d;
    }

    private void updateVisualItems() {
        ObservableList<Node> observableList = this.itemsContainer.getChildren();
        this.disposeVisualItems();
        for (int i = 0; i < this.getItems().size(); ++i) {
            Node node;
            MenuItem menuItem = (MenuItem)this.getItems().get(i);
            if (menuItem instanceof CustomMenuItem && ((CustomMenuItem)menuItem).getContent() == null) continue;
            if (menuItem instanceof SeparatorMenuItem) {
                node = ((CustomMenuItem)menuItem).getContent();
                node.visibleProperty().bind(menuItem.visibleProperty());
                observableList.add(node);
                node.getProperties().put(MenuItem.class, menuItem);
                continue;
            }
            node = new MenuItemContainer(menuItem);
            node.visibleProperty().bind(menuItem.visibleProperty());
            observableList.add(node);
        }
        if (this.getItems().size() > 0) {
            MenuItem menuItem = (MenuItem)this.getItems().get(0);
            this.getProperties().put(Menu.class, menuItem.getParentMenu());
        }
        NodeHelper.reapplyCSS(this);
    }

    private void disposeVisualItems() {
        ObservableList<Node> observableList = this.itemsContainer.getChildren();
        int n = observableList.size();
        for (int i = 0; i < n; ++i) {
            Node node = (Node)observableList.get(i);
            if (!(node instanceof MenuItemContainer)) continue;
            MenuItemContainer menuItemContainer = (MenuItemContainer)node;
            menuItemContainer.visibleProperty().unbind();
            menuItemContainer.dispose();
        }
        observableList.clear();
    }

    public void dispose() {
        this.disposeBinds();
        this.disposeVisualItems();
        this.disposeContextMenu(this.submenu);
        this.submenu = null;
        this.openSubmenu = null;
        this.selectedBackground = null;
        if (this.contextMenu != null) {
            this.contextMenu.getItems().clear();
            this.contextMenu = null;
        }
    }

    public void disposeContextMenu(ContextMenu contextMenu) {
        if (contextMenu == null) {
            return;
        }
        Skin<?> skin = contextMenu.getSkin();
        if (skin == null) {
            return;
        }
        ContextMenuContent contextMenuContent = (ContextMenuContent)skin.getNode();
        if (contextMenuContent == null) {
            return;
        }
        contextMenuContent.dispose();
    }

    @Override
    protected void layoutChildren() {
        double d;
        if (this.itemsContainer.getChildren().size() == 0) {
            return;
        }
        double d2 = this.snappedLeftInset();
        double d3 = this.snappedTopInset();
        double d4 = this.getWidth() - d2 - this.snappedRightInset();
        double d5 = this.getHeight() - d3 - this.snappedBottomInset();
        double d6 = this.snapSizeY(this.getContentHeight());
        this.itemsContainer.resize(d4, d6);
        this.itemsContainer.relocate(d2, d3);
        if (this.isFirstShow && this.ty == 0.0) {
            this.upArrow.setVisible(false);
            this.isFirstShow = false;
        } else {
            this.upArrow.setVisible(this.ty < d3 && this.ty < 0.0);
        }
        this.downArrow.setVisible(this.ty + d6 > d3 + d5);
        this.clipRect.setX(0.0);
        this.clipRect.setY(0.0);
        this.clipRect.setWidth(d4);
        this.clipRect.setHeight(d5);
        if (this.upArrow.isVisible()) {
            d = this.snapSizeY(this.upArrow.prefHeight(-1.0));
            this.clipRect.setHeight(this.snapSizeY(this.clipRect.getHeight() - d));
            this.clipRect.setY(this.snapSizeY(this.clipRect.getY()) + d);
            this.upArrow.resize(this.snapSizeX(this.upArrow.prefWidth(-1.0)), d);
            this.positionInArea(this.upArrow, d2, d3, d4, d, 0.0, HPos.CENTER, VPos.CENTER);
        }
        if (this.downArrow.isVisible()) {
            d = this.snapSizeY(this.downArrow.prefHeight(-1.0));
            this.clipRect.setHeight(this.snapSizeY(this.clipRect.getHeight()) - d);
            this.downArrow.resize(this.snapSizeX(this.downArrow.prefWidth(-1.0)), d);
            this.positionInArea(this.downArrow, d2, d3 + d5 - d, d4, d, 0.0, HPos.CENTER, VPos.CENTER);
        }
    }

    @Override
    protected double computePrefWidth(double d) {
        this.computeVisualMetrics();
        double d2 = 0.0;
        if (this.itemsContainer.getChildren().size() == 0) {
            return 0.0;
        }
        for (Node node : this.itemsContainer.getChildren()) {
            if (!node.isVisible()) continue;
            d2 = Math.max(d2, this.snapSizeX(node.prefWidth(-1.0)));
        }
        return this.snappedLeftInset() + this.snapSizeX(d2) + this.snappedRightInset();
    }

    @Override
    protected double computePrefHeight(double d) {
        if (this.itemsContainer.getChildren().size() == 0) {
            return 0.0;
        }
        double d2 = this.getScreenHeight();
        double d3 = this.getContentHeight();
        double d4 = this.snappedTopInset() + this.snapSizeY(d3) + this.snappedBottomInset();
        double d5 = d2 <= 0.0 ? d4 : Math.min(d4, d2);
        return d5;
    }

    @Override
    protected double computeMinHeight(double d) {
        return 0.0;
    }

    @Override
    protected double computeMaxHeight(double d) {
        return this.getScreenHeight();
    }

    private double getScreenHeight() {
        if (this.contextMenu == null || this.contextMenu.getOwnerWindow() == null || this.contextMenu.getOwnerWindow().getScene() == null) {
            return -1.0;
        }
        return this.snapSizeY(com.sun.javafx.util.Utils.getScreen(this.contextMenu.getOwnerWindow().getScene().getRoot()).getVisualBounds().getHeight());
    }

    private double getContentHeight() {
        double d = 0.0;
        for (Node node : this.itemsContainer.getChildren()) {
            if (!node.isVisible()) continue;
            d += this.snapSizeY(node.prefHeight(-1.0));
        }
        return d;
    }

    private void ensureFocusedMenuItemIsVisible(Node node) {
        if (node == null) {
            return;
        }
        Bounds bounds = node.getBoundsInParent();
        Bounds bounds2 = this.clipRect.getBoundsInParent();
        if (bounds.getMaxY() >= bounds2.getMaxY()) {
            this.scroll(-bounds.getMaxY() + bounds2.getMaxY());
        } else if (bounds.getMinY() <= bounds2.getMinY()) {
            this.scroll(-bounds.getMinY() + bounds2.getMinY());
        }
    }

    protected ObservableList<MenuItem> getItems() {
        return this.contextMenu.getItems();
    }

    private int findFocusedIndex() {
        for (int i = 0; i < this.itemsContainer.getChildren().size(); ++i) {
            Node node = (Node)this.itemsContainer.getChildren().get(i);
            if (!node.isFocused()) continue;
            return i;
        }
        return -1;
    }

    private void initialize() {
        this.contextMenu.addEventHandler(Menu.ON_SHOWN, event -> {
            this.currentFocusedIndex = -1;
            for (Node node : this.itemsContainer.getChildren()) {
                MenuItem menuItem;
                if (!(node instanceof MenuItemContainer) || !"choice-box-menu-item".equals((menuItem = ((MenuItemContainer)node).item).getId()) || !((RadioMenuItem)menuItem).isSelected()) continue;
                node.requestFocus();
                break;
            }
        });
        this.setOnKeyPressed((EventHandler<? super KeyEvent>)new EventHandler<KeyEvent>(){

            @Override
            public void handle(KeyEvent keyEvent) {
                Node node;
                switch (keyEvent.getCode()) {
                    case LEFT: {
                        if (ContextMenuContent.this.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
                            ContextMenuContent.this.processRightKey(keyEvent);
                            break;
                        }
                        ContextMenuContent.this.processLeftKey(keyEvent);
                        break;
                    }
                    case RIGHT: {
                        if (ContextMenuContent.this.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
                            ContextMenuContent.this.processLeftKey(keyEvent);
                            break;
                        }
                        ContextMenuContent.this.processRightKey(keyEvent);
                        break;
                    }
                    case CANCEL: {
                        keyEvent.consume();
                        break;
                    }
                    case ESCAPE: {
                        node = ContextMenuContent.this.contextMenu.getOwnerNode();
                        if (node instanceof MenuBarButton) break;
                        ContextMenuContent.this.contextMenu.hide();
                        keyEvent.consume();
                        break;
                    }
                    case DOWN: {
                        ContextMenuContent.this.move(Direction.NEXT);
                        keyEvent.consume();
                        break;
                    }
                    case UP: {
                        ContextMenuContent.this.move(Direction.PREVIOUS);
                        keyEvent.consume();
                        break;
                    }
                    case SPACE: 
                    case ENTER: {
                        ContextMenuContent.this.selectMenuItem();
                        keyEvent.consume();
                        break;
                    }
                }
                if (!keyEvent.isConsumed()) {
                    node = ContextMenuContent.this.contextMenu.getOwnerNode();
                    if (node instanceof MenuItemContainer) {
                        Parent parent;
                        for (parent = node.getParent(); parent != null && !(parent instanceof ContextMenuContent); parent = parent.getParent()) {
                        }
                        if (parent instanceof ContextMenuContent) {
                            parent.getOnKeyPressed().handle(keyEvent);
                        }
                    } else if (node instanceof MenuBarButton) {
                        // empty if block
                    }
                }
            }
        });
        this.addEventHandler(ScrollEvent.SCROLL, scrollEvent -> {
            double d = scrollEvent.getTextDeltaY();
            double d2 = scrollEvent.getDeltaY();
            if (this.downArrow.isVisible() && (d < 0.0 || d2 < 0.0) || this.upArrow.isVisible() && (d > 0.0 || d2 > 0.0)) {
                switch (scrollEvent.getTextDeltaYUnits()) {
                    case LINES: {
                        int n = this.findFocusedIndex();
                        if (n == -1) {
                            n = 0;
                        }
                        double d3 = ((Node)this.itemsContainer.getChildren().get(n)).prefHeight(-1.0);
                        this.scroll(d * d3);
                        break;
                    }
                    case PAGES: {
                        this.scroll(d * this.itemsContainer.getHeight());
                        break;
                    }
                    case NONE: {
                        this.scroll(d2);
                    }
                }
                scrollEvent.consume();
            }
        });
    }

    private Optional<Node> getFocusedNode() {
        ObservableList<Node> observableList = this.itemsContainer.getChildren();
        boolean bl = this.currentFocusedIndex >= 0 && this.currentFocusedIndex < observableList.size();
        return bl ? Optional.of((Node)observableList.get(this.currentFocusedIndex)) : Optional.empty();
    }

    private void processLeftKey(KeyEvent keyEvent) {
        this.getFocusedNode().ifPresent(node -> {
            Menu menu;
            MenuItem menuItem;
            if (node instanceof MenuItemContainer && (menuItem = ((MenuItemContainer)node).item) instanceof Menu && (menu = (Menu)menuItem) == this.openSubmenu && this.submenu != null && this.submenu.isShowing()) {
                this.hideSubmenu();
                keyEvent.consume();
            }
        });
    }

    private void processRightKey(KeyEvent keyEvent) {
        this.getFocusedNode().ifPresent(node -> {
            MenuItem menuItem;
            if (node instanceof MenuItemContainer && (menuItem = ((MenuItemContainer)node).item) instanceof Menu) {
                Menu menu = (Menu)menuItem;
                if (menu.isDisable()) {
                    return;
                }
                this.selectedBackground = (MenuItemContainer)node;
                if (this.openSubmenu == menu && this.submenu != null && this.submenu.isShowing()) {
                    return;
                }
                this.showMenu(menu);
                keyEvent.consume();
            }
        });
    }

    private void showMenu(Menu menu) {
        menu.show();
        ContextMenuContent contextMenuContent = (ContextMenuContent)this.submenu.getSkin().getNode();
        if (contextMenuContent != null) {
            if (contextMenuContent.itemsContainer.getChildren().size() > 0) {
                ((Node)contextMenuContent.itemsContainer.getChildren().get(0)).requestFocus();
                contextMenuContent.currentFocusedIndex = 0;
            } else {
                contextMenuContent.requestFocus();
            }
        }
    }

    private void selectMenuItem() {
        this.getFocusedNode().ifPresent(node -> {
            if (node instanceof MenuItemContainer) {
                MenuItem menuItem = ((MenuItemContainer)node).item;
                if (menuItem instanceof Menu) {
                    Menu menu = (Menu)menuItem;
                    if (this.openSubmenu != null) {
                        this.hideSubmenu();
                    }
                    if (menu.isDisable()) {
                        return;
                    }
                    this.selectedBackground = (MenuItemContainer)node;
                    menu.show();
                } else {
                    ((MenuItemContainer)node).doSelect();
                }
            }
        });
    }

    private void move(Direction direction) {
        int n = this.currentFocusedIndex != -1 ? this.currentFocusedIndex : this.itemsContainer.getChildren().size();
        this.requestFocusOnIndex(this.findSibling(direction, n));
    }

    private int findSibling(Direction direction, int n) {
        int n2 = this.itemsContainer.getChildren().size();
        int n3 = n;
        do {
            n3 = direction.isForward() && n3 >= n2 - 1 ? 0 : (!direction.isForward() && n3 == 0 ? n2 - 1 : (n3 += direction.isForward() ? 1 : -1));
            Node node = (Node)this.itemsContainer.getChildren().get(n3);
            if (!(node instanceof MenuItemContainer) || !node.isVisible()) continue;
            return n3;
        } while (n3 != n);
        return -1;
    }

    public void requestFocusOnIndex(int n) {
        this.currentFocusedIndex = n;
        Node node = (Node)this.itemsContainer.getChildren().get(n);
        this.selectedBackground = (MenuItemContainer)node;
        node.requestFocus();
        this.ensureFocusedMenuItemIsVisible(node);
    }

    public double getMenuYOffset(int n) {
        double d = 0.0;
        if (this.itemsContainer.getChildren().size() > n) {
            d = this.snappedTopInset();
            Node node = (Node)this.itemsContainer.getChildren().get(n);
            d += node.getLayoutY() + node.prefHeight(-1.0);
        }
        return d;
    }

    private void setUpBinds() {
        this.updateMenuShowingListeners(this.contextMenu.getItems(), true);
        this.contextMenu.getItems().addListener(this.contextMenuItemsListener);
    }

    private void disposeBinds() {
        this.updateMenuShowingListeners(this.contextMenu.getItems(), false);
        this.contextMenu.getItems().removeListener(this.contextMenuItemsListener);
    }

    private void updateMenuShowingListeners(List<? extends MenuItem> list, boolean bl) {
        for (MenuItem menuItem : list) {
            if (menuItem instanceof Menu) {
                Menu menu = (Menu)menuItem;
                if (bl) {
                    menu.showingProperty().addListener(this.menuShowingListener);
                } else {
                    menu.showingProperty().removeListener(this.menuShowingListener);
                }
            }
            if (bl) {
                menuItem.visibleProperty().addListener(this.menuItemVisibleListener);
                continue;
            }
            menuItem.visibleProperty().removeListener(this.menuItemVisibleListener);
        }
    }

    ContextMenu getSubMenu() {
        return this.submenu;
    }

    Menu getOpenSubMenu() {
        return this.openSubmenu;
    }

    private void createSubmenu() {
        if (this.submenu == null) {
            this.submenu = new ContextMenu();
            this.submenu.showingProperty().addListener(new ChangeListener<Boolean>(){

                @Override
                public void changed(ObservableValue<? extends Boolean> observableValue, Boolean bl, Boolean bl2) {
                    if (!ContextMenuContent.this.submenu.isShowing()) {
                        for (Node node : ContextMenuContent.this.itemsContainer.getChildren()) {
                            Menu menu;
                            if (!(node instanceof MenuItemContainer) || !(((MenuItemContainer)node).item instanceof Menu) || !(menu = (Menu)((MenuItemContainer)node).item).isShowing()) continue;
                            menu.hide();
                        }
                    }
                }
            });
        }
    }

    private void showSubmenu(Menu menu) {
        this.openSubmenu = menu;
        this.createSubmenu();
        this.submenu.getItems().setAll((Collection<MenuItem>)menu.getItems());
        this.submenu.show(this.selectedBackground, Side.RIGHT, 0.0, 0.0);
    }

    private void hideSubmenu() {
        if (this.submenu == null) {
            return;
        }
        this.submenu.hide();
        this.openSubmenu = null;
        this.disposeContextMenu(this.submenu);
        this.submenu = null;
        this.getFocusedNode().ifPresent(node -> {
            this.requestFocus();
            node.requestFocus();
        });
    }

    private void hideAllMenus(MenuItem menuItem) {
        Menu menu;
        if (this.contextMenu != null) {
            this.contextMenu.hide();
        }
        while ((menu = menuItem.getParentMenu()) != null) {
            menu.hide();
            menuItem = menu;
        }
        if (menuItem.getParentPopup() != null) {
            menuItem.getParentPopup().hide();
        }
    }

    void scroll(double d) {
        double d2 = this.ty + d;
        if (this.ty == d2) {
            return;
        }
        if (d2 > 0.0) {
            d2 = 0.0;
        }
        if (d < 0.0 && this.getHeight() - d2 > this.itemsContainer.getHeight() - this.downArrow.getHeight()) {
            d2 = this.getHeight() - this.itemsContainer.getHeight() - this.downArrow.getHeight();
        }
        this.ty = d2;
        this.itemsContainer.requestLayout();
    }

    @Override
    public Styleable getStyleableParent() {
        return this.contextMenu;
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return ContextMenuContent.getClassCssMetaData();
    }

    public Label getLabelAt(int n) {
        return ((MenuItemContainer)this.itemsContainer.getChildren().get(n)).getLabel();
    }

    private class MenuLabel
    extends Label {
        public MenuLabel(MenuItem menuItem, MenuItemContainer menuItemContainer) {
            super(menuItem.getText());
            this.setMnemonicParsing(menuItem.isMnemonicParsing());
            this.setLabelFor(menuItemContainer);
        }
    }

    public class MenuItemContainer
    extends Region {
        private final MenuItem item;
        private Node left;
        private Node graphic;
        private Node label;
        private Node right;
        private final LambdaMultiplePropertyChangeListenerHandler listener = new LambdaMultiplePropertyChangeListenerHandler();
        private EventHandler<MouseEvent> mouseEnteredEventHandler;
        private EventHandler<MouseEvent> mouseReleasedEventHandler;
        private EventHandler<ActionEvent> actionEventHandler;
        private EventHandler<MouseEvent> customMenuItemMouseClickedHandler;

        protected Label getLabel() {
            return (Label)this.label;
        }

        public MenuItem getItem() {
            return this.item;
        }

        public MenuItemContainer(MenuItem menuItem) {
            if (menuItem == null) {
                throw new NullPointerException("MenuItem can not be null");
            }
            this.getStyleClass().addAll((Collection<String>)menuItem.getStyleClass());
            this.setId(menuItem.getId());
            this.setFocusTraversable(!(menuItem instanceof CustomMenuItem));
            this.item = menuItem;
            this.createChildren();
            if (menuItem instanceof Menu) {
                ReadOnlyBooleanProperty readOnlyBooleanProperty = ((Menu)menuItem).showingProperty();
                this.listener.registerChangeListener(readOnlyBooleanProperty, observableValue -> this.pseudoClassStateChanged(SELECTED_PSEUDOCLASS_STATE, ((Menu)menuItem).isShowing()));
                this.pseudoClassStateChanged(SELECTED_PSEUDOCLASS_STATE, readOnlyBooleanProperty.get());
                this.setAccessibleRole(AccessibleRole.MENU);
            } else if (menuItem instanceof RadioMenuItem) {
                BooleanProperty booleanProperty = ((RadioMenuItem)menuItem).selectedProperty();
                this.listener.registerChangeListener(booleanProperty, observableValue -> this.pseudoClassStateChanged(CHECKED_PSEUDOCLASS_STATE, ((RadioMenuItem)menuItem).isSelected()));
                this.pseudoClassStateChanged(CHECKED_PSEUDOCLASS_STATE, booleanProperty.get());
                this.setAccessibleRole(AccessibleRole.RADIO_MENU_ITEM);
            } else if (menuItem instanceof CheckMenuItem) {
                BooleanProperty booleanProperty = ((CheckMenuItem)menuItem).selectedProperty();
                this.listener.registerChangeListener(booleanProperty, observableValue -> this.pseudoClassStateChanged(CHECKED_PSEUDOCLASS_STATE, ((CheckMenuItem)menuItem).isSelected()));
                this.pseudoClassStateChanged(CHECKED_PSEUDOCLASS_STATE, booleanProperty.get());
                this.setAccessibleRole(AccessibleRole.CHECK_MENU_ITEM);
            } else {
                this.setAccessibleRole(AccessibleRole.MENU_ITEM);
            }
            this.pseudoClassStateChanged(DISABLED_PSEUDOCLASS_STATE, menuItem.disableProperty().get());
            this.listener.registerChangeListener(menuItem.disableProperty(), observableValue -> this.pseudoClassStateChanged(DISABLED_PSEUDOCLASS_STATE, menuItem.isDisable()));
            this.getProperties().put(MenuItem.class, menuItem);
            this.listener.registerChangeListener(menuItem.graphicProperty(), observableValue -> {
                this.createChildren();
                ContextMenuContent.this.computeVisualMetrics();
            });
            this.actionEventHandler = actionEvent -> {
                if (menuItem instanceof Menu) {
                    Menu menu = (Menu)menuItem;
                    if (ContextMenuContent.this.openSubmenu == menu && ContextMenuContent.this.submenu.isShowing()) {
                        return;
                    }
                    if (ContextMenuContent.this.openSubmenu != null) {
                        ContextMenuContent.this.hideSubmenu();
                    }
                    ContextMenuContent.this.selectedBackground = this;
                    ContextMenuContent.this.showMenu(menu);
                } else {
                    this.doSelect();
                }
            };
            this.addEventHandler(ActionEvent.ACTION, this.actionEventHandler);
        }

        public void dispose() {
            Object object;
            if (this.item instanceof CustomMenuItem && (object = ((CustomMenuItem)this.item).getContent()) != null) {
                ((Node)object).removeEventHandler(MouseEvent.MOUSE_CLICKED, this.customMenuItemMouseClickedHandler);
            }
            this.listener.dispose();
            this.removeEventHandler(ActionEvent.ACTION, this.actionEventHandler);
            if (this.label != null) {
                ((Label)this.label).textProperty().unbind();
                this.label.styleProperty().unbind();
                this.label.idProperty().unbind();
                object = (ListChangeListener)this.item.getProperties().remove(ContextMenuContent.ITEM_STYLE_CLASS_LISTENER);
                if (object != null) {
                    this.item.getStyleClass().removeListener((ListChangeListener<String>)object);
                }
            }
            this.left = null;
            this.graphic = null;
            this.label = null;
            this.right = null;
        }

        private void createChildren() {
            this.getChildren().clear();
            if (this.item instanceof CustomMenuItem) {
                this.createNodeMenuItemChildren((CustomMenuItem)this.item);
                if (this.mouseEnteredEventHandler == null) {
                    this.mouseEnteredEventHandler = mouseEvent -> this.requestFocus();
                } else {
                    this.removeEventHandler(MouseEvent.MOUSE_ENTERED, this.mouseEnteredEventHandler);
                }
                this.addEventHandler(MouseEvent.MOUSE_ENTERED, this.mouseEnteredEventHandler);
            } else {
                Region region;
                Object object;
                Node node = this.getLeftGraphic(this.item);
                if (node != null) {
                    object = new StackPane();
                    ((Node)object).getStyleClass().add("left-container");
                    ((Pane)object).getChildren().add(node);
                    this.left = object;
                    this.getChildren().add(this.left);
                    this.left.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
                }
                if (this.item.getGraphic() != null) {
                    object = this.item.getGraphic();
                    region = new StackPane();
                    region.getStyleClass().add("graphic-container");
                    ((Pane)region).getChildren().add((Node)object);
                    this.graphic = region;
                    this.getChildren().add(this.graphic);
                }
                this.label = new MenuLabel(this.item, this);
                ((Label)this.label).textProperty().bind(this.item.textProperty());
                this.label.styleProperty().bind(this.item.styleProperty());
                this.label.idProperty().bind(this.item.styleProperty());
                object = change -> {
                    while (change.next()) {
                        this.label.getStyleClass().removeAll(change.getRemoved());
                        this.label.getStyleClass().addAll(change.getAddedSubList());
                    }
                };
                this.item.getStyleClass().addListener((ListChangeListener<String>)object);
                this.item.getProperties().put(ContextMenuContent.ITEM_STYLE_CLASS_LISTENER, object);
                this.label.setMouseTransparent(true);
                this.getChildren().add(this.label);
                this.listener.unregisterChangeListeners(this.focusedProperty());
                this.listener.registerChangeListener(this.focusedProperty(), observableValue -> {
                    if (this.isFocused()) {
                        ContextMenuContent.this.currentFocusedIndex = ContextMenuContent.this.itemsContainer.getChildren().indexOf(this);
                    }
                });
                if (this.item instanceof Menu) {
                    region = new Region();
                    region.setMouseTransparent(true);
                    region.getStyleClass().add("arrow");
                    StackPane stackPane = new StackPane();
                    stackPane.setMaxWidth(Math.max(region.prefWidth(-1.0), 10.0));
                    stackPane.setMouseTransparent(true);
                    stackPane.getStyleClass().add("right-container");
                    stackPane.getChildren().add(region);
                    this.right = stackPane;
                    this.getChildren().add(stackPane);
                    if (this.mouseEnteredEventHandler == null) {
                        this.mouseEnteredEventHandler = mouseEvent -> {
                            if (ContextMenuContent.this.openSubmenu != null && this.item != ContextMenuContent.this.openSubmenu) {
                                ContextMenuContent.this.hideSubmenu();
                            }
                            ContextMenuContent.this.selectedBackground = this;
                            this.requestFocus();
                            Menu menu = (Menu)this.item;
                            if (menu.isDisable()) {
                                return;
                            }
                            menu.show();
                        };
                    } else {
                        this.removeEventHandler(MouseEvent.MOUSE_ENTERED, this.mouseEnteredEventHandler);
                    }
                    if (this.mouseReleasedEventHandler == null) {
                        this.mouseReleasedEventHandler = mouseEvent -> this.item.fire();
                    } else {
                        this.removeEventHandler(MouseEvent.MOUSE_RELEASED, this.mouseReleasedEventHandler);
                    }
                    this.addEventHandler(MouseEvent.MOUSE_ENTERED, this.mouseEnteredEventHandler);
                    this.addEventHandler(MouseEvent.MOUSE_RELEASED, this.mouseReleasedEventHandler);
                } else {
                    this.listener.unregisterChangeListeners(this.item.acceleratorProperty());
                    this.updateAccelerator();
                    if (this.mouseEnteredEventHandler == null) {
                        this.mouseEnteredEventHandler = mouseEvent -> {
                            if (ContextMenuContent.this.openSubmenu != null) {
                                ContextMenuContent.this.openSubmenu.hide();
                            }
                            this.requestFocus();
                        };
                    } else {
                        this.removeEventHandler(MouseEvent.MOUSE_ENTERED, this.mouseEnteredEventHandler);
                    }
                    if (this.mouseReleasedEventHandler == null) {
                        this.mouseReleasedEventHandler = mouseEvent -> this.doSelect();
                    } else {
                        this.removeEventHandler(MouseEvent.MOUSE_RELEASED, this.mouseReleasedEventHandler);
                    }
                    this.addEventHandler(MouseEvent.MOUSE_ENTERED, this.mouseEnteredEventHandler);
                    this.addEventHandler(MouseEvent.MOUSE_RELEASED, this.mouseReleasedEventHandler);
                    this.listener.registerChangeListener(this.item.acceleratorProperty(), observableValue -> this.updateAccelerator());
                }
            }
        }

        private void updateAccelerator() {
            if (this.item.getAccelerator() != null) {
                if (this.right != null) {
                    this.getChildren().remove(this.right);
                }
                String string = this.item.getAccelerator().getDisplayText();
                this.right = new Label(string);
                this.right.setStyle(this.item.getStyle());
                this.right.getStyleClass().add("accelerator-text");
                this.getChildren().add(this.right);
            } else {
                this.getChildren().remove(this.right);
            }
        }

        void doSelect() {
            MenuItem menuItem;
            if (this.item.isDisable()) {
                return;
            }
            if (this.item instanceof CheckMenuItem) {
                ((CheckMenuItem)menuItem).setSelected(!((CheckMenuItem)(menuItem = (CheckMenuItem)this.item)).isSelected());
            } else if (this.item instanceof RadioMenuItem) {
                ((RadioMenuItem)menuItem).setSelected(((RadioMenuItem)(menuItem = (RadioMenuItem)this.item)).getToggleGroup() != null ? true : !((RadioMenuItem)menuItem).isSelected());
            }
            this.item.fire();
            if (this.item instanceof CustomMenuItem) {
                menuItem = (CustomMenuItem)this.item;
                if (((CustomMenuItem)menuItem).isHideOnClick()) {
                    ContextMenuContent.this.hideAllMenus(this.item);
                }
            } else {
                ContextMenuContent.this.hideAllMenus(this.item);
            }
        }

        private void createNodeMenuItemChildren(CustomMenuItem customMenuItem) {
            Node node = customMenuItem.getContent();
            this.getChildren().add(node);
            this.customMenuItemMouseClickedHandler = mouseEvent -> {
                if (customMenuItem == null || customMenuItem.isDisable()) {
                    return;
                }
                customMenuItem.fire();
                if (customMenuItem.isHideOnClick()) {
                    ContextMenuContent.this.hideAllMenus(customMenuItem);
                }
            };
            node.addEventHandler(MouseEvent.MOUSE_CLICKED, this.customMenuItemMouseClickedHandler);
        }

        @Override
        protected void layoutChildren() {
            double d;
            double d2 = this.prefHeight(-1.0);
            if (this.left != null) {
                d = this.snappedLeftInset();
                this.left.resize(this.left.prefWidth(-1.0), this.left.prefHeight(-1.0));
                this.positionInArea(this.left, d, 0.0, ContextMenuContent.this.maxLeftWidth, d2, 0.0, HPos.LEFT, VPos.CENTER);
            }
            if (this.graphic != null) {
                d = this.snappedLeftInset() + ContextMenuContent.this.maxLeftWidth;
                this.graphic.resize(this.graphic.prefWidth(-1.0), this.graphic.prefHeight(-1.0));
                this.positionInArea(this.graphic, d, 0.0, ContextMenuContent.this.maxGraphicWidth, d2, 0.0, HPos.LEFT, VPos.CENTER);
            }
            if (this.label != null) {
                d = this.snappedLeftInset() + ContextMenuContent.this.maxLeftWidth + ContextMenuContent.this.maxGraphicWidth;
                this.label.resize(this.label.prefWidth(-1.0), this.label.prefHeight(-1.0));
                this.positionInArea(this.label, d, 0.0, ContextMenuContent.this.maxLabelWidth, d2, 0.0, HPos.LEFT, VPos.CENTER);
            }
            if (this.right != null) {
                d = this.snappedLeftInset() + ContextMenuContent.this.maxLeftWidth + ContextMenuContent.this.maxGraphicWidth + ContextMenuContent.this.maxLabelWidth;
                this.right.resize(this.right.prefWidth(-1.0), this.right.prefHeight(-1.0));
                this.positionInArea(this.right, d, 0.0, ContextMenuContent.this.maxRightWidth, d2, 0.0, HPos.RIGHT, VPos.CENTER);
            }
            if (this.item instanceof CustomMenuItem) {
                Node node = ((CustomMenuItem)this.item).getContent();
                if (this.item instanceof SeparatorMenuItem) {
                    double d3 = this.prefWidth(-1.0) - (this.snappedLeftInset() + ContextMenuContent.this.maxGraphicWidth + this.snappedRightInset());
                    node.resize(d3, node.prefHeight(-1.0));
                    this.positionInArea(node, this.snappedLeftInset() + ContextMenuContent.this.maxGraphicWidth, 0.0, this.prefWidth(-1.0), d2, 0.0, HPos.LEFT, VPos.CENTER);
                } else {
                    node.resize(node.prefWidth(-1.0), node.prefHeight(-1.0));
                    this.positionInArea(node, this.snappedLeftInset(), 0.0, this.getWidth(), d2, 0.0, HPos.LEFT, VPos.CENTER);
                }
            }
        }

        @Override
        protected double computePrefHeight(double d) {
            double d2 = 0.0;
            if (this.item instanceof CustomMenuItem || this.item instanceof SeparatorMenuItem) {
                d2 = this.getChildren().isEmpty() ? 0.0 : ((Node)this.getChildren().get(0)).prefHeight(-1.0);
            } else {
                d2 = Math.max(d2, this.left != null ? this.left.prefHeight(-1.0) : 0.0);
                d2 = Math.max(d2, this.graphic != null ? this.graphic.prefHeight(-1.0) : 0.0);
                d2 = Math.max(d2, this.label != null ? this.label.prefHeight(-1.0) : 0.0);
                d2 = Math.max(d2, this.right != null ? this.right.prefHeight(-1.0) : 0.0);
            }
            return this.snappedTopInset() + d2 + this.snappedBottomInset();
        }

        @Override
        protected double computePrefWidth(double d) {
            double d2 = 0.0;
            if (this.item instanceof CustomMenuItem && !(this.item instanceof SeparatorMenuItem)) {
                d2 = this.snappedLeftInset() + ((CustomMenuItem)this.item).getContent().prefWidth(-1.0) + this.snappedRightInset();
            }
            return Math.max(d2, this.snappedLeftInset() + ContextMenuContent.this.maxLeftWidth + ContextMenuContent.this.maxGraphicWidth + ContextMenuContent.this.maxLabelWidth + ContextMenuContent.this.maxRightWidth + this.snappedRightInset());
        }

        private Node getLeftGraphic(MenuItem menuItem) {
            if (menuItem instanceof RadioMenuItem) {
                Region region = new Region();
                region.getStyleClass().add("radio");
                return region;
            }
            if (menuItem instanceof CheckMenuItem) {
                StackPane stackPane = new StackPane();
                stackPane.getStyleClass().add("check");
                return stackPane;
            }
            return null;
        }

        @Override
        public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
            switch (accessibleAttribute) {
                case SELECTED: {
                    if (this.item instanceof CheckMenuItem) {
                        return ((CheckMenuItem)this.item).isSelected();
                    }
                    if (this.item instanceof RadioMenuItem) {
                        return ((RadioMenuItem)this.item).isSelected();
                    }
                    return false;
                }
                case ACCELERATOR: {
                    return this.item.getAccelerator();
                }
                case TEXT: {
                    String string;
                    Object object;
                    Object object2;
                    Object object3 = "";
                    if (this.graphic != null && (object2 = (String)this.graphic.queryAccessibleAttribute(AccessibleAttribute.TEXT, new Object[0])) != null) {
                        object3 = (String)object3 + (String)object2;
                    }
                    if ((object2 = this.getLabel()) != null && (object = (String)((Control)object2).queryAccessibleAttribute(AccessibleAttribute.TEXT, new Object[0])) != null) {
                        object3 = (String)object3 + (String)object;
                    }
                    if (this.item instanceof CustomMenuItem && (object = ((CustomMenuItem)this.item).getContent()) != null && (string = (String)((Node)object).queryAccessibleAttribute(AccessibleAttribute.TEXT, new Object[0])) != null) {
                        object3 = (String)object3 + string;
                    }
                    return object3;
                }
                case MNEMONIC: {
                    String string;
                    Label label = this.getLabel();
                    if (label != null && (string = (String)label.queryAccessibleAttribute(AccessibleAttribute.MNEMONIC, new Object[0])) != null) {
                        return string;
                    }
                    return null;
                }
                case DISABLED: {
                    return this.item.isDisable();
                }
                case SUBMENU: {
                    ContextMenuContent.this.createSubmenu();
                    if (ContextMenuContent.this.submenu.getSkin() == null) {
                        ContextMenuContent.this.submenu.getStyleableNode().applyCss();
                    }
                    ContextMenuContent contextMenuContent = (ContextMenuContent)ContextMenuContent.this.submenu.getSkin().getNode();
                    return contextMenuContent.itemsContainer;
                }
            }
            return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
        }

        @Override
        public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
            switch (accessibleAction) {
                case SHOW_MENU: {
                    if (!(this.item instanceof Menu)) break;
                    Menu menu = (Menu)this.item;
                    if (menu.isShowing()) {
                        menu.hide();
                        break;
                    }
                    menu.show();
                    break;
                }
                case FIRE: {
                    this.doSelect();
                    break;
                }
                default: {
                    super.executeAccessibleAction(accessibleAction, new Object[0]);
                }
            }
        }
    }

    class ArrowMenuItem
    extends StackPane {
        private StackPane upDownArrow;
        private ContextMenuContent popupMenuContent;
        private boolean up = false;
        private Timeline scrollTimeline;

        public final boolean isUp() {
            return this.up;
        }

        public void setUp(boolean bl) {
            this.up = bl;
            this.upDownArrow.getStyleClass().setAll((String[])new String[]{this.isUp() ? "menu-up-arrow" : "menu-down-arrow"});
        }

        public ArrowMenuItem(ContextMenuContent contextMenuContent2) {
            this.getStyleClass().setAll((String[])new String[]{"scroll-arrow"});
            this.upDownArrow = new StackPane();
            this.popupMenuContent = contextMenuContent2;
            this.upDownArrow.setMouseTransparent(true);
            this.upDownArrow.getStyleClass().setAll((String[])new String[]{this.isUp() ? "menu-up-arrow" : "menu-down-arrow"});
            this.addEventHandler(MouseEvent.MOUSE_ENTERED, mouseEvent -> {
                if (this.scrollTimeline != null && this.scrollTimeline.getStatus() != Animation.Status.STOPPED) {
                    return;
                }
                this.startTimeline();
            });
            this.addEventHandler(MouseEvent.MOUSE_EXITED, mouseEvent -> this.stopTimeline());
            this.setVisible(false);
            this.setManaged(false);
            this.getChildren().add(this.upDownArrow);
        }

        @Override
        protected double computePrefWidth(double d) {
            return ContextMenuContent.this.itemsContainer.getWidth();
        }

        @Override
        protected double computePrefHeight(double d) {
            return this.snappedTopInset() + this.upDownArrow.prefHeight(-1.0) + this.snappedBottomInset();
        }

        @Override
        protected void layoutChildren() {
            double d = this.snapSizeX(this.upDownArrow.prefWidth(-1.0));
            double d2 = this.snapSizeY(this.upDownArrow.prefHeight(-1.0));
            this.upDownArrow.resize(d, d2);
            this.positionInArea(this.upDownArrow, 0.0, 0.0, this.getWidth(), this.getHeight(), 0.0, HPos.CENTER, VPos.CENTER);
        }

        private void adjust() {
            if (this.up) {
                this.popupMenuContent.scroll(12.0);
            } else {
                this.popupMenuContent.scroll(-12.0);
            }
        }

        private void startTimeline() {
            this.scrollTimeline = new Timeline();
            this.scrollTimeline.setCycleCount(-1);
            KeyFrame keyFrame = new KeyFrame(Duration.millis(60.0), actionEvent -> this.adjust(), new KeyValue[0]);
            this.scrollTimeline.getKeyFrames().clear();
            this.scrollTimeline.getKeyFrames().add(keyFrame);
            this.scrollTimeline.play();
        }

        private void stopTimeline() {
            this.scrollTimeline.stop();
            this.scrollTimeline = null;
        }
    }

    class MenuBox
    extends VBox {
        MenuBox() {
            this.setAccessibleRole(AccessibleRole.CONTEXT_MENU);
        }

        @Override
        protected void layoutChildren() {
            double d = ContextMenuContent.this.ty;
            for (Node node : this.getChildren()) {
                if (!node.isVisible()) continue;
                double d2 = this.snapSizeY(node.prefHeight(-1.0));
                node.resize(this.snapSizeX(this.getWidth()), d2);
                node.relocate(this.snappedLeftInset(), d);
                d += d2;
            }
        }

        @Override
        public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
            switch (accessibleAttribute) {
                case VISIBLE: {
                    return ContextMenuContent.this.contextMenu.isShowing();
                }
                case PARENT_MENU: {
                    return ContextMenuContent.this.contextMenu.getOwnerNode();
                }
            }
            return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
        }
    }

    private static class StyleableProperties {
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Region.getClassCssMetaData());
            List<CssMetaData<Styleable, ?>> list = Node.getClassCssMetaData();
            int n = list.size();
            for (int i = 0; i < n; ++i) {
                CssMetaData<Styleable, ?> cssMetaData = list.get(i);
                if (!"effect".equals(cssMetaData.getProperty())) continue;
                arrayList.add(cssMetaData);
                break;
            }
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

