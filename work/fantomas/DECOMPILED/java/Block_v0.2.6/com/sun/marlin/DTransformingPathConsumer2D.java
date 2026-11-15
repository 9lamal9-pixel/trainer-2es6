/*
 * Decompiled with CFR 0.152.
 */
package com.sun.marlin;

import com.sun.javafx.geom.Path2D;
import com.sun.javafx.geom.transform.BaseTransform;
import com.sun.marlin.DCurve;
import com.sun.marlin.DHelpers;
import com.sun.marlin.DPathConsumer2D;
import com.sun.marlin.DRendererContext;
import com.sun.marlin.MarlinConst;
import com.sun.marlin.MarlinProperties;
import com.sun.marlin.MarlinUtils;
import java.util.Arrays;

public final class DTransformingPathConsumer2D {
    static final double CLIP_RECT_PADDING = 0.25;
    private final DRendererContext rdrCtx;
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

    DTransformingPathConsumer2D(DRendererContext dRendererContext) {
        this.rdrCtx = dRendererContext;
        this.cpDetector = new ClosedPathDetector(dRendererContext);
        this.pathClipper = new PathClipFilter(dRendererContext);
    }

    public DPathConsumer2D wrapPath2D(Path2D path2D) {
        return this.wp_Path2DWrapper.init(path2D);
    }

    public DPathConsumer2D traceInput(DPathConsumer2D dPathConsumer2D) {
        return this.tracerInput.init(dPathConsumer2D);
    }

    public DPathConsumer2D traceClosedPathDetector(DPathConsumer2D dPathConsumer2D) {
        return this.tracerCPDetector.init(dPathConsumer2D);
    }

    public DPathConsumer2D traceFiller(DPathConsumer2D dPathConsumer2D) {
        return this.tracerFiller.init(dPathConsumer2D);
    }

    public DPathConsumer2D traceStroker(DPathConsumer2D dPathConsumer2D) {
        return this.tracerStroker.init(dPathConsumer2D);
    }

    public DPathConsumer2D traceDasher(DPathConsumer2D dPathConsumer2D) {
        return this.tracerDasher.init(dPathConsumer2D);
    }

    public DPathConsumer2D detectClosedPath(DPathConsumer2D dPathConsumer2D) {
        return this.cpDetector.init(dPathConsumer2D);
    }

    public DPathConsumer2D pathClipper(DPathConsumer2D dPathConsumer2D) {
        return this.pathClipper.init(dPathConsumer2D);
    }

    public DPathConsumer2D deltaTransformConsumer(DPathConsumer2D dPathConsumer2D, BaseTransform baseTransform) {
        if (baseTransform == null) {
            return dPathConsumer2D;
        }
        double d = baseTransform.getMxx();
        double d2 = baseTransform.getMxy();
        double d3 = baseTransform.getMyx();
        double d4 = baseTransform.getMyy();
        if (d2 == 0.0 && d3 == 0.0) {
            if (d == 1.0 && d4 == 1.0) {
                return dPathConsumer2D;
            }
            if (this.rdrCtx.doClip) {
                this.rdrCtx.clipInvScale = DTransformingPathConsumer2D.adjustClipScale(this.rdrCtx.clipRect, d, d4);
            }
            return this.dt_DeltaScaleFilter.init(dPathConsumer2D, d, d4);
        }
        if (this.rdrCtx.doClip) {
            this.rdrCtx.clipInvScale = DTransformingPathConsumer2D.adjustClipInverseDelta(this.rdrCtx.clipRect, d, d2, d3, d4);
        }
        return this.dt_DeltaTransformFilter.init(dPathConsumer2D, d, d2, d3, d4);
    }

    private static double adjustClipScale(double[] dArray, double d, double d2) {
        double d3;
        double d4 = 1.0 / d2;
        dArray[0] = dArray[0] * d4;
        dArray[1] = dArray[1] * d4;
        if (dArray[1] < dArray[0]) {
            d3 = dArray[0];
            dArray[0] = dArray[1];
            dArray[1] = d3;
        }
        d3 = 1.0 / d;
        dArray[2] = dArray[2] * d3;
        dArray[3] = dArray[3] * d3;
        if (dArray[3] < dArray[2]) {
            double d5 = dArray[2];
            dArray[2] = dArray[3];
            dArray[3] = d5;
        }
        if (MarlinConst.DO_LOG_CLIP) {
            MarlinUtils.logInfo("clipRect (ClipScale): " + Arrays.toString(dArray));
        }
        return 0.5 * (Math.abs(d3) + Math.abs(d4));
    }

