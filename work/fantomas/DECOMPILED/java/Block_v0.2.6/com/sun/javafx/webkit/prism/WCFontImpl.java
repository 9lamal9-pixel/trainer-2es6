/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.webkit.prism;

import com.sun.javafx.font.CharToGlyphMapper;
import com.sun.javafx.font.FontFactory;
import com.sun.javafx.font.FontStrike;
import com.sun.javafx.font.PGFont;
import com.sun.javafx.geom.BaseBounds;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.logging.PlatformLogger;
import com.sun.javafx.scene.text.GlyphList;
import com.sun.javafx.scene.text.TextLayout;
import com.sun.javafx.text.TextRun;
import com.sun.javafx.webkit.prism.TextUtilities;
import com.sun.prism.GraphicsPipeline;
import com.sun.webkit.graphics.WCFont;
import com.sun.webkit.graphics.WCGlyphBuffer;
import java.util.HashMap;

final class WCFontImpl
extends WCFont {
    private static final PlatformLogger log = PlatformLogger.getLogger(WCFontImpl.class.getName());
    private static final HashMap<String, String> FONT_MAP = new HashMap();
    private final PGFont font;
    private FontStrike strike;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static WCFont getFont(String string, boolean bl, boolean bl2, float f) {
        FontFactory fontFactory = GraphicsPipeline.getPipeline().getFontFactory();
        Object object = FONT_MAP;
        synchronized (object) {
            if (FONT_MAP.isEmpty()) {
                FONT_MAP.put("serif", "Serif");
                FONT_MAP.put("dialog", "SansSerif");
                FONT_MAP.put("helvetica", "SansSerif");
                FONT_MAP.put("sansserif", "SansSerif");
                FONT_MAP.put("sans-serif", "SansSerif");
                FONT_MAP.put("monospace", "Monospaced");
                FONT_MAP.put("monospaced", "Monospaced");
                for (String string2 : fontFactory.getFontFamilyNames()) {
                    FONT_MAP.put(string2.toLowerCase(), string2);
                }
            }
        }
        object = FONT_MAP.get(string.toLowerCase());
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            String[] stringArray = new StringBuilder("WCFontImpl.get(");
            stringArray.append(string).append(", ").append(f);
            if (bl) {
                stringArray.append(", bold");
            }
            if (bl2) {
                stringArray.append(", italic");
            }
            log.fine(stringArray.append(") = ").append((String)object).toString());
        }
        return object != null ? new WCFontImpl(fontFactory.createFont((String)object, bl, bl2, f)) : null;
    }

    WCFontImpl(PGFont pGFont) {
        this.font = pGFont;
    }

    @Override
    public WCFont deriveFont(float f) {
        FontFactory fontFactory = GraphicsPipeline.getPipeline().getFontFactory();
        return new WCFontImpl(fontFactory.deriveFont(this.font, this.font.getFontResource().isBold(), this.font.getFontResource().isItalic(), f));
    }

    @Override
    public int getOffsetForPosition(String string, float f) {
        TextLayout textLayout = TextUtilities.createLayout(string, this.font);
        GlyphList[] glyphListArray = textLayout.getRuns();
        TextRun textRun = (TextRun)glyphListArray[0];
        int n = textRun.getOffsetAtX(f, null);
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine(String.format("str='%s' (length=%d), x=%.2f => %d", string, string.length(), Float.valueOf(f), n));
        }
        return n;
    }

    @Override
    public WCGlyphBuffer getGlyphsAndAdvances(String string, int n, int n2, boolean bl) {
        GlyphList[] glyphListArray;
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine(String.format("str='%s' (length=%d), from=%d, to=%d, rtl=%b", string, string.length(), n, n2, bl));
        }
        TextLayout textLayout = TextUtilities.createLayout(string.substring(n, n2), this.getPlatformFont());
        int n3 = 0;
        for (GlyphList glyphList : glyphListArray = textLayout.getRuns()) {
            n3 += glyphList.getGlyphCount();
        }
        int[] nArray = new int[n3];
        float[] fArray = new float[n3];
        n3 = 0;
        for (GlyphList glyphList : textLayout.getRuns()) {
            int n4 = glyphList.getGlyphCount();
            for (int i = 0; i < n4; ++i) {
                nArray[n3] = glyphList.getGlyphCode(i);
                fArray[n3] = glyphList.getPosX(i + 1) - glyphList.getPosX(i);
                ++n3;
            }
        }
        float f = 0.0f;
        f = bl ? (f += TextUtilities.getLayoutWidth(string.substring(n), this.getPlatformFont()) - textLayout.getBounds().getWidth()) : (f += TextUtilities.getLayoutWidth(string.substring(0, n), this.getPlatformFont()));
        return new WCGlyphBuffer(nArray, fArray, f);
    }

    private FontStrike getFontStrike() {
        if (this.strike == null) {
            this.strike = this.font.getStrike(BaseTransform.IDENTITY_TRANSFORM, 1);
        }
        return this.strike;
    }

    @Override
    public double getGlyphWidth(int n) {
        return this.getFontStrike().getFontResource().getAdvance(n, this.font.getSize());
    }

    @Override
    public float[] getGlyphBoundingBox(int n) {
        float[] fArray = new float[4];
        fArray = this.getFontStrike().getFontResource().getGlyphBoundingBox(n, this.font.getSize(), fArray);
        return new float[]{fArray[0], -fArray[3], fArray[2], fArray[3] - fArray[1]};
    }

    @Override
    public float getXHeight() {
        return this.getFontStrike().getMetrics().getXHeight();
    }

    @Override
    public int[] getGlyphCodes(char[] cArray) {
        int[] nArray = new int[cArray.length];
        CharToGlyphMapper charToGlyphMapper = this.getFontStrike().getFontResource().getGlyphMapper();
        charToGlyphMapper.charsToGlyphs(cArray.length, cArray, nArray);
        return nArray;
    }

    @Override
    public double getStringWidth(String string) {
        double d = TextUtilities.getLayoutWidth(string, this.font);
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine(String.format("str='%s' (length=%d) => %.1f", string, string.length(), d));
        }
        return d;
    }

    @Override
    public double[] getStringBounds(String string, int n, int n2, boolean bl) {
        float f = TextUtilities.getLayoutWidth(string.substring(0, n), this.font);
        BaseBounds baseBounds = TextUtilities.getLayoutBounds(string.substring(0, n2), this.font);
        double[] dArray = new double[]{f, 0.0, baseBounds.getWidth() - f, baseBounds.getHeight()};
        if (bl) {
            float f2 = TextUtilities.getLayoutWidth(string, this.font);
            dArray[0] = f2 - baseBounds.getWidth();
        }
        if (log.isLoggable(PlatformLogger.Level.FINE)) {
            log.fine(String.format("str='%s' (length=%d) [%d, %d], rtl=%b => [%.1f, %.1f + %.1f x %.1f]", string, string.length(), n, n2, bl, dArray[0], dArray[1], dArray[2], dArray[3]));
        }
        return dArray;
    }

    @Override
    public float getAscent() {
        float f = -this.getFontStrike().getMetrics().getAscent();
        if (log.isLoggable(PlatformLogger.Level.FINER)) {
            log.finer("getAscent({0}, {1}) = {2}", this.font.getName(), Float.valueOf(this.font.getSize()), Float.valueOf(f));
        }
        return f;
    }

    @Override
    public float getDescent() {
        float f = this.getFontStrike().getMetrics().getDescent();
        if (log.isLoggable(PlatformLogger.Level.FINER)) {
            log.finer("getDescent({0}, {1}) = {2}", this.font.getName(), Float.valueOf(this.font.getSize()), Float.valueOf(f));
        }
        return f;
    }

    @Override
    public float getLineSpacing() {
        float f = this.getFontStrike().getMetrics().getLineHeight();
        if (log.isLoggable(PlatformLogger.Level.FINER)) {
            log.finer("getLineSpacing({0}, {1}) = {2}", this.font.getName(), Float.valueOf(this.font.getSize()), Float.valueOf(f));
        }
        return f;
    }

    @Override
    public float getLineGap() {
        float f = this.getFontStrike().getMetrics().getLineGap();
        if (log.isLoggable(PlatformLogger.Level.FINER)) {
            log.finer("getLineGap({0}, {1}) = {2}", this.font.getName(), Float.valueOf(this.font.getSize()), Float.valueOf(f));
        }
        return f;
    }

    @Override
    public boolean hasUniformLineMetrics() {
        return false;
    }

    @Override
    public Object getPlatformFont() {
        return this.font;
    }

    @Override
    public float getCapHeight() {
        return this.getFontStrike().getMetrics().getCapHeight();
    }
}

