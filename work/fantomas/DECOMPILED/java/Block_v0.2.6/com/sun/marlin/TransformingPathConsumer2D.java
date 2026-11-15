/*
 * Decompiled with CFR 0.152.
 */
package com.sun.marlin;

import com.sun.javafx.geom.Path2D;
import com.sun.javafx.geom.PathConsumer2D;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.marlin.Curve;
import com.sun.marlin.Helpers;
import com.sun.marlin.MarlinConst;
import com.sun.marlin.MarlinProperties;
import com.sun.marlin.MarlinUtils;
import com.sun.marlin.RendererContext;
import java.util.Arrays;

public final class TransformingPathConsumer2D {
    static final float CLIP_RECT_PADDING = 1.0f;
    private final RendererContext rdrCtx;
    private final ClosedPathDetector cpDetector;
    private final PathClipFilter pathClipper;
    private final Path2DWrapper wp_Path2DWrapper = new Path2DWrapper();
    private final DeltaScaleFilter dt_DeltaScaleFilter = new DeltaScaleFilter();
    private final DeltaTransformFilter dt_DeltaTransformFilter = new DeltaTransformFilter();
    private final DeltaScaleFilter iv_DeltaScaleFilter = new DeltaScaleFilter();
    private final DeltaTransformFilter iv_DeltaTransformFilter = new DeltaTransformFilter();
    private final PathTracer tracerInput = new PathTracer("[Input]");
    private final PathTracer tracerCPDetector = new PathTracer("ClosedPathDetector");
    private final PathTracer tracerFiller = new PathTracer("Filler");
    private final PathTracer tracerStroker = new PathTracer("Stroker");
    private final PathTracer tracerDasher = new PathTracer("Dasher");

    TransformingPathConsumer2D(RendererContext rendererContext) {
        this.rdrCtx = rendererContext;
        this.cpDetector = new ClosedPathDetector(rendererContext);
        this.pathClipper = new PathClipFilter(rendererContext);
    }

    public PathConsumer2D wrapPath2D(Path2D path2D) {
        return this.wp_Path2DWrapper.init(path2D);
    }

    public PathConsumer2D traceInput(PathConsumer2D pathConsumer2D) {
        return this.tracerInput.init(pathConsumer2D);
    }

    public PathConsumer2D traceClosedPathDetector(PathConsumer2D pathConsumer2D) {
        return this.tracerCPDetector.init(pathConsumer2D);
    }

    public PathConsumer2D traceFiller(PathConsumer2D pathConsumer2D) {
        return this.tracerFiller.init(pathConsumer2D);
    }

    public PathConsumer2D traceStroker(PathConsumer2D pathConsumer2D) {
        return this.tracerStroker.init(pathConsumer2D);
    }

    public PathConsumer2D traceDasher(PathConsumer2D pathConsumer2D) {
        return this.tracerDasher.init(pathConsumer2D);
    }

    public PathConsumer2D detectClosedPath(PathConsumer2D pathConsumer2D) {
        return this.cpDetector.init(pathConsumer2D);
    }

    public PathConsumer2D pathClipper(PathConsumer2D pathConsumer2D) {
        return this.pathClipper.init(pathConsumer2D);
    }

    public PathConsumer2D deltaTransformConsumer(PathConsumer2D pathConsumer2D, BaseTransform baseTransform) {
        if (baseTransform == null) {
            return pathConsumer2D;
        }
        float f = (float)baseTransform.getMxx();
        float f2 = (float)baseTransform.getMxy();
        float f3 = (float)baseTransform.getMyx();
        float f4 = (float)baseTransform.getMyy();
        if (f2 == 0.0f && f3 == 0.0f) {
            if (f == 1.0f && f4 == 1.0f) {
                return pathConsumer2D;
            }
            if (this.rdrCtx.doClip) {
                this.rdrCtx.clipInvScale = TransformingPathConsumer2D.adjustClipScale(this.rdrCtx.clipRect, f, f4);
            }
            return this.dt_DeltaScaleFilter.init(pathConsumer2D, f, f4);
        }
        if (this.rdrCtx.doClip) {
            this.rdrCtx.clipInvScale = TransformingPathConsumer2D.adjustClipInverseDelta(this.rdrCtx.clipRect, f, f2, f3, f4);
        }
        return this.dt_DeltaTransformFilter.init(pathConsumer2D, f, f2, f3, f4);
    }