    private static double adjustClipInverseDelta(double[] dArray, double d, double d2, double d3, double d4) {
        double d5;
        double d6;
        double d7 = d * d4 - d2 * d3;
        double d8 = d4 / d7;
        double d9 = -d2 / d7;
        double d10 = -d3 / d7;
        double d11 = d / d7;
        double d12 = dArray[2] * d8 + dArray[0] * d9;
        double d13 = dArray[2] * d10 + dArray[0] * d11;
        double d14 = d6 = d12;
        double d15 = d5 = d13;
        d12 = dArray[3] * d8 + dArray[0] * d9;
        d13 = dArray[3] * d10 + dArray[0] * d11;
        if (d12 < d14) {
            d14 = d12;
        } else if (d12 > d6) {
            d6 = d12;
        }
        if (d13 < d15) {
            d15 = d13;
        } else if (d13 > d5) {
            d5 = d13;
        }
        d12 = dArray[2] * d8 + dArray[1] * d9;
        d13 = dArray[2] * d10 + dArray[1] * d11;
        if (d12 < d14) {
            d14 = d12;
        } else if (d12 > d6) {
            d6 = d12;
        }
        if (d13 < d15) {
            d15 = d13;
        } else if (d13 > d5) {
            d5 = d13;
        }
        d12 = dArray[3] * d8 + dArray[1] * d9;
        d13 = dArray[3] * d10 + dArray[1] * d11;
        if (d12 < d14) {
            d14 = d12;
        } else if (d12 > d6) {
            d6 = d12;
        }
        if (d13 < d15) {
            d15 = d13;
        } else if (d13 > d5) {
            d5 = d13;
        }
        dArray[0] = d15;
        dArray[1] = d5;
        dArray[2] = d14;
        dArray[3] = d6;
        if (MarlinConst.DO_LOG_CLIP) {
            MarlinUtils.logInfo("clipRect (ClipInverseDelta): " + Arrays.toString(dArray));
        }
        double d16 = Math.sqrt(d8 * d8 + d9 * d9);
        double d17 = Math.sqrt(d10 * d10 + d11 * d11);
        return 0.5 * (d16 + d17);
    }

    public DPathConsumer2D inverseDeltaTransformConsumer(DPathConsumer2D dPathConsumer2D, BaseTransform baseTransform) {
        if (baseTransform == null) {
            return dPathConsumer2D;
        }
        double d = baseTransform.getMxx();
        double d2 = baseTransform.getMxy();
        double d3 = baseTransform.getMyx();
        double d4 = baseTransform.getMyy();
        if (d2 == 0.0 && d3 == 0.0) {
            if (d == 1.0 && d4 == 1.0) {
                return dPathConsumer2D;
            }
            return this.iv_DeltaScaleFilter.init(dPathConsumer2D, 1.0 / d, 1.0 / d4);
        }
        double d5 = d * d4 - d2 * d3;
        return this.iv_DeltaTransformFilter.init(dPathConsumer2D, d4 / d5, -d2 / d5, -d3 / d5, d / d5);
    }

