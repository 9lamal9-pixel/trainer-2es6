/*
 * Decompiled with CFR 0.152.
 */
package javafx.css;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.css.CssMetaData;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.converter.FontConverter;
import javafx.css.converter.SizeConverter;
import javafx.css.converter.StringConverter;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

public abstract class FontCssMetaData<S extends Styleable>
extends CssMetaData<S, Font> {
    public FontCssMetaData(String string, Font font) {
        super(string, FontConverter.getInstance(), font, true, FontCssMetaData.createSubProperties(string, font));
    }

    private static <S extends Styleable> List<CssMetaData<? extends Styleable, ?>> createSubProperties(String string, Font font) {
        ArrayList<CssMetaData> arrayList = new ArrayList<CssMetaData>();
        Font font2 = font != null ? font : Font.getDefault();
        CssMetaData cssMetaData = new CssMetaData<S, String>(string.concat("-family"), StringConverter.getInstance(), font2.getFamily(), true){

            @Override
            public boolean isSettable(S s) {
                return false;
            }

            @Override
            public StyleableProperty<String> getStyleableProperty(S s) {
                return null;
            }
        };
        arrayList.add(cssMetaData);
        CssMetaData cssMetaData2 = new CssMetaData<S, Number>(string.concat("-size"), SizeConverter.getInstance(), font2.getSize(), true){

            @Override
            public boolean isSettable(S s) {
                return false;
            }

            @Override
            public StyleableProperty<Number> getStyleableProperty(S s) {
                return null;
            }
        };
        arrayList.add(cssMetaData2);
        CssMetaData cssMetaData3 = new CssMetaData<S, FontPosture>(string.concat("-style"), (StyleConverter)FontConverter.FontStyleConverter.getInstance(), FontPosture.REGULAR, true){

            @Override
            public boolean isSettable(S s) {
                return false;
            }

            @Override
            public StyleableProperty<FontPosture> getStyleableProperty(S s) {
                return null;
            }
        };
        arrayList.add(cssMetaData3);
        CssMetaData cssMetaData4 = new CssMetaData<S, FontWeight>(string.concat("-weight"), (StyleConverter)FontConverter.FontWeightConverter.getInstance(), FontWeight.NORMAL, true){

            @Override
            public boolean isSettable(S s) {
                return false;
            }

            @Override
            public StyleableProperty<FontWeight> getStyleableProperty(S s) {
                return null;
            }
        };
        arrayList.add(cssMetaData4);
        return Collections.unmodifiableList(arrayList);
    }
}

