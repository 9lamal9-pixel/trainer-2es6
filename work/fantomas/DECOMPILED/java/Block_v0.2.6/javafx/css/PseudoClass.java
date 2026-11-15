/*
 * Decompiled with CFR 0.152.
 */
package javafx.css;

import com.sun.javafx.css.PseudoClassState;

public abstract class PseudoClass {
    public static PseudoClass getPseudoClass(String string) {
        return PseudoClassState.getPseudoClass(string);
    }

    public abstract String getPseudoClassName();
}

