/*
 * Decompiled with CFR 0.152.
 */
package com.sun.marlin;

import com.sun.javafx.geom.PathConsumer2D;
import com.sun.marlin.FloatArrayCache;
import com.sun.marlin.FloatMath;
import com.sun.marlin.Helpers;
import com.sun.marlin.MarlinConst;
import com.sun.marlin.MarlinProperties;
import com.sun.marlin.RendererContext;
import com.sun.marlin.TransformingPathConsumer2D;

public final class Dasher
implements PathConsumer2D,
MarlinConst {
    static final int REC_LIMIT = 16;
    static final float CURVE_LEN_ERR = MarlinProperties.getCurveLengthError();
    static final float MIN_T_INC = 1.5258789E-5f;
    static final float MAX_CYCLES = 1.6E7f;
    private PathConsumer2D out;
    private float[] dash;
    private int dashLen;
    private float startPhase;
    private boolean startDashOn;
    private int startIdx;
    private boolean starting;
    private boolean needsMoveTo;
    private int idx;
    private boolean dashOn;
    private float phase;
    private float sx0;
    private float sy0;
    private float cx0;
    private float cy0;
    private final float[] curCurvepts;
    final RendererContext rdrCtx;
    boolean recycleDashes;
    private float[] firstSegmentsBuffer;
    private int firstSegidx;
    final FloatArrayCache.Reference dashes_ref;
    final FloatArrayCache.Reference firstSegmentsBuffer_ref;
    private float[] clipRect;
    private int cOutCode = 0;
    private boolean subdivide = DO_CLIP_SUBDIVIDER;
    private final LengthIterator li = new LengthIterator();
    private final TransformingPathConsumer2D.CurveClipSplitter curveSplitter;
    private float cycleLen;
    private boolean outside;
    private float totalSkipLen;

    Dasher(RendererContext rendererContext) {
        this.rdrCtx = rendererContext;
        this.dashes_ref = rendererContext.newDirtyFloatArrayRef(256);
        this.firstSegmentsBuffer_ref = rendererContext.newDirtyFloatArrayRef(256);
        this.firstSegmentsBuffer = this.firstSegmentsBuffer_ref.initial;
        this.curCurvepts = new float[16];
        this.curveSplitter = rendererContext.curveClipSplitter;
    }

    public Dasher init(PathConsumer2D pathConsumer2D, float[] fArray, int n, float f, boolean bl) {
        this.out = pathConsumer2D;
        int n2 = 0;
        this.dashOn = true;
        float f2 = 0.0f;
        for (int i = 0; i < n; ++i) {
            f2 += fArray[i];
        }
        this.cycleLen = f2;
        float f3 = f / f2;
        if (f < 0.0f) {
            if (-f3 >= 1.6E7f) {
                f = 0.0f;
            } else {
                int n3 = FloatMath.floor_int(-f3);
                if ((n3 & n & 1) != 0) {
                    this.dashOn = !this.dashOn;
                }
                f += (float)n3 * f2;
                while (f < 0.0f) {
                    if (--n2 < 0) {
                        n2 = n - 1;
                    }
                    f += fArray[n2];
                    this.dashOn = !this.dashOn;
                }
            }
        } else if (f > 0.0f) {
            if (f3 >= 1.6E7f) {
                f = 0.0f;
            } else {
                int n4 = FloatMath.floor_int(f3);
                if ((n4 & n & 1) != 0) {
                    this.dashOn = !this.dashOn;
                }
                f -= (float)n4 * f2;
                while (true) {
                    float f4;
                    float f5 = fArray[n2];
                    if (!(f >= f4)) break;
                    f -= f5;
                    n2 = (n2 + 1) % n;
                    this.dashOn = !this.dashOn;
                }
            }
        }
        this.dash = fArray;
        this.dashLen = n;
        this.phase = f;
        this.startPhase = f;
        this.startDashOn = this.dashOn;
        this.startIdx = n2;
        this.starting = true;
        this.needsMoveTo = false;
        this.firstSegidx = 0;
        this.recycleDashes = bl;
        if (this.rdrCtx.doClip) {
            this.clipRect = this.rdrCtx.clipRect;
        } else {
            this.clipRect = null;
            this.cOutCode = 0;
        }
        return this;
    }

    void dispose() {
        if (this.recycleDashes) {
            this.dash = this.dashes_ref.putArray(this.dash);
        }
        this.firstSegmentsBuffer = this.firstSegmentsBuffer_ref.putArray(this.firstSegmentsBuffer);
    }

    public float[] copyDashArray(float[] fArray) {
        float[] fArray2;
        int n = fArray.length;
        if (n <= 256) {
            fArray2 = this.dashes_ref.initial;
        } else {
            if (DO_STATS) {
                this.rdrCtx.stats.stat_array_dasher_dasher.add(n);
            }
            fArray2 = this.dashes_ref.getArray(n);
        }
        System.arraycopy(fArray, 0, fArray2, 0, n);
        return fArray2;
    }

    @Override
    public void moveTo(float f, float f2) {
        if (this.firstSegidx != 0) {
            this.out.moveTo(this.sx0, this.sy0);
            this.emitFirstSegments();
        }
        this.needsMoveTo = true;
        this.idx = this.startIdx;
        this.dashOn = this.startDashOn;
        this.phase = this.startPhase;
        this.cx0 = f;
        this.cy0 = f2;
        this.sx0 = f;
        this.sy0 = f2;
        this.starting = true;
        if (this.clipRect != null) {
            int n;
            this.cOutCode = n = Helpers.outcode(f, f2, this.clipRect);
            this.outside = false;
            this.totalSkipLen = 0.0f;
        }
    }

    private void emitSeg(float[] fArray, int n, int n2) {
        switch (n2) {
            case 4: {
                this.out.lineTo(fArray[n], fArray[n + 1]);
                return;
            }
            case 8: {
                this.out.curveTo(fArray[n], fArray[n + 1], fArray[n + 2], fArray[n + 3], fArray[n + 4], fArray[n + 5]);
                return;
            }
            case 6: {
                this.out.quadTo(fArray[n], fArray[n + 1], fArray[n + 2], fArray[n + 3]);
                return;
            }
        }
    }

    private void emitFirstSegments() {
        int n;
        float[] fArray = this.firstSegmentsBuffer;
        int n2 = this.firstSegidx;
        for (int i = 0; i < n2; i += n - 1) {
            n = (int)fArray[i];
            this.emitSeg(fArray, i + 1, n);
        }
        this.firstSegidx = 0;
    }

    private void goTo(float[] fArray, int n, int n2, boolean bl) {
        int n3 = n + n2;
        float f = fArray[n3 - 4];
        float f2 = fArray[n3 - 3];
        if (bl) {
            if (this.starting) {
                this.goTo_starting(fArray, n, n2);
            } else {
                if (this.needsMoveTo) {
                    this.needsMoveTo = false;
                    this.out.moveTo(this.cx0, this.cy0);
                }
                this.emitSeg(fArray, n, n2);
            }
        } else {
            if (this.starting) {
                this.starting = false;
            }
            this.needsMoveTo = true;
        }
        this.cx0 = f;
        this.cy0 = f2;
    }

    private void goTo_starting(float[] fArray, int n, int n2) {
        int n3 = this.firstSegidx;
        int n4 = n2 - 1;
        float[] fArray2 = this.firstSegmentsBuffer;
        if (n3 + n4 > fArray2.length) {
            if (DO_STATS) {
                this.rdrCtx.stats.stat_array_dasher_firstSegmentsBuffer.add(n3 + n4);
            }
            this.firstSegmentsBuffer = fArray2 = this.firstSegmentsBuffer_ref.widenArray(fArray2, n3, n3 + n4);
        }
        fArray2[n3++] = n2;
        System.arraycopy(fArray, n, fArray2, n3, --n4);
        this.firstSegidx = n3 + n4;
    }

    @Override
    public void lineTo(float f, float f2) {
        int n = this.cOutCode;
        if (this.clipRect != null) {
            int n2 = Helpers.outcode(f, f2, this.clipRect);
            int n3 = n | n2;
            if (n3 != 0) {
                int n4 = n & n2;
                if (n4 == 0) {
                    if (this.subdivide) {
                        this.subdivide = false;
                        boolean bl = this.curveSplitter.splitLine(this.cx0, this.cy0, f, f2, n3, this);
                        this.subdivide = true;
                        if (bl) {
                            return;
                        }
                    }
                } else {
                    this.cOutCode = n2;
                    this.skipLineTo(f, f2);
                    return;
                }
            }
            this.cOutCode = n2;
            if (this.outside) {
                this.outside = false;
                this.skipLen();
            }
        }
        this._lineTo(f, f2);
    }

    private void _lineTo(float f, float f2) {
        float f3 = f - this.cx0;
        float f4 = f2 - this.cy0;
        float f5 = f3 * f3 + f4 * f4;
        if (f5 == 0.0f) {
            return;
        }
        f5 = (float)Math.sqrt(f5);
        float f6 = f3 / f5;
        float f7 = f4 / f5;
        float[] fArray = this.curCurvepts;
        float[] fArray2 = this.dash;
        int n = this.dashLen;
        int n2 = this.idx;
        boolean bl = this.dashOn;
        float f8 = this.phase;
        while (true) {
            float f9;
            float f10;
            if (f5 <= (f10 = (f9 = fArray2[n2]) - f8)) {
                fArray[0] = f;
                fArray[1] = f2;
                this.goTo(fArray, 0, 4, bl);
                f8 += f5;
                if (f5 != f10) break;
                f8 = 0.0f;
                n2 = (n2 + 1) % n;
                bl = !bl;
                break;
            }
            if (f8 == 0.0f) {
                fArray[0] = this.cx0 + f9 * f6;
                fArray[1] = this.cy0 + f9 * f7;
            } else {
                fArray[0] = this.cx0 + f10 * f6;
                fArray[1] = this.cy0 + f10 * f7;
            }
            this.goTo(fArray, 0, 4, bl);
            f5 -= f10;
            n2 = (n2 + 1) % n;
            bl = !bl;
            f8 = 0.0f;
        }
        this.idx = n2;
        this.dashOn = bl;
        this.phase = f8;
    }

    private void skipLineTo(float f, float f2) {
        float f3 = f - this.cx0;
        float f4 = f2 - this.cy0;
        float f5 = f3 * f3 + f4 * f4;
        if (f5 != 0.0f) {
            f5 = (float)Math.sqrt(f5);
        }
        this.outside = true;
        this.totalSkipLen += f5;
        this.needsMoveTo = true;
        this.starting = false;
        this.cx0 = f;
        this.cy0 = f2;
    }

    public void skipLen() {
        float f = this.totalSkipLen;
        this.totalSkipLen = 0.0f;
        float[] fArray = this.dash;
        int n = this.dashLen;
        int n2 = this.idx;
        boolean bl = this.dashOn;
        float f2 = this.phase;
        long l = (long)Math.floor(f / this.cycleLen) - 2L;
        if (l > 0L) {
            f -= this.cycleLen * (float)l;
            long l2 = l * (long)n;
            n2 = (int)(l2 + (long)n2) % n;
            boolean bl2 = bl = (l2 + (bl ? 1L : 0L) & 1L) == 1L;
        }
        while (true) {
            float f3;
            float f4;
            if (f <= (f4 = (f3 = fArray[n2]) - f2)) {
                f2 += f;
                if (f != f4) break;
                f2 = 0.0f;
                n2 = (n2 + 1) % n;
                bl = !bl;
                break;
            }
            f -= f4;
            n2 = (n2 + 1) % n;
            bl = !bl;
            f2 = 0.0f;
        }
        this.idx = n2;
        this.dashOn = bl;
        this.phase = f2;
    }

    private void somethingTo(int n) {
        float[] fArray = this.curCurvepts;
        if (Dasher.pointCurve(fArray, n)) {
            return;
        }
        LengthIterator lengthIterator = this.li;
        float[] fArray2 = this.dash;
        int n2 = this.dashLen;
        lengthIterator.initializeIterationOnCurve(fArray, n);
        int n3 = this.idx;
        boolean bl = this.dashOn;
        float f = this.phase;
        int n4 = 0;
        float f2 = 0.0f;
        float f3 = fArray2[n3] - f;
        while (true) {
            float f4;
            float f5 = lengthIterator.next(f3);
            if (!(f4 < 1.0f)) break;
            if (f5 != 0.0f) {
                Helpers.subdivideAt((f5 - f2) / (1.0f - f2), fArray, n4, fArray, 0, n);
                f2 = f5;
                this.goTo(fArray, 2, n, bl);
                n4 = n;
            }
            n3 = (n3 + 1) % n2;
            bl = !bl;
            f = 0.0f;
            f3 = fArray2[n3];
        }
        this.goTo(fArray, n4 + 2, n, bl);
        f += lengthIterator.lastSegLen();
        if (f >= fArray2[n3]) {
            f = 0.0f;
            n3 = (n3 + 1) % n2;
            bl = !bl;
        }
        this.idx = n3;
        this.dashOn = bl;
        this.phase = f;
        lengthIterator.reset();
    }

    private void skipSomethingTo(int n) {
        float[] fArray = this.curCurvepts;
        if (Dasher.pointCurve(fArray, n)) {
            return;
        }
        LengthIterator lengthIterator = this.li;
        lengthIterator.initializeIterationOnCurve(fArray, n);
        float f = lengthIterator.totalLength();
        this.outside = true;
        this.totalSkipLen += f;
        this.needsMoveTo = true;
        this.starting = false;
    }

    private static boolean pointCurve(float[] fArray, int n) {
        for (int i = 2; i < n; ++i) {
            if (fArray[i] == fArray[i - 2]) continue;
            return false;
        }
        return true;
    }

    @Override
    public void curveTo(float f, float f2, float f3, float f4, float f5, float f6) {
        int n = this.cOutCode;
        if (this.clipRect != null) {
            int n2;
            int n3;
            int n4 = Helpers.outcode(f, f2, this.clipRect);
            int n5 = n | n4 | (n3 = Helpers.outcode(f3, f4, this.clipRect)) | (n2 = Helpers.outcode(f5, f6, this.clipRect));
            if (n5 != 0) {
                int n6 = n & n4 & n3 & n2;
                if (n6 == 0) {
                    if (this.subdivide) {
                        this.subdivide = false;
                        boolean bl = this.curveSplitter.splitCurve(this.cx0, this.cy0, f, f2, f3, f4, f5, f6, n5, this);
                        this.subdivide = true;
                        if (bl) {
                            return;
                        }
                    }
                } else {
                    this.cOutCode = n2;
                    this.skipCurveTo(f, f2, f3, f4, f5, f6);
                    return;
                }
            }
            this.cOutCode = n2;
            if (this.outside) {
                this.outside = false;
                this.skipLen();
            }
        }
        this._curveTo(f, f2, f3, f4, f5, f6);
    }

    private void _curveTo(float f, float f2, float f3, float f4, float f5, float f6) {
        float[] fArray = this.curCurvepts;
        TransformingPathConsumer2D.CurveBasicMonotonizer curveBasicMonotonizer = this.rdrCtx.monotonizer.curve(this.cx0, this.cy0, f, f2, f3, f4, f5, f6);
        int n = curveBasicMonotonizer.nbSplits;
        float[] fArray2 = curveBasicMonotonizer.middle;
        int n2 = 0;
        int n3 = 0;
        while (n2 <= n) {
            System.arraycopy(fArray2, n3, fArray, 0, 8);
            this.somethingTo(8);
            ++n2;
            n3 += 6;
        }
    }

    private void skipCurveTo(float f, float f2, float f3, float f4, float f5, float f6) {
        float[] fArray = this.curCurvepts;
        fArray[0] = this.cx0;
        fArray[1] = this.cy0;
        fArray[2] = f;
        fArray[3] = f2;
        fArray[4] = f3;
        fArray[5] = f4;
        fArray[6] = f5;
        fArray[7] = f6;
        this.skipSomethingTo(8);
        this.cx0 = f5;
        this.cy0 = f6;
    }

    @Override
    public void quadTo(float f, float f2, float f3, float f4) {
        int n = this.cOutCode;
        if (this.clipRect != null) {
            int n2;
            int n3 = Helpers.outcode(f, f2, this.clipRect);
            int n4 = n | n3 | (n2 = Helpers.outcode(f3, f4, this.clipRect));
            if (n4 != 0) {
                int n5 = n & n3 & n2;
                if (n5 == 0) {
                    if (this.subdivide) {
                        this.subdivide = false;
                        boolean bl = this.curveSplitter.splitQuad(this.cx0, this.cy0, f, f2, f3, f4, n4, this);
                        this.subdivide = true;
                        if (bl) {
                            return;
                        }
                    }
                } else {
                    this.cOutCode = n2;
                    this.skipQuadTo(f, f2, f3, f4);
                    return;
                }
            }
            this.cOutCode = n2;
            if (this.outside) {
                this.outside = false;
                this.skipLen();
            }
        }
        this._quadTo(f, f2, f3, f4);
    }

    private void _quadTo(float f, float f2, float f3, float f4) {
        float[] fArray = this.curCurvepts;
        TransformingPathConsumer2D.CurveBasicMonotonizer curveBasicMonotonizer = this.rdrCtx.monotonizer.quad(this.cx0, this.cy0, f, f2, f3, f4);
        int n = curveBasicMonotonizer.nbSplits;
        float[] fArray2 = curveBasicMonotonizer.middle;
        int n2 = 0;
        int n3 = 0;
        while (n2 <= n) {
            System.arraycopy(fArray2, n3, fArray, 0, 8);
            this.somethingTo(6);
            ++n2;
            n3 += 4;
        }
    }

    private void skipQuadTo(float f, float f2, float f3, float f4) {
        float[] fArray = this.curCurvepts;
        fArray[0] = this.cx0;
        fArray[1] = this.cy0;
        fArray[2] = f;
        fArray[3] = f2;
        fArray[4] = f3;
        fArray[5] = f4;
        this.skipSomethingTo(6);
        this.cx0 = f3;
        this.cy0 = f4;
    }

    @Override
    public void closePath() {
        if (this.cx0 != this.sx0 || this.cy0 != this.sy0) {
            this.lineTo(this.sx0, this.sy0);
        }
        if (this.firstSegidx != 0) {
            if (!this.dashOn || this.needsMoveTo) {
                this.out.moveTo(this.sx0, this.sy0);
            }
            this.emitFirstSegments();
        }
        this.moveTo(this.sx0, this.sy0);
    }

    @Override
    public void pathDone() {
        if (this.firstSegidx != 0) {
            this.out.moveTo(this.sx0, this.sy0);
            this.emitFirstSegments();
        }
        this.out.pathDone();
        this.dispose();
    }

    static final class LengthIterator {
        private final float[][] recCurveStack;
        private final boolean[] sidesRight;
        private int curveType;
        private float nextT;
        private float lenAtNextT;
        private float lastT;
        private float lenAtLastT;
        private float lenAtLastSplit;
        private float lastSegLen;
        private int recLevel;
        private boolean done = true;
        private final float[] curLeafCtrlPolyLengths = new float[3];
        private int cachedHaveLowAcceleration = -1;
        private final float[] nextRoots = new float[4];
        private final float[] flatLeafCoefCache = new float[]{0.0f, 0.0f, -1.0f, 0.0f};

        LengthIterator() {
            this.recCurveStack = new float[17][8];
            this.sidesRight = new boolean[16];
            this.nextT = Float.MAX_VALUE;
            this.lenAtNextT = Float.MAX_VALUE;
            this.lenAtLastSplit = Float.MIN_VALUE;
            this.recLevel = Integer.MIN_VALUE;
            this.lastSegLen = Float.MAX_VALUE;
        }

        void reset() {
        }

        void initializeIterationOnCurve(float[] fArray, int n) {
            System.arraycopy(fArray, 0, this.recCurveStack[0], 0, 8);
            this.curveType = n;
            this.recLevel = 0;
            this.lastT = 0.0f;
            this.lenAtLastT = 0.0f;
            this.nextT = 0.0f;
            this.lenAtNextT = 0.0f;
            this.goLeft();
            this.lenAtLastSplit = 0.0f;
            if (this.recLevel > 0) {
                this.sidesRight[0] = false;
                this.done = false;
            } else {
                this.sidesRight[0] = true;
                this.done = true;
            }
            this.lastSegLen = 0.0f;
        }

        private boolean haveLowAcceleration(float f) {
            if (this.cachedHaveLowAcceleration == -1) {
                float f2;
                float f3;
                float f4 = this.curLeafCtrlPolyLengths[0];
                float f5 = this.curLeafCtrlPolyLengths[1];
                if (!Helpers.within(f4, f5, f * f5)) {
                    this.cachedHaveLowAcceleration = 0;
                    return false;
                }
                if (!(this.curveType != 8 || Helpers.within(f5, f3 = this.curLeafCtrlPolyLengths[2], f2 = f * f3) && Helpers.within(f4, f3, f2))) {
                    this.cachedHaveLowAcceleration = 0;
                    return false;
                }
                this.cachedHaveLowAcceleration = 1;
                return true;
            }
            return this.cachedHaveLowAcceleration == 1;
        }

        float next(float f) {
            float f2 = this.lenAtLastSplit + f;
            while (this.lenAtNextT < f2) {
                if (this.done) {
                    this.lastSegLen = this.lenAtNextT - this.lenAtLastSplit;
                    return 1.0f;
                }
                this.goToNextLeaf();
            }
            this.lenAtLastSplit = f2;
            float f3 = this.lenAtNextT - this.lenAtLastT;
            float f4 = (f2 - this.lenAtLastT) / f3;
            if (!this.haveLowAcceleration(0.05f)) {
                float f5;
                int n;
                float f6;
                float f7;
                float f8;
                float[] fArray = this.flatLeafCoefCache;
                if (fArray[2] < 0.0f) {
                    f8 = this.curLeafCtrlPolyLengths[0];
                    f7 = f8 + this.curLeafCtrlPolyLengths[1];
                    if (this.curveType == 8) {
                        f6 = f7 + this.curLeafCtrlPolyLengths[2];
                        fArray[0] = 3.0f * (f8 - f7) + f6;
                        fArray[1] = 3.0f * (f7 - 2.0f * f8);
                        fArray[2] = 3.0f * f8;
                        fArray[3] = -f6;
                    } else if (this.curveType == 6) {
                        fArray[0] = 0.0f;
                        fArray[1] = f7 - 2.0f * f8;
                        fArray[2] = 2.0f * f8;
                        fArray[3] = -f7;
                    }
                }
                if ((n = Helpers.cubicRootsInAB(f8 = fArray[0], f7 = fArray[1], f6 = fArray[2], f5 = f4 * fArray[3], this.nextRoots, 0, 0.0f, 1.0f)) == 1 && !Float.isNaN(this.nextRoots[0])) {
                    f4 = this.nextRoots[0];
                }
            }
            if ((f4 = f4 * (this.nextT - this.lastT) + this.lastT) >= 1.0f) {
                f4 = 1.0f;
                this.done = true;
            }
            this.lastSegLen = f;
            return f4;
        }

        float totalLength() {
            while (!this.done) {
                this.goToNextLeaf();
            }
            this.reset();
            return this.lenAtNextT;
        }

        float lastSegLen() {
            return this.lastSegLen;
        }

        private void goToNextLeaf() {
            boolean[] blArray = this.sidesRight;
            int n = this.recLevel;
            --n;
            while (blArray[n]) {
                if (n == 0) {
                    this.recLevel = 0;
                    this.done = true;
                    return;
                }
                --n;
            }
            blArray[n] = true;
            System.arraycopy(this.recCurveStack[n++], 0, this.recCurveStack[n], 0, 8);
            this.recLevel = n;
            this.goLeft();
        }

        private void goLeft() {
            float f = this.onLeaf();
            if (f >= 0.0f) {
                this.lastT = this.nextT;
                this.lenAtLastT = this.lenAtNextT;
                this.nextT += (float)(1 << 16 - this.recLevel) * 1.5258789E-5f;
                this.lenAtNextT += f;
                this.flatLeafCoefCache[2] = -1.0f;
                this.cachedHaveLowAcceleration = -1;
            } else {
                Helpers.subdivide(this.recCurveStack[this.recLevel], this.recCurveStack[this.recLevel + 1], this.recCurveStack[this.recLevel], this.curveType);
                this.sidesRight[this.recLevel] = false;
                ++this.recLevel;
                this.goLeft();
            }
        }

        private float onLeaf() {
            float[] fArray = this.recCurveStack[this.recLevel];
            int n = this.curveType;
            float f = 0.0f;
            float f2 = fArray[0];
            float f3 = fArray[1];
            for (int i = 2; i < n; i += 2) {
                float f4 = fArray[i];
                float f5 = fArray[i + 1];
                float f6 = Helpers.linelen(f2, f3, f4, f5);
                f += f6;
                this.curLeafCtrlPolyLengths[(i >> 1) - 1] = f6;
                f2 = f4;
                f3 = f5;
            }
            float f7 = Helpers.linelen(fArray[0], fArray[1], f2, f3);
            if (f - f7 < CURVE_LEN_ERR || this.recLevel == 16) {
                return (f + f7) / 2.0f;
            }
            return -1.0f;
        }
    }
}

