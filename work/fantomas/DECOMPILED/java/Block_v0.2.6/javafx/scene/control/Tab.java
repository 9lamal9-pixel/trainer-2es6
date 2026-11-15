/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.beans.IDProperty;
import com.sun.javafx.event.EventHandlerManager;
import com.sun.javafx.scene.control.ControlAcceleratorSupport;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javafx.beans.DefaultProperty;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;

@DefaultProperty(value="content")
@IDProperty(value="id")
public class Tab
implements EventTarget,
Styleable {
    private StringProperty id;
    private StringProperty style;
    private ReadOnlyBooleanWrapper selected;
    private ReadOnlyObjectWrapper<TabPane> tabPane;
    private final InvalidationListener parentDisabledChangedListener = observable -> this.updateDisabled();
    private StringProperty text;
    private ObjectProperty<Node> graphic;
    private ObjectProperty<Node> content;
    private ObjectProperty<ContextMenu> contextMenu;
    private BooleanProperty closable;
    public static final EventType<Event> SELECTION_CHANGED_EVENT = new EventType<Event>(Event.ANY, "SELECTION_CHANGED_EVENT");
    private ObjectProperty<EventHandler<Event>> onSelectionChanged;
    public static final EventType<Event> CLOSED_EVENT = new EventType<Event>(Event.ANY, "TAB_CLOSED");
    private ObjectProperty<EventHandler<Event>> onClosed;
    private ObjectProperty<Tooltip> tooltip;
    private final ObservableList<String> styleClass = FXCollections.observableArrayList();
    private BooleanProperty disable;
    private ReadOnlyBooleanWrapper disabled;
    public static final EventType<Event> TAB_CLOSE_REQUEST_EVENT = new EventType<Event>(Event.ANY, "TAB_CLOSE_REQUEST_EVENT");
    private ObjectProperty<EventHandler<Event>> onCloseRequest;
    private static final Object USER_DATA_KEY = new Object();
    private ObservableMap<Object, Object> properties;
    private final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);
    private static final String DEFAULT_STYLE_CLASS = "tab";

    public Tab() {
        this(null);
    }

    public Tab(String string) {
        this(string, null);
    }

    public Tab(String string, Node node) {
        this.setText(string);
        this.setContent(node);
        this.styleClass.addAll((String[])new String[]{DEFAULT_STYLE_CLASS});
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
        return this.style == null ? null : (String)this.style.get();
    }

    public final StringProperty styleProperty() {
        if (this.style == null) {
            this.style = new SimpleStringProperty(this, "style");
        }
        return this.style;
    }

    final void setSelected(boolean bl) {
        this.selectedPropertyImpl().set(bl);
    }

    public final boolean isSelected() {
        return this.selected == null ? false : this.selected.get();
    }

    public final ReadOnlyBooleanProperty selectedProperty() {
        return this.selectedPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyBooleanWrapper selectedPropertyImpl() {
        if (this.selected == null) {
            this.selected = new ReadOnlyBooleanWrapper(){

                @Override
                protected void invalidated() {
                    if (Tab.this.getOnSelectionChanged() != null) {
                        Event.fireEvent(Tab.this, new Event(SELECTION_CHANGED_EVENT));
                    }
                }

                @Override
                public Object getBean() {
                    return Tab.this;
                }

                @Override
                public String getName() {
                    return "selected";
                }
            };
        }
        return this.selected;
    }

    final void setTabPane(TabPane tabPane) {
        this.tabPanePropertyImpl().set(tabPane);
    }

    public final TabPane getTabPane() {
        return this.tabPane == null ? null : (TabPane)this.tabPane.get();
    }

    public final ReadOnlyObjectProperty<TabPane> tabPaneProperty() {
        return this.tabPanePropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<TabPane> tabPanePropertyImpl() {
        if (this.tabPane == null) {
            this.tabPane = new ReadOnlyObjectWrapper<TabPane>((Object)this, "tabPane"){
                private WeakReference<TabPane> oldParent;

                @Override
                protected void invalidated() {
                    if (this.oldParent != null && this.oldParent.get() != null) {
                        ((TabPane)this.oldParent.get()).disabledProperty().removeListener(Tab.this.parentDisabledChangedListener);
                    }
                    Tab.this.updateDisabled();
                    TabPane tabPane = (TabPane)this.get();
                    if (tabPane != null) {
                        tabPane.disabledProperty().addListener(Tab.this.parentDisabledChangedListener);
                    }
                    this.oldParent = new WeakReference<TabPane>(tabPane);
                    super.invalidated();
                }
            };
        }
        return this.tabPane;
    }

    public final void setText(String string) {
        this.textProperty().set(string);
    }

    public final String getText() {
        return this.text == null ? null : (String)this.text.get();
    }

    public final StringProperty textProperty() {
        if (this.text == null) {
            this.text = new SimpleStringProperty(this, "text");
        }
        return this.text;
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

    public final void setContent(Node node) {
        this.contentProperty().set(node);
    }

    public final Node getContent() {
        return this.content == null ? null : (Node)this.content.get();
    }

    public final ObjectProperty<Node> contentProperty() {
        if (this.content == null) {
            this.content = new SimpleObjectProperty<Node>((Object)this, "content"){

                @Override
                protected void invalidated() {
                    Tab.this.updateDisabled();
                }
            };
        }
        return this.content;
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
                        ControlAcceleratorSupport.removeAcceleratorsFromScene(contextMenu.getItems(), Tab.this);
                    }
                    ContextMenu contextMenu3 = (ContextMenu)this.get();
                    this.contextMenuRef = new WeakReference<ContextMenu>(contextMenu3);
                    if (contextMenu3 != null) {
                        ControlAcceleratorSupport.addAcceleratorsIntoScene(contextMenu3.getItems(), Tab.this);
                    }
                }
            };
        }
        return this.contextMenu;
    }

    public final void setClosable(boolean bl) {
        this.closableProperty().set(bl);
    }

    public final boolean isClosable() {
        return this.closable == null ? true : this.closable.get();
    }

    public final BooleanProperty closableProperty() {
        if (this.closable == null) {
            this.closable = new SimpleBooleanProperty(this, "closable", true);
        }
        return this.closable;
    }

    public final void setOnSelectionChanged(EventHandler<Event> eventHandler) {
        this.onSelectionChangedProperty().set(eventHandler);
    }

    public final EventHandler<Event> getOnSelectionChanged() {
        return this.onSelectionChanged == null ? null : (EventHandler)this.onSelectionChanged.get();
    }

    public final ObjectProperty<EventHandler<Event>> onSelectionChangedProperty() {
        if (this.onSelectionChanged == null) {
            this.onSelectionChanged = new ObjectPropertyBase<EventHandler<Event>>(){

                @Override
                protected void invalidated() {
                    Tab.this.setEventHandler(SELECTION_CHANGED_EVENT, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Tab.this;
                }

                @Override
                public String getName() {
                    return "onSelectionChanged";
                }
            };
        }
        return this.onSelectionChanged;
    }

    public final void setOnClosed(EventHandler<Event> eventHandler) {
        this.onClosedProperty().set(eventHandler);
    }

    public final EventHandler<Event> getOnClosed() {
        return this.onClosed == null ? null : (EventHandler)this.onClosed.get();
    }

    public final ObjectProperty<EventHandler<Event>> onClosedProperty() {
        if (this.onClosed == null) {
            this.onClosed = new ObjectPropertyBase<EventHandler<Event>>(){

                @Override
                protected void invalidated() {
                    Tab.this.setEventHandler(CLOSED_EVENT, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Tab.this;
                }

                @Override
                public String getName() {
                    return "onClosed";
                }
            };
        }
        return this.onClosed;
    }

    public final void setTooltip(Tooltip tooltip) {
        this.tooltipProperty().setValue(tooltip);
    }

    public final Tooltip getTooltip() {
        return this.tooltip == null ? null : (Tooltip)this.tooltip.getValue();
    }

    public final ObjectProperty<Tooltip> tooltipProperty() {
        if (this.tooltip == null) {
            this.tooltip = new SimpleObjectProperty<Tooltip>(this, "tooltip");
        }
        return this.tooltip;
    }

    public final void setDisable(boolean bl) {
        this.disableProperty().set(bl);
    }

    public final boolean isDisable() {
        return this.disable == null ? false : this.disable.get();
    }

    public final BooleanProperty disableProperty() {
        if (this.disable == null) {
            this.disable = new BooleanPropertyBase(false){

                @Override
                protected void invalidated() {
                    Tab.this.updateDisabled();
                }

                @Override
                public Object getBean() {
                    return Tab.this;
                }

                @Override
                public String getName() {
                    return "disable";
                }
            };
        }
        return this.disable;
    }

    private final void setDisabled(boolean bl) {
        this.disabledPropertyImpl().set(bl);
    }

    public final boolean isDisabled() {
        return this.disabled == null ? false : this.disabled.get();
    }

    public final ReadOnlyBooleanProperty disabledProperty() {
        return this.disabledPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyBooleanWrapper disabledPropertyImpl() {
        if (this.disabled == null) {
            this.disabled = new ReadOnlyBooleanWrapper(){

                @Override
                public Object getBean() {
                    return Tab.this;
                }

                @Override
                public String getName() {
                    return "disabled";
                }
            };
        }
        return this.disabled;
    }

    private void updateDisabled() {
        boolean bl = this.isDisable() || this.getTabPane() != null && this.getTabPane().isDisabled();
        this.setDisabled(bl);
        Node node = this.getContent();
        if (node != null) {
            node.setDisable(bl);
        }
    }

    public final ObjectProperty<EventHandler<Event>> onCloseRequestProperty() {
        if (this.onCloseRequest == null) {
            this.onCloseRequest = new ObjectPropertyBase<EventHandler<Event>>(){

                @Override
                protected void invalidated() {
                    Tab.this.setEventHandler(TAB_CLOSE_REQUEST_EVENT, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return Tab.this;
                }

                @Override
                public String getName() {
                    return "onCloseRequest";
                }
            };
        }
        return this.onCloseRequest;
    }

    public EventHandler<Event> getOnCloseRequest() {
        if (this.onCloseRequest == null) {
            return null;
        }
        return (EventHandler)this.onCloseRequest.get();
    }

    public void setOnCloseRequest(EventHandler<Event> eventHandler) {
        this.onCloseRequestProperty().set(eventHandler);
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

    @Override
    public ObservableList<String> getStyleClass() {
        return this.styleClass;
    }

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain eventDispatchChain) {
        return eventDispatchChain.prepend(this.eventHandlerManager);
    }

    <E extends Event> void setEventHandler(EventType<E> eventType, EventHandler<E> eventHandler) {
        this.eventHandlerManager.setEventHandler(eventType, eventHandler);
    }

    Node lookup(String string) {
        if (string == null) {
            return null;
        }
        Node node = null;
        if (this.getContent() != null) {
            node = this.getContent().lookup(string);
        }
        if (node == null && this.getGraphic() != null) {
            node = this.getGraphic().lookup(string);
        }
        return node;
    }

    List<Node> lookupAll(String string) {
        Set<Node> set;
        ArrayList<Node> arrayList = new ArrayList<Node>();
        if (this.getContent() != null && !(set = this.getContent().lookupAll(string)).isEmpty()) {
            arrayList.addAll(set);
        }
        if (this.getGraphic() != null && !(set = this.getGraphic().lookupAll(string)).isEmpty()) {
            arrayList.addAll(set);
        }
        return arrayList;
    }

    @Override
    public String getTypeSelector() {
        return "Tab";
    }

    @Override
    public Styleable getStyleableParent() {
        return this.getTabPane();
    }

    @Override
    public final ObservableSet<PseudoClass> getPseudoClassStates() {
        return FXCollections.emptyObservableSet();
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return Tab.getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return Collections.emptyList();
    }
}

