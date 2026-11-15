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
import com.sun.openpisces.Dasher;
import com.sun.openpisces.Renderer;
import com.sun.openpisces.Stroker;
import com.sun.openpisces.TransformingPathConsumer2D;
import com.sun.prism.BasicStroke;

public class OpenPiscesPrismUtils {
    private static final Renderer savedAARenderer = new Renderer(3, 3);
    private static final Renderer savedRenderer = new Renderer(0, 0);
    private static final Stroker savedStroker = new Stroker(savedRenderer);
    private static final Dasher savedDasher = new Dasher(savedStroker);
    private static TransformingPathConsumer2D.FilterSet transformer = new TransformingPathConsumer2D.FilterSet();

    private static PathConsumer2D initRenderer(BasicStroke basicStroke, BaseTransform baseTransform, Rectangle rectangle, int n, Renderer renderer) {
        int n2 = basicStroke == null && n == 0 ? 0 : 1;
        renderer.reset(rectangle.x, rectangle.y, rectangle.width, rectangle.height, n2);
        PathConsumer2D pathConsumer2D = transformer.getConsumer(renderer, baseTransform);
        if (basicStroke != null) {
            savedStroker.reset(basicStroke.getLineWidth(), basicStroke.getEndCap(), basicStroke.getLineJoin(), basicStroke.getMiterLimit());
            savedStroker.setConsumer(pathConsumer2D);
            pathConsumer2D = savedStroker;
            float[] fArray = basicStroke.getDashArray();
            if (fArray != null) {
                savedDasher.reset(fArray, basicStroke.getDashPhase());
                pathConsumer2D = savedDasher;
            }
        }
        return pathConsumer2D;
    }

    public static void feedConsumer(PathIterator pathIterator, PathConsumer2D pathConsumer2D) {
        float[] fArray = new float[6];
        while (!pathIterator.isDone()) {
            int n = pathIterator.currentSegment(fArray);
            switch (n) {
                case 0: {
                    pathConsumer2D.moveTo(fArray[0], fArray[1]);
                    break;
                }
                case 1: {
                    pathConsumer2D.lineTo(fArray[0], fArray[1]);
                    break;
                }
                case 2: {
                    pathConsumer2D.quadTo(fArray[0], fArray[1], fArray[2], fArray[3]);
                    break;
                }
                case 3: {
                    pathConsumer2D.curveTo(fArray[0], fArray[1], fArray[2], fArray[3], fArray[4], fArray[5]);
                    break;
                }
                case 4: {
                    pathConsumer2D.closePath();
                }
            }
            pathIterator.next();
        }
        pathConsumer2D.pathDone();
    }

    public static Renderer setupRenderer(Shape shape, BasicStroke basicStroke, BaseTransform baseTransform, Rectangle rectangle, boolean bl) {
        PathIterator pathIterator = shape.getPathIterator(null);
        Renderer renderer = bl ? savedAARenderer : savedRenderer;
        OpenPiscesPrismUtils.feedConsumer(pathIterator, OpenPiscesPrismUtils.initRenderer(basicStroke, baseTransform, rectangle, pathIterator.getWindingRule(), renderer));
        return renderer;
    }

    public static Renderer setupRenderer(Path2D path2D, BasicStroke basicStroke, BaseTransform baseTransform, Rectangle rectangle, boolean bl) {
        Renderer renderer = bl ? savedAARenderer : savedRenderer;
        PathConsumer2D pathConsumer2D = OpenPiscesPrismUtils.initRenderer(basicStroke, baseTransform, rectangle, path2D.getWindingRule(), renderer);
        float[] fArray = path2D.getFloatCoordsNoClone();
        byte[] byArray = path2D.getCommandsNoClone();
        int n = path2D.getNumCommands();
        int n2 = 0;
        block7: for (int i = 0; i < n; ++i) {
            switch (byArray[i]) {
                case 0: {
                    pathConsumer2D.moveTo(fArray[n2 + 0], fArray[n2 + 1]);
                    n2 += 2;
                    continue block7;
                }
                case 1: {
                    pathConsumer2D.lineTo(fArray[n2 + 0], fArray[n2 + 1]);
                    n2 += 2;
                    continue block7;
                }
                case 2: {
                    pathConsumer2D.quadTo(fArray[n2 + 0], fArray[n2 + 1], fArray[n2 + 2], fArray[n2 + 3]);
                    n2 += 4;
                    continue block7;
                }
                case 3: {
                    pathConsumer2D.curveTo(fArray[n2 + 0], fArray[n2 + 1], fArray[n2 + 2], fArray[n2 + 3], fArray[n2 + 4], fArray[n2 + 5]);
                    n2 += 6;
                    continue block7;
                }
                case 4: {
                    pathConsumer2D.closePath();
                }
            }
        }
        pathConsumer2D.pathDone();
        return renderer;
    }
}

