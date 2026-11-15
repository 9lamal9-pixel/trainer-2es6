/*
 * Decompiled with CFR 0.152.
 */
package com.sun.prism.sw;

import com.sun.javafx.geom.Path2D;
import com.sun.javafx.geom.Rectangle;
import com.sun.javafx.geom.Shape;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.marlin.DMarlinRenderer;
import com.sun.marlin.DMarlinRenderingEngine;
import com.sun.marlin.DRendererContext;
import com.sun.marlin.IntArrayCache;
import com.sun.marlin.MarlinAlphaConsumer;
import com.sun.marlin.MarlinConst;
import com.sun.marlin.MarlinRenderer;
import com.sun.marlin.MarlinRenderingEngine;
import com.sun.marlin.RendererContext;
import com.sun.openpisces.Renderer;
import com.sun.pisces.PiscesRenderer;
import com.sun.prism.BasicStroke;
import com.sun.prism.PixelFormat;
import com.sun.prism.ResourceFactory;
import com.sun.prism.Texture;
import com.sun.prism.impl.PrismSettings;
import com.sun.prism.impl.shape.DMarlinPrismUtils;
import com.sun.prism.impl.shape.MarlinPrismUtils;
import com.sun.prism.impl.shape.MaskData;
import com.sun.prism.impl.shape.OpenPiscesPrismUtils;
import com.sun.prism.impl.shape.ShapeUtil;
import com.sun.prism.sw.DirectRTPiscesAlphaConsumer;
import com.sun.prism.sw.SWArgbPreTexture;
import com.sun.prism.sw.SWMaskTexture;
import com.sun.prism.sw.SWRTTexture;
import java.lang.ref.SoftReference;

final class SWContext {
    private final ResourceFactory factory;
    private final ShapeRenderer shapeRenderer;
    private SoftReference<SWRTTexture> readBackBufferRef;
    private SoftReference<SWArgbPreTexture> imagePaintTextureRef;

    SWContext(ResourceFactory resourceFactory) {
        this.factory = resourceFactory;
        switch (PrismSettings.rasterizerSpec) {
            case JavaPisces: {
                this.shapeRenderer = new JavaShapeRenderer();
                break;
            }
            case NativePisces: {
                this.shapeRenderer = new NativeShapeRenderer();
                break;
            }
            case FloatMarlin: {
                this.shapeRenderer = new MarlinShapeRenderer();
                break;
            }
            default: {
                this.shapeRenderer = new DMarlinShapeRenderer();
            }
        }
    }

    void renderShape(PiscesRenderer piscesRenderer, Shape shape, BasicStroke basicStroke, BaseTransform baseTransform, Rectangle rectangle, boolean bl) {
        this.shapeRenderer.renderShape(piscesRenderer, shape, basicStroke, baseTransform, rectangle, bl);
    }

    private SWRTTexture initRBBuffer(int n, int n2) {
        SWRTTexture sWRTTexture = (SWRTTexture)this.factory.createRTTexture(n, n2, Texture.WrapMode.CLAMP_NOT_NEEDED);
        this.readBackBufferRef = new SoftReference<SWRTTexture>(sWRTTexture);
        return sWRTTexture;
    }

    private void disposeRBBuffer() {
        if (this.readBackBufferRef != null) {
            this.readBackBufferRef.clear();
            this.readBackBufferRef = null;
        }
    }

    SWRTTexture validateRBBuffer(int n, int n2) {
        SWRTTexture sWRTTexture;
        if (this.readBackBufferRef == null) {
            sWRTTexture = this.initRBBuffer(n, n2);
        } else {
            sWRTTexture = this.readBackBufferRef.get();
            if (sWRTTexture == null || sWRTTexture.getPhysicalWidth() < n || sWRTTexture.getPhysicalHeight() < n2) {
                this.disposeRBBuffer();
                sWRTTexture = this.initRBBuffer(n, n2);
            }
            sWRTTexture.setContentWidth(n);
            sWRTTexture.setContentHeight(n2);
        }
        return sWRTTexture;
    }

    private SWArgbPreTexture initImagePaintTexture(int n, int n2) {
        SWArgbPreTexture sWArgbPreTexture = (SWArgbPreTexture)this.factory.createTexture(PixelFormat.INT_ARGB_PRE, Texture.Usage.DEFAULT, Texture.WrapMode.REPEAT, n, n2);
        this.imagePaintTextureRef = new SoftReference<SWArgbPreTexture>(sWArgbPreTexture);
        return sWArgbPreTexture;
    }

    private void disposeImagePaintTexture() {
        if (this.imagePaintTextureRef != null) {
            this.imagePaintTextureRef.clear();
            this.imagePaintTextureRef = null;
        }
    }

