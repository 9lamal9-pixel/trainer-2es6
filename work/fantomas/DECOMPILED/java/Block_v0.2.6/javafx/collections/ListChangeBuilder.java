/*
 * Decompiled with CFR 0.152.
 */
package javafx.collections;

import com.sun.javafx.collections.ChangeHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;

final class ListChangeBuilder<E> {
    private static final int[] EMPTY_PERM = new int[0];
    private final ObservableListBase<E> list;
    private int changeLock;
    private List<SubChange<E>> addRemoveChanges;
    private List<SubChange<E>> updateChanges;
    private SubChange<E> permutationChange;

    private void checkAddRemoveList() {
        if (this.addRemoveChanges == null) {
            this.addRemoveChanges = new ArrayList<SubChange<E>>();
        }
    }

    private void checkState() {
        if (this.changeLock == 0) {
            throw new IllegalStateException("beginChange was not called on this builder");
        }
    }

    private int findSubChange(int n, List<SubChange<E>> list) {
        int n2 = 0;
        int n3 = list.size() - 1;
        while (n2 <= n3) {
            int n4 = (n2 + n3) / 2;
            SubChange<E> subChange = list.get(n4);
            if (n >= subChange.to) {
                n2 = n4 + 1;
                continue;
            }
            if (n < subChange.from) {
                n3 = n4 - 1;
                continue;
            }
            return n4;
        }
        return ~n2;
    }

    /*
     * Enabled aggressive block sorting
     */
    private void insertUpdate(int n) {
        SubChange<E> subChange;
        int n2 = this.findSubChange(n, this.updateChanges);
        if (n2 >= 0) return;
        if ((n2 ^= 0xFFFFFFFF) > 0) {
            subChange = this.updateChanges.get(n2 - 1);
            if (subChange.to == n) {
                subChange.to = n + 1;
                return;
            }
        }
        if (n2 < this.updateChanges.size()) {
            subChange = this.updateChanges.get(n2);
            if (subChange.from == n + 1) {
                subChange.from = n;
                return;
            }
        }
        this.updateChanges.add(n2, new SubChange(n, n + 1, null, EMPTY_PERM, true));
    }

    /*
     * Unable to fully structure code
     */
    private void insertRemoved(int var1_1, E var2_2) {
        block6: {
            block4: {
                block5: {
                    var3_3 = this.findSubChange(var1_1, this.addRemoveChanges);
                    if (var3_3 >= 0) break block4;
                    if ((var3_3 ^= -1) <= 0) break block5;
                    var4_4 = this.addRemoveChanges.get(var3_3 - 1);
                    if (var4_4.to != var1_1) break block5;
                    var4_4.removed.add(var2_2);
                    --var3_3;
                    break block6;
                }
                if (var3_3 >= this.addRemoveChanges.size()) ** GOTO lbl-1000
                var4_4 = this.addRemoveChanges.get(var3_3);
                if (var4_4.from == var1_1 + 1) {
                    --var4_4.from;
                    --var4_4.to;
                    var4_4.removed.add(0, var2_2);
                } else lbl-1000:
                // 2 sources

                {
                    var5_6 = new ArrayList<E>();
                    var5_6.add(var2_2);
                    this.addRemoveChanges.add(var3_3, new SubChange<E>(var1_1, var1_1, var5_6, ListChangeBuilder.EMPTY_PERM, false));
                }
                break block6;
            }
            var4_4 = this.addRemoveChanges.get(var3_3);
            --var4_4.to;
            if (var4_4.from == var4_4.to && (var4_4.removed == null || var4_4.removed.isEmpty())) {
                this.addRemoveChanges.remove(var3_3);
            }
        }
        for (var4_5 = var3_3 + 1; var4_5 < this.addRemoveChanges.size(); ++var4_5) {
            var5_6 = this.addRemoveChanges.get(var4_5);
            --var5_6.from;
            --var5_6.to;
        }
    }

    /*
     * Unable to fully structure code
     */
    private void insertAdd(int var1_1, int var2_2) {
        block4: {
            block3: {
                var3_3 = this.findSubChange(var1_1, this.addRemoveChanges);
                var4_4 = var2_2 - var1_1;
                if (var3_3 >= 0) break block3;
                if ((var3_3 ^= -1) <= 0) ** GOTO lbl-1000
                var5_5 = this.addRemoveChanges.get(var3_3 - 1);
                if (var5_5.to == var1_1) {
                    var5_5.to = var2_2;
                    --var3_3;
                } else lbl-1000:
                // 2 sources

                {
                    this.addRemoveChanges.add(var3_3, new SubChange<E>(var1_1, var2_2, new ArrayList<E>(), ListChangeBuilder.EMPTY_PERM, false));
                }
                break block4;
            }
            var5_5 = this.addRemoveChanges.get(var3_3);
            var5_5.to += var4_4;
        }
        for (var5_6 = var3_3 + 1; var5_6 < this.addRemoveChanges.size(); ++var5_6) {
            var6_7 = this.addRemoveChanges.get(var5_6);
            var6_7.from += var4_4;
            var6_7.to += var4_4;
        }
    }

