/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.TreeTableRowBehavior;
import java.lang.ref.Reference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.skin.CellSkinBase;
import javafx.scene.control.skin.TableRowSkinBase;
import javafx.scene.control.skin.TreeTableViewSkin;

public class TreeTableRowSkin<T>
extends TableRowSkinBase<TreeItem<T>, TreeTableRow<T>, TreeTableCell<T, ?>> {
    private TreeItem<?> treeItem;
    private boolean disclosureNodeDirty = true;
    private Node graphic;
    private final BehaviorBase<TreeTableRow<T>> behavior;
    private TreeTableViewSkin treeTableViewSkin;
    private boolean childrenDirty = false;
    private final InvalidationListener graphicListener = observable -> {
        this.disclosureNodeDirty = true;
        ((TreeTableRow)this.getSkinnable()).requestLayout();
    };
    private DoubleProperty indent = null;

    public TreeTableRowSkin(TreeTableRow<T> treeTableRow) {
        super(treeTableRow);
        this.behavior = new TreeTableRowBehavior<T>(treeTableRow);
        this.updateTreeItem();
        this.updateTableViewSkin();
        this.registerChangeListener(treeTableRow.treeTableViewProperty(), observableValue -> this.updateTableViewSkin());
        this.registerChangeListener(treeTableRow.indexProperty(), observableValue -> {
            this.updateCells = true;
        });
        this.registerChangeListener(treeTableRow.treeItemProperty(), observableValue -> this.updateTreeItem());
        this.setupTreeTableViewListeners();
    }

    private void setupTreeTableViewListeners() {
        TreeTableView treeTableView = ((TreeTableRow)this.getSkinnable()).getTreeTableView();
        if (treeTableView == null) {
            ((TreeTableRow)this.getSkinnable()).treeTableViewProperty().addListener(new InvalidationListener(){

                @Override
                public void invalidated(Observable observable) {
                    ((TreeTableRow)TreeTableRowSkin.this.getSkinnable()).treeTableViewProperty().removeListener(this);
                    TreeTableRowSkin.this.setupTreeTableViewListeners();
                }
            });
        } else {
            this.registerChangeListener(treeTableView.treeColumnProperty(), observableValue -> {
                this.isDirty = true;
                ((TreeTableRow)this.getSkinnable()).requestLayout();
            });
            DoubleProperty doubleProperty = this.getTreeTableView().fixedCellSizeProperty();
            if (doubleProperty != null) {
                this.registerChangeListener(doubleProperty, observableValue -> {
                    this.fixedCellSize = doubleProperty.get();
                    this.fixedCellSizeEnabled = this.fixedCellSize > 0.0;
                });
                this.fixedCellSize = doubleProperty.get();
                this.fixedCellSizeEnabled = this.fixedCellSize > 0.0;
                this.registerChangeListener(this.getVirtualFlow().widthProperty(), observableValue -> treeTableView.requestLayout());
            }
        }
    }

    public final void setIndent(double d) {
        this.indentProperty().set(d);
    }

    public final double getIndent() {
        return this.indent == null ? 10.0 : this.indent.get();
    }

    public final DoubleProperty indentProperty() {
        if (this.indent == null) {
            this.indent = new StyleableDoubleProperty(10.0){

                @Override
                public Object getBean() {
                    return TreeTableRowSkin.this;
                }

                @Override
                public String getName() {
                    return "indent";
                }

                @Override
                public CssMetaData<TreeTableRow<?>, Number> getCssMetaData() {
                    return StyleableProperties.INDENT;
                }
            };
        }
        return this.indent;
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }

    @Override
    protected void updateChildren() {
        super.updateChildren();
        this.updateDisclosureNodeAndGraphic();
        if (this.childrenDirty) {
            this.childrenDirty = false;
            if (this.cells.isEmpty()) {
                this.getChildren().clear();
            } else {
                this.getChildren().addAll(this.cells);
            }
        }
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        Node node;
        if (this.disclosureNodeDirty) {
            this.updateDisclosureNodeAndGraphic();
            this.disclosureNodeDirty = false;
        }
        if ((node = this.getDisclosureNode()) != null && node.getScene() == null) {
            this.updateDisclosureNodeAndGraphic();
        }
        super.layoutChildren(d, d2, d3, d4);
    }

    @Override
    protected TreeTableCell<T, ?> createCell(TableColumnBase tableColumnBase) {
        TreeTableColumn treeTableColumn = (TreeTableColumn)tableColumnBase;
        TreeTableCell treeTableCell = treeTableColumn.getCellFactory().call(treeTableColumn);
        treeTableCell.updateTreeTableColumn(treeTableColumn);
        treeTableCell.updateTreeTableView(treeTableColumn.getTreeTableView());
        return treeTableCell;
    }

    @Override
    void updateCells(boolean bl) {
        super.updateCells(bl);
        if (bl) {
            this.childrenDirty = true;
            this.updateChildren();
        }
    }

    @Override
    boolean isIndentationRequired() {
        return true;
    }

    @Override
    TableColumnBase getTreeColumn() {
        return this.getTreeTableView().getTreeColumn();
    }

    @Override
    int getIndentationLevel(TreeTableRow<T> treeTableRow) {
        return this.getTreeTableView().getTreeItemLevel(treeTableRow.getTreeItem());
    }

    @Override
    double getIndentationPerLevel() {
        return this.getIndent();
    }

    @Override
    Node getDisclosureNode() {
        return ((TreeTableRow)this.getSkinnable()).getDisclosureNode();
    }

    @Override
    boolean isDisclosureNodeVisible() {
        return this.getDisclosureNode() != null && this.treeItem != null && !this.treeItem.isLeaf();
    }

    @Override
    boolean isShowRoot() {
        return this.getTreeTableView().isShowRoot();
    }

    @Override
    protected ObservableList<TreeTableColumn<T, ?>> getVisibleLeafColumns() {
        return this.getTreeTableView() == null ? FXCollections.emptyObservableList() : this.getTreeTableView().getVisibleLeafColumns();
    }

    @Override
    protected void updateCell(TreeTableCell<T, ?> treeTableCell, TreeTableRow<T> treeTableRow) {
        treeTableCell.updateTreeTableRow(treeTableRow);
    }

    @Override
    protected TreeTableColumn<T, ?> getTableColumn(TreeTableCell treeTableCell) {
        return treeTableCell.getTableColumn();
    }

    @Override
    protected ObjectProperty<Node> graphicProperty() {
        TreeTableRow treeTableRow = (TreeTableRow)this.getSkinnable();
        if (treeTableRow == null) {
            return null;
        }
        if (this.treeItem == null) {
            return null;
        }
        return this.treeItem.graphicProperty();
    }

    private void updateTreeItem() {
        if (this.treeItem != null) {
            this.treeItem.graphicProperty().removeListener(this.graphicListener);
        }
        this.treeItem = ((TreeTableRow)this.getSkinnable()).getTreeItem();
        if (this.treeItem != null) {
            this.treeItem.graphicProperty().addListener(this.graphicListener);
        }
    }

    private TreeTableView<T> getTreeTableView() {
        return ((TreeTableRow)this.getSkinnable()).getTreeTableView();
    }

    private void updateDisclosureNodeAndGraphic() {
        Node node;
        Node node2;
        if (((TreeTableRow)this.getSkinnable()).isEmpty()) {
            this.getChildren().remove(this.graphic);
            return;
        }
        ObjectProperty<Node> objectProperty = this.graphicProperty();
        Node node3 = node2 = objectProperty == null ? null : (Node)objectProperty.get();
        if (node2 != null) {
            if (node2 != this.graphic) {
                this.getChildren().remove(this.graphic);
            }
            if (!this.getChildren().contains(node2)) {
                this.getChildren().add(node2);
                this.graphic = node2;
            }
        }
        if ((node = ((TreeTableRow)this.getSkinnable()).getDisclosureNode()) != null) {
            boolean bl = this.treeItem != null && !this.treeItem.isLeaf();
            node.setVisible(bl);
            if (!bl) {
                this.getChildren().remove(node);
            } else if (node.getParent() == null) {
                this.getChildren().add(node);
                node.toFront();
            } else {
                node.toBack();
            }
            if (node.getScene() != null) {
                node.applyCss();
            }
        }
    }

    private void updateTableViewSkin() {
        TreeTableView treeTableView = ((TreeTableRow)this.getSkinnable()).getTreeTableView();
        if (treeTableView != null && treeTableView.getSkin() instanceof TreeTableViewSkin) {
            this.treeTableViewSkin = (TreeTableViewSkin)treeTableView.getSkin();
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return TreeTableRowSkin.getClassCssMetaData();
    }

    @Override
    protected Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        TreeTableView treeTableView = ((TreeTableRow)this.getSkinnable()).getTreeTableView();
        switch (accessibleAttribute) {
            case SELECTED_ITEMS: {
                ArrayList<TreeTableCell> arrayList = new ArrayList<TreeTableCell>();
                int n = ((TreeTableRow)this.getSkinnable()).getIndex();
                Iterator iterator = treeTableView.getSelectionModel().getSelectedCells().iterator();
                if (iterator.hasNext()) {
                    TreeTablePosition treeTablePosition = (TreeTablePosition)iterator.next();
                    if (treeTablePosition.getRow() == n) {
                        TreeTableCell treeTableCell;
                        TreeTableColumn treeTableColumn = treeTablePosition.getTableColumn();
                        if (treeTableColumn == null) {
                            treeTableColumn = treeTableView.getVisibleLeafColumn(0);
                        }
                        if ((treeTableCell = (TreeTableCell)((Reference)this.cellsMap.get(treeTableColumn)).get()) != null) {
                            arrayList.add(treeTableCell);
                        }
                    }
                    return FXCollections.observableArrayList(arrayList);
                }
            }
            case CELL_AT_ROW_COLUMN: {
                int n = (Integer)objectArray[1];
                TreeTableColumn treeTableColumn = treeTableView.getVisibleLeafColumn(n);
                if (this.cellsMap.containsKey(treeTableColumn)) {
                    return ((Reference)this.cellsMap.get(treeTableColumn)).get();
                }
                return null;
            }
            case FOCUS_ITEM: {
                TreeTableView.TreeTableViewFocusModel treeTableViewFocusModel = treeTableView.getFocusModel();
                TreeTablePosition treeTablePosition = treeTableViewFocusModel.getFocusedCell();
                TreeTableColumn treeTableColumn = treeTablePosition.getTableColumn();
                if (treeTableColumn == null) {
                    treeTableColumn = treeTableView.getVisibleLeafColumn(0);
                }
                if (this.cellsMap.containsKey(treeTableColumn)) {
                    return ((Reference)this.cellsMap.get(treeTableColumn)).get();
                }
                return null;
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    private static class StyleableProperties {
        private static final CssMetaData<TreeTableRow<?>, Number> INDENT = new CssMetaData<TreeTableRow<?>, Number>("-fx-indent", SizeConverter.getInstance(), 10.0){

            @Override
            public boolean isSettable(TreeTableRow<?> treeTableRow) {
                DoubleProperty doubleProperty = ((TreeTableRowSkin)treeTableRow.getSkin()).indentProperty();
                return doubleProperty == null || !doubleProperty.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(TreeTableRow<?> treeTableRow) {
                TreeTableRowSkin treeTableRowSkin = (TreeTableRowSkin)treeTableRow.getSkin();
                return (StyleableProperty)((Object)treeTableRowSkin.indentProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(CellSkinBase.getClassCssMetaData());
            arrayList.add(INDENT);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

