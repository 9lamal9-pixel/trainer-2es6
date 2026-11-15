/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.effect;

import com.sun.javafx.effect.EffectDirtyBits;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.RectBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.BoundsAccessor;
import com.sun.javafx.util.Utils;
import com.sun.scenario.effect.Blend;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.Node;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Effect;

public class Blend
extends Effect {
    private ObjectProperty<BlendMode> mode;
    private DoubleProperty opacity;
    private ObjectProperty<Effect> bottomInput;
    private ObjectProperty<Effect> topInput;

    private static Blend.Mode toPGMode(BlendMode blendMode) {
        if (blendMode == null) {
            return Blend.Mode.SRC_OVER;
        }
        if (blendMode == BlendMode.SRC_OVER) {
            return Blend.Mode.SRC_OVER;
        }
        if (blendMode == BlendMode.SRC_ATOP) {
            return Blend.Mode.SRC_ATOP;
        }
        if (blendMode == BlendMode.ADD) {
            return Blend.Mode.ADD;
        }
        if (blendMode == BlendMode.MULTIPLY) {
            return Blend.Mode.MULTIPLY;
        }
        if (blendMode == BlendMode.SCREEN) {
            return Blend.Mode.SCREEN;
        }
        if (blendMode == BlendMode.OVERLAY) {
            return Blend.Mode.OVERLAY;
        }
        if (blendMode == BlendMode.DARKEN) {
            return Blend.Mode.DARKEN;
        }
        if (blendMode == BlendMode.LIGHTEN) {
            return Blend.Mode.LIGHTEN;
        }
        if (blendMode == BlendMode.COLOR_DODGE) {
            return Blend.Mode.COLOR_DODGE;
        }
        if (blendMode == BlendMode.COLOR_BURN) {
            return Blend.Mode.COLOR_BURN;
        }
        if (blendMode == BlendMode.HARD_LIGHT) {
            return Blend.Mode.HARD_LIGHT;
        }
        if (blendMode == BlendMode.SOFT_LIGHT) {
            return Blend.Mode.SOFT_LIGHT;
        }
        if (blendMode == BlendMode.DIFFERENCE) {
            return Blend.Mode.DIFFERENCE;
        }
        if (blendMode == BlendMode.EXCLUSION) {
            return Blend.Mode.EXCLUSION;
        }
        if (blendMode == BlendMode.RED) {
            return Blend.Mode.RED;
        }
        if (blendMode == BlendMode.GREEN) {
            return Blend.Mode.GREEN;
        }
        if (blendMode == BlendMode.BLUE) {
            return Blend.Mode.BLUE;
        }
        throw new AssertionError((Object)"Unrecognized blend mode: {mode}");
    }

    static Blend.Mode getToolkitMode(BlendMode blendMode) {
        return Blend.toPGMode(blendMode);
    }

    public Blend() {
    }

    public Blend(BlendMode blendMode) {
        this.setMode(blendMode);
    }

    public Blend(BlendMode blendMode, Effect effect, Effect effect2) {
        this.setMode(blendMode);
        this.setBottomInput(effect);
        this.setTopInput(effect2);
    }

    @Override
    com.sun.scenario.effect.Blend createPeer() {
        return new com.sun.scenario.effect.Blend(Blend.toPGMode(BlendMode.SRC_OVER), com.sun.scenario.effect.Effect.DefaultInput, com.sun.scenario.effect.Effect.DefaultInput);
    }

    public final void setMode(BlendMode blendMode) {
        this.modeProperty().set(blendMode);
    }

    public final BlendMode getMode() {
        return this.mode == null ? BlendMode.SRC_OVER : (BlendMode)((Object)this.mode.get());
    }

    public final ObjectProperty<BlendMode> modeProperty() {
        if (this.mode == null) {
            this.mode = new ObjectPropertyBase<BlendMode>(BlendMode.SRC_OVER){

                @Override
                public void invalidated() {
                    Blend.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                }

                @Override
                public Object getBean() {
                    return Blend.this;
                }

                @Override
                public String getName() {
                    return "mode";
                }
            };
        }
        return this.mode;
    }

    public final void setOpacity(double d) {
        this.opacityProperty().set(d);
    }

    public final double getOpacity() {
        return this.opacity == null ? 1.0 : this.opacity.get();
    }

    public final DoubleProperty opacityProperty() {
        if (this.opacity == null) {
            this.opacity = new DoublePropertyBase(1.0){

                @Override
                public void invalidated() {
                    Blend.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                }

                @Override
                public Object getBean() {
                    return Blend.this;
                }

                @Override
                public String getName() {
                    return "opacity";
                }
            };
        }
        return this.opacity;
    }

    public final void setBottomInput(Effect effect) {
        this.bottomInputProperty().set(effect);
    }

    public final Effect getBottomInput() {
        return this.bottomInput == null ? null : (Effect)this.bottomInput.get();
    }

    public final ObjectProperty<Effect> bottomInputProperty() {
        if (this.bottomInput == null) {
            this.bottomInput = new Effect.EffectInputProperty(this, "bottomInput");
        }
        return this.bottomInput;
    }

    public final void setTopInput(Effect effect) {
        this.topInputProperty().set(effect);
    }

    public final Effect getTopInput() {
        return this.topInput == null ? null : (Effect)this.topInput.get();
    }

    public final ObjectProperty<Effect> topInputProperty() {
        if (this.topInput == null) {
            this.topInput = new Effect.EffectInputProperty(this, "topInput");
        }
        return this.topInput;
    }

    @Override
    boolean checkChainContains(Effect effect) {
        Effect effect2 = this.getTopInput();
        Effect effect3 = this.getBottomInput();
        if (effect2 == effect || effect3 == effect) {
            return true;
        }
        if (effect2 != null && effect2.checkChainContains(effect)) {
            return true;
        }
        return effect3 != null && effect3.checkChainContains(effect);
    }

    @Override
    void update() {
        Effect effect = this.getBottomInput();
        Effect effect2 = this.getTopInput();
        if (effect2 != null) {
            effect2.sync();
        }
        if (effect != null) {
            effect.sync();
        }
        com.sun.scenario.effect.Blend blend = (com.sun.scenario.effect.Blend)this.getPeer();
        blend.setTopInput(effect2 == null ? null : effect2.getPeer());
        blend.setBottomInput(effect == null ? null : effect.getPeer());
        blend.setOpacity((float)Utils.clamp(0.0, this.getOpacity(), 1.0));
        blend.setMode(Blend.toPGMode(this.getMode()));
    }

    @Override
    BaseBounds getBounds(BaseBounds baseBounds, BaseTransform baseTransform, Node node, BoundsAccessor boundsAccessor) {
        BaseBounds baseBounds2 = new RectBounds();
        BaseBounds baseBounds3 = new RectBounds();
        baseBounds3 = Blend.getInputBounds(baseBounds3, baseTransform, node, boundsAccessor, this.getBottomInput());
        baseBounds2 = Blend.getInputBounds(baseBounds2, baseTransform, node, boundsAccessor, this.getTopInput());
        BaseBounds baseBounds4 = baseBounds2.deriveWithUnion(baseBounds3);
        return baseBounds4;
    }

    @Override
    Effect copy() {
        return new Blend(this.getMode(), this.getBottomInput(), this.getTopInput());
    }
}

