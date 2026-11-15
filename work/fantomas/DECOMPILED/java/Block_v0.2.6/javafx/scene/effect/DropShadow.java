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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;

public class DropShadow
extends Effect {
    private boolean changeIsLocal;
    private ObjectProperty<Effect> input;
    private DoubleProperty radius;
    private DoubleProperty width;
    private DoubleProperty height;
    private ObjectProperty<BlurType> blurType;
    private DoubleProperty spread;
    private ObjectProperty<Color> color;
    private DoubleProperty offsetX;
    private DoubleProperty offsetY;

    public DropShadow() {
    }

    public DropShadow(double d, Color color) {
        this.setRadius(d);
        this.setColor(color);
    }

    public DropShadow(double d, double d2, double d3, Color color) {
        this.setRadius(d);
        this.setOffsetX(d2);
        this.setOffsetY(d3);
        this.setColor(color);
    }

    public DropShadow(BlurType blurType, Color color, double d, double d2, double d3, double d4) {
        this.setBlurType(blurType);
        this.setColor(color);
        this.setRadius(d);
        this.setSpread(d2);
        this.setOffsetX(d3);
        this.setOffsetY(d4);
    }

    @Override
    com.sun.scenario.effect.DropShadow createPeer() {
        return new com.sun.scenario.effect.DropShadow();
    }

    public final void setInput(Effect effect) {
        this.inputProperty().set(effect);
    }

    public final Effect getInput() {
        return this.input == null ? null : (Effect)this.input.get();
    }

    public final ObjectProperty<Effect> inputProperty() {
        if (this.input == null) {
            this.input = new Effect.EffectInputProperty(this, "input");
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
                    double d = DropShadow.this.getRadius();
                    if (!DropShadow.this.changeIsLocal) {
                        DropShadow.this.changeIsLocal = true;
                        DropShadow.this.updateRadius(d);
                        DropShadow.this.changeIsLocal = false;
                        DropShadow.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                        DropShadow.this.effectBoundsChanged();
                    }
                }

                @Override
                public Object getBean() {
                    return DropShadow.this;
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
                    double d = DropShadow.this.getWidth();
                    if (!DropShadow.this.changeIsLocal) {
                        DropShadow.this.changeIsLocal = true;
                        DropShadow.this.updateWidth(d);
                        DropShadow.this.changeIsLocal = false;
                        DropShadow.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                        DropShadow.this.effectBoundsChanged();
                    }
                }

                @Override
                public Object getBean() {
                    return DropShadow.this;
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
                    double d = DropShadow.this.getHeight();
                    if (!DropShadow.this.changeIsLocal) {
                        DropShadow.this.changeIsLocal = true;
                        DropShadow.this.updateHeight(d);
                        DropShadow.this.changeIsLocal = false;
                        DropShadow.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                        DropShadow.this.effectBoundsChanged();
                    }
                }

                @Override
                public Object getBean() {
                    return DropShadow.this;
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
                    DropShadow.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                    DropShadow.this.effectBoundsChanged();
                }

                @Override
                public Object getBean() {
                    return DropShadow.this;
                }

                @Override
                public String getName() {
                    return "blurType";
                }
            };
        }
        return this.blurType;
    }

    public final void setSpread(double d) {
        this.spreadProperty().set(d);
    }

    public final double getSpread() {
        return this.spread == null ? 0.0 : this.spread.get();
    }

    public final DoubleProperty spreadProperty() {
        if (this.spread == null) {
            this.spread = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    DropShadow.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                }

                @Override
                public Object getBean() {
                    return DropShadow.this;
                }

                @Override
                public String getName() {
                    return "spread";
                }
            };
        }
        return this.spread;
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
                    DropShadow.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                }

                @Override
                public Object getBean() {
                    return DropShadow.this;
                }

                @Override
                public String getName() {
                    return "color";
                }
            };
        }
        return this.color;
    }

    public final void setOffsetX(double d) {
        this.offsetXProperty().set(d);
    }

    public final double getOffsetX() {
        return this.offsetX == null ? 0.0 : this.offsetX.get();
    }

    public final DoubleProperty offsetXProperty() {
        if (this.offsetX == null) {
            this.offsetX = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    DropShadow.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                    DropShadow.this.effectBoundsChanged();
                }

                @Override
                public Object getBean() {
                    return DropShadow.this;
                }

                @Override
                public String getName() {
                    return "offsetX";
                }
            };
        }
        return this.offsetX;
    }

    public final void setOffsetY(double d) {
        this.offsetYProperty().set(d);
    }

    public final double getOffsetY() {
        return this.offsetY == null ? 0.0 : this.offsetY.get();
    }

    public final DoubleProperty offsetYProperty() {
        if (this.offsetY == null) {
            this.offsetY = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    DropShadow.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                    DropShadow.this.effectBoundsChanged();
                }

                @Override
                public Object getBean() {
                    return DropShadow.this;
                }

                @Override
                public String getName() {
                    return "offsetY";
                }
            };
        }
        return this.offsetY;
    }

    private float getClampedWidth() {
        return (float)Utils.clamp(0.0, this.getWidth(), 255.0);
    }

    private float getClampedHeight() {
        return (float)Utils.clamp(0.0, this.getHeight(), 255.0);
    }

    private float getClampedSpread() {
        return (float)Utils.clamp(0.0, this.getSpread(), 1.0);
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
        com.sun.scenario.effect.DropShadow dropShadow = (com.sun.scenario.effect.DropShadow)this.getPeer();
        dropShadow.setShadowSourceInput(effect == null ? null : effect.getPeer());
        dropShadow.setContentInput(effect == null ? null : effect.getPeer());
        dropShadow.setGaussianWidth(this.getClampedWidth());
        dropShadow.setGaussianHeight(this.getClampedHeight());
        dropShadow.setSpread(this.getClampedSpread());
        dropShadow.setShadowMode(Toolkit.getToolkit().toShadowMode(this.getBlurTypeInternal()));
        dropShadow.setColor(Toolkit.getToolkit().toColor4f(this.getColorInternal()));
        dropShadow.setOffsetX((int)this.getOffsetX());
        dropShadow.setOffsetY((int)this.getOffsetY());
    }

    @Override
    BaseBounds getBounds(BaseBounds baseBounds, BaseTransform baseTransform, Node node, BoundsAccessor boundsAccessor) {
        baseBounds = DropShadow.getInputBounds(baseBounds, BaseTransform.IDENTITY_TRANSFORM, node, boundsAccessor, this.getInput());
        int n = (int)this.getOffsetX();
        int n2 = (int)this.getOffsetY();
        BaseBounds baseBounds2 = BaseBounds.getInstance(baseBounds.getMinX() + (float)n, baseBounds.getMinY() + (float)n2, baseBounds.getMinZ(), baseBounds.getMaxX() + (float)n, baseBounds.getMaxY() + (float)n2, baseBounds.getMaxZ());
        baseBounds2 = DropShadow.getShadowBounds(baseBounds2, baseTransform, this.getClampedWidth(), this.getClampedHeight(), this.getBlurTypeInternal());
        BaseBounds baseBounds3 = DropShadow.transformBounds(baseTransform, baseBounds);
        BaseBounds baseBounds4 = baseBounds3.deriveWithUnion(baseBounds2);
        return baseBounds4;
    }

    @Override
    Effect copy() {
        DropShadow dropShadow = new DropShadow(this.getBlurType(), this.getColor(), this.getRadius(), this.getSpread(), this.getOffsetX(), this.getOffsetY());
        dropShadow.setInput(this.getInput());
        dropShadow.setWidth(this.getWidth());
        dropShadow.setHeight(this.getHeight());
        return dropShadow;
    }
}

