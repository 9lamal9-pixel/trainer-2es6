/*
 * Decompiled with CFR 0.152.
 */
package com.sun.prism.impl.shape;

import com.sun.javafx.geom.Path2D;
import com.sun.javafx.geom.RectBounds;
import com.sun.javafx.geom.Rectangle;
import com.sun.javafx.geom.Shape;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.marlin.DMarlinRenderer;
import com.sun.marlin.DMarlinRenderingEngine;
import com.sun.marlin.DRendererContext;
import com.sun.marlin.MaskMarlinAlphaConsumer;
import com.sun.prism.BasicStroke;
import com.sun.prism.impl.PrismSettings;
import com.sun.prism.impl.shape.DMarlinPrismUtils;
import com.sun.prism.impl.shape.MaskData;
import com.sun.prism.impl.shape.ShapeRasterizer;

public final class DMarlinRasterizer
implements ShapeRasterizer {
    private static final MaskData EMPTY_MASK = MaskData.create(new byte[1], 0, 0, 1, 1);

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public MaskData getMaskData(Shape shape, BasicStroke basicStroke, RectBounds rectBounds, BaseTransform baseTransform, boolean bl, boolean bl2) {
        if (basicStroke != null && basicStroke.getType() != 0) {
            shape = basicStroke.createStrokedShape(shape);
            basicStroke = null;
        }
        if (rectBounds == null) {
            if (basicStroke != null) {
                shape = basicStroke.createStrokedShape(shape);
                basicStroke = null;
            }
            rectBounds = new RectBounds();
            rectBounds = (RectBounds)baseTransform.transform(shape.getBounds(), rectBounds);
        }
        if (rectBounds.isEmpty()) {
            return EMPTY_MASK;
        }
        DRendererContext dRendererContext = DMarlinRenderingEngine.getRendererContext();
        DMarlinRenderer dMarlinRenderer = null;
        try {
            Rectangle rectangle = dRendererContext.clip;
            rectangle.setBounds(rectBounds);
            dMarlinRenderer = DMarlinPrismUtils.setupRenderer(dRendererContext, shape, basicStroke, baseTransform, rectangle, bl2);
            int n = dMarlinRenderer.getOutpixMinX();
            int n2 = dMarlinRenderer.getOutpixMaxX();
            int n3 = dMarlinRenderer.getOutpixMinY();
            int n4 = dMarlinRenderer.getOutpixMaxY();
            int n5 = n2 - n;
            int n6 = n4 - n3;
            if (n5 <= 0 || n6 <= 0) {
                MaskData maskData = EMPTY_MASK;
                return maskData;
            }
            MaskMarlinAlphaConsumer maskMarlinAlphaConsumer = dRendererContext.consumer;
            if (maskMarlinAlphaConsumer == null || n5 * n6 > maskMarlinAlphaConsumer.getAlphaLength()) {
                int n7 = n5 * n6 + 4095 & 0xFFFFF000;
                dRendererContext.consumer = maskMarlinAlphaConsumer = new MaskMarlinAlphaConsumer(n7);
                if (PrismSettings.verbose) {
                    System.out.println("new alphas with length = " + n7);
                }
            }
            maskMarlinAlphaConsumer.setBoundsNoClone(n, n3, n5, n6);
            dMarlinRenderer.produceAlphas(maskMarlinAlphaConsumer);
            MaskData maskData = maskMarlinAlphaConsumer.getMaskData();
            return maskData;
        }
        finally {
            if (dMarlinRenderer != null) {
                dMarlinRenderer.dispose();
            }
            DMarlinRenderingEngine.returnRendererContext(dRendererContext);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static Shape createCenteredStrokedShape(Shape shape, BasicStroke basicStroke) {
        float f = basicStroke.getType() == 0 ? basicStroke.getLineWidth() : basicStroke.getLineWidth() * 2.0f;
        DRendererContext dRendererContext = DMarlinRenderingEngine.getRendererContext();
        try {
            Path2D path2D = dRendererContext.getPath2D();
            DMarlinPrismUtils.strokeTo(dRendererContext, shape, basicStroke, f, dRendererContext.transformerPC2D.wrapPath2D(path2D));
            Path2D path2D2 = new Path2D(path2D);
            return path2D2;
        }
        finally {
            DMarlinRenderingEngine.returnRendererContext(dRendererContext);
        }
    }
}

