/*
 * Decompiled with CFR 0.152.
 */
package com.sun.webkit;

import com.sun.javafx.scene.control.CustomColorDialog;
import com.sun.webkit.WebPage;
import com.sun.webkit.WebPageClient;
import javafx.scene.paint.Color;
import javafx.scene.web.WebView;

public final class ColorChooser {
    private static final double COLOR_DOUBLE_TO_UCHAR_FACTOR = 255.0;
    private final CustomColorDialog colorChooserDialog;
    private final long pdata;

    private ColorChooser(WebPage webPage, Color color, long l) {
        this.pdata = l;
        WebPageClient webPageClient = webPage.getPageClient();
        assert (webPageClient != null);
        this.colorChooserDialog = new CustomColorDialog(((WebView)webPageClient.getContainer()).getScene().getWindow());
        this.colorChooserDialog.setSaveBtnToOk();
        this.colorChooserDialog.setShowUseBtn(false);
        this.colorChooserDialog.setShowOpacitySlider(false);
        this.colorChooserDialog.setOnSave(() -> this.twkSetSelectedColor(this.pdata, (int)Math.round(this.colorChooserDialog.getCustomColor().getRed() * 255.0), (int)Math.round(this.colorChooserDialog.getCustomColor().getGreen() * 255.0), (int)Math.round(this.colorChooserDialog.getCustomColor().getBlue() * 255.0)));
        this.colorChooserDialog.setCurrentColor(color);
        this.colorChooserDialog.show();
    }

    private static ColorChooser fwkCreateAndShowColorChooser(WebPage webPage, int n, int n2, int n3, long l) {
        return new ColorChooser(webPage, Color.rgb(n, n2, n3), l);
    }

    private void fwkShowColorChooser(int n, int n2, int n3) {
        this.colorChooserDialog.setCurrentColor(Color.rgb(n, n2, n3));
        this.colorChooserDialog.show();
    }

    private void fwkHideColorChooser() {
        this.colorChooserDialog.hide();
    }

    private native void twkSetSelectedColor(long var1, int var3, int var4, int var5);
}

