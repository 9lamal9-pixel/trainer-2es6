/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.image;

import com.sun.javafx.beans.event.AbstractNotifyListener;
import com.sun.javafx.css.StyleManager;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.ImageViewHelper;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.sg.prism.NGImageView;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.tk.Toolkit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.DefaultProperty;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.beans.property.StringProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.StyleableStringProperty;
import javafx.css.converter.URLConverter;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.image.Image;

@DefaultProperty(value="image")
public class ImageView
extends Node {
    private ObjectProperty<Image> image;
    private Image oldImage;
    private StringProperty imageUrl;
    private final AbstractNotifyListener platformImageChangeListener;
    private DoubleProperty x;
    private DoubleProperty y;
    private DoubleProperty fitWidth;
    private DoubleProperty fitHeight;
    private BooleanProperty preserveRatio;
    private BooleanProperty smooth;
    public static final boolean SMOOTH_DEFAULT;
    private ObjectProperty<Rectangle2D> viewport;
    private double destWidth;
    private double destHeight;
    private boolean validWH;
    private static final String DEFAULT_STYLE_CLASS = "image-view";

    public ImageView() {
        ImageViewHelper.initHelper(this);
        this.imageUrl = null;
        this.platformImageChangeListener = new AbstractNotifyListener(){

            @Override
            public void invalidated(Observable observable) {
                ImageView.this.invalidateWidthHeight();
                NodeHelper.markDirty(ImageView.this, DirtyBits.NODE_CONTENTS);
                NodeHelper.geomChanged(ImageView.this);
            }
        };
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
        this.setAccessibleRole(AccessibleRole.IMAGE_VIEW);
        this.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
    }

    public ImageView(String string) {
        this(new Image(string));
    }

    public ImageView(Image image) {
        ImageViewHelper.initHelper(this);
        this.imageUrl = null;
        this.platformImageChangeListener = new /* invalid duplicate definition of identical inner class */;
        this.getStyleClass().add(DEFAULT_STYLE_CLASS);
        this.setAccessibleRole(AccessibleRole.IMAGE_VIEW);
        this.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        this.setImage(image);
    }

    public final void setImage(Image image) {
        this.imageProperty().set(image);
    }

    public final Image getImage() {
        return this.image == null ? null : (Image)this.image.get();
    }

    public final ObjectProperty<Image> imageProperty() {
        if (this.image == null) {
            this.image = new ObjectPropertyBase<Image>(){
                private boolean needsListeners = false;

                @Override
                public void invalidated() {
                    boolean bl;
                    Image image = (Image)this.get();
                    boolean bl2 = bl = image == null || ImageView.this.oldImage == null || ImageView.this.oldImage.getWidth() != image.getWidth() || ImageView.this.oldImage.getHeight() != image.getHeight();
                    if (this.needsListeners) {
                        Toolkit.getImageAccessor().getImageProperty(ImageView.this.oldImage).removeListener(ImageView.this.platformImageChangeListener.getWeakListener());
                    }
                    this.needsListeners = image != null && (image.isAnimation() || image.getProgress() < 1.0);
                    ImageView.this.oldImage = image;
                    if (this.needsListeners) {
                        Toolkit.getImageAccessor().getImageProperty(image).addListener(ImageView.this.platformImageChangeListener.getWeakListener());
                    }
                    if (bl) {
                        ImageView.this.invalidateWidthHeight();
                        NodeHelper.geomChanged(ImageView.this);
                    }
                    NodeHelper.markDirty(ImageView.this, DirtyBits.NODE_CONTENTS);
                }

                @Override
                public Object getBean() {
                    return ImageView.this;
                }

                @Override
                public String getName() {
                    return "image";
                }
            };
        }
        return this.image;
    }

    private StringProperty imageUrlProperty() {
        if (this.imageUrl == null) {
            this.imageUrl = new StyleableStringProperty(){

                @Override
                protected void invalidated() {
                    String string = this.get();
                    if (string != null) {
                        ImageView.this.setImage(StyleManager.getInstance().getCachedImage(string));
                    } else {
                        ImageView.this.setImage(null);
                    }
                }

                @Override
                public Object getBean() {
                    return ImageView.this;
                }

                @Override
                public String getName() {
                    return "imageUrl";
                }

                @Override
                public CssMetaData<ImageView, String> getCssMetaData() {
                    return StyleableProperties.IMAGE;
                }
            };
        }
        return this.imageUrl;
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
                protected void invalidated() {
                    NodeHelper.markDirty(ImageView.this, DirtyBits.NODE_GEOMETRY);
                    NodeHelper.geomChanged(ImageView.this);
                }

                @Override
                public Object getBean() {
                    return ImageView.this;
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
                protected void invalidated() {
                    NodeHelper.markDirty(ImageView.this, DirtyBits.NODE_GEOMETRY);
                    NodeHelper.geomChanged(ImageView.this);
                }

                @Override
                public Object getBean() {
                    return ImageView.this;
                }

                @Override
                public String getName() {
                    return "y";
                }
            };
        }
        return this.y;
    }

    public final void setFitWidth(double d) {
        this.fitWidthProperty().set(d);
    }

    public final double getFitWidth() {
        return this.fitWidth == null ? 0.0 : this.fitWidth.get();
    }

    public final DoubleProperty fitWidthProperty() {
        if (this.fitWidth == null) {
            this.fitWidth = new DoublePropertyBase(){

                @Override
                protected void invalidated() {
                    ImageView.this.invalidateWidthHeight();
                    NodeHelper.markDirty(ImageView.this, DirtyBits.NODE_VIEWPORT);
                    NodeHelper.geomChanged(ImageView.this);
                }

                @Override
                public Object getBean() {
                    return ImageView.this;
                }

                @Override
                public String getName() {
                    return "fitWidth";
                }
            };
        }
        return this.fitWidth;
    }

    public final void setFitHeight(double d) {
        this.fitHeightProperty().set(d);
    }

    public final double getFitHeight() {
        return this.fitHeight == null ? 0.0 : this.fitHeight.get();
    }

    public final DoubleProperty fitHeightProperty() {
        if (this.fitHeight == null) {
            this.fitHeight = new DoublePropertyBase(){

                @Override
                protected void invalidated() {
                    ImageView.this.invalidateWidthHeight();
                    NodeHelper.markDirty(ImageView.this, DirtyBits.NODE_VIEWPORT);
                    NodeHelper.geomChanged(ImageView.this);
                }

                @Override
                public Object getBean() {
                    return ImageView.this;
                }

                @Override
                public String getName() {
                    return "fitHeight";
                }
            };
        }
        return this.fitHeight;
    }

    public final void setPreserveRatio(boolean bl) {
        this.preserveRatioProperty().set(bl);
    }

    public final boolean isPreserveRatio() {
        return this.preserveRatio == null ? false : this.preserveRatio.get();
    }

    public final BooleanProperty preserveRatioProperty() {
        if (this.preserveRatio == null) {
            this.preserveRatio = new BooleanPropertyBase(){

                @Override
                protected void invalidated() {
                    ImageView.this.invalidateWidthHeight();
                    NodeHelper.markDirty(ImageView.this, DirtyBits.NODE_VIEWPORT);
                    NodeHelper.geomChanged(ImageView.this);
                }

                @Override
                public Object getBean() {
                    return ImageView.this;
                }

                @Override
                public String getName() {
                    return "preserveRatio";
                }
            };
        }
        return this.preserveRatio;
    }

    public final void setSmooth(boolean bl) {
        this.smoothProperty().set(bl);
    }

    public final boolean isSmooth() {
        return this.smooth == null ? SMOOTH_DEFAULT : this.smooth.get();
    }

    public final BooleanProperty smoothProperty() {
        if (this.smooth == null) {
            this.smooth = new BooleanPropertyBase(SMOOTH_DEFAULT){

                @Override
                protected void invalidated() {
                    NodeHelper.markDirty(ImageView.this, DirtyBits.NODE_SMOOTH);
                }

                @Override
                public Object getBean() {
                    return ImageView.this;
                }

                @Override
                public String getName() {
                    return "smooth";
                }
            };
        }
        return this.smooth;
    }

    public final void setViewport(Rectangle2D rectangle2D) {
        this.viewportProperty().set(rectangle2D);
    }

    public final Rectangle2D getViewport() {
        return this.viewport == null ? null : (Rectangle2D)this.viewport.get();
    }

    public final ObjectProperty<Rectangle2D> viewportProperty() {
        if (this.viewport == null) {
            this.viewport = new ObjectPropertyBase<Rectangle2D>(){

                @Override
                protected void invalidated() {
                    ImageView.this.invalidateWidthHeight();
                    NodeHelper.markDirty(ImageView.this, DirtyBits.NODE_VIEWPORT);
                    NodeHelper.geomChanged(ImageView.this);
                }

                @Override
                public Object getBean() {
                    return ImageView.this;
                }

                @Override
                public String getName() {
                    return "viewport";
                }
            };
        }
        return this.viewport;
    }

    private NGNode doCreatePeer() {
        return new NGImageView();
    }

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        this.recomputeWidthHeight();
        baseBounds = baseBounds.deriveWithNewBounds((float)this.getX(), (float)this.getY(), 0.0f, (float)(this.getX() + this.destWidth), (float)(this.getY() + this.destHeight), 0.0f);
        baseBounds = baseTransform.transform(baseBounds, baseBounds);
        return baseBounds;
    }

    private void invalidateWidthHeight() {
        this.validWH = false;
    }

    private void recomputeWidthHeight() {
        if (this.validWH) {
            return;
        }
        Image image = this.getImage();
        Rectangle2D rectangle2D = this.getViewport();
        double d = 0.0;
        double d2 = 0.0;
        if (rectangle2D != null && rectangle2D.getWidth() > 0.0 && rectangle2D.getHeight() > 0.0) {
            d = rectangle2D.getWidth();
            d2 = rectangle2D.getHeight();
        } else if (image != null) {
            d = image.getWidth();
            d2 = image.getHeight();
        }
        double d3 = this.getFitWidth();
        double d4 = this.getFitHeight();
        if (this.isPreserveRatio() && d > 0.0 && d2 > 0.0 && (d3 > 0.0 || d4 > 0.0)) {
            if (d3 <= 0.0 || d4 > 0.0 && d3 * d2 > d4 * d) {
                d = d * d4 / d2;
                d2 = d4;
            } else {
                d2 = d2 * d3 / d;
                d = d3;
            }
        } else {
            if (d3 > 0.0) {
                d = d3;
            }
            if (d4 > 0.0) {
                d2 = d4;
            }
        }
        this.destWidth = d;
        this.destHeight = d2;
        this.validWH = true;
    }

    private boolean doComputeContains(double d, double d2) {
        if (this.getImage() == null) {
            return false;
        }
        this.recomputeWidthHeight();
        double d3 = d - this.getX();
        double d4 = d2 - this.getY();
        Image image = this.getImage();
        double d5 = image.getWidth();
        double d6 = image.getHeight();
        double d7 = d5;
        double d8 = d6;
        double d9 = 0.0;
        double d10 = 0.0;
        double d11 = 0.0;
        double d12 = 0.0;
        Rectangle2D rectangle2D = this.getViewport();
        if (rectangle2D != null) {
            d9 = rectangle2D.getWidth();
            d10 = rectangle2D.getHeight();
            d11 = rectangle2D.getMinX();
            d12 = rectangle2D.getMinY();
        }
        if (d9 > 0.0 && d10 > 0.0) {
            d7 = d9;
            d8 = d10;
        }
        d3 = d11 + d3 * d7 / this.destWidth;
        d4 = d12 + d4 * d8 / this.destHeight;
        if (d3 < 0.0 || d4 < 0.0 || d3 >= d5 || d4 >= d6 || d3 < d11 || d4 < d12 || d3 >= d11 + d7 || d4 >= d12 + d8) {
            return false;
        }
        return Toolkit.getToolkit().imageContains(Toolkit.getImageAccessor().getPlatformImage(image), (float)d3, (float)d4);
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return ImageView.getClassCssMetaData();
    }

    void updateViewport() {
        this.recomputeWidthHeight();
        if (this.getImage() == null || Toolkit.getImageAccessor().getPlatformImage(this.getImage()) == null) {
            return;
        }
        Rectangle2D rectangle2D = this.getViewport();
        NGImageView nGImageView = (NGImageView)NodeHelper.getPeer(this);
        if (rectangle2D != null) {
            nGImageView.setViewport((float)rectangle2D.getMinX(), (float)rectangle2D.getMinY(), (float)rectangle2D.getWidth(), (float)rectangle2D.getHeight(), (float)this.destWidth, (float)this.destHeight);
        } else {
            nGImageView.setViewport(0.0f, 0.0f, 0.0f, 0.0f, (float)this.destWidth, (float)this.destHeight);
        }
    }

    private void doUpdatePeer() {
        NGImageView nGImageView = (NGImageView)NodeHelper.getPeer(this);
        if (NodeHelper.isDirty(this, DirtyBits.NODE_GEOMETRY)) {
            nGImageView.setX((float)this.getX());
            nGImageView.setY((float)this.getY());
        }
        if (NodeHelper.isDirty(this, DirtyBits.NODE_SMOOTH)) {
            nGImageView.setSmooth(this.isSmooth());
        }
        if (NodeHelper.isDirty(this, DirtyBits.NODE_CONTENTS)) {
            nGImageView.setImage(this.getImage() != null ? Toolkit.getImageAccessor().getPlatformImage(this.getImage()) : null);
        }
        if (NodeHelper.isDirty(this, DirtyBits.NODE_VIEWPORT) || NodeHelper.isDirty(this, DirtyBits.NODE_CONTENTS)) {
            this.updateViewport();
        }
    }

    static {
        ImageViewHelper.setImageViewAccessor(new ImageViewHelper.ImageViewAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((ImageView)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((ImageView)node).doUpdatePeer();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((ImageView)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public boolean doComputeContains(Node node, double d, double d2) {
                return ((ImageView)node).doComputeContains(d, d2);
            }
        });
        SMOOTH_DEFAULT = Toolkit.getToolkit().getDefaultImageSmooth();
    }

    private static class StyleableProperties {
        private static final CssMetaData<ImageView, String> IMAGE = new CssMetaData<ImageView, String>("-fx-image", URLConverter.getInstance()){

            @Override
            public boolean isSettable(ImageView imageView) {
                return imageView.image == null || !imageView.image.isBound();
            }

            @Override
            public StyleableProperty<String> getStyleableProperty(ImageView imageView) {
                return (StyleableProperty)((Object)imageView.imageUrlProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Node.getClassCssMetaData());
            arrayList.add(IMAGE);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }
}

