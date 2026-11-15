/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.beans.IDProperty;
import com.sun.javafx.event.EventHandlerManager;
import com.sun.javafx.scene.control.ControlAcceleratorSupport;
import com.sun.javafx.scene.control.TableColumnBaseHelper;
import com.sun.javafx.scene.control.skin.Utils;
import java.lang.ref.WeakReference;
import java.text.Collator;
import java.util.Comparator;
import java.util.HashMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;

@IDProperty(value="id")
public abstract class TableColumnBase<S, T>
implements EventTarget,
Styleable {
    static final double DEFAULT_WIDTH = 80.0;
    static final double DEFAULT_MIN_WIDTH = 10.0;
    static final double DEFAULT_MAX_WIDTH = 5000.0;
    public static final Comparator DEFAULT_COMPARATOR;
    final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);
    private StringProperty text = new SimpleStringProperty(this, "text", "");
    private BooleanProperty visible = new SimpleBooleanProperty(this, "visible", true){

        @Override
        protected void invalidated() {
            for (TableColumnBase tableColumnBase : TableColumnBase.this.getColumns()) {
                tableColumnBase.setVisible(TableColumnBase.this.isVisible());
            }
        }
    };
    private ReadOnlyObjectWrapper<TableColumnBase<S, ?>> parentColumn;
    private ObjectProperty<ContextMenu> contextMenu;
    private StringProperty id;
    private StringProperty style;
    private final ObservableList<String> styleClass = FXCollections.observableArrayList();
    private ObjectProperty<Node> graphic;
    private ObjectProperty<Node> sortNode = new SimpleObjectProperty<Node>(this, "sortNode");
    private ReadOnlyDoubleWrapper width = new ReadOnlyDoubleWrapper(this, "width", 80.0);
    private DoubleProperty minWidth;
    private final DoubleProperty prefWidth = new SimpleDoubleProperty(this, "prefWidth", 80.0){

        @Override
        protected void invalidated() {
            TableColumnBase.this.doSetWidth(TableColumnBase.this.getPrefWidth());
        }
    };
    private DoubleProperty maxWidth = new SimpleDoubleProperty(this, "maxWidth", 5000.0){

        @Override
        protected void invalidated() {
            TableColumnBase.this.doSetWidth(TableColumnBase.this.getWidth());
        }
    };
    private BooleanProperty resizable;
    private BooleanProperty sortable;
    private BooleanProperty reorderable;
    private ObjectProperty<Comparator<T>> comparator;
    private BooleanProperty editable;
    private static final Object USER_DATA_KEY;
    private ObservableMap<Object, Object> properties;

    protected TableColumnBase() {
        this("");
    }

    protected TableColumnBase(String string) {
        this.setText(string);
    }

    public final StringProperty textProperty() {
        return this.text;
    }

    public final void setText(String string) {
        this.text.set(string);
    }

    public final String getText() {
        return (String)this.text.get();
    }

    public final void setVisible(boolean bl) {
        this.visibleProperty().set(bl);
    }

    public final boolean isVisible() {
        return this.visible.get();
    }

    public final BooleanProperty visibleProperty() {
        return this.visible;
    }

    void setParentColumn(TableColumnBase<S, ?> tableColumnBase) {
        this.parentColumnPropertyImpl().set(tableColumnBase);
    }

    public final TableColumnBase<S, ?> getParentColumn() {
        return this.parentColumn == null ? null : (TableColumnBase)this.parentColumn.get();
    }

    public final ReadOnlyObjectProperty<TableColumnBase<S, ?>> parentColumnProperty() {
        return this.parentColumnPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<TableColumnBase<S, ?>> parentColumnPropertyImpl() {
        if (this.parentColumn == null) {
            this.parentColumn = new ReadOnlyObjectWrapper(this, "parentColumn");
        }
        return this.parentColumn;
    }

    public final void setContextMenu(ContextMenu contextMenu) {
        this.contextMenuProperty().set(contextMenu);
    }

    public final ContextMenu getContextMenu() {
        return this.contextMenu == null ? null : (ContextMenu)this.contextMenu.get();
    }

    public final ObjectProperty<ContextMenu> contextMenuProperty() {
        if (this.contextMenu == null) {
            this.contextMenu = new SimpleObjectProperty<ContextMenu>((Object)this, "contextMenu"){
                private WeakReference<ContextMenu> contextMenuRef;

                @Override
                protected void invalidated() {
                    ContextMenu contextMenu;
                    ContextMenu contextMenu2 = contextMenu = this.contextMenuRef == null ? null : (ContextMenu)this.contextMenuRef.get();
                    if (contextMenu != null) {
                        ControlAcceleratorSupport.removeAcceleratorsFromScene(contextMenu.getItems(), TableColumnBase.this);
                    }
                    ContextMenu contextMenu3 = (ContextMenu)this.get();
                    this.contextMenuRef = new WeakReference<ContextMenu>(contextMenu3);
                    if (contextMenu3 != null) {
                        ControlAcceleratorSupport.addAcceleratorsIntoScene(contextMenu3.getItems(), TableColumnBase.this);
                    }
                }
            };
        }
        return this.contextMenu;
    }

    public final void setId(String string) {
        this.idProperty().set(string);
    }

    @Override
    public final String getId() {
        return this.id == null ? null : (String)this.id.get();
    }

    public final StringProperty idProperty() {
        if (this.id == null) {
            this.id = new SimpleStringProperty(this, "id");
        }
        return this.id;
    }

    public final void setStyle(String string) {
        this.styleProperty().set(string);
    }

    @Override
    public final String getStyle() {
        return this.style == null ? "" : (String)this.style.get();
    }

    public final StringProperty styleProperty() {
        if (this.style == null) {
            this.style = new SimpleStringProperty(this, "style");
        }
        return this.style;
    }

    @Override
    public ObservableList<String> getStyleClass() {
        return this.styleClass;
    }

    public final void setGraphic(Node node) {
        this.graphicProperty().set(node);
    }

    public final Node getGraphic() {
        return this.graphic == null ? null : (Node)this.graphic.get();
    }

    public final ObjectProperty<Node> graphicProperty() {
        if (this.graphic == null) {
            this.graphic = new SimpleObjectProperty<Node>(this, "graphic");
        }
        return this.graphic;
    }

    public final void setSortNode(Node node) {
        this.sortNodeProperty().set(node);
    }

    public final Node getSortNode() {
        return (Node)this.sortNode.get();
    }

    public final ObjectProperty<Node> sortNodeProperty() {
        return this.sortNode;
    }

    public final ReadOnlyDoubleProperty widthProperty() {
        return this.width.getReadOnlyProperty();
    }

    public final double getWidth() {
        return this.width.get();
    }

    void setWidth(double d) {
        this.width.set(d);
    }

    public final void setMinWidth(double d) {
        this.minWidthProperty().set(d);
    }

    public final double getMinWidth() {
        return this.minWidth == null ? 10.0 : this.minWidth.get();
    }

    public final DoubleProperty minWidthProperty() {
        if (this.minWidth == null) {
            this.minWidth = new SimpleDoubleProperty(this, "minWidth", 10.0){

                @Override
                protected void invalidated() {
                    if (TableColumnBase.this.getMinWidth() < 0.0) {
                        TableColumnBase.this.setMinWidth(0.0);
                    }
                    TableColumnBase.this.doSetWidth(TableColumnBase.this.getWidth());
                }
            };
        }
        return this.minWidth;
    }

    public final DoubleProperty prefWidthProperty() {
        return this.prefWidth;
    }

    public final void setPrefWidth(double d) {
        this.prefWidthProperty().set(d);
    }

    public final double getPrefWidth() {
        return this.prefWidth.get();
    }

    public final DoubleProperty maxWidthProperty() {
        return this.maxWidth;
    }

    public final void setMaxWidth(double d) {
        this.maxWidthProperty().set(d);
    }

    public final double getMaxWidth() {
        return this.maxWidth.get();
    }

    public final BooleanProperty resizableProperty() {
        if (this.resizable == null) {
            this.resizable = new SimpleBooleanProperty(this, "resizable", true);
        }
        return this.resizable;
    }

    public final void setResizable(boolean bl) {
        this.resizableProperty().set(bl);
    }

    public final boolean isResizable() {
        return this.resizable == null ? true : this.resizable.get();
    }

    public final BooleanProperty sortableProperty() {
        if (this.sortable == null) {
            this.sortable = new SimpleBooleanProperty(this, "sortable", true);
        }
        return this.sortable;
    }

    public final void setSortable(boolean bl) {
        this.sortableProperty().set(bl);
    }

    public final boolean isSortable() {
        return this.sortable == null ? true : this.sortable.get();
    }

    public final BooleanProperty reorderableProperty() {
        if (this.reorderable == null) {
            this.reorderable = new SimpleBooleanProperty(this, "reorderable", true);
        }
        return this.reorderable;
    }

    public final void setReorderable(boolean bl) {
        this.reorderableProperty().set(bl);
    }

    public final boolean isReorderable() {
        return this.reorderable == null ? true : this.reorderable.get();
    }

    public final ObjectProperty<Comparator<T>> comparatorProperty() {
        if (this.comparator == null) {
            this.comparator = new SimpleObjectProperty<Comparator>(this, "comparator", DEFAULT_COMPARATOR);
        }
        return this.comparator;
    }

    public final void setComparator(Comparator<T> comparator) {
        this.comparatorProperty().set(comparator);
    }

    public final Comparator<T> getComparator() {
        return this.comparator == null ? DEFAULT_COMPARATOR : (Comparator)this.comparator.get();
    }

    public final void setEditable(boolean bl) {
        this.editableProperty().set(bl);
    }

    public final boolean isEditable() {
        return this.editable == null ? true : this.editable.get();
    }

    public final BooleanProperty editableProperty() {
        if (this.editable == null) {
            this.editable = new SimpleBooleanProperty(this, "editable", true);
        }
        return this.editable;
    }

    public final ObservableMap<Object, Object> getProperties() {
        if (this.properties == null) {
            this.properties = FXCollections.observableMap(new HashMap());
        }
        return this.properties;
    }

    public boolean hasProperties() {
        return this.properties != null && !this.properties.isEmpty();
    }

    public void setUserData(Object object) {
        this.getProperties().put(USER_DATA_KEY, object);
    }

    public Object getUserData() {
        return this.getProperties().get(USER_DATA_KEY);
    }

    public abstract ObservableList<? extends TableColumnBase<S, ?>> getColumns();

    public final T getCellData(int n) {
        ObservableValue<T> observableValue = this.getCellObservableValue((S)n);
        return observableValue == null ? null : (T)observableValue.getValue();
    }

    public final T getCellData(S s) {
        ObservableValue<T> observableValue = this.getCellObservableValue(s);
        return observableValue == null ? null : (T)observableValue.getValue();
    }

    public abstract ObservableValue<T> getCellObservableValue(int var1);

    public abstract ObservableValue<T> getCellObservableValue(S var1);

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain eventDispatchChain) {
        return eventDispatchChain.prepend(this.eventHandlerManager);
    }

    public <E extends Event> void addEventHandler(EventType<E> eventType, EventHandler<E> eventHandler) {
        this.eventHandlerManager.addEventHandler(eventType, eventHandler);
    }

    public <E extends Event> void removeEventHandler(EventType<E> eventType, EventHandler<E> eventHandler) {
        this.eventHandlerManager.removeEventHandler(eventType, eventHandler);
    }

    void doSetWidth(double d) {
        this.setWidth(Utils.boundedSize(d, this.getMinWidth(), this.getMaxWidth()));
    }

    void updateColumnWidths() {
        if (!this.getColumns().isEmpty()) {
            double d = 0.0;
            double d2 = 0.0;
            double d3 = 0.0;
            for (TableColumnBase tableColumnBase : this.getColumns()) {
                tableColumnBase.setParentColumn(this);
                d += tableColumnBase.getMinWidth();
                d2 += tableColumnBase.getPrefWidth();
                d3 += tableColumnBase.getMaxWidth();
            }
            this.setMinWidth(d);
            this.setPrefWidth(d2);
            this.setMaxWidth(d3);
        }
    }

    @Override
    public final ObservableSet<PseudoClass> getPseudoClassStates() {
        return FXCollections.emptyObservableSet();
    }

    static {
        TableColumnBaseHelper.setTableColumnBaseAccessor(new TableColumnBaseHelper.TableColumnBaseAccessor(){

            @Override
            public void setWidth(TableColumnBase tableColumnBase, double d) {
                tableColumnBase.doSetWidth(d);
            }
        });
        DEFAULT_COMPARATOR = (object, object2) -> {
            if (object == null && object2 == null) {
                return 0;
            }
            if (object == null) {
                return -1;
            }
            if (object2 == null) {
                return 1;
            }
            if (object instanceof Comparable && (object.getClass() == object2.getClass() || object.getClass().isAssignableFrom(object2.getClass()))) {
                return object instanceof String ? Collator.getInstance().compare(object, object2) : ((Comparable)object).compareTo(object2);
            }
            return Collator.getInstance().compare(object.toString(), object2.toString());
        };
        USER_DATA_KEY = new Object();
    }
}

