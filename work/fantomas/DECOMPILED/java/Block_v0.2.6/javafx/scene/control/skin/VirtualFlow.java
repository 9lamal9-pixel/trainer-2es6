/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.scene.ParentHelper;
import com.sun.javafx.scene.control.Logging;
import com.sun.javafx.scene.control.Properties;
import com.sun.javafx.scene.control.VirtualScrollBar;
import com.sun.javafx.scene.control.skin.Utils;
import com.sun.javafx.scene.traversal.Algorithm;
import com.sun.javafx.scene.traversal.Direction;
import com.sun.javafx.scene.traversal.ParentTraversalEngine;
import com.sun.javafx.scene.traversal.TraversalContext;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventDispatcher;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleRole;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Cell;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.ScrollBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import javafx.util.Duration;

public class VirtualFlow<T extends IndexedCell>
extends Region {
    private static final int MIN_SCROLLING_LINES_PER_PAGE = 8;
    private static final String NEW_CELL = "newcell";
    private static final double GOLDEN_RATIO_MULTIPLIER = 0.618033987;
    private boolean touchDetected = false;
    private boolean mouseDown = false;
    double lastWidth = -1.0;
    double lastHeight = -1.0;
    int lastCellCount = 0;
    boolean lastVertical;
    double lastPosition;
    double lastCellBreadth = -1.0;
    double lastCellLength = -1.0;
    final ArrayLinkedList<T> cells = new ArrayLinkedList();
    final ArrayLinkedList<T> pile = new ArrayLinkedList();
    T accumCell;
    Group accumCellParent;
    final Group sheet;
    final ObservableList<Node> sheetChildren;
    private VirtualScrollBar hbar = new VirtualScrollBar(this);
    private VirtualScrollBar vbar = new VirtualScrollBar(this);
    ClippedContainer clipView;
    StackPane corner;
    private double lastX;
    private double lastY;
    private boolean isPanning = false;
    private boolean fixedCellSizeEnabled = false;
    private boolean needsReconfigureCells = false;
    private boolean needsRecreateCells = false;
    private boolean needsRebuildCells = false;
    private boolean needsCellsLayout = false;
    private boolean sizeChanged = false;
    private final BitSet dirtyCells = new BitSet();
    Timeline sbTouchTimeline;
    KeyFrame sbTouchKF1;
    KeyFrame sbTouchKF2;
    private boolean needBreadthBar;
    private boolean needLengthBar;
    private boolean tempVisibility = false;
    private BooleanProperty vertical;
    private BooleanProperty pannable = new SimpleBooleanProperty(this, "pannable", true);
    private IntegerProperty cellCount = new SimpleIntegerProperty(this, "cellCount", 0){
        private int oldCount;
        {
            this.oldCount = 0;
        }

        @Override
        protected void invalidated() {
            Parent parent;
            int n = this.get();
            boolean bl = this.oldCount != n;
            this.oldCount = n;
            if (bl) {
                parent = VirtualFlow.this.isVertical() ? VirtualFlow.this.vbar : VirtualFlow.this.hbar;
                ((ScrollBar)parent).setMax(n);
            }
            if (bl) {
                VirtualFlow.this.layoutChildren();
                VirtualFlow.this.sheetChildren.clear();
                parent = VirtualFlow.this.getParent();
                if (parent != null) {
                    parent.requestLayout();
                }
            }
        }
    };
    private DoubleProperty position = new SimpleDoubleProperty(this, "position"){

        @Override
        public void setValue(Number number) {
            super.setValue(com.sun.javafx.util.Utils.clamp(0.0, this.get(), 1.0));
        }

        @Override
        protected void invalidated() {
            super.invalidated();
            VirtualFlow.this.requestLayout();
        }
    };
    private DoubleProperty fixedCellSize = new SimpleDoubleProperty(this, "fixedCellSize"){

        @Override
        protected void invalidated() {
            VirtualFlow.this.fixedCellSizeEnabled = this.get() > 0.0;
            VirtualFlow.this.needsCellsLayout = true;
            VirtualFlow.this.layoutChildren();
        }
    };
    private ObjectProperty<Callback<VirtualFlow<T>, T>> cellFactory;
    private double maxPrefBreadth;
    private double viewportBreadth;
    private double viewportLength;
    private final List<T> privateCells = new ArrayList<T>();

    public VirtualFlow() {
        this.getStyleClass().add("virtual-flow");
        this.setId("virtual-flow");
        this.sheet = new Group();
        this.sheet.getStyleClass().add("sheet");
        this.sheet.setAutoSizeChildren(false);
        this.sheetChildren = this.sheet.getChildren();
        this.clipView = new ClippedContainer(this);
        this.clipView.setNode(this.sheet);
        this.getChildren().add(this.clipView);
        this.accumCellParent = new Group();
        this.accumCellParent.setVisible(false);
        this.getChildren().add(this.accumCellParent);
        EventDispatcher eventDispatcher = (event, eventDispatchChain) -> event;
        EventDispatcher eventDispatcher2 = this.hbar.getEventDispatcher();
        this.hbar.setEventDispatcher((event, eventDispatchChain) -> {
            if (event.getEventType() == ScrollEvent.SCROLL && !((ScrollEvent)event).isDirect()) {
                eventDispatchChain = eventDispatchChain.prepend(eventDispatcher);
                eventDispatchChain = eventDispatchChain.prepend(eventDispatcher2);
                return eventDispatchChain.dispatchEvent(event);
            }
            return eventDispatcher2.dispatchEvent(event, eventDispatchChain);
        });
        EventDispatcher eventDispatcher3 = this.vbar.getEventDispatcher();
        this.vbar.setEventDispatcher((event, eventDispatchChain) -> {
            if (event.getEventType() == ScrollEvent.SCROLL && !((ScrollEvent)event).isDirect()) {
                eventDispatchChain = eventDispatchChain.prepend(eventDispatcher);
                eventDispatchChain = eventDispatchChain.prepend(eventDispatcher3);
                return eventDispatchChain.dispatchEvent(event);
            }
            return eventDispatcher3.dispatchEvent(event, eventDispatchChain);
        });
        this.setOnScroll((EventHandler<? super ScrollEvent>)new EventHandler<ScrollEvent>(){

            @Override
            public void handle(ScrollEvent scrollEvent) {
                VirtualScrollBar virtualScrollBar;
                double d;
                if (Properties.IS_TOUCH_SUPPORTED && !VirtualFlow.this.touchDetected && !VirtualFlow.this.mouseDown) {
                    VirtualFlow.this.startSBReleasedAnimation();
                }
                double d2 = 0.0;
                if (VirtualFlow.this.isVertical()) {
                    switch (scrollEvent.getTextDeltaYUnits()) {
                        case PAGES: {
                            d2 = scrollEvent.getTextDeltaY() * VirtualFlow.this.lastHeight;
                            break;
                        }
                        case LINES: {
                            if (VirtualFlow.this.fixedCellSizeEnabled) {
                                d = VirtualFlow.this.getFixedCellSize();
                            } else {
                                IndexedCell indexedCell = (IndexedCell)VirtualFlow.this.cells.getLast();
                                d = (VirtualFlow.this.getCellPosition(indexedCell) + VirtualFlow.this.getCellLength(indexedCell) - VirtualFlow.this.getCellPosition((IndexedCell)VirtualFlow.this.cells.getFirst())) / (double)VirtualFlow.this.cells.size();
                            }
                            if (VirtualFlow.this.lastHeight / d < 8.0) {
                                d = VirtualFlow.this.lastHeight / 8.0;
                            }
                            d2 = scrollEvent.getTextDeltaY() * d;
                            break;
                        }
                        case NONE: {
                            d2 = scrollEvent.getDeltaY();
                        }
                    }
                } else {
                    switch (scrollEvent.getTextDeltaXUnits()) {
                        case CHARACTERS: 
                        case NONE: {
                            d = scrollEvent.getDeltaX();
                            double d3 = scrollEvent.getDeltaY();
                            double d4 = d2 = Math.abs(d) > Math.abs(d3) ? d : d3;
                        }
                    }
                }
                if (d2 != 0.0 && (d = VirtualFlow.this.scrollPixels(-d2)) != 0.0) {
                    scrollEvent.consume();
                }
                VirtualScrollBar virtualScrollBar2 = virtualScrollBar = VirtualFlow.this.isVertical() ? VirtualFlow.this.hbar : VirtualFlow.this.vbar;
                if (VirtualFlow.this.needBreadthBar) {
                    double d5;
                    double d6 = d5 = VirtualFlow.this.isVertical() ? scrollEvent.getDeltaX() : scrollEvent.getDeltaY();
                    if (d5 != 0.0) {
                        double d7 = virtualScrollBar.getValue() - d5;
                        if (d7 < virtualScrollBar.getMin()) {
                            virtualScrollBar.setValue(virtualScrollBar.getMin());
                        } else if (d7 > virtualScrollBar.getMax()) {
                            virtualScrollBar.setValue(virtualScrollBar.getMax());
                        } else {
                            virtualScrollBar.setValue(d7);
                        }
                        scrollEvent.consume();
                    }
                }
            }
        });
        this.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>(){

            @Override
            public void handle(MouseEvent mouseEvent) {
                VirtualFlow.this.mouseDown = true;
                if (Properties.IS_TOUCH_SUPPORTED) {
                    VirtualFlow.this.scrollBarOn();
                }
                if (VirtualFlow.this.isFocusTraversable()) {
                    boolean bl = true;
                    Node node = VirtualFlow.this.getScene().getFocusOwner();
                    if (node != null) {
                        for (Parent parent = node.getParent(); parent != null; parent = parent.getParent()) {
                            if (!parent.equals(VirtualFlow.this)) continue;
                            bl = false;
                            break;
                        }
                    }
                    if (bl) {
                        VirtualFlow.this.requestFocus();
                    }
                }
                VirtualFlow.this.lastX = mouseEvent.getX();
                VirtualFlow.this.lastY = mouseEvent.getY();
                VirtualFlow.this.isPanning = !VirtualFlow.this.vbar.getBoundsInParent().contains(mouseEvent.getX(), mouseEvent.getY()) && !VirtualFlow.this.hbar.getBoundsInParent().contains(mouseEvent.getX(), mouseEvent.getY());
            }
        });
        this.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseEvent -> {
            this.mouseDown = false;
            if (Properties.IS_TOUCH_SUPPORTED) {
                this.startSBReleasedAnimation();
            }
        });
        this.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseEvent -> {
            VirtualScrollBar virtualScrollBar;
            if (Properties.IS_TOUCH_SUPPORTED) {
                this.scrollBarOn();
            }
            if (!this.isPanning || !this.isPannable()) {
                return;
            }
            double d = this.lastX - mouseEvent.getX();
            double d2 = this.lastY - mouseEvent.getY();
            double d3 = this.isVertical() ? d2 : d;
            double d4 = this.scrollPixels(d3);
            if (d4 != 0.0) {
                if (this.isVertical()) {
                    this.lastY = mouseEvent.getY();
                } else {
                    this.lastX = mouseEvent.getX();
                }
            }
            double d5 = this.isVertical() ? d : d2;
            VirtualScrollBar virtualScrollBar2 = virtualScrollBar = this.isVertical() ? this.hbar : this.vbar;
            if (virtualScrollBar.isVisible()) {
                double d6 = virtualScrollBar.getValue() + d5;
                if (d6 < virtualScrollBar.getMin()) {
                    virtualScrollBar.setValue(virtualScrollBar.getMin());
                } else if (d6 > virtualScrollBar.getMax()) {
                    virtualScrollBar.setValue(virtualScrollBar.getMax());
                } else {
                    virtualScrollBar.setValue(d6);
                    if (this.isVertical()) {
                        this.lastX = mouseEvent.getX();
                    } else {
                        this.lastY = mouseEvent.getY();
                    }
                }
            }
        });
        this.vbar.setOrientation(Orientation.VERTICAL);
        this.vbar.addEventHandler(MouseEvent.ANY, mouseEvent -> mouseEvent.consume());
        this.getChildren().add(this.vbar);
        this.hbar.setOrientation(Orientation.HORIZONTAL);
        this.hbar.addEventHandler(MouseEvent.ANY, mouseEvent -> mouseEvent.consume());
        this.getChildren().add(this.hbar);
        this.corner = new StackPane();
        this.corner.getStyleClass().setAll((String[])new String[]{"corner"});
        this.getChildren().add(this.corner);
        InvalidationListener invalidationListener = observable -> this.updateHbar();
        this.verticalProperty().addListener(invalidationListener);
        this.hbar.valueProperty().addListener(invalidationListener);
        this.hbar.visibleProperty().addListener(invalidationListener);
        ChangeListener<Number> changeListener = (observableValue, number, number2) -> this.clipView.setClipY(this.isVertical() ? 0.0 : this.vbar.getValue());
        this.vbar.valueProperty().addListener(changeListener);
        super.heightProperty().addListener((observableValue, number, number2) -> {
            if (number.doubleValue() == 0.0 && number2.doubleValue() > 0.0) {
                this.recreateCells();
            }
        });
        this.setOnTouchPressed(touchEvent -> {
            this.touchDetected = true;
            this.scrollBarOn();
        });
        this.setOnTouchReleased(touchEvent -> {
            this.touchDetected = false;
            this.startSBReleasedAnimation();
        });
        ParentHelper.setTraversalEngine(this, new ParentTraversalEngine(this, new Algorithm(){

            Node selectNextAfterIndex(int n, TraversalContext traversalContext) {
                Object t;
                while ((t = VirtualFlow.this.getVisibleCell(++n)) != null) {
                    if (((Node)t).isFocusTraversable()) {
                        return t;
                    }
                    Node node = traversalContext.selectFirstInParent((Parent)t);
                    if (node == null) continue;
                    return node;
                }
                return null;
            }

            Node selectPreviousBeforeIndex(int n, TraversalContext traversalContext) {
                Object t;
                while ((t = VirtualFlow.this.getVisibleCell(--n)) != null) {
                    Node node = traversalContext.selectLastInParent((Parent)t);
                    if (node != null) {
                        return node;
                    }
                    if (!((Node)t).isFocusTraversable()) continue;
                    return t;
                }
                return null;
            }

            @Override
            public Node select(Node node, Direction direction, TraversalContext traversalContext) {
                IndexedCell indexedCell;
                if (VirtualFlow.this.cells.isEmpty()) {
                    return null;
                }
                if (VirtualFlow.this.cells.contains(node)) {
                    indexedCell = (IndexedCell)node;
                } else {
                    indexedCell = this.findOwnerCell(node);
                    Node node2 = traversalContext.selectInSubtree(indexedCell, node, direction);
                    if (node2 != null) {
                        return node2;
                    }
                    if (direction == Direction.NEXT) {
                        direction = Direction.NEXT_IN_LINE;
                    }
                }
                int n = indexedCell.getIndex();
                switch (direction) {
                    case PREVIOUS: {
                        return this.selectPreviousBeforeIndex(n, traversalContext);
                    }
                    case NEXT: {
                        Node node3 = traversalContext.selectFirstInParent(indexedCell);
                        if (node3 != null) {
                            return node3;
                        }
                    }
                    case NEXT_IN_LINE: {
                        return this.selectNextAfterIndex(n, traversalContext);
                    }
                }
                return null;
            }

            private T findOwnerCell(Node node) {
                Parent parent = node.getParent();
                while (!VirtualFlow.this.cells.contains(parent)) {
                    parent = parent.getParent();
                }
                return (IndexedCell)parent;
            }

            @Override
            public Node selectFirst(TraversalContext traversalContext) {
                IndexedCell indexedCell = (IndexedCell)VirtualFlow.this.cells.getFirst();
                if (indexedCell == null) {
                    return null;
                }
                if (indexedCell.isFocusTraversable()) {
                    return indexedCell;
                }
                Node node = traversalContext.selectFirstInParent(indexedCell);
                if (node != null) {
                    return node;
                }
                return this.selectNextAfterIndex(indexedCell.getIndex(), traversalContext);
            }

            @Override
            public Node selectLast(TraversalContext traversalContext) {
                IndexedCell indexedCell = (IndexedCell)VirtualFlow.this.cells.getLast();
                if (indexedCell == null) {
                    return null;
                }
                Node node = traversalContext.selectLastInParent(indexedCell);
                if (node != null) {
                    return node;
                }
                if (indexedCell.isFocusTraversable()) {
                    return indexedCell;
                }
                return this.selectPreviousBeforeIndex(indexedCell.getIndex(), traversalContext);
            }
        }));
    }

    public final void setVertical(boolean bl) {
        this.verticalProperty().set(bl);
    }

    public final boolean isVertical() {
        return this.vertical == null ? true : this.vertical.get();
    }

    public final BooleanProperty verticalProperty() {
        if (this.vertical == null) {
            this.vertical = new BooleanPropertyBase(true){

                @Override
                protected void invalidated() {
                    VirtualFlow.this.pile.clear();
                    VirtualFlow.this.sheetChildren.clear();
                    VirtualFlow.this.cells.clear();
                    VirtualFlow.this.lastHeight = -1.0;
                    VirtualFlow.this.lastWidth = -1.0;
                    VirtualFlow.this.setMaxPrefBreadth(-1.0);
                    VirtualFlow.this.setViewportBreadth(0.0);
                    VirtualFlow.this.setViewportLength(0.0);
                    VirtualFlow.this.lastPosition = 0.0;
                    VirtualFlow.this.hbar.setValue(0.0);
                    VirtualFlow.this.vbar.setValue(0.0);
                    VirtualFlow.this.setPosition(0.0);
                    VirtualFlow.this.setNeedsLayout(true);
                    VirtualFlow.this.requestLayout();
                }

                @Override
                public Object getBean() {
                    return VirtualFlow.this;
                }

                @Override
                public String getName() {
                    return "vertical";
                }
            };
        }
        return this.vertical;
    }

    public final boolean isPannable() {
        return this.pannable.get();
    }

    public final void setPannable(boolean bl) {
        this.pannable.set(bl);
    }

    public final BooleanProperty pannableProperty() {
        return this.pannable;
    }

    public final int getCellCount() {
        return this.cellCount.get();
    }

    public final void setCellCount(int n) {
        this.cellCount.set(n);
    }

    public final IntegerProperty cellCountProperty() {
        return this.cellCount;
    }

    public final double getPosition() {
        return this.position.get();
    }

    public final void setPosition(double d) {
        this.position.set(d);
    }

    public final DoubleProperty positionProperty() {
        return this.position;
    }

    public final void setFixedCellSize(double d) {
        this.fixedCellSize.set(d);
    }

    public final double getFixedCellSize() {
        return this.fixedCellSize.get();
    }

    public final DoubleProperty fixedCellSizeProperty() {
        return this.fixedCellSize;
    }

    public final void setCellFactory(Callback<VirtualFlow<T>, T> callback) {
        this.cellFactoryProperty().set(callback);
    }

    public final Callback<VirtualFlow<T>, T> getCellFactory() {
        return this.cellFactory == null ? null : (Callback)this.cellFactory.get();
    }

    public final ObjectProperty<Callback<VirtualFlow<T>, T>> cellFactoryProperty() {
        if (this.cellFactory == null) {
            this.cellFactory = new SimpleObjectProperty<Callback<VirtualFlow<T>, T>>(this, "cellFactory"){

                @Override
                protected void invalidated() {
                    if (this.get() != null) {
                        VirtualFlow.this.accumCell = null;
                        VirtualFlow.this.setNeedsLayout(true);
                        VirtualFlow.this.recreateCells();
                        if (VirtualFlow.this.getParent() != null) {
                            VirtualFlow.this.getParent().requestLayout();
                        }
                    }
                }
            };
        }
        return this.cellFactory;
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
    }

    @Override
    protected void layoutChildren() {
        double d;
        int n;
        boolean bl;
        double d2;
        boolean bl2;
        Object object;
        int n2;
        int n3;
        int n4;
        if (this.needsRecreateCells) {
            this.lastWidth = -1.0;
            this.lastHeight = -1.0;
            this.releaseCell(this.accumCell);
            this.sheet.getChildren().clear();
            n4 = this.cells.size();
            for (n3 = 0; n3 < n4; ++n3) {
                ((IndexedCell)this.cells.get(n3)).updateIndex(-1);
            }
            this.cells.clear();
            this.pile.clear();
            this.releaseAllPrivateCells();
        } else if (this.needsRebuildCells) {
            this.lastWidth = -1.0;
            this.lastHeight = -1.0;
            this.releaseCell(this.accumCell);
            n4 = this.cells.size();
            for (n3 = 0; n3 < n4; ++n3) {
                ((IndexedCell)this.cells.get(n3)).updateIndex(-1);
            }
            this.addAllToPile();
            this.releaseAllPrivateCells();
        } else if (this.needsReconfigureCells) {
            this.setMaxPrefBreadth(-1.0);
            this.lastWidth = -1.0;
            this.lastHeight = -1.0;
        }
        if (!this.dirtyCells.isEmpty()) {
            n4 = this.cells.size();
            while ((n3 = this.dirtyCells.nextSetBit(0)) != -1 && n3 < n4) {
                IndexedCell indexedCell = (IndexedCell)this.cells.get(n3);
                if (indexedCell != null) {
                    indexedCell.requestLayout();
                }
                this.dirtyCells.clear(n3);
            }
            this.setMaxPrefBreadth(-1.0);
            this.lastWidth = -1.0;
            this.lastHeight = -1.0;
        }
        n3 = this.sizeChanged;
        n4 = this.needsRebuildCells || this.needsRecreateCells || this.sizeChanged ? 1 : 0;
        this.needsRecreateCells = false;
        this.needsReconfigureCells = false;
        this.needsRebuildCells = false;
        this.sizeChanged = false;
        if (this.needsCellsLayout) {
            int n5 = this.cells.size();
            for (int i = 0; i < n5; ++i) {
                Cell cell = (Cell)this.cells.get(i);
                if (cell == null) continue;
                cell.requestLayout();
            }
            this.needsCellsLayout = false;
            return;
        }
        double d3 = this.getWidth();
        double d4 = this.getHeight();
        boolean bl3 = this.isVertical();
        double d5 = this.getPosition();
        if (d3 <= 0.0 || d4 <= 0.0) {
            this.addAllToPile();
            this.lastWidth = d3;
            this.lastHeight = d4;
            this.hbar.setVisible(false);
            this.vbar.setVisible(false);
            this.corner.setVisible(false);
            return;
        }
        boolean bl4 = false;
        boolean bl5 = false;
        if (Properties.IS_TOUCH_SUPPORTED && (this.tempVisibility && (!this.hbar.isVisible() || !this.vbar.isVisible()) || !this.tempVisibility && (this.hbar.isVisible() || this.vbar.isVisible()))) {
            bl5 = true;
        }
        if (!bl4) {
            for (n2 = 0; n2 < this.cells.size() && !(bl4 = ((Parent)(object = (Cell)this.cells.get(n2))).isNeedsLayout()); ++n2) {
            }
        }
        n2 = this.getCellCount();
        object = this.getFirstVisibleCell();
        if (!bl4 && !bl5) {
            bl2 = false;
            if (object != null) {
                double d6 = this.getCellBreadth((Cell)object);
                d2 = this.getCellLength(object);
                bl2 = d6 != this.lastCellBreadth || d2 != this.lastCellLength;
                this.lastCellBreadth = d6;
                this.lastCellLength = d2;
            }
            if (d3 == this.lastWidth && d4 == this.lastHeight && n2 == this.lastCellCount && bl3 == this.lastVertical && d5 == this.lastPosition && !bl2) {
                return;
            }
        }
        bl2 = false;
        boolean bl6 = bl = bl4 || bl3 != this.lastVertical || this.cells.isEmpty() || this.getMaxPrefBreadth() == -1.0 || d5 != this.lastPosition || n2 != this.lastCellCount || n3 != 0 || bl3 && d4 < this.lastHeight || !bl3 && d3 < this.lastWidth;
        if (!bl) {
            double d7 = this.getMaxPrefBreadth();
            boolean bl7 = false;
            for (n = 0; n < this.cells.size(); ++n) {
                d = this.getCellBreadth((Cell)this.cells.get(n));
                if (d7 == d) {
                    bl7 = true;
                    continue;
                }
                if (!(d > d7)) continue;
                bl = true;
                break;
            }
            if (!bl7) {
                bl = true;
            }
        }
        if (!bl && (bl3 && d4 > this.lastHeight || !bl3 && d3 > this.lastWidth)) {
            bl2 = true;
        }
        this.initViewport();
        int n6 = this.computeCurrentIndex();
        if (this.lastCellCount != n2) {
            if (d5 != 0.0 && d5 != 1.0) {
                if (n6 >= n2) {
                    this.setPosition(1.0);
                } else if (object != null) {
                    d2 = this.getCellPosition(object);
                    n = this.getCellIndex(object);
                    this.adjustPositionToIndex(n);
                    d = -this.computeOffsetForCell(n);
                    this.adjustByPixelAmount(d - d2);
                }
            }
            n6 = this.computeCurrentIndex();
        }
        if (bl) {
            this.setMaxPrefBreadth(-1.0);
            this.addAllToPile();
            d2 = -this.computeViewportOffset(this.getPosition());
            this.addLeadingCells(n6, d2);
            this.addTrailingCells(true);
        } else if (bl2) {
            this.addTrailingCells(true);
        }
        this.computeBarVisiblity();
        n4 = n4 != 0 || bl ? 1 : 0;
        this.updateScrollBarsAndCells(n4 != 0);
        this.lastWidth = this.getWidth();
        this.lastHeight = this.getHeight();
        this.lastCellCount = this.getCellCount();
        this.lastVertical = this.isVertical();
        this.lastPosition = this.getPosition();
        this.cleanPile();
    }

    @Override
    protected void setWidth(double d) {
        if (d != this.lastWidth) {
            super.setWidth(d);
            this.sizeChanged = true;
            this.setNeedsLayout(true);
            this.requestLayout();
        }
    }

    @Override
    protected void setHeight(double d) {
        if (d != this.lastHeight) {
            super.setHeight(d);
            this.sizeChanged = true;
            this.setNeedsLayout(true);
            this.requestLayout();
        }
    }

    protected T getAvailableCell(int n) {
        Node node = null;
        int n2 = this.pile.size();
        for (int i = 0; i < n2; ++i) {
            IndexedCell indexedCell = (IndexedCell)this.pile.get(i);
            assert (indexedCell != null);
            if (this.getCellIndex(indexedCell) != n) continue;
            node = indexedCell;
            this.pile.remove(i);
            break;
        }
        if (node == null && !this.pile.isEmpty()) {
            node = (IndexedCell)this.pile.removeLast();
        }
        if (node == null) {
            node = (IndexedCell)this.getCellFactory().call(this);
            node.getProperties().put(NEW_CELL, null);
        }
        if (node.getParent() == null) {
            this.sheetChildren.add(node);
        }
        return (T)node;
    }

    protected void addAllToPile() {
        int n = this.cells.size();
        for (int i = 0; i < n; ++i) {
            this.addToPile((IndexedCell)this.cells.removeFirst());
        }
    }

    public T getVisibleCell(int n) {
        IndexedCell indexedCell;
        if (this.cells.isEmpty()) {
            return null;
        }
        IndexedCell indexedCell2 = (IndexedCell)this.cells.getLast();
        int n2 = this.getCellIndex(indexedCell2);
        if (n == n2) {
            return (T)indexedCell2;
        }
        IndexedCell indexedCell3 = (IndexedCell)this.cells.getFirst();
        int n3 = this.getCellIndex(indexedCell3);
        if (n == n3) {
            return (T)indexedCell3;
        }
        if (n > n3 && n < n2 && this.getCellIndex(indexedCell = (IndexedCell)this.cells.get(n - n3)) == n) {
            return (T)indexedCell;
        }
        return null;
    }

    public T getLastVisibleCell() {
        if (this.cells.isEmpty() || this.getViewportLength() <= 0.0) {
            return null;
        }
        for (int i = this.cells.size() - 1; i >= 0; --i) {
            IndexedCell indexedCell = (IndexedCell)this.cells.get(i);
            if (indexedCell.isEmpty()) continue;
            return (T)indexedCell;
        }
        return null;
    }

    public T getFirstVisibleCell() {
        if (this.cells.isEmpty() || this.getViewportLength() <= 0.0) {
            return null;
        }
        IndexedCell indexedCell = (IndexedCell)this.cells.getFirst();
        return (T)(indexedCell.isEmpty() ? null : indexedCell);
    }

    public void scrollToTop(T t) {
        if (t != null) {
            this.scrollPixels(this.getCellPosition(t));
        }
    }

    public void scrollToBottom(T t) {
        if (t != null) {
            this.scrollPixels(this.getCellPosition(t) + this.getCellLength(t) - this.getViewportLength());
        }
    }

    public void scrollTo(T t) {
        if (t != null) {
            double d = this.getCellPosition(t);
            double d2 = this.getCellLength(t);
            double d3 = d + d2;
            double d4 = this.getViewportLength();
            if (d < 0.0) {
                this.scrollPixels(d);
            } else if (d3 > d4) {
                this.scrollPixels(d3 - d4);
            }
        }
    }

    public void scrollTo(int n) {
        T t = this.getVisibleCell(n);
        if (t != null) {
            this.scrollTo(t);
        } else {
            this.adjustPositionToIndex(n);
            this.addAllToPile();
            this.requestLayout();
        }
    }

    public void scrollToTop(int n) {
        boolean bl = false;
        if (n >= this.getCellCount() - 1) {
            this.setPosition(1.0);
            bl = true;
        } else if (n < 0) {
            this.setPosition(0.0);
            bl = true;
        }
        if (!bl) {
            this.adjustPositionToIndex(n);
            double d = -this.computeOffsetForCell(n);
            this.adjustByPixelAmount(d);
        }
        this.requestLayout();
    }

    public double scrollPixels(double d) {
        if (d == 0.0) {
            return 0.0;
        }
        boolean bl = this.isVertical();
        if (bl && (!this.tempVisibility ? !this.vbar.isVisible() : !this.needLengthBar) || !bl && (this.tempVisibility ? !this.needLengthBar : !this.hbar.isVisible())) {
            return 0.0;
        }
        double d2 = this.getPosition();
        if (d2 == 0.0 && d < 0.0) {
            return 0.0;
        }
        if (d2 == 1.0 && d > 0.0) {
            return 0.0;
        }
        this.adjustByPixelAmount(d);
        if (d2 == this.getPosition()) {
            return 0.0;
        }
        if (this.cells.size() > 0) {
            double d3;
            int n;
            for (int i = 0; i < this.cells.size(); ++i) {
                IndexedCell indexedCell = (IndexedCell)this.cells.get(i);
                assert (indexedCell != null);
                this.positionCell(indexedCell, this.getCellPosition(indexedCell) - d);
            }
            IndexedCell indexedCell = (IndexedCell)this.cells.getFirst();
            double d4 = indexedCell == null ? 0.0 : this.getCellPosition(indexedCell);
            for (n = 0; n < this.cells.size(); ++n) {
                IndexedCell indexedCell2 = (IndexedCell)this.cells.get(n);
                assert (indexedCell2 != null);
                double d5 = this.getCellPosition(indexedCell2);
                if (Math.abs(d5 - d4) > 0.001) {
                    this.positionCell(indexedCell2, d4);
                }
                d4 += this.getCellLength(indexedCell2);
            }
            this.cull();
            indexedCell = (IndexedCell)this.cells.getFirst();
            if (indexedCell != null) {
                n = this.getCellIndex(indexedCell);
                d3 = this.getCellLength(n - 1);
                this.addLeadingCells(n - 1, this.getCellPosition(indexedCell) - d3);
            } else {
                n = this.computeCurrentIndex();
                d3 = -this.computeViewportOffset(this.getPosition());
                this.addLeadingCells(n, d3);
            }
            if (!this.addTrailingCells(false)) {
                double d6;
                T t = this.getLastVisibleCell();
                d3 = this.getCellLength(t);
                double d7 = this.getCellPosition(t) + d3;
                if (d7 < (d6 = this.getViewportLength())) {
                    int n2;
                    double d8 = d6 - d7;
                    for (n2 = 0; n2 < this.cells.size(); ++n2) {
                        IndexedCell indexedCell3 = (IndexedCell)this.cells.get(n2);
                        this.positionCell(indexedCell3, this.getCellPosition(indexedCell3) + d8);
                    }
                    this.setPosition(1.0);
                    indexedCell = (IndexedCell)this.cells.getFirst();
                    n2 = this.getCellIndex(indexedCell);
                    double d9 = this.getCellLength(n2 - 1);
                    this.addLeadingCells(n2 - 1, this.getCellPosition(indexedCell) - d9);
                }
            }
        }
        this.cull();
        this.updateScrollBarsAndCells(false);
        this.lastPosition = this.getPosition();
        return d;
    }

    @Override
    protected double computePrefWidth(double d) {
        double d2 = this.isVertical() ? this.getPrefBreadth(d) : this.getPrefLength();
        return d2 + this.vbar.prefWidth(-1.0);
    }

    @Override
    protected double computePrefHeight(double d) {
        double d2 = this.isVertical() ? this.getPrefLength() : this.getPrefBreadth(d);
        return d2 + this.hbar.prefHeight(-1.0);
    }

    public T getCell(int n) {
        Callback<VirtualFlow<T>, T> callback;
        T t;
        if (!this.cells.isEmpty() && (t = this.getVisibleCell(n)) != null) {
            return t;
        }
        for (int i = 0; i < this.pile.size(); ++i) {
            IndexedCell indexedCell = (IndexedCell)this.pile.get(i);
            if (this.getCellIndex(indexedCell) != n) continue;
            return (T)indexedCell;
        }
        if (this.pile.size() > 0) {
            return (T)((IndexedCell)this.pile.get(0));
        }
        if (this.accumCell == null && (callback = this.getCellFactory()) != null) {
            this.accumCell = (IndexedCell)callback.call(this);
            ((Node)this.accumCell).getProperties().put(NEW_CELL, null);
            this.accumCellParent.getChildren().setAll((Node[])new Node[]{this.accumCell});
            ((Node)this.accumCell).setAccessibleRole(AccessibleRole.NODE);
            ((Parent)this.accumCell).getChildrenUnmodifiable().addListener(observable -> {
                for (Node node : ((Parent)this.accumCell).getChildrenUnmodifiable()) {
                    node.setAccessibleRole(AccessibleRole.NODE);
                }
            });
        }
        this.setCellIndex(this.accumCell, n);
        this.resizeCellSize(this.accumCell);
        return this.accumCell;
    }

    protected void setCellIndex(T t, int n) {
        assert (t != null);
        ((IndexedCell)t).updateIndex(n);
        if (((Parent)t).isNeedsLayout() && ((Node)t).getScene() != null || ((Node)t).getProperties().containsKey(NEW_CELL)) {
            ((Node)t).applyCss();
            ((Node)t).getProperties().remove(NEW_CELL);
        }
    }

    protected int getCellIndex(T t) {
        return ((IndexedCell)t).getIndex();
    }

    final VirtualScrollBar getHbar() {
        return this.hbar;
    }

    final VirtualScrollBar getVbar() {
        return this.vbar;
    }

    private final void setMaxPrefBreadth(double d) {
        this.maxPrefBreadth = d;
    }

    final double getMaxPrefBreadth() {
        return this.maxPrefBreadth;
    }

    private final void setViewportBreadth(double d) {
        this.viewportBreadth = d;
    }

    private final double getViewportBreadth() {
        return this.viewportBreadth;
    }

    void setViewportLength(double d) {
        this.viewportLength = d;
    }

    double getViewportLength() {
        return this.viewportLength;
    }

    double getCellLength(int n) {
        if (this.fixedCellSizeEnabled) {
            return this.getFixedCellSize();
        }
        T t = this.getCell(n);
        double d = this.getCellLength(t);
        this.releaseCell(t);
        return d;
    }

    double getCellBreadth(int n) {
        T t = this.getCell(n);
        double d = this.getCellBreadth((Cell)t);
        this.releaseCell(t);
        return d;
    }

    double getCellLength(T t) {
        if (t == null) {
            return 0.0;
        }
        if (this.fixedCellSizeEnabled) {
            return this.getFixedCellSize();
        }
        return this.isVertical() ? ((Node)t).getLayoutBounds().getHeight() : ((Node)t).getLayoutBounds().getWidth();
    }

    double getCellBreadth(Cell cell) {
        return this.isVertical() ? cell.prefWidth(-1.0) : cell.prefHeight(-1.0);
    }

    double getCellPosition(T t) {
        if (t == null) {
            return 0.0;
        }
        return this.isVertical() ? ((Node)t).getLayoutY() : ((Node)t).getLayoutX();
    }

    private void positionCell(T t, double d) {
        if (this.isVertical()) {
            ((Node)t).setLayoutX(0.0);
            ((Node)t).setLayoutY(this.snapSizeY(d));
        } else {
            ((Node)t).setLayoutX(this.snapSizeX(d));
            ((Node)t).setLayoutY(0.0);
        }
    }

    private void resizeCellSize(T t) {
        if (t == null) {
            return;
        }
        if (this.isVertical()) {
            double d = Math.max(this.getMaxPrefBreadth(), this.getViewportBreadth());
            ((Region)t).resize(d, this.fixedCellSizeEnabled ? this.getFixedCellSize() : Utils.boundedSize(((Region)t).prefHeight(d), ((Region)t).minHeight(d), ((Region)t).maxHeight(d)));
        } else {
            double d = Math.max(this.getMaxPrefBreadth(), this.getViewportBreadth());
            ((Region)t).resize(this.fixedCellSizeEnabled ? this.getFixedCellSize() : Utils.boundedSize(((Region)t).prefWidth(d), ((Region)t).minWidth(d), ((Region)t).maxWidth(d)), d);
        }
    }

    private List<T> getCells() {
        return this.cells;
    }

    T getLastVisibleCellWithinViewPort() {
        if (this.cells.isEmpty() || this.getViewportLength() <= 0.0) {
            return null;
        }
        double d = this.getViewportLength();
        for (int i = this.cells.size() - 1; i >= 0; --i) {
            double d2;
            double d3;
            IndexedCell indexedCell = (IndexedCell)this.cells.get(i);
            if (indexedCell.isEmpty() || !((d3 = (d2 = this.getCellPosition(indexedCell)) + this.getCellLength(indexedCell)) <= d + 2.0)) continue;
            return (T)indexedCell;
        }
        return null;
    }

    T getFirstVisibleCellWithinViewPort() {
        if (this.cells.isEmpty() || this.getViewportLength() <= 0.0) {
            return null;
        }
        for (int i = 0; i < this.cells.size(); ++i) {
            double d;
            IndexedCell indexedCell = (IndexedCell)this.cells.get(i);
            if (indexedCell.isEmpty() || !((d = this.getCellPosition(indexedCell)) >= 0.0)) continue;
            return (T)indexedCell;
        }
        return null;
    }

    void addLeadingCells(int n, double d) {
        double d2 = d;
        int n2 = n;
        boolean bl = true;
        IndexedCell indexedCell = null;
        if (n2 == this.getCellCount() && d2 == this.getViewportLength()) {
            --n2;
            bl = false;
        }
        while (n2 >= 0 && (d2 > 0.0 || bl)) {
            indexedCell = (IndexedCell)this.getAvailableCell(n2);
            this.setCellIndex(indexedCell, n2);
            this.resizeCellSize(indexedCell);
            this.cells.addFirst(indexedCell);
            if (bl) {
                bl = false;
            } else {
                d2 -= this.getCellLength(indexedCell);
            }
            this.positionCell(indexedCell, d2);
            this.setMaxPrefBreadth(Math.max(this.getMaxPrefBreadth(), this.getCellBreadth(indexedCell)));
            indexedCell.setVisible(true);
            --n2;
        }
        if (this.cells.size() > 0) {
            indexedCell = (IndexedCell)this.cells.getFirst();
            int n3 = this.getCellIndex(indexedCell);
            double d3 = this.getCellPosition(indexedCell);
            if (n3 == 0 && d3 > 0.0) {
                this.setPosition(0.0);
                d2 = 0.0;
                for (int i = 0; i < this.cells.size(); ++i) {
                    indexedCell = (IndexedCell)this.cells.get(i);
                    this.positionCell(indexedCell, d2);
                    d2 += this.getCellLength(indexedCell);
                }
            }
        } else {
            this.vbar.setValue(0.0);
            this.hbar.setValue(0.0);
        }
    }

    boolean addTrailingCells(boolean bl) {
        Object object;
        int n;
        if (this.cells.isEmpty()) {
            return false;
        }
        IndexedCell indexedCell = (IndexedCell)this.cells.getLast();
        double d = this.getCellPosition(indexedCell) + this.getCellLength(indexedCell);
        int n2 = this.getCellIndex(indexedCell) + 1;
        boolean bl2 = n2 <= (n = this.getCellCount());
        double d2 = this.getViewportLength();
        if (d < 0.0 && !bl) {
            return false;
        }
        double d3 = d2;
        while (d < d2) {
            if (n2 >= n) {
                if (d < d2) {
                    bl2 = false;
                }
                if (!bl) {
                    return bl2;
                }
                if ((double)n2 > d3) {
                    object = Logging.getControlsLogger();
                    if (((PlatformLogger)object).isLoggable(PlatformLogger.Level.INFO)) {
                        ((PlatformLogger)object).info("index exceeds maxCellCount. Check size calculations for " + indexedCell.getClass());
                    }
                    return bl2;
                }
            }
            object = this.getAvailableCell(n2);
            this.setCellIndex(object, n2);
            this.resizeCellSize(object);
            this.cells.addLast(object);
            this.positionCell(object, d);
            this.setMaxPrefBreadth(Math.max(this.getMaxPrefBreadth(), this.getCellBreadth((Cell)object)));
            d += this.getCellLength(object);
            ((Node)object).setVisible(true);
            ++n2;
        }
        object = (IndexedCell)this.cells.getFirst();
        n2 = this.getCellIndex(object);
        T t = this.getLastVisibleCell();
        double d4 = this.getCellPosition(object);
        double d5 = this.getCellPosition(t) + this.getCellLength(t);
        if ((n2 != 0 || n2 == 0 && d4 < 0.0) && bl && t != null && this.getCellIndex(t) == n - 1 && d5 < d2) {
            double d6;
            double d7 = d2 - d5;
            for (double d8 = d5; d8 < d2 && n2 != 0 && -d4 < d7; d8 += d6) {
                T t2 = this.getAvailableCell(--n2);
                this.setCellIndex(t2, n2);
                this.resizeCellSize(t2);
                this.cells.addFirst(t2);
                d6 = this.getCellLength(t2);
                this.positionCell(t2, d4 -= d6);
                this.setMaxPrefBreadth(Math.max(this.getMaxPrefBreadth(), this.getCellBreadth((Cell)t2)));
                ((Node)t2).setVisible(true);
            }
            object = (IndexedCell)this.cells.getFirst();
            d4 = this.getCellPosition(object);
            double d9 = d2 - d5;
            if (this.getCellIndex(object) == 0 && d9 > -d4) {
                d9 = -d4;
            }
            for (int i = 0; i < this.cells.size(); ++i) {
                IndexedCell indexedCell2 = (IndexedCell)this.cells.get(i);
                this.positionCell(indexedCell2, this.getCellPosition(indexedCell2) + d9);
            }
            d4 = this.getCellPosition(object);
            if (this.getCellIndex(object) == 0 && d4 == 0.0) {
                this.setPosition(0.0);
            } else if (this.getPosition() != 1.0) {
                this.setPosition(1.0);
            }
        }
        return bl2;
    }

    void reconfigureCells() {
        this.needsReconfigureCells = true;
        this.requestLayout();
    }

    void recreateCells() {
        this.needsRecreateCells = true;
        this.requestLayout();
    }

    void rebuildCells() {
        this.needsRebuildCells = true;
        this.requestLayout();
    }

    void requestCellLayout() {
        this.needsCellsLayout = true;
        this.requestLayout();
    }

    void setCellDirty(int n) {
        this.dirtyCells.set(n);
        this.requestLayout();
    }

    private void startSBReleasedAnimation() {
        if (this.sbTouchTimeline == null) {
            this.sbTouchTimeline = new Timeline();
            this.sbTouchKF1 = new KeyFrame(Duration.millis(0.0), actionEvent -> {
                this.tempVisibility = true;
                this.requestLayout();
            }, new KeyValue[0]);
            this.sbTouchKF2 = new KeyFrame(Duration.millis(1000.0), actionEvent -> {
                if (!this.touchDetected && !this.mouseDown) {
                    this.tempVisibility = false;
                    this.requestLayout();
                }
            }, new KeyValue[0]);
            this.sbTouchTimeline.getKeyFrames().addAll((KeyFrame[])new KeyFrame[]{this.sbTouchKF1, this.sbTouchKF2});
        }
        this.sbTouchTimeline.playFromStart();
    }

    private void scrollBarOn() {
        this.tempVisibility = true;
        this.requestLayout();
    }

    void updateHbar() {
        if (!this.isVisible() || this.getScene() == null) {
            return;
        }
        if (this.isVertical()) {
            if (this.hbar.isVisible()) {
                this.clipView.setClipX(this.hbar.getValue());
            } else {
                this.clipView.setClipX(0.0);
                this.hbar.setValue(0.0);
            }
        }
    }

    private boolean computeBarVisiblity() {
        if (this.cells.isEmpty()) {
            this.needLengthBar = false;
            this.needBreadthBar = false;
            return true;
        }
        boolean bl = this.isVertical();
        boolean bl2 = false;
        VirtualScrollBar virtualScrollBar = bl ? this.hbar : this.vbar;
        VirtualScrollBar virtualScrollBar2 = bl ? this.vbar : this.hbar;
        double d = this.getViewportBreadth();
        int n = this.cells.size();
        int n2 = this.getCellCount();
        for (int i = 0; i < 2; ++i) {
            boolean bl3;
            boolean bl4;
            boolean bl5 = bl4 = this.getPosition() > 0.0 || n2 > n || n2 == n && this.getCellPosition((IndexedCell)this.cells.getLast()) + this.getCellLength((IndexedCell)this.cells.getLast()) > this.getViewportLength() || n2 == n - 1 && bl2 && this.needBreadthBar;
            if (bl4 ^ this.needLengthBar) {
                this.needLengthBar = bl4;
                bl2 = true;
            }
            boolean bl6 = bl3 = this.maxPrefBreadth > d;
            if (!(bl3 ^ this.needBreadthBar)) continue;
            this.needBreadthBar = bl3;
            bl2 = true;
        }
        if (!Properties.IS_TOUCH_SUPPORTED) {
            this.updateViewportDimensions();
            virtualScrollBar.setVisible(this.needBreadthBar);
            virtualScrollBar2.setVisible(this.needLengthBar);
        } else {
            virtualScrollBar.setVisible(this.needBreadthBar && this.tempVisibility);
            virtualScrollBar2.setVisible(this.needLengthBar && this.tempVisibility);
        }
        return bl2;
    }

    private void updateViewportDimensions() {
        boolean bl = this.isVertical();
        double d = bl ? this.snapSizeY(this.hbar.prefHeight(-1.0)) : this.snapSizeX(this.vbar.prefWidth(-1.0));
        double d2 = bl ? this.snapSizeX(this.vbar.prefWidth(-1.0)) : this.snapSizeY(this.hbar.prefHeight(-1.0));
        this.setViewportBreadth((bl ? this.getWidth() : this.getHeight()) - (this.needLengthBar ? d2 : 0.0));
        this.setViewportLength((bl ? this.getHeight() : this.getWidth()) - (this.needBreadthBar ? d : 0.0));
    }

    private void initViewport() {
        boolean bl = this.isVertical();
        this.updateViewportDimensions();
        VirtualScrollBar virtualScrollBar = bl ? this.hbar : this.vbar;
        VirtualScrollBar virtualScrollBar2 = bl ? this.vbar : this.hbar;
        virtualScrollBar.setVirtual(false);
        virtualScrollBar2.setVirtual(true);
    }

    private void updateScrollBarsAndCells(boolean bl) {
        double d;
        double d2;
        boolean bl2 = this.isVertical();
        VirtualScrollBar virtualScrollBar = bl2 ? this.hbar : this.vbar;
        VirtualScrollBar virtualScrollBar2 = bl2 ? this.vbar : this.hbar;
        this.fitCells();
        if (!this.cells.isEmpty()) {
            IndexedCell indexedCell;
            int n;
            d2 = -this.computeViewportOffset(this.getPosition());
            int n2 = this.computeCurrentIndex() - ((IndexedCell)this.cells.getFirst()).getIndex();
            int n3 = this.cells.size();
            d = d2;
            for (n = n2 - 1; n >= 0 && n < n3; --n) {
                indexedCell = (IndexedCell)this.cells.get(n);
                this.positionCell(indexedCell, d -= this.getCellLength(indexedCell));
            }
            d = d2;
            for (n = n2; n >= 0 && n < n3; ++n) {
                indexedCell = (IndexedCell)this.cells.get(n);
                this.positionCell(indexedCell, d);
                d += this.getCellLength(indexedCell);
            }
        }
        this.corner.setVisible(virtualScrollBar.isVisible() && virtualScrollBar2.isVisible());
        d2 = 0.0;
        double d3 = (bl2 ? this.getHeight() : this.getWidth()) - (virtualScrollBar.isVisible() ? virtualScrollBar.prefHeight(-1.0) : 0.0);
        d = this.getViewportBreadth();
        double d4 = this.getViewportLength();
        if (virtualScrollBar.isVisible()) {
            double d5;
            if (!Properties.IS_TOUCH_SUPPORTED) {
                if (bl2) {
                    this.hbar.resizeRelocate(0.0, d4, d, this.hbar.prefHeight(d));
                } else {
                    this.vbar.resizeRelocate(d4, 0.0, this.vbar.prefWidth(d), d);
                }
            } else if (bl2) {
                this.hbar.resizeRelocate(0.0, d4 - this.hbar.getHeight(), d, this.hbar.prefHeight(d));
            } else {
                this.vbar.resizeRelocate(d4 - this.vbar.getWidth(), 0.0, this.vbar.prefWidth(d), d);
            }
            if (this.getMaxPrefBreadth() != -1.0 && (d5 = Math.max(1.0, this.getMaxPrefBreadth() - d)) != virtualScrollBar.getMax()) {
                boolean bl3;
                virtualScrollBar.setMax(d5);
                double d6 = virtualScrollBar.getValue();
                boolean bl4 = bl3 = d6 != 0.0 && d5 == d6;
                if (bl3 || d6 > d5) {
                    virtualScrollBar.setValue(d5);
                }
                virtualScrollBar.setVisibleAmount(d / this.getMaxPrefBreadth() * d5);
            }
        }
        if (bl && (virtualScrollBar2.isVisible() || Properties.IS_TOUCH_SUPPORTED)) {
            int n = this.getCellCount();
            int n4 = 0;
            int n5 = this.cells.size();
            for (int i = 0; i < n5; ++i) {
                IndexedCell indexedCell = (IndexedCell)this.cells.get(i);
                if (indexedCell == null || indexedCell.isEmpty()) continue;
                if ((d2 += bl2 ? indexedCell.getHeight() : indexedCell.getWidth()) > d3) break;
                ++n4;
            }
            virtualScrollBar2.setMax(1.0);
            if (n4 == 0 && n == 1) {
                virtualScrollBar2.setVisibleAmount(d3 / d2);
            } else {
                virtualScrollBar2.setVisibleAmount((float)n4 / (float)n);
            }
        }
        if (virtualScrollBar2.isVisible()) {
            if (!Properties.IS_TOUCH_SUPPORTED) {
                if (bl2) {
                    this.vbar.resizeRelocate(d, 0.0, this.vbar.prefWidth(d4), d4);
                } else {
                    this.hbar.resizeRelocate(0.0, d, d4, this.hbar.prefHeight(-1.0));
                }
            } else if (bl2) {
                this.vbar.resizeRelocate(d - this.vbar.getWidth(), 0.0, this.vbar.prefWidth(d4), d4);
            } else {
                this.hbar.resizeRelocate(0.0, d - this.hbar.getHeight(), d4, this.hbar.prefHeight(-1.0));
            }
        }
        if (this.corner.isVisible()) {
            if (!Properties.IS_TOUCH_SUPPORTED) {
                this.corner.resize(this.vbar.getWidth(), this.hbar.getHeight());
                this.corner.relocate(this.hbar.getLayoutX() + this.hbar.getWidth(), this.vbar.getLayoutY() + this.vbar.getHeight());
            } else {
                this.corner.resize(this.vbar.getWidth(), this.hbar.getHeight());
                this.corner.relocate(this.hbar.getLayoutX() + (this.hbar.getWidth() - this.vbar.getWidth()), this.vbar.getLayoutY() + (this.vbar.getHeight() - this.hbar.getHeight()));
                this.hbar.resize(this.hbar.getWidth() - this.vbar.getWidth(), this.hbar.getHeight());
                this.vbar.resize(this.vbar.getWidth(), this.vbar.getHeight() - this.hbar.getHeight());
            }
        }
        this.clipView.resize(this.snapSizeX(bl2 ? d : d4), this.snapSizeY(bl2 ? d4 : d));
        if (this.getPosition() != virtualScrollBar2.getValue()) {
            virtualScrollBar2.setValue(this.getPosition());
        }
    }

    private void fitCells() {
        double d = Math.max(this.getMaxPrefBreadth(), this.getViewportBreadth());
        boolean bl = this.isVertical();
        for (int i = 0; i < this.cells.size(); ++i) {
            Cell cell = (Cell)this.cells.get(i);
            if (bl) {
                cell.resize(d, cell.prefHeight(d));
                continue;
            }
            cell.resize(cell.prefWidth(d), d);
        }
    }

    private void cull() {
        double d = this.getViewportLength();
        for (int i = this.cells.size() - 1; i >= 0; --i) {
            IndexedCell indexedCell = (IndexedCell)this.cells.get(i);
            double d2 = this.getCellLength(indexedCell);
            double d3 = this.getCellPosition(indexedCell);
            double d4 = d3 + d2;
            if (!(d3 >= d) && !(d4 < 0.0)) continue;
            this.addToPile((IndexedCell)this.cells.remove(i));
        }
    }

    private void releaseCell(T t) {
        if (this.accumCell != null && t == this.accumCell) {
            ((IndexedCell)this.accumCell).updateIndex(-1);
        }
    }

    T getPrivateCell(int n) {
        Callback<VirtualFlow<T>, T> callback;
        Parent parent = null;
        if (!this.cells.isEmpty() && (parent = (Parent)this.getVisibleCell(n)) != null) {
            parent.layout();
            return (T)parent;
        }
        if (parent == null) {
            for (int i = 0; i < this.sheetChildren.size(); ++i) {
                IndexedCell indexedCell = (IndexedCell)this.sheetChildren.get(i);
                if (this.getCellIndex(indexedCell) != n) continue;
                return (T)indexedCell;
            }
        }
        if ((callback = this.getCellFactory()) != null) {
            parent = (IndexedCell)callback.call(this);
        }
        if (parent != null) {
            this.setCellIndex(parent, n);
            this.resizeCellSize(parent);
            parent.setVisible(false);
            this.sheetChildren.add(parent);
            this.privateCells.add(parent);
        }
        return (T)parent;
    }

    private void releaseAllPrivateCells() {
        this.sheetChildren.removeAll((Collection<?>)this.privateCells);
        this.privateCells.clear();
    }

    private void addToPile(T t) {
        assert (t != null);
        this.pile.addLast(t);
    }

    private void cleanPile() {
        boolean bl = false;
        int n = this.pile.size();
        for (int i = 0; i < n; ++i) {
            IndexedCell indexedCell = (IndexedCell)this.pile.get(i);
            bl = bl || this.doesCellContainFocus(indexedCell);
            indexedCell.setVisible(false);
        }
        if (bl) {
            this.requestFocus();
        }
    }

    private boolean doesCellContainFocus(Cell<?> cell) {
        Node node;
        Scene scene = cell.getScene();
        Node node2 = node = scene == null ? null : scene.getFocusOwner();
        if (node != null) {
            if (cell.equals(node)) {
                return true;
            }
            for (Parent parent = node.getParent(); parent != null && !(parent instanceof VirtualFlow); parent = parent.getParent()) {
                if (!cell.equals(parent)) continue;
                return true;
            }
        }
        return false;
    }

    private double getPrefBreadth(double d) {
        double d2 = this.getMaxCellWidth(10);
        if (d > -1.0) {
            double d3 = this.getPrefLength();
            d2 = Math.max(d2, d3 * 0.618033987);
        }
        return d2;
    }

    private double getPrefLength() {
        double d = 0.0;
        int n = Math.min(10, this.getCellCount());
        for (int i = 0; i < n; ++i) {
            d += this.getCellLength(i);
        }
        return d;
    }

    double getMaxCellWidth(int n) {
        double d = 0.0;
        int n2 = Math.max(1, n == -1 ? this.getCellCount() : n);
        for (int i = 0; i < n2; ++i) {
            d = Math.max(d, this.getCellBreadth(i));
        }
        return d;
    }

    private double computeViewportOffset(double d) {
        double d2 = com.sun.javafx.util.Utils.clamp(0.0, d, 1.0);
        double d3 = d2 * (double)this.getCellCount();
        int n = (int)d3;
        double d4 = d3 - (double)n;
        double d5 = this.getCellLength(n);
        double d6 = d5 * d4;
        double d7 = this.getViewportLength() * d2;
        return d6 - d7;
    }

    private void adjustPositionToIndex(int n) {
        int n2 = this.getCellCount();
        if (n2 <= 0) {
            this.setPosition(0.0);
        } else {
            this.setPosition((double)n / (double)n2);
        }
    }

    private void adjustByPixelAmount(double d) {
        double d2;
        if (d == 0.0) {
            return;
        }
        boolean bl = d > 0.0;
        int n = this.getCellCount();
        double d3 = this.getPosition() * (double)n;
        int n2 = (int)d3;
        if (bl && n2 == n) {
            return;
        }
        double d4 = this.getCellLength(n2);
        double d5 = d3 - (double)n2;
        double d6 = d4 * d5;
        double d7 = 1.0 / (double)n;
        double d8 = this.computeOffsetForCell(n2);
        double d9 = d4 + this.computeOffsetForCell(n2 + 1);
        double d10 = d9 - d8;
        double d11 = d7 * (double)n2;
        for (d2 = bl ? d + d6 - this.getViewportLength() * this.getPosition() - d8 : -d + d9 - (d6 - this.getViewportLength() * this.getPosition()); d2 > d10 && (bl && n2 < n - 1 || !bl && n2 > 0); d2 -= d10) {
            n2 = bl ? ++n2 : --n2;
            d4 = this.getCellLength(n2);
            d8 = this.computeOffsetForCell(n2);
            d9 = d4 + this.computeOffsetForCell(n2 + 1);
            d10 = d9 - d8;
            d11 = d7 * (double)n2;
        }
        if (d2 > d10) {
            this.setPosition(bl ? 1.0 : 0.0);
        } else if (bl) {
            double d12 = d7 / Math.abs(d9 - d8);
            this.setPosition(d11 + d12 * d2);
        } else {
            double d13 = d7 / Math.abs(d9 - d8);
            this.setPosition(d11 + d7 - d13 * d2);
        }
    }

    private int computeCurrentIndex() {
        return (int)(this.getPosition() * (double)this.getCellCount());
    }

    private double computeOffsetForCell(int n) {
        double d = this.getCellCount();
        double d2 = com.sun.javafx.util.Utils.clamp(0.0, (double)n, d) / d;
        return -(this.getViewportLength() * d2);
    }

    static class ArrayLinkedList<T>
    extends AbstractList<T> {
        private final ArrayList<T> array = new ArrayList(50);
        private int firstIndex = -1;
        private int lastIndex = -1;

        public ArrayLinkedList() {
            for (int i = 0; i < 50; ++i) {
                this.array.add(null);
            }
        }

        @Override
        public T getFirst() {
            return this.firstIndex == -1 ? null : (T)this.array.get(this.firstIndex);
        }

        @Override
        public T getLast() {
            return this.lastIndex == -1 ? null : (T)this.array.get(this.lastIndex);
        }

        @Override
        public void addFirst(T t) {
            if (this.firstIndex == -1) {
                this.firstIndex = this.lastIndex = this.array.size() / 2;
                this.array.set(this.firstIndex, t);
            } else if (this.firstIndex == 0) {
                this.array.add(0, t);
                ++this.lastIndex;
            } else {
                this.array.set(--this.firstIndex, t);
            }
        }

        @Override
        public void addLast(T t) {
            if (this.firstIndex == -1) {
                this.firstIndex = this.lastIndex = this.array.size() / 2;
                this.array.set(this.lastIndex, t);
            } else if (this.lastIndex == this.array.size() - 1) {
                this.array.add(++this.lastIndex, t);
            } else {
                this.array.set(++this.lastIndex, t);
            }
        }

        @Override
        public int size() {
            return this.firstIndex == -1 ? 0 : this.lastIndex - this.firstIndex + 1;
        }

        @Override
        public boolean isEmpty() {
            return this.firstIndex == -1;
        }

        @Override
        public T get(int n) {
            if (n > this.lastIndex - this.firstIndex || n < 0) {
                return null;
            }
            return this.array.get(this.firstIndex + n);
        }

        @Override
        public void clear() {
            for (int i = 0; i < this.array.size(); ++i) {
                this.array.set(i, null);
            }
            this.lastIndex = -1;
            this.firstIndex = -1;
        }

        @Override
        public T removeFirst() {
            if (this.isEmpty()) {
                return null;
            }
            return this.remove(0);
        }

        @Override
        public T removeLast() {
            if (this.isEmpty()) {
                return null;
            }
            return this.remove(this.lastIndex - this.firstIndex);
        }

        @Override
        public T remove(int n) {
            if (n > this.lastIndex - this.firstIndex || n < 0) {
                throw new ArrayIndexOutOfBoundsException();
            }
            if (n == 0) {
                T t = this.array.get(this.firstIndex);
                this.array.set(this.firstIndex, null);
                if (this.firstIndex == this.lastIndex) {
                    this.lastIndex = -1;
                    this.firstIndex = -1;
                } else {
                    ++this.firstIndex;
                }
                return t;
            }
            if (n == this.lastIndex - this.firstIndex) {
                T t = this.array.get(this.lastIndex);
                this.array.set(this.lastIndex--, null);
                return t;
            }
            T t = this.array.get(this.firstIndex + n);
            this.array.set(this.firstIndex + n, null);
            for (int i = this.firstIndex + n + 1; i <= this.lastIndex; ++i) {
                this.array.set(i - 1, this.array.get(i));
            }
            this.array.set(this.lastIndex--, null);
            return t;
        }
    }

    static class ClippedContainer
    extends Region {
        private Node node;
        private final Rectangle clipRect;

        public Node getNode() {
            return this.node;
        }

        public void setNode(Node node) {
            this.node = node;
            this.getChildren().clear();
            this.getChildren().add(this.node);
        }

        public void setClipX(double d) {
            this.setLayoutX(-d);
            this.clipRect.setLayoutX(d);
        }

        public void setClipY(double d) {
            this.setLayoutY(-d);
            this.clipRect.setLayoutY(d);
        }

        public ClippedContainer(VirtualFlow<?> virtualFlow) {
            if (virtualFlow == null) {
                throw new IllegalArgumentException("VirtualFlow can not be null");
            }
            this.getStyleClass().add("clipped-container");
            this.clipRect = new Rectangle();
            this.clipRect.setSmooth(false);
            this.setClip(this.clipRect);
            super.widthProperty().addListener(observable -> this.clipRect.setWidth(this.getWidth()));
            super.heightProperty().addListener(observable -> this.clipRect.setHeight(this.getHeight()));
        }
    }
}

