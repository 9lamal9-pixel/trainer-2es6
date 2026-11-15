/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.ParentHelper;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.ToolBarBehavior;
import com.sun.javafx.scene.control.skin.resources.ControlResources;
import com.sun.javafx.scene.traversal.Algorithm;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import com.sun.javafx.scene.traversal.TraversalContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.css.converter.SizeConverter;
import javafx.event.EventTarget;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SkinBase;
import javafx.scene.control.ToolBar;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ToolBarSkin
extends SkinBase<ToolBar> {
    private Pane box;
    private ToolBarOverflowMenu overflowMenu;
    private boolean overflow = false;
    private double previousWidth = 0.0;
    private double previousHeight = 0.0;
    private double savedPrefWidth = 0.0;
    private double savedPrefHeight = 0.0;
    private ObservableList<MenuItem> overflowMenuItems;
    private boolean needsUpdate = false;
    private final ParentTraversalEngine engine;
    private final BehaviorBase<ToolBar> behavior;
    private DoubleProperty spacing;
    private ObjectProperty<Pos> boxAlignment;

    public ToolBarSkin(ToolBar toolBar) {
        super(toolBar);
        this.behavior = new ToolBarBehavior(toolBar);
        this.overflowMenuItems = FXCollections.observableArrayList();
        this.initialize();
        this.registerChangeListener(toolBar.orientationProperty(), observableValue -> this.initialize());
        this.engine = new ParentTraversalEngine((Parent)((Object)this.getSkinnable()), new Algorithm(){

            private Node selectPrev(int n, TraversalContext traversalContext) {
                for (int i = n; i >= 0; --i) {
                    Node node;
                    Node node2 = (Node)ToolBarSkin.this.box.getChildren().get(i);
                    if (node2.isDisabled() || !NodeHelper.isTreeShowing(node2)) continue;
                    if (node2 instanceof Parent && (node = traversalContext.selectLastInParent((Parent)node2)) != null) {
                        return node;
                    }
                    if (!node2.isFocusTraversable()) continue;
                    return node2;
                }
                return null;
            }

            private Node selectNext(int n, TraversalContext traversalContext) {
                int n2 = ToolBarSkin.this.box.getChildren().size();
                for (int i = n; i < n2; ++i) {
                    Node node;
                    Node node2 = (Node)ToolBarSkin.this.box.getChildren().get(i);
                    if (node2.isDisabled() || !NodeHelper.isTreeShowing(node2)) continue;
                    if (node2.isFocusTraversable()) {
                        return node2;
                    }
                    if (!(node2 instanceof Parent) || (node = traversalContext.selectFirstInParent((Parent)node2)) == null) continue;
                    return node;
                }
                return null;
            }

            @Override
            public Node select(Node node, Direction direction, TraversalContext traversalContext) {
                Node node2;
                int n;
                ObservableList<Node> observableList = ToolBarSkin.this.box.getChildren();
                if (node == ToolBarSkin.this.overflowMenu) {
                    if (direction.isForward()) {
                        return null;
                    }
                    Node node3 = this.selectPrev(observableList.size() - 1, traversalContext);
                    if (node3 != null) {
                        return node3;
                    }
                }
                if ((n = observableList.indexOf(node)) < 0) {
                    node2 = node.getParent();
                    while (!observableList.contains(node2)) {
                        node2 = node2.getParent();
                    }
                    Node node4 = traversalContext.selectInSubtree((Parent)node2, node, direction);
                    if (node4 != null) {
                        return node4;
                    }
                    n = observableList.indexOf(node2);
                    if (direction == Direction.NEXT) {
                        direction = Direction.NEXT_IN_LINE;
                    }
                }
                if (n >= 0) {
                    if (direction.isForward()) {
                        node2 = this.selectNext(n + 1, traversalContext);
                        if (node2 != null) {
                            return node2;
                        }
                        if (ToolBarSkin.this.overflow) {
                            ToolBarSkin.this.overflowMenu.requestFocus();
                            return ToolBarSkin.this.overflowMenu;
                        }
                    } else {
                        node2 = this.selectPrev(n - 1, traversalContext);
                        if (node2 != null) {
                            return node2;
                        }
                    }
                }
                return null;
            }

            @Override
            public Node selectFirst(TraversalContext traversalContext) {
                Node node = this.selectNext(0, traversalContext);
                if (node != null) {
                    return node;
                }
                if (ToolBarSkin.this.overflow) {
                    return ToolBarSkin.this.overflowMenu;
                }
                return null;
            }

            @Override
            public Node selectLast(TraversalContext traversalContext) {
                if (ToolBarSkin.this.overflow) {
                    return ToolBarSkin.this.overflowMenu;
                }
                return this.selectPrev(ToolBarSkin.this.box.getChildren().size() - 1, traversalContext);
            }
        });
        ParentHelper.setTraversalEngine((Parent)((Object)this.getSkinnable()), this.engine);
        toolBar.focusedProperty().addListener((observableValue, bl, bl2) -> {
            if (bl2.booleanValue()) {
                if (!this.box.getChildren().isEmpty()) {
                    ((Node)this.box.getChildren().get(0)).requestFocus();
                } else {
                    this.overflowMenu.requestFocus();
                }
            }
        });
        toolBar.getItems().addListener(change -> {
            while (change.next()) {
                for (Node node : change.getRemoved()) {
                    this.box.getChildren().remove(node);
                }
                this.box.getChildren().addAll(change.getAddedSubList());
            }
            this.needsUpdate = true;
            ((ToolBar)this.getSkinnable()).requestLayout();
        });
    }

    private double snapSpacing(double d) {
        if (((ToolBar)this.getSkinnable()).getOrientation() == Orientation.VERTICAL) {
            return this.snapSpaceY(d);
        }
        return this.snapSpaceX(d);
    }

    private final void setSpacing(double d) {
        this.spacingProperty().set(this.snapSpacing(d));
    }

    private final double getSpacing() {
        return this.spacing == null ? 0.0 : this.snapSpacing(this.spacing.get());
    }

    private final DoubleProperty spacingProperty() {
        if (this.spacing == null) {
            this.spacing = new StyleableDoubleProperty(){

                @Override
                protected void invalidated() {
                    double d = this.get();
                    if (((ToolBar)ToolBarSkin.this.getSkinnable()).getOrientation() == Orientation.VERTICAL) {
                        ((VBox)ToolBarSkin.this.box).setSpacing(d);
                    } else {
                        ((HBox)ToolBarSkin.this.box).setSpacing(d);
                    }
                }

                @Override
                public Object getBean() {
                    return ToolBarSkin.this;
                }

                @Override
                public String getName() {
                    return "spacing";
                }

                @Override
                public CssMetaData<ToolBar, Number> getCssMetaData() {
                    return StyleableProperties.SPACING;
                }
            };
        }
        return this.spacing;
    }

    private final void setBoxAlignment(Pos pos) {
        this.boxAlignmentProperty().set(pos);
    }

    private final Pos getBoxAlignment() {
        return this.boxAlignment == null ? Pos.TOP_LEFT : (Pos)((Object)this.boxAlignment.get());
    }

    private final ObjectProperty<Pos> boxAlignmentProperty() {
        if (this.boxAlignment == null) {
            this.boxAlignment = new StyleableObjectProperty<Pos>(Pos.TOP_LEFT){

                @Override
                public void invalidated() {
                    Pos pos = (Pos)((Object)this.get());
                    if (((ToolBar)ToolBarSkin.this.getSkinnable()).getOrientation() == Orientation.VERTICAL) {
                        ((VBox)ToolBarSkin.this.box).setAlignment(pos);
                    } else {
                        ((HBox)ToolBarSkin.this.box).setAlignment(pos);
                    }
                }

                @Override
                public Object getBean() {
                    return ToolBarSkin.this;
                }

                @Override
                public String getName() {
                    return "boxAlignment";
                }

                @Override
                public CssMetaData<ToolBar, Pos> getCssMetaData() {
                    return StyleableProperties.ALIGNMENT;
                }
            };
        }
        return this.boxAlignment;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }

    @Override
    protected double computeMinWidth(double d, double d2, double d3, double d4, double d5) {
        ToolBar toolBar = (ToolBar)this.getSkinnable();
        return toolBar.getOrientation() == Orientation.VERTICAL ? this.computePrefWidth(-1.0, d2, d3, d4, d5) : this.snapSizeX(this.overflowMenu.prefWidth(-1.0)) + d5 + d3;
    }

    @Override
    protected double computeMinHeight(double d, double d2, double d3, double d4, double d5) {
        ToolBar toolBar = (ToolBar)this.getSkinnable();
        return toolBar.getOrientation() == Orientation.VERTICAL ? this.snapSizeY(this.overflowMenu.prefHeight(-1.0)) + d2 + d4 : this.computePrefHeight(-1.0, d2, d3, d4, d5);
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        double d6 = 0.0;
        ToolBar toolBar = (ToolBar)this.getSkinnable();
        if (toolBar.getOrientation() == Orientation.HORIZONTAL) {
            for (Node node : toolBar.getItems()) {
                if (!node.isManaged()) continue;
                d6 += this.snapSizeX(node.prefWidth(-1.0)) + this.getSpacing();
            }
            d6 -= this.getSpacing();
        } else {
            for (Node node : toolBar.getItems()) {
                if (!node.isManaged()) continue;
                d6 = Math.max(d6, this.snapSizeX(node.prefWidth(-1.0)));
            }
            if (toolBar.getItems().size() > 0) {
                this.savedPrefWidth = d6;
            } else {
                d6 = this.savedPrefWidth;
            }
        }
        return d5 + d6 + d3;
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        double d6 = 0.0;
        ToolBar toolBar = (ToolBar)this.getSkinnable();
        if (toolBar.getOrientation() == Orientation.VERTICAL) {
            for (Node node : toolBar.getItems()) {
                if (!node.isManaged()) continue;
                d6 += this.snapSizeY(node.prefHeight(-1.0)) + this.getSpacing();
            }
            d6 -= this.getSpacing();
        } else {
            for (Node node : toolBar.getItems()) {
                if (!node.isManaged()) continue;
                d6 = Math.max(d6, this.snapSizeY(node.prefHeight(-1.0)));
            }
            if (toolBar.getItems().size() > 0) {
                this.savedPrefHeight = d6;
            } else {
                d6 = this.savedPrefHeight;
            }
        }
        return d2 + d6 + d4;
    }

    @Override
    protected double computeMaxWidth(double d, double d2, double d3, double d4, double d5) {
        return ((ToolBar)this.getSkinnable()).getOrientation() == Orientation.VERTICAL ? this.snapSizeX(((ToolBar)this.getSkinnable()).prefWidth(-1.0)) : Double.MAX_VALUE;
    }

    @Override
    protected double computeMaxHeight(double d, double d2, double d3, double d4, double d5) {
        return ((ToolBar)this.getSkinnable()).getOrientation() == Orientation.VERTICAL ? Double.MAX_VALUE : this.snapSizeY(((ToolBar)this.getSkinnable()).prefHeight(-1.0));
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        ToolBar toolBar = (ToolBar)this.getSkinnable();
        if (toolBar.getOrientation() == Orientation.VERTICAL) {
            if (this.snapSizeY(toolBar.getHeight()) != this.previousHeight || this.needsUpdate) {
                ((VBox)this.box).setSpacing(this.getSpacing());
                ((VBox)this.box).setAlignment(this.getBoxAlignment());
                this.previousHeight = this.snapSizeY(toolBar.getHeight());
                this.addNodesToToolBar();
            }
        } else if (this.snapSizeX(toolBar.getWidth()) != this.previousWidth || this.needsUpdate) {
            ((HBox)this.box).setSpacing(this.getSpacing());
            ((HBox)this.box).setAlignment(this.getBoxAlignment());
            this.previousWidth = this.snapSizeX(toolBar.getWidth());
            this.addNodesToToolBar();
        }
        this.needsUpdate = false;
        double d5 = d3;
        double d6 = d4;
        if (((ToolBar)this.getSkinnable()).getOrientation() == Orientation.VERTICAL) {
            d6 -= this.overflow ? this.snapSizeY(this.overflowMenu.prefHeight(-1.0)) : 0.0;
        } else {
            d5 -= this.overflow ? this.snapSizeX(this.overflowMenu.prefWidth(-1.0)) : 0.0;
        }
        this.box.resize(d5, d6);
        this.positionInArea(this.box, d, d2, d5, d6, 0.0, HPos.CENTER, VPos.CENTER);
        if (this.overflow) {
            double d7 = this.snapSizeX(this.overflowMenu.prefWidth(-1.0));
            double d8 = this.snapSizeY(this.overflowMenu.prefHeight(-1.0));
            double d9 = d;
            double d10 = d;
            if (((ToolBar)this.getSkinnable()).getOrientation() == Orientation.VERTICAL) {
                HPos hPos;
                if (d5 == 0.0) {
                    d5 = this.savedPrefWidth;
                }
                d9 = HPos.LEFT.equals((Object)(hPos = ((VBox)this.box).getAlignment().getHpos())) ? d + Math.abs((d5 - d7) / 2.0) : (HPos.RIGHT.equals((Object)hPos) ? this.snapSizeX(toolBar.getWidth()) - this.snappedRightInset() - d5 + Math.abs((d5 - d7) / 2.0) : d + Math.abs((this.snapSizeX(toolBar.getWidth()) - d + this.snappedRightInset() - d7) / 2.0));
                d10 = this.snapSizeY(toolBar.getHeight()) - d8 - d2;
            } else {
                VPos vPos;
                if (d6 == 0.0) {
                    d6 = this.savedPrefHeight;
                }
                d10 = VPos.TOP.equals((Object)(vPos = ((HBox)this.box).getAlignment().getVpos())) ? d2 + Math.abs((d6 - d8) / 2.0) : (VPos.BOTTOM.equals((Object)vPos) ? this.snapSizeY(toolBar.getHeight()) - this.snappedBottomInset() - d6 + Math.abs((d6 - d8) / 2.0) : d2 + Math.abs((d6 - d8) / 2.0));
                d9 = this.snapSizeX(toolBar.getWidth()) - d7 - this.snappedRightInset();
            }
            this.overflowMenu.resize(d7, d8);
            this.positionInArea(this.overflowMenu, d9, d10, d7, d8, 0.0, HPos.CENTER, VPos.CENTER);
        }
    }

    private void initialize() {
        this.box = ((ToolBar)this.getSkinnable()).getOrientation() == Orientation.VERTICAL ? new VBox() : new HBox();
        this.box.getStyleClass().add("container");
        this.box.getChildren().addAll((Collection<Node>)((ToolBar)this.getSkinnable()).getItems());
        this.overflowMenu = new ToolBarOverflowMenu(this.overflowMenuItems);
        this.overflowMenu.setVisible(false);
        this.overflowMenu.setManaged(false);
        this.getChildren().clear();
        this.getChildren().add(this.box);
        this.getChildren().add(this.overflowMenu);
        this.previousWidth = 0.0;
        this.previousHeight = 0.0;
        this.savedPrefWidth = 0.0;
        this.savedPrefHeight = 0.0;
        this.needsUpdate = true;
        ((ToolBar)this.getSkinnable()).requestLayout();
    }

    private void addNodesToToolBar() {
        ToolBar toolBar = (ToolBar)this.getSkinnable();
        double d = 0.0;
        d = ((ToolBar)this.getSkinnable()).getOrientation() == Orientation.VERTICAL ? this.snapSizeY(toolBar.getHeight()) - this.snappedTopInset() - this.snappedBottomInset() + this.getSpacing() : this.snapSizeX(toolBar.getWidth()) - this.snappedLeftInset() - this.snappedRightInset() + this.getSpacing();
        double d2 = 0.0;
        boolean bl = false;
        for (Node node : ((ToolBar)this.getSkinnable()).getItems()) {
            if (!node.isManaged()) continue;
            d2 = ((ToolBar)this.getSkinnable()).getOrientation() == Orientation.VERTICAL ? (d2 += this.snapSizeY(node.prefHeight(-1.0)) + this.getSpacing()) : (d2 += this.snapSizeX(node.prefWidth(-1.0)) + this.getSpacing());
            if (!(d2 > d)) continue;
            bl = true;
            break;
        }
        if (bl) {
            d = ((ToolBar)this.getSkinnable()).getOrientation() == Orientation.VERTICAL ? (d -= this.snapSizeY(this.overflowMenu.prefHeight(-1.0))) : (d -= this.snapSizeX(this.overflowMenu.prefWidth(-1.0)));
            d -= this.getSpacing();
        }
        d2 = 0.0;
        this.overflowMenuItems.clear();
        this.box.getChildren().clear();
        Object object = ((ToolBar)this.getSkinnable()).getItems().iterator();
        while (object.hasNext()) {
            String string;
            EventTarget eventTarget;
            Node node;
            node = (Node)object.next();
            node.getStyleClass().remove("menu-item");
            node.getStyleClass().remove("custom-menu-item");
            if (node.isManaged()) {
                d2 = ((ToolBar)this.getSkinnable()).getOrientation() == Orientation.VERTICAL ? (d2 += this.snapSizeY(node.prefHeight(-1.0)) + this.getSpacing()) : (d2 += this.snapSizeX(node.prefWidth(-1.0)) + this.getSpacing());
            }
            if (d2 <= d) {
                this.box.getChildren().add(node);
                continue;
            }
            if (node.isFocused()) {
                if (!this.box.getChildren().isEmpty()) {
                    eventTarget = this.engine.selectLast();
                    if (eventTarget != null) {
                        ((Node)eventTarget).requestFocus();
                    }
                } else {
                    this.overflowMenu.requestFocus();
                }
            }
            if (node instanceof Separator) {
                this.overflowMenuItems.add(new SeparatorMenuItem());
                continue;
            }
            eventTarget = new CustomMenuItem(node);
            switch (string = node.getTypeSelector()) {
                case "Button": 
                case "Hyperlink": 
                case "Label": {
                    ((CustomMenuItem)eventTarget).setHideOnClick(true);
                    break;
                }
                default: {
                    ((CustomMenuItem)eventTarget).setHideOnClick(false);
                }
            }
            this.overflowMenuItems.add((MenuItem)eventTarget);
        }
        boolean bl2 = this.overflow = this.overflowMenuItems.size() > 0;
        if (!this.overflow && this.overflowMenu.isFocused() && (object = this.engine.selectLast()) != null) {
            ((Node)object).requestFocus();
        }
        this.overflowMenu.setVisible(this.overflow);
        this.overflowMenu.setManaged(this.overflow);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return ToolBarSkin.getClassCssMetaData();
    }

    @Override
    protected Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case OVERFLOW_BUTTON: {
                return this.overflowMenu;
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    @Override
    protected void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
        switch (accessibleAction) {
            case SHOW_MENU: {
                this.overflowMenu.fire();
                break;
            }
            default: {
                super.executeAccessibleAction(accessibleAction, objectArray);
            }
        }
    }

    private static class StyleableProperties {
        private static final CssMetaData<ToolBar, Number> SPACING = new CssMetaData<ToolBar, Number>("-fx-spacing", SizeConverter.getInstance(), (Number)0.0){

            @Override
            public boolean isSettable(ToolBar toolBar) {
                ToolBarSkin toolBarSkin = (ToolBarSkin)toolBar.getSkin();
                return toolBarSkin.spacing == null || !toolBarSkin.spacing.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(ToolBar toolBar) {
                ToolBarSkin toolBarSkin = (ToolBarSkin)toolBar.getSkin();
                return (StyleableProperty)((Object)toolBarSkin.spacingProperty());
            }
        };
        private static final CssMetaData<ToolBar, Pos> ALIGNMENT = new CssMetaData<ToolBar, Pos>("-fx-alignment", new EnumConverter<Pos>(Pos.class), Pos.TOP_LEFT){

            @Override
            public boolean isSettable(ToolBar toolBar) {
                ToolBarSkin toolBarSkin = (ToolBarSkin)toolBar.getSkin();
                return toolBarSkin.boxAlignment == null || !toolBarSkin.boxAlignment.isBound();
            }

            @Override
            public StyleableProperty<Pos> getStyleableProperty(ToolBar toolBar) {
                ToolBarSkin toolBarSkin = (ToolBarSkin)toolBar.getSkin();
                return (StyleableProperty)((Object)toolBarSkin.boxAlignmentProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(SkinBase.getClassCssMetaData());
            String string = ALIGNMENT.getProperty();
            int n = arrayList.size();
            for (int i = 0; i < n; ++i) {
                CssMetaData cssMetaData = (CssMetaData)arrayList.get(i);
                if (!string.equals(cssMetaData.getProperty())) continue;
                arrayList.remove(cssMetaData);
            }
            arrayList.add(SPACING);
            arrayList.add(ALIGNMENT);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }

    class ToolBarOverflowMenu
    extends StackPane {
        private StackPane downArrow;
        private ContextMenu popup;
        private ObservableList<MenuItem> menuItems;

        public ToolBarOverflowMenu(ObservableList<MenuItem> observableList) {
            this.getStyleClass().setAll((String[])new String[]{"tool-bar-overflow-button"});
            this.setAccessibleRole(AccessibleRole.BUTTON);
            this.setAccessibleText(ControlResources.getString("Accessibility.title.ToolBar.OverflowButton"));
            this.setFocusTraversable(true);
            this.menuItems = observableList;
            this.downArrow = new StackPane();
            this.downArrow.getStyleClass().setAll((String[])new String[]{"arrow"});
            this.downArrow.setOnMousePressed(mouseEvent -> this.fire());
            this.setOnKeyPressed(keyEvent -> {
                if (KeyCode.SPACE.equals((Object)keyEvent.getCode())) {
                    if (!this.popup.isShowing()) {
                        this.popup.getItems().clear();
                        this.popup.getItems().addAll((Collection<MenuItem>)this.menuItems);
                        this.popup.show(this.downArrow, Side.BOTTOM, 0.0, 0.0);
                    }
                    keyEvent.consume();
                } else if (KeyCode.ESCAPE.equals((Object)keyEvent.getCode())) {
                    if (this.popup.isShowing()) {
                        this.popup.hide();
                    }
                    keyEvent.consume();
                } else if (KeyCode.ENTER.equals((Object)keyEvent.getCode())) {
                    this.fire();
                    keyEvent.consume();
                }
            });
            this.visibleProperty().addListener((observableValue, bl, bl2) -> {
                if (bl2.booleanValue() && ToolBarSkin.this.box.getChildren().isEmpty()) {
                    this.setFocusTraversable(true);
                }
            });
            this.popup = new ContextMenu();
            this.setVisible(false);
            this.setManaged(false);
            this.getChildren().add(this.downArrow);
        }

        private void fire() {
            if (this.popup.isShowing()) {
                this.popup.hide();
            } else {
                this.popup.getItems().clear();
                this.popup.getItems().addAll((Collection<MenuItem>)this.menuItems);
                this.popup.show(this.downArrow, Side.BOTTOM, 0.0, 0.0);
            }
        }

        @Override
        protected double computePrefWidth(double d) {
            return this.snappedLeftInset() + this.snappedRightInset();
        }

        @Override
        protected double computePrefHeight(double d) {
            return this.snappedTopInset() + this.snappedBottomInset();
        }

        @Override
        protected void layoutChildren() {
            double d = this.snapSize(this.downArrow.prefWidth(-1.0));
            double d2 = this.snapSize(this.downArrow.prefHeight(-1.0));
            double d3 = (this.snapSize(this.getWidth()) - d) / 2.0;
            double d4 = (this.snapSize(this.getHeight()) - d2) / 2.0;
            if (((ToolBar)ToolBarSkin.this.getSkinnable()).getOrientation() == Orientation.VERTICAL) {
                this.downArrow.setRotate(0.0);
            }
            this.downArrow.resize(d, d2);
            this.positionInArea(this.downArrow, d3, d4, d, d2, 0.0, HPos.CENTER, VPos.CENTER);
        }

        @Override
        public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
            switch (accessibleAction) {
                case FIRE: {
                    this.fire();
                    break;
                }
                default: {
                    super.executeAccessibleAction(accessibleAction, new Object[0]);
                }
            }
        }
    }
}

