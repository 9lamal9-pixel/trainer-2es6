/*
 * Decompiled with CFR 0.152.
 */
package com.sun.marlin;

import com.sun.marlin.ArrayCacheConst;
import com.sun.marlin.MarlinConst;
import com.sun.marlin.MarlinUtils;
import java.lang.ref.WeakReference;
import java.util.Arrays;

public final class IntArrayCache
implements MarlinConst {
    final boolean clean;
    private final int bucketCapacity;
    private WeakReference<Bucket[]> refBuckets = null;
    final ArrayCacheConst.CacheStats stats;

    IntArrayCache(boolean bl, int n) {
        this.clean = bl;
        this.bucketCapacity = n;
        this.stats = DO_STATS ? new ArrayCacheConst.CacheStats(IntArrayCache.getLogPrefix(bl) + "IntArrayCache") : null;
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

    static int[] createArray(int n) {
        return new int[n];
    }

    static void fill(int[] nArray, int n, int n2, int n3) {
        Arrays.fill(nArray, n, n2, n3);
        if (DO_CHECKS) {
            IntArrayCache.check(nArray, n, n2, n3);
        }
    }

    public static void check(int[] nArray, int n, int n2, int n3) {
        if (DO_CHECKS) {
            for (int i = 0; i < nArray.length; ++i) {
                if (nArray[i] == n3) continue;
                MarlinUtils.logException("Invalid value at: " + i + " = " + nArray[i] + " from: " + n + " to: " + n2 + "\n" + Arrays.toString(nArray), new Throwable());
                Arrays.fill(nArray, n3);
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
        private final int[][] arrays;
        private final ArrayCacheConst.BucketStats stats;

        Bucket(boolean bl, int n, int n2, ArrayCacheConst.BucketStats bucketStats) {
            this.arraySize = n;
            this.clean = bl;
            this.stats = bucketStats;
            this.arrays = new int[n2][];
        }

        int[] getArray() {
            if (MarlinConst.DO_STATS) {
                ++this.stats.getOp;
            }
            if (this.tail != 0) {
                int[] nArray = this.arrays[--this.tail];
                this.arrays[this.tail] = null;
                return nArray;
            }
            if (MarlinConst.DO_STATS) {
                ++this.stats.createOp;
            }
            return IntArrayCache.createArray(this.arraySize);
        }

        void putArray(int[] nArray) {
            if (MarlinConst.DO_CHECKS && nArray.length != this.arraySize) {
                MarlinUtils.logInfo(IntArrayCache.getLogPrefix(this.clean) + "IntArrayCache: bad length = " + nArray.length);
                return;
            }
            if (MarlinConst.DO_STATS) {
                ++this.stats.returnOp;
            }
            if (this.arrays.length > this.tail) {
                this.arrays[this.tail++] = nArray;
                if (MarlinConst.DO_STATS) {
                    this.stats.updateMaxSize(this.tail);
                }
            } else if (MarlinConst.DO_CHECKS) {
                MarlinUtils.logInfo(IntArrayCache.getLogPrefix(this.clean) + "IntArrayCache: array capacity exceeded !");
            }
        }
    }

    static final class Reference {
        final int[] initial;
        private final boolean clean;
        private final IntArrayCache cache;

        Reference(IntArrayCache intArrayCache, int n) {
            this.cache = intArrayCache;
            this.clean = intArrayCache.clean;
            this.initial = IntArrayCache.createArray(n);
            if (MarlinConst.DO_STATS) {
                intArrayCache.stats.totalInitial += (long)n;
            }
        }

        int[] getArray(int n) {
            if (n <= ArrayCacheConst.MAX_ARRAY_SIZE) {
                return this.cache.getCacheBucket(n).getArray();
            }
            if (MarlinConst.DO_STATS) {
                ++this.cache.stats.oversize;
            }
            if (MarlinConst.DO_LOG_OVERSIZE) {
                MarlinUtils.logInfo(IntArrayCache.getLogPrefix(this.clean) + "IntArrayCache: getArray[oversize]: length=\t" + n);
            }
            return IntArrayCache.createArray(n);
        }

        int[] widenArray(int[] nArray, int n, int n2) {
            int n3 = nArray.length;
            if (MarlinConst.DO_CHECKS && n3 >= n2) {
                return nArray;
            }
            if (MarlinConst.DO_STATS) {
                ++this.cache.stats.resize;
            }
            int[] nArray2 = this.getArray(ArrayCacheConst.getNewSize(n, n2));
            System.arraycopy(nArray, 0, nArray2, 0, n);
            this.putArray(nArray, 0, n);
            if (MarlinConst.DO_LOG_WIDEN_ARRAY) {
                MarlinUtils.logInfo(IntArrayCache.getLogPrefix(this.clean) + "IntArrayCache: widenArray[" + nArray2.length + "]: usedSize=\t" + n + "\tlength=\t" + n3 + "\tneeded length=\t" + n2);
            }
            return nArray2;
        }

        int[] putArray(int[] nArray) {
            return this.putArray(nArray, 0, nArray.length);
        }

        int[] putArray(int[] nArray, int n, int n2) {
            if (nArray.length <= ArrayCacheConst.MAX_ARRAY_SIZE) {
                if (this.clean && n2 != 0) {
                    IntArrayCache.fill(nArray, n, n2, 0);
                }
                if (nArray != this.initial) {
                    this.cache.getCacheBucket(nArray.length).putArray(nArray);
                }
            }
            return this.initial;
        }
    }
}

