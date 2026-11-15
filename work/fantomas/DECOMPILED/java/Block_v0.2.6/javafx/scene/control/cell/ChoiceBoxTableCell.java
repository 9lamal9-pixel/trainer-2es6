/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.cell;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.CellUtils;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ChoiceBoxTableCell<S, T>
extends TableCell<S, T> {
    private final ObservableList<T> items;
    private ChoiceBox<T> choiceBox;
    private ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this, "converter");

    @SafeVarargs
    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(T ... TArray) {
        return ChoiceBoxTableCell.forTableColumn(null, TArray);
    }

    @SafeVarargs
    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(StringConverter<T> stringConverter, T ... TArray) {
        return ChoiceBoxTableCell.forTableColumn(stringConverter, FXCollections.observableArrayList(TArray));
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(ObservableList<T> observableList) {
        return ChoiceBoxTableCell.forTableColumn(null, observableList);
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(StringConverter<T> stringConverter, ObservableList<T> observableList) {
        return tableColumn -> new ChoiceBoxTableCell(stringConverter, observableList);
    }

    public ChoiceBoxTableCell() {
        this(FXCollections.observableArrayList());
    }

    @SafeVarargs
    public ChoiceBoxTableCell(T ... TArray) {
        this(FXCollections.observableArrayList(TArray));
    }

    @SafeVarargs
    public ChoiceBoxTableCell(StringConverter<T> stringConverter, T ... TArray) {
        this(stringConverter, FXCollections.observableArrayList(TArray));
    }

    public ChoiceBoxTableCell(ObservableList<T> observableList) {
        this((StringConverter<T>)null, observableList);
    }

    public ChoiceBoxTableCell(StringConverter<T> stringConverter, ObservableList<T> observableList) {
        this.getStyleClass().add("choice-box-table-cell");
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
        if (!(this.isEditable() && this.getTableView().isEditable() && this.getTableColumn().isEditable())) {
            return;
        }
        if (this.choiceBox == null) {
            this.choiceBox = CellUtils.createChoiceBox(this, this.items, this.converterProperty());
        }
        this.choiceBox.getSelectionModel().select(this.getItem());
        super.startEdit();
        this.setText(null);
        this.setGraphic(this.choiceBox);
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

