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
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.CellUtils;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ComboBoxTreeTableCell<S, T>
extends TreeTableCell<S, T> {
    private final ObservableList<T> items;
    private ComboBox<T> comboBox;
    private ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this, "converter");
    private BooleanProperty comboBoxEditable = new SimpleBooleanProperty(this, "comboBoxEditable");

    @SafeVarargs
    public static <S, T> Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> forTreeTableColumn(T ... TArray) {
        return ComboBoxTreeTableCell.forTreeTableColumn(null, TArray);
    }

    @SafeVarargs
    public static <S, T> Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> forTreeTableColumn(StringConverter<T> stringConverter, T ... TArray) {
        return ComboBoxTreeTableCell.forTreeTableColumn(stringConverter, FXCollections.observableArrayList(TArray));
    }

    public static <S, T> Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> forTreeTableColumn(ObservableList<T> observableList) {
        return ComboBoxTreeTableCell.forTreeTableColumn(null, observableList);
    }

    public static <S, T> Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> forTreeTableColumn(StringConverter<T> stringConverter, ObservableList<T> observableList) {
        return treeTableColumn -> new ComboBoxTreeTableCell(stringConverter, observableList);
    }

    public ComboBoxTreeTableCell() {
        this(FXCollections.observableArrayList());
    }

    @SafeVarargs
    public ComboBoxTreeTableCell(T ... TArray) {
        this(FXCollections.observableArrayList(TArray));
    }

    @SafeVarargs
    public ComboBoxTreeTableCell(StringConverter<T> stringConverter, T ... TArray) {
        this(stringConverter, FXCollections.observableArrayList(TArray));
    }

    public ComboBoxTreeTableCell(ObservableList<T> observableList) {
        this((StringConverter<T>)null, observableList);
    }

    public ComboBoxTreeTableCell(StringConverter<T> stringConverter, ObservableList<T> observableList) {
        this.getStyleClass().add("combo-box-tree-table-cell");
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
        if (!(this.isEditable() && this.getTreeTableView().isEditable() && this.getTableColumn().isEditable())) {
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

