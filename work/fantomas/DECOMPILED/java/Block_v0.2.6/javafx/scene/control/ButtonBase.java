/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.AccessibleAction;
import javafx.scene.Node;
import javafx.scene.control.Labeled;

public abstract class ButtonBase
extends Labeled {
    private ReadOnlyBooleanWrapper armed = new ReadOnlyBooleanWrapper(){

        @Override
        protected void invalidated() {
            ButtonBase.this.pseudoClassStateChanged(ARMED_PSEUDOCLASS_STATE, this.get());
        }

        @Override
        public Object getBean() {
            return ButtonBase.this;
        }

        @Override
        public String getName() {
            return "armed";
        }
    };
    private ObjectProperty<EventHandler<ActionEvent>> onAction = new ObjectPropertyBase<EventHandler<ActionEvent>>(){

        @Override
        protected void invalidated() {
            ButtonBase.this.setEventHandler(ActionEvent.ACTION, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return ButtonBase.this;
        }

        @Override
        public String getName() {
            return "onAction";
        }
    };
    private static final PseudoClass ARMED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("armed");

    public ButtonBase() {
    }

    public ButtonBase(String string) {
        super(string);
    }

    public ButtonBase(String string, Node node) {
        super(string, node);
    }

    public final ReadOnlyBooleanProperty armedProperty() {
        return this.armed.getReadOnlyProperty();
    }

    private void setArmed(boolean bl) {
        this.armed.set(bl);
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

    public void arm() {
        this.setArmed(true);
    }

    public void disarm() {
        this.setArmed(false);
    }

    public abstract void fire();

    @Override
    public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
        switch (accessibleAction) {
            case FIRE: {
                this.fire();
                break;
            }
            default: {
                super.executeAccessibleAction(accessibleAction, new Object[0]);
            }
        }
    }
}

