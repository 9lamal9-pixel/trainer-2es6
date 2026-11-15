/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;

public abstract class FocusModel<T> {
    private ReadOnlyIntegerWrapper focusedIndex = new ReadOnlyIntegerWrapper(this, "focusedIndex", -1);
    private ReadOnlyObjectWrapper<T> focusedItem = new ReadOnlyObjectWrapper(this, "focusedItem");

    public FocusModel() {
        this.focusedIndexProperty().addListener(observable -> this.setFocusedItem(this.getModelItem(this.getFocusedIndex())));
    }

    public final ReadOnlyIntegerProperty focusedIndexProperty() {
        return this.focusedIndex.getReadOnlyProperty();
    }

    public final int getFocusedIndex() {
        return this.focusedIndex.get();
    }

    final void setFocusedIndex(int n) {
        this.focusedIndex.set(n);
    }

    public final ReadOnlyObjectProperty<T> focusedItemProperty() {
        return this.focusedItem.getReadOnlyProperty();
    }

    public final T getFocusedItem() {
        return this.focusedItemProperty().get();
    }

    final void setFocusedItem(T t) {
        this.focusedItem.set(t);
    }

    protected abstract int getItemCount();

    protected abstract T getModelItem(int var1);

    public boolean isFocused(int n) {
        if (n < 0 || n >= this.getItemCount()) {
            return false;
        }
        return this.getFocusedIndex() == n;
    }

    public void focus(int n) {
        if (n < 0 || n >= this.getItemCount()) {
            this.setFocusedIndex(-1);
        } else {
            int n2 = this.getFocusedIndex();
            this.setFocusedIndex(n);
            if (n2 == n) {
                this.setFocusedItem(this.getModelItem(n));
            }
        }
    }

    public void focusPrevious() {
        if (this.getFocusedIndex() == -1) {
            this.focus(0);
        } else if (this.getFocusedIndex() > 0) {
            this.focus(this.getFocusedIndex() - 1);
        }
    }

    public void focusNext() {
        if (this.getFocusedIndex() == -1) {
            this.focus(0);
        } else if (this.getFocusedIndex() != this.getItemCount() - 1) {
            this.focus(this.getFocusedIndex() + 1);
        }
    }
}

