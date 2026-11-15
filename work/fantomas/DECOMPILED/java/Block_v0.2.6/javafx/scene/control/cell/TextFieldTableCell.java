/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.cell;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CellUtils;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

public class TextFieldTableCell<S, T>
extends TableCell<S, T> {
    private TextField textField;
    private ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this, "converter");

    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> forTableColumn() {
        return TextFieldTableCell.forTableColumn(new DefaultStringConverter());
    }

    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(StringConverter<T> stringConverter) {
        return tableColumn -> new TextFieldTableCell(stringConverter);
    }

    public TextFieldTableCell() {
        this((StringConverter<T>)null);
    }

    public TextFieldTableCell(StringConverter<T> stringConverter) {
        this.getStyleClass().add("text-field-table-cell");
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
        if (!(this.isEditable() && this.getTableView().isEditable() && this.getTableColumn().isEditable())) {
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

