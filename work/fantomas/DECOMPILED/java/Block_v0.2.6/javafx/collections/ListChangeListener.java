/*
 * Decompiled with CFR 0.152.
 */
package javafx.collections;

import java.util.Collections;
import java.util.List;
import javafx.collections.ObservableList;

@FunctionalInterface
public interface ListChangeListener<E> {
    public void onChanged(Change<? extends E> var1);

    public static abstract class Change<E> {
        private final ObservableList<E> list;

        public abstract boolean next();

        public abstract void reset();

        public Change(ObservableList<E> observableList) {
            this.list = observableList;
        }

        public ObservableList<E> getList() {
            return this.list;
        }

        public abstract int getFrom();

        public abstract int getTo();

        public abstract List<E> getRemoved();

        public boolean wasPermutated() {
            return this.getPermutation().length != 0;
        }

        public boolean wasAdded() {
            return !this.wasPermutated() && !this.wasUpdated() && this.getFrom() < this.getTo();
        }

        public boolean wasRemoved() {
            return !this.getRemoved().isEmpty();
        }

        public boolean wasReplaced() {
            return this.wasAdded() && this.wasRemoved();
        }

        public boolean wasUpdated() {
            return false;
        }

        public List<E> getAddedSubList() {
            return this.wasAdded() ? this.getList().subList(this.getFrom(), this.getTo()) : Collections.emptyList();
        }

        public int getRemovedSize() {
            return this.getRemoved().size();
        }

        public int getAddedSize() {
            return this.wasAdded() ? this.getTo() - this.getFrom() : 0;
        }

        protected abstract int[] getPermutation();

        public int getPermutation(int n) {
            if (!this.wasPermutated()) {
                throw new IllegalStateException("Not a permutation change");
            }
            return this.getPermutation()[n - this.getFrom()];
        }
    }
}

