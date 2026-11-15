/*
 * Decompiled with CFR 0.152.
 */
package com.sun.scenario.effect.light;

import com.sun.scenario.effect.Color4f;

public abstract class Light {
    private final Type type;
    private Color4f color;

    Light(Type type) {
        this(type, Color4f.WHITE);
    }

    Light(Type type, Color4f color4f) {
        if (type == null) {
            throw new InternalError("Light type must be non-null");
        }
        this.type = type;
        this.setColor(color4f);
    }

    public Type getType() {
        return this.type;
    }

    public Color4f getColor() {
        return this.color;
    }

    public void setColor(Color4f color4f) {
        if (color4f == null) {
            throw new IllegalArgumentException("Color must be non-null");
        }
        this.color = color4f;
    }

    public abstract float[] getNormalizedLightPosition();

    public static enum Type {
        DISTANT,
        POINT,
        SPOT;

    }
}

