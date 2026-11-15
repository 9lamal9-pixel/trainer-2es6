/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.control.Control;

public abstract class ComboBoxBase<T>
extends Control {
    public static final EventType<Event> ON_SHOWING = new EventType<Event>(Event.ANY, "COMBO_BOX_BASE_ON_SHOWING");
    public static final EventType<Event> ON_SHOWN = new EventType<Event>(Event.ANY, "COMBO_BOX_BASE_ON_SHOWN");
    public static final EventType<Event> ON_HIDING = new EventType<Event>(Event.ANY, "COMBO_BOX_BASE_ON_HIDING");
    public static final EventType<Event> ON_HIDDEN = new EventType<Event>(Event.ANY, "COMBO_BOX_BASE_ON_HIDDEN");
    private ObjectProperty<T> value = new SimpleObjectProperty(this, "value");
    private BooleanProperty editable = new SimpleBooleanProperty(this, "editable", false){

        @Override
        protected void invalidated() {
            ComboBoxBase.this.pseudoClassStateChanged(PSEUDO_CLASS_EDITABLE, this.get());
        }
    };
    private ReadOnlyBooleanWrapper showing;
    private StringProperty promptText = new SimpleStringProperty(this, "promptText", null){

        @Override
        protected void invalidated() {
            String string = this.get();
            if (string != null && string.contains("\n")) {
                string = string.replace("\n", "");
                this.set(string);
            }
        }
    };
    private BooleanProperty armed = new SimpleBooleanProperty(this, "armed", false){

        @Override
        protected void invalidated() {
            ComboBoxBase.this.pseudoClassStateChanged(PSEUDO_CLASS_ARMED, this.get());
        }
    };
    private ObjectProperty<EventHandler<ActionEvent>> onAction = new ObjectPropertyBase<EventHandler<ActionEvent>>(){

        @Override
        protected void invalidated() {
            ComboBoxBase.this.setEventHandler(ActionEvent.ACTION, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return ComboBoxBase.this;
        }

        @Override
        public String getName() {
            return "onAction";
        }
    };
    private ObjectProperty<EventHandler<Event>> onShowing = new ObjectPropertyBase<EventHandler<Event>>(){

        @Override
        protected void invalidated() {
            ComboBoxBase.this.setEventHandler(ON_SHOWING, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return ComboBoxBase.this;
        }

        @Override
        public String getName() {
            return "onShowing";
        }
    };
    private ObjectProperty<EventHandler<Event>> onShown = new ObjectPropertyBase<EventHandler<Event>>(){

        @Override
        protected void invalidated() {
            ComboBoxBase.this.setEventHandler(ON_SHOWN, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return ComboBoxBase.this;
        }

        @Override
        public String getName() {
            return "onShown";
        }
    };
    private ObjectProperty<EventHandler<Event>> onHiding = new ObjectPropertyBase<EventHandler<Event>>(){

        @Override
        protected void invalidated() {
            ComboBoxBase.this.setEventHandler(ON_HIDING, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return ComboBoxBase.this;
        }

        @Override
        public String getName() {
            return "onHiding";
        }
    };
    private ObjectProperty<EventHandler<Event>> onHidden = new ObjectPropertyBase<EventHandler<Event>>(){

        @Override
        protected void invalidated() {
            ComboBoxBase.this.setEventHandler(ON_HIDDEN, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return ComboBoxBase.this;
        }

        @Override
        public String getName() {
            return "onHidden";
        }
    };
    private static final String DEFAULT_STYLE_CLASS = "combo-box-base";
    private static final PseudoClass PSEUDO_CLASS_EDITABLE = PseudoClass.getPseudoClass("editable");
    private static final PseudoClass PSEUDO_CLASS_SHOWING = PseudoClass.getPseudoClass("showing");
    private static final PseudoClass PSEUDO_CLASS_ARMED = PseudoClass.getPseudoClass("armed");

    public ComboBoxBase() {
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
        this.getProperties().addListener(change -> {
            if (change.wasAdded() && change.getKey() == "FOCUSED") {
                this.setFocused((Boolean)change.getValueAdded());
                this.getProperties().remove("FOCUSED");
            }
        });
    }

    public ObjectProperty<T> valueProperty() {
        return this.value;
    }

    public final void setValue(T t) {
        this.valueProperty().set(t);
    }

    public final T getValue() {
        return this.valueProperty().get();
    }

    public BooleanProperty editableProperty() {
        return this.editable;
    }

    public final void setEditable(boolean bl) {
        this.editableProperty().set(bl);
    }

    public final boolean isEditable() {
        return this.editableProperty().get();
    }

    public ReadOnlyBooleanProperty showingProperty() {
        return this.showingPropertyImpl().getReadOnlyProperty();
    }

    public final boolean isShowing() {
        return this.showingPropertyImpl().get();
    }

    private void setShowing(boolean bl) {
        Event.fireEvent(this, bl ? new Event(ON_SHOWING) : new Event(ON_HIDING));
        this.showingPropertyImpl().set(bl);
        Event.fireEvent(this, bl ? new Event(ON_SHOWN) : new Event(ON_HIDDEN));
    }

    private ReadOnlyBooleanWrapper showingPropertyImpl() {
        if (this.showing == null) {
            this.showing = new ReadOnlyBooleanWrapper(false){

                @Override
                protected void invalidated() {
                    ComboBoxBase.this.pseudoClassStateChanged(PSEUDO_CLASS_SHOWING, this.get());
                    ComboBoxBase.this.notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
                }

                @Override
                public Object getBean() {
                    return ComboBoxBase.this;
                }

                @Override
                public String getName() {
                    return "showing";
                }
            };
        }
        return this.showing;
    }

    public final StringProperty promptTextProperty() {
        return this.promptText;
    }

    public final String getPromptText() {
        return (String)this.promptText.get();
    }

    public final void setPromptText(String string) {
        this.promptText.set(string);
    }

    public BooleanProperty armedProperty() {
        return this.armed;
    }

    private final void setArmed(boolean bl) {
        this.armedProperty().set(bl);
    }

    public final boolean isArmed() {
        return this.armedProperty().get();
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return this.onAction;
    }

    public final void setOnAction(EventHandler<ActionEvent> eventHandler) {
        this.onActionProperty().set(eventHandler);
    }

    public final EventHandler<ActionEvent> getOnAction() {
        return (EventHandler)this.onActionProperty().get();
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
        if (!this.isDisabled()) {
            this.setShowing(true);
        }
    }

    public void hide() {
        if (this.isShowing()) {
            this.setShowing(false);
        }
    }

    public void arm() {
        if (!this.armedProperty().isBound()) {
            this.setArmed(true);
        }
    }

    public void disarm() {
        if (!this.armedProperty().isBound()) {
            this.setArmed(false);
        }
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case EXPANDED: {
                return this.isShowing();
            }
            case EDITABLE: {
                return this.isEditable();
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    @Override
    public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
        switch (accessibleAction) {
            case EXPAND: {
                this.show();
                break;
            }
            case COLLAPSE: {
                this.hide();
                break;
            }
            default: {
                super.executeAccessibleAction(accessibleAction, new Object[0]);
            }
        }
    }
}

