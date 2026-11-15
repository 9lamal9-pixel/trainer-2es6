/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.effect;

import com.sun.javafx.effect.EffectDirtyBits;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.RectBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.BoundsAccessor;
import com.sun.scenario.effect.Blend;
import com.sun.scenario.effect.EffectHelper;
import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.Node;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.EffectChangeListener;

public abstract class Effect {
    private com.sun.scenario.effect.Effect peer;
    private IntegerProperty effectDirty = new SimpleIntegerProperty(this, "effectDirty");

    protected Effect() {
        this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
    }

    void effectBoundsChanged() {
        this.toggleDirty(EffectDirtyBits.BOUNDS_CHANGED);
    }

    abstract com.sun.scenario.effect.Effect createPeer();

    com.sun.scenario.effect.Effect getPeer() {
        if (this.peer == null) {
            this.peer = this.createPeer();
        }
        return this.peer;
    }

    private void setEffectDirty(int n) {
        this.effectDirtyProperty().set(n);
    }

    private final IntegerProperty effectDirtyProperty() {
        return this.effectDirty;
    }

    private final boolean isEffectDirty() {
        return this.isEffectDirty(EffectDirtyBits.EFFECT_DIRTY);
    }

    final void markDirty(EffectDirtyBits effectDirtyBits) {
        this.setEffectDirty(this.effectDirty.get() | effectDirtyBits.getMask());
    }

    private void toggleDirty(EffectDirtyBits effectDirtyBits) {
        this.setEffectDirty(this.effectDirty.get() ^ effectDirtyBits.getMask());
    }

    private boolean isEffectDirty(EffectDirtyBits effectDirtyBits) {
        return (this.effectDirty.get() & effectDirtyBits.getMask()) != 0;
    }

    private void clearEffectDirty(EffectDirtyBits effectDirtyBits) {
        this.setEffectDirty(this.effectDirty.get() & ~effectDirtyBits.getMask());
    }

    final void sync() {
        if (this.isEffectDirty(EffectDirtyBits.EFFECT_DIRTY)) {
            this.update();
            this.clearEffectDirty(EffectDirtyBits.EFFECT_DIRTY);
        }
    }

    abstract void update();

    abstract boolean checkChainContains(Effect var1);

    boolean containsCycles(Effect effect) {
        return effect != null && (effect == this || effect.checkChainContains(this));
    }

    abstract BaseBounds getBounds(BaseBounds var1, BaseTransform var2, Node var3, BoundsAccessor var4);

    abstract Effect copy();

    static BaseBounds transformBounds(BaseTransform baseTransform, BaseBounds baseBounds) {
        if (baseTransform == null || baseTransform.isIdentity()) {
            return baseBounds;
        }
        BaseBounds baseBounds2 = new RectBounds();
        baseBounds2 = baseTransform.transform(baseBounds, baseBounds2);
        return baseBounds2;
    }

    static int getKernelSize(float f, int n) {
        int n2 = (int)Math.ceil(f);
        if (n2 < 1) {
            n2 = 1;
        }
        n2 = (n2 - 1) * n + 1;
        return (n2 |= 1) / 2;
    }

    static BaseBounds getShadowBounds(BaseBounds baseBounds, BaseTransform baseTransform, float f, float f2, BlurType blurType) {
        int n = 0;
        int n2 = 0;
        switch (blurType) {
            case GAUSSIAN: {
                float f3 = f < 1.0f ? 0.0f : (f - 1.0f) / 2.0f;
                float f4 = f2 < 1.0f ? 0.0f : (f2 - 1.0f) / 2.0f;
                n = (int)Math.ceil(f3);
                n2 = (int)Math.ceil(f4);
                break;
            }
            case ONE_PASS_BOX: {
                n = Effect.getKernelSize(Math.round(f / 3.0f), 1);
                n2 = Effect.getKernelSize(Math.round(f2 / 3.0f), 1);
                break;
            }
            case TWO_PASS_BOX: {
                n = Effect.getKernelSize(Math.round(f / 3.0f), 2);
                n2 = Effect.getKernelSize(Math.round(f2 / 3.0f), 2);
                break;
            }
            case THREE_PASS_BOX: {
                n = Effect.getKernelSize(Math.round(f / 3.0f), 3);
                n2 = Effect.getKernelSize(Math.round(f2 / 3.0f), 3);
            }
        }
        baseBounds = baseBounds.deriveWithPadding(n, n2, 0.0f);
        return Effect.transformBounds(baseTransform, baseBounds);
    }