    SWArgbPreTexture validateImagePaintTexture(int n, int n2) {
        SWArgbPreTexture sWArgbPreTexture;
        if (this.imagePaintTextureRef == null) {
            sWArgbPreTexture = this.initImagePaintTexture(n, n2);
        } else {
            sWArgbPreTexture = this.imagePaintTextureRef.get();
            if (sWArgbPreTexture == null || sWArgbPreTexture.getPhysicalWidth() < n || sWArgbPreTexture.getPhysicalHeight() < n2) {
                this.disposeImagePaintTexture();
                sWArgbPreTexture = this.initImagePaintTexture(n, n2);
            }
            sWArgbPreTexture.setContentWidth(n);
            sWArgbPreTexture.setContentHeight(n2);
        }
        return sWArgbPreTexture;
    }

    void dispose() {
        this.disposeRBBuffer();
        this.disposeImagePaintTexture();
        this.shapeRenderer.dispose();
    }

    static final class DMarlinShapeRenderer
    implements ShapeRenderer {
        private final DirectRTMarlinAlphaConsumer alphaConsumer = new DirectRTMarlinAlphaConsumer();

        DMarlinShapeRenderer() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void renderShape(PiscesRenderer piscesRenderer, Shape shape, BasicStroke basicStroke, BaseTransform baseTransform, Rectangle rectangle, boolean bl) {
            if (basicStroke != null && basicStroke.getType() != 0) {
                shape = basicStroke.createStrokedShape(shape);
                basicStroke = null;
            }
            DRendererContext dRendererContext = DMarlinRenderingEngine.getRendererContext();
            DMarlinRenderer dMarlinRenderer = null;
            try {
                if (shape instanceof Path2D) {
                    dMarlinRenderer = DMarlinPrismUtils.setupRenderer(dRendererContext, (Path2D)shape, basicStroke, baseTransform, rectangle, bl);
                }
                if (dMarlinRenderer == null) {
                    dMarlinRenderer = DMarlinPrismUtils.setupRenderer(dRendererContext, shape, basicStroke, baseTransform, rectangle, bl);
                }
                int n = dMarlinRenderer.getOutpixMinX();
                int n2 = dMarlinRenderer.getOutpixMaxX();
                int n3 = dMarlinRenderer.getOutpixMinY();
                int n4 = dMarlinRenderer.getOutpixMaxY();
                int n5 = n2 - n;
                int n6 = n4 - n3;
                if (n5 <= 0 || n6 <= 0) {
                    return;
                }
                this.alphaConsumer.initConsumer(n, n3, n5, n6, piscesRenderer);
                dMarlinRenderer.produceAlphas(this.alphaConsumer);
            }
            finally {
                if (dMarlinRenderer != null) {
                    dMarlinRenderer.dispose();
                }
                DMarlinRenderingEngine.returnRendererContext(dRendererContext);
            }
        }

        @Override
        public void dispose() {
        }
    }

    static final class DirectRTMarlinAlphaConsumer
    implements MarlinAlphaConsumer {
        private byte[] alpha_map;
        private int x;
        private int y;
        private int w;
        private int h;
        private int rowNum;
        private PiscesRenderer pr;

        DirectRTMarlinAlphaConsumer() {
        }

        public void initConsumer(int n, int n2, int n3, int n4, PiscesRenderer piscesRenderer) {
            this.x = n;
            this.y = n2;
            this.w = n3;
            this.h = n4;
            this.rowNum = 0;
            this.pr = piscesRenderer;
        }

        @Override
        public int getOriginX() {
            return this.x;
        }

        @Override
        public int getOriginY() {
            return this.y;
        }

        @Override
        public int getWidth() {
            return this.w;
        }

        @Override
        public int getHeight() {
            return this.h;
        }

        @Override
        public void setMaxAlpha(int n) {
            if (this.alpha_map == null || this.alpha_map.length != n + 1) {
                this.alpha_map = new byte[n + 1];
                for (int i = 0; i <= n; ++i) {
                    this.alpha_map[i] = (byte)((i * 255 + n / 2) / n);
                }
            }
        }

        @Override
        public boolean supportBlockFlags() {
            return false;
        }

        @Override
        public void clearAlphas(int n) {
        }

        @Override
        public void setAndClearRelativeAlphas(int[] nArray, int n, int n2, int n3) {
            this.pr.emitAndClearAlphaRow(this.alpha_map, nArray, n, n2, n3, n2 - this.x, this.rowNum);
            ++this.rowNum;
            int n4 = n3 - this.x;
            if (n4 <= this.w) {
                nArray[n4] = 0;
            } else {
                nArray[this.w] = 0;
            }
            if (MarlinConst.DO_CHECKS) {
                IntArrayCache.check(nArray, n2 - this.x, n4 + 1, 0);
            }
        }

        @Override
        public void setAndClearRelativeAlphas(int[] nArray, int[] nArray2, int n, int n2, int n3) {
            throw new UnsupportedOperationException();
        }
    }