    private int compress(List<SubChange<E>> list) {
        int n = 0;
        SubChange<E> subChange = list.get(0);
        int n2 = list.size();
        for (int i = 1; i < n2; ++i) {
            SubChange<E> subChange2 = list.get(i);
            if (subChange.to == subChange2.from) {
                subChange.to = subChange2.to;
                if (subChange.removed != null || subChange2.removed != null) {
                    if (subChange.removed == null) {
                        subChange.removed = new ArrayList();
                    }
                    subChange.removed.addAll(subChange2.removed);
                }
                list.set(i, null);
                ++n;
                continue;
            }
            subChange = subChange2;
        }
        return n;
    }

    ListChangeBuilder(ObservableListBase<E> observableListBase) {
        this.list = observableListBase;
    }

    public void nextRemove(int n, E e) {
        SubChange<E> subChange;
        this.checkState();
        this.checkAddRemoveList();
        SubChange<E> subChange2 = subChange = this.addRemoveChanges.isEmpty() ? null : this.addRemoveChanges.get(this.addRemoveChanges.size() - 1);
        if (subChange != null && subChange.to == n) {
            subChange.removed.add(e);
        } else if (subChange != null && subChange.from == n + 1) {
            --subChange.from;
            --subChange.to;
            subChange.removed.add(0, e);
        } else {
            this.insertRemoved(n, e);
        }
        if (this.updateChanges != null && !this.updateChanges.isEmpty()) {
            int n2 = this.findSubChange(n, this.updateChanges);
            if (n2 < 0) {
                n2 ^= 0xFFFFFFFF;
            } else {
                SubChange<E> subChange3 = this.updateChanges.get(n2);
                if (subChange3.from == subChange3.to - 1) {
                    this.updateChanges.remove(n2);
                } else {
                    --subChange3.to;
                    ++n2;
                }
            }
            for (int i = n2; i < this.updateChanges.size(); ++i) {
                --this.updateChanges.get((int)i).from;
                --this.updateChanges.get((int)i).to;
            }
        }
    }

    public void nextRemove(int n, List<? extends E> list) {
        this.checkState();
        for (int i = 0; i < list.size(); ++i) {
            this.nextRemove(n, list.get(i));
        }
    }

    public void nextAdd(int n, int n2) {
        this.checkState();
        this.checkAddRemoveList();
        SubChange<E> subChange = this.addRemoveChanges.isEmpty() ? null : this.addRemoveChanges.get(this.addRemoveChanges.size() - 1);
        int n3 = n2 - n;
        if (subChange != null && subChange.to == n) {
            subChange.to = n2;
        } else if (subChange != null && n >= subChange.from && n < subChange.to) {
            subChange.to += n3;
        } else {
            this.insertAdd(n, n2);
        }
        if (this.updateChanges != null && !this.updateChanges.isEmpty()) {
            int n4 = this.findSubChange(n, this.updateChanges);
            if (n4 < 0) {
                n4 ^= 0xFFFFFFFF;
            } else {
                SubChange<E> subChange2 = this.updateChanges.get(n4);
                this.updateChanges.add(n4 + 1, new SubChange(n2, subChange2.to + n2 - n, null, EMPTY_PERM, true));
                subChange2.to = n;
                n4 += 2;
            }
            for (int i = n4; i < this.updateChanges.size(); ++i) {
                this.updateChanges.get((int)i).from += n3;
                this.updateChanges.get((int)i).to += n3;
            }
        }
    }

