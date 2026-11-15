/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Side;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.MenuButtonSkin;

public class MenuButton
extends ButtonBase {
    public static final EventType<Event> ON_SHOWING = new EventType<Event>(Event.ANY, "MENU_BUTTON_ON_SHOWING");
    public static final EventType<Event> ON_SHOWN = new EventType<Event>(Event.ANY, "MENU_BUTTON_ON_SHOWN");
    public static final EventType<Event> ON_HIDING = new EventType<Event>(Event.ANY, "MENU_BUTTON_ON_HIDING");
    public static final EventType<Event> ON_HIDDEN = new EventType<Event>(Event.ANY, "MENU_BUTTON_ON_HIDDEN");
    private final ObservableList<MenuItem> items = FXCollections.observableArrayList();
    private ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing", false){

        @Override
        protected void invalidated() {
            MenuButton.this.pseudoClassStateChanged(PSEUDO_CLASS_SHOWING, this.get());
            super.invalidated();
        }
    };
    private ObjectProperty<Side> popupSide;
    private ObjectProperty<EventHandler<Event>> onShowing = new ObjectPropertyBase<EventHandler<Event>>(){

        @Override
        protected void invalidated() {
            MenuButton.this.setEventHandler(ON_SHOWING, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return MenuButton.this;
        }

        @Override
        public String getName() {
            return "onShowing";
        }
    };
    private ObjectProperty<EventHandler<Event>> onShown = new ObjectPropertyBase<EventHandler<Event>>(){

        @Override
        protected void invalidated() {
            MenuButton.this.setEventHandler(ON_SHOWN, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return MenuButton.this;
        }

        @Override
        public String getName() {
            return "onShown";
        }
    };
    private ObjectProperty<EventHandler<Event>> onHiding = new ObjectPropertyBase<EventHandler<Event>>(){

        @Override
        protected void invalidated() {
            MenuButton.this.setEventHandler(ON_HIDING, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return MenuButton.this;
        }

        @Override
        public String getName() {
            return "onHiding";
        }
    };
    private ObjectProperty<EventHandler<Event>> onHidden = new ObjectPropertyBase<EventHandler<Event>>(){

        @Override
        protected void invalidated() {
            MenuButton.this.setEventHandler(ON_HIDDEN, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return MenuButton.this;
        }

        @Override
        public String getName() {
            return "onHidden";
        }
    };
    private static final String DEFAULT_STYLE_CLASS = "menu-button";
    private static final PseudoClass PSEUDO_CLASS_OPENVERTICALLY = PseudoClass.getPseudoClass("openvertically");
    private static final PseudoClass PSEUDO_CLASS_SHOWING = PseudoClass.getPseudoClass("showing");

    public MenuButton() {
        this(null, null);
    }

    public MenuButton(String string) {
        this(string, null);
    }

    public MenuButton(String string, Node node) {
        this(string, node, null);
    }

    public MenuButton(String string, Node node, MenuItem ... menuItemArray) {
        if (string != null) {
            this.setText(string);
        }
        if (node != null) {
            this.setGraphic(node);
        }
        if (menuItemArray != null) {
            this.getItems().addAll((MenuItem[])menuItemArray);
        }
        this.getStyleClass().setAll((String[])new String[]{DEFAULT_STYLE_CLASS});
        this.setAccessibleRole(AccessibleRole.MENU_BUTTON);
        this.setMnemonicParsing(true);
        this.pseudoClassStateChanged(PSEUDO_CLASS_OPENVERTICALLY, true);
    }

    public final ObservableList<MenuItem> getItems() {
        return this.items;
    }

    private void setShowing(boolean bl) {
        Event.fireEvent(this, bl ? new Event(ON_SHOWING) : new Event(ON_HIDING));
        this.showing.set(bl);
        Event.fireEvent(this, bl ? new Event(ON_SHOWN) : new Event(ON_HIDDEN));
    }

    public final boolean isShowing() {
        return this.showing.get();
    }

    public final ReadOnlyBooleanProperty showingProperty() {
        return this.showing.getReadOnlyProperty();
    }

    public final void setPopupSide(Side side) {
        this.popupSideProperty().set(side);
    }

    public final Side getPopupSide() {
        return this.popupSide == null ? Side.BOTTOM : (Side)((Object)this.popupSide.get());
    }

    public final ObjectProperty<Side> popupSideProperty() {
        if (this.popupSide == null) {
            this.popupSide = new ObjectPropertyBase<Side>(Side.BOTTOM){

                @Override
                protected void invalidated() {
                    Side side = (Side)((Object)this.get());
                    boolean bl = side == Side.TOP || side == Side.BOTTOM;
                    MenuButton.this.pseudoClassStateChanged(PSEUDO_CLASS_OPENVERTICALLY, bl);
                }

                @Override
                public Object getBean() {
                    return MenuButton.this;
                }

                @Override
                public String getName() {
                    return "popupSide";
                }
            };
        }
        return this.popupSide;
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

    public void show() {
        if (!this.isDisabled() && !this.showing.isBound()) {
            this.setShowing(true);
        }
    }

    public void hide() {
        if (!this.showing.isBound()) {
            this.setShowing(false);
        }
    }

    @Override
    public void fire() {
        if (!this.isDisabled()) {
            this.fireEvent(new ActionEvent());
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new MenuButtonSkin(this);
    }

    @Override
    public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
        switch (accessibleAction) {
            case FIRE: {
                if (this.isShowing()) {
                    this.hide();
                    break;
                }
                this.show();
                break;
            }
            default: {
                super.executeAccessibleAction(accessibleAction, new Object[0]);
            }
        }
    }
}

