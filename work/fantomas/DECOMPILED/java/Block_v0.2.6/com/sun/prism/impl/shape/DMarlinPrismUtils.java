/*
 * Decompiled with CFR 0.152.
 */
package com.sun.prism.impl.shape;

import com.sun.javafx.geom.Path2D;
import com.sun.javafx.geom.PathIterator;
import com.sun.javafx.geom.Rectangle;
import com.sun.javafx.geom.Shape;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.marlin.DMarlinRenderer;
import com.sun.marlin.DPathConsumer2D;
import com.sun.marlin.DRendererContext;
import com.sun.marlin.DTransformingPathConsumer2D;
import com.sun.marlin.MarlinConst;
import com.sun.marlin.MarlinProperties;
import com.sun.marlin.MarlinUtils;
import com.sun.prism.BasicStroke;
import java.util.Arrays;

public final class DMarlinPrismUtils {
    private static final boolean FORCE_NO_AA = false;
    static final boolean DISABLE_2ND_STROKER_CLIPPING = true;
    static final boolean DO_TRACE_PATH = false;
    static final boolean DO_CLIP = MarlinProperties.isDoClip();
    static final boolean DO_CLIP_FILL = true;
    static final boolean DO_CLIP_RUNTIME_ENABLE = MarlinProperties.isDoClipRuntimeFlag();
    static final float UPPER_BND = 1.7014117E38f;
    static final float LOWER_BND = -1.7014117E38f;

    private DMarlinPrismUtils() {
    }

    private static DPathConsumer2D initStroker(DRendererContext dRendererContext, BasicStroke basicStroke, float f, BaseTransform baseTransform, DPathConsumer2D dPathConsumer2D) {
        BaseTransform baseTransform2 = null;
        int n = -1;
        boolean bl = false;
        double d = f;
        float[] fArray = basicStroke.getDashArray();
        double[] dArray = null;
        double d2 = basicStroke.getDashPhase();
        if (fArray != null) {
            bl = true;
            n = fArray.length;
            dArray = dRendererContext.dasher.copyDashArray(fArray);
        }
        if (baseTransform != null && !baseTransform.isIdentity()) {
            double d3;
            double d4;
            double d5;
            double d6 = baseTransform.getMxx();
            if (DMarlinPrismUtils.nearZero(d6 * (d5 = baseTransform.getMxy()) + (d4 = baseTransform.getMyx()) * (d3 = baseTransform.getMyy())) && DMarlinPrismUtils.nearZero(d6 * d6 + d4 * d4 - (d5 * d5 + d3 * d3))) {
                double d7 = Math.sqrt(d6 * d6 + d4 * d4);
                if (dArray != null) {
                    int n2 = 0;
                    while (n2 < n) {
                        int n3 = n2++;
                        dArray[n3] = dArray[n3] * d7;
                    }
                    d2 *= d7;
                }
                d *= d7;
            } else {
                baseTransform2 = baseTransform;
            }
        } else {
            baseTransform = null;
        }
        DPathConsumer2D dPathConsumer2D2 = dPathConsumer2D;
        DTransformingPathConsumer2D dTransformingPathConsumer2D = dRendererContext.transformerPC2D;
        if (MarlinConst.USE_SIMPLIFIER) {
            dPathConsumer2D2 = dRendererContext.simplifier.init(dPathConsumer2D2);
        }
        dPathConsumer2D2 = dTransformingPathConsumer2D.deltaTransformConsumer(dPathConsumer2D2, baseTransform2);
        dPathConsumer2D2 = dRendererContext.stroker.init(dPathConsumer2D2, d, basicStroke.getEndCap(), basicStroke.getLineJoin(), basicStroke.getMiterLimit(), dArray == null);
        dRendererContext.monotonizer.init(d);
        if (dArray != null) {
            dPathConsumer2D2 = dRendererContext.dasher.init(dPathConsumer2D2, dArray, n, d2, bl);
            dRendererContext.stroker.disableClipping();
        } else if (dRendererContext.doClip && basicStroke.getEndCap() != 0) {
            dPathConsumer2D2 = dTransformingPathConsumer2D.detectClosedPath(dPathConsumer2D2);
        }
        dPathConsumer2D2 = dTransformingPathConsumer2D.inverseDeltaTransformConsumer(dPathConsumer2D2, baseTransform2);
        return dPathConsumer2D2;
    }

