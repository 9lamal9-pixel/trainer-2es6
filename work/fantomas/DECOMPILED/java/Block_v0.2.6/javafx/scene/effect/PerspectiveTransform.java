/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.effect;

import com.sun.javafx.effect.EffectDirtyBits;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.RectBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.BoundsAccessor;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import javafx.scene.effect.Effect;

public class PerspectiveTransform
extends Effect {
    private ObjectProperty<Effect> input;
    private DoubleProperty ulx;
    private DoubleProperty uly;
    private DoubleProperty urx;
    private DoubleProperty ury;
    private DoubleProperty lrx;
    private DoubleProperty lry;
    private DoubleProperty llx;
    private DoubleProperty lly;
    private float[] devcoords = new float[8];

    public PerspectiveTransform() {
    }

    public PerspectiveTransform(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8) {
        this.setUlx(d);
        this.setUly(d2);
        this.setUrx(d3);
        this.setUry(d4);
        this.setLlx(d7);
        this.setLly(d8);
        this.setLrx(d5);
        this.setLry(d6);
    }

    private void updateXform() {
        ((com.sun.scenario.effect.PerspectiveTransform)this.getPeer()).setQuadMapping((float)this.getUlx(), (float)this.getUly(), (float)this.getUrx(), (float)this.getUry(), (float)this.getLrx(), (float)this.getLry(), (float)this.getLlx(), (float)this.getLly());
    }

    @Override
    com.sun.scenario.effect.PerspectiveTransform createPeer() {
        return new com.sun.scenario.effect.PerspectiveTransform();
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

    public final void setUlx(double d) {
        this.ulxProperty().set(d);
    }

    public final double getUlx() {
        return this.ulx == null ? 0.0 : this.ulx.get();
    }

    public final DoubleProperty ulxProperty() {
        if (this.ulx == null) {
            this.ulx = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    PerspectiveTransform.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                    PerspectiveTransform.this.effectBoundsChanged();
                }

                @Override
                public Object getBean() {
                    return PerspectiveTransform.this;
                }

                @Override
                public String getName() {
                    return "ulx";
                }
            };
        }
        return this.ulx;
    }

    public final void setUly(double d) {
        this.ulyProperty().set(d);
    }

    public final double getUly() {
        return this.uly == null ? 0.0 : this.uly.get();
    }

    public final DoubleProperty ulyProperty() {
        if (this.uly == null) {
            this.uly = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    PerspectiveTransform.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                    PerspectiveTransform.this.effectBoundsChanged();
                }

                @Override
                public Object getBean() {
                    return PerspectiveTransform.this;
                }

                @Override
                public String getName() {
                    return "uly";
                }
            };
        }
        return this.uly;
    }

    public final void setUrx(double d) {
        this.urxProperty().set(d);
    }

    public final double getUrx() {
        return this.urx == null ? 0.0 : this.urx.get();
    }

    public final DoubleProperty urxProperty() {
        if (this.urx == null) {
            this.urx = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    PerspectiveTransform.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                    PerspectiveTransform.this.effectBoundsChanged();
                }

                @Override
                public Object getBean() {
                    return PerspectiveTransform.this;
                }

                @Override
                public String getName() {
                    return "urx";
                }
            };
        }
        return this.urx;
    }

    public final void setUry(double d) {
        this.uryProperty().set(d);
    }

    public final double getUry() {
        return this.ury == null ? 0.0 : this.ury.get();
    }

    public final DoubleProperty uryProperty() {
        if (this.ury == null) {
            this.ury = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    PerspectiveTransform.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                    PerspectiveTransform.this.effectBoundsChanged();
                }

                @Override
                public Object getBean() {
                    return PerspectiveTransform.this;
                }

                @Override
                public String getName() {
                    return "ury";
                }
            };
        }
        return this.ury;
    }

    public final void setLrx(double d) {
        this.lrxProperty().set(d);
    }

    public final double getLrx() {
        return this.lrx == null ? 0.0 : this.lrx.get();
    }

    public final DoubleProperty lrxProperty() {
        if (this.lrx == null) {
            this.lrx = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    PerspectiveTransform.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                    PerspectiveTransform.this.effectBoundsChanged();
                }

                @Override
                public Object getBean() {
                    return PerspectiveTransform.this;
                }

                @Override
                public String getName() {
                    return "lrx";
                }
            };
        }
        return this.lrx;
    }

    public final void setLry(double d) {
        this.lryProperty().set(d);
    }

    public final double getLry() {
        return this.lry == null ? 0.0 : this.lry.get();
    }

    public final DoubleProperty lryProperty() {
        if (this.lry == null) {
            this.lry = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    PerspectiveTransform.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                    PerspectiveTransform.this.effectBoundsChanged();
                }

                @Override
                public Object getBean() {
                    return PerspectiveTransform.this;
                }

                @Override
                public String getName() {
                    return "lry";
                }
            };
        }
        return this.lry;
    }

    public final void setLlx(double d) {
        this.llxProperty().set(d);
    }

    public final double getLlx() {
        return this.llx == null ? 0.0 : this.llx.get();
    }

    public final DoubleProperty llxProperty() {
        if (this.llx == null) {
            this.llx = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    PerspectiveTransform.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                    PerspectiveTransform.this.effectBoundsChanged();
                }

                @Override
                public Object getBean() {
                    return PerspectiveTransform.this;
                }

                @Override
                public String getName() {
                    return "llx";
                }
            };
        }
        return this.llx;
    }

    public final void setLly(double d) {
        this.llyProperty().set(d);
    }

    public final double getLly() {
        return this.lly == null ? 0.0 : this.lly.get();
    }

    public final DoubleProperty llyProperty() {
        if (this.lly == null) {
            this.lly = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    PerspectiveTransform.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                    PerspectiveTransform.this.effectBoundsChanged();
                }

                @Override
                public Object getBean() {
                    return PerspectiveTransform.this;
                }

                @Override
                public String getName() {
                    return "lly";
                }
            };
        }
        return this.lly;
    }

    @Override
    void update() {
        Effect effect = this.getInput();
        if (effect != null) {
            effect.sync();
        }
        ((com.sun.scenario.effect.PerspectiveTransform)this.getPeer()).setInput(effect == null ? null : effect.getPeer());
        this.updateXform();
    }

    @Override
    BaseBounds getBounds(BaseBounds baseBounds, BaseTransform baseTransform, Node node, BoundsAccessor boundsAccessor) {
        float f;
        float f2;
        this.setupDevCoords(baseTransform);
        float f3 = f2 = this.devcoords[0];
        float f4 = f = this.devcoords[1];
        for (int i = 2; i < this.devcoords.length; i += 2) {
            if (f3 > this.devcoords[i]) {
                f3 = this.devcoords[i];
            } else if (f2 < this.devcoords[i]) {
                f2 = this.devcoords[i];
            }
            if (f4 > this.devcoords[i + 1]) {
                f4 = this.devcoords[i + 1];
                continue;
            }
            if (!(f < this.devcoords[i + 1])) continue;
            f = this.devcoords[i + 1];
        }
        return new RectBounds(f3, f4, f2, f);
    }

    private void setupDevCoords(BaseTransform baseTransform) {
        this.devcoords[0] = (float)this.getUlx();
        this.devcoords[1] = (float)this.getUly();
        this.devcoords[2] = (float)this.getUrx();
        this.devcoords[3] = (float)this.getUry();
        this.devcoords[4] = (float)this.getLrx();
        this.devcoords[5] = (float)this.getLry();
        this.devcoords[6] = (float)this.getLlx();
        this.devcoords[7] = (float)this.getLly();
        baseTransform.transform(this.devcoords, 0, this.devcoords, 0, 4);
    }

    @Override
    Effect copy() {
        return new PerspectiveTransform(this.getUlx(), this.getUly(), this.getUrx(), this.getUry(), this.getLrx(), this.getLry(), this.getLlx(), this.getLly());
    }
}