    private static float adjustClipScale(float[] fArray, float f, float f2) {
        float f3;
        float f4 = 1.0f / f2;
        fArray[0] = fArray[0] * f4;
        fArray[1] = fArray[1] * f4;
        if (fArray[1] < fArray[0]) {
            f3 = fArray[0];
            fArray[0] = fArray[1];
            fArray[1] = f3;
        }
        f3 = 1.0f / f;
        fArray[2] = fArray[2] * f3;
        fArray[3] = fArray[3] * f3;
        if (fArray[3] < fArray[2]) {
            float f5 = fArray[2];
            fArray[2] = fArray[3];
            fArray[3] = f5;
        }
        if (MarlinConst.DO_LOG_CLIP) {
            MarlinUtils.logInfo("clipRect (ClipScale): " + Arrays.toString(fArray));
        }
        return 0.5f * (Math.abs(f3) + Math.abs(f4));
    }

    private static float adjustClipInverseDelta(float[] fArray, float f, float f2, float f3, float f4) {
        float f5;
        float f6;
        float f7 = f * f4 - f2 * f3;
        float f8 = f4 / f7;
        float f9 = -f2 / f7;
        float f10 = -f3 / f7;
        float f11 = f / f7;
        float f12 = fArray[2] * f8 + fArray[0] * f9;
        float f13 = fArray[2] * f10 + fArray[0] * f11;
        float f14 = f6 = f12;
        float f15 = f5 = f13;
        f12 = fArray[3] * f8 + fArray[0] * f9;
        f13 = fArray[3] * f10 + fArray[0] * f11;
        if (f12 < f14) {
            f14 = f12;
        } else if (f12 > f6) {
            f6 = f12;
        }
        if (f13 < f15) {
            f15 = f13;
        } else if (f13 > f5) {
            f5 = f13;
        }
        f12 = fArray[2] * f8 + fArray[1] * f9;
        f13 = fArray[2] * f10 + fArray[1] * f11;
        if (f12 < f14) {
            f14 = f12;
        } else if (f12 > f6) {
            f6 = f12;
        }
        if (f13 < f15) {
            f15 = f13;
        } else if (f13 > f5) {
            f5 = f13;
        }
        f12 = fArray[3] * f8 + fArray[1] * f9;
        f13 = fArray[3] * f10 + fArray[1] * f11;
        if (f12 < f14) {
            f14 = f12;
        } else if (f12 > f6) {
            f6 = f12;
        }
        if (f13 < f15) {
            f15 = f13;
        } else if (f13 > f5) {
            f5 = f13;
        }
        fArray[0] = f15;
        fArray[1] = f5;
        fArray[2] = f14;
        fArray[3] = f6;
        if (MarlinConst.DO_LOG_CLIP) {
            MarlinUtils.logInfo("clipRect (ClipInverseDelta): " + Arrays.toString(fArray));
        }
        float f16 = (float)Math.sqrt(f8 * f8 + f9 * f9);
        float f17 = (float)Math.sqrt(f10 * f10 + f11 * f11);
        return 0.5f * (f16 + f17);
    }

    public PathConsumer2D inverseDeltaTransformConsumer(PathConsumer2D pathConsumer2D, BaseTransform baseTransform) {
        if (baseTransform == null) {
            return pathConsumer2D;
        }
        float f = (float)baseTransform.getMxx();
        float f2 = (float)baseTransform.getMxy();
        float f3 = (float)baseTransform.getMyx();
        float f4 = (float)baseTransform.getMyy();
        if (f2 == 0.0f && f3 == 0.0f) {
            if (f == 1.0f && f4 == 1.0f) {
                return pathConsumer2D;
            }
            return this.iv_DeltaScaleFilter.init(pathConsumer2D, 1.0f / f, 1.0f / f4);
        }
        float f5 = f * f4 - f2 * f3;
        return this.iv_DeltaTransformFilter.init(pathConsumer2D, f4 / f5, -f2 / f5, -f3 / f5, f / f5);
    }

