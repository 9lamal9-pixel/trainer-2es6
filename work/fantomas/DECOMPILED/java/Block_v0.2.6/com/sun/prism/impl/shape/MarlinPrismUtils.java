/*
 * Decompiled with CFR 0.152.
 */
package com.sun.prism.impl.shape;

import com.sun.javafx.geom.Path2D;
import com.sun.javafx.geom.PathConsumer2D;
import com.sun.javafx.geom.PathIterator;
import com.sun.javafx.geom.Rectangle;
import com.sun.javafx.geom.Shape;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.marlin.MarlinConst;
import com.sun.marlin.MarlinProperties;
import com.sun.marlin.MarlinRenderer;
import com.sun.marlin.MarlinUtils;
import com.sun.marlin.RendererContext;
import com.sun.marlin.TransformingPathConsumer2D;
import com.sun.prism.BasicStroke;
import java.util.Arrays;

public final class MarlinPrismUtils {
    private static final boolean FORCE_NO_AA = false;
    static final boolean DISABLE_2ND_STROKER_CLIPPING = true;
    static final boolean DO_TRACE_PATH = false;
    static final boolean DO_CLIP = MarlinProperties.isDoClip();
    static final boolean DO_CLIP_FILL = true;
    static final boolean DO_CLIP_RUNTIME_ENABLE = MarlinProperties.isDoClipRuntimeFlag();
    static final float UPPER_BND = 1.7014117E38f;
    static final float LOWER_BND = -1.7014117E38f;

    private MarlinPrismUtils() {
    }

    private static PathConsumer2D initStroker(RendererContext rendererContext, BasicStroke basicStroke, float f, BaseTransform baseTransform, PathConsumer2D pathConsumer2D) {
        BaseTransform baseTransform2 = null;
        int n = -1;
        boolean bl = false;
        float f2 = f;
        float[] fArray = basicStroke.getDashArray();
        float f3 = basicStroke.getDashPhase();
        if (baseTransform != null && !baseTransform.isIdentity()) {
            double d;
            double d2;
            double d3;
            double d4 = baseTransform.getMxx();
            if (MarlinPrismUtils.nearZero(d4 * (d3 = baseTransform.getMxy()) + (d2 = baseTransform.getMyx()) * (d = baseTransform.getMyy())) && MarlinPrismUtils.nearZero(d4 * d4 + d2 * d2 - (d3 * d3 + d * d))) {
                float f4 = (float)Math.sqrt(d4 * d4 + d2 * d2);
                if (fArray != null) {
                    bl = true;
                    n = fArray.length;
                    fArray = rendererContext.dasher.copyDashArray(fArray);
                    int n2 = 0;
                    while (n2 < n) {
                        int n3 = n2++;
                        fArray[n3] = fArray[n3] * f4;
                    }
                    f3 *= f4;
                }
                f2 *= f4;
            } else {
                baseTransform2 = baseTransform;
            }
        } else {
            baseTransform = null;
        }
        PathConsumer2D pathConsumer2D2 = pathConsumer2D;
        TransformingPathConsumer2D transformingPathConsumer2D = rendererContext.transformerPC2D;
        if (MarlinConst.USE_SIMPLIFIER) {
            pathConsumer2D2 = rendererContext.simplifier.init(pathConsumer2D2);
        }
        pathConsumer2D2 = transformingPathConsumer2D.deltaTransformConsumer(pathConsumer2D2, baseTransform2);
        pathConsumer2D2 = rendererContext.stroker.init(pathConsumer2D2, f2, basicStroke.getEndCap(), basicStroke.getLineJoin(), basicStroke.getMiterLimit(), fArray == null);
        rendererContext.monotonizer.init(f2);
        if (fArray != null) {
            if (!bl) {
                n = fArray.length;
            }
            pathConsumer2D2 = rendererContext.dasher.init(pathConsumer2D2, fArray, n, f3, bl);
            rendererContext.stroker.disableClipping();
        } else if (rendererContext.doClip && basicStroke.getEndCap() != 0) {
            pathConsumer2D2 = transformingPathConsumer2D.detectClosedPath(pathConsumer2D2);
        }
        pathConsumer2D2 = transformingPathConsumer2D.inverseDeltaTransformConsumer(pathConsumer2D2, baseTransform2);
        return pathConsumer2D2;
    }

    private static boolean nearZero(double d) {
        return Math.abs(d) < 2.0 * Math.ulp(d);
    }

