/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.beans.IDProperty;
import com.sun.javafx.event.EventHandlerManager;
import com.sun.javafx.scene.control.ContextMenuContent;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
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
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventTarget;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.skin.ContextMenuSkin;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;

@IDProperty(value="id")
public class MenuItem
implements EventTarget,
Styleable {
    private final ObservableList<String> styleClass = FXCollections.observableArrayList();
    final EventHandlerManager eventHandlerManager = new EventHandlerManager(this);
    private Object userData;
    private ObservableMap<Object, Object> properties;
    private StringProperty id;
    private StringProperty style;
    private ReadOnlyObjectWrapper<Menu> parentMenu;
    private ReadOnlyObjectWrapper<ContextMenu> parentPopup;
    private StringProperty text;
    private ObjectProperty<Node> graphic;
    private ObjectProperty<EventHandler<ActionEvent>> onAction;
    public static final EventType<Event> MENU_VALIDATION_EVENT = new EventType<Event>(Event.ANY, "MENU_VALIDATION_EVENT");
    private ObjectProperty<EventHandler<Event>> onMenuValidation;
    private BooleanProperty disable;
    private BooleanProperty visible;
    private ObjectProperty<KeyCombination> accelerator;
    private BooleanProperty mnemonicParsing;
    private static final String DEFAULT_STYLE_CLASS = "menu-item";

    public MenuItem() {
        this(null, null);
    }

    public MenuItem(String string) {
        this(string, null);
    }

    public MenuItem(String string, Node node) {
        this.setText(string);
        this.setGraphic(node);
        this.styleClass.add(DEFAULT_STYLE_CLASS);
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

    protected final void setParentMenu(Menu menu) {
        this.parentMenuPropertyImpl().set(menu);
    }

    public final Menu getParentMenu() {
        return this.parentMenu == null ? null : (Menu)this.parentMenu.get();
    }

    public final ReadOnlyObjectProperty<Menu> parentMenuProperty() {
        return this.parentMenuPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<Menu> parentMenuPropertyImpl() {
        if (this.parentMenu == null) {
            this.parentMenu = new ReadOnlyObjectWrapper(this, "parentMenu");
        }
        return this.parentMenu;
    }

    protected final void setParentPopup(ContextMenu contextMenu) {
        this.parentPopupPropertyImpl().set(contextMenu);
    }

    public final ContextMenu getParentPopup() {
        return this.parentPopup == null ? null : (ContextMenu)this.parentPopup.get();
    }

    public final ReadOnlyObjectProperty<ContextMenu> parentPopupProperty() {
        return this.parentPopupPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyObjectWrapper<ContextMenu> parentPopupPropertyImpl() {
        if (this.parentPopup == null) {
            this.parentPopup = new ReadOnlyObjectWrapper(this, "parentPopup");
        }
        return this.parentPopup;
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

    public final void setOnAction(EventHandler<ActionEvent> eventHandler) {
        this.onActionProperty().set(eventHandler);
    }

    public final EventHandler<ActionEvent> getOnAction() {
        return this.onAction == null ? null : (EventHandler)this.onAction.get();
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        if (this.onAction == null) {
            this.onAction = new ObjectPropertyBase<EventHandler<ActionEvent>>(){

                @Override
                protected void invalidated() {
                    MenuItem.this.eventHandlerManager.setEventHandler(ActionEvent.ACTION, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return MenuItem.this;
                }

                @Override
                public String getName() {
                    return "onAction";
                }
            };
        }
        return this.onAction;
    }

    public final void setOnMenuValidation(EventHandler<Event> eventHandler) {
        this.onMenuValidationProperty().set(eventHandler);
    }

    public final EventHandler<Event> getOnMenuValidation() {
        return this.onMenuValidation == null ? null : (EventHandler)this.onMenuValidation.get();
    }

    public final ObjectProperty<EventHandler<Event>> onMenuValidationProperty() {
        if (this.onMenuValidation == null) {
            this.onMenuValidation = new ObjectPropertyBase<EventHandler<Event>>(){

                @Override
                protected void invalidated() {
                    MenuItem.this.eventHandlerManager.setEventHandler(MENU_VALIDATION_EVENT, (EventHandler)this.get());
                }

                @Override
                public Object getBean() {
                    return MenuItem.this;
                }

                @Override
                public String getName() {
                    return "onMenuValidation";
                }
            };
        }
        return this.onMenuValidation;
    }

    public final void setDisable(boolean bl) {
        this.disableProperty().set(bl);
    }

    public final boolean isDisable() {
        return this.disable == null ? false : this.disable.get();
    }

    public final BooleanProperty disableProperty() {
        if (this.disable == null) {
            this.disable = new SimpleBooleanProperty(this, "disable");
        }
        return this.disable;
    }

    public final void setVisible(boolean bl) {
        this.visibleProperty().set(bl);
    }

    public final boolean isVisible() {
        return this.visible == null ? true : this.visible.get();
    }

    public final BooleanProperty visibleProperty() {
        if (this.visible == null) {
            this.visible = new SimpleBooleanProperty(this, "visible", true);
        }
        return this.visible;
    }

    public final void setAccelerator(KeyCombination keyCombination) {
        this.acceleratorProperty().set(keyCombination);
    }

    public final KeyCombination getAccelerator() {
        return this.accelerator == null ? null : (KeyCombination)this.accelerator.get();
    }

    public final ObjectProperty<KeyCombination> acceleratorProperty() {
        if (this.accelerator == null) {
            this.accelerator = new SimpleObjectProperty<KeyCombination>(this, "accelerator");
        }
        return this.accelerator;
    }

    public final void setMnemonicParsing(boolean bl) {
        this.mnemonicParsingProperty().set(bl);
    }

    public final boolean isMnemonicParsing() {
        return this.mnemonicParsing == null ? true : this.mnemonicParsing.get();
    }

    public final BooleanProperty mnemonicParsingProperty() {
        if (this.mnemonicParsing == null) {
            this.mnemonicParsing = new SimpleBooleanProperty(this, "mnemonicParsing", true);
        }
        return this.mnemonicParsing;
    }

    @Override
    public ObservableList<String> getStyleClass() {
        return this.styleClass;
    }

    public void fire() {
        Event.fireEvent(this, new ActionEvent(this, this));
    }

    public <E extends Event> void addEventHandler(EventType<E> eventType, EventHandler<E> eventHandler) {
        this.eventHandlerManager.addEventHandler(eventType, eventHandler);
    }

    public <E extends Event> void removeEventHandler(EventType<E> eventType, EventHandler<E> eventHandler) {
        this.eventHandlerManager.removeEventHandler(eventType, eventHandler);
    }

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain eventDispatchChain) {
        if (this.getParentPopup() != null) {
            this.getParentPopup().buildEventDispatchChain(eventDispatchChain);
        }
        if (this.getParentMenu() != null) {
            this.getParentMenu().buildEventDispatchChain(eventDispatchChain);
        }
        return eventDispatchChain.prepend(this.eventHandlerManager);
    }

    public Object getUserData() {
        return this.userData;
    }

    public void setUserData(Object object) {
        this.userData = object;
    }

    public ObservableMap<Object, Object> getProperties() {
        if (this.properties == null) {
            this.properties = FXCollections.observableMap(new HashMap());
        }
        return this.properties;
    }

    @Override
    public String getTypeSelector() {
        return "MenuItem";
    }

    @Override
    public Styleable getStyleableParent() {
        if (this.getParentMenu() == null) {
            return this.getParentPopup();
        }
        return this.getParentMenu();
    }

    @Override
    public final ObservableSet<PseudoClass> getPseudoClassStates() {
        return FXCollections.emptyObservableSet();
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return Collections.emptyList();
    }

    @Override
    public Node getStyleableNode() {
        ContextMenu contextMenu = this.getParentPopup();
        if (contextMenu == null || !(contextMenu.getSkin() instanceof ContextMenuSkin)) {
            return null;
        }
        ContextMenuSkin contextMenuSkin = (ContextMenuSkin)contextMenu.getSkin();
        if (!(contextMenuSkin.getNode() instanceof ContextMenuContent)) {
            return null;
        }
        ContextMenuContent contextMenuContent = (ContextMenuContent)contextMenuSkin.getNode();
        VBox vBox = contextMenuContent.getItemsContainer();
        MenuItem menuItem = this;
        ObservableList<Node> observableList = vBox.getChildrenUnmodifiable();
        for (int i = 0; i < observableList.size(); ++i) {
            ContextMenuContent.MenuItemContainer menuItemContainer;
            if (!(observableList.get(i) instanceof ContextMenuContent.MenuItemContainer) || !menuItem.equals((menuItemContainer = (ContextMenuContent.MenuItemContainer)observableList.get(i)).getItem())) continue;
            return menuItemContainer;
        }
        return null;
    }

    public String toString() {
        boolean bl;
        StringBuilder stringBuilder = new StringBuilder(this.getClass().getSimpleName());
        boolean bl2 = this.id != null && !"".equals(this.getId());
        boolean bl3 = bl = !this.getStyleClass().isEmpty();
        if (!bl2) {
            stringBuilder.append('@');
            stringBuilder.append(Integer.toHexString(this.hashCode()));
        } else {
            stringBuilder.append("[id=");
            stringBuilder.append(this.getId());
            if (!bl) {
                stringBuilder.append("]");
            }
        }
        if (bl) {
            if (!bl2) {
                stringBuilder.append('[');
            } else {
                stringBuilder.append(", ");
            }
            stringBuilder.append("styleClass=");
            stringBuilder.append(this.getStyleClass());
            stringBuilder.append("]");
        }
        return stringBuilder.toString();
    }
}

