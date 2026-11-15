/*
 * Decompiled with CFR 0.152.
 */
package javafx.css;

public final class StyleClass {
    private final String styleClassName;
    private final int index;

    public StyleClass(String string, int n) {
        this.styleClassName = string;
        this.index = n;
    }

    public String getStyleClassName() {
        return this.styleClassName;
    }

    public String toString() {
        return this.styleClassName;
    }

    public int getIndex() {
        return this.index;
    }
}

