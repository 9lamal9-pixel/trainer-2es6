/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;

public abstract class SelectedItemsReadOnlyObservableList<E>
extends ObservableListBase<E> {
    private final ObservableList<Integer> selectedIndices;
    private ObservableList<E> itemsList;
    private boolean itemsListChanged = false;
    private ListChangeListener.Change<? extends E> itemsListChange;
    private final ListChangeListener itemsListListener = change -> {
        this.itemsListChanged = true;
        this.itemsListChange = change;
    };
    private final Supplier<Integer> modelSizeSupplier;
    private final List<WeakReference<E>> itemsRefList;

    public SelectedItemsReadOnlyObservableList(ObservableList<Integer> observableList, Supplier<Integer> supplier) {
        this.modelSizeSupplier = supplier;
        this.selectedIndices = observableList;
        this.itemsRefList = new ArrayList<WeakReference<E>>();
        observableList.addListener((ListChangeListener.Change<? super E> change) -> {
            this.beginChange();
            while (change.next()) {
                if (change.wasReplaced()) {
                    List<E> list;
                    List<E> list2 = this.getRemovedElements(change);
                    if (list2.equals(list = this.getAddedElements(change))) continue;
                    this.nextReplace(change.getFrom(), change.getTo(), list2);
                    continue;
                }
                if (change.wasAdded()) {
                    this.nextAdd(change.getFrom(), change.getTo());
                    continue;
                }
                if (change.wasRemoved()) {
                    int n = change.getRemovedSize();
                    if (n == 1) {
                        this.nextRemove(change.getFrom(), this.getRemovedModelItem(change.getFrom()));
                        continue;
                    }
                    this.nextRemove(change.getFrom(), this.getRemovedElements(change));
                    continue;
                }
                if (change.wasPermutated()) {
                    int[] nArray = new int[this.size()];
                    for (int i = 0; i < this.size(); ++i) {
                        nArray[i] = change.getPermutation(i);
                    }
                    this.nextPermutation(change.getFrom(), change.getTo(), nArray);
                    continue;
                }
                if (!change.wasUpdated()) continue;
                for (int i = change.getFrom(); i < change.getTo(); ++i) {
                    this.nextUpdate(i);
                }
            }
            this.itemsRefList.clear();
            Iterator iterator = observableList.iterator();
            while (iterator.hasNext()) {
                int n = (Integer)iterator.next();
                this.itemsRefList.add(new WeakReference<E>(this.getModelItem(n)));
            }
            this.itemsListChanged = false;
            this.itemsListChange = null;
            this.endChange();
        });
    }

    protected abstract E getModelItem(int var1);

    @Override
    public E get(int n) {
        int n2 = (Integer)this.selectedIndices.get(n);
        return this.getModelItem(n2);
    }

    @Override
    public int size() {
        return this.selectedIndices.size();
    }

    public void setItemsList(ObservableList<E> observableList) {
        if (this.itemsList != null) {
            this.itemsList.removeListener(this.itemsListListener);
        }
        this.itemsList = observableList;
        if (observableList != null) {
            observableList.addListener(this.itemsListListener);
        }
    }

    private E _getModelItem(int n) {
        if (n >= this.modelSizeSupplier.get()) {
            return this.getRemovedModelItem(n);
        }
        return this.getModelItem(n);
    }

    private E getRemovedModelItem(int n) {
        return n < 0 || n >= this.itemsRefList.size() ? null : (E)this.itemsRefList.get(n).get();
    }

    private List<E> getRemovedElements(ListChangeListener.Change<? extends Integer> change) {
        int n;
        ArrayList<E> arrayList = new ArrayList<E>(change.getRemovedSize());
        int n2 = n + change.getRemovedSize();
        for (int i = n = change.getFrom(); i < n2; ++i) {
            arrayList.add(this.getRemovedModelItem(i));
        }
        return arrayList;
    }

    private List<E> getAddedElements(ListChangeListener.Change<? extends Integer> change) {
        ArrayList<E> arrayList = new ArrayList<E>(change.getAddedSize());
        Iterator<? extends Integer> iterator = change.getAddedSubList().iterator();
        while (iterator.hasNext()) {
            int n = iterator.next();
            arrayList.add(this._getModelItem(n));
        }
        return arrayList;
    }
}

