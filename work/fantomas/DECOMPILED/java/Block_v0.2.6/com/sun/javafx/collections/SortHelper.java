/*
 * Decompiled with CFR 0.152.
 */
package com.sun.javafx.collections;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

public class SortHelper {
    private int[] permutation;
    private int[] reversePermutation;
    private static final int INSERTIONSORT_THRESHOLD = 7;

    public <T extends Comparable<? super T>> int[] sort(List<T> list) {
        Comparable[] comparableArray = (Comparable[])Array.newInstance(Comparable.class, list.size());
        try {
            comparableArray = list.toArray(comparableArray);
        }
        catch (ArrayStoreException arrayStoreException) {
            throw new ClassCastException();
        }
        int[] nArray = this.sort(comparableArray);
        ListIterator<T> listIterator = list.listIterator();
        for (int i = 0; i < comparableArray.length; ++i) {
            listIterator.next();
            listIterator.set(comparableArray[i]);
        }
        return nArray;
    }

    public <T> int[] sort(List<T> list, Comparator<? super T> comparator) {
        Object[] objectArray = list.toArray();
        int[] nArray = this.sort(objectArray, comparator);
        ListIterator<T> listIterator = list.listIterator();
        for (int i = 0; i < objectArray.length; ++i) {
            listIterator.next();
            listIterator.set(objectArray[i]);
        }
        return nArray;
    }

    public <T extends Comparable<? super T>> int[] sort(T[] TArray) {
        return this.sort(TArray, null);
    }

    public <T> int[] sort(T[] TArray, Comparator<? super T> comparator) {
        Object[] objectArray = (Object[])TArray.clone();
        int[] nArray = this.initPermutation(TArray.length);
        if (comparator == null) {
            this.mergeSort(objectArray, TArray, 0, TArray.length, 0);
        } else {
            this.mergeSort(objectArray, TArray, 0, TArray.length, 0, comparator);
        }
        this.reversePermutation = null;
        this.permutation = null;
        return nArray;
    }

    public <T> int[] sort(T[] TArray, int n, int n2, Comparator<? super T> comparator) {
        SortHelper.rangeCheck(TArray.length, n, n2);
        Object[] objectArray = SortHelper.copyOfRange(TArray, n, n2);
        int[] nArray = this.initPermutation(TArray.length);
        if (comparator == null) {
            this.mergeSort(objectArray, TArray, n, n2, -n);
        } else {
            this.mergeSort(objectArray, TArray, n, n2, -n, comparator);
        }
        this.reversePermutation = null;
        this.permutation = null;
        return Arrays.copyOfRange(nArray, n, n2);
    }

    public int[] sort(int[] nArray, int n, int n2) {
        SortHelper.rangeCheck(nArray.length, n, n2);
        int[] nArray2 = SortHelper.copyOfRange(nArray, n, n2);
        int[] nArray3 = this.initPermutation(nArray.length);
        this.mergeSort(nArray2, nArray, n, n2, -n);
        this.reversePermutation = null;
        this.permutation = null;
        return Arrays.copyOfRange(nArray3, n, n2);
    }

    private static void rangeCheck(int n, int n2, int n3) {
        if (n2 > n3) {
            throw new IllegalArgumentException("fromIndex(" + n2 + ") > toIndex(" + n3 + ")");
        }
        if (n2 < 0) {
            throw new ArrayIndexOutOfBoundsException(n2);
        }
        if (n3 > n) {
            throw new ArrayIndexOutOfBoundsException(n3);
        }
    }

    private static int[] copyOfRange(int[] nArray, int n, int n2) {
        int n3 = n2 - n;
        if (n3 < 0) {
            throw new IllegalArgumentException(n + " > " + n2);
        }
        int[] nArray2 = new int[n3];
        System.arraycopy(nArray, n, nArray2, 0, Math.min(nArray.length - n, n3));
        return nArray2;
    }

    private static <T> T[] copyOfRange(T[] TArray, int n, int n2) {
        return SortHelper.copyOfRange(TArray, n, n2, TArray.getClass());
    }

    private static <T, U> T[] copyOfRange(U[] UArray, int n, int n2, Class<? extends T[]> clazz) {
        int n3 = n2 - n;
        if (n3 < 0) {
            throw new IllegalArgumentException(n + " > " + n2);
        }
        Object[] objectArray = clazz == Object[].class ? new Object[n3] : (Object[])Array.newInstance(clazz.getComponentType(), n3);
        System.arraycopy(UArray, n, objectArray, 0, Math.min(UArray.length - n, n3));
        return objectArray;
    }

