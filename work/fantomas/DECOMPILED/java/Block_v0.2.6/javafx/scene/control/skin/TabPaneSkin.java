/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.LambdaMultiplePropertyChangeListenerHandler;
import com.sun.javafx.scene.control.Properties;
import com.sun.javafx.scene.control.TabObservableList;
import com.sun.javafx.scene.control.behavior.TabPaneBehavior;
import com.sun.javafx.scene.control.skin.resources.ControlResources;
import com.sun.javafx.util.Utils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.SwipeEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class TabPaneSkin
extends SkinBase<TabPane> {
    static int CLOSE_BTN_SIZE = 16;
    private static final double ANIMATION_SPEED = 150.0;
    private static final int SPACER = 10;
    private TabHeaderArea tabHeaderArea;
    private ObservableList<TabContentRegion> tabContentRegions;
    private Rectangle clipRect;
    private Rectangle tabHeaderAreaClipRect;
    private Tab selectedTab;
    private boolean isSelectingTab;
    private final TabPaneBehavior behavior;
    private ObjectProperty<TabAnimation> openTabAnimation = new StyleableObjectProperty<TabAnimation>(TabAnimation.GROW){

        @Override
        public CssMetaData<TabPane, TabAnimation> getCssMetaData() {
            return StyleableProperties.OPEN_TAB_ANIMATION;
        }

        @Override
        public Object getBean() {
            return TabPaneSkin.this;
        }

        @Override
        public String getName() {
            return "openTabAnimation";
        }
    };
    private ObjectProperty<TabAnimation> closeTabAnimation = new StyleableObjectProperty<TabAnimation>(TabAnimation.GROW){

        @Override
        public CssMetaData<TabPane, TabAnimation> getCssMetaData() {
            return StyleableProperties.CLOSE_TAB_ANIMATION;
        }

        @Override
        public Object getBean() {
            return TabPaneSkin.this;
        }

        @Override
        public String getName() {
            return "closeTabAnimation";
        }
    };
    private static final PseudoClass SELECTED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("selected");
    private static final PseudoClass TOP_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("top");
    private static final PseudoClass BOTTOM_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("bottom");
    private static final PseudoClass LEFT_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("left");
    private static final PseudoClass RIGHT_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("right");
    private static final PseudoClass DISABLED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("disabled");
    private EventHandler<MouseEvent> headerDraggedHandler = this::handleHeaderDragged;
    private EventHandler<MouseEvent> headerMousePressedHandler = this::handleHeaderMousePressed;
    private EventHandler<MouseEvent> headerMouseReleasedHandler = this::handleHeaderMouseReleased;
    private int dragTabHeaderIndex;
    private TabHeaderSkin dragTabHeader;
    private TabHeaderSkin dropTabHeader;
    private StackPane headersRegion;
    private DragState dragState;
    private final int MIN_TO_MAX = 1;
    private final int MAX_TO_MIN = -1;
    private int xLayoutDirection;
    private double dragEventPrevLoc;
    private int prevDragDirection = 1;
    private final double DRAG_DIST_THRESHOLD = 0.75;
    private final double ANIM_DURATION = 120.0;
    private TabHeaderSkin dropAnimHeader;
    private Tab swapTab;
    private double dropHeaderSourceX;
    private double dropHeaderTransitionX;
    private final Animation dropHeaderAnim = new Transition(){
        {
            this.setInterpolator(Interpolator.EASE_BOTH);
            this.setCycleDuration(Duration.millis(120.0));
            this.setOnFinished(actionEvent -> TabPaneSkin.this.completeHeaderReordering());
        }

        @Override
        protected void interpolate(double d) {
            TabPaneSkin.this.dropAnimHeader.setLayoutX(TabPaneSkin.this.dropHeaderSourceX + TabPaneSkin.this.dropHeaderTransitionX * d);
        }
    };
    private double dragHeaderStartX;
    private double dragHeaderDestX;
    private double dragHeaderSourceX;
    private double dragHeaderTransitionX;
    private final Animation dragHeaderAnim = new Transition(){
        {
            this.setInterpolator(Interpolator.EASE_OUT);
            this.setCycleDuration(Duration.millis(120.0));
            this.setOnFinished(actionEvent -> TabPaneSkin.this.resetDrag());
        }

        @Override
        protected void interpolate(double d) {
            TabPaneSkin.this.dragTabHeader.setLayoutX(TabPaneSkin.this.dragHeaderSourceX + TabPaneSkin.this.dragHeaderTransitionX * d);
        }
    };
    private ListChangeListener childListener = new ListChangeListener<Node>(){

        @Override
        public void onChanged(ListChangeListener.Change<? extends Node> change) {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Node node : change.getAddedSubList()) {
                        TabPaneSkin.this.addReorderListeners(node);
                    }
                }
                if (!change.wasRemoved()) continue;
                for (Node node : change.getRemoved()) {
                    TabPaneSkin.this.removeReorderListeners(node);
                }
            }
        }
    };

    public TabPaneSkin(TabPane tabPane) {
        super(tabPane);
        this.behavior = new TabPaneBehavior(tabPane);
        this.clipRect = new Rectangle(tabPane.getWidth(), tabPane.getHeight());
        ((TabPane)this.getSkinnable()).setClip(this.clipRect);
        this.tabContentRegions = FXCollections.observableArrayList();
        for (Tab tab : ((TabPane)this.getSkinnable()).getTabs()) {
            this.addTabContent(tab);
        }
        this.tabHeaderAreaClipRect = new Rectangle();
        this.tabHeaderArea = new TabHeaderArea();
        this.tabHeaderArea.setClip(this.tabHeaderAreaClipRect);
        this.getChildren().add(this.tabHeaderArea);
        if (((TabPane)this.getSkinnable()).getTabs().size() == 0) {
            this.tabHeaderArea.setVisible(false);
        }
        this.initializeTabListener();
        this.registerChangeListener(tabPane.getSelectionModel().selectedItemProperty(), observableValue -> {
            this.isSelectingTab = true;
            this.selectedTab = (Tab)((TabPane)this.getSkinnable()).getSelectionModel().getSelectedItem();
            ((TabPane)this.getSkinnable()).requestLayout();
        });
        this.registerChangeListener(tabPane.sideProperty(), observableValue -> this.updateTabPosition());
        this.registerChangeListener(tabPane.widthProperty(), observableValue -> this.clipRect.setWidth(((TabPane)this.getSkinnable()).getWidth()));
        this.registerChangeListener(tabPane.heightProperty(), observableValue -> this.clipRect.setHeight(((TabPane)this.getSkinnable()).getHeight()));
        this.selectedTab = (Tab)((TabPane)this.getSkinnable()).getSelectionModel().getSelectedItem();
        if (this.selectedTab == null && ((TabPane)this.getSkinnable()).getSelectionModel().getSelectedIndex() != -1) {
            ((TabPane)this.getSkinnable()).getSelectionModel().select(((TabPane)this.getSkinnable()).getSelectionModel().getSelectedIndex());
            this.selectedTab = (Tab)((TabPane)this.getSkinnable()).getSelectionModel().getSelectedItem();
        }
        if (this.selectedTab == null) {
            ((TabPane)this.getSkinnable()).getSelectionModel().selectFirst();
        }
        this.selectedTab = (Tab)((TabPane)this.getSkinnable()).getSelectionModel().getSelectedItem();
        this.isSelectingTab = false;
        this.initializeSwipeHandlers();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        double d6 = 0.0;
        for (TabContentRegion tabContentRegion : this.tabContentRegions) {
            d6 = Math.max(d6, this.snapSizeX(tabContentRegion.prefWidth(-1.0)));
        }
        boolean bl = this.isHorizontal();
        double d7 = bl ? this.snapSizeX(this.tabHeaderArea.prefWidth(-1.0)) : this.snapSizeY(this.tabHeaderArea.prefHeight(-1.0));
        double d8 = bl ? Math.max(d6, d7) : d6 + d7;
        return this.snapSizeX(d8) + d3 + d5;
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        double d6 = 0.0;
        for (TabContentRegion tabContentRegion : this.tabContentRegions) {
            d6 = Math.max(d6, this.snapSizeY(tabContentRegion.prefHeight(-1.0)));
        }
        boolean bl = this.isHorizontal();
        double d7 = bl ? this.snapSizeY(this.tabHeaderArea.prefHeight(-1.0)) : this.snapSizeX(this.tabHeaderArea.prefWidth(-1.0));
        double d8 = bl ? d6 + this.snapSizeY(d7) : Math.max(d6, d7);
        return this.snapSizeY(d8) + d2 + d4;
    }

    @Override
    public double computeBaselineOffset(double d, double d2, double d3, double d4) {
        Side side = ((TabPane)this.getSkinnable()).getSide();
        if (side == Side.TOP) {
            return this.tabHeaderArea.getBaselineOffset() + d;
        }
        return 0.0;
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        TabPane tabPane = (TabPane)this.getSkinnable();
        Side side = tabPane.getSide();
        double d5 = side.isHorizontal() ? this.snapSizeY(this.tabHeaderArea.prefHeight(-1.0)) : this.snapSizeX(this.tabHeaderArea.prefHeight(-1.0));
        double d6 = side.equals((Object)Side.RIGHT) ? d + d3 - d5 : d;
        double d7 = side.equals((Object)Side.BOTTOM) ? d2 + d4 - d5 : d2;
        double d8 = this.snappedLeftInset();
        double d9 = this.snappedTopInset();
        if (side == Side.TOP) {
            this.tabHeaderArea.resize(d3, d5);
            this.tabHeaderArea.relocate(d6, d7);
            this.tabHeaderArea.getTransforms().clear();
            this.tabHeaderArea.getTransforms().add(new Rotate(TabPaneSkin.getRotation(Side.TOP)));
        } else if (side == Side.BOTTOM) {
            this.tabHeaderArea.resize(d3, d5);
            this.tabHeaderArea.relocate(d3 + d8, d7 - d5);
            this.tabHeaderArea.getTransforms().clear();
            this.tabHeaderArea.getTransforms().add(new Rotate(TabPaneSkin.getRotation(Side.BOTTOM), 0.0, d5));
        } else if (side == Side.LEFT) {
            this.tabHeaderArea.resize(d4, d5);
            this.tabHeaderArea.relocate(d6 + d5, d4 - d5 + d9);
            this.tabHeaderArea.getTransforms().clear();
            this.tabHeaderArea.getTransforms().add(new Rotate(TabPaneSkin.getRotation(Side.LEFT), 0.0, d5));
        } else if (side == Side.RIGHT) {
            this.tabHeaderArea.resize(d4, d5);
            this.tabHeaderArea.relocate(d6, d2 - d5);
            this.tabHeaderArea.getTransforms().clear();
            this.tabHeaderArea.getTransforms().add(new Rotate(TabPaneSkin.getRotation(Side.RIGHT), 0.0, d5));
        }
        this.tabHeaderAreaClipRect.setX(0.0);
        this.tabHeaderAreaClipRect.setY(0.0);
        if (this.isHorizontal()) {
            this.tabHeaderAreaClipRect.setWidth(d3);
        } else {
            this.tabHeaderAreaClipRect.setWidth(d4);
        }
        this.tabHeaderAreaClipRect.setHeight(d5);
        double d10 = 0.0;
        double d11 = 0.0;
        if (side == Side.TOP) {
            d10 = d;
            d11 = d2 + d5;
            if (this.isFloatingStyleClass()) {
                d11 -= 1.0;
            }
        } else if (side == Side.BOTTOM) {
            d10 = d;
            d11 = d2 + d9;
            if (this.isFloatingStyleClass()) {
                d11 = 1.0 + d9;
            }
        } else if (side == Side.LEFT) {
            d10 = d + d5;
            d11 = d2;
            if (this.isFloatingStyleClass()) {
                d10 -= 1.0;
            }
        } else if (side == Side.RIGHT) {
            d10 = d + d8;
            d11 = d2;
            if (this.isFloatingStyleClass()) {
                d10 = 1.0 + d8;
            }
        }
        double d12 = d3 - (this.isHorizontal() ? 0.0 : d5);
        double d13 = d4 - (this.isHorizontal() ? d5 : 0.0);
        int n = this.tabContentRegions.size();
        for (int i = 0; i < n; ++i) {
            TabContentRegion tabContentRegion = (TabContentRegion)this.tabContentRegions.get(i);
            tabContentRegion.setAlignment(Pos.TOP_LEFT);
            if (tabContentRegion.getClip() != null) {
                ((Rectangle)tabContentRegion.getClip()).setWidth(d12);
                ((Rectangle)tabContentRegion.getClip()).setHeight(d13);
            }
            tabContentRegion.resize(d12, d13);
            tabContentRegion.relocate(d10, d11);
        }
    }

    private static int getRotation(Side side) {
        switch (side) {
            case TOP: {
                return 0;
            }
            case BOTTOM: {
                return 180;
            }
            case LEFT: {
                return -90;
            }
            case RIGHT: {
                return 90;
            }
        }
        return 0;
    }

    private static Node clone(Node node) {
        if (node == null) {
            return null;
        }
        if (node instanceof ImageView) {
            ImageView imageView = (ImageView)node;
            ImageView imageView2 = new ImageView();
            imageView2.imageProperty().bind(imageView.imageProperty());
            return imageView2;
        }
        if (node instanceof Label) {
            Label label = (Label)node;
            Label label2 = new Label(label.getText(), TabPaneSkin.clone(label.getGraphic()));
            label2.textProperty().bind(label.textProperty());
            return label2;
        }
        return null;
    }

    private void removeTabs(List<? extends Tab> list) {
        for (Tab tab : list) {
            this.stopCurrentAnimation(tab);
            TabHeaderSkin tabHeaderSkin = this.tabHeaderArea.getTabHeaderSkin(tab);
            if (tabHeaderSkin == null) continue;
            tabHeaderSkin.isClosing = true;
            tabHeaderSkin.removeListeners(tab);
            this.removeTabContent(tab);
            EventHandler<ActionEvent> eventHandler = actionEvent -> {
                tabHeaderSkin.animationState = TabAnimationState.NONE;
                this.tabHeaderArea.removeTab(tab);
                this.tabHeaderArea.requestLayout();
                if (((TabPane)this.getSkinnable()).getTabs().isEmpty()) {
                    this.tabHeaderArea.setVisible(false);
                }
            };
            if (this.closeTabAnimation.get() == TabAnimation.GROW) {
                tabHeaderSkin.animationState = TabAnimationState.HIDING;
                Timeline timeline = tabHeaderSkin.currentAnimation = this.createTimeline(tabHeaderSkin, Duration.millis(150.0), 0.0, eventHandler);
                timeline.play();
                continue;
            }
            eventHandler.handle(null);
        }
    }

    private void stopCurrentAnimation(Tab tab) {
        Timeline timeline;
        TabHeaderSkin tabHeaderSkin = this.tabHeaderArea.getTabHeaderSkin(tab);
        if (tabHeaderSkin != null && (timeline = tabHeaderSkin.currentAnimation) != null && timeline.getStatus() == Animation.Status.RUNNING) {
            timeline.getOnFinished().handle(null);
            timeline.stop();
            tabHeaderSkin.currentAnimation = null;
        }
    }

    private void addTabs(List<? extends Tab> list, int n) {
        int n2 = 0;
        ArrayList<Node> arrayList = new ArrayList<Node>(this.tabHeaderArea.headersRegion.getChildren());
        for (Node eventTarget : arrayList) {
            TabHeaderSkin tabHeaderSkin = (TabHeaderSkin)eventTarget;
            if (tabHeaderSkin.animationState != TabAnimationState.HIDING) continue;
            this.stopCurrentAnimation(tabHeaderSkin.tab);
        }
        for (Tab tab : list) {
            this.stopCurrentAnimation(tab);
            if (!this.tabHeaderArea.isVisible()) {
                this.tabHeaderArea.setVisible(true);
            }
            int n3 = n + n2++;
            this.tabHeaderArea.addTab(tab, n3);
            this.addTabContent(tab);
            TabHeaderSkin tabHeaderSkin = this.tabHeaderArea.getTabHeaderSkin(tab);
            if (tabHeaderSkin == null) continue;
            if (this.openTabAnimation.get() == TabAnimation.GROW) {
                tabHeaderSkin.animationState = TabAnimationState.SHOWING;
                tabHeaderSkin.animationTransition.setValue(0.0);
                tabHeaderSkin.setVisible(true);
                tabHeaderSkin.currentAnimation = this.createTimeline(tabHeaderSkin, Duration.millis(150.0), 1.0, actionEvent -> {
                    tabHeaderSkin.animationState = TabAnimationState.NONE;
                    tabHeaderSkin.setVisible(true);
                    tabHeaderSkin.inner.requestLayout();
                });
                tabHeaderSkin.currentAnimation.play();
                continue;
            }
            tabHeaderSkin.setVisible(true);
            tabHeaderSkin.inner.requestLayout();
        }
    }

    private void initializeTabListener() {
        ((TabPane)this.getSkinnable()).getTabs().addListener(change -> {
            EventTarget eventTarget;
            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList();
            int n = -1;
            while (change.next()) {
                if (change.wasPermutated() && this.dragState != DragState.REORDER) {
                    TabPane tabPane = (TabPane)this.getSkinnable();
                    ObservableList<Tab> object = tabPane.getTabs();
                    int n2 = change.getTo() - change.getFrom();
                    eventTarget = (Tab)tabPane.getSelectionModel().getSelectedItem();
                    ArrayList<Tab> arrayList3 = new ArrayList<Tab>(n2);
                    ((TabPane)this.getSkinnable()).getSelectionModel().clearSelection();
                    TabAnimation tabAnimation = (TabAnimation)((Object)((Object)this.openTabAnimation.get()));
                    TabAnimation tabAnimation2 = (TabAnimation)((Object)((Object)this.closeTabAnimation.get()));
                    this.openTabAnimation.set(TabAnimation.NONE);
                    this.closeTabAnimation.set(TabAnimation.NONE);
                    for (int i = change.getFrom(); i < change.getTo(); ++i) {
                        arrayList3.add((Tab)object.get(i));
                    }
                    this.removeTabs(arrayList3);
                    this.addTabs(arrayList3, change.getFrom());
                    this.openTabAnimation.set(tabAnimation);
                    this.closeTabAnimation.set(tabAnimation2);
                    ((TabPane)this.getSkinnable()).getSelectionModel().select((Tab)eventTarget);
                }
                if (change.wasRemoved()) {
                    arrayList.addAll(change.getRemoved());
                }
                if (!change.wasAdded()) continue;
                arrayList2.addAll(change.getAddedSubList());
                n = change.getFrom();
            }
            arrayList.removeAll(arrayList2);
            this.removeTabs(arrayList);
            if (!arrayList2.isEmpty()) {
                for (TabContentRegion tabContentRegion : this.tabContentRegions) {
                    Tab tab = tabContentRegion.getTab();
                    eventTarget = this.tabHeaderArea.getTabHeaderSkin(tab);
                    if (((TabHeaderSkin)eventTarget).isClosing || !arrayList2.contains(tabContentRegion.getTab())) continue;
                    arrayList2.remove(tabContentRegion.getTab());
                }
                this.addTabs(arrayList2, n == -1 ? this.tabContentRegions.size() : n);
            }
            ((TabPane)this.getSkinnable()).requestLayout();
        });
    }

    private void addTabContent(Tab tab) {
        TabContentRegion tabContentRegion = new TabContentRegion(tab);
        tabContentRegion.setClip(new Rectangle());
        this.tabContentRegions.add(tabContentRegion);
        this.getChildren().add(0, tabContentRegion);
    }

    private void removeTabContent(Tab tab) {
        for (TabContentRegion tabContentRegion : this.tabContentRegions) {
            if (!tabContentRegion.getTab().equals(tab)) continue;
            tabContentRegion.removeListeners(tab);
            this.getChildren().remove(tabContentRegion);
            this.tabContentRegions.remove(tabContentRegion);
            break;
        }
    }

    private void updateTabPosition() {
        this.tabHeaderArea.setScrollOffset(0.0);
        ((TabPane)this.getSkinnable()).applyCss();
        ((TabPane)this.getSkinnable()).requestLayout();
    }

    private Timeline createTimeline(TabHeaderSkin tabHeaderSkin, Duration duration, double d, EventHandler<ActionEvent> eventHandler) {
        Timeline timeline = new Timeline();
        timeline.setCycleCount(1);
        KeyValue keyValue = new KeyValue(tabHeaderSkin.animationTransition, d, Interpolator.LINEAR);
        timeline.getKeyFrames().clear();
        timeline.getKeyFrames().add(new KeyFrame(duration, keyValue));
        timeline.setOnFinished(eventHandler);
        return timeline;
    }

    private boolean isHorizontal() {
        Side side = ((TabPane)this.getSkinnable()).getSide();
        return Side.TOP.equals((Object)side) || Side.BOTTOM.equals((Object)side);
    }

    private void initializeSwipeHandlers() {
        if (Properties.IS_TOUCH_SUPPORTED) {
            ((TabPane)this.getSkinnable()).addEventHandler(SwipeEvent.SWIPE_LEFT, swipeEvent -> this.behavior.selectNextTab());
            ((TabPane)this.getSkinnable()).addEventHandler(SwipeEvent.SWIPE_RIGHT, swipeEvent -> this.behavior.selectPreviousTab());
        }
    }

    private boolean isFloatingStyleClass() {
        return ((TabPane)this.getSkinnable()).getStyleClass().contains("floating");
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return TabPaneSkin.getClassCssMetaData();
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case FOCUS_ITEM: {
                return this.tabHeaderArea.getTabHeaderSkin(this.selectedTab);
            }
            case ITEM_COUNT: {
                return this.tabHeaderArea.headersRegion.getChildren().size();
            }
            case ITEM_AT_INDEX: {
                Integer n = (Integer)objectArray[0];
                if (n == null) {
                    return null;
                }
                return this.tabHeaderArea.headersRegion.getChildren().get(n);
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    private void addReorderListeners(Node node) {
        node.addEventHandler(MouseEvent.MOUSE_PRESSED, this.headerMousePressedHandler);
        node.addEventHandler(MouseEvent.MOUSE_RELEASED, this.headerMouseReleasedHandler);
        node.addEventHandler(MouseEvent.MOUSE_DRAGGED, this.headerDraggedHandler);
    }

    private void removeReorderListeners(Node node) {
        node.removeEventHandler(MouseEvent.MOUSE_PRESSED, this.headerMousePressedHandler);
        node.removeEventHandler(MouseEvent.MOUSE_RELEASED, this.headerMouseReleasedHandler);
        node.removeEventHandler(MouseEvent.MOUSE_DRAGGED, this.headerDraggedHandler);
    }

    private void updateListeners() {
        if (((TabPane)this.getSkinnable()).getTabDragPolicy() == TabPane.TabDragPolicy.FIXED || ((TabPane)this.getSkinnable()).getTabDragPolicy() == null) {
            for (Node node : this.headersRegion.getChildren()) {
                this.removeReorderListeners(node);
            }
            this.headersRegion.getChildren().removeListener(this.childListener);
        } else if (((TabPane)this.getSkinnable()).getTabDragPolicy() == TabPane.TabDragPolicy.REORDER) {
            for (Node node : this.headersRegion.getChildren()) {
                this.addReorderListeners(node);
            }
            this.headersRegion.getChildren().addListener(this.childListener);
        }
    }

    private void setupReordering(StackPane stackPane) {
        this.dragState = DragState.NONE;
        this.headersRegion = stackPane;
        this.updateListeners();
        ((TabPane)this.getSkinnable()).tabDragPolicyProperty().addListener((observableValue, tabDragPolicy, tabDragPolicy2) -> {
            if (tabDragPolicy != tabDragPolicy2) {
                this.updateListeners();
            }
        });
    }

    private void handleHeaderMousePressed(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals((Object)MouseButton.PRIMARY)) {
            ((StackPane)mouseEvent.getSource()).setMouseTransparent(true);
            this.startDrag(mouseEvent);
        }
    }

    private void handleHeaderMouseReleased(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals((Object)MouseButton.PRIMARY)) {
            ((StackPane)mouseEvent.getSource()).setMouseTransparent(false);
            this.stopDrag();
            mouseEvent.consume();
        }
    }

    private void handleHeaderDragged(MouseEvent mouseEvent) {
        if (mouseEvent.getButton().equals((Object)MouseButton.PRIMARY)) {
            this.perfromDrag(mouseEvent);
        }
    }

    private double getDragDelta(double d, double d2) {
        if (((TabPane)this.getSkinnable()).getSide().equals((Object)Side.TOP) || ((TabPane)this.getSkinnable()).getSide().equals((Object)Side.RIGHT)) {
            return d - d2;
        }
        return d2 - d;
    }

    private int deriveTabHeaderLayoutXDirection() {
        if (((TabPane)this.getSkinnable()).getSide().equals((Object)Side.TOP) || ((TabPane)this.getSkinnable()).getSide().equals((Object)Side.RIGHT)) {
            return 1;
        }
        return -1;
    }

    private void perfromDrag(MouseEvent mouseEvent) {
        double d;
        double d2 = this.getHeaderRegionLocalX(mouseEvent);
        double d3 = this.getDragDelta(d2, this.dragEventPrevLoc);
        int n = d3 > 0.0 ? 1 : -1;
        if (this.prevDragDirection != n) {
            this.stopAnim(this.dropHeaderAnim);
            this.prevDragDirection = n;
        }
        if ((d = this.dragTabHeader.getLayoutX() + (double)this.xLayoutDirection * d3) >= 0.0 && d + this.dragTabHeader.getWidth() <= this.headersRegion.getWidth()) {
            this.dragState = DragState.REORDER;
            this.dragTabHeader.setLayoutX(d);
            Bounds bounds = this.dragTabHeader.getBoundsInParent();
            if (n == 1) {
                for (int i = this.dragTabHeaderIndex + 1; i < this.headersRegion.getChildren().size(); ++i) {
                    Bounds bounds2;
                    this.dropTabHeader = (TabHeaderSkin)this.headersRegion.getChildren().get(i);
                    if (this.dropAnimHeader == this.dropTabHeader) continue;
                    double d4 = this.xLayoutDirection == 1 ? bounds.getMaxX() - bounds2.getMinX() : bounds2.getMaxX() - bounds.getMinX();
                    if (d4 > (bounds2 = this.dropTabHeader.getBoundsInParent()).getWidth() * 0.75) {
                        this.stopAnim(this.dropHeaderAnim);
                        this.dropHeaderTransitionX = (double)this.xLayoutDirection * -bounds.getWidth();
                        this.dragHeaderDestX = this.xLayoutDirection == 1 ? bounds2.getMaxX() - bounds.getWidth() : bounds2.getMinX();
                        this.startHeaderReorderingAnim();
                        continue;
                    }
                    break;
                }
            } else {
                for (int i = this.dragTabHeaderIndex - 1; i >= 0; --i) {
                    Bounds bounds3;
                    this.dropTabHeader = (TabHeaderSkin)this.headersRegion.getChildren().get(i);
                    if (this.dropAnimHeader == this.dropTabHeader) continue;
                    double d5 = this.xLayoutDirection == 1 ? bounds3.getMaxX() - bounds.getMinX() : bounds.getMaxX() - bounds3.getMinX();
                    if (d5 > (bounds3 = this.dropTabHeader.getBoundsInParent()).getWidth() * 0.75) {
                        this.stopAnim(this.dropHeaderAnim);
                        this.dropHeaderTransitionX = (double)this.xLayoutDirection * bounds.getWidth();
                        this.dragHeaderDestX = this.xLayoutDirection == 1 ? bounds3.getMinX() : bounds3.getMaxX() - bounds.getWidth();
                        this.startHeaderReorderingAnim();
                        continue;
                    }
                    break;
                }
            }
        }
        this.dragEventPrevLoc = d2;
        mouseEvent.consume();
    }

    private void startDrag(MouseEvent mouseEvent) {
        this.stopAnim(this.dropHeaderAnim);
        this.stopAnim(this.dragHeaderAnim);
        this.dragTabHeader = (TabHeaderSkin)mouseEvent.getSource();
        if (this.dragTabHeader != null) {
            this.dragState = DragState.START;
            this.swapTab = null;
            this.xLayoutDirection = this.deriveTabHeaderLayoutXDirection();
            this.dragEventPrevLoc = this.getHeaderRegionLocalX(mouseEvent);
            this.dragTabHeaderIndex = this.headersRegion.getChildren().indexOf(this.dragTabHeader);
            this.dragTabHeader.setViewOrder(0.0);
            this.dragHeaderStartX = this.dragHeaderDestX = this.dragTabHeader.getLayoutX();
        }
    }

    private double getHeaderRegionLocalX(MouseEvent mouseEvent) {
        Point2D point2D = this.headersRegion.sceneToLocal(mouseEvent.getSceneX(), mouseEvent.getSceneY());
        return point2D.getX();
    }

    private void stopDrag() {
        if (this.dragState == DragState.START) {
            this.resetDrag();
            return;
        }
        this.dragHeaderSourceX = this.dragTabHeader.getLayoutX();
        this.dragHeaderTransitionX = this.dragHeaderDestX - this.dragHeaderSourceX;
        this.dragHeaderAnim.playFromStart();
        if (this.dragHeaderStartX != this.dragHeaderDestX) {
            ((TabObservableList)((TabPane)this.getSkinnable()).getTabs()).reorder(this.dragTabHeader.tab, this.swapTab);
            this.swapTab = null;
        }
    }

    private void resetDrag() {
        this.dragState = DragState.NONE;
        this.dragTabHeader.setViewOrder(1.0);
        this.dragTabHeader = null;
        this.dropTabHeader = null;
        this.headersRegion.requestLayout();
    }

    private void startHeaderReorderingAnim() {
        this.dropAnimHeader = this.dropTabHeader;
        this.swapTab = this.dropAnimHeader.tab;
        this.dropHeaderSourceX = this.dropAnimHeader.getLayoutX();
        this.dropHeaderAnim.playFromStart();
    }

    private void completeHeaderReordering() {
        if (this.dropAnimHeader != null) {
            this.headersRegion.getChildren().remove(this.dropAnimHeader);
            this.headersRegion.getChildren().add(this.dragTabHeaderIndex, this.dropAnimHeader);
            this.dropAnimHeader = null;
            this.headersRegion.requestLayout();
            this.dragTabHeaderIndex = this.headersRegion.getChildren().indexOf(this.dragTabHeader);
        }
    }

    private void stopAnim(Animation animation) {
        if (animation.getStatus() == Animation.Status.RUNNING) {
            animation.getOnFinished().handle(null);
            animation.stop();
        }
    }

    ContextMenu test_getTabsMenu() {
        return this.tabHeaderArea.controlButtons.popup;
    }

    private static enum DragState {
        NONE,
        START,
        REORDER;

    }

    static class TabMenuItem
    extends RadioMenuItem {
        Tab tab;
        private InvalidationListener disableListener = new InvalidationListener(){

            @Override
            public void invalidated(Observable observable) {
                this.setDisable(tab.isDisable());
            }
        };
        private WeakInvalidationListener weakDisableListener = new WeakInvalidationListener(this.disableListener);

        public TabMenuItem(Tab tab) {
            super(tab.getText(), TabPaneSkin.clone(tab.getGraphic()));
            this.tab = tab;
            this.setDisable(tab.isDisable());
            tab.disableProperty().addListener(this.weakDisableListener);
            this.textProperty().bind(tab.textProperty());
        }

        public Tab getTab() {
            return this.tab;
        }

        public void dispose() {
            this.textProperty().unbind();
            this.tab.disableProperty().removeListener(this.weakDisableListener);
            this.tab = null;
        }
    }

    class TabControlButtons
    extends StackPane {
        private StackPane inner;
        private StackPane downArrow;
        private Pane downArrowBtn;
        private boolean showControlButtons;
        private ContextMenu popup;
        private boolean showTabsMenu = false;

        public TabControlButtons() {
            this.getStyleClass().setAll((String[])new String[]{"control-buttons-tab"});
            TabPane tabPane = (TabPane)TabPaneSkin.this.getSkinnable();
            this.downArrowBtn = new Pane();
            this.downArrowBtn.getStyleClass().setAll((String[])new String[]{"tab-down-button"});
            this.downArrowBtn.setVisible(this.isShowTabsMenu());
            this.downArrow = new StackPane();
            this.downArrow.setManaged(false);
            this.downArrow.getStyleClass().setAll((String[])new String[]{"arrow"});
            this.downArrow.setRotate(tabPane.getSide().equals((Object)Side.BOTTOM) ? 180.0 : 0.0);
            this.downArrowBtn.getChildren().add(this.downArrow);
            this.downArrowBtn.setOnMouseClicked(mouseEvent -> this.showPopupMenu());
            this.setupPopupMenu();
            this.inner = new StackPane(){

                @Override
                protected double computePrefWidth(double d) {
                    double d2 = !TabControlButtons.this.isShowTabsMenu() ? 0.0 : this.snapSize(TabControlButtons.this.downArrow.prefWidth(this.getHeight())) + this.snapSize(TabControlButtons.this.downArrowBtn.prefWidth(this.getHeight()));
                    double d3 = 0.0;
                    if (TabControlButtons.this.isShowTabsMenu()) {
                        d3 += d2;
                    }
                    if (d3 > 0.0) {
                        d3 += this.snappedLeftInset() + this.snappedRightInset();
                    }
                    return d3;
                }

                @Override
                protected double computePrefHeight(double d) {
                    double d2 = 0.0;
                    if (TabControlButtons.this.isShowTabsMenu()) {
                        d2 = Math.max(d2, this.snapSize(TabControlButtons.this.downArrowBtn.prefHeight(d)));
                    }
                    if (d2 > 0.0) {
                        d2 += this.snappedTopInset() + this.snappedBottomInset();
                    }
                    return d2;
                }

                @Override
                protected void layoutChildren() {
                    if (TabControlButtons.this.isShowTabsMenu()) {
                        double d = 0.0;
                        double d2 = this.snappedTopInset();
                        double d3 = this.snapSize(this.getWidth()) - d + this.snappedLeftInset();
                        double d4 = this.snapSize(this.getHeight()) - d2 + this.snappedBottomInset();
                        this.positionArrow(TabControlButtons.this.downArrowBtn, TabControlButtons.this.downArrow, d, d2, d3, d4);
                    }
                }

                private void positionArrow(Pane pane, StackPane stackPane, double d, double d2, double d3, double d4) {
                    pane.resize(d3, d4);
                    this.positionInArea(pane, d, d2, d3, d4, 0.0, HPos.CENTER, VPos.CENTER);
                    double d5 = this.snapSize(stackPane.prefWidth(-1.0));
                    double d6 = this.snapSize(stackPane.prefHeight(-1.0));
                    stackPane.resize(d5, d6);
                    this.positionInArea(stackPane, pane.snappedLeftInset(), pane.snappedTopInset(), d3 - pane.snappedLeftInset() - pane.snappedRightInset(), d4 - pane.snappedTopInset() - pane.snappedBottomInset(), 0.0, HPos.CENTER, VPos.CENTER);
                }
            };
            this.inner.getStyleClass().add("container");
            this.inner.getChildren().add(this.downArrowBtn);
            this.getChildren().add(this.inner);
            tabPane.sideProperty().addListener(observable -> {
                Side side = ((TabPane)TabPaneSkin.this.getSkinnable()).getSide();
                this.downArrow.setRotate(side.equals((Object)Side.BOTTOM) ? 180.0 : 0.0);
            });
            tabPane.getTabs().addListener(change -> this.setupPopupMenu());
            this.showControlButtons = false;
            if (this.isShowTabsMenu()) {
                this.showControlButtons = true;
                this.requestLayout();
            }
            this.getProperties().put(ContextMenu.class, this.popup);
        }

        private void showTabsMenu(boolean bl) {
            boolean bl2 = this.isShowTabsMenu();
            this.showTabsMenu = bl;
            if (this.showTabsMenu && !bl2) {
                this.downArrowBtn.setVisible(true);
                this.showControlButtons = true;
                this.inner.requestLayout();
                TabPaneSkin.this.tabHeaderArea.requestLayout();
            } else if (!this.showTabsMenu && bl2) {
                this.hideControlButtons();
            }
        }

        private boolean isShowTabsMenu() {
            return this.showTabsMenu;
        }

        @Override
        protected double computePrefWidth(double d) {
            double d2 = this.snapSize(this.inner.prefWidth(d));
            if (d2 > 0.0) {
                d2 += this.snappedLeftInset() + this.snappedRightInset();
            }
            return d2;
        }

        @Override
        protected double computePrefHeight(double d) {
            return Math.max(((TabPane)TabPaneSkin.this.getSkinnable()).getTabMinHeight(), this.snapSize(this.inner.prefHeight(d))) + this.snappedTopInset() + this.snappedBottomInset();
        }

        @Override
        protected void layoutChildren() {
            double d = this.snappedLeftInset();
            double d2 = this.snappedTopInset();
            double d3 = this.snapSize(this.getWidth()) - d + this.snappedRightInset();
            double d4 = this.snapSize(this.getHeight()) - d2 + this.snappedBottomInset();
            if (this.showControlButtons) {
                this.showControlButtons();
                this.showControlButtons = false;
            }
            this.inner.resize(d3, d4);
            this.positionInArea(this.inner, d, d2, d3, d4, 0.0, HPos.CENTER, VPos.BOTTOM);
        }

        private void showControlButtons() {
            this.setVisible(true);
            if (this.popup == null) {
                this.setupPopupMenu();
            }
        }

        private void hideControlButtons() {
            if (this.isShowTabsMenu()) {
                this.showControlButtons = true;
            } else {
                this.setVisible(false);
                this.clearPopupMenu();
                this.popup = null;
            }
            this.requestLayout();
        }

        private void setupPopupMenu() {
            if (this.popup == null) {
                this.popup = new ContextMenu();
            }
            this.clearPopupMenu();
            ToggleGroup toggleGroup = new ToggleGroup();
            ObservableList<TabMenuItem> observableList = FXCollections.observableArrayList();
            for (Tab tab : ((TabPane)TabPaneSkin.this.getSkinnable()).getTabs()) {
                TabMenuItem tabMenuItem = new TabMenuItem(tab);
                tabMenuItem.setToggleGroup(toggleGroup);
                tabMenuItem.setOnAction(actionEvent -> ((TabPane)TabPaneSkin.this.getSkinnable()).getSelectionModel().select(tab));
                observableList.add(tabMenuItem);
            }
            this.popup.getItems().addAll((Collection<MenuItem>)observableList);
        }

        private void clearPopupMenu() {
            for (MenuItem menuItem : this.popup.getItems()) {
                ((TabMenuItem)menuItem).dispose();
            }
            this.popup.getItems().clear();
        }

        private void showPopupMenu() {
            for (MenuItem menuItem : this.popup.getItems()) {
                TabMenuItem tabMenuItem = (TabMenuItem)menuItem;
                if (!TabPaneSkin.this.selectedTab.equals(tabMenuItem.getTab())) continue;
                tabMenuItem.setSelected(true);
                break;
            }
            this.popup.show(this.downArrowBtn, Side.BOTTOM, 0.0, 0.0);
        }
    }

    static class TabContentRegion
    extends StackPane {
        private Tab tab;
        private InvalidationListener tabContentListener = observable -> this.updateContent();
        private InvalidationListener tabSelectedListener = new InvalidationListener(){

            @Override
            public void invalidated(Observable observable) {
                this.setVisible(tab.isSelected());
            }
        };
        private WeakInvalidationListener weakTabContentListener = new WeakInvalidationListener(this.tabContentListener);
        private WeakInvalidationListener weakTabSelectedListener = new WeakInvalidationListener(this.tabSelectedListener);

        public Tab getTab() {
            return this.tab;
        }

        public TabContentRegion(Tab tab) {
            this.getStyleClass().setAll((String[])new String[]{"tab-content-area"});
            this.setManaged(false);
            this.tab = tab;
            this.updateContent();
            this.setVisible(tab.isSelected());
            tab.selectedProperty().addListener(this.weakTabSelectedListener);
            tab.contentProperty().addListener(this.weakTabContentListener);
        }

        private void updateContent() {
            Node node = this.getTab().getContent();
            if (node == null) {
                this.getChildren().clear();
            } else {
                this.getChildren().setAll((Node[])new Node[]{node});
            }
        }

        private void removeListeners(Tab tab) {
            tab.selectedProperty().removeListener(this.weakTabSelectedListener);
            tab.contentProperty().removeListener(this.weakTabContentListener);
        }
    }

    class TabHeaderSkin
    extends StackPane {
        private final Tab tab;
        private Label label;
        private StackPane closeBtn;
        private StackPane inner;
        private Tooltip oldTooltip;
        private Tooltip tooltip;
        private Rectangle clip;
        private boolean isClosing = false;
        private LambdaMultiplePropertyChangeListenerHandler listener = new LambdaMultiplePropertyChangeListenerHandler();
        private final ListChangeListener<String> styleClassListener = new ListChangeListener<String>(){

            @Override
            public void onChanged(ListChangeListener.Change<? extends String> change) {
                TabHeaderSkin.this.getStyleClass().setAll((Collection<String>)TabHeaderSkin.this.tab.getStyleClass());
            }
        };
        private final WeakListChangeListener<String> weakStyleClassListener = new WeakListChangeListener<String>(this.styleClassListener);
        private final DoubleProperty animationTransition = new SimpleDoubleProperty(this, "animationTransition", 1.0){

            @Override
            protected void invalidated() {
                TabHeaderSkin.this.requestLayout();
            }
        };
        private TabAnimationState animationState = TabAnimationState.NONE;
        private Timeline currentAnimation;

        public Tab getTab() {
            return this.tab;
        }

        public TabHeaderSkin(Tab tab) {
            this.getStyleClass().setAll((Collection<String>)tab.getStyleClass());
            this.setId(tab.getId());
            this.setStyle(tab.getStyle());
            this.setAccessibleRole(AccessibleRole.TAB_ITEM);
            this.setViewOrder(1.0);
            this.tab = tab;
            this.clip = new Rectangle();
            this.setClip(this.clip);
            this.label = new Label(tab.getText(), tab.getGraphic());
            this.label.getStyleClass().setAll((String[])new String[]{"tab-label"});
            this.closeBtn = new StackPane(){

                @Override
                protected double computePrefWidth(double d) {
                    return CLOSE_BTN_SIZE;
                }

                @Override
                protected double computePrefHeight(double d) {
                    return CLOSE_BTN_SIZE;
                }

                @Override
                public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
                    switch (accessibleAction) {
                        case FIRE: {
                            Tab tab = TabHeaderSkin.this.getTab();
                            if (!TabPaneSkin.this.behavior.canCloseTab(tab)) break;
                            TabPaneSkin.this.behavior.closeTab(tab);
                            this.setOnMousePressed(null);
                            break;
                        }
                        default: {
                            super.executeAccessibleAction(accessibleAction, objectArray);
                        }
                    }
                }
            };
            this.closeBtn.setAccessibleRole(AccessibleRole.BUTTON);
            this.closeBtn.setAccessibleText(ControlResources.getString("Accessibility.title.TabPane.CloseButton"));
            this.closeBtn.getStyleClass().setAll((String[])new String[]{"tab-close-button"});
            this.closeBtn.setOnMousePressed((EventHandler<? super MouseEvent>)new EventHandler<MouseEvent>(){

                @Override
                public void handle(MouseEvent mouseEvent) {
                    Tab tab = TabHeaderSkin.this.getTab();
                    if (mouseEvent.getButton().equals((Object)MouseButton.PRIMARY) && TabPaneSkin.this.behavior.canCloseTab(tab)) {
                        TabPaneSkin.this.behavior.closeTab(tab);
                        TabHeaderSkin.this.setOnMousePressed(null);
                        mouseEvent.consume();
                    }
                }
            });
            this.updateGraphicRotation();
            final Region region = new Region();
            region.setMouseTransparent(true);
            region.getStyleClass().add("focus-indicator");
            this.inner = new StackPane(){

                @Override
                protected void layoutChildren() {
                    TabPane tabPane = (TabPane)TabPaneSkin.this.getSkinnable();
                    double d = this.snappedTopInset();
                    double d2 = this.snappedRightInset();
                    double d3 = this.snappedBottomInset();
                    double d4 = this.snappedLeftInset();
                    double d5 = this.getWidth() - (d4 + d2);
                    double d6 = this.getHeight() - (d + d3);
                    double d7 = this.snapSize(TabHeaderSkin.this.label.prefWidth(-1.0));
                    double d8 = this.snapSize(TabHeaderSkin.this.label.prefHeight(-1.0));
                    double d9 = TabHeaderSkin.this.showCloseButton() ? this.snapSize(TabHeaderSkin.this.closeBtn.prefWidth(-1.0)) : 0.0;
                    double d10 = TabHeaderSkin.this.showCloseButton() ? this.snapSize(TabHeaderSkin.this.closeBtn.prefHeight(-1.0)) : 0.0;
                    double d11 = this.snapSize(tabPane.getTabMinWidth());
                    double d12 = this.snapSize(tabPane.getTabMaxWidth());
                    double d13 = this.snapSize(tabPane.getTabMaxHeight());
                    double d14 = d7;
                    double d15 = d7;
                    double d16 = d8;
                    double d17 = d14 + d9;
                    double d18 = Math.max(d16, d10);
                    if (d17 > d12 && d12 != Double.MAX_VALUE) {
                        d14 = d12 - d9;
                        d15 = d12 - d9;
                    } else if (d17 < d11) {
                        d14 = d11 - d9;
                    }
                    if (d18 > d13 && d13 != Double.MAX_VALUE) {
                        d16 = d13;
                    }
                    if (TabHeaderSkin.this.animationState != TabAnimationState.NONE) {
                        d14 *= TabHeaderSkin.this.animationTransition.get();
                        TabHeaderSkin.this.closeBtn.setVisible(false);
                    } else {
                        TabHeaderSkin.this.closeBtn.setVisible(TabHeaderSkin.this.showCloseButton());
                    }
                    TabHeaderSkin.this.label.resize(d15, d16);
                    double d19 = d4;
                    double d20 = (d12 < Double.MAX_VALUE ? Math.min(d5, d12) : d5) - d2 - d9;
                    this.positionInArea(TabHeaderSkin.this.label, d19, d, d14, d6, 0.0, HPos.CENTER, VPos.CENTER);
                    if (TabHeaderSkin.this.closeBtn.isVisible()) {
                        TabHeaderSkin.this.closeBtn.resize(d9, d10);
                        this.positionInArea(TabHeaderSkin.this.closeBtn, d20, d, d9, d6, 0.0, HPos.CENTER, VPos.CENTER);
                    }
                    int n = Utils.isMac() ? 2 : 3;
                    int n2 = Utils.isMac() ? 2 : 1;
                    region.resizeRelocate(d4 - (double)n2, d + (double)n, d5 + (double)(2 * n2), d6 - (double)(2 * n));
                }
            };
            this.inner.getStyleClass().add("tab-container");
            this.inner.setRotate(((TabPane)TabPaneSkin.this.getSkinnable()).getSide().equals((Object)Side.BOTTOM) ? 180.0 : 0.0);
            this.inner.getChildren().addAll((Node[])new Node[]{this.label, this.closeBtn, region});
            this.getChildren().addAll((Node[])new Node[]{this.inner});
            this.tooltip = tab.getTooltip();
            if (this.tooltip != null) {
                Tooltip.install(this, this.tooltip);
                this.oldTooltip = this.tooltip;
            }
            this.listener.registerChangeListener(tab.closableProperty(), observableValue -> {
                this.inner.requestLayout();
                this.requestLayout();
            });
            this.listener.registerChangeListener(tab.selectedProperty(), observableValue -> {
                this.pseudoClassStateChanged(SELECTED_PSEUDOCLASS_STATE, tab.isSelected());
                this.inner.requestLayout();
                this.requestLayout();
            });
            this.listener.registerChangeListener(tab.textProperty(), observableValue -> this.label.setText(this.getTab().getText()));
            this.listener.registerChangeListener(tab.graphicProperty(), observableValue -> this.label.setGraphic(this.getTab().getGraphic()));
            this.listener.registerChangeListener(tab.tooltipProperty(), observableValue -> {
                if (this.oldTooltip != null) {
                    Tooltip.uninstall(this, this.oldTooltip);
                }
                this.tooltip = tab.getTooltip();
                if (this.tooltip != null) {
                    Tooltip.install(this, this.tooltip);
                    this.oldTooltip = this.tooltip;
                }
            });
            this.listener.registerChangeListener(tab.disabledProperty(), observableValue -> this.updateTabDisabledState());
            this.listener.registerChangeListener(tab.getTabPane().disabledProperty(), observableValue -> this.updateTabDisabledState());
            this.listener.registerChangeListener(tab.styleProperty(), observableValue -> this.setStyle(tab.getStyle()));
            tab.getStyleClass().addListener(this.weakStyleClassListener);
            this.listener.registerChangeListener(((TabPane)TabPaneSkin.this.getSkinnable()).tabClosingPolicyProperty(), observableValue -> {
                this.inner.requestLayout();
                this.requestLayout();
            });
            this.listener.registerChangeListener(((TabPane)TabPaneSkin.this.getSkinnable()).sideProperty(), observableValue -> {
                Side side = ((TabPane)TabPaneSkin.this.getSkinnable()).getSide();
                this.pseudoClassStateChanged(TOP_PSEUDOCLASS_STATE, side == Side.TOP);
                this.pseudoClassStateChanged(RIGHT_PSEUDOCLASS_STATE, side == Side.RIGHT);
                this.pseudoClassStateChanged(BOTTOM_PSEUDOCLASS_STATE, side == Side.BOTTOM);
                this.pseudoClassStateChanged(LEFT_PSEUDOCLASS_STATE, side == Side.LEFT);
                this.inner.setRotate(side == Side.BOTTOM ? 180.0 : 0.0);
                if (((TabPane)TabPaneSkin.this.getSkinnable()).isRotateGraphic()) {
                    this.updateGraphicRotation();
                }
            });
            this.listener.registerChangeListener(((TabPane)TabPaneSkin.this.getSkinnable()).rotateGraphicProperty(), observableValue -> this.updateGraphicRotation());
            this.listener.registerChangeListener(((TabPane)TabPaneSkin.this.getSkinnable()).tabMinWidthProperty(), observableValue -> {
                this.requestLayout();
                ((TabPane)TabPaneSkin.this.getSkinnable()).requestLayout();
            });
            this.listener.registerChangeListener(((TabPane)TabPaneSkin.this.getSkinnable()).tabMaxWidthProperty(), observableValue -> {
                this.requestLayout();
                ((TabPane)TabPaneSkin.this.getSkinnable()).requestLayout();
            });
            this.listener.registerChangeListener(((TabPane)TabPaneSkin.this.getSkinnable()).tabMinHeightProperty(), observableValue -> {
                this.requestLayout();
                ((TabPane)TabPaneSkin.this.getSkinnable()).requestLayout();
            });
            this.listener.registerChangeListener(((TabPane)TabPaneSkin.this.getSkinnable()).tabMaxHeightProperty(), observableValue -> {
                this.requestLayout();
                ((TabPane)TabPaneSkin.this.getSkinnable()).requestLayout();
            });
            this.getProperties().put(Tab.class, tab);
            this.getProperties().put(ContextMenu.class, tab.getContextMenu());
            this.setOnContextMenuRequested(contextMenuEvent -> {
                if (this.getTab().getContextMenu() != null) {
                    this.getTab().getContextMenu().show(this.inner, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
                    contextMenuEvent.consume();
                }
            });
            this.setOnMousePressed((EventHandler<? super MouseEvent>)new EventHandler<MouseEvent>(){

                @Override
                public void handle(MouseEvent mouseEvent) {
                    Tab tab = TabHeaderSkin.this.getTab();
                    if (tab.isDisable()) {
                        return;
                    }
                    if ((mouseEvent.getButton().equals((Object)MouseButton.MIDDLE) || mouseEvent.getButton().equals((Object)MouseButton.PRIMARY)) && tab.getContextMenu() != null && tab.getContextMenu().isShowing()) {
                        tab.getContextMenu().hide();
                    }
                    if (mouseEvent.getButton().equals((Object)MouseButton.MIDDLE)) {
                        if (TabHeaderSkin.this.showCloseButton() && TabPaneSkin.this.behavior.canCloseTab(tab)) {
                            TabHeaderSkin.this.removeListeners(tab);
                            TabPaneSkin.this.behavior.closeTab(tab);
                        }
                    } else if (mouseEvent.getButton().equals((Object)MouseButton.PRIMARY)) {
                        TabPaneSkin.this.behavior.selectTab(tab);
                    }
                }
            });
            this.pseudoClassStateChanged(SELECTED_PSEUDOCLASS_STATE, tab.isSelected());
            this.pseudoClassStateChanged(DISABLED_PSEUDOCLASS_STATE, tab.isDisabled());
            Side side = ((TabPane)TabPaneSkin.this.getSkinnable()).getSide();
            this.pseudoClassStateChanged(TOP_PSEUDOCLASS_STATE, side == Side.TOP);
            this.pseudoClassStateChanged(RIGHT_PSEUDOCLASS_STATE, side == Side.RIGHT);
            this.pseudoClassStateChanged(BOTTOM_PSEUDOCLASS_STATE, side == Side.BOTTOM);
            this.pseudoClassStateChanged(LEFT_PSEUDOCLASS_STATE, side == Side.LEFT);
        }

        private void updateTabDisabledState() {
            this.pseudoClassStateChanged(DISABLED_PSEUDOCLASS_STATE, this.tab.isDisabled());
            this.inner.requestLayout();
            this.requestLayout();
        }

        private void updateGraphicRotation() {
            if (this.label.getGraphic() != null) {
                this.label.getGraphic().setRotate(((TabPane)TabPaneSkin.this.getSkinnable()).isRotateGraphic() ? 0.0 : (double)(((TabPane)TabPaneSkin.this.getSkinnable()).getSide().equals((Object)Side.RIGHT) ? -90.0f : (((TabPane)TabPaneSkin.this.getSkinnable()).getSide().equals((Object)Side.LEFT) ? 90.0f : 0.0f)));
            }
        }

        private boolean showCloseButton() {
            return this.tab.isClosable() && (((TabPane)TabPaneSkin.this.getSkinnable()).getTabClosingPolicy().equals((Object)TabPane.TabClosingPolicy.ALL_TABS) || ((TabPane)TabPaneSkin.this.getSkinnable()).getTabClosingPolicy().equals((Object)TabPane.TabClosingPolicy.SELECTED_TAB) && this.tab.isSelected());
        }

        private void removeListeners(Tab tab) {
            this.listener.dispose();
            this.inner.getChildren().clear();
            this.getChildren().clear();
            this.setOnContextMenuRequested(null);
            this.setOnMousePressed(null);
        }

        @Override
        protected double computePrefWidth(double d) {
            double d2 = this.snapSize(((TabPane)TabPaneSkin.this.getSkinnable()).getTabMinWidth());
            double d3 = this.snapSize(((TabPane)TabPaneSkin.this.getSkinnable()).getTabMaxWidth());
            double d4 = this.snappedRightInset();
            double d5 = this.snappedLeftInset();
            double d6 = this.snapSize(this.label.prefWidth(-1.0));
            if (this.showCloseButton()) {
                d6 += this.snapSize(this.closeBtn.prefWidth(-1.0));
            }
            if (d6 > d3) {
                d6 = d3;
            } else if (d6 < d2) {
                d6 = d2;
            }
            return d6 += d4 + d5;
        }

        @Override
        protected double computePrefHeight(double d) {
            double d2 = this.snapSize(((TabPane)TabPaneSkin.this.getSkinnable()).getTabMinHeight());
            double d3 = this.snapSize(((TabPane)TabPaneSkin.this.getSkinnable()).getTabMaxHeight());
            double d4 = this.snappedTopInset();
            double d5 = this.snappedBottomInset();
            double d6 = this.snapSize(this.label.prefHeight(d));
            if (d6 > d3) {
                d6 = d3;
            } else if (d6 < d2) {
                d6 = d2;
            }
            return d6 += d4 + d5;
        }

        @Override
        protected void layoutChildren() {
            double d = (this.snapSize(this.getWidth()) - this.snappedRightInset() - this.snappedLeftInset()) * this.animationTransition.getValue();
            this.inner.resize(d, this.snapSize(this.getHeight()) - this.snappedTopInset() - this.snappedBottomInset());
            this.inner.relocate(this.snappedLeftInset(), this.snappedTopInset());
        }

        @Override
        protected void setWidth(double d) {
            super.setWidth(d);
            this.clip.setWidth(d);
        }

        @Override
        protected void setHeight(double d) {
            super.setHeight(d);
            this.clip.setHeight(d);
        }

        @Override
        public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
            switch (accessibleAttribute) {
                case TEXT: {
                    return this.getTab().getText();
                }
                case SELECTED: {
                    return TabPaneSkin.this.selectedTab == this.getTab();
                }
            }
            return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
        }

        @Override
        public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
            switch (accessibleAction) {
                case REQUEST_FOCUS: {
                    ((TabPane)TabPaneSkin.this.getSkinnable()).getSelectionModel().select(this.getTab());
                    break;
                }
                default: {
                    super.executeAccessibleAction(accessibleAction, objectArray);
                }
            }
        }
    }

    class TabHeaderArea
    extends StackPane {
        private Rectangle headerClip;
        private StackPane headersRegion;
        private StackPane headerBackground;
        private TabControlButtons controlButtons;
        private boolean measureClosingTabs = false;
        private double scrollOffset;

        public TabHeaderArea() {
            this.getStyleClass().setAll((String[])new String[]{"tab-header-area"});
            this.setManaged(false);
            TabPane tabPane = (TabPane)TabPaneSkin.this.getSkinnable();
            this.headerClip = new Rectangle();
            this.headersRegion = new StackPane(){

                @Override
                protected double computePrefWidth(double d) {
                    double d2 = 0.0;
                    for (Node node : this.getChildren()) {
                        TabHeaderSkin tabHeaderSkin = (TabHeaderSkin)node;
                        if (!tabHeaderSkin.isVisible() || !TabHeaderArea.this.measureClosingTabs && tabHeaderSkin.isClosing) continue;
                        d2 += tabHeaderSkin.prefWidth(d);
                    }
                    return this.snapSize(d2) + this.snappedLeftInset() + this.snappedRightInset();
                }

                @Override
                protected double computePrefHeight(double d) {
                    double d2 = 0.0;
                    for (Node node : this.getChildren()) {
                        TabHeaderSkin tabHeaderSkin = (TabHeaderSkin)node;
                        d2 = Math.max(d2, tabHeaderSkin.prefHeight(d));
                    }
                    return this.snapSize(d2) + this.snappedTopInset() + this.snappedBottomInset();
                }

                @Override
                protected void layoutChildren() {
                    if (TabHeaderArea.this.tabsFit()) {
                        TabHeaderArea.this.setScrollOffset(0.0);
                    } else if (TabPaneSkin.this.isSelectingTab) {
                        TabHeaderArea.this.ensureSelectedTabIsVisible();
                    } else {
                        TabHeaderArea.this.validateScrollOffset();
                    }
                    TabPaneSkin.this.isSelectingTab = false;
                    Side side = ((TabPane)TabPaneSkin.this.getSkinnable()).getSide();
                    double d = this.snapSize(this.prefHeight(-1.0));
                    double d2 = side.equals((Object)Side.LEFT) || side.equals((Object)Side.BOTTOM) ? this.snapSize(this.getWidth()) - TabHeaderArea.this.getScrollOffset() : TabHeaderArea.this.getScrollOffset();
                    TabHeaderArea.this.updateHeaderClip();
                    for (Node node : this.getChildren()) {
                        double d3;
                        TabHeaderSkin tabHeaderSkin = (TabHeaderSkin)node;
                        double d4 = this.snapSize(tabHeaderSkin.prefWidth(-1.0) * tabHeaderSkin.animationTransition.get());
                        double d5 = this.snapSize(tabHeaderSkin.prefHeight(-1.0));
                        tabHeaderSkin.resize(d4, d5);
                        double d6 = d3 = side.equals((Object)Side.BOTTOM) ? 0.0 : d - d5 - this.snappedBottomInset();
                        if (side.equals((Object)Side.LEFT) || side.equals((Object)Side.BOTTOM)) {
                            d2 -= d4;
                            if (TabPaneSkin.this.dragState == DragState.REORDER && (tabHeaderSkin == TabPaneSkin.this.dragTabHeader || tabHeaderSkin == TabPaneSkin.this.dropAnimHeader)) continue;
                            tabHeaderSkin.relocate(d2, d3);
                            continue;
                        }
                        if (TabPaneSkin.this.dragState != DragState.REORDER || tabHeaderSkin != TabPaneSkin.this.dragTabHeader && tabHeaderSkin != TabPaneSkin.this.dropAnimHeader) {
                            tabHeaderSkin.relocate(d2, d3);
                        }
                        d2 += d4;
                    }
                }
            };
            this.headersRegion.getStyleClass().setAll((String[])new String[]{"headers-region"});
            this.headersRegion.setClip(this.headerClip);
            TabPaneSkin.this.setupReordering(this.headersRegion);
            this.headerBackground = new StackPane();
            this.headerBackground.getStyleClass().setAll((String[])new String[]{"tab-header-background"});
            int n = 0;
            for (Tab tab : tabPane.getTabs()) {
                this.addTab(tab, n++);
            }
            this.controlButtons = new TabControlButtons();
            this.controlButtons.setVisible(false);
            if (this.controlButtons.isVisible()) {
                this.controlButtons.setVisible(true);
            }
            this.getChildren().addAll((Node[])new Node[]{this.headerBackground, this.headersRegion, this.controlButtons});
            this.addEventHandler(ScrollEvent.SCROLL, scrollEvent -> {
                Side side = ((TabPane)TabPaneSkin.this.getSkinnable()).getSide();
                side = side == null ? Side.TOP : side;
                switch (side) {
                    default: {
                        this.setScrollOffset(this.scrollOffset + scrollEvent.getDeltaY());
                        break;
                    }
                    case LEFT: 
                    case RIGHT: {
                        this.setScrollOffset(this.scrollOffset - scrollEvent.getDeltaY());
                    }
                }
            });
        }

        private void updateHeaderClip() {
            Side side = ((TabPane)TabPaneSkin.this.getSkinnable()).getSide();
            double d = 0.0;
            double d2 = 0.0;
            double d3 = 0.0;
            double d4 = 0.0;
            double d5 = 0.0;
            double d6 = 0.0;
            double d7 = this.firstTabIndent();
            double d8 = this.snapSize(this.controlButtons.prefWidth(-1.0));
            this.measureClosingTabs = true;
            double d9 = this.snapSize(this.headersRegion.prefWidth(-1.0));
            this.measureClosingTabs = false;
            double d10 = this.snapSize(this.headersRegion.prefHeight(-1.0));
            if (d8 > 0.0) {
                d8 += 10.0;
            }
            if (this.headersRegion.getEffect() instanceof DropShadow) {
                DropShadow dropShadow = (DropShadow)this.headersRegion.getEffect();
                d6 = dropShadow.getRadius();
            }
            d5 = this.snapSize(this.getWidth()) - d8 - d7;
            if (side.equals((Object)Side.LEFT) || side.equals((Object)Side.BOTTOM)) {
                if (d9 < d5) {
                    d3 = d9 + d6;
                } else {
                    d = d9 - d5;
                    d3 = d5 + d6;
                }
                d4 = d10;
            } else {
                d = -d6;
                d3 = (d9 < d5 ? d9 : d5) + d6;
                d4 = d10;
            }
            this.headerClip.setX(d);
            this.headerClip.setY(d2);
            this.headerClip.setWidth(d3);
            this.headerClip.setHeight(d4);
        }

        private void addTab(Tab tab, int n) {
            TabHeaderSkin tabHeaderSkin = new TabHeaderSkin(tab);
            this.headersRegion.getChildren().add(n, tabHeaderSkin);
        }

        private void removeTab(Tab tab) {
            TabHeaderSkin tabHeaderSkin = this.getTabHeaderSkin(tab);
            if (tabHeaderSkin != null) {
                this.headersRegion.getChildren().remove(tabHeaderSkin);
            }
        }

        private TabHeaderSkin getTabHeaderSkin(Tab tab) {
            for (Node node : this.headersRegion.getChildren()) {
                TabHeaderSkin tabHeaderSkin = (TabHeaderSkin)node;
                if (!tabHeaderSkin.getTab().equals(tab)) continue;
                return tabHeaderSkin;
            }
            return null;
        }

        private boolean tabsFit() {
            double d;
            double d2 = this.snapSize(this.headersRegion.prefWidth(-1.0));
            double d3 = d2 + (d = this.snapSize(this.controlButtons.prefWidth(-1.0))) + this.firstTabIndent() + 10.0;
            return d3 < this.getWidth();
        }

        private void ensureSelectedTabIsVisible() {
            double d = this.snapSize(TabPaneSkin.this.isHorizontal() ? ((TabPane)TabPaneSkin.this.getSkinnable()).getWidth() : ((TabPane)TabPaneSkin.this.getSkinnable()).getHeight());
            double d2 = this.snapSize(this.controlButtons.getWidth());
            double d3 = d - d2 - this.firstTabIndent() - 10.0;
            double d4 = 0.0;
            double d5 = 0.0;
            double d6 = 0.0;
            for (Node node : this.headersRegion.getChildren()) {
                TabHeaderSkin tabHeaderSkin = (TabHeaderSkin)node;
                double d7 = this.snapSize(tabHeaderSkin.prefWidth(-1.0));
                if (TabPaneSkin.this.selectedTab != null && TabPaneSkin.this.selectedTab.equals(tabHeaderSkin.getTab())) {
                    d5 = d4;
                    d6 = d7;
                }
                d4 += d7;
            }
            double d8 = this.getScrollOffset();
            double d9 = d5;
            double d10 = d5 + d6;
            double d11 = d3;
            if (d9 < -d8) {
                this.setScrollOffset(-d9);
            } else if (d10 > d11 - d8) {
                this.setScrollOffset(d11 - d10);
            }
        }

        public double getScrollOffset() {
            return this.scrollOffset;
        }

        private void validateScrollOffset() {
            this.setScrollOffset(this.getScrollOffset());
        }

        private void setScrollOffset(double d) {
            double d2 = this.snapSize(TabPaneSkin.this.isHorizontal() ? ((TabPane)TabPaneSkin.this.getSkinnable()).getWidth() : ((TabPane)TabPaneSkin.this.getSkinnable()).getHeight());
            double d3 = this.snapSize(this.controlButtons.getWidth());
            double d4 = d2 - d3 - this.firstTabIndent() - 10.0;
            double d5 = 0.0;
            for (Node node : this.headersRegion.getChildren()) {
                TabHeaderSkin tabHeaderSkin = (TabHeaderSkin)node;
                double d6 = this.snapSize(tabHeaderSkin.prefWidth(-1.0));
                d5 += d6;
            }
            double d7 = d4 - d > d5 && d < 0.0 ? d4 - d5 : (d > 0.0 ? 0.0 : d);
            if (Math.abs(d7 - this.scrollOffset) > 0.001) {
                this.scrollOffset = d7;
                this.headersRegion.requestLayout();
            }
        }

        private double firstTabIndent() {
            switch (((TabPane)TabPaneSkin.this.getSkinnable()).getSide()) {
                case TOP: 
                case BOTTOM: {
                    return this.snappedLeftInset();
                }
                case LEFT: 
                case RIGHT: {
                    return this.snappedTopInset();
                }
            }
            return 0.0;
        }

        @Override
        protected double computePrefWidth(double d) {
            double d2 = TabPaneSkin.this.isHorizontal() ? this.snappedLeftInset() + this.snappedRightInset() : this.snappedTopInset() + this.snappedBottomInset();
            return this.snapSize(this.headersRegion.prefWidth(d)) + this.controlButtons.prefWidth(d) + this.firstTabIndent() + 10.0 + d2;
        }

        @Override
        protected double computePrefHeight(double d) {
            double d2 = TabPaneSkin.this.isHorizontal() ? this.snappedTopInset() + this.snappedBottomInset() : this.snappedLeftInset() + this.snappedRightInset();
            return this.snapSize(this.headersRegion.prefHeight(-1.0)) + d2;
        }

        @Override
        public double getBaselineOffset() {
            if (((TabPane)TabPaneSkin.this.getSkinnable()).getSide() == Side.TOP) {
                return this.headersRegion.getBaselineOffset() + this.snappedTopInset();
            }
            return 0.0;
        }

        @Override
        protected void layoutChildren() {
            double d = this.snappedLeftInset();
            double d2 = this.snappedRightInset();
            double d3 = this.snappedTopInset();
            double d4 = this.snappedBottomInset();
            double d5 = this.snapSize(this.getWidth()) - (TabPaneSkin.this.isHorizontal() ? d + d2 : d3 + d4);
            double d6 = this.snapSize(this.getHeight()) - (TabPaneSkin.this.isHorizontal() ? d3 + d4 : d + d2);
            double d7 = this.snapSize(this.prefHeight(-1.0));
            double d8 = this.snapSize(this.headersRegion.prefWidth(-1.0));
            double d9 = this.snapSize(this.headersRegion.prefHeight(-1.0));
            this.controlButtons.showTabsMenu(!this.tabsFit());
            this.updateHeaderClip();
            this.headersRegion.requestLayout();
            double d10 = this.snapSize(this.controlButtons.prefWidth(-1.0));
            double d11 = this.controlButtons.prefHeight(d10);
            this.controlButtons.resize(d10, d11);
            this.headersRegion.resize(d8, d9);
            if (TabPaneSkin.this.isFloatingStyleClass()) {
                this.headerBackground.setVisible(false);
            } else {
                this.headerBackground.resize(this.snapSize(this.getWidth()), this.snapSize(this.getHeight()));
                this.headerBackground.setVisible(true);
            }
            double d12 = 0.0;
            double d13 = 0.0;
            double d14 = 0.0;
            double d15 = 0.0;
            Side side = ((TabPane)TabPaneSkin.this.getSkinnable()).getSide();
            if (side.equals((Object)Side.TOP)) {
                d12 = d;
                d13 = d7 - d9 - d4;
                d14 = d5 - d10 + d;
                d15 = this.snapSize(this.getHeight()) - d11 - d4;
            } else if (side.equals((Object)Side.RIGHT)) {
                d12 = d3;
                d13 = d7 - d9 - d;
                d14 = d5 - d10 + d3;
                d15 = this.snapSize(this.getHeight()) - d11 - d;
            } else if (side.equals((Object)Side.BOTTOM)) {
                d12 = this.snapSize(this.getWidth()) - d8 - d;
                d13 = d7 - d9 - d3;
                d14 = d2;
                d15 = this.snapSize(this.getHeight()) - d11 - d3;
            } else if (side.equals((Object)Side.LEFT)) {
                d12 = this.snapSize(this.getWidth()) - d8 - d3;
                d13 = d7 - d9 - d2;
                d14 = d;
                d15 = this.snapSize(this.getHeight()) - d11 - d2;
            }
            if (this.headerBackground.isVisible()) {
                this.positionInArea(this.headerBackground, 0.0, 0.0, this.snapSize(this.getWidth()), this.snapSize(this.getHeight()), 0.0, HPos.CENTER, VPos.CENTER);
            }
            this.positionInArea(this.headersRegion, d12, d13, d5, d6, 0.0, HPos.LEFT, VPos.CENTER);
            this.positionInArea(this.controlButtons, d14, d15, d10, d11, 0.0, HPos.CENTER, VPos.CENTER);
        }
    }

    private static class StyleableProperties {
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        private static final CssMetaData<TabPane, TabAnimation> OPEN_TAB_ANIMATION;
        private static final CssMetaData<TabPane, TabAnimation> CLOSE_TAB_ANIMATION;

        private StyleableProperties() {
        }

        static {
            OPEN_TAB_ANIMATION = new CssMetaData<TabPane, TabAnimation>("-fx-open-tab-animation", new EnumConverter<TabAnimation>(TabAnimation.class), TabAnimation.GROW){

                @Override
                public boolean isSettable(TabPane tabPane) {
                    return true;
                }

                @Override
                public StyleableProperty<TabAnimation> getStyleableProperty(TabPane tabPane) {
                    TabPaneSkin tabPaneSkin = (TabPaneSkin)tabPane.getSkin();
                    return (StyleableProperty)((Object)tabPaneSkin.openTabAnimation);
                }
            };
            CLOSE_TAB_ANIMATION = new CssMetaData<TabPane, TabAnimation>("-fx-close-tab-animation", new EnumConverter<TabAnimation>(TabAnimation.class), TabAnimation.GROW){

                @Override
                public boolean isSettable(TabPane tabPane) {
                    return true;
                }

                @Override
                public StyleableProperty<TabAnimation> getStyleableProperty(TabPane tabPane) {
                    TabPaneSkin tabPaneSkin = (TabPaneSkin)tabPane.getSkin();
                    return (StyleableProperty)((Object)tabPaneSkin.closeTabAnimation);
                }
            };
            ArrayList arrayList = new ArrayList(SkinBase.getClassCssMetaData());
            arrayList.add(OPEN_TAB_ANIMATION);
            arrayList.add(CLOSE_TAB_ANIMATION);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }

    private static enum TabAnimationState {
        SHOWING,
        HIDING,
        NONE;

    }

    private static enum TabAnimation {
        NONE,
        GROW;

    }
}

