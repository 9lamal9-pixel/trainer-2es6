/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.scene.control.SelectionModel;

public abstract class SingleSelectionModel<T>
extends SelectionModel<T> {
    @Override
    public void clearSelection() {
        this.updateSelectedIndex(-1);
    }

    @Override
    public void clearSelection(int n) {
        if (this.getSelectedIndex() == n) {
            this.clearSelection();
        }
    }

    @Override
    public boolean isEmpty() {
        return this.getItemCount() == 0 || this.getSelectedIndex() == -1;
    }

    @Override
    public boolean isSelected(int n) {
        return this.getSelectedIndex() == n;
    }

    @Override
    public void clearAndSelect(int n) {
        this.select(n);
    }

    @Override
    public void select(T t) {
        if (t == null) {
            this.setSelectedIndex(-1);
            this.setSelectedItem(null);
            return;
        }
        int n = this.getItemCount();
        for (int i = 0; i < n; ++i) {
            T t2 = this.getModelItem(i);
            if (t2 == null || !t2.equals(t)) continue;
            this.select(i);
            return;
        }
        this.setSelectedItem(t);
    }

    @Override
    public void select(int n) {
        if (n == -1) {
            this.clearSelection();
            return;
        }
        int n2 = this.getItemCount();
        if (n2 == 0 || n < 0 || n >= n2) {
            return;
        }
        this.updateSelectedIndex(n);
    }

    @Override
    public void selectPrevious() {
        if (this.getSelectedIndex() == 0) {
            return;
        }
        this.select(this.getSelectedIndex() - 1);
    }

    @Override
    public void selectNext() {
        this.select(this.getSelectedIndex() + 1);
    }

    @Override
    public void selectFirst() {
        if (this.getItemCount() > 0) {
            this.select(0);
        }
    }

    @Override
    public void selectLast() {
        int n = this.getItemCount();
        if (n > 0 && this.getSelectedIndex() < n - 1) {
            this.select(n - 1);
        }
    }

    protected abstract T getModelItem(int var1);

    protected abstract int getItemCount();

    private void updateSelectedIndex(int n) {
        int n2 = this.getSelectedIndex();
        Object t = this.getSelectedItem();
        this.setSelectedIndex(n);
        if (n2 != -1 || t == null || n != -1) {
            this.setSelectedItem(this.getModelItem(this.getSelectedIndex()));
        }
    }
}

