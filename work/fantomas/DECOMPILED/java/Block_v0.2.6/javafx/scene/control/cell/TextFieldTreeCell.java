/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.cell;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CellUtils;
import javafx.scene.control.cell.DefaultTreeCell;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

public class TextFieldTreeCell<T>
extends DefaultTreeCell<T> {
    private TextField textField;
    private HBox hbox;
    private ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this, "converter");

    public static Callback<TreeView<String>, TreeCell<String>> forTreeView() {
        return TextFieldTreeCell.forTreeView(new DefaultStringConverter());
    }

    public static <T> Callback<TreeView<T>, TreeCell<T>> forTreeView(StringConverter<T> stringConverter) {
        return treeView -> new TextFieldTreeCell(stringConverter);
    }

    public TextFieldTreeCell() {
        this((StringConverter<T>)null);
    }

    public TextFieldTreeCell(StringConverter<T> stringConverter) {
        this.getStyleClass().add("text-field-tree-cell");
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
        if (!this.isEditable() || !this.getTreeView().isEditable()) {
            return;
        }
        super.startEdit();
        if (this.isEditing()) {
            StringConverter<T> stringConverter = this.getConverter();
            if (this.textField == null) {
                this.textField = CellUtils.createTextField(this, stringConverter);
            }
            if (this.hbox == null) {
                this.hbox = new HBox(CellUtils.TREE_VIEW_HBOX_GRAPHIC_PADDING);
            }
            CellUtils.startEdit(this, stringConverter, this.hbox, this.getTreeItemGraphic(), this.textField);
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        CellUtils.cancelEdit(this, this.getConverter(), this.getTreeItemGraphic());
    }

    @Override
    public void updateItem(T t, boolean bl) {
        super.updateItem(t, bl);
        CellUtils.updateItem(this, this.getConverter(), this.hbox, this.getTreeItemGraphic(), this.textField);
    }

    private Node getTreeItemGraphic() {
        TreeItem treeItem = this.getTreeItem();
        return treeItem == null ? null : treeItem.getGraphic();
    }
}

