/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.cell;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.CellUtils;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ChoiceBoxTreeTableCell<S, T>
extends TreeTableCell<S, T> {
    private final ObservableList<T> items;
    private ChoiceBox<T> choiceBox;
    private ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this, "converter");

    @SafeVarargs
    public static <S, T> Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> forTreeTableColumn(T ... TArray) {
        return ChoiceBoxTreeTableCell.forTreeTableColumn(null, TArray);
    }

    @SafeVarargs
    public static <S, T> Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> forTreeTableColumn(StringConverter<T> stringConverter, T ... TArray) {
        return ChoiceBoxTreeTableCell.forTreeTableColumn(stringConverter, FXCollections.observableArrayList(TArray));
    }

    public static <S, T> Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> forTreeTableColumn(ObservableList<T> observableList) {
        return ChoiceBoxTreeTableCell.forTreeTableColumn(null, observableList);
    }

    public static <S, T> Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> forTreeTableColumn(StringConverter<T> stringConverter, ObservableList<T> observableList) {
        return treeTableColumn -> new ChoiceBoxTreeTableCell(stringConverter, observableList);
    }

    public ChoiceBoxTreeTableCell() {
        this(FXCollections.observableArrayList());
    }

    @SafeVarargs
    public ChoiceBoxTreeTableCell(T ... TArray) {
        this(FXCollections.observableArrayList(TArray));
    }

    @SafeVarargs
    public ChoiceBoxTreeTableCell(StringConverter<T> stringConverter, T ... TArray) {
        this(stringConverter, FXCollections.observableArrayList(TArray));
    }

    public ChoiceBoxTreeTableCell(ObservableList<T> observableList) {
        this((StringConverter<T>)null, observableList);
    }

    public ChoiceBoxTreeTableCell(StringConverter<T> stringConverter, ObservableList<T> observableList) {
        this.getStyleClass().add("choice-box-tree-table-cell");
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
        if (!(this.isEditable() && this.getTreeTableView().isEditable() && this.getTableColumn().isEditable())) {
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

