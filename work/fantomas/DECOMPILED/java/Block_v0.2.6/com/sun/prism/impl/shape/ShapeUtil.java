/*
 * Decompiled with CFR 0.152.
 */
package com.sun.prism.impl.shape;

import com.sun.javafx.geom.Path2D;
import com.sun.javafx.geom.PathConsumer2D;
import com.sun.javafx.geom.RectBounds;
import com.sun.javafx.geom.Shape;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.openpisces.Dasher;
import com.sun.openpisces.Stroker;
import com.sun.prism.BasicStroke;
import com.sun.prism.impl.PrismSettings;
import com.sun.prism.impl.shape.DMarlinRasterizer;
import com.sun.prism.impl.shape.MarlinRasterizer;
import com.sun.prism.impl.shape.MaskData;
import com.sun.prism.impl.shape.NativePiscesRasterizer;
import com.sun.prism.impl.shape.OpenPiscesPrismUtils;
import com.sun.prism.impl.shape.OpenPiscesRasterizer;
import com.sun.prism.impl.shape.ShapeRasterizer;

public class ShapeUtil {
    private static final ShapeRasterizer shapeRasterizer;

    public static MaskData rasterizeShape(Shape shape, BasicStroke basicStroke, RectBounds rectBounds, BaseTransform baseTransform, boolean bl, boolean bl2) {
        return shapeRasterizer.getMaskData(shape, basicStroke, rectBounds, baseTransform, bl, bl2);
    }

    public static Shape createCenteredStrokedShape(Shape shape, BasicStroke basicStroke) {
        if (PrismSettings.rasterizerSpec == PrismSettings.RasterizerType.DoubleMarlin) {
            return DMarlinRasterizer.createCenteredStrokedShape(shape, basicStroke);
        }
        if (PrismSettings.rasterizerSpec == PrismSettings.RasterizerType.FloatMarlin) {
            return MarlinRasterizer.createCenteredStrokedShape(shape, basicStroke);
        }
        return ShapeUtil.createCenteredStrokedShapeOpenPisces(shape, basicStroke);
    }

    private static Shape createCenteredStrokedShapeOpenPisces(Shape shape, BasicStroke basicStroke) {
        float f = basicStroke.getType() == 0 ? basicStroke.getLineWidth() : basicStroke.getLineWidth() * 2.0f;
        Path2D path2D = new Path2D(1);
        PathConsumer2D pathConsumer2D = new Stroker(path2D, f, basicStroke.getEndCap(), basicStroke.getLineJoin(), basicStroke.getMiterLimit());
        if (basicStroke.isDashed()) {
            pathConsumer2D = new Dasher(pathConsumer2D, basicStroke.getDashArray(), basicStroke.getDashPhase());
        }
        OpenPiscesPrismUtils.feedConsumer(shape.getPathIterator(null), pathConsumer2D);
        return path2D;
    }

    private ShapeUtil() {
    }

    static {
        switch (PrismSettings.rasterizerSpec) {
            case JavaPisces: {
                shapeRasterizer = new OpenPiscesRasterizer();
                break;
            }
            case NativePisces: {
                shapeRasterizer = new NativePiscesRasterizer();
                break;
            }
            case FloatMarlin: {
                shapeRasterizer = new MarlinRasterizer();
                break;
            }
            default: {
                shapeRasterizer = new DMarlinRasterizer();
            }
        }
    }
}

