/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.control;

import javafx.beans.NamedArg;

public final class IndexRange {
    private int start;
    private int end;
    public static final String VALUE_DELIMITER = ",";

    public IndexRange(@NamedArg(value="start") int n, @NamedArg(value="end") int n2) {
        if (n2 < n) {
            throw new IllegalArgumentException();
        }
        this.start = n;
        this.end = n2;
    }

    public IndexRange(@NamedArg(value="range") IndexRange indexRange) {
        this.start = indexRange.start;
        this.end = indexRange.end;
    }

    public int getStart() {
        return this.start;
    }

    public int getEnd() {
        return this.end;
    }

    public int getLength() {
        return this.end - this.start;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (object instanceof IndexRange) {
            IndexRange indexRange = (IndexRange)object;
            return this.start == indexRange.start && this.end == indexRange.end;
        }
        return false;
    }

    public int hashCode() {
        return 31 * this.start + this.end;
    }

    public String toString() {
        return this.start + ", " + this.end;
    }

    public static IndexRange normalize(int n, int n2) {
        return new IndexRange(Math.min(n, n2), Math.max(n, n2));
    }

    public static IndexRange valueOf(String string) {
        if (string == null) {
            throw new IllegalArgumentException();
        }
        String[] stringArray = string.split(VALUE_DELIMITER);
        if (stringArray.length != 2) {
            throw new IllegalArgumentException();
        }
        int n = Integer.parseInt(stringArray[0].trim());
        int n2 = Integer.parseInt(stringArray[1].trim());
        return IndexRange.normalize(n, n2);
    }
}

