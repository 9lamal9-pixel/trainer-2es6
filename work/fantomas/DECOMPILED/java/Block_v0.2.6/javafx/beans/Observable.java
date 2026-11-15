/*
 * Decompiled with CFR 0.152.
 */
package javafx.beans;

import javafx.beans.InvalidationListener;

public interface Observable {
    public void addListener(InvalidationListener var1);

    public void removeListener(InvalidationListener var1);
}

