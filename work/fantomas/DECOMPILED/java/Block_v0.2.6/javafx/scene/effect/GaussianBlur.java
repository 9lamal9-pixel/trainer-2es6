/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.effect;

import com.sun.javafx.effect.EffectDirtyBits;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.BoundsAccessor;
import com.sun.javafx.util.Utils;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.effect.Effect;

public class GaussianBlur
extends Effect {
    private ObjectProperty<Effect> input;
    private DoubleProperty radius;

    public GaussianBlur() {
    }

    public GaussianBlur(double d) {
        this.setRadius(d);
    }

    @Override
    com.sun.scenario.effect.GaussianBlur createPeer() {
        return new com.sun.scenario.effect.GaussianBlur();
    }

    public final void setInput(Effect effect) {
        this.inputProperty().set(effect);
    }

    public final Effect getInput() {
        return this.input == null ? null : (Effect)this.input.get();
    }

    public final ObjectProperty<Effect> inputProperty() {
        if (this.input == null) {
            this.input = new Effect.EffectInputProperty("input");
        }
        return this.input;
    }

    @Override
    boolean checkChainContains(Effect effect) {
        Effect effect2 = this.getInput();
        if (effect2 == null) {
            return false;
        }
        if (effect2 == effect) {
            return true;
        }
        return effect2.checkChainContains(effect);
    }

    public final void setRadius(double d) {
        this.radiusProperty().set(d);
    }

    public final double getRadius() {
        return this.radius == null ? 10.0 : this.radius.get();
    }

    public final DoubleProperty radiusProperty() {
        if (this.radius == null) {
            this.radius = new DoublePropertyBase(10.0){

                @Override
                public void invalidated() {
                    GaussianBlur.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                    GaussianBlur.this.effectBoundsChanged();
                }

                @Override
                public Object getBean() {
                    return GaussianBlur.this;
                }

                @Override
                public String getName() {
                    return "radius";
                }
            };
        }
        return this.radius;
    }

    private float getClampedRadius() {
        return (float)Utils.clamp(0.0, this.getRadius(), 63.0);
    }

    @Override
    void update() {
        Effect effect = this.getInput();
        if (effect != null) {
            effect.sync();
        }
        com.sun.scenario.effect.GaussianBlur gaussianBlur = (com.sun.scenario.effect.GaussianBlur)this.getPeer();
        gaussianBlur.setRadius(this.getClampedRadius());
        gaussianBlur.setInput(effect == null ? null : effect.getPeer());
    }

    @Override
    BaseBounds getBounds(BaseBounds baseBounds, BaseTransform baseTransform, Node node, BoundsAccessor boundsAccessor) {
        baseBounds = GaussianBlur.getInputBounds(baseBounds, BaseTransform.IDENTITY_TRANSFORM, node, boundsAccessor, this.getInput());
        float f = this.getClampedRadius();
        baseBounds = baseBounds.deriveWithPadding(f, f, 0.0f);
        return GaussianBlur.transformBounds(baseTransform, baseBounds);
    }

    @Override
    Effect copy() {
        return new GaussianBlur(this.getRadius());
    }
}

