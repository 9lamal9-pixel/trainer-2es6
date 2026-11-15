/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import com.sun.javafx.util.WeakReferenceQueue;
import java.util.Iterator;
import javafx.scene.Parent;

public abstract class ConstraintsBase {
    public static final double CONSTRAIN_TO_PREF = Double.NEGATIVE_INFINITY;
    private WeakReferenceQueue nodes = new WeakReferenceQueue();

    ConstraintsBase() {
    }

    void add(Parent parent) {
        this.nodes.add(parent);
    }

    void remove(Parent parent) {
        this.nodes.remove(parent);
    }

    protected void requestLayout() {
        Iterator iterator = this.nodes.iterator();
        while (iterator.hasNext()) {
            ((Parent)iterator.next()).requestLayout();
        }
    }
}