    private void mergeSort(int[] nArray, int[] nArray2, int n, int n2, int n3) {
        int n4;
        int n5 = n2 - n;
        if (n5 < 7) {
            for (int i = n; i < n2; ++i) {
                for (int j = i; j > n && Integer.valueOf(nArray2[j - 1]).compareTo(nArray2[j]) > 0; --j) {
                    this.swap(nArray2, j, j - 1);
                }
            }
            return;
        }
        int n6 = n;
        int n7 = n2;
        int n8 = (n += n3) + (n2 += n3) >>> 1;
        this.mergeSort(nArray2, nArray, n, n8, -n3);
        this.mergeSort(nArray2, nArray, n8, n2, -n3);
        if (Integer.valueOf(nArray[n8 - 1]).compareTo(nArray[n8]) <= 0) {
            System.arraycopy(nArray, n, nArray2, n6, n5);
            return;
        }
        int n9 = n;
        int n10 = n8;
        for (n4 = n6; n4 < n7; ++n4) {
            if (n10 >= n2 || n9 < n8 && Integer.valueOf(nArray[n9]).compareTo(nArray[n10]) <= 0) {
                nArray2[n4] = nArray[n9];
                this.permutation[this.reversePermutation[n9++]] = n4;
                continue;
            }
            nArray2[n4] = nArray[n10];
            this.permutation[this.reversePermutation[n10++]] = n4;
        }
        for (n4 = n6; n4 < n7; ++n4) {
            this.reversePermutation[this.permutation[n4]] = n4;
        }
    }

    private void mergeSort(Object[] objectArray, Object[] objectArray2, int n, int n2, int n3) {
        int n4;
        int n5 = n2 - n;
        if (n5 < 7) {
            for (int i = n; i < n2; ++i) {
                for (int j = i; j > n && ((Comparable)objectArray2[j - 1]).compareTo(objectArray2[j]) > 0; --j) {
                    this.swap(objectArray2, j, j - 1);
                }
            }
            return;
        }
        int n6 = n;
        int n7 = n2;
        int n8 = (n += n3) + (n2 += n3) >>> 1;
        this.mergeSort(objectArray2, objectArray, n, n8, -n3);
        this.mergeSort(objectArray2, objectArray, n8, n2, -n3);
        if (((Comparable)objectArray[n8 - 1]).compareTo(objectArray[n8]) <= 0) {
            System.arraycopy(objectArray, n, objectArray2, n6, n5);
            return;
        }
        int n9 = n;
        int n10 = n8;
        for (n4 = n6; n4 < n7; ++n4) {
            if (n10 >= n2 || n9 < n8 && ((Comparable)objectArray[n9]).compareTo(objectArray[n10]) <= 0) {
                objectArray2[n4] = objectArray[n9];
                this.permutation[this.reversePermutation[n9++]] = n4;
                continue;
            }
            objectArray2[n4] = objectArray[n10];
            this.permutation[this.reversePermutation[n10++]] = n4;
        }
        for (n4 = n6; n4 < n7; ++n4) {
            this.reversePermutation[this.permutation[n4]] = n4;
        }
    }

    private void mergeSort(Object[] objectArray, Object[] objectArray2, int n, int n2, int n3, Comparator comparator) {
        int n4;
        int n5 = n2 - n;
        if (n5 < 7) {
            for (int i = n; i < n2; ++i) {
                for (int j = i; j > n && comparator.compare(objectArray2[j - 1], objectArray2[j]) > 0; --j) {
                    this.swap(objectArray2, j, j - 1);
                }
            }
            return;
        }
        int n6 = n;
        int n7 = n2;
        int n8 = (n += n3) + (n2 += n3) >>> 1;
        this.mergeSort(objectArray2, objectArray, n, n8, -n3, comparator);
        this.mergeSort(objectArray2, objectArray, n8, n2, -n3, comparator);
        if (comparator.compare(objectArray[n8 - 1], objectArray[n8]) <= 0) {
            System.arraycopy(objectArray, n, objectArray2, n6, n5);
            return;
        }
        int n9 = n;
        int n10 = n8;
        for (n4 = n6; n4 < n7; ++n4) {
            if (n10 >= n2 || n9 < n8 && comparator.compare(objectArray[n9], objectArray[n10]) <= 0) {
                objectArray2[n4] = objectArray[n9];
                this.permutation[this.reversePermutation[n9++]] = n4;
                continue;
            }
            objectArray2[n4] = objectArray[n10];
            this.permutation[this.reversePermutation[n10++]] = n4;
        }
        for (n4 = n6; n4 < n7; ++n4) {
            this.reversePermutation[this.permutation[n4]] = n4;
        }
    }

    private void swap(int[] nArray, int n, int n2) {
        int n3 = nArray[n];
        nArray[n] = nArray[n2];
        nArray[n2] = n3;
        this.permutation[this.reversePermutation[n]] = n2;
        this.permutation[this.reversePermutation[n2]] = n;
        int n4 = this.reversePermutation[n];
        this.reversePermutation[n] = this.reversePermutation[n2];
        this.reversePermutation[n2] = n4;
    }

    private void swap(Object[] objectArray, int n, int n2) {
        Object object = objectArray[n];
        objectArray[n] = objectArray[n2];
        objectArray[n2] = object;
        this.permutation[this.reversePermutation[n]] = n2;
        this.permutation[this.reversePermutation[n2]] = n;
        int n3 = this.reversePermutation[n];
        this.reversePermutation[n] = this.reversePermutation[n2];
        this.reversePermutation[n2] = n3;
    }

    private int[] initPermutation(int n) {
        this.permutation = new int[n];
        this.reversePermutation = new int[n];
        for (int i = 0; i < n; ++i) {
            this.permutation[i] = this.reversePermutation[i] = i;
        }
        return this.permutation;
    }
}

