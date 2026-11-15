/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.collections.MappingChange;
import com.sun.javafx.collections.NonIterableChange;
import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.scene.control.Logging;
import com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList;
import com.sun.javafx.scene.control.SelectedCellsMap;
import com.sun.javafx.scene.control.SelectedItemsReadOnlyObservableList;
import com.sun.javafx.scene.control.TableColumnComparatorBase;
import com.sun.javafx.scene.control.behavior.TableCellBehavior;
import com.sun.javafx.scene.control.behavior.TableCellBehaviorBase;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.WeakHashMap;
import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.collections.transformation.SortedList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.ControlUtils;
import javafx.scene.control.MultipleSelectionModelBase;
import javafx.scene.control.ResizeFeaturesBase;
import javafx.scene.control.ScrollToEvent;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.control.SortEvent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TableFocusModel;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TablePositionBase;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableSelectionModel;
import javafx.scene.control.TableUtil;
import javafx.scene.control.skin.TableViewSkin;
import javafx.util.Callback;

@DefaultProperty(value="items")
public class TableView<S>
extends Control {
    static final String SET_CONTENT_WIDTH = "TableView.contentWidth";
    public static final Callback<ResizeFeatures, Boolean> UNCONSTRAINED_RESIZE_POLICY = new Callback<ResizeFeatures, Boolean>(){

        public String toString() {
            return "unconstrained-resize";
        }

        @Override
        public Boolean call(ResizeFeatures resizeFeatures) {
            double d = TableUtil.resize(resizeFeatures.getColumn(), resizeFeatures.getDelta());
            return Double.compare(d, 0.0) == 0;
        }
    };
    public static final Callback<ResizeFeatures, Boolean> CONSTRAINED_RESIZE_POLICY = new Callback<ResizeFeatures, Boolean>(){
        private boolean isFirstRun = true;

        public String toString() {
            return "constrained-resize";
        }

        @Override
        public Boolean call(ResizeFeatures resizeFeatures) {
            TableView tableView = resizeFeatures.getTable();
            ObservableList observableList = tableView.getVisibleLeafColumns();
            Boolean bl = TableUtil.constrainedResize(resizeFeatures, this.isFirstRun, tableView.contentWidth, observableList);
            this.isFirstRun = !this.isFirstRun ? false : bl == false;
            return bl;
        }
    };
    public static final Callback<TableView, Boolean> DEFAULT_SORT_POLICY = new Callback<TableView, Boolean>(){

        @Override
        public Boolean call(TableView tableView) {
            try {
                ObservableList observableList = tableView.getItems();
                if (observableList instanceof SortedList) {
                    SortedList sortedList = (SortedList)observableList;
                    boolean bl = sortedList.comparatorProperty().isEqualTo(tableView.comparatorProperty()).get();
                    if (!bl && Logging.getControlsLogger().isLoggable(PlatformLogger.Level.INFO)) {
                        String string = "TableView items list is a SortedList, but the SortedList comparator should be bound to the TableView comparator for sorting to be enabled (e.g. sortedList.comparatorProperty().bind(tableView.comparatorProperty());).";
                        Logging.getControlsLogger().info(string);
                    }
                    return bl;
                }
                if (observableList == null || observableList.isEmpty()) {
                    return true;
                }
                Comparator comparator = tableView.getComparator();
                if (comparator == null) {
                    return true;
                }
                FXCollections.sort(observableList, comparator);
                return true;
            }
            catch (UnsupportedOperationException unsupportedOperationException) {
                return false;
            }
        }
    };
    private final ObservableList<TableColumn<S, ?>> columns = FXCollections.observableArrayList();
    private final ObservableList<TableColumn<S, ?>> visibleLeafColumns = FXCollections.observableArrayList();
    private final ObservableList<TableColumn<S, ?>> unmodifiableVisibleLeafColumns = FXCollections.unmodifiableObservableList(this.visibleLeafColumns);
    private ObservableList<TableColumn<S, ?>> sortOrder = FXCollections.observableArrayList();
    private double contentWidth;
    private boolean isInited = false;
    private final ListChangeListener<TableColumn<S, ?>> columnsObserver = new ListChangeListener<TableColumn<S, ?>>(){

        @Override
        public void onChanged(ListChangeListener.Change<? extends TableColumn<S, ?>> change) {
            Object object3;
            Object object2;
            ArrayList arrayList2;
            ObservableList observableList = TableView.this.getColumns();
            while (change.next()) {
                if (!change.wasAdded()) continue;
                arrayList2 = new ArrayList();
                for (TableColumn object32 : change.getAddedSubList()) {
                    if (object32 == null) continue;
                    int arrayList3 = 0;
                    for (TableColumn tableColumn : observableList) {
                        if (object32 != tableColumn) continue;
                        ++arrayList3;
                    }
                    if (arrayList3 <= true) continue;
                    arrayList2.add(object32);
                }
                if (arrayList2.isEmpty()) continue;
                object2 = "";
                for (TableColumn tableColumn : arrayList2) {
                    object2 = object2 + "'" + tableColumn.getText() + "', ";
                }
                throw new IllegalStateException("Duplicate TableColumns detected in TableView columns list with titles " + object2);
            }
            change.reset();
            arrayList2 = new ArrayList();
            while (change.next()) {
                object2 = change.getRemoved();
                List list = change.getAddedSubList();
                if (change.wasRemoved()) {
                    arrayList2.addAll(object2);
                    Iterator iterator = object2.iterator();
                    while (iterator.hasNext()) {
                        object3 = (TableColumn)iterator.next();
                        ((TableColumn)object3).setTableView(null);
                    }
                }
                if (change.wasAdded()) {
                    arrayList2.removeAll(list);
                    for (Object object3 : list) {
                        ((TableColumn)object3).setTableView(TableView.this);
                    }
                }
                TableUtil.removeColumnsListener(object2, TableView.this.weakColumnsObserver);
                TableUtil.addColumnsListener(list, TableView.this.weakColumnsObserver);
                TableUtil.removeTableColumnListener(change.getRemoved(), TableView.this.weakColumnVisibleObserver, TableView.this.weakColumnSortableObserver, TableView.this.weakColumnSortTypeObserver, TableView.this.weakColumnComparatorObserver);
                TableUtil.addTableColumnListener(change.getAddedSubList(), TableView.this.weakColumnVisibleObserver, TableView.this.weakColumnSortableObserver, TableView.this.weakColumnSortTypeObserver, TableView.this.weakColumnComparatorObserver);
            }
            TableView.this.updateVisibleLeafColumns();
            TableView.this.sortOrder.removeAll(arrayList2);
            object2 = TableView.this.getFocusModel();
            TableViewSelectionModel tableViewSelectionModel = TableView.this.getSelectionModel();
            change.reset();
            ArrayList arrayList = new ArrayList();
            object3 = new ArrayList();
            while (change.next()) {
                if (change.wasRemoved()) {
                    arrayList.addAll(change.getRemoved());
                }
                if (!change.wasAdded()) continue;
                object3.addAll(change.getAddedSubList());
            }
            arrayList.removeAll((Collection<?>)object3);
            if (object2 != null) {
                boolean bl;
                TablePosition tablePosition = ((TableViewFocusModel)object2).getFocusedCell();
                boolean bl2 = false;
                for (TableColumn tableColumn : arrayList) {
                    bl = tablePosition != null && tablePosition.getTableColumn() == tableColumn;
                    if (!bl) continue;
                    break;
                }
                if (bl) {
                    int n = TableView.this.lastKnownColumnIndex.getOrDefault(tablePosition.getTableColumn(), 0);
                    int n2 = n == 0 ? 0 : Math.min(TableView.this.getVisibleLeafColumns().size() - 1, n - 1);
                    ((TableViewFocusModel)object2).focus(tablePosition.getRow(), TableView.this.getVisibleLeafColumn(n2));
                }
            }
            if (tableViewSelectionModel != null) {
                ArrayList<TablePosition> arrayList3 = new ArrayList<TablePosition>(tableViewSelectionModel.getSelectedCells());
                for (Object object4 : arrayList3) {
                    int n;
                    boolean bl;
                    boolean bl3 = false;
                    for (TableColumn tableColumn : arrayList) {
                        bl = object4 != null && ((TablePosition)object4).getTableColumn() == tableColumn;
                        if (!bl) continue;
                        break;
                    }
                    if (!bl || (n = TableView.this.lastKnownColumnIndex.getOrDefault(((TablePosition)object4).getTableColumn(), -1).intValue()) == -1) continue;
                    if (tableViewSelectionModel instanceof TableViewArrayListSelectionModel) {
                        TablePosition tablePosition = new TablePosition(TableView.this, ((TablePositionBase)object4).getRow(), ((TablePosition)object4).getTableColumn());
                        tablePosition.fixedColumnIndex = n;
                        ((TableViewArrayListSelectionModel)tableViewSelectionModel).clearSelection(tablePosition);
                        continue;
                    }
                    tableViewSelectionModel.clearSelection(((TablePositionBase)object4).getRow(), ((TablePosition)object4).getTableColumn());
                }
            }
            TableView.this.lastKnownColumnIndex.clear();
            for (TableColumn tableColumn : TableView.this.getColumns()) {
                int n = TableView.this.getVisibleLeafIndex(tableColumn);
                if (n <= -1) continue;
                TableView.this.lastKnownColumnIndex.put(tableColumn, n);
            }
        }
    };
    private final WeakHashMap<TableColumn<S, ?>, Integer> lastKnownColumnIndex = new WeakHashMap();
    private final InvalidationListener columnVisibleObserver = observable -> this.updateVisibleLeafColumns();
    private final InvalidationListener columnSortableObserver = observable -> {
        Object object = ((Property)observable).getBean();
        if (!this.getSortOrder().contains(object)) {
            return;
        }
        this.doSort(TableUtil.SortEventType.COLUMN_SORTABLE_CHANGE, object);
    };
    private final InvalidationListener columnSortTypeObserver = observable -> {
        Object object = ((Property)observable).getBean();
        if (!this.getSortOrder().contains(object)) {
            return;
        }
        this.doSort(TableUtil.SortEventType.COLUMN_SORT_TYPE_CHANGE, object);
    };
    private final InvalidationListener columnComparatorObserver = observable -> {
        Object object = ((Property)observable).getBean();
        if (!this.getSortOrder().contains(object)) {
            return;
        }
        this.doSort(TableUtil.SortEventType.COLUMN_COMPARATOR_CHANGE, object);
    };
    private final InvalidationListener cellSelectionModelInvalidationListener = observable -> {
        boolean bl = ((BooleanProperty)observable).get();
        this.pseudoClassStateChanged(PSEUDO_CLASS_CELL_SELECTION, bl);
        this.pseudoClassStateChanged(PSEUDO_CLASS_ROW_SELECTION, !bl);
    };
    private final WeakInvalidationListener weakColumnVisibleObserver = new WeakInvalidationListener(this.columnVisibleObserver);
    private final WeakInvalidationListener weakColumnSortableObserver = new WeakInvalidationListener(this.columnSortableObserver);
    private final WeakInvalidationListener weakColumnSortTypeObserver = new WeakInvalidationListener(this.columnSortTypeObserver);
    private final WeakInvalidationListener weakColumnComparatorObserver = new WeakInvalidationListener(this.columnComparatorObserver);
    private final WeakListChangeListener<TableColumn<S, ?>> weakColumnsObserver = new WeakListChangeListener(this.columnsObserver);
    private final WeakInvalidationListener weakCellSelectionModelInvalidationListener = new WeakInvalidationListener(this.cellSelectionModelInvalidationListener);
    private ObjectProperty<ObservableList<S>> items = new SimpleObjectProperty<ObservableList<S>>(this, "items"){
        WeakReference<ObservableList<S>> oldItemsRef;

        @Override
        protected void invalidated() {
            ObservableList observableList = this.oldItemsRef == null ? null : (ObservableList)this.oldItemsRef.get();
            ObservableList observableList2 = TableView.this.getItems();
            if (observableList2 != null && observableList2 == observableList) {
                return;
            }
            if (!(observableList2 instanceof SortedList)) {
                TableView.this.getSortOrder().clear();
            }
            this.oldItemsRef = new WeakReference(observableList2);
        }
    };
    private BooleanProperty tableMenuButtonVisible;
    private ObjectProperty<Callback<ResizeFeatures, Boolean>> columnResizePolicy;
    private ObjectProperty<Callback<TableView<S>, TableRow<S>>> rowFactory;
    private ObjectProperty<Node> placeholder;
    private ObjectProperty<TableViewSelectionModel<S>> selectionModel = new SimpleObjectProperty<TableViewSelectionModel<S>>(this, "selectionModel"){
        TableViewSelectionModel<S> oldValue;
        {
            this.oldValue = null;
        }

        @Override
        protected void invalidated() {
            if (this.oldValue != null) {
                this.oldValue.cellSelectionEnabledProperty().removeListener(TableView.this.weakCellSelectionModelInvalidationListener);
                if (this.oldValue instanceof TableViewArrayListSelectionModel) {
                    ((TableViewArrayListSelectionModel)this.oldValue).dispose();
                }
            }
            this.oldValue = (TableViewSelectionModel)this.get();
            if (this.oldValue != null) {
                this.oldValue.cellSelectionEnabledProperty().addListener(TableView.this.weakCellSelectionModelInvalidationListener);
                TableView.this.weakCellSelectionModelInvalidationListener.invalidated(this.oldValue.cellSelectionEnabledProperty());
            }
        }
    };
    private ObjectProperty<TableViewFocusModel<S>> focusModel;
    private BooleanProperty editable;
    private DoubleProperty fixedCellSize;
    private ReadOnlyObjectWrapper<TablePosition<S, ?>> editingCell;
    private ReadOnlyObjectWrapper<Comparator<S>> comparator;
    private ObjectProperty<Callback<TableView<S>, Boolean>> sortPolicy;
    private ObjectProperty<EventHandler<SortEvent<TableView<S>>>> onSort;
    private ObjectProperty<EventHandler<ScrollToEvent<Integer>>> onScrollTo;
    private ObjectProperty<EventHandler<ScrollToEvent<TableColumn<S, ?>>>> onScrollToColumn;
    private boolean sortLock = false;
    private TableUtil.SortEventType lastSortEventType = null;
    private Object[] lastSortEventSupportInfo = null;
    private static final String DEFAULT_STYLE_CLASS = "table-view";
    private static final PseudoClass PSEUDO_CLASS_CELL_SELECTION = PseudoClass.getPseudoClass("cell-selection");
    private static final PseudoClass PSEUDO_CLASS_ROW_SELECTION = PseudoClass.getPseudoClass("row-selection");

    public TableView() {
        this(FXCollections.observableArrayList());
    }

    public TableView(ObservableList<S> observableList) {
        this.getStyleClass().setAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.setAccessibleRole(AccessibleRole.TABLE_VIEW);
        this.setItems(observableList);
        this.setSelectionModel(new TableViewArrayListSelectionModel(this));
        this.setFocusModel(new TableViewFocusModel(this));
        this.getColumns().addListener(this.weakColumnsObserver);
        this.getSortOrder().addListener(change -> this.doSort(TableUtil.SortEventType.SORT_ORDER_CHANGE, change));
        this.getProperties().addListener(new MapChangeListener<Object, Object>(){

            @Override
            public void onChanged(MapChangeListener.Change<? extends Object, ? extends Object> change) {
                if (change.wasAdded() && TableView.SET_CONTENT_WIDTH.equals(change.getKey())) {
                    if (change.getValueAdded() instanceof Number) {
                        TableView.this.setContentWidth((Double)change.getValueAdded());
                    }
                    TableView.this.getProperties().remove(TableView.SET_CONTENT_WIDTH);
                }
            }
        });
        this.isInited = true;
    }

    public final ObjectProperty<ObservableList<S>> itemsProperty() {
        return this.items;
    }

    public final void setItems(ObservableList<S> observableList) {
        this.itemsProperty().set(observableList);
    }

    public final ObservableList<S> getItems() {
        return (ObservableList)this.items.get();
    }

    public final BooleanProperty tableMenuButtonVisibleProperty() {
        if (this.tableMenuButtonVisible == null) {
            this.tableMenuButtonVisible = new SimpleBooleanProperty(this, "tableMenuButtonVisible");
        }
        return this.tableMenuButtonVisible;
    }

    public final void setTableMenuButtonVisible(boolean bl) {
        this.tableMenuButtonVisibleProperty().set(bl);
    }

    public final boolean isTableMenuButtonVisible() {
        return this.tableMenuButtonVisible == null ? false : this.tableMenuButtonVisible.get();
    }

    public final void setColumnResizePolicy(Callback<ResizeFeatures, Boolean> callback) {
        this.columnResizePolicyProperty().set(callback);
    }

    public final Callback<ResizeFeatures, Boolean> getColumnResizePolicy() {
        return this.columnResizePolicy == null ? UNCONSTRAINED_RESIZE_POLICY : (Callback)this.columnResizePolicy.get();
    }

    public final ObjectProperty<Callback<ResizeFeatures, Boolean>> columnResizePolicyProperty() {
        if (this.columnResizePolicy == null) {
            this.columnResizePolicy = new SimpleObjectProperty<Callback<ResizeFeatures, Boolean>>((Object)this, "columnResizePolicy", UNCONSTRAINED_RESIZE_POLICY){
                private Callback<ResizeFeatures, Boolean> oldPolicy;

                @Override
                protected void invalidated() {
                    if (TableView.this.isInited) {
                        PseudoClass pseudoClass;
                        ((Callback)this.get()).call(new ResizeFeatures(TableView.this, null, 0.0));
                        if (this.oldPolicy != null) {
                            pseudoClass = PseudoClass.getPseudoClass(this.oldPolicy.toString());
                            TableView.this.pseudoClassStateChanged(pseudoClass, false);
                        }
                        if (this.get() != null) {
                            pseudoClass = PseudoClass.getPseudoClass(((Callback)this.get()).toString());
                            TableView.this.pseudoClassStateChanged(pseudoClass, true);
                        }
                        this.oldPolicy = (Callback)this.get();
                    }
                }
            };
        }
        return this.columnResizePolicy;
    }

    public final ObjectProperty<Callback<TableView<S>, TableRow<S>>> rowFactoryProperty() {
        if (this.rowFactory == null) {
            this.rowFactory = new SimpleObjectProperty<Callback<TableView<S>, TableRow<S>>>(this, "rowFactory");
        }
        return this.rowFactory;
    }

    public final void setRowFactory(Callback<TableView<S>, TableRow<S>> callback) {
        this.rowFactoryProperty().set(callback);
    }

    public final Callback<TableView<S>, TableRow<S>> getRowFactory() {
        return this.rowFactory == null ? null : (Callback)this.rowFactory.get();
    }

    public final ObjectProperty<Node> placeholderProperty() {
        if (this.placeholder == null) {
            this.placeholder = new SimpleObjectProperty<Node>(this, "placeholder");
        }
        return this.placeholder;
    }

    public final void setPlaceholder(Node node) {
        this.placeholderProperty().set(node);
    }

    public final Node getPlaceholder() {
        return this.placeholder == null ? null : (Node)this.placeholder.get();
    }

    public final ObjectProperty<TableViewSelectionModel<S>> selectionModelProperty() {
        return this.selectionModel;
    }

    public final void setSelectionModel(TableViewSelectionModel<S> tableViewSelectionModel) {
        this.selectionModelProperty().set(tableViewSelectionModel);
    }

    public final TableViewSelectionModel<S> getSelectionModel() {
        return (TableViewSelectionModel)this.selectionModel.get();
    }

    public final void setFocusModel(TableViewFocusModel<S> tableViewFocusModel) {
        this.focusModelProperty().set(tableViewFocusModel);
    }

    public final TableViewFocusModel<S> getFocusModel() {
        return this.focusModel == null ? null : (TableViewFocusModel)this.focusModel.get();
    }

    public final ObjectProperty<TableViewFocusModel<S>> focusModelProperty() {
        if (this.focusModel == null) {
            this.focusModel = new SimpleObjectProperty<TableViewFocusModel<S>>(this, "focusModel");
        }
        return this.focusModel;
    }

    public final void setEditable(boolean bl) {
        this.editableProperty().set(bl);
    }

    public final boolean isEditable() {
        return this.editable == null ? false : this.editable.get();
    }

    public final BooleanProperty editableProperty() {
        if (this.editable == null) {
            this.editable = new SimpleBooleanProperty(this, "editable", false);
        }
        return this.editable;
    }

    public final void setFixedCellSize(double d) {
        this.fixedCellSizeProperty().set(d);
    }

    public final double getFixedCellSize() {
        return this.fixedCellSize == null ? -1.0 : this.fixedCellSize.get();
    }

    public final DoubleProperty fixedCellSizeProperty() {
        if (this.fixedCellSize == null) {
            this.fixedCellSize = new StyleableDoubleProperty(-1.0){

                @Override
                public CssMetaData<TableView<?>, Number> getCssMetaData() {
                    return StyleableProperties.FIXED_CELL_SIZE;
                }

                @Override
                public Object getBean() {
                    return TableView.this;
                }

                @Override
                public String getName() {
                    return "fixedCellSize";
                }
            };
        }
        return this.fixedCellSize;
    }

    private void setEditingCell(TablePosition<S, ?> tablePosition) {
        this.editingCellPropertyImpl().set(tablePosition);
    }

    public final TablePosition<S, ?> getEditingCell() {
        return this.editingCell == null ? null : (TablePosition)this.editingCell.get();
    }

    public final ReadOnlyObjectProperty<TablePosition<S, ?>> editingCellProperty() {
        return this.editingCellPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<TablePosition<S, ?>> editingCellPropertyImpl() {
        if (this.editingCell == null) {
            this.editingCell = new ReadOnlyObjectWrapper(this, "editingCell");
        }
        return this.editingCell;
    }

    private void setComparator(Comparator<S> comparator) {
        this.comparatorPropertyImpl().set(comparator);
    }

    public final Comparator<S> getComparator() {
        return this.comparator == null ? null : (Comparator)this.comparator.get();
    }

    public final ReadOnlyObjectProperty<Comparator<S>> comparatorProperty() {
        return this.comparatorPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<Comparator<S>> comparatorPropertyImpl() {
        if (this.comparator == null) {
            this.comparator = new ReadOnlyObjectWrapper(this, "comparator");
        }
        return this.comparator;
    }

    public final void setSortPolicy(Callback<TableView<S>, Boolean> callback) {
        this.sortPolicyProperty().set(callback);
    }

    public final Callback<TableView<S>, Boolean> getSortPolicy() {
        return this.sortPolicy == null ? DEFAULT_SORT_POLICY : (Callback)this.sortPolicy.get();
    }

    public final ObjectProperty<Callback<TableView<S>, Boolean>> sortPolicyProperty() {
        if (this.sortPolicy == null) {
            this.sortPolicy = new SimpleObjectProperty<Callback<TableView<S>, Boolean>>(this, "sortPolicy", DEFAULT_SORT_POLICY){

                @Override
                protected void invalidated() {
                    TableView.this.sort();
                }
            };
        }
        return this.sortPolicy;
    }

    public void setOnSort(EventHandler<SortEvent<TableView<S>>> eventHandler) {
        this.onSortProperty().set(eventHandler);
    }

    public EventHandler<SortEvent<TableView<S>>> getOnSort() {
        if (this.onSort != null) {
            return (EventHandler)this.onSort.get();
        }
        return null;
    }

    public ObjectProperty<EventHandler<SortEvent<TableView<S>>>> onSortProperty() {
        if (this.onSort == null) {
            this.onSort = new ObjectPropertyBase<EventHandler<SortEvent<TableView<S>>>>(){

                @Override
                protected void invalidated() {
                    EventType eventType = SortEvent.sortEvent();
                    EventHandler eventHandler = (EventHandler)this.get();
                    TableView.this.setEventHandler(eventType, eventHandler);
                }

                @Override
                public Object getBean() {
                    return TableView.this;
                }

                @Override
                public String getName() {
                    return "onSort";
                }
            };
        }
        return this.onSort;
    }

    public final ObservableList<TableColumn<S, ?>> getColumns() {
        return this.columns;
    }

    public final ObservableList<TableColumn<S, ?>> getSortOrder() {
        return this.sortOrder;
    }

    public void scrollTo(int n) {
        ControlUtils.scrollToIndex(this, n);
    }

    public void scrollTo(S s) {
        int n;
        if (this.getItems() != null && (n = this.getItems().indexOf(s)) >= 0) {
            ControlUtils.scrollToIndex(this, n);
        }
    }

    public void setOnScrollTo(EventHandler<ScrollToEvent<Integer>> eventHandler) {
        this.onScrollToProperty().set(eventHandler);
    }

    public EventHandler<ScrollToEvent<Integer>> getOnScrollTo() {
        if (this.onScrollTo != null) {
            return (EventHandler)this.onScrollTo.get();
        }
        return null;
    }

    public ObjectProperty<EventHandler<ScrollToEvent<Integer>>> onScrollToProperty() {
        if (this.onScrollTo == null) {
            this.onScrollTo = new ObjectPropertyBase<EventHandler<ScrollToEvent<Integer>>>(){

                @Override
                protected void invalidated() {
                    TableView.this.setEventHandler(ScrollToEvent.scrollToTopIndex(), (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return TableView.this;
                }

                @Override
                public String getName() {
                    return "onScrollTo";
                }
            };
        }
        return this.onScrollTo;
    }

    public void scrollToColumn(TableColumn<S, ?> tableColumn) {
        ControlUtils.scrollToColumn(this, tableColumn);
    }

    public void scrollToColumnIndex(int n) {
        if (this.getColumns() != null) {
            ControlUtils.scrollToColumn(this, (TableColumnBase)this.getColumns().get(n));
        }
    }

    public void setOnScrollToColumn(EventHandler<ScrollToEvent<TableColumn<S, ?>>> eventHandler) {
        this.onScrollToColumnProperty().set(eventHandler);
    }

    public EventHandler<ScrollToEvent<TableColumn<S, ?>>> getOnScrollToColumn() {
        if (this.onScrollToColumn != null) {
            return (EventHandler)this.onScrollToColumn.get();
        }
        return null;
    }

    public ObjectProperty<EventHandler<ScrollToEvent<TableColumn<S, ?>>>> onScrollToColumnProperty() {
        if (this.onScrollToColumn == null) {
            this.onScrollToColumn = new ObjectPropertyBase<EventHandler<ScrollToEvent<TableColumn<S, ?>>>>(){

                @Override
                protected void invalidated() {
                    EventType eventType = ScrollToEvent.scrollToColumn();
                    TableView.this.setEventHandler(eventType, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return TableView.this;
                }

                @Override
                public String getName() {
                    return "onScrollToColumn";
                }
            };
        }
        return this.onScrollToColumn;
    }

    public boolean resizeColumn(TableColumn<S, ?> tableColumn, double d) {
        if (tableColumn == null || Double.compare(d, 0.0) == 0) {
            return false;
        }
        boolean bl = this.getColumnResizePolicy().call(new ResizeFeatures<S>(this, tableColumn, d));
        return bl;
    }

    public void edit(int n, TableColumn<S, ?> tableColumn) {
        if (!this.isEditable() || tableColumn != null && !tableColumn.isEditable()) {
            return;
        }
        if (n < 0 && tableColumn == null) {
            this.setEditingCell(null);
        } else {
            this.setEditingCell(new TablePosition(this, n, tableColumn));
        }
    }

    public ObservableList<TableColumn<S, ?>> getVisibleLeafColumns() {
        return this.unmodifiableVisibleLeafColumns;
    }

    public int getVisibleLeafIndex(TableColumn<S, ?> tableColumn) {
        return this.visibleLeafColumns.indexOf(tableColumn);
    }

    public TableColumn<S, ?> getVisibleLeafColumn(int n) {
        if (n < 0 || n >= this.visibleLeafColumns.size()) {
            return null;
        }
        return (TableColumn)this.visibleLeafColumns.get(n);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TableViewSkin(this);
    }

    public void sort() {
        ObservableList observableList = this.getSortOrder();
        Comparator<S> comparator = this.getComparator();
        this.setComparator(observableList.isEmpty() ? null : new TableColumnComparatorBase.TableColumnComparator(observableList));
        SortEvent<TableView> sortEvent = new SortEvent<TableView>(this, this);
        this.fireEvent(sortEvent);
        if (sortEvent.isConsumed()) {
            return;
        }
        ArrayList<TablePosition> arrayList = new ArrayList<TablePosition>(this.getSelectionModel().getSelectedCells());
        int n = arrayList.size();
        this.getSelectionModel().startAtomic();
        Callback<TableView<S>, Boolean> callback = this.getSortPolicy();
        if (callback == null) {
            return;
        }
        Boolean bl = callback.call(this);
        this.getSelectionModel().stopAtomic();
        if (bl == null || !bl.booleanValue()) {
            this.sortLock = true;
            TableUtil.handleSortFailure(observableList, this.lastSortEventType, this.lastSortEventSupportInfo);
            this.setComparator(comparator);
            this.sortLock = false;
        } else if (this.getSelectionModel() instanceof TableViewArrayListSelectionModel) {
            TableViewArrayListSelectionModel tableViewArrayListSelectionModel = (TableViewArrayListSelectionModel)this.getSelectionModel();
            ObservableList<TablePosition> observableList2 = tableViewArrayListSelectionModel.getSelectedCells();
            ArrayList<TablePosition> arrayList2 = new ArrayList<TablePosition>();
            for (int i = 0; i < n; ++i) {
                TablePosition tablePosition = (TablePosition)arrayList.get(i);
                if (observableList2.contains(tablePosition)) continue;
                arrayList2.add(tablePosition);
            }
            if (!arrayList2.isEmpty()) {
                NonIterableChange.GenericAddRemoveChange<TablePosition> genericAddRemoveChange = new NonIterableChange.GenericAddRemoveChange<TablePosition>(0, n, arrayList2, observableList2);
                tableViewArrayListSelectionModel.fireCustomSelectedCellsListChangeEvent(genericAddRemoveChange);
            }
        }
    }

    public void refresh() {
        this.getProperties().put("recreateKey", Boolean.TRUE);
    }

    private void doSort(TableUtil.SortEventType sortEventType, Object ... objectArray) {
        if (this.sortLock) {
            return;
        }
        this.lastSortEventType = sortEventType;
        this.lastSortEventSupportInfo = objectArray;
        this.sort();
        this.lastSortEventType = null;
        this.lastSortEventSupportInfo = null;
    }

    private void setContentWidth(double d) {
        this.contentWidth = d;
        if (this.isInited) {
            this.getColumnResizePolicy().call(new ResizeFeatures(this, null, 0.0));
        }
    }

    private void updateVisibleLeafColumns() {
        ArrayList arrayList = new ArrayList();
        this.buildVisibleLeafColumns(this.getColumns(), arrayList);
        this.visibleLeafColumns.setAll(arrayList);
        this.getColumnResizePolicy().call(new ResizeFeatures(this, null, 0.0));
    }

    private void buildVisibleLeafColumns(List<TableColumn<S, ?>> list, List<TableColumn<S, ?>> list2) {
        for (TableColumn<S, ?> tableColumn : list) {
            boolean bl;
            if (tableColumn == null) continue;
            boolean bl2 = bl = !tableColumn.getColumns().isEmpty();
            if (bl) {
                this.buildVisibleLeafColumns(tableColumn.getColumns(), list2);
                continue;
            }
            if (!tableColumn.isVisible()) continue;
            list2.add(tableColumn);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return TableView.getClassCssMetaData();
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case COLUMN_COUNT: {
                return this.getVisibleLeafColumns().size();
            }
            case ROW_COUNT: {
                return this.getItems().size();
            }
            case SELECTED_ITEMS: {
                ObservableList observableList = (ObservableList)super.queryAccessibleAttribute(accessibleAttribute, objectArray);
                ArrayList arrayList = new ArrayList();
                for (TableRow tableRow : observableList) {
                    ObservableList observableList2 = (ObservableList)tableRow.queryAccessibleAttribute(accessibleAttribute, objectArray);
                    if (observableList2 == null) continue;
                    arrayList.addAll(observableList2);
                }
                return FXCollections.observableArrayList(arrayList);
            }
            case FOCUS_ITEM: {
                Node node = (Node)super.queryAccessibleAttribute(accessibleAttribute, objectArray);
                if (node == null) {
                    return null;
                }
                Node node2 = (Node)node.queryAccessibleAttribute(accessibleAttribute, objectArray);
                return node2 != null ? node2 : node;
            }
            case CELL_AT_ROW_COLUMN: {
                TableRow tableRow = (TableRow)super.queryAccessibleAttribute(accessibleAttribute, objectArray);
                return tableRow != null ? tableRow.queryAccessibleAttribute(accessibleAttribute, objectArray) : null;
            }
            case MULTIPLE_SELECTION: {
                TableViewSelectionModel<S> tableViewSelectionModel = this.getSelectionModel();
                return tableViewSelectionModel != null && tableViewSelectionModel.getSelectionMode() == SelectionMode.MULTIPLE;
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    public static class TableViewFocusModel<S>
    extends TableFocusModel<S, TableColumn<S, ?>> {
        private final TableView<S> tableView;
        private final TablePosition<S, ?> EMPTY_CELL;
        private final InvalidationListener itemsObserver;
        private final ListChangeListener<S> itemsContentListener = change -> {
            change.next();
            if (change.wasReplaced() || change.getAddedSize() == this.getItemCount()) {
                this.updateDefaultFocus();
                return;
            }
            TablePosition tablePosition = this.getFocusedCell();
            int n = tablePosition.getRow();
            if (n == -1 || change.getFrom() > n) {
                return;
            }
            change.reset();
            boolean bl = false;
            boolean bl2 = false;
            int n2 = 0;
            int n3 = 0;
            while (change.next()) {
                bl |= change.wasAdded();
                bl2 |= change.wasRemoved();
                n2 += change.getAddedSize();
                n3 += change.getRemovedSize();
            }
            if (bl && !bl2) {
                if (n2 < change.getList().size()) {
                    int n4 = Math.min(this.getItemCount() - 1, this.getFocusedIndex() + n2);
                    this.focus(n4, (TableColumn<S, ?>)tablePosition.getTableColumn());
                }
            } else if (!bl && bl2) {
                int n5 = Math.max(0, this.getFocusedIndex() - n3);
                if (n5 < 0) {
                    this.focus(0, (TableColumn<S, ?>)tablePosition.getTableColumn());
                } else {
                    this.focus(n5, (TableColumn<S, ?>)tablePosition.getTableColumn());
                }
            }
        };
        private WeakListChangeListener<S> weakItemsContentListener = new WeakListChangeListener<S>(this.itemsContentListener);
        private ReadOnlyObjectWrapper<TablePosition> focusedCell;

        public TableViewFocusModel(final TableView<S> tableView) {
            if (tableView == null) {
                throw new NullPointerException("TableView can not be null");
            }
            this.tableView = tableView;
            this.EMPTY_CELL = new TablePosition(tableView, -1, null);
            this.itemsObserver = new InvalidationListener(){
                private WeakReference<ObservableList<S>> weakItemsRef;
                {
                    this.weakItemsRef = new WeakReference(tableView.getItems());
                }

                @Override
                public void invalidated(Observable observable) {
                    ObservableList observableList = (ObservableList)this.weakItemsRef.get();
                    this.weakItemsRef = new WeakReference(tableView.getItems());
                    this.updateItemsObserver(observableList, tableView.getItems());
                }
            };
            this.tableView.itemsProperty().addListener(new WeakInvalidationListener(this.itemsObserver));
            if (tableView.getItems() != null) {
                this.tableView.getItems().addListener(this.weakItemsContentListener);
            }
            this.updateDefaultFocus();
            this.focusedCellProperty().addListener(observable -> tableView.notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_ITEM));
        }

        private void updateItemsObserver(ObservableList<S> observableList, ObservableList<S> observableList2) {
            if (observableList != null) {
                observableList.removeListener(this.weakItemsContentListener);
            }
            if (observableList2 != null) {
                observableList2.addListener(this.weakItemsContentListener);
            }
            this.updateDefaultFocus();
        }

        @Override
        protected int getItemCount() {
            if (this.tableView.getItems() == null) {
                return -1;
            }
            return this.tableView.getItems().size();
        }

        @Override
        protected S getModelItem(int n) {
            if (this.tableView.getItems() == null) {
                return null;
            }
            if (n < 0 || n >= this.getItemCount()) {
                return null;
            }
            return (S)this.tableView.getItems().get(n);
        }

        public final ReadOnlyObjectProperty<TablePosition> focusedCellProperty() {
            return this.focusedCellPropertyImpl().getReadOnlyProperty();
        }

        private void setFocusedCell(TablePosition tablePosition) {
            this.focusedCellPropertyImpl().set(tablePosition);
        }

        public final TablePosition getFocusedCell() {
            return this.focusedCell == null ? this.EMPTY_CELL : (TablePosition)this.focusedCell.get();
        }

        private ReadOnlyObjectWrapper<TablePosition> focusedCellPropertyImpl() {
            if (this.focusedCell == null) {
                this.focusedCell = new ReadOnlyObjectWrapper<TablePosition>(this.EMPTY_CELL){
                    private TablePosition old;

                    @Override
                    protected void invalidated() {
                        if (this.get() == null) {
                            return;
                        }
                        if (this.old == null || !this.old.equals(this.get())) {
                            this.setFocusedIndex(((TablePosition)this.get()).getRow());
                            this.setFocusedItem(this.getModelItem(((TablePosition)this.getValue()).getRow()));
                            this.old = (TablePosition)this.get();
                        }
                    }

                    @Override
                    public Object getBean() {
                        return this;
                    }

                    @Override
                    public String getName() {
                        return "focusedCell";
                    }
                };
            }
            return this.focusedCell;
        }

        @Override
        public void focus(int n, TableColumn<S, ?> tableColumn) {
            if (n < 0 || n >= this.getItemCount()) {
                this.setFocusedCell(this.EMPTY_CELL);
            } else {
                TablePosition tablePosition = this.getFocusedCell();
                TablePosition tablePosition2 = new TablePosition(this.tableView, n, tableColumn);
                this.setFocusedCell(tablePosition2);
                if (tablePosition2.equals(tablePosition)) {
                    this.setFocusedIndex(n);
                    this.setFocusedItem(this.getModelItem(n));
                }
            }
        }

        public void focus(TablePosition tablePosition) {
            if (tablePosition == null) {
                return;
            }
            this.focus(tablePosition.getRow(), (TableColumn<S, ?>)tablePosition.getTableColumn());
        }

        @Override
        public boolean isFocused(int n, TableColumn<S, ?> tableColumn) {
            if (n < 0 || n >= this.getItemCount()) {
                return false;
            }
            TablePosition tablePosition = this.getFocusedCell();
            boolean bl = tableColumn == null || tableColumn.equals(tablePosition.getTableColumn());
            return tablePosition.getRow() == n && bl;
        }

        @Override
        public void focus(int n) {
            if (n < 0 || n >= this.getItemCount()) {
                this.setFocusedCell(this.EMPTY_CELL);
            } else {
                this.setFocusedCell(new TablePosition(this.tableView, n, null));
            }
        }

        @Override
        public void focusAboveCell() {
            TablePosition tablePosition = this.getFocusedCell();
            if (this.getFocusedIndex() == -1) {
                this.focus(this.getItemCount() - 1, (TableColumn<S, ?>)tablePosition.getTableColumn());
            } else if (this.getFocusedIndex() > 0) {
                this.focus(this.getFocusedIndex() - 1, (TableColumn<S, ?>)tablePosition.getTableColumn());
            }
        }

        @Override
        public void focusBelowCell() {
            TablePosition tablePosition = this.getFocusedCell();
            if (this.getFocusedIndex() == -1) {
                this.focus(0, (TableColumn<S, ?>)tablePosition.getTableColumn());
            } else if (this.getFocusedIndex() != this.getItemCount() - 1) {
                this.focus(this.getFocusedIndex() + 1, (TableColumn<S, ?>)tablePosition.getTableColumn());
            }
        }

        @Override
        public void focusLeftCell() {
            TablePosition tablePosition = this.getFocusedCell();
            if (tablePosition.getColumn() <= 0) {
                return;
            }
            this.focus(tablePosition.getRow(), this.getTableColumn((TableColumn<S, ?>)tablePosition.getTableColumn(), -1));
        }

        @Override
        public void focusRightCell() {
            TablePosition tablePosition = this.getFocusedCell();
            if (tablePosition.getColumn() == this.getColumnCount() - 1) {
                return;
            }
            this.focus(tablePosition.getRow(), this.getTableColumn((TableColumn<S, ?>)tablePosition.getTableColumn(), 1));
        }

        @Override
        public void focusPrevious() {
            if (this.getFocusedIndex() == -1) {
                this.focus(0);
            } else if (this.getFocusedIndex() > 0) {
                this.focusAboveCell();
            }
        }

        @Override
        public void focusNext() {
            if (this.getFocusedIndex() == -1) {
                this.focus(0);
            } else if (this.getFocusedIndex() != this.getItemCount() - 1) {
                this.focusBelowCell();
            }
        }

        private void updateDefaultFocus() {
            Object object;
            int n = -1;
            if (this.tableView.getItems() != null) {
                object = this.getFocusedItem();
                if (object != null) {
                    n = this.tableView.getItems().indexOf(object);
                }
                if (n == -1) {
                    n = this.tableView.getItems().size() > 0 ? 0 : -1;
                }
            }
            TableColumn<S, ?> tableColumn = (object = this.getFocusedCell()) != null && !this.EMPTY_CELL.equals(object) ? ((TablePosition)object).getTableColumn() : this.tableView.getVisibleLeafColumn(0);
            this.focus(n, tableColumn);
        }

        private int getColumnCount() {
            return this.tableView.getVisibleLeafColumns().size();
        }

        private TableColumn<S, ?> getTableColumn(TableColumn<S, ?> tableColumn, int n) {
            int n2 = this.tableView.getVisibleLeafIndex(tableColumn);
            int n3 = n2 + n;
            return this.tableView.getVisibleLeafColumn(n3);
        }
    }

    static class TableViewArrayListSelectionModel<S>
    extends TableViewSelectionModel<S> {
        private int itemCount = 0;
        private final MappingChange.Map<TablePosition<S, ?>, Integer> cellToIndicesMap = tablePosition -> tablePosition.getRow();
        private final TableView<S> tableView;
        final InvalidationListener itemsPropertyListener;
        final ListChangeListener<S> itemsContentListener = change -> {
            this.updateItemCount();
            List list = this.getTableModel();
            boolean bl = true;
            while (change.next()) {
                Object s;
                int n;
                if (change.wasReplaced() || change.getAddedSize() == this.getItemCount()) {
                    this.selectedItemChange = change;
                    this.updateDefaultSelection();
                    this.selectedItemChange = null;
                    return;
                }
                Object t = this.getSelectedItem();
                int n2 = this.getSelectedIndex();
                if (list == null || list.isEmpty()) {
                    this.clearSelection();
                    continue;
                }
                if (this.getSelectedIndex() == -1 && this.getSelectedItem() != null) {
                    n = list.indexOf(this.getSelectedItem());
                    if (n == -1) continue;
                    this.setSelectedIndex(n);
                    bl = false;
                    continue;
                }
                if (!change.wasRemoved() || change.getRemovedSize() != 1 || change.wasAdded() || t == null || !t.equals(change.getRemoved().get(0)) || this.getSelectedIndex() >= this.getItemCount() || t.equals(s = this.getModelItem(n = n2 == 0 ? 0 : n2 - 1))) continue;
                this.clearAndSelect(n);
            }
            if (bl) {
                this.updateSelection(change);
            }
        };
        final WeakListChangeListener<S> weakItemsContentListener = new WeakListChangeListener<S>(this.itemsContentListener);
        private final SelectedCellsMap<TablePosition<S, ?>> selectedCellsMap;
        private final ReadOnlyUnbackedObservableList<TablePosition<S, ?>> selectedCellsSeq;
        private int previousModelSize = 0;

        public TableViewArrayListSelectionModel(final TableView<S> tableView) {
            super(tableView);
            this.tableView = tableView;
            this.itemsPropertyListener = new InvalidationListener(){
                private WeakReference<ObservableList<S>> weakItemsRef;
                {
                    this.weakItemsRef = new WeakReference(tableView.getItems());
                }

                @Override
                public void invalidated(Observable observable) {
                    ObservableList observableList = (ObservableList)this.weakItemsRef.get();
                    this.weakItemsRef = new WeakReference(tableView.getItems());
                    this.updateItemsObserver(observableList, tableView.getItems());
                    ((SelectedItemsReadOnlyObservableList)this.getSelectedItems()).setItemsList(tableView.getItems());
                }
            };
            this.tableView.itemsProperty().addListener(this.itemsPropertyListener);
            this.selectedCellsMap = new SelectedCellsMap<TablePosition<S, ?>>(this::fireCustomSelectedCellsListChangeEvent){

                @Override
                public boolean isCellSelectionEnabled() {
                    return this.isCellSelectionEnabled();
                }
            };
            this.selectedCellsSeq = new ReadOnlyUnbackedObservableList<TablePosition<S, ?>>(){

                @Override
                public TablePosition<S, ?> get(int n) {
                    return (TablePosition)selectedCellsMap.get(n);
                }

                @Override
                public int size() {
                    return selectedCellsMap.size();
                }
            };
            ObservableList observableList = this.getTableView().getItems();
            if (observableList != null) {
                ((SelectedItemsReadOnlyObservableList)this.getSelectedItems()).setItemsList(observableList);
                observableList.addListener(this.weakItemsContentListener);
            }
            this.updateItemCount();
            this.updateDefaultSelection();
            this.cellSelectionEnabledProperty().addListener(observable -> {
                this.updateDefaultSelection();
                TableCellBehaviorBase.setAnchor(tableView, this.getFocusedCell(), true);
            });
        }

        private void dispose() {
            this.tableView.itemsProperty().removeListener(this.itemsPropertyListener);
            ObservableList observableList = this.getTableView().getItems();
            if (observableList != null) {
                observableList.removeListener(this.weakItemsContentListener);
            }
        }

        @Override
        public ObservableList<TablePosition> getSelectedCells() {
            return this.selectedCellsSeq;
        }

        private void updateSelection(ListChangeListener.Change<? extends S> change) {
            change.reset();
            int n = 0;
            int n2 = -1;
            while (change.next()) {
                int n3;
                int n4;
                if (change.wasReplaced()) {
                    if (change.getList().isEmpty()) {
                        this.clearSelection();
                        continue;
                    }
                    n4 = this.getSelectedIndex();
                    if (this.previousModelSize == change.getRemovedSize()) {
                        this.clearSelection();
                        continue;
                    }
                    if (n4 < this.getItemCount() && n4 >= 0) {
                        this.startAtomic();
                        this.clearSelection(n4);
                        this.stopAtomic();
                        this.select(n4);
                        continue;
                    }
                    this.clearSelection();
                    continue;
                }
                if (change.wasAdded() || change.wasRemoved()) {
                    n2 = change.getFrom();
                    n += change.wasAdded() ? change.getAddedSize() : -change.getRemovedSize();
                    continue;
                }
                if (!change.wasPermutated()) continue;
                this.startAtomic();
                n4 = this.getSelectedIndex();
                int n5 = change.getTo() - change.getFrom();
                HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>(n5);
                for (int i = change.getFrom(); i < change.getTo(); ++i) {
                    hashMap.put(i, change.getPermutation(i));
                }
                ArrayList<TablePosition> arrayList = new ArrayList<TablePosition>(this.getSelectedCells());
                ArrayList arrayList2 = new ArrayList(arrayList.size());
                boolean bl = false;
                for (n3 = 0; n3 < arrayList.size(); ++n3) {
                    TablePosition tablePosition = (TablePosition)arrayList.get(n3);
                    int n6 = tablePosition.getRow();
                    if (!hashMap.containsKey(n6)) continue;
                    int n7 = (Integer)hashMap.get(n6);
                    bl = bl || n7 != n6;
                    arrayList2.add(new TablePosition(tablePosition.getTableView(), n7, tablePosition.getTableColumn()));
                }
                if (bl) {
                    this.quietClearSelection();
                    this.stopAtomic();
                    this.selectedCellsMap.setAll(arrayList2);
                    if (n4 < 0 || n4 >= this.itemCount) continue;
                    n3 = change.getPermutation(n4);
                    this.setSelectedIndex(n3);
                    this.focus(n3);
                    continue;
                }
                this.stopAtomic();
            }
            TablePosition tablePosition = TableCellBehavior.getAnchor(this.tableView, null);
            if (n != 0 && n2 >= 0 && tablePosition != null && (change.wasRemoved() || change.wasAdded()) && this.isSelected(tablePosition.getRow(), (TableColumn<S, ?>)tablePosition.getTableColumn())) {
                TablePosition tablePosition2 = new TablePosition(this.tableView, tablePosition.getRow() + n, tablePosition.getTableColumn());
                TableCellBehavior.setAnchor(this.tableView, tablePosition2, false);
            }
            this.shiftSelection(n2, n, new Callback<MultipleSelectionModelBase.ShiftParams, Void>(){

                @Override
                public Void call(MultipleSelectionModelBase.ShiftParams shiftParams) {
                    this.startAtomic();
                    int n = shiftParams.getClearIndex();
                    int n2 = shiftParams.getSetIndex();
                    TablePosition tablePosition = null;
                    if (n > -1) {
                        for (int i = 0; i < selectedCellsMap.size(); ++i) {
                            TablePosition tablePosition2 = (TablePosition)selectedCellsMap.get(i);
                            if (tablePosition2.getRow() == n) {
                                tablePosition = tablePosition2;
                                selectedCellsMap.remove(tablePosition2);
                                continue;
                            }
                            if (tablePosition2.getRow() != n2 || shiftParams.isSelected()) continue;
                            selectedCellsMap.remove(tablePosition2);
                        }
                    }
                    if (tablePosition != null && shiftParams.isSelected()) {
                        TablePosition tablePosition3 = new TablePosition(tableView, shiftParams.getSetIndex(), tablePosition.getTableColumn());
                        selectedCellsMap.add(tablePosition3);
                    }
                    this.stopAtomic();
                    return null;
                }
            });
            this.previousModelSize = this.getItemCount();
        }

        @Override
        public void clearAndSelect(int n) {
            this.clearAndSelect(n, (TableColumn<S, ?>)null);
        }

        @Override
        public void clearAndSelect(int n, TableColumn<S, ?> tableColumn) {
            ListChangeListener.Change<TablePosition<S, ?>> change;
            if (n < 0 || n >= this.getItemCount()) {
                return;
            }
            TablePosition tablePosition = new TablePosition(this.getTableView(), n, tableColumn);
            boolean bl = this.isCellSelectionEnabled();
            TableCellBehavior.setAnchor(this.tableView, tablePosition, false);
            ArrayList arrayList = new ArrayList(this.selectedCellsMap.getSelectedCells());
            boolean bl2 = this.isSelected(n, tableColumn);
            if (bl2 && arrayList.size() == 1) {
                change = (TablePosition)this.getSelectedCells().get(0);
                if (this.getSelectedItem() == this.getModelItem(n) && ((TablePositionBase)((Object)change)).getRow() == n && ((TablePosition)((Object)change)).getTableColumn() == tableColumn) {
                    return;
                }
            }
            this.startAtomic();
            this.clearSelection();
            this.select(n, tableColumn);
            this.stopAtomic();
            if (bl) {
                arrayList.remove(tablePosition);
            } else {
                for (TablePosition tablePosition2 : arrayList) {
                    if (tablePosition2.getRow() != n) continue;
                    arrayList.remove(tablePosition2);
                    break;
                }
            }
            if (bl2) {
                change = ControlUtils.buildClearAndSelectChange(this.selectedCellsSeq, arrayList, n);
            } else {
                int n2 = bl ? 0 : Math.max(0, this.selectedCellsSeq.indexOf(tablePosition));
                int n3 = bl ? this.getSelectedCells().size() : 1;
                change = new NonIterableChange.GenericAddRemoveChange(n2, n2 + n3, arrayList, this.selectedCellsSeq);
            }
            this.fireCustomSelectedCellsListChangeEvent(change);
        }

        @Override
        public void select(int n) {
            this.select(n, (TableColumn<S, ?>)null);
        }

        @Override
        public void select(int n, TableColumn<S, ?> tableColumn) {
            if (n < 0 || n >= this.getItemCount()) {
                return;
            }
            if (this.isCellSelectionEnabled() && tableColumn == null) {
                ObservableList observableList = this.getTableView().getVisibleLeafColumns();
                for (int i = 0; i < observableList.size(); ++i) {
                    this.select(n, (TableColumn)observableList.get(i));
                }
                return;
            }
            if (TableCellBehavior.hasDefaultAnchor(this.tableView)) {
                TableCellBehavior.removeAnchor(this.tableView);
            }
            if (this.getSelectionMode() == SelectionMode.SINGLE) {
                this.quietClearSelection();
            }
            this.selectedCellsMap.add(new TablePosition(this.getTableView(), n, tableColumn));
            this.updateSelectedIndex(n);
            this.focus(n, tableColumn);
        }

        @Override
        public void select(S s) {
            if (s == null && this.getSelectionMode() == SelectionMode.SINGLE) {
                this.clearSelection();
                return;
            }
            Object object = null;
            for (int i = 0; i < this.getItemCount(); ++i) {
                object = this.getModelItem(i);
                if (object == null || !object.equals(s)) continue;
                if (this.isSelected(i)) {
                    return;
                }
                if (this.getSelectionMode() == SelectionMode.SINGLE) {
                    this.quietClearSelection();
                }
                this.select(i);
                return;
            }
            this.setSelectedIndex(-1);
            this.setSelectedItem(s);
        }

        @Override
        public void selectIndices(int n, int ... nArray) {
            if (nArray == null) {
                this.select(n);
                return;
            }
            int n2 = this.getItemCount();
            if (this.getSelectionMode() == SelectionMode.SINGLE) {
                this.quietClearSelection();
                for (int i = nArray.length - 1; i >= 0; --i) {
                    int n3 = nArray[i];
                    if (n3 < 0 || n3 >= n2) continue;
                    this.select(n3);
                    break;
                }
                if (this.selectedCellsMap.isEmpty() && n > 0 && n < n2) {
                    this.select(n);
                }
            } else {
                int n4;
                int n5 = -1;
                LinkedHashSet linkedHashSet = new LinkedHashSet();
                if (n >= 0 && n < n2) {
                    if (this.isCellSelectionEnabled()) {
                        ObservableList observableList = this.getTableView().getVisibleLeafColumns();
                        for (n4 = 0; n4 < observableList.size(); ++n4) {
                            if (this.selectedCellsMap.isSelected(n, n4)) continue;
                            linkedHashSet.add(new TablePosition(this.getTableView(), n, (TableColumn)observableList.get(n4)));
                            n5 = n;
                        }
                    } else {
                        boolean bl = this.selectedCellsMap.isSelected(n, -1);
                        if (!bl) {
                            linkedHashSet.add(new TablePosition(this.getTableView(), n, null));
                        }
                    }
                    n5 = n;
                }
                for (int i = 0; i < nArray.length; ++i) {
                    n4 = nArray[i];
                    if (n4 < 0 || n4 >= n2) continue;
                    n5 = n4;
                    if (this.isCellSelectionEnabled()) {
                        ObservableList observableList = this.getTableView().getVisibleLeafColumns();
                        for (int j = 0; j < observableList.size(); ++j) {
                            if (this.selectedCellsMap.isSelected(n4, j)) continue;
                            linkedHashSet.add(new TablePosition(this.getTableView(), n4, (TableColumn)observableList.get(j)));
                            n5 = n4;
                        }
                        continue;
                    }
                    if (this.selectedCellsMap.isSelected(n4, -1)) continue;
                    linkedHashSet.add(new TablePosition(this.getTableView(), n4, null));
                }
                this.selectedCellsMap.addAll(linkedHashSet);
                if (n5 != -1) {
                    this.select(n5);
                }
            }
        }

        @Override
        public void selectAll() {
            if (this.getSelectionMode() == SelectionMode.SINGLE) {
                return;
            }
            if (this.isCellSelectionEnabled()) {
                ArrayList arrayList = new ArrayList();
                TablePosition tablePosition = null;
                for (int i = 0; i < this.getTableView().getVisibleLeafColumns().size(); ++i) {
                    TableColumn tableColumn = (TableColumn)this.getTableView().getVisibleLeafColumns().get(i);
                    for (int j = 0; j < this.getItemCount(); ++j) {
                        tablePosition = new TablePosition(this.getTableView(), j, tableColumn);
                        arrayList.add(tablePosition);
                    }
                }
                this.selectedCellsMap.setAll(arrayList);
                if (tablePosition != null) {
                    this.select(tablePosition.getRow(), (TableColumn<S, ?>)tablePosition.getTableColumn());
                    this.focus(tablePosition.getRow(), tablePosition.getTableColumn());
                }
            } else {
                int n;
                ArrayList arrayList = new ArrayList();
                for (n = 0; n < this.getItemCount(); ++n) {
                    arrayList.add(new TablePosition(this.getTableView(), n, null));
                }
                this.selectedCellsMap.setAll(arrayList);
                n = this.getFocusedIndex();
                if (n == -1) {
                    int n2 = this.getItemCount();
                    if (n2 > 0) {
                        this.select(n2 - 1);
                        this.focus((TablePosition)arrayList.get(arrayList.size() - 1));
                    }
                } else {
                    this.select(n);
                    this.focus(n);
                }
            }
        }

        @Override
        public void selectRange(int n, TableColumnBase<S, ?> tableColumnBase, int n2, TableColumnBase<S, ?> tableColumnBase2) {
            if (this.getSelectionMode() == SelectionMode.SINGLE) {
                this.quietClearSelection();
                ((TableViewSelectionModel)this).select(n2, tableColumnBase2);
                return;
            }
            this.startAtomic();
            int n3 = this.getItemCount();
            boolean bl = this.isCellSelectionEnabled();
            int n4 = this.tableView.getVisibleLeafIndex((TableColumn)tableColumnBase);
            int n5 = this.tableView.getVisibleLeafIndex((TableColumn)tableColumnBase2);
            int n6 = Math.min(n4, n5);
            int n7 = Math.max(n4, n5);
            int n8 = Math.min(n, n2);
            int n9 = Math.max(n, n2);
            ArrayList arrayList = new ArrayList();
            for (int i = n8; i <= n9; ++i) {
                if (i < 0 || i >= n3) continue;
                if (!bl) {
                    arrayList.add(new TablePosition(this.tableView, i, (TableColumn)tableColumnBase));
                    continue;
                }
                for (int j = n6; j <= n7; ++j) {
                    TableColumn<S, ?> tableColumn = this.tableView.getVisibleLeafColumn(j);
                    if (tableColumn == null && bl) continue;
                    arrayList.add(new TablePosition(this.tableView, i, tableColumn));
                }
            }
            arrayList.removeAll(this.getSelectedCells());
            this.selectedCellsMap.addAll(arrayList);
            this.stopAtomic();
            this.updateSelectedIndex(n2);
            this.focus(n2, (TableColumn)tableColumnBase2);
            TableColumn tableColumn = (TableColumn)tableColumnBase;
            TableColumn tableColumn2 = bl ? (TableColumn)tableColumnBase2 : tableColumn;
            int n10 = this.selectedCellsMap.indexOf(new TablePosition(this.tableView, n, tableColumn));
            int n11 = this.selectedCellsMap.indexOf(new TablePosition(this.tableView, n2, tableColumn2));
            if (n10 > -1 && n11 > -1) {
                int n12 = Math.min(n10, n11);
                int n13 = Math.max(n10, n11);
                NonIterableChange.SimpleAddChange simpleAddChange = new NonIterableChange.SimpleAddChange(n12, n13 + 1, this.selectedCellsSeq);
                this.fireCustomSelectedCellsListChangeEvent(simpleAddChange);
            }
        }

        @Override
        public void clearSelection(int n) {
            this.clearSelection(n, (TableColumn<S, ?>)null);
        }

        @Override
        public void clearSelection(int n, TableColumn<S, ?> tableColumn) {
            this.clearSelection(new TablePosition(this.getTableView(), n, tableColumn));
        }

        private void clearSelection(TablePosition<S, ?> tablePosition) {
            boolean bl = this.isCellSelectionEnabled();
            int n = tablePosition.getRow();
            boolean bl2 = tablePosition.getTableColumn() == null;
            ArrayList arrayList = new ArrayList();
            for (TablePosition tablePosition2 : this.getSelectedCells()) {
                if (!bl) {
                    if (tablePosition2.getRow() != n) continue;
                    arrayList.add(tablePosition2);
                    break;
                }
                if (bl2 && tablePosition2.getRow() == n) {
                    arrayList.add(tablePosition2);
                    continue;
                }
                if (!tablePosition2.equals(tablePosition)) continue;
                arrayList.add(tablePosition);
                break;
            }
            arrayList.stream().forEach(this.selectedCellsMap::remove);
            if (this.isEmpty() && !this.isAtomic()) {
                this.updateSelectedIndex(-1);
                this.selectedCellsMap.clear();
            }
        }

        @Override
        public void clearSelection() {
            final ArrayList<TablePosition> arrayList = new ArrayList<TablePosition>(this.getSelectedCells());
            this.quietClearSelection();
            if (!this.isAtomic()) {
                this.updateSelectedIndex(-1);
                this.focus(-1);
                if (!arrayList.isEmpty()) {
                    NonIterableChange nonIterableChange = new NonIterableChange<TablePosition<S, ?>>(0, 0, this.selectedCellsSeq){

                        @Override
                        public List<TablePosition<S, ?>> getRemoved() {
                            return arrayList;
                        }
                    };
                    this.fireCustomSelectedCellsListChangeEvent(nonIterableChange);
                }
            }
        }

        private void quietClearSelection() {
            this.startAtomic();
            this.selectedCellsMap.clear();
            this.stopAtomic();
        }

        @Override
        public boolean isSelected(int n) {
            return this.isSelected(n, (TableColumn<S, ?>)null);
        }

        @Override
        public boolean isSelected(int n, TableColumn<S, ?> tableColumn) {
            boolean bl = this.isCellSelectionEnabled();
            if (bl && tableColumn == null) {
                int n2 = this.tableView.getVisibleLeafColumns().size();
                for (int i = 0; i < n2; ++i) {
                    if (this.selectedCellsMap.isSelected(n, i)) continue;
                    return false;
                }
                return true;
            }
            int n3 = !bl || tableColumn == null ? -1 : this.tableView.getVisibleLeafIndex(tableColumn);
            return this.selectedCellsMap.isSelected(n, n3);
        }

        @Override
        public boolean isEmpty() {
            return this.selectedCellsMap.isEmpty();
        }

        @Override
        public void selectPrevious() {
            if (this.isCellSelectionEnabled()) {
                TablePosition tablePosition = this.getFocusedCell();
                if (tablePosition.getColumn() - 1 >= 0) {
                    this.select(tablePosition.getRow(), this.getTableColumn((TableColumn<S, ?>)tablePosition.getTableColumn(), -1));
                } else if (tablePosition.getRow() < this.getItemCount() - 1) {
                    this.select(tablePosition.getRow() - 1, this.getTableColumn(this.getTableView().getVisibleLeafColumns().size() - 1));
                }
            } else {
                int n = this.getFocusedIndex();
                if (n == -1) {
                    this.select(this.getItemCount() - 1);
                } else if (n > 0) {
                    this.select(n - 1);
                }
            }
        }

        @Override
        public void selectNext() {
            if (this.isCellSelectionEnabled()) {
                TablePosition tablePosition = this.getFocusedCell();
                if (tablePosition.getColumn() + 1 < this.getTableView().getVisibleLeafColumns().size()) {
                    this.select(tablePosition.getRow(), this.getTableColumn((TableColumn<S, ?>)tablePosition.getTableColumn(), 1));
                } else if (tablePosition.getRow() < this.getItemCount() - 1) {
                    this.select(tablePosition.getRow() + 1, this.getTableColumn(0));
                }
            } else {
                int n = this.getFocusedIndex();
                if (n == -1) {
                    this.select(0);
                } else if (n < this.getItemCount() - 1) {
                    this.select(n + 1);
                }
            }
        }

        @Override
        public void selectAboveCell() {
            TablePosition tablePosition = this.getFocusedCell();
            if (tablePosition.getRow() == -1) {
                this.select(this.getItemCount() - 1);
            } else if (tablePosition.getRow() > 0) {
                this.select(tablePosition.getRow() - 1, (TableColumn<S, ?>)tablePosition.getTableColumn());
            }
        }

        @Override
        public void selectBelowCell() {
            TablePosition tablePosition = this.getFocusedCell();
            if (tablePosition.getRow() == -1) {
                this.select(0);
            } else if (tablePosition.getRow() < this.getItemCount() - 1) {
                this.select(tablePosition.getRow() + 1, (TableColumn<S, ?>)tablePosition.getTableColumn());
            }
        }

        @Override
        public void selectFirst() {
            TablePosition tablePosition = this.getFocusedCell();
            if (this.getSelectionMode() == SelectionMode.SINGLE) {
                this.quietClearSelection();
            }
            if (this.getItemCount() > 0) {
                if (this.isCellSelectionEnabled()) {
                    this.select(0, (TableColumn<S, ?>)tablePosition.getTableColumn());
                } else {
                    this.select(0);
                }
            }
        }

        @Override
        public void selectLast() {
            int n;
            TablePosition tablePosition = this.getFocusedCell();
            if (this.getSelectionMode() == SelectionMode.SINGLE) {
                this.quietClearSelection();
            }
            if ((n = this.getItemCount()) > 0 && this.getSelectedIndex() < n - 1) {
                if (this.isCellSelectionEnabled()) {
                    this.select(n - 1, (TableColumn<S, ?>)tablePosition.getTableColumn());
                } else {
                    this.select(n - 1);
                }
            }
        }

        @Override
        public void selectLeftCell() {
            if (!this.isCellSelectionEnabled()) {
                return;
            }
            TablePosition tablePosition = this.getFocusedCell();
            if (tablePosition.getColumn() - 1 >= 0) {
                this.select(tablePosition.getRow(), this.getTableColumn((TableColumn<S, ?>)tablePosition.getTableColumn(), -1));
            }
        }

        @Override
        public void selectRightCell() {
            if (!this.isCellSelectionEnabled()) {
                return;
            }
            TablePosition tablePosition = this.getFocusedCell();
            if (tablePosition.getColumn() + 1 < this.getTableView().getVisibleLeafColumns().size()) {
                this.select(tablePosition.getRow(), this.getTableColumn((TableColumn<S, ?>)tablePosition.getTableColumn(), 1));
            }
        }

        private void updateItemsObserver(ObservableList<S> observableList, ObservableList<S> observableList2) {
            if (observableList != null) {
                observableList.removeListener(this.weakItemsContentListener);
            }
            if (observableList2 != null) {
                observableList2.addListener(this.weakItemsContentListener);
            }
            this.updateItemCount();
            this.updateDefaultSelection();
        }

        private void updateDefaultSelection() {
            Object t;
            int n = -1;
            if (this.tableView.getItems() != null && (t = this.getSelectedItem()) != null) {
                n = this.tableView.getItems().indexOf(t);
            }
            this.clearSelection();
            this.select(n, this.isCellSelectionEnabled() ? this.getTableColumn(0) : null);
        }

        private TableColumn<S, ?> getTableColumn(int n) {
            return this.getTableView().getVisibleLeafColumn(n);
        }

        private TableColumn<S, ?> getTableColumn(TableColumn<S, ?> tableColumn, int n) {
            int n2 = this.getTableView().getVisibleLeafIndex(tableColumn);
            int n3 = n2 + n;
            return this.getTableView().getVisibleLeafColumn(n3);
        }

        private void updateSelectedIndex(int n) {
            this.setSelectedIndex(n);
            this.setSelectedItem(this.getModelItem(n));
        }

        @Override
        protected int getItemCount() {
            return this.itemCount;
        }

        private void updateItemCount() {
            List list;
            this.itemCount = this.tableView == null ? -1 : ((list = this.getTableModel()) == null ? -1 : list.size());
        }

        private void fireCustomSelectedCellsListChangeEvent(ListChangeListener.Change<? extends TablePosition<S, ?>> change) {
            ControlUtils.updateSelectedIndices(this, change);
            if (this.isAtomic()) {
                return;
            }
            this.selectedCellsSeq.callObservers(new MappingChange(change, MappingChange.NOOP_MAP, this.selectedCellsSeq));
        }
    }

    public static abstract class TableViewSelectionModel<S>
    extends TableSelectionModel<S> {
        private final TableView<S> tableView;
        boolean blockFocusCall = false;

        public TableViewSelectionModel(TableView<S> tableView) {
            if (tableView == null) {
                throw new NullPointerException("TableView can not be null");
            }
            this.tableView = tableView;
        }

        public abstract ObservableList<TablePosition> getSelectedCells();

        @Override
        public boolean isSelected(int n, TableColumnBase<S, ?> tableColumnBase) {
            return this.isSelected(n, (TableColumn)tableColumnBase);
        }

        @Override
        public abstract boolean isSelected(int var1, TableColumn<S, ?> var2);

        @Override
        public void select(int n, TableColumnBase<S, ?> tableColumnBase) {
            this.select(n, (TableColumn)tableColumnBase);
        }

        @Override
        public abstract void select(int var1, TableColumn<S, ?> var2);

        @Override
        public void clearAndSelect(int n, TableColumnBase<S, ?> tableColumnBase) {
            this.clearAndSelect(n, (TableColumn)tableColumnBase);
        }

        @Override
        public abstract void clearAndSelect(int var1, TableColumn<S, ?> var2);

        @Override
        public void clearSelection(int n, TableColumnBase<S, ?> tableColumnBase) {
            this.clearSelection(n, (TableColumn)tableColumnBase);
        }

        @Override
        public abstract void clearSelection(int var1, TableColumn<S, ?> var2);

        @Override
        public void selectRange(int n, TableColumnBase<S, ?> tableColumnBase, int n2, TableColumnBase<S, ?> tableColumnBase2) {
            int n3 = this.tableView.getVisibleLeafIndex((TableColumn)tableColumnBase);
            int n4 = this.tableView.getVisibleLeafIndex((TableColumn)tableColumnBase2);
            for (int i = n; i <= n2; ++i) {
                for (int j = n3; j <= n4; ++j) {
                    this.select(i, this.tableView.getVisibleLeafColumn(j));
                }
            }
        }

        public TableView<S> getTableView() {
            return this.tableView;
        }

        protected List<S> getTableModel() {
            return this.tableView.getItems();
        }

        @Override
        protected S getModelItem(int n) {
            if (n < 0 || n >= this.getItemCount()) {
                return null;
            }
            return (S)this.tableView.getItems().get(n);
        }

        @Override
        protected int getItemCount() {
            return this.getTableModel().size();
        }

        @Override
        public void focus(int n) {
            this.focus(n, null);
        }

        @Override
        public int getFocusedIndex() {
            return this.getFocusedCell().getRow();
        }

        void focus(int n, TableColumn<S, ?> tableColumn) {
            this.focus(new TablePosition(this.getTableView(), n, tableColumn));
            this.getTableView().notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_ITEM);
        }

        void focus(TablePosition<S, ?> tablePosition) {
            if (this.blockFocusCall) {
                return;
            }
            if (this.getTableView().getFocusModel() == null) {
                return;
            }
            this.getTableView().getFocusModel().focus(tablePosition.getRow(), (TableColumn<S, ?>)tablePosition.getTableColumn());
        }

        TablePosition<S, ?> getFocusedCell() {
            if (this.getTableView().getFocusModel() == null) {
                return new TablePosition(this.getTableView(), -1, null);
            }
            return this.getTableView().getFocusModel().getFocusedCell();
        }
    }

    public static class ResizeFeatures<S>
    extends ResizeFeaturesBase<S> {
        private TableView<S> table;

        public ResizeFeatures(TableView<S> tableView, TableColumn<S, ?> tableColumn, Double d) {
            super(tableColumn, d);
            this.table = tableView;
        }

        @Override
        public TableColumn<S, ?> getColumn() {
            return (TableColumn)super.getColumn();
        }

        public TableView<S> getTable() {
            return this.table;
        }
    }

    private static class StyleableProperties {
        private static final CssMetaData<TableView<?>, Number> FIXED_CELL_SIZE = new CssMetaData<TableView<?>, Number>("-fx-fixed-cell-size", SizeConverter.getInstance(), -1.0){

            @Override
            public Double getInitialValue(TableView<?> tableView) {
                return tableView.getFixedCellSize();
            }

            @Override
            public boolean isSettable(TableView<?> tableView) {
                return ((TableView)tableView).fixedCellSize == null || !((TableView)tableView).fixedCellSize.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(TableView<?> tableView) {
                return (StyleableProperty)((Object)tableView.fixedCellSizeProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Control.getClassCssMetaData());
            arrayList.add(FIXED_CELL_SIZE);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