    private static boolean nearZero(double d) {
        return Math.abs(d) < 2.0 * Math.ulp(d);
    }

    private static DPathConsumer2D initRenderer(DRendererContext dRendererContext, BasicStroke basicStroke, BaseTransform baseTransform, Rectangle rectangle, int n, DMarlinRenderer dMarlinRenderer) {
        if (DO_CLIP || DO_CLIP_RUNTIME_ENABLE && MarlinProperties.isDoClipAtRuntime()) {
            double[] dArray = dRendererContext.clipRect;
            double d = dMarlinRenderer.getOffsetX();
            double d2 = dMarlinRenderer.getOffsetY();
            dArray[0] = (double)rectangle.y - 0.001 + d2;
            dArray[1] = (double)(rectangle.y + rectangle.height) + 0.001 + d2;
            dArray[2] = (double)rectangle.x - 0.001 + d;
            dArray[3] = (double)(rectangle.x + rectangle.width) + 0.001 + d;
            if (MarlinConst.DO_LOG_CLIP) {
                MarlinUtils.logInfo("clipRect (clip): " + Arrays.toString(dRendererContext.clipRect));
            }
            dRendererContext.doClip = true;
        }
        if (basicStroke != null) {
            dMarlinRenderer.init(rectangle.x, rectangle.y, rectangle.width, rectangle.height, 1);
            return DMarlinPrismUtils.initStroker(dRendererContext, basicStroke, basicStroke.getLineWidth(), baseTransform, dMarlinRenderer);
        }
        int n2 = n == 0 ? 0 : 1;
        dMarlinRenderer.init(rectangle.x, rectangle.y, rectangle.width, rectangle.height, n2);
        DPathConsumer2D dPathConsumer2D = dMarlinRenderer;
        DTransformingPathConsumer2D dTransformingPathConsumer2D = dRendererContext.transformerPC2D;
        if (dRendererContext.doClip) {
            dPathConsumer2D = dRendererContext.transformerPC2D.pathClipper(dPathConsumer2D);
        }
        return dPathConsumer2D;
    }

    public static DMarlinRenderer setupRenderer(DRendererContext dRendererContext, Shape shape, BasicStroke basicStroke, BaseTransform baseTransform, Rectangle rectangle, boolean bl) {
        MarlinConst marlinConst;
        BaseTransform baseTransform2 = baseTransform != null && !baseTransform.isIdentity() ? baseTransform : null;
        MarlinConst marlinConst2 = marlinConst = bl ? dRendererContext.renderer : dRendererContext.getRendererNoAA();
        if (shape instanceof Path2D) {
            Path2D path2D = (Path2D)shape;
            DPathConsumer2D dPathConsumer2D = DMarlinPrismUtils.initRenderer(dRendererContext, basicStroke, baseTransform2, rectangle, path2D.getWindingRule(), marlinConst);
            DMarlinPrismUtils.feedConsumer(dRendererContext, path2D, baseTransform2, dPathConsumer2D);
        } else {
            PathIterator pathIterator = shape.getPathIterator(baseTransform2);
            DPathConsumer2D dPathConsumer2D = DMarlinPrismUtils.initRenderer(dRendererContext, basicStroke, baseTransform2, rectangle, pathIterator.getWindingRule(), marlinConst);
            DMarlinPrismUtils.feedConsumer(dRendererContext, pathIterator, dPathConsumer2D);
        }
        return marlinConst;
    }

    public static void strokeTo(DRendererContext dRendererContext, Shape shape, BasicStroke basicStroke, float f, DPathConsumer2D dPathConsumer2D) {
        DPathConsumer2D dPathConsumer2D2 = DMarlinPrismUtils.initStroker(dRendererContext, basicStroke, f, null, dPathConsumer2D);
        if (shape instanceof Path2D) {
            DMarlinPrismUtils.feedConsumer(dRendererContext, (Path2D)shape, null, dPathConsumer2D2);
        } else {
            DMarlinPrismUtils.feedConsumer(dRendererContext, shape.getPathIterator(null), dPathConsumer2D2);
        }
    }

