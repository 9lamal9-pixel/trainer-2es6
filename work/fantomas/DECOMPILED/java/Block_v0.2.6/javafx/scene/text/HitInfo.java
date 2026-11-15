/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.text;

import java.text.BreakIterator;

public class HitInfo {
    private int charIndex;
    private boolean leading;
    private int insertionIndex;
    private String text;
    private static BreakIterator charIterator = BreakIterator.getCharacterInstance();

    HitInfo(int n, int n2, boolean bl, String string) {
        this.charIndex = n;
        this.leading = bl;
        this.insertionIndex = n2;
        this.text = string;
    }

    public int getCharIndex() {
        return this.charIndex;
    }

    public boolean isLeading() {
        return this.leading;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public int getInsertionIndex() {
        if (this.insertionIndex == -1) {
            this.insertionIndex = this.charIndex;
            if (!this.leading) {
                if (this.text != null) {
                    int n;
                    BreakIterator breakIterator = charIterator;
                    synchronized (breakIterator) {
                        charIterator.setText(this.text);
                        n = charIterator.following(this.insertionIndex);
                    }
                    this.insertionIndex = n == -1 ? ++this.insertionIndex : n;
                } else {
                    ++this.insertionIndex;
                }
            }
        }
        return this.insertionIndex;
    }

    public String toString() {
        return "charIndex: " + this.charIndex + ", isLeading: " + this.leading + ", insertionIndex: " + this.getInsertionIndex();
    }
}

