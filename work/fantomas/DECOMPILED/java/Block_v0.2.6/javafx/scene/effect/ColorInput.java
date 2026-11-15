/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.effect;

import com.sun.javafx.effect.EffectDirtyBits;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.RectBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.BoundsAccessor;
import com.sun.javafx.tk.Toolkit;
import com.sun.scenario.effect.Flood;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.Node;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class ColorInput
extends Effect {
    private ObjectProperty<Paint> paint;
    private DoubleProperty x;
    private DoubleProperty y;
    private DoubleProperty width;
    private DoubleProperty height;

    public ColorInput() {
    }

    public ColorInput(double d, double d2, double d3, double d4, Paint paint) {
        this.setX(d);
        this.setY(d2);
        this.setWidth(d3);
        this.setHeight(d4);
        this.setPaint(paint);
    }

    @Override
    Flood createPeer() {
        return new Flood(Toolkit.getPaintAccessor().getPlatformPaint(Color.RED));
    }

    public final void setPaint(Paint paint) {
        this.paintProperty().set(paint);
    }

    public final Paint getPaint() {
        return this.paint == null ? Color.RED : (Paint)this.paint.get();
    }

    public final ObjectProperty<Paint> paintProperty() {
        if (this.paint == null) {
            this.paint = new ObjectPropertyBase<Paint>((Paint)Color.RED){

                @Override
                public void invalidated() {
                    ColorInput.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                }

                @Override
                public Object getBean() {
                    return ColorInput.this;
                }

                @Override
                public String getName() {
                    return "paint";
                }
            };
        }
        return this.paint;
    }

    public final void setX(double d) {
        this.xProperty().set(d);
    }

    public final double getX() {
        return this.x == null ? 0.0 : this.x.get();
    }

    public final DoubleProperty xProperty() {
        if (this.x == null) {
            this.x = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    ColorInput.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                    ColorInput.this.effectBoundsChanged();
                }

                @Override
                public Object getBean() {
                    return ColorInput.this;
                }

                @Override
                public String getName() {
                    return "x";
                }
            };
        }
        return this.x;
    }

    public final void setY(double d) {
        this.yProperty().set(d);
    }

    public final double getY() {
        return this.y == null ? 0.0 : this.y.get();
    }

    public final DoubleProperty yProperty() {
        if (this.y == null) {
            this.y = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    ColorInput.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                    ColorInput.this.effectBoundsChanged();
                }

                @Override
                public Object getBean() {
                    return ColorInput.this;
                }

                @Override
                public String getName() {
                    return "y";
                }
            };
        }
        return this.y;
    }

    public final void setWidth(double d) {
        this.widthProperty().set(d);
    }

    public final double getWidth() {
        return this.width == null ? 0.0 : this.width.get();
    }

    public final DoubleProperty widthProperty() {
        if (this.width == null) {
            this.width = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    ColorInput.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                    ColorInput.this.effectBoundsChanged();
                }

                @Override
                public Object getBean() {
                    return ColorInput.this;
                }

                @Override
                public String getName() {
                    return "width";
                }
            };
        }
        return this.width;
    }

    public final void setHeight(double d) {
        this.heightProperty().set(d);
    }

    public final double getHeight() {
        return this.height == null ? 0.0 : this.height.get();
    }

    public final DoubleProperty heightProperty() {
        if (this.height == null) {
            this.height = new DoublePropertyBase(){

                @Override
                public void invalidated() {
                    ColorInput.this.markDirty(EffectDirtyBits.EFFECT_DIRTY);
                    ColorInput.this.effectBoundsChanged();
                }

                @Override
                public Object getBean() {
                    return ColorInput.this;
                }

                @Override
                public String getName() {
                    return "height";
                }
            };
        }
        return this.height;
    }

    private Paint getPaintInternal() {
        Paint paint = this.getPaint();
        return paint == null ? Color.RED : paint;
    }

    @Override
    void update() {
        Flood flood = (Flood)this.getPeer();
        flood.setPaint(Toolkit.getPaintAccessor().getPlatformPaint(this.getPaintInternal()));
        flood.setFloodBounds(new RectBounds((float)this.getX(), (float)this.getY(), (float)(this.getX() + this.getWidth()), (float)(this.getY() + this.getHeight())));
    }

    @Override
    boolean checkChainContains(Effect effect) {
        return false;
    }

    @Override
    BaseBounds getBounds(BaseBounds baseBounds, BaseTransform baseTransform, Node node, BoundsAccessor boundsAccessor) {
        RectBounds rectBounds = new RectBounds((float)this.getX(), (float)this.getY(), (float)(this.getX() + this.getWidth()), (float)(this.getY() + this.getHeight()));
        return ColorInput.transformBounds(baseTransform, rectBounds);
    }

    @Override
    Effect copy() {
        return new ColorInput(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.getPaint());
    }
}

