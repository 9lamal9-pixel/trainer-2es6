/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.text;

public enum FontPosture {
    REGULAR("", "regular"),
    ITALIC("italic");

    private final String[] names;

    private FontPosture(String ... stringArray) {
        this.names = stringArray;
    }

    public static FontPosture findByName(String string) {
        if (string == null) {
            return null;
        }
        for (FontPosture fontPosture : FontPosture.values()) {
            for (String string2 : fontPosture.names) {
                if (!string2.equalsIgnoreCase(string)) continue;
                return fontPosture;
            }
        }
        return null;
    }
}

