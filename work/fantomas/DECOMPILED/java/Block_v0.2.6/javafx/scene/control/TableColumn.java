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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumnBase;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.skin.NestedTableColumnHeader;
import javafx.scene.control.skin.TableColumnHeader;
import javafx.scene.control.skin.TableHeaderRow;
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

public class TableColumn<S, T>
extends TableColumnBase<S, T>
implements EventTarget {
    private static final EventType<?> EDIT_ANY_EVENT = new EventType<Event>(Event.ANY, "TABLE_COLUMN_EDIT");
    private static final EventType<?> EDIT_START_EVENT = new EventType(TableColumn.editAnyEvent(), "EDIT_START");
    private static final EventType<?> EDIT_CANCEL_EVENT = new EventType(TableColumn.editAnyEvent(), "EDIT_CANCEL");
    private static final EventType<?> EDIT_COMMIT_EVENT = new EventType(TableColumn.editAnyEvent(), "EDIT_COMMIT");
    public static final Callback<TableColumn<?, ?>, TableCell<?, ?>> DEFAULT_CELL_FACTORY = new Callback<TableColumn<?, ?>, TableCell<?, ?>>(){

        @Override
        public TableCell<?, ?> call(TableColumn<?, ?> tableColumn) {
            return new TableCell<Object, Object>(){

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
        int n = cellEditEvent.getTablePosition().getRow();
        ObservableList observableList = cellEditEvent.getTableView().getItems();
        if (observableList == null || n < 0 || n >= observableList.size()) {
            return;
        }
        Object e = observableList.get(n);
        ObservableValue<T> observableValue = this.getCellObservableValue(e);
        if (observableValue instanceof WritableValue) {
            ((WritableValue)((Object)observableValue)).setValue(cellEditEvent.getNewValue());
        }
    };
    private ListChangeListener<TableColumn<S, ?>> columnsListener = change -> {
        while (change.next()) {
            for (TableColumn tableColumn : change.getRemoved()) {
                if (this.getColumns().contains(tableColumn)) continue;
                tableColumn.setTableView(null);
                tableColumn.setParentColumn(null);
            }
            for (TableColumn tableColumn : change.getAddedSubList()) {
                tableColumn.setTableView(this.getTableView());
            }
            this.updateColumnWidths();
        }
    };
    private WeakListChangeListener<TableColumn<S, ?>> weakColumnsListener = new WeakListChangeListener(this.columnsListener);
    private final ObservableList<TableColumn<S, ?>> columns = FXCollections.observableArrayList();
    private ReadOnlyObjectWrapper<TableView<S>> tableView = new ReadOnlyObjectWrapper(this, "tableView");
    private ObjectProperty<Callback<CellDataFeatures<S, T>, ObservableValue<T>>> cellValueFactory;
    private final ObjectProperty<Callback<TableColumn<S, T>, TableCell<S, T>>> cellFactory = new SimpleObjectProperty<Callback<TableColumn<S, T>, TableCell<S, T>>>(this, "cellFactory", DEFAULT_CELL_FACTORY){

        @Override
        protected void invalidated() {
            TableView tableView = TableColumn.this.getTableView();
            if (tableView == null) {
                return;
            }
            ObservableMap<Object, Object> observableMap = tableView.getProperties();
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

    public TableColumn() {
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
        this.setOnEditCommit(this.DEFAULT_EDIT_COMMIT_HANDLER);
        this.getColumns().addListener(this.weakColumnsListener);
        this.tableViewProperty().addListener(observable -> {
            for (TableColumn tableColumn : this.getColumns()) {
                tableColumn.setTableView(this.getTableView());
            }
        });
    }

    public TableColumn(String string) {
        this();
        this.setText(string);
    }

    public final ReadOnlyObjectProperty<TableView<S>> tableViewProperty() {
        return this.tableView.getReadOnlyProperty();
    }

    final void setTableView(TableView<S> tableView) {
        this.tableView.set(tableView);
    }

    public final TableView<S> getTableView() {
        return (TableView)this.tableView.get();
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

    public final void setCellFactory(Callback<TableColumn<S, T>, TableCell<S, T>> callback) {
        this.cellFactory.set(callback);
    }

    public final Callback<TableColumn<S, T>, TableCell<S, T>> getCellFactory() {
        return (Callback)this.cellFactory.get();
    }

    public final ObjectProperty<Callback<TableColumn<S, T>, TableCell<S, T>>> cellFactoryProperty() {
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
                    TableColumn.this.eventHandlerManager.setEventHandler(TableColumn.editStartEvent(), (EventHandler)this.get());
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
                    TableColumn.this.eventHandlerManager.setEventHandler(TableColumn.editCommitEvent(), (EventHandler)this.get());
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
                    TableColumn.this.eventHandlerManager.setEventHandler(TableColumn.editCancelEvent(), (EventHandler)this.get());
                }
            };
        }
        return this.onEditCancel;
    }

    @Override
    public final ObservableList<TableColumn<S, ?>> getColumns() {
        return this.columns;
    }

    @Override
    public final ObservableValue<T> getCellObservableValue(int n) {
        if (n < 0) {
            return null;
        }
        TableView<S> tableView = this.getTableView();
        if (tableView == null || tableView.getItems() == null) {
            return null;
        }
        ObservableList<S> observableList = tableView.getItems();
        if (n >= observableList.size()) {
            return null;
        }
        Object e = observableList.get(n);
        return this.getCellObservableValue(e);
    }

    @Override
    public final ObservableValue<T> getCellObservableValue(S s) {
        Callback<CellDataFeatures<S, T>, ObservableValue<T>> callback = this.getCellValueFactory();
        if (callback == null) {
            return null;
        }
        TableView<S> tableView = this.getTableView();
        if (tableView == null) {
            return null;
        }
        CellDataFeatures cellDataFeatures = new CellDataFeatures(tableView, this, s);
        return callback.call(cellDataFeatures);
    }

    @Override
    public String getTypeSelector() {
        return "TableColumn";
    }

    @Override
    public Styleable getStyleableParent() {
        return this.getTableView();
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return TableColumn.getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return Collections.emptyList();
    }

    @Override
    public Node getStyleableNode() {
        if (!(this.getTableView().getSkin() instanceof TableViewSkin)) {
            return null;
        }
        TableViewSkin tableViewSkin = (TableViewSkin)this.getTableView().getSkin();
        Pane pane = null;
        for (Node object : tableViewSkin.getChildren()) {
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
        public static final EventType<?> ANY = TableColumn.access$000();
        private final T newValue;
        private final transient TablePosition<S, T> pos;

        public CellEditEvent(TableView<S> tableView, TablePosition<S, T> tablePosition, EventType<CellEditEvent<S, T>> eventType, T t) {
            super(tableView, Event.NULL_SOURCE_TARGET, eventType);
            if (tableView == null) {
                throw new NullPointerException("TableView can not be null");
            }
            this.pos = tablePosition;
            this.newValue = t;
        }

        public TableView<S> getTableView() {
            return this.pos.getTableView();
        }

        public TableColumn<S, T> getTableColumn() {
            return this.pos.getTableColumn();
        }

        public TablePosition<S, T> getTablePosition() {
            return this.pos;
        }

        public T getNewValue() {
            return this.newValue;
        }

        public T getOldValue() {
            S s = this.getRowValue();
            if (s == null || this.pos.getTableColumn() == null) {
                return null;
            }
            return this.pos.getTableColumn().getCellData(s);
        }

        public S getRowValue() {
            ObservableList<S> observableList = this.getTableView().getItems();
            if (observableList == null) {
                return null;
            }
            int n = this.pos.getRow();
            if (n < 0 || n >= observableList.size()) {
                return null;
            }
            return (S)observableList.get(n);
        }
    }

    public static class CellDataFeatures<S, T> {
        private final TableView<S> tableView;
        private final TableColumn<S, T> tableColumn;
        private final S value;

        public CellDataFeatures(TableView<S> tableView, TableColumn<S, T> tableColumn, S s) {
            this.tableView = tableView;
            this.tableColumn = tableColumn;
            this.value = s;
        }

        public S getValue() {
            return this.value;
        }

        public TableColumn<S, T> getTableColumn() {
            return this.tableColumn;
        }

        public TableView<S> getTableView() {
            return this.tableView;
        }
    }
}

