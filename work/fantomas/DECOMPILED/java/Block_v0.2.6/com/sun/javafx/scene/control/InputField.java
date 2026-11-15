/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.StringPropertyBase;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Control;

public abstract class InputField
extends Control {
    public static final int DEFAULT_PREF_COLUMN_COUNT = 12;
    private BooleanProperty editable = new SimpleBooleanProperty(this, "editable", true);
    private StringProperty promptText = new StringPropertyBase(""){

        @Override
        protected void invalidated() {
            String string = this.get();
            if (string != null && string.contains("\n")) {
                string = string.replace("\n", "");
                this.set(string);
            }
        }

        @Override
        public Object getBean() {
            return InputField.this;
        }

        @Override
        public String getName() {
            return "promptText";
        }
    };
    private IntegerProperty prefColumnCount = new IntegerPropertyBase(12){
        private int oldValue;
        {
            this.oldValue = this.get();
        }

        @Override
        protected void invalidated() {
            int n = this.get();
            if (n < 0) {
                if (this.isBound()) {
                    this.unbind();
                }
                this.set(this.oldValue);
                throw new IllegalArgumentException("value cannot be negative.");
            }
            this.oldValue = n;
        }

        @Override
        public Object getBean() {
            return InputField.this;
        }

        @Override
        public String getName() {
            return "prefColumnCount";
        }
    };
    private ObjectProperty<EventHandler<ActionEvent>> onAction = new ObjectPropertyBase<EventHandler<ActionEvent>>(){

        @Override
        protected void invalidated() {
            InputField.this.setEventHandler(ActionEvent.ACTION, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return InputField.this;
        }

        @Override
        public String getName() {
            return "onAction";
        }
    };

    public final boolean isEditable() {
        return this.editable.getValue();
    }

    public final void setEditable(boolean bl) {
        this.editable.setValue(bl);
    }

    public final BooleanProperty editableProperty() {
        return this.editable;
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

    public final IntegerProperty prefColumnCountProperty() {
        return this.prefColumnCount;
    }

    public final int getPrefColumnCount() {
        return this.prefColumnCount.getValue();
    }

    public final void setPrefColumnCount(int n) {
        this.prefColumnCount.setValue(n);
    }

    public final ObjectProperty<EventHandler<ActionEvent>> onActionProperty() {
        return this.onAction;
    }

    public final EventHandler<ActionEvent> getOnAction() {
        return (EventHandler)this.onActionProperty().get();
    }

    public final void setOnAction(EventHandler<ActionEvent> eventHandler) {
        this.onActionProperty().set(eventHandler);
    }

    public InputField() {
        this.getStyleClass().setAll((String[])new String[]{"input-field"});
    }
}

