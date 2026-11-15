/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.cell;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.CellUtils;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ComboBoxListCell<T>
extends ListCell<T> {
    private final ObservableList<T> items;
    private ComboBox<T> comboBox;
    private ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this, "converter");
    private BooleanProperty comboBoxEditable = new SimpleBooleanProperty(this, "comboBoxEditable");

    @SafeVarargs
    public static <T> Callback<ListView<T>, ListCell<T>> forListView(T ... TArray) {
        return ComboBoxListCell.forListView(FXCollections.observableArrayList(TArray));
    }

    @SafeVarargs
    public static <T> Callback<ListView<T>, ListCell<T>> forListView(StringConverter<T> stringConverter, T ... TArray) {
        return ComboBoxListCell.forListView(stringConverter, FXCollections.observableArrayList(TArray));
    }

    public static <T> Callback<ListView<T>, ListCell<T>> forListView(ObservableList<T> observableList) {
        return ComboBoxListCell.forListView(null, observableList);
    }

    public static <T> Callback<ListView<T>, ListCell<T>> forListView(StringConverter<T> stringConverter, ObservableList<T> observableList) {
        return listView -> new ComboBoxListCell(stringConverter, observableList);
    }

    public ComboBoxListCell() {
        this(FXCollections.observableArrayList());
    }

    @SafeVarargs
    public ComboBoxListCell(T ... TArray) {
        this(FXCollections.observableArrayList(TArray));
    }

    @SafeVarargs
    public ComboBoxListCell(StringConverter<T> stringConverter, T ... TArray) {
        this(stringConverter, FXCollections.observableArrayList(TArray));
    }

    public ComboBoxListCell(ObservableList<T> observableList) {
        this((StringConverter<T>)null, observableList);
    }

    public ComboBoxListCell(StringConverter<T> stringConverter, ObservableList<T> observableList) {
        this.getStyleClass().add("combo-box-list-cell");
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

    public final BooleanProperty comboBoxEditableProperty() {
        return this.comboBoxEditable;
    }

    public final void setComboBoxEditable(boolean bl) {
        this.comboBoxEditableProperty().set(bl);
    }

    public final boolean isComboBoxEditable() {
        return this.comboBoxEditableProperty().get();
    }

    public ObservableList<T> getItems() {
        return this.items;
    }

    @Override
    public void startEdit() {
        if (!this.isEditable() || !this.getListView().isEditable()) {
            return;
        }
        if (this.comboBox == null) {
            this.comboBox = CellUtils.createComboBox(this, this.items, this.converterProperty());
            this.comboBox.editableProperty().bind(this.comboBoxEditableProperty());
        }
        this.comboBox.getSelectionModel().select(this.getItem());
        super.startEdit();
        if (this.isEditing()) {
            this.setText(null);
            this.setGraphic(this.comboBox);
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
        CellUtils.updateItem(this, this.getConverter(), null, null, this.comboBox);
    }
}

