/*
 * Decompiled with CFR 0.152.
 */
package com.sun.marlin;

import com.sun.javafx.geom.PathConsumer2D;
import com.sun.marlin.Curve;
import com.sun.marlin.Helpers;
import com.sun.marlin.MarlinConst;
import com.sun.marlin.MarlinUtils;
import com.sun.marlin.RendererContext;
import com.sun.marlin.TransformingPathConsumer2D;
import java.util.Arrays;

public final class Stroker
implements PathConsumer2D,
MarlinConst {
    private static final int MOVE_TO = 0;
    private static final int DRAWING_OP_TO = 1;
    private static final int CLOSE = 2;
    private static final float ERR_JOIN = 1.0f / MIN_SUBPIXELS;
    private static final float ROUND_JOIN_THRESHOLD = ERR_JOIN * ERR_JOIN;
    private static final float C = (float)(4.0 * (Math.sqrt(2.0) - 1.0) / 3.0);
    private static final float SQRT_2 = (float)Math.sqrt(2.0);
    private PathConsumer2D out;
    private int capStyle;
    private int joinStyle;
    private float lineWidth2;
    private float invHalfLineWidth2Sq;
    private final float[] offset0 = new float[2];
    private final float[] offset1 = new float[2];
    private final float[] offset2 = new float[2];
    private final float[] miter = new float[2];
    private float miterLimitSq;
    private int prev;
    private float sx0;
    private float sy0;
    private float sdx;
    private float sdy;
    private float cx0;
    private float cy0;
    private float cdx;
    private float cdy;
    private float smx;
    private float smy;
    private float cmx;
    private float cmy;
    private final Helpers.PolyStack reverse;
    private final float[] lp = new float[8];
    private final float[] rp = new float[8];
    final RendererContext rdrCtx;
    final Curve curve;
    private float[] clipRect;
    private int cOutCode = 0;
    private int sOutCode = 0;
    private boolean opened = false;
    private boolean capStart = false;
    private boolean monotonize;
    private boolean subdivide = DO_CLIP_SUBDIVIDER;
    private final TransformingPathConsumer2D.CurveClipSplitter curveSplitter;

    Stroker(RendererContext rendererContext) {
        this.rdrCtx = rendererContext;
        this.reverse = rendererContext.stats != null ? new Helpers.PolyStack(rendererContext, rendererContext.stats.stat_str_polystack_types, rendererContext.stats.stat_str_polystack_curves, rendererContext.stats.hist_str_polystack_curves, rendererContext.stats.stat_array_str_polystack_curves, rendererContext.stats.stat_array_str_polystack_types) : new Helpers.PolyStack(rendererContext);
        this.curve = rendererContext.curve;
        this.curveSplitter = rendererContext.curveClipSplitter;
    }

    public Stroker init(PathConsumer2D pathConsumer2D, float f, int n, int n2, float f2, boolean bl) {
        this.out = pathConsumer2D;
        this.lineWidth2 = f / 2.0f;
        this.invHalfLineWidth2Sq = 1.0f / (2.0f * this.lineWidth2 * this.lineWidth2);
        this.monotonize = bl;
        this.capStyle = n;
        this.joinStyle = n2;
        float f3 = f2 * this.lineWidth2;
        this.miterLimitSq = f3 * f3;
        this.prev = 2;
        this.rdrCtx.stroking = 1;
        if (this.rdrCtx.doClip) {
            float f4 = this.lineWidth2;
            if (n == 2) {
                f4 *= SQRT_2;
            }
            if (n2 == 0 && f4 < f3) {
                f4 = f3;
            }
            float[] fArray = this.rdrCtx.clipRect;
            fArray[0] = fArray[0] - f4;
            fArray[1] = fArray[1] + f4;
            fArray[2] = fArray[2] - f4;
            fArray[3] = fArray[3] + f4;
            this.clipRect = fArray;
            if (MarlinConst.DO_LOG_CLIP) {
                MarlinUtils.logInfo("clipRect (stroker): " + Arrays.toString(this.rdrCtx.clipRect));
            }
            if (DO_CLIP_SUBDIVIDER) {
                this.subdivide = bl;
                this.curveSplitter.init();
            } else {
                this.subdivide = false;
            }
        } else {
            this.clipRect = null;
            this.cOutCode = 0;
            this.sOutCode = 0;
        }
        return this;
    }

    public void disableClipping() {
        this.clipRect = null;
        this.cOutCode = 0;
        this.sOutCode = 0;
    }

    void dispose() {
        this.reverse.dispose();
        this.opened = false;
        this.capStart = false;
    }

    private static void computeOffset(float f, float f2, float f3, float[] fArray) {
        float f4 = f * f + f2 * f2;
        if (f4 == 0.0f) {
            fArray[0] = 0.0f;
            fArray[1] = 0.0f;
        } else {
            f4 = (float)Math.sqrt(f4);
            fArray[0] = f2 * f3 / f4;
            fArray[1] = -(f * f3) / f4;
        }
    }

    private static boolean isCW(float f, float f2, float f3, float f4) {
        return f * f4 <= f2 * f3;
    }

    private void mayDrawRoundJoin(float f, float f2, float f3, float f4, float f5, float f6, boolean bl) {
        if (f3 == 0.0f && f4 == 0.0f || f5 == 0.0f && f6 == 0.0f) {
            return;
        }
        float f7 = f3 - f5;
        float f8 = f4 - f6;
        float f9 = f7 * f7 + f8 * f8;
        if (f9 < ROUND_JOIN_THRESHOLD) {
            return;
        }
        if (bl) {
            f3 = -f3;
            f4 = -f4;
            f5 = -f5;
            f6 = -f6;
        }
        this.drawRoundJoin(f, f2, f3, f4, f5, f6, bl);
    }

    private void drawRoundJoin(float f, float f2, float f3, float f4, float f5, float f6, boolean bl) {
        float f7 = f3 * f5 + f4 * f6;
        if (f7 >= 0.0f) {
            this.drawBezApproxForArc(f, f2, f3, f4, f5, f6, bl);
        } else {
            float f8 = f6 - f4;
            float f9 = f3 - f5;
            float f10 = (float)Math.sqrt(f8 * f8 + f9 * f9);
            float f11 = this.lineWidth2 / f10;
            float f12 = f8 * f11;
            float f13 = f9 * f11;
            if (bl) {
                f12 = -f12;
                f13 = -f13;
            }
            this.drawBezApproxForArc(f, f2, f3, f4, f12, f13, bl);
            this.drawBezApproxForArc(f, f2, f12, f13, f5, f6, bl);
        }
    }

    private void drawBezApproxForArc(float f, float f2, float f3, float f4, float f5, float f6, boolean bl) {
        float f7 = (f3 * f5 + f4 * f6) * this.invHalfLineWidth2Sq;
        if (f7 >= 0.5f) {
            return;
        }
        float f8 = (float)(1.3333333333333333 * Math.sqrt(0.5 - (double)f7) / (1.0 + Math.sqrt((double)f7 + 0.5)));
        if (bl) {
            f8 = -f8;
        }
        float f9 = f + f3;
        float f10 = f2 + f4;
        float f11 = f9 - f8 * f4;
        float f12 = f10 + f8 * f3;
        float f13 = f + f5;
        float f14 = f2 + f6;
        float f15 = f13 + f8 * f6;
        float f16 = f14 - f8 * f5;
        this.emitCurveTo(f9, f10, f11, f12, f15, f16, f13, f14, bl);
    }

    private void drawRoundCap(float f, float f2, float f3, float f4) {
        float f5 = C * f3;
        float f6 = C * f4;
        this.emitCurveTo(f + f3 - f6, f2 + f4 + f5, f - f4 + f5, f2 + f3 + f6, f - f4, f2 + f3);
        this.emitCurveTo(f - f4 - f5, f2 + f3 - f6, f - f3 - f6, f2 - f4 + f5, f - f3, f2 - f4);
    }

    private static void computeMiter(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float[] fArray) {
        float f9 = f3 - f;
        float f10 = f4 - f2;
        float f11 = f7 - f5;
        float f12 = f8 - f6;
        float f13 = f9 * f12 - f11 * f10;
        float f14 = f11 * (f2 - f6) - f12 * (f - f5);
        fArray[0] = f + (f14 /= f13) * f9;
        fArray[1] = f2 + f14 * f10;
    }

    private static void safeComputeMiter(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float[] fArray) {
        float f9 = f3 - f;
        float f10 = f8 - f6;
        float f11 = f7 - f5;
        float f12 = f4 - f2;
        float f13 = f9 * f10 - f11 * f12;
        if (f13 == 0.0f) {
            fArray[2] = (f + f5) / 2.0f;
            fArray[3] = (f2 + f6) / 2.0f;
        } else {
            float f14 = f11 * (f2 - f6) - f10 * (f - f5);
            fArray[2] = f + (f14 /= f13) * f9;
            fArray[3] = f2 + f14 * f12;
        }
    }

    private void drawMiter(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, boolean bl) {
        if (f9 == f7 && f10 == f8 || f == 0.0f && f2 == 0.0f || f5 == 0.0f && f6 == 0.0f) {
            return;
        }
        if (bl) {
            f7 = -f7;
            f8 = -f8;
            f9 = -f9;
            f10 = -f10;
        }
        Stroker.computeMiter(f3 - f + f7, f4 - f2 + f8, f3 + f7, f4 + f8, f5 + f3 + f9, f6 + f4 + f10, f3 + f9, f4 + f10, this.miter);
        float f11 = this.miter[0];
        float f12 = this.miter[1];
        float f13 = (f11 - f3) * (f11 - f3) + (f12 - f4) * (f12 - f4);
        if (f13 < this.miterLimitSq) {
            this.emitLineTo(f11, f12, bl);
        }
    }

    @Override
    public void moveTo(float f, float f2) {
        this._moveTo(f, f2, this.cOutCode);
        this.sx0 = f;
        this.sy0 = f2;
        this.sdx = 1.0f;
        this.sdy = 0.0f;
        this.opened = false;
        this.capStart = false;
        if (this.clipRect != null) {
            int n;
            this.cOutCode = n = Helpers.outcode(f, f2, this.clipRect);
            this.sOutCode = n;
        }
    }

    private void _moveTo(float f, float f2, int n) {
        if (this.prev == 0) {
            this.cx0 = f;
            this.cy0 = f2;
        } else {
            if (this.prev == 1) {
                this.finish(n);
            }
            this.prev = 0;
            this.cx0 = f;
            this.cy0 = f2;
            this.cdx = 1.0f;
            this.cdy = 0.0f;
        }
    }

    @Override
    public void lineTo(float f, float f2) {
        this.lineTo(f, f2, false);
    }

    private void lineTo(float f, float f2, boolean bl) {
        int n = this.cOutCode;
        if (!bl && this.clipRect != null) {
            int n2 = Helpers.outcode(f, f2, this.clipRect);
            int n3 = n | n2;
            if (n3 != 0) {
                int n4 = n & n2;
                if (n4 == 0) {
                    if (this.subdivide) {
                        this.subdivide = false;
                        boolean bl2 = this.curveSplitter.splitLine(this.cx0, this.cy0, f, f2, n3, this);
                        this.subdivide = true;
                        if (bl2) {
                            return;
                        }
                    }
                } else {
                    this.cOutCode = n2;
                    this._moveTo(f, f2, n);
                    this.opened = true;
                    return;
                }
            }
            this.cOutCode = n2;
        }
        float f3 = f - this.cx0;
        float f4 = f2 - this.cy0;
        if (f3 == 0.0f && f4 == 0.0f) {
            f3 = 1.0f;
        }
        Stroker.computeOffset(f3, f4, this.lineWidth2, this.offset0);
        float f5 = this.offset0[0];
        float f6 = this.offset0[1];
        this.drawJoin(this.cdx, this.cdy, this.cx0, this.cy0, f3, f4, this.cmx, this.cmy, f5, f6, n);
        this.emitLineTo(this.cx0 + f5, this.cy0 + f6);
        this.emitLineTo(f + f5, f2 + f6);
        this.emitLineToRev(this.cx0 - f5, this.cy0 - f6);
        this.emitLineToRev(f - f5, f2 - f6);
        this.prev = 1;
        this.cx0 = f;
        this.cy0 = f2;
        this.cdx = f3;
        this.cdy = f4;
        this.cmx = f5;
        this.cmy = f6;
    }

    @Override
    public void closePath() {
        if (this.prev != 1 && !this.opened) {
            if (this.prev == 2) {
                return;
            }
            this.emitMoveTo(this.cx0, this.cy0 - this.lineWidth2);
            this.sdx = 1.0f;
            this.sdy = 0.0f;
            this.cdx = 1.0f;
            this.cdy = 0.0f;
            this.smx = 0.0f;
            this.smy = -this.lineWidth2;
            this.cmx = 0.0f;
            this.cmy = -this.lineWidth2;
            this.finish(this.cOutCode);
            return;
        }
        if ((this.sOutCode & this.cOutCode) == 0) {
            if (this.cx0 != this.sx0 || this.cy0 != this.sy0) {
                this.lineTo(this.sx0, this.sy0, true);
            }
            this.drawJoin(this.cdx, this.cdy, this.cx0, this.cy0, this.sdx, this.sdy, this.cmx, this.cmy, this.smx, this.smy, this.sOutCode);
            this.emitLineTo(this.sx0 + this.smx, this.sy0 + this.smy);
            if (this.opened) {
                this.emitLineTo(this.sx0 - this.smx, this.sy0 - this.smy);
            } else {
                this.emitMoveTo(this.sx0 - this.smx, this.sy0 - this.smy);
            }
        }
        this.emitReverse();
        this.prev = 2;
        if (this.opened) {
            this.opened = false;
        } else {
            this.emitClose();
        }
    }

    private void emitReverse() {
        this.reverse.popAll(this.out);
    }

    @Override
    public void pathDone() {
        if (this.prev == 1) {
            this.finish(this.cOutCode);
        }
        this.out.pathDone();
        this.prev = 2;
        this.dispose();
    }

    private void finish(int n) {
        if (!this.rdrCtx.closedPath) {
            if (n == 0) {
                if (this.capStyle == 1) {
                    this.drawRoundCap(this.cx0, this.cy0, this.cmx, this.cmy);
                } else if (this.capStyle == 2) {
                    this.emitLineTo(this.cx0 - this.cmy + this.cmx, this.cy0 + this.cmx + this.cmy);
                    this.emitLineTo(this.cx0 - this.cmy - this.cmx, this.cy0 + this.cmx - this.cmy);
                }
            }
            this.emitReverse();
            if (!this.capStart) {
                this.capStart = true;
                if (this.sOutCode == 0) {
                    if (this.capStyle == 1) {
                        this.drawRoundCap(this.sx0, this.sy0, -this.smx, -this.smy);
                    } else if (this.capStyle == 2) {
                        this.emitLineTo(this.sx0 + this.smy - this.smx, this.sy0 - this.smx - this.smy);
                        this.emitLineTo(this.sx0 + this.smy + this.smx, this.sy0 - this.smx + this.smy);
                    }
                }
            }
        } else {
            this.emitReverse();
        }
        this.emitClose();
    }

    private void emitMoveTo(float f, float f2) {
        this.out.moveTo(f, f2);
    }

    private void emitLineTo(float f, float f2) {
        this.out.lineTo(f, f2);
    }

    private void emitLineToRev(float f, float f2) {
        this.reverse.pushLine(f, f2);
    }

    private void emitLineTo(float f, float f2, boolean bl) {
        if (bl) {
            this.emitLineToRev(f, f2);
        } else {
            this.emitLineTo(f, f2);
        }
    }

    private void emitQuadTo(float f, float f2, float f3, float f4) {
        this.out.quadTo(f, f2, f3, f4);
    }

    private void emitQuadToRev(float f, float f2, float f3, float f4) {
        this.reverse.pushQuad(f, f2, f3, f4);
    }

    private void emitCurveTo(float f, float f2, float f3, float f4, float f5, float f6) {
        this.out.curveTo(f, f2, f3, f4, f5, f6);
    }

    private void emitCurveToRev(float f, float f2, float f3, float f4, float f5, float f6) {
        this.reverse.pushCubic(f, f2, f3, f4, f5, f6);
    }

    private void emitCurveTo(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, boolean bl) {
        if (bl) {
            this.reverse.pushCubic(f, f2, f3, f4, f5, f6);
        } else {
            this.out.curveTo(f3, f4, f5, f6, f7, f8);
        }
    }

    private void emitClose() {
        this.out.closePath();
    }

    private void drawJoin(float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, float f9, float f10, int n) {
        if (this.prev != 1) {
            this.emitMoveTo(f3 + f9, f4 + f10);
            if (!this.opened) {
                this.sdx = f5;
                this.sdy = f6;
                this.smx = f9;
                this.smy = f10;
            }
        } else {
            boolean bl = Stroker.isCW(f, f2, f5, f6);
            if (n == 0) {
                if (this.joinStyle == 0) {
                    this.drawMiter(f, f2, f3, f4, f5, f6, f7, f8, f9, f10, bl);
                } else if (this.joinStyle == 1) {
                    this.mayDrawRoundJoin(f3, f4, f7, f8, f9, f10, bl);
                }
            }
            this.emitLineTo(f3, f4, !bl);
        }
        this.prev = 1;
    }

    private static boolean within(float f, float f2, float f3, float f4, float f5) {
        assert (f5 > 0.0f) : "";
        return Helpers.within(f, f3, f5) && Helpers.within(f2, f4, f5);
    }

    private void getLineOffsets(float f, float f2, float f3, float f4, float[] fArray, float[] fArray2) {
        Stroker.computeOffset(f3 - f, f4 - f2, this.lineWidth2, this.offset0);
        float f5 = this.offset0[0];
        float f6 = this.offset0[1];
        fArray[0] = f + f5;
        fArray[1] = f2 + f6;
        fArray[2] = f3 + f5;
        fArray[3] = f4 + f6;
        fArray2[0] = f - f5;
        fArray2[1] = f2 - f6;
        fArray2[2] = f3 - f5;
        fArray2[3] = f4 - f6;
    }

    private int computeOffsetCubic(float[] fArray, int n, float[] fArray2, float[] fArray3) {
        float f = fArray[n];
        float f2 = fArray[n + 1];
        float f3 = fArray[n + 2];
        float f4 = fArray[n + 3];
        float f5 = fArray[n + 4];
        float f6 = fArray[n + 5];
        float f7 = fArray[n + 6];
        float f8 = fArray[n + 7];
        float f9 = f7 - f5;
        float f10 = f8 - f6;
        float f11 = f3 - f;
        float f12 = f4 - f2;
        boolean bl = Stroker.within(f, f2, f3, f4, 6.0f * Math.ulp(f4));
        boolean bl2 = Stroker.within(f5, f6, f7, f8, 6.0f * Math.ulp(f8));
        if (bl && bl2) {
            this.getLineOffsets(f, f2, f7, f8, fArray2, fArray3);
            return 4;
        }
        if (bl) {
            f11 = f5 - f;
            f12 = f6 - f2;
        } else if (bl2) {
            f9 = f7 - f3;
            f10 = f8 - f4;
        }
        float f13 = f11 * f9 + f12 * f10;
        f13 *= f13;
        float f14 = f11 * f11 + f12 * f12;
        float f15 = f9 * f9 + f10 * f10;
        if (Helpers.within(f13, f14 * f15, 4.0f * Math.ulp(f13))) {
            this.getLineOffsets(f, f2, f7, f8, fArray2, fArray3);
            return 4;
        }
        float f16 = (f + 3.0f * (f3 + f5) + f7) / 8.0f;
        float f17 = (f2 + 3.0f * (f4 + f6) + f8) / 8.0f;
        float f18 = f5 + f7 - f - f3;
        float f19 = f6 + f8 - f2 - f4;
        Stroker.computeOffset(f11, f12, this.lineWidth2, this.offset0);
        Stroker.computeOffset(f18, f19, this.lineWidth2, this.offset1);
        Stroker.computeOffset(f9, f10, this.lineWidth2, this.offset2);
        float f20 = f + this.offset0[0];
        float f21 = f2 + this.offset0[1];
        float f22 = f16 + this.offset1[0];
        float f23 = f17 + this.offset1[1];
        float f24 = f7 + this.offset2[0];
        float f25 = f8 + this.offset2[1];
        float f26 = 4.0f / (3.0f * (f11 * f10 - f12 * f9));
        float f27 = 2.0f * f22 - f20 - f24;
        float f28 = 2.0f * f23 - f21 - f25;
        float f29 = f26 * (f10 * f27 - f9 * f28);
        float f30 = f26 * (f11 * f28 - f12 * f27);
        float f31 = f20 + f29 * f11;
        float f32 = f21 + f29 * f12;
        float f33 = f24 + f30 * f9;
        float f34 = f25 + f30 * f10;
        fArray2[0] = f20;
        fArray2[1] = f21;
        fArray2[2] = f31;
        fArray2[3] = f32;
        fArray2[4] = f33;
        fArray2[5] = f34;
        fArray2[6] = f24;
        fArray2[7] = f25;
        f20 = f - this.offset0[0];
        f21 = f2 - this.offset0[1];
        f24 = f7 - this.offset2[0];
        f25 = f8 - this.offset2[1];
        f27 = 2.0f * (f22 -= 2.0f * this.offset1[0]) - f20 - f24;
        f28 = 2.0f * (f23 -= 2.0f * this.offset1[1]) - f21 - f25;
        f29 = f26 * (f10 * f27 - f9 * f28);
        f30 = f26 * (f11 * f28 - f12 * f27);
        f31 = f20 + f29 * f11;
        f32 = f21 + f29 * f12;
        f33 = f24 + f30 * f9;
        f34 = f25 + f30 * f10;
        fArray3[0] = f20;
        fArray3[1] = f21;
        fArray3[2] = f31;
        fArray3[3] = f32;
        fArray3[4] = f33;
        fArray3[5] = f34;
        fArray3[6] = f24;
        fArray3[7] = f25;
        return 8;
    }

    private int computeOffsetQuad(float[] fArray, int n, float[] fArray2, float[] fArray3) {
        float f = fArray[n];
        float f2 = fArray[n + 1];
        float f3 = fArray[n + 2];
        float f4 = fArray[n + 3];
        float f5 = fArray[n + 4];
        float f6 = fArray[n + 5];
        float f7 = f5 - f3;
        float f8 = f6 - f4;
        float f9 = f3 - f;
        float f10 = f4 - f2;
        boolean bl = Stroker.within(f, f2, f3, f4, 6.0f * Math.ulp(f4));
        boolean bl2 = Stroker.within(f3, f4, f5, f6, 6.0f * Math.ulp(f6));
        if (bl || bl2) {
            this.getLineOffsets(f, f2, f5, f6, fArray2, fArray3);
            return 4;
        }
        float f11 = f9 * f7 + f10 * f8;
        float f12 = f9 * f9 + f10 * f10;
        float f13 = f7 * f7 + f8 * f8;
        if (Helpers.within(f11 *= f11, f12 * f13, 4.0f * Math.ulp(f11))) {
            this.getLineOffsets(f, f2, f5, f6, fArray2, fArray3);
            return 4;
        }
        Stroker.computeOffset(f9, f10, this.lineWidth2, this.offset0);
        Stroker.computeOffset(f7, f8, this.lineWidth2, this.offset1);
        float f14 = f + this.offset0[0];
        float f15 = f2 + this.offset0[1];
        float f16 = f5 + this.offset1[0];
        float f17 = f6 + this.offset1[1];
        Stroker.safeComputeMiter(f14, f15, f14 + f9, f15 + f10, f16, f17, f16 - f7, f17 - f8, fArray2);
        fArray2[0] = f14;
        fArray2[1] = f15;
        fArray2[4] = f16;
        fArray2[5] = f17;
        f14 = f - this.offset0[0];
        f15 = f2 - this.offset0[1];
        f16 = f5 - this.offset1[0];
        f17 = f6 - this.offset1[1];
        Stroker.safeComputeMiter(f14, f15, f14 + f9, f15 + f10, f16, f17, f16 - f7, f17 - f8, fArray3);
        fArray3[0] = f14;
        fArray3[1] = f15;
        fArray3[4] = f16;
        fArray3[5] = f17;
        return 6;
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
                    this._moveTo(f5, f6, n);
                    this.opened = true;
                    return;
                }
            }
            this.cOutCode = n2;
        }
        this._curveTo(f, f2, f3, f4, f5, f6, n);
    }

    private void _curveTo(float f, float f2, float f3, float f4, float f5, float f6, int n) {
        float[] fArray;
        Object object;
        float f7;
        float f8 = f - this.cx0;
        float f9 = f2 - this.cy0;
        float f10 = f5 - f3;
        float f11 = f6 - f4;
        if (f8 == 0.0f && f9 == 0.0f) {
            f8 = f3 - this.cx0;
            f9 = f4 - this.cy0;
            if (f8 == 0.0f && f9 == 0.0f) {
                f8 = f5 - this.cx0;
                f9 = f6 - this.cy0;
            }
        }
        if (f10 == 0.0f && f11 == 0.0f) {
            f10 = f5 - f;
            f11 = f6 - f2;
            if (f10 == 0.0f && f11 == 0.0f) {
                f10 = f5 - this.cx0;
                f11 = f6 - this.cy0;
            }
        }
        if (f8 == 0.0f && f9 == 0.0f) {
            if (this.clipRect != null) {
                this.cOutCode = n;
            }
            this.lineTo(this.cx0, this.cy0);
            return;
        }
        if (Math.abs(f8) < 0.1f && Math.abs(f9) < 0.1f) {
            f7 = (float)Math.sqrt(f8 * f8 + f9 * f9);
            f8 /= f7;
            f9 /= f7;
        }
        if (Math.abs(f10) < 0.1f && Math.abs(f11) < 0.1f) {
            f7 = (float)Math.sqrt(f10 * f10 + f11 * f11);
            f10 /= f7;
            f11 /= f7;
        }
        Stroker.computeOffset(f8, f9, this.lineWidth2, this.offset0);
        this.drawJoin(this.cdx, this.cdy, this.cx0, this.cy0, f8, f9, this.cmx, this.cmy, this.offset0[0], this.offset0[1], n);
        int n2 = 0;
        float[] fArray2 = this.lp;
        if (this.monotonize) {
            object = this.rdrCtx.monotonizer.curve(this.cx0, this.cy0, f, f2, f3, f4, f5, f6);
            n2 = ((TransformingPathConsumer2D.CurveBasicMonotonizer)object).nbSplits;
            fArray = ((TransformingPathConsumer2D.CurveBasicMonotonizer)object).middle;
        } else {
            fArray = fArray2;
            fArray[0] = this.cx0;
            fArray[1] = this.cy0;
            fArray[2] = f;
            fArray[3] = f2;
            fArray[4] = f3;
            fArray[5] = f4;
            fArray[6] = f5;
            fArray[7] = f6;
        }
        object = this.rp;
        int n3 = 0;
        int n4 = 0;
        int n5 = 0;
        while (n4 <= n2) {
            n3 = this.computeOffsetCubic(fArray, n5, fArray2, (float[])object);
            this.emitLineTo(fArray2[0], fArray2[1]);
            switch (n3) {
                case 8: {
                    this.emitCurveTo(fArray2[2], fArray2[3], fArray2[4], fArray2[5], fArray2[6], fArray2[7]);
                    this.emitCurveToRev((float)object[0], (float)object[1], (float)object[2], (float)object[3], (float)object[4], (float)object[5]);
                    break;
                }
                case 4: {
                    this.emitLineTo(fArray2[2], fArray2[3]);
                    this.emitLineToRev((float)object[0], (float)object[1]);
                    break;
                }
            }
            this.emitLineToRev((float)object[n3 - 2], (float)object[n3 - 1]);
            ++n4;
            n5 += 6;
        }
        this.prev = 1;
        this.cx0 = f5;
        this.cy0 = f6;
        this.cdx = f10;
        this.cdy = f11;
        this.cmx = (fArray2[n3 - 2] - object[n3 - 2]) / 2.0f;
        this.cmy = (fArray2[n3 - 1] - object[n3 - 1]) / 2.0f;
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
                    this._moveTo(f3, f4, n);
                    this.opened = true;
                    return;
                }
            }
            this.cOutCode = n2;
        }
        this._quadTo(f, f2, f3, f4, n);
    }

    private void _quadTo(float f, float f2, float f3, float f4, int n) {
        float[] fArray;
        Object object;
        float f5;
        float f6 = f - this.cx0;
        float f7 = f2 - this.cy0;
        float f8 = f3 - f;
        float f9 = f4 - f2;
        if (f6 == 0.0f && f7 == 0.0f || f8 == 0.0f && f9 == 0.0f) {
            f6 = f8 = f3 - this.cx0;
            f7 = f9 = f4 - this.cy0;
        }
        if (f6 == 0.0f && f7 == 0.0f) {
            if (this.clipRect != null) {
                this.cOutCode = n;
            }
            this.lineTo(this.cx0, this.cy0);
            return;
        }
        if (Math.abs(f6) < 0.1f && Math.abs(f7) < 0.1f) {
            f5 = (float)Math.sqrt(f6 * f6 + f7 * f7);
            f6 /= f5;
            f7 /= f5;
        }
        if (Math.abs(f8) < 0.1f && Math.abs(f9) < 0.1f) {
            f5 = (float)Math.sqrt(f8 * f8 + f9 * f9);
            f8 /= f5;
            f9 /= f5;
        }
        Stroker.computeOffset(f6, f7, this.lineWidth2, this.offset0);
        this.drawJoin(this.cdx, this.cdy, this.cx0, this.cy0, f6, f7, this.cmx, this.cmy, this.offset0[0], this.offset0[1], n);
        int n2 = 0;
        float[] fArray2 = this.lp;
        if (this.monotonize) {
            object = this.rdrCtx.monotonizer.quad(this.cx0, this.cy0, f, f2, f3, f4);
            n2 = ((TransformingPathConsumer2D.CurveBasicMonotonizer)object).nbSplits;
            fArray = ((TransformingPathConsumer2D.CurveBasicMonotonizer)object).middle;
        } else {
            fArray = fArray2;
            fArray[0] = this.cx0;
            fArray[1] = this.cy0;
            fArray[2] = f;
            fArray[3] = f2;
            fArray[4] = f3;
            fArray[5] = f4;
        }
        object = this.rp;
        int n3 = 0;
        int n4 = 0;
        int n5 = 0;
        while (n4 <= n2) {
            n3 = this.computeOffsetQuad(fArray, n5, fArray2, (float[])object);
            this.emitLineTo(fArray2[0], fArray2[1]);
            switch (n3) {
                case 6: {
                    this.emitQuadTo(fArray2[2], fArray2[3], fArray2[4], fArray2[5]);
                    this.emitQuadToRev((float)object[0], (float)object[1], (float)object[2], (float)object[3]);
                    break;
                }
                case 4: {
                    this.emitLineTo(fArray2[2], fArray2[3]);
                    this.emitLineToRev((float)object[0], (float)object[1]);
                    break;
                }
            }
            this.emitLineToRev((float)object[n3 - 2], (float)object[n3 - 1]);
            ++n4;
            n5 += 4;
        }
        this.prev = 1;
        this.cx0 = f3;
        this.cy0 = f4;
        this.cdx = f8;
        this.cdy = f9;
        this.cmx = (fArray2[n3 - 2] - object[n3 - 2]) / 2.0f;
        this.cmy = (fArray2[n3 - 1] - object[n3 - 1]) / 2.0f;
    }
}

