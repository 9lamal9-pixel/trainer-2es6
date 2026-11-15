/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.cell;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CellUtils;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ChoiceBoxListCell<T>
extends ListCell<T> {
    private final ObservableList<T> items;
    private ChoiceBox<T> choiceBox;
    private ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this, "converter");

    @SafeVarargs
    public static <T> Callback<ListView<T>, ListCell<T>> forListView(T ... TArray) {
        return ChoiceBoxListCell.forListView(FXCollections.observableArrayList(TArray));
    }

    @SafeVarargs
    public static <T> Callback<ListView<T>, ListCell<T>> forListView(StringConverter<T> stringConverter, T ... TArray) {
        return ChoiceBoxListCell.forListView(stringConverter, FXCollections.observableArrayList(TArray));
    }

    public static <T> Callback<ListView<T>, ListCell<T>> forListView(ObservableList<T> observableList) {
        return ChoiceBoxListCell.forListView(null, observableList);
    }

    public static <T> Callback<ListView<T>, ListCell<T>> forListView(StringConverter<T> stringConverter, ObservableList<T> observableList) {
        return listView -> new ChoiceBoxListCell(stringConverter, observableList);
    }

    public ChoiceBoxListCell() {
        this(FXCollections.observableArrayList());
    }

    @SafeVarargs
    public ChoiceBoxListCell(T ... TArray) {
        this(FXCollections.observableArrayList(TArray));
    }

    @SafeVarargs
    public ChoiceBoxListCell(StringConverter<T> stringConverter, T ... TArray) {
        this(stringConverter, FXCollections.observableArrayList(TArray));
    }

    public ChoiceBoxListCell(ObservableList<T> observableList) {
        this((StringConverter<T>)null, observableList);
    }

    public ChoiceBoxListCell(StringConverter<T> stringConverter, ObservableList<T> observableList) {
        this.getStyleClass().add("choice-box-list-cell");
        this.items = observableList;
        this.setConverter(stringConverter != null ? stringConverter : CellUtils.defaultStringConverter());
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

    public ObservableList<T> getItems() {
        return this.items;
    }

    @Override
    public void startEdit() {
        if (!this.isEditable() || !this.getListView().isEditable()) {
            return;
        }
        if (this.choiceBox == null) {
            this.choiceBox = CellUtils.createChoiceBox(this, this.items, this.converterProperty());
        }
        this.choiceBox.getSelectionModel().select(this.getItem());
        super.startEdit();
        if (this.isEditing()) {
            this.setText(null);
            this.setGraphic(this.choiceBox);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        this.setText(this.getConverter().toString(this.getItem()));
        this.setGraphic(null);
    }

    @Override
    public void updateItem(T t, boolean bl) {
        super.updateItem(t, bl);
        CellUtils.updateItem(this, this.getConverter(), null, null, this.choiceBox);
    }
}

