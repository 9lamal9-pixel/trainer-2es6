/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.PlatformUtil;
import com.sun.javafx.tk.Toolkit;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javafx.animation.FadeTransition;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.css.StyleOrigin;
import javafx.css.StyleableObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.skin.CellSkinBase;
import javafx.scene.control.skin.VirtualFlow;
import javafx.util.Duration;

public abstract class TableRowSkinBase<T, C extends IndexedCell, R extends IndexedCell>
extends CellSkinBase<C> {
    private static boolean IS_STUB_TOOLKIT = Toolkit.getToolkit().toString().contains("StubToolkit");
    private static boolean DO_ANIMATIONS = !IS_STUB_TOOLKIT && !PlatformUtil.isEmbedded();
    private static final Duration FADE_DURATION = Duration.millis(200.0);
    static final Map<TableColumnBase<?, ?>, Double> maxDisclosureWidthMap = new WeakHashMap();
    private static final int DEFAULT_FULL_REFRESH_COUNTER = 100;
    WeakHashMap<TableColumnBase, Reference<R>> cellsMap;
    final List<R> cells = new ArrayList<R>();
    private int fullRefreshCounter = 100;
    boolean isDirty = false;
    boolean updateCells = false;
    double fixedCellSize;
    boolean fixedCellSizeEnabled;
    private ListChangeListener<TableColumnBase> visibleLeafColumnsListener = change -> {
        this.isDirty = true;
        ((IndexedCell)this.getSkinnable()).requestLayout();
    };
    private WeakListChangeListener<TableColumnBase> weakVisibleLeafColumnsListener = new WeakListChangeListener<TableColumnBase>(this.visibleLeafColumnsListener);

    public TableRowSkinBase(C c) {
        super(c);
        ((IndexedCell)this.getSkinnable()).setPickOnBounds(false);
        this.recreateCells();
        this.updateCells(true);
        this.getVisibleLeafColumns().addListener(this.weakVisibleLeafColumnsListener);
        ((Cell)c).itemProperty().addListener(observable -> this.requestCellUpdate());
        this.registerChangeListener(((IndexedCell)c).indexProperty(), observableValue -> {
            if (((IndexedCell)this.getSkinnable()).isEmpty()) {
                this.requestCellUpdate();
            }
        });
    }

    protected abstract R createCell(TableColumnBase<T, ?> var1);

    protected abstract void updateCell(R var1, C var2);

    protected abstract TableColumnBase<T, ?> getTableColumn(R var1);

    protected abstract ObservableList<? extends TableColumnBase> getVisibleLeafColumns();

    protected ObjectProperty<Node> graphicProperty() {
        return null;
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        double d5;
        double d6;
        this.checkState();
        if (this.cellsMap.isEmpty()) {
            return;
        }
        ObservableList<TableColumnBase> observableList = this.getVisibleLeafColumns();
        if (observableList.isEmpty()) {
            super.layoutChildren(d, d2, d3, d4);
            return;
        }
        IndexedCell indexedCell = (IndexedCell)this.getSkinnable();
        double d7 = 0.0;
        double d8 = 0.0;
        double d9 = 0.0;
        boolean bl = this.isIndentationRequired();
        boolean bl2 = this.isDisclosureNodeVisible();
        int n = 0;
        Node node = null;
        if (bl) {
            TableColumnBase tableColumnBase = this.getTreeColumn();
            n = tableColumnBase == null ? 0 : observableList.indexOf(tableColumnBase);
            n = n < 0 ? 0 : n;
            int n2 = this.getIndentationLevel(indexedCell);
            if (!this.isShowRoot()) {
                --n2;
            }
            d6 = this.getIndentationPerLevel();
            d7 = (double)n2 * d6;
            d8 = d5 = maxDisclosureWidthMap.containsKey(tableColumnBase) ? maxDisclosureWidthMap.get(tableColumnBase) : 0.0;
            node = this.getDisclosureNode();
            if (node != null) {
                node.setVisible(bl2);
                if (bl2 && (d8 = node.prefWidth(d4)) > d5) {
                    maxDisclosureWidthMap.put(tableColumnBase, d8);
                    VirtualFlow<C> virtualFlow = this.getVirtualFlow();
                    int n3 = ((IndexedCell)this.getSkinnable()).getIndex();
                    for (int i = 0; i < virtualFlow.cells.size(); ++i) {
                        IndexedCell indexedCell2 = (IndexedCell)virtualFlow.cells.get(i);
                        if (indexedCell2 == null || indexedCell2.isEmpty()) continue;
                        indexedCell2.requestLayout();
                        indexedCell2.layout();
                    }
                }
            }
        }
        d5 = this.snappedTopInset() + this.snappedBottomInset();
        double d10 = this.snappedLeftInset() + this.snappedRightInset();
        double d11 = indexedCell.getHeight();
        int n4 = indexedCell.getIndex();
        if (n4 < 0) {
            return;
        }
        int n5 = this.cells.size();
        for (int i = 0; i < n5; ++i) {
            double d12;
            IndexedCell indexedCell3 = (IndexedCell)this.cells.get(i);
            TableColumnBase<T, ?> tableColumnBase = this.getTableColumn(indexedCell3);
            boolean bl3 = true;
            if (this.fixedCellSizeEnabled) {
                bl3 = this.isColumnPartiallyOrFullyVisible(tableColumnBase);
                d6 = this.fixedCellSize;
            } else {
                d6 = Math.max(d11, indexedCell3.prefHeight(-1.0));
                d6 = this.snapSizeY(d6) - this.snapSizeY(d5);
            }
            if (bl3) {
                if (this.fixedCellSizeEnabled && indexedCell3.getParent() == null) {
                    this.getChildren().add(indexedCell3);
                }
                d12 = indexedCell3.prefWidth(d6) - this.snapSizeX(d10);
                boolean bl4 = d4 <= 24.0;
                StyleOrigin styleOrigin = ((StyleableObjectProperty)indexedCell3.alignmentProperty()).getStyleOrigin();
                if (!bl4 && styleOrigin == null) {
                    indexedCell3.setAlignment(Pos.TOP_LEFT);
                }
                if (bl && i == n) {
                    ObjectProperty<Node> objectProperty;
                    Node node2;
                    if (bl2) {
                        double d13 = node.prefHeight(d8);
                        if (d12 > 0.0 && d12 < d8 + d7) {
                            this.fadeOut(node);
                        } else {
                            this.fadeIn(node);
                            node.resize(d8, d13);
                            node.relocate(d + d7, bl4 ? d4 / 2.0 - d13 / 2.0 : d2 + indexedCell3.getPadding().getTop());
                            node.toFront();
                        }
                    }
                    Node node3 = node2 = (objectProperty = this.graphicProperty()) == null ? null : (Node)objectProperty.get();
                    if (node2 != null) {
                        d9 = node2.prefWidth(-1.0) + 3.0;
                        double d14 = node2.prefHeight(d9);
                        if (d12 > 0.0 && d12 < d8 + d7 + d9) {
                            this.fadeOut(node2);
                        } else {
                            this.fadeIn(node2);
                            node2.relocate(d + d7 + d8, bl4 ? d4 / 2.0 - d14 / 2.0 : d2 + indexedCell3.getPadding().getTop());
                            node2.toFront();
                        }
                    }
                }
                indexedCell3.resize(d12, d6);
                indexedCell3.relocate(d, this.snappedTopInset());
                indexedCell3.requestLayout();
            } else {
                d12 = this.snapSizeX(indexedCell3.prefWidth(-1.0)) - this.snapSizeX(d10);
                if (this.fixedCellSizeEnabled) {
                    this.getChildren().remove(indexedCell3);
                }
            }
            d += d12;
        }
    }

    int getIndentationLevel(C c) {
        return 0;
    }

    double getIndentationPerLevel() {
        return 0.0;
    }

    boolean isIndentationRequired() {
        return false;
    }

    TableColumnBase getTreeColumn() {
        return null;
    }

    Node getDisclosureNode() {
        return null;
    }

    boolean isDisclosureNodeVisible() {
        return false;
    }

    boolean isShowRoot() {
        return true;
    }

    void updateCells(boolean bl) {
        if (bl) {
            if (this.fullRefreshCounter == 0) {
                this.recreateCells();
            }
            --this.fullRefreshCounter;
        }
        boolean bl2 = this.cells.isEmpty();
        this.cells.clear();
        IndexedCell indexedCell = (IndexedCell)this.getSkinnable();
        int n = indexedCell.getIndex();
        ObservableList<TableColumnBase> observableList = this.getVisibleLeafColumns();
        int n2 = observableList.size();
        for (int i = 0; i < n2; ++i) {
            TableColumnBase eventTarget = (TableColumnBase)observableList.get(i);
            IndexedCell indexedCell2 = null;
            if (this.cellsMap.containsKey(eventTarget) && (indexedCell2 = (IndexedCell)this.cellsMap.get(eventTarget).get()) == null) {
                this.cellsMap.remove(eventTarget);
            }
            if (indexedCell2 == null) {
                indexedCell2 = (IndexedCell)this.createCellAndCache(eventTarget);
            }
            this.updateCell(indexedCell2, indexedCell);
            indexedCell2.updateIndex(n);
            this.cells.add(indexedCell2);
        }
        if (this.fixedCellSizeEnabled) {
            ArrayList<Node> arrayList = new ArrayList<Node>();
            for (Node node : this.getChildren()) {
                if (!(node instanceof IndexedCell) || this.getTableColumn((IndexedCell)node).isVisible()) continue;
                arrayList.add(node);
            }
            this.getChildren().removeAll((Collection<?>)arrayList);
        } else if (!this.fixedCellSizeEnabled && (bl || bl2)) {
            this.getChildren().setAll((Collection<Node>)this.cells);
        }
    }

    VirtualFlow<C> getVirtualFlow() {
        for (Object object = this.getSkinnable(); object != null; object = ((Node)object).getParent()) {
            if (!(object instanceof VirtualFlow)) continue;
            return (VirtualFlow)object;
        }
        return null;
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        double d6 = 0.0;
        for (IndexedCell indexedCell : this.cells) {
            d6 += indexedCell.prefWidth(d);
        }
        return d6;
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        if (this.fixedCellSizeEnabled) {
            return this.fixedCellSize;
        }
        this.checkState();
        if (this.getCellSize() < 24.0) {
            return this.getCellSize();
        }
        double d6 = 0.0;
        int n = this.cells.size();
        for (int i = 0; i < n; ++i) {
            IndexedCell indexedCell = (IndexedCell)this.cells.get(i);
            d6 = Math.max(d6, indexedCell.prefHeight(-1.0));
        }
        double d7 = Math.max(d6, Math.max(this.getCellSize(), ((IndexedCell)this.getSkinnable()).minHeight(-1.0)));
        return d7;
    }

    @Override
    protected double computeMinHeight(double d, double d2, double d3, double d4, double d5) {
        if (this.fixedCellSizeEnabled) {
            return this.fixedCellSize;
        }
        this.checkState();
        if (this.getCellSize() < 24.0) {
            return this.getCellSize();
        }
        double d6 = 0.0;
        int n = this.cells.size();
        for (int i = 0; i < n; ++i) {
            IndexedCell indexedCell = (IndexedCell)this.cells.get(i);
            d6 = Math.max(d6, indexedCell.minHeight(-1.0));
        }
        return d6;
    }

    @Override
    protected double computeMaxHeight(double d, double d2, double d3, double d4, double d5) {
        if (this.fixedCellSizeEnabled) {
            return this.fixedCellSize;
        }
        return super.computeMaxHeight(d, d2, d3, d4, d5);
    }

    final void checkState() {
        if (this.isDirty) {
            this.updateCells(true);
            this.isDirty = false;
            this.updateCells = false;
        } else if (this.updateCells) {
            this.updateCells(false);
            this.updateCells = false;
        }
    }

    private boolean isColumnPartiallyOrFullyVisible(TableColumnBase tableColumnBase) {
        Object object;
        if (tableColumnBase == null || !tableColumnBase.isVisible()) {
            return false;
        }
        VirtualFlow<C> virtualFlow = this.getVirtualFlow();
        double d = virtualFlow == null ? 0.0 : virtualFlow.getHbar().getValue();
        double d2 = 0.0;
        ObservableList<TableColumnBase> observableList = this.getVisibleLeafColumns();
        int n = observableList.size();
        for (int i = 0; i < n && !(object = (TableColumnBase)observableList.get(i)).equals(tableColumnBase); ++i) {
            d2 += ((TableColumnBase)object).getWidth();
        }
        double d3 = d2 + tableColumnBase.getWidth();
        object = ((IndexedCell)this.getSkinnable()).getPadding();
        double d4 = ((IndexedCell)this.getSkinnable()).getWidth() - ((Insets)object).getLeft() + ((Insets)object).getRight();
        return (d2 >= d || d3 > d) && (d2 < d4 + d || d3 <= d4 + d);
    }

    private void requestCellUpdate() {
        this.updateCells = true;
        ((IndexedCell)this.getSkinnable()).requestLayout();
        int n = ((IndexedCell)this.getSkinnable()).getIndex();
        int n2 = this.cells.size();
        for (int i = 0; i < n2; ++i) {
            ((IndexedCell)this.cells.get(i)).updateIndex(n);
        }
    }

    private void recreateCells() {
        Collection<Object> collection;
        if (this.cellsMap != null) {
            collection = this.cellsMap.values();
            for (Object object : collection) {
                IndexedCell indexedCell = (IndexedCell)((Reference)object).get();
                if (indexedCell == null) continue;
                indexedCell.updateIndex(-1);
                indexedCell.getSkin().dispose();
                indexedCell.setSkin(null);
            }
            this.cellsMap.clear();
        }
        collection = this.getVisibleLeafColumns();
        this.cellsMap = new WeakHashMap(collection.size());
        this.fullRefreshCounter = 100;
        this.getChildren().clear();
        for (Object object : collection) {
            if (this.cellsMap.containsKey(object)) continue;
            this.createCellAndCache((TableColumnBase<T, ?>)object);
        }
    }

    private R createCellAndCache(TableColumnBase<T, ?> tableColumnBase) {
        R r = this.createCell(tableColumnBase);
        this.cellsMap.put(tableColumnBase, new WeakReference<R>(r));
        return r;
    }

    private void fadeOut(Node node) {
        if (node.getOpacity() < 1.0) {
            return;
        }
        if (!DO_ANIMATIONS) {
            node.setOpacity(0.0);
            return;
        }
        FadeTransition fadeTransition = new FadeTransition(FADE_DURATION, node);
        fadeTransition.setToValue(0.0);
        fadeTransition.play();
    }

    private void fadeIn(Node node) {
        if (node.getOpacity() > 0.0) {
            return;
        }
        if (!DO_ANIMATIONS) {
            node.setOpacity(1.0);
            return;
        }
        FadeTransition fadeTransition = new FadeTransition(FADE_DURATION, node);
        fadeTransition.setToValue(1.0);
        fadeTransition.play();
    }
}

