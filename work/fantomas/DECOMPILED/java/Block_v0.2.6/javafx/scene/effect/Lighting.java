/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.effect;

import com.sun.javafx.effect.EffectDirtyBits;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.BoundsAccessor;
import com.sun.javafx.util.Utils;
import com.sun.scenario.effect.PhongLighting;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.Node;
import javafx.scene.effect.Effect;
import javafx.scene.effect.EffectChangeListener;
import javafx.scene.effect.Light;
import javafx.scene.effect.Shadow;

public class Lighting
extends Effect {
    private final Light defaultLight = new Light.Distant();
    private ObjectProperty<Light> light = new ObjectPropertyBase<Light>((Light)new Light.Distant()){

        @Override
        public void invalidated() {
            Lighting.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
            Lighting.this.effectBoundsChanged();
        }

        @Override
        public Object getBean() {
            return Lighting.this;
        }

        @Override
        public String getName() {
            return "light";
        }
    };
    private final LightChangeListener lightChangeListener = new LightChangeListener();
    private ObjectProperty<Effect> bumpInput;
    private ObjectProperty<Effect> contentInput;
    private DoubleProperty diffuseConstant;
    private DoubleProperty specularConstant;
    private DoubleProperty specularExponent;
    private DoubleProperty surfaceScale;

    @Override
    PhongLighting createPeer() {
        return new PhongLighting(this.getLightInternal().getPeer());
    }

    public Lighting() {
        Shadow shadow = new Shadow();
        shadow.setRadius(10.0);
        this.setBumpInput(shadow);
    }

    public Lighting(Light light) {
        Shadow shadow = new Shadow();
        shadow.setRadius(10.0);
        this.setBumpInput(shadow);
        this.setLight(light);
    }

    public final void setLight(Light light) {
        this.lightProperty().set(light);
    }

    public final Light getLight() {
        return (Light)this.light.get();
    }

    public final ObjectProperty<Light> lightProperty() {
        return this.light;
    }

    @Override
    Effect copy() {
        Lighting lighting = new Lighting(this.getLight());
        lighting.setBumpInput(this.getBumpInput());
        lighting.setContentInput(this.getContentInput());
        lighting.setDiffuseConstant(this.getDiffuseConstant());
        lighting.setSpecularConstant(this.getSpecularConstant());
        lighting.setSpecularExponent(this.getSpecularExponent());
        lighting.setSurfaceScale(this.getSurfaceScale());
        return lighting;
    }

    public final void setBumpInput(Effect effect) {
        this.bumpInputProperty().set(effect);
    }

    public final Effect getBumpInput() {
        return this.bumpInput == null ? null : (Effect)this.bumpInput.get();
    }

    public final ObjectProperty<Effect> bumpInputProperty() {
        if (this.bumpInput == null) {
            this.bumpInput = new Effect.EffectInputProperty("bumpInput");
        }
        return this.bumpInput;
    }

    public final void setContentInput(Effect effect) {
        this.contentInputProperty().set(effect);
    }

    public final Effect getContentInput() {
        return this.contentInput == null ? null : (Effect)this.contentInput.get();
    }

    public final ObjectProperty<Effect> contentInputProperty() {
        if (this.contentInput == null) {
            this.contentInput = new Effect.EffectInputProperty("contentInput");
        }
        return this.contentInput;
    }

    @Override
    boolean checkChainContains(Effect effect) {
        Effect effect2 = this.getBumpInput();
        Effect effect3 = this.getContentInput();
        if (effect3 == effect || effect2 == effect) {
            return true;
        }
        if (effect3 != null && effect3.checkChainContains(effect)) {
            return true;
        }
        return effect2 != null && effect2.checkChainContains(effect);
    }

    public final void setDiffuseConstant(double d) {
        this.diffuseConstantProperty().set(d);
    }

    public final double getDiffuseConstant() {
        return this.diffuseConstant == null ? 1.0 : this.diffuseConstant.get();
    }

    public final DoubleProperty diffuseConstantProperty() {
        if (this.diffuseConstant == null) {
            this.diffuseConstant = new DoublePropertyBase(1.0){

                @Override
                public void invalidated() {
                    Lighting.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                }

                @Override
                public Object getBean() {
                    return Lighting.this;
                }

                @Override
                public String getName() {
                    return "diffuseConstant";
                }
            };
        }
        return this.diffuseConstant;
    }

    public final void setSpecularConstant(double d) {
        this.specularConstantProperty().set(d);
    }

    public final double getSpecularConstant() {
        return this.specularConstant == null ? 0.3 : this.specularConstant.get();
    }

    public final DoubleProperty specularConstantProperty() {
        if (this.specularConstant == null) {
            this.specularConstant = new DoublePropertyBase(0.3){

                @Override
                public void invalidated() {
                    Lighting.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                }

                @Override
                public Object getBean() {
                    return Lighting.this;
                }

                @Override
                public String getName() {
                    return "specularConstant";
                }
            };
        }
        return this.specularConstant;
    }

    public final void setSpecularExponent(double d) {
        this.specularExponentProperty().set(d);
    }

    public final double getSpecularExponent() {
        return this.specularExponent == null ? 20.0 : this.specularExponent.get();
    }

    public final DoubleProperty specularExponentProperty() {
        if (this.specularExponent == null) {
            this.specularExponent = new DoublePropertyBase(20.0){

                @Override
                public void invalidated() {
                    Lighting.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                }

                @Override
                public Object getBean() {
                    return Lighting.this;
                }

                @Override
                public String getName() {
                    return "specularExponent";
                }
            };
        }
        return this.specularExponent;
    }

    public final void setSurfaceScale(double d) {
        this.surfaceScaleProperty().set(d);
    }

    public final double getSurfaceScale() {
        return this.surfaceScale == null ? 1.5 : this.surfaceScale.get();
    }

    public final DoubleProperty surfaceScaleProperty() {
        if (this.surfaceScale == null) {
            this.surfaceScale = new DoublePropertyBase(1.5){

                @Override
                public void invalidated() {
                    Lighting.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                }

                @Override
                public Object getBean() {
                    return Lighting.this;
                }

                @Override
                public String getName() {
                    return "surfaceScale";
                }
            };
        }
        return this.surfaceScale;
    }

    private Light getLightInternal() {
        Light light = this.getLight();
        return light == null ? this.defaultLight : light;
    }

    @Override
    void update() {
        Effect effect;
        Effect effect2 = this.getBumpInput();
        if (effect2 != null) {
            effect2.sync();
        }
        if ((effect = this.getContentInput()) != null) {
            effect.sync();
        }
        PhongLighting phongLighting = (PhongLighting)this.getPeer();
        phongLighting.setBumpInput(effect2 == null ? null : effect2.getPeer());
        phongLighting.setContentInput(effect == null ? null : effect.getPeer());
        phongLighting.setDiffuseConstant((float)Utils.clamp(0.0, this.getDiffuseConstant(), 2.0));
        phongLighting.setSpecularConstant((float)Utils.clamp(0.0, this.getSpecularConstant(), 2.0));
        phongLighting.setSpecularExponent((float)Utils.clamp(0.0, this.getSpecularExponent(), 40.0));
        phongLighting.setSurfaceScale((float)Utils.clamp(0.0, this.getSurfaceScale(), 10.0));
        this.lightChangeListener.register(this.getLight());
        this.getLightInternal().sync();
        phongLighting.setLight(this.getLightInternal().getPeer());
    }

    @Override
    BaseBounds getBounds(BaseBounds baseBounds, BaseTransform baseTransform, Node node, BoundsAccessor boundsAccessor) {
        return Lighting.getInputBounds(baseBounds, baseTransform, node, boundsAccessor, this.getContentInput());
    }

    private class LightChangeListener
    extends EffectChangeListener {
        Light light;

        private LightChangeListener() {
        }

        public void register(Light light) {
            this.light = light;
            super.register(this.light == null ? null : this.light.effectDirtyProperty());
        }

        @Override
        public void invalidated(Observable observable) {
            if (this.light.isEffectDirty()) {
                Lighting.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                Lighting.this.effectBoundsChanged();
            }
        }
    }
}

