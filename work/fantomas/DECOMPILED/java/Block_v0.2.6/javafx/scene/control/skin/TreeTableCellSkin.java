/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.behavior.BehaviorBase;
import com.sun.javafx.scene.control.behavior.TreeTableCellBehavior;
import java.util.Map;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.skin.TableCellSkinBase;
import javafx.scene.control.skin.TableRowSkinBase;
import javafx.scene.control.skin.TreeTableRowSkin;

public class TreeTableCellSkin<S, T>
extends TableCellSkinBase<TreeItem<S>, T, TreeTableCell<S, T>> {
    private final BehaviorBase<TreeTableCell<S, T>> behavior;

    public TreeTableCellSkin(TreeTableCell<S, T> treeTableCell) {
        super(treeTableCell);
        this.behavior = new TreeTableCellBehavior<S, T>(treeTableCell);
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }

    @Override
    public ReadOnlyObjectProperty<TreeTableColumn<S, T>> tableColumnProperty() {
        return ((TreeTableCell)this.getSkinnable()).tableColumnProperty();
    }

    @Override
    double leftLabelPadding() {
        double d = super.leftLabelPadding();
        double d2 = this.getCellSize();
        TreeTableCell treeTableCell = (TreeTableCell)this.getSkinnable();
        TreeTableColumn treeTableColumn = treeTableCell.getTableColumn();
        if (treeTableColumn == null) {
            return d;
        }
        TreeTableView treeTableView = treeTableCell.getTreeTableView();
        if (treeTableView == null) {
            return d;
        }
        int n = treeTableView.getVisibleLeafIndex(treeTableColumn);
        TreeTableColumn treeTableColumn2 = treeTableView.getTreeColumn();
        if (treeTableColumn2 == null && n != 0 || treeTableColumn2 != null && !treeTableColumn.equals(treeTableColumn2)) {
            return d;
        }
        TreeTableRow treeTableRow = treeTableCell.getTreeTableRow();
        if (treeTableRow == null) {
            return d;
        }
        TreeItem treeItem = treeTableRow.getTreeItem();
        if (treeItem == null) {
            return d;
        }
        int n2 = treeTableView.getTreeItemLevel(treeItem);
        if (!treeTableView.isShowRoot()) {
            --n2;
        }
        double d3 = 10.0;
        if (treeTableRow.getSkin() instanceof TreeTableRowSkin) {
            d3 = ((TreeTableRowSkin)treeTableRow.getSkin()).getIndentationPerLevel();
        }
        d += (double)n2 * d3;
        Map<TableColumnBase<?, ?>, Double> map = TableRowSkinBase.maxDisclosureWidthMap;
        d += map.containsKey(treeTableColumn2) ? map.get(treeTableColumn2) : 0.0;
        Node node = treeItem.getGraphic();
        return d += node == null ? 0.0 : node.prefWidth(d2);
    }
}

