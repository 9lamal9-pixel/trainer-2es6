/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.skin.resources.ControlResources;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Skinnable;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableSkinUtils;
import javafx.scene.control.skin.TableViewSkinBase;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class TableHeaderRow
extends StackPane {
    private final String MENU_SEPARATOR = ControlResources.getString("TableView.nestedColumnControlMenuSeparator");
    private final VirtualFlow flow;
    final TableViewSkinBase<?, ?, ?, ?, ?> tableSkin;
    private Map<TableColumnBase, CheckMenuItem> columnMenuItems = new HashMap<TableColumnBase, CheckMenuItem>();
    private double scrollX;
    private double tableWidth;
    private Rectangle clip;
    private TableColumnHeader reorderingRegion;
    private StackPane dragHeader;
    private final Label dragHeaderLabel = new Label();
    private Region filler;
    private Pane cornerRegion;
    private ContextMenu columnPopupMenu;
    boolean columnDragLock = false;
    private InvalidationListener tableWidthListener = observable -> this.updateTableWidth();
    private InvalidationListener tablePaddingListener = observable -> this.updateTableWidth();
    private ListChangeListener visibleLeafColumnsListener = change -> this.getRootHeader().setHeadersNeedUpdate();
    private final ListChangeListener tableColumnsListener = change -> {
        while (change.next()) {
            this.updateTableColumnListeners(change.getAddedSubList(), change.getRemoved());
        }
    };
    private final InvalidationListener columnTextListener = observable -> {
        TableColumnBase tableColumnBase = (TableColumnBase)((StringProperty)observable).getBean();
        CheckMenuItem checkMenuItem = this.columnMenuItems.get(tableColumnBase);
        if (checkMenuItem != null) {
            checkMenuItem.setText(this.getText(tableColumnBase.getText(), tableColumnBase));
        }
    };
    private final WeakInvalidationListener weakTableWidthListener = new WeakInvalidationListener(this.tableWidthListener);
    private final WeakInvalidationListener weakTablePaddingListener = new WeakInvalidationListener(this.tablePaddingListener);
    private final WeakListChangeListener weakVisibleLeafColumnsListener = new WeakListChangeListener(this.visibleLeafColumnsListener);
    private final WeakListChangeListener weakTableColumnsListener = new WeakListChangeListener(this.tableColumnsListener);
    private final WeakInvalidationListener weakColumnTextListener = new WeakInvalidationListener(this.columnTextListener);
    private BooleanProperty reordering = new SimpleBooleanProperty(this, "reordering", false){

        @Override
        protected void invalidated() {
            TableColumnHeader tableColumnHeader = TableHeaderRow.this.getReorderingRegion();
            if (tableColumnHeader != null) {
                double d = tableColumnHeader.getNestedColumnHeader() != null ? tableColumnHeader.getNestedColumnHeader().getHeight() : TableHeaderRow.this.getReorderingRegion().getHeight();
                TableHeaderRow.this.dragHeader.resize(TableHeaderRow.this.dragHeader.getWidth(), d);
                TableHeaderRow.this.dragHeader.setTranslateY(TableHeaderRow.this.getHeight() - d);
            }
            TableHeaderRow.this.dragHeader.setVisible(TableHeaderRow.this.isReordering());
        }
    };
    private ReadOnlyObjectWrapper<NestedTableColumnHeader> rootHeader = new ReadOnlyObjectWrapper(this, "rootHeader");

    public TableHeaderRow(TableViewSkinBase tableViewSkinBase) {
        this.tableSkin = tableViewSkinBase;
        this.flow = tableViewSkinBase.flow;
        this.getStyleClass().setAll((String[])new String[]{"column-header-background"});
        this.clip = new Rectangle();
        this.clip.setSmooth(false);
        this.clip.heightProperty().bind(this.heightProperty());
        this.setClip(this.clip);
        this.updateTableWidth();
        ((Region)((Object)this.tableSkin.getSkinnable())).widthProperty().addListener(this.weakTableWidthListener);
        ((Region)((Object)this.tableSkin.getSkinnable())).paddingProperty().addListener(this.weakTablePaddingListener);
        TableSkinUtils.getVisibleLeafColumns(tableViewSkinBase).addListener(this.weakVisibleLeafColumnsListener);
        this.columnPopupMenu = new ContextMenu();
        this.updateTableColumnListeners(TableSkinUtils.getColumns(this.tableSkin), Collections.emptyList());
        TableSkinUtils.getVisibleLeafColumns(tableViewSkinBase).addListener(this.weakTableColumnsListener);
        TableSkinUtils.getColumns(this.tableSkin).addListener(this.weakTableColumnsListener);
        this.dragHeader = new StackPane();
        this.dragHeader.setVisible(false);
        this.dragHeader.getStyleClass().setAll((String[])new String[]{"column-drag-header"});
        this.dragHeader.setManaged(false);
        this.dragHeader.setMouseTransparent(true);
        this.dragHeader.getChildren().add(this.dragHeaderLabel);
        NestedTableColumnHeader nestedTableColumnHeader = this.createRootHeader();
        this.setRootHeader(nestedTableColumnHeader);
        nestedTableColumnHeader.setFocusTraversable(false);
        nestedTableColumnHeader.setTableHeaderRow(this);
        this.filler = new Region();
        this.filler.getStyleClass().setAll((String[])new String[]{"filler"});
        this.setOnMousePressed(mouseEvent -> ((Node)((Object)tableViewSkinBase.getSkinnable())).requestFocus());
        final StackPane stackPane = new StackPane();
        stackPane.setSnapToPixel(false);
        stackPane.getStyleClass().setAll((String[])new String[]{"show-hide-column-image"});
        this.cornerRegion = new StackPane(){

            @Override
            protected void layoutChildren() {
                double d = stackPane.snappedLeftInset() + stackPane.snappedRightInset();
                double d2 = stackPane.snappedTopInset() + stackPane.snappedBottomInset();
                stackPane.resize(d, d2);
                this.positionInArea(stackPane, 0.0, 0.0, this.getWidth(), this.getHeight() - 3.0, 0.0, HPos.CENTER, VPos.CENTER);
            }
        };
        this.cornerRegion.getStyleClass().setAll((String[])new String[]{"show-hide-columns-button"});
        this.cornerRegion.getChildren().addAll((Node[])new Node[]{stackPane});
        BooleanProperty booleanProperty = TableSkinUtils.tableMenuButtonVisibleProperty(tableViewSkinBase);
        if (booleanProperty != null) {
            this.cornerRegion.visibleProperty().bind(booleanProperty);
        }
        this.cornerRegion.setOnMousePressed(mouseEvent -> {
            this.columnPopupMenu.show(this.cornerRegion, Side.BOTTOM, 0.0, 0.0);
            mouseEvent.consume();
        });
        this.getChildren().addAll((Node[])new Node[]{this.filler, nestedTableColumnHeader, this.cornerRegion, this.dragHeader});
    }

    final void setReordering(boolean bl) {
        this.reordering.set(bl);
    }

    final boolean isReordering() {
        return this.reordering.get();
    }

    final BooleanProperty reorderingProperty() {
        return this.reordering;
    }

    private final ReadOnlyObjectProperty<NestedTableColumnHeader> rootHeaderProperty() {
        return this.rootHeader.getReadOnlyProperty();
    }

    final NestedTableColumnHeader getRootHeader() {
        return (NestedTableColumnHeader)this.rootHeader.get();
    }

    private final void setRootHeader(NestedTableColumnHeader nestedTableColumnHeader) {
        this.rootHeader.set(nestedTableColumnHeader);
    }

    @Override
    protected void layoutChildren() {
        double d = this.scrollX;
        double d2 = this.snapSizeX(this.getRootHeader().prefWidth(-1.0));
        double d3 = this.getHeight() - this.snappedTopInset() - this.snappedBottomInset();
        double d4 = this.snapSizeX(this.flow.getVbar().prefWidth(-1.0));
        this.getRootHeader().resizeRelocate(d, this.snappedTopInset(), d2, d3);
        Skinnable skinnable = this.tableSkin.getSkinnable();
        if (skinnable == null) {
            return;
        }
        BooleanProperty booleanProperty = TableSkinUtils.tableMenuButtonVisibleProperty(this.tableSkin);
        double d5 = ((Region)((Object)skinnable)).snappedLeftInset() + ((Region)((Object)skinnable)).snappedRightInset();
        double d6 = this.tableWidth - d2 + this.filler.getInsets().getLeft() - d5;
        this.filler.setVisible((d6 -= booleanProperty != null && booleanProperty.get() ? d4 : 0.0) > 0.0);
        if (d6 > 0.0) {
            this.filler.resizeRelocate(d + d2, this.snappedTopInset(), d6, d3);
        }
        this.cornerRegion.resizeRelocate(this.tableWidth - d4, this.snappedTopInset(), d4, d3);
    }

    @Override
    protected double computePrefWidth(double d) {
        return this.getRootHeader().prefWidth(d);
    }

    @Override
    protected double computeMinHeight(double d) {
        return this.computePrefHeight(d);
    }

    @Override
    protected double computePrefHeight(double d) {
        double d2 = this.getRootHeader().prefHeight(d);
        d2 = d2 == 0.0 ? 24.0 : d2;
        return this.snappedTopInset() + d2 + this.snappedBottomInset();
    }

    void updateScrollX() {
        this.scrollX = this.flow.getHbar().isVisible() ? -this.flow.getHbar().getValue() : 0.0;
        this.requestLayout();
        this.layout();
    }

    private void updateTableWidth() {
        Skinnable skinnable = this.tableSkin.getSkinnable();
        if (skinnable == null) {
            this.tableWidth = 0.0;
        } else {
            Insets insets = ((Region)((Object)skinnable)).getInsets() == null ? Insets.EMPTY : ((Region)((Object)skinnable)).getInsets();
            double d = this.snapSizeX(insets.getLeft()) + this.snapSizeX(insets.getRight());
            this.tableWidth = this.snapSizeX(((Region)((Object)skinnable)).getWidth()) - d;
        }
        this.clip.setWidth(this.tableWidth);
    }

    protected NestedTableColumnHeader createRootHeader() {
        return new NestedTableColumnHeader(null);
    }

    TableColumnHeader getReorderingRegion() {
        return this.reorderingRegion;
    }

    void setReorderingColumn(TableColumnBase tableColumnBase) {
        this.dragHeaderLabel.setText(tableColumnBase == null ? "" : tableColumnBase.getText());
    }

    void setReorderingRegion(TableColumnHeader tableColumnHeader) {
        this.reorderingRegion = tableColumnHeader;
        if (tableColumnHeader != null) {
            this.dragHeader.resize(tableColumnHeader.getWidth(), this.dragHeader.getHeight());
        }
    }

    void setDragHeaderX(double d) {
        this.dragHeader.setTranslateX(d);
    }

    TableColumnHeader getColumnHeaderFor(TableColumnBase<?, ?> tableColumnBase) {
        if (tableColumnBase == null) {
            return null;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add(tableColumnBase);
        for (TableColumnBase<?, ?> tableColumnBase2 = tableColumnBase.getParentColumn(); tableColumnBase2 != null; tableColumnBase2 = tableColumnBase2.getParentColumn()) {
            arrayList.add(0, tableColumnBase2);
        }
        TableColumnHeader tableColumnHeader = this.getRootHeader();
        for (int i = 0; i < arrayList.size(); ++i) {
            TableColumnBase tableColumnBase3 = (TableColumnBase)arrayList.get(i);
            tableColumnHeader = this.getColumnHeaderFor(tableColumnBase3, tableColumnHeader);
        }
        return tableColumnHeader;
    }

    private TableColumnHeader getColumnHeaderFor(TableColumnBase<?, ?> tableColumnBase, TableColumnHeader tableColumnHeader) {
        if (tableColumnHeader instanceof NestedTableColumnHeader) {
            ObservableList<TableColumnHeader> observableList = ((NestedTableColumnHeader)tableColumnHeader).getColumnHeaders();
            for (int i = 0; i < observableList.size(); ++i) {
                TableColumnHeader tableColumnHeader2 = (TableColumnHeader)observableList.get(i);
                if (tableColumnHeader2.getTableColumn() != tableColumnBase) continue;
                return tableColumnHeader2;
            }
        }
        return null;
    }

    private void updateTableColumnListeners(List<? extends TableColumnBase<?, ?>> list, List<? extends TableColumnBase<?, ?>> list2) {
        for (TableColumnBase<?, ?> tableColumnBase : list2) {
            this.remove(tableColumnBase);
        }
        this.rebuildColumnMenu();
    }

    private void remove(TableColumnBase<?, ?> tableColumnBase) {
        if (tableColumnBase == null) {
            return;
        }
        CheckMenuItem checkMenuItem = this.columnMenuItems.remove(tableColumnBase);
        if (checkMenuItem != null) {
            tableColumnBase.textProperty().removeListener(this.weakColumnTextListener);
            checkMenuItem.selectedProperty().unbindBidirectional(tableColumnBase.visibleProperty());
            this.columnPopupMenu.getItems().remove(checkMenuItem);
        }
        if (!tableColumnBase.getColumns().isEmpty()) {
            for (TableColumnBase tableColumnBase2 : tableColumnBase.getColumns()) {
                this.remove(tableColumnBase2);
            }
        }
    }

    private void rebuildColumnMenu() {
        this.columnPopupMenu.getItems().clear();
        for (TableColumnBase tableColumnBase : TableSkinUtils.getColumns(this.tableSkin)) {
            if (tableColumnBase.getColumns().isEmpty()) {
                this.createMenuItem(tableColumnBase);
                continue;
            }
            List<TableColumnBase<?, ?>> list = this.getLeafColumns(tableColumnBase);
            for (TableColumnBase<?, ?> tableColumnBase2 : list) {
                this.createMenuItem(tableColumnBase2);
            }
        }
    }

    private List<TableColumnBase<?, ?>> getLeafColumns(TableColumnBase<?, ?> tableColumnBase) {
        ArrayList arrayList = new ArrayList();
        for (TableColumnBase tableColumnBase2 : tableColumnBase.getColumns()) {
            if (tableColumnBase2.getColumns().isEmpty()) {
                arrayList.add(tableColumnBase2);
                continue;
            }
            arrayList.addAll(this.getLeafColumns(tableColumnBase2));
        }
        return arrayList;
    }

    private void createMenuItem(TableColumnBase<?, ?> tableColumnBase) {
        CheckMenuItem checkMenuItem = this.columnMenuItems.get(tableColumnBase);
        if (checkMenuItem == null) {
            checkMenuItem = new CheckMenuItem();
            this.columnMenuItems.put(tableColumnBase, checkMenuItem);
        }
        checkMenuItem.setText(this.getText(tableColumnBase.getText(), tableColumnBase));
        tableColumnBase.textProperty().addListener(this.weakColumnTextListener);
        checkMenuItem.setDisable(tableColumnBase.visibleProperty().isBound());
        checkMenuItem.setSelected(tableColumnBase.isVisible());
        CheckMenuItem checkMenuItem2 = checkMenuItem;
        checkMenuItem.selectedProperty().addListener(observable -> {
            if (tableColumnBase.visibleProperty().isBound()) {
                return;
            }
            tableColumnBase.setVisible(checkMenuItem2.isSelected());
        });
        tableColumnBase.visibleProperty().addListener(observable -> checkMenuItem2.setSelected(tableColumnBase.isVisible()));
        this.columnPopupMenu.getItems().add(checkMenuItem);
    }

    private String getText(String string, TableColumnBase tableColumnBase) {
        Object object = string;
        for (TableColumnBase tableColumnBase2 = tableColumnBase.getParentColumn(); tableColumnBase2 != null; tableColumnBase2 = tableColumnBase2.getParentColumn()) {
            if (!this.isColumnVisibleInHeader(tableColumnBase2, TableSkinUtils.getColumns(this.tableSkin))) continue;
            object = tableColumnBase2.getText() + this.MENU_SEPARATOR + (String)object;
        }
        return object;
    }

    private boolean isColumnVisibleInHeader(TableColumnBase tableColumnBase, List list) {
        if (tableColumnBase == null) {
            return false;
        }
        for (int i = 0; i < list.size(); ++i) {
            boolean bl;
            TableColumnBase tableColumnBase2 = (TableColumnBase)list.get(i);
            if (tableColumnBase.equals(tableColumnBase2)) {
                return true;
            }
            if (tableColumnBase2.getColumns().isEmpty() || !(bl = this.isColumnVisibleInHeader(tableColumnBase, tableColumnBase2.getColumns()))) continue;
            return true;
        }
        return false;
    }
}

