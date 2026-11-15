/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.effect;

import com.sun.javafx.effect.EffectDirtyBits;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.BoundsAccessor;
import com.sun.javafx.tk.Toolkit;
import com.sun.javafx.util.Utils;
import com.sun.scenario.effect.GeneralShadow;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;

public class Shadow
extends Effect {
    private boolean changeIsLocal;
    private ObjectProperty<Effect> input;
    private DoubleProperty radius;
    private DoubleProperty width;
    private DoubleProperty height;
    private ObjectProperty<BlurType> blurType;
    private ObjectProperty<Color> color;

    public Shadow() {
    }

    public Shadow(double d, Color color) {
        this.setRadius(d);
        this.setColor(color);
    }

    public Shadow(BlurType blurType, Color color, double d) {
        this.setBlurType(blurType);
        this.setColor(color);
        this.setRadius(d);
    }

    @Override
    GeneralShadow createPeer() {
        return new GeneralShadow();
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
                    double d = Shadow.this.getRadius();
                    if (!Shadow.this.changeIsLocal) {
                        Shadow.this.changeIsLocal = true;
                        Shadow.this.updateRadius(d);
                        Shadow.this.changeIsLocal = false;
                        Shadow.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                        Shadow.this.effectBoundsChanged();
                    }
                }

                @Override
                public Object getBean() {
                    return Shadow.this;
                }

                @Override
                public String getName() {
                    return "radius";
                }
            };
        }
        return this.radius;
    }

    private void updateRadius(double d) {
        double d2 = d * 2.0 + 1.0;
        if (this.width != null && this.width.isBound()) {
            if (this.height == null || !this.height.isBound()) {
                this.setHeight(d2 * 2.0 - this.getWidth());
            }
        } else if (this.height != null && this.height.isBound()) {
            this.setWidth(d2 * 2.0 - this.getHeight());
        } else {
            this.setWidth(d2);
            this.setHeight(d2);
        }
    }

    public final void setWidth(double d) {
        this.widthProperty().set(d);
    }

    public final double getWidth() {
        return this.width == null ? 21.0 : this.width.get();
    }

    public final DoubleProperty widthProperty() {
        if (this.width == null) {
            this.width = new DoublePropertyBase(21.0){

                @Override
                public void invalidated() {
                    double d = Shadow.this.getWidth();
                    if (!Shadow.this.changeIsLocal) {
                        Shadow.this.changeIsLocal = true;
                        Shadow.this.updateWidth(d);
                        Shadow.this.changeIsLocal = false;
                        Shadow.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                        Shadow.this.effectBoundsChanged();
                    }
                }

                @Override
                public Object getBean() {
                    return Shadow.this;
                }

                @Override
                public String getName() {
                    return "width";
                }
            };
        }
        return this.width;
    }

    private void updateWidth(double d) {
        if (this.radius == null || !this.radius.isBound()) {
            double d2 = (d + this.getHeight()) / 2.0;
            if ((d2 = (d2 - 1.0) / 2.0) < 0.0) {
                d2 = 0.0;
            }
            this.setRadius(d2);
        } else if (this.height == null || !this.height.isBound()) {
            double d3 = this.getRadius() * 2.0 + 1.0;
            this.setHeight(d3 * 2.0 - d);
        }
    }

    public final void setHeight(double d) {
        this.heightProperty().set(d);
    }

    public final double getHeight() {
        return this.height == null ? 21.0 : this.height.get();
    }

    public final DoubleProperty heightProperty() {
        if (this.height == null) {
            this.height = new DoublePropertyBase(21.0){

                @Override
                public void invalidated() {
                    double d = Shadow.this.getHeight();
                    if (!Shadow.this.changeIsLocal) {
                        Shadow.this.changeIsLocal = true;
                        Shadow.this.updateHeight(d);
                        Shadow.this.changeIsLocal = false;
                        Shadow.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                        Shadow.this.effectBoundsChanged();
                    }
                }

                @Override
                public Object getBean() {
                    return Shadow.this;
                }

                @Override
                public String getName() {
                    return "height";
                }
            };
        }
        return this.height;
    }

    private void updateHeight(double d) {
        if (this.radius == null || !this.radius.isBound()) {
            double d2 = (this.getWidth() + d) / 2.0;
            if ((d2 = (d2 - 1.0) / 2.0) < 0.0) {
                d2 = 0.0;
            }
            this.setRadius(d2);
        } else if (this.width == null || !this.width.isBound()) {
            double d3 = this.getRadius() * 2.0 + 1.0;
            this.setWidth(d3 * 2.0 - d);
        }
    }

    public final void setBlurType(BlurType blurType) {
        this.blurTypeProperty().set(blurType);
    }

    public final BlurType getBlurType() {
        return this.blurType == null ? BlurType.THREE_PASS_BOX : (BlurType)((Object)this.blurType.get());
    }

    public final ObjectProperty<BlurType> blurTypeProperty() {
        if (this.blurType == null) {
            this.blurType = new ObjectPropertyBase<BlurType>(BlurType.THREE_PASS_BOX){

                @Override
                public void invalidated() {
                    Shadow.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                    Shadow.this.effectBoundsChanged();
                }

                @Override
                public Object getBean() {
                    return Shadow.this;
                }

                @Override
                public String getName() {
                    return "blurType";
                }
            };
        }
        return this.blurType;
    }

    public final void setColor(Color color) {
        this.colorProperty().set(color);
    }

    public final Color getColor() {
        return this.color == null ? Color.BLACK : (Color)this.color.get();
    }

    public final ObjectProperty<Color> colorProperty() {
        if (this.color == null) {
            this.color = new ObjectPropertyBase<Color>(Color.BLACK){

                @Override
                public void invalidated() {
                    Shadow.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                }

                @Override
                public Object getBean() {
                    return Shadow.this;
                }

                @Override
                public String getName() {
                    return "color";
                }
            };
        }
        return this.color;
    }

    private float getClampedWidth() {
        return (float)Utils.clamp(0.0, this.getWidth(), 255.0);
    }

    private float getClampedHeight() {
        return (float)Utils.clamp(0.0, this.getHeight(), 255.0);
    }

    private Color getColorInternal() {
        Color color = this.getColor();
        return color == null ? Color.BLACK : color;
    }

    private BlurType getBlurTypeInternal() {
        BlurType blurType = this.getBlurType();
        return blurType == null ? BlurType.THREE_PASS_BOX : blurType;
    }

    @Override
    void update() {
        Effect effect = this.getInput();
        if (effect != null) {
            effect.sync();
        }
        GeneralShadow generalShadow = (GeneralShadow)this.getPeer();
        generalShadow.setInput(effect == null ? null : effect.getPeer());
        generalShadow.setGaussianWidth(this.getClampedWidth());
        generalShadow.setGaussianHeight(this.getClampedHeight());
        generalShadow.setShadowMode(Toolkit.getToolkit().toShadowMode(this.getBlurTypeInternal()));
        generalShadow.setColor(Toolkit.getToolkit().toColor4f(this.getColorInternal()));
    }

    @Override
    BaseBounds getBounds(BaseBounds baseBounds, BaseTransform baseTransform, Node node, BoundsAccessor boundsAccessor) {
        baseBounds = Shadow.getInputBounds(baseBounds, BaseTransform.IDENTITY_TRANSFORM, node, boundsAccessor, this.getInput());
        return Shadow.getShadowBounds(baseBounds, baseTransform, this.getClampedWidth(), this.getClampedHeight(), this.getBlurTypeInternal());
    }

    @Override
    Effect copy() {
        Shadow shadow = new Shadow(this.getBlurType(), this.getColor(), this.getRadius());
        shadow.setInput(this.getInput());
        shadow.setHeight(this.getHeight());
        shadow.setWidth(this.getWidth());
        return shadow;
    }
}

