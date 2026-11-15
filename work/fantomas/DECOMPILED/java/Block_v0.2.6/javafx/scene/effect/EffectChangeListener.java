/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.effect;

import com.sun.javafx.beans.event.AbstractNotifyListener;
import javafx.beans.value.ObservableValue;

abstract class EffectChangeListener
extends AbstractNotifyListener {
    protected ObservableValue registredOn;

    EffectChangeListener() {
    }

    public void register(ObservableValue observableValue) {
        if (this.registredOn == observableValue) {
            return;
        }
        if (this.registredOn != null) {
            this.registredOn.removeListener(this.getWeakListener());
        }
        this.registredOn = observableValue;
        if (this.registredOn != null) {
            this.registredOn.addListener(this.getWeakListener());
        }
    }
}

