/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;

public abstract class MultipleSelectionModel<T>
extends SelectionModel<T> {
    private ObjectProperty<SelectionMode> selectionMode;

    public final void setSelectionMode(SelectionMode selectionMode) {
        this.selectionModeProperty().set(selectionMode);
    }

    public final SelectionMode getSelectionMode() {
        return this.selectionMode == null ? SelectionMode.SINGLE : (SelectionMode)((Object)this.selectionMode.get());
    }

    public final ObjectProperty<SelectionMode> selectionModeProperty() {
        if (this.selectionMode == null) {
            this.selectionMode = new ObjectPropertyBase<SelectionMode>(SelectionMode.SINGLE){

                @Override
                protected void invalidated() {
                    if (MultipleSelectionModel.this.getSelectionMode() == SelectionMode.SINGLE && !MultipleSelectionModel.this.isEmpty()) {
                        int n = MultipleSelectionModel.this.getSelectedIndex();
                        MultipleSelectionModel.this.clearSelection();
                        MultipleSelectionModel.this.select(n);
                    }
                }

                @Override
                public Object getBean() {
                    return MultipleSelectionModel.this;
                }

                @Override
                public String getName() {
                    return "selectionMode";
                }
            };
        }
        return this.selectionMode;
    }

    public abstract ObservableList<Integer> getSelectedIndices();

    public abstract ObservableList<T> getSelectedItems();

    public abstract void selectIndices(int var1, int ... var2);

    public void selectRange(int n, int n2) {
        if (n == n2) {
            return;
        }
        boolean bl = n < n2;
        int n3 = bl ? n : n2;
        int n4 = bl ? n2 : n;
        int n5 = n4 - n3 - 1;
        int[] nArray = new int[n5];
        int n6 = bl ? n3 : n4;
        int n7 = bl ? n6++ : n6--;
        for (int i = 0; i < n5; ++i) {
            nArray[i] = bl ? n6++ : n6--;
        }
        this.selectIndices(n7, nArray);
    }

    public abstract void selectAll();

    @Override
    public abstract void selectFirst();

    @Override
    public abstract void selectLast();
}

