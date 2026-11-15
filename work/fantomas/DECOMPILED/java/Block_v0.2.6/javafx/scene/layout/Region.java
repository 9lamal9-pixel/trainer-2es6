/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import com.sun.javafx.binding.ExpressionHelper;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.PickRay;
import com.sun.javafx.geom.RectBounds;
import com.sun.javafx.geom.Shape;
import com.sun.javafx.geom.Vec2d;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.scene.DirtyBits;
import com.sun.javafx.scene.NodeHelper;
import com.sun.javafx.scene.ParentHelper;
import com.sun.javafx.scene.input.PickResultChooser;
import com.sun.javafx.scene.layout.RegionHelper;
import com.sun.javafx.scene.shape.ShapeHelper;
import com.sun.javafx.sg.prism.NGNode;
import com.sun.javafx.sg.prism.NGRegion;
import com.sun.javafx.tk.Toolkit;
import com.sun.javafx.util.Logging;
import com.sun.javafx.util.TempState;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectPropertyBase;
import javafx.beans.value.ChangeListener;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.InsetsConverter;
import javafx.css.converter.ShapeConverter;
import javafx.css.converter.SizeConverter;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundConverter;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderConverter;
import javafx.scene.layout.BorderImage;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.shape.StrokeType;
import javafx.stage.Window;
import javafx.util.Callback;

