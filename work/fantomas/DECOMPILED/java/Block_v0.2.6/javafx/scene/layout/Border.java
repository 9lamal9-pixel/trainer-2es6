/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import com.sun.javafx.UnmodifiableArrayList;
import com.sun.javafx.css.SubCssMetaData;
import com.sun.javafx.scene.layout.region.BorderImageSlices;
import com.sun.javafx.scene.layout.region.BorderImageWidthConverter;
import com.sun.javafx.scene.layout.region.CornerRadiiConverter;
import com.sun.javafx.scene.layout.region.LayeredBorderPaintConverter;
import com.sun.javafx.scene.layout.region.LayeredBorderStyleConverter;
import com.sun.javafx.scene.layout.region.Margins;
import com.sun.javafx.scene.layout.region.RepeatStruct;
import com.sun.javafx.scene.layout.region.RepeatStructConverter;
import com.sun.javafx.scene.layout.region.SliceSequenceConverter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javafx.beans.NamedArg;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.converter.InsetsConverter;
import javafx.css.converter.URLConverter;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BorderImage;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Paint;

public final class Border {
    static final CssMetaData<Node, Paint[]> BORDER_COLOR = new SubCssMetaData<Paint[]>("-fx-border-color", (StyleConverter)LayeredBorderPaintConverter.getInstance());
    static final CssMetaData<Node, BorderStrokeStyle[][]> BORDER_STYLE = new SubCssMetaData<BorderStrokeStyle[][]>("-fx-border-style", (StyleConverter)LayeredBorderStyleConverter.getInstance());
    static final CssMetaData<Node, Margins[]> BORDER_WIDTH = new SubCssMetaData<Margins[]>("-fx-border-width", (StyleConverter)Margins.SequenceConverter.getInstance());
    static final CssMetaData<Node, CornerRadii[]> BORDER_RADIUS = new SubCssMetaData<CornerRadii[]>("-fx-border-radius", (StyleConverter)CornerRadiiConverter.getInstance());
    static final CssMetaData<Node, Insets[]> BORDER_INSETS = new SubCssMetaData<Insets[]>("-fx-border-insets", (StyleConverter)InsetsConverter.SequenceConverter.getInstance());
    static final CssMetaData<Node, String[]> BORDER_IMAGE_SOURCE = new SubCssMetaData<String[]>("-fx-border-image-source", (StyleConverter)URLConverter.SequenceConverter.getInstance());
    static final CssMetaData<Node, RepeatStruct[]> BORDER_IMAGE_REPEAT = new SubCssMetaData<RepeatStruct[]>("-fx-border-image-repeat", (StyleConverter)RepeatStructConverter.getInstance(), new RepeatStruct[]{new RepeatStruct(BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT)});
    static final CssMetaData<Node, BorderImageSlices[]> BORDER_IMAGE_SLICE = new SubCssMetaData<BorderImageSlices[]>("-fx-border-image-slice", (StyleConverter)SliceSequenceConverter.getInstance(), new BorderImageSlices[]{BorderImageSlices.DEFAULT});
    static final CssMetaData<Node, BorderWidths[]> BORDER_IMAGE_WIDTH = new SubCssMetaData<BorderWidths[]>("-fx-border-image-width", (StyleConverter)BorderImageWidthConverter.getInstance(), new BorderWidths[]{BorderWidths.DEFAULT});
    static final CssMetaData<Node, Insets[]> BORDER_IMAGE_INSETS = new SubCssMetaData<Insets[]>("-fx-border-image-insets", (StyleConverter)InsetsConverter.SequenceConverter.getInstance(), new Insets[]{Insets.EMPTY});
    private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES = Collections.unmodifiableList(Arrays.asList(BORDER_COLOR, BORDER_STYLE, BORDER_WIDTH, BORDER_RADIUS, BORDER_INSETS, BORDER_IMAGE_SOURCE, BORDER_IMAGE_REPEAT, BORDER_IMAGE_SLICE, BORDER_IMAGE_WIDTH, BORDER_IMAGE_INSETS));
    public static final Border EMPTY = new Border((BorderStroke[])null, null);
    final List<BorderStroke> strokes;
    final List<BorderImage> images;
    final Insets outsets;
    final Insets insets;
    private final int hash;

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return STYLEABLES;
    }

    public final List<BorderStroke> getStrokes() {
        return this.strokes;
    }

    public final List<BorderImage> getImages() {
        return this.images;
    }

    public final Insets getOutsets() {
        return this.outsets;
    }

    public final Insets getInsets() {
        return this.insets;
    }

    public final boolean isEmpty() {
        return this.strokes.isEmpty() && this.images.isEmpty();
    }

    public Border(BorderStroke ... borderStrokeArray) {
        this(borderStrokeArray, (BorderImage[])null);
    }

    public Border(BorderImage ... borderImageArray) {
        this((BorderStroke[])null, borderImageArray);
    }

    public Border(@NamedArg(value="strokes") List<BorderStroke> list, @NamedArg(value="images") List<BorderImage> list2) {
        this(list == null ? null : list.toArray(new BorderStroke[list.size()]), list2 == null ? null : list2.toArray(new BorderImage[list2.size()]));
    }

    public Border(@NamedArg(value="strokes") BorderStroke[] borderStrokeArray, @NamedArg(value="images") BorderImage[] borderImageArray) {
        double d;
        double d2;
        double d3;
        double d4;
        double d5;
        double d6;
        double d7;
        double d8;
        Object object;
        int n;
        int n2;
        Object[] objectArray;
        double d9 = 0.0;
        double d10 = 0.0;
        double d11 = 0.0;
        double d12 = 0.0;
        double d13 = 0.0;
        double d14 = 0.0;
        double d15 = 0.0;
        double d16 = 0.0;
        if (borderStrokeArray == null || borderStrokeArray.length == 0) {
            this.strokes = Collections.emptyList();
        } else {
            objectArray = new BorderStroke[borderStrokeArray.length];
            n2 = 0;
            for (n = 0; n < borderStrokeArray.length; ++n) {
                object = borderStrokeArray[n];
                if (object == null) continue;
                objectArray[n2++] = object;
                d8 = ((BorderStroke)object).innerEdge.getTop();
                d7 = ((BorderStroke)object).innerEdge.getRight();
                d6 = ((BorderStroke)object).innerEdge.getBottom();
                d5 = ((BorderStroke)object).innerEdge.getLeft();
                d9 = d9 >= d8 ? d9 : d8;
                d10 = d10 >= d7 ? d10 : d7;
                d11 = d11 >= d6 ? d11 : d6;
                d12 = d12 >= d5 ? d12 : d5;
                d4 = ((BorderStroke)object).outerEdge.getTop();
                d3 = ((BorderStroke)object).outerEdge.getRight();
                d2 = ((BorderStroke)object).outerEdge.getBottom();
                d = ((BorderStroke)object).outerEdge.getLeft();
                d13 = d13 >= d4 ? d13 : d4;
                d14 = d14 >= d3 ? d14 : d3;
                d15 = d15 >= d2 ? d15 : d2;
                d16 = d16 >= d ? d16 : d;
            }
            this.strokes = new UnmodifiableArrayList<Object>(objectArray, n2);
        }
        if (borderImageArray == null || borderImageArray.length == 0) {
            this.images = Collections.emptyList();
        } else {
            objectArray = new BorderImage[borderImageArray.length];
            n2 = 0;
            for (n = 0; n < borderImageArray.length; ++n) {
                object = borderImageArray[n];
                if (object == null) continue;
                objectArray[n2++] = object;
                d8 = ((BorderImage)object).innerEdge.getTop();
                d7 = ((BorderImage)object).innerEdge.getRight();
                d6 = ((BorderImage)object).innerEdge.getBottom();
                d5 = ((BorderImage)object).innerEdge.getLeft();
                d9 = d9 >= d8 ? d9 : d8;
                d10 = d10 >= d7 ? d10 : d7;
                d11 = d11 >= d6 ? d11 : d6;
                d12 = d12 >= d5 ? d12 : d5;
                d4 = ((BorderImage)object).outerEdge.getTop();
                d3 = ((BorderImage)object).outerEdge.getRight();
                d2 = ((BorderImage)object).outerEdge.getBottom();
                d = ((BorderImage)object).outerEdge.getLeft();
                d13 = d13 >= d4 ? d13 : d4;
                d14 = d14 >= d3 ? d14 : d3;
                d15 = d15 >= d2 ? d15 : d2;
                d16 = d16 >= d ? d16 : d;
            }
            this.images = new UnmodifiableArrayList<Object>(objectArray, n2);
        }
        this.outsets = new Insets(d13, d14, d15, d16);
        this.insets = new Insets(d9, d10, d11, d12);
        int n3 = this.strokes.hashCode();
        this.hash = n3 = 31 * n3 + this.images.hashCode();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        Border border = (Border)object;
        if (this.hash != border.hash) {
            return false;
        }
        if (!this.images.equals(border.images)) {
            return false;
        }
        return this.strokes.equals(border.strokes);
    }

    public int hashCode() {
        return this.hash;
    }
}