    private static void feedConsumer(DRendererContext dRendererContext, PathIterator pathIterator, DPathConsumer2D dPathConsumer2D) {
        if (MarlinConst.USE_PATH_SIMPLIFIER) {
            dPathConsumer2D = dRendererContext.pathSimplifier.init(dPathConsumer2D);
        }
        dRendererContext.dirty = true;
        float[] fArray = dRendererContext.float6;
        boolean bl = false;
        while (!pathIterator.isDone()) {
            switch (pathIterator.currentSegment(fArray)) {
                case 0: {
                    if (!(fArray[0] < 1.7014117E38f) || !(fArray[0] > -1.7014117E38f) || !(fArray[1] < 1.7014117E38f) || !(fArray[1] > -1.7014117E38f)) break;
                    dPathConsumer2D.moveTo(fArray[0], fArray[1]);
                    bl = true;
                    break;
                }
                case 1: {
                    if (!(fArray[0] < 1.7014117E38f) || !(fArray[0] > -1.7014117E38f) || !(fArray[1] < 1.7014117E38f) || !(fArray[1] > -1.7014117E38f)) break;
                    if (bl) {
                        dPathConsumer2D.lineTo(fArray[0], fArray[1]);
                        break;
                    }
                    dPathConsumer2D.moveTo(fArray[0], fArray[1]);
                    bl = true;
                    break;
                }
                case 2: {
                    if (!(fArray[2] < 1.7014117E38f) || !(fArray[2] > -1.7014117E38f) || !(fArray[3] < 1.7014117E38f) || !(fArray[3] > -1.7014117E38f)) break;
                    if (bl) {
                        if (fArray[0] < 1.7014117E38f && fArray[0] > -1.7014117E38f && fArray[1] < 1.7014117E38f && fArray[1] > -1.7014117E38f) {
                            dPathConsumer2D.quadTo(fArray[0], fArray[1], fArray[2], fArray[3]);
                            break;
                        }
                        dPathConsumer2D.lineTo(fArray[2], fArray[3]);
                        break;
                    }
                    dPathConsumer2D.moveTo(fArray[2], fArray[3]);
                    bl = true;
                    break;
                }
                case 3: {
                    if (!(fArray[4] < 1.7014117E38f) || !(fArray[4] > -1.7014117E38f) || !(fArray[5] < 1.7014117E38f) || !(fArray[5] > -1.7014117E38f)) break;
                    if (bl) {
                        if (fArray[0] < 1.7014117E38f && fArray[0] > -1.7014117E38f && fArray[1] < 1.7014117E38f && fArray[1] > -1.7014117E38f && fArray[2] < 1.7014117E38f && fArray[2] > -1.7014117E38f && fArray[3] < 1.7014117E38f && fArray[3] > -1.7014117E38f) {
                            dPathConsumer2D.curveTo(fArray[0], fArray[1], fArray[2], fArray[3], fArray[4], fArray[5]);
                            break;
                        }
                        dPathConsumer2D.lineTo(fArray[4], fArray[5]);
                        break;
                    }
                    dPathConsumer2D.moveTo(fArray[4], fArray[5]);
                    bl = true;
                    break;
                }
                case 4: {
                    if (!bl) break;
                    dPathConsumer2D.closePath();
                    break;
                }
            }
            pathIterator.next();
        }
        dPathConsumer2D.pathDone();
        dRendererContext.dirty = false;
    }

