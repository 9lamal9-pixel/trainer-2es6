/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import com.sun.javafx.UnmodifiableArrayList;
import com.sun.javafx.css.SubCssMetaData;
import com.sun.javafx.scene.layout.region.CornerRadiiConverter;
import com.sun.javafx.scene.layout.region.LayeredBackgroundPositionConverter;
import com.sun.javafx.scene.layout.region.LayeredBackgroundSizeConverter;
import com.sun.javafx.scene.layout.region.RepeatStruct;
import com.sun.javafx.scene.layout.region.RepeatStructConverter;
import com.sun.javafx.tk.PlatformImage;
import com.sun.javafx.tk.Toolkit;
import com.sun.prism.Image;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javafx.beans.NamedArg;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.converter.InsetsConverter;
import javafx.css.converter.PaintConverter;
import javafx.css.converter.URLConverter;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public final class Background {
    static final CssMetaData<Node, Paint[]> BACKGROUND_COLOR = new SubCssMetaData<Paint[]>("-fx-background-color", (StyleConverter)PaintConverter.SequenceConverter.getInstance(), new Paint[]{Color.TRANSPARENT});
    static final CssMetaData<Node, CornerRadii[]> BACKGROUND_RADIUS = new SubCssMetaData<CornerRadii[]>("-fx-background-radius", (StyleConverter)CornerRadiiConverter.getInstance(), new CornerRadii[]{CornerRadii.EMPTY});
    static final CssMetaData<Node, Insets[]> BACKGROUND_INSETS = new SubCssMetaData<Insets[]>("-fx-background-insets", (StyleConverter)InsetsConverter.SequenceConverter.getInstance(), new Insets[]{Insets.EMPTY});
    static final CssMetaData<Node, javafx.scene.image.Image[]> BACKGROUND_IMAGE = new SubCssMetaData<javafx.scene.image.Image[]>("-fx-background-image", (StyleConverter)URLConverter.SequenceConverter.getInstance());
    static final CssMetaData<Node, RepeatStruct[]> BACKGROUND_REPEAT = new SubCssMetaData<RepeatStruct[]>("-fx-background-repeat", (StyleConverter)RepeatStructConverter.getInstance(), new RepeatStruct[]{new RepeatStruct(BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT)});
    static final CssMetaData<Node, BackgroundPosition[]> BACKGROUND_POSITION = new SubCssMetaData<BackgroundPosition[]>("-fx-background-position", (StyleConverter)LayeredBackgroundPositionConverter.getInstance(), new BackgroundPosition[]{BackgroundPosition.DEFAULT});
    static final CssMetaData<Node, BackgroundSize[]> BACKGROUND_SIZE = new SubCssMetaData<BackgroundSize[]>("-fx-background-size", (StyleConverter)LayeredBackgroundSizeConverter.getInstance(), new BackgroundSize[]{BackgroundSize.DEFAULT});
    private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES = Collections.unmodifiableList(Arrays.asList(BACKGROUND_COLOR, BACKGROUND_INSETS, BACKGROUND_RADIUS, BACKGROUND_IMAGE, BACKGROUND_REPEAT, BACKGROUND_POSITION, BACKGROUND_SIZE));
    public static final Background EMPTY = new Background((BackgroundFill[])null, null);
    final List<BackgroundFill> fills;
    final List<BackgroundImage> images;
    final Insets outsets;
    private final boolean hasOpaqueFill;
    private final double opaqueFillTop;
    private final double opaqueFillRight;
    private final double opaqueFillBottom;
    private final double opaqueFillLeft;
    final boolean hasPercentageBasedOpaqueFills;
    final boolean hasPercentageBasedFills;
    private final int hash;

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return STYLEABLES;
    }

    public final List<BackgroundFill> getFills() {
        return this.fills;
    }

    public final List<BackgroundImage> getImages() {
        return this.images;
    }

    public final Insets getOutsets() {
        return this.outsets;
    }

    public final boolean isEmpty() {
        return this.fills.isEmpty() && this.images.isEmpty();
    }

    public Background(BackgroundFill ... backgroundFillArray) {
        this(backgroundFillArray, (BackgroundImage[])null);
    }

    public Background(BackgroundImage ... backgroundImageArray) {
        this((BackgroundFill[])null, backgroundImageArray);
    }

    public Background(@NamedArg(value="fills") List<BackgroundFill> list, @NamedArg(value="images") List<BackgroundImage> list2) {
        this(list == null ? null : list.toArray(new BackgroundFill[list.size()]), list2 == null ? null : list2.toArray(new BackgroundImage[list2.size()]));
    }

    public Background(@NamedArg(value="fills") BackgroundFill[] backgroundFillArray, @NamedArg(value="images") BackgroundImage[] backgroundImageArray) {
        Object object;
        int n;
        int n2;
        Object[] objectArray;
        double d = 0.0;
        double d2 = 0.0;
        double d3 = 0.0;
        double d4 = 0.0;
        boolean bl = false;
        boolean bl2 = false;
        boolean bl3 = false;
        if (backgroundFillArray == null || backgroundFillArray.length == 0) {
            this.fills = Collections.emptyList();
        } else {
            objectArray = new BackgroundFill[backgroundFillArray.length];
            n2 = 0;
            for (n = 0; n < backgroundFillArray.length; ++n) {
                object = backgroundFillArray[n];
                if (object == null) continue;
                objectArray[n2++] = object;
                Insets insets = ((BackgroundFill)object).getInsets();
                double d5 = insets.getTop();
                double d6 = insets.getRight();
                double d7 = insets.getBottom();
                double d8 = insets.getLeft();
                d = d <= d5 ? d : d5;
                d2 = d2 <= d6 ? d2 : d6;
                d3 = d3 <= d7 ? d3 : d7;
                d4 = d4 <= d8 ? d4 : d8;
                boolean bl4 = ((BackgroundFill)object).getRadii().hasPercentBasedRadii;
                bl2 |= bl4;
                if (!((BackgroundFill)object).fill.isOpaque()) continue;
                bl3 = true;
                if (!bl4) continue;
                bl = true;
            }
            this.fills = new UnmodifiableArrayList<BackgroundFill>(objectArray, n2);
        }
        this.hasPercentageBasedFills = bl2;
        this.outsets = new Insets(Math.max(0.0, -d), Math.max(0.0, -d2), Math.max(0.0, -d3), Math.max(0.0, -d4));
        if (backgroundImageArray == null || backgroundImageArray.length == 0) {
            this.images = Collections.emptyList();
        } else {
            objectArray = new BackgroundImage[backgroundImageArray.length];
            n2 = 0;
            for (n = 0; n < backgroundImageArray.length; ++n) {
                object = backgroundImageArray[n];
                if (object == null) continue;
                objectArray[n2++] = object;
            }
            this.images = new UnmodifiableArrayList<Object>(objectArray, n2);
        }
        this.hasOpaqueFill = bl3;
        if (bl) {
            this.opaqueFillTop = Double.NaN;
            this.opaqueFillRight = Double.NaN;
            this.opaqueFillBottom = Double.NaN;
            this.opaqueFillLeft = Double.NaN;
        } else {
            objectArray = new double[4];
            this.computeOpaqueInsets(1.0, 1.0, true, (double[])objectArray);
            this.opaqueFillTop = (double)objectArray[0];
            this.opaqueFillRight = (double)objectArray[1];
            this.opaqueFillBottom = (double)objectArray[2];
            this.opaqueFillLeft = (double)objectArray[3];
        }
        this.hasPercentageBasedOpaqueFills = bl;
        int n3 = this.fills.hashCode();
        this.hash = n3 = 31 * n3 + this.images.hashCode();
    }

    public boolean isFillPercentageBased() {
        return this.hasPercentageBasedFills;
    }

    void computeOpaqueInsets(double d, double d2, double[] dArray) {
        this.computeOpaqueInsets(d, d2, false, dArray);
    }

    private void computeOpaqueInsets(double d, double d2, boolean bl, double[] dArray) {
        double d3;
        double d4;
        double d5;
        double d6;
        double d7;
        double d8 = Double.NaN;
        double d9 = Double.NaN;
        double d10 = Double.NaN;
        double d11 = Double.NaN;
        if (this.hasOpaqueFill) {
            if (!bl && !this.hasPercentageBasedOpaqueFills) {
                d8 = this.opaqueFillTop;
                d9 = this.opaqueFillRight;
                d10 = this.opaqueFillBottom;
                d11 = this.opaqueFillLeft;
            } else {
                int n = this.fills.size();
                for (int i = 0; i < n; ++i) {
                    boolean bl2;
                    BackgroundFill object = this.fills.get(i);
                    Insets bl8 = object.getInsets();
                    double d12 = bl8.getTop();
                    double d13 = bl8.getRight();
                    double d14 = bl8.getBottom();
                    double d15 = bl8.getLeft();
                    if (!object.fill.isOpaque()) continue;
                    CornerRadii cornerRadii = object.getRadii();
                    d7 = cornerRadii.isTopLeftHorizontalRadiusAsPercentage() ? d * cornerRadii.getTopLeftHorizontalRadius() : cornerRadii.getTopLeftHorizontalRadius();
                    d6 = cornerRadii.isTopLeftVerticalRadiusAsPercentage() ? d2 * cornerRadii.getTopLeftVerticalRadius() : cornerRadii.getTopLeftVerticalRadius();
                    d5 = cornerRadii.isTopRightVerticalRadiusAsPercentage() ? d2 * cornerRadii.getTopRightVerticalRadius() : cornerRadii.getTopRightVerticalRadius();
                    d4 = cornerRadii.isTopRightHorizontalRadiusAsPercentage() ? d * cornerRadii.getTopRightHorizontalRadius() : cornerRadii.getTopRightHorizontalRadius();
                    d3 = cornerRadii.isBottomRightHorizontalRadiusAsPercentage() ? d * cornerRadii.getBottomRightHorizontalRadius() : cornerRadii.getBottomRightHorizontalRadius();
                    double d16 = cornerRadii.isBottomRightVerticalRadiusAsPercentage() ? d2 * cornerRadii.getBottomRightVerticalRadius() : cornerRadii.getBottomRightVerticalRadius();
                    double d17 = cornerRadii.isBottomLeftVerticalRadiusAsPercentage() ? d2 * cornerRadii.getBottomLeftVerticalRadius() : cornerRadii.getBottomLeftVerticalRadius();
                    double d18 = cornerRadii.isBottomLeftHorizontalRadiusAsPercentage() ? d * cornerRadii.getBottomLeftHorizontalRadius() : cornerRadii.getBottomLeftHorizontalRadius();
                    double d19 = d12 + Math.max(d6, d5) / 2.0;
                    double d20 = d13 + Math.max(d4, d3) / 2.0;
                    double d21 = d14 + Math.max(d17, d16) / 2.0;
                    double d22 = d15 + Math.max(d7, d18) / 2.0;
                    if (Double.isNaN(d8)) {
                        d8 = d19;
                        d9 = d20;
                        d10 = d21;
                        d11 = d22;
                        continue;
                    }
                    boolean bl3 = d19 >= d8;
                    boolean bl4 = d20 >= d9;
                    boolean bl5 = d21 >= d10;
                    boolean bl6 = bl2 = d22 >= d11;
                    if (bl3 && bl4 && bl5 && bl2) continue;
                    if (!(bl3 || bl4 || bl5 || bl2)) {
                        d8 = d12;
                        d9 = d13;
                        d10 = d14;
                        d11 = d15;
                        continue;
                    }
                    if (d22 == d11 && d20 == d9) {
                        d8 = Math.min(d19, d8);
                        d10 = Math.min(d21, d10);
                        continue;
                    }
                    if (d19 != d8 || d21 != d10) continue;
                    d11 = Math.min(d22, d11);
                    d9 = Math.min(d20, d9);
                }
            }
        }
        Toolkit.ImageAccessor imageAccessor = Toolkit.getImageAccessor();
        for (BackgroundImage backgroundImage : this.images) {
            boolean bl7;
            if (backgroundImage.opaque == null) {
                PlatformImage platformImage = (PlatformImage)imageAccessor.getImageProperty(backgroundImage.image).get();
                if (platformImage == null || !(platformImage instanceof Image)) continue;
                backgroundImage.opaque = ((Image)platformImage).isOpaque();
            }
            if (!backgroundImage.opaque.booleanValue()) continue;
            if (backgroundImage.size.cover || backgroundImage.size.height == -1.0 && backgroundImage.size.width == -1.0 && backgroundImage.size.widthAsPercentage && backgroundImage.size.heightAsPercentage) {
                d8 = Double.isNaN(d8) ? 0.0 : Math.min(0.0, d8);
                d9 = Double.isNaN(d9) ? 0.0 : Math.min(0.0, d9);
                d10 = Double.isNaN(d10) ? 0.0 : Math.min(0.0, d10);
                d11 = Double.isNaN(d11) ? 0.0 : Math.min(0.0, d11);
                break;
            }
            if (backgroundImage.repeatX == BackgroundRepeat.SPACE || backgroundImage.repeatY == BackgroundRepeat.SPACE) {
                backgroundImage.opaque = false;
                continue;
            }
            boolean bl8 = backgroundImage.repeatX == BackgroundRepeat.REPEAT || backgroundImage.repeatX == BackgroundRepeat.ROUND;
            boolean bl9 = bl7 = backgroundImage.repeatY == BackgroundRepeat.REPEAT || backgroundImage.repeatY == BackgroundRepeat.ROUND;
            if (bl8 && bl7) {
                d8 = Double.isNaN(d8) ? 0.0 : Math.min(0.0, d8);
                d9 = Double.isNaN(d9) ? 0.0 : Math.min(0.0, d9);
                d10 = Double.isNaN(d10) ? 0.0 : Math.min(0.0, d10);
                d11 = Double.isNaN(d11) ? 0.0 : Math.min(0.0, d11);
                break;
            }
            double d23 = backgroundImage.size.widthAsPercentage ? backgroundImage.size.width * d : backgroundImage.size.width;
            double d24 = backgroundImage.size.heightAsPercentage ? backgroundImage.size.height * d2 : backgroundImage.size.height;
            double d25 = backgroundImage.image.getWidth();
            double d26 = backgroundImage.image.getHeight();
            if (backgroundImage.size.contain) {
                d5 = d / d25;
                d4 = d2 / d26;
                d3 = Math.min(d5, d4);
                d7 = Math.ceil(d3 * d25);
                d6 = Math.ceil(d3 * d26);
            } else if (backgroundImage.size.width >= 0.0 && backgroundImage.size.height >= 0.0) {
                d7 = d23;
                d6 = d24;
            } else if (d23 >= 0.0) {
                d7 = d23;
                d5 = d7 / d25;
                d6 = d26 * d5;
            } else if (d24 >= 0.0) {
                d6 = d24;
                d5 = d6 / d26;
                d7 = d25 * d5;
            } else {
                d7 = d25;
                d6 = d26;
            }
            d8 = Double.isNaN(d8) ? 0.0 : Math.min(0.0, d8);
            d9 = Double.isNaN(d9) ? d - d7 : Math.min(d - d7, d9);
            d10 = Double.isNaN(d10) ? d2 - d6 : Math.min(d2 - d6, d10);
            d11 = Double.isNaN(d11) ? 0.0 : Math.min(0.0, d11);
        }
        dArray[0] = d8;
        dArray[1] = d9;
        dArray[2] = d10;
        dArray[3] = d11;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        Background background = (Background)object;
        if (this.hash != background.hash) {
            return false;
        }
        if (!this.fills.equals(background.fills)) {
            return false;
        }
        return this.images.equals(background.images);
    }

    public int hashCode() {
        return this.hash;
    }
}

