/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import com.sun.javafx.css.StyleManager;
import com.sun.javafx.scene.layout.region.BorderImageSlices;
import com.sun.javafx.scene.layout.region.Margins;
import com.sun.javafx.scene.layout.region.RepeatStruct;
import java.util.Map;
import javafx.css.CssMetaData;
import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderImage;
import javafx.scene.layout.BorderRepeat;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

class BorderConverter
extends StyleConverter<ParsedValue[], Border> {
    private static final BorderConverter BORDER_IMAGE_CONVERTER = new BorderConverter();

    public static BorderConverter getInstance() {
        return BORDER_IMAGE_CONVERTER;
    }

    private BorderConverter() {
    }

    @Override
    public Border convert(Map<CssMetaData<? extends Styleable, ?>, Object> map) {
        BorderWidths borderWidths;
        Object object;
        Object object2;
        Object object3;
        Object object4;
        int n;
        int n2;
        Insets[] insetsArray;
        int n3;
        Object[] objectArray;
        int n4;
        Object[] objectArray2;
        boolean bl;
        Paint[][] paintArray = (Paint[][])map.get(Border.BORDER_COLOR);
        BorderStrokeStyle[][] borderStrokeStyleArray = (BorderStrokeStyle[][])map.get(Border.BORDER_STYLE);
        String[] stringArray = (String[])map.get(Border.BORDER_IMAGE_SOURCE);
        boolean bl2 = paintArray != null && paintArray.length > 0 || borderStrokeStyleArray != null && borderStrokeStyleArray.length > 0;
        boolean bl3 = bl = stringArray != null && stringArray.length > 0;
        if (!bl2 && !bl) {
            return null;
        }
        BorderStroke[] borderStrokeArray = null;
        if (bl2) {
            int n5 = paintArray != null ? paintArray.length - 1 : -1;
            int n6 = borderStrokeStyleArray != null ? borderStrokeStyleArray.length - 1 : -1;
            int n7 = (n5 >= n6 ? n5 : n6) + 1;
            Object object5 = map.get(Border.BORDER_WIDTH);
            objectArray2 = object5 == null ? new Margins[]{} : (Margins[])object5;
            n4 = objectArray2.length - 1;
            object5 = map.get(Border.BORDER_RADIUS);
            objectArray = object5 == null ? new CornerRadii[]{} : (CornerRadii[])object5;
            n3 = objectArray.length - 1;
            object5 = map.get(Border.BORDER_INSETS);
            insetsArray = object5 == null ? new Insets[]{} : (Insets[])object5;
            n2 = insetsArray.length - 1;
            for (n = 0; n < n7; ++n) {
                if (n6 < 0) {
                    object4 = new BorderStrokeStyle[4];
                    object4[2] = object4[3] = BorderStrokeStyle.SOLID;
                    object4[1] = object4[3];
                    object4[0] = object4[3];
                } else {
                    object4 = borderStrokeStyleArray[n <= n6 ? n : n6];
                }
                if (object4[0] == BorderStrokeStyle.NONE && object4[1] == BorderStrokeStyle.NONE && object4[2] == BorderStrokeStyle.NONE && object4[3] == BorderStrokeStyle.NONE) continue;
                if (n5 < 0) {
                    object3 = new Paint[4];
                    object3[2] = object3[3] = Color.BLACK;
                    object3[1] = object3[3];
                    object3[0] = object3[3];
                } else {
                    object3 = paintArray[n <= n5 ? n : n5];
                }
                if (borderStrokeArray == null) {
                    borderStrokeArray = new BorderStroke[n7];
                }
                Object object6 = objectArray2.length == 0 ? null : (object2 = objectArray2[n <= n4 ? n : n4]);
                Object object7 = objectArray.length == 0 ? CornerRadii.EMPTY : (object = objectArray[n <= n3 ? n : n3]);
                borderWidths = insetsArray.length == 0 ? null : insetsArray[n <= n2 ? n : n2];
                borderStrokeArray[n] = new BorderStroke(object3[0], object3[1], object3[2], object3[3], object4[0], object4[1], object4[2], object4[3], (CornerRadii)object, object2 == null ? BorderStroke.DEFAULT_WIDTHS : new BorderWidths(((Margins)object2).getTop(), ((Margins)object2).getRight(), ((Margins)object2).getBottom(), ((Margins)object2).getLeft()), (Insets)((Object)borderWidths));
            }
        }
        BorderImage[] borderImageArray = null;
        if (bl) {
            borderImageArray = new BorderImage[stringArray.length];
            Object object8 = map.get(Border.BORDER_IMAGE_REPEAT);
            RepeatStruct[] repeatStructArray = object8 == null ? new RepeatStruct[]{} : (RepeatStruct[])object8;
            int n8 = repeatStructArray.length - 1;
            object8 = map.get(Border.BORDER_IMAGE_SLICE);
            objectArray2 = object8 == null ? new BorderImageSlices[]{} : (BorderImageSlices[])object8;
            n4 = objectArray2.length - 1;
            object8 = map.get(Border.BORDER_IMAGE_WIDTH);
            objectArray = object8 == null ? new BorderWidths[]{} : (BorderWidths[])object8;
            n3 = objectArray.length - 1;
            object8 = map.get(Border.BORDER_IMAGE_INSETS);
            insetsArray = object8 == null ? new Insets[]{} : (Insets[])object8;
            n2 = insetsArray.length - 1;
            for (n = 0; n < stringArray.length; ++n) {
                if (stringArray[n] == null) continue;
                object4 = BorderRepeat.STRETCH;
                object3 = BorderRepeat.STRETCH;
                if (repeatStructArray.length > 0) {
                    object2 = repeatStructArray[n <= n8 ? n : n8];
                    switch (((RepeatStruct)object2).repeatX) {
                        case SPACE: {
                            object4 = BorderRepeat.SPACE;
                            break;
                        }
                        case ROUND: {
                            object4 = BorderRepeat.ROUND;
                            break;
                        }
                        case REPEAT: {
                            object4 = BorderRepeat.REPEAT;
                            break;
                        }
                        case NO_REPEAT: {
                            object4 = BorderRepeat.STRETCH;
                        }
                    }
                    switch (((RepeatStruct)object2).repeatY) {
                        case SPACE: {
                            object3 = BorderRepeat.SPACE;
                            break;
                        }
                        case ROUND: {
                            object3 = BorderRepeat.ROUND;
                            break;
                        }
                        case REPEAT: {
                            object3 = BorderRepeat.REPEAT;
                            break;
                        }
                        case NO_REPEAT: {
                            object3 = BorderRepeat.STRETCH;
                        }
                    }
                }
                Object object9 = objectArray2.length > 0 ? objectArray2[n <= n4 ? n : n4] : (object2 = BorderImageSlices.DEFAULT);
                Object object10 = insetsArray.length > 0 ? insetsArray[n <= n2 ? n : n2] : (object = Insets.EMPTY);
                borderWidths = objectArray.length > 0 ? objectArray[n <= n3 ? n : n3] : BorderWidths.DEFAULT;
                Image image = StyleManager.getInstance().getCachedImage(stringArray[n]);
                borderImageArray[n] = new BorderImage(image, borderWidths, (Insets)object, ((BorderImageSlices)object2).widths, ((BorderImageSlices)object2).filled, (BorderRepeat)((Object)object4), (BorderRepeat)((Object)object3));
            }
        }
        return borderStrokeArray == null && borderImageArray == null ? null : new Border(borderStrokeArray, borderImageArray);
    }

    public String toString() {
        return "BorderConverter";
    }
}

