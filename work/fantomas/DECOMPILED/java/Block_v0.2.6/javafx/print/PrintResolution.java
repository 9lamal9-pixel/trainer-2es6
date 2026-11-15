/*
 * Decompiled with CFR 0.152.
 */
package javafx.print;

public final class PrintResolution {
    private int cfRes;
    private int fRes;

    PrintResolution(int n, int n2) throws IllegalArgumentException {
        if (n <= 0 || n2 <= 0) {
            throw new IllegalArgumentException("Values must be positive");
        }
        this.cfRes = n;
        this.fRes = n2;
    }

    public int getCrossFeedResolution() {
        return this.cfRes;
    }

    public int getFeedResolution() {
        return this.fRes;
    }

    public boolean equals(Object object) {
        try {
            PrintResolution printResolution = (PrintResolution)object;
            return this.cfRes == printResolution.cfRes && this.fRes == printResolution.fRes;
        }
        catch (Exception exception) {
            return false;
        }
    }

    public int hashCode() {
        return this.cfRes << 16 | this.fRes;
    }

    public String toString() {
        return "Feed res=" + this.fRes + "dpi. Cross Feed res=" + this.cfRes + "dpi.";
    }
}

