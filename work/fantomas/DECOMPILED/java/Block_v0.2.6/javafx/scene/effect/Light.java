/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.effect;

import com.sun.javafx.tk.Toolkit;
import com.sun.javafx.util.Utils;
import com.sun.scenario.effect.light.DistantLight;
import com.sun.scenario.effect.light.PointLight;
import com.sun.scenario.effect.light.SpotLight;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.paint.Color;

public abstract class Light {
    private com.sun.scenario.effect.light.Light peer;
    private ObjectProperty<Color> color;
    private BooleanProperty effectDirty;

    protected Light() {
        this.markDirty();
    }

    abstract com.sun.scenario.effect.light.Light createPeer();

    com.sun.scenario.effect.light.Light getPeer() {
        if (this.peer == null) {
            this.peer = this.createPeer();
        }
        return this.peer;
    }

    public final void setColor(Color color) {
        this.colorProperty().set(color);
    }

    public final Color getColor() {
        return this.color == null ? Color.WHITE : (Color)this.color.get();
    }

    public final ObjectProperty<Color> colorProperty() {
        if (this.color == null) {
            this.color = new ObjectPropertyBase<Color>(Color.WHITE){

                @Override
                public void invalidated() {
                    Light.this.markDirty();
                }

                @Override
                public Object getBean() {
                    return Light.this;
                }

                @Override
                public String getName() {
                    return "color";
                }
            };
        }
        return this.color;
    }

    void sync() {
        if (this.isEffectDirty()) {
            this.update();
            this.clearDirty();
        }
    }

    private Color getColorInternal() {
        Color color = this.getColor();
        return color == null ? Color.WHITE : color;
    }

