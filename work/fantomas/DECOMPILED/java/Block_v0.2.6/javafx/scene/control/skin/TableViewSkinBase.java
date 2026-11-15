/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.skin.resources.ControlResources;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.util.Iterator;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Cell;
import javafx.scene.control.Control;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollToEvent;
import javafx.scene.control.Skinnable;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableFocusModel;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableSkinUtils;
import javafx.scene.control.skin.VirtualContainerBase;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

public abstract class TableViewSkinBase<M, S, C extends Control, I extends IndexedCell<M>, TC extends TableColumnBase<S, ?>>
extends VirtualContainerBase<C, I> {
    private static final double GOLDEN_RATIO_MULTIPLIER = 0.618033987;
    private static final boolean IS_PANNABLE = AccessController.doPrivileged(() -> Boolean.getBoolean("javafx.scene.control.skin.TableViewSkin.pannable"));
    private final String EMPTY_TABLE_TEXT = ControlResources.getString("TableView.noContent");
    private final String NO_COLUMNS_TEXT = ControlResources.getString("TableView.noColumns");
    VirtualFlow<I> flow;
    private boolean contentWidthDirty = true;
    private Region columnReorderLine;
    private Region columnReorderOverlay;
    private TableHeaderRow tableHeaderRow;
    private Callback<C, I> rowFactory;
    private StackPane placeholderRegion;
    private Label placeholderLabel;
    private int visibleColCount;
    boolean needCellsRecreated = true;
    boolean needCellsReconfigured = false;
    private int itemCount = -1;
    private MapChangeListener<Object, Object> propertiesMapListener = change -> {
        if (!change.wasAdded()) {
            return;
        }
        if ("refreshKey".equals(change.getKey())) {
            this.refreshView();
            ((Node)((Object)this.getSkinnable())).getProperties().remove("refreshKey");
        } else if ("recreateKey".equals(change.getKey())) {
            this.needCellsRecreated = true;
            this.refreshView();
            ((Node)((Object)this.getSkinnable())).getProperties().remove("recreateKey");
        }
    };
    private ListChangeListener<S> rowCountListener = change -> {
        while (change.next()) {
            if (change.wasReplaced()) {
                this.itemCount = 0;
                break;
            }
            if (change.getRemovedSize() != this.itemCount) continue;
            this.itemCount = 0;
            break;
        }
        if (this.getSkinnable() instanceof TableView) {
            ((TableView)this.getSkinnable()).edit(-1, null);
        }
        this.markItemCountDirty();
        ((Parent)((Object)this.getSkinnable())).requestLayout();
    };
    private ListChangeListener<TC> visibleLeafColumnsListener = change -> {
        this.updateVisibleColumnCount();
        while (change.next()) {
            this.updateVisibleLeafColumnWidthListeners(change.getAddedSubList(), change.getRemoved());
        }
    };
    private InvalidationListener widthListener = observable -> {
        this.needCellsReconfigured = true;
        if (this.getSkinnable() != null) {
            ((Parent)((Object)this.getSkinnable())).requestLayout();
        }
    };
    private InvalidationListener itemsChangeListener;
    private WeakListChangeListener<S> weakRowCountListener = new WeakListChangeListener<S>(this.rowCountListener);
    private WeakListChangeListener<TC> weakVisibleLeafColumnsListener = new WeakListChangeListener<TC>(this.visibleLeafColumnsListener);
    private WeakInvalidationListener weakWidthListener = new WeakInvalidationListener(this.widthListener);
    private WeakInvalidationListener weakItemsChangeListener;

    public TableViewSkinBase(C c) {
        super(c);
        this.flow = this.getVirtualFlow();
        this.flow.setPannable(IS_PANNABLE);
        this.flow.getHbar().valueProperty().addListener(observable -> this.horizontalScroll());
        this.flow.getHbar().setUnitIncrement(15.0);
        this.flow.getHbar().setBlockIncrement(80.0);
        this.columnReorderLine = new Region();
        this.columnReorderLine.getStyleClass().setAll((String[])new String[]{"column-resize-line"});
        this.columnReorderLine.setManaged(false);
        this.columnReorderLine.setVisible(false);
        this.columnReorderOverlay = new Region();
        this.columnReorderOverlay.getStyleClass().setAll((String[])new String[]{"column-overlay"});
        this.columnReorderOverlay.setVisible(false);
        this.columnReorderOverlay.setManaged(false);
        this.tableHeaderRow = this.createTableHeaderRow();
        this.tableHeaderRow.setFocusTraversable(false);
        this.getChildren().addAll((Node[])new Node[]{this.tableHeaderRow, this.flow, this.columnReorderOverlay, this.columnReorderLine});
        this.updateVisibleColumnCount();
        this.updateVisibleLeafColumnWidthListeners(this.getVisibleLeafColumns(), FXCollections.emptyObservableList());
        this.tableHeaderRow.reorderingProperty().addListener(observable -> ((Parent)((Object)this.getSkinnable())).requestLayout());
        this.getVisibleLeafColumns().addListener(this.weakVisibleLeafColumnsListener);
        final ObjectProperty objectProperty = TableSkinUtils.itemsProperty(this);
        this.updateTableItems(null, (ObservableList)objectProperty.get());
        this.itemsChangeListener = new InvalidationListener(){
            private WeakReference<ObservableList<S>> weakItemsRef;
            {
                this.weakItemsRef = new WeakReference<ObservableList>((ObservableList)objectProperty.get());
            }

            @Override
            public void invalidated(Observable observable) {
                ObservableList observableList = (ObservableList)this.weakItemsRef.get();
                this.weakItemsRef = new WeakReference<ObservableList>((ObservableList)objectProperty.get());
                TableViewSkinBase.this.updateTableItems(observableList, (ObservableList)objectProperty.get());
            }
        };
        this.weakItemsChangeListener = new WeakInvalidationListener(this.itemsChangeListener);
        objectProperty.addListener(this.weakItemsChangeListener);
        ObservableMap<Object, Object> observableMap = ((Node)c).getProperties();
        observableMap.remove("refreshKey");
        observableMap.remove("recreateKey");
        observableMap.addListener(this.propertiesMapListener);
        ((Node)c).addEventHandler(ScrollToEvent.scrollToColumn(), scrollToEvent -> this.scrollHorizontally((TableColumnBase)scrollToEvent.getScrollTarget()));
        InvalidationListener invalidationListener = observable -> {
            this.contentWidthDirty = true;
            ((Parent)((Object)this.getSkinnable())).requestLayout();
        };
        this.flow.widthProperty().addListener(invalidationListener);
        this.flow.getVbar().widthProperty().addListener(invalidationListener);
        ObjectProperty objectProperty2 = TableSkinUtils.rowFactoryProperty(this);
        this.registerChangeListener(objectProperty2, observableValue -> {
            Callback<C, I> callback = this.rowFactory;
            this.rowFactory = (Callback)objectProperty2.get();
            if (callback != this.rowFactory) {
                this.requestRebuildCells();
            }
        });
        this.registerChangeListener(TableSkinUtils.placeholderProperty(this), observableValue -> this.updatePlaceholderRegionVisibility());
        this.registerChangeListener(this.flow.getVbar().visibleProperty(), observableValue -> this.updateContentWidth());
    }

    @Override
    public void dispose() {
        ObjectProperty objectProperty = TableSkinUtils.itemsProperty(this);
        this.getVisibleLeafColumns().removeListener(this.weakVisibleLeafColumnsListener);
        objectProperty.removeListener(this.weakItemsChangeListener);
        ((Node)((Object)this.getSkinnable())).getProperties().removeListener(this.propertiesMapListener);
        this.updateTableItems((ObservableList)objectProperty.get(), null);
        super.dispose();
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        return 400.0;
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        double d6 = this.computePrefHeight(-1.0, d2, d3, d4, d5);
        ObservableList<TC> observableList = this.getVisibleLeafColumns();
        if (observableList == null || observableList.isEmpty()) {
            return d6 * 0.618033987;
        }
        double d7 = d5 + d3;
        int n = observableList.size();
        for (int i = 0; i < n; ++i) {
            TableColumnBase tableColumnBase = (TableColumnBase)observableList.get(i);
            d7 += Math.max(tableColumnBase.getPrefWidth(), tableColumnBase.getMinWidth());
        }
        return Math.max(d7, d6 * 0.618033987);
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        Skinnable skinnable = this.getSkinnable();
        if (skinnable == null) {
            return;
        }
        super.layoutChildren(d, d2, d3, d4);
        if (this.needCellsRecreated) {
            this.flow.recreateCells();
        } else if (this.needCellsReconfigured) {
            this.flow.reconfigureCells();
        }
        this.needCellsRecreated = false;
        this.needCellsReconfigured = false;
        double d5 = ((Node)((Object)skinnable)).getLayoutBounds().getHeight() / 2.0;
        double d6 = this.tableHeaderRow.prefHeight(-1.0);
        this.layoutInArea(this.tableHeaderRow, d, d2, d3, d6, d5, HPos.CENTER, VPos.CENTER);
        d2 += d6;
        double d7 = Math.floor(d4 - d6);
        if (this.getItemCount() == 0 || this.visibleColCount == 0) {
            this.layoutInArea(this.placeholderRegion, d, d2, d3, d7, d5, HPos.CENTER, VPos.CENTER);
        } else {
            this.layoutInArea(this.flow, d, d2, d3, d7, d5, HPos.CENTER, VPos.CENTER);
        }
        if (this.tableHeaderRow.getReorderingRegion() != null) {
            TableColumnHeader tableColumnHeader = this.tableHeaderRow.getReorderingRegion();
            TableColumnBase<?, ?> tableColumnBase = tableColumnHeader.getTableColumn();
            if (tableColumnBase != null) {
                TableColumnHeader tableColumnHeader2 = this.tableHeaderRow.getReorderingRegion();
                double d8 = this.tableHeaderRow.sceneToLocal(tableColumnHeader2.localToScene(tableColumnHeader2.getBoundsInLocal())).getMinX();
                double d9 = tableColumnHeader.getWidth();
                if (d8 < 0.0) {
                    d9 += d8;
                }
                double d10 = d8 = d8 < 0.0 ? 0.0 : d8;
                if (d8 + d9 > d3) {
                    d9 = d3 - d8;
                    if (this.flow.getVbar().isVisible()) {
                        d9 -= this.flow.getVbar().getWidth() - 1.0;
                    }
                }
                double d11 = d7;
                if (this.flow.getHbar().isVisible()) {
                    d11 -= this.flow.getHbar().getHeight();
                }
                this.columnReorderOverlay.resize(d9, d11);
                this.columnReorderOverlay.setLayoutX(d8);
                this.columnReorderOverlay.setLayoutY(this.tableHeaderRow.getHeight());
            }
            double d12 = this.columnReorderLine.snappedLeftInset() + this.columnReorderLine.snappedRightInset();
            double d13 = d4 - (this.flow.getHbar().isVisible() ? this.flow.getHbar().getHeight() - 1.0 : 0.0);
            this.columnReorderLine.resizeRelocate(0.0, this.columnReorderLine.snappedTopInset(), d12, d13);
        }
        this.columnReorderLine.setVisible(this.tableHeaderRow.isReordering());
        this.columnReorderOverlay.setVisible(this.tableHeaderRow.isReordering());
        this.checkContentWidthState();
    }

    protected TableHeaderRow createTableHeaderRow() {
        return new TableHeaderRow(this);
    }

    final TableHeaderRow getTableHeaderRow() {
        return this.tableHeaderRow;
    }

    private TableSelectionModel<S> getSelectionModel() {
        return TableSkinUtils.getSelectionModel(this);
    }

    private TableFocusModel<M, ?> getFocusModel() {
        return TableSkinUtils.getFocusModel(this);
    }

    private TablePositionBase<? extends TC> getFocusedCell() {
        return TableSkinUtils.getFocusedCell(this);
    }

    private ObservableList<? extends TC> getVisibleLeafColumns() {
        return TableSkinUtils.getVisibleLeafColumns(this);
    }

    @Override
    protected void updateItemCount() {
        int n;
        this.updatePlaceholderRegionVisibility();
        int n2 = this.itemCount;
        this.itemCount = n = this.getItemCount();
        if (this.itemCount == 0) {
            this.flow.getHbar().setValue(0.0);
        }
        this.flow.setCellCount(n);
        if (n != n2) {
            this.requestRebuildCells();
        } else {
            this.needCellsReconfigured = true;
        }
    }

    private void checkContentWidthState() {
        if (this.contentWidthDirty || this.getItemCount() == 0) {
            this.updateContentWidth();
            this.contentWidthDirty = false;
        }
    }

    void horizontalScroll() {
        this.tableHeaderRow.updateScrollX();
    }

    void onFocusPreviousCell() {
        TableFocusModel<M, ?> tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        this.flow.scrollTo(tableFocusModel.getFocusedIndex());
    }

    void onFocusNextCell() {
        TableFocusModel<M, ?> tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        this.flow.scrollTo(tableFocusModel.getFocusedIndex());
    }

    void onSelectPreviousCell() {
        TableSelectionModel<S> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        this.flow.scrollTo(tableSelectionModel.getSelectedIndex());
    }

    void onSelectNextCell() {
        TableSelectionModel<S> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return;
        }
        this.flow.scrollTo(tableSelectionModel.getSelectedIndex());
    }

    void onSelectLeftCell() {
        this.scrollHorizontally();
    }

    void onSelectRightCell() {
        this.scrollHorizontally();
    }

    void onMoveToFirstCell() {
        this.flow.scrollTo(0);
        this.flow.setPosition(0.0);
    }

    void onMoveToLastCell() {
        int n = this.getItemCount();
        this.flow.scrollTo(n);
        this.flow.setPosition(1.0);
    }

    private void updateTableItems(ObservableList<S> observableList, ObservableList<S> observableList2) {
        if (observableList != null) {
            observableList.removeListener(this.weakRowCountListener);
        }
        if (observableList2 != null) {
            observableList2.addListener(this.weakRowCountListener);
        }
        this.markItemCountDirty();
        ((Parent)((Object)this.getSkinnable())).requestLayout();
    }

    Region getColumnReorderLine() {
        return this.columnReorderLine;
    }

    int onScrollPageDown(boolean bl) {
        int n;
        boolean bl2;
        TableSelectionModel<S> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return -1;
        }
        int n2 = this.getItemCount();
        I i = this.flow.getLastVisibleCellWithinViewPort();
        if (i == null) {
            return -1;
        }
        int n3 = ((IndexedCell)i).getIndex();
        int n4 = n3 = n3 >= n2 ? n2 - 1 : n3;
        if (bl) {
            bl2 = ((Node)i).isFocused() || this.isCellFocused(n3);
        } else {
            boolean bl3 = bl2 = ((Cell)i).isSelected() || this.isCellSelected(n3);
        }
        if (bl2 && (n = (int)(this.isLeadIndex(bl, n3) ? 1 : 0)) != 0) {
            this.flow.scrollToTop(i);
            I i2 = this.flow.getLastVisibleCellWithinViewPort();
            i = i2 == null ? i : i2;
        }
        n = (n = ((IndexedCell)i).getIndex()) >= n2 ? n2 - 1 : n;
        this.flow.scrollTo(n);
        return n;
    }

    int onScrollPageUp(boolean bl) {
        int n;
        I i = this.flow.getFirstVisibleCellWithinViewPort();
        if (i == null) {
            return -1;
        }
        int n2 = ((IndexedCell)i).getIndex();
        boolean bl2 = false;
        if (bl) {
            bl2 = ((Node)i).isFocused() || this.isCellFocused(n2);
        } else {
            boolean bl3 = bl2 = ((Cell)i).isSelected() || this.isCellSelected(n2);
        }
        if (bl2 && (n = (int)(this.isLeadIndex(bl, n2) ? 1 : 0)) != 0) {
            this.flow.scrollToBottom(i);
            I i2 = this.flow.getFirstVisibleCellWithinViewPort();
            i = i2 == null ? i : i2;
        }
        n = ((IndexedCell)i).getIndex();
        this.flow.scrollTo(n);
        return n;
    }

    private boolean isLeadIndex(boolean bl, int n) {
        TableSelectionModel<S> tableSelectionModel = this.getSelectionModel();
        TableFocusModel<M, ?> tableFocusModel = this.getFocusModel();
        return bl && tableFocusModel.getFocusedIndex() == n || !bl && tableSelectionModel.getSelectedIndex() == n;
    }

    private void updateVisibleColumnCount() {
        this.visibleColCount = this.getVisibleLeafColumns().size();
        this.updatePlaceholderRegionVisibility();
        this.requestRebuildCells();
    }

    private void updateVisibleLeafColumnWidthListeners(List<? extends TC> list, List<? extends TC> list2) {
        TableColumnBase tableColumnBase;
        int n;
        int n2 = list2.size();
        for (n = 0; n < n2; ++n) {
            tableColumnBase = (TableColumnBase)list2.get(n);
            tableColumnBase.widthProperty().removeListener(this.weakWidthListener);
        }
        n2 = list.size();
        for (n = 0; n < n2; ++n) {
            tableColumnBase = (TableColumnBase)list.get(n);
            tableColumnBase.widthProperty().addListener(this.weakWidthListener);
        }
        this.requestRebuildCells();
    }

    final void updatePlaceholderRegionVisibility() {
        boolean bl;
        boolean bl2 = bl = this.visibleColCount == 0 || this.getItemCount() == 0;
        if (bl) {
            Node node;
            if (this.placeholderRegion == null) {
                this.placeholderRegion = new StackPane();
                this.placeholderRegion.getStyleClass().setAll((String[])new String[]{"placeholder"});
                this.getChildren().add(this.placeholderRegion);
            }
            if ((node = (Node)TableSkinUtils.placeholderProperty(this).get()) == null) {
                if (this.placeholderLabel == null) {
                    this.placeholderLabel = new Label();
                }
                String string = this.visibleColCount == 0 ? this.NO_COLUMNS_TEXT : this.EMPTY_TABLE_TEXT;
                this.placeholderLabel.setText(string);
                this.placeholderRegion.getChildren().setAll((Node[])new Node[]{this.placeholderLabel});
            } else {
                this.placeholderRegion.getChildren().setAll((Node[])new Node[]{node});
            }
        }
        this.flow.setVisible(!bl);
        if (this.placeholderRegion != null) {
            this.placeholderRegion.setVisible(bl);
        }
    }

    private void updateContentWidth() {
        double d = this.flow.getWidth();
        if (this.flow.getVbar().isVisible()) {
            d -= this.flow.getVbar().getWidth();
        }
        if (d <= 0.0) {
            Skinnable skinnable = this.getSkinnable();
            d = ((Region)((Object)skinnable)).getWidth() - (this.snappedLeftInset() + this.snappedRightInset());
        }
        d = Math.max(0.0, d);
        ((Node)((Object)this.getSkinnable())).getProperties().put("TableView.contentWidth", Math.floor(d));
    }

    private void refreshView() {
        this.markItemCountDirty();
        Skinnable skinnable = this.getSkinnable();
        if (skinnable != null) {
            ((Parent)((Object)skinnable)).requestLayout();
        }
    }

    void scrollHorizontally() {
        TableFocusModel<M, ?> tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return;
        }
        TC TC = this.getFocusedCell().getTableColumn();
        this.scrollHorizontally(TC);
    }

    void scrollHorizontally(TC TC) {
        double d;
        TableColumnBase tableColumnBase;
        if (TC == null || !((TableColumnBase)TC).isVisible()) {
            return;
        }
        Skinnable skinnable = this.getSkinnable();
        TableColumnHeader tableColumnHeader = this.tableHeaderRow.getColumnHeaderFor((TableColumnBase<?, ?>)TC);
        if (tableColumnHeader == null || tableColumnHeader.getWidth() <= 0.0) {
            Platform.runLater(() -> this.scrollHorizontally(TC));
            return;
        }
        double d2 = 0.0;
        Iterator iterator = this.getVisibleLeafColumns().iterator();
        while (iterator.hasNext() && !(tableColumnBase = (TableColumnBase)iterator.next()).equals(TC)) {
            d2 += tableColumnBase.getWidth();
        }
        double d3 = d2 + ((TableColumnBase)TC).getWidth();
        double d4 = ((Region)((Object)skinnable)).getWidth() - this.snappedLeftInset() - this.snappedRightInset();
        double d5 = this.flow.getHbar().getValue();
        double d6 = this.flow.getHbar().getMax();
        if (d2 < d5 && d2 >= 0.0) {
            d = d2;
        } else {
            double d7 = d2 < 0.0 || d3 > d4 ? d2 - d5 : 0.0;
            d = d5 + d7 > d6 ? d6 : d5 + d7;
        }
        this.flow.getHbar().setValue(d);
    }

    private boolean isCellSelected(int n) {
        TableSelectionModel<S> tableSelectionModel = this.getSelectionModel();
        if (tableSelectionModel == null) {
            return false;
        }
        if (!tableSelectionModel.isCellSelectionEnabled()) {
            return false;
        }
        int n2 = this.getVisibleLeafColumns().size();
        for (int i = 0; i < n2; ++i) {
            if (!tableSelectionModel.isSelected(n, (TableColumnBase<S, ?>)TableSkinUtils.getVisibleLeafColumn(this, i))) continue;
            return true;
        }
        return false;
    }

    private boolean isCellFocused(int n) {
        TableFocusModel<M, ?> tableFocusModel = this.getFocusModel();
        if (tableFocusModel == null) {
            return false;
        }
        int n2 = this.getVisibleLeafColumns().size();
        for (int i = 0; i < n2; ++i) {
            if (!tableFocusModel.isFocused(n, TableSkinUtils.getVisibleLeafColumn(this, i))) continue;
            return true;
        }
        return false;
    }

    @Override
    protected Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case FOCUS_ITEM: {
                TableFocusModel<M, ?> tableFocusModel = this.getFocusModel();
                int n = tableFocusModel.getFocusedIndex();
                if (n == -1) {
                    if (this.placeholderRegion != null && this.placeholderRegion.isVisible()) {
                        return this.placeholderRegion.getChildren().get(0);
                    }
                    if (this.getItemCount() > 0) {
                        n = 0;
                    } else {
                        return null;
                    }
                }
                return this.flow.getPrivateCell(n);
            }
            case CELL_AT_ROW_COLUMN: {
                int n = (Integer)objectArray[0];
                return this.flow.getPrivateCell(n);
            }
            case COLUMN_AT_INDEX: {
                int n = (Integer)objectArray[0];
                Object TC = TableSkinUtils.getVisibleLeafColumn(this, n);
                return this.getTableHeaderRow().getColumnHeaderFor((TableColumnBase<?, ?>)TC);
            }
            case HEADER: {
                return this.getTableHeaderRow();
            }
            case VERTICAL_SCROLLBAR: {
                return this.flow.getVbar();
            }
            case HORIZONTAL_SCROLLBAR: {
                return this.flow.getHbar();
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }
}

