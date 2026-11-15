/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.DefaultProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.AccessibleAction;
import javafx.scene.AccessibleAttribute;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Separator;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.Skin;
import javafx.scene.control.skin.ChoiceBoxSkin;
import javafx.util.StringConverter;

@DefaultProperty(value="items")
public class ChoiceBox<T>
extends Control {
    public static final EventType<Event> ON_SHOWING = new EventType<Event>(Event.ANY, "CHOICE_BOX_ON_SHOWING");
    public static final EventType<Event> ON_SHOWN = new EventType<Event>(Event.ANY, "CHOICE_BOX_ON_SHOWN");
    public static final EventType<Event> ON_HIDING = new EventType<Event>(Event.ANY, "CHOICE_BOX_ON_HIDING");
    public static final EventType<Event> ON_HIDDEN = new EventType<Event>(Event.ANY, "CHOICE_BOX_ON_HIDDEN");
    private ObjectProperty<SingleSelectionModel<T>> selectionModel = new SimpleObjectProperty<SingleSelectionModel<T>>(this, "selectionModel"){
        private SelectionModel<T> oldSM;
        {
            this.oldSM = null;
        }

        @Override
        protected void invalidated() {
            SelectionModel selectionModel;
            if (this.oldSM != null) {
                this.oldSM.selectedItemProperty().removeListener(ChoiceBox.this.selectedItemListener);
            }
            this.oldSM = selectionModel = (SelectionModel)this.get();
            if (selectionModel != null) {
                selectionModel.selectedItemProperty().addListener(ChoiceBox.this.selectedItemListener);
                if (selectionModel.getSelectedItem() != null && !ChoiceBox.this.valueProperty().isBound()) {
                    ChoiceBox.this.setValue(selectionModel.getSelectedItem());
                }
            }
        }
    };
    private ChangeListener<T> selectedItemListener = (observableValue, object, object2) -> {
        if (!this.valueProperty().isBound()) {
            this.setValue(object2);
        }
    };
    private ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(){

        @Override
        protected void invalidated() {
            ChoiceBox.this.pseudoClassStateChanged(SHOWING_PSEUDOCLASS_STATE, this.get());
            ChoiceBox.this.notifyAccessibleAttributeChanged(AccessibleAttribute.EXPANDED);
        }

        @Override
        public Object getBean() {
            return ChoiceBox.this;
        }

        @Override
        public String getName() {
            return "showing";
        }
    };
    private ObjectProperty<ObservableList<T>> items = new ObjectPropertyBase<ObservableList<T>>(){
        ObservableList<T> old;

        @Override
        protected void invalidated() {
            ObservableList observableList = (ObservableList)this.get();
            if (this.old != observableList) {
                SingleSelectionModel singleSelectionModel;
                if (this.old != null) {
                    this.old.removeListener(ChoiceBox.this.itemsListener);
                }
                if (observableList != null) {
                    observableList.addListener(ChoiceBox.this.itemsListener);
                }
                if ((singleSelectionModel = ChoiceBox.this.getSelectionModel()) != null) {
                    if (observableList != null && observableList.isEmpty()) {
                        singleSelectionModel.clearSelection();
                    } else if (singleSelectionModel.getSelectedIndex() == -1 && singleSelectionModel.getSelectedItem() != null) {
                        int n = ChoiceBox.this.getItems().indexOf(singleSelectionModel.getSelectedItem());
                        if (n != -1) {
                            singleSelectionModel.setSelectedIndex(n);
                        }
                    } else {
                        singleSelectionModel.clearSelection();
                    }
                }
                this.old = observableList;
            }
        }

        @Override
        public Object getBean() {
            return ChoiceBox.this;
        }

        @Override
        public String getName() {
            return "items";
        }
    };
    private final ListChangeListener<T> itemsListener = change -> {
        SingleSelectionModel<T> singleSelectionModel = this.getSelectionModel();
        if (singleSelectionModel != null) {
            if (this.getItems() == null || this.getItems().isEmpty()) {
                singleSelectionModel.clearSelection();
            } else {
                int n = this.getItems().indexOf(singleSelectionModel.getSelectedItem());
                singleSelectionModel.setSelectedIndex(n);
            }
        }
        if (singleSelectionModel != null) {
            Object t = singleSelectionModel.getSelectedItem();
            while (change.next()) {
                if (t == null || !change.getRemoved().contains(t)) continue;
                singleSelectionModel.clearSelection();
                break;
            }
        }
    };
    private ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<Object>(this, "converter", null);
    private ObjectProperty<T> value = new SimpleObjectProperty<T>(this, "value"){

        @Override
        protected void invalidated() {
            super.invalidated();
            ChoiceBox.this.fireEvent(new ActionEvent());
            SingleSelectionModel singleSelectionModel = ChoiceBox.this.getSelectionModel();
            if (singleSelectionModel != null) {
                singleSelectionModel.select(super.getValue());
            }
            ChoiceBox.this.notifyAccessibleAttributeChanged(AccessibleAttribute.TEXT);
        }
    };
    private ObjectProperty<EventHandler<ActionEvent>> onAction = new ObjectPropertyBase<EventHandler<ActionEvent>>(){

        @Override
        protected void invalidated() {
            ChoiceBox.this.setEventHandler(ActionEvent.ACTION, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return ChoiceBox.this;
        }

        @Override
        public String getName() {
            return "onAction";
        }
    };
    private ObjectProperty<EventHandler<Event>> onShowing = new ObjectPropertyBase<EventHandler<Event>>(){

        @Override
        protected void invalidated() {
            ChoiceBox.this.setEventHandler(ON_SHOWING, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return ChoiceBox.this;
        }

        @Override
        public String getName() {
            return "onShowing";
        }
    };
    private ObjectProperty<EventHandler<Event>> onShown = new ObjectPropertyBase<EventHandler<Event>>(){

        @Override
        protected void invalidated() {
            ChoiceBox.this.setEventHandler(ON_SHOWN, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return ChoiceBox.this;
        }

        @Override
        public String getName() {
            return "onShown";
        }
    };
    private ObjectProperty<EventHandler<Event>> onHiding = new ObjectPropertyBase<EventHandler<Event>>(){

        @Override
        protected void invalidated() {
            ChoiceBox.this.setEventHandler(ON_HIDING, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return ChoiceBox.this;
        }

        @Override
        public String getName() {
            return "onHiding";
        }
    };
    private ObjectProperty<EventHandler<Event>> onHidden = new ObjectPropertyBase<EventHandler<Event>>(){

        @Override
        protected void invalidated() {
            ChoiceBox.this.setEventHandler(ON_HIDDEN, (EventHandler)this.get());
        }

        @Override
        public Object getBean() {
            return ChoiceBox.this;
        }

        @Override
        public String getName() {
            return "onHidden";
        }
    };
    private static final PseudoClass SHOWING_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("showing");

    public ChoiceBox() {
        this(FXCollections.observableArrayList());
    }

    public ChoiceBox(ObservableList<T> observableList) {
        this.getStyleClass().setAll((String[])new String[]{"choice-box"});
        this.setAccessibleRole(AccessibleRole.COMBO_BOX);
        this.setItems(observableList);
        this.setSelectionModel(new ChoiceBoxSelectionModel(this));
        this.valueProperty().addListener((observableValue, object, object2) -> {
            if (this.getItems() == null) {
                return;
            }
            int n = this.getItems().indexOf(object2);
            if (n > -1) {
                this.getSelectionModel().select(n);
            }
        });
    }

    public final void setSelectionModel(SingleSelectionModel<T> singleSelectionModel) {
        this.selectionModel.set(singleSelectionModel);
    }

    public final SingleSelectionModel<T> getSelectionModel() {
        return (SingleSelectionModel)this.selectionModel.get();
    }

    public final ObjectProperty<SingleSelectionModel<T>> selectionModelProperty() {
        return this.selectionModel;
    }

    public final boolean isShowing() {
        return this.showing.get();
    }

    public final ReadOnlyBooleanProperty showingProperty() {
        return this.showing.getReadOnlyProperty();
    }

    private void setShowing(boolean bl) {
        Event.fireEvent(this, bl ? new Event(ON_SHOWING) : new Event(ON_HIDING));
        this.showing.set(bl);
        Event.fireEvent(this, bl ? new Event(ON_SHOWN) : new Event(ON_HIDDEN));
    }

    public final void setItems(ObservableList<T> observableList) {
        this.items.set(observableList);
    }

    public final ObservableList<T> getItems() {
        return (ObservableList)this.items.get();
    }

    public final ObjectProperty<ObservableList<T>> itemsProperty() {
        return this.items;
    }

    public ObjectProperty<StringConverter<T>> converterProperty() {
        return this.converter;
    }

    public final void setConverter(StringConverter<T> stringConverter) {
        this.converterProperty().set(stringConverter);
    }

    public final StringConverter<T> getConverter() {
        return (StringConverter)this.converterProperty().get();
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
        this.setShowing(false);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ChoiceBoxSkin(this);
    }

    @Override
    public Object queryAccessibleAttribute(AccessibleAttribute accessibleAttribute, Object ... objectArray) {
        switch (accessibleAttribute) {
            case TEXT: {
                String string = this.getAccessibleText();
                if (string != null && !string.isEmpty()) {
                    return string;
                }
                Object object = super.queryAccessibleAttribute(accessibleAttribute, objectArray);
                if (object != null) {
                    return object;
                }
                StringConverter<T> stringConverter = this.getConverter();
                if (stringConverter == null) {
                    return this.getValue() != null ? this.getValue().toString() : "";
                }
                return stringConverter.toString(this.getValue());
            }
            case EXPANDED: {
                return this.isShowing();
            }
        }
        return super.queryAccessibleAttribute(accessibleAttribute, objectArray);
    }

    @Override
    public void executeAccessibleAction(AccessibleAction accessibleAction, Object ... objectArray) {
        switch (accessibleAction) {
            case COLLAPSE: {
                this.hide();
                break;
            }
            case EXPAND: {
                this.show();
                break;
            }
            default: {
                super.executeAccessibleAction(accessibleAction, new Object[0]);
            }
        }
    }

    static class ChoiceBoxSelectionModel<T>
    extends SingleSelectionModel<T> {
        private final ChoiceBox<T> choiceBox;

        public ChoiceBoxSelectionModel(ChoiceBox<T> choiceBox) {
            if (choiceBox == null) {
                throw new NullPointerException("ChoiceBox can not be null");
            }
            this.choiceBox = choiceBox;
            ListChangeListener listChangeListener = change -> {
                int n;
                if (this.choiceBox.getItems() == null || this.choiceBox.getItems().isEmpty()) {
                    this.setSelectedIndex(-1);
                } else if (this.getSelectedIndex() == -1 && this.getSelectedItem() != null && (n = this.choiceBox.getItems().indexOf(this.getSelectedItem())) != -1) {
                    this.setSelectedIndex(n);
                }
            };
            if (this.choiceBox.getItems() != null) {
                this.choiceBox.getItems().addListener(listChangeListener);
            }
            ChangeListener<ObservableList> changeListener = (observableValue, observableList, observableList2) -> {
                int n;
                if (observableList != null) {
                    observableList.removeListener(listChangeListener);
                }
                if (observableList2 != null) {
                    observableList2.addListener(listChangeListener);
                }
                this.setSelectedIndex(-1);
                if (this.getSelectedItem() != null && (n = this.choiceBox.getItems().indexOf(this.getSelectedItem())) != -1) {
                    this.setSelectedIndex(n);
                }
            };
            this.choiceBox.itemsProperty().addListener(changeListener);
        }

        @Override
        protected T getModelItem(int n) {
            ObservableList<T> observableList = this.choiceBox.getItems();
            if (observableList == null) {
                return null;
            }
            if (n < 0 || n >= observableList.size()) {
                return null;
            }
            return (T)observableList.get(n);
        }

        @Override
        protected int getItemCount() {
            ObservableList<T> observableList = this.choiceBox.getItems();
            return observableList == null ? 0 : observableList.size();
        }

        @Override
        public void select(int n) {
            super.select(n);
            if (this.choiceBox.isShowing()) {
                this.choiceBox.hide();
            }
        }

        @Override
        public void selectPrevious() {
            for (int i = this.getSelectedIndex() - 1; i >= 0; --i) {
                T t = this.getModelItem(i);
                if (t instanceof Separator) {
                    continue;
                }
                this.select(i);
                break;
            }
        }

        @Override
        public void selectNext() {
            for (int i = this.getSelectedIndex() + 1; i < this.getItemCount(); ++i) {
                T t = this.getModelItem(i);
                if (t instanceof Separator) {
                    continue;
                }
                this.select(i);
                break;
            }
        }
    }
}

