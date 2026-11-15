/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.behavior.TreeViewBehavior;
import java.lang.ref.WeakReference;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Iterator;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.event.WeakEventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.FocusModel;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.skin.VirtualContainerBase;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class TreeViewSkin<T>
extends VirtualContainerBase<TreeView<T>, TreeCell<T>> {
    private static final boolean IS_PANNABLE = AccessController.doPrivileged(() -> Boolean.getBoolean("javafx.scene.control.skin.TreeViewSkin.pannable"));
    private final VirtualFlow<TreeCell<T>> flow;
    private WeakReference<TreeItem<T>> weakRoot;
    private final TreeViewBehavior<T> behavior;
    private MapChangeListener<Object, Object> propertiesMapListener = change -> {
        if (!change.wasAdded()) {
            return;
        }
        if ("recreateKey".equals(change.getKey())) {
            this.requestRebuildCells();
            ((TreeView)this.getSkinnable()).getProperties().remove("recreateKey");
        }
    };
    private EventHandler<TreeItem.TreeModificationEvent<T>> rootListener = treeModificationEvent -> {
        if (treeModificationEvent.wasAdded() && treeModificationEvent.wasRemoved() && treeModificationEvent.getAddedSize() == treeModificationEvent.getRemovedSize()) {
            this.markItemCountDirty();
            ((TreeView)this.getSkinnable()).requestLayout();
        } else if (treeModificationEvent.getEventType().equals(TreeItem.valueChangedEvent())) {
            this.requestRebuildCells();
        } else {
            for (EventType<? extends Event> eventType = treeModificationEvent.getEventType(); eventType != null; eventType = eventType.getSuperType()) {
                if (!eventType.equals(TreeItem.expandedItemCountChangeEvent())) continue;
                this.markItemCountDirty();
                ((TreeView)this.getSkinnable()).requestLayout();
                break;
            }
        }
        ((TreeView)this.getSkinnable()).edit(null);
    };
    private WeakEventHandler<TreeItem.TreeModificationEvent<T>> weakRootListener;

    public TreeViewSkin(TreeView treeView) {
        super(treeView);
        this.behavior = new TreeViewBehavior(treeView);
        this.flow = this.getVirtualFlow();
        this.flow.setPannable(IS_PANNABLE);
        this.flow.setCellFactory(this::createCell);
        this.flow.setFixedCellSize(treeView.getFixedCellSize());
        this.getChildren().add(this.flow);
        this.setRoot(((TreeView)this.getSkinnable()).getRoot());
        EventHandler<MouseEvent> eventHandler = mouseEvent -> {
            if (treeView.getEditingItem() != null) {
                treeView.edit(null);
            }
            if (treeView.isFocusTraversable()) {
                treeView.requestFocus();
            }
        };
        this.flow.getVbar().addEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler);
        this.flow.getHbar().addEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler);
        ObservableMap<Object, Object> observableMap = treeView.getProperties();
        observableMap.remove("recreateKey");
        observableMap.addListener(this.propertiesMapListener);
        this.behavior.setOnFocusPreviousRow(() -> this.onFocusPreviousCell());
        this.behavior.setOnFocusNextRow(() -> this.onFocusNextCell());
        this.behavior.setOnMoveToFirstCell(() -> this.onMoveToFirstCell());
        this.behavior.setOnMoveToLastCell(() -> this.onMoveToLastCell());
        this.behavior.setOnScrollPageDown(this::onScrollPageDown);
        this.behavior.setOnScrollPageUp(this::onScrollPageUp);
        this.behavior.setOnSelectPreviousRow(() -> this.onSelectPreviousCell());
        this.behavior.setOnSelectNextRow(() -> this.onSelectNextCell());
        this.registerChangeListener(treeView.rootProperty(), observableValue -> this.setRoot(((TreeView)this.getSkinnable()).getRoot()));
        this.registerChangeListener(treeView.showRootProperty(), observableValue -> {
            if (!((TreeView)this.getSkinnable()).isShowRoot() && this.getRoot() != null) {
                this.getRoot().setExpanded(true);
            }
            this.updateItemCount();
        });
        this.registerChangeListener(treeView.cellFactoryProperty(), observableValue -> this.flow.recreateCells());
        this.registerChangeListener(treeView.fixedCellSizeProperty(), observableValue -> this.flow.setFixedCellSize(((TreeView)this.getSkinnable()).getFixedCellSize()));
        this.updateItemCount();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        return this.computePrefHeight(-1.0, d2, d3, d4, d5) * 0.618033987;
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        return 400.0;
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        super.layoutChildren(d, d2, d3, d4);
        this.flow.resizeRelocate(d, d2, d3, d4);
    }

    @Override
    protected Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case FOCUS_ITEM: {
                FocusModel focusModel = ((TreeView)this.getSkinnable()).getFocusModel();
                int n = focusModel.getFocusedIndex();
                if (n == -1) {
                    if (this.getItemCount() > 0) {
                        n = 0;
                    } else {
                        return null;
                    }
                }
                return this.flow.getPrivateCell(n);
            }
            case ROW_AT_INDEX: {
                int n = (Integer)objectArray[0];
                return n < 0 ? null : this.flow.getPrivateCell(n);
            }
            case SELECTED_ITEMS: {
                MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getSkinnable()).getSelectionModel();
                ObservableList<Integer> observableList = multipleSelectionModel.getSelectedIndices();
                ArrayList<TreeCell<T>> arrayList = new ArrayList<TreeCell<T>>(observableList.size());
                Iterator iterator = observableList.iterator();
                while (iterator.hasNext()) {
                    int n = (Integer)iterator.next();
                    TreeCell<T> treeCell = this.flow.getPrivateCell(n);
                    if (treeCell == null) continue;
                    arrayList.add(treeCell);
                }
                return FXCollections.observableArrayList(arrayList);
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

    @Override
    protected void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
        switch (accessibleAction) {
            case SHOW_ITEM: {
                Node node = (Node)objectArray[0];
                if (!(node instanceof TreeCell)) break;
                TreeCell treeCell = (TreeCell)node;
                this.flow.scrollTo(treeCell.getIndex());
                break;
            }
            case SET_SELECTED_ITEMS: {
                MultipleSelectionModel multipleSelectionModel;
                ObservableList observableList = (ObservableList)objectArray[0];
                if (observableList == null || (multipleSelectionModel = ((TreeView)this.getSkinnable()).getSelectionModel()) == null) break;
                multipleSelectionModel.clearSelection();
                for (Node node : observableList) {
                    if (!(node instanceof TreeCell)) continue;
                    TreeCell treeCell = (TreeCell)node;
                    multipleSelectionModel.select(treeCell.getIndex());
                }
                break;
            }
            default: {
                super.executeAccessibleAction(accessibleAction, objectArray);
            }
        }
    }

    private TreeCell<T> createCell(VirtualFlow<TreeCell<T>> virtualFlow) {
        TreeCell<T> treeCell = ((TreeView)this.getSkinnable()).getCellFactory() != null ? ((TreeView)this.getSkinnable()).getCellFactory().call((TreeView)this.getSkinnable()) : this.createDefaultCellImpl();
        if (treeCell.getDisclosureNode() == null) {
            StackPane stackPane = new StackPane();
            stackPane.getStyleClass().setAll((String[])new String[]{"tree-disclosure-node"});
            StackPane stackPane2 = new StackPane();
            stackPane2.getStyleClass().setAll((String[])new String[]{"arrow"});
            stackPane.getChildren().add(stackPane2);
            treeCell.setDisclosureNode(stackPane);
        }
        treeCell.updateTreeView((TreeView)this.getSkinnable());
        return treeCell;
    }

    private TreeItem<T> getRoot() {
        return this.weakRoot == null ? null : (TreeItem)this.weakRoot.get();
    }

    private void setRoot(TreeItem<T> treeItem) {
        if (this.getRoot() != null && this.weakRootListener != null) {
            this.getRoot().removeEventHandler(TreeItem.treeNotificationEvent(), this.weakRootListener);
        }
        this.weakRoot = new WeakReference<TreeItem<TreeItem<T>>>(treeItem);
        if (this.getRoot() != null) {
            this.weakRootListener = new WeakEventHandler<TreeItem.TreeModificationEvent<TreeItem.TreeModificationEvent<T>>>(this.rootListener);
            this.getRoot().addEventHandler(TreeItem.treeNotificationEvent(), this.weakRootListener);
        }
        this.updateItemCount();
    }

    @Override
    protected int getItemCount() {
        return ((TreeView)this.getSkinnable()).getExpandedItemCount();
    }

    @Override
    protected void updateItemCount() {
        int n = this.getItemCount();
        this.requestRebuildCells();
        this.flow.setCellCount(n);
        ((TreeView)this.getSkinnable()).requestLayout();
    }

    private TreeCell<T> createDefaultCellImpl() {
        return new TreeCell<T>(){
            private HBox hbox;
            private WeakReference<TreeItem<T>> treeItemRef;
            private InvalidationListener treeItemGraphicListener = observable -> this.updateDisplay(this.getItem(), this.isEmpty());
            private InvalidationListener treeItemListener = new InvalidationListener(){

                @Override
                public void invalidated(Observable observable) {
                    TreeItem treeItem;
                    TreeItem treeItem2;
                    TreeItem treeItem3 = treeItem2 = treeItemRef == null ? null : (TreeItem)treeItemRef.get();
                    if (treeItem2 != null) {
                        treeItem2.graphicProperty().removeListener(weakTreeItemGraphicListener);
                    }
                    if ((treeItem = this.getTreeItem()) != null) {
                        treeItem.graphicProperty().addListener(weakTreeItemGraphicListener);
                        treeItemRef = new WeakReference(treeItem);
                    }
                }
            };
            private WeakInvalidationListener weakTreeItemGraphicListener = new WeakInvalidationListener(this.treeItemGraphicListener);
            private WeakInvalidationListener weakTreeItemListener = new WeakInvalidationListener(this.treeItemListener);
            {
                this.treeItemProperty().addListener(this.weakTreeItemListener);
                if (this.getTreeItem() != null) {
                    this.getTreeItem().graphicProperty().addListener(this.weakTreeItemGraphicListener);
                }
            }

            private void updateDisplay(T t, boolean bl) {
                if (t == null || bl) {
                    this.hbox = null;
                    this.setText(null);
                    this.setGraphic(null);
                } else {
                    Node node;
                    TreeItem treeItem = this.getTreeItem();
                    Node node2 = node = treeItem == null ? null : treeItem.getGraphic();
                    if (node != null) {
                        if (t instanceof Node) {
                            this.setText(null);
                            if (this.hbox == null) {
                                this.hbox = new HBox(3.0);
                            }
                            this.hbox.getChildren().setAll((Node[])new Node[]{node, (Node)t});
                            this.setGraphic(this.hbox);
                        } else {
                            this.hbox = null;
                            this.setText(t.toString());
                            this.setGraphic(node);
                        }
                    } else {
                        this.hbox = null;
                        if (t instanceof Node) {
                            this.setText(null);
                            this.setGraphic((Node)t);
                        } else {
                            this.setText(t.toString());
                            this.setGraphic(null);
                        }
                    }
                }
            }

            @Override
            public void updateItem(T t, boolean bl) {
                super.updateItem(t, bl);
                this.updateDisplay(t, bl);
            }
        };
    }

    private void onFocusPreviousCell() {
        FocusModel focusModel = ((TreeView)this.getSkinnable()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        this.flow.scrollTo(focusModel.getFocusedIndex());
    }

    private void onFocusNextCell() {
        FocusModel focusModel = ((TreeView)this.getSkinnable()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        this.flow.scrollTo(focusModel.getFocusedIndex());
    }

    private void onSelectPreviousCell() {
        int n = ((TreeView)this.getSkinnable()).getSelectionModel().getSelectedIndex();
        this.flow.scrollTo(n);
    }

    private void onSelectNextCell() {
        int n = ((TreeView)this.getSkinnable()).getSelectionModel().getSelectedIndex();
        this.flow.scrollTo(n);
    }

    private void onMoveToFirstCell() {
        this.flow.scrollTo(0);
        this.flow.setPosition(0.0);
    }

    private void onMoveToLastCell() {
        this.flow.scrollTo(this.getItemCount());
        this.flow.setPosition(1.0);
    }

    private int onScrollPageDown(boolean bl) {
        int n;
        TreeCell<T> treeCell = this.flow.getLastVisibleCellWithinViewPort();
        if (treeCell == null) {
            return -1;
        }
        MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getSkinnable()).getSelectionModel();
        FocusModel focusModel = ((TreeView)this.getSkinnable()).getFocusModel();
        if (multipleSelectionModel == null || focusModel == null) {
            return -1;
        }
        int n2 = treeCell.getIndex();
        boolean bl2 = false;
        if (bl) {
            bl2 = treeCell.isFocused() || focusModel.isFocused(n2);
        } else {
            boolean bl3 = bl2 = treeCell.isSelected() || multipleSelectionModel.isSelected(n2);
        }
        if (bl2) {
            int n3 = n = bl && focusModel.getFocusedIndex() == n2 || !bl && multipleSelectionModel.getSelectedIndex() == n2 ? 1 : 0;
            if (n != 0) {
                this.flow.scrollToTop(treeCell);
                TreeCell<T> treeCell2 = this.flow.getLastVisibleCellWithinViewPort();
                treeCell = treeCell2 == null ? treeCell : treeCell2;
            }
        }
        n = treeCell.getIndex();
        this.flow.scrollTo(treeCell);
        return n;
    }

    private int onScrollPageUp(boolean bl) {
        int n;
        TreeCell<T> treeCell = this.flow.getFirstVisibleCellWithinViewPort();
        if (treeCell == null) {
            return -1;
        }
        MultipleSelectionModel multipleSelectionModel = ((TreeView)this.getSkinnable()).getSelectionModel();
        FocusModel focusModel = ((TreeView)this.getSkinnable()).getFocusModel();
        if (multipleSelectionModel == null || focusModel == null) {
            return -1;
        }
        int n2 = treeCell.getIndex();
        boolean bl2 = false;
        if (bl) {
            bl2 = treeCell.isFocused() || focusModel.isFocused(n2);
        } else {
            boolean bl3 = bl2 = treeCell.isSelected() || multipleSelectionModel.isSelected(n2);
        }
        if (bl2) {
            int n3 = n = bl && focusModel.getFocusedIndex() == n2 || !bl && multipleSelectionModel.getSelectedIndex() == n2 ? 1 : 0;
            if (n != 0) {
                this.flow.scrollToBottom(treeCell);
                TreeCell<T> treeCell2 = this.flow.getFirstVisibleCellWithinViewPort();
                treeCell = treeCell2 == null ? treeCell : treeCell2;
            }
        }
        n = treeCell.getIndex();
        this.flow.scrollTo(treeCell);
        return n;
    }
}

