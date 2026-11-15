/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.cell;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CellUtils;
import javafx.scene.control.cell.DefaultTreeCell;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ChoiceBoxTreeCell<T>
extends DefaultTreeCell<T> {
    private final ObservableList<T> items;
    private ChoiceBox<T> choiceBox;
    private HBox hbox;
    private ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this, "converter");

    @SafeVarargs
    public static <T> Callback<TreeView<T>, TreeCell<T>> forTreeView(T ... TArray) {
        return ChoiceBoxTreeCell.forTreeView(FXCollections.observableArrayList(TArray));
    }

    public static <T> Callback<TreeView<T>, TreeCell<T>> forTreeView(ObservableList<T> observableList) {
        return ChoiceBoxTreeCell.forTreeView(null, observableList);
    }

    @SafeVarargs
    public static <T> Callback<TreeView<T>, TreeCell<T>> forTreeView(StringConverter<T> stringConverter, T ... TArray) {
        return ChoiceBoxTreeCell.forTreeView(stringConverter, FXCollections.observableArrayList(TArray));
    }

    public static <T> Callback<TreeView<T>, TreeCell<T>> forTreeView(StringConverter<T> stringConverter, ObservableList<T> observableList) {
        return treeView -> new ChoiceBoxTreeCell(stringConverter, observableList);
    }

    public ChoiceBoxTreeCell() {
        this(FXCollections.observableArrayList());
    }

    @SafeVarargs
    public ChoiceBoxTreeCell(T ... TArray) {
        this(FXCollections.observableArrayList(TArray));
    }

    @SafeVarargs
    public ChoiceBoxTreeCell(StringConverter<T> stringConverter, T ... TArray) {
        this(stringConverter, FXCollections.observableArrayList(TArray));
    }

    public ChoiceBoxTreeCell(ObservableList<T> observableList) {
        this((StringConverter<T>)null, observableList);
    }

    public ChoiceBoxTreeCell(StringConverter<T> stringConverter, ObservableList<T> observableList) {
        this.getStyleClass().add("choice-box-tree-cell");
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
        if (!this.isEditable() || !this.getTreeView().isEditable()) {
            return;
        }
        TreeItem treeItem = this.getTreeItem();
        if (treeItem == null) {
            return;
        }
        if (this.choiceBox == null) {
            this.choiceBox = CellUtils.createChoiceBox(this, this.items, this.converterProperty());
        }
        if (this.hbox == null) {
            this.hbox = new HBox(CellUtils.TREE_VIEW_HBOX_GRAPHIC_PADDING);
        }
        this.choiceBox.getSelectionModel().select(treeItem.getValue());
        super.startEdit();
        if (this.isEditing()) {
            this.setText(null);
            Node node = this.getTreeItemGraphic();
            if (node != null) {
                this.hbox.getChildren().setAll((Node[])new Node[]{node, this.choiceBox});
                this.setGraphic(this.hbox);
            } else {
                this.setGraphic(this.choiceBox);
            }
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
        CellUtils.updateItem(this, this.getConverter(), this.hbox, this.getTreeItemGraphic(), this.choiceBox);
    }

    private Node getTreeItemGraphic() {
        TreeItem treeItem = this.getTreeItem();
        return treeItem == null ? null : treeItem.getGraphic();
    }
}

