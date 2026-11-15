/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import com.sun.javafx.collections.NonIterableChange;
import com.sun.javafx.scene.control.MultipleAdditionAndRemovedChange;
import com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList;
import com.sun.javafx.scene.control.SelectedItemsReadOnlyObservableList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.scene.control.ControlUtils;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.util.Callback;
import javafx.util.Pair;

abstract class MultipleSelectionModelBase<T>
extends MultipleSelectionModel<T> {
    final SelectedIndicesList selectedIndices;
    private final ObservableListBase<T> selectedItems;
    ListChangeListener.Change selectedItemChange;

    public MultipleSelectionModelBase() {
        this.selectedIndexProperty().addListener(observable -> this.setSelectedItem(this.getModelItem(this.getSelectedIndex())));
        this.selectedIndices = new SelectedIndicesList();
        this.selectedItems = new SelectedItemsReadOnlyObservableList<T>(this.selectedIndices, () -> this.getItemCount()){

            @Override
            protected T getModelItem(int n) {
                return MultipleSelectionModelBase.this.getModelItem(n);
            }
        };
    }

    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return this.selectedIndices;
    }

    @Override
    public ObservableList<T> getSelectedItems() {
        return this.selectedItems;
    }

    protected abstract int getItemCount();

    protected abstract T getModelItem(int var1);

    protected abstract void focus(int var1);

    protected abstract int getFocusedIndex();

    void shiftSelection(int n, int n2, Callback<ShiftParams, Void> callback) {
        this.shiftSelection(Arrays.asList(new Pair<Integer, Integer>(n, n2)), callback);
    }

    void shiftSelection(List<Pair<Integer, Integer>> list, Callback<ShiftParams, Void> callback) {
        int n2 = this.selectedIndices.size();
        if (n2 == 0) {
            return;
        }
        int n3 = this.selectedIndices.bitsetSize();
        int[] nArray = new int[n3];
        Arrays.fill(nArray, -1);
        Collections.sort(list, (pair, pair2) -> Integer.compare((Integer)pair2.getKey(), (Integer)pair.getKey()));
        int n4 = list.get(list.size() - 1).getKey();
        BitSet bitSet = (BitSet)this.selectedIndices.bitset.clone();
        this.startAtomic();
        for (Pair<Integer, Integer> pair3 : list) {
            this.doShift(pair3, callback, nArray);
        }
        this.stopAtomic();
        Object object = Arrays.stream(nArray).filter(n -> n > -1).toArray();
        boolean bl = ((Object)object).length > 0;
        int n5 = this.getSelectedIndex();
        if (n5 >= n4 && n5 > -1) {
            int n6 = list.stream().filter(pair -> (Integer)pair.getKey() <= n5).mapToInt(pair -> (Integer)pair.getValue()).sum();
            int n7 = Math.max(0, n5 + n6);
            this.setSelectedIndex(n7);
            if (bl) {
                this.selectedIndices.set(n7, true);
            } else {
                this.select(n7);
            }
        }
        if (bl) {
            BitSet bitSet2 = (BitSet)bitSet.clone();
            bitSet2.andNot(this.selectedIndices.bitset);
            BitSet bitSet3 = (BitSet)this.selectedIndices.bitset.clone();
            bitSet3.andNot(bitSet);
            this.selectedIndices.reset();
            this.selectedIndices.callObservers(new MultipleAdditionAndRemovedChange<Integer>(bitSet3.stream().boxed().collect(Collectors.toList()), bitSet2.stream().boxed().collect(Collectors.toList()), this.selectedIndices));
        }
    }

    private void doShift(Pair<Integer, Integer> pair, Callback<ShiftParams, Void> callback, int[] nArray) {
        int n2 = pair.getKey();
        int n3 = pair.getValue();
        if (n2 < 0) {
            return;
        }
        if (n3 == 0) {
            return;
        }
        int n4 = (int)Arrays.stream(nArray).filter(n -> n > -1).count();
        int n5 = this.selectedIndices.bitsetSize() - n4;
        if (n3 > 0) {
            for (int i = n5 - 1; i >= n2 && i >= 0; --i) {
                boolean bl = this.selectedIndices.isSelected(i);
                if (callback == null) {
                    this.selectedIndices.clear(i);
                    this.selectedIndices.set(i + n3, bl);
                } else {
                    callback.call(new ShiftParams(i, i + n3, bl));
                }
                if (!bl) continue;
                nArray[n4++] = i + 1;
            }
            this.selectedIndices.clear(n2);
        } else if (n3 < 0) {
            for (int i = n2; i < n5; ++i) {
                if (i + n3 < 0 || i + 1 + n3 < n2) continue;
                boolean bl = this.selectedIndices.isSelected(i + 1);
                if (callback == null) {
                    this.selectedIndices.clear(i + 1);
                    this.selectedIndices.set(i + 1 + n3, bl);
                } else {
                    callback.call(new ShiftParams(i + 1, i + 1 + n3, bl));
                }
                if (!bl) continue;
                nArray[n4++] = i;
            }
        }
    }

    void startAtomic() {
        this.selectedIndices.startAtomic();
    }

    void stopAtomic() {
        this.selectedIndices.stopAtomic();
    }

    boolean isAtomic() {
        return this.selectedIndices.isAtomic();
    }

    @Override
    public void clearAndSelect(int n) {
        ListChangeListener.Change<Integer> change;
        if (n < 0 || n >= this.getItemCount()) {
            this.clearSelection();
            return;
        }
        boolean bl = this.isSelected(n);
        if (bl && this.getSelectedIndices().size() == 1 && this.getSelectedItem() == this.getModelItem(n)) {
            return;
        }
        BitSet bitSet = new BitSet();
        bitSet.or(this.selectedIndices.bitset);
        bitSet.clear(n);
        SelectedIndicesList selectedIndicesList = new SelectedIndicesList(bitSet);
        this.startAtomic();
        this.clearSelection();
        this.select(n);
        this.stopAtomic();
        if (bl) {
            change = ControlUtils.buildClearAndSelectChange(this.selectedIndices, selectedIndicesList, n);
        } else {
            int n2 = Math.max(0, this.selectedIndices.indexOf(n));
            change = new NonIterableChange.GenericAddRemoveChange<Integer>(n2, n2 + 1, selectedIndicesList, this.selectedIndices);
        }
        this.selectedIndices.callObservers(change);
    }

    @Override
    public void select(int n) {
        if (n == -1) {
            this.clearSelection();
            return;
        }
        if (n < 0 || n >= this.getItemCount()) {
            return;
        }
        boolean bl = n == this.getSelectedIndex();
        Object t = this.getSelectedItem();
        T t2 = this.getModelItem(n);
        boolean bl2 = t2 != null && t2.equals(t);
        boolean bl3 = bl && !bl2;
        this.focus(n);
        if (!this.selectedIndices.isSelected(n)) {
            if (this.getSelectionMode() == SelectionMode.SINGLE) {
                this.startAtomic();
                this.quietClearSelection();
                this.stopAtomic();
            }
            this.selectedIndices.set(n);
        }
        this.setSelectedIndex(n);
        if (bl3) {
            this.setSelectedItem(t2);
        }
    }

    @Override
    public void select(T t) {
        if (t == null && this.getSelectionMode() == SelectionMode.SINGLE) {
            this.clearSelection();
            return;
        }
        Object object = null;
        int n = this.getItemCount();
        for (int i = 0; i < n; ++i) {
            object = this.getModelItem(i);
            if (object == null || !object.equals(t)) continue;
            if (this.isSelected(i)) {
                return;
            }
            if (this.getSelectionMode() == SelectionMode.SINGLE) {
                this.quietClearSelection();
            }
            this.select(i);
            return;
        }
        this.setSelectedIndex(-1);
        this.setSelectedItem(t);
    }

    @Override
    public void selectIndices(int n3, int ... nArray) {
        if (nArray == null || nArray.length == 0) {
            this.select(n3);
            return;
        }
        int n4 = this.getItemCount();
        if (this.getSelectionMode() == SelectionMode.SINGLE) {
            this.quietClearSelection();
            for (int i = nArray.length - 1; i >= 0; --i) {
                int n5 = nArray[i];
                if (n5 < 0 || n5 >= n4) continue;
                this.selectedIndices.set(n5);
                this.select(n5);
                break;
            }
            if (this.selectedIndices.isEmpty() && n3 > 0 && n3 < n4) {
                this.selectedIndices.set(n3);
                this.select(n3);
            }
        } else {
            this.selectedIndices.set(n3, nArray);
            IntStream.concat(IntStream.of(n3), IntStream.of(nArray)).filter(n2 -> n2 >= 0 && n2 < n4).reduce((n, n2) -> n2).ifPresent(n -> {
                this.setSelectedIndex(n);
                this.focus(n);
                this.setSelectedItem(this.getModelItem(n));
            });
        }
    }

    @Override
    public void selectAll() {
        if (this.getSelectionMode() == SelectionMode.SINGLE) {
            return;
        }
        if (this.getItemCount() <= 0) {
            return;
        }
        int n = this.getItemCount();
        int n2 = this.getFocusedIndex();
        this.clearSelection();
        this.selectedIndices.set(0, n, true);
        if (n2 == -1) {
            this.setSelectedIndex(n - 1);
            this.focus(n - 1);
        } else {
            this.setSelectedIndex(n2);
            this.focus(n2);
        }
    }

    @Override
    public void selectFirst() {
        if (this.getSelectionMode() == SelectionMode.SINGLE) {
            this.quietClearSelection();
        }
        if (this.getItemCount() > 0) {
            this.select(0);
        }
    }

    @Override
    public void selectLast() {
        int n;
        if (this.getSelectionMode() == SelectionMode.SINGLE) {
            this.quietClearSelection();
        }
        if ((n = this.getItemCount()) > 0 && this.getSelectedIndex() < n - 1) {
            this.select(n - 1);
        }
    }

    @Override
    public void clearSelection(int n) {
        if (n < 0) {
            return;
        }
        boolean bl = this.selectedIndices.isEmpty();
        this.selectedIndices.clear(n);
        if (!bl && this.selectedIndices.isEmpty()) {
            this.clearSelection();
        }
    }

    @Override
    public void clearSelection() {
        this.quietClearSelection();
        if (!this.isAtomic()) {
            this.setSelectedIndex(-1);
            this.focus(-1);
        }
    }

    private void quietClearSelection() {
        this.selectedIndices.clear();
    }

    @Override
    public boolean isSelected(int n) {
        if (n >= 0 && n < this.selectedIndices.bitsetSize()) {
            return this.selectedIndices.isSelected(n);
        }
        return false;
    }

    @Override
    public boolean isEmpty() {
        return this.selectedIndices.isEmpty();
    }

    @Override
    public void selectPrevious() {
        int n = this.getFocusedIndex();
        if (this.getSelectionMode() == SelectionMode.SINGLE) {
            this.quietClearSelection();
        }
        if (n == -1) {
            this.select(this.getItemCount() - 1);
        } else if (n > 0) {
            this.select(n - 1);
        }
    }

    @Override
    public void selectNext() {
        int n = this.getFocusedIndex();
        if (this.getSelectionMode() == SelectionMode.SINGLE) {
            this.quietClearSelection();
        }
        if (n == -1) {
            this.select(0);
        } else if (n != this.getItemCount() - 1) {
            this.select(n + 1);
        }
    }

    class SelectedIndicesList
    extends ReadOnlyUnbackedObservableList<Integer> {
        private final BitSet bitset;
        private int lastGetIndex = -1;
        private int lastGetValue = -1;
        private int atomicityCount = 0;

        public SelectedIndicesList() {
            this(new BitSet());
        }

        public SelectedIndicesList(BitSet bitSet) {
            this.bitset = bitSet;
        }

        boolean isAtomic() {
            return this.atomicityCount > 0;
        }

        void startAtomic() {
            ++this.atomicityCount;
        }

        void stopAtomic() {
            this.atomicityCount = Math.max(0, this.atomicityCount - 1);
        }

        @Override
        public Integer get(int n) {
            int n2 = this.size();
            if (n < 0 || n >= n2) {
                throw new IndexOutOfBoundsException(n + " >= " + n2);
            }
            if (n == this.lastGetIndex + 1 && this.lastGetValue < n2) {
                ++this.lastGetIndex;
                this.lastGetValue = this.bitset.nextSetBit(this.lastGetValue + 1);
                return this.lastGetValue;
            }
            if (n == this.lastGetIndex - 1 && this.lastGetValue > 0) {
                --this.lastGetIndex;
                this.lastGetValue = this.bitset.previousSetBit(this.lastGetValue - 1);
                return this.lastGetValue;
            }
            this.lastGetIndex = 0;
            this.lastGetValue = this.bitset.nextSetBit(0);
            while (this.lastGetValue >= 0 || this.lastGetIndex == n) {
                if (this.lastGetIndex == n) {
                    return this.lastGetValue;
                }
                ++this.lastGetIndex;
                this.lastGetValue = this.bitset.nextSetBit(this.lastGetValue + 1);
            }
            return -1;
        }

        public void set(int n) {
            if (!this.isValidIndex(n) || this.isSelected(n)) {
                return;
            }
            this._beginChange();
            this.bitset.set(n);
            int n2 = this.indexOf(n);
            this._nextAdd(n2, n2 + 1);
            this._endChange();
        }

        private boolean isValidIndex(int n) {
            return n >= 0 && n < MultipleSelectionModelBase.this.getItemCount();
        }

        @Override
        public void set(int n, boolean bl) {
            if (bl) {
                this.set(n);
            } else {
                this.clear(n);
            }
        }

        public void set(int n, int n2, boolean bl) {
            this._beginChange();
            if (bl) {
                this.bitset.set(n, n2, bl);
                int n3 = this.indexOf(n);
                int n4 = n2 - n;
                this._nextAdd(n3, n3 + n4);
            } else {
                this.bitset.set(n, n2, bl);
            }
            this._endChange();
        }

        @Override
        public void set(int n, int ... nArray) {
            if (nArray == null || nArray.length == 0) {
                this.set(n);
            } else {
                this.startAtomic();
                List list = IntStream.concat(IntStream.of(n), IntStream.of(nArray)).distinct().filter(this::isValidIndex).filter(this::isNotSelected).sorted().boxed().peek(this::set).collect(Collectors.toList());
                this.stopAtomic();
                int n2 = list.size();
                if (n2 != 0) {
                    if (n2 == 1) {
                        this._beginChange();
                        int n3 = (Integer)list.get(0);
                        int n4 = this.indexOf(n3);
                        this._nextAdd(n4, n4 + 1);
                        this._endChange();
                    } else {
                        this._beginChange();
                        int n5 = 0;
                        int n6 = 0;
                        int n7 = 0;
                        int n8 = (Integer)list.get(n5++);
                        n6 = this.indexOf(n8);
                        n7 = n6 + 1;
                        int n9 = n8;
                        while (n5 < n2) {
                            int n10 = n9;
                            n9 = (Integer)list.get(n5++);
                            ++n7;
                            if (n10 != n9 - 1) {
                                this._nextAdd(n6, n7);
                                n6 = n7;
                                continue;
                            }
                            if (n5 != n2) continue;
                            this._nextAdd(n6, n6 + n5);
                        }
                        this._endChange();
                    }
                }
            }
        }

        @Override
        public void clear() {
            this._beginChange();
            List list = this.bitset.stream().boxed().collect(Collectors.toList());
            this.bitset.clear();
            this._nextRemove(0, list);
            this._endChange();
        }

        public void clear(int n) {
            if (!this.bitset.get(n)) {
                return;
            }
            this._beginChange();
            this.bitset.clear(n);
            this._nextRemove(n, n);
            this._endChange();
        }

        public boolean isSelected(int n) {
            return this.bitset.get(n);
        }

        public boolean isNotSelected(int n) {
            return !this.isSelected(n);
        }

        @Override
        public int size() {
            return this.bitset.cardinality();
        }

        public int bitsetSize() {
            return this.bitset.size();
        }

        @Override
        public int indexOf(Object object) {
            this.reset();
            return super.indexOf(object);
        }

        @Override
        public boolean contains(Object object) {
            if (object instanceof Number) {
                Number number = (Number)object;
                int n = number.intValue();
                return n >= 0 && n < this.bitset.length() && this.bitset.get(n);
            }
            return false;
        }

        public void reset() {
            this.lastGetIndex = -1;
            this.lastGetValue = -1;
        }

        @Override
        public void _beginChange() {
            if (!this.isAtomic()) {
                super._beginChange();
            }
        }

        @Override
        public void _endChange() {
            if (!this.isAtomic()) {
                super._endChange();
            }
        }

        @Override
        public final void _nextUpdate(int n) {
            if (!this.isAtomic()) {
                this.nextUpdate(n);
            }
        }

        @Override
        public final void _nextSet(int n, Integer n2) {
            if (!this.isAtomic()) {
                this.nextSet(n, n2);
            }
        }

        @Override
        public final void _nextReplace(int n, int n2, List<? extends Integer> list) {
            if (!this.isAtomic()) {
                this.nextReplace(n, n2, list);
            }
        }

        @Override
        public final void _nextRemove(int n, List<? extends Integer> list) {
            if (!this.isAtomic()) {
                this.nextRemove(n, list);
            }
        }

        @Override
        public final void _nextRemove(int n, Integer n2) {
            if (!this.isAtomic()) {
                this.nextRemove(n, n2);
            }
        }

        @Override
        public final void _nextPermutation(int n, int n2, int[] nArray) {
            if (!this.isAtomic()) {
                this.nextPermutation(n, n2, nArray);
            }
        }

        @Override
        public final void _nextAdd(int n, int n2) {
            if (!this.isAtomic()) {
                this.nextAdd(n, n2);
            }
        }
    }

    static class ShiftParams {
        private final int clearIndex;
        private final int setIndex;
        private final boolean selected;

        ShiftParams(int n, int n2, boolean bl) {
            this.clearIndex = n;
            this.setIndex = n2;
            this.selected = bl;
        }

        public final int getClearIndex() {
            return this.clearIndex;
        }

        public final int getSetIndex() {
            return this.setIndex;
        }

        public final boolean isSelected() {
            return this.selected;
        }
    }
}

