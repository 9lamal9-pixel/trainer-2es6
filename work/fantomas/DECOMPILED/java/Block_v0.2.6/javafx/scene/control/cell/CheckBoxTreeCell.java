/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.cell;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CellUtils;
import javafx.scene.control.cell.DefaultTreeCell;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class CheckBoxTreeCell<T>
extends DefaultTreeCell<T> {
    private final CheckBox checkBox;
    private ObservableValue<Boolean> booleanProperty;
    private BooleanProperty indeterminateProperty;
    private ObjectProperty<StringConverter<TreeItem<T>>> converter = new SimpleObjectProperty<StringConverter<TreeItem<T>>>(this, "converter");
    private ObjectProperty<Callback<TreeItem<T>, ObservableValue<Boolean>>> selectedStateCallback = new SimpleObjectProperty<Callback<TreeItem<T>, ObservableValue<Boolean>>>(this, "selectedStateCallback");

    public static <T> Callback<TreeView<T>, TreeCell<T>> forTreeView() {
        Callback<TreeItem<T>, ObservableValue<Boolean>> callback = treeItem -> {
            if (treeItem instanceof CheckBoxTreeItem) {
                return ((CheckBoxTreeItem)treeItem).selectedProperty();
            }
            return null;
        };
        return CheckBoxTreeCell.forTreeView(callback, CellUtils.defaultTreeItemStringConverter());
    }

    public static <T> Callback<TreeView<T>, TreeCell<T>> forTreeView(Callback<TreeItem<T>, ObservableValue<Boolean>> callback) {
        return CheckBoxTreeCell.forTreeView(callback, CellUtils.defaultTreeItemStringConverter());
    }

    public static <T> Callback<TreeView<T>, TreeCell<T>> forTreeView(Callback<TreeItem<T>, ObservableValue<Boolean>> callback, StringConverter<TreeItem<T>> stringConverter) {
        return treeView -> new CheckBoxTreeCell(callback, stringConverter);
    }

    public CheckBoxTreeCell() {
        this((TreeItem<T> treeItem) -> {
            if (treeItem instanceof CheckBoxTreeItem) {
                return ((CheckBoxTreeItem)treeItem).selectedProperty();
            }
            return null;
        });
    }

    public CheckBoxTreeCell(Callback<TreeItem<T>, ObservableValue<Boolean>> callback) {
        this(callback, CellUtils.defaultTreeItemStringConverter(), null);
    }

    public CheckBoxTreeCell(Callback<TreeItem<T>, ObservableValue<Boolean>> callback, StringConverter<TreeItem<T>> stringConverter) {
        this(callback, stringConverter, null);
    }

    private CheckBoxTreeCell(Callback<TreeItem<T>, ObservableValue<Boolean>> callback, StringConverter<TreeItem<T>> stringConverter, Callback<TreeItem<T>, ObservableValue<Boolean>> callback2) {
        this.getStyleClass().add("check-box-tree-cell");
        this.setSelectedStateCallback(callback);
        this.setConverter(stringConverter);
        this.checkBox = new CheckBox();
        this.checkBox.setAllowIndeterminate(false);
        this.setGraphic(null);
    }

    public final ObjectProperty<StringConverter<TreeItem<T>>> converterProperty() {
        return this.converter;
    }

    public final void setConverter(StringConverter<TreeItem<T>> stringConverter) {
        this.converterProperty().set(stringConverter);
    }

    public final StringConverter<TreeItem<T>> getConverter() {
        return (StringConverter)this.converterProperty().get();
    }

    public final ObjectProperty<Callback<TreeItem<T>, ObservableValue<Boolean>>> selectedStateCallbackProperty() {
        return this.selectedStateCallback;
    }

    public final void setSelectedStateCallback(Callback<TreeItem<T>, ObservableValue<Boolean>> callback) {
        this.selectedStateCallbackProperty().set(callback);
    }

    public final Callback<TreeItem<T>, ObservableValue<Boolean>> getSelectedStateCallback() {
        return (Callback)this.selectedStateCallbackProperty().get();
    }

    @Override
    public void updateItem(T t, boolean bl) {
        super.updateItem(t, bl);
        if (bl) {
            this.setText(null);
            this.setGraphic(null);
        } else {
            StringConverter stringConverter = this.getConverter();
            TreeItem treeItem = this.getTreeItem();
            this.setText(stringConverter != null ? stringConverter.toString(treeItem) : (treeItem == null ? "" : treeItem.toString()));
            this.checkBox.setGraphic(treeItem == null ? null : treeItem.getGraphic());
            this.setGraphic(this.checkBox);
            if (this.booleanProperty != null) {
                this.checkBox.selectedProperty().unbindBidirectional((BooleanProperty)this.booleanProperty);
            }
            if (this.indeterminateProperty != null) {
                this.checkBox.indeterminateProperty().unbindBidirectional(this.indeterminateProperty);
            }
            if (treeItem instanceof CheckBoxTreeItem) {
                CheckBoxTreeItem checkBoxTreeItem = (CheckBoxTreeItem)treeItem;
                this.booleanProperty = checkBoxTreeItem.selectedProperty();
                this.checkBox.selectedProperty().bindBidirectional((BooleanProperty)this.booleanProperty);
                this.indeterminateProperty = checkBoxTreeItem.indeterminateProperty();
                this.checkBox.indeterminateProperty().bindBidirectional(this.indeterminateProperty);
            } else {
                Callback<TreeItem<T>, ObservableValue<Boolean>> callback = this.getSelectedStateCallback();
                if (callback == null) {
                    throw new NullPointerException("The CheckBoxTreeCell selectedStateCallbackProperty can not be null");
                }
                this.booleanProperty = callback.call(treeItem);
                if (this.booleanProperty != null) {
                    this.checkBox.selectedProperty().bindBidirectional((BooleanProperty)this.booleanProperty);
                }
            }
        }
    }

    @Override
    void updateDisplay(T t, boolean bl) {
    }
}