    /*
     * WARNING - void declaration
     */
    public void nextPermutation(int n, int n2, int[] nArray) {
        int n3;
        int n4;
        int n5;
        int n6;
        Cloneable cloneable;
        this.checkState();
        int n7 = n;
        int n8 = n2;
        int[] nArray2 = nArray;
        if (this.addRemoveChanges != null && !this.addRemoveChanges.isEmpty()) {
            int subChange;
            void nArray4;
            int[] nArray3 = new int[this.list.size()];
            cloneable = new TreeSet();
            n6 = 0;
            n5 = 0;
            boolean n9 = false;
            n4 = this.addRemoveChanges.size();
            while (nArray4 < n4) {
                int n10;
                SubChange<E> subChange2 = this.addRemoveChanges.get((int)nArray4);
                for (n10 = n6; n10 < subChange2.from; ++n10) {
                    nArray3[n10 < n || n10 >= n2 ? n10 : nArray[n10 - n]] = n10 + n5;
                }
                for (n10 = subChange2.from; n10 < subChange2.to; ++n10) {
                    nArray3[n10 < n || n10 >= n2 ? n10 : nArray[n10 - n]] = -1;
                }
                n6 = subChange2.to;
                n10 = subChange2.removed != null ? subChange2.removed.size() : 0;
                int n11 = subChange2.from + n5 + n10;
                for (int i = subChange2.from + n5; i < n11; ++i) {
                    cloneable.add(i);
                }
                n5 += n10 - (subChange2.to - subChange2.from);
                ++nArray4;
            }
            int n14 = n6;
            while (subChange < nArray3.length) {
                nArray3[subChange < n || subChange >= n2 ? subChange : nArray[subChange - n]] = subChange + n5;
                ++subChange;
            }
            int[] n15 = new int[this.list.size() + n5];
            n4 = 0;
            for (n3 = 0; n3 < n15.length; ++n3) {
                if (cloneable.contains(n3)) {
                    n15[n3] = n3;
                    continue;
                }
                while (nArray3[n4] == -1) {
                    ++n4;
                }
                n15[nArray3[n4++]] = n3;
            }
            n7 = 0;
            n8 = n15.length;
            nArray2 = n15;
        }
        if (this.permutationChange != null) {
            if (n7 == this.permutationChange.from && n8 == this.permutationChange.to) {
                for (int i = 0; i < nArray2.length; ++i) {
                    this.permutationChange.perm[i] = nArray2[this.permutationChange.perm[i] - n7];
                }
            } else {
                int n12 = Math.max(this.permutationChange.to, n8);
                int n13 = Math.min(this.permutationChange.from, n7);
                int[] nArray5 = new int[n12 - n13];
                for (n5 = n13; n5 < n12; ++n5) {
                    int entry;
                    nArray5[n5 - n13] = n5 < this.permutationChange.from || n5 >= this.permutationChange.to ? nArray2[n5 - n7] : ((entry = this.permutationChange.perm[n5 - this.permutationChange.from]) < n7 || entry >= n8 ? entry : nArray2[entry - n7]);
                }
                this.permutationChange.from = n13;
                this.permutationChange.to = n12;
                this.permutationChange.perm = nArray5;
            }
        } else {
            this.permutationChange = new SubChange(n7, n8, null, nArray2, false);
        }
        if (this.addRemoveChanges != null && !this.addRemoveChanges.isEmpty()) {
            TreeSet<Integer> treeSet = new TreeSet<Integer>();
            cloneable = new HashMap();
            n5 = this.addRemoveChanges.size();
            for (n6 = 0; n6 < n5; ++n6) {
                SubChange<E> j = this.addRemoveChanges.get(n6);
                for (n4 = j.from; n4 < j.to; ++n4) {
                    if (n4 < n || n4 >= n2) {
                        treeSet.add(n4);
                        continue;
                    }
                    treeSet.add(nArray[n4 - n]);
                }
                if (j.removed == null) continue;
                if (j.from < n || j.from >= n2) {
                    cloneable.put(j.from, j.removed);
                    continue;
                }
                cloneable.put(nArray[j.from - n], j.removed);
            }
            this.addRemoveChanges.clear();
            SubChange subChange = null;
            for (Integer n12 : treeSet) {
                List list;
                if (subChange == null || subChange.to != n12) {
                    subChange = new SubChange(n12, n12 + 1, null, EMPTY_PERM, false);
                    this.addRemoveChanges.add(subChange);
                } else {
                    subChange.to = n12 + 1;
                }
                if ((list = (List)cloneable.remove(n12)) == null) continue;
                if (subChange.removed != null) {
                    subChange.removed.addAll(list);
                    continue;
                }
                subChange.removed = list;
            }
            for (Map.Entry entry : cloneable.entrySet()) {
                Integer n13 = (Integer)entry.getKey();
                n3 = this.findSubChange(n13, this.addRemoveChanges);
                assert (n3 < 0);
                this.addRemoveChanges.add(~n3, new SubChange(n13, n13, (List)entry.getValue(), new int[0], false));
            }
        }
        if (this.updateChanges != null && !this.updateChanges.isEmpty()) {
            TreeSet<Integer> treeSet = new TreeSet<Integer>();
            n6 = this.updateChanges.size();
            for (int i = 0; i < n6; ++i) {
                void var11_34;
                SubChange<E> subChange = this.updateChanges.get(i);
                int n14 = subChange.from;
                while (var11_34 < subChange.to) {
                    if (var11_34 < n || var11_34 >= n2) {
                        treeSet.add((int)var11_34);
                    } else {
                        treeSet.add(nArray[var11_34 - n]);
                    }
                    ++var11_34;
                }
            }
            this.updateChanges.clear();
            SubChange subChange = null;
            for (Integer n17 : treeSet) {
                if (subChange == null || subChange.to != n17) {
                    subChange = new SubChange(n17, n17 + 1, null, EMPTY_PERM, true);
                    this.updateChanges.add(subChange);
                    continue;
                }
                subChange.to = n17 + 1;
            }
        }
    }

