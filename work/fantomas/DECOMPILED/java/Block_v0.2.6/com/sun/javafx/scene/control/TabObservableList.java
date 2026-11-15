/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control;

import com.sun.javafx.collections.NonIterableChange;
import com.sun.javafx.collections.ObservableListWrapper;
import java.util.List;
import java.util.ListIterator;
import javafx.scene.control.Tab;

public class TabObservableList<E>
extends ObservableListWrapper<E> {
    private final List<E> tabList;

    public TabObservableList(List<E> list) {
        super(list);
        this.tabList = list;
    }

    public void reorder(Tab tab, Tab tab2) {
        int n;
        if (!this.tabList.contains(tab) || !this.tabList.contains(tab2) || tab == tab2) {
            return;
        }
        Object[] objectArray = this.tabList.toArray();
        int n2 = this.tabList.indexOf(tab);
        int n3 = this.tabList.indexOf(tab2);
        if (n2 == -1 || n3 == -1) {
            return;
        }
        int n4 = (n3 - n2) / Math.abs(n3 - n2);
        for (int i = n2; i != n3; i += n4) {
            objectArray[i] = objectArray[i + n4];
        }
        objectArray[n3] = tab;
        ListIterator<Object> listIterator = this.tabList.listIterator();
        for (n = 0; n < this.tabList.size(); ++n) {
            listIterator.next();
            listIterator.set(objectArray[n]);
        }
        tab.getTabPane().getSelectionModel().select(tab);
        n = Math.abs(n3 - n2) + 1;
        int[] nArray = new int[n];
        int n5 = n4 > 0 ? n2 : n3;
        int n6 = n4 < 0 ? n2 : n3;
        for (int i = 0; i < n; ++i) {
            nArray[i] = i + n5;
        }
        this.fireChange(new NonIterableChange.SimplePermutationChange(n5, n6 + 1, nArray, this));
    }
}

