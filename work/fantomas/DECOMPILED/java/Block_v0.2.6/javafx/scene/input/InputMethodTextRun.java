/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.input;

import java.io.Serializable;
import javafx.beans.NamedArg;
import javafx.scene.input.InputMethodHighlight;

public class InputMethodTextRun
implements Serializable {
    private final String text;
    private final InputMethodHighlight highlight;

    public InputMethodTextRun(@NamedArg(value="text") String string, @NamedArg(value="highlight") InputMethodHighlight inputMethodHighlight) {
        this.text = string;
        this.highlight = inputMethodHighlight;
    }

    public final String getText() {
        return this.text;
    }

    public final InputMethodHighlight getHighlight() {
        return this.highlight;
    }

    public String toString() {
        return "InputMethodTextRun text [" + this.getText() + "], highlight [" + this.getHighlight() + "]";
    }
}

