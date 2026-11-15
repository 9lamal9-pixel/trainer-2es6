/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.cell;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CellUtils;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class CheckBoxListCell<T>
extends ListCell<T> {
    private final CheckBox checkBox;
    private ObservableValue<Boolean> booleanProperty;
    private ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this, "converter");
    private ObjectProperty<Callback<T, ObservableValue<Boolean>>> selectedStateCallback = new SimpleObjectProperty<Callback<T, ObservableValue<Boolean>>>(this, "selectedStateCallback");

    public static <T> Callback<ListView<T>, ListCell<T>> forListView(Callback<T, ObservableValue<Boolean>> callback) {
        return CheckBoxListCell.forListView(callback, CellUtils.defaultStringConverter());
    }

    public static <T> Callback<ListView<T>, ListCell<T>> forListView(Callback<T, ObservableValue<Boolean>> callback, StringConverter<T> stringConverter) {
        return listView -> new CheckBoxListCell(callback, stringConverter);
    }

    public CheckBoxListCell() {
        this((Callback<T, ObservableValue<Boolean>>)null);
    }

    public CheckBoxListCell(Callback<T, ObservableValue<Boolean>> callback) {
        this(callback, CellUtils.defaultStringConverter());
    }

    public CheckBoxListCell(Callback<T, ObservableValue<Boolean>> callback, StringConverter<T> stringConverter) {
        this.getStyleClass().add("check-box-list-cell");
        this.setSelectedStateCallback(callback);
        this.setConverter(stringConverter);
        this.checkBox = new CheckBox();
        this.setAlignment(Pos.CENTER_LEFT);
        this.setContentDisplay(ContentDisplay.LEFT);
        this.setGraphic(null);
    }

    public final ObjectProperty<StringConverter<T>> converterProperty() {
        return this.converter;
    }

    public final void setConverter(StringConverter<T> stringConverter) {
        this.converterProperty().set(stringConverter);
    }

    public final StringConverter<T> getConverter() {
        return (StringConverter)this.converterProperty().get();
    }

    public final ObjectProperty<Callback<T, ObservableValue<Boolean>>> selectedStateCallbackProperty() {
        return this.selectedStateCallback;
    }

    public final void setSelectedStateCallback(Callback<T, ObservableValue<Boolean>> callback) {
        this.selectedStateCallbackProperty().set(callback);
    }

    public final Callback<T, ObservableValue<Boolean>> getSelectedStateCallback() {
        return (Callback)this.selectedStateCallbackProperty().get();
    }

    @Override
    public void updateItem(T t, boolean bl) {
        super.updateItem(t, bl);
        if (!bl) {
            StringConverter<T> stringConverter = this.getConverter();
            Callback<T, ObservableValue<Boolean>> callback = this.getSelectedStateCallback();
            if (callback == null) {
                throw new NullPointerException("The CheckBoxListCell selectedStateCallbackProperty can not be null");
            }
            this.setGraphic(this.checkBox);
            this.setText(stringConverter != null ? stringConverter.toString(t) : (t == null ? "" : t.toString()));
            if (this.booleanProperty != null) {
                this.checkBox.selectedProperty().unbindBidirectional((BooleanProperty)this.booleanProperty);
            }
            this.booleanProperty = callback.call(t);
            if (this.booleanProperty != null) {
                this.checkBox.selectedProperty().bindBidirectional((BooleanProperty)this.booleanProperty);
            }
        } else {
            this.setGraphic(null);
            this.setText(null);
        }
    }
}

