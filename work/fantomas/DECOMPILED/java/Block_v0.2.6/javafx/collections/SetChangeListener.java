/*
 * Decompiled with CFR 0.152.
 */
package javafx.collections;

import javafx.collections.ObservableSet;

@FunctionalInterface
public interface SetChangeListener<E> {
    public void onChanged(Change<? extends E> var1);

    public static abstract class Change<E> {
        private ObservableSet<E> set;

        public Change(ObservableSet<E> observableSet) {
            this.set = observableSet;
        }

        public ObservableSet<E> getSet() {
            return this.set;
        }

        public abstract boolean wasAdded();

        public abstract boolean wasRemoved();

        public abstract E getElementAdded();

        public abstract E getElementRemoved();
    }
}