    private static PathConsumer2D initRenderer(RendererContext rendererContext, BasicStroke basicStroke, BaseTransform baseTransform, Rectangle rectangle, int n, MarlinRenderer marlinRenderer) {
        if (DO_CLIP || DO_CLIP_RUNTIME_ENABLE && MarlinProperties.isDoClipAtRuntime()) {
            float[] fArray = rendererContext.clipRect;
            float f = marlinRenderer.getOffsetX();
            float f2 = marlinRenderer.getOffsetY();
            fArray[0] = (float)rectangle.y - 0.001f + f2;
            fArray[1] = (float)(rectangle.y + rectangle.height) + 0.001f + f2;
            fArray[2] = (float)rectangle.x - 0.001f + f;
            fArray[3] = (float)(rectangle.x + rectangle.width) + 0.001f + f;
            if (MarlinConst.DO_LOG_CLIP) {
                MarlinUtils.logInfo("clipRect (clip): " + Arrays.toString(rendererContext.clipRect));
            }
            rendererContext.doClip = true;
        }
        if (basicStroke != null) {
            marlinRenderer.init(rectangle.x, rectangle.y, rectangle.width, rectangle.height, 1);
            return MarlinPrismUtils.initStroker(rendererContext, basicStroke, basicStroke.getLineWidth(), baseTransform, marlinRenderer);
        }
        int n2 = n == 0 ? 0 : 1;
        marlinRenderer.init(rectangle.x, rectangle.y, rectangle.width, rectangle.height, n2);
        PathConsumer2D pathConsumer2D = marlinRenderer;
        TransformingPathConsumer2D transformingPathConsumer2D = rendererContext.transformerPC2D;
        if (rendererContext.doClip) {
            pathConsumer2D = rendererContext.transformerPC2D.pathClipper(pathConsumer2D);
        }
        return pathConsumer2D;
    }

    public static MarlinRenderer setupRenderer(RendererContext rendererContext, Shape shape, BasicStroke basicStroke, BaseTransform baseTransform, Rectangle rectangle, boolean bl) {
        MarlinRenderer marlinRenderer;
        BaseTransform baseTransform2 = baseTransform != null && !baseTransform.isIdentity() ? baseTransform : null;
        MarlinRenderer marlinRenderer2 = marlinRenderer = bl ? rendererContext.renderer : rendererContext.getRendererNoAA();
        if (shape instanceof Path2D) {
            Path2D path2D = (Path2D)shape;
            PathConsumer2D pathConsumer2D = MarlinPrismUtils.initRenderer(rendererContext, basicStroke, baseTransform2, rectangle, path2D.getWindingRule(), marlinRenderer);
            MarlinPrismUtils.feedConsumer(rendererContext, path2D, baseTransform2, pathConsumer2D);
        } else {
            PathIterator pathIterator = shape.getPathIterator(baseTransform2);
            PathConsumer2D pathConsumer2D = MarlinPrismUtils.initRenderer(rendererContext, basicStroke, baseTransform2, rectangle, pathIterator.getWindingRule(), marlinRenderer);
            MarlinPrismUtils.feedConsumer(rendererContext, pathIterator, pathConsumer2D);
        }
        return marlinRenderer;
    }

    public static void strokeTo(RendererContext rendererContext, Shape shape, BasicStroke basicStroke, float f, PathConsumer2D pathConsumer2D) {
        PathConsumer2D pathConsumer2D2 = MarlinPrismUtils.initStroker(rendererContext, basicStroke, f, null, pathConsumer2D);
        if (shape instanceof Path2D) {
            MarlinPrismUtils.feedConsumer(rendererContext, (Path2D)shape, null, pathConsumer2D2);
        } else {
            MarlinPrismUtils.feedConsumer(rendererContext, shape.getPathIterator(null), pathConsumer2D2);
        }
    }

