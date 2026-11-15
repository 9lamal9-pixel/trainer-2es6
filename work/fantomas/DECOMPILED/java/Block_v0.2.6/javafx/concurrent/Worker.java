/*
 * Decompiled with CFR 0.152.
 */
package javafx.concurrent;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;

public interface Worker<V> {
    public State getState();

    public ReadOnlyObjectProperty<State> stateProperty();

    public V getValue();

    public ReadOnlyObjectProperty<V> valueProperty();

    public Throwable getException();

    public ReadOnlyObjectProperty<Throwable> exceptionProperty();

    public double getWorkDone();

    public ReadOnlyDoubleProperty workDoneProperty();

    public double getTotalWork();

    public ReadOnlyDoubleProperty totalWorkProperty();

    public double getProgress();

    public ReadOnlyDoubleProperty progressProperty();

    public boolean isRunning();

    public ReadOnlyBooleanProperty runningProperty();

    public String getMessage();

    public ReadOnlyStringProperty messageProperty();

    public String getTitle();

    public ReadOnlyStringProperty titleProperty();

    public boolean cancel();

    public static enum State {
        READY,
        SCHEDULED,
        RUNNING,
        SUCCEEDED,
        CANCELLED,
        FAILED;

    }
}

