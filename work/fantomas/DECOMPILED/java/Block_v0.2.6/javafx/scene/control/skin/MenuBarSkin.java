/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.FXPermissions;
import com.sun.javafx.menu.MenuBase;
import com.sun.javafx.scene.ParentHelper;
import com.sun.javafx.scene.SceneHelper;
import com.sun.javafx.scene.control.GlobalMenuAdapter;
import com.sun.javafx.scene.control.MenuBarButton;
import com.sun.javafx.scene.control.skin.Utils;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import com.sun.javafx.tk.Toolkit;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.css.converter.SizeConverter;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.skin.MenuButtonSkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Pair;

public class MenuBarSkin
extends SkinBase<MenuBar> {
    private static final ObservableList<Window> stages;
    private final HBox container;
    private Menu openMenu;
    private MenuBarButton openMenuButton;
    private Menu focusedMenu;
    private int focusedMenuIndex = -1;
    private static WeakHashMap<Stage, Reference<MenuBarSkin>> systemMenuMap;
    private static List<MenuBase> wrappedDefaultMenus;
    private static Stage currentMenuBarStage;
    private List<MenuBase> wrappedMenus;
    private WeakEventHandler<KeyEvent> weakSceneKeyEventHandler;
    private WeakEventHandler<MouseEvent> weakSceneMouseEventHandler;
    private WeakEventHandler<KeyEvent> weakSceneAltKeyEventHandler;
    private WeakChangeListener<Boolean> weakWindowFocusListener;
    private WeakChangeListener<Window> weakWindowSceneListener;
    private EventHandler<KeyEvent> keyEventHandler;
    private EventHandler<KeyEvent> altKeyEventHandler;
    private EventHandler<MouseEvent> mouseEventHandler;
    private ChangeListener<Boolean> menuBarFocusedPropertyListener;
    private ChangeListener<Scene> sceneChangeListener;
    private ChangeListener<Boolean> menuVisibilityChangeListener;
    private boolean pendingDismiss = false;
    private boolean altKeyPressed = false;
    private EventHandler<ActionEvent> menuActionEventHandler = actionEvent -> {
        CustomMenuItem customMenuItem;
        if (actionEvent.getSource() instanceof CustomMenuItem && !(customMenuItem = (CustomMenuItem)actionEvent.getSource()).isHideOnClick()) {
            return;
        }
        this.unSelectMenus();
    };
    private ListChangeListener<MenuItem> menuItemListener = change -> {
        while (change.next()) {
            for (MenuItem menuItem : change.getAddedSubList()) {
                this.updateActionListeners(menuItem, true);
            }
            for (MenuItem menuItem : change.getRemoved()) {
                this.updateActionListeners(menuItem, false);
            }
        }
    };
    Runnable firstMenuRunnable = new Runnable(){

        @Override
        public void run() {
            if (MenuBarSkin.this.container.getChildren().size() > 0 && MenuBarSkin.this.container.getChildren().get(0) instanceof MenuButton) {
                if (MenuBarSkin.this.focusedMenuIndex != 0) {
                    MenuBarSkin.this.unSelectMenus();
                    MenuBarSkin.this.menuModeStart(0);
                    MenuBarSkin.this.openMenuButton = (MenuBarButton)MenuBarSkin.this.container.getChildren().get(0);
                    MenuBarSkin.this.openMenuButton.setHover();
                } else {
                    MenuBarSkin.this.unSelectMenus();
                }
            }
        }
    };
    private DoubleProperty spacing;
    private ObjectProperty<Pos> containerAlignment;
    private static final CssMetaData<MenuBar, Number> SPACING;
    private static final CssMetaData<MenuBar, Pos> ALIGNMENT;
    private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

    public MenuBarSkin(MenuBar menuBar) {
        super(menuBar);
        this.container = new HBox();
        this.container.getStyleClass().add("container");
        this.getChildren().add(this.container);
        this.keyEventHandler = keyEvent -> {
            if (this.focusedMenu != null) {
                switch (keyEvent.getCode()) {
                    case LEFT: {
                        boolean bl;
                        boolean bl2 = bl = menuBar.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
                        if (menuBar.getScene().getWindow().isFocused()) {
                            if (this.openMenu != null && !this.openMenu.isShowing()) {
                                if (bl) {
                                    this.moveToMenu(Direction.NEXT, false);
                                } else {
                                    this.moveToMenu(Direction.PREVIOUS, false);
                                }
                                keyEvent.consume();
                                return;
                            }
                            if (bl) {
                                this.moveToMenu(Direction.NEXT, true);
                            } else {
                                this.moveToMenu(Direction.PREVIOUS, true);
                            }
                        }
                        keyEvent.consume();
                        break;
                    }
                    case RIGHT: {
                        boolean bl;
                        boolean bl3 = bl = menuBar.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT;
                        if (menuBar.getScene().getWindow().isFocused()) {
                            if (this.openMenu != null && !this.openMenu.isShowing()) {
                                if (bl) {
                                    this.moveToMenu(Direction.PREVIOUS, false);
                                } else {
                                    this.moveToMenu(Direction.NEXT, false);
                                }
                                keyEvent.consume();
                                return;
                            }
                            if (bl) {
                                this.moveToMenu(Direction.PREVIOUS, true);
                            } else {
                                this.moveToMenu(Direction.NEXT, true);
                            }
                        }
                        keyEvent.consume();
                        break;
                    }
                    case DOWN: {
                        if (!menuBar.getScene().getWindow().isFocused() || this.focusedMenuIndex == -1) break;
                        Menu menu = (Menu)((MenuBar)this.getSkinnable()).getMenus().get(this.focusedMenuIndex);
                        this.showMenu(menu, true);
                        keyEvent.consume();
                        break;
                    }
                    case ESCAPE: {
                        this.unSelectMenus();
                        keyEvent.consume();
                        break;
                    }
                }
            }
        };
        this.menuBarFocusedPropertyListener = (observableValue, bl, bl2) -> {
            if (bl2.booleanValue()) {
                this.unSelectMenus();
                this.menuModeStart(0);
                this.openMenuButton = (MenuBarButton)this.container.getChildren().get(0);
                this.setFocusedMenuIndex(0);
                this.openMenuButton.setHover();
            } else {
                this.unSelectMenus();
            }
        };
        this.weakSceneKeyEventHandler = new WeakEventHandler<KeyEvent>(this.keyEventHandler);
        Utils.executeOnceWhenPropertyIsNonNull(menuBar.sceneProperty(), scene -> scene.addEventFilter(KeyEvent.KEY_PRESSED, this.weakSceneKeyEventHandler));
        this.mouseEventHandler = mouseEvent -> {
            Bounds bounds = this.container.localToScreen(this.container.getLayoutBounds());
            if (bounds == null || !bounds.contains(mouseEvent.getScreenX(), mouseEvent.getScreenY())) {
                this.unSelectMenus();
            }
        };
        this.weakSceneMouseEventHandler = new WeakEventHandler<MouseEvent>(this.mouseEventHandler);
        Utils.executeOnceWhenPropertyIsNonNull(menuBar.sceneProperty(), scene -> scene.addEventFilter(MouseEvent.MOUSE_CLICKED, this.weakSceneMouseEventHandler));
        this.weakWindowFocusListener = new WeakChangeListener<Boolean>((observableValue, bl, bl2) -> {
            if (!bl2.booleanValue()) {
                this.unSelectMenus();
            }
        });
        Utils.executeOnceWhenPropertyIsNonNull(menuBar.sceneProperty(), scene -> {
            if (scene.getWindow() != null) {
                scene.getWindow().focusedProperty().addListener(this.weakWindowFocusListener);
            } else {
                ChangeListener<Window> changeListener = (observableValue, window, window2) -> {
                    if (window != null) {
                        window.focusedProperty().removeListener(this.weakWindowFocusListener);
                    }
                    if (window2 != null) {
                        window2.focusedProperty().addListener(this.weakWindowFocusListener);
                    }
                };
                this.weakWindowSceneListener = new WeakChangeListener<Window>(changeListener);
                scene.windowProperty().addListener(this.weakWindowSceneListener);
            }
        });
        this.menuVisibilityChangeListener = (observableValue, bl, bl2) -> this.rebuildUI();
        this.rebuildUI();
        menuBar.getMenus().addListener(change -> this.rebuildUI());
        if (Toolkit.getToolkit().getSystemMenu().isSupported()) {
            menuBar.useSystemMenuBarProperty().addListener(observable -> this.rebuildUI());
        }
        KeyCombination keyCombination = com.sun.javafx.util.Utils.isMac() ? KeyCombination.keyCombination("ctrl+F10") : KeyCombination.keyCombination("F10");
        this.altKeyEventHandler = keyEvent -> {
            if (keyEvent.getEventType() == KeyEvent.KEY_PRESSED) {
                this.altKeyPressed = false;
                if (keyEvent.getCode() == KeyCode.ALT && !keyEvent.isConsumed()) {
                    if (this.focusedMenuIndex == -1) {
                        this.altKeyPressed = true;
                    }
                    this.unSelectMenus();
                }
            } else if (keyEvent.getEventType() == KeyEvent.KEY_RELEASED) {
                if (this.altKeyPressed && keyEvent.getCode() == KeyCode.ALT && !keyEvent.isConsumed()) {
                    this.firstMenuRunnable.run();
                }
                this.altKeyPressed = false;
            }
        };
        this.weakSceneAltKeyEventHandler = new WeakEventHandler<KeyEvent>(this.altKeyEventHandler);
        Utils.executeOnceWhenPropertyIsNonNull(menuBar.sceneProperty(), scene -> {
            scene.getAccelerators().put(keyCombination, this.firstMenuRunnable);
            scene.addEventHandler(KeyEvent.ANY, this.weakSceneAltKeyEventHandler);
        });
        ParentTraversalEngine parentTraversalEngine = new ParentTraversalEngine((Parent)((Object)this.getSkinnable()));
        parentTraversalEngine.addTraverseListener((node, bounds) -> {
            if (this.openMenu != null) {
                this.openMenu.hide();
            }
            this.setFocusedMenuIndex(0);
        });
        ParentHelper.setTraversalEngine((Parent)((Object)this.getSkinnable()), parentTraversalEngine);
        menuBar.sceneProperty().addListener((observableValue, scene, scene2) -> {
            if (scene != null) {
                if (this.weakSceneKeyEventHandler != null) {
                    scene.removeEventFilter(KeyEvent.KEY_PRESSED, this.weakSceneKeyEventHandler);
                }
                if (this.weakSceneMouseEventHandler != null) {
                    scene.removeEventFilter(MouseEvent.MOUSE_CLICKED, this.weakSceneMouseEventHandler);
                }
                if (this.weakSceneAltKeyEventHandler != null) {
                    scene.removeEventHandler(KeyEvent.ANY, this.weakSceneAltKeyEventHandler);
                }
            }
            if (scene != null) {
                scene.getAccelerators().remove(keyCombination);
            }
            if (scene2 != null) {
                scene2.getAccelerators().put(keyCombination, this.firstMenuRunnable);
            }
        });
    }

    private void showMenu(Menu menu) {
        this.showMenu(menu, false);
    }

    private void showMenu(Menu menu, boolean bl) {
        if (this.openMenu == menu) {
            return;
        }
        if (this.openMenu != null) {
            this.openMenu.hide();
        }
        this.openMenu = menu;
        if (!menu.isShowing() && !this.isMenuEmpty(menu)) {
            MenuButton menuButton;
            Skin<?> skin;
            if (bl && (skin = (menuButton = this.getNodeForMenu(this.focusedMenuIndex)).getSkin()) instanceof MenuButtonSkinBase) {
                ((MenuButtonSkinBase)skin).requestFocusOnFirstMenuItem();
            }
            this.openMenu.show();
        }
    }

    private void setFocusedMenuIndex(int n) {
        this.focusedMenuIndex = n;
        Menu menu = this.focusedMenu = n == -1 ? null : (Menu)((MenuBar)this.getSkinnable()).getMenus().get(n);
        if (this.focusedMenu != null && this.focusedMenuIndex != -1) {
            this.openMenuButton = (MenuBarButton)this.container.getChildren().get(this.focusedMenuIndex);
            this.openMenuButton.setHover();
        }
    }

    public static void setDefaultSystemMenuBar(MenuBar menuBar) {
        if (Toolkit.getToolkit().getSystemMenu().isSupported()) {
            wrappedDefaultMenus.clear();
            for (Menu menu : menuBar.getMenus()) {
                wrappedDefaultMenus.add(GlobalMenuAdapter.adapt(menu));
            }
            menuBar.getMenus().addListener(change -> {
                wrappedDefaultMenus.clear();
                for (Menu menu : menuBar.getMenus()) {
                    wrappedDefaultMenus.add(GlobalMenuAdapter.adapt(menu));
                }
            });
        }
    }

    private static MenuBarSkin getMenuBarSkin(Stage stage) {
        if (systemMenuMap == null) {
            return null;
        }
        Reference<MenuBarSkin> reference = systemMenuMap.get(stage);
        return reference == null ? null : reference.get();
    }

    private static void setSystemMenu(Stage stage) {
        List<MenuBase> list;
        if (stage != null && stage.isFocused()) {
            while (stage != null && stage.getOwner() instanceof Stage && ((list = MenuBarSkin.getMenuBarSkin(stage)) == null || ((MenuBarSkin)((Object)list)).wrappedMenus == null)) {
                stage = (Stage)stage.getOwner();
            }
        } else {
            stage = null;
        }
        if (stage != currentMenuBarStage) {
            MenuBarSkin menuBarSkin;
            list = null;
            if (stage != null && (menuBarSkin = MenuBarSkin.getMenuBarSkin(stage)) != null) {
                list = menuBarSkin.wrappedMenus;
            }
            if (list == null) {
                list = wrappedDefaultMenus;
            }
            Toolkit.getToolkit().getSystemMenu().setMenus(list);
            currentMenuBarStage = stage;
        }
    }

    private static void initSystemMenuBar() {
        systemMenuMap = new WeakHashMap();
        InvalidationListener invalidationListener = observable -> MenuBarSkin.setSystemMenu((Stage)((ReadOnlyProperty)observable).getBean());
        for (Window window : stages) {
            window.focusedProperty().addListener(invalidationListener);
        }
        stages.addListener(change -> {
            while (change.next()) {
                for (Window window : change.getRemoved()) {
                    window.focusedProperty().removeListener(invalidationListener);
                }
                for (Window window : change.getAddedSubList()) {
                    window.focusedProperty().addListener(invalidationListener);
                    MenuBarSkin.setSystemMenu((Stage)window);
                }
            }
        });
    }

    public final void setSpacing(double d) {
        this.spacingProperty().set(this.snapSpaceX(d));
    }

    public final double getSpacing() {
        return this.spacing == null ? 0.0 : this.snapSpaceX(this.spacing.get());
    }

    public final DoubleProperty spacingProperty() {
        if (this.spacing == null) {
            this.spacing = new StyleableDoubleProperty(){

                @Override
                protected void invalidated() {
                    double d = this.get();
                    MenuBarSkin.this.container.setSpacing(d);
                }

                @Override
                public Object getBean() {
                    return MenuBarSkin.this;
                }

                @Override
                public String getName() {
                    return "spacing";
                }

                @Override
                public CssMetaData<MenuBar, Number> getCssMetaData() {
                    return SPACING;
                }
            };
        }
        return this.spacing;
    }

    public final void setContainerAlignment(Pos pos) {
        this.containerAlignmentProperty().set(pos);
    }

    public final Pos getContainerAlignment() {
        return this.containerAlignment == null ? Pos.TOP_LEFT : (Pos)((Object)this.containerAlignment.get());
    }

    public final ObjectProperty<Pos> containerAlignmentProperty() {
        if (this.containerAlignment == null) {
            this.containerAlignment = new StyleableObjectProperty<Pos>(Pos.TOP_LEFT){

                @Override
                public void invalidated() {
                    Pos pos = (Pos)((Object)this.get());
                    MenuBarSkin.this.container.setAlignment(pos);
                }

                @Override
                public Object getBean() {
                    return MenuBarSkin.this;
                }

                @Override
                public String getName() {
                    return "containerAlignment";
                }

                @Override
                public CssMetaData<MenuBar, Pos> getCssMetaData() {
                    return ALIGNMENT;
                }
            };
        }
        return this.containerAlignment;
    }

    @Override
    public void dispose() {
        this.cleanUpSystemMenu();
        super.dispose();
    }

    @Override
    protected double snappedTopInset() {
        return this.container.getChildren().isEmpty() ? 0.0 : super.snappedTopInset();
    }

    @Override
    protected double snappedBottomInset() {
        return this.container.getChildren().isEmpty() ? 0.0 : super.snappedBottomInset();
    }

    @Override
    protected double snappedLeftInset() {
        return this.container.getChildren().isEmpty() ? 0.0 : super.snappedLeftInset();
    }

    @Override
    protected double snappedRightInset() {
        return this.container.getChildren().isEmpty() ? 0.0 : super.snappedRightInset();
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        this.container.resizeRelocate(d, d2, d3, d4);
    }

    @Override
    protected double computeMinWidth(double d, double d2, double d3, double d4, double d5) {
        return this.container.minWidth(d) + this.snappedLeftInset() + this.snappedRightInset();
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        return this.container.prefWidth(d) + this.snappedLeftInset() + this.snappedRightInset();
    }

    @Override
    protected double computeMinHeight(double d, double d2, double d3, double d4, double d5) {
        return this.container.minHeight(d) + this.snappedTopInset() + this.snappedBottomInset();
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        return this.container.prefHeight(d) + this.snappedTopInset() + this.snappedBottomInset();
    }

    @Override
    protected double computeMaxHeight(double d, double d2, double d3, double d4, double d5) {
        return ((MenuBar)this.getSkinnable()).prefHeight(-1.0);
    }

    MenuButton getNodeForMenu(int n) {
        if (n < this.container.getChildren().size()) {
            return (MenuBarButton)this.container.getChildren().get(n);
        }
        return null;
    }

    int getFocusedMenuIndex() {
        return this.focusedMenuIndex;
    }

    private boolean menusContainCustomMenuItem() {
        for (Menu menu : ((MenuBar)this.getSkinnable()).getMenus()) {
            if (!this.menuContainsCustomMenuItem(menu)) continue;
            System.err.println("Warning: MenuBar ignored property useSystemMenuBar because menus contain CustomMenuItem");
            return true;
        }
        return false;
    }

    private boolean menuContainsCustomMenuItem(Menu menu) {
        for (MenuItem menuItem : menu.getItems()) {
            if (menuItem instanceof CustomMenuItem && !(menuItem instanceof SeparatorMenuItem)) {
                return true;
            }
            if (!(menuItem instanceof Menu) || !this.menuContainsCustomMenuItem((Menu)menuItem)) continue;
            return true;
        }
        return false;
    }

    private int getMenuBarButtonIndex(MenuBarButton menuBarButton) {
        for (int i = 0; i < this.container.getChildren().size(); ++i) {
            MenuBarButton menuBarButton2 = (MenuBarButton)this.container.getChildren().get(i);
            if (menuBarButton != menuBarButton2) continue;
            return i;
        }
        return -1;
    }

    private void updateActionListeners(MenuItem menuItem, boolean bl) {
        if (menuItem instanceof Menu) {
            Menu menu = (Menu)menuItem;
            if (bl) {
                menu.getItems().addListener(this.menuItemListener);
            } else {
                menu.getItems().removeListener(this.menuItemListener);
            }
            for (MenuItem menuItem2 : menu.getItems()) {
                this.updateActionListeners(menuItem2, bl);
            }
        } else if (bl) {
            menuItem.addEventHandler(ActionEvent.ACTION, this.menuActionEventHandler);
        } else {
            menuItem.removeEventHandler(ActionEvent.ACTION, this.menuActionEventHandler);
        }
    }

    private void rebuildUI() {
        MenuBarButton menuBarButton;
        ((MenuBar)this.getSkinnable()).focusedProperty().removeListener(this.menuBarFocusedPropertyListener);
        for (Menu object : ((MenuBar)this.getSkinnable()).getMenus()) {
            this.updateActionListeners(object, false);
            object.visibleProperty().removeListener(this.menuVisibilityChangeListener);
        }
        for (Node node : this.container.getChildren()) {
            menuBarButton = (MenuBarButton)node;
            menuBarButton.hide();
            menuBarButton.menu.showingProperty().removeListener(menuBarButton.menuListener);
            menuBarButton.disableProperty().unbind();
            menuBarButton.textProperty().unbind();
            menuBarButton.graphicProperty().unbind();
            menuBarButton.styleProperty().unbind();
            menuBarButton.dispose();
            menuBarButton.setSkin(null);
            menuBarButton = null;
        }
        this.container.getChildren().clear();
        if (Toolkit.getToolkit().getSystemMenu().isSupported()) {
            MenuBarSkin menuBarSkin;
            Scene scene3 = ((MenuBar)this.getSkinnable()).getScene();
            if (scene3 != null) {
                if (this.sceneChangeListener == null) {
                    this.sceneChangeListener = (observableValue, scene, scene2) -> {
                        Stage stage;
                        if (scene != null && scene.getWindow() instanceof Stage) {
                            stage = (Stage)scene.getWindow();
                            MenuBarSkin menuBarSkin = MenuBarSkin.getMenuBarSkin(stage);
                            if (menuBarSkin == this) {
                                menuBarSkin.wrappedMenus = null;
                                systemMenuMap.remove(stage);
                                if (currentMenuBarStage == stage) {
                                    currentMenuBarStage = null;
                                    MenuBarSkin.setSystemMenu(stage);
                                }
                            } else if (menuBarSkin != null && menuBarSkin.getSkinnable() != null && ((MenuBar)menuBarSkin.getSkinnable()).isUseSystemMenuBar()) {
                                ((MenuBar)menuBarSkin.getSkinnable()).setUseSystemMenuBar(false);
                            }
                        }
                        if (scene2 != null && ((MenuBar)this.getSkinnable()).isUseSystemMenuBar() && !this.menusContainCustomMenuItem() && scene2.getWindow() instanceof Stage) {
                            stage = (Stage)scene2.getWindow();
                            if (systemMenuMap == null) {
                                MenuBarSkin.initSystemMenuBar();
                            }
                            this.wrappedMenus = new ArrayList<MenuBase>();
                            systemMenuMap.put(stage, new WeakReference<MenuBarSkin>(this));
                            for (Menu menu : ((MenuBar)this.getSkinnable()).getMenus()) {
                                this.wrappedMenus.add(GlobalMenuAdapter.adapt(menu));
                            }
                            currentMenuBarStage = null;
                            MenuBarSkin.setSystemMenu(stage);
                            ((MenuBar)this.getSkinnable()).requestLayout();
                            Platform.runLater(() -> ((MenuBar)this.getSkinnable()).requestLayout());
                        }
                    };
                    ((MenuBar)this.getSkinnable()).sceneProperty().addListener(this.sceneChangeListener);
                }
                this.sceneChangeListener.changed(((MenuBar)this.getSkinnable()).sceneProperty(), scene3, scene3);
                if (currentMenuBarStage != null ? MenuBarSkin.getMenuBarSkin(currentMenuBarStage) == this : ((MenuBar)this.getSkinnable()).isUseSystemMenuBar()) {
                    return;
                }
            } else if (currentMenuBarStage != null && (menuBarSkin = MenuBarSkin.getMenuBarSkin(currentMenuBarStage)) == this) {
                MenuBarSkin.setSystemMenu(null);
            }
        }
        ((MenuBar)this.getSkinnable()).focusedProperty().addListener(this.menuBarFocusedPropertyListener);
        for (Menu menu : ((MenuBar)this.getSkinnable()).getMenus()) {
            menu.visibleProperty().addListener(this.menuVisibilityChangeListener);
            if (!menu.isVisible()) continue;
            menuBarButton = new MenuBarButton(this, menu);
            menuBarButton.setFocusTraversable(false);
            menuBarButton.getStyleClass().add("menu");
            menuBarButton.setStyle(menu.getStyle());
            menuBarButton.getItems().setAll((Collection<MenuItem>)menu.getItems());
            this.container.getChildren().add(menuBarButton);
            menuBarButton.menuListener = (observableValue, bl, bl2) -> {
                if (menu.isShowing()) {
                    menuBarButton.show();
                    this.menuModeStart(this.container.getChildren().indexOf(menuBarButton));
                } else {
                    menuBarButton.hide();
                }
            };
            menuBarButton.menu = menu;
            menu.showingProperty().addListener(menuBarButton.menuListener);
            menuBarButton.disableProperty().bindBidirectional(menu.disableProperty());
            menuBarButton.textProperty().bind(menu.textProperty());
            menuBarButton.graphicProperty().bind(menu.graphicProperty());
            menuBarButton.styleProperty().bind(menu.styleProperty());
            menuBarButton.getProperties().addListener(change -> {
                if (change.wasAdded() && "autoHide".equals(change.getKey())) {
                    menuBarButton.getProperties().remove("autoHide");
                    menu.hide();
                }
            });
            menuBarButton.showingProperty().addListener((observableValue, bl, bl2) -> {
                if (bl2.booleanValue()) {
                    if (this.openMenuButton == null && this.focusedMenuIndex != -1) {
                        this.openMenuButton = (MenuBarButton)this.container.getChildren().get(this.focusedMenuIndex);
                    }
                    if (this.openMenuButton != null && this.openMenuButton != menuBarButton) {
                        this.openMenuButton.clearHover();
                    }
                    this.openMenuButton = menuBarButton;
                    this.showMenu(menu);
                } else {
                    this.openMenu = null;
                    this.openMenuButton = null;
                }
            });
            menuBarButton.setOnMousePressed(mouseEvent -> {
                this.pendingDismiss = menuBarButton.isShowing();
                if (menuBarButton.getScene().getWindow().isFocused()) {
                    this.showMenu(menu);
                    this.menuModeStart(this.getMenuBarButtonIndex(menuBarButton));
                }
            });
            menuBarButton.setOnMouseReleased(mouseEvent -> {
                if (menuBarButton.getScene().getWindow().isFocused() && this.pendingDismiss) {
                    this.resetOpenMenu();
                }
                this.pendingDismiss = false;
            });
            menuBarButton.setOnMouseEntered(mouseEvent -> {
                if (menuBarButton.getScene() != null && menuBarButton.getScene().getWindow() != null && menuBarButton.getScene().getWindow().isFocused()) {
                    if (this.openMenuButton != null && this.openMenuButton != menuBarButton) {
                        this.openMenuButton.clearHover();
                        this.openMenuButton = null;
                        this.openMenuButton = menuBarButton;
                    }
                    this.updateFocusedIndex();
                    if (this.openMenu != null && this.openMenu != menu) {
                        this.showMenu(menu);
                    }
                }
            });
            this.updateActionListeners(menu, true);
        }
        ((MenuBar)this.getSkinnable()).requestLayout();
    }

    private void cleanUpSystemMenu() {
        if (this.sceneChangeListener != null && this.getSkinnable() != null) {
            ((MenuBar)this.getSkinnable()).sceneProperty().removeListener(this.sceneChangeListener);
            this.sceneChangeListener = null;
        }
        if (currentMenuBarStage != null && MenuBarSkin.getMenuBarSkin(currentMenuBarStage) == this) {
            MenuBarSkin.setSystemMenu(null);
        }
        if (systemMenuMap != null) {
            Iterator<Map.Entry<Stage, Reference<MenuBarSkin>>> iterator = systemMenuMap.entrySet().iterator();
            while (iterator.hasNext()) {
                MenuBarSkin menuBarSkin;
                Map.Entry<Stage, Reference<MenuBarSkin>> entry = iterator.next();
                Reference<MenuBarSkin> reference = entry.getValue();
                MenuBarSkin menuBarSkin2 = menuBarSkin = reference != null ? reference.get() : null;
                if (menuBarSkin != null && menuBarSkin != this) continue;
                iterator.remove();
            }
        }
    }

    private boolean isMenuEmpty(Menu menu) {
        boolean bl = true;
        if (menu != null) {
            for (MenuItem menuItem : menu.getItems()) {
                if (menuItem == null || !menuItem.isVisible()) continue;
                bl = false;
            }
        }
        return bl;
    }

    private void resetOpenMenu() {
        if (this.openMenu != null) {
            this.openMenu.hide();
            this.openMenu = null;
        }
    }

    private void unSelectMenus() {
        this.clearMenuButtonHover();
        if (this.focusedMenuIndex == -1) {
            return;
        }
        if (this.openMenu != null) {
            this.openMenu.hide();
            this.openMenu = null;
        }
        if (this.openMenuButton != null) {
            this.openMenuButton.clearHover();
            this.openMenuButton = null;
        }
        this.menuModeEnd();
    }

    private void menuModeStart(int n) {
        if (this.focusedMenuIndex == -1) {
            SceneHelper.getSceneAccessor().setTransientFocusContainer(((MenuBar)this.getSkinnable()).getScene(), (Node)((Object)this.getSkinnable()));
        }
        this.setFocusedMenuIndex(n);
    }

    private void menuModeEnd() {
        if (this.focusedMenuIndex != -1) {
            SceneHelper.getSceneAccessor().setTransientFocusContainer(((MenuBar)this.getSkinnable()).getScene(), null);
            ((MenuBar)this.getSkinnable()).notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
        }
        this.setFocusedMenuIndex(-1);
    }

    private void moveToMenu(Direction direction, boolean bl) {
        boolean bl2 = bl && this.focusedMenu.isShowing();
        this.findSibling(direction, this.focusedMenuIndex).ifPresent(pair -> {
            this.setFocusedMenuIndex((Integer)pair.getValue());
            if (bl2) {
                this.showMenu((Menu)pair.getKey(), false);
            }
        });
    }

    private Optional<Pair<Menu, Integer>> findSibling(Direction direction, int n) {
        if (n == -1) {
            return Optional.empty();
        }
        int n2 = ((MenuBar)this.getSkinnable()).getMenus().size();
        int n3 = 0;
        int n4 = 0;
        while (n3 < n2) {
            ++n3;
            n4 = (n + (direction.isForward() ? 1 : -1)) % n2;
            if (n4 == -1) {
                n4 = n2 - 1;
            }
            if (!((Menu)((MenuBar)this.getSkinnable()).getMenus().get(n4)).isDisable()) break;
            n = n4;
        }
        this.clearMenuButtonHover();
        return Optional.of(new Pair<Menu, Integer>((Menu)((MenuBar)this.getSkinnable()).getMenus().get(n4), n4));
    }

    private void updateFocusedIndex() {
        int n = 0;
        for (Node node : this.container.getChildren()) {
            if (node.isHover()) {
                this.setFocusedMenuIndex(n);
                return;
            }
            ++n;
        }
        this.menuModeEnd();
    }

    private void clearMenuButtonHover() {
        for (Node node : this.container.getChildren()) {
            if (!node.isHover()) continue;
            ((MenuBarButton)node).clearHover();
            ((MenuBarButton)node).disarm();
            return;
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return MenuBarSkin.getClassCssMetaData();
    }

    @Override
    protected Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case FOCUS_NODE: {
                return this.openMenuButton;
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    static {
        Object object = window -> window instanceof Stage;
        Object object2 = AccessController.doPrivileged(() -> Window.getWindows(), null, FXPermissions.ACCESS_WINDOW_LIST_PERMISSION);
        stages = object2.filtered(object);
        wrappedDefaultMenus = new ArrayList<MenuBase>();
        SPACING = new CssMetaData<MenuBar, Number>("-fx-spacing", SizeConverter.getInstance(), (Number)0.0){

            @Override
            public boolean isSettable(MenuBar menuBar) {
                MenuBarSkin menuBarSkin = (MenuBarSkin)menuBar.getSkin();
                return menuBarSkin.spacing == null || !menuBarSkin.spacing.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(MenuBar menuBar) {
                MenuBarSkin menuBarSkin = (MenuBarSkin)menuBar.getSkin();
                return (StyleableProperty)((Object)menuBarSkin.spacingProperty());
            }
        };
        ALIGNMENT = new CssMetaData<MenuBar, Pos>("-fx-alignment", new EnumConverter<Pos>(Pos.class), Pos.TOP_LEFT){

            @Override
            public boolean isSettable(MenuBar menuBar) {
                MenuBarSkin menuBarSkin = (MenuBarSkin)menuBar.getSkin();
                return menuBarSkin.containerAlignment == null || !menuBarSkin.containerAlignment.isBound();
            }

            @Override
            public StyleableProperty<Pos> getStyleableProperty(MenuBar menuBar) {
                MenuBarSkin menuBarSkin = (MenuBarSkin)menuBar.getSkin();
                return (StyleableProperty)((Object)menuBarSkin.containerAlignmentProperty());
            }
        };
        object = new ArrayList(SkinBase.getClassCssMetaData());
        object2 = ALIGNMENT.getProperty();
        int n = object.size();
        for (int i = 0; i < n; ++i) {
            CssMetaData cssMetaData = (CssMetaData)object.get(i);
            if (!((String)object2).equals(cssMetaData.getProperty())) continue;
            object.remove(cssMetaData);
        }
        object.add(SPACING);
        object.add(ALIGNMENT);
        STYLEABLES = Collections.unmodifiableList(object);
    }
}