    static BaseBounds getInputBounds(BaseBounds baseBounds, BaseTransform baseTransform, Node node, BoundsAccessor boundsAccessor, Effect effect) {
        baseBounds = effect != null ? effect.getBounds(baseBounds, baseTransform, node, boundsAccessor) : boundsAccessor.getGeomBounds(baseBounds, baseTransform, node);
        return baseBounds;
    }

    static {
        EffectHelper.setEffectAccessor(new EffectHelper.EffectAccessor(){

            @Override
            public com.sun.scenario.effect.Effect getPeer(Effect effect) {
                return effect.getPeer();
            }

            @Override
            public void sync(Effect effect) {
                effect.sync();
            }

            @Override
            public IntegerProperty effectDirtyProperty(Effect effect) {
                return effect.effectDirtyProperty();
            }

            @Override
            public boolean isEffectDirty(Effect effect) {
                return effect.isEffectDirty();
            }

            @Override
            public BaseBounds getBounds(Effect effect, BaseBounds baseBounds, BaseTransform baseTransform, Node node, BoundsAccessor boundsAccessor) {
                return effect.getBounds(baseBounds, baseTransform, node, boundsAccessor);
            }

            @Override
            public Effect copy(Effect effect) {
                return effect.copy();
            }

            @Override
            public Blend.Mode getToolkitBlendMode(BlendMode blendMode) {
                return Blend.getToolkitMode(blendMode);
            }
        });
    }

    class EffectInputProperty
    extends ObjectPropertyBase<Effect> {
        private final String propertyName;
        private Effect validInput = null;
        private final EffectInputChangeListener effectChangeListener = new EffectInputChangeListener();

        public EffectInputProperty(String string) {
            this.propertyName = string;
        }

        @Override
        public void invalidated() {
            Effect effect = (Effect)super.get();
            if (Effect.this.containsCycles(effect)) {
                if (this.isBound()) {
                    this.unbind();
                    this.set(this.validInput);
                    throw new IllegalArgumentException("Cycle in effect chain detected, binding was set to incorrect value, unbinding the input property");
                }
                this.set(this.validInput);
                throw new IllegalArgumentException("Cycle in effect chain detected");
            }
            this.validInput = effect;
            this.effectChangeListener.register(effect);
            Effect.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
            Effect.this.effectBoundsChanged();
        }

        @Override
        public Object getBean() {
            return Effect.this;
        }

        @Override
        public String getName() {
            return this.propertyName;
        }
    }

    class EffectInputChangeListener
    extends EffectChangeListener {
        private int oldBits;

        EffectInputChangeListener() {
        }

        public void register(Effect effect) {
            super.register(effect == null ? null : effect.effectDirtyProperty());
            if (effect != null) {
                this.oldBits = effect.effectDirtyProperty().get();
            }
        }

        @Override
        public void invalidated(Observable observable) {
            int n = ((IntegerProperty)observable).get();
            int n2 = n ^ this.oldBits;
            this.oldBits = n;
            if (EffectDirtyBits.isSet(n2, EffectDirtyBits.EFFECT_DIRTY) && EffectDirtyBits.isSet(n, EffectDirtyBits.EFFECT_DIRTY)) {
                Effect.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
            }
            if (EffectDirtyBits.isSet(n2, EffectDirtyBits.BOUNDS_CHANGED)) {
                Effect.this.toggleDirty(EffectDirtyBits.BOUNDS_CHANGED);
            }
        }
    }
}

