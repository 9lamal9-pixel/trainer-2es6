/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.scene.control;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;

public final class LambdaMultiplePropertyChangeListenerHandler {
    private final Map<ObservableValue<?>, Consumer<ObservableValue<?>>> propertyReferenceMap = new HashMap();
    private final ChangeListener<Object> propertyChangedListener = (observableValue, object, object2) -> this.propertyReferenceMap.getOrDefault(observableValue, EMPTY_CONSUMER).accept(observableValue);
    private final WeakChangeListener<Object> weakPropertyChangedListener = new WeakChangeListener<Object>(this.propertyChangedListener);
    private static final Consumer<ObservableValue<?>> EMPTY_CONSUMER = observableValue -> {};

    public final void registerChangeListener(ObservableValue<?> observableValue, Consumer<ObservableValue<?>> consumer) {
        if (consumer == null) {
            return;
        }
        if (!this.propertyReferenceMap.containsKey(observableValue)) {
            observableValue.addListener(this.weakPropertyChangedListener);
        }
        this.propertyReferenceMap.merge(observableValue, consumer, Consumer::andThen);
    }

    public final Consumer<ObservableValue<?>> unregisterChangeListeners(ObservableValue<?> observableValue) {
        observableValue.removeListener(this.weakPropertyChangedListener);
        return this.propertyReferenceMap.remove(observableValue);
    }

    public void dispose() {
        for (ObservableValue<?> observableValue : this.propertyReferenceMap.keySet()) {
            observableValue.removeListener(this.weakPropertyChangedListener);
        }
        this.propertyReferenceMap.clear();
    }
}

