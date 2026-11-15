/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.webkit.prism;

import com.sun.glass.ui.Screen;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.javafx.webkit.prism.PrismImage;
import com.sun.javafx.webkit.prism.RTImage;
import com.sun.javafx.webkit.prism.WCBufferedContext;
import com.sun.javafx.webkit.prism.WCFontCustomPlatformDataImpl;
import com.sun.javafx.webkit.prism.WCFontImpl;
import com.sun.javafx.webkit.prism.WCGraphicsPrismContext;
import com.sun.javafx.webkit.prism.WCImageDecoderImpl;
import com.sun.javafx.webkit.prism.WCImageImpl;
import com.sun.javafx.webkit.prism.WCMediaPlayerImpl;
import com.sun.javafx.webkit.prism.WCPageBackBufferImpl;
import com.sun.javafx.webkit.prism.WCPathImpl;
import com.sun.javafx.webkit.prism.WCRenderQueueImpl;
import com.sun.media.jfxmedia.MediaManager;
import com.sun.prism.Graphics;
import com.sun.webkit.graphics.WCFont;
import com.sun.webkit.graphics.WCFontCustomPlatformData;
import com.sun.webkit.graphics.WCGraphicsContext;
import com.sun.webkit.graphics.WCGraphicsManager;
import com.sun.webkit.graphics.WCImage;
import com.sun.webkit.graphics.WCImageDecoder;
import com.sun.webkit.graphics.WCImageFrame;
import com.sun.webkit.graphics.WCMediaPlayer;
import com.sun.webkit.graphics.WCPageBackBuffer;
import com.sun.webkit.graphics.WCPath;
import com.sun.webkit.graphics.WCRectangle;
import com.sun.webkit.graphics.WCRenderQueue;
import com.sun.webkit.graphics.WCTransform;
import com.sun.webkit.perf.WCFontPerfLogger;
import com.sun.webkit.perf.WCGraphicsPerfLogger;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public final class PrismGraphicsManager
extends WCGraphicsManager {
    private static final float highestPixelScale;
    private static final BaseTransform pixelScaleTransform;

    static BaseTransform getPixelScaleTransform() {
        return pixelScaleTransform;
    }

    @Override
    public float getDevicePixelScale() {
        return highestPixelScale;
    }

    @Override
    protected WCImageDecoder getImageDecoder() {
        return new WCImageDecoderImpl();
    }

    @Override
    public WCRenderQueue createRenderQueue(WCRectangle wCRectangle, boolean bl) {
        return new WCRenderQueueImpl(wCRectangle, bl);
    }

    @Override
    protected WCRenderQueue createBufferedContextRQ(WCImage wCImage) {
        WCBufferedContext wCBufferedContext = new WCBufferedContext((PrismImage)wCImage);
        WCRenderQueueImpl wCRenderQueueImpl = new WCRenderQueueImpl(WCGraphicsPerfLogger.isEnabled() ? new WCGraphicsPerfLogger(wCBufferedContext) : wCBufferedContext);
        wCImage.setRQ(wCRenderQueueImpl);
        return wCRenderQueueImpl;
    }

    @Override
    protected WCFont getWCFont(String string, boolean bl, boolean bl2, float f) {
        WCFont wCFont = WCFontImpl.getFont(string, bl, bl2, f);
        return WCFontPerfLogger.isEnabled() && wCFont != null ? new WCFontPerfLogger(wCFont) : wCFont;
    }

    @Override
    protected WCFontCustomPlatformData createFontCustomPlatformData(InputStream inputStream) throws IOException {
        return new WCFontCustomPlatformDataImpl(inputStream);
    }

    @Override
    public WCGraphicsContext createGraphicsContext(Object object) {
        WCGraphicsPrismContext wCGraphicsPrismContext = new WCGraphicsPrismContext((Graphics)object);
        return WCGraphicsPerfLogger.isEnabled() ? new WCGraphicsPerfLogger(wCGraphicsPrismContext) : wCGraphicsPrismContext;
    }

    @Override
    public WCPageBackBuffer createPageBackBuffer() {
        return new WCPageBackBufferImpl(highestPixelScale);
    }

    @Override
    protected WCPath createWCPath() {
        return new WCPathImpl();
    }

    @Override
    protected WCPath createWCPath(WCPath wCPath) {
        return new WCPathImpl((WCPathImpl)wCPath);
    }

    @Override
    protected WCImage createWCImage(int n, int n2) {
        return new WCImageImpl(n, n2);
    }

    @Override
    protected WCImage createRTImage(int n, int n2) {
        return new RTImage(n, n2, highestPixelScale);
    }

    @Override
    public WCImage getIconImage(String string) {
        return null;
    }

    @Override
    public Object toPlatformImage(WCImage wCImage) {
        return ((WCImageImpl)wCImage).getImage();
    }

    @Override
    protected WCImageFrame createFrame(final int n, final int n2, ByteBuffer byteBuffer) {
        int[] nArray = new int[byteBuffer.capacity() / 4];
        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.asIntBuffer().get(nArray);
        final WCImageImpl wCImageImpl = new WCImageImpl(nArray, n, n2);
        return new WCImageFrame(){

            @Override
            public WCImage getFrame() {
                return wCImageImpl;
            }

            @Override
            public int[] getSize() {
                return new int[]{n, n2};
            }
        };
    }

    @Override
    protected WCTransform createTransform(double d, double d2, double d3, double d4, double d5, double d6) {
        return new WCTransform(d, d2, d3, d4, d5, d6);
    }

    @Override
    protected String[] getSupportedMediaTypes() {
        String[] stringArray = MediaManager.getSupportedContentTypes();
        int n = stringArray.length;
        for (int i = 0; i < n; ++i) {
            if ("video/x-flv".compareToIgnoreCase(stringArray[i]) != 0) continue;
            System.arraycopy(stringArray, i + 1, stringArray, i, n - (i + 1));
            --n;
        }
        if (n < stringArray.length) {
            String[] stringArray2 = new String[n];
            System.arraycopy(stringArray, 0, stringArray2, 0, n);
            stringArray = stringArray2;
        }
        return stringArray;
    }

    @Override
    protected WCMediaPlayer createMediaPlayer() {
        return new WCMediaPlayerImpl();
    }

    static {
        float f = 1.0f;
        for (Screen screen : Screen.getScreens()) {
            f = Math.max(screen.getRecommendedOutputScaleX(), f);
            f = Math.max(screen.getRecommendedOutputScaleY(), f);
        }
        highestPixelScale = (float)Math.ceil(f);
        pixelScaleTransform = BaseTransform.getScaleInstance(f, f);
    }
}

