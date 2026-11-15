/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.runtime.async;

public interface AsyncOperation {
    public void start();

    public void cancel();

    public boolean isCancelled();

    public boolean isDone();
}

