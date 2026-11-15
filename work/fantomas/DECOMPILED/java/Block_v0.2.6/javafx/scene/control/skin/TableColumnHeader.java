/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.LambdaMultiplePropertyChangeListenerHandler;
import com.sun.javafx.scene.control.Properties;
import com.sun.javafx.scene.control.TableColumnBaseHelper;
import com.sun.javafx.scene.control.TableColumnSortTypeWrapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableSkinUtils;
import javafx.scene.control.skin.TableViewSkinBase;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class TableColumnHeader
extends Region {
    static final String DEFAULT_STYLE_CLASS = "column-header";
    static final double DEFAULT_COLUMN_WIDTH = 80.0;
    private boolean autoSizeComplete = false;
    private double dragOffset;
    private NestedTableColumnHeader nestedColumnHeader;
    private TableHeaderRow tableHeaderRow;
    private NestedTableColumnHeader parentHeader;
    Label label;
    int sortPos = -1;
    private Region arrow;
    private Label sortOrderLabel;
    private HBox sortOrderDots;
    private Node sortArrow;
    private boolean isSortColumn;
    private boolean isSizeDirty = false;
    boolean isLastVisibleColumn = false;
    int columnIndex = -1;
    private int newColumnPos;
    Region columnReorderLine;
    final LambdaMultiplePropertyChangeListenerHandler changeListenerHandler;
    private ListChangeListener<TableColumnBase<?, ?>> sortOrderListener = change -> this.updateSortPosition();
    private ListChangeListener<TableColumnBase<?, ?>> visibleLeafColumnsListener = change -> {
        this.updateColumnIndex();
        this.updateSortPosition();
    };
    private ListChangeListener<String> styleClassListener = change -> {
        while (change.next()) {
            if (change.wasRemoved()) {
                this.getStyleClass().removeAll(change.getRemoved());
            }
            if (!change.wasAdded()) continue;
            this.getStyleClass().addAll(change.getAddedSubList());
        }
    };
    private WeakListChangeListener<TableColumnBase<?, ?>> weakSortOrderListener = new WeakListChangeListener(this.sortOrderListener);
    private final WeakListChangeListener<TableColumnBase<?, ?>> weakVisibleLeafColumnsListener = new WeakListChangeListener(this.visibleLeafColumnsListener);
    private final WeakListChangeListener<String> weakStyleClassListener = new WeakListChangeListener<String>(this.styleClassListener);
    private static final EventHandler<MouseEvent> mousePressedHandler = mouseEvent -> {
        TableColumnHeader tableColumnHeader = (TableColumnHeader)mouseEvent.getSource();
        TableColumnBase<?, ?> tableColumnBase = tableColumnHeader.getTableColumn();
        ContextMenu contextMenu = tableColumnBase.getContextMenu();
        if (contextMenu != null && contextMenu.isShowing()) {
            contextMenu.hide();
        }
        if (mouseEvent.isConsumed()) {
            return;
        }
        mouseEvent.consume();
        tableColumnHeader.getTableHeaderRow().columnDragLock = true;
        ((Node)((Object)tableColumnHeader.getTableSkin().getSkinnable())).requestFocus();
        if (mouseEvent.isPrimaryButtonDown() && tableColumnHeader.isColumnReorderingEnabled()) {
            tableColumnHeader.columnReorderingStarted(mouseEvent.getX());
        }
    };
    private static final EventHandler<MouseEvent> mouseDraggedHandler = mouseEvent -> {
        if (mouseEvent.isConsumed()) {
            return;
        }
        mouseEvent.consume();
        TableColumnHeader tableColumnHeader = (TableColumnHeader)mouseEvent.getSource();
        if (mouseEvent.isPrimaryButtonDown() && tableColumnHeader.isColumnReorderingEnabled()) {
            tableColumnHeader.columnReordering(mouseEvent.getSceneX(), mouseEvent.getSceneY());
        }
    };
    private static final EventHandler<MouseEvent> mouseReleasedHandler = mouseEvent -> {
        if (mouseEvent.isPopupTrigger()) {
            return;
        }
        if (mouseEvent.isConsumed()) {
            return;
        }
        mouseEvent.consume();
        TableColumnHeader tableColumnHeader = (TableColumnHeader)mouseEvent.getSource();
        tableColumnHeader.getTableHeaderRow().columnDragLock = false;
        if (tableColumnHeader.getTableHeaderRow().isReordering() && tableColumnHeader.isColumnReorderingEnabled()) {
            tableColumnHeader.columnReorderingComplete();
        } else if (mouseEvent.isStillSincePress()) {
            tableColumnHeader.sortColumn(mouseEvent.isShiftDown());
        }
    };
    private static final EventHandler<ContextMenuEvent> contextMenuRequestedHandler = contextMenuEvent -> {
        TableColumnHeader tableColumnHeader = (TableColumnHeader)contextMenuEvent.getSource();
        TableColumnBase<?, ?> tableColumnBase = tableColumnHeader.getTableColumn();
        ContextMenu contextMenu = tableColumnBase.getContextMenu();
        if (contextMenu != null) {
            contextMenu.show(tableColumnHeader, contextMenuEvent.getScreenX(), contextMenuEvent.getScreenY());
            contextMenuEvent.consume();
        }
    };
    private DoubleProperty size;
    private ReadOnlyObjectWrapper<TableColumnBase<?, ?>> tableColumn = new ReadOnlyObjectWrapper(this, "tableColumn");
    private static final PseudoClass PSEUDO_CLASS_LAST_VISIBLE = PseudoClass.getPseudoClass("last-visible");

    public TableColumnHeader(TableColumnBase tableColumnBase) {
        this.setTableColumn(tableColumnBase);
        this.setFocusTraversable(false);
        this.initStyleClasses();
        this.initUI();
        this.changeListenerHandler = new LambdaMultiplePropertyChangeListenerHandler();
        this.changeListenerHandler.registerChangeListener(this.sceneProperty(), observableValue -> this.updateScene());
        if (this.getTableColumn() != null) {
            this.changeListenerHandler.registerChangeListener(tableColumnBase.idProperty(), observableValue -> this.setId(tableColumnBase.getId()));
            this.changeListenerHandler.registerChangeListener(tableColumnBase.styleProperty(), observableValue -> this.setStyle(tableColumnBase.getStyle()));
            this.changeListenerHandler.registerChangeListener(tableColumnBase.widthProperty(), observableValue -> {
                this.isSizeDirty = true;
                this.requestLayout();
            });
            this.changeListenerHandler.registerChangeListener(tableColumnBase.visibleProperty(), observableValue -> this.setVisible(this.getTableColumn().isVisible()));
            this.changeListenerHandler.registerChangeListener(tableColumnBase.sortNodeProperty(), observableValue -> this.updateSortGrid());
            this.changeListenerHandler.registerChangeListener(tableColumnBase.sortableProperty(), observableValue -> {
                if (TableSkinUtils.getSortOrder(this.getTableSkin()).contains(this.getTableColumn())) {
                    NestedTableColumnHeader nestedTableColumnHeader = this.getTableHeaderRow().getRootHeader();
                    this.updateAllHeaders(nestedTableColumnHeader);
                }
            });
            this.changeListenerHandler.registerChangeListener(tableColumnBase.textProperty(), observableValue -> this.label.setText(tableColumnBase.getText()));
            this.changeListenerHandler.registerChangeListener(tableColumnBase.graphicProperty(), observableValue -> this.label.setGraphic(tableColumnBase.getGraphic()));
            this.setId(tableColumnBase.getId());
            this.setStyle(tableColumnBase.getStyle());
            this.setAccessibleRole(AccessibleRole.TABLE_COLUMN);
        }
    }

    private final double getSize() {
        return this.size == null ? 20.0 : this.size.doubleValue();
    }

    private final DoubleProperty sizeProperty() {
        if (this.size == null) {
            this.size = new StyleableDoubleProperty(20.0){

                @Override
                protected void invalidated() {
                    double d = this.get();
                    if (d <= 0.0) {
                        if (this.isBound()) {
                            this.unbind();
                        }
                        this.set(20.0);
                        throw new IllegalArgumentException("Size cannot be 0 or negative");
                    }
                }

                @Override
                public Object getBean() {
                    return TableColumnHeader.this;
                }

                @Override
                public String getName() {
                    return "size";
                }

                @Override
                public CssMetaData<TableColumnHeader, Number> getCssMetaData() {
                    return StyleableProperties.SIZE;
                }
            };
        }
        return this.size;
    }

    private final void setTableColumn(TableColumnBase<?, ?> tableColumnBase) {
        this.tableColumn.set(tableColumnBase);
    }

    public final TableColumnBase<?, ?> getTableColumn() {
        return (TableColumnBase)this.tableColumn.get();
    }

    public final ReadOnlyObjectProperty<TableColumnBase<?, ?>> tableColumnProperty() {
        return this.tableColumn.getReadOnlyProperty();
    }

    @Override
    protected void layoutChildren() {
        if (this.isSizeDirty) {
            this.resize(this.getTableColumn().getWidth(), this.getHeight());
            this.isSizeDirty = false;
        }
        double d = 0.0;
        double d2 = this.snapSizeX(this.getWidth()) - (this.snappedLeftInset() + this.snappedRightInset());
        double d3 = this.getHeight() - (this.snappedTopInset() + this.snappedBottomInset());
        double d4 = d2;
        if (this.arrow != null) {
            this.arrow.setMaxSize(this.arrow.prefWidth(-1.0), this.arrow.prefHeight(-1.0));
        }
        if (this.sortArrow != null && this.sortArrow.isVisible()) {
            d = this.sortArrow.prefWidth(-1.0);
            this.sortArrow.resize(d, this.sortArrow.prefHeight(-1.0));
            this.positionInArea(this.sortArrow, d4 -= d, this.snappedTopInset(), d, d3, 0.0, HPos.CENTER, VPos.CENTER);
        }
        if (this.label != null) {
            double d5 = d2 - d;
            this.label.resizeRelocate(this.snappedLeftInset(), 0.0, d5, this.getHeight());
        }
    }

    @Override
    protected double computePrefWidth(double d) {
        if (this.getNestedColumnHeader() != null) {
            double d2 = this.getNestedColumnHeader().prefWidth(d);
            if (this.getTableColumn() != null) {
                TableColumnBaseHelper.setWidth(this.getTableColumn(), d2);
            }
            return d2;
        }
        if (this.getTableColumn() != null && this.getTableColumn().isVisible()) {
            return this.snapSizeX(this.getTableColumn().getWidth());
        }
        return 0.0;
    }

    @Override
    protected double computeMinHeight(double d) {
        return this.label == null ? 0.0 : this.label.minHeight(d);
    }

    @Override
    protected double computePrefHeight(double d) {
        if (this.getTableColumn() == null) {
            return 0.0;
        }
        return Math.max(this.getSize(), this.label.prefHeight(-1.0));
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return TableColumnHeader.getClassCssMetaData();
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case INDEX: {
                return this.getIndex(this.getTableColumn());
            }
            case TEXT: {
                return this.getTableColumn() != null ? this.getTableColumn().getText() : null;
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    void initStyleClasses() {
        this.getStyleClass().setAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.installTableColumnStyleClassListener();
    }

    void installTableColumnStyleClassListener() {
        TableColumnBase<?, ?> tableColumnBase = this.getTableColumn();
        if (tableColumnBase != null) {
            this.getStyleClass().addAll((Collection<String>)tableColumnBase.getStyleClass());
            tableColumnBase.getStyleClass().addListener(this.weakStyleClassListener);
        }
    }

    NestedTableColumnHeader getNestedColumnHeader() {
        return this.nestedColumnHeader;
    }

    void setNestedColumnHeader(NestedTableColumnHeader nestedTableColumnHeader) {
        this.nestedColumnHeader = nestedTableColumnHeader;
    }

    TableHeaderRow getTableHeaderRow() {
        return this.tableHeaderRow;
    }

    void setTableHeaderRow(TableHeaderRow tableHeaderRow) {
        this.tableHeaderRow = tableHeaderRow;
        this.updateTableSkin();
    }

    private void updateTableSkin() {
        TableViewSkinBase<?, ?, ?, ?, ?> tableViewSkinBase = this.getTableSkin();
        if (tableViewSkinBase == null) {
            return;
        }
        this.updateColumnIndex();
        this.columnReorderLine = tableViewSkinBase.getColumnReorderLine();
        if (this.getTableColumn() != null) {
            this.updateSortPosition();
            TableSkinUtils.getSortOrder(tableViewSkinBase).addListener(this.weakSortOrderListener);
            TableSkinUtils.getVisibleLeafColumns(tableViewSkinBase).addListener(this.weakVisibleLeafColumnsListener);
        }
    }

    TableViewSkinBase<?, ?, ?, ?, ?> getTableSkin() {
        return this.tableHeaderRow == null ? null : this.tableHeaderRow.tableSkin;
    }

    NestedTableColumnHeader getParentHeader() {
        return this.parentHeader;
    }

    void setParentHeader(NestedTableColumnHeader nestedTableColumnHeader) {
        this.parentHeader = nestedTableColumnHeader;
    }

    private void updateAllHeaders(TableColumnHeader tableColumnHeader) {
        if (tableColumnHeader instanceof NestedTableColumnHeader) {
            ObservableList<TableColumnHeader> observableList = ((NestedTableColumnHeader)tableColumnHeader).getColumnHeaders();
            for (int i = 0; i < observableList.size(); ++i) {
                this.updateAllHeaders((TableColumnHeader)observableList.get(i));
            }
        } else {
            tableColumnHeader.updateSortPosition();
        }
    }

    private void updateScene() {
        if (!this.autoSizeComplete) {
            if (this.getTableColumn() == null || this.getTableColumn().getWidth() != 80.0 || this.getScene() == null) {
                return;
            }
            this.doColumnAutoSize(this.getTableColumn(), 30);
            this.autoSizeComplete = true;
        }
    }

    void dispose() {
        TableViewSkinBase<?, ?, ?, ?, ?> tableViewSkinBase = this.getTableSkin();
        if (tableViewSkinBase != null) {
            TableSkinUtils.getVisibleLeafColumns(tableViewSkinBase).removeListener(this.weakVisibleLeafColumnsListener);
            TableSkinUtils.getSortOrder(tableViewSkinBase).removeListener(this.weakSortOrderListener);
        }
        this.changeListenerHandler.dispose();
    }

    private boolean isSortingEnabled() {
        return true;
    }

    private boolean isColumnReorderingEnabled() {
        return !Properties.IS_TOUCH_SUPPORTED && TableSkinUtils.getVisibleLeafColumns(this.getTableSkin()).size() > 1;
    }

    private void initUI() {
        if (this.getTableColumn() == null) {
            return;
        }
        this.setOnMousePressed(mousePressedHandler);
        this.setOnMouseDragged(mouseDraggedHandler);
        this.setOnDragDetected(mouseEvent -> mouseEvent.consume());
        this.setOnContextMenuRequested(contextMenuRequestedHandler);
        this.setOnMouseReleased(mouseReleasedHandler);
        this.label = new Label();
        this.label.setText(this.getTableColumn().getText());
        this.label.setGraphic(this.getTableColumn().getGraphic());
        this.label.setVisible(this.getTableColumn().isVisible());
        if (this.isSortingEnabled()) {
            this.updateSortGrid();
        }
    }

    private void doColumnAutoSize(TableColumnBase<?, ?> tableColumnBase, int n) {
        double d = tableColumnBase.getPrefWidth();
        if (d == 80.0) {
            TableSkinUtils.resizeColumnToFitContent(this.getTableSkin(), tableColumnBase, n);
        }
    }

    private void updateSortPosition() {
        this.sortPos = !this.getTableColumn().isSortable() ? -1 : this.getSortPosition();
        this.updateSortGrid();
    }

    private void updateSortGrid() {
        if (this instanceof NestedTableColumnHeader) {
            return;
        }
        this.getChildren().clear();
        this.getChildren().add(this.label);
        if (!this.isSortingEnabled()) {
            return;
        }
        boolean bl = this.isSortColumn = this.sortPos != -1;
        if (!this.isSortColumn) {
            if (this.sortArrow != null) {
                this.sortArrow.setVisible(false);
            }
            return;
        }
        int n = TableSkinUtils.getVisibleLeafIndex(this.getTableSkin(), this.getTableColumn());
        if (n == -1) {
            return;
        }
        int n2 = this.getVisibleSortOrderColumnCount();
        boolean bl2 = this.sortPos <= 3 && n2 > 1;
        Node node = null;
        if (this.getTableColumn().getSortNode() != null) {
            node = this.getTableColumn().getSortNode();
            this.getChildren().add(node);
        } else {
            GridPane gridPane = new GridPane();
            node = gridPane;
            gridPane.setPadding(new Insets(0.0, 3.0, 0.0, 0.0));
            this.getChildren().add(gridPane);
            if (this.arrow == null) {
                this.arrow = new Region();
                this.arrow.getStyleClass().setAll((String[])new String[]{"arrow"});
                this.arrow.setVisible(true);
                this.arrow.setRotate(TableColumnSortTypeWrapper.isAscending(this.getTableColumn()) ? 180.0 : 0.0);
                this.changeListenerHandler.registerChangeListener(TableColumnSortTypeWrapper.getSortTypeProperty(this.getTableColumn()), observableValue -> {
                    this.updateSortGrid();
                    if (this.arrow != null) {
                        this.arrow.setRotate(TableColumnSortTypeWrapper.isAscending(this.getTableColumn()) ? 180.0 : 0.0);
                    }
                });
            }
            this.arrow.setVisible(this.isSortColumn);
            if (this.sortPos > 2) {
                if (this.sortOrderLabel == null) {
                    this.sortOrderLabel = new Label();
                    this.sortOrderLabel.getStyleClass().add("sort-order");
                }
                this.sortOrderLabel.setText("" + (this.sortPos + 1));
                this.sortOrderLabel.setVisible(n2 > 1);
                gridPane.add(this.arrow, 1, 1);
                GridPane.setHgrow(this.arrow, Priority.NEVER);
                GridPane.setVgrow(this.arrow, Priority.NEVER);
                gridPane.add(this.sortOrderLabel, 2, 1);
            } else if (bl2) {
                boolean bl3;
                if (this.sortOrderDots == null) {
                    this.sortOrderDots = new HBox(0.0);
                    this.sortOrderDots.getStyleClass().add("sort-order-dots-container");
                }
                int n3 = (bl3 = TableColumnSortTypeWrapper.isAscending(this.getTableColumn())) ? 1 : 2;
                int n4 = bl3 ? 2 : 1;
                gridPane.add(this.arrow, 1, n3);
                GridPane.setHalignment(this.arrow, HPos.CENTER);
                gridPane.add(this.sortOrderDots, 1, n4);
                this.updateSortOrderDots(this.sortPos);
            } else {
                gridPane.add(this.arrow, 1, 1);
                GridPane.setHgrow(this.arrow, Priority.NEVER);
                GridPane.setVgrow(this.arrow, Priority.ALWAYS);
            }
        }
        this.sortArrow = node;
        if (this.sortArrow != null) {
            this.sortArrow.setVisible(this.isSortColumn);
        }
        this.requestLayout();
    }

    private void updateSortOrderDots(int n) {
        double d = this.arrow.prefWidth(-1.0);
        this.sortOrderDots.getChildren().clear();
        for (int i = 0; i <= n; ++i) {
            Region region = new Region();
            region.getStyleClass().add("sort-order-dot");
            String string = TableColumnSortTypeWrapper.getSortTypeName(this.getTableColumn());
            if (string != null && !string.isEmpty()) {
                region.getStyleClass().add(string.toLowerCase(Locale.ROOT));
            }
            this.sortOrderDots.getChildren().add(region);
            if (i >= n) continue;
            Region region2 = new Region();
            double d2 = n == 1 ? 1.0 : 0.0;
            region2.setPadding(new Insets(0.0, 1.0, 0.0, d2));
            this.sortOrderDots.getChildren().add(region2);
        }
        this.sortOrderDots.setAlignment(Pos.TOP_CENTER);
        this.sortOrderDots.setMaxWidth(d);
    }

    void moveColumn(TableColumnBase tableColumnBase, int n) {
        int n2;
        if (tableColumnBase == null || n < 0) {
            return;
        }
        ObservableList<TableColumnBase<?, ?>> observableList = this.getColumns(tableColumnBase);
        int n3 = observableList.size();
        int n4 = observableList.indexOf(tableColumnBase);
        int n5 = n2 = n;
        int n6 = 0;
        for (int i = 0; i < n3 && n6 != n5 + 1; ++i) {
            if (((TableColumnBase)observableList.get(i)).isVisible()) {
                ++n6;
                continue;
            }
            ++n2;
        }
        if (n2 >= n3) {
            n2 = n3 - 1;
        } else if (n2 < 0) {
            n2 = 0;
        }
        if (n2 == n4) {
            return;
        }
        ArrayList arrayList = new ArrayList(observableList);
        arrayList.remove(tableColumnBase);
        arrayList.add(n2, tableColumnBase);
        observableList.setAll(arrayList);
    }

    private ObservableList<TableColumnBase<?, ?>> getColumns(TableColumnBase tableColumnBase) {
        return tableColumnBase.getParentColumn() == null ? TableSkinUtils.getColumns(this.getTableSkin()) : tableColumnBase.getParentColumn().getColumns();
    }

    private int getIndex(TableColumnBase<?, ?> tableColumnBase) {
        if (tableColumnBase == null) {
            return -1;
        }
        ObservableList<TableColumnBase<?, ?>> observableList = this.getColumns(tableColumnBase);
        int n = -1;
        for (int i = 0; i < observableList.size(); ++i) {
            TableColumnBase tableColumnBase2 = (TableColumnBase)observableList.get(i);
            if (!tableColumnBase2.isVisible()) continue;
            ++n;
            if (tableColumnBase.equals(tableColumnBase2)) break;
        }
        return n;
    }

    private void updateColumnIndex() {
        TableColumnBase<?, ?> tableColumnBase = this.getTableColumn();
        TableViewSkinBase<?, ?, ?, ?, ?> tableViewSkinBase = this.getTableSkin();
        this.columnIndex = tableViewSkinBase == null || tableColumnBase == null ? -1 : TableSkinUtils.getVisibleLeafIndex(tableViewSkinBase, tableColumnBase);
        this.isLastVisibleColumn = this.getTableColumn() != null && this.columnIndex != -1 && this.columnIndex == TableSkinUtils.getVisibleLeafColumns(tableViewSkinBase).size() - 1;
        this.pseudoClassStateChanged(PSEUDO_CLASS_LAST_VISIBLE, this.isLastVisibleColumn);
    }

    private void sortColumn(boolean bl) {
        if (!this.isSortingEnabled()) {
            return;
        }
        if (this.getTableColumn() == null || this.getTableColumn().getColumns().size() != 0 || this.getTableColumn().getComparator() == null || !this.getTableColumn().isSortable()) {
            return;
        }
        ObservableList<TableColumnBase<?, ?>> observableList = TableSkinUtils.getSortOrder(this.getTableSkin());
        if (bl) {
            if (!this.isSortColumn) {
                TableColumnSortTypeWrapper.setSortType(this.getTableColumn(), TableColumn.SortType.ASCENDING);
                observableList.add(this.getTableColumn());
            } else if (TableColumnSortTypeWrapper.isAscending(this.getTableColumn())) {
                TableColumnSortTypeWrapper.setSortType(this.getTableColumn(), TableColumn.SortType.DESCENDING);
            } else {
                int n = observableList.indexOf(this.getTableColumn());
                if (n != -1) {
                    observableList.remove(n);
                }
            }
        } else if (this.isSortColumn && observableList.size() == 1) {
            if (TableColumnSortTypeWrapper.isAscending(this.getTableColumn())) {
                TableColumnSortTypeWrapper.setSortType(this.getTableColumn(), TableColumn.SortType.DESCENDING);
            } else {
                observableList.remove(this.getTableColumn());
            }
        } else if (this.isSortColumn) {
            if (TableColumnSortTypeWrapper.isAscending(this.getTableColumn())) {
                TableColumnSortTypeWrapper.setSortType(this.getTableColumn(), TableColumn.SortType.DESCENDING);
            } else if (TableColumnSortTypeWrapper.isDescending(this.getTableColumn())) {
                TableColumnSortTypeWrapper.setSortType(this.getTableColumn(), TableColumn.SortType.ASCENDING);
            }
            ArrayList arrayList = new ArrayList(observableList);
            arrayList.remove(this.getTableColumn());
            arrayList.add(0, this.getTableColumn());
            observableList.setAll(this.getTableColumn());
        } else {
            TableColumnSortTypeWrapper.setSortType(this.getTableColumn(), TableColumn.SortType.ASCENDING);
            observableList.setAll(this.getTableColumn());
        }
    }

    private int getSortPosition() {
        if (this.getTableColumn() == null) {
            return -1;
        }
        List<TableColumnBase> list = this.getVisibleSortOrderColumns();
        int n = 0;
        for (int i = 0; i < list.size(); ++i) {
            TableColumnBase tableColumnBase = list.get(i);
            if (this.getTableColumn().equals(tableColumnBase)) {
                return n;
            }
            ++n;
        }
        return -1;
    }

    private List<TableColumnBase> getVisibleSortOrderColumns() {
        ObservableList<TableColumnBase<?, ?>> observableList = TableSkinUtils.getSortOrder(this.getTableSkin());
        ArrayList<TableColumnBase> arrayList = new ArrayList<TableColumnBase>();
        for (int i = 0; i < observableList.size(); ++i) {
            TableColumnBase tableColumnBase = (TableColumnBase)observableList.get(i);
            if (tableColumnBase == null || !tableColumnBase.isSortable() || !tableColumnBase.isVisible()) continue;
            arrayList.add(tableColumnBase);
        }
        return arrayList;
    }

    private int getVisibleSortOrderColumnCount() {
        return this.getVisibleSortOrderColumns().size();
    }

    void columnReorderingStarted(double d) {
        if (!this.getTableColumn().isReorderable()) {
            return;
        }
        this.dragOffset = d;
        this.getTableHeaderRow().setReorderingColumn(this.getTableColumn());
        this.getTableHeaderRow().setReorderingRegion(this);
    }

    void columnReordering(double d, double d2) {
        if (!this.getTableColumn().isReorderable()) {
            return;
        }
        this.getTableHeaderRow().setReordering(true);
        TableColumnHeader tableColumnHeader = null;
        double d3 = this.getParentHeader().sceneToLocal(d, d2).getX();
        double d4 = ((Node)((Object)this.getTableSkin().getSkinnable())).sceneToLocal(d, d2).getX() - this.dragOffset;
        this.getTableHeaderRow().setDragHeaderX(d4);
        double d5 = 0.0;
        double d6 = 0.0;
        double d7 = 0.0;
        this.newColumnPos = 0;
        for (TableColumnHeader tableColumnHeader2 : this.getParentHeader().getColumnHeaders()) {
            if (!tableColumnHeader2.isVisible()) continue;
            double d8 = tableColumnHeader2.prefWidth(-1.0);
            d7 += d8;
            d5 = tableColumnHeader2.getBoundsInParent().getMinX();
            d6 = d5 + d8;
            if (d3 >= d5 && d3 < d6) {
                tableColumnHeader = tableColumnHeader2;
                break;
            }
            ++this.newColumnPos;
        }
        if (tableColumnHeader == null) {
            this.newColumnPos = d3 > d7 ? this.getParentHeader().getColumns().size() - 1 : 0;
            return;
        }
        double d9 = d5 + (d6 - d5) / 2.0;
        boolean bl = d3 <= d9;
        int n = this.getIndex(this.getTableColumn());
        this.newColumnPos += this.newColumnPos > n && bl ? -1 : (this.newColumnPos < n && !bl ? 1 : 0);
        double d10 = this.getTableHeaderRow().sceneToLocal(tableColumnHeader.localToScene(tableColumnHeader.getBoundsInLocal())).getMinX();
        if ((d10 += bl ? 0.0 : tableColumnHeader.getWidth()) >= -0.5 && d10 <= ((Region)((Object)this.getTableSkin().getSkinnable())).getWidth()) {
            this.columnReorderLine.setTranslateX(d10);
            this.columnReorderLine.setVisible(true);
        }
        this.getTableHeaderRow().setReordering(true);
    }

    void columnReorderingComplete() {
        if (!this.getTableColumn().isReorderable()) {
            return;
        }
        this.moveColumn(this.getTableColumn(), this.newColumnPos);
        this.columnReorderLine.setTranslateX(0.0);
        this.columnReorderLine.setLayoutX(0.0);
        this.newColumnPos = 0;
        this.getTableHeaderRow().setReordering(false);
        this.columnReorderLine.setVisible(false);
        this.getTableHeaderRow().setReorderingColumn(null);
        this.getTableHeaderRow().setReorderingRegion(null);
        this.dragOffset = 0.0;
    }

    double getDragRectHeight() {
        return this.getHeight();
    }

    boolean represents(TableColumnBase<?, ?> tableColumnBase) {
        if (!tableColumnBase.getColumns().isEmpty()) {
            return false;
        }
        return tableColumnBase == this.getTableColumn();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    private static class StyleableProperties {
        private static final CssMetaData<TableColumnHeader, Number> SIZE = new CssMetaData<TableColumnHeader, Number>("-fx-size", SizeConverter.getInstance(), (Number)20.0){

            @Override
            public boolean isSettable(TableColumnHeader tableColumnHeader) {
                return tableColumnHeader.size == null || !tableColumnHeader.size.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(TableColumnHeader tableColumnHeader) {
                return (StyleableProperty)((Object)tableColumnHeader.sizeProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Region.getClassCssMetaData());
            arrayList.add(SIZE);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

