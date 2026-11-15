/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.ParentHelper;
import com.sun.javafx.scene.control.Properties;
import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.ScrollPaneBehavior;
import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import com.sun.javafx.util.Utils;
import java.util.function.Consumer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventDispatcher;
import javafx.event.EventHandler;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

public class ScrollPaneSkin
extends SkinBase<ScrollPane> {
    private static final double DEFAULT_PREF_SIZE = 100.0;
    private static final double DEFAULT_MIN_SIZE = 36.0;
    private static final double DEFAULT_SB_BREADTH = 12.0;
    private static final double DEFAULT_EMBEDDED_SB_BREADTH = 8.0;
    private static final double PAN_THRESHOLD = 0.5;
    private Node scrollNode;
    private final BehaviorBase<ScrollPane> behavior;
    private double nodeWidth;
    private double nodeHeight;
    private boolean nodeSizeInvalid = true;
    private double posX;
    private double posY;
    private boolean hsbvis;
    private boolean vsbvis;
    private double hsbHeight;
    private double vsbWidth;
    private StackPane viewRect;
    private StackPane viewContent;
    private double contentWidth;
    private double contentHeight;
    private StackPane corner;
    ScrollBar hsb;
    ScrollBar vsb;
    double pressX;
    double pressY;
    double ohvalue;
    double ovvalue;
    private Cursor saveCursor = null;
    private boolean dragDetected = false;
    private boolean touchDetected = false;
    private boolean mouseDown = false;
    Rectangle clipRect;
    Timeline sbTouchTimeline;
    KeyFrame sbTouchKF1;
    KeyFrame sbTouchKF2;
    Timeline contentsToViewTimeline;
    KeyFrame contentsToViewKF1;
    KeyFrame contentsToViewKF2;
    KeyFrame contentsToViewKF3;
    private boolean tempVisibility;
    private final InvalidationListener nodeListener = new InvalidationListener(){

        @Override
        public void invalidated(Observable observable) {
            if (!ScrollPaneSkin.this.nodeSizeInvalid) {
                Bounds bounds = ScrollPaneSkin.this.scrollNode.getLayoutBounds();
                double d = bounds.getWidth();
                double d2 = bounds.getHeight();
                if (ScrollPaneSkin.this.vsbvis != ScrollPaneSkin.this.determineVerticalSBVisible() || ScrollPaneSkin.this.hsbvis != ScrollPaneSkin.this.determineHorizontalSBVisible() || d != 0.0 && ScrollPaneSkin.this.nodeWidth != d || d2 != 0.0 && ScrollPaneSkin.this.nodeHeight != d2) {
                    ((ScrollPane)ScrollPaneSkin.this.getSkinnable()).requestLayout();
                } else if (!ScrollPaneSkin.this.dragDetected) {
                    ScrollPaneSkin.this.updateVerticalSB();
                    ScrollPaneSkin.this.updateHorizontalSB();
                }
            }
        }
    };
    private final ChangeListener<Bounds> boundsChangeListener = new ChangeListener<Bounds>(){

        @Override
        public void changed(ObservableValue<? extends Bounds> observableValue, Bounds bounds, Bounds bounds2) {
            double d;
            double d2;
            double d3;
            double d4 = bounds.getHeight();
            double d5 = bounds2.getHeight();
            if (d4 > 0.0 && d4 != d5) {
                d3 = ScrollPaneSkin.this.snapPositionY(ScrollPaneSkin.this.snappedTopInset() - ScrollPaneSkin.this.posY / (ScrollPaneSkin.this.vsb.getMax() - ScrollPaneSkin.this.vsb.getMin()) * (d4 - ScrollPaneSkin.this.contentHeight));
                d = d3 / (d2 = ScrollPaneSkin.this.snapPositionY(ScrollPaneSkin.this.snappedTopInset() - ScrollPaneSkin.this.posY / (ScrollPaneSkin.this.vsb.getMax() - ScrollPaneSkin.this.vsb.getMin()) * (d5 - ScrollPaneSkin.this.contentHeight))) * ScrollPaneSkin.this.vsb.getValue();
                if (d < 0.0) {
                    ScrollPaneSkin.this.vsb.setValue(0.0);
                } else if (d < 1.0) {
                    ScrollPaneSkin.this.vsb.setValue(d);
                } else if (d > 1.0) {
                    ScrollPaneSkin.this.vsb.setValue(1.0);
                }
            }
            d3 = bounds.getWidth();
            d2 = bounds2.getWidth();
            if (d3 > 0.0 && d3 != d2) {
                double d6;
                d = ScrollPaneSkin.this.snapPositionX(ScrollPaneSkin.this.snappedLeftInset() - ScrollPaneSkin.this.posX / (ScrollPaneSkin.this.hsb.getMax() - ScrollPaneSkin.this.hsb.getMin()) * (d3 - ScrollPaneSkin.this.contentWidth));
                double d7 = d / (d6 = ScrollPaneSkin.this.snapPositionX(ScrollPaneSkin.this.snappedLeftInset() - ScrollPaneSkin.this.posX / (ScrollPaneSkin.this.hsb.getMax() - ScrollPaneSkin.this.hsb.getMin()) * (d2 - ScrollPaneSkin.this.contentWidth))) * ScrollPaneSkin.this.hsb.getValue();
                if (d7 < 0.0) {
                    ScrollPaneSkin.this.hsb.setValue(0.0);
                } else if (d7 < 1.0) {
                    ScrollPaneSkin.this.hsb.setValue(d7);
                } else if (d7 > 1.0) {
                    ScrollPaneSkin.this.hsb.setValue(1.0);
                }
            }
        }
    };
    private DoubleProperty contentPosX;
    private DoubleProperty contentPosY;

    public ScrollPaneSkin(ScrollPane scrollPane) {
        super(scrollPane);
        this.behavior = new ScrollPaneBehavior(scrollPane);
        this.initialize();
        Consumer<ObservableValue<?>> consumer = observableValue -> ((ScrollPane)this.getSkinnable()).requestLayout();
        this.registerChangeListener(scrollPane.contentProperty(), observableValue -> {
            if (this.scrollNode != ((ScrollPane)this.getSkinnable()).getContent()) {
                if (this.scrollNode != null) {
                    this.scrollNode.layoutBoundsProperty().removeListener(this.nodeListener);
                    this.scrollNode.layoutBoundsProperty().removeListener(this.boundsChangeListener);
                    this.viewContent.getChildren().remove(this.scrollNode);
                }
                this.scrollNode = ((ScrollPane)this.getSkinnable()).getContent();
                if (this.scrollNode != null) {
                    this.nodeWidth = this.snapSizeX(this.scrollNode.getLayoutBounds().getWidth());
                    this.nodeHeight = this.snapSizeY(this.scrollNode.getLayoutBounds().getHeight());
                    this.viewContent.getChildren().setAll((Node[])new Node[]{this.scrollNode});
                    this.scrollNode.layoutBoundsProperty().addListener(this.nodeListener);
                    this.scrollNode.layoutBoundsProperty().addListener(this.boundsChangeListener);
                }
            }
            ((ScrollPane)this.getSkinnable()).requestLayout();
        });
        this.registerChangeListener(scrollPane.fitToWidthProperty(), observableValue -> {
            ((ScrollPane)this.getSkinnable()).requestLayout();
            this.viewRect.requestLayout();
        });
        this.registerChangeListener(scrollPane.fitToHeightProperty(), observableValue -> {
            ((ScrollPane)this.getSkinnable()).requestLayout();
            this.viewRect.requestLayout();
        });
        this.registerChangeListener(scrollPane.hbarPolicyProperty(), observableValue -> ((ScrollPane)this.getSkinnable()).requestLayout());
        this.registerChangeListener(scrollPane.vbarPolicyProperty(), observableValue -> ((ScrollPane)this.getSkinnable()).requestLayout());
        this.registerChangeListener(scrollPane.hvalueProperty(), observableValue -> this.hsb.setValue(((ScrollPane)this.getSkinnable()).getHvalue()));
        this.registerChangeListener(scrollPane.hmaxProperty(), observableValue -> this.hsb.setMax(((ScrollPane)this.getSkinnable()).getHmax()));
        this.registerChangeListener(scrollPane.hminProperty(), observableValue -> this.hsb.setMin(((ScrollPane)this.getSkinnable()).getHmin()));
        this.registerChangeListener(scrollPane.vvalueProperty(), observableValue -> this.vsb.setValue(((ScrollPane)this.getSkinnable()).getVvalue()));
        this.registerChangeListener(scrollPane.vmaxProperty(), observableValue -> this.vsb.setMax(((ScrollPane)this.getSkinnable()).getVmax()));
        this.registerChangeListener(scrollPane.vminProperty(), observableValue -> this.vsb.setMin(((ScrollPane)this.getSkinnable()).getVmin()));
        this.registerChangeListener(scrollPane.prefViewportWidthProperty(), consumer);
        this.registerChangeListener(scrollPane.prefViewportHeightProperty(), consumer);
        this.registerChangeListener(scrollPane.minViewportWidthProperty(), consumer);
        this.registerChangeListener(scrollPane.minViewportHeightProperty(), consumer);
    }

    private final void setContentPosX(double d) {
        this.contentPosXProperty().set(d);
    }

    private final double getContentPosX() {
        return this.contentPosX == null ? 0.0 : this.contentPosX.get();
    }

    private final DoubleProperty contentPosXProperty() {
        if (this.contentPosX == null) {
            this.contentPosX = new DoublePropertyBase(){

                @Override
                protected void invalidated() {
                    ScrollPaneSkin.this.hsb.setValue(ScrollPaneSkin.this.getContentPosX());
                    ((ScrollPane)ScrollPaneSkin.this.getSkinnable()).requestLayout();
                }

                @Override
                public Object getBean() {
                    return ScrollPaneSkin.this;
                }

                @Override
                public String getName() {
                    return "contentPosX";
                }
            };
        }
        return this.contentPosX;
    }

    private final void setContentPosY(double d) {
        this.contentPosYProperty().set(d);
    }

    private final double getContentPosY() {
        return this.contentPosY == null ? 0.0 : this.contentPosY.get();
    }

    private final DoubleProperty contentPosYProperty() {
        if (this.contentPosY == null) {
            this.contentPosY = new DoublePropertyBase(){

                @Override
                protected void invalidated() {
                    ScrollPaneSkin.this.vsb.setValue(ScrollPaneSkin.this.getContentPosY());
                    ((ScrollPane)ScrollPaneSkin.this.getSkinnable()).requestLayout();
                }

                @Override
                public Object getBean() {
                    return ScrollPaneSkin.this;
                }

                @Override
                public String getName() {
                    return "contentPosY";
                }
            };
        }
        return this.contentPosY;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }

    public final ScrollBar getHorizontalScrollBar() {
        return this.hsb;
    }

    public final ScrollBar getVerticalScrollBar() {
        return this.vsb;
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        ScrollPane scrollPane = (ScrollPane)this.getSkinnable();
        double d6 = this.computeVsbSizeHint(scrollPane);
        double d7 = d6 + this.snappedLeftInset() + this.snappedRightInset();
        if (scrollPane.getPrefViewportWidth() > 0.0) {
            return scrollPane.getPrefViewportWidth() + d7;
        }
        if (scrollPane.getContent() != null) {
            return scrollPane.getContent().prefWidth(d) + d7;
        }
        return Math.max(d7, 100.0);
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        ScrollPane scrollPane = (ScrollPane)this.getSkinnable();
        double d6 = this.computeHsbSizeHint(scrollPane);
        double d7 = d6 + this.snappedTopInset() + this.snappedBottomInset();
        if (scrollPane.getPrefViewportHeight() > 0.0) {
            return scrollPane.getPrefViewportHeight() + d7;
        }
        if (scrollPane.getContent() != null) {
            return scrollPane.getContent().prefHeight(d) + d7;
        }
        return Math.max(d7, 100.0);
    }

    @Override
    protected double computeMinWidth(double d, double d2, double d3, double d4, double d5) {
        ScrollPane scrollPane = (ScrollPane)this.getSkinnable();
        double d6 = this.computeVsbSizeHint(scrollPane);
        double d7 = d6 + this.snappedLeftInset() + this.snappedRightInset();
        if (scrollPane.getMinViewportWidth() > 0.0) {
            return scrollPane.getMinViewportWidth() + d7;
        }
        double d8 = this.corner.minWidth(-1.0);
        return d8 > 0.0 ? 3.0 * d8 : 36.0;
    }

    @Override
    protected double computeMinHeight(double d, double d2, double d3, double d4, double d5) {
        ScrollPane scrollPane = (ScrollPane)this.getSkinnable();
        double d6 = this.computeHsbSizeHint(scrollPane);
        double d7 = d6 + this.snappedTopInset() + this.snappedBottomInset();
        if (scrollPane.getMinViewportHeight() > 0.0) {
            return scrollPane.getMinViewportHeight() + d7;
        }
        double d8 = this.corner.minHeight(-1.0);
        return d8 > 0.0 ? 3.0 * d8 : 36.0;
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        ScrollPane scrollPane = (ScrollPane)this.getSkinnable();
        Insets insets = scrollPane.getPadding();
        double d5 = this.snapSizeX(insets.getRight());
        double d6 = this.snapSizeX(insets.getLeft());
        double d7 = this.snapSizeY(insets.getTop());
        double d8 = this.snapSizeY(insets.getBottom());
        this.vsb.setMin(scrollPane.getVmin());
        this.vsb.setMax(scrollPane.getVmax());
        this.hsb.setMin(scrollPane.getHmin());
        this.hsb.setMax(scrollPane.getHmax());
        this.contentWidth = d3;
        this.contentHeight = d4;
        double d9 = 0.0;
        double d10 = 0.0;
        this.computeScrollNodeSize(this.contentWidth, this.contentHeight);
        this.computeScrollBarSize();
        for (int i = 0; i < 2; ++i) {
            this.vsbvis = this.determineVerticalSBVisible();
            this.hsbvis = this.determineHorizontalSBVisible();
            if (this.vsbvis && !Properties.IS_TOUCH_SUPPORTED) {
                this.contentWidth = d3 - this.vsbWidth;
            }
            d9 = d3 + d6 + d5 - (this.vsbvis ? this.vsbWidth : 0.0);
            if (this.hsbvis && !Properties.IS_TOUCH_SUPPORTED) {
                this.contentHeight = d4 - this.hsbHeight;
            }
            d10 = d4 + d7 + d8 - (this.hsbvis ? this.hsbHeight : 0.0);
        }
        if (this.scrollNode != null && this.scrollNode.isResizable()) {
            if (this.vsbvis && this.hsbvis) {
                this.computeScrollNodeSize(this.contentWidth, this.contentHeight);
            } else if (this.hsbvis && !this.vsbvis) {
                this.computeScrollNodeSize(this.contentWidth, this.contentHeight);
                this.vsbvis = this.determineVerticalSBVisible();
                if (this.vsbvis) {
                    this.contentWidth -= this.vsbWidth;
                    d9 -= this.vsbWidth;
                    this.computeScrollNodeSize(this.contentWidth, this.contentHeight);
                }
            } else if (this.vsbvis && !this.hsbvis) {
                this.computeScrollNodeSize(this.contentWidth, this.contentHeight);
                this.hsbvis = this.determineHorizontalSBVisible();
                if (this.hsbvis) {
                    this.contentHeight -= this.hsbHeight;
                    d10 -= this.hsbHeight;
                    this.computeScrollNodeSize(this.contentWidth, this.contentHeight);
                }
            }
        }
        double d11 = this.snappedLeftInset() - d6;
        double d12 = this.snappedTopInset() - d7;
        this.vsb.setVisible(this.vsbvis);
        if (this.vsbvis) {
            this.vsb.resizeRelocate(this.snappedLeftInset() + d3 - this.vsbWidth + (d5 < 1.0 ? 0.0 : d5 - 1.0), d12, this.vsbWidth, d10);
        }
        this.updateVerticalSB();
        this.hsb.setVisible(this.hsbvis);
        if (this.hsbvis) {
            this.hsb.resizeRelocate(d11, this.snappedTopInset() + d4 - this.hsbHeight + (d8 < 1.0 ? 0.0 : d8 - 1.0), d9, this.hsbHeight);
        }
        this.updateHorizontalSB();
        this.viewRect.resizeRelocate(this.snappedLeftInset(), this.snappedTopInset(), this.snapSizeX(this.contentWidth), this.snapSizeY(this.contentHeight));
        this.resetClip();
        if (this.vsbvis && this.hsbvis) {
            this.corner.setVisible(true);
            double d13 = this.vsbWidth;
            double d14 = this.hsbHeight;
            this.corner.resizeRelocate(this.snapPositionX(this.vsb.getLayoutX()), this.snapPositionY(this.hsb.getLayoutY()), this.snapSizeX(d13), this.snapSizeY(d14));
        } else {
            this.corner.setVisible(false);
        }
        scrollPane.setViewportBounds(new BoundingBox(this.snapPositionX(this.viewContent.getLayoutX()), this.snapPositionY(this.viewContent.getLayoutY()), this.snapSizeX(this.contentWidth), this.snapSizeY(this.contentHeight)));
    }

    @Override
    protected Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case VERTICAL_SCROLLBAR: {
                return this.vsb;
            }
            case HORIZONTAL_SCROLLBAR: {
                return this.hsb;
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    private void initialize() {
        ScrollPane scrollPane = (ScrollPane)this.getSkinnable();
        this.scrollNode = scrollPane.getContent();
        ParentTraversalEngine parentTraversalEngine = new ParentTraversalEngine((Parent)((Object)this.getSkinnable()));
        parentTraversalEngine.addTraverseListener((node, bounds) -> this.scrollBoundsIntoView(bounds));
        ParentHelper.setTraversalEngine((Parent)((Object)this.getSkinnable()), parentTraversalEngine);
        if (this.scrollNode != null) {
            this.scrollNode.layoutBoundsProperty().addListener(this.nodeListener);
            this.scrollNode.layoutBoundsProperty().addListener(this.boundsChangeListener);
        }
        this.viewRect = new StackPane(){

            @Override
            protected void layoutChildren() {
                ScrollPaneSkin.this.viewContent.resize(this.getWidth(), this.getHeight());
            }
        };
        this.viewRect.setManaged(false);
        this.viewRect.setCache(true);
        this.viewRect.getStyleClass().add("viewport");
        this.clipRect = new Rectangle();
        this.viewRect.setClip(this.clipRect);
        this.hsb = new ScrollBar();
        this.vsb = new ScrollBar();
        this.vsb.setOrientation(Orientation.VERTICAL);
        EventHandler<MouseEvent> eventHandler = mouseEvent -> {
            if (((ScrollPane)this.getSkinnable()).isFocusTraversable()) {
                ((ScrollPane)this.getSkinnable()).requestFocus();
            }
        };
        this.hsb.addEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler);
        this.vsb.addEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler);
        this.corner = new StackPane();
        this.corner.getStyleClass().setAll((String[])new String[]{"corner"});
        this.viewContent = new StackPane(){

            @Override
            public void requestLayout() {
                ScrollPaneSkin.this.nodeSizeInvalid = true;
                super.requestLayout();
                ((ScrollPane)ScrollPaneSkin.this.getSkinnable()).requestLayout();
            }

            @Override
            protected void layoutChildren() {
                if (ScrollPaneSkin.this.nodeSizeInvalid) {
                    ScrollPaneSkin.this.computeScrollNodeSize(this.getWidth(), this.getHeight());
                }
                if (ScrollPaneSkin.this.scrollNode != null && ScrollPaneSkin.this.scrollNode.isResizable()) {
                    ScrollPaneSkin.this.scrollNode.resize(this.snapSize(ScrollPaneSkin.this.nodeWidth), this.snapSize(ScrollPaneSkin.this.nodeHeight));
                    if (ScrollPaneSkin.this.vsbvis != ScrollPaneSkin.this.determineVerticalSBVisible() || ScrollPaneSkin.this.hsbvis != ScrollPaneSkin.this.determineHorizontalSBVisible()) {
                        ((ScrollPane)ScrollPaneSkin.this.getSkinnable()).requestLayout();
                    }
                }
                if (ScrollPaneSkin.this.scrollNode != null) {
                    ScrollPaneSkin.this.scrollNode.relocate(0.0, 0.0);
                }
            }
        };
        this.viewRect.getChildren().add(this.viewContent);
        if (this.scrollNode != null) {
            this.viewContent.getChildren().add(this.scrollNode);
            this.viewRect.nodeOrientationProperty().bind(this.scrollNode.nodeOrientationProperty());
        }
        this.getChildren().clear();
        this.getChildren().addAll((Node[])new Node[]{this.viewRect, this.vsb, this.hsb, this.corner});
        InvalidationListener invalidationListener = observable -> {
            this.posY = !Properties.IS_TOUCH_SUPPORTED ? Utils.clamp(((ScrollPane)this.getSkinnable()).getVmin(), this.vsb.getValue(), ((ScrollPane)this.getSkinnable()).getVmax()) : this.vsb.getValue();
            this.updatePosY();
        };
        this.vsb.valueProperty().addListener(invalidationListener);
        InvalidationListener invalidationListener2 = observable -> {
            this.posX = !Properties.IS_TOUCH_SUPPORTED ? Utils.clamp(((ScrollPane)this.getSkinnable()).getHmin(), this.hsb.getValue(), ((ScrollPane)this.getSkinnable()).getHmax()) : this.hsb.getValue();
            this.updatePosX();
        };
        this.hsb.valueProperty().addListener(invalidationListener2);
        this.viewRect.setOnMousePressed(mouseEvent -> {
            this.mouseDown = true;
            if (Properties.IS_TOUCH_SUPPORTED) {
                this.startSBReleasedAnimation();
            }
            this.pressX = mouseEvent.getX();
            this.pressY = mouseEvent.getY();
            this.ohvalue = this.hsb.getValue();
            this.ovvalue = this.vsb.getValue();
        });
        this.viewRect.setOnDragDetected(mouseEvent -> {
            if (Properties.IS_TOUCH_SUPPORTED) {
                this.startSBReleasedAnimation();
            }
            if (((ScrollPane)this.getSkinnable()).isPannable()) {
                this.dragDetected = true;
                if (this.saveCursor == null) {
                    this.saveCursor = ((ScrollPane)this.getSkinnable()).getCursor();
                    if (this.saveCursor == null) {
                        this.saveCursor = Cursor.DEFAULT;
                    }
                    ((ScrollPane)this.getSkinnable()).setCursor(Cursor.MOVE);
                    ((ScrollPane)this.getSkinnable()).requestLayout();
                }
            }
        });
        this.viewRect.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
            this.mouseDown = false;
            if (this.dragDetected) {
                if (this.saveCursor != null) {
                    ((ScrollPane)this.getSkinnable()).setCursor(this.saveCursor);
                    this.saveCursor = null;
                    ((ScrollPane)this.getSkinnable()).requestLayout();
                }
                this.dragDetected = false;
            }
            if ((this.posY > ((ScrollPane)this.getSkinnable()).getVmax() || this.posY < ((ScrollPane)this.getSkinnable()).getVmin() || this.posX > ((ScrollPane)this.getSkinnable()).getHmax() || this.posX < ((ScrollPane)this.getSkinnable()).getHmin()) && !this.touchDetected) {
                this.startContentsToViewport();
            }
        });
        this.viewRect.setOnMouseDragged(mouseEvent -> {
            if (Properties.IS_TOUCH_SUPPORTED) {
                this.startSBReleasedAnimation();
            }
            if (((ScrollPane)this.getSkinnable()).isPannable() || Properties.IS_TOUCH_SUPPORTED) {
                double d;
                double d2 = this.pressX - mouseEvent.getX();
                double d3 = this.pressY - mouseEvent.getY();
                if (this.hsb.getVisibleAmount() > 0.0 && this.hsb.getVisibleAmount() < this.hsb.getMax() && Math.abs(d2) > 0.5) {
                    if (this.isReverseNodeOrientation()) {
                        d2 = -d2;
                    }
                    d = this.ohvalue + d2 / (this.nodeWidth - this.viewRect.getWidth()) * (this.hsb.getMax() - this.hsb.getMin());
                    if (!Properties.IS_TOUCH_SUPPORTED) {
                        if (d > this.hsb.getMax()) {
                            d = this.hsb.getMax();
                        } else if (d < this.hsb.getMin()) {
                            d = this.hsb.getMin();
                        }
                        this.hsb.setValue(d);
                    } else {
                        this.hsb.setValue(d);
                    }
                }
                if (this.vsb.getVisibleAmount() > 0.0 && this.vsb.getVisibleAmount() < this.vsb.getMax() && Math.abs(d3) > 0.5) {
                    d = this.ovvalue + d3 / (this.nodeHeight - this.viewRect.getHeight()) * (this.vsb.getMax() - this.vsb.getMin());
                    if (!Properties.IS_TOUCH_SUPPORTED) {
                        if (d > this.vsb.getMax()) {
                            d = this.vsb.getMax();
                        } else if (d < this.vsb.getMin()) {
                            d = this.vsb.getMin();
                        }
                        this.vsb.setValue(d);
                    } else {
                        this.vsb.setValue(d);
                    }
                }
            }
            mouseEvent.consume();
        });
        EventDispatcher eventDispatcher = (event, eventDispatchChain) -> event;
        EventDispatcher eventDispatcher2 = this.hsb.getEventDispatcher();
        this.hsb.setEventDispatcher((event, eventDispatchChain) -> {
            if (event.getEventType() == ScrollEvent.SCROLL && !((ScrollEvent)event).isDirect()) {
                eventDispatchChain = eventDispatchChain.prepend(eventDispatcher);
                eventDispatchChain = eventDispatchChain.prepend(eventDispatcher2);
                return eventDispatchChain.dispatchEvent(event);
            }
            return eventDispatcher2.dispatchEvent(event, eventDispatchChain);
        });
        EventDispatcher eventDispatcher3 = this.vsb.getEventDispatcher();
        this.vsb.setEventDispatcher((event, eventDispatchChain) -> {
            if (event.getEventType() == ScrollEvent.SCROLL && !((ScrollEvent)event).isDirect()) {
                eventDispatchChain = eventDispatchChain.prepend(eventDispatcher);
                eventDispatchChain = eventDispatchChain.prepend(eventDispatcher3);
                return eventDispatchChain.dispatchEvent(event);
            }
            return eventDispatcher3.dispatchEvent(event, eventDispatchChain);
        });
        this.viewRect.addEventHandler(ScrollEvent.SCROLL, scrollEvent -> {
            double d;
            double d2;
            double d3;
            if (Properties.IS_TOUCH_SUPPORTED) {
                this.startSBReleasedAnimation();
            }
            if (this.vsb.getVisibleAmount() < this.vsb.getMax()) {
                d3 = ((ScrollPane)this.getSkinnable()).getVmax() - ((ScrollPane)this.getSkinnable()).getVmin();
                d2 = this.nodeHeight > 0.0 ? d3 / this.nodeHeight : 0.0;
                d = this.vsb.getValue() + -scrollEvent.getDeltaY() * d2;
                if (!Properties.IS_TOUCH_SUPPORTED) {
                    if (scrollEvent.getDeltaY() > 0.0 && this.vsb.getValue() > this.vsb.getMin() || scrollEvent.getDeltaY() < 0.0 && this.vsb.getValue() < this.vsb.getMax()) {
                        this.vsb.setValue(d);
                        scrollEvent.consume();
                    }
                } else if (!scrollEvent.isInertia() || scrollEvent.isInertia() && (this.contentsToViewTimeline == null || this.contentsToViewTimeline.getStatus() == Animation.Status.STOPPED)) {
                    this.vsb.setValue(d);
                    if ((d > this.vsb.getMax() || d < this.vsb.getMin()) && !this.mouseDown && !this.touchDetected) {
                        this.startContentsToViewport();
                    }
                    scrollEvent.consume();
                }
            }
            if (this.hsb.getVisibleAmount() < this.hsb.getMax()) {
                d3 = ((ScrollPane)this.getSkinnable()).getHmax() - ((ScrollPane)this.getSkinnable()).getHmin();
                d2 = this.nodeWidth > 0.0 ? d3 / this.nodeWidth : 0.0;
                d = this.hsb.getValue() + -scrollEvent.getDeltaX() * d2;
                if (!Properties.IS_TOUCH_SUPPORTED) {
                    if (scrollEvent.getDeltaX() > 0.0 && this.hsb.getValue() > this.hsb.getMin() || scrollEvent.getDeltaX() < 0.0 && this.hsb.getValue() < this.hsb.getMax()) {
                        this.hsb.setValue(d);
                        scrollEvent.consume();
                    }
                } else if (!scrollEvent.isInertia() || scrollEvent.isInertia() && (this.contentsToViewTimeline == null || this.contentsToViewTimeline.getStatus() == Animation.Status.STOPPED)) {
                    this.hsb.setValue(d);
                    if ((d > this.hsb.getMax() || d < this.hsb.getMin()) && !this.mouseDown && !this.touchDetected) {
                        this.startContentsToViewport();
                    }
                    scrollEvent.consume();
                }
            }
        });
        ((ScrollPane)this.getSkinnable()).addEventHandler(TouchEvent.TOUCH_PRESSED, touchEvent -> {
            this.touchDetected = true;
            this.startSBReleasedAnimation();
            touchEvent.consume();
        });
        ((ScrollPane)this.getSkinnable()).addEventHandler(TouchEvent.TOUCH_RELEASED, touchEvent -> {
            this.touchDetected = false;
            touchEvent.consume();
        });
        this.consumeMouseEvents(false);
        this.hsb.setValue(scrollPane.getHvalue());
        this.vsb.setValue(scrollPane.getVvalue());
    }

    void scrollBoundsIntoView(Bounds bounds) {
        double d;
        double d2 = 0.0;
        double d3 = 0.0;
        if (bounds.getMaxX() > this.contentWidth) {
            d2 = bounds.getMinX() - this.snappedLeftInset();
        }
        if (bounds.getMinX() < this.snappedLeftInset()) {
            d2 = bounds.getMaxX() - this.contentWidth - this.snappedLeftInset();
        }
        if (bounds.getMaxY() > this.snappedTopInset() + this.contentHeight) {
            d3 = bounds.getMinY() - this.snappedTopInset();
        }
        if (bounds.getMinY() < this.snappedTopInset()) {
            d3 = bounds.getMaxY() - this.contentHeight - this.snappedTopInset();
        }
        if (d2 != 0.0) {
            d = d2 * (this.hsb.getMax() - this.hsb.getMin()) / (this.nodeWidth - this.contentWidth);
            d += -1.0 * Math.signum(d) * this.hsb.getUnitIncrement() / 5.0;
            this.hsb.setValue(this.hsb.getValue() + d);
            ((ScrollPane)this.getSkinnable()).requestLayout();
        }
        if (d3 != 0.0) {
            d = d3 * (this.vsb.getMax() - this.vsb.getMin()) / (this.nodeHeight - this.contentHeight);
            d += -1.0 * Math.signum(d) * this.vsb.getUnitIncrement() / 5.0;
            this.vsb.setValue(this.vsb.getValue() + d);
            ((ScrollPane)this.getSkinnable()).requestLayout();
        }
    }

    private double computeHsbSizeHint(ScrollPane scrollPane) {
        return scrollPane.getHbarPolicy() == ScrollPane.ScrollBarPolicy.ALWAYS || scrollPane.getHbarPolicy() == ScrollPane.ScrollBarPolicy.AS_NEEDED && (scrollPane.getPrefViewportHeight() > 0.0 || scrollPane.getMinViewportHeight() > 0.0) ? this.hsb.prefHeight(-1.0) : 0.0;
    }

    private double computeVsbSizeHint(ScrollPane scrollPane) {
        return scrollPane.getVbarPolicy() == ScrollPane.ScrollBarPolicy.ALWAYS || scrollPane.getVbarPolicy() == ScrollPane.ScrollBarPolicy.AS_NEEDED && (scrollPane.getPrefViewportWidth() > 0.0 || scrollPane.getMinViewportWidth() > 0.0) ? this.vsb.prefWidth(-1.0) : 0.0;
    }

    private void computeScrollNodeSize(double d, double d2) {
        if (this.scrollNode != null) {
            if (this.scrollNode.isResizable()) {
                ScrollPane scrollPane = (ScrollPane)this.getSkinnable();
                Orientation orientation = this.scrollNode.getContentBias();
                if (orientation == null) {
                    this.nodeWidth = this.snapSizeX(com.sun.javafx.scene.control.skin.Utils.boundedSize(scrollPane.isFitToWidth() ? d : this.scrollNode.prefWidth(-1.0), this.scrollNode.minWidth(-1.0), this.scrollNode.maxWidth(-1.0)));
                    this.nodeHeight = this.snapSizeY(com.sun.javafx.scene.control.skin.Utils.boundedSize(scrollPane.isFitToHeight() ? d2 : this.scrollNode.prefHeight(-1.0), this.scrollNode.minHeight(-1.0), this.scrollNode.maxHeight(-1.0)));
                } else if (orientation == Orientation.HORIZONTAL) {
                    this.nodeWidth = this.snapSizeX(com.sun.javafx.scene.control.skin.Utils.boundedSize(scrollPane.isFitToWidth() ? d : this.scrollNode.prefWidth(-1.0), this.scrollNode.minWidth(-1.0), this.scrollNode.maxWidth(-1.0)));
                    this.nodeHeight = this.snapSizeY(com.sun.javafx.scene.control.skin.Utils.boundedSize(scrollPane.isFitToHeight() ? d2 : this.scrollNode.prefHeight(this.nodeWidth), this.scrollNode.minHeight(this.nodeWidth), this.scrollNode.maxHeight(this.nodeWidth)));
                } else {
                    this.nodeHeight = this.snapSizeY(com.sun.javafx.scene.control.skin.Utils.boundedSize(scrollPane.isFitToHeight() ? d2 : this.scrollNode.prefHeight(-1.0), this.scrollNode.minHeight(-1.0), this.scrollNode.maxHeight(-1.0)));
                    this.nodeWidth = this.snapSizeX(com.sun.javafx.scene.control.skin.Utils.boundedSize(scrollPane.isFitToWidth() ? d : this.scrollNode.prefWidth(this.nodeHeight), this.scrollNode.minWidth(this.nodeHeight), this.scrollNode.maxWidth(this.nodeHeight)));
                }
            } else {
                this.nodeWidth = this.snapSizeX(this.scrollNode.getLayoutBounds().getWidth());
                this.nodeHeight = this.snapSizeY(this.scrollNode.getLayoutBounds().getHeight());
            }
            this.nodeSizeInvalid = false;
        }
    }

    private boolean isReverseNodeOrientation() {
        return this.scrollNode != null && ((ScrollPane)this.getSkinnable()).getEffectiveNodeOrientation() != this.scrollNode.getEffectiveNodeOrientation();
    }

    private boolean determineHorizontalSBVisible() {
        ScrollPane scrollPane = (ScrollPane)this.getSkinnable();
        if (Properties.IS_TOUCH_SUPPORTED) {
            return this.tempVisibility && this.nodeWidth > this.contentWidth;
        }
        ScrollPane.ScrollBarPolicy scrollBarPolicy = scrollPane.getHbarPolicy();
        return ScrollPane.ScrollBarPolicy.NEVER == scrollBarPolicy ? false : (ScrollPane.ScrollBarPolicy.ALWAYS == scrollBarPolicy ? true : (scrollPane.isFitToWidth() && this.scrollNode != null && this.scrollNode.isResizable() ? this.nodeWidth > this.contentWidth && this.scrollNode.minWidth(-1.0) > this.contentWidth : this.nodeWidth > this.contentWidth));
    }

    private boolean determineVerticalSBVisible() {
        ScrollPane scrollPane = (ScrollPane)this.getSkinnable();
        if (Properties.IS_TOUCH_SUPPORTED) {
            return this.tempVisibility && this.nodeHeight > this.contentHeight;
        }
        ScrollPane.ScrollBarPolicy scrollBarPolicy = scrollPane.getVbarPolicy();
        return ScrollPane.ScrollBarPolicy.NEVER == scrollBarPolicy ? false : (ScrollPane.ScrollBarPolicy.ALWAYS == scrollBarPolicy ? true : (scrollPane.isFitToHeight() && this.scrollNode != null && this.scrollNode.isResizable() ? this.nodeHeight > this.contentHeight && this.scrollNode.minHeight(-1.0) > this.contentHeight : this.nodeHeight > this.contentHeight));
    }

    private void computeScrollBarSize() {
        this.vsbWidth = this.snapSizeX(this.vsb.prefWidth(-1.0));
        if (this.vsbWidth == 0.0) {
            this.vsbWidth = Properties.IS_TOUCH_SUPPORTED ? 8.0 : 12.0;
        }
        this.hsbHeight = this.snapSizeY(this.hsb.prefHeight(-1.0));
        if (this.hsbHeight == 0.0) {
            this.hsbHeight = Properties.IS_TOUCH_SUPPORTED ? 8.0 : 12.0;
        }
    }

    private void updateHorizontalSB() {
        double d = this.nodeWidth * (this.hsb.getMax() - this.hsb.getMin());
        if (d > 0.0) {
            this.hsb.setVisibleAmount(this.contentWidth / d);
            this.hsb.setBlockIncrement(0.9 * this.hsb.getVisibleAmount());
            this.hsb.setUnitIncrement(0.1 * this.hsb.getVisibleAmount());
        } else {
            this.hsb.setVisibleAmount(0.0);
            this.hsb.setBlockIncrement(0.0);
            this.hsb.setUnitIncrement(0.0);
        }
        if (this.hsb.isVisible()) {
            this.updatePosX();
        } else if (this.nodeWidth > this.contentWidth) {
            this.updatePosX();
        } else {
            this.viewContent.setLayoutX(0.0);
        }
    }

    private void updateVerticalSB() {
        double d = this.nodeHeight * (this.vsb.getMax() - this.vsb.getMin());
        if (d > 0.0) {
            this.vsb.setVisibleAmount(this.contentHeight / d);
            this.vsb.setBlockIncrement(0.9 * this.vsb.getVisibleAmount());
            this.vsb.setUnitIncrement(0.1 * this.vsb.getVisibleAmount());
        } else {
            this.vsb.setVisibleAmount(0.0);
            this.vsb.setBlockIncrement(0.0);
            this.vsb.setUnitIncrement(0.0);
        }
        if (this.vsb.isVisible()) {
            this.updatePosY();
        } else if (this.nodeHeight > this.contentHeight) {
            this.updatePosY();
        } else {
            this.viewContent.setLayoutY(0.0);
        }
    }

    private double updatePosX() {
        ScrollPane scrollPane = (ScrollPane)this.getSkinnable();
        double d = this.isReverseNodeOrientation() ? this.hsb.getMax() - (this.posX - this.hsb.getMin()) : this.posX;
        double d2 = Math.min(-d / (this.hsb.getMax() - this.hsb.getMin()) * (this.nodeWidth - this.contentWidth), 0.0);
        this.viewContent.setLayoutX(this.snapPositionX(d2));
        if (!scrollPane.hvalueProperty().isBound()) {
            scrollPane.setHvalue(Utils.clamp(scrollPane.getHmin(), this.posX, scrollPane.getHmax()));
        }
        return this.posX;
    }

    private double updatePosY() {
        ScrollPane scrollPane = (ScrollPane)this.getSkinnable();
        double d = Math.min(-this.posY / (this.vsb.getMax() - this.vsb.getMin()) * (this.nodeHeight - this.contentHeight), 0.0);
        this.viewContent.setLayoutY(this.snapPositionY(d));
        if (!scrollPane.vvalueProperty().isBound()) {
            scrollPane.setVvalue(Utils.clamp(scrollPane.getVmin(), this.posY, scrollPane.getVmax()));
        }
        return this.posY;
    }

    private void resetClip() {
        this.clipRect.setWidth(this.snapSizeX(this.contentWidth));
        this.clipRect.setHeight(this.snapSizeY(this.contentHeight));
    }

    private void startSBReleasedAnimation() {
        if (this.sbTouchTimeline == null) {
            this.sbTouchTimeline = new Timeline();
            this.sbTouchKF1 = new KeyFrame(Duration.millis(0.0), actionEvent -> {
                this.tempVisibility = true;
                if (this.touchDetected || this.mouseDown) {
                    this.sbTouchTimeline.playFromStart();
                }
            }, new KeyValue[0]);
            this.sbTouchKF2 = new KeyFrame(Duration.millis(1000.0), actionEvent -> {
                this.tempVisibility = false;
                ((ScrollPane)this.getSkinnable()).requestLayout();
            }, new KeyValue[0]);
            this.sbTouchTimeline.getKeyFrames().addAll((KeyFrame[])new KeyFrame[]{this.sbTouchKF1, this.sbTouchKF2});
        }
        this.sbTouchTimeline.playFromStart();
    }

    private void startContentsToViewport() {
        double d = this.posX;
        double d2 = this.posY;
        this.setContentPosX(this.posX);
        this.setContentPosY(this.posY);
        if (this.posY > ((ScrollPane)this.getSkinnable()).getVmax()) {
            d2 = ((ScrollPane)this.getSkinnable()).getVmax();
        } else if (this.posY < ((ScrollPane)this.getSkinnable()).getVmin()) {
            d2 = ((ScrollPane)this.getSkinnable()).getVmin();
        }
        if (this.posX > ((ScrollPane)this.getSkinnable()).getHmax()) {
            d = ((ScrollPane)this.getSkinnable()).getHmax();
        } else if (this.posX < ((ScrollPane)this.getSkinnable()).getHmin()) {
            d = ((ScrollPane)this.getSkinnable()).getHmin();
        }
        if (!Properties.IS_TOUCH_SUPPORTED) {
            this.startSBReleasedAnimation();
        }
        if (this.contentsToViewTimeline != null) {
            this.contentsToViewTimeline.stop();
        }
        this.contentsToViewTimeline = new Timeline();
        this.contentsToViewKF1 = new KeyFrame(Duration.millis(50.0), new KeyValue[0]);
        this.contentsToViewKF2 = new KeyFrame(Duration.millis(150.0), actionEvent -> ((ScrollPane)this.getSkinnable()).requestLayout(), new KeyValue(this.contentPosX, d), new KeyValue(this.contentPosY, d2));
        this.contentsToViewKF3 = new KeyFrame(Duration.millis(1500.0), new KeyValue[0]);
        this.contentsToViewTimeline.getKeyFrames().addAll((KeyFrame[])new KeyFrame[]{this.contentsToViewKF1, this.contentsToViewKF2, this.contentsToViewKF3});
        this.contentsToViewTimeline.playFromStart();
    }
}

