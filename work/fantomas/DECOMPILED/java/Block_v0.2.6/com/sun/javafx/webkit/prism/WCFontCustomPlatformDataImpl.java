/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.webkit.prism;

import com.sun.javafx.font.FontFactory;
import com.sun.javafx.font.PGFont;
import com.sun.javafx.webkit.prism.WCFontImpl;
import com.sun.prism.GraphicsPipeline;
import com.sun.webkit.graphics.WCFont;
import com.sun.webkit.graphics.WCFontCustomPlatformData;
import java.io.IOException;
import java.io.InputStream;

final class WCFontCustomPlatformDataImpl
extends WCFontCustomPlatformData {
    private final PGFont font;

    WCFontCustomPlatformDataImpl(InputStream inputStream) throws IOException {
        FontFactory fontFactory = GraphicsPipeline.getPipeline().getFontFactory();
        PGFont[] pGFontArray = fontFactory.loadEmbeddedFont(null, inputStream, 10.0f, false, false);
        if (pGFontArray == null) {
            throw new IOException("Error loading font");
        }
        this.font = pGFontArray[0];
    }

    @Override
    protected WCFont createFont(int n, boolean bl, boolean bl2) {
        FontFactory fontFactory = GraphicsPipeline.getPipeline().getFontFactory();
        return new WCFontImpl(fontFactory.deriveFont(this.font, bl, bl2, n));
    }
}

