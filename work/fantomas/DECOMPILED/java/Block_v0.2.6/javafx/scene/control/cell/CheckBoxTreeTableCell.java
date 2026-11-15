/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control.cell;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.CellUtils;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class CheckBoxTreeTableCell<S, T>
extends TreeTableCell<S, T> {
    private final CheckBox checkBox;
    private boolean showLabel;
    private ObservableValue<Boolean> booleanProperty;
    private ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this, "converter"){

        @Override
        protected void invalidated() {
            CheckBoxTreeTableCell.this.updateShowLabel();
        }
    };
    private ObjectProperty<Callback<Integer, ObservableValue<Boolean>>> selectedStateCallback = new SimpleObjectProperty<Callback<Integer, ObservableValue<Boolean>>>(this, "selectedStateCallback");

    public static <S> Callback<TreeTableColumn<S, Boolean>, TreeTableCell<S, Boolean>> forTreeTableColumn(TreeTableColumn<S, Boolean> treeTableColumn) {
        return CheckBoxTreeTableCell.forTreeTableColumn(null, null);
    }

    public static <S, T> Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> forTreeTableColumn(Callback<Integer, ObservableValue<Boolean>> callback) {
        return CheckBoxTreeTableCell.forTreeTableColumn(callback, null);
    }

    public static <S, T> Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> forTreeTableColumn(Callback<Integer, ObservableValue<Boolean>> callback, boolean bl) {
        StringConverter stringConverter = !bl ? null : CellUtils.defaultStringConverter();
        return CheckBoxTreeTableCell.forTreeTableColumn(callback, stringConverter);
    }

    public static <S, T> Callback<TreeTableColumn<S, T>, TreeTableCell<S, T>> forTreeTableColumn(Callback<Integer, ObservableValue<Boolean>> callback, StringConverter<T> stringConverter) {
        return treeTableColumn -> new CheckBoxTreeTableCell(callback, stringConverter);
    }

    public CheckBoxTreeTableCell() {
        this((Callback<Integer, ObservableValue<Boolean>>)null, (StringConverter<T>)null);
    }

    public CheckBoxTreeTableCell(Callback<Integer, ObservableValue<Boolean>> callback) {
        this(callback, null);
    }

    public CheckBoxTreeTableCell(Callback<Integer, ObservableValue<Boolean>> callback, StringConverter<T> stringConverter) {
        this.getStyleClass().add("check-box-tree-table-cell");
        this.checkBox = new CheckBox();
        this.setGraphic(null);
        this.setSelectedStateCallback(callback);
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

    public final ObjectProperty<Callback<Integer, ObservableValue<Boolean>>> selectedStateCallbackProperty() {
        return this.selectedStateCallback;
    }

    public final void setSelectedStateCallback(Callback<Integer, ObservableValue<Boolean>> callback) {
        this.selectedStateCallbackProperty().set(callback);
    }

    public final Callback<Integer, ObservableValue<Boolean>> getSelectedStateCallback() {
        return (Callback)this.selectedStateCallbackProperty().get();
    }

    @Override
    public void updateItem(T t, boolean bl) {
        super.updateItem(t, bl);
        if (bl) {
            this.setText(null);
            this.setGraphic(null);
        } else {
            ObservableValue<?> observableValue;
            StringConverter<T> stringConverter = this.getConverter();
            if (this.showLabel) {
                this.setText(stringConverter.toString(t));
            }
            this.setGraphic(this.checkBox);
            if (this.booleanProperty instanceof BooleanProperty) {
                this.checkBox.selectedProperty().unbindBidirectional((BooleanProperty)this.booleanProperty);
            }
            if ((observableValue = this.getSelectedProperty()) instanceof BooleanProperty) {
                this.booleanProperty = observableValue;
                this.checkBox.selectedProperty().bindBidirectional((BooleanProperty)this.booleanProperty);
            }
            this.checkBox.disableProperty().bind(Bindings.not(this.getTreeTableView().editableProperty().and(this.getTableColumn().editableProperty()).and(this.editableProperty())));
        }
    }

    private void updateShowLabel() {
        this.showLabel = this.converter != null;
        this.checkBox.setAlignment(this.showLabel ? Pos.CENTER_LEFT : Pos.CENTER);
    }

    private ObservableValue<?> getSelectedProperty() {
        return this.getSelectedStateCallback() != null ? this.getSelectedStateCallback().call(this.getIndex()) : this.getTableColumn().getCellObservableValue(this.getIndex());
    }
}

