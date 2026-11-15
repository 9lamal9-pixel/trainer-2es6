/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.TreeTableViewBackingList;
import com.sun.javafx.scene.control.behavior.TreeTableViewBehavior;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.event.WeakEventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.skin.TableViewSkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class TreeTableViewSkin<T>
extends TableViewSkinBase<T, TreeItem<T>, TreeTableView<T>, TreeTableRow<T>, TreeTableColumn<T, ?>> {
    TreeTableViewBackingList<T> tableBackingList;
    ObjectProperty<ObservableList<TreeItem<T>>> tableBackingListProperty;
    private WeakReference<TreeItem<T>> weakRootRef;
    private final TreeTableViewBehavior<T> behavior;
    private EventHandler<TreeItem.TreeModificationEvent<T>> rootListener = treeModificationEvent -> {
        if (treeModificationEvent.wasAdded() && treeModificationEvent.wasRemoved() && treeModificationEvent.getAddedSize() == treeModificationEvent.getRemovedSize()) {
            this.markItemCountDirty();
            ((TreeTableView)this.getSkinnable()).requestLayout();
        } else if (treeModificationEvent.getEventType().equals(TreeItem.valueChangedEvent())) {
            this.requestRebuildCells();
        } else {
            for (EventType<? extends Event> eventType = treeModificationEvent.getEventType(); eventType != null; eventType = eventType.getSuperType()) {
                if (!eventType.equals(TreeItem.expandedItemCountChangeEvent())) continue;
                this.markItemCountDirty();
                ((TreeTableView)this.getSkinnable()).requestLayout();
                break;
            }
        }
        ((TreeTableView)this.getSkinnable()).edit(-1, null);
    };
    private WeakEventHandler<TreeItem.TreeModificationEvent<T>> weakRootListener;

    public TreeTableViewSkin(TreeTableView<T> treeTableView) {
        super(treeTableView);
        this.behavior = new TreeTableViewBehavior<T>(treeTableView);
        this.flow.setFixedCellSize(treeTableView.getFixedCellSize());
        this.flow.setCellFactory(virtualFlow -> this.createCell());
        this.setRoot(((TreeTableView)this.getSkinnable()).getRoot());
        EventHandler<MouseEvent> eventHandler = mouseEvent -> {
            if (treeTableView.getEditingCell() != null) {
                treeTableView.edit(-1, null);
            }
            if (treeTableView.isFocusTraversable()) {
                treeTableView.requestFocus();
            }
        };
        this.flow.getVbar().addEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler);
        this.flow.getHbar().addEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler);
        this.behavior.setOnFocusPreviousRow(() -> this.onFocusPreviousCell());
        this.behavior.setOnFocusNextRow(() -> this.onFocusNextCell());
        this.behavior.setOnMoveToFirstCell(() -> this.onMoveToFirstCell());
        this.behavior.setOnMoveToLastCell(() -> this.onMoveToLastCell());
        this.behavior.setOnScrollPageDown(bl -> this.onScrollPageDown((boolean)bl));
        this.behavior.setOnScrollPageUp(bl -> this.onScrollPageUp((boolean)bl));
        this.behavior.setOnSelectPreviousRow(() -> this.onSelectPreviousCell());
        this.behavior.setOnSelectNextRow(() -> this.onSelectNextCell());
        this.behavior.setOnSelectLeftCell(() -> this.onSelectLeftCell());
        this.behavior.setOnSelectRightCell(() -> this.onSelectRightCell());
        this.registerChangeListener(treeTableView.rootProperty(), observableValue -> {
            ((TreeTableView)this.getSkinnable()).edit(-1, null);
            this.setRoot(((TreeTableView)this.getSkinnable()).getRoot());
        });
        this.registerChangeListener(treeTableView.showRootProperty(), observableValue -> {
            if (!((TreeTableView)this.getSkinnable()).isShowRoot() && this.getRoot() != null) {
                this.getRoot().setExpanded(true);
            }
            this.updateItemCount();
        });
        this.registerChangeListener(treeTableView.rowFactoryProperty(), observableValue -> this.flow.recreateCells());
        this.registerChangeListener(treeTableView.expandedItemCountProperty(), observableValue -> this.markItemCountDirty());
        this.registerChangeListener(treeTableView.fixedCellSizeProperty(), observableValue -> this.flow.setFixedCellSize(((TreeTableView)this.getSkinnable()).getFixedCellSize()));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }

    @Override
    protected Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case ROW_AT_INDEX: {
                int n = (Integer)objectArray[0];
                return n < 0 ? null : this.flow.getPrivateCell(n);
            }
            case SELECTED_ITEMS: {
                ArrayList<TreeTableRow> arrayList = new ArrayList<TreeTableRow>();
                TreeTableView.TreeTableViewSelectionModel treeTableViewSelectionModel = ((TreeTableView)this.getSkinnable()).getSelectionModel();
                for (TreeTablePosition treeTablePosition : treeTableViewSelectionModel.getSelectedCells()) {
                    TreeTableRow treeTableRow = (TreeTableRow)this.flow.getPrivateCell(treeTablePosition.getRow());
                    if (treeTableRow == null) continue;
                    arrayList.add(treeTableRow);
                }
                return FXCollections.observableArrayList(arrayList);
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    @Override
    protected void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
        switch (accessibleAction) {
            case SHOW_ITEM: {
                Node node = (Node)objectArray[0];
                if (!(node instanceof TreeTableCell)) break;
                TreeTableCell treeTableCell = (TreeTableCell)node;
                this.flow.scrollTo(treeTableCell.getIndex());
                break;
            }
            case SET_SELECTED_ITEMS: {
                TreeTableView.TreeTableViewSelectionModel treeTableViewSelectionModel;
                ObservableList observableList = (ObservableList)objectArray[0];
                if (observableList == null || (treeTableViewSelectionModel = ((TreeTableView)this.getSkinnable()).getSelectionModel()) == null) break;
                treeTableViewSelectionModel.clearSelection();
                for (Node node : observableList) {
                    if (!(node instanceof TreeTableCell)) continue;
                    TreeTableCell treeTableCell = (TreeTableCell)node;
                    treeTableViewSelectionModel.select(treeTableCell.getIndex(), treeTableCell.getTableColumn());
                }
                break;
            }
            default: {
                super.executeAccessibleAction(accessibleAction, objectArray);
            }
        }
    }

    private TreeTableRow<T> createCell() {
        TreeTableView treeTableView = (TreeTableView)this.getSkinnable();
        TreeTableRow<Object> treeTableRow = treeTableView.getRowFactory() != null ? treeTableView.getRowFactory().call(treeTableView) : new TreeTableRow();
        if (treeTableRow.getDisclosureNode() == null) {
            StackPane stackPane = new StackPane();
            stackPane.getStyleClass().setAll((String[])new String[]{"tree-disclosure-node"});
            stackPane.setMouseTransparent(true);
            StackPane stackPane2 = new StackPane();
            stackPane2.getStyleClass().setAll((String[])new String[]{"arrow"});
            stackPane.getChildren().add(stackPane2);
            treeTableRow.setDisclosureNode(stackPane);
        }
        treeTableRow.updateTreeTableView(treeTableView);
        return treeTableRow;
    }

    private TreeItem<T> getRoot() {
        return this.weakRootRef == null ? null : (TreeItem)this.weakRootRef.get();
    }

    private void setRoot(TreeItem<T> treeItem) {
        if (this.getRoot() != null && this.weakRootListener != null) {
            this.getRoot().removeEventHandler(TreeItem.treeNotificationEvent(), this.weakRootListener);
        }
        this.weakRootRef = new WeakReference<TreeItem<TreeItem<T>>>(treeItem);
        if (this.getRoot() != null) {
            this.weakRootListener = new WeakEventHandler<TreeItem.TreeModificationEvent<TreeItem.TreeModificationEvent<T>>>(this.rootListener);
            this.getRoot().addEventHandler(TreeItem.treeNotificationEvent(), this.weakRootListener);
        }
        this.updateItemCount();
    }

    @Override
    protected int getItemCount() {
        return ((TreeTableView)this.getSkinnable()).getExpandedItemCount();
    }

    @Override
    void horizontalScroll() {
        super.horizontalScroll();
        if (((TreeTableView)this.getSkinnable()).getFixedCellSize() > 0.0) {
            this.flow.requestCellLayout();
        }
    }

    @Override
    protected void updateItemCount() {
        this.updatePlaceholderRegionVisibility();
        this.tableBackingList.resetSize();
        int n = this.flow.getCellCount();
        int n2 = this.getItemCount();
        this.flow.setCellCount(n2);
        if (n2 == n) {
            this.needCellsReconfigured = true;
        }
    }
}

