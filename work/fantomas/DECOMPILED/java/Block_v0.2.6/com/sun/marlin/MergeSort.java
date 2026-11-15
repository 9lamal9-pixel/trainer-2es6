/*
 * Decompiled with CFR 0.152.
 */
package com.sun.marlin;

final class MergeSort {
    public static final int INSERTION_SORT_THRESHOLD = 14;

    static void mergeSortNoCopy(int[] nArray, int[] nArray2, int[] nArray3, int[] nArray4, int n, int n2) {
        if (n > nArray.length || n > nArray2.length || n > nArray3.length || n > nArray4.length) {
            throw new ArrayIndexOutOfBoundsException("bad arguments: toIndex=" + n);
        }
        MergeSort.mergeSort(nArray, nArray2, nArray, nArray3, nArray2, nArray4, n2, n);
        if (n2 == 0 || nArray3[n2 - 1] <= nArray3[n2]) {
            System.arraycopy(nArray3, 0, nArray, 0, n);
            System.arraycopy(nArray4, 0, nArray2, 0, n);
            return;
        }
        int n3 = 0;
        int n4 = n2;
        for (int i = 0; i < n; ++i) {
            if (n4 >= n || n3 < n2 && nArray3[n3] <= nArray3[n4]) {
                nArray[i] = nArray3[n3];
                nArray2[i] = nArray4[n3];
                ++n3;
                continue;
            }
            nArray[i] = nArray3[n4];
            nArray2[i] = nArray4[n4];
            ++n4;
        }
    }

    private static void mergeSort(int[] nArray, int[] nArray2, int[] nArray3, int[] nArray4, int[] nArray5, int[] nArray6, int n, int n2) {
        int n3 = n2 - n;
        if (n3 <= 14) {
            nArray4[n] = nArray[n];
            nArray6[n] = nArray2[n];
            int n4 = n + 1;
            int n5 = n;
            while (n4 < n2) {
                int n6 = nArray[n4];
                int n7 = nArray2[n4];
                while (nArray4[n5] > n6) {
                    nArray4[n5 + 1] = nArray4[n5];
                    nArray6[n5 + 1] = nArray6[n5];
                    if (n5-- != n) continue;
                }
                nArray4[n5 + 1] = n6;
                nArray6[n5 + 1] = n7;
                n5 = n4++;
            }
            return;
        }
        int n8 = n + n2 >> 1;
        MergeSort.mergeSort(nArray, nArray2, nArray4, nArray3, nArray6, nArray5, n, n8);
        MergeSort.mergeSort(nArray, nArray2, nArray4, nArray3, nArray6, nArray5, n8, n2);
        if (nArray3[n2 - 1] <= nArray3[n]) {
            int n9 = n8 - n;
            int n10 = n2 - n8;
            int n11 = n9 != n10 ? 1 : 0;
            System.arraycopy(nArray3, n, nArray4, n8 + n11, n9);
            System.arraycopy(nArray3, n8, nArray4, n, n10);
            System.arraycopy(nArray5, n, nArray6, n8 + n11, n9);
            System.arraycopy(nArray5, n8, nArray6, n, n10);
            return;
        }
        if (nArray3[n8 - 1] <= nArray3[n8]) {
            System.arraycopy(nArray3, n, nArray4, n, n3);
            System.arraycopy(nArray5, n, nArray6, n, n3);
            return;
        }
        int n12 = n;
        int n13 = n8;
        for (int i = n; i < n2; ++i) {
            if (n13 >= n2 || n12 < n8 && nArray3[n12] <= nArray3[n13]) {
                nArray4[i] = nArray3[n12];
                nArray6[i] = nArray5[n12];
                ++n12;
                continue;
            }
            nArray4[i] = nArray3[n13];
            nArray6[i] = nArray5[n13];
            ++n13;
        }
    }

    private MergeSort() {
    }
}

