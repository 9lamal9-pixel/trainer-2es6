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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CellUtils;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ComboBoxTableCell<S, T>
extends TableCell<S, T> {
    private final ObservableList<T> items;
    private ComboBox<T> comboBox;
    private ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this, "converter");
    private BooleanProperty comboBoxEditable = new SimpleBooleanProperty(this, "comboBoxEditable");

    @SafeVarargs
    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(T ... TArray) {
        return ComboBoxTableCell.forTableColumn(null, TArray);
    }

    @SafeVarargs
    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(StringConverter<T> stringConverter, T ... TArray) {
        return ComboBoxTableCell.forTableColumn(stringConverter, FXCollections.observableArrayList(TArray));
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(ObservableList<T> observableList) {
        return ComboBoxTableCell.forTableColumn(null, observableList);
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(StringConverter<T> stringConverter, ObservableList<T> observableList) {
        return tableColumn -> new ComboBoxTableCell(stringConverter, observableList);
    }

    public ComboBoxTableCell() {
        this(FXCollections.observableArrayList());
    }

    @SafeVarargs
    public ComboBoxTableCell(T ... TArray) {
        this(FXCollections.observableArrayList(TArray));
    }

    @SafeVarargs
    public ComboBoxTableCell(StringConverter<T> stringConverter, T ... TArray) {
        this(stringConverter, FXCollections.observableArrayList(TArray));
    }

    public ComboBoxTableCell(ObservableList<T> observableList) {
        this((StringConverter<T>)null, observableList);
    }

    public ComboBoxTableCell(StringConverter<T> stringConverter, ObservableList<T> observableList) {
        this.getStyleClass().add("combo-box-table-cell");
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
        if (!(this.isEditable() && this.getTableView().isEditable() && this.getTableColumn().isEditable())) {
            return;
        }
        if (this.comboBox == null) {
            this.comboBox = CellUtils.createComboBox(this, this.items, this.converterProperty());
            this.comboBox.editableProperty().bind(this.comboBoxEditableProperty());
        }
        this.comboBox.getSelectionModel().select(this.getItem());
        super.startEdit();
        this.setText(null);
        this.setGraphic(this.comboBox);
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

