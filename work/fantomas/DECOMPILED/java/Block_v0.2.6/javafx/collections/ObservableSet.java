/*
 * Decompiled with CFR 0.152.
 */
package javafx.collections;

import java.util.Set;
import javafx.beans.Observable;
import javafx.collections.SetChangeListener;

public interface ObservableSet<E>
extends Set<E>,
Observable {
    public void addListener(SetChangeListener<? super E> var1);

    public void removeListener(SetChangeListener<? super E> var1);
}