    static final class PathTracer
    implements PathConsumer2D {
        private final String prefix;
        private PathConsumer2D out;

        PathTracer(String string) {
            this.prefix = string + ": ";
        }

        PathTracer init(PathConsumer2D pathConsumer2D) {
            this.out = pathConsumer2D;
            return this;
        }

        @Override
        public void moveTo(float f, float f2) {
            this.log("moveTo (" + f + ", " + f2 + ")");
            this.out.moveTo(f, f2);
        }

        @Override
        public void lineTo(float f, float f2) {
            this.log("lineTo (" + f + ", " + f2 + ")");
            this.out.lineTo(f, f2);
        }

        @Override
        public void curveTo(float f, float f2, float f3, float f4, float f5, float f6) {
            this.log("curveTo P1(" + f + ", " + f2 + ") P2(" + f3 + ", " + f4 + ") P3(" + f5 + ", " + f6 + ")");
            this.out.curveTo(f, f2, f3, f4, f5, f6);
        }

        @Override
        public void quadTo(float f, float f2, float f3, float f4) {
            this.log("quadTo P1(" + f + ", " + f2 + ") P2(" + f3 + ", " + f4 + ")");
            this.out.quadTo(f, f2, f3, f4);
        }

        @Override
        public void closePath() {
            this.log("closePath");
            this.out.closePath();
        }

        @Override
        public void pathDone() {
            this.log("pathDone");
            this.out.pathDone();
        }

        private void log(String string) {
            MarlinUtils.logInfo(this.prefix + string);
        }
    }

    public static final class CurveBasicMonotonizer {
        private static final int MAX_N_CURVES = 11;
        private float lw2;
        int nbSplits;
        final float[] middle = new float[68];
        private final float[] subdivTs = new float[10];
        private final Curve curve;

        CurveBasicMonotonizer(RendererContext rendererContext) {
            this.curve = rendererContext.curve;
        }

        public void init(float f) {
            this.lw2 = f * f / 4.0f;
        }

        CurveBasicMonotonizer curve(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8) {
            float[] fArray = this.middle;
            fArray[0] = f;
            fArray[1] = f2;
            fArray[2] = f3;
            fArray[3] = f4;
            fArray[4] = f5;
            fArray[5] = f6;
            fArray[6] = f7;
            fArray[7] = f8;
            float[] fArray2 = this.subdivTs;
            int n = Helpers.findSubdivPoints(this.curve, fArray, fArray2, 8, this.lw2);
            float f9 = 0.0f;
            int n2 = 0;
            int n3 = 0;
            while (n2 < n) {
                float f10 = fArray2[n2];
                Helpers.subdivideCubicAt((f10 - f9) / (1.0f - f9), fArray, n3, fArray, n3, n3 + 6);
                f9 = f10;
                ++n2;
                n3 += 6;
            }
            this.nbSplits = n;
            return this;
        }

        CurveBasicMonotonizer quad(float f, float f2, float f3, float f4, float f5, float f6) {
            float[] fArray = this.middle;
            fArray[0] = f;
            fArray[1] = f2;
            fArray[2] = f3;
            fArray[3] = f4;
            fArray[4] = f5;
            fArray[5] = f6;
            float[] fArray2 = this.subdivTs;
            int n = Helpers.findSubdivPoints(this.curve, fArray, fArray2, 6, this.lw2);
            float f7 = 0.0f;
            int n2 = 0;
            int n3 = 0;
            while (n2 < n) {
                float f8 = fArray2[n2];
                Helpers.subdivideQuadAt((f8 - f7) / (1.0f - f7), fArray, n3, fArray, n3, n3 + 4);
                f7 = f8;
                ++n2;
                n3 += 4;
            }
            this.nbSplits = n;
            return this;
        }
    }

    static final class CurveClipSplitter {
        static final float LEN_TH = MarlinProperties.getSubdividerMinLength();
        static final boolean DO_CHECK_LENGTH = LEN_TH > 0.0f;
        private static final boolean TRACE = false;
        private static final int MAX_N_CURVES = 12;
        private final RendererContext rdrCtx;
        private float minLength;
        final float[] clipRect;
        final float[] clipRectPad = new float[4];
        private boolean init_clipRectPad = false;
        final float[] middle = new float[98];
        private final float[] subdivTs = new float[12];
        private final Curve curve;

        CurveClipSplitter(RendererContext rendererContext) {
            this.rdrCtx = rendererContext;
            this.clipRect = rendererContext.clipRect;
            this.curve = rendererContext.curve;
        }

