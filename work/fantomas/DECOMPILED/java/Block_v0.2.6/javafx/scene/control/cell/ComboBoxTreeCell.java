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
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CellUtils;
import javafx.scene.control.cell.DefaultTreeCell;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class ComboBoxTreeCell<T>
extends DefaultTreeCell<T> {
    private final ObservableList<T> items;
    private ComboBox<T> comboBox;
    private HBox hbox;
    private ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this, "converter");
    private BooleanProperty comboBoxEditable = new SimpleBooleanProperty(this, "comboBoxEditable");

    @SafeVarargs
    public static <T> Callback<TreeView<T>, TreeCell<T>> forTreeView(T ... TArray) {
        return ComboBoxTreeCell.forTreeView(FXCollections.observableArrayList(TArray));
    }

    public static <T> Callback<TreeView<T>, TreeCell<T>> forTreeView(ObservableList<T> observableList) {
        return ComboBoxTreeCell.forTreeView(null, observableList);
    }

    @SafeVarargs
    public static <T> Callback<TreeView<T>, TreeCell<T>> forTreeView(StringConverter<T> stringConverter, T ... TArray) {
        return ComboBoxTreeCell.forTreeView(stringConverter, FXCollections.observableArrayList(TArray));
    }

    public static <T> Callback<TreeView<T>, TreeCell<T>> forTreeView(StringConverter<T> stringConverter, ObservableList<T> observableList) {
        return treeView -> new ComboBoxTreeCell(stringConverter, observableList);
    }

    public ComboBoxTreeCell() {
        this(FXCollections.observableArrayList());
    }

    @SafeVarargs
    public ComboBoxTreeCell(T ... TArray) {
        this(FXCollections.observableArrayList(TArray));
    }

    @SafeVarargs
    public ComboBoxTreeCell(StringConverter<T> stringConverter, T ... TArray) {
        this(stringConverter, FXCollections.observableArrayList(TArray));
    }

    public ComboBoxTreeCell(ObservableList<T> observableList) {
        this((StringConverter<T>)null, observableList);
    }

    public ComboBoxTreeCell(StringConverter<T> stringConverter, ObservableList<T> observableList) {
        this.getStyleClass().add("combo-box-tree-cell");
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
        if (!this.isEditable() || !this.getTreeView().isEditable()) {
            return;
        }
        TreeItem treeItem = this.getTreeItem();
        if (treeItem == null) {
            return;
        }
        if (this.comboBox == null) {
            this.comboBox = CellUtils.createComboBox(this, this.items, this.converterProperty());
            this.comboBox.editableProperty().bind(this.comboBoxEditableProperty());
        }
        if (this.hbox == null) {
            this.hbox = new HBox(CellUtils.TREE_VIEW_HBOX_GRAPHIC_PADDING);
        }
        this.comboBox.getSelectionModel().select(treeItem.getValue());
        super.startEdit();
        if (this.isEditing()) {
            this.setText(null);
            Node node = CellUtils.getGraphic(treeItem);
            if (node != null) {
                this.hbox.getChildren().setAll((Node[])new Node[]{node, this.comboBox});
                this.setGraphic(this.hbox);
            } else {
                this.setGraphic(this.comboBox);
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
        Node node = CellUtils.getGraphic(this.getTreeItem());
        CellUtils.updateItem(this, this.getConverter(), this.hbox, node, this.comboBox);
    }
}