    static final class PathTracer
    implements DPathConsumer2D {
        private final String prefix;
        private DPathConsumer2D out;

        PathTracer(String string) {
            this.prefix = string + ": ";
        }

        PathTracer init(DPathConsumer2D dPathConsumer2D) {
            this.out = dPathConsumer2D;
            return this;
        }

        @Override
        public void moveTo(double d, double d2) {
            this.log("moveTo (" + d + ", " + d2 + ")");
            this.out.moveTo(d, d2);
        }

        @Override
        public void lineTo(double d, double d2) {
            this.log("lineTo (" + d + ", " + d2 + ")");
            this.out.lineTo(d, d2);
        }

        @Override
        public void curveTo(double d, double d2, double d3, double d4, double d5, double d6) {
            this.log("curveTo P1(" + d + ", " + d2 + ") P2(" + d3 + ", " + d4 + ") P3(" + d5 + ", " + d6 + ")");
            this.out.curveTo(d, d2, d3, d4, d5, d6);
        }

        @Override
        public void quadTo(double d, double d2, double d3, double d4) {
            this.log("quadTo P1(" + d + ", " + d2 + ") P2(" + d3 + ", " + d4 + ")");
            this.out.quadTo(d, d2, d3, d4);
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
        private double lw2;
        int nbSplits;
        final double[] middle = new double[68];
        private final double[] subdivTs = new double[10];
        private final DCurve curve;

        CurveBasicMonotonizer(DRendererContext dRendererContext) {
            this.curve = dRendererContext.curve;
        }

        public void init(double d) {
            this.lw2 = d * d / 4.0;
        }

        CurveBasicMonotonizer curve(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8) {
            double[] dArray = this.middle;
            dArray[0] = d;
            dArray[1] = d2;
            dArray[2] = d3;
            dArray[3] = d4;
            dArray[4] = d5;
            dArray[5] = d6;
            dArray[6] = d7;
            dArray[7] = d8;
            double[] dArray2 = this.subdivTs;
            int n = DHelpers.findSubdivPoints(this.curve, dArray, dArray2, 8, this.lw2);
            double d9 = 0.0;
            int n2 = 0;
            int n3 = 0;
            while (n2 < n) {
                double d10 = dArray2[n2];
                DHelpers.subdivideCubicAt((d10 - d9) / (1.0 - d9), dArray, n3, dArray, n3, n3 + 6);
                d9 = d10;
                ++n2;
                n3 += 6;
            }
            this.nbSplits = n;
            return this;
        }

        CurveBasicMonotonizer quad(double d, double d2, double d3, double d4, double d5, double d6) {
            double[] dArray = this.middle;
            dArray[0] = d;
            dArray[1] = d2;
            dArray[2] = d3;
            dArray[3] = d4;
            dArray[4] = d5;
            dArray[5] = d6;
            double[] dArray2 = this.subdivTs;
            int n = DHelpers.findSubdivPoints(this.curve, dArray, dArray2, 6, this.lw2);
            double d7 = 0.0;
            int n2 = 0;
            int n3 = 0;
            while (n2 < n) {
                double d8 = dArray2[n2];
                DHelpers.subdivideQuadAt((d8 - d7) / (1.0 - d7), dArray, n3, dArray, n3, n3 + 4);
                d7 = d8;
                ++n2;
                n3 += 4;
            }
            this.nbSplits = n;
            return this;
        }
    }

    static final class CurveClipSplitter {
        static final double LEN_TH = MarlinProperties.getSubdividerMinLength();
        static final boolean DO_CHECK_LENGTH = LEN_TH > 0.0;
        private static final boolean TRACE = false;
        private static final int MAX_N_CURVES = 12;
        private final DRendererContext rdrCtx;
        private double minLength;
        final double[] clipRect;
        final double[] clipRectPad = new double[4];
        private boolean init_clipRectPad = false;
        final double[] middle = new double[98];
        private final double[] subdivTs = new double[12];
        private final DCurve curve;

        CurveClipSplitter(DRendererContext dRendererContext) {
            this.rdrCtx = dRendererContext;
            this.clipRect = dRendererContext.clipRect;
            this.curve = dRendererContext.curve;
        }

        void init() {
            this.init_clipRectPad = true;
            if (DO_CHECK_LENGTH) {
                double d = this.minLength = this.rdrCtx.clipInvScale == 0.0 ? LEN_TH : LEN_TH * this.rdrCtx.clipInvScale;
                if (MarlinConst.DO_LOG_CLIP) {
                    MarlinUtils.logInfo("CurveClipSplitter.minLength = " + this.minLength);
                }
            }
        }

        private void initPaddedClip() {
            double[] dArray = this.clipRect;
            double[] dArray2 = this.clipRectPad;
            dArray2[0] = dArray[0] - 0.25;
            dArray2[1] = dArray[1] + 0.25;
            dArray2[2] = dArray[2] - 0.25;
            dArray2[3] = dArray[3] + 0.25;
        }

        boolean splitLine(double d, double d2, double d3, double d4, int n, DPathConsumer2D dPathConsumer2D) {
            if (DO_CHECK_LENGTH && DHelpers.fastLineLen(d, d2, d3, d4) <= this.minLength) {
                return false;
            }
            double[] dArray = this.middle;
            dArray[0] = d;
            dArray[1] = d2;
            dArray[2] = d3;
            dArray[3] = d4;
            return this.subdivideAtIntersections(4, n, dPathConsumer2D);
        }

        boolean splitQuad(double d, double d2, double d3, double d4, double d5, double d6, int n, DPathConsumer2D dPathConsumer2D) {
            if (DO_CHECK_LENGTH && DHelpers.fastQuadLen(d, d2, d3, d4, d5, d6) <= this.minLength) {
                return false;
            }
            double[] dArray = this.middle;
            dArray[0] = d;
            dArray[1] = d2;
            dArray[2] = d3;
            dArray[3] = d4;
            dArray[4] = d5;
            dArray[5] = d6;
            return this.subdivideAtIntersections(6, n, dPathConsumer2D);
        }

        boolean splitCurve(double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, int n, DPathConsumer2D dPathConsumer2D) {
            if (DO_CHECK_LENGTH && DHelpers.fastCurvelen(d, d2, d3, d4, d5, d6, d7, d8) <= this.minLength) {
                return false;
            }
            double[] dArray = this.middle;
            dArray[0] = d;
            dArray[1] = d2;
            dArray[2] = d3;
            dArray[3] = d4;
            dArray[4] = d5;
            dArray[5] = d6;
            dArray[6] = d7;
            dArray[7] = d8;
            return this.subdivideAtIntersections(8, n, dPathConsumer2D);
        }

        private boolean subdivideAtIntersections(int n, int n2, DPathConsumer2D dPathConsumer2D) {
            int n3;
            double[] dArray = this.middle;
            double[] dArray2 = this.subdivTs;
            if (this.init_clipRectPad) {
                this.init_clipRectPad = false;
                this.initPaddedClip();
            }
            if ((n3 = DHelpers.findClipPoints(this.curve, dArray, dArray2, n, n2, this.clipRectPad)) == 0) {
                return false;
            }
            double d = 0.0;
            int n4 = 0;
            int n5 = 0;
            while (n4 < n3) {
                double d2 = dArray2[n4];
                DHelpers.subdivideAt((d2 - d) / (1.0 - d), dArray, n5, dArray, n5, n);
                d = d2;
                ++n4;
                n5 += n;
            }
            n4 = 0;
            n5 = 0;
            while (n4 <= n3) {
                CurveClipSplitter.emitCurrent(n, dArray, n5, dPathConsumer2D);
                ++n4;
                n5 += n;
            }
            return true;
        }

        static void emitCurrent(int n, double[] dArray, int n2, DPathConsumer2D dPathConsumer2D) {
            if (n == 8) {
                dPathConsumer2D.curveTo(dArray[n2 + 2], dArray[n2 + 3], dArray[n2 + 4], dArray[n2 + 5], dArray[n2 + 6], dArray[n2 + 7]);
            } else if (n == 4) {
                dPathConsumer2D.lineTo(dArray[n2 + 2], dArray[n2 + 3]);
            } else {
                dPathConsumer2D.quadTo(dArray[n2 + 2], dArray[n2 + 3], dArray[n2 + 4], dArray[n2 + 5]);
            }
        }
    }

    static final class PathClipFilter
    implements DPathConsumer2D {
        private DPathConsumer2D out;
        private final double[] clipRect;
        private final double[] corners = new double[8];
        private boolean init_corners = false;
        private final DHelpers.IndexStack stack;
        private int cOutCode = 0;
        private int gOutCode = 15;
        private boolean outside = false;
        private double cx0;
        private double cy0;
        private double cox0;
        private double coy0;
        private boolean subdivide = MarlinConst.DO_CLIP_SUBDIVIDER;
        private final CurveClipSplitter curveSplitter;

        PathClipFilter(DRendererContext dRendererContext) {
            this.clipRect = dRendererContext.clipRect;
            this.curveSplitter = dRendererContext.curveClipSplitter;
            this.stack = dRendererContext.stats != null ? new DHelpers.IndexStack(dRendererContext, dRendererContext.stats.stat_pcf_idxstack_indices, dRendererContext.stats.hist_pcf_idxstack_indices, dRendererContext.stats.stat_array_pcf_idxstack_indices) : new DHelpers.IndexStack(dRendererContext);
        }

        PathClipFilter init(DPathConsumer2D dPathConsumer2D) {
            this.out = dPathConsumer2D;
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
                    double[] dArray = this.corners;
                    double[] dArray2 = this.clipRect;
                    dArray[0] = dArray2[2];
                    dArray[1] = dArray2[0];
                    dArray[2] = dArray2[2];
                    dArray[3] = dArray2[1];
                    dArray[4] = dArray2[3];
                    dArray[5] = dArray2[0];
                    dArray[6] = dArray2[3];
                    dArray[7] = dArray2[1];
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
        public void moveTo(double d, double d2) {
            this.finishPath();
            this.cOutCode = DHelpers.outcode(d, d2, this.clipRect);
            this.outside = false;
            this.out.moveTo(d, d2);
            this.cx0 = d;
            this.cy0 = d2;
        }

        @Override
        public void lineTo(double d, double d2) {
            int n = this.cOutCode;
            int n2 = DHelpers.outcode(d, d2, this.clipRect);
            int n3 = n | n2;
            if (n3 != 0) {
                int n4 = n & n2;
                if (n4 == 0) {
                    if (this.subdivide) {
                        this.subdivide = false;
                        boolean bl = this.outside ? this.curveSplitter.splitLine(this.cox0, this.coy0, d, d2, n3, this) : this.curveSplitter.splitLine(this.cx0, this.cy0, d, d2, n3, this);
                        this.subdivide = true;
                        if (bl) {
                            return;
                        }
                    }
                } else {
                    this.cOutCode = n2;
                    this.gOutCode &= n4;
                    this.outside = true;
                    this.cox0 = d;
                    this.coy0 = d2;
                    this.clip(n4, n, n2);
                    return;
                }
            }
            this.cOutCode = n2;
            this.gOutCode = 0;
            if (this.outside) {
                this.finish();
            }
            this.out.lineTo(d, d2);
            this.cx0 = d;
            this.cy0 = d2;
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
        public void curveTo(double d, double d2, double d3, double d4, double d5, double d6) {
            int n;
            int n2;
            int n3 = this.cOutCode;
            int n4 = DHelpers.outcode(d, d2, this.clipRect);
            int n5 = n3 | n4 | (n2 = DHelpers.outcode(d3, d4, this.clipRect)) | (n = DHelpers.outcode(d5, d6, this.clipRect));
            if (n5 != 0) {
                int n6 = n3 & n4 & n2 & n;
                if (n6 == 0) {
                    if (this.subdivide) {
                        this.subdivide = false;
                        boolean bl = this.outside ? this.curveSplitter.splitCurve(this.cox0, this.coy0, d, d2, d3, d4, d5, d6, n5, this) : this.curveSplitter.splitCurve(this.cx0, this.cy0, d, d2, d3, d4, d5, d6, n5, this);
                        this.subdivide = true;
                        if (bl) {
                            return;
                        }
                    }
                } else {
                    this.cOutCode = n;
                    this.gOutCode &= n6;
                    this.outside = true;
                    this.cox0 = d5;
                    this.coy0 = d6;
                    this.clip(n6, n3, n);
                    return;
                }
            }
            this.cOutCode = n;
            this.gOutCode = 0;
            if (this.outside) {
                this.finish();
            }
            this.out.curveTo(d, d2, d3, d4, d5, d6);
            this.cx0 = d5;
            this.cy0 = d6;
        }

        @Override
        public void quadTo(double d, double d2, double d3, double d4) {
            int n;
            int n2 = this.cOutCode;
            int n3 = DHelpers.outcode(d, d2, this.clipRect);
            int n4 = n2 | n3 | (n = DHelpers.outcode(d3, d4, this.clipRect));
            if (n4 != 0) {
                int n5 = n2 & n3 & n;
                if (n5 == 0) {
                    if (this.subdivide) {
                        this.subdivide = false;
                        boolean bl = this.outside ? this.curveSplitter.splitQuad(this.cox0, this.coy0, d, d2, d3, d4, n4, this) : this.curveSplitter.splitQuad(this.cx0, this.cy0, d, d2, d3, d4, n4, this);
                        this.subdivide = true;
                        if (bl) {
                            return;
                        }
                    }
                } else {
                    this.cOutCode = n;
                    this.gOutCode &= n5;
                    this.outside = true;
                    this.cox0 = d3;
                    this.coy0 = d4;
                    this.clip(n5, n2, n);
                    return;
                }
            }
            this.cOutCode = n;
            this.gOutCode = 0;
            if (this.outside) {
                this.finish();
            }
            this.out.quadTo(d, d2, d3, d4);
            this.cx0 = d3;
            this.cy0 = d4;
        }
    }

    static final class ClosedPathDetector
    implements DPathConsumer2D {
        private final DRendererContext rdrCtx;
        private final DHelpers.PolyStack stack;
        private DPathConsumer2D out;

        ClosedPathDetector(DRendererContext dRendererContext) {
            this.rdrCtx = dRendererContext;
            this.stack = dRendererContext.stats != null ? new DHelpers.PolyStack(dRendererContext, dRendererContext.stats.stat_cpd_polystack_types, dRendererContext.stats.stat_cpd_polystack_curves, dRendererContext.stats.hist_cpd_polystack_curves, dRendererContext.stats.stat_array_cpd_polystack_curves, dRendererContext.stats.stat_array_cpd_polystack_types) : new DHelpers.PolyStack(dRendererContext);
        }

        ClosedPathDetector init(DPathConsumer2D dPathConsumer2D) {
            this.out = dPathConsumer2D;
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
        public void moveTo(double d, double d2) {
            this.finish(false);
            this.out.moveTo(d, d2);
        }

        private void finish(boolean bl) {
            this.rdrCtx.closedPath = bl;
            this.stack.pullAll(this.out);
        }

        @Override
        public void lineTo(double d, double d2) {
            this.stack.pushLine(d, d2);
        }

        @Override
        public void curveTo(double d, double d2, double d3, double d4, double d5, double d6) {
            this.stack.pushCubic(d5, d6, d3, d4, d, d2);
        }

        @Override
        public void quadTo(double d, double d2, double d3, double d4) {
            this.stack.pushQuad(d3, d4, d, d2);
        }
    }

    static final class Path2DWrapper
    implements DPathConsumer2D {
        private Path2D p2d;

        Path2DWrapper() {
        }

        Path2DWrapper init(Path2D path2D) {
            this.p2d = path2D;
            return this;
        }

        @Override
        public void moveTo(double d, double d2) {
            this.p2d.moveTo((float)d, (float)d2);
        }

        @Override
        public void lineTo(double d, double d2) {
            this.p2d.lineTo((float)d, (float)d2);
        }

        @Override
        public void closePath() {
            this.p2d.closePath();
        }

        @Override
        public void pathDone() {
        }

        @Override
        public void curveTo(double d, double d2, double d3, double d4, double d5, double d6) {
            this.p2d.curveTo((float)d, (float)d2, (float)d3, (float)d4, (float)d5, (float)d6);
        }

        @Override
        public void quadTo(double d, double d2, double d3, double d4) {
            this.p2d.quadTo((float)d, (float)d2, (float)d3, (float)d4);
        }
    }

    static final class DeltaTransformFilter
    implements DPathConsumer2D {
        private DPathConsumer2D out;
        private double mxx;
        private double mxy;
        private double myx;
        private double myy;

        DeltaTransformFilter() {
        }

        DeltaTransformFilter init(DPathConsumer2D dPathConsumer2D, double d, double d2, double d3, double d4) {
            this.out = dPathConsumer2D;
            this.mxx = d;
            this.mxy = d2;
            this.myx = d3;
            this.myy = d4;
            return this;
        }

        @Override
        public void moveTo(double d, double d2) {
            this.out.moveTo(d * this.mxx + d2 * this.mxy, d * this.myx + d2 * this.myy);
        }

        @Override
        public void lineTo(double d, double d2) {
            this.out.lineTo(d * this.mxx + d2 * this.mxy, d * this.myx + d2 * this.myy);
        }

        @Override
        public void quadTo(double d, double d2, double d3, double d4) {
            this.out.quadTo(d * this.mxx + d2 * this.mxy, d * this.myx + d2 * this.myy, d3 * this.mxx + d4 * this.mxy, d3 * this.myx + d4 * this.myy);
        }

        @Override
        public void curveTo(double d, double d2, double d3, double d4, double d5, double d6) {
            this.out.curveTo(d * this.mxx + d2 * this.mxy, d * this.myx + d2 * this.myy, d3 * this.mxx + d4 * this.mxy, d3 * this.myx + d4 * this.myy, d5 * this.mxx + d6 * this.mxy, d5 * this.myx + d6 * this.myy);
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
    implements DPathConsumer2D {
        private DPathConsumer2D out;
        private double sx;
        private double sy;

        DeltaScaleFilter() {
        }

        DeltaScaleFilter init(DPathConsumer2D dPathConsumer2D, double d, double d2) {
            this.out = dPathConsumer2D;
            this.sx = d;
            this.sy = d2;
            return this;
        }

        @Override
        public void moveTo(double d, double d2) {
            this.out.moveTo(d * this.sx, d2 * this.sy);
        }

        @Override
        public void lineTo(double d, double d2) {
            this.out.lineTo(d * this.sx, d2 * this.sy);
        }

        @Override
        public void quadTo(double d, double d2, double d3, double d4) {
            this.out.quadTo(d * this.sx, d2 * this.sy, d3 * this.sx, d4 * this.sy);
        }

        @Override
        public void curveTo(double d, double d2, double d3, double d4, double d5, double d6) {
            this.out.curveTo(d * this.sx, d2 * this.sy, d3 * this.sx, d4 * this.sy, d5 * this.sx, d6 * this.sy);
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