    private static void feedConsumer(DRendererContext dRendererContext, Path2D path2D, BaseTransform baseTransform, DPathConsumer2D dPathConsumer2D) {
        if (MarlinConst.USE_PATH_SIMPLIFIER) {
            dPathConsumer2D = dRendererContext.pathSimplifier.init(dPathConsumer2D);
        }
        dRendererContext.dirty = true;
        float[] fArray = dRendererContext.float6;
        boolean bl = false;
        float[] fArray2 = path2D.getFloatCoordsNoClone();
        byte[] byArray = path2D.getCommandsNoClone();
        int n = path2D.getNumCommands();
        int n2 = 0;
        block7: for (int i = 0; i < n; ++i) {
            switch (byArray[i]) {
                case 0: {
                    if (baseTransform == null) {
                        fArray[0] = fArray2[n2];
                        fArray[1] = fArray2[n2 + 1];
                    } else {
                        baseTransform.transform(fArray2, n2, fArray, 0, 1);
                    }
                    n2 += 2;
                    if (!(fArray[0] < 1.7014117E38f) || !(fArray[0] > -1.7014117E38f) || !(fArray[1] < 1.7014117E38f) || !(fArray[1] > -1.7014117E38f)) continue block7;
                    dPathConsumer2D.moveTo(fArray[0], fArray[1]);
                    bl = true;
                    continue block7;
                }
                case 1: {
                    if (baseTransform == null) {
                        fArray[0] = fArray2[n2];
                        fArray[1] = fArray2[n2 + 1];
                    } else {
                        baseTransform.transform(fArray2, n2, fArray, 0, 1);
                    }
                    n2 += 2;
                    if (!(fArray[0] < 1.7014117E38f) || !(fArray[0] > -1.7014117E38f) || !(fArray[1] < 1.7014117E38f) || !(fArray[1] > -1.7014117E38f)) continue block7;
                    if (bl) {
                        dPathConsumer2D.lineTo(fArray[0], fArray[1]);
                        continue block7;
                    }
                    dPathConsumer2D.moveTo(fArray[0], fArray[1]);
                    bl = true;
                    continue block7;
                }
                case 2: {
                    if (baseTransform == null) {
                        fArray[0] = fArray2[n2];
                        fArray[1] = fArray2[n2 + 1];
                        fArray[2] = fArray2[n2 + 2];
                        fArray[3] = fArray2[n2 + 3];
                    } else {
                        baseTransform.transform(fArray2, n2, fArray, 0, 2);
                    }
                    n2 += 4;
                    if (!(fArray[2] < 1.7014117E38f) || !(fArray[2] > -1.7014117E38f) || !(fArray[3] < 1.7014117E38f) || !(fArray[3] > -1.7014117E38f)) continue block7;
                    if (bl) {
                        if (fArray[0] < 1.7014117E38f && fArray[0] > -1.7014117E38f && fArray[1] < 1.7014117E38f && fArray[1] > -1.7014117E38f) {
                            dPathConsumer2D.quadTo(fArray[0], fArray[1], fArray[2], fArray[3]);
                            continue block7;
                        }
                        dPathConsumer2D.lineTo(fArray[2], fArray[3]);
                        continue block7;
                    }
                    dPathConsumer2D.moveTo(fArray[2], fArray[3]);
                    bl = true;
                    continue block7;
                }
                case 3: {
                    if (baseTransform == null) {
                        fArray[0] = fArray2[n2];
                        fArray[1] = fArray2[n2 + 1];
                        fArray[2] = fArray2[n2 + 2];
                        fArray[3] = fArray2[n2 + 3];
                        fArray[4] = fArray2[n2 + 4];
                        fArray[5] = fArray2[n2 + 5];
                    } else {
                        baseTransform.transform(fArray2, n2, fArray, 0, 3);
                    }
                    n2 += 6;
                    if (!(fArray[4] < 1.7014117E38f) || !(fArray[4] > -1.7014117E38f) || !(fArray[5] < 1.7014117E38f) || !(fArray[5] > -1.7014117E38f)) continue block7;
                    if (bl) {
                        if (fArray[0] < 1.7014117E38f && fArray[0] > -1.7014117E38f && fArray[1] < 1.7014117E38f && fArray[1] > -1.7014117E38f && fArray[2] < 1.7014117E38f && fArray[2] > -1.7014117E38f && fArray[3] < 1.7014117E38f && fArray[3] > -1.7014117E38f) {
                            dPathConsumer2D.curveTo(fArray[0], fArray[1], fArray[2], fArray[3], fArray[4], fArray[5]);
                            continue block7;
                        }
                        dPathConsumer2D.lineTo(fArray[4], fArray[5]);
                        continue block7;
                    }
                    dPathConsumer2D.moveTo(fArray[4], fArray[5]);
                    bl = true;
                    continue block7;
                }
                case 4: {
                    if (!bl) continue block7;
                    dPathConsumer2D.closePath();
                    continue block7;
                }
            }
        }
        dPathConsumer2D.pathDone();
        dRendererContext.dirty = false;
    }
}

