/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.skin;

import com.sun.javafx.scene.control.behavior.ListViewBehavior;
import com.sun.javafx.scene.control.skin.resources.ControlResources;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.Iterator;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.WeakListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.FocusModel;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.skin.VirtualContainerBase;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

public class ListViewSkin<T>
extends VirtualContainerBase<ListView<T>, ListCell<T>> {
    private static final boolean IS_PANNABLE = AccessController.doPrivileged(() -> Boolean.getBoolean("javafx.scene.control.skin.ListViewSkin.pannable"));
    private static final String EMPTY_LIST_TEXT = ControlResources.getString("ListView.noContent");
    private final VirtualFlow<ListCell<T>> flow;
    private StackPane placeholderRegion;
    private Node placeholderNode;
    private ObservableList<T> listViewItems;
    private final InvalidationListener itemsChangeListener = observable -> this.updateListViewItems();
    private boolean needCellsRebuilt = true;
    private boolean needCellsReconfigured = false;
    private int itemCount = -1;
    private ListViewBehavior<T> behavior;
    private MapChangeListener<Object, Object> propertiesMapListener = change -> {
        if (!change.wasAdded()) {
            return;
        }
        if ("recreateKey".equals(change.getKey())) {
            this.needCellsRebuilt = true;
            ((ListView)this.getSkinnable()).requestLayout();
            ((ListView)this.getSkinnable()).getProperties().remove("recreateKey");
        }
    };
    private final ListChangeListener<T> listViewItemsListener = new ListChangeListener<T>(){

        @Override
        public void onChanged(ListChangeListener.Change<? extends T> change) {
            while (change.next()) {
                if (change.wasReplaced()) {
                    for (int i = change.getFrom(); i < change.getTo(); ++i) {
                        ListViewSkin.this.flow.setCellDirty(i);
                    }
                    break;
                }
                if (change.getRemovedSize() != ListViewSkin.this.itemCount) continue;
                ListViewSkin.this.itemCount = 0;
                break;
            }
            ((ListView)ListViewSkin.this.getSkinnable()).edit(-1);
            ListViewSkin.this.markItemCountDirty();
            ((ListView)ListViewSkin.this.getSkinnable()).requestLayout();
        }
    };
    private final WeakListChangeListener<T> weakListViewItemsListener = new WeakListChangeListener<T>(this.listViewItemsListener);

    public ListViewSkin(ListView<T> listView) {
        super(listView);
        this.behavior = new ListViewBehavior<T>(listView);
        this.behavior.setOnFocusPreviousRow(() -> this.onFocusPreviousCell());
        this.behavior.setOnFocusNextRow(() -> this.onFocusNextCell());
        this.behavior.setOnMoveToFirstCell(() -> this.onMoveToFirstCell());
        this.behavior.setOnMoveToLastCell(() -> this.onMoveToLastCell());
        this.behavior.setOnSelectPreviousRow(() -> this.onSelectPreviousCell());
        this.behavior.setOnSelectNextRow(() -> this.onSelectNextCell());
        this.behavior.setOnScrollPageDown(this::onScrollPageDown);
        this.behavior.setOnScrollPageUp(this::onScrollPageUp);
        this.updateListViewItems();
        this.flow = this.getVirtualFlow();
        this.flow.setId("virtual-flow");
        this.flow.setPannable(IS_PANNABLE);
        this.flow.setVertical(listView.getOrientation() == Orientation.VERTICAL);
        this.flow.setCellFactory(virtualFlow -> this.createCell());
        this.flow.setFixedCellSize(listView.getFixedCellSize());
        this.getChildren().add(this.flow);
        EventHandler<MouseEvent> eventHandler = mouseEvent -> {
            if (listView.getEditingIndex() > -1) {
                listView.edit(-1);
            }
            if (listView.isFocusTraversable()) {
                listView.requestFocus();
            }
        };
        this.flow.getVbar().addEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler);
        this.flow.getHbar().addEventFilter(MouseEvent.MOUSE_PRESSED, eventHandler);
        this.updateItemCount();
        listView.itemsProperty().addListener(new WeakInvalidationListener(this.itemsChangeListener));
        ObservableMap<Object, Object> observableMap = listView.getProperties();
        observableMap.remove("recreateKey");
        observableMap.addListener(this.propertiesMapListener);
        this.registerChangeListener(listView.itemsProperty(), observableValue -> this.updateListViewItems());
        this.registerChangeListener(listView.orientationProperty(), observableValue -> this.flow.setVertical(listView.getOrientation() == Orientation.VERTICAL));
        this.registerChangeListener(listView.cellFactoryProperty(), observableValue -> this.flow.recreateCells());
        this.registerChangeListener(listView.parentProperty(), observableValue -> {
            if (listView.getParent() != null && listView.isVisible()) {
                listView.requestLayout();
            }
        });
        this.registerChangeListener(listView.placeholderProperty(), observableValue -> this.updatePlaceholderRegionVisibility());
        this.registerChangeListener(listView.fixedCellSizeProperty(), observableValue -> this.flow.setFixedCellSize(listView.getFixedCellSize()));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (this.behavior != null) {
            this.behavior.dispose();
        }
    }

    @Override
    protected void layoutChildren(double d, double d2, double d3, double d4) {
        super.layoutChildren(d, d2, d3, d4);
        if (this.needCellsRebuilt) {
            this.flow.rebuildCells();
        } else if (this.needCellsReconfigured) {
            this.flow.reconfigureCells();
        }
        this.needCellsRebuilt = false;
        this.needCellsReconfigured = false;
        if (this.getItemCount() == 0) {
            if (this.placeholderRegion != null) {
                this.placeholderRegion.setVisible(d3 > 0.0 && d4 > 0.0);
                this.placeholderRegion.resizeRelocate(d, d2, d3, d4);
            }
        } else {
            this.flow.resizeRelocate(d, d2, d3, d4);
        }
    }

    @Override
    protected double computePrefWidth(double d, double d2, double d3, double d4, double d5) {
        this.checkState();
        if (this.getItemCount() == 0) {
            if (this.placeholderRegion == null) {
                this.updatePlaceholderRegionVisibility();
            }
            if (this.placeholderRegion != null) {
                return this.placeholderRegion.prefWidth(d) + d5 + d3;
            }
        }
        return this.computePrefHeight(-1.0, d2, d3, d4, d5) * 0.618033987;
    }

    @Override
    protected double computePrefHeight(double d, double d2, double d3, double d4, double d5) {
        return 400.0;
    }

    @Override
    protected int getItemCount() {
        return this.itemCount;
    }

    @Override
    protected void updateItemCount() {
        int n;
        if (this.flow == null) {
            return;
        }
        int n2 = this.itemCount;
        this.itemCount = n = this.listViewItems == null ? 0 : this.listViewItems.size();
        this.flow.setCellCount(n);
        this.updatePlaceholderRegionVisibility();
        if (n != n2) {
            this.requestRebuildCells();
        } else {
            this.needCellsReconfigured = true;
        }
    }

    @Override
    protected Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case FOCUS_ITEM: {
                FocusModel focusModel = ((ListView)this.getSkinnable()).getFocusModel();
                int n = focusModel.getFocusedIndex();
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
            case ITEM_COUNT: {
                return this.getItemCount();
            }
            case ITEM_AT_INDEX: {
                Integer n = (Integer)objectArray[0];
                if (n == null) {
                    return null;
                }
                if (0 <= n && n < this.getItemCount()) {
                    return this.flow.getPrivateCell(n);
                }
                return null;
            }
            case SELECTED_ITEMS: {
                MultipleSelectionModel multipleSelectionModel = ((ListView)this.getSkinnable()).getSelectionModel();
                ObservableList<Integer> observableList = multipleSelectionModel.getSelectedIndices();
                ArrayList<ListCell<T>> arrayList = new ArrayList<ListCell<T>>(observableList.size());
                Iterator iterator = observableList.iterator();
                while (iterator.hasNext()) {
                    int n = (Integer)iterator.next();
                    ListCell<T> listCell = this.flow.getPrivateCell(n);
                    if (listCell == null) continue;
                    arrayList.add(listCell);
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
                if (!(node instanceof ListCell)) break;
                ListCell listCell = (ListCell)node;
                this.flow.scrollTo(listCell.getIndex());
                break;
            }
            case SET_SELECTED_ITEMS: {
                MultipleSelectionModel multipleSelectionModel;
                ObservableList observableList = (ObservableList)objectArray[0];
                if (observableList == null || (multipleSelectionModel = ((ListView)this.getSkinnable()).getSelectionModel()) == null) break;
                multipleSelectionModel.clearSelection();
                for (Node node : observableList) {
                    if (!(node instanceof ListCell)) continue;
                    ListCell listCell = (ListCell)node;
                    multipleSelectionModel.select(listCell.getIndex());
                }
                break;
            }
            default: {
                super.executeAccessibleAction(accessibleAction, objectArray);
            }
        }
    }

    private ListCell<T> createCell() {
        ListCell<T> listCell = ((ListView)this.getSkinnable()).getCellFactory() != null ? ((ListView)this.getSkinnable()).getCellFactory().call((ListView)this.getSkinnable()) : ListViewSkin.createDefaultCellImpl();
        listCell.updateListView((ListView)this.getSkinnable());
        return listCell;
    }

    private void updateListViewItems() {
        if (this.listViewItems != null) {
            this.listViewItems.removeListener(this.weakListViewItemsListener);
        }
        this.listViewItems = ((ListView)this.getSkinnable()).getItems();
        if (this.listViewItems != null) {
            this.listViewItems.addListener(this.weakListViewItemsListener);
        }
        this.markItemCountDirty();
        ((ListView)this.getSkinnable()).requestLayout();
    }

    private final void updatePlaceholderRegionVisibility() {
        boolean bl;
        boolean bl2 = bl = this.getItemCount() == 0;
        if (bl) {
            this.placeholderNode = ((ListView)this.getSkinnable()).getPlaceholder();
            if (this.placeholderNode == null && EMPTY_LIST_TEXT != null && !EMPTY_LIST_TEXT.isEmpty()) {
                this.placeholderNode = new Label();
                ((Label)this.placeholderNode).setText(EMPTY_LIST_TEXT);
            }
            if (this.placeholderNode != null) {
                if (this.placeholderRegion == null) {
                    this.placeholderRegion = new StackPane();
                    this.placeholderRegion.getStyleClass().setAll((String[])new String[]{"placeholder"});
                    this.getChildren().add(this.placeholderRegion);
                }
                this.placeholderRegion.getChildren().setAll((Node[])new Node[]{this.placeholderNode});
            }
        }
        this.flow.setVisible(!bl);
        if (this.placeholderRegion != null) {
            this.placeholderRegion.setVisible(bl);
        }
    }

    private static <T> ListCell<T> createDefaultCellImpl() {
        return new ListCell<T>(){

            @Override
            public void updateItem(T t, boolean bl) {
                super.updateItem(t, bl);
                if (bl) {
                    this.setText(null);
                    this.setGraphic(null);
                } else if (t instanceof Node) {
                    this.setText(null);
                    Node node = this.getGraphic();
                    Node node2 = (Node)t;
                    if (node == null || !node.equals(node2)) {
                        this.setGraphic(node2);
                    }
                } else {
                    this.setText(t == null ? "null" : t.toString());
                    this.setGraphic(null);
                }
            }
        };
    }

    private void onFocusPreviousCell() {
        FocusModel focusModel = ((ListView)this.getSkinnable()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        this.flow.scrollTo(focusModel.getFocusedIndex());
    }

    private void onFocusNextCell() {
        FocusModel focusModel = ((ListView)this.getSkinnable()).getFocusModel();
        if (focusModel == null) {
            return;
        }
        this.flow.scrollTo(focusModel.getFocusedIndex());
    }

    private void onSelectPreviousCell() {
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getSkinnable()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        int n = multipleSelectionModel.getSelectedIndex();
        this.flow.scrollTo(n);
        ListCell<T> listCell = this.flow.getFirstVisibleCell();
        if (listCell == null || n < listCell.getIndex()) {
            this.flow.setPosition((double)n / (double)this.getItemCount());
        }
    }

    private void onSelectNextCell() {
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getSkinnable()).getSelectionModel();
        if (multipleSelectionModel == null) {
            return;
        }
        int n = multipleSelectionModel.getSelectedIndex();
        this.flow.scrollTo(n);
        ListCell<T> listCell = this.flow.getLastVisibleCell();
        if (listCell == null || listCell.getIndex() < n) {
            this.flow.setPosition((double)n / (double)this.getItemCount());
        }
    }

    private void onMoveToFirstCell() {
        this.flow.scrollTo(0);
        this.flow.setPosition(0.0);
    }

    private void onMoveToLastCell() {
        int n = this.getItemCount() - 1;
        this.flow.scrollTo(n);
        this.flow.setPosition(1.0);
    }

    private int onScrollPageDown(boolean bl) {
        int n;
        ListCell<T> listCell = this.flow.getLastVisibleCellWithinViewPort();
        if (listCell == null) {
            return -1;
        }
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getSkinnable()).getSelectionModel();
        FocusModel focusModel = ((ListView)this.getSkinnable()).getFocusModel();
        if (multipleSelectionModel == null || focusModel == null) {
            return -1;
        }
        int n2 = listCell.getIndex();
        boolean bl2 = false;
        if (bl) {
            bl2 = listCell.isFocused() || focusModel.isFocused(n2);
        } else {
            boolean bl3 = bl2 = listCell.isSelected() || multipleSelectionModel.isSelected(n2);
        }
        if (bl2) {
            int n3 = n = bl && focusModel.getFocusedIndex() == n2 || !bl && multipleSelectionModel.getSelectedIndex() == n2 ? 1 : 0;
            if (n != 0) {
                this.flow.scrollToTop(listCell);
                ListCell<T> listCell2 = this.flow.getLastVisibleCellWithinViewPort();
                listCell = listCell2 == null ? listCell : listCell2;
            }
        }
        n = listCell.getIndex();
        this.flow.scrollTo(listCell);
        return n;
    }

    private int onScrollPageUp(boolean bl) {
        int n;
        ListCell<T> listCell = this.flow.getFirstVisibleCellWithinViewPort();
        if (listCell == null) {
            return -1;
        }
        MultipleSelectionModel multipleSelectionModel = ((ListView)this.getSkinnable()).getSelectionModel();
        FocusModel focusModel = ((ListView)this.getSkinnable()).getFocusModel();
        if (multipleSelectionModel == null || focusModel == null) {
            return -1;
        }
        int n2 = listCell.getIndex();
        boolean bl2 = false;
        if (bl) {
            bl2 = listCell.isFocused() || focusModel.isFocused(n2);
        } else {
            boolean bl3 = bl2 = listCell.isSelected() || multipleSelectionModel.isSelected(n2);
        }
        if (bl2) {
            int n3 = n = bl && focusModel.getFocusedIndex() == n2 || !bl && multipleSelectionModel.getSelectedIndex() == n2 ? 1 : 0;
            if (n != 0) {
                this.flow.scrollToBottom(listCell);
                ListCell<T> listCell2 = this.flow.getFirstVisibleCellWithinViewPort();
                listCell = listCell2 == null ? listCell : listCell2;
            }
        }
        n = listCell.getIndex();
        this.flow.scrollTo(listCell);
        return n;
    }
}

