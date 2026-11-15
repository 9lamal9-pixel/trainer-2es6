/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.layout;

import com.sun.javafx.css.StyleManager;
import com.sun.javafx.scene.layout.region.RepeatStruct;
import java.util.Map;
import javafx.css.CssMetaData;
import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Paint;

class BackgroundConverter
extends StyleConverter<ParsedValue[], Background> {
    static final StyleConverter<ParsedValue[], Background> INSTANCE = new BackgroundConverter();

    BackgroundConverter() {
    }

    @Override
    public Background convert(Map<CssMetaData<? extends Styleable, ?>, Object> map) {
        int n;
        Object[] objectArray;
        Object object;
        Object object2;
        boolean bl;
        Paint[] paintArray = (Paint[])map.get(Background.BACKGROUND_COLOR);
        String[] stringArray = (String[])map.get(Background.BACKGROUND_IMAGE);
        boolean bl2 = paintArray != null && paintArray.length > 0;
        boolean bl3 = bl = stringArray != null && stringArray.length > 0;
        if (!bl2 && !bl) {
            return null;
        }
        BackgroundFill[] backgroundFillArray = null;
        if (bl2) {
            backgroundFillArray = new BackgroundFill[paintArray.length];
            object2 = map.get(Background.BACKGROUND_INSETS);
            object = object2 == null ? new Insets[]{} : (Insets[])object2;
            object2 = map.get(Background.BACKGROUND_RADIUS);
            objectArray = object2 == null ? new CornerRadii[]{} : (CornerRadii[])object2;
            int n2 = ((Insets[])object).length - 1;
            int n3 = objectArray.length - 1;
            for (n = 0; n < paintArray.length; ++n) {
                Insets insets;
                Insets insets2 = ((Insets[])object).length > 0 ? object[n <= n2 ? n : n2] : (insets = Insets.EMPTY);
                CornerRadii cornerRadii = objectArray.length > 0 ? objectArray[n <= n3 ? n : n3] : CornerRadii.EMPTY;
                backgroundFillArray[n] = new BackgroundFill(paintArray[n], cornerRadii, insets);
            }
        }
        object2 = null;
        if (bl) {
            object2 = new BackgroundImage[stringArray.length];
            object = map.get(Background.BACKGROUND_REPEAT);
            objectArray = object == null ? new RepeatStruct[]{} : (RepeatStruct[])object;
            object = map.get(Background.BACKGROUND_POSITION);
            BackgroundPosition[] backgroundPositionArray = object == null ? new BackgroundPosition[]{} : (BackgroundPosition[])object;
            object = map.get(Background.BACKGROUND_SIZE);
            BackgroundSize[] backgroundSizeArray = object == null ? new BackgroundSize[]{} : (BackgroundSize[])object;
            n = objectArray.length - 1;
            int n4 = backgroundPositionArray.length - 1;
            int n5 = backgroundSizeArray.length - 1;
            for (int i = 0; i < stringArray.length; ++i) {
                BackgroundPosition backgroundPosition;
                Object object3;
                Image image;
                if (stringArray[i] == null || (image = StyleManager.getInstance().getCachedImage(stringArray[i])) == null) continue;
                Object object4 = objectArray.length > 0 ? objectArray[i <= n ? i : n] : (object3 = null);
                BackgroundPosition backgroundPosition2 = backgroundPositionArray.length > 0 ? backgroundPositionArray[i <= n4 ? i : n4] : (backgroundPosition = null);
                BackgroundSize backgroundSize = backgroundSizeArray.length > 0 ? backgroundSizeArray[i <= n5 ? i : n5] : null;
                object2[i] = new BackgroundImage(image, object3 == null ? null : ((RepeatStruct)object3).repeatX, object3 == null ? null : ((RepeatStruct)object3).repeatY, backgroundPosition, backgroundSize);
            }
        }
        return new Background(backgroundFillArray, (BackgroundImage[])object2);
    }
}