    static final class MarlinShapeRenderer
    implements ShapeRenderer {
        private final DirectRTMarlinAlphaConsumer alphaConsumer = new DirectRTMarlinAlphaConsumer();

        MarlinShapeRenderer() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void renderShape(PiscesRenderer piscesRenderer, Shape shape, BasicStroke basicStroke, BaseTransform baseTransform, Rectangle rectangle, boolean bl) {
            if (basicStroke != null && basicStroke.getType() != 0) {
                shape = basicStroke.createStrokedShape(shape);
                basicStroke = null;
            }
            RendererContext rendererContext = MarlinRenderingEngine.getRendererContext();
            MarlinRenderer marlinRenderer = null;
            try {
                if (shape instanceof Path2D) {
                    marlinRenderer = MarlinPrismUtils.setupRenderer(rendererContext, (Path2D)shape, basicStroke, baseTransform, rectangle, bl);
                }
                if (marlinRenderer == null) {
                    marlinRenderer = MarlinPrismUtils.setupRenderer(rendererContext, shape, basicStroke, baseTransform, rectangle, bl);
                }
                int n = marlinRenderer.getOutpixMinX();
                int n2 = marlinRenderer.getOutpixMaxX();
                int n3 = marlinRenderer.getOutpixMinY();
                int n4 = marlinRenderer.getOutpixMaxY();
                int n5 = n2 - n;
                int n6 = n4 - n3;
                if (n5 <= 0 || n6 <= 0) {
                    return;
                }
                this.alphaConsumer.initConsumer(n, n3, n5, n6, piscesRenderer);
                marlinRenderer.produceAlphas(this.alphaConsumer);
            }
            finally {
                if (marlinRenderer != null) {
                    marlinRenderer.dispose();
                }
                MarlinRenderingEngine.returnRendererContext(rendererContext);
            }
        }

        @Override
        public void dispose() {
        }
    }

    static final class JavaShapeRenderer
    implements ShapeRenderer {
        private final DirectRTPiscesAlphaConsumer alphaConsumer = new DirectRTPiscesAlphaConsumer();

        JavaShapeRenderer() {
        }

        @Override
        public void renderShape(PiscesRenderer piscesRenderer, Shape shape, BasicStroke basicStroke, BaseTransform baseTransform, Rectangle rectangle, boolean bl) {
            if (basicStroke != null && basicStroke.getType() != 0) {
                shape = basicStroke.createStrokedShape(shape);
                basicStroke = null;
            }
            Renderer renderer = OpenPiscesPrismUtils.setupRenderer(shape, basicStroke, baseTransform, rectangle, bl);
            this.alphaConsumer.initConsumer(renderer, piscesRenderer);
            renderer.produceAlphas(this.alphaConsumer);
        }

        @Override
        public void dispose() {
        }
    }

    class NativeShapeRenderer
    implements ShapeRenderer {
        private SoftReference<SWMaskTexture> maskTextureRef;

        NativeShapeRenderer() {
        }

        @Override
        public void renderShape(PiscesRenderer piscesRenderer, Shape shape, BasicStroke basicStroke, BaseTransform baseTransform, Rectangle rectangle, boolean bl) {
            MaskData maskData = ShapeUtil.rasterizeShape(shape, basicStroke, rectangle.toRectBounds(), baseTransform, true, bl);
            SWMaskTexture sWMaskTexture = this.validateMaskTexture(maskData.getWidth(), maskData.getHeight());
            maskData.uploadToTexture(sWMaskTexture, 0, 0, false);
            piscesRenderer.fillAlphaMask(sWMaskTexture.getDataNoClone(), maskData.getOriginX(), maskData.getOriginY(), maskData.getWidth(), maskData.getHeight(), 0, sWMaskTexture.getPhysicalWidth());
        }

        private SWMaskTexture initMaskTexture(int n, int n2) {
            SWMaskTexture sWMaskTexture = (SWMaskTexture)SWContext.this.factory.createMaskTexture(n, n2, Texture.WrapMode.CLAMP_NOT_NEEDED);
            this.maskTextureRef = new SoftReference<SWMaskTexture>(sWMaskTexture);
            return sWMaskTexture;
        }

        private void disposeMaskTexture() {
            if (this.maskTextureRef != null) {
                this.maskTextureRef.clear();
                this.maskTextureRef = null;
            }
        }

        private SWMaskTexture validateMaskTexture(int n, int n2) {
            SWMaskTexture sWMaskTexture;
            if (this.maskTextureRef == null) {
                sWMaskTexture = this.initMaskTexture(n, n2);
            } else {
                sWMaskTexture = this.maskTextureRef.get();
                if (sWMaskTexture == null || sWMaskTexture.getPhysicalWidth() < n || sWMaskTexture.getPhysicalHeight() < n2) {
                    this.disposeMaskTexture();
                    sWMaskTexture = this.initMaskTexture(n, n2);
                }
            }
            return sWMaskTexture;
        }

        @Override
        public void dispose() {
            this.disposeMaskTexture();
        }
    }

    static interface ShapeRenderer {
        public void renderShape(PiscesRenderer var1, Shape var2, BasicStroke var3, BaseTransform var4, Rectangle var5, boolean var6);

        public void dispose();
    }
}

