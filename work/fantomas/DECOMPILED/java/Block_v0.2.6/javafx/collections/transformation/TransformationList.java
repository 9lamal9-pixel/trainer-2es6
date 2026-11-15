/*
 * Decompiled with CFR 0.152.
 */
package javafx.collections.transformation;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.collections.WeakListChangeListener;

public abstract class TransformationList<E, F>
extends ObservableListBase<E>
implements ObservableList<E> {
    private ObservableList<? extends F> source;
    private ListChangeListener<F> sourceListener;

    protected TransformationList(ObservableList<? extends F> observableList) {
        if (observableList == null) {
            throw new NullPointerException();
        }
        this.source = observableList;
        observableList.addListener(new WeakListChangeListener<F>(this.getListener()));
    }

    public final ObservableList<? extends F> getSource() {
        return this.source;
    }

    public final boolean isInTransformationChain(ObservableList<?> observableList) {
        if (this.source == observableList) {
            return true;
        }
        ObservableList<Object> observableList2 = this.source;
        while (observableList2 instanceof TransformationList) {
            observableList2 = ((TransformationList)observableList2).source;
            if (observableList2 != observableList) continue;
            return true;
        }
        return false;
    }

    private ListChangeListener<F> getListener() {
        if (this.sourceListener == null) {
            this.sourceListener = change -> this.sourceChanged(change);
        }
        return this.sourceListener;
    }

    protected abstract void sourceChanged(ListChangeListener.Change<? extends F> var1);

    public abstract int getSourceIndex(int var1);

    public final int getSourceIndexFor(ObservableList<?> observableList, int n) {
        if (!this.isInTransformationChain(observableList)) {
            throw new IllegalArgumentException("Provided list is not in the transformation chain of thistransformation list");
        }
        ObservableList<? extends F> observableList2 = this.source;
        int n2 = this.getSourceIndex(n);
        while (observableList2 != observableList && observableList2 instanceof TransformationList) {
            TransformationList transformationList = (TransformationList)observableList2;
            n2 = transformationList.getSourceIndex(n2);
            observableList2 = transformationList.source;
        }
        return n2;
    }

    public abstract int getViewIndex(int var1);
}