        void init() {
            this.init_clipRectPad = true;
            if (DO_CHECK_LENGTH) {
                float f = this.minLength = this.rdrCtx.clipInvScale == 0.0f ? LEN_TH : LEN_TH * this.rdrCtx.clipInvScale;
                if (MarlinConst.DO_LOG_CLIP) {
                    MarlinUtils.logInfo("CurveClipSplitter.minLength = " + this.minLength);
                }
            }
        }

        private void initPaddedClip() {
            float[] fArray = this.clipRect;
            float[] fArray2 = this.clipRectPad;
            fArray2[0] = fArray[0] - 1.0f;
            fArray2[1] = fArray[1] + 1.0f;
            fArray2[2] = fArray[2] - 1.0f;
            fArray2[3] = fArray[3] + 1.0f;
        }

        boolean splitLine(float f, float f2, float f3, float f4, int n, PathConsumer2D pathConsumer2D) {
            if (DO_CHECK_LENGTH && Helpers.fastLineLen(f, f2, f3, f4) <= this.minLength) {
                return false;
            }
            float[] fArray = this.middle;
            fArray[0] = f;
            fArray[1] = f2;
            fArray[2] = f3;
            fArray[3] = f4;
            return this.subdivideAtIntersections(4, n, pathConsumer2D);
        }

        boolean splitQuad(float f, float f2, float f3, float f4, float f5, float f6, int n, PathConsumer2D pathConsumer2D) {
            if (DO_CHECK_LENGTH && Helpers.fastQuadLen(f, f2, f3, f4, f5, f6) <= this.minLength) {
                return false;
            }
            float[] fArray = this.middle;
            fArray[0] = f;
            fArray[1] = f2;
            fArray[2] = f3;
            fArray[3] = f4;
            fArray[4] = f5;
            fArray[5] = f6;
            return this.subdivideAtIntersections(6, n, pathConsumer2D);
        }

        boolean splitCurve(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, int n, PathConsumer2D pathConsumer2D) {
            if (DO_CHECK_LENGTH && Helpers.fastCurvelen(f, f2, f3, f4, f5, f6, f7, f8) <= this.minLength) {
                return false;
            }
            float[] fArray = this.middle;
            fArray[0] = f;
            fArray[1] = f2;
            fArray[2] = f3;
            fArray[3] = f4;
            fArray[4] = f5;
            fArray[5] = f6;
            fArray[6] = f7;
            fArray[7] = f8;
            return this.subdivideAtIntersections(8, n, pathConsumer2D);
        }

        private boolean subdivideAtIntersections(int n, int n2, PathConsumer2D pathConsumer2D) {
            int n3;
            float[] fArray = this.middle;
            float[] fArray2 = this.subdivTs;
            if (this.init_clipRectPad) {
                this.init_clipRectPad = false;
                this.initPaddedClip();
            }
            if ((n3 = Helpers.findClipPoints(this.curve, fArray, fArray2, n, n2, this.clipRectPad)) == 0) {
                return false;
            }
            float f = 0.0f;
            int n4 = 0;
            int n5 = 0;
            while (n4 < n3) {
                float f2 = fArray2[n4];
                Helpers.subdivideAt((f2 - f) / (1.0f - f), fArray, n5, fArray, n5, n);
                f = f2;
                ++n4;
                n5 += n;
            }
            n4 = 0;
            n5 = 0;
            while (n4 <= n3) {
                CurveClipSplitter.emitCurrent(n, fArray, n5, pathConsumer2D);
                ++n4;
                n5 += n;
            }
            return true;
        }

        static void emitCurrent(int n, float[] fArray, int n2, PathConsumer2D pathConsumer2D) {
            if (n == 8) {
                pathConsumer2D.curveTo(fArray[n2 + 2], fArray[n2 + 3], fArray[n2 + 4], fArray[n2 + 5], fArray[n2 + 6], fArray[n2 + 7]);
            } else if (n == 4) {
                pathConsumer2D.lineTo(fArray[n2 + 2], fArray[n2 + 3]);
            } else {
                pathConsumer2D.quadTo(fArray[n2 + 2], fArray[n2 + 3], fArray[n2 + 4], fArray[n2 + 5]);
            }
        }
    }

