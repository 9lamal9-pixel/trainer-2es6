/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.font.freetype;

import com.sun.javafx.font.FontStrike;
import com.sun.javafx.font.PGFont;
import com.sun.javafx.font.PrismFontFactory;
import com.sun.javafx.font.PrismFontFile;
import com.sun.javafx.font.freetype.FTFontFile;
import com.sun.javafx.font.freetype.HBGlyphLayout;
import com.sun.javafx.font.freetype.OSFreetype;
import com.sun.javafx.font.freetype.OSPango;
import com.sun.javafx.font.freetype.PangoGlyphLayout;
import com.sun.javafx.text.GlyphLayout;
import com.sun.javafx.text.TextRun;

public class FTFactory
extends PrismFontFactory {
    static boolean LCD_SUPPORT;

    public static PrismFontFactory getFactory() {
        FTFactory fTFactory = null;
        long[] lArray = new long[1];
        int n = OSFreetype.FT_Init_FreeType(lArray);
        long l = lArray[0];
        int[] nArray = new int[1];
        int[] nArray2 = new int[1];
        int[] nArray3 = new int[1];
        if (n == 0) {
            fTFactory = new FTFactory();
            OSFreetype.FT_Library_Version(l, nArray, nArray2, nArray3);
            n = OSFreetype.FT_Library_SetLcdFilter(l, 1);
            LCD_SUPPORT = n == 0;
            OSFreetype.FT_Done_FreeType(l);
        }
        if (PrismFontFactory.debugFonts) {
            if (fTFactory != null) {
                String string = nArray[0] + "." + nArray2[0] + "." + nArray3[0];
                System.err.println("Freetype2 Loaded (version " + string + ")");
                String string2 = LCD_SUPPORT ? "Enabled" : "Disabled";
                System.err.println("LCD support " + string2);
            } else {
                System.err.println("Freetype2 Failed (error " + n + ")");
            }
        }
        return fTFactory;
    }

    private FTFactory() {
    }

    @Override
    protected PrismFontFile createFontFile(String string, String string2, int n, boolean bl, boolean bl2, boolean bl3, boolean bl4) throws Exception {
        return new FTFontFile(string, string2, n, bl, bl2, bl3, bl4);
    }

    @Override
    public GlyphLayout createGlyphLayout() {
        if (OSFreetype.isPangoEnabled()) {
            return new PangoGlyphLayout();
        }
        if (OSFreetype.isHarfbuzzEnabled()) {
            return new HBGlyphLayout();
        }
        return new StubGlyphLayout();
    }

    @Override
    public boolean isLCDTextSupported() {
        return LCD_SUPPORT && super.isLCDTextSupported();
    }

    @Override
    protected boolean registerEmbeddedFont(String string) {
        long[] lArray = new long[1];
        int n = OSFreetype.FT_Init_FreeType(lArray);
        if (n != 0) {
            return false;
        }
        long l = lArray[0];
        byte[] byArray = (string + "\u0000").getBytes();
        n = OSFreetype.FT_New_Face(l, byArray, 0L, lArray);
        if (n != 0) {
            long l2 = lArray[0];
            OSFreetype.FT_Done_Face(l2);
        }
        OSFreetype.FT_Done_FreeType(l);
        if (n != 0) {
            return false;
        }
        if (OSFreetype.isPangoEnabled()) {
            return OSPango.FcConfigAppFontAddFile(0L, string);
        }
        return true;
    }

    private static class StubGlyphLayout
    extends GlyphLayout {
        @Override
        public void layout(TextRun textRun, PGFont pGFont, FontStrike fontStrike, char[] cArray) {
        }
    }
}

