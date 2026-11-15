/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.WeakHashMap;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableSkinUtils;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.TableViewSkinBase;
import javafx.scene.control.skin.TreeTableViewSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;

public class NestedTableColumnHeader
extends TableColumnHeader {
    static final String DEFAULT_STYLE_CLASS = "nested-column-header";
    private static final int DRAG_RECT_WIDTH = 4;
    private static final String TABLE_COLUMN_KEY = "TableColumn";
    private static final String TABLE_COLUMN_HEADER_KEY = "TableColumnHeader";
    private ObservableList<? extends TableColumnBase> columns;
    private TableColumnHeader label;
    private ObservableList<TableColumnHeader> columnHeaders;
    private ObservableList<TableColumnHeader> unmodifiableColumnHeaders;
    private double lastX = 0.0;
    private double dragAnchorX = 0.0;
    private Map<TableColumnBase<?, ?>, Rectangle> dragRects = new WeakHashMap();
    boolean updateColumns = true;
    private final ListChangeListener<TableColumnBase> columnsListener = change -> this.setHeadersNeedUpdate();
    private final WeakListChangeListener weakColumnsListener = new WeakListChangeListener<TableColumnBase>(this.columnsListener);
    private static final EventHandler<MouseEvent> rectMousePressed = mouseEvent -> {
        Rectangle rectangle = (Rectangle)mouseEvent.getSource();
        TableColumnBase tableColumnBase = (TableColumnBase)rectangle.getProperties().get(TABLE_COLUMN_KEY);
        NestedTableColumnHeader nestedTableColumnHeader = (NestedTableColumnHeader)rectangle.getProperties().get(TABLE_COLUMN_HEADER_KEY);
        if (!nestedTableColumnHeader.isColumnResizingEnabled()) {
            return;
        }
        if (nestedTableColumnHeader.getTableHeaderRow().columnDragLock) {
            return;
        }
        if (mouseEvent.isConsumed()) {
            return;
        }
        mouseEvent.consume();
        if (mouseEvent.getClickCount() == 2 && mouseEvent.isPrimaryButtonDown()) {
            TableSkinUtils.resizeColumnToFitContent(nestedTableColumnHeader.getTableSkin(), tableColumnBase, -1);
        } else {
            Rectangle rectangle2 = (Rectangle)mouseEvent.getSource();
            double d = nestedTableColumnHeader.getTableHeaderRow().sceneToLocal(rectangle2.localToScene(rectangle2.getBoundsInLocal())).getMinX() + 2.0;
            nestedTableColumnHeader.dragAnchorX = mouseEvent.getSceneX();
            nestedTableColumnHeader.columnResizingStarted(d);
        }
    };
    private static final EventHandler<MouseEvent> rectMouseDragged = mouseEvent -> {
        Rectangle rectangle = (Rectangle)mouseEvent.getSource();
        TableColumnBase tableColumnBase = (TableColumnBase)rectangle.getProperties().get(TABLE_COLUMN_KEY);
        NestedTableColumnHeader nestedTableColumnHeader = (NestedTableColumnHeader)rectangle.getProperties().get(TABLE_COLUMN_HEADER_KEY);
        if (!nestedTableColumnHeader.isColumnResizingEnabled()) {
            return;
        }
        if (nestedTableColumnHeader.getTableHeaderRow().columnDragLock) {
            return;
        }
        if (mouseEvent.isConsumed()) {
            return;
        }
        mouseEvent.consume();
        nestedTableColumnHeader.columnResizing(tableColumnBase, (MouseEvent)mouseEvent);
    };
    private static final EventHandler<MouseEvent> rectMouseReleased = mouseEvent -> {
        Rectangle rectangle = (Rectangle)mouseEvent.getSource();
        TableColumnBase tableColumnBase = (TableColumnBase)rectangle.getProperties().get(TABLE_COLUMN_KEY);
        NestedTableColumnHeader nestedTableColumnHeader = (NestedTableColumnHeader)rectangle.getProperties().get(TABLE_COLUMN_HEADER_KEY);
        if (!nestedTableColumnHeader.isColumnResizingEnabled()) {
            return;
        }
        if (nestedTableColumnHeader.getTableHeaderRow().columnDragLock) {
            return;
        }
        if (mouseEvent.isConsumed()) {
            return;
        }
        mouseEvent.consume();
        nestedTableColumnHeader.columnResizingComplete(tableColumnBase, (MouseEvent)mouseEvent);
    };
    private static final EventHandler<MouseEvent> rectCursorChangeListener = mouseEvent -> {
        Rectangle rectangle = (Rectangle)mouseEvent.getSource();
        TableColumnBase tableColumnBase = (TableColumnBase)rectangle.getProperties().get(TABLE_COLUMN_KEY);
        NestedTableColumnHeader nestedTableColumnHeader = (NestedTableColumnHeader)rectangle.getProperties().get(TABLE_COLUMN_HEADER_KEY);
        if (nestedTableColumnHeader.getTableHeaderRow().columnDragLock) {
            return;
        }
        if (nestedTableColumnHeader.getCursor() == null) {
            rectangle.setCursor(nestedTableColumnHeader.isColumnResizingEnabled() && rectangle.isHover() && tableColumnBase.isResizable() ? Cursor.H_RESIZE : null);
        }
    };

    public NestedTableColumnHeader(TableColumnBase tableColumnBase) {
        super(tableColumnBase);
        this.setFocusTraversable(false);
        this.label = this.createTableColumnHeader(this.getTableColumn());
        this.label.setTableHeaderRow(this.getTableHeaderRow());
        this.label.setParentHeader(this.getParentHeader());
        this.label.setNestedColumnHeader(this);
        if (this.getTableColumn() != null) {
            this.changeListenerHandler.registerChangeListener(this.getTableColumn().textProperty(), observableValue -> this.label.setVisible(this.getTableColumn().getText() != null && !this.getTableColumn().getText().isEmpty()));
        }
    }

    @Override
    void dispose() {
        super.dispose();
        if (this.label != null) {
            this.label.dispose();
        }
        if (this.getColumns() != null) {
            this.getColumns().removeListener(this.weakColumnsListener);
        }
        for (int i = 0; i < this.getColumnHeaders().size(); ++i) {
            TableColumnHeader node = (TableColumnHeader)this.getColumnHeaders().get(i);
            node.dispose();
        }
        for (Rectangle rectangle : this.dragRects.values()) {
            if (rectangle == null) continue;
            rectangle.visibleProperty().unbind();
        }
        this.dragRects.clear();
        this.getChildren().clear();
        this.changeListenerHandler.dispose();
    }

    public final ObservableList<TableColumnHeader> getColumnHeaders() {
        if (this.columnHeaders == null) {
            this.columnHeaders = FXCollections.observableArrayList();
            this.unmodifiableColumnHeaders = FXCollections.unmodifiableObservableList(this.columnHeaders);
        }
        return this.unmodifiableColumnHeaders;
    }

    @Override
    protected void layoutChildren() {
        double d = this.getWidth() - this.snappedLeftInset() - this.snappedRightInset();
        double d2 = this.getHeight() - this.snappedTopInset() - this.snappedBottomInset();
        int n = 0;
        if (this.label.isVisible() && this.getTableColumn() != null) {
            n = (int)this.label.prefHeight(-1.0);
            this.label.resize(d, n);
            this.label.relocate(this.snappedLeftInset(), this.snappedTopInset());
        }
        double d3 = this.snappedLeftInset();
        double d4 = this.snapSizeY(d2 - (double)n);
        int n2 = this.getColumnHeaders().size();
        for (int i = 0; i < n2; ++i) {
            TableColumnHeader tableColumnHeader = (TableColumnHeader)this.getColumnHeaders().get(i);
            if (!tableColumnHeader.isVisible()) continue;
            double d5 = tableColumnHeader.prefWidth(d4);
            tableColumnHeader.resize(d5, d4);
            tableColumnHeader.relocate(d3, (double)n + this.snappedTopInset());
            d3 += d5;
            Rectangle rectangle = this.dragRects.get(tableColumnHeader.getTableColumn());
            if (rectangle == null) continue;
            rectangle.setHeight(tableColumnHeader.getDragRectHeight());
            rectangle.relocate(d3 - 2.0, this.snappedTopInset() + (double)n);
        }
    }

    @Override
    protected double computePrefWidth(double d) {
        this.checkState();
        double d2 = 0.0;
        if (this.getColumns() != null) {
            for (TableColumnHeader tableColumnHeader : this.getColumnHeaders()) {
                if (!tableColumnHeader.isVisible()) continue;
                d2 += tableColumnHeader.computePrefWidth(d);
            }
        }
        return d2;
    }

    @Override
    protected double computePrefHeight(double d) {
        this.checkState();
        double d2 = 0.0;
        if (this.getColumnHeaders() != null) {
            for (TableColumnHeader tableColumnHeader : this.getColumnHeaders()) {
                d2 = Math.max(d2, tableColumnHeader.prefHeight(-1.0));
            }
        }
        double d3 = 0.0;
        if (this.label.isVisible() && this.getTableColumn() != null) {
            d3 = this.label.prefHeight(-1.0);
        }
        return d2 + d3 + this.snappedTopInset() + this.snappedBottomInset();
    }

    protected TableColumnHeader createTableColumnHeader(TableColumnBase tableColumnBase) {
        return tableColumnBase == null || tableColumnBase.getColumns().isEmpty() || tableColumnBase == this.getTableColumn() ? new TableColumnHeader(tableColumnBase) : new NestedTableColumnHeader(tableColumnBase);
    }

    @Override
    void initStyleClasses() {
        this.getStyleClass().setAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.installTableColumnStyleClassListener();
    }

    @Override
    void setTableHeaderRow(TableHeaderRow tableHeaderRow) {
        super.setTableHeaderRow(tableHeaderRow);
        if (this.getTableSkin() != null) {
            this.changeListenerHandler.registerChangeListener(TableSkinUtils.columnResizePolicyProperty(this.getTableSkin()), observableValue -> this.updateContent());
        }
        this.label.setTableHeaderRow(tableHeaderRow);
        for (TableColumnHeader tableColumnHeader : this.getColumnHeaders()) {
            tableColumnHeader.setTableHeaderRow(tableHeaderRow);
        }
    }

    @Override
    void setParentHeader(NestedTableColumnHeader nestedTableColumnHeader) {
        super.setParentHeader(nestedTableColumnHeader);
        this.label.setParentHeader(nestedTableColumnHeader);
    }

    ObservableList<? extends TableColumnBase> getColumns() {
        return this.columns;
    }

    void setColumns(ObservableList<? extends TableColumnBase> observableList) {
        if (this.columns != null) {
            this.columns.removeListener(this.weakColumnsListener);
        }
        this.columns = observableList;
        if (this.columns != null) {
            this.columns.addListener(this.weakColumnsListener);
        }
    }

    void updateTableColumnHeaders() {
        int n;
        Object object;
        if (this.getTableColumn() == null && this.getTableSkin() != null) {
            this.setColumns(TableSkinUtils.getColumns(this.getTableSkin()));
        } else if (this.getTableColumn() != null) {
            this.setColumns(this.getTableColumn().getColumns());
        }
        if (this.getColumns().isEmpty()) {
            for (int i = 0; i < this.getColumnHeaders().size(); ++i) {
                TableColumnHeader observableList2 = (TableColumnHeader)this.getColumnHeaders().get(i);
                observableList2.dispose();
            }
            object = this.getParentHeader();
            if (object != null) {
                ObservableList<TableColumnHeader> observableList = ((NestedTableColumnHeader)object).getColumnHeaders();
                n = observableList.indexOf(this);
                if (n >= 0 && n < observableList.size()) {
                    observableList.set(n, this.createColumnHeader(this.getTableColumn()));
                }
            } else {
                this.columnHeaders.clear();
            }
        } else {
            object = new ArrayList<TableColumnHeader>(this.getColumnHeaders());
            ArrayList<TableColumnHeader> arrayList = new ArrayList<TableColumnHeader>();
            for (n = 0; n < this.getColumns().size(); ++n) {
                TableColumnBase tableColumnBase = (TableColumnBase)this.getColumns().get(n);
                if (tableColumnBase == null || !tableColumnBase.isVisible()) continue;
                boolean bl = false;
                for (int i = 0; i < object.size(); ++i) {
                    TableColumnHeader tableColumnHeader = (TableColumnHeader)object.get(i);
                    if (!tableColumnHeader.represents(tableColumnBase)) continue;
                    arrayList.add(tableColumnHeader);
                    bl = true;
                    break;
                }
                if (bl) continue;
                arrayList.add(this.createColumnHeader(tableColumnBase));
            }
            this.columnHeaders.setAll((Collection<TableColumnHeader>)arrayList);
            object.removeAll(arrayList);
            for (n = 0; n < object.size(); ++n) {
                ((TableColumnHeader)object.get(n)).dispose();
            }
        }
        this.updateContent();
        for (TableColumnHeader tableColumnHeader : this.getColumnHeaders()) {
            tableColumnHeader.applyCss();
        }
    }

    @Override
    boolean represents(TableColumnBase<?, ?> tableColumnBase) {
        int n;
        if (tableColumnBase.getColumns().isEmpty()) {
            return false;
        }
        if (tableColumnBase != this.getTableColumn()) {
            return false;
        }
        int n2 = tableColumnBase.getColumns().size();
        if (n2 != (n = this.getColumnHeaders().size())) {
            return false;
        }
        for (int i = 0; i < n2; ++i) {
            TableColumnBase tableColumnBase2 = (TableColumnBase)tableColumnBase.getColumns().get(i);
            TableColumnHeader tableColumnHeader = (TableColumnHeader)this.getColumnHeaders().get(i);
            if (tableColumnHeader.represents(tableColumnBase2)) continue;
            return false;
        }
        return true;
    }

    @Override
    double getDragRectHeight() {
        return this.label.prefHeight(-1.0);
    }

    void setHeadersNeedUpdate() {
        this.updateColumns = true;
        for (int i = 0; i < this.getColumnHeaders().size(); ++i) {
            TableColumnHeader tableColumnHeader = (TableColumnHeader)this.getColumnHeaders().get(i);
            if (!(tableColumnHeader instanceof NestedTableColumnHeader)) continue;
            ((NestedTableColumnHeader)tableColumnHeader).setHeadersNeedUpdate();
        }
        this.requestLayout();
    }

    private void updateContent() {
        ArrayList<Node> arrayList = new ArrayList<Node>();
        arrayList.add(this.label);
        arrayList.addAll(this.getColumnHeaders());
        if (this.isColumnResizingEnabled()) {
            this.rebuildDragRects();
            arrayList.addAll(this.dragRects.values());
        }
        this.getChildren().setAll((Collection<Node>)arrayList);
    }

    private void rebuildDragRects() {
        if (!this.isColumnResizingEnabled()) {
            return;
        }
        this.getChildren().removeAll(this.dragRects.values());
        for (Rectangle rectangle : this.dragRects.values()) {
            rectangle.visibleProperty().unbind();
        }
        this.dragRects.clear();
        ObservableList<? extends TableColumnBase> observableList = this.getColumns();
        if (observableList == null) {
            return;
        }
        boolean bl = false;
        TableViewSkinBase<?, ?, ?, ?, ?> tableViewSkinBase = this.getTableSkin();
        Callback callback = (Callback)TableSkinUtils.columnResizePolicyProperty(tableViewSkinBase).get();
        if (callback != null) {
            boolean bl2 = tableViewSkinBase instanceof TableViewSkin ? TableView.CONSTRAINED_RESIZE_POLICY.equals(callback) : (bl = tableViewSkinBase instanceof TreeTableViewSkin ? TreeTableView.CONSTRAINED_RESIZE_POLICY.equals(callback) : false);
        }
        if (bl && TableSkinUtils.getVisibleLeafColumns(tableViewSkinBase).size() == 1) {
            return;
        }
        for (int i = 0; !(i >= observableList.size() || bl && i == this.getColumns().size() - 1); ++i) {
            TableColumnBase tableColumnBase = (TableColumnBase)observableList.get(i);
            Rectangle rectangle = new Rectangle();
            rectangle.getProperties().put(TABLE_COLUMN_KEY, tableColumnBase);
            rectangle.getProperties().put(TABLE_COLUMN_HEADER_KEY, this);
            rectangle.setWidth(4.0);
            rectangle.setHeight(this.getHeight() - this.label.getHeight());
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.visibleProperty().bind(tableColumnBase.visibleProperty().and(tableColumnBase.resizableProperty()));
            rectangle.setOnMousePressed(rectMousePressed);
            rectangle.setOnMouseDragged(rectMouseDragged);
            rectangle.setOnMouseReleased(rectMouseReleased);
            rectangle.setOnMouseEntered(rectCursorChangeListener);
            rectangle.setOnMouseExited(rectCursorChangeListener);
            this.dragRects.put(tableColumnBase, rectangle);
        }
    }

    private void checkState() {
        if (this.updateColumns) {
            this.updateTableColumnHeaders();
            this.updateColumns = false;
        }
    }

    private TableColumnHeader createColumnHeader(TableColumnBase tableColumnBase) {
        TableColumnHeader tableColumnHeader = this.createTableColumnHeader(tableColumnBase);
        tableColumnHeader.setTableHeaderRow(this.getTableHeaderRow());
        tableColumnHeader.setParentHeader(this);
        return tableColumnHeader;
    }

    private boolean isColumnResizingEnabled() {
        return true;
    }

    private void columnResizingStarted(double d) {
        this.setCursor(Cursor.H_RESIZE);
        this.columnReorderLine.setLayoutX(d);
    }

    private void columnResizing(TableColumnBase tableColumnBase, MouseEvent mouseEvent) {
        double d = mouseEvent.getSceneX() - this.dragAnchorX;
        if (this.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
            d = -d;
        }
        double d2 = d - this.lastX;
        boolean bl = TableSkinUtils.resizeColumn(this.getTableSkin(), tableColumnBase, d2);
        if (bl) {
            this.lastX = d;
        }
    }

    private void columnResizingComplete(TableColumnBase tableColumnBase, MouseEvent mouseEvent) {
        this.setCursor(null);
        this.columnReorderLine.setTranslateX(0.0);
        this.columnReorderLine.setLayoutX(0.0);
        this.lastX = 0.0;
    }
}

