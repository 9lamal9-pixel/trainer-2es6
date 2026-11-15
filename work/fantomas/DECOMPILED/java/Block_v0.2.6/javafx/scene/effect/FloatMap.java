/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.effect;

import com.sun.javafx.util.Utils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.IntegerPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;

public class FloatMap {
    private com.sun.scenario.effect.FloatMap map;
    private float[] buf;
    private boolean mapBufferDirty = true;
    private BooleanProperty effectDirty;
    private IntegerProperty width;
    private IntegerProperty height;

    com.sun.scenario.effect.FloatMap getImpl() {
        return this.map;
    }

    private void updateBuffer() {
        if (this.getWidth() > 0 && this.getHeight() > 0) {
            int n = Utils.clampMax(this.getWidth(), 4096);
            int n2 = Utils.clampMax(this.getHeight(), 4096);
            int n3 = n * n2 * 4;
            this.buf = new float[n3];
            this.mapBufferDirty = true;
        }
    }

    private void update() {
        if (this.mapBufferDirty) {
            this.map = new com.sun.scenario.effect.FloatMap(Utils.clamp(1, this.getWidth(), 4096), Utils.clamp(1, this.getHeight(), 4096));
            this.mapBufferDirty = false;
        }
        this.map.put(this.buf);
    }

    void sync() {
        if (this.isEffectDirty()) {
            this.update();
            this.clearDirty();
        }
    }

    private void setEffectDirty(boolean bl) {
        this.effectDirtyProperty().set(bl);
    }

    final BooleanProperty effectDirtyProperty() {
        if (this.effectDirty == null) {
            this.effectDirty = new SimpleBooleanProperty(this, "effectDirty");
        }
        return this.effectDirty;
    }

    boolean isEffectDirty() {
        return this.effectDirty == null ? false : this.effectDirty.get();
    }

    private void markDirty() {
        this.setEffectDirty(true);
    }

    private void clearDirty() {
        this.setEffectDirty(false);
    }

    public FloatMap() {
        this.updateBuffer();
        this.markDirty();
    }

    public FloatMap(int n, int n2) {
        this.setWidth(n);
        this.setHeight(n2);
        this.updateBuffer();
        this.markDirty();
    }

    public final void setWidth(int n) {
        this.widthProperty().set(n);
    }

    public final int getWidth() {
        return this.width == null ? 1 : this.width.get();
    }

    public final IntegerProperty widthProperty() {
        if (this.width == null) {
            this.width = new IntegerPropertyBase(1){

                @Override
                public void invalidated() {
                    FloatMap.this.updateBuffer();
                    FloatMap.this.markDirty();
                }

                @Override
                public Object getBean() {
                    return FloatMap.this;
                }

                @Override
                public String getName() {
                    return "width";
                }
            };
        }
        return this.width;
    }

    public final void setHeight(int n) {
        this.heightProperty().set(n);
    }

    public final int getHeight() {
        return this.height == null ? 1 : this.height.get();
    }

    public final IntegerProperty heightProperty() {
        if (this.height == null) {
            this.height = new IntegerPropertyBase(1){

                @Override
                public void invalidated() {
                    FloatMap.this.updateBuffer();
                    FloatMap.this.markDirty();
                }

                @Override
                public Object getBean() {
                    return FloatMap.this;
                }

                @Override
                public String getName() {
                    return "height";
                }
            };
        }
        return this.height;
    }

    public void setSample(int n, int n2, int n3, float f) {
        this.buf[(n + n2 * this.getWidth()) * 4 + n3] = f;
        this.markDirty();
    }

    public void setSamples(int n, int n2, float f) {
        int n3 = (n + n2 * this.getWidth()) * 4;
        this.buf[n3 + 0] = f;
        this.markDirty();
    }

    public void setSamples(int n, int n2, float f, float f2) {
        int n3 = (n + n2 * this.getWidth()) * 4;
        this.buf[n3 + 0] = f;
        this.buf[n3 + 1] = f2;
        this.markDirty();
    }

    public void setSamples(int n, int n2, float f, float f2, float f3) {
        int n3 = (n + n2 * this.getWidth()) * 4;
        this.buf[n3 + 0] = f;
        this.buf[n3 + 1] = f2;
        this.buf[n3 + 2] = f3;
        this.markDirty();
    }

    public void setSamples(int n, int n2, float f, float f2, float f3, float f4) {
        int n3 = (n + n2 * this.getWidth()) * 4;
        this.buf[n3 + 0] = f;
        this.buf[n3 + 1] = f2;
        this.buf[n3 + 2] = f3;
        this.buf[n3 + 3] = f4;
        this.markDirty();
    }

    FloatMap copy() {
        FloatMap floatMap = new FloatMap(this.getWidth(), this.getHeight());
        System.arraycopy(this.buf, 0, floatMap.buf, 0, this.buf.length);
        return floatMap;
    }
}

