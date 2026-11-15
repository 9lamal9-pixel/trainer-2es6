/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

public abstract class SelectionModel<T> {
    private ReadOnlyIntegerWrapper selectedIndex = new ReadOnlyIntegerWrapper(this, "selectedIndex", -1);
    private ReadOnlyObjectWrapper<T> selectedItem = new ReadOnlyObjectWrapper(this, "selectedItem");

    public final ReadOnlyIntegerProperty selectedIndexProperty() {
        return this.selectedIndex.getReadOnlyProperty();
    }

    protected final void setSelectedIndex(int n) {
        this.selectedIndex.set(n);
    }

    public final int getSelectedIndex() {
        return this.selectedIndexProperty().get();
    }

    public final ReadOnlyObjectProperty<T> selectedItemProperty() {
        return this.selectedItem.getReadOnlyProperty();
    }

    protected final void setSelectedItem(T t) {
        this.selectedItem.set(t);
    }

    public final T getSelectedItem() {
        return this.selectedItemProperty().get();
    }

    public abstract void clearAndSelect(int var1);

    public abstract void select(int var1);

    public abstract void select(T var1);

    public abstract void clearSelection(int var1);

    public abstract void clearSelection();

    public abstract boolean isSelected(int var1);

    public abstract boolean isEmpty();

    public abstract void selectPrevious();

    public abstract void selectNext();

    public abstract void selectFirst();

    public abstract void selectLast();
}