    static final class PathClipFilter
    implements PathConsumer2D {
        private PathConsumer2D out;
        private final float[] clipRect;
        private final float[] corners = new float[8];
        private boolean init_corners = false;
        private final Helpers.IndexStack stack;
        private int cOutCode = 0;
        private int gOutCode = 15;
        private boolean outside = false;
        private float cx0;
        private float cy0;
        private float cox0;
        private float coy0;
        private boolean subdivide = MarlinConst.DO_CLIP_SUBDIVIDER;
        private final CurveClipSplitter curveSplitter;

        PathClipFilter(RendererContext rendererContext) {
            this.clipRect = rendererContext.clipRect;
            this.curveSplitter = rendererContext.curveClipSplitter;
            this.stack = rendererContext.stats != null ? new Helpers.IndexStack(rendererContext, rendererContext.stats.stat_pcf_idxstack_indices, rendererContext.stats.hist_pcf_idxstack_indices, rendererContext.stats.stat_array_pcf_idxstack_indices) : new Helpers.IndexStack(rendererContext);
        }

        PathClipFilter init(PathConsumer2D pathConsumer2D) {
            this.out = pathConsumer2D;
            if (MarlinConst.DO_CLIP_SUBDIVIDER) {
                this.curveSplitter.init();
            }
            this.init_corners = true;
            this.gOutCode = 15;
            return this;
        }

        void dispose() {
            this.stack.dispose();
        }

        private void finishPath() {
            if (this.outside) {
                if (this.gOutCode == 0) {
                    this.finish();
                } else {
                    this.outside = false;
                    this.stack.reset();
                }
            }
        }

        private void finish() {
            this.outside = false;
            if (!this.stack.isEmpty()) {
                if (this.init_corners) {
                    this.init_corners = false;
                    float[] fArray = this.corners;
                    float[] fArray2 = this.clipRect;
                    fArray[0] = fArray2[2];
                    fArray[1] = fArray2[0];
                    fArray[2] = fArray2[2];
                    fArray[3] = fArray2[1];
                    fArray[4] = fArray2[3];
                    fArray[5] = fArray2[0];
                    fArray[6] = fArray2[3];
                    fArray[7] = fArray2[1];
                }
                this.stack.pullAll(this.corners, this.out);
            }
            this.out.lineTo(this.cox0, this.coy0);
            this.cx0 = this.cox0;
            this.cy0 = this.coy0;
        }

        @Override
        public void pathDone() {
            this.finishPath();
            this.out.pathDone();
            this.dispose();
        }

        @Override
        public void closePath() {
            this.finishPath();
            this.out.closePath();
        }

        @Override
        public void moveTo(float f, float f2) {
            this.finishPath();
            this.cOutCode = Helpers.outcode(f, f2, this.clipRect);
            this.outside = false;
            this.out.moveTo(f, f2);
            this.cx0 = f;
            this.cy0 = f2;
        }

        @Override
        public void lineTo(float f, float f2) {
            int n = this.cOutCode;
            int n2 = Helpers.outcode(f, f2, this.clipRect);
            int n3 = n | n2;
            if (n3 != 0) {
                int n4 = n & n2;
                if (n4 == 0) {
                    if (this.subdivide) {
                        this.subdivide = false;
                        boolean bl = this.outside ? this.curveSplitter.splitLine(this.cox0, this.coy0, f, f2, n3, this) : this.curveSplitter.splitLine(this.cx0, this.cy0, f, f2, n3, this);
                        this.subdivide = true;
                        if (bl) {
                            return;
                        }
                    }
                } else {
                    this.cOutCode = n2;
                    this.gOutCode &= n4;
                    this.outside = true;
                    this.cox0 = f;
                    this.coy0 = f2;
                    this.clip(n4, n, n2);
                    return;
                }
            }
            this.cOutCode = n2;
            this.gOutCode = 0;
            if (this.outside) {
                this.finish();
            }
            this.out.lineTo(f, f2);
            this.cx0 = f;
            this.cy0 = f2;
        }

        private void clip(int n, int n2, int n3) {
            if (n2 != n3 && (n & 0xC) != 0) {
                int n4 = n2 | n3;
                int n5 = n4 & 3;
                int n6 = n4 & 0xC;
                int n7 = n6 == 4 ? 0 : 2;
                switch (n5) {
                    case 1: {
                        this.stack.push(n7);
                        return;
                    }
                    case 2: {
                        this.stack.push(n7 + 1);
                        return;
                    }
                }
                if ((n2 & 1) != 0) {
                    this.stack.push(n7);
                    this.stack.push(n7 + 1);
                } else {
                    this.stack.push(n7 + 1);
                    this.stack.push(n7);
                }
            }
        }

        @Override
        public void curveTo(float f, float f2, float f3, float f4, float f5, float f6) {
            int n;
            int n2;
            int n3 = this.cOutCode;
            int n4 = Helpers.outcode(f, f2, this.clipRect);
            int n5 = n3 | n4 | (n2 = Helpers.outcode(f3, f4, this.clipRect)) | (n = Helpers.outcode(f5, f6, this.clipRect));
            if (n5 != 0) {
                int n6 = n3 & n4 & n2 & n;
                if (n6 == 0) {
                    if (this.subdivide) {
                        this.subdivide = false;
                        boolean bl = this.outside ? this.curveSplitter.splitCurve(this.cox0, this.coy0, f, f2, f3, f4, f5, f6, n5, this) : this.curveSplitter.splitCurve(this.cx0, this.cy0, f, f2, f3, f4, f5, f6, n5, this);
                        this.subdivide = true;
                        if (bl) {
                            return;
                        }
                    }
                } else {
                    this.cOutCode = n;
                    this.gOutCode &= n6;
                    this.outside = true;
                    this.cox0 = f5;
                    this.coy0 = f6;
                    this.clip(n6, n3, n);
                    return;
                }
            }
            this.cOutCode = n;
            this.gOutCode = 0;
            if (this.outside) {
                this.finish();
            }
            this.out.curveTo(f, f2, f3, f4, f5, f6);
            this.cx0 = f5;
            this.cy0 = f6;
        }

        @Override
        public void quadTo(float f, float f2, float f3, float f4) {
            int n;
            int n2 = this.cOutCode;
            int n3 = Helpers.outcode(f, f2, this.clipRect);
            int n4 = n2 | n3 | (n = Helpers.outcode(f3, f4, this.clipRect));
            if (n4 != 0) {
                int n5 = n2 & n3 & n;
                if (n5 == 0) {
                    if (this.subdivide) {
                        this.subdivide = false;
                        boolean bl = this.outside ? this.curveSplitter.splitQuad(this.cox0, this.coy0, f, f2, f3, f4, n4, this) : this.curveSplitter.splitQuad(this.cx0, this.cy0, f, f2, f3, f4, n4, this);
                        this.subdivide = true;
                        if (bl) {
                            return;
                        }
                    }
                } else {
                    this.cOutCode = n;
                    this.gOutCode &= n5;
                    this.outside = true;
                    this.cox0 = f3;
                    this.coy0 = f4;
                    this.clip(n5, n2, n);
                    return;
                }
            }
            this.cOutCode = n;
            this.gOutCode = 0;
            if (this.outside) {
                this.finish();
            }
            this.out.quadTo(f, f2, f3, f4);
            this.cx0 = f3;
            this.cy0 = f4;
        }
    }

