/*
 * Decompiled with CFR 0.152.
 */
package javafx.collections;

import com.sun.javafx.collections.ListListenerHelper;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import javafx.beans.InvalidationListener;
import javafx.collections.ListChangeBuilder;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

public abstract class ObservableListBase<E>
extends AbstractList<E>
implements ObservableList<E> {
    private ListListenerHelper<E> listenerHelper;
    private final ListChangeBuilder<E> changeBuilder = new ListChangeBuilder(this);

    protected final void nextUpdate(int n) {
        this.changeBuilder.nextUpdate(n);
    }

    protected final void nextSet(int n, E e) {
        this.changeBuilder.nextSet(n, e);
    }

    protected final void nextReplace(int n, int n2, List<? extends E> list) {
        this.changeBuilder.nextReplace(n, n2, list);
    }

    protected final void nextRemove(int n, List<? extends E> list) {
        this.changeBuilder.nextRemove(n, list);
    }

    protected final void nextRemove(int n, E e) {
        this.changeBuilder.nextRemove(n, e);
    }

    protected final void nextPermutation(int n, int n2, int[] nArray) {
        this.changeBuilder.nextPermutation(n, n2, nArray);
    }

    protected final void nextAdd(int n, int n2) {
        this.changeBuilder.nextAdd(n, n2);
    }

    protected final void beginChange() {
        this.changeBuilder.beginChange();
    }

    protected final void endChange() {
        this.changeBuilder.endChange();
    }

    @Override
    public final void addListener(InvalidationListener invalidationListener) {
        this.listenerHelper = ListListenerHelper.addListener(this.listenerHelper, invalidationListener);
    }

    @Override
    public final void removeListener(InvalidationListener invalidationListener) {
        this.listenerHelper = ListListenerHelper.removeListener(this.listenerHelper, invalidationListener);
    }

    @Override
    public final void addListener(ListChangeListener<? super E> listChangeListener) {
        this.listenerHelper = ListListenerHelper.addListener(this.listenerHelper, listChangeListener);
    }

    @Override
    public final void removeListener(ListChangeListener<? super E> listChangeListener) {
        this.listenerHelper = ListListenerHelper.removeListener(this.listenerHelper, listChangeListener);
    }

    protected final void fireChange(ListChangeListener.Change<? extends E> change) {
        ListListenerHelper.fireValueChangedEvent(this.listenerHelper, change);
    }

    protected final boolean hasListeners() {
        return ListListenerHelper.hasListeners(this.listenerHelper);
    }

    @Override
    public boolean addAll(E ... EArray) {
        return this.addAll(Arrays.asList(EArray));
    }

    @Override
    public boolean setAll(E ... EArray) {
        return this.setAll((Collection<? extends E>)Arrays.asList(EArray));
    }

    @Override
    public boolean setAll(Collection<? extends E> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(E ... EArray) {
        return this.removeAll((Collection<?>)Arrays.asList(EArray));
    }

    @Override
    public boolean retainAll(E ... EArray) {
        return this.retainAll((Collection<?>)Arrays.asList(EArray));
    }

    @Override
    public void remove(int n, int n2) {
        this.removeRange(n, n2);
    }
}

