/*
 * Decompiled with CFR 0.152.
 */
package com.sun.marlin;

import com.sun.marlin.ArrayCacheConst;
import com.sun.marlin.Curve;
import com.sun.marlin.FloatMath;
import com.sun.marlin.IntArrayCache;
import com.sun.marlin.MarlinAlphaConsumer;
import com.sun.marlin.MarlinConst;
import com.sun.marlin.MarlinProperties;
import com.sun.marlin.MarlinRenderer;
import com.sun.marlin.MarlinUtils;
import com.sun.marlin.MergeSort;
import com.sun.marlin.OffHeapArray;
import com.sun.marlin.RendererContext;
import sun.misc.Unsafe;

public final class Renderer
implements MarlinRenderer,
MarlinConst {
    static final boolean DISABLE_RENDER = false;
    private static final int ALL_BUT_LSB = -2;
    private static final int ERR_STEP_MAX = Integer.MAX_VALUE;
    private static final double POWER_2_TO_32 = 4.294967296E9;
    static final float SUBPIXEL_SCALE_X = SUBPIXEL_POSITIONS_X;
    static final float SUBPIXEL_SCALE_Y = SUBPIXEL_POSITIONS_Y;
    static final int SUBPIXEL_MASK_X = SUBPIXEL_POSITIONS_X - 1;
    static final int SUBPIXEL_MASK_Y = SUBPIXEL_POSITIONS_Y - 1;
    private static final float RDR_OFFSET_X = 0.5f / SUBPIXEL_SCALE_X;
    private static final float RDR_OFFSET_Y = 0.5f / SUBPIXEL_SCALE_Y;
    public static final long OFF_CURX_OR = 0L;
    public static final long OFF_ERROR = 0L + (long)OffHeapArray.SIZE_INT;
    public static final long OFF_BUMP_X = OFF_ERROR + (long)OffHeapArray.SIZE_INT;
    public static final long OFF_BUMP_ERR = OFF_BUMP_X + (long)OffHeapArray.SIZE_INT;
    public static final long OFF_NEXT = OFF_BUMP_ERR + (long)OffHeapArray.SIZE_INT;
    public static final long OFF_YMAX = OFF_NEXT + (long)OffHeapArray.SIZE_INT;
    public static final int SIZEOF_EDGE_BYTES = (int)(OFF_YMAX + (long)OffHeapArray.SIZE_INT);
    private static final float CUB_DEC_ERR_SUBPIX = MarlinProperties.getCubicDecD2() * ((float)SUBPIXEL_POSITIONS_X / 8.0f);
    private static final float CUB_INC_ERR_SUBPIX = MarlinProperties.getCubicIncD1() * ((float)SUBPIXEL_POSITIONS_X / 8.0f);
    public static final float SCALE_DY = (float)SUBPIXEL_POSITIONS_X / (float)SUBPIXEL_POSITIONS_Y;
    public static final float CUB_DEC_BND = 8.0f * CUB_DEC_ERR_SUBPIX;
    public static final float CUB_INC_BND = 8.0f * CUB_INC_ERR_SUBPIX;
    public static final int CUB_COUNT_LG = 2;
    private static final int CUB_COUNT = 4;
    private static final int CUB_COUNT_2 = 16;
    private static final int CUB_COUNT_3 = 64;
    private static final float CUB_INV_COUNT = 0.25f;
    private static final float CUB_INV_COUNT_2 = 0.0625f;
    private static final float CUB_INV_COUNT_3 = 0.015625f;
    private static final float QUAD_DEC_ERR_SUBPIX = MarlinProperties.getQuadDecD2() * ((float)SUBPIXEL_POSITIONS_X / 8.0f);
    public static final float QUAD_DEC_BND = 8.0f * QUAD_DEC_ERR_SUBPIX;
    private int[] crossings;
    private int[] aux_crossings;
    private int edgeCount;
    private int[] edgePtrs;
    private int[] aux_edgePtrs;
    private int activeEdgeMaxUsed;
    private final IntArrayCache.Reference crossings_ref;
    private final IntArrayCache.Reference edgePtrs_ref;
    private final IntArrayCache.Reference aux_crossings_ref;
    private final IntArrayCache.Reference aux_edgePtrs_ref;
    private int edgeMinY = Integer.MAX_VALUE;
    private int edgeMaxY = Integer.MIN_VALUE;
    private float edgeMinX = Float.POSITIVE_INFINITY;
    private float edgeMaxX = Float.NEGATIVE_INFINITY;
    private final OffHeapArray edges;
    private int[] edgeBuckets;
    private int[] edgeBucketCounts;
    private int buckets_minY;
    private int buckets_maxY;
    private final IntArrayCache.Reference edgeBuckets_ref;
    private final IntArrayCache.Reference edgeBucketCounts_ref;
    boolean useRLE = false;
    private int boundsMinX;
    private int boundsMinY;
    private int boundsMaxX;
    private int boundsMaxY;
    private int windingRule;
    private float x0;
    private float y0;
    private float sx0;
    private float sy0;
    final RendererContext rdrCtx;
    private final Curve curve;
    private int[] alphaLine;
    private final IntArrayCache.Reference alphaLine_ref;
    private boolean enableBlkFlags = false;
    private boolean prevUseBlkFlags = false;
    private int[] blkFlags;
    private final IntArrayCache.Reference blkFlags_ref;
    private int bbox_spminX;
    private int bbox_spmaxX;
    private int bbox_spminY;
    private int bbox_spmaxY;
    int bboxX0;
    int bboxX1;
    int bboxY0;
    int bboxY1;

    private void quadBreakIntoLinesAndAdd(float f, float f2, Curve curve, float f3, float f4) {
        int n = 1;
        float f5 = Math.abs(curve.dbx) + Math.abs(curve.dby) * SCALE_DY;
        float f6 = QUAD_DEC_BND;
        while (f5 >= f6) {
            f5 /= 4.0f;
            n <<= 1;
            if (!DO_STATS) continue;
            this.rdrCtx.stats.stat_rdr_quadBreak_dec.add(n);
        }
        int n2 = n;
        if (n > 1) {
            float f7 = 1.0f / (float)n;
            float f8 = f7 * f7;
            float f9 = curve.dbx * f8;
            float f10 = curve.dby * f8;
            float f11 = curve.bx * f8 + curve.cx * f7;
            float f12 = curve.by * f8 + curve.cy * f7;
            float f13 = f;
            float f14 = f2;
            while (--n > 0) {
                this.addLine(f, f2, f13 += f11, f14 += f12);
                f = f13;
                f2 = f14;
                f11 += f9;
                f12 += f10;
            }
        }
        this.addLine(f, f2, f3, f4);
        if (DO_STATS) {
            this.rdrCtx.stats.stat_rdr_quadBreak.add(n2);
        }
    }

    private void curveBreakIntoLinesAndAdd(float f, float f2, Curve curve, float f3, float f4) {
        int n = 4;
        float f5 = 2.0f * curve.dax * 0.015625f;
        float f6 = 2.0f * curve.day * 0.015625f;
        float f7 = f5 + curve.dbx * 0.0625f;
        float f8 = f6 + curve.dby * 0.0625f;
        float f9 = curve.ax * 0.015625f + curve.bx * 0.0625f + curve.cx * 0.25f;
        float f10 = curve.ay * 0.015625f + curve.by * 0.0625f + curve.cy * 0.25f;
        int n2 = 0;
        float f11 = CUB_DEC_BND;
        float f12 = CUB_INC_BND;
        float f13 = SCALE_DY;
        float f14 = f;
        float f15 = f2;
        while (n > 0) {
            while (n % 2 == 0 && Math.abs(f7) + Math.abs(f8) * f13 <= f12) {
                f9 = 2.0f * f9 + f7;
                f10 = 2.0f * f10 + f8;
                f7 = 4.0f * (f7 + f5);
                f8 = 4.0f * (f8 + f6);
                f5 *= 8.0f;
                f6 *= 8.0f;
                n >>= 1;
                if (!DO_STATS) continue;
                this.rdrCtx.stats.stat_rdr_curveBreak_inc.add(n);
            }
            while (Math.abs(f7) + Math.abs(f8) * f13 >= f11) {
                f7 = f7 / 4.0f - (f5 /= 8.0f);
                f8 = f8 / 4.0f - (f6 /= 8.0f);
                f9 = (f9 - f7) / 2.0f;
                f10 = (f10 - f8) / 2.0f;
                n <<= 1;
                if (!DO_STATS) continue;
                this.rdrCtx.stats.stat_rdr_curveBreak_dec.add(n);
            }
            if (--n == 0) break;
            f7 += f5;
            this.addLine(f, f2, f14 += (f9 += f7), f15 += (f10 += (f8 += f6)));
            f = f14;
            f2 = f15;
        }
        this.addLine(f, f2, f3, f4);
        if (DO_STATS) {
            this.rdrCtx.stats.stat_rdr_curveBreak.add(n2 + 1);
        }
    }

    private void addLine(float f, float f2, float f3, float f4) {
        double d;
        double d2;
        double d3;
        int n;
        int n2;
        if (DO_STATS) {
            this.rdrCtx.stats.stat_rdr_addLine.add(1);
        }
        int n3 = 1;
        if (f4 < f2) {
            n3 = 0;
            float f5 = f4;
            f4 = f2;
            f2 = f5;
            f5 = f3;
            f3 = f;
            f = f5;
        }
        if ((n2 = FloatMath.max(FloatMath.ceil_int(f2), this.boundsMinY)) >= (n = FloatMath.min(FloatMath.ceil_int(f4), this.boundsMaxY))) {
            if (DO_STATS) {
                this.rdrCtx.stats.stat_rdr_addLine_skip.add(1);
            }
            return;
        }
        if (n2 < this.edgeMinY) {
            this.edgeMinY = n2;
        }
        if (n > this.edgeMaxY) {
            this.edgeMaxY = n;
        }
        if ((d3 = ((d2 = (double)f) - (double)f3) / ((d = (double)f2) - (double)f4)) >= 0.0) {
            if (f < this.edgeMinX) {
                this.edgeMinX = f;
            }
            if (f3 > this.edgeMaxX) {
                this.edgeMaxX = f3;
            }
        } else {
            if (f3 < this.edgeMinX) {
                this.edgeMinX = f3;
            }
            if (f > this.edgeMaxX) {
                this.edgeMaxX = f;
            }
        }
        int n4 = SIZEOF_EDGE_BYTES;
        OffHeapArray offHeapArray = this.edges;
        int n5 = offHeapArray.used;
        if (offHeapArray.length - (long)n5 < (long)n4) {
            long l = ArrayCacheConst.getNewLargeSize(offHeapArray.length, n5 + n4);
            if (DO_STATS) {
                this.rdrCtx.stats.stat_rdr_edges_resizes.add(l);
            }
            offHeapArray.resize(l);
        }
        Unsafe unsafe = OffHeapArray.UNSAFE;
        long l = offHeapArray.address + (long)n5;
        double d4 = d2 + ((double)n2 - d) * d3;
        long l2 = (long)(4.294967296E9 * d4) + Integer.MAX_VALUE;
        unsafe.putInt(l, (int)(l2 >> 31) & 0xFFFFFFFE | n3);
        unsafe.putInt(l += 4L, (int)l2 >>> 1);
        long l3 = (long)(4.294967296E9 * d3);
        unsafe.putInt(l += 4L, (int)(l3 >> 31) & 0xFFFFFFFE);
        unsafe.putInt(l += 4L, (int)l3 >>> 1);
        int[] nArray = this.edgeBuckets;
        int[] nArray2 = this.edgeBucketCounts;
        int n6 = this.boundsMinY;
        int n7 = n2 - n6;
        unsafe.putInt(l += 4L, nArray[n7]);
        unsafe.putInt(l += 4L, n);
        nArray[n7] = n5;
        int n8 = n7;
        nArray2[n8] = nArray2[n8] + 2;
        int n9 = n - n6;
        nArray2[n9] = nArray2[n9] | 1;
        offHeapArray.used += n4;
    }

    Renderer(RendererContext rendererContext) {
        this.rdrCtx = rendererContext;
        this.curve = rendererContext.curve;
        this.edges = rendererContext.rdrMem.edges;
        this.edgeBuckets_ref = rendererContext.rdrMem.edgeBuckets_ref;
        this.edgeBucketCounts_ref = rendererContext.rdrMem.edgeBucketCounts_ref;
        this.edgeBuckets = this.edgeBuckets_ref.initial;
        this.edgeBucketCounts = this.edgeBucketCounts_ref.initial;
        this.alphaLine_ref = rendererContext.rdrMem.alphaLine_ref;
        this.alphaLine = this.alphaLine_ref.initial;
        this.crossings_ref = rendererContext.rdrMem.crossings_ref;
        this.aux_crossings_ref = rendererContext.rdrMem.aux_crossings_ref;
        this.edgePtrs_ref = rendererContext.rdrMem.edgePtrs_ref;
        this.aux_edgePtrs_ref = rendererContext.rdrMem.aux_edgePtrs_ref;
        this.crossings = this.crossings_ref.initial;
        this.aux_crossings = this.aux_crossings_ref.initial;
        this.edgePtrs = this.edgePtrs_ref.initial;
        this.aux_edgePtrs = this.aux_edgePtrs_ref.initial;
        this.blkFlags_ref = rendererContext.rdrMem.blkFlags_ref;
        this.blkFlags = this.blkFlags_ref.initial;
    }

    @Override
    public Renderer init(int n, int n2, int n3, int n4, int n5) {
        int n6;
        this.windingRule = n5;
        this.boundsMinX = n << SUBPIXEL_LG_POSITIONS_X;
        this.boundsMaxX = n + n3 << SUBPIXEL_LG_POSITIONS_X;
        this.boundsMinY = n2 << SUBPIXEL_LG_POSITIONS_Y;
        this.boundsMaxY = n2 + n4 << SUBPIXEL_LG_POSITIONS_Y;
        if (DO_LOG_BOUNDS) {
            MarlinUtils.logInfo("boundsXY = [" + this.boundsMinX + " ... " + this.boundsMaxX + "[ [" + this.boundsMinY + " ... " + this.boundsMaxY + "[");
        }
        if ((n6 = this.boundsMaxY - this.boundsMinY + 1) > INITIAL_BUCKET_ARRAY) {
            if (DO_STATS) {
                this.rdrCtx.stats.stat_array_renderer_edgeBuckets.add(n6);
                this.rdrCtx.stats.stat_array_renderer_edgeBucketCounts.add(n6);
            }
            this.edgeBuckets = this.edgeBuckets_ref.getArray(n6);
            this.edgeBucketCounts = this.edgeBucketCounts_ref.getArray(n6);
        }
        this.edgeMinY = Integer.MAX_VALUE;
        this.edgeMaxY = Integer.MIN_VALUE;
        this.edgeMinX = Float.POSITIVE_INFINITY;
        this.edgeMaxX = Float.NEGATIVE_INFINITY;
        this.edgeCount = 0;
        this.activeEdgeMaxUsed = 0;
        this.edges.used = 0;
        this.bboxX0 = 0;
        this.bboxX1 = 0;
        return this;
    }

    @Override
    public void dispose() {
        if (DO_STATS) {
            this.rdrCtx.stats.stat_rdr_activeEdges.add(this.activeEdgeMaxUsed);
            this.rdrCtx.stats.stat_rdr_edges.add(this.edges.used);
            this.rdrCtx.stats.stat_rdr_edges_count.add(this.edges.used / SIZEOF_EDGE_BYTES);
            this.rdrCtx.stats.hist_rdr_edges_count.add(this.edges.used / SIZEOF_EDGE_BYTES);
            this.rdrCtx.stats.totalOffHeap += this.edges.length;
        }
        this.crossings = this.crossings_ref.putArray(this.crossings);
        this.aux_crossings = this.aux_crossings_ref.putArray(this.aux_crossings);
        this.edgePtrs = this.edgePtrs_ref.putArray(this.edgePtrs);
        this.aux_edgePtrs = this.aux_edgePtrs_ref.putArray(this.aux_edgePtrs);
        this.alphaLine = this.alphaLine_ref.putArray(this.alphaLine, 0, 0);
        this.blkFlags = this.blkFlags_ref.putArray(this.blkFlags, 0, 0);
        if (this.edgeMinY != Integer.MAX_VALUE) {
            if (this.rdrCtx.dirty) {
                this.buckets_minY = 0;
                this.buckets_maxY = this.boundsMaxY - this.boundsMinY;
            }
            this.edgeBuckets = this.edgeBuckets_ref.putArray(this.edgeBuckets, this.buckets_minY, this.buckets_maxY);
            this.edgeBucketCounts = this.edgeBucketCounts_ref.putArray(this.edgeBucketCounts, this.buckets_minY, this.buckets_maxY + 1);
        } else {
            this.edgeBuckets = this.edgeBuckets_ref.putArray(this.edgeBuckets, 0, 0);
            this.edgeBucketCounts = this.edgeBucketCounts_ref.putArray(this.edgeBucketCounts, 0, 0);
        }
        if (this.edges.length != (long)INITIAL_EDGES_CAPACITY) {
            this.edges.resize(INITIAL_EDGES_CAPACITY);
        }
    }

    private static float tosubpixx(float f) {
        return SUBPIXEL_SCALE_X * f;
    }

    private static float tosubpixy(float f) {
        return SUBPIXEL_SCALE_Y * f - 0.5f;
    }

    @Override
    public void moveTo(float f, float f2) {
        this.closePath();
        float f3 = Renderer.tosubpixx(f);
        float f4 = Renderer.tosubpixy(f2);
        this.sx0 = f3;
        this.sy0 = f4;
        this.x0 = f3;
        this.y0 = f4;
    }

    @Override
    public void lineTo(float f, float f2) {
        float f3 = Renderer.tosubpixx(f);
        float f4 = Renderer.tosubpixy(f2);
        this.addLine(this.x0, this.y0, f3, f4);
        this.x0 = f3;
        this.y0 = f4;
    }

    @Override
    public void curveTo(float f, float f2, float f3, float f4, float f5, float f6) {
        float f7 = Renderer.tosubpixx(f5);
        float f8 = Renderer.tosubpixy(f6);
        this.curve.set(this.x0, this.y0, Renderer.tosubpixx(f), Renderer.tosubpixy(f2), Renderer.tosubpixx(f3), Renderer.tosubpixy(f4), f7, f8);
        this.curveBreakIntoLinesAndAdd(this.x0, this.y0, this.curve, f7, f8);
        this.x0 = f7;
        this.y0 = f8;
    }

    @Override
    public void quadTo(float f, float f2, float f3, float f4) {
        float f5 = Renderer.tosubpixx(f3);
        float f6 = Renderer.tosubpixy(f4);
        this.curve.set(this.x0, this.y0, Renderer.tosubpixx(f), Renderer.tosubpixy(f2), f5, f6);
        this.quadBreakIntoLinesAndAdd(this.x0, this.y0, this.curve, f5, f6);
        this.x0 = f5;
        this.y0 = f6;
    }

    @Override
    public void closePath() {
        if (this.x0 != this.sx0 || this.y0 != this.sy0) {
            this.addLine(this.x0, this.y0, this.sx0, this.sy0);
            this.x0 = this.sx0;
            this.y0 = this.sy0;
        }
    }

    @Override
    public void pathDone() {
        this.closePath();
        this.endRendering();
    }

    private void _endRendering(int n, int n2, MarlinAlphaConsumer marlinAlphaConsumer) {
        int n3 = this.bbox_spminX;
        int n4 = this.bbox_spmaxX;
        boolean bl = this.windingRule == 0;
        int[] nArray = this.alphaLine;
        OffHeapArray offHeapArray = this.edges;
        int[] nArray2 = this.edgeBuckets;
        int[] nArray3 = this.edgeBucketCounts;
        int[] nArray4 = this.crossings;
        int[] nArray5 = this.edgePtrs;
        int[] nArray6 = this.aux_crossings;
        int[] nArray7 = this.aux_edgePtrs;
        long l = OFF_ERROR;
        long l2 = OFF_BUMP_X;
        long l3 = OFF_BUMP_ERR;
        long l4 = OFF_NEXT;
        long l5 = OFF_YMAX;
        Unsafe unsafe = OffHeapArray.UNSAFE;
        long l6 = offHeapArray.address;
        int n5 = SUBPIXEL_LG_POSITIONS_X;
        int n6 = SUBPIXEL_LG_POSITIONS_Y;
        int n7 = SUBPIXEL_MASK_X;
        int n8 = SUBPIXEL_MASK_Y;
        int n9 = SUBPIXEL_POSITIONS_X;
        int n10 = Integer.MAX_VALUE;
        int n11 = Integer.MIN_VALUE;
        int n12 = n;
        int n13 = n12 - this.boundsMinY;
        int n14 = this.edgeCount;
        int n15 = nArray5.length;
        int n16 = nArray4.length;
        int n17 = this.activeEdgeMaxUsed;
        int n18 = 0;
        int[] nArray8 = this.blkFlags;
        int n19 = BLOCK_SIZE_LG;
        int n20 = BLOCK_SIZE;
        boolean bl2 = ENABLE_BLOCK_FLAGS_HEURISTICS && this.enableBlkFlags;
        boolean bl3 = this.prevUseBlkFlags;
        int n21 = this.rdrCtx.stroking;
        int n22 = -1;
        while (n12 < n2) {
            int n23;
            int n24;
            int n25;
            long l7;
            int n26 = nArray3[n13];
            int n27 = n14;
            if (n26 != 0) {
                if (DO_STATS) {
                    this.rdrCtx.stats.stat_rdr_activeEdges_updates.add(n14);
                }
                if ((n26 & 1) != 0) {
                    l7 = l6 + l5;
                    int n28 = 0;
                    for (n25 = 0; n25 < n14; ++n25) {
                        n24 = nArray5[n25];
                        if (unsafe.getInt(l7 + (long)n24) <= n12) continue;
                        nArray5[n28++] = n24;
                    }
                    n27 = n14 = n28;
                }
                if ((n18 = n26 >> 1) != 0) {
                    int n29;
                    if (DO_STATS) {
                        this.rdrCtx.stats.stat_rdr_activeEdges_adds.add(n18);
                        if (n18 > 10) {
                            this.rdrCtx.stats.stat_rdr_activeEdges_adds_high.add(n18);
                        }
                    }
                    if (n15 < (n29 = n14 + n18)) {
                        if (DO_STATS) {
                            this.rdrCtx.stats.stat_array_renderer_edgePtrs.add(n29);
                        }
                        this.edgePtrs = nArray5 = this.edgePtrs_ref.widenArray(nArray5, n14, n29);
                        n15 = nArray5.length;
                        this.aux_edgePtrs_ref.putArray(nArray7);
                        if (DO_STATS) {
                            this.rdrCtx.stats.stat_array_renderer_aux_edgePtrs.add(n29);
                        }
                        this.aux_edgePtrs = nArray7 = this.aux_edgePtrs_ref.getArray(ArrayCacheConst.getNewSize(n14, n29));
                    }
                    l7 = l6 + l4;
                    n24 = nArray2[n13];
                    while (n14 < n29) {
                        nArray5[n14] = n24;
                        n24 = unsafe.getInt(l7 + (long)n24);
                        ++n14;
                    }
                    if (n16 < n14) {
                        this.crossings_ref.putArray(nArray4);
                        if (DO_STATS) {
                            this.rdrCtx.stats.stat_array_renderer_crossings.add(n14);
                        }
                        this.crossings = nArray4 = this.crossings_ref.getArray(n14);
                        this.aux_crossings_ref.putArray(nArray6);
                        if (DO_STATS) {
                            this.rdrCtx.stats.stat_array_renderer_aux_crossings.add(n14);
                        }
                        this.aux_crossings = nArray6 = this.aux_crossings_ref.getArray(n14);
                        n16 = nArray4.length;
                    }
                    if (DO_STATS && n14 > n17) {
                        n17 = n14;
                    }
                }
            }
            if (n14 != 0) {
                int n30;
                int n31;
                int n32;
                int n33;
                int n34;
                int n35;
                int n36;
                int n37;
                int n38;
                int n39;
                if (n18 < 10 || n14 < 40) {
                    if (DO_STATS) {
                        this.rdrCtx.stats.hist_rdr_crossings.add(n14);
                        this.rdrCtx.stats.hist_rdr_crossings_adds.add(n18);
                    }
                    boolean bl4 = n14 >= 20;
                    n39 = Integer.MIN_VALUE;
                    for (n25 = 0; n25 < n14; ++n25) {
                        n24 = nArray5[n25];
                        l7 = l6 + (long)n24;
                        n37 = n38 = unsafe.getInt(l7);
                        n36 = unsafe.getInt(l7 + l) + unsafe.getInt(l7 + l3);
                        unsafe.putInt(l7, (n38 += unsafe.getInt(l7 + l2)) - (n36 >> 30 & 0xFFFFFFFE));
                        unsafe.putInt(l7 + l, n36 & Integer.MAX_VALUE);
                        if (DO_STATS) {
                            this.rdrCtx.stats.stat_rdr_crossings_updates.add(n14);
                        }
                        if (n37 < n39) {
                            if (DO_STATS) {
                                this.rdrCtx.stats.stat_rdr_crossings_sorts.add(n25);
                            }
                            if (bl4 && n25 >= n27) {
                                if (DO_STATS) {
                                    this.rdrCtx.stats.stat_rdr_crossings_bsearch.add(n25);
                                }
                                int n40 = 0;
                                int n41 = n25 - 1;
                                do {
                                    int n42;
                                    if (nArray4[n42 = n40 + n41 >> 1] < n37) {
                                        n40 = n42 + 1;
                                        continue;
                                    }
                                    n41 = n42 - 1;
                                } while (n40 <= n41);
                                for (n35 = n25 - 1; n35 >= n40; --n35) {
                                    nArray4[n35 + 1] = nArray4[n35];
                                    nArray5[n35 + 1] = nArray5[n35];
                                }
                                nArray4[n40] = n37;
                                nArray5[n40] = n24;
                                continue;
                            }
                            n35 = n25 - 1;
                            nArray4[n25] = nArray4[n35];
                            nArray5[n25] = nArray5[n35];
                            while (--n35 >= 0 && nArray4[n35] > n37) {
                                nArray4[n35 + 1] = nArray4[n35];
                                nArray5[n35 + 1] = nArray5[n35];
                            }
                            nArray4[n35 + 1] = n37;
                            nArray5[n35 + 1] = n24;
                            continue;
                        }
                        nArray4[n25] = n39 = n37;
                    }
                } else {
                    if (DO_STATS) {
                        this.rdrCtx.stats.stat_rdr_crossings_msorts.add(n14);
                        this.rdrCtx.stats.hist_rdr_crossings_ratio.add(1000 * n18 / n14);
                        this.rdrCtx.stats.hist_rdr_crossings_msorts.add(n14);
                        this.rdrCtx.stats.hist_rdr_crossings_msorts_adds.add(n18);
                    }
                    n39 = Integer.MIN_VALUE;
                    for (n25 = 0; n25 < n14; ++n25) {
                        n24 = nArray5[n25];
                        l7 = l6 + (long)n24;
                        n37 = n38 = unsafe.getInt(l7);
                        n36 = unsafe.getInt(l7 + l) + unsafe.getInt(l7 + l3);
                        unsafe.putInt(l7, (n38 += unsafe.getInt(l7 + l2)) - (n36 >> 30 & 0xFFFFFFFE));
                        unsafe.putInt(l7 + l, n36 & Integer.MAX_VALUE);
                        if (DO_STATS) {
                            this.rdrCtx.stats.stat_rdr_crossings_updates.add(n14);
                        }
                        if (n25 >= n27) {
                            nArray4[n25] = n37;
                            continue;
                        }
                        if (n37 < n39) {
                            if (DO_STATS) {
                                this.rdrCtx.stats.stat_rdr_crossings_sorts.add(n25);
                            }
                            n35 = n25 - 1;
                            nArray6[n25] = nArray6[n35];
                            nArray7[n25] = nArray7[n35];
                            while (--n35 >= 0 && nArray6[n35] > n37) {
                                nArray6[n35 + 1] = nArray6[n35];
                                nArray7[n35 + 1] = nArray7[n35];
                            }
                            nArray6[n35 + 1] = n37;
                            nArray7[n35 + 1] = n24;
                            continue;
                        }
                        nArray6[n25] = n39 = n37;
                        nArray7[n25] = n24;
                    }
                    MergeSort.mergeSortNoCopy(nArray4, nArray5, nArray6, nArray7, n14, n27);
                }
                n18 = 0;
                int n43 = nArray4[0];
                int n44 = n43 >> 1;
                if (n44 < n10) {
                    n10 = n44;
                }
                if ((n34 = nArray4[n14 - 1] >> 1) > n11) {
                    n11 = n34;
                }
                int n45 = n38 = n44;
                int n46 = ((n43 & 1) << 1) - 1;
                if (bl) {
                    n33 = n46;
                    for (n25 = 1; n25 < n14; ++n25) {
                        n43 = nArray4[n25];
                        n38 = n43 >> 1;
                        n46 = ((n43 & 1) << 1) - 1;
                        if ((n33 & 1) != 0) {
                            int n47 = n44 = n45 > n3 ? n45 : n3;
                            if (n38 < n4) {
                                n34 = n38;
                            } else {
                                n34 = n4;
                                n25 = n14;
                            }
                            if (n44 < n34) {
                                n32 = (n44 -= n3) >> n5;
                                n31 = (n34 -= n3) - 1 >> n5;
                                if (n32 == n31) {
                                    n23 = n34 - n44;
                                    int n48 = n32;
                                    nArray[n48] = nArray[n48] + n23;
                                    int n49 = n32 + 1;
                                    nArray[n49] = nArray[n49] - n23;
                                    if (bl3) {
                                        nArray8[n32 >> n19] = 1;
                                    }
                                } else {
                                    n23 = n44 & n7;
                                    int n50 = n32;
                                    nArray[n50] = nArray[n50] + (n9 - n23);
                                    int n51 = n32 + 1;
                                    nArray[n51] = nArray[n51] + n23;
                                    n30 = n34 >> n5;
                                    n23 = n34 & n7;
                                    int n52 = n30;
                                    nArray[n52] = nArray[n52] - (n9 - n23);
                                    int n53 = n30 + 1;
                                    nArray[n53] = nArray[n53] - n23;
                                    if (bl3) {
                                        nArray8[n32 >> n19] = 1;
                                        nArray8[n30 >> n19] = 1;
                                    }
                                }
                            }
                        }
                        n33 += n46;
                        n45 = n38;
                    }
                } else {
                    n25 = 1;
                    n33 = 0;
                    while (true) {
                        if ((n33 += n46) != 0) {
                            if (n45 > n38) {
                                n45 = n38;
                            }
                        } else {
                            int n54 = n44 = n45 > n3 ? n45 : n3;
                            if (n38 < n4) {
                                n34 = n38;
                            } else {
                                n34 = n4;
                                n25 = n14;
                            }
                            if (n44 < n34) {
                                n32 = (n44 -= n3) >> n5;
                                n31 = (n34 -= n3) - 1 >> n5;
                                if (n32 == n31) {
                                    n23 = n34 - n44;
                                    int n55 = n32;
                                    nArray[n55] = nArray[n55] + n23;
                                    int n56 = n32 + 1;
                                    nArray[n56] = nArray[n56] - n23;
                                    if (bl3) {
                                        nArray8[n32 >> n19] = 1;
                                    }
                                } else {
                                    n23 = n44 & n7;
                                    int n57 = n32;
                                    nArray[n57] = nArray[n57] + (n9 - n23);
                                    int n58 = n32 + 1;
                                    nArray[n58] = nArray[n58] + n23;
                                    n30 = n34 >> n5;
                                    n23 = n34 & n7;
                                    int n59 = n30;
                                    nArray[n59] = nArray[n59] - (n9 - n23);
                                    int n60 = n30 + 1;
                                    nArray[n60] = nArray[n60] - n23;
                                    if (bl3) {
                                        nArray8[n32 >> n19] = 1;
                                        nArray8[n30 >> n19] = 1;
                                    }
                                }
                            }
                            n45 = Integer.MAX_VALUE;
                        }
                        if (n25 == n14) break;
                        n43 = nArray4[n25];
                        n38 = n43 >> 1;
                        n46 = ((n43 & 1) << 1) - 1;
                        ++n25;
                    }
                }
            }
            if ((n12 & n8) == n8) {
                n22 = n12 >> n6;
                n10 = FloatMath.max(n10, n3) >> n5;
                if ((n11 = FloatMath.min(n11, n4) >> n5) >= n10) {
                    this.copyAARow(nArray, n22, n10, n11 + 1, bl3, marlinAlphaConsumer);
                    if (bl2) {
                        boolean bl5 = bl3 = (n11 -= n10) > n20 && n11 > (n14 >> n21) - 1 << n19;
                        if (DO_STATS) {
                            n23 = FloatMath.max(1, (n14 >> n21) - 1);
                            this.rdrCtx.stats.hist_tile_generator_encoding_dist.add(n11 / n23);
                        }
                    }
                } else {
                    marlinAlphaConsumer.clearAlphas(n22);
                }
                n10 = Integer.MAX_VALUE;
                n11 = Integer.MIN_VALUE;
            }
            ++n12;
            ++n13;
        }
        --n12;
        n12 >>= n6;
        n10 = FloatMath.max(n10, n3) >> n5;
        if ((n11 = FloatMath.min(n11, n4) >> n5) >= n10) {
            this.copyAARow(nArray, n12, n10, n11 + 1, bl3, marlinAlphaConsumer);
        } else if (n12 != n22) {
            marlinAlphaConsumer.clearAlphas(n12);
        }
        this.edgeCount = n14;
        this.prevUseBlkFlags = bl3;
        if (DO_STATS) {
            this.activeEdgeMaxUsed = n17;
        }
    }

    void endRendering() {
        int n;
        if (this.edgeMinY == Integer.MAX_VALUE) {
            return;
        }
        int n2 = FloatMath.max(FloatMath.ceil_int(this.edgeMinX - 0.5f), this.boundsMinX);
        int n3 = FloatMath.min(FloatMath.ceil_int(this.edgeMaxX - 0.5f), this.boundsMaxX);
        int n4 = this.edgeMinY;
        int n5 = this.edgeMaxY;
        this.buckets_minY = n4 - this.boundsMinY;
        this.buckets_maxY = n5 - this.boundsMinY;
        if (DO_LOG_BOUNDS) {
            MarlinUtils.logInfo("edgesXY = [" + this.edgeMinX + " ... " + this.edgeMaxX + "[ [" + this.edgeMinY + " ... " + this.edgeMaxY + "[");
            MarlinUtils.logInfo("spXY    = [" + n2 + " ... " + n3 + "[ [" + n4 + " ... " + n5 + "[");
        }
        if (n2 >= n3 || n4 >= n5) {
            return;
        }
        int n6 = n2 >> SUBPIXEL_LG_POSITIONS_X;
        int n7 = n3 + SUBPIXEL_MASK_X >> SUBPIXEL_LG_POSITIONS_X;
        int n8 = n4 >> SUBPIXEL_LG_POSITIONS_Y;
        int n9 = n5 + SUBPIXEL_MASK_Y >> SUBPIXEL_LG_POSITIONS_Y;
        this.initConsumer(n6, n8, n7, n9);
        if (ENABLE_BLOCK_FLAGS) {
            this.enableBlkFlags = this.useRLE;
            boolean bl = this.prevUseBlkFlags = this.enableBlkFlags && !ENABLE_BLOCK_FLAGS_HEURISTICS;
            if (this.enableBlkFlags && (n = (n7 - n6 >> BLOCK_SIZE_LG) + 2) > 256) {
                this.blkFlags = this.blkFlags_ref.getArray(n);
            }
        }
        this.bbox_spminX = n6 << SUBPIXEL_LG_POSITIONS_X;
        this.bbox_spmaxX = n7 << SUBPIXEL_LG_POSITIONS_X;
        this.bbox_spminY = n4;
        this.bbox_spmaxY = n5;
        if (DO_LOG_BOUNDS) {
            MarlinUtils.logInfo("pXY       = [" + n6 + " ... " + n7 + "[ [" + n8 + " ... " + n9 + "[");
            MarlinUtils.logInfo("bbox_spXY = [" + this.bbox_spminX + " ... " + this.bbox_spmaxX + "[ [" + this.bbox_spminY + " ... " + this.bbox_spmaxY + "[");
        }
        if ((n = n7 - n6 + 2) > INITIAL_AA_ARRAY) {
            if (DO_STATS) {
                this.rdrCtx.stats.stat_array_renderer_alphaline.add(n);
            }
            this.alphaLine = this.alphaLine_ref.getArray(n);
        }
    }

    void initConsumer(int n, int n2, int n3, int n4) {
        this.bboxX0 = n;
        this.bboxX1 = n3;
        this.bboxY0 = n2;
        this.bboxY1 = n4;
        int n5 = n3 - n;
        this.useRLE = FORCE_NO_RLE ? false : (FORCE_RLE ? true : n5 > RLE_MIN_WIDTH);
    }

    @Override
    public void produceAlphas(MarlinAlphaConsumer marlinAlphaConsumer) {
        marlinAlphaConsumer.setMaxAlpha(MAX_AA_ALPHA);
        if (this.enableBlkFlags && !marlinAlphaConsumer.supportBlockFlags()) {
            this.enableBlkFlags = false;
            this.prevUseBlkFlags = false;
        }
        this._endRendering(this.bbox_spminY, this.bbox_spmaxY, marlinAlphaConsumer);
    }

    void copyAARow(int[] nArray, int n, int n2, int n3, boolean bl, MarlinAlphaConsumer marlinAlphaConsumer) {
        if (DO_STATS) {
            this.rdrCtx.stats.stat_cache_rowAA.add(n3 - n2);
        }
        if (bl) {
            if (DO_STATS) {
                this.rdrCtx.stats.hist_tile_generator_encoding.add(1);
            }
            marlinAlphaConsumer.setAndClearRelativeAlphas(this.blkFlags, nArray, n, n2, n3);
        } else {
            if (DO_STATS) {
                this.rdrCtx.stats.hist_tile_generator_encoding.add(0);
            }
            marlinAlphaConsumer.setAndClearRelativeAlphas(nArray, n, n2, n3);
        }
    }

    @Override
    public int getOutpixMinX() {
        return this.bboxX0;
    }

    @Override
    public int getOutpixMaxX() {
        return this.bboxX1;
    }

    @Override
    public int getOutpixMinY() {
        return this.bboxY0;
    }

    @Override
    public int getOutpixMaxY() {
        return this.bboxY1;
    }

    @Override
    public float getOffsetX() {
        return RDR_OFFSET_X;
    }

    @Override
    public float getOffsetY() {
        return RDR_OFFSET_Y;
    }
}