    static final class ClosedPathDetector
    implements PathConsumer2D {
        private final RendererContext rdrCtx;
        private final Helpers.PolyStack stack;
        private PathConsumer2D out;

        ClosedPathDetector(RendererContext rendererContext) {
            this.rdrCtx = rendererContext;
            this.stack = rendererContext.stats != null ? new Helpers.PolyStack(rendererContext, rendererContext.stats.stat_cpd_polystack_types, rendererContext.stats.stat_cpd_polystack_curves, rendererContext.stats.hist_cpd_polystack_curves, rendererContext.stats.stat_array_cpd_polystack_curves, rendererContext.stats.stat_array_cpd_polystack_types) : new Helpers.PolyStack(rendererContext);
        }

        ClosedPathDetector init(PathConsumer2D pathConsumer2D) {
            this.out = pathConsumer2D;
            return this;
        }

        void dispose() {
            this.stack.dispose();
        }

        @Override
        public void pathDone() {
            this.finish(false);
            this.out.pathDone();
            this.dispose();
        }

        @Override
        public void closePath() {
            this.finish(true);
            this.out.closePath();
        }

        @Override
        public void moveTo(float f, float f2) {
            this.finish(false);
            this.out.moveTo(f, f2);
        }

        private void finish(boolean bl) {
            this.rdrCtx.closedPath = bl;
            this.stack.pullAll(this.out);
        }

        @Override
        public void lineTo(float f, float f2) {
            this.stack.pushLine(f, f2);
        }

        @Override
        public void curveTo(float f, float f2, float f3, float f4, float f5, float f6) {
            this.stack.pushCubic(f5, f6, f3, f4, f, f2);
        }

        @Override
        public void quadTo(float f, float f2, float f3, float f4) {
            this.stack.pushQuad(f3, f4, f, f2);
        }
    }

