/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.cell;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.CellUtils;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

public class TextFieldTreeTableCell<S, T>
extends TreeTableCell<S, T> {
    private TextField textField;
    private ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this, "converter");

    public static <S> Callback<TreeTableColumn<S, String>, TreeTableCell<S, String>> forTreeTableColumn() {
        return TextFieldTreeTableCell.forTreeTableColumn(new DefaultStringConverter());
    }

    public static <S, T> Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> forTreeTableColumn(StringConverter<T> stringConverter) {
        return treeTableColumn -> new TextFieldTreeTableCell(stringConverter);
    }

    public TextFieldTreeTableCell() {
        this((StringConverter<T>)null);
    }

    public TextFieldTreeTableCell(StringConverter<T> stringConverter) {
        this.getStyleClass().add("text-field-tree-table-cell");
        this.setConverter(stringConverter);
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

    @Override
    public void startEdit() {
        if (!(this.isEditable() && this.getTreeTableView().isEditable() && this.getTableColumn().isEditable())) {
            return;
        }
        super.startEdit();
        if (this.isEditing()) {
            if (this.textField == null) {
                this.textField = CellUtils.createTextField(this, this.getConverter());
            }
            CellUtils.startEdit(this, this.getConverter(), null, null, this.textField);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        CellUtils.cancelEdit(this, this.getConverter(), null);
    }

    @Override
    public void updateItem(T t, boolean bl) {
        super.updateItem(t, bl);
        CellUtils.updateItem(this, this.getConverter(), null, null, this.textField);
    }
}