    void update() {
        this.getPeer().setColor(Toolkit.getToolkit().toColor4f(this.getColorInternal()));
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

    final void markDirty() {
        this.setEffectDirty(true);
    }

    final void clearDirty() {
        this.setEffectDirty(false);
    }

    public static class Spot
    extends Point {
        private DoubleProperty pointsAtX;
        private DoubleProperty pointsAtY;
        private DoubleProperty pointsAtZ;
        private DoubleProperty specularExponent;

        public Spot() {
        }

        public Spot(double d, double d2, double d3, double d4, Color color) {
            this.setX(d);
            this.setY(d2);
            this.setZ(d3);
            this.setSpecularExponent(d4);
            this.setColor(color);
        }

        @Override
        SpotLight createPeer() {
            return new SpotLight();
        }

        public final void setPointsAtX(double d) {
            this.pointsAtXProperty().set(d);
        }

        public final double getPointsAtX() {
            return this.pointsAtX == null ? 0.0 : this.pointsAtX.get();
        }

        public final DoubleProperty pointsAtXProperty() {
            if (this.pointsAtX == null) {
                this.pointsAtX = new DoublePropertyBase(){

                    @Override
                    public void invalidated() {
                        this.markDirty();
                    }

                    @Override
                    public Object getBean() {
                        return this;
                    }

                    @Override
                    public String getName() {
                        return "pointsAtX";
                    }
                };
            }
            return this.pointsAtX;
        }

        public final void setPointsAtY(double d) {
            this.pointsAtYProperty().set(d);
        }

        public final double getPointsAtY() {
            return this.pointsAtY == null ? 0.0 : this.pointsAtY.get();
        }

        public final DoubleProperty pointsAtYProperty() {
            if (this.pointsAtY == null) {
                this.pointsAtY = new DoublePropertyBase(){

                    @Override
                    public void invalidated() {
                        this.markDirty();
                    }

                    @Override
                    public Object getBean() {
                        return this;
                    }

                    @Override
                    public String getName() {
                        return "pointsAtY";
                    }
                };
            }
            return this.pointsAtY;
        }

        public final void setPointsAtZ(double d) {
            this.pointsAtZProperty().set(d);
        }

        public final double getPointsAtZ() {
            return this.pointsAtZ == null ? 0.0 : this.pointsAtZ.get();
        }

        public final DoubleProperty pointsAtZProperty() {
            if (this.pointsAtZ == null) {
                this.pointsAtZ = new DoublePropertyBase(){

                    @Override
                    public void invalidated() {
                        this.markDirty();
                    }

                    @Override
                    public Object getBean() {
                        return this;
                    }

                    @Override
                    public String getName() {
                        return "pointsAtZ";
                    }
                };
            }
            return this.pointsAtZ;
        }

        public final void setSpecularExponent(double d) {
            this.specularExponentProperty().set(d);
        }

        public final double getSpecularExponent() {
            return this.specularExponent == null ? 1.0 : this.specularExponent.get();
        }

        public final DoubleProperty specularExponentProperty() {
            if (this.specularExponent == null) {
                this.specularExponent = new DoublePropertyBase(1.0){

                    @Override
                    public void invalidated() {
                        this.markDirty();
                    }

                    @Override
                    public Object getBean() {
                        return this;
                    }

                    @Override
                    public String getName() {
                        return "specularExponent";
                    }
                };
            }
            return this.specularExponent;
        }

        @Override
        void update() {
            super.update();
            SpotLight spotLight = (SpotLight)this.getPeer();
            spotLight.setPointsAtX((float)this.getPointsAtX());
            spotLight.setPointsAtY((float)this.getPointsAtY());
            spotLight.setPointsAtZ((float)this.getPointsAtZ());
            spotLight.setSpecularExponent((float)Utils.clamp(0.0, this.getSpecularExponent(), 4.0));
        }
    }

    public static class Point
    extends Light {
        private DoubleProperty x;
        private DoubleProperty y;
        private DoubleProperty z;

        public Point() {
        }

        public Point(double d, double d2, double d3, Color color) {
            this.setX(d);
            this.setY(d2);
            this.setZ(d3);
            this.setColor(color);
        }

        @Override
        PointLight createPeer() {
            return new PointLight();
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
                        this.markDirty();
                    }

                    @Override
                    public Object getBean() {
                        return this;
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
                        this.markDirty();
                    }

                    @Override
                    public Object getBean() {
                        return this;
                    }

                    @Override
                    public String getName() {
                        return "y";
                    }
                };
            }
            return this.y;
        }

        public final void setZ(double d) {
            this.zProperty().set(d);
        }

        public final double getZ() {
            return this.z == null ? 0.0 : this.z.get();
        }

        public final DoubleProperty zProperty() {
            if (this.z == null) {
                this.z = new DoublePropertyBase(){

                    @Override
                    public void invalidated() {
                        this.markDirty();
                    }

                    @Override
                    public Object getBean() {
                        return this;
                    }

                    @Override
                    public String getName() {
                        return "z";
                    }
                };
            }
            return this.z;
        }

        @Override
        void update() {
            super.update();
            PointLight pointLight = (PointLight)this.getPeer();
            pointLight.setX((float)this.getX());
            pointLight.setY((float)this.getY());
            pointLight.setZ((float)this.getZ());
        }
    }

    public static class Distant
    extends Light {
        private DoubleProperty azimuth;
        private DoubleProperty elevation;

        public Distant() {
        }

        public Distant(double d, double d2, Color color) {
            this.setAzimuth(d);
            this.setElevation(d2);
            this.setColor(color);
        }

        @Override
        DistantLight createPeer() {
            return new DistantLight();
        }

        public final void setAzimuth(double d) {
            this.azimuthProperty().set(d);
        }

        public final double getAzimuth() {
            return this.azimuth == null ? 45.0 : this.azimuth.get();
        }

        public final DoubleProperty azimuthProperty() {
            if (this.azimuth == null) {
                this.azimuth = new DoublePropertyBase(45.0){

                    @Override
                    public void invalidated() {
                        this.markDirty();
                    }

                    @Override
                    public Object getBean() {
                        return this;
                    }

                    @Override
                    public String getName() {
                        return "azimuth";
                    }
                };
            }
            return this.azimuth;
        }

        public final void setElevation(double d) {
            this.elevationProperty().set(d);
        }

        public final double getElevation() {
            return this.elevation == null ? 45.0 : this.elevation.get();
        }

        public final DoubleProperty elevationProperty() {
            if (this.elevation == null) {
                this.elevation = new DoublePropertyBase(45.0){

                    @Override
                    public void invalidated() {
                        this.markDirty();
                    }

                    @Override
                    public Object getBean() {
                        return this;
                    }

                    @Override
                    public String getName() {
                        return "elevation";
                    }
                };
            }
            return this.elevation;
        }

        @Override
        void update() {
            super.update();
            DistantLight distantLight = (DistantLight)this.getPeer();
            distantLight.setAzimuth((float)this.getAzimuth());
            distantLight.setElevation((float)this.getElevation());
        }
    }
}