    static final class Path2DWrapper
    implements PathConsumer2D {
        private Path2D p2d;

        Path2DWrapper() {
        }

        Path2DWrapper init(Path2D path2D) {
            this.p2d = path2D;
            return this;
        }

        @Override
        public void moveTo(float f, float f2) {
            this.p2d.moveTo(f, f2);
        }

        @Override
        public void lineTo(float f, float f2) {
            this.p2d.lineTo(f, f2);
        }

        @Override
        public void closePath() {
            this.p2d.closePath();
        }

        @Override
        public void pathDone() {
        }

        @Override
        public void curveTo(float f, float f2, float f3, float f4, float f5, float f6) {
            this.p2d.curveTo(f, f2, f3, f4, f5, f6);
        }

        @Override
        public void quadTo(float f, float f2, float f3, float f4) {
            this.p2d.quadTo(f, f2, f3, f4);
        }
    }

    static final class DeltaTransformFilter
    implements PathConsumer2D {
        private PathConsumer2D out;
        private float mxx;
        private float mxy;
        private float myx;
        private float myy;

        DeltaTransformFilter() {
        }

        DeltaTransformFilter init(PathConsumer2D pathConsumer2D, float f, float f2, float f3, float f4) {
            this.out = pathConsumer2D;
            this.mxx = f;
            this.mxy = f2;
            this.myx = f3;
            this.myy = f4;
            return this;
        }

        @Override
        public void moveTo(float f, float f2) {
            this.out.moveTo(f * this.mxx + f2 * this.mxy, f * this.myx + f2 * this.myy);
        }

        @Override
        public void lineTo(float f, float f2) {
            this.out.lineTo(f * this.mxx + f2 * this.mxy, f * this.myx + f2 * this.myy);
        }

        @Override
        public void quadTo(float f, float f2, float f3, float f4) {
            this.out.quadTo(f * this.mxx + f2 * this.mxy, f * this.myx + f2 * this.myy, f3 * this.mxx + f4 * this.mxy, f3 * this.myx + f4 * this.myy);
        }

        @Override
        public void curveTo(float f, float f2, float f3, float f4, float f5, float f6) {
            this.out.curveTo(f * this.mxx + f2 * this.mxy, f * this.myx + f2 * this.myy, f3 * this.mxx + f4 * this.mxy, f3 * this.myx + f4 * this.myy, f5 * this.mxx + f6 * this.mxy, f5 * this.myx + f6 * this.myy);
        }

        @Override
        public void closePath() {
            this.out.closePath();
        }

        @Override
        public void pathDone() {
            this.out.pathDone();
        }
    }

    static final class DeltaScaleFilter
    implements PathConsumer2D {
        private PathConsumer2D out;
        private float sx;
        private float sy;

        DeltaScaleFilter() {
        }

        DeltaScaleFilter init(PathConsumer2D pathConsumer2D, float f, float f2) {
            this.out = pathConsumer2D;
            this.sx = f;
            this.sy = f2;
            return this;
        }

        @Override
        public void moveTo(float f, float f2) {
            this.out.moveTo(f * this.sx, f2 * this.sy);
        }

        @Override
        public void lineTo(float f, float f2) {
            this.out.lineTo(f * this.sx, f2 * this.sy);
        }

        @Override
        public void quadTo(float f, float f2, float f3, float f4) {
            this.out.quadTo(f * this.sx, f2 * this.sy, f3 * this.sx, f4 * this.sy);
        }

        @Override
        public void curveTo(float f, float f2, float f3, float f4, float f5, float f6) {
            this.out.curveTo(f * this.sx, f2 * this.sy, f3 * this.sx, f4 * this.sy, f5 * this.sx, f6 * this.sy);
        }

        @Override
        public void closePath() {
            this.out.closePath();
        }

        @Override
        public void pathDone() {
            this.out.pathDone();
        }
    }
}