    public void nextReplace(int n, int n2, List<? extends E> list) {
        this.nextRemove(n, list);
        this.nextAdd(n, n2);
    }

    public void nextSet(int n, E e) {
        this.nextRemove(n, e);
        this.nextAdd(n, n + 1);
    }

    public void nextUpdate(int n) {
        SubChange<E> subChange;
        this.checkState();
        if (this.updateChanges == null) {
            this.updateChanges = new ArrayList<SubChange<E>>();
        }
        SubChange<E> subChange2 = subChange = this.updateChanges.isEmpty() ? null : this.updateChanges.get(this.updateChanges.size() - 1);
        if (subChange != null && subChange.to == n) {
            subChange.to = n + 1;
        } else {
            this.insertUpdate(n);
        }
    }

    private void commit() {
        boolean bl;
        boolean bl2 = this.addRemoveChanges != null && !this.addRemoveChanges.isEmpty();
        boolean bl3 = bl = this.updateChanges != null && !this.updateChanges.isEmpty();
        if (this.changeLock == 0 && (bl2 || bl || this.permutationChange != null)) {
            int n = (this.updateChanges != null ? this.updateChanges.size() : 0) + (this.addRemoveChanges != null ? this.addRemoveChanges.size() : 0) + (this.permutationChange != null ? 1 : 0);
            if (n == 1) {
                if (bl2) {
                    this.list.fireChange(new SingleChange<E>(ListChangeBuilder.finalizeSubChange(this.addRemoveChanges.get(0)), this.list));
                    this.addRemoveChanges.clear();
                } else if (bl) {
                    this.list.fireChange(new SingleChange<E>(ListChangeBuilder.finalizeSubChange(this.updateChanges.get(0)), this.list));
                    this.updateChanges.clear();
                } else {
                    this.list.fireChange(new SingleChange<E>(ListChangeBuilder.finalizeSubChange(this.permutationChange), this.list));
                    this.permutationChange = null;
                }
            } else {
                SubChange<E> subChange;
                int n2;
                int n3;
                int n4;
                if (bl) {
                    n4 = this.compress(this.updateChanges);
                    n -= n4;
                }
                if (bl2) {
                    n4 = this.compress(this.addRemoveChanges);
                    n -= n4;
                }
                SubChange[] subChangeArray = new SubChange[n];
                int n5 = 0;
                if (this.permutationChange != null) {
                    subChangeArray[n5++] = this.permutationChange;
                }
                if (bl2) {
                    n3 = this.addRemoveChanges.size();
                    for (n2 = 0; n2 < n3; ++n2) {
                        subChange = this.addRemoveChanges.get(n2);
                        if (subChange == null) continue;
                        subChangeArray[n5++] = subChange;
                    }
                }
                if (bl) {
                    n3 = this.updateChanges.size();
                    for (n2 = 0; n2 < n3; ++n2) {
                        subChange = this.updateChanges.get(n2);
                        if (subChange == null) continue;
                        subChangeArray[n5++] = subChange;
                    }
                }
                this.list.fireChange(new IterableChange(ListChangeBuilder.finalizeSubChangeArray(subChangeArray), this.list));
                if (this.addRemoveChanges != null) {
                    this.addRemoveChanges.clear();
                }
                if (this.updateChanges != null) {
                    this.updateChanges.clear();
                }
                this.permutationChange = null;
            }
        }
    }

    public void beginChange() {
        ++this.changeLock;
    }

