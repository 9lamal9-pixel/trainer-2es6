/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.paint;

import com.sun.javafx.beans.event.AbstractNotifyListener;
import com.sun.javafx.scene.paint.MaterialHelper;
import com.sun.javafx.sg.prism.NGPhongMaterial;
import com.sun.javafx.tk.Toolkit;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;

public class PhongMaterial
extends Material {
    private boolean diffuseColorDirty = true;
    private boolean specularColorDirty = true;
    private boolean specularPowerDirty = true;
    private boolean diffuseMapDirty = true;
    private boolean specularMapDirty = true;
    private boolean bumpMapDirty = true;
    private boolean selfIlluminationMapDirty = true;
    private ObjectProperty<Color> diffuseColor;
    private ObjectProperty<Color> specularColor;
    private DoubleProperty specularPower;
    private final AbstractNotifyListener platformImageChangeListener = new AbstractNotifyListener(){

        @Override
        public void invalidated(Observable observable) {
            if (PhongMaterial.this.oldDiffuseMap != null && observable == Toolkit.getImageAccessor().getImageProperty(PhongMaterial.this.oldDiffuseMap)) {
                PhongMaterial.this.diffuseMapDirty = true;
            } else if (PhongMaterial.this.oldSpecularMap != null && observable == Toolkit.getImageAccessor().getImageProperty(PhongMaterial.this.oldSpecularMap)) {
                PhongMaterial.this.specularMapDirty = true;
            } else if (PhongMaterial.this.oldBumpMap != null && observable == Toolkit.getImageAccessor().getImageProperty(PhongMaterial.this.oldBumpMap)) {
                PhongMaterial.this.bumpMapDirty = true;
            } else if (PhongMaterial.this.oldSelfIlluminationMap != null && observable == Toolkit.getImageAccessor().getImageProperty(PhongMaterial.this.oldSelfIlluminationMap)) {
                PhongMaterial.this.selfIlluminationMapDirty = true;
            }
            PhongMaterial.this.setDirty(true);
        }
    };
    private ObjectProperty<Image> diffuseMap;
    private Image oldDiffuseMap;
    private ObjectProperty<Image> specularMap;
    private Image oldSpecularMap;
    private ObjectProperty<Image> bumpMap;
    private Image oldBumpMap;
    private ObjectProperty<Image> selfIlluminationMap;
    private Image oldSelfIlluminationMap;
    private NGPhongMaterial peer;

    public PhongMaterial() {
        this.setDiffuseColor(Color.WHITE);
    }

    public PhongMaterial(Color color) {
        this.setDiffuseColor(color);
    }

    public PhongMaterial(Color color, Image image, Image image2, Image image3, Image image4) {
        this.setDiffuseColor(color);
        this.setDiffuseMap(image);
        this.setSpecularMap(image2);
        this.setBumpMap(image3);
        this.setSelfIlluminationMap(image4);
    }

    public final void setDiffuseColor(Color color) {
        this.diffuseColorProperty().set(color);
    }

    public final Color getDiffuseColor() {
        return this.diffuseColor == null ? null : (Color)this.diffuseColor.get();
    }

    public final ObjectProperty<Color> diffuseColorProperty() {
        if (this.diffuseColor == null) {
            this.diffuseColor = new SimpleObjectProperty<Color>((Object)this, "diffuseColor"){

                @Override
                protected void invalidated() {
                    PhongMaterial.this.diffuseColorDirty = true;
                    PhongMaterial.this.setDirty(true);
                }
            };
        }
        return this.diffuseColor;
    }

    public final void setSpecularColor(Color color) {
        this.specularColorProperty().set(color);
    }

    public final Color getSpecularColor() {
        return this.specularColor == null ? null : (Color)this.specularColor.get();
    }

    public final ObjectProperty<Color> specularColorProperty() {
        if (this.specularColor == null) {
            this.specularColor = new SimpleObjectProperty<Color>((Object)this, "specularColor"){

                @Override
                protected void invalidated() {
                    PhongMaterial.this.specularColorDirty = true;
                    PhongMaterial.this.setDirty(true);
                }
            };
        }
        return this.specularColor;
    }

    public final void setSpecularPower(double d) {
        this.specularPowerProperty().set(d);
    }

    public final double getSpecularPower() {
        return this.specularPower == null ? 32.0 : this.specularPower.get();
    }

    public final DoubleProperty specularPowerProperty() {
        if (this.specularPower == null) {
            this.specularPower = new SimpleDoubleProperty(this, "specularPower", 32.0){

                @Override
                public void invalidated() {
                    PhongMaterial.this.specularPowerDirty = true;
                    PhongMaterial.this.setDirty(true);
                }
            };
        }
        return this.specularPower;
    }

    public final void setDiffuseMap(Image image) {
        this.diffuseMapProperty().set(image);
    }

    public final Image getDiffuseMap() {
        return this.diffuseMap == null ? null : (Image)this.diffuseMap.get();
    }

    public final ObjectProperty<Image> diffuseMapProperty() {
        if (this.diffuseMap == null) {
            this.diffuseMap = new SimpleObjectProperty<Image>((Object)this, "diffuseMap"){
                private boolean needsListeners;
                {
                    this.needsListeners = false;
                }

                @Override
                public void invalidated() {
                    Image image = (Image)this.get();
                    if (this.needsListeners) {
                        Toolkit.getImageAccessor().getImageProperty(PhongMaterial.this.oldDiffuseMap).removeListener(PhongMaterial.this.platformImageChangeListener.getWeakListener());
                    }
                    boolean bl = this.needsListeners = image != null && (Toolkit.getImageAccessor().isAnimation(image) || image.getProgress() < 1.0);
                    if (this.needsListeners) {
                        Toolkit.getImageAccessor().getImageProperty(image).addListener(PhongMaterial.this.platformImageChangeListener.getWeakListener());
                    }
                    PhongMaterial.this.oldDiffuseMap = image;
                    PhongMaterial.this.diffuseMapDirty = true;
                    PhongMaterial.this.setDirty(true);
                }
            };
        }
        return this.diffuseMap;
    }

    public final void setSpecularMap(Image image) {
        this.specularMapProperty().set(image);
    }

    public final Image getSpecularMap() {
        return this.specularMap == null ? null : (Image)this.specularMap.get();
    }

    public final ObjectProperty<Image> specularMapProperty() {
        if (this.specularMap == null) {
            this.specularMap = new SimpleObjectProperty<Image>((Object)this, "specularMap"){
                private boolean needsListeners;
                {
                    this.needsListeners = false;
                }

                @Override
                public void invalidated() {
                    Image image = (Image)this.get();
                    if (this.needsListeners) {
                        Toolkit.getImageAccessor().getImageProperty(PhongMaterial.this.oldSpecularMap).removeListener(PhongMaterial.this.platformImageChangeListener.getWeakListener());
                    }
                    boolean bl = this.needsListeners = image != null && (Toolkit.getImageAccessor().isAnimation(image) || image.getProgress() < 1.0);
                    if (this.needsListeners) {
                        Toolkit.getImageAccessor().getImageProperty(image).addListener(PhongMaterial.this.platformImageChangeListener.getWeakListener());
                    }
                    PhongMaterial.this.oldSpecularMap = image;
                    PhongMaterial.this.specularMapDirty = true;
                    PhongMaterial.this.setDirty(true);
                }
            };
        }
        return this.specularMap;
    }

    public final void setBumpMap(Image image) {
        this.bumpMapProperty().set(image);
    }

    public final Image getBumpMap() {
        return this.bumpMap == null ? null : (Image)this.bumpMap.get();
    }

    public final ObjectProperty<Image> bumpMapProperty() {
        if (this.bumpMap == null) {
            this.bumpMap = new SimpleObjectProperty<Image>((Object)this, "bumpMap"){
                private boolean needsListeners;
                {
                    this.needsListeners = false;
                }

                @Override
                public void invalidated() {
                    Image image = (Image)this.get();
                    if (this.needsListeners) {
                        Toolkit.getImageAccessor().getImageProperty(PhongMaterial.this.oldBumpMap).removeListener(PhongMaterial.this.platformImageChangeListener.getWeakListener());
                    }
                    boolean bl = this.needsListeners = image != null && (Toolkit.getImageAccessor().isAnimation(image) || image.getProgress() < 1.0);
                    if (this.needsListeners) {
                        Toolkit.getImageAccessor().getImageProperty(image).addListener(PhongMaterial.this.platformImageChangeListener.getWeakListener());
                    }
                    PhongMaterial.this.oldBumpMap = image;
                    PhongMaterial.this.bumpMapDirty = true;
                    PhongMaterial.this.setDirty(true);
                }
            };
        }
        return this.bumpMap;
    }

    public final void setSelfIlluminationMap(Image image) {
        this.selfIlluminationMapProperty().set(image);
    }

    public final Image getSelfIlluminationMap() {
        return this.selfIlluminationMap == null ? null : (Image)this.selfIlluminationMap.get();
    }

    public final ObjectProperty<Image> selfIlluminationMapProperty() {
        if (this.selfIlluminationMap == null) {
            this.selfIlluminationMap = new SimpleObjectProperty<Image>((Object)this, "selfIlluminationMap"){
                private boolean needsListeners;
                {
                    this.needsListeners = false;
                }

                @Override
                public void invalidated() {
                    Image image = (Image)this.get();
                    if (this.needsListeners) {
                        Toolkit.getImageAccessor().getImageProperty(PhongMaterial.this.oldSelfIlluminationMap).removeListener(PhongMaterial.this.platformImageChangeListener.getWeakListener());
                    }
                    boolean bl = this.needsListeners = image != null && (Toolkit.getImageAccessor().isAnimation(image) || image.getProgress() < 1.0);
                    if (this.needsListeners) {
                        Toolkit.getImageAccessor().getImageProperty(image).addListener(PhongMaterial.this.platformImageChangeListener.getWeakListener());
                    }
                    PhongMaterial.this.oldSelfIlluminationMap = image;
                    PhongMaterial.this.selfIlluminationMapDirty = true;
                    PhongMaterial.this.setDirty(true);
                }
            };
        }
        return this.selfIlluminationMap;
    }

    @Override
    void setDirty(boolean bl) {
        super.setDirty(bl);
        if (!bl) {
            this.diffuseColorDirty = false;
            this.specularColorDirty = false;
            this.specularPowerDirty = false;
            this.diffuseMapDirty = false;
            this.specularMapDirty = false;
            this.bumpMapDirty = false;
            this.selfIlluminationMapDirty = false;
        }
    }

    @Override
    NGPhongMaterial getNGMaterial() {
        if (this.peer == null) {
            this.peer = new NGPhongMaterial();
        }
        return this.peer;
    }

    @Override
    void updatePG() {
        if (!this.isDirty()) {
            return;
        }
        NGPhongMaterial nGPhongMaterial = MaterialHelper.getNGMaterial(this);
        if (this.diffuseColorDirty) {
            nGPhongMaterial.setDiffuseColor(this.getDiffuseColor() == null ? null : Toolkit.getPaintAccessor().getPlatformPaint(this.getDiffuseColor()));
        }
        if (this.specularColorDirty) {
            nGPhongMaterial.setSpecularColor(this.getSpecularColor() == null ? null : Toolkit.getPaintAccessor().getPlatformPaint(this.getSpecularColor()));
        }
        if (this.specularPowerDirty) {
            nGPhongMaterial.setSpecularPower((float)this.getSpecularPower());
        }
        if (this.diffuseMapDirty) {
            nGPhongMaterial.setDiffuseMap(this.getDiffuseMap() == null ? null : Toolkit.getImageAccessor().getPlatformImage(this.getDiffuseMap()));
        }
        if (this.specularMapDirty) {
            nGPhongMaterial.setSpecularMap(this.getSpecularMap() == null ? null : Toolkit.getImageAccessor().getPlatformImage(this.getSpecularMap()));
        }
        if (this.bumpMapDirty) {
            nGPhongMaterial.setBumpMap(this.getBumpMap() == null ? null : Toolkit.getImageAccessor().getPlatformImage(this.getBumpMap()));
        }
        if (this.selfIlluminationMapDirty) {
            nGPhongMaterial.setSelfIllumMap(this.getSelfIlluminationMap() == null ? null : Toolkit.getImageAccessor().getPlatformImage(this.getSelfIlluminationMap()));
        }
        this.setDirty(false);
    }

    public String toString() {
        return "PhongMaterial[diffuseColor=" + this.getDiffuseColor() + ", specularColor=" + this.getSpecularColor() + ", specularPower=" + this.getSpecularPower() + ", diffuseMap=" + this.getDiffuseMap() + ", specularMap=" + this.getSpecularMap() + ", bumpMap=" + this.getBumpMap() + ", selfIlluminationMap=" + this.getSelfIlluminationMap() + "]";
    }
}

