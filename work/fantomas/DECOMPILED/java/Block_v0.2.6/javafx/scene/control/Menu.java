/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.collections.TrackableObservableList;
import com.sun.javafx.scene.control.Logging;
import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventDispatchChain;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;

@DefaultProperty(value="items")
public class Menu
extends MenuItem {
    public static final EventType<Event> ON_SHOWING = new EventType<Event>(Event.ANY, "MENU_ON_SHOWING");
    public static final EventType<Event> ON_SHOWN = new EventType<Event>(Event.ANY, "MENU_ON_SHOWN");
    public static final EventType<Event> ON_HIDING = new EventType<Event>(Event.ANY, "MENU_ON_HIDING");
    public static final EventType<Event> ON_HIDDEN = new EventType<Event>(Event.ANY, "MENU_ON_HIDDEN");
    private ReadOnlyBooleanWrapper showing;
    private ObjectProperty<EventHandler<Event>> onShowing = new ObjectPropertyBase<EventHandler<Event>>(){

        @Override
        protected void invalidated() {
            Menu.this.eventHandlerManager.setEventHandler(ON_SHOWING, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return Menu.this;
        }

        @Override
        public String getName() {
            return "onShowing";
        }
    };
    private ObjectProperty<EventHandler<Event>> onShown = new ObjectPropertyBase<EventHandler<Event>>(){

        @Override
        protected void invalidated() {
            Menu.this.eventHandlerManager.setEventHandler(ON_SHOWN, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return Menu.this;
        }

        @Override
        public String getName() {
            return "onShown";
        }
    };
    private ObjectProperty<EventHandler<Event>> onHiding = new ObjectPropertyBase<EventHandler<Event>>(){

        @Override
        protected void invalidated() {
            Menu.this.eventHandlerManager.setEventHandler(ON_HIDING, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return Menu.this;
        }

        @Override
        public String getName() {
            return "onHiding";
        }
    };
    private ObjectProperty<EventHandler<Event>> onHidden = new ObjectPropertyBase<EventHandler<Event>>(){

        @Override
        protected void invalidated() {
            Menu.this.eventHandlerManager.setEventHandler(ON_HIDDEN, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return Menu.this;
        }

        @Override
        public String getName() {
            return "onHidden";
        }
    };
    private final ObservableList<MenuItem> items = new TrackableObservableList<MenuItem>(){

        @Override
        protected void onChanged(ListChangeListener.Change<MenuItem> change) {
            while (change.next()) {
                for (MenuItem menuItem : change.getRemoved()) {
                    menuItem.setParentMenu(null);
                    menuItem.setParentPopup(null);
                }
                for (MenuItem menuItem : change.getAddedSubList()) {
                    if (menuItem.getParentMenu() != null) {
                        Logging.getControlsLogger().warning("Adding MenuItem " + menuItem.getText() + " that has already been added to " + menuItem.getParentMenu().getText());
                        menuItem.getParentMenu().getItems().remove(menuItem);
                    }
                    menuItem.setParentMenu(Menu.this);
                    menuItem.setParentPopup(Menu.this.getParentPopup());
                }
            }
            if (Menu.this.getItems().size() == 0 && Menu.this.isShowing()) {
                Menu.this.showingPropertyImpl().set(false);
            }
        }
    };
    private static final String DEFAULT_STYLE_CLASS = "menu";
    private static final String STYLE_CLASS_SHOWING = "showing";

    public Menu() {
        this("");
    }

    public Menu(String string) {
        this(string, null);
    }

    public Menu(String string, Node node) {
        this(string, node, null);
    }

    public Menu(String string, Node node, MenuItem ... menuItemArray) {
        super(string, node);
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
        if (menuItemArray != null) {
            this.getItems().addAll((MenuItem[])menuItemArray);
        }
        this.parentPopupProperty().addListener(observable -> {
            for (int i = 0; i < this.getItems().size(); ++i) {
                MenuItem menuItem = (MenuItem)this.getItems().get(i);
                menuItem.setParentPopup(this.getParentPopup());
            }
        });
    }

    private void setShowing(boolean bl) {
        if (this.getItems().size() == 0 || bl && this.isShowing()) {
            return;
        }
        if (bl) {
            if (this.getOnMenuValidation() != null) {
                Event.fireEvent(this, new Event(MENU_VALIDATION_EVENT));
                for (MenuItem menuItem : this.getItems()) {
                    if (menuItem instanceof Menu || menuItem.getOnMenuValidation() == null) continue;
                    Event.fireEvent(menuItem, new Event(MenuItem.MENU_VALIDATION_EVENT));
                }
            }
            Event.fireEvent(this, new Event(ON_SHOWING));
        } else {
            Event.fireEvent(this, new Event(ON_HIDING));
        }
        this.showingPropertyImpl().set(bl);
        Event.fireEvent(this, bl ? new Event(ON_SHOWN) : new Event(ON_HIDDEN));
    }

    public final boolean isShowing() {
        return this.showing == null ? false : this.showing.get();
    }

    public final ReadOnlyBooleanProperty showingProperty() {
        return this.showingPropertyImpl().getReadOnlyProperty();
    }

    private ReadOnlyBooleanWrapper showingPropertyImpl() {
        if (this.showing == null) {
            this.showing = new ReadOnlyBooleanWrapper(){

                @Override
                protected void invalidated() {
                    this.get();
                    if (Menu.this.isShowing()) {
                        Menu.this.getStyleClass().add(Menu.STYLE_CLASS_SHOWING);
                    } else {
                        Menu.this.getStyleClass().remove(Menu.STYLE_CLASS_SHOWING);
                    }
                }

                @Override
                public Object getBean() {
                    return Menu.this;
                }

                @Override
                public String getName() {
                    return Menu.STYLE_CLASS_SHOWING;
                }
            };
        }
        return this.showing;
    }

    public final ObjectProperty<EventHandler<Event>> onShowingProperty() {
        return this.onShowing;
    }

    public final void setOnShowing(EventHandler<Event> eventHandler) {
        this.onShowingProperty().set(eventHandler);
    }

    public final EventHandler<Event> getOnShowing() {
        return (EventHandler)this.onShowingProperty().get();
    }

    public final ObjectProperty<EventHandler<Event>> onShownProperty() {
        return this.onShown;
    }

    public final void setOnShown(EventHandler<Event> eventHandler) {
        this.onShownProperty().set(eventHandler);
    }

    public final EventHandler<Event> getOnShown() {
        return (EventHandler)this.onShownProperty().get();
    }

    public final ObjectProperty<EventHandler<Event>> onHidingProperty() {
        return this.onHiding;
    }

    public final void setOnHiding(EventHandler<Event> eventHandler) {
        this.onHidingProperty().set(eventHandler);
    }

    public final EventHandler<Event> getOnHiding() {
        return (EventHandler)this.onHidingProperty().get();
    }

    public final ObjectProperty<EventHandler<Event>> onHiddenProperty() {
        return this.onHidden;
    }

    public final void setOnHidden(EventHandler<Event> eventHandler) {
        this.onHiddenProperty().set(eventHandler);
    }

    public final EventHandler<Event> getOnHidden() {
        return (EventHandler)this.onHiddenProperty().get();
    }

    public final ObservableList<MenuItem> getItems() {
        return this.items;
    }

    public void show() {
        if (this.isDisable()) {
            return;
        }
        this.setShowing(true);
    }

    public void hide() {
        if (!this.isShowing()) {
            return;
        }
        for (MenuItem menuItem : this.getItems()) {
            if (!(menuItem instanceof Menu)) continue;
            Menu menu = (Menu)menuItem;
            menu.hide();
        }
        this.setShowing(false);
    }

    @Override
    public <E extends Event> void addEventHandler(EventType<E> eventType, EventHandler<E> eventHandler) {
        this.eventHandlerManager.addEventHandler(eventType, eventHandler);
    }

    @Override
    public <E extends Event> void removeEventHandler(EventType<E> eventType, EventHandler<E> eventHandler) {
        this.eventHandlerManager.removeEventHandler(eventType, eventHandler);
    }

    @Override
    public EventDispatchChain buildEventDispatchChain(EventDispatchChain eventDispatchChain) {
        return eventDispatchChain.prepend(this.eventHandlerManager);
    }
}