    public void endChange() {
        if (this.changeLock <= 0) {
            throw new IllegalStateException("Called endChange before beginChange");
        }
        --this.changeLock;
        this.commit();
    }

    private static <E> SubChange<E>[] finalizeSubChangeArray(SubChange<E>[] subChangeArray) {
        for (SubChange<E> subChange : subChangeArray) {
            ListChangeBuilder.finalizeSubChange(subChange);
        }
        return subChangeArray;
    }

    private static <E> SubChange<E> finalizeSubChange(SubChange<E> subChange) {
        if (subChange.perm == null) {
            subChange.perm = EMPTY_PERM;
        }
        subChange.removed = subChange.removed == null ? Collections.emptyList() : Collections.unmodifiableList(subChange.removed);
        return subChange;
    }

    private static class IterableChange<E>
    extends ListChangeListener.Change<E> {
        private SubChange[] changes;
        private int cursor = -1;

        private IterableChange(SubChange[] subChangeArray, ObservableList<E> observableList) {
            super(observableList);
            this.changes = subChangeArray;
        }

        @Override
        public boolean next() {
            if (this.cursor + 1 < this.changes.length) {
                ++this.cursor;
                return true;
            }
            return false;
        }

        @Override
        public void reset() {
            this.cursor = -1;
        }

        @Override
        public int getFrom() {
            this.checkState();
            return this.changes[this.cursor].from;
        }

        @Override
        public int getTo() {
            this.checkState();
            return this.changes[this.cursor].to;
        }

        @Override
        public List<E> getRemoved() {
            this.checkState();
            return this.changes[this.cursor].removed;
        }

        @Override
        protected int[] getPermutation() {
            this.checkState();
            return this.changes[this.cursor].perm;
        }

        @Override
        public boolean wasUpdated() {
            this.checkState();
            return this.changes[this.cursor].updated;
        }

        private void checkState() {
            if (this.cursor == -1) {
                throw new IllegalStateException("Invalid Change state: next() must be called before inspecting the Change.");
            }
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{ ");
            for (int i = 0; i < this.changes.length; ++i) {
                if (this.changes[i].perm.length != 0) {
                    stringBuilder.append(ChangeHelper.permChangeToString(this.changes[i].perm));
                } else if (this.changes[i].updated) {
                    stringBuilder.append(ChangeHelper.updateChangeToString(this.changes[i].from, this.changes[i].to));
                } else {
                    stringBuilder.append(ChangeHelper.addRemoveChangeToString(this.changes[i].from, this.changes[i].to, this.getList(), this.changes[i].removed));
                }
                if (i == this.changes.length - 1) continue;
                stringBuilder.append(", ");
            }
            stringBuilder.append(" }");
            return stringBuilder.toString();
        }
    }

    private static class SingleChange<E>
    extends ListChangeListener.Change<E> {
        private final SubChange<E> change;
        private boolean onChange;

        public SingleChange(SubChange<E> subChange, ObservableListBase<E> observableListBase) {
            super(observableListBase);
            this.change = subChange;
        }

        @Override
        public boolean next() {
            if (this.onChange) {
                return false;
            }
            this.onChange = true;
            return true;
        }

        @Override
        public void reset() {
            this.onChange = false;
        }

        @Override
        public int getFrom() {
            this.checkState();
            return this.change.from;
        }

        @Override
        public int getTo() {
            this.checkState();
            return this.change.to;
        }

        @Override
        public List<E> getRemoved() {
            this.checkState();
            return this.change.removed;
        }

        @Override
        protected int[] getPermutation() {
            this.checkState();
            return this.change.perm;
        }

        @Override
        public boolean wasUpdated() {
            this.checkState();
            return this.change.updated;
        }

        private void checkState() {
            if (!this.onChange) {
                throw new IllegalStateException("Invalid Change state: next() must be called before inspecting the Change.");
            }
        }

        public String toString() {
            String string = this.change.perm.length != 0 ? ChangeHelper.permChangeToString(this.change.perm) : (this.change.updated ? ChangeHelper.updateChangeToString(this.change.from, this.change.to) : ChangeHelper.addRemoveChangeToString(this.change.from, this.change.to, this.getList(), this.change.removed));
            return "{ " + string + " }";
        }
    }

    private static class SubChange<E> {
        int from;
        int to;
        List<E> removed;
        int[] perm;
        boolean updated;

        public SubChange(int n, int n2, List<E> list, int[] nArray, boolean bl) {
            this.from = n;
            this.to = n2;
            this.removed = list;
            this.perm = nArray;
            this.updated = bl;
        }
    }
}

