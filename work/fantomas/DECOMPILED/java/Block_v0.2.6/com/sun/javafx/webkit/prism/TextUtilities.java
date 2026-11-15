/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.webkit.prism;

import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.RectBounds;
import com.sun.javafx.scene.text.TextLayout;
import com.sun.javafx.text.PrismTextLayoutFactory;
import com.sun.javafx.text.TextRun;

final class TextUtilities {
    TextUtilities() {
    }

    static TextLayout createLayout(String string, Object object) {
        TextLayout textLayout = PrismTextLayoutFactory.getFactory().createLayout();
        textLayout.setContent(string, object);
        return textLayout;
    }

    static BaseBounds getLayoutBounds(String string, Object object) {
        return TextUtilities.createLayout(string, object).getBounds();
    }

    static float getLayoutWidth(String string, Object object) {
        return TextUtilities.getLayoutBounds(string, object).getWidth();
    }

    static TextRun createGlyphList(int[] nArray, float[] fArray, float f, float f2) {
        TextRun textRun = new TextRun(0, nArray.length, 0, true, 0, null, 0, false){

            @Override
            public RectBounds getLineBounds() {
                return new RectBounds();
            }
        };
        textRun.shape(nArray.length, nArray, fArray);
        textRun.setLocation(f, f2);
        return textRun;
    }
}

