/*
 * Decompiled with CFR 0.152.
 */
package com.sun.marlin;

import com.sun.marlin.ArrayCacheConst;
import com.sun.marlin.MarlinConst;
import com.sun.marlin.MarlinUtils;
import java.lang.ref.WeakReference;
import java.util.Arrays;

public final class ByteArrayCache
implements MarlinConst {
    final boolean clean;
    private final int bucketCapacity;
    private WeakReference<Bucket[]> refBuckets = null;
    final ArrayCacheConst.CacheStats stats;

    ByteArrayCache(boolean bl, int n) {
        this.clean = bl;
        this.bucketCapacity = n;
        this.stats = DO_STATS ? new ArrayCacheConst.CacheStats(ByteArrayCache.getLogPrefix(bl) + "ByteArrayCache") : null;
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

    static byte[] createArray(int n) {
        return new byte[n];
    }

    static void fill(byte[] byArray, int n, int n2, byte by) {
        Arrays.fill(byArray, n, n2, by);
        if (DO_CHECKS) {
            ByteArrayCache.check(byArray, n, n2, by);
        }
    }

    public static void check(byte[] byArray, int n, int n2, byte by) {
        if (DO_CHECKS) {
            for (int i = 0; i < byArray.length; ++i) {
                if (byArray[i] == by) continue;
                MarlinUtils.logException("Invalid value at: " + i + " = " + byArray[i] + " from: " + n + " to: " + n2 + "\n" + Arrays.toString(byArray), new Throwable());
                Arrays.fill(byArray, by);
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
        private final byte[][] arrays;
        private final ArrayCacheConst.BucketStats stats;

        Bucket(boolean bl, int n, int n2, ArrayCacheConst.BucketStats bucketStats) {
            this.arraySize = n;
            this.clean = bl;
            this.stats = bucketStats;
            this.arrays = new byte[n2][];
        }

        byte[] getArray() {
            if (MarlinConst.DO_STATS) {
                ++this.stats.getOp;
            }
            if (this.tail != 0) {
                byte[] byArray = this.arrays[--this.tail];
                this.arrays[this.tail] = null;
                return byArray;
            }
            if (MarlinConst.DO_STATS) {
                ++this.stats.createOp;
            }
            return ByteArrayCache.createArray(this.arraySize);
        }

        void putArray(byte[] byArray) {
            if (MarlinConst.DO_CHECKS && byArray.length != this.arraySize) {
                MarlinUtils.logInfo(ByteArrayCache.getLogPrefix(this.clean) + "ByteArrayCache: bad length = " + byArray.length);
                return;
            }
            if (MarlinConst.DO_STATS) {
                ++this.stats.returnOp;
            }
            if (this.arrays.length > this.tail) {
                this.arrays[this.tail++] = byArray;
                if (MarlinConst.DO_STATS) {
                    this.stats.updateMaxSize(this.tail);
                }
            } else if (MarlinConst.DO_CHECKS) {
                MarlinUtils.logInfo(ByteArrayCache.getLogPrefix(this.clean) + "ByteArrayCache: array capacity exceeded !");
            }
        }
    }

    static final class Reference {
        final byte[] initial;
        private final boolean clean;
        private final ByteArrayCache cache;

        Reference(ByteArrayCache byteArrayCache, int n) {
            this.cache = byteArrayCache;
            this.clean = byteArrayCache.clean;
            this.initial = ByteArrayCache.createArray(n);
            if (MarlinConst.DO_STATS) {
                byteArrayCache.stats.totalInitial += (long)n;
            }
        }

        byte[] getArray(int n) {
            if (n <= ArrayCacheConst.MAX_ARRAY_SIZE) {
                return this.cache.getCacheBucket(n).getArray();
            }
            if (MarlinConst.DO_STATS) {
                ++this.cache.stats.oversize;
            }
            if (MarlinConst.DO_LOG_OVERSIZE) {
                MarlinUtils.logInfo(ByteArrayCache.getLogPrefix(this.clean) + "ByteArrayCache: getArray[oversize]: length=\t" + n);
            }
            return ByteArrayCache.createArray(n);
        }

        byte[] widenArray(byte[] byArray, int n, int n2) {
            int n3 = byArray.length;
            if (MarlinConst.DO_CHECKS && n3 >= n2) {
                return byArray;
            }
            if (MarlinConst.DO_STATS) {
                ++this.cache.stats.resize;
            }
            byte[] byArray2 = this.getArray(ArrayCacheConst.getNewSize(n, n2));
            System.arraycopy(byArray, 0, byArray2, 0, n);
            this.putArray(byArray, 0, n);
            if (MarlinConst.DO_LOG_WIDEN_ARRAY) {
                MarlinUtils.logInfo(ByteArrayCache.getLogPrefix(this.clean) + "ByteArrayCache: widenArray[" + byArray2.length + "]: usedSize=\t" + n + "\tlength=\t" + n3 + "\tneeded length=\t" + n2);
            }
            return byArray2;
        }

        byte[] putArray(byte[] byArray) {
            return this.putArray(byArray, 0, byArray.length);
        }

        byte[] putArray(byte[] byArray, int n, int n2) {
            if (byArray.length <= ArrayCacheConst.MAX_ARRAY_SIZE) {
                if (this.clean && n2 != 0) {
                    ByteArrayCache.fill(byArray, n, n2, (byte)0);
                }
                if (byArray != this.initial) {
                    this.cache.getCacheBucket(byArray.length).putArray(byArray);
                }
            }
            return this.initial;
        }
    }
}

