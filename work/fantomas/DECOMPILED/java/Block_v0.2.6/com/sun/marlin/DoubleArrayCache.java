/*
 * Decompiled with CFR 0.152.
 */
package com.sun.marlin;

import com.sun.marlin.ArrayCacheConst;
import com.sun.marlin.MarlinConst;
import com.sun.marlin.MarlinUtils;
import java.lang.ref.WeakReference;
import java.util.Arrays;

public final class DoubleArrayCache
implements MarlinConst {
    final boolean clean;
    private final int bucketCapacity;
    private WeakReference<Bucket[]> refBuckets = null;
    final ArrayCacheConst.CacheStats stats;

    DoubleArrayCache(boolean bl, int n) {
        this.clean = bl;
        this.bucketCapacity = n;
        this.stats = DO_STATS ? new ArrayCacheConst.CacheStats(DoubleArrayCache.getLogPrefix(bl) + "DoubleArrayCache") : null;
    }

    Bucket getCacheBucket(int n) {
        int n2 = ArrayCacheConst.getBucket(n);
        return this.getBuckets()[n2];
    }

    private Bucket[] getBuckets() {
        Bucket[] bucketArray;
        Bucket[] bucketArray2 = bucketArray = this.refBuckets != null ? (Bucket[])this.refBuckets.get() : null;
        if (bucketArray == null) {
            bucketArray = new Bucket[8];
            for (int i = 0; i < 8; ++i) {
                bucketArray[i] = new Bucket(this.clean, ArrayCacheConst.ARRAY_SIZES[i], this.bucketCapacity, DO_STATS ? this.stats.bucketStats[i] : null);
            }
            this.refBuckets = new WeakReference<Bucket[]>(bucketArray);
        }
        return bucketArray;
    }

    Reference createRef(int n) {
        return new Reference(this, n);
    }

    static double[] createArray(int n) {
        return new double[n];
    }

    static void fill(double[] dArray, int n, int n2, double d) {
        Arrays.fill(dArray, n, n2, d);
        if (DO_CHECKS) {
            DoubleArrayCache.check(dArray, n, n2, d);
        }
    }

    public static void check(double[] dArray, int n, int n2, double d) {
        if (DO_CHECKS) {
            for (int i = 0; i < dArray.length; ++i) {
                if (dArray[i] == d) continue;
                MarlinUtils.logException("Invalid value at: " + i + " = " + dArray[i] + " from: " + n + " to: " + n2 + "\n" + Arrays.toString(dArray), new Throwable());
                Arrays.fill(dArray, d);
                return;
            }
        }
    }

    static String getLogPrefix(boolean bl) {
        return bl ? "Clean" : "Dirty";
    }

    static final class Bucket {
        private int tail = 0;
        private final int arraySize;
        private final boolean clean;
        private final double[][] arrays;
        private final ArrayCacheConst.BucketStats stats;

        Bucket(boolean bl, int n, int n2, ArrayCacheConst.BucketStats bucketStats) {
            this.arraySize = n;
            this.clean = bl;
            this.stats = bucketStats;
            this.arrays = new double[n2][];
        }

        double[] getArray() {
            if (MarlinConst.DO_STATS) {
                ++this.stats.getOp;
            }
            if (this.tail != 0) {
                double[] dArray = this.arrays[--this.tail];
                this.arrays[this.tail] = null;
                return dArray;
            }
            if (MarlinConst.DO_STATS) {
                ++this.stats.createOp;
            }
            return DoubleArrayCache.createArray(this.arraySize);
        }

        void putArray(double[] dArray) {
            if (MarlinConst.DO_CHECKS && dArray.length != this.arraySize) {
                MarlinUtils.logInfo(DoubleArrayCache.getLogPrefix(this.clean) + "DoubleArrayCache: bad length = " + dArray.length);
                return;
            }
            if (MarlinConst.DO_STATS) {
                ++this.stats.returnOp;
            }
            if (this.arrays.length > this.tail) {
                this.arrays[this.tail++] = dArray;
                if (MarlinConst.DO_STATS) {
                    this.stats.updateMaxSize(this.tail);
                }
            } else if (MarlinConst.DO_CHECKS) {
                MarlinUtils.logInfo(DoubleArrayCache.getLogPrefix(this.clean) + "DoubleArrayCache: array capacity exceeded !");
            }
        }
    }

    static final class Reference {
        final double[] initial;
        private final boolean clean;
        private final DoubleArrayCache cache;

        Reference(DoubleArrayCache doubleArrayCache, int n) {
            this.cache = doubleArrayCache;
            this.clean = doubleArrayCache.clean;
            this.initial = DoubleArrayCache.createArray(n);
            if (MarlinConst.DO_STATS) {
                doubleArrayCache.stats.totalInitial += (long)n;
            }
        }

        double[] getArray(int n) {
            if (n <= ArrayCacheConst.MAX_ARRAY_SIZE) {
                return this.cache.getCacheBucket(n).getArray();
            }
            if (MarlinConst.DO_STATS) {
                ++this.cache.stats.oversize;
            }
            if (MarlinConst.DO_LOG_OVERSIZE) {
                MarlinUtils.logInfo(DoubleArrayCache.getLogPrefix(this.clean) + "DoubleArrayCache: getArray[oversize]: length=\t" + n);
            }
            return DoubleArrayCache.createArray(n);
        }

        double[] widenArray(double[] dArray, int n, int n2) {
            int n3 = dArray.length;
            if (MarlinConst.DO_CHECKS && n3 >= n2) {
                return dArray;
            }
            if (MarlinConst.DO_STATS) {
                ++this.cache.stats.resize;
            }
            double[] dArray2 = this.getArray(ArrayCacheConst.getNewSize(n, n2));
            System.arraycopy(dArray, 0, dArray2, 0, n);
            this.putArray(dArray, 0, n);
            if (MarlinConst.DO_LOG_WIDEN_ARRAY) {
                MarlinUtils.logInfo(DoubleArrayCache.getLogPrefix(this.clean) + "DoubleArrayCache: widenArray[" + dArray2.length + "]: usedSize=\t" + n + "\tlength=\t" + n3 + "\tneeded length=\t" + n2);
            }
            return dArray2;
        }

        double[] putArray(double[] dArray) {
            return this.putArray(dArray, 0, dArray.length);
        }

        double[] putArray(double[] dArray, int n, int n2) {
            if (dArray.length <= ArrayCacheConst.MAX_ARRAY_SIZE) {
                if (this.clean && n2 != 0) {
                    DoubleArrayCache.fill(dArray, n, n2, 0.0);
                }
                if (dArray != this.initial) {
                    this.cache.getCacheBucket(dArray.length).putArray(dArray);
                }
            }
            return this.initial;
        }
    }
}