    private static void feedConsumer(RendererContext rendererContext, PathIterator pathIterator, PathConsumer2D pathConsumer2D) {
        if (MarlinConst.USE_PATH_SIMPLIFIER) {
            pathConsumer2D = rendererContext.pathSimplifier.init(pathConsumer2D);
        }
        rendererContext.dirty = true;
        float[] fArray = rendererContext.float6;
        boolean bl = false;
        while (!pathIterator.isDone()) {
            switch (pathIterator.currentSegment(fArray)) {
                case 0: {
                    if (!(fArray[0] < 1.7014117E38f) || !(fArray[0] > -1.7014117E38f) || !(fArray[1] < 1.7014117E38f) || !(fArray[1] > -1.7014117E38f)) break;
                    pathConsumer2D.moveTo(fArray[0], fArray[1]);
                    bl = true;
                    break;
                }
                case 1: {
                    if (!(fArray[0] < 1.7014117E38f) || !(fArray[0] > -1.7014117E38f) || !(fArray[1] < 1.7014117E38f) || !(fArray[1] > -1.7014117E38f)) break;
                    if (bl) {
                        pathConsumer2D.lineTo(fArray[0], fArray[1]);
                        break;
                    }
                    pathConsumer2D.moveTo(fArray[0], fArray[1]);
                    bl = true;
                    break;
                }
                case 2: {
                    if (!(fArray[2] < 1.7014117E38f) || !(fArray[2] > -1.7014117E38f) || !(fArray[3] < 1.7014117E38f) || !(fArray[3] > -1.7014117E38f)) break;
                    if (bl) {
                        if (fArray[0] < 1.7014117E38f && fArray[0] > -1.7014117E38f && fArray[1] < 1.7014117E38f && fArray[1] > -1.7014117E38f) {
                            pathConsumer2D.quadTo(fArray[0], fArray[1], fArray[2], fArray[3]);
                            break;
                        }
                        pathConsumer2D.lineTo(fArray[2], fArray[3]);
                        break;
                    }
                    pathConsumer2D.moveTo(fArray[2], fArray[3]);
                    bl = true;
                    break;
                }
                case 3: {
                    if (!(fArray[4] < 1.7014117E38f) || !(fArray[4] > -1.7014117E38f) || !(fArray[5] < 1.7014117E38f) || !(fArray[5] > -1.7014117E38f)) break;
                    if (bl) {
                        if (fArray[0] < 1.7014117E38f && fArray[0] > -1.7014117E38f && fArray[1] < 1.7014117E38f && fArray[1] > -1.7014117E38f && fArray[2] < 1.7014117E38f && fArray[2] > -1.7014117E38f && fArray[3] < 1.7014117E38f && fArray[3] > -1.7014117E38f) {
                            pathConsumer2D.curveTo(fArray[0], fArray[1], fArray[2], fArray[3], fArray[4], fArray[5]);
                            break;
                        }
                        pathConsumer2D.lineTo(fArray[4], fArray[5]);
                        break;
                    }
                    pathConsumer2D.moveTo(fArray[4], fArray[5]);
                    bl = true;
                    break;
                }
                case 4: {
                    if (!bl) break;
                    pathConsumer2D.closePath();
                    break;
                }
            }
            pathIterator.next();
        }
        pathConsumer2D.pathDone();
        rendererContext.dirty = false;
    }

    private static void feedConsumer(RendererContext rendererContext, Path2D path2D, BaseTransform baseTransform, PathConsumer2D pathConsumer2D) {
        if (MarlinConst.USE_PATH_SIMPLIFIER) {
            pathConsumer2D = rendererContext.pathSimplifier.init(pathConsumer2D);
        }
        rendererContext.dirty = true;
        float[] fArray = rendererContext.float6;
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
                    pathConsumer2D.moveTo(fArray[0], fArray[1]);
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
                        pathConsumer2D.lineTo(fArray[0], fArray[1]);
                        continue block7;
                    }
                    pathConsumer2D.moveTo(fArray[0], fArray[1]);
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
                            pathConsumer2D.quadTo(fArray[0], fArray[1], fArray[2], fArray[3]);
                            continue block7;
                        }
                        pathConsumer2D.lineTo(fArray[2], fArray[3]);
                        continue block7;
                    }
                    pathConsumer2D.moveTo(fArray[2], fArray[3]);
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
                            pathConsumer2D.curveTo(fArray[0], fArray[1], fArray[2], fArray[3], fArray[4], fArray[5]);
                            continue block7;
                        }
                        pathConsumer2D.lineTo(fArray[4], fArray[5]);
                        continue block7;
                    }
                    pathConsumer2D.moveTo(fArray[4], fArray[5]);
                    bl = true;
                    continue block7;
                }
                case 4: {
                    if (!bl) continue block7;
                    pathConsumer2D.closePath();
                    continue block7;
                }
            }
        }
        pathConsumer2D.pathDone();
        rendererContext.dirty = false;
    }
}

