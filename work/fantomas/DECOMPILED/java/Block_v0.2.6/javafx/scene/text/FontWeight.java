/*
 * Decompiled with CFR 0.152.
 */
package javafx.scene.text;

public enum FontWeight {
    THIN(100, "Thin"),
    EXTRA_LIGHT(200, "Extra Light", "Ultra Light"),
    LIGHT(300, "Light"),
    NORMAL(400, "Normal", "Regular"),
    MEDIUM(500, "Medium"),
    SEMI_BOLD(600, "Semi Bold", "Demi Bold"),
    BOLD(700, "Bold"),
    EXTRA_BOLD(800, "Extra Bold", "Ultra Bold"),
    BLACK(900, "Black", "Heavy");

    private final int weight;
    private final String[] names;

    private FontWeight(int n2, String ... stringArray) {
        this.weight = n2;
        this.names = stringArray;
    }

    public int getWeight() {
        return this.weight;
    }

    public static FontWeight findByName(String string) {
        if (string == null) {
            return null;
        }
        for (FontWeight fontWeight : FontWeight.values()) {
            for (String string2 : fontWeight.names) {
                if (!string2.equalsIgnoreCase(string)) continue;
                return fontWeight;
            }
        }
        return null;
    }

    public static FontWeight findByWeight(int n) {
        if (n <= 150) {
            return THIN;
        }
        if (n <= 250) {
            return EXTRA_LIGHT;
        }
        if (n < 350) {
            return LIGHT;
        }
        if (n <= 450) {
            return NORMAL;
        }
        if (n <= 550) {
            return MEDIUM;
        }
        if (n < 650) {
            return SEMI_BOLD;
        }
        if (n <= 750) {
            return BOLD;
        }
        if (n <= 850) {
            return EXTRA_BOLD;
        }
        return BLACK;
    }
}

