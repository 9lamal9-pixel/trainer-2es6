/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import java.util.Collections;
import java.util.List;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.WeakListChangeListener;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TreeTableViewSkin;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

public class TreeTableColumn<S, T>
extends TableColumnBase<TreeItem<S>, T>
implements EventTarget {
    private static final EventType<?> EDIT_ANY_EVENT = new EventType<Event>(Event.ANY, "TREE_TABLE_COLUMN_EDIT");
    private static final EventType<?> EDIT_START_EVENT = new EventType(TreeTableColumn.editAnyEvent(), "EDIT_START");
    private static final EventType<?> EDIT_CANCEL_EVENT = new EventType(TreeTableColumn.editAnyEvent(), "EDIT_CANCEL");
    private static final EventType<?> EDIT_COMMIT_EVENT = new EventType(TreeTableColumn.editAnyEvent(), "EDIT_COMMIT");
    public static final Callback<TreeTableColumn<?, ?>, TreeTableCell<?, ?>> DEFAULT_CELL_FACTORY = new Callback<TreeTableColumn<?, ?>, TreeTableCell<?, ?>>(){

        @Override
        public TreeTableCell<?, ?> call(TreeTableColumn<?, ?> treeTableColumn) {
            return new TreeTableCell(){

                @Override
                protected void updateItem(Object object, boolean bl) {
                    if (object == this.getItem()) {
                        return;
                    }
                    super.updateItem(object, bl);
                    if (object == null) {
                        super.setText(null);
                        super.setGraphic(null);
                    } else if (object instanceof Node) {
                        super.setText(null);
                        super.setGraphic((Node)object);
                    } else {
                        super.setText(object.toString());
                        super.setGraphic(null);
                    }
                }
            };
        }
    };
    private EventHandler<CellEditEvent<S, T>> DEFAULT_EDIT_COMMIT_HANDLER = cellEditEvent -> {
        ObservableValue<T> observableValue = this.getCellObservableValue(cellEditEvent.getRowValue());
        if (observableValue instanceof WritableValue) {
            ((WritableValue)((Object)observableValue)).setValue(cellEditEvent.getNewValue());
        }
    };
    private ListChangeListener<TreeTableColumn<S, ?>> columnsListener = new ListChangeListener<TreeTableColumn<S, ?>>(){

        @Override
        public void onChanged(ListChangeListener.Change<? extends TreeTableColumn<S, ?>> change) {
            while (change.next()) {
                for (TreeTableColumn treeTableColumn : change.getRemoved()) {
                    if (TreeTableColumn.this.getColumns().contains(treeTableColumn)) continue;
                    treeTableColumn.setTreeTableView(null);
                    treeTableColumn.setParentColumn(null);
                }
                for (TreeTableColumn treeTableColumn : change.getAddedSubList()) {
                    treeTableColumn.setTreeTableView(TreeTableColumn.this.getTreeTableView());
                }
                TreeTableColumn.this.updateColumnWidths();
            }
        }
    };
    private WeakListChangeListener<TreeTableColumn<S, ?>> weakColumnsListener = new WeakListChangeListener(this.columnsListener);
    private final ObservableList<TreeTableColumn<S, ?>> columns = FXCollections.observableArrayList();
    private ReadOnlyObjectWrapper<TreeTableView<S>> treeTableView = new ReadOnlyObjectWrapper(this, "treeTableView");
    private ObjectProperty<Callback<CellDataFeatures<S, T>, ObservableValue<T>>> cellValueFactory;
    private final ObjectProperty<Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>>> cellFactory = new SimpleObjectProperty<Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>>>(this, "cellFactory", DEFAULT_CELL_FACTORY){

        @Override
        protected void invalidated() {
            TreeTableView treeTableView = TreeTableColumn.this.getTreeTableView();
            if (treeTableView == null) {
                return;
            }
            ObservableMap<Object, Object> observableMap = treeTableView.getProperties();
            if (observableMap.containsKey("recreateKey")) {
                observableMap.remove("recreateKey");
            }
            observableMap.put("recreateKey", Boolean.TRUE);
        }
    };
    private ObjectProperty<SortType> sortType;
    private ObjectProperty<EventHandler<CellEditEvent<S, T>>> onEditStart;
    private ObjectProperty<EventHandler<CellEditEvent<S, T>>> onEditCommit;
    private ObjectProperty<EventHandler<CellEditEvent<S, T>>> onEditCancel;
    private static final String DEFAULT_STYLE_CLASS = "table-column";

    public static <S, T> EventType<CellEditEvent<S, T>> editAnyEvent() {
        return EDIT_ANY_EVENT;
    }

    public static <S, T> EventType<CellEditEvent<S, T>> editStartEvent() {
        return EDIT_START_EVENT;
    }

    public static <S, T> EventType<CellEditEvent<S, T>> editCancelEvent() {
        return EDIT_CANCEL_EVENT;
    }

    public static <S, T> EventType<CellEditEvent<S, T>> editCommitEvent() {
        return EDIT_COMMIT_EVENT;
    }

    public TreeTableColumn() {
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
        this.setOnEditCommit(this.DEFAULT_EDIT_COMMIT_HANDLER);
        this.getColumns().addListener(this.weakColumnsListener);
        this.treeTableViewProperty().addListener(observable -> {
            for (TreeTableColumn treeTableColumn : this.getColumns()) {
                treeTableColumn.setTreeTableView(this.getTreeTableView());
            }
        });
    }

    public TreeTableColumn(String string) {
        this();
        this.setText(string);
    }

    public final ReadOnlyObjectProperty<TreeTableView<S>> treeTableViewProperty() {
        return this.treeTableView.getReadOnlyProperty();
    }

    final void setTreeTableView(TreeTableView<S> treeTableView) {
        this.treeTableView.set(treeTableView);
    }

    public final TreeTableView<S> getTreeTableView() {
        return (TreeTableView)this.treeTableView.get();
    }

    public final void setCellValueFactory(Callback<CellDataFeatures<S, T>, ObservableValue<T>> callback) {
        this.cellValueFactoryProperty().set(callback);
    }

    public final Callback<CellDataFeatures<S, T>, ObservableValue<T>> getCellValueFactory() {
        return this.cellValueFactory == null ? null : (Callback)this.cellValueFactory.get();
    }

    public final ObjectProperty<Callback<CellDataFeatures<S, T>, ObservableValue<T>>> cellValueFactoryProperty() {
        if (this.cellValueFactory == null) {
            this.cellValueFactory = new SimpleObjectProperty<Callback<CellDataFeatures<S, T>, ObservableValue<T>>>(this, "cellValueFactory");
        }
        return this.cellValueFactory;
    }

    public final void setCellFactory(Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> callback) {
        this.cellFactory.set(callback);
    }

    public final Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> getCellFactory() {
        return (Callback)this.cellFactory.get();
    }

    public final ObjectProperty<Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>>> cellFactoryProperty() {
        return this.cellFactory;
    }

    public final ObjectProperty<SortType> sortTypeProperty() {
        if (this.sortType == null) {
            this.sortType = new SimpleObjectProperty<SortType>(this, "sortType", SortType.ASCENDING);
        }
        return this.sortType;
    }

    public final void setSortType(SortType sortType) {
        this.sortTypeProperty().set(sortType);
    }

    public final SortType getSortType() {
        return this.sortType == null ? SortType.ASCENDING : (SortType)((Object)this.sortType.get());
    }

    public final void setOnEditStart(EventHandler<CellEditEvent<S, T>> eventHandler) {
        this.onEditStartProperty().set(eventHandler);
    }

    public final EventHandler<CellEditEvent<S, T>> getOnEditStart() {
        return this.onEditStart == null ? null : (EventHandler)this.onEditStart.get();
    }

    public final ObjectProperty<EventHandler<CellEditEvent<S, T>>> onEditStartProperty() {
        if (this.onEditStart == null) {
            this.onEditStart = new SimpleObjectProperty<EventHandler<CellEditEvent<S, T>>>(this, "onEditStart"){

                @Override
                protected void invalidated() {
                    TreeTableColumn.this.eventHandlerManager.setEventHandler(TreeTableColumn.editStartEvent(), (EventHandler)this.get());
                }
            };
        }
        return this.onEditStart;
    }

    public final void setOnEditCommit(EventHandler<CellEditEvent<S, T>> eventHandler) {
        this.onEditCommitProperty().set(eventHandler);
    }

    public final EventHandler<CellEditEvent<S, T>> getOnEditCommit() {
        return this.onEditCommit == null ? null : (EventHandler)this.onEditCommit.get();
    }

    public final ObjectProperty<EventHandler<CellEditEvent<S, T>>> onEditCommitProperty() {
        if (this.onEditCommit == null) {
            this.onEditCommit = new SimpleObjectProperty<EventHandler<CellEditEvent<S, T>>>(this, "onEditCommit"){

                @Override
                protected void invalidated() {
                    TreeTableColumn.this.eventHandlerManager.setEventHandler(TreeTableColumn.editCommitEvent(), (EventHandler)this.get());
                }
            };
        }
        return this.onEditCommit;
    }

    public final void setOnEditCancel(EventHandler<CellEditEvent<S, T>> eventHandler) {
        this.onEditCancelProperty().set(eventHandler);
    }

    public final EventHandler<CellEditEvent<S, T>> getOnEditCancel() {
        return this.onEditCancel == null ? null : (EventHandler)this.onEditCancel.get();
    }

    public final ObjectProperty<EventHandler<CellEditEvent<S, T>>> onEditCancelProperty() {
        if (this.onEditCancel == null) {
            this.onEditCancel = new SimpleObjectProperty<EventHandler<CellEditEvent<S, T>>>(this, "onEditCancel"){

                @Override
                protected void invalidated() {
                    TreeTableColumn.this.eventHandlerManager.setEventHandler(TreeTableColumn.editCancelEvent(), (EventHandler)this.get());
                }
            };
        }
        return this.onEditCancel;
    }

    @Override
    public final ObservableList<TreeTableColumn<S, ?>> getColumns() {
        return this.columns;
    }

    @Override
    public final ObservableValue<T> getCellObservableValue(int n) {
        if (n < 0) {
            return null;
        }
        TreeTableView<S> treeTableView = this.getTreeTableView();
        if (treeTableView == null || n >= treeTableView.getExpandedItemCount()) {
            return null;
        }
        TreeItem<S> treeItem = treeTableView.getTreeItem(n);
        return this.getCellObservableValue(treeItem);
    }

    @Override
    public final ObservableValue<T> getCellObservableValue(TreeItem<S> treeItem) {
        Callback<CellDataFeatures<S, T>, ObservableValue<T>> callback = this.getCellValueFactory();
        if (callback == null) {
            return null;
        }
        TreeTableView<S> treeTableView = this.getTreeTableView();
        if (treeTableView == null) {
            return null;
        }
        CellDataFeatures cellDataFeatures = new CellDataFeatures(treeTableView, this, treeItem);
        return callback.call(cellDataFeatures);
    }

    @Override
    public String getTypeSelector() {
        return "TreeTableColumn";
    }

    @Override
    public Styleable getStyleableParent() {
        return this.getTreeTableView();
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return TreeTableColumn.getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return Collections.emptyList();
    }

    @Override
    public Node getStyleableNode() {
        if (!(this.getTreeTableView().getSkin() instanceof TreeTableViewSkin)) {
            return null;
        }
        TreeTableViewSkin treeTableViewSkin = (TreeTableViewSkin)this.getTreeTableView().getSkin();
        Pane pane = null;
        for (Node object : treeTableViewSkin.getChildren()) {
            if (!(object instanceof TableHeaderRow)) continue;
            pane = (TableHeaderRow)object;
        }
        Object object = null;
        for (Node node : pane.getChildren()) {
            if (!(node instanceof NestedTableColumnHeader)) continue;
            object = (NestedTableColumnHeader)node;
        }
        return this.scan((TableColumnHeader)object);
    }

    private TableColumnHeader scan(TableColumnHeader tableColumnHeader) {
        if (this.equals(tableColumnHeader.getTableColumn())) {
            return tableColumnHeader;
        }
        if (tableColumnHeader instanceof NestedTableColumnHeader) {
            NestedTableColumnHeader nestedTableColumnHeader = (NestedTableColumnHeader)tableColumnHeader;
            for (int i = 0; i < nestedTableColumnHeader.getColumnHeaders().size(); ++i) {
                TableColumnHeader tableColumnHeader2 = this.scan((TableColumnHeader)nestedTableColumnHeader.getColumnHeaders().get(i));
                if (tableColumnHeader2 == null) continue;
                return tableColumnHeader2;
            }
        }
        return null;
    }

    static /* synthetic */ EventType access$000() {
        return EDIT_ANY_EVENT;
    }

    public static enum SortType {
        ASCENDING,
        DESCENDING;

    }

    public static class CellEditEvent<S, T>
    extends Event {
        private static final long serialVersionUID = -609964441682677579L;
        public static final EventType<?> ANY = TreeTableColumn.access$000();
        private final T newValue;
        private final transient TreeTablePosition<S, T> pos;

        public CellEditEvent(TreeTableView<S> treeTableView, TreeTablePosition<S, T> treeTablePosition, EventType<CellEditEvent<S, T>> eventType, T t) {
            super(treeTableView, Event.NULL_SOURCE_TARGET, eventType);
            if (treeTableView == null) {
                throw new NullPointerException("TableView can not be null");
            }
            this.pos = treeTablePosition;
            this.newValue = t;
        }

        public TreeTableView<S> getTreeTableView() {
            return this.pos.getTreeTableView();
        }

        public TreeTableColumn<S, T> getTableColumn() {
            return this.pos.getTableColumn();
        }

        public TreeTablePosition<S, T> getTreeTablePosition() {
            return this.pos;
        }

        public T getNewValue() {
            return this.newValue;
        }

        public T getOldValue() {
            TreeItem<S> treeItem = this.getRowValue();
            if (treeItem == null || this.pos.getTableColumn() == null) {
                return null;
            }
            return this.pos.getTableColumn().getCellData(treeItem);
        }

        public TreeItem<S> getRowValue() {
            TreeTableView<S> treeTableView = this.getTreeTableView();
            int n = this.pos.getRow();
            if (n < 0 || n >= treeTableView.getExpandedItemCount()) {
                return null;
            }
            return treeTableView.getTreeItem(n);
        }
    }

    public static class CellDataFeatures<S, T> {
        private final TreeTableView<S> treeTableView;
        private final TreeTableColumn<S, T> tableColumn;
        private final TreeItem<S> value;

        public CellDataFeatures(TreeTableView<S> treeTableView, TreeTableColumn<S, T> treeTableColumn, TreeItem<S> treeItem) {
            this.treeTableView = treeTableView;
            this.tableColumn = treeTableColumn;
            this.value = treeItem;
        }

        public TreeItem<S> getValue() {
            return this.value;
        }

        public TreeTableColumn<S, T> getTreeTableColumn() {
            return this.tableColumn;
        }

        public TreeTableView<S> getTreeTableView() {
            return this.treeTableView;
        }
    }
}