public class Region
extends Parent {
    public static final double USE_PREF_SIZE = Double.NEGATIVE_INFINITY;
    public static final double USE_COMPUTED_SIZE = -1.0;
    static Vec2d TEMP_VEC2D;
    private InvalidationListener imageChangeListener = observable -> {
        ReadOnlyObjectPropertyBase readOnlyObjectPropertyBase = (ReadOnlyObjectPropertyBase)observable;
        Image image = (Image)readOnlyObjectPropertyBase.getBean();
        Toolkit.ImageAccessor imageAccessor = Toolkit.getImageAccessor();
        if (image.getProgress() == 1.0 && !imageAccessor.isAnimation(image)) {
            this.removeImageListener(image);
        }
        NodeHelper.markDirty(this, DirtyBits.NODE_CONTENTS);
    };
    private BooleanProperty snapToPixel;
    private boolean _snapToPixel;
    private ObjectProperty<Insets> padding;
    private final ObjectProperty<Background> background;
    private final ObjectProperty<Border> border;
    private ObjectProperty<Insets> opaqueInsets;
    private final InsetsProperty insets;
    private double snappedTopInset;
    private double snappedRightInset;
    private double snappedBottomInset;
    private double snappedLeftInset;
    private ReadOnlyDoubleWrapper width;
    private double _width;
    private ReadOnlyDoubleWrapper height;
    private double _height;
    private DoubleProperty minWidth;
    private double _minWidth;
    private DoubleProperty minHeight;
    private double _minHeight;
    private DoubleProperty prefWidth;
    private double _prefWidth;
    private DoubleProperty prefHeight;
    private double _prefHeight;
    private DoubleProperty maxWidth;
    private double _maxWidth;
    private DoubleProperty maxHeight;
    private double _maxHeight;
    private ObjectProperty<javafx.scene.shape.Shape> shape;
    private javafx.scene.shape.Shape _shape;
    private BooleanProperty scaleShape;
    private BooleanProperty centerShape;
    private BooleanProperty cacheShape;
    private boolean cornersValid;
    private List<CornerRadii> normalizedFillCorners;
    private List<CornerRadii> normalizedStrokeCorners;
    private Bounds boundingBox;

    static double boundedSize(double d, double d2, double d3) {
        double d4 = d2 >= d ? d2 : d;
        double d5 = d >= d3 ? d : d3;
        return d4 <= d5 ? d4 : d5;
    }

    double adjustWidthByMargin(double d, Insets insets) {
        if (insets == null || insets == Insets.EMPTY) {
            return d;
        }
        boolean bl = this.isSnapToPixel();
        return d - this.snapSpaceX(insets.getLeft(), bl) - this.snapSpaceX(insets.getRight(), bl);
    }

    double adjustHeightByMargin(double d, Insets insets) {
        if (insets == null || insets == Insets.EMPTY) {
            return d;
        }
        boolean bl = this.isSnapToPixel();
        return d - this.snapSpaceY(insets.getTop(), bl) - this.snapSpaceY(insets.getBottom(), bl);
    }

    private static double getSnapScaleX(Node node) {
        return Region._getSnapScaleXimpl(node.getScene());
    }

    private static double _getSnapScaleXimpl(Scene scene) {
        if (scene == null) {
            return 1.0;
        }
        Window window = scene.getWindow();
        if (window == null) {
            return 1.0;
        }
        return window.getRenderScaleX();
    }

    private static double getSnapScaleY(Node node) {
        return Region._getSnapScaleYimpl(node.getScene());
    }

    private static double _getSnapScaleYimpl(Scene scene) {
        if (scene == null) {
            return 1.0;
        }
        Window window = scene.getWindow();
        if (window == null) {
            return 1.0;
        }
        return window.getRenderScaleY();
    }

    private double getSnapScaleX() {
        return Region._getSnapScaleXimpl(this.getScene());
    }

    private double getSnapScaleY() {
        return Region._getSnapScaleYimpl(this.getScene());
    }

    private static double scaledRound(double d, double d2) {
        return (double)Math.round(d * d2) / d2;
    }

    private static double scaledFloor(double d, double d2) {
        return Math.floor(d * d2) / d2;
    }

    private static double scaledCeil(double d, double d2) {
        return Math.ceil(d * d2) / d2;
    }

    private double snapSpaceX(double d, boolean bl) {
        return bl ? Region.scaledRound(d, this.getSnapScaleX()) : d;
    }

    private double snapSpaceY(double d, boolean bl) {
        return bl ? Region.scaledRound(d, this.getSnapScaleY()) : d;
    }

    private static double snapSpace(double d, boolean bl, double d2) {
        return bl ? Region.scaledRound(d, d2) : d;
    }

    private double snapSizeX(double d, boolean bl) {
        return bl ? Region.scaledCeil(d, this.getSnapScaleX()) : d;
    }

    private double snapSizeY(double d, boolean bl) {
        return bl ? Region.scaledCeil(d, this.getSnapScaleY()) : d;
    }

    private static double snapSize(double d, boolean bl, double d2) {
        return bl ? Region.scaledCeil(d, d2) : d;
    }

    private double snapPositionX(double d, boolean bl) {
        return bl ? Region.scaledRound(d, this.getSnapScaleX()) : d;
    }

    private double snapPositionY(double d, boolean bl) {
        return bl ? Region.scaledRound(d, this.getSnapScaleY()) : d;
    }

    private static double snapPosition(double d, boolean bl, double d2) {
        return bl ? Region.scaledRound(d, d2) : d;
    }

    private double snapPortionX(double d, boolean bl) {
        if (!bl || d == 0.0) {
            return d;
        }
        double d2 = this.getSnapScaleX();
        d = (d *= d2) > 0.0 ? Math.max(1.0, Math.floor(d)) : Math.min(-1.0, Math.ceil(d));
        return d / d2;
    }

    private double snapPortionY(double d, boolean bl) {
        if (!bl || d == 0.0) {
            return d;
        }
        double d2 = this.getSnapScaleY();
        d = (d *= d2) > 0.0 ? Math.max(1.0, Math.floor(d)) : Math.min(-1.0, Math.ceil(d));
        return d / d2;
    }

    double getAreaBaselineOffset(List<Node> list, Callback<Node, Insets> callback, Function<Integer, Double> function, double d, boolean bl) {
        return Region.getAreaBaselineOffset(list, callback, function, d, bl, this.isSnapToPixel());
    }

    static double getAreaBaselineOffset(List<Node> list, Callback<Node, Insets> callback, Function<Integer, Double> function, double d, boolean bl, boolean bl2) {
        return Region.getAreaBaselineOffset(list, callback, function, d, bl, Region.getMinBaselineComplement(list), bl2);
    }

    double getAreaBaselineOffset(List<Node> list, Callback<Node, Insets> callback, Function<Integer, Double> function, double d, boolean bl, double d2) {
        return Region.getAreaBaselineOffset(list, callback, function, d, bl, d2, this.isSnapToPixel());
    }

    static double getAreaBaselineOffset(List<Node> list, Callback<Node, Insets> callback, Function<Integer, Double> function, double d, boolean bl, double d2, boolean bl2) {
        return Region.getAreaBaselineOffset(list, callback, function, d, n -> bl, d2, bl2);
    }

    double getAreaBaselineOffset(List<Node> list, Callback<Node, Insets> callback, Function<Integer, Double> function, double d, Function<Integer, Boolean> function2, double d2) {
        return Region.getAreaBaselineOffset(list, callback, function, d, function2, d2, this.isSnapToPixel());
    }

    static double getAreaBaselineOffset(List<Node> list, Callback<Node, Insets> callback, Function<Integer, Double> function, double d, Function<Integer, Boolean> function2, double d2, boolean bl) {
        double d3 = 0.0;
        double d4 = 0.0;
        for (int i = 0; i < list.size(); ++i) {
            Insets insets;
            Node node = list.get(i);
            if (bl && i == 0) {
                d4 = Region.getSnapScaleY(node.getParent());
            }
            double d5 = (insets = callback.call(node)) != null ? Region.snapSpace(insets.getTop(), bl, d4) : 0.0;
            double d6 = insets != null ? Region.snapSpace(insets.getBottom(), bl, d4) : 0.0;
            double d7 = node.getBaselineOffset();
            if (d7 == Double.NEGATIVE_INFINITY) {
                double d8 = -1.0;
                if (node.getContentBias() == Orientation.HORIZONTAL) {
                    d8 = function.apply(i);
                }
                if (function2.apply(i).booleanValue()) {
                    d3 = Math.max(d3, d5 + Region.boundedSize(node.minHeight(d8), d - d2 - d5 - d6, node.maxHeight(d8)));
                    continue;
                }
                d3 = Math.max(d3, d5 + Region.boundedSize(node.minHeight(d8), node.prefHeight(d8), Math.min(node.maxHeight(d8), d - d2 - d5 - d6)));
                continue;
            }
            d3 = Math.max(d3, d5 + d7);
        }
        return d3;
    }

    static double getMinBaselineComplement(List<Node> list) {
        return Region.getBaselineComplement(list, true, false);
    }

    static double getPrefBaselineComplement(List<Node> list) {
        return Region.getBaselineComplement(list, false, false);
    }

    static double getMaxBaselineComplement(List<Node> list) {
        return Region.getBaselineComplement(list, false, true);
    }

    private static double getBaselineComplement(List<Node> list, boolean bl, boolean bl2) {
        double d = 0.0;
        for (Node node : list) {
            double d2 = node.getBaselineOffset();
            if (d2 == Double.NEGATIVE_INFINITY) continue;
            if (node.isResizable()) {
                d = Math.max(d, (bl ? node.minHeight(-1.0) : (bl2 ? node.maxHeight(-1.0) : node.prefHeight(-1.0))) - d2);
                continue;
            }
            d = Math.max(d, node.getLayoutBounds().getHeight() - d2);
        }
        return d;
    }

    static double computeXOffset(double d, double d2, HPos hPos) {
        switch (hPos) {
            case LEFT: {
                return 0.0;
            }
            case CENTER: {
                return (d - d2) / 2.0;
            }
            case RIGHT: {
                return d - d2;
            }
        }
        throw new AssertionError((Object)"Unhandled hPos");
    }

    static double computeYOffset(double d, double d2, VPos vPos) {
        switch (vPos) {
            case BASELINE: 
            case TOP: {
                return 0.0;
            }
            case CENTER: {
                return (d - d2) / 2.0;
            }
            case BOTTOM: {
                return d - d2;
            }
        }
        throw new AssertionError((Object)"Unhandled vPos");
    }

    static double[] createDoubleArray(int n, double d) {
        double[] dArray = new double[n];
        for (int i = 0; i < n; ++i) {
            dArray[i] = d;
        }
        return dArray;
    }

    public Region() {
        RegionHelper.initHelper(this);
        this._snapToPixel = true;
        this.padding = new StyleableObjectProperty<Insets>(Insets.EMPTY){
            private Insets lastValidValue;
            {
                this.lastValidValue = Insets.EMPTY;
            }

            @Override
            public Object getBean() {
                return Region.this;
            }

            @Override
            public String getName() {
                return "padding";
            }

            @Override
            public CssMetaData<Region, Insets> getCssMetaData() {
                return StyleableProperties.PADDING;
            }

            @Override
            public void invalidated() {
                Insets insets = (Insets)this.get();
                if (insets == null) {
                    if (this.isBound()) {
                        this.unbind();
                    }
                    this.set(this.lastValidValue);
                    throw new NullPointerException("cannot set padding to null");
                }
                if (!insets.equals(this.lastValidValue)) {
                    this.lastValidValue = insets;
                    Region.this.insets.fireValueChanged();
                }
            }
        };
        this.background = new StyleableObjectProperty<Background>(null){
            private Background old;
            {
                this.old = null;
            }

            @Override
            public Object getBean() {
                return Region.this;
            }

            @Override
            public String getName() {
                return "background";
            }

            @Override
            public CssMetaData<Region, Background> getCssMetaData() {
                return StyleableProperties.BACKGROUND;
            }

            @Override
            protected void invalidated() {
                Background background = (Background)this.get();
                if (this.old != null ? !this.old.equals(background) : background != null) {
                    if (this.old == null || background == null || !this.old.getOutsets().equals(background.getOutsets())) {
                        NodeHelper.geomChanged(Region.this);
                        Region.this.insets.fireValueChanged();
                    }
                    if (background != null) {
                        for (BackgroundImage backgroundImage : background.getImages()) {
                            Image image = backgroundImage.image;
                            Toolkit.ImageAccessor imageAccessor = Toolkit.getImageAccessor();
                            if (!imageAccessor.isAnimation(image) && !(image.getProgress() < 1.0)) continue;
                            Region.this.addImageListener(image);
                        }
                    }
                    if (this.old != null) {
                        for (BackgroundImage backgroundImage : this.old.getImages()) {
                            Region.this.removeImageListener(backgroundImage.image);
                        }
                    }
                    NodeHelper.markDirty(Region.this, DirtyBits.SHAPE_FILL);
                    Region.this.cornersValid = false;
                    this.old = background;
                }
            }
        };
        this.border = new StyleableObjectProperty<Border>(null){
            private Border old;
            {
                this.old = null;
            }

            @Override
            public Object getBean() {
                return Region.this;
            }

            @Override
            public String getName() {
                return "border";
            }

            @Override
            public CssMetaData<Region, Border> getCssMetaData() {
                return StyleableProperties.BORDER;
            }

            @Override
            protected void invalidated() {
                Border border = (Border)this.get();
                if (this.old != null ? !this.old.equals(border) : border != null) {
                    if (this.old == null || border == null || !this.old.getOutsets().equals(border.getOutsets())) {
                        NodeHelper.geomChanged(Region.this);
                    }
                    if (this.old == null || border == null || !this.old.getInsets().equals(border.getInsets())) {
                        Region.this.insets.fireValueChanged();
                    }
                    if (border != null) {
                        for (BorderImage borderImage : border.getImages()) {
                            Image image = borderImage.image;
                            Toolkit.ImageAccessor imageAccessor = Toolkit.getImageAccessor();
                            if (!imageAccessor.isAnimation(image) && !(image.getProgress() < 1.0)) continue;
                            Region.this.addImageListener(image);
                        }
                    }
                    if (this.old != null) {
                        for (BorderImage borderImage : this.old.getImages()) {
                            Region.this.removeImageListener(borderImage.image);
                        }
                    }
                    NodeHelper.markDirty(Region.this, DirtyBits.SHAPE_STROKE);
                    Region.this.cornersValid = false;
                    this.old = border;
                }
            }
        };
        this.insets = new InsetsProperty();
        this.snappedTopInset = 0.0;
        this.snappedRightInset = 0.0;
        this.snappedBottomInset = 0.0;
        this.snappedLeftInset = 0.0;
        this._minWidth = -1.0;
        this._minHeight = -1.0;
        this._prefWidth = -1.0;
        this._prefHeight = -1.0;
        this._maxWidth = -1.0;
        this._maxHeight = -1.0;
        this.shape = null;
        this.scaleShape = null;
        this.centerShape = null;
        this.cacheShape = null;
        this.setPickOnBounds(true);
    }

    public final boolean isSnapToPixel() {
        return this._snapToPixel;
    }

    public final void setSnapToPixel(boolean bl) {
        if (this.snapToPixel == null) {
            if (this._snapToPixel != bl) {
                this._snapToPixel = bl;
                this.updateSnappedInsets();
                this.requestParentLayout();
            }
        } else {
            this.snapToPixel.set(bl);
        }
    }

    public final BooleanProperty snapToPixelProperty() {
        if (this.snapToPixel == null) {
            this.snapToPixel = new StyleableBooleanProperty(this._snapToPixel){

                @Override
                public Object getBean() {
                    return Region.this;
                }

                @Override
                public String getName() {
                    return "snapToPixel";
                }

                @Override
                public CssMetaData<Region, Boolean> getCssMetaData() {
                    return StyleableProperties.SNAP_TO_PIXEL;
                }

                @Override
                public void invalidated() {
                    boolean bl = this.get();
                    if (Region.this._snapToPixel != bl) {
                        Region.this._snapToPixel = bl;
                        Region.this.updateSnappedInsets();
                        Region.this.requestParentLayout();
                    }
                }
            };
        }
        return this.snapToPixel;
    }

    public final void setPadding(Insets insets) {
        this.padding.set(insets);
    }

    public final Insets getPadding() {
        return (Insets)this.padding.get();
    }

    public final ObjectProperty<Insets> paddingProperty() {
        return this.padding;
    }

    public final void setBackground(Background background) {
        this.background.set(background);
    }

    public final Background getBackground() {
        return (Background)this.background.get();
    }

    public final ObjectProperty<Background> backgroundProperty() {
        return this.background;
    }

    public final void setBorder(Border border) {
        this.border.set(border);
    }

    public final Border getBorder() {
        return (Border)this.border.get();
    }

    public final ObjectProperty<Border> borderProperty() {
        return this.border;
    }

    void addImageListener(Image image) {
        Toolkit.ImageAccessor imageAccessor = Toolkit.getImageAccessor();
        imageAccessor.getImageProperty(image).addListener(this.imageChangeListener);
    }

    void removeImageListener(Image image) {
        Toolkit.ImageAccessor imageAccessor = Toolkit.getImageAccessor();
        imageAccessor.getImageProperty(image).removeListener(this.imageChangeListener);
    }

    public final ObjectProperty<Insets> opaqueInsetsProperty() {
        if (this.opaqueInsets == null) {
            this.opaqueInsets = new StyleableObjectProperty<Insets>(){

                @Override
                public Object getBean() {
                    return Region.this;
                }

                @Override
                public String getName() {
                    return "opaqueInsets";
                }

                @Override
                public CssMetaData<Region, Insets> getCssMetaData() {
                    return StyleableProperties.OPAQUE_INSETS;
                }

                @Override
                protected void invalidated() {
                    NodeHelper.markDirty(Region.this, DirtyBits.SHAPE_FILL);
                }
            };
        }
        return this.opaqueInsets;
    }

    public final void setOpaqueInsets(Insets insets) {
        this.opaqueInsetsProperty().set(insets);
    }

    public final Insets getOpaqueInsets() {
        return this.opaqueInsets == null ? null : (Insets)this.opaqueInsets.get();
    }

    public final Insets getInsets() {
        return this.insets.get();
    }

    public final ReadOnlyObjectProperty<Insets> insetsProperty() {
        return this.insets;
    }

    private void updateSnappedInsets() {
        Insets insets = this.getInsets();
        if (this._snapToPixel) {
            this.snappedTopInset = Math.ceil(insets.getTop());
            this.snappedRightInset = Math.ceil(insets.getRight());
            this.snappedBottomInset = Math.ceil(insets.getBottom());
            this.snappedLeftInset = Math.ceil(insets.getLeft());
        } else {
            this.snappedTopInset = insets.getTop();
            this.snappedRightInset = insets.getRight();
            this.snappedBottomInset = insets.getBottom();
            this.snappedLeftInset = insets.getLeft();
        }
    }

    protected void setWidth(double d) {
        if (this.width == null) {
            this.widthChanged(d);
        } else {
            this.width.set(d);
        }
    }

    private void widthChanged(double d) {
        if (d != this._width) {
            this._width = d;
            this.cornersValid = false;
            this.boundingBox = null;
            NodeHelper.layoutBoundsChanged(this);
            NodeHelper.geomChanged(this);
            NodeHelper.markDirty(this, DirtyBits.NODE_GEOMETRY);
            this.setNeedsLayout(true);
            this.requestParentLayout();
        }
    }

    public final double getWidth() {
        return this.width == null ? this._width : this.width.get();
    }

    public final ReadOnlyDoubleProperty widthProperty() {
        if (this.width == null) {
            this.width = new ReadOnlyDoubleWrapper(this._width){

                @Override
                protected void invalidated() {
                    Region.this.widthChanged(this.get());
                }

                @Override
                public Object getBean() {
                    return Region.this;
                }

                @Override
                public String getName() {
                    return "width";
                }
            };
        }
        return this.width.getReadOnlyProperty();
    }

    protected void setHeight(double d) {
        if (this.height == null) {
            this.heightChanged(d);
        } else {
            this.height.set(d);
        }
    }

    private void heightChanged(double d) {
        if (this._height != d) {
            this._height = d;
            this.cornersValid = false;
            this.boundingBox = null;
            NodeHelper.geomChanged(this);
            NodeHelper.layoutBoundsChanged(this);
            NodeHelper.markDirty(this, DirtyBits.NODE_GEOMETRY);
            this.setNeedsLayout(true);
            this.requestParentLayout();
        }
    }

    public final double getHeight() {
        return this.height == null ? this._height : this.height.get();
    }

    public final ReadOnlyDoubleProperty heightProperty() {
        if (this.height == null) {
            this.height = new ReadOnlyDoubleWrapper(this._height){

                @Override
                protected void invalidated() {
                    Region.this.heightChanged(this.get());
                }

                @Override
                public Object getBean() {
                    return Region.this;
                }

                @Override
                public String getName() {
                    return "height";
                }
            };
        }
        return this.height.getReadOnlyProperty();
    }

    public final void setMinWidth(double d) {
        if (this.minWidth == null) {
            this._minWidth = d;
            this.requestParentLayout();
        } else {
            this.minWidth.set(d);
        }
    }

    public final double getMinWidth() {
        return this.minWidth == null ? this._minWidth : this.minWidth.get();
    }

    public final DoubleProperty minWidthProperty() {
        if (this.minWidth == null) {
            this.minWidth = new MinPrefMaxProperty("minWidth", this._minWidth, StyleableProperties.MIN_WIDTH);
        }
        return this.minWidth;
    }

    public final void setMinHeight(double d) {
        if (this.minHeight == null) {
            this._minHeight = d;
            this.requestParentLayout();
        } else {
            this.minHeight.set(d);
        }
    }

    public final double getMinHeight() {
        return this.minHeight == null ? this._minHeight : this.minHeight.get();
    }

    public final DoubleProperty minHeightProperty() {
        if (this.minHeight == null) {
            this.minHeight = new MinPrefMaxProperty("minHeight", this._minHeight, StyleableProperties.MIN_HEIGHT);
        }
        return this.minHeight;
    }

    public void setMinSize(double d, double d2) {
        this.setMinWidth(d);
        this.setMinHeight(d2);
    }

    public final void setPrefWidth(double d) {
        if (this.prefWidth == null) {
            this._prefWidth = d;
            this.requestParentLayout();
        } else {
            this.prefWidth.set(d);
        }
    }

    public final double getPrefWidth() {
        return this.prefWidth == null ? this._prefWidth : this.prefWidth.get();
    }

    public final DoubleProperty prefWidthProperty() {
        if (this.prefWidth == null) {
            this.prefWidth = new MinPrefMaxProperty("prefWidth", this._prefWidth, StyleableProperties.PREF_WIDTH);
        }
        return this.prefWidth;
    }

    public final void setPrefHeight(double d) {
        if (this.prefHeight == null) {
            this._prefHeight = d;
            this.requestParentLayout();
        } else {
            this.prefHeight.set(d);
        }
    }

    public final double getPrefHeight() {
        return this.prefHeight == null ? this._prefHeight : this.prefHeight.get();
    }

    public final DoubleProperty prefHeightProperty() {
        if (this.prefHeight == null) {
            this.prefHeight = new MinPrefMaxProperty("prefHeight", this._prefHeight, StyleableProperties.PREF_HEIGHT);
        }
        return this.prefHeight;
    }

    public void setPrefSize(double d, double d2) {
        this.setPrefWidth(d);
        this.setPrefHeight(d2);
    }

    public final void setMaxWidth(double d) {
        if (this.maxWidth == null) {
            this._maxWidth = d;
            this.requestParentLayout();
        } else {
            this.maxWidth.set(d);
        }
    }

    public final double getMaxWidth() {
        return this.maxWidth == null ? this._maxWidth : this.maxWidth.get();
    }

    public final DoubleProperty maxWidthProperty() {
        if (this.maxWidth == null) {
            this.maxWidth = new MinPrefMaxProperty("maxWidth", this._maxWidth, StyleableProperties.MAX_WIDTH);
        }
        return this.maxWidth;
    }

    public final void setMaxHeight(double d) {
        if (this.maxHeight == null) {
            this._maxHeight = d;
            this.requestParentLayout();
        } else {
            this.maxHeight.set(d);
        }
    }

    public final double getMaxHeight() {
        return this.maxHeight == null ? this._maxHeight : this.maxHeight.get();
    }

    public final DoubleProperty maxHeightProperty() {
        if (this.maxHeight == null) {
            this.maxHeight = new MinPrefMaxProperty("maxHeight", this._maxHeight, StyleableProperties.MAX_HEIGHT);
        }
        return this.maxHeight;
    }

    public void setMaxSize(double d, double d2) {
        this.setMaxWidth(d);
        this.setMaxHeight(d2);
    }

    public final javafx.scene.shape.Shape getShape() {
        return this.shape == null ? this._shape : (javafx.scene.shape.Shape)this.shape.get();
    }

    public final void setShape(javafx.scene.shape.Shape shape) {
        this.shapeProperty().set(shape);
    }

    public final ObjectProperty<javafx.scene.shape.Shape> shapeProperty() {
        if (this.shape == null) {
            this.shape = new ShapeProperty();
        }
        return this.shape;
    }

    public final void setScaleShape(boolean bl) {
        this.scaleShapeProperty().set(bl);
    }

    public final boolean isScaleShape() {
        return this.scaleShape == null ? true : this.scaleShape.get();
    }

    public final BooleanProperty scaleShapeProperty() {
        if (this.scaleShape == null) {
            this.scaleShape = new StyleableBooleanProperty(true){

                @Override
                public Object getBean() {
                    return Region.this;
                }

                @Override
                public String getName() {
                    return "scaleShape";
                }

                @Override
                public CssMetaData<Region, Boolean> getCssMetaData() {
                    return StyleableProperties.SCALE_SHAPE;
                }

                @Override
                public void invalidated() {
                    NodeHelper.geomChanged(Region.this);
                    NodeHelper.markDirty(Region.this, DirtyBits.REGION_SHAPE);
                }
            };
        }
        return this.scaleShape;
    }

    public final void setCenterShape(boolean bl) {
        this.centerShapeProperty().set(bl);
    }

    public final boolean isCenterShape() {
        return this.centerShape == null ? true : this.centerShape.get();
    }

    public final BooleanProperty centerShapeProperty() {
        if (this.centerShape == null) {
            this.centerShape = new StyleableBooleanProperty(true){

                @Override
                public Object getBean() {
                    return Region.this;
                }

                @Override
                public String getName() {
                    return "centerShape";
                }

                @Override
                public CssMetaData<Region, Boolean> getCssMetaData() {
                    return StyleableProperties.POSITION_SHAPE;
                }

                @Override
                public void invalidated() {
                    NodeHelper.geomChanged(Region.this);
                    NodeHelper.markDirty(Region.this, DirtyBits.REGION_SHAPE);
                }
            };
        }
        return this.centerShape;
    }

    public final void setCacheShape(boolean bl) {
        this.cacheShapeProperty().set(bl);
    }

    public final boolean isCacheShape() {
        return this.cacheShape == null ? true : this.cacheShape.get();
    }

    public final BooleanProperty cacheShapeProperty() {
        if (this.cacheShape == null) {
            this.cacheShape = new StyleableBooleanProperty(true){

                @Override
                public Object getBean() {
                    return Region.this;
                }

                @Override
                public String getName() {
                    return "cacheShape";
                }

                @Override
                public CssMetaData<Region, Boolean> getCssMetaData() {
                    return StyleableProperties.CACHE_SHAPE;
                }
            };
        }
        return this.cacheShape;
    }

    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public void resize(double d, double d2) {
        this.setWidth(d);
        this.setHeight(d2);
        PlatformLogger platformLogger = Logging.getLayoutLogger();
        if (platformLogger.isLoggable(PlatformLogger.Level.FINER)) {
            platformLogger.finer(this.toString() + " resized to " + d + " x " + d2);
        }
    }

    @Override
    public final double minWidth(double d) {
        double d2 = this.getMinWidth();
        if (d2 == -1.0) {
            return super.minWidth(d);
        }
        if (d2 == Double.NEGATIVE_INFINITY) {
            return this.prefWidth(d);
        }
        return Double.isNaN(d2) || d2 < 0.0 ? 0.0 : d2;
    }

    @Override
    public final double minHeight(double d) {
        double d2 = this.getMinHeight();
        if (d2 == -1.0) {
            return super.minHeight(d);
        }
        if (d2 == Double.NEGATIVE_INFINITY) {
            return this.prefHeight(d);
        }
        return Double.isNaN(d2) || d2 < 0.0 ? 0.0 : d2;
    }

    @Override
    public final double prefWidth(double d) {
        double d2 = this.getPrefWidth();
        if (d2 == -1.0) {
            return super.prefWidth(d);
        }
        return Double.isNaN(d2) || d2 < 0.0 ? 0.0 : d2;
    }

    @Override
    public final double prefHeight(double d) {
        double d2 = this.getPrefHeight();
        if (d2 == -1.0) {
            return super.prefHeight(d);
        }
        return Double.isNaN(d2) || d2 < 0.0 ? 0.0 : d2;
    }

    @Override
    public final double maxWidth(double d) {
        double d2 = this.getMaxWidth();
        if (d2 == -1.0) {
            return this.computeMaxWidth(d);
        }
        if (d2 == Double.NEGATIVE_INFINITY) {
            return this.prefWidth(d);
        }
        return Double.isNaN(d2) || d2 < 0.0 ? 0.0 : d2;
    }

    @Override
    public final double maxHeight(double d) {
        double d2 = this.getMaxHeight();
        if (d2 == -1.0) {
            return this.computeMaxHeight(d);
        }
        if (d2 == Double.NEGATIVE_INFINITY) {
            return this.prefHeight(d);
        }
        return Double.isNaN(d2) || d2 < 0.0 ? 0.0 : d2;
    }

    @Override
    protected double computeMinWidth(double d) {
        return this.getInsets().getLeft() + this.getInsets().getRight();
    }

    @Override
    protected double computeMinHeight(double d) {
        return this.getInsets().getTop() + this.getInsets().getBottom();
    }

    @Override
    protected double computePrefWidth(double d) {
        double d2 = super.computePrefWidth(d);
        return this.getInsets().getLeft() + d2 + this.getInsets().getRight();
    }

    @Override
    protected double computePrefHeight(double d) {
        double d2 = super.computePrefHeight(d);
        return this.getInsets().getTop() + d2 + this.getInsets().getBottom();
    }

    protected double computeMaxWidth(double d) {
        return Double.MAX_VALUE;
    }

    protected double computeMaxHeight(double d) {
        return Double.MAX_VALUE;
    }

    @Deprecated(since="9")
    protected double snapSpace(double d) {
        return this.snapSpaceX(d, this.isSnapToPixel());
    }

    public double snapSpaceX(double d) {
        return this.snapSpaceX(d, this.isSnapToPixel());
    }

    public double snapSpaceY(double d) {
        return this.snapSpaceY(d, this.isSnapToPixel());
    }

    @Deprecated(since="9")
    protected double snapSize(double d) {
        return this.snapSizeX(d, this.isSnapToPixel());
    }

    public double snapSizeX(double d) {
        return this.snapSizeX(d, this.isSnapToPixel());
    }

    public double snapSizeY(double d) {
        return this.snapSizeY(d, this.isSnapToPixel());
    }

    @Deprecated(since="9")
    protected double snapPosition(double d) {
        return this.snapPositionX(d, this.isSnapToPixel());
    }

    public double snapPositionX(double d) {
        return this.snapPositionX(d, this.isSnapToPixel());
    }

    public double snapPositionY(double d) {
        return this.snapPositionY(d, this.isSnapToPixel());
    }

    double snapPortionX(double d) {
        return this.snapPortionX(d, this.isSnapToPixel());
    }

    double snapPortionY(double d) {
        return this.snapPortionY(d, this.isSnapToPixel());
    }

    public final double snappedTopInset() {
        return this.snappedTopInset;
    }

    public final double snappedBottomInset() {
        return this.snappedBottomInset;
    }

    public final double snappedLeftInset() {
        return this.snappedLeftInset;
    }

    public final double snappedRightInset() {
        return this.snappedRightInset;
    }

    double computeChildMinAreaWidth(Node node, Insets insets) {
        return this.computeChildMinAreaWidth(node, -1.0, insets, -1.0, false);
    }

    double computeChildMinAreaWidth(Node node, double d, Insets insets, double d2, boolean bl) {
        boolean bl2 = this.isSnapToPixel();
        double d3 = insets != null ? this.snapSpaceX(insets.getLeft(), bl2) : 0.0;
        double d4 = insets != null ? this.snapSpaceX(insets.getRight(), bl2) : 0.0;
        double d5 = -1.0;
        if (d2 != -1.0 && node.isResizable() && node.getContentBias() == Orientation.VERTICAL) {
            double d6 = insets != null ? this.snapSpaceY(insets.getTop(), bl2) : 0.0;
            double d7 = insets != null ? this.snapSpaceY(insets.getBottom(), bl2) : 0.0;
            double d8 = node.getBaselineOffset();
            double d9 = d8 == Double.NEGATIVE_INFINITY && d != -1.0 ? d2 - d6 - d7 - d : d2 - d6 - d7;
            d5 = bl ? this.snapSizeY(Region.boundedSize(node.minHeight(-1.0), d9, node.maxHeight(-1.0))) : this.snapSizeY(Region.boundedSize(node.minHeight(-1.0), node.prefHeight(-1.0), Math.min(node.maxHeight(-1.0), d9)));
        }
        return d3 + this.snapSizeX(node.minWidth(d5)) + d4;
    }

    double computeChildMinAreaHeight(Node node, Insets insets) {
        return this.computeChildMinAreaHeight(node, -1.0, insets, -1.0);
    }

    double computeChildMinAreaHeight(Node node, double d, Insets insets, double d2) {
        double d3;
        boolean bl = this.isSnapToPixel();
        double d4 = insets != null ? this.snapSpaceY(insets.getTop(), bl) : 0.0;
        double d5 = insets != null ? this.snapSpaceY(insets.getBottom(), bl) : 0.0;
        double d6 = -1.0;
        if (node.isResizable() && node.getContentBias() == Orientation.HORIZONTAL) {
            d3 = insets != null ? this.snapSpaceX(insets.getLeft(), bl) : 0.0;
            double d7 = insets != null ? this.snapSpaceX(insets.getRight(), bl) : 0.0;
            d6 = this.snapSizeX(d2 != -1.0 ? Region.boundedSize(node.minWidth(-1.0), d2 - d3 - d7, node.maxWidth(-1.0)) : node.maxWidth(-1.0));
        }
        if (d != -1.0) {
            d3 = node.getBaselineOffset();
            if (node.isResizable() && d3 == Double.NEGATIVE_INFINITY) {
                return d4 + this.snapSizeY(node.minHeight(d6)) + d5 + d;
            }
            return d3 + d;
        }
        return d4 + this.snapSizeY(node.minHeight(d6)) + d5;
    }

    double computeChildPrefAreaWidth(Node node, Insets insets) {
        return this.computeChildPrefAreaWidth(node, -1.0, insets, -1.0, false);
    }

    double computeChildPrefAreaWidth(Node node, double d, Insets insets, double d2, boolean bl) {
        boolean bl2 = this.isSnapToPixel();
        double d3 = insets != null ? this.snapSpaceX(insets.getLeft(), bl2) : 0.0;
        double d4 = insets != null ? this.snapSpaceX(insets.getRight(), bl2) : 0.0;
        double d5 = -1.0;
        if (d2 != -1.0 && node.isResizable() && node.getContentBias() == Orientation.VERTICAL) {
            double d6 = insets != null ? this.snapSpaceY(insets.getTop(), bl2) : 0.0;
            double d7 = insets != null ? this.snapSpaceY(insets.getBottom(), bl2) : 0.0;
            double d8 = node.getBaselineOffset();
            double d9 = d8 == Double.NEGATIVE_INFINITY && d != -1.0 ? d2 - d6 - d7 - d : d2 - d6 - d7;
            d5 = bl ? this.snapSizeY(Region.boundedSize(node.minHeight(-1.0), d9, node.maxHeight(-1.0))) : this.snapSizeY(Region.boundedSize(node.minHeight(-1.0), node.prefHeight(-1.0), Math.min(node.maxHeight(-1.0), d9)));
        }
        return d3 + this.snapSizeX(Region.boundedSize(node.minWidth(d5), node.prefWidth(d5), node.maxWidth(d5))) + d4;
    }

    double computeChildPrefAreaHeight(Node node, Insets insets) {
        return this.computeChildPrefAreaHeight(node, -1.0, insets, -1.0);
    }

    double computeChildPrefAreaHeight(Node node, double d, Insets insets, double d2) {
        double d3;
        boolean bl = this.isSnapToPixel();
        double d4 = insets != null ? this.snapSpaceY(insets.getTop(), bl) : 0.0;
        double d5 = insets != null ? this.snapSpaceY(insets.getBottom(), bl) : 0.0;
        double d6 = -1.0;
        if (node.isResizable() && node.getContentBias() == Orientation.HORIZONTAL) {
            d3 = insets != null ? this.snapSpaceX(insets.getLeft(), bl) : 0.0;
            double d7 = insets != null ? this.snapSpaceX(insets.getRight(), bl) : 0.0;
            d6 = this.snapSizeX(Region.boundedSize(node.minWidth(-1.0), d2 != -1.0 ? d2 - d3 - d7 : node.prefWidth(-1.0), node.maxWidth(-1.0)));
        }
        if (d != -1.0) {
            d3 = node.getBaselineOffset();
            if (node.isResizable() && d3 == Double.NEGATIVE_INFINITY) {
                return d4 + this.snapSizeY(Region.boundedSize(node.minHeight(d6), node.prefHeight(d6), node.maxHeight(d6))) + d5 + d;
            }
            return d4 + d3 + d + d5;
        }
        return d4 + this.snapSizeY(Region.boundedSize(node.minHeight(d6), node.prefHeight(d6), node.maxHeight(d6))) + d5;
    }

    double computeChildMaxAreaWidth(Node node, double d, Insets insets, double d2, boolean bl) {
        double d3 = node.maxWidth(-1.0);
        if (d3 == Double.MAX_VALUE) {
            return d3;
        }
        boolean bl2 = this.isSnapToPixel();
        double d4 = insets != null ? this.snapSpaceX(insets.getLeft(), bl2) : 0.0;
        double d5 = insets != null ? this.snapSpaceX(insets.getRight(), bl2) : 0.0;
        double d6 = -1.0;
        if (d2 != -1.0 && node.isResizable() && node.getContentBias() == Orientation.VERTICAL) {
            double d7 = insets != null ? this.snapSpaceY(insets.getTop(), bl2) : 0.0;
            double d8 = insets != null ? this.snapSpaceY(insets.getBottom(), bl2) : 0.0;
            double d9 = node.getBaselineOffset();
            double d10 = d9 == Double.NEGATIVE_INFINITY && d != -1.0 ? d2 - d7 - d8 - d : d2 - d7 - d8;
            d6 = bl ? this.snapSizeY(Region.boundedSize(node.minHeight(-1.0), d10, node.maxHeight(-1.0))) : this.snapSizeY(Region.boundedSize(node.minHeight(-1.0), node.prefHeight(-1.0), Math.min(node.maxHeight(-1.0), d10)));
            d3 = node.maxWidth(d6);
        }
        return d4 + this.snapSizeX(Region.boundedSize(node.minWidth(d6), d3, Double.MAX_VALUE)) + d5;
    }

    double computeChildMaxAreaHeight(Node node, double d, Insets insets, double d2) {
        double d3;
        double d4 = node.maxHeight(-1.0);
        if (d4 == Double.MAX_VALUE) {
            return d4;
        }
        boolean bl = this.isSnapToPixel();
        double d5 = insets != null ? this.snapSpaceY(insets.getTop(), bl) : 0.0;
        double d6 = insets != null ? this.snapSpaceY(insets.getBottom(), bl) : 0.0;
        double d7 = -1.0;
        if (node.isResizable() && node.getContentBias() == Orientation.HORIZONTAL) {
            d3 = insets != null ? this.snapSpaceX(insets.getLeft(), bl) : 0.0;
            double d8 = insets != null ? this.snapSpaceX(insets.getRight(), bl) : 0.0;
            d7 = this.snapSizeX(d2 != -1.0 ? Region.boundedSize(node.minWidth(-1.0), d2 - d3 - d8, node.maxWidth(-1.0)) : node.minWidth(-1.0));
            d4 = node.maxHeight(d7);
        }
        if (d != -1.0) {
            d3 = node.getBaselineOffset();
            if (node.isResizable() && d3 == Double.NEGATIVE_INFINITY) {
                return d5 + this.snapSizeY(Region.boundedSize(node.minHeight(d7), node.maxHeight(d7), Double.MAX_VALUE)) + d6 + d;
            }
            return d5 + d3 + d + d6;
        }
        return d5 + this.snapSizeY(Region.boundedSize(node.minHeight(d7), d4, Double.MAX_VALUE)) + d6;
    }

    double computeMaxMinAreaWidth(List<Node> list, Callback<Node, Insets> callback) {
        return this.getMaxAreaWidth(list, callback, new double[]{-1.0}, false, true);
    }

    double computeMaxMinAreaWidth(List<Node> list, Callback<Node, Insets> callback, double d, boolean bl) {
        return this.getMaxAreaWidth(list, callback, new double[]{d}, bl, true);
    }

    double computeMaxMinAreaWidth(List<Node> list, Callback<Node, Insets> callback, double[] dArray, boolean bl) {
        return this.getMaxAreaWidth(list, callback, dArray, bl, true);
    }

    double computeMaxMinAreaHeight(List<Node> list, Callback<Node, Insets> callback, VPos vPos) {
        return this.getMaxAreaHeight(list, callback, null, vPos, true);
    }

    double computeMaxMinAreaHeight(List<Node> list, Callback<Node, Insets> callback, VPos vPos, double d) {
        return this.getMaxAreaHeight(list, callback, new double[]{d}, vPos, true);
    }

    double computeMaxMinAreaHeight(List<Node> list, Callback<Node, Insets> callback, double[] dArray, VPos vPos) {
        return this.getMaxAreaHeight(list, callback, dArray, vPos, true);
    }

    double computeMaxPrefAreaWidth(List<Node> list, Callback<Node, Insets> callback) {
        return this.getMaxAreaWidth(list, callback, new double[]{-1.0}, false, false);
    }

    double computeMaxPrefAreaWidth(List<Node> list, Callback<Node, Insets> callback, double d, boolean bl) {
        return this.getMaxAreaWidth(list, callback, new double[]{d}, bl, false);
    }

    double computeMaxPrefAreaWidth(List<Node> list, Callback<Node, Insets> callback, double[] dArray, boolean bl) {
        return this.getMaxAreaWidth(list, callback, dArray, bl, false);
    }

    double computeMaxPrefAreaHeight(List<Node> list, Callback<Node, Insets> callback, VPos vPos) {
        return this.getMaxAreaHeight(list, callback, null, vPos, false);
    }

    double computeMaxPrefAreaHeight(List<Node> list, Callback<Node, Insets> callback, double d, VPos vPos) {
        return this.getMaxAreaHeight(list, callback, new double[]{d}, vPos, false);
    }

    double computeMaxPrefAreaHeight(List<Node> list, Callback<Node, Insets> callback, double[] dArray, VPos vPos) {
        return this.getMaxAreaHeight(list, callback, dArray, vPos, false);
    }

    static Vec2d boundedNodeSizeWithBias(Node node, double d, double d2, boolean bl, boolean bl2, Vec2d vec2d) {
        if (vec2d == null) {
            vec2d = new Vec2d();
        }
        Orientation orientation = node.getContentBias();
        double d3 = 0.0;
        double d4 = 0.0;
        if (orientation == null) {
            d3 = Region.boundedSize(node.minWidth(-1.0), bl ? d : Math.min(d, node.prefWidth(-1.0)), node.maxWidth(-1.0));
            d4 = Region.boundedSize(node.minHeight(-1.0), bl2 ? d2 : Math.min(d2, node.prefHeight(-1.0)), node.maxHeight(-1.0));
        } else if (orientation == Orientation.HORIZONTAL) {
            d3 = Region.boundedSize(node.minWidth(-1.0), bl ? d : Math.min(d, node.prefWidth(-1.0)), node.maxWidth(-1.0));
            d4 = Region.boundedSize(node.minHeight(d3), bl2 ? d2 : Math.min(d2, node.prefHeight(d3)), node.maxHeight(d3));
        } else {
            d4 = Region.boundedSize(node.minHeight(-1.0), bl2 ? d2 : Math.min(d2, node.prefHeight(-1.0)), node.maxHeight(-1.0));
            d3 = Region.boundedSize(node.minWidth(d4), bl ? d : Math.min(d, node.prefWidth(d4)), node.maxWidth(d4));
        }
        vec2d.set(d3, d4);
        return vec2d;
    }

    private double getMaxAreaHeight(List<Node> list, Callback<Node, Insets> callback, double[] dArray, VPos vPos, boolean bl) {
        double d;
        double d2 = dArray == null ? -1.0 : (d = dArray.length == 1 ? dArray[0] : Double.NaN);
        if (vPos == VPos.BASELINE) {
            double d3 = 0.0;
            double d4 = 0.0;
            int n = list.size();
            for (int i = 0; i < n; ++i) {
                double d5;
                Node node = list.get(i);
                double d6 = Double.isNaN(d) ? dArray[i] : d;
                Insets insets = callback.call(node);
                double d7 = insets != null ? this.snapSpaceY(insets.getTop()) : 0.0;
                double d8 = insets != null ? this.snapSpaceY(insets.getBottom()) : 0.0;
                double d9 = node.getBaselineOffset();
                double d10 = d5 = bl ? this.snapSizeY(node.minHeight(d6)) : this.snapSizeY(node.prefHeight(d6));
                if (d9 == Double.NEGATIVE_INFINITY) {
                    d3 = Math.max(d3, d5 + d7);
                    continue;
                }
                d3 = Math.max(d3, d9 + d7);
                d4 = Math.max(d4, this.snapSpaceY(bl ? this.snapSizeY(node.minHeight(d6)) : this.snapSizeY(node.prefHeight(d6))) - d9 + d8);
            }
            return d3 + d4;
        }
        double d11 = 0.0;
        int n = list.size();
        for (int i = 0; i < n; ++i) {
            Node node = list.get(i);
            Insets insets = callback.call(node);
            double d12 = Double.isNaN(d) ? dArray[i] : d;
            d11 = Math.max(d11, bl ? this.computeChildMinAreaHeight(node, -1.0, insets, d12) : this.computeChildPrefAreaHeight(node, -1.0, insets, d12));
        }
        return d11;
    }

    private double getMaxAreaWidth(List<Node> list, Callback<Node, Insets> callback, double[] dArray, boolean bl, boolean bl2) {
        double d = dArray == null ? -1.0 : (dArray.length == 1 ? dArray[0] : Double.NaN);
        double d2 = 0.0;
        int n = list.size();
        for (int i = 0; i < n; ++i) {
            Node node = list.get(i);
            Insets insets = callback.call(node);
            double d3 = Double.isNaN(d) ? dArray[i] : d;
            d2 = Math.max(d2, bl2 ? this.computeChildMinAreaWidth(list.get(i), -1.0, insets, d3, bl) : this.computeChildPrefAreaWidth(node, -1.0, insets, d3, bl));
        }
        return d2;
    }

    protected void positionInArea(Node node, double d, double d2, double d3, double d4, double d5, HPos hPos, VPos vPos) {
        Region.positionInArea(node, d, d2, d3, d4, d5, Insets.EMPTY, hPos, vPos, this.isSnapToPixel());
    }

    public static void positionInArea(Node node, double d, double d2, double d3, double d4, double d5, Insets insets, HPos hPos, VPos vPos, boolean bl) {
        Insets insets2 = insets != null ? insets : Insets.EMPTY;
        double d6 = bl ? Region.getSnapScaleX(node) : 1.0;
        double d7 = bl ? Region.getSnapScaleY(node) : 1.0;
        Region.position(node, d, d2, d3, d4, d5, Region.snapSpace(insets2.getTop(), bl, d7), Region.snapSpace(insets2.getRight(), bl, d6), Region.snapSpace(insets2.getBottom(), bl, d7), Region.snapSpace(insets2.getLeft(), bl, d6), hPos, vPos, bl);
    }

    protected void layoutInArea(Node node, double d, double d2, double d3, double d4, double d5, HPos hPos, VPos vPos) {
        this.layoutInArea(node, d, d2, d3, d4, d5, Insets.EMPTY, hPos, vPos);
    }

    protected void layoutInArea(Node node, double d, double d2, double d3, double d4, double d5, Insets insets, HPos hPos, VPos vPos) {
        this.layoutInArea(node, d, d2, d3, d4, d5, insets, true, true, hPos, vPos);
    }

    protected void layoutInArea(Node node, double d, double d2, double d3, double d4, double d5, Insets insets, boolean bl, boolean bl2, HPos hPos, VPos vPos) {
        Region.layoutInArea(node, d, d2, d3, d4, d5, insets, bl, bl2, hPos, vPos, this.isSnapToPixel());
    }

    public static void layoutInArea(Node node, double d, double d2, double d3, double d4, double d5, Insets insets, boolean bl, boolean bl2, HPos hPos, VPos vPos, boolean bl3) {
        Insets insets2 = insets != null ? insets : Insets.EMPTY;
        double d6 = bl3 ? Region.getSnapScaleX(node) : 1.0;
        double d7 = bl3 ? Region.getSnapScaleY(node) : 1.0;
        double d8 = Region.snapSpace(insets2.getTop(), bl3, d7);
        double d9 = Region.snapSpace(insets2.getBottom(), bl3, d7);
        double d10 = Region.snapSpace(insets2.getLeft(), bl3, d6);
        double d11 = Region.snapSpace(insets2.getRight(), bl3, d6);
        if (vPos == VPos.BASELINE) {
            double d12 = node.getBaselineOffset();
            if (d12 == Double.NEGATIVE_INFINITY) {
                if (node.isResizable()) {
                    d9 += Region.snapSpace(d4 - d5, bl3, d7);
                } else {
                    d8 = Region.snapSpace(d5 - node.getLayoutBounds().getHeight(), bl3, d7);
                }
            } else {
                d8 = Region.snapSpace(d5 - d12, bl3, d7);
            }
        }
        if (node.isResizable()) {
            Vec2d vec2d = Region.boundedNodeSizeWithBias(node, d3 - d10 - d11, d4 - d8 - d9, bl, bl2, TEMP_VEC2D);
            node.resize(Region.snapSize(vec2d.x, bl3, d6), Region.snapSize(vec2d.y, bl3, d6));
        }
        Region.position(node, d, d2, d3, d4, d5, d8, d11, d9, d10, hPos, vPos, bl3);
    }

    private static void position(Node node, double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, HPos hPos, VPos vPos, boolean bl) {
        double d10;
        double d11 = d9 + Region.computeXOffset(d3 - d9 - d7, node.getLayoutBounds().getWidth(), hPos);
        double d12 = vPos == VPos.BASELINE ? ((d10 = node.getBaselineOffset()) == Double.NEGATIVE_INFINITY ? d5 - node.getLayoutBounds().getHeight() : d5 - d10) : d6 + Region.computeYOffset(d4 - d6 - d8, node.getLayoutBounds().getHeight(), vPos);
        d10 = d + d11;
        double d13 = d2 + d12;
        if (bl) {
            d10 = Region.snapPosition(d10, true, Region.getSnapScaleX(node));
            d13 = Region.snapPosition(d13, true, Region.getSnapScaleY(node));
        }
        node.relocate(d10, d13);
    }

    private void doUpdatePeer() {
        boolean bl;
        boolean bl2;
        if (this._shape != null) {
            NodeHelper.syncPeer(this._shape);
        }
        NGRegion nGRegion = (NGRegion)NodeHelper.getPeer(this);
        if (!this.cornersValid) {
            this.validateCorners();
        }
        if (bl2 = NodeHelper.isDirty(this, DirtyBits.NODE_GEOMETRY)) {
            nGRegion.setSize((float)this.getWidth(), (float)this.getHeight());
        }
        if (bl = NodeHelper.isDirty(this, DirtyBits.REGION_SHAPE)) {
            nGRegion.updateShape(this._shape, this.isScaleShape(), this.isCenterShape(), this.isCacheShape());
        }
        nGRegion.updateFillCorners(this.normalizedFillCorners);
        boolean bl3 = NodeHelper.isDirty(this, DirtyBits.SHAPE_FILL);
        Background background = this.getBackground();
        if (bl3) {
            nGRegion.updateBackground(background);
        }
        if (NodeHelper.isDirty(this, DirtyBits.NODE_CONTENTS)) {
            nGRegion.imagesUpdated();
        }
        nGRegion.updateStrokeCorners(this.normalizedStrokeCorners);
        if (NodeHelper.isDirty(this, DirtyBits.SHAPE_STROKE)) {
            nGRegion.updateBorder(this.getBorder());
        }
        if (bl2 || bl3 || bl) {
            Insets insets = this.getOpaqueInsets();
            if (this._shape != null) {
                if (insets != null) {
                    nGRegion.setOpaqueInsets((float)insets.getTop(), (float)insets.getRight(), (float)insets.getBottom(), (float)insets.getLeft());
                } else {
                    nGRegion.setOpaqueInsets(Float.NaN, Float.NaN, Float.NaN, Float.NaN);
                }
            } else if (background == null || background.isEmpty()) {
                nGRegion.setOpaqueInsets(Float.NaN, Float.NaN, Float.NaN, Float.NaN);
            } else {
                double[] dArray = new double[4];
                background.computeOpaqueInsets(this.getWidth(), this.getHeight(), dArray);
                if (insets != null) {
                    double d = Double.isNaN(dArray[0]) ? insets.getTop() : (dArray[0] = Double.isNaN(insets.getTop()) ? dArray[0] : Math.min(dArray[0], insets.getTop()));
                    double d2 = Double.isNaN(dArray[1]) ? insets.getRight() : (dArray[1] = Double.isNaN(insets.getRight()) ? dArray[1] : Math.min(dArray[1], insets.getRight()));
                    double d3 = Double.isNaN(dArray[2]) ? insets.getBottom() : (dArray[2] = Double.isNaN(insets.getBottom()) ? dArray[2] : Math.min(dArray[2], insets.getBottom()));
                    dArray[3] = Double.isNaN(dArray[3]) ? insets.getLeft() : (Double.isNaN(insets.getLeft()) ? dArray[3] : Math.min(dArray[3], insets.getLeft()));
                }
                nGRegion.setOpaqueInsets((float)dArray[0], (float)dArray[1], (float)dArray[2], (float)dArray[3]);
            }
        }
    }

    private NGNode doCreatePeer() {
        return new NGRegion();
    }

    private boolean shapeContains(Shape shape, double d, double d2, double d3, double d4, double d5, double d6) {
        double d7 = d;
        double d8 = d2;
        RectBounds rectBounds = shape.getBounds();
        if (this.isScaleShape()) {
            d7 -= d6;
            d8 -= d3;
            d7 *= (double)rectBounds.getWidth() / (this.getWidth() - d6 - d4);
            d8 *= (double)rectBounds.getHeight() / (this.getHeight() - d3 - d5);
            if (this.isCenterShape()) {
                d7 += (double)rectBounds.getMinX();
                d8 += (double)rectBounds.getMinY();
            }
        } else if (this.isCenterShape()) {
            double d9 = rectBounds.getWidth();
            double d10 = rectBounds.getHeight();
            double d11 = d9 / (d9 - d6 - d4);
            double d12 = d10 / (d10 - d3 - d5);
            d7 = d11 * (d7 - (d6 + (this.getWidth() - d9) / 2.0)) + (double)rectBounds.getMinX();
            d8 = d12 * (d8 - (d3 + (this.getHeight() - d10) / 2.0)) + (double)rectBounds.getMinY();
        } else if (d3 != 0.0 || d4 != 0.0 || d5 != 0.0 || d6 != 0.0) {
            double d13 = (double)rectBounds.getWidth() / ((double)rectBounds.getWidth() - d6 - d4);
            double d14 = (double)rectBounds.getHeight() / ((double)rectBounds.getHeight() - d3 - d5);
            d7 = d13 * (d7 - d6 - (double)rectBounds.getMinX()) + (double)rectBounds.getMinX();
            d8 = d14 * (d8 - d3 - (double)rectBounds.getMinY()) + (double)rectBounds.getMinY();
        }
        return shape.contains((float)d7, (float)d8);
    }

    private boolean doComputeContains(double d, double d2) {
        int n;
        Object object;
        double d3 = this.getWidth();
        double d4 = this.getHeight();
        Background background = this.getBackground();
        if (this._shape != null) {
            if (background != null && !background.getFills().isEmpty()) {
                List<BackgroundFill> list = background.getFills();
                double d5 = Double.MAX_VALUE;
                double d6 = Double.MAX_VALUE;
                double d7 = Double.MAX_VALUE;
                double d8 = Double.MAX_VALUE;
                int n2 = list.size();
                for (int i = 0; i < n2; ++i) {
                    BackgroundFill backgroundFill = list.get(0);
                    d5 = Math.min(d5, backgroundFill.getInsets().getTop());
                    d6 = Math.min(d6, backgroundFill.getInsets().getLeft());
                    d7 = Math.min(d7, backgroundFill.getInsets().getBottom());
                    d8 = Math.min(d8, backgroundFill.getInsets().getRight());
                }
                return this.shapeContains(ShapeHelper.configShape(this._shape), d, d2, d5, d6, d7, d8);
            }
            return false;
        }
        if (background != null) {
            object = background.getFills();
            n = object.size();
            for (int i = 0; i < n; ++i) {
                BackgroundFill backgroundFill = (BackgroundFill)object.get(i);
                if (!this.contains(d, d2, 0.0, 0.0, d3, d4, backgroundFill.getInsets(), this.getNormalizedFillCorner(i))) continue;
                return true;
            }
        }
        if ((object = this.getBorder()) != null) {
            List<BorderStroke> list = ((Border)object).getStrokes();
            int n3 = list.size();
            for (n = 0; n < n3; ++n) {
                BorderStroke borderStroke = list.get(n);
                if (!this.contains(d, d2, 0.0, 0.0, d3, d4, borderStroke.getWidths(), false, borderStroke.getInsets(), this.getNormalizedStrokeCorner(n))) continue;
                return true;
            }
            List<BorderImage> list2 = ((Border)object).getImages();
            int n4 = list2.size();
            for (n3 = 0; n3 < n4; ++n3) {
                BorderImage borderImage = list2.get(n3);
                if (!this.contains(d, d2, 0.0, 0.0, d3, d4, borderImage.getWidths(), borderImage.isFilled(), borderImage.getInsets(), CornerRadii.EMPTY)) continue;
                return true;
            }
        }
        return false;
    }

    private boolean contains(double d, double d2, double d3, double d4, double d5, double d6, BorderWidths borderWidths, boolean bl, Insets insets, CornerRadii cornerRadii) {
        if (bl) {
            if (this.contains(d, d2, d3, d4, d5, d6, insets, cornerRadii)) {
                return true;
            }
        } else {
            boolean bl2 = this.contains(d, d2, d3, d4, d5, d6, insets, cornerRadii);
            if (bl2) {
                boolean bl3;
                boolean bl4 = bl3 = !this.contains(d, d2, d3 + (borderWidths.isLeftAsPercentage() ? this.getWidth() * borderWidths.getLeft() : borderWidths.getLeft()), d4 + (borderWidths.isTopAsPercentage() ? this.getHeight() * borderWidths.getTop() : borderWidths.getTop()), d5 - (borderWidths.isRightAsPercentage() ? this.getWidth() * borderWidths.getRight() : borderWidths.getRight()), d6 - (borderWidths.isBottomAsPercentage() ? this.getHeight() * borderWidths.getBottom() : borderWidths.getBottom()), insets, cornerRadii);
                if (bl3) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean contains(double d, double d2, double d3, double d4, double d5, double d6, Insets insets, CornerRadii cornerRadii) {
        double d7 = d3 + insets.getLeft();
        double d8 = d4 + insets.getTop();
        double d9 = d5 - insets.getRight();
        double d10 = d6 - insets.getBottom();
        if (d >= d7 && d2 >= d8 && d <= d9 && d2 <= d10) {
            double d11;
            double d12;
            double d13;
            double d14;
            double d15 = cornerRadii.getTopLeftHorizontalRadius();
            if (cornerRadii.isUniform() && d15 == 0.0) {
                return true;
            }
            double d16 = cornerRadii.getTopLeftVerticalRadius();
            double d17 = cornerRadii.getTopRightHorizontalRadius();
            double d18 = cornerRadii.getTopRightVerticalRadius();
            double d19 = cornerRadii.getBottomLeftHorizontalRadius();
            double d20 = cornerRadii.getBottomLeftVerticalRadius();
            double d21 = cornerRadii.getBottomRightHorizontalRadius();
            double d22 = cornerRadii.getBottomRightVerticalRadius();
            if (d <= d7 + d15 && d2 <= d8 + d16) {
                d14 = d7 + d15;
                d13 = d8 + d16;
                d12 = d15;
                d11 = d16;
            } else if (d >= d9 - d17 && d2 <= d8 + d18) {
                d14 = d9 - d17;
                d13 = d8 + d18;
                d12 = d17;
                d11 = d18;
            } else if (d >= d9 - d21 && d2 >= d10 - d22) {
                d14 = d9 - d21;
                d13 = d10 - d22;
                d12 = d21;
                d11 = d22;
            } else if (d <= d7 + d19 && d2 >= d10 - d20) {
                d14 = d7 + d19;
                d13 = d10 - d20;
                d12 = d19;
                d11 = d20;
            } else {
                return true;
            }
            double d23 = d - d14;
            double d24 = d2 - d13;
            double d25 = d23 * d23 / (d12 * d12) + d24 * d24 / (d11 * d11);
            if (d25 - 1.0E-7 <= 1.0) {
                return true;
            }
        }
        return false;
    }

    private CornerRadii getNormalizedFillCorner(int n) {
        if (!this.cornersValid) {
            this.validateCorners();
        }
        return this.normalizedFillCorners == null ? this.getBackground().getFills().get(n).getRadii() : this.normalizedFillCorners.get(n);
    }

    private CornerRadii getNormalizedStrokeCorner(int n) {
        if (!this.cornersValid) {
            this.validateCorners();
        }
        return this.normalizedStrokeCorners == null ? this.getBorder().getStrokes().get(n).getRadii() : this.normalizedStrokeCorners.get(n);
    }

    private void validateCorners() {
        int n;
        Object object;
        CornerRadii cornerRadii;
        Object object2;
        double d = this.getWidth();
        double d2 = this.getHeight();
        List<Object> list = null;
        List<Object> list2 = null;
        Background background = this.getBackground();
        List<BackgroundFill> list3 = background == null ? Collections.EMPTY_LIST : background.getFills();
        for (int i = 0; i < list3.size(); ++i) {
            object2 = list3.get(i);
            CornerRadii cornerRadii2 = ((BackgroundFill)object2).getRadii();
            if (cornerRadii2 == (cornerRadii = Region.normalize(cornerRadii2, (Insets)(object = ((BackgroundFill)object2).getInsets()), d, d2))) continue;
            if (list == null) {
                list = Arrays.asList(new CornerRadii[list3.size()]);
            }
            list.set(i, cornerRadii);
        }
        Border border = this.getBorder();
        object2 = border == null ? Collections.EMPTY_LIST : border.getStrokes();
        for (n = 0; n < object2.size(); ++n) {
            Insets insets;
            CornerRadii cornerRadii3;
            object = (BorderStroke)object2.get(n);
            cornerRadii = ((BorderStroke)object).getRadii();
            if (cornerRadii == (cornerRadii3 = Region.normalize(cornerRadii, insets = ((BorderStroke)object).getInsets(), d, d2))) continue;
            if (list2 == null) {
                list2 = Arrays.asList(new CornerRadii[object2.size()]);
            }
            list2.set(n, cornerRadii3);
        }
        if (list != null) {
            for (n = 0; n < list3.size(); ++n) {
                if (list.get(n) != null) continue;
                list.set(n, list3.get(n).getRadii());
            }
            list = Collections.unmodifiableList(list);
        }
        if (list2 != null) {
            for (n = 0; n < object2.size(); ++n) {
                if (list2.get(n) != null) continue;
                list2.set(n, ((BorderStroke)object2.get(n)).getRadii());
            }
            list2 = Collections.unmodifiableList(list2);
        }
        this.normalizedFillCorners = list;
        this.normalizedStrokeCorners = list2;
        this.cornersValid = true;
    }

    private static CornerRadii normalize(CornerRadii cornerRadii, Insets insets, double d, double d2) {
        d2 -= insets.getTop() + insets.getBottom();
        if ((d -= insets.getLeft() + insets.getRight()) <= 0.0 || d2 <= 0.0) {
            return CornerRadii.EMPTY;
        }
        double d3 = cornerRadii.getTopLeftVerticalRadius();
        double d4 = cornerRadii.getTopLeftHorizontalRadius();
        double d5 = cornerRadii.getTopRightVerticalRadius();
        double d6 = cornerRadii.getTopRightHorizontalRadius();
        double d7 = cornerRadii.getBottomRightVerticalRadius();
        double d8 = cornerRadii.getBottomRightHorizontalRadius();
        double d9 = cornerRadii.getBottomLeftVerticalRadius();
        double d10 = cornerRadii.getBottomLeftHorizontalRadius();
        if (cornerRadii.hasPercentBasedRadii) {
            if (cornerRadii.isTopLeftVerticalRadiusAsPercentage()) {
                d3 *= d2;
            }
            if (cornerRadii.isTopLeftHorizontalRadiusAsPercentage()) {
                d4 *= d;
            }
            if (cornerRadii.isTopRightVerticalRadiusAsPercentage()) {
                d5 *= d2;
            }
            if (cornerRadii.isTopRightHorizontalRadiusAsPercentage()) {
                d6 *= d;
            }
            if (cornerRadii.isBottomRightVerticalRadiusAsPercentage()) {
                d7 *= d2;
            }
            if (cornerRadii.isBottomRightHorizontalRadiusAsPercentage()) {
                d8 *= d;
            }
            if (cornerRadii.isBottomLeftVerticalRadiusAsPercentage()) {
                d9 *= d2;
            }
            if (cornerRadii.isBottomLeftHorizontalRadiusAsPercentage()) {
                d10 *= d;
            }
        }
        double d11 = 1.0;
        if (d4 + d6 > d) {
            d11 = Math.min(d11, d / (d4 + d6));
        }
        if (d10 + d8 > d) {
            d11 = Math.min(d11, d / (d10 + d8));
        }
        if (d3 + d9 > d2) {
            d11 = Math.min(d11, d2 / (d3 + d9));
        }
        if (d5 + d7 > d2) {
            d11 = Math.min(d11, d2 / (d5 + d7));
        }
        if (d11 < 1.0) {
            d3 *= d11;
            d4 *= d11;
            d5 *= d11;
            d6 *= d11;
            d7 *= d11;
            d8 *= d11;
            d9 *= d11;
            d10 *= d11;
        }
        if (cornerRadii.hasPercentBasedRadii || d11 < 1.0) {
            return new CornerRadii(d4, d3, d5, d6, d8, d7, d9, d10, false, false, false, false, false, false, false, false);
        }
        return cornerRadii;
    }

    private void doPickNodeLocal(PickRay pickRay, PickResultChooser pickResultChooser) {
        double d = NodeHelper.intersectsBounds(this, pickRay);
        if (!Double.isNaN(d) && ParentHelper.pickChildrenNode(this, pickRay, pickResultChooser)) {
            NodeHelper.intersects(this, pickRay, pickResultChooser);
        }
    }

    private Bounds doComputeLayoutBounds() {
        if (this.boundingBox == null) {
            this.boundingBox = new BoundingBox(0.0, 0.0, 0.0, this.getWidth(), this.getHeight(), 0.0);
        }
        return this.boundingBox;
    }

    private void doNotifyLayoutBoundsChanged() {
    }

    private BaseBounds computeShapeBounds(BaseBounds baseBounds) {
        Object object;
        Shape shape = ShapeHelper.configShape(this._shape);
        float[] fArray = new float[]{Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY};
        Background background = this.getBackground();
        if (background != null) {
            object = shape.getBounds();
            Insets insets = background.getOutsets();
            fArray[0] = ((RectBounds)object).getMinX() - (float)insets.getLeft();
            fArray[1] = ((RectBounds)object).getMinY() - (float)insets.getTop();
            fArray[2] = ((RectBounds)object).getMaxX() + (float)insets.getBottom();
            fArray[3] = ((RectBounds)object).getMaxY() + (float)insets.getRight();
        }
        if ((object = this.getBorder()) != null && ((Border)object).getStrokes().size() > 0) {
            for (BorderStroke borderStroke : ((Border)object).getStrokes()) {
                BorderStrokeStyle borderStrokeStyle = borderStroke.getTopStyle() != null ? borderStroke.getTopStyle() : (borderStroke.getLeftStyle() != null ? borderStroke.getLeftStyle() : (borderStroke.getBottomStyle() != null ? borderStroke.getBottomStyle() : (borderStroke.getRightStyle() != null ? borderStroke.getRightStyle() : null)));
                if (borderStrokeStyle == null || borderStrokeStyle == BorderStrokeStyle.NONE) continue;
                StrokeType strokeType = borderStrokeStyle.getType();
                double d = Math.max(borderStroke.getWidths().top, 0.0);
                StrokeLineCap strokeLineCap = borderStrokeStyle.getLineCap();
                StrokeLineJoin strokeLineJoin = borderStrokeStyle.getLineJoin();
                float f = (float)Math.max(borderStrokeStyle.getMiterLimit(), 1.0);
                Toolkit.getToolkit().accumulateStrokeBounds(shape, fArray, strokeType, d, strokeLineCap, strokeLineJoin, f, BaseTransform.IDENTITY_TRANSFORM);
            }
        }
        if (fArray[2] < fArray[0] || fArray[3] < fArray[1]) {
            return baseBounds.makeEmpty();
        }
        return baseBounds.deriveWithNewBounds(fArray[0], fArray[1], 0.0f, fArray[2], fArray[3], 0.0f);
    }

    private BaseBounds doComputeGeomBounds(BaseBounds baseBounds, BaseTransform baseTransform) {
        Object object;
        Object object2;
        double d = 0.0;
        double d2 = 0.0;
        double d3 = this.getWidth();
        double d4 = this.getHeight();
        if (this._shape != null && !this.isScaleShape()) {
            object2 = this.computeShapeBounds(baseBounds);
            double d5 = ((BaseBounds)object2).getWidth();
            double d6 = ((BaseBounds)object2).getHeight();
            if (this.isCenterShape()) {
                d = (d3 - d5) / 2.0;
                d2 = (d4 - d6) / 2.0;
                d3 = d + d5;
                d4 = d2 + d6;
            } else {
                d = ((BaseBounds)object2).getMinX();
                d2 = ((BaseBounds)object2).getMinY();
                d3 = ((BaseBounds)object2).getMaxX();
                d4 = ((BaseBounds)object2).getMaxY();
            }
        } else {
            object2 = this.getBackground();
            Border border = this.getBorder();
            object = object2 == null ? Insets.EMPTY : ((Background)object2).getOutsets();
            Insets insets = border == null ? Insets.EMPTY : border.getOutsets();
            d -= Math.max(((Insets)object).getLeft(), insets.getLeft());
            d2 -= Math.max(((Insets)object).getTop(), insets.getTop());
            d3 += Math.max(((Insets)object).getRight(), insets.getRight());
            d4 += Math.max(((Insets)object).getBottom(), insets.getBottom());
        }
        object2 = RegionHelper.superComputeGeomBounds(this, baseBounds, baseTransform);
        if (((BaseBounds)object2).isEmpty()) {
            baseBounds = baseBounds.deriveWithNewBounds((float)d, (float)d2, 0.0f, (float)d3, (float)d4, 0.0f);
            baseBounds = baseTransform.transform(baseBounds, baseBounds);
            return baseBounds;
        }
        BaseBounds baseBounds2 = TempState.getInstance().bounds;
        baseBounds2 = baseBounds2.deriveWithNewBounds((float)d, (float)d2, 0.0f, (float)d3, (float)d4, 0.0f);
        object = baseTransform.transform(baseBounds2, baseBounds2);
        object2 = ((BaseBounds)object2).deriveWithUnion((BaseBounds)object);
        return object2;
    }

    public String getUserAgentStylesheet() {
        return null;
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return Region.getClassCssMetaData();
    }

    static {
        RegionHelper.setRegionAccessor(new RegionHelper.RegionAccessor(){

            @Override
            public NGNode doCreatePeer(Node node) {
                return ((Region)node).doCreatePeer();
            }

            @Override
            public void doUpdatePeer(Node node) {
                ((Region)node).doUpdatePeer();
            }

            @Override
            public Bounds doComputeLayoutBounds(Node node) {
                return ((Region)node).doComputeLayoutBounds();
            }

            @Override
            public BaseBounds doComputeGeomBounds(Node node, BaseBounds baseBounds, BaseTransform baseTransform) {
                return ((Region)node).doComputeGeomBounds(baseBounds, baseTransform);
            }

            @Override
            public boolean doComputeContains(Node node, double d, double d2) {
                return ((Region)node).doComputeContains(d, d2);
            }

            @Override
            public void doNotifyLayoutBoundsChanged(Node node) {
                ((Region)node).doNotifyLayoutBoundsChanged();
            }

            @Override
            public void doPickNodeLocal(Node node, PickRay pickRay, PickResultChooser pickResultChooser) {
                ((Region)node).doPickNodeLocal(pickRay, pickResultChooser);
            }
        });
        TEMP_VEC2D = new Vec2d();
    }

    private static class StyleableProperties {
        private static final CssMetaData<Region, Insets> PADDING = new CssMetaData<Region, Insets>("-fx-padding", InsetsConverter.getInstance(), Insets.EMPTY){

            @Override
            public boolean isSettable(Region region) {
                return region.padding == null || !region.padding.isBound();
            }

            @Override
            public StyleableProperty<Insets> getStyleableProperty(Region region) {
                return (StyleableProperty)((Object)region.paddingProperty());
            }
        };
        private static final CssMetaData<Region, Insets> OPAQUE_INSETS = new CssMetaData<Region, Insets>("-fx-opaque-insets", InsetsConverter.getInstance(), null){

            @Override
            public boolean isSettable(Region region) {
                return region.opaqueInsets == null || !region.opaqueInsets.isBound();
            }

            @Override
            public StyleableProperty<Insets> getStyleableProperty(Region region) {
                return (StyleableProperty)((Object)region.opaqueInsetsProperty());
            }
        };
        private static final CssMetaData<Region, Background> BACKGROUND = new CssMetaData<Region, Background>("-fx-region-background", BackgroundConverter.INSTANCE, null, false, Background.getClassCssMetaData()){

            @Override
            public boolean isSettable(Region region) {
                return !region.background.isBound();
            }

            @Override
            public StyleableProperty<Background> getStyleableProperty(Region region) {
                return (StyleableProperty)((Object)region.background);
            }
        };
        private static final CssMetaData<Region, Border> BORDER = new CssMetaData<Region, Border>("-fx-region-border", (StyleConverter)BorderConverter.getInstance(), null, false, Border.getClassCssMetaData()){

            @Override
            public boolean isSettable(Region region) {
                return !region.border.isBound();
            }

            @Override
            public StyleableProperty<Border> getStyleableProperty(Region region) {
                return (StyleableProperty)((Object)region.border);
            }
        };
        private static final CssMetaData<Region, javafx.scene.shape.Shape> SHAPE = new CssMetaData<Region, javafx.scene.shape.Shape>("-fx-shape", ShapeConverter.getInstance()){

            @Override
            public boolean isSettable(Region region) {
                return region.shape == null || !region.shape.isBound();
            }

            @Override
            public StyleableProperty<javafx.scene.shape.Shape> getStyleableProperty(Region region) {
                return (StyleableProperty)((Object)region.shapeProperty());
            }
        };
        private static final CssMetaData<Region, Boolean> SCALE_SHAPE = new CssMetaData<Region, Boolean>("-fx-scale-shape", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(Region region) {
                return region.scaleShape == null || !region.scaleShape.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(Region region) {
                return (StyleableProperty)((Object)region.scaleShapeProperty());
            }
        };
        private static final CssMetaData<Region, Boolean> POSITION_SHAPE = new CssMetaData<Region, Boolean>("-fx-position-shape", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(Region region) {
                return region.centerShape == null || !region.centerShape.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(Region region) {
                return (StyleableProperty)((Object)region.centerShapeProperty());
            }
        };
        private static final CssMetaData<Region, Boolean> CACHE_SHAPE = new CssMetaData<Region, Boolean>("-fx-cache-shape", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(Region region) {
                return region.cacheShape == null || !region.cacheShape.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(Region region) {
                return (StyleableProperty)((Object)region.cacheShapeProperty());
            }
        };
        private static final CssMetaData<Region, Boolean> SNAP_TO_PIXEL = new CssMetaData<Region, Boolean>("-fx-snap-to-pixel", BooleanConverter.getInstance(), Boolean.TRUE){

            @Override
            public boolean isSettable(Region region) {
                return region.snapToPixel == null || !region.snapToPixel.isBound();
            }

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(Region region) {
                return (StyleableProperty)((Object)region.snapToPixelProperty());
            }
        };
        private static final CssMetaData<Region, Number> MIN_HEIGHT = new CssMetaData<Region, Number>("-fx-min-height", SizeConverter.getInstance(), (Number)-1.0){

            @Override
            public boolean isSettable(Region region) {
                return region.minHeight == null || !region.minHeight.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Region region) {
                return (StyleableProperty)((Object)region.minHeightProperty());
            }
        };
        private static final CssMetaData<Region, Number> PREF_HEIGHT = new CssMetaData<Region, Number>("-fx-pref-height", SizeConverter.getInstance(), (Number)-1.0){

            @Override
            public boolean isSettable(Region region) {
                return region.prefHeight == null || !region.prefHeight.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Region region) {
                return (StyleableProperty)((Object)region.prefHeightProperty());
            }
        };
        private static final CssMetaData<Region, Number> MAX_HEIGHT = new CssMetaData<Region, Number>("-fx-max-height", SizeConverter.getInstance(), (Number)-1.0){

            @Override
            public boolean isSettable(Region region) {
                return region.maxHeight == null || !region.maxHeight.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Region region) {
                return (StyleableProperty)((Object)region.maxHeightProperty());
            }
        };
        private static final CssMetaData<Region, Number> MIN_WIDTH = new CssMetaData<Region, Number>("-fx-min-width", SizeConverter.getInstance(), (Number)-1.0){

            @Override
            public boolean isSettable(Region region) {
                return region.minWidth == null || !region.minWidth.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Region region) {
                return (StyleableProperty)((Object)region.minWidthProperty());
            }
        };
        private static final CssMetaData<Region, Number> PREF_WIDTH = new CssMetaData<Region, Number>("-fx-pref-width", SizeConverter.getInstance(), (Number)-1.0){

            @Override
            public boolean isSettable(Region region) {
                return region.prefWidth == null || !region.prefWidth.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Region region) {
                return (StyleableProperty)((Object)region.prefWidthProperty());
            }
        };
        private static final CssMetaData<Region, Number> MAX_WIDTH = new CssMetaData<Region, Number>("-fx-max-width", SizeConverter.getInstance(), (Number)-1.0){

            @Override
            public boolean isSettable(Region region) {
                return region.maxWidth == null || !region.maxWidth.isBound();
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(Region region) {
                return (StyleableProperty)((Object)region.maxWidthProperty());
            }
        };
        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        private StyleableProperties() {
        }

        static {
            ArrayList arrayList = new ArrayList(Parent.getClassCssMetaData());
            arrayList.add(PADDING);
            arrayList.add(BACKGROUND);
            arrayList.add(BORDER);
            arrayList.add(OPAQUE_INSETS);
            arrayList.add(SHAPE);
            arrayList.add(SCALE_SHAPE);
            arrayList.add(POSITION_SHAPE);
            arrayList.add(SNAP_TO_PIXEL);
            arrayList.add(MIN_WIDTH);
            arrayList.add(PREF_WIDTH);
            arrayList.add(MAX_WIDTH);
            arrayList.add(MIN_HEIGHT);
            arrayList.add(PREF_HEIGHT);
            arrayList.add(MAX_HEIGHT);
            STYLEABLES = Collections.unmodifiableList(arrayList);
        }
    }

    private final class ShapeProperty
    extends StyleableObjectProperty<javafx.scene.shape.Shape>
    implements Runnable {
        private ShapeProperty() {
        }

        @Override
        public Object getBean() {
            return Region.this;
        }

        @Override
        public String getName() {
            return "shape";
        }

        @Override
        public CssMetaData<Region, javafx.scene.shape.Shape> getCssMetaData() {
            return StyleableProperties.SHAPE;
        }

        @Override
        protected void invalidated() {
            javafx.scene.shape.Shape shape = (javafx.scene.shape.Shape)this.get();
            if (Region.this._shape != shape) {
                if (Region.this._shape != null) {
                    ShapeHelper.setShapeChangeListener(Region.this._shape, null);
                }
                if (shape != null) {
                    ShapeHelper.setShapeChangeListener(shape, this);
                }
                this.run();
                if (Region.this._shape == null || shape == null) {
                    Region.this.insets.fireValueChanged();
                }
                Region.this._shape = shape;
            }
        }

        @Override
        public void run() {
            NodeHelper.geomChanged(Region.this);
            NodeHelper.markDirty(Region.this, DirtyBits.REGION_SHAPE);
        }
    }

    private final class MinPrefMaxProperty
    extends StyleableDoubleProperty {
        private final String name;
        private final CssMetaData<? extends Styleable, Number> cssMetaData;

        MinPrefMaxProperty(String string, double d, CssMetaData<? extends Styleable, Number> cssMetaData) {
            super(d);
            this.name = string;
            this.cssMetaData = cssMetaData;
        }

        @Override
        public void invalidated() {
            Region.this.requestParentLayout();
        }

        @Override
        public Object getBean() {
            return Region.this;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return this.cssMetaData;
        }
    }

    private final class InsetsProperty
    extends ReadOnlyObjectProperty<Insets> {
        private Insets cache = null;
        private ExpressionHelper<Insets> helper = null;

        private InsetsProperty() {
        }

        @Override
        public Object getBean() {
            return Region.this;
        }

        @Override
        public String getName() {
            return "insets";
        }

        @Override
        public void addListener(InvalidationListener invalidationListener) {
            this.helper = ExpressionHelper.addListener(this.helper, this, invalidationListener);
        }

        @Override
        public void removeListener(InvalidationListener invalidationListener) {
            this.helper = ExpressionHelper.removeListener(this.helper, invalidationListener);
        }

        @Override
        public void addListener(ChangeListener<? super Insets> changeListener) {
            this.helper = ExpressionHelper.addListener(this.helper, this, changeListener);
        }

        @Override
        public void removeListener(ChangeListener<? super Insets> changeListener) {
            this.helper = ExpressionHelper.removeListener(this.helper, changeListener);
        }

        void fireValueChanged() {
            this.cache = null;
            Region.this.updateSnappedInsets();
            Region.this.requestLayout();
            ExpressionHelper.fireValueChangedEvent(this.helper);
        }

        @Override
        public Insets get() {
            if (Region.this._shape != null) {
                return Region.this.getPadding();
            }
            Border border = Region.this.getBorder();
            if (border == null || Insets.EMPTY.equals(border.getInsets())) {
                return Region.this.getPadding();
            }
            if (this.cache == null) {
                Insets insets = border.getInsets();
                Insets insets2 = Region.this.getPadding();
                this.cache = new Insets(insets.getTop() + insets2.getTop(), insets.getRight() + insets2.getRight(), insets.getBottom() + insets2.getBottom(), insets.getLeft() + insets2.getLeft());
            }
            return this.cache;
        }
    }
}

